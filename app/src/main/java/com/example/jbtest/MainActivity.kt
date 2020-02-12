package com.example.jbtest

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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

class MainActivity : AppCompatActivity() {

    //private var accountInfoViewModel by lazy { ViewModelProviders.of(this).get(UserViewModel::class.java) }

    private lateinit var googleSignInClient : GoogleSignInClient
    private var googleSignInAccount : GoogleSignInAccount? = null
    private lateinit var driveUploader: DriveUploader
    private val tempFolderPath = "/storage/emulated/0/Download/RecorderTemp"
    private val voiceRecorder = VoiceRecorder(tempFolderPath)

    companion object {
        private const val REQUEST_SIGN_IN = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //accountInfoViewModel = ViewModelProvider.

        setContentView(R.layout.activity_main)

        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestScopes(Scope(DriveScopes.DRIVE_FILE))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, signInOptions)

        recordButton.setOnClickListener {
            voiceRecorder.startRecording()
        }

        changePathButton.setOnClickListener {
            val fileName = voiceRecorder.stopRecording()
            driveUploader.uploadFile("${tempFolderPath}/${fileName}", fileName).addOnSuccessListener {
                Toast.makeText(this, "Oho", Toast.LENGTH_LONG).show() }
        }
    }

    override fun onStart() {
        super.onStart()

        googleSignInAccount = GoogleSignIn.getLastSignedInAccount(this)

        if (googleSignInAccount == null) {
            signIn()
        }
        else {
            val credential = GoogleAccountCredential.usingOAuth2(this, Collections.singleton(DriveScopes.DRIVE_FILE))
            credential.setSelectedAccount(googleSignInAccount!!.account)
            val googleDriveService = Drive.Builder(NetHttpTransport(), GsonFactory(), credential)
                .setApplicationName("Audio Recorder").build()
            //вынести
            driveUploader = DriveUploader(googleDriveService)
        }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, REQUEST_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_SIGN_IN) {
            GoogleSignIn.getSignedInAccountFromIntent(data).addOnSuccessListener { googleAccount ->
                    googleSignInAccount = googleAccount
                    val credential = GoogleAccountCredential.usingOAuth2(this, Collections.singleton(DriveScopes.DRIVE_FILE))
                    credential.setSelectedAccount(googleSignInAccount!!.account)
                    val googleDriveService = Drive.Builder(NetHttpTransport(), GsonFactory(), credential)
                        .setApplicationName("Audio Recorder").build()
                    //вынести
                    driveUploader = DriveUploader(googleDriveService)
            }
        }
    }
}
