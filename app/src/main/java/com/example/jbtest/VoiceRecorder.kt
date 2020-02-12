package com.example.jbtest

import android.media.MediaRecorder
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class VoiceRecorder(public val saveFolderPath : String) {

    private val mediaRecorder = MediaRecorder()
    private val fileExtension = ".mp3"
    private var latestFileName = ""

    public var isRecording : Boolean = false
        private set

    init {
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
    }

    public fun startRecording() {

        try {
            latestFileName = "REC-${SimpleDateFormat("dd-MM-yyyy hh-mm-ss aa",
                Locale.getDefault()).format(Date())}${fileExtension}"
            mediaRecorder.setOutputFile("${saveFolderPath}/${latestFileName}")
            mediaRecorder.prepare()
            mediaRecorder.start()
            isRecording = true
        }
        catch (exc : Exception) {
            exc.printStackTrace()
        }
    }

    public fun stopRecording() : String {

        try {
            mediaRecorder.stop()
            mediaRecorder.release()

        }
        catch (exc : Exception) {
            exc.printStackTrace()
        }
        finally {
            isRecording = false
        }

        return latestFileName
    }
}