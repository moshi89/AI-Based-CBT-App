package com.example.termproject.service

import android.content.Context
import com.example.termproject.model.CognitiveDistortion
import com.example.termproject.model.SavedJournal
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * 마음 성찰 일지의 영속화를 담당하는 저장소.
 * SharedPreferences + Gson 직렬화를 사용해 앱 재시작 후에도 데이터를 유지한다.
 * userId를 포함한 계정별 독립 파일로 계정 간 데이터를 완전 분리한다.
 */
class JournalRepository(context: Context, userId: String) {

    init {
        android.util.Log.d("ACCOUNT_DEBUG", "JournalRepository initialized with userId: $userId")
    }

    // userId를 파일 이름에 포함시켜 계정별 독립 SharedPreferences 사용
    private val prefs = context.getSharedPreferences(
        "${PREFS_BASE_NAME}_$userId",
        Context.MODE_PRIVATE
    )
    private val gson = Gson()

    /** 저장된 전체 저널 목록 조회 */
    fun loadJournals(): List<SavedJournal> {
        return try {
            val json = prefs.getString(KEY_JOURNALS, null) ?: return emptyList()
            val type = object : TypeToken<List<SavedJournal>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /** 저널 목록 전체 저장 */
    fun saveJournals(journals: List<SavedJournal>) {
        prefs.edit()
            .putString(KEY_JOURNALS, gson.toJson(journals))
            .apply()
    }

    /** 새 저널을 맨 앞에 추가하고 저장된 전체 목록 반환 */
    fun addJournal(journal: SavedJournal, current: List<SavedJournal>): List<SavedJournal> {
        val updated = listOf(journal) + current
        saveJournals(updated)
        return updated
    }

    /** 지정 id의 저널을 삭제하고 갱신된 목록 반환 */
    fun deleteJournal(id: String, current: List<SavedJournal>): List<SavedJournal> {
        val updated = current.filter { it.id != id }
        saveJournals(updated)
        return updated
    }

    companion object {
        private const val PREFS_BASE_NAME = "cbt_sanctuary_journals"
        private const val KEY_JOURNALS = "journals"
    }
}
