package com.dream.chatbot.utils

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.text.TextUtils
import java.io.*
import java.net.URLDecoder
import java.util.regex.Pattern


object FileHandle {

    fun listDir(path: String, list: ArrayList<String>) {
        val dir = File(path)
        if (!dir.exists() || dir.isFile) return
        val listFiles: Array<File> = dir.listFiles() as Array<File>
        if (listFiles.isEmpty()) return
        list.clear()
        for (file in listFiles) {
            list.add(file.absolutePath)
        }
    }

    private fun createNewFile(path: String) {
        val lastSep = path.lastIndexOf(File.separator)
        if (lastSep > 0) {
            val dirPath = path.substring(0, lastSep)
            makeDir(dirPath)
        }
        val file = File(path)
        try {
            if (!file.exists()) file.createNewFile()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun readFile(path: String): String {
        createNewFile(path)
        val sb = StringBuilder()
        var fr: FileReader? = null
        try {
            fr = FileReader(File(path))
            val buff = CharArray(1024)
            var length: Int
            while (fr.read(buff).also { length = it } > 0) {
                sb.append(String(buff, 0, length))
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            if (fr != null) {
                try {
                    fr.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        return sb.toString()
    }

    fun writeFile(path: String, str: String?) {
        createNewFile(path)
        var fileWriter: FileWriter? = null
        try {
            fileWriter = FileWriter(File(path), false)
            fileWriter.write(str)
            fileWriter.flush()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                fileWriter?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun copyFile(sourcePath: String, destPath: String) {
        if (!isExistFile(sourcePath)) return
        createNewFile(destPath)
        var fis: FileInputStream? = null
        var fos: FileOutputStream? = null
        try {
            fis = FileInputStream(sourcePath)
            fos = FileOutputStream(destPath, false)
            val buff = ByteArray(1024)
            var length: Int
            while (fis.read(buff).also { length = it } > 0) {
                fos.write(buff, 0, length)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            if (fis != null) {
                try {
                    fis.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            if (fos != null) {
                try {
                    fos.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun moveFile(sourcePath: String, destPath: String) {
        copyFile(sourcePath, destPath)
        deleteFile(sourcePath)
    }

    fun deleteFile(path: String) {
        val file = File(path)
        if (!file.exists()) return
        if (file.isFile) {
            file.delete()
            return
        }
        val fileArr = file.listFiles()
        if (fileArr != null) {
            for (subFile in fileArr) {
                if (subFile.isDirectory) {
                    deleteFile(subFile.absolutePath)
                }
                if (subFile.isFile) {
                    subFile.delete()
                }
            }
        }
        file.delete()
    }

    fun isExistFile(path: String): Boolean {
        val file = File(path)
        return file.exists()
    }

    fun isDirectory(path: String): Boolean {
        val file = File(path)
        return file.isDirectory
    }

    fun makeDir(path: String) {
        if (!isExistFile(path)) {
            val file = File(path)
            file.mkdirs()
        }
    }

    fun isContainChinese(str: String) : Boolean {
        val pattern = Pattern.compile("[\u4e00-\u9fcc]+")
        val matcher = pattern.matcher(str)
        return matcher.find()
    }

    fun convertUriToFilePath(context: Context, uri: Uri): String? {
        var path: String? = null
        if (DocumentsContract.isDocumentUri(context, uri)) {
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":").toTypedArray()
                val type = split[0]
                if ("primary".equals(type, ignoreCase = true)) {
                    path = Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                }
            } else if (isDownloadsDocument(uri)) {
                val id = DocumentsContract.getDocumentId(uri)
                if (!TextUtils.isEmpty(id)) {
                    if (id.startsWith("raw:")) {
                        return id.replaceFirst("raw:".toRegex(), "")
                    }
                }
                val contentUri: Uri = ContentUris
                    .withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        java.lang.Long.valueOf(id)
                    )
                path = getDataColumn(context, contentUri, null, null)
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":").toTypedArray()
                val type = split[0]
                var contentUri: Uri? = null
                when (type) {
                    "image" -> {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    }
                    "video" -> {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    }
                    "audio" -> {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }
                }
                val selection = "_id=?"
                val selectionArgs = arrayOf(
                    split[1]
                )
                path = getDataColumn(context, contentUri, selection, selectionArgs)
            }
        } else if (ContentResolver.SCHEME_CONTENT.equals(uri.scheme, ignoreCase = true)) {
            path = getDataColumn(context, uri, null, null)
        } else if (ContentResolver.SCHEME_FILE.equals(uri.scheme, ignoreCase = true)) {
            path = uri.path
        }
        return if (path != null) {
            try {
                URLDecoder.decode(path, "UTF-8")
            } catch (e: Exception) {
                null
            }
        } else null
    }

    private fun getDataColumn(
        context: Context,
        uri: Uri?,
        selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        val column = MediaStore.Images.Media.DATA
        val projection = arrayOf(
            column
        )
        try {
            context.contentResolver.query(uri!!, projection, selection, selectionArgs, null)
                .use { cursor ->
                    if (cursor != null && cursor.moveToFirst()) {
                        val columnIndex: Int = cursor.getColumnIndexOrThrow(column)
                        return cursor.getString(columnIndex)
                    }
                }
        } catch (_: Exception) {
        }
        return null
    }


    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    fun decodeSampleBitmapFromPath(path: String?, reqWidth: Int, reqHeight: Int): Bitmap? {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(path, options)
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeFile(path, options)
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val width = options.outWidth
        val height = options.outHeight
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}