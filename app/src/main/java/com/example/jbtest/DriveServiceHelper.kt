package com.example.jbtest

import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.api.client.http.FileContent
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import java.util.concurrent.Callable
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class DriveServiceHelper(private val drive : Drive) {
    private val executor = Executors.newSingleThreadExecutor()

    public fun uploadFile() : Task<String> {
        return Tasks.call(executor, Callable {
            val fileInfo = File()
            Log.i("OLOLO", "1")
            fileInfo.name = "kot"
            Log.i("OLOLO", "2")
            val file = java.io.File("/storage/emulated/0/ololo/kot.jpg")
            Log.i("OLOLO", "3")
            val content = FileContent("image/jpeg", file)
            Log.i("OLOLO", "4")
            val result = drive.Files().create(fileInfo, content).execute()
            result.id
        })
        }
}