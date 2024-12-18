package com.dicoding.sambaserver.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile
import com.dicoding.sambaserver.main.activity.MainActivity
import jcifs.CIFSContext
import jcifs.config.PropertyConfiguration
import jcifs.context.BaseContext
import jcifs.smb.SmbFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.InetAddress
import java.util.Properties

class SambaRepository(var smbUrl: String) {
    private val initialSmbUrl: String = smbUrl
    lateinit var cifsContext: CIFSContext

    suspend fun initialize() = withContext(Dispatchers.IO) {
        try {
            cifsContext = BaseContext(PropertyConfiguration(Properties().apply {
                setProperty("jcifs.smb.client.username", "guest")
                setProperty("jcifs.smb.client.password", "")
            }))
            Log.d("SambaRepository", "CIFSContext initialized")
        } catch (e: Exception) {
            Log.e("SambaRepository", "Error initializing CIFSContext: ${e.message}", e)
            throw e
        }
    }

    suspend fun resolveHostnameWithFallback(hostname: String): String = withContext(Dispatchers.IO) {
        try {
            InetAddress.getByName(hostname).hostAddress
        } catch (e: Exception) {
            "192.xxx.xxx.xx" // Fallback ke IP default
        }
    }

    fun isAtRootDirectory(): Boolean {
        return smbUrl == initialSmbUrl // Bandingkan URL saat ini dengan root directory
    }

    suspend fun fetchFiles(): List<String> = withContext(Dispatchers.IO) {
        try {
            val remoteDir = SmbFile(smbUrl, cifsContext)
            if (remoteDir.exists() && remoteDir.isDirectory) {
                val files = remoteDir.listFiles().map { it.name }
                Log.d("SambaRepository", "Fetched files: $files")
                files
            } else {
                throw Exception("Directory not accessible")
            }
        } catch (e: Exception) {
            Log.e("SambaRepository", "Error fetching files: ${e.message}", e)
            throw e
        }
    }

    suspend fun deleteFile(fileName: String) = withContext(Dispatchers.IO) {
        try {
            val fileToDelete = SmbFile("$smbUrl/$fileName", cifsContext)
            if (fileToDelete.exists()) {
                fileToDelete.delete()
            } else {
                throw Exception("File/Folder not found")
            }
        } catch (e: Exception) {
            Log.e("SambaRepository", "Error deleting file: ${e.message}", e)
            throw e
        }
    }

    suspend fun downloadFileToCustomLocation(context: Context, fileName: String, fileUri: Uri) = withContext(Dispatchers.IO) {
        try {
            val remoteFile = SmbFile("$smbUrl/$fileName", cifsContext)
            if (!remoteFile.exists()) throw Exception("File does not exist on server")

            context.contentResolver.openOutputStream(fileUri)?.use { output ->
                remoteFile.inputStream.use { input -> input.copyTo(output) }
            } ?: throw Exception("Failed to create output stream")
        } catch (e: Exception) {
            Log.e("SambaRepository", "Error downloading file: ${e.message}", e)
            throw e
        }
    }

    suspend fun openFile(context: Context, fileName: String) = withContext(Dispatchers.IO) {
        try {
            val remoteFile = SmbFile("$smbUrl/$fileName", cifsContext)

            if (!remoteFile.exists()) {
                throw Exception("File does not exist on the server")
            }

            if (remoteFile.isDirectory) {
                // Jika file adalah direktori, navigasi ke direktori tersebut
                smbUrl = remoteFile.canonicalPath // Update URL direktori saat ini
                val filesInDirectory = remoteFile.listFiles().map { it.name }
                withContext(Dispatchers.Main) {
                    (context as? MainActivity)?.updateFileList(filesInDirectory)
                }
            } else {
                // Jika file adalah file, unduh ke cache lokal dan buka
                val localFile = File(context.cacheDir, fileName)
                remoteFile.inputStream.use { input ->
                    localFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                // Gunakan FileProvider untuk mendapatkan URI aman
                val fileUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    localFile
                )

                // Tentukan MIME type untuk membuka file
                val mimeType = context.contentResolver.getType(fileUri) ?: "application/octet-stream"

                withContext(Dispatchers.Main) {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(fileUri, mimeType)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }

                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        throw Exception("No application available to open this file")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("SambaRepository", "Error opening file: ${e.message}", e)
            throw e
        }
    }

    suspend fun createFolder(folderName: String) = withContext(Dispatchers.IO) {
        try {
            val newFolder = SmbFile("$smbUrl/$folderName/", cifsContext)
            if (!newFolder.exists()) {
                newFolder.mkdirs()
                Log.d("SambaRepository", "Folder created at: ${newFolder.canonicalPath}")
            } else {
                throw Exception("Folder already exists")
            }
        } catch (e: Exception) {
            Log.e("SambaRepository", "Error creating folder: ${e.message}", e)
            throw e
        }
    }

    fun updateSmbUrl(newUrl: String) {
        smbUrl = newUrl.trimEnd('/') + "/"
        Log.d("SambaRepository", "Samba URL updated to: $smbUrl")
    }

    suspend fun uploadFile(context: Context,uri: Uri) = withContext(Dispatchers.IO) {
        try {
            val fileName = DocumentFile.fromSingleUri(context, uri)?.name ?: "uploaded_file"
            val remoteFile = SmbFile("$smbUrl/$fileName", cifsContext)

            context.contentResolver.openInputStream(uri)?.use { input ->
                remoteFile.outputStream.use { output ->
                    input.copyTo(output)
                }
            } ?: throw Exception("Failed to open input stream")
        } catch (e: Exception) {
            Log.e("SambaRepository", "Error uploading file: ${e.message}", e)
            throw Exception("Error uploading file: ${e.message}")
        }
    }
}