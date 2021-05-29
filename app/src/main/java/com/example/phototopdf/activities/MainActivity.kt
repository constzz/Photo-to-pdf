package com.example.phototopdf.activities

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.phototopdf.R
import com.example.phototopdf.dialogs.PdfConvertingDialog
import com.example.phototopdf.utils.CameraUtil
import com.example.phototopdf.utils.FileUtil
import com.example.phototopdf.utils.PdfUtil
import com.pd.chocobar.ChocoBar
import kotlinx.android.synthetic.main.activity_main.*
import org.imaginativeworld.oopsnointernet.NoInternetDialog
import java.util.*


class MainActivity : AppCompatActivity() {
    private var tempPhotosCounter = 0
    private lateinit var cameraUtil: CameraUtil
    private lateinit var fileUtil: FileUtil
    private val pdfConvertingDialog = PdfConvertingDialog(this)
    private var noInternetDialog: NoInternetDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()

        fileUtil = FileUtil(this)
        cameraUtil = CameraUtil(this)
        cameraUtil.requestCameraPermission()
        cameraUtil.initCamera()

        startNewPhotosRecord()

    }

    override fun onResume() {
        super.onResume()
        buildNoInternetDialog()
    }

    override fun onPause() {
        super.onPause()
        noInternetDialog?.destroy()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraUtil.shutdownCamera()
    }

    private fun initViews() {
        camera_capture_button.setOnClickListener {
            capturePhoto()
        }
        create_pdf_btn.progress = 1.0f
        create_pdf_btn.setOnClickListener(View.OnClickListener {
            createPdfFromPicture()
        })
        new_photos_record_btn.setOnClickListener {
            startNewPhotosRecord()
        }
        go_to_output_folder_btn.setOnClickListener(View.OnClickListener() {
            fileUtil.openOutputDirectory()
        })

        switch_flashlight_mode_btn.setOnClickListener {
            //Check for flashlight availability
            if (!cameraUtil.flashlightIsEnabled()) {
                Log.e(CameraUtil.TAG, "Flashlight is not available on this device")
                Toast.makeText(this,
                        getString(R.string.flashlight_not_available_on_device), Toast.LENGTH_SHORT).show()
                switch_flashlight_mode_btn.visibility = View.GONE
                return@setOnClickListener
            }

            changeFlashlightMode()
        }
    }

    private fun startNewPhotosRecord() {
        fileUtil.clearCache()
        new_photos_record_btn.playAnimation()
        tempPhotosCounter = 0
        updateCachedPhotosCounter(0)
    }

    private fun createPdfFromPicture() {
        if (fileUtil.cacheFolder()?.listFiles().isNullOrEmpty()) {
            create_pdf_btn.playAnimation()
            Toast.makeText(this, getString(R.string.click_camera_to_start_capturing), Toast.LENGTH_SHORT).show()
            return
        }


        pdfConvertingDialog.startLoadingAnimation()
        Thread(){
            PdfUtil().convertJpgToPdf(fileUtil.cacheFolder()!!, fileUtil.cacheFolder()!!, fileUtil.getOutputDirectory())
            runOnUiThread {
                pdfConvertingDialog.dismissDialog()
                showSnackBarWithCreatedFile()
            }
        }.start()
    }

    private fun showSnackBarWithCreatedFile() {
        ChocoBar.builder().setActivity(this@MainActivity).setActionText("ACTION")
                .setActionClickListener {
                    fileUtil.openPdf(fileUtil.getLastModifiedFileInFolder(fileUtil.getOutputDirectory()))
                }
                .setText(getString(R.string.you_succesfully_converted_photos_to_pdf))
                .setDuration(ChocoBar.LENGTH_INDEFINITE)
                .setActionText(getString(R.string.open))
                .build()
                .show()
    }

    private fun changeFlashlightMode() {
        //Revert animation when switching *off* the flashlight
        switch_flashlight_mode_btn.speed = if (!cameraUtil.flashlightIsOn()) 1F else -1F
        switch_flashlight_mode_btn.playAnimation()
        //Change flashlight`s icon state
        Handler().postDelayed({
            switch_flashlight_mode_btn.progress = if (!cameraUtil.flashlightIsOn()) 0F else 1F
        }, switch_flashlight_mode_btn.duration)

        cameraUtil.toggleFlashlight()
    }

    private fun capturePhoto() {
        cameraUtil.takePhoto()
        camera_capture_button.playAnimation()
        updateCachedPhotosCounter(++tempPhotosCounter)
        MediaPlayer.create(this, R.raw.camera_shutter).start()
    }

    //Set new value for temporary photos counter and update it on the screen
    private fun updateCachedPhotosCounter(newTempPhotosQuantity: Int) {
        tempPhotosCounter = newTempPhotosQuantity
        findViewById<TextView>(R.id.cached_photos_counter_textview).setText(tempPhotosCounter.toString())
    }

    private fun buildNoInternetDialog() {
        noInternetDialog = NoInternetDialog.Builder(this)
                .apply {
                    cancelable = false
                    noInternetConnectionTitle = getString(R.string.no_internet)
                    noInternetConnectionMessage =
                            getString(R.string.check_your_internet_connection_and_try_again)
                    showInternetOnButtons = true
                    pleaseTurnOnText = getString(R.string.please_turn_on)
                    wifiOnButtonText = "Wifi"
                    mobileDataOnButtonText = getString(R.string.mobile_data)

                    onAirplaneModeTitle = getString(R.string.no_internet)
                    onAirplaneModeMessage = getString(R.string.you_turned_on_airplane_mode)
                    pleaseTurnOffText = getString(R.string.please_turn_off)
                    airplaneModeOffButtonText = getString(R.string.airplane_mode)
                    showAirplaneModeOffButtons = true
                }
                .build()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == CameraUtil.REQUEST_CODE_PERMISSIONS) {
            if (cameraUtil.allPermissionsGranted()) {
                cameraUtil.startCamera()
            } else {
                Toast.makeText(this,
                        getString(R.string.premissions_not_granted_by_user),
                        Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
