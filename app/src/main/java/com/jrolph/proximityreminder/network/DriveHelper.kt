package com.jrolph.proximityreminder.network

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.api.client.http.ByteArrayContent
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import com.jrolph.proximityreminder.ReminderSerializer
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.lang.StringBuilder
import java.util.*
import java.util.concurrent.Executors

class DriveHelper {

    private val mExecutor = Executors.newSingleThreadExecutor()
    private val mDriveService: Drive

    constructor(driveService: Drive) {
        mDriveService = driveService
    }

    fun queryFiles(): Task<FileList> {
        return Tasks.call(mExecutor) { mDriveService.files().list().setSpaces("appDataFolder").execute() }
    }

    fun writeFile(fileName: String, content: String): Task<String> {
        return Tasks.call(mExecutor) {
            val metadata: File = File()
                .setParents(Collections.singletonList("appDataFolder"))
                .setMimeType("text/plain")
                .setName(fileName)
            val contentStream = ByteArrayContent.fromString("text/plain", content)
            val googleFile = mDriveService.files().create(metadata, contentStream).execute() ?: throw IOException("Null result when requesting file creation.")
            googleFile.id
        }
    }

    // From given file name, find associated file ID and delete file
    fun deleteFile(fileName: String) {
        var fileId: String? = null
        val fileList = mDriveService.files().list()
            .setQ("name = $fileName")
            .setSpaces("appDataFolder")
            .execute()
        fileList.files.forEach { it ->
            if (it.name==fileName) fileId = it.id
        }
        if (fileId==null)
            return
        Tasks.call(mExecutor) {
            mDriveService.files().delete(fileId).execute()
        }
    }

    fun readFile(fileId: String?): Task<Pair<String, String>> {
        return Tasks.call(mExecutor) {
            val metadata: File = mDriveService.files().get(fileId).execute()
            val name: String = metadata.name
            mDriveService.files().get(fileId).executeMediaAsInputStream().use { `is` ->
                BufferedReader(InputStreamReader(`is`)).use { reader ->
                    val stringBuilder = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        stringBuilder.append(line)
                    }
                    val contents = stringBuilder.toString()
                    return@call Pair(name, contents)
                }
            }
        }
    }
}