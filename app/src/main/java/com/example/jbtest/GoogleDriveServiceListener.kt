package com.example.jbtest

interface GoogleDriveServiceListener {
    public fun onSignIn()
    public fun onSignOut()
    public fun onActionCancel()
    public fun onUploaded()
}