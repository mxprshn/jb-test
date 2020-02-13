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


class GoogleDriveService(private val activity: Activity, private val listener: GoogleDriveServiceListener) {

    private val googleSignInClient : GoogleSignInClient
    private val googleAccountCredential = GoogleAccountCredential.usingOAuth2(activity, Collections.singleton(DriveScopes.DRIVE_FILE))
    private val drive = Drive.Builder(NetHttpTransport(), GsonFactory(), googleAccountCredential)
        .setApplicationName(activity.resources.getString(R.string.app_name)).build()
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

    public fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_SIGN_IN)
            if (data != null) {
                signIn(data)
            }
            else {

            }
    }

    public fun initSignIn() = activity.startActivityForResult(googleSignInClient.signInIntent, REQUEST_SIGN_IN)

    public fun signIn(data: Intent?) {
        GoogleSignIn.getSignedInAccountFromIntent(data).addOnSuccessListener {
            refreshAccountCredential(it)
        }.addOnFailureListener {

            }
    }

    public fun signOut() {
        googleSignInClient.signOut().addOnSuccessListener {
            listener.onSignOut()
        }.addOnFailureListener {

        }
    }

    private fun refreshAccountCredential(googleAccount : GoogleSignInAccount) {
        googleAccountCredential.selectedAccount = googleAccount.account
        listener.onSignIn()
    }

    public fun checkLoginStatus() : Boolean
    {
        val account = GoogleSignIn.getLastSignedInAccount(activity)
        val containsScope = account?.grantedScopes?.contains(Scope(DriveScopes.DRIVE_FILE))
        if (account != null && containsScope == true) {
            refreshAccountCredential(account)
            return true
        } else {
            return false
        }
    }

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

        }
    }
}