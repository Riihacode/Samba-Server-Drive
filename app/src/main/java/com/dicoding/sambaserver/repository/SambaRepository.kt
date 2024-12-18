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
                // Proses login dilakukan menggunakan akun guest
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

    // Mendownload File
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

    // untuk membuka folder tertentu.
    suspend fun openFolder(context: Context, folderName: String) = withContext(Dispatchers.IO) {
        try {
            val folder = SmbFile("$smbUrl/$folderName", cifsContext)

            if (!folder.exists() || !folder.isDirectory) {
                throw Exception("Folder does not exist or is not a directory")
            }

            // Update URL direktori saat ini
            smbUrl = folder.canonicalPath
            val filesInDirectory = folder.listFiles().map { it.name }

            // Update daftar file di UI thread
            withContext(Dispatchers.Main) {
                (context as? MainActivity)?.updateFileList(filesInDirectory)
            }
        } catch (e: Exception) {
            Log.e("SambaRepository", "Error navigating to folder: ${e.message}", e)
            throw e
        }
    }

    suspend fun openFile(context: Context, fileName: String) = withContext(Dispatchers.IO) {
        try {
            val remoteFile = SmbFile("$smbUrl/$fileName", cifsContext)

            if (!remoteFile.exists() || remoteFile.isDirectory) {
                throw Exception("File does not exist or is a directory")
            }

            // Unduh file ke cache lokal
            val localFile = File(context.cacheDir, fileName)
            remoteFile.inputStream.use { input ->
                localFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            // Gunakan FileProvider untuk URI aman
            val fileUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                localFile
            )

            // Tentukan MIME type dan buka file
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
        } catch (e: Exception) {
            Log.e("SambaRepository", "Error opening file: ${e.message}", e)
            throw e
        }
    }



    // Membuat Folder
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

    // untuk mengunggah file.
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