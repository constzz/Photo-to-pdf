package com.example.phototopdf.utils

import android.util.Log
import com.convertapi.client.Config
import com.convertapi.client.ConvertApi
import com.convertapi.client.Param
import java.io.File
import java.util.ArrayList

class PdfUtil() {
    //Util uses convert API from website https://www.convertapi.com/ . You register here and put
    //your API key down below
    init {
        Config.setDefaultSecret("PbkNqnLu6P9tw2Tr")//The API key
    }

    //Converts photos in .jpg to .pdf file
    fun convertJpgToPdf(inputPhotos: File, tempFolder: File, outputFolder: File) {
        if (checkInputForNullOrEmpty(inputPhotos)) {
            Log.e(TAG, "Your folders are empty or null.")
            return
        };

        //Convert every photo to separate pdf
        convert("jpg",
                inputPhotos,
                "pdf",
                tempFolder)

        //Merge separate PDF files to the one
        mergePdfFiles(tempFolder, outputFolder)

    }

    private fun checkInputForNullOrEmpty(
            inputPhotos: File,
    ): Boolean {
        if (inputPhotos.listFiles().isNullOrEmpty()) {
            Log.e(TAG, "Input photos folder can not be null or empty")
            return true
        }
        return false
    }

    private fun convert(
            inputFormat: String,
            inputFolder: File,
            outputFormat: String,
            outputFolder: File,
    ) {
        inputFolder.listFiles().forEach {
            if (it.name.endsWith(".$inputFormat")) {
                ConvertApi.convert(inputFormat, outputFormat,
                        Param("File", it.toPath())
                ).get().saveFilesSync(outputFolder.toPath())
            }
        }
    }

    private fun mergePdfFiles(inputFolder: File, outputFolder: File) {
        //Add seperate pdf files to api params that merge them
        var params = ArrayList<Param>()
        inputFolder.listFiles().forEach {
            if (it.name.endsWith(".pdf")) {
                params.add(Param("Files", it.toPath()))
            }
        }

        //Merge pdf files to one pdf
        ConvertApi.convert("pdf", "merge",
                *params.toTypedArray()
        ).get().saveFilesSync(outputFolder.toPath())
    }

    companion object {
        const val TAG = "PdfUtil"
    }
}