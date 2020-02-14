package com.example.jbtest

interface Listener {
    public fun onSignIn(email : String)
    public fun onUploaded()
    public fun onError(exception : Exception, message : String)
}