package com.example.jbtest

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.api.client.http.FileContent
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import java.util.concurrent.Callable
import java.util.concurrent.Executors

class DriveUploader(private val drive : Drive) {

    private val executor = Executors.newSingleThreadExecutor()

    public fun uploadFile(filePath : String, fileName : String) : Task<String> {
        return Tasks.call(executor, Callable {
            val fileInfo = File()
            fileInfo.name = fileName
            val file = java.io.File(filePath)
            val content = FileContent("audio/mpeg", file)
            val result = drive.Files().create(fileInfo, content).execute()
            result.id
            })
        }
}