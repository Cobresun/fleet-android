package com.example.fleet.ui.main

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.example.fleet.R
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.main_fragment.*
import java.io.BufferedReader

class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private val filename = "fleetFile"
    private val files: Array<String> by lazy { requireContext().fileList() }

    private val clipboardManager by lazy { requireContext().getSystemService(CLIPBOARD_SERVICE) as ClipboardManager }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (files.contains(filename)) {
            val savedText = requireContext().openFileInput(filename).bufferedReader().use(BufferedReader::readText)
            if (savedText.isNotEmpty()) {
                inputText.setText(savedText)
            }
        }

        deleteButton.setOnClickListener {
            val deletedText = inputText.text.toString()
            if (deletedText.isNotEmpty()) {
                inputText.setText("")
                saveText("")

                Snackbar.make(main, getString(R.string.note_deleted), Snackbar.LENGTH_LONG)
                    .apply {
                        setAction(getString(R.string.undo)) {
                            inputText.setText(deletedText)
                            saveText(deletedText)
                        }
                        show()
                    }
            }
        }

        copyButton.setOnClickListener {
            val copiedText = inputText.text.toString()
            if (copiedText.isNotEmpty()) {
                val clip = ClipData.newPlainText("text", copiedText)
                clipboardManager.setPrimaryClip(clip)

                Snackbar.make(main, "Note copied!", Snackbar.LENGTH_SHORT)
                    .apply {
                        show()
                    }
            }

        }

        inputText.addTextChangedListener {
            it?.let {
                saveText(it.toString())
            }
        }
    }

    private fun saveText(text: String) {
        requireContext().openFileOutput(filename, Context.MODE_PRIVATE).use {
            it.write(text.toByteArray())
        }
    }

}
