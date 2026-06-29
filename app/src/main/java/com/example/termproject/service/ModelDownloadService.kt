package com.example.termproject.service

import android.content.Context
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.io.File

/**
 * Firebase Storage에서 ML 모델 파일을 앱 내부 저장소로 다운로드하는 서비스.
 *
 * ─────────────────────────────────────────────────────────────────────────
 * 📌 Firebase Storage 업로드 방법 (담당자 설정 후 교체):
 *
 *   1. Firebase Console → Storage → 파일 업로드
 *      - gs://YOUR_BUCKET/models/cbt_multilingual.tflite
 *      - gs://YOUR_BUCKET/models/tokenizer.json
 *
 *   2. Storage 보안 규칙 (공개 읽기 허용):
 *      rules_version = '2';
 *      service firebase.storage {
 *        match /b/{bucket}/o {
 *          match /models/{file} {
 *            allow read;           // 인증 없이 읽기 허용
 *            allow write: if false; // 쓰기 차단
 *          }
 *        }
 *      }
 *
 *   3. 아래 STORAGE_PATH_MODEL / STORAGE_PATH_TOKENIZER 상수를 실제 경로로 교체.
 * ─────────────────────────────────────────────────────────────────────────
 */
object ModelDownloadService {

    private const val TAG = "ModelDownload"

    // ── TODO: 실제 Firebase Storage 경로로 교체 ──────────────────────────
    private const val STORAGE_PATH_MODEL     = "models/cbt_multilingual.tflite"
    private const val STORAGE_PATH_TOKENIZER = "models/tokenizer.json"
    // ─────────────────────────────────────────────────────────────────────

    const val LOCAL_MODEL_FILENAME     = "cbt_multilingual.tflite"
    const val LOCAL_TOKENIZER_FILENAME = "tokenizer.json"

    /**
     * 앱 내부 저장소의 모델 파일 경로를 반환.
     * MoodInferenceService는 이 경로에서 파일을 로드한다.
     */
    fun getLocalModelFile(context: Context): File =
        File(context.filesDir, LOCAL_MODEL_FILENAME)

    fun getLocalTokenizerFile(context: Context): File =
        File(context.filesDir, LOCAL_TOKENIZER_FILENAME)

    /**
     * 두 파일 모두 이미 다운로드되어 있는지 확인.
     */
    fun areFilesReady(context: Context): Boolean {
        val model     = getLocalModelFile(context)
        val tokenizer = getLocalTokenizerFile(context)
        val ready = model.exists() && model.length() > 0L &&
                tokenizer.exists() && tokenizer.length() > 0L
        Log.d(TAG, "Files ready: $ready " +
                "(model=${model.length()} bytes, tokenizer=${tokenizer.length()} bytes)")
        return ready
    }

    /**
     * Firebase Storage에서 모델과 토크나이저를 순서대로 다운로드.
     *
     * @param context        앱 컨텍스트
     * @param onProgress     진행률 콜백 (0f ~ 1f). 두 파일 합산 기준.
     * @param onComplete     다운로드 완료 콜백
     * @param onError        오류 발생 시 콜백 (에러 메시지)
     */
    suspend fun downloadModels(
        context: Context,
        onProgress: (Float) -> Unit,
        onComplete: () -> Unit,
        onError: (String) -> Unit
    ) {
        val storage = Firebase.storage

        try {
            // ── 1. 모델 다운로드 (진행률 0% ~ 70%) ──────────────────────
            Log.d(TAG, "Downloading model...")
            downloadFile(
                storage       = storage,
                storagePath   = STORAGE_PATH_MODEL,
                localFile     = getLocalModelFile(context),
                progressStart = 0f,
                progressEnd   = 0.7f,
                onProgress    = onProgress
            )

            // ── 2. 토크나이저 다운로드 (진행률 70% ~ 100%) ───────────────
            Log.d(TAG, "Downloading tokenizer...")
            downloadFile(
                storage       = storage,
                storagePath   = STORAGE_PATH_TOKENIZER,
                localFile     = getLocalTokenizerFile(context),
                progressStart = 0.7f,
                progressEnd   = 1.0f,
                onProgress    = onProgress
            )

            Log.d(TAG, "All downloads complete.")
            onComplete()

        } catch (e: Exception) {
            Log.e(TAG, "Download failed: ${e.message}", e)
            // 실패한 파일 정리 (부분 다운로드 방지)
            getLocalModelFile(context).delete()
            getLocalTokenizerFile(context).delete()
            onError(e.message ?: "Unknown download error")
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    // 내부 구현
    // ──────────────────────────────────────────────────────────────────────

    private suspend fun downloadFile(
        storage: FirebaseStorage,
        storagePath: String,
        localFile: File,
        progressStart: Float,
        progressEnd: Float,
        onProgress: (Float) -> Unit
    ) {
        val ref = storage.reference.child(storagePath)

        // 파일 크기 먼저 확인 (진행률 계산용)
        val metadata = ref.metadata.await()
        val totalBytes = metadata.sizeBytes

        // 임시 파일로 다운로드 후 이동 (원자적 교체)
        val tempFile = File(localFile.parent, "${localFile.name}.tmp")

        val task = ref.getFile(tempFile)

        // 진행률 리스너
        task.addOnProgressListener { snapshot ->
            val fraction = if (totalBytes > 0) {
                snapshot.bytesTransferred.toFloat() / totalBytes.toFloat()
            } else {
                snapshot.bytesTransferred.toFloat() / (snapshot.bytesTransferred + 1f)
            }
            val overall = progressStart + fraction * (progressEnd - progressStart)
            onProgress(overall.coerceIn(progressStart, progressEnd))
        }

        task.await() // 완료 대기 (실패 시 예외 throw)

        // 성공 시 임시 파일을 최종 파일로 이동
        tempFile.renameTo(localFile)
        Log.d(TAG, "Saved: ${localFile.absolutePath} (${localFile.length()} bytes)")
    }
}
