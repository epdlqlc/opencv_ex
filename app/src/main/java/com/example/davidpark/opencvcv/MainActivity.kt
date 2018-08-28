package com.example.davidpark.opencvcv

import android.annotation.TargetApi
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.SurfaceView
import android.view.View
import android.view.WindowManager
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import org.opencv.core.Mat


class MainActivity : AppCompatActivity(), CameraBridgeViewBase.CvCameraViewListener2 {
    private val TAG = "opencv"
    private var mOpenCvCameraView: CameraBridgeViewBase? = null
    private var matInput: Mat? = null
    private var matResult: Mat? = null

     external fun ConvertRGBtoGray(matAddrInput: Long, matAddrResult: Long)


    init {
        Log.v("execute","loadLibrary")
        System.loadLibrary("opencv_java3")
        System.loadLibrary("native-lib")
    }



    private val mLoaderCallback = object : BaseLoaderCallback(this) {
        override fun onManagerConnected(status: Int) {
            when (status) {
                LoaderCallbackInterface.SUCCESS -> {
                    mOpenCvCameraView!!.enableView()
                }
                else -> {
                    super.onManagerConnected(status)
                }
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v("execute","onCreate")

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_main)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //퍼미션 상태 확인
            if (!hasPermissions(PERMISSIONS)) {

                //퍼미션 허가 안되어있다면 사용자에게 요청
                requestPermissions(PERMISSIONS, PERMISSIONS_REQUEST_CODE)
            }
        }
        Log.v("execute","onCreate2")

        mOpenCvCameraView = findViewById<CameraBridgeViewBase>(R.id.activity_surface_view)

        Log.v("execute","onCreate3")
        mOpenCvCameraView!!.visibility = SurfaceView.VISIBLE

        Log.v("execute","onCreate4")
        mOpenCvCameraView!!.setCvCameraViewListener(this)

        Log.v("execute","onCreate5")
        mOpenCvCameraView!!.setCameraIndex(0) // front-camera(1),  back-camera(0)
        Log.v("execute","onCreate6")
        mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        Log.v("execute","onCreate7")
    }

    public override fun onPause() {
        super.onPause()
        if (mOpenCvCameraView != null)
            mOpenCvCameraView!!.disableView()
    }

    public override fun onResume() {
        super.onResume()

        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "onResume :: Internal OpenCV library not found.")
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, mLoaderCallback)
        } else {
            Log.d(TAG, "onResum :: OpenCV library found inside package. Using it!")
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        }
    }

    public override fun onDestroy() {
        super.onDestroy()

        if (mOpenCvCameraView != null)
            mOpenCvCameraView!!.disableView()
    }

    override fun onCameraViewStarted(width: Int, height: Int) {

    }

    override fun onCameraViewStopped() {

    }

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame): Mat {
        Log.v("execute","onCameraFrame")
        matInput = inputFrame.rgba()
        Log.v("execute","onCameraFrame2")
        if (matResult != null) matResult!!.release()
        matResult = Mat(matInput!!.rows(), matInput!!.cols(), matInput!!.type())
        Log.v("execute","onCameraFrame3")
        ConvertRGBtoGray(matInput!!.nativeObjAddr, matResult!!.nativeObjAddr)
        Log.v("execute","onCameraFrame4")
        return matResult as Mat
    }

    //여기서부턴 퍼미션 관련 메소드
    internal val PERMISSIONS_REQUEST_CODE = 1000
    internal var PERMISSIONS = arrayOf("android.permission.CAMERA")

    private fun hasPermissions(permissions: Array<String>): Boolean {
        Log.v("execute","hasPermissions")
        var result: Int

        //스트링 배열에 있는 퍼미션들의 허가 상태 여부 확인
        for (perms in permissions) {

            result = ContextCompat.checkSelfPermission(this, perms)

            if (result == PackageManager.PERMISSION_DENIED) {
                //허가 안된 퍼미션 발견
                return false
            }
        }

        //모든 퍼미션이 허가되었음
        return true
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.v("execute","onRequestPermissionsResult")
        when (requestCode) {

            PERMISSIONS_REQUEST_CODE -> if (grantResults.size > 0) {
                val cameraPermissionAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED

                if (!cameraPermissionAccepted)
                    showDialogForPermission("앱을 실행하려면 퍼미션을 허가하셔야합니다.")
            }
        }
    }


    @TargetApi(Build.VERSION_CODES.M)
    private fun showDialogForPermission(msg: String) {
        Log.v("execute","showDialogForPermission")
        val builder = AlertDialog.Builder(this@MainActivity)
        builder.setTitle("알림")
        builder.setMessage(msg)
        builder.setCancelable(false)
        builder.setPositiveButton("예") { dialog, id -> requestPermissions(PERMISSIONS, PERMISSIONS_REQUEST_CODE) }
        builder.setNegativeButton("아니오") { arg0, arg1 -> finish() }
        builder.create().show()
    }
}