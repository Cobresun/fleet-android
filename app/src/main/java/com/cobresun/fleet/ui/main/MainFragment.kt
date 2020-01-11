package com.cobresun.fleet.ui.main

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.cobresun.fleet.MainActivity
import com.cobresun.fleet.R
import com.microsoft.appcenter.utils.HandlerUtils.runOnUiThread
import kotlinx.android.synthetic.main.main_fragment.*
import java.io.BufferedReader
import java.util.*


class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private val notificationId = 8888
    private val CHANNEL_ID = "FLEET_CHANNEL_ID"
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

        createNotificationChannel()

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

        reminderButton.setOnClickListener {
            val remindText = inputText.text.toString()
            if (remindText.isNotEmpty()) {
                // Create an explicit intent for an Activity in your app
                val intent = Intent(requireContext(), MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                val pendingIntent: PendingIntent = PendingIntent.getActivity(requireContext(), 0, intent, 0)
                val builder = NotificationCompat.Builder(requireContext(), CHANNEL_ID)
                    .setSmallIcon(R.drawable.notification_icon)
                    .setContentTitle("QuikNote Reminder")
                    .setContentText(remindText)
                    .setStyle(
                        NotificationCompat.BigTextStyle().bigText(remindText)
                    )
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    // Set the intent that will fire when the user taps the notification
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)

                with(NotificationManagerCompat.from(requireContext())) {
                    // notificationId is a unique int for each notification that you must define
                    notify(notificationId, builder.build())
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

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                requireContext().getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

}
