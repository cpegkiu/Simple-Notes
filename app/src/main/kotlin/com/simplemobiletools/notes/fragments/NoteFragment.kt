package com.simplemobiletools.notes.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import com.simplemobiletools.notes.R
import com.simplemobiletools.notes.activities.MainActivity
import com.simplemobiletools.notes.extensions.config
import com.simplemobiletools.notes.extensions.getTextGravity
import com.simplemobiletools.notes.extensions.getTextSize
import com.simplemobiletools.notes.extensions.updateWidget
import com.simplemobiletools.notes.helpers.DBHelper
import com.simplemobiletools.notes.helpers.NOTE_ID
import com.simplemobiletools.notes.models.Note
import kotlinx.android.synthetic.main.fragment_note.view.*

class NoteFragment : Fragment() {
    var noteId = 0
    lateinit var view: ViewGroup
    lateinit var note: Note
    lateinit var mDb: DBHelper

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        view = inflater.inflate(R.layout.fragment_note, container, false) as ViewGroup
        noteId = arguments.getInt(NOTE_ID)
        mDb = DBHelper.newInstance(context)
        note = mDb.getNote(noteId) ?: return view

        if (context.config.clickableLinks) {
            view.notes_view.apply {
                linksClickable = true
                autoLinkMask = Linkify.WEB_URLS or Linkify.EMAIL_ADDRESSES
                movementMethod = LinkMovementMethod.getInstance()
                addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                    }

                    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    }

                    override fun afterTextChanged(s: Editable) {
                        Linkify.addLinks(this@apply, autoLinkMask)
                    }
                })
            }
        }

        return view
    }

    fun saveText() {
        val newText = getCurrentNoteViewText()
        val oldText = getNoteStoredValue()
        if (newText != oldText) {
            note.value = newText
            saveNoteValue(note)
        }
        context.updateWidget()
    }

    fun showKeyboard() {
        view.notes_view.requestFocus()
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view.notes_view, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun saveNoteValue(note: Note) {
        if (note.path.isEmpty()) {
            mDb.updateNoteValue(note)
        } else {
            (activity as MainActivity).saveNoteValueToFile(note.path, getCurrentNoteViewText())
        }
    }

    fun getCurrentNoteViewText() = view.notes_view.text.toString()

    private fun getNoteStoredValue(): String {
        return note.value
    }

    override fun onResume() {
        super.onResume()

        val config = context.config
        view.notes_view.apply {
            setText(getNoteStoredValue())
            setColors(config.textColor, config.primaryColor, config.backgroundColor)
            setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getTextSize())
            gravity = context.getTextGravity()
        }
    }

    override fun onPause() {
        super.onPause()
        saveText()
    }
}
