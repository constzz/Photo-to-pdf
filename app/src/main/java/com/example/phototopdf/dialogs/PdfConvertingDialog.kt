package com.example.phototopdf.dialogs

import android.app.Activity
import android.app.Dialog
import com.example.phototopdf.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class PdfConvertingDialog(private val activity: Activity) {
    private lateinit var dialog: Dialog

    fun startLoadingAnimation() {
        dialog = MaterialAlertDialogBuilder(activity).apply {
            setView(activity.layoutInflater.inflate(R.layout.pdf_converting_dialog, null))
            setCancelable(false)
        }.create()

        dialog.show()
    }

    fun dismissDialog() {
        dialog.dismiss()
    }
}