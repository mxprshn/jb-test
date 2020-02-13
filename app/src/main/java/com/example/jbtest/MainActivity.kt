package com.example.jbtest

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity(), GoogleDriveServiceListener {

    private lateinit var googleDriveService: GoogleDriveService
    private val tempFolderPath = "/storage/emulated/0/Download/RecorderTemp"
    private lateinit var voiceRecorder : VoiceRecorder
    private var isRecordedAndUploaded = true
    private lateinit var settings : SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        checkPermissions()
        settings = getSharedPreferences("settings", 0)
        voiceRecorder = VoiceRecorder(tempFolderPath)
        googleDriveService = GoogleDriveService(this, this)

        if (!googleDriveService.checkLoginStatus()){
            googleDriveService.initSignIn()
        }

        recordButton.setOnClickListener {
            if (isRecordedAndUploaded) {
                isRecordedAndUploaded = false
                voiceRecorder.startRecording()
                recordButton.setImageResource(R.drawable.ic_stop_rec_button)
            } else {
                val fileName = voiceRecorder.stopRecording()
                recordButton.isEnabled = false
                googleDriveService.uploadFile("${tempFolderPath}/${fileName}", fileName, getFolderId(linkInput.text.toString())
                ).addOnSuccessListener {
                    recordButton.setImageResource(R.drawable.ic_rec_button)
                }
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

    override fun onStart() {
        super.onStart()
        linkInput.setText(settings.getString("folderLink", ""))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        googleDriveService.onActivityResult(requestCode, resultCode, data)
    }

    override fun onSignIn() {
    }

    override fun onSignOut() {
    }

    override fun onActionCancel() {
    }

    override fun onUploaded() {
        isRecordedAndUploaded = true
        recordButton.isEnabled = true
        Toast.makeText(this, "Saved to Google Drive.", Toast.LENGTH_LONG).show()
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED
        ) {
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

    private fun getFolderId(link : String) : String {
        val splitResult = link.split("/")
        if (splitResult.isEmpty()) {
            Toast.makeText(this, "Invalid URL. Saving to the root folder.", Toast.LENGTH_LONG).show()
            return "root"
        } else {
            return splitResult.last()
        }
    }
}