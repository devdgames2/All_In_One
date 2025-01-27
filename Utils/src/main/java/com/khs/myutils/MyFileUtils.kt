package com.khs.myutils

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.annotation.RequiresApi
import java.io.File
import java.text.DecimalFormat


class MyFileUtils {

    companion object {

        fun showToast(context: Context,tstMessage: String){
            Toast.makeText(context,tstMessage,Toast.LENGTH_SHORT).show()
        }
        private fun getPathFromContentUri(context: Context, contentUri: Uri?): String? {
            val contentResolver = context
                .contentResolver
            val column = "_data"
            val projection = arrayOf(
                column
            )
            val cursor = contentResolver.query(
                contentUri!!, projection, null, null,
                null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(columnIndex)
            }
            return null
        }

        fun getSharedStorageDirectory(context: Context, fileName: String?,folderName: String?): String? {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val imageCollection: Uri
                imageCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                } else {
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                }

                val imageDetails = ContentValues()
                imageDetails.put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                imageDetails.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg")
                imageDetails.put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    "Pictures/$folderName"
                )

                //temporary file name to get directory path. this file will be removed immediately
                imageDetails.put(MediaStore.Images.Media.DISPLAY_NAME, "temp000000.jpg")

                //            songDetails.put(MediaStore.Audio.Media.IS_PENDING, 1);
                val imageContentUri = resolver
                    .insert(imageCollection, imageDetails)
                val tempFilePath: String =
                    getPathFromContentUri(
                        context,
                        imageContentUri
                    )!!

                val tempFolderPath = tempFilePath.substring(0, tempFilePath.lastIndexOf("/"))
                resolver.delete(imageContentUri!!, null, null)
                tempFolderPath
            } else {
                val dir = File(
                    Environment.getExternalStorageDirectory(),
                  folderName
                )
                if (!dir.exists()) dir.mkdirs()
                dir.absolutePath
            }
        }

        fun getFileSizeByPathId(file: File): String? {
            var fSize = ""
            val size = file.length()
            val df = DecimalFormat("0.00")
            val sizeKb = 1024.0f
            val sizeMb = sizeKb * sizeKb
            val sizeGb = sizeMb * sizeKb
            val sizeTerra = sizeGb * sizeKb
            if (size < sizeMb) fSize =
                df.format((size / sizeKb).toDouble()) + " Kb" else if (size < sizeGb) fSize =
                df.format((size / sizeMb).toDouble()) + " Mb" else if (size < sizeTerra) fSize =
                df.format((size / sizeGb).toDouble()) + " Gb"
            return fSize
        }

        fun getUriFromFilePath(context: Context, filePath: String): Uri? {
            // Handle different Android versions and storage access frameworks
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                getUriForSAF(context, filePath)
            } else {
                getUriForLegacy(filePath)
            }
        }

        @RequiresApi(Build.VERSION_CODES.Q)
        private fun getUriForSAF(context: Context, filePath: String): Uri? {
            // For Android 10 (Q) and above, use Storage Access Framework (SAF)
            val columns = arrayOf(MediaStore.Images.Media._ID)
            val selection = MediaStore.Images.Media.DATA + " = ?"
            val selectionArgs = arrayOf(filePath)

            val cursor: Cursor? = context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                columns,
                selection,
                selectionArgs,
                null
            )

            cursor?.use {
                if (it.moveToFirst()) {
                    val id = it.getInt(it.getColumnIndex(MediaStore.Images.Media._ID))
                    return Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id.toString())
                }
            }
            return null
        }

        private fun getUriForLegacy(filePath: String): Uri? {
            // For older Android versions, use the legacy method
            return Uri.fromFile(File(filePath))
        }


    }
}