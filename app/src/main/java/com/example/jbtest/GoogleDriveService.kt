package com.example.jbtest

import android.app.Activity
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors

/// Service class for accessing Google Drive options.
class GoogleDriveService(private val activity: Activity, private val listener: Listener) {

    private val googleSignInClient : GoogleSignInClient
    private val googleAccountCredential = GoogleAccountCredential.usingOAuth2(activity, Collections.singleton(DriveScopes.DRIVE_FILE))
    private val drive = Drive.Builder(NetHttpTransport(), GsonFactory(), googleAccountCredential)
        .setApplicationName(activity.resources.getString(R.string.app_name)).build()

    /// Executor for file uploading.
    private val executor = Executors.newSingleThreadExecutor()

    init {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestScopes(Scope(DriveScopes.DRIVE_FILE))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(activity, signInOptions)
    }

    companion object {
        const val REQUEST_SIGN_IN = 101
    }

    /// Method for handling results got from activities.
    public fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_SIGN_IN) {
            if (data != null) {
                signIn(data)
            }
        }
    }

    /// Starts Google sign in activity.
    public fun initSignIn() = activity.startActivityForResult(googleSignInClient.signInIntent, REQUEST_SIGN_IN)

    /// Signs in to Google account using information received from sign in intent.
    public fun signIn(data: Intent?) {
        GoogleSignIn.getSignedInAccountFromIntent(data).addOnSuccessListener {
            refreshAccountCredential(it)
        }.addOnFailureListener {
            listener.onError(it, "Sign in error")
        }
    }

    /// Updates Google account credential with data from provided Google account.
    private fun refreshAccountCredential(googleAccount : GoogleSignInAccount) {
        googleAccountCredential.selectedAccount = googleAccount.account
        listener.onSignIn(googleAccount.email.toString())
    }

    /// Checks if the user is already logged in.
    public fun checkLoginStatus() : Boolean {
        val account = GoogleSignIn.getLastSignedInAccount(activity)
        val containsScope = account?.grantedScopes?.contains(Scope(DriveScopes.DRIVE_FILE))
        if (account != null && containsScope == true) {
            refreshAccountCredential(account)
            return true
        }
        return false
    }

    /// Uploads file to Google Drive.
    public fun uploadFile(filePath : String, fileName : String, folderId : String) : Task<String> {
        return Tasks.call(executor, Callable {
            val fileInfo = File()
            fileInfo.name = fileName
            fileInfo.parents = Collections.singletonList(folderId)
            val file = java.io.File(filePath)
            val content = FileContent("audio/mpeg", file)
            val result = drive.Files().create(fileInfo, content).setFields("id, parents").execute()
            result.id
        }).addOnSuccessListener {
            listener.onUploaded()
        }.addOnFailureListener {
            listener.onError(it, "Upload error")
        }
    }
}