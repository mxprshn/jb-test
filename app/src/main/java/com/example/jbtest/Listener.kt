package com.example.jbtest

/// Interface of the class which can be notified about service events.
interface Listener {

    /// Method invoked after signing in to Google account.
    public fun onSignIn(email : String)

    /// Method invoked when the file is successfully uploaded to Drive.
    public fun onUploaded()

    /// Method invoked when an error occurs in a notifier class.
    public fun onError(exception : Exception, message : String)
}