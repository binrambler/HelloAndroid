package com.example.hello

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipInputStream

fun unzip(
    dirArchive: String,
    fileArchive: String,
    dirExtract: String
): Flow<Float> =
    flow {
        val fileSource = "$dirArchive/$fileArchive"
        val buffer = ByteArray(1024 * 1024)
        try {
            val streamInputFile = FileInputStream(fileSource)
            ZipInputStream(streamInputFile).use { zis ->
                while (true) {
                    val zipEntry = zis.nextEntry ?: break
                    val fileSize = zipEntry.size
                    val streamOutputFile = FileOutputStream("$dirExtract/${zipEntry.name}")
                    var complete: Long = 0L
                    while (true) {
                        val bytesRead = zis.read(buffer)
                        if (bytesRead == -1) break
                        streamOutputFile.write(buffer, 0, bytesRead)
                        complete += bytesRead
                        emit(complete / fileSize.toFloat())
                    }
                    streamOutputFile.flush()
                    streamOutputFile.close()
                }
            }
        } catch (e: IOException) {
            throw e
        }
    }
        .flowOn(Dispatchers.IO)