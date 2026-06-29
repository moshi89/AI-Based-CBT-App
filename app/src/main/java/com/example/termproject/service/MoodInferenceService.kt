package com.example.termproject.service

import android.content.Context
import android.util.Log
import com.example.termproject.service.ModelDownloadService
import com.google.gson.stream.JsonReader
import org.json.JSONArray
import org.tensorflow.lite.Interpreter
import java.io.File
import java.io.FileInputStream
import java.io.FileReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

/**
 * XLM-RoBERTa 기반 다국어 감정 분류 서비스.
 *
 * 모델: cbt_multilingual.tflite
 *   - 입력 1: input_ids     shape [1, 128] int32
 *   - 입력 2: attention_mask shape [1, 128] int32
 *   - 출력:  logits          shape [1, 5]  float32
 *
 * 토크나이저: tokenizer.json (HuggingFace Unigram/SentencePiece)
 *   - 어휘 크기: 250,002
 *   - 특수 토큰: <s>=0, <pad>=1, </s>=2, <unk>=3
 *   - 포맷: <s> + tokens + </s>
 *
 * 출력 클래스 (index):
 *   0=Sadness/슬픔  1=Anxiety/불안  2=Anger/분노  3=Joy/기쁨  4=Calm/평온
 */
class MoodInferenceService(private val context: Context) {

    private val interpreter: Interpreter
    private val vocab: List<String>          // index → token
    private val tokenToId: Map<String, Int>  // token → index

    private val maxSeqLen = 128
    private val BOS_ID = 0   // <s>
    private val EOS_ID = 2   // </s>
    private val PAD_ID = 1   // <pad>
    private val UNK_ID = 3   // <unk>

    private val MODEL_NAME  = "cbt_multilingual.tflite"
    private val TOKENIZER_NAME = "tokenizer.json"

    // 출력 레이블: 영어/한국어 공통 (다국어 단일 모델)
    private val LABELS = listOf("슬픔", "불안", "분노", "기쁨", "평온")

    init {
        // ── 모델 로드 (내부 저장소에서) ─────────────────────────────────
        val modelFile = ModelDownloadService.getLocalModelFile(context)
        Log.d(TAG, "Loading model from: ${modelFile.absolutePath} (${modelFile.length()} bytes)")
        require(modelFile.exists()) {
            "Model file not found at ${modelFile.absolutePath}. Download it first."
        }

        val modelBuffer = FileInputStream(modelFile).use { fis ->
            fis.channel.map(FileChannel.MapMode.READ_ONLY, 0, modelFile.length())
        }
        val options = Interpreter.Options().apply { numThreads = 2 }
        interpreter = Interpreter(modelBuffer, options)
        Log.d(TAG, "Model loaded. Input count=${interpreter.inputTensorCount}")

        // ── 토크나이저 로드 (Gson JsonReader를 사용한 스트리밍 파싱) ───────────────────
        val tokenizerFile = ModelDownloadService.getLocalTokenizerFile(context)
        Log.d(TAG, "Loading tokenizer with streaming from: ${tokenizerFile.absolutePath}")
        require(tokenizerFile.exists()) {
            "Tokenizer file not found at ${tokenizerFile.absolutePath}. Download it first."
        }

        val mutableVocab = ArrayList<String>()
        val mutableMap  = HashMap<String, Int>()
        
        JsonReader(FileReader(tokenizerFile)).use { reader ->
            reader.beginObject()
            while (reader.hasNext()) {
                val name = reader.nextName()
                if (name == "model") {
                    reader.beginObject()
                    while (reader.hasNext()) {
                        val modelName = reader.nextName()
                        if (modelName == "vocab") {
                            reader.beginArray()
                            var index = 0
                            while (reader.hasNext()) {
                                reader.beginArray()
                                if (reader.hasNext()) {
                                    val token = reader.nextString()
                                    mutableVocab.add(token)
                                    mutableMap[token] = index
                                    index++
                                }
                                while (reader.hasNext()) {
                                    reader.skipValue()
                                }
                                reader.endArray()
                            }
                            reader.endArray()
                        } else {
                            reader.skipValue()
                        }
                    }
                    reader.endObject()
                } else {
                    reader.skipValue()
                }
            }
            reader.endObject()
        }
        vocab = mutableVocab
        tokenToId = mutableMap
        Log.d(TAG, "Tokenizer loaded successfully with streaming. Vocab size=${vocab.size}")
    }

    // ──────────────────────────────────────────────────────────────────
    // 공개 API
    // ──────────────────────────────────────────────────────────────────

