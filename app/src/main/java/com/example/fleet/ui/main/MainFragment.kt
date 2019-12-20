package com.example.fleet.ui.main

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.example.fleet.R
import com.microsoft.appcenter.utils.HandlerUtils.runOnUiThread
import kotlinx.android.synthetic.main.main_fragment.*
import java.io.BufferedReader
import java.util.*


class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private val filename = "fleetFile"
    private val files: Array<String> by lazy { requireContext().fileList() }

    private val inputMethodManager by lazy { requireContext().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager }

    private var timer = Timer(false)
    private var deletedText: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        inputText.requestFocus()
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)

        if (files.contains(filename)) {
            val savedText = requireContext().openFileInput(filename).bufferedReader().use(BufferedReader::readText)
            if (savedText.isNotEmpty()) {
                inputText.setText(savedText)
            }
        }

        undoButton.setOnClickListener {
            timer.cancel()
            timer = Timer()
            inputText.setText(deletedText)
            saveText(deletedText)
            deleteButton.visibility = View.VISIBLE
            undoButton.visibility = View.GONE
            deletedText = ""
        }

        deleteButton.setOnClickListener {
            deletedText = inputText.text.toString()
            if (deletedText.isNotEmpty()) {
                inputText.setText("")
                saveText("")

                deleteButton.visibility = View.INVISIBLE
                undoButton.visibility = View.VISIBLE
                timer.schedule(object : TimerTask() {
                    override fun run() {
                        runOnUiThread {
                            deleteButton?.let {
                                deleteButton.visibility = View.VISIBLE
                                undoButton.visibility = View.GONE
                            }
                            deletedText = ""
                        }
                    }
                }, 3000)
            }
        }

        shareButton.setOnClickListener {
            val shareText = inputText.text.toString()
            if (shareText.isNotEmpty()) {
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, inputText.text.toString())
                    type = "text/plain"
                }

                val shareIntent = Intent.createChooser(sendIntent, null)
                startActivity(shareIntent)
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
