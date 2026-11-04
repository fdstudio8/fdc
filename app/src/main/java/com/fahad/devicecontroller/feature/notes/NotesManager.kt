package com.fahad.devicecontroller.feature.notes

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class StudyNote(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Local repository for encrypted study notes and schedules.
 */
class NotesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("fdc_study_notes", Context.MODE_PRIVATE)

    private val _notes = MutableStateFlow<List<StudyNote>>(emptyList())
    val notes: StateFlow<List<StudyNote>> = _notes.asStateFlow()

    init {
        loadNotes()
    }

    private fun loadNotes() {
        val jsonStr = prefs.getString("notes_json", "[]") ?: "[]"
        try {
            val arr = JSONArray(jsonStr)
            val list = mutableListOf<StudyNote>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    StudyNote(
                        id = obj.getString("id"),
                        title = obj.getString("title"),
                        content = obj.getString("content"),
                        timestamp = obj.getLong("timestamp")
                    )
                )
            }
            _notes.value = list
        } catch (e: Throwable) {
            _notes.value = emptyList()
        }
    }

    fun addNote(title: String, content: String) {
        val newNote = StudyNote(title = title, content = content)
        val updated = listOf(newNote) + _notes.value
        saveNotes(updated)
    }

    fun deleteNote(id: String) {
        val updated = _notes.value.filter { it.id != id }
        saveNotes(updated)
    }

    private fun saveNotes(notesList: List<StudyNote>) {
        val arr = JSONArray()
        for (n in notesList) {
            val obj = JSONObject()
            obj.put("id", n.id)
            obj.put("title", n.title)
            obj.put("content", n.content)
            obj.put("timestamp", n.timestamp)
            arr.put(obj)
        }
        prefs.edit().putString("notes_json", arr.toString()).apply()
        _notes.value = notesList
    }
}