    /**
     * 텍스트를 입력받아 감정 레이블(한국어)을 반환합니다.
     */
    fun predictMood(text: String): String {
        if (text.isBlank()) return LABELS[4] // 빈 입력 → 평온

        val tokenIds = tokenize(text)
        val scores = runInference(tokenIds)

        Log.d(TAG, "Scores: ${scores.joinToString { "%.3f".format(it) }}")

        val maxIndex = scores.indices.maxByOrNull { scores[it] } ?: 4
        val label = LABELS.getOrElse(maxIndex) { "평온" }
        Log.d(TAG, "Predicted: $label (index=$maxIndex)")
        return label
    }

    /**
     * 점수 배열(소프트맥스 전 로짓)도 함께 반환하는 버전.
     */
    fun predictMoodWithScores(text: String): Pair<String, FloatArray> {
        if (text.isBlank()) return LABELS[4] to FloatArray(5)

        val tokenIds = tokenize(text)
        val scores = runInference(tokenIds)
        val maxIndex = scores.indices.maxByOrNull { scores[it] } ?: 4
        return LABELS.getOrElse(maxIndex) { "평온" } to scores
    }

    // ──────────────────────────────────────────────────────────────────
    // 내부 구현
    // ──────────────────────────────────────────────────────────────────

    /**
     * Unigram (XLM-RoBERTa 스타일) 토크나이저.
     * ▁ (U+2581) = Metaspace prefix (단어 시작을 의미)
     * 반환: <s> + token_ids + </s>, maxSeqLen 길이로 잘리거나 PAD 채움
     */
    private fun tokenize(text: String): IntArray {
        val normalized = text.trim()

        // SentencePiece Unigram: 최장 일치 그리디 토크나이징
        val words = normalized.split(Regex("\\s+"))
        val pieceIds = mutableListOf<Int>()

        for ((wi, word) in words.withIndex()) {
            // 단어 앞에 ▁ 붙이기 (Metaspace prepend_scheme=always)
            val prefixed = if (wi == 0) "▁$word" else "▁$word"
            val pieces = unigramEncode(prefixed)
            pieceIds.addAll(pieces)
        }

        // <s> + pieces + </s>, 최대 maxSeqLen 기준으로 자르기
        val contentMax = maxSeqLen - 2  // BOS, EOS 자리 제외
        val truncated = if (pieceIds.size > contentMax) pieceIds.subList(0, contentMax) else pieceIds

        val result = IntArray(maxSeqLen) { PAD_ID }
        result[0] = BOS_ID
        for (i in truncated.indices) result[i + 1] = truncated[i]
        val eosPos = truncated.size + 1
        if (eosPos < maxSeqLen) result[eosPos] = EOS_ID

        return result
    }

    /**
     * Unigram 그리디 인코딩 (최장 일치 우선).
     */
    private fun unigramEncode(text: String): List<Int> {
        val result = mutableListOf<Int>()
        var i = 0
        while (i < text.length) {
            var found = false
            // 가장 긴 토큰부터 시도
            for (len in minOf(text.length - i, MAX_PIECE_LEN) downTo 1) {
                val sub = text.substring(i, i + len)
                val id = tokenToId[sub]
                if (id != null) {
                    result.add(id)
                    i += len
                    found = true
                    break
                }
            }
            if (!found) {
                // 단일 문자도 없으면 <unk>
                result.add(UNK_ID)
                i++
            }
        }
        return result
    }

    /**
     * TFLite 추론 실행.
     * 입력: input_ids[1,128] + attention_mask[1,128]
     * 출력: logits[1,5]
     */
    private val LONG_BYTES = 8

    private fun runInference(tokenIds: IntArray): FloatArray {
        // input_ids 버퍼 (128 × 8 bytes)
        val inputIdsBuf = ByteBuffer
            .allocateDirect(maxSeqLen * LONG_BYTES)
            .order(ByteOrder.nativeOrder())
        for (id in tokenIds) inputIdsBuf.putLong(id.toLong())
        inputIdsBuf.rewind()

        // attention_mask 버퍼: PAD=0, 나머지=1 (128 × 8 bytes)
        val maskBuf = ByteBuffer
            .allocateDirect(maxSeqLen * LONG_BYTES)
            .order(ByteOrder.nativeOrder())
        for (id in tokenIds) maskBuf.putLong(if (id == PAD_ID) 0L else 1L)
        maskBuf.rewind()

        // 출력 버퍼 [1, 5]
        val outputBuf = Array(1) { FloatArray(NUM_LABELS) }

        // 다중 입력 추론
        val inputs = mapOf(
            interpreter.getInputIndex("input_ids") to inputIdsBuf,
            interpreter.getInputIndex("attention_mask") to maskBuf
        )
        val outputs = mapOf(0 to outputBuf)

        interpreter.runForMultipleInputsOutputs(inputs.values.toTypedArray(), outputs)

        return outputBuf[0]
    }

    companion object {
        private const val TAG = "MoodInference"
        private const val INT_BYTES = 4
        private const val NUM_LABELS = 5
        private const val MAX_PIECE_LEN = 16  // SentencePiece 최대 조각 길이 (실용적 한계)
    }
}