package com.example.jbtest

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

// Class of the main app activity.
class MainActivity : AppCompatActivity(), Listener {

    /// Service for accessing Google Drive options.
    private lateinit var googleDriveService: GoogleDriveService

    /// Path to the folder where audio records are saved before upload.
    private lateinit var tempFolderPath : String

    /// VoiceRecorder used by the activity.
    private lateinit var voiceRecorder : VoiceRecorder

    /// Shows that the app is ready to record and upload sound.
    private var isReady = true

    /// Preferences of the app.
    private lateinit var settings : SharedPreferences

    // Method invoked when activity created.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkPermissions()
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        checkPermissions()
        tempFolderPath = applicationContext.filesDir.absolutePath
        settings = getSharedPreferences("settings", 0)
        voiceRecorder = VoiceRecorder(tempFolderPath, this)
        googleDriveService = GoogleDriveService(this, this)

        if (!googleDriveService.checkLoginStatus()){
            googleDriveService.initSignIn()
        }

        recordButton.setOnClickListener {
            if (isReady) {
                isReady = false
                voiceRecorder.startRecording()
                recordButton.setImageResource(R.drawable.ic_stop_rec_button)
            } else {
                val fileName = voiceRecorder.stopRecording()
                recordButton.isEnabled = false
                val path = "${tempFolderPath}/${fileName}"
                googleDriveService.uploadFile(path, fileName, getFolderId(linkInput.text.toString())
                ).addOnSuccessListener {
                    val myFile = File(path)
                    if (myFile.exists()) {
                        myFile.delete()
                    }
                    recordButton.setImageResource(R.drawable.ic_rec_button)
                }
                Toast.makeText(this, "Uploading...", Toast.LENGTH_LONG).show()
            }
        }

        linkInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val editor = settings.edit()
                editor.putString("folderLink", linkInput.text.toString())
                editor.apply()
            }
        })
    }

    /// Method invoked when activity enters the Started state.
    override fun onStart() {
        super.onStart()
        linkInput.setText(settings.getString("folderLink", ""))
    }

    /// Method for handling results got from other activities.
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        googleDriveService.onActivityResult(requestCode, resultCode, data)
    }

    /// Method invoked after signing in to Google account.
    override fun onSignIn(email : String) {
        Toast.makeText(this, "Signed in to ${email}", Toast.LENGTH_LONG).show()
    }

    /// Method invoked when the file is successfully uploaded to Drive.
    override fun onUploaded() {
        isReady = true
        recordButton.isEnabled = true
        Toast.makeText(this, "Saved to Google Drive.", Toast.LENGTH_LONG).show()
    }

    /// Method invoked when an error occurs in a notifier class.
    override fun onError(exception: Exception, message: String) {
        Toast.makeText(this, "ERROR: ${message}", Toast.LENGTH_LONG).show()
    }

    /// Checks permissions necessary for the app and asks for them if they are not granted.
    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
            val permissions = arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET,
                Manifest.permission.GET_ACCOUNTS
            )
            ActivityCompat.requestPermissions(this, permissions, 0)
        }
    }

    /// Gets Google Drive folder id from its link.
    private fun getFolderId(link : String) : String {
        val regex = Regex("(?<=id=)\\S+")
        val matchResults = regex.findAll(link)
        if (matchResults.count() == 0) {
            return "root"
        }
        return matchResults.last().value
    }
}