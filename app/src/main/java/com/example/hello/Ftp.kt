package com.example.hello

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import java.io.IOException

enum class Server(
    val ip: String,
    val port: Int,
    val login: String,
    val password: String,
    val timeoutConnect: Int,
    val timeoutData: Int
) {
    N1("192.168.20.6", 21, "amberftp", "201002", 2000, 1000),
    N2("91.208.84.67", 21, "amberftp", "201002", 2000, 1000),
    N3("46.23.184.156", 21, "amberftp", "201002", 2000, 1000);
}

//dirRemote, dirLocal - без слэша в конце
fun ftpDownload(
    dirRemote: String,
    dirLocal: String,
    file: String
): Flow<Float> = flow {
    var flSuccess = false
    for (server in Server.entries) {
        try {
            ftpDownloadSecondary(server, dirRemote, dirLocal, file).collect() { emit(it) }
            flSuccess = true
            break
        } catch (e: Throwable) {
        }
    }
    if (!flSuccess) throw Exception("Ошибка загрузки")
}

private fun ftpDownloadSecondary(
    server: Server,
    dirRemote: String,
    dirLocal: String,
    file: String
): Flow<Float> = flow {
    val ftpClient = FTPClient()
    ftpClient.connectTimeout = server.timeoutConnect
    ftpClient.setDataTimeout(server.timeoutData)

    try {
        ftpClient.connect(server.ip, server.port)
        ftpClient.login(server.login, server.password)
        ftpClient.enterLocalPassiveMode()
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE)

        val fileRemote = "$dirRemote/$file"
        val fileDownload = "$dirLocal/$file"
        val fileSizeRemote = ftpClient.getSize(fileRemote)
        val streamOutput = BufferedOutputStream(FileOutputStream(fileDownload))
        val streamInput = ftpClient.retrieveFileStream(fileRemote)
        val buffer = ByteArray(1024)
        var downloadBytes: Long = 0L
        while (true) {
            val bytesRead = streamInput.read(buffer)
            if (bytesRead == -1) break
            streamOutput.write(buffer, 0, bytesRead)
            downloadBytes += bytesRead
            emit(downloadBytes / fileSizeRemote.toFloat())
        }
        val success = ftpClient.completePendingCommand()
        streamOutput.close()
        streamInput.close()
    } catch (e: IOException) {
        throw e
    } finally {
        try {
            if (ftpClient.isConnected) {
                ftpClient.logout()
                ftpClient.disconnect()
            }
        } catch (e: IOException) {
            throw e
        }
    }
}
    .flowOn(Dispatchers.IO)
