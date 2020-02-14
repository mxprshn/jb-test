package com.example.jbtest

import android.media.MediaRecorder
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

/// Class for recording audio from the device microphone.
class VoiceRecorder(public val saveFolderPath : String, private val listener: Listener) {

    private val mediaRecorder = MediaRecorder()

    /// Saved record files extension.
    private val fileExtension = ".mp3"

    /// Name of the last saved record file.
    private var latestFileName = ""

    /// Shows that audio is being recorded.
    public var isRecording : Boolean = false
        private set

    /// Starts recording audio from microphone.
    public fun startRecording() {
        try {
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            latestFileName = "REC-${SimpleDateFormat("dd-MM-yyyy hh-mm-ss aa",
                Locale.getDefault()).format(Date())}${fileExtension}"
            mediaRecorder.setOutputFile("${saveFolderPath}/${latestFileName}")
            mediaRecorder.prepare()
            mediaRecorder.start()
            isRecording = true
        }
        catch (exc : Exception) {
            listener.onError(exc, "Recording error")
        }
    }

    /// Stops recording audio from microphone and returns the name of the saved file.
    public fun stopRecording() : String {
        try {
            mediaRecorder.stop()
            mediaRecorder.reset()
        }
        catch (exc : Exception) {
            listener.onError(exc, "Recording error")
        }
        finally {
            isRecording = false
        }
        return latestFileName
    }
}