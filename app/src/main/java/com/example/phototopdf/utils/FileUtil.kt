package com.example.phototopdf.utils

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import com.example.phototopdf.R
import java.io.File
import java.sql.Array


class FileUtil(private val activity: AppCompatActivity) {

    fun getOutputDirectory(): File {
        val mediaDir = activity.externalMediaDirs.firstOrNull()?.let {
            File(it, activity.resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else activity.filesDir
    }

    fun openOutputDirectory() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        val selectedUri = Uri.Builder().appendPath(getOutputDirectory().path)
                .appendPath("/").build()
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setDataAndType(selectedUri, "application/pdf")
        activity.startActivity(Intent.createChooser(intent, "Open folder"))
    }

    fun openPdf(pdfFile: File) {
        var target = Intent(Intent.ACTION_VIEW)
        val selectedUri = FileProvider.getUriForFile(activity.baseContext,
                activity.getPackageName() + ".provider",
                pdfFile)
        target.setDataAndType(selectedUri,"application/pdf");
        target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        target.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        val intent = Intent.createChooser(target, "Open File");
        try {
            activity.startActivity(intent);
        } catch (e: ActivityNotFoundException) {
            Log.e("FILE EXPLORING", "The device does not have an application to open PDF.")
        }

    }

    fun cacheFolder(): File? {
        return activity.application.applicationContext.cacheDir
    }

    fun clearCache() {
        cacheFolder()?.deleteRecursively()
    }

    fun getLastModifiedFileInFolder(filesFolder: File): File {
        return filesFolder.listFiles().get(filesFolder.listFiles().size-1)
    }
}

