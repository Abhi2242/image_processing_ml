package com.smartgeek.imageprocessingtest.views

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.widget.Button
import android.widget.ImageView
import com.smartgeek.imageprocessingtest.R
import com.smartgeek.imageprocessingtest.ml.AutoModel4
import com.smartgeek.imageprocessingtest.model.PupilDistanceModel
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import kotlin.math.pow

class MainActivity : AppCompatActivity() {

    private lateinit var ttvTextureView: TextureView
    private lateinit var cameraManager: CameraManager
    private lateinit var handler: Handler
    private lateinit var handlerThread: HandlerThread
    private lateinit var imageView: ImageView
    private lateinit var bitmap: Bitmap
    private lateinit var model: AutoModel4
    private lateinit var imageProcessor: ImageProcessor
    private val paint = Paint()
    private val paintYellow = Paint()
    private val paintBlue = Paint()
    private var distanceList: ArrayList<PupilDistanceModel> = arrayListOf()
    private var mDistanceList: ArrayList<PupilDistanceModel> = arrayListOf()
    private lateinit var btnNext: Button
    private var count = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkPermission()

        imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(192, 192, ResizeOp.ResizeMethod.BILINEAR)).build()
        model = AutoModel4.newInstance(this@MainActivity)
        ttvTextureView = findViewById(R.id.ttv_textureView)
        imageView = findViewById(R.id.imageView)
        btnNext = findViewById(R.id.btn_next)
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        handlerThread = HandlerThread("videoThread")
        handlerThread.start()
        handler = Handler(handlerThread.looper)

        paint.color = Color.RED
        paintYellow.color = Color.GREEN
        paintBlue.color = Color.BLUE

        ttvTextureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(p0: SurfaceTexture, p1: Int, p2: Int) {
                openCamera()
            }

            override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture, p1: Int, p2: Int) {
            }

            override fun onSurfaceTextureDestroyed(p0: SurfaceTexture): Boolean {
                return false
            }

            override fun onSurfaceTextureUpdated(p0: SurfaceTexture) {
                bitmap = ttvTextureView.bitmap!!
                var tensorImage = TensorImage(DataType.UINT8)
                tensorImage.load(bitmap)
                tensorImage = imageProcessor.process(tensorImage)

                // Creates inputs for reference.
                val inputFeature0 =
                    TensorBuffer.createFixedSize(intArrayOf(1, 192, 192, 3), DataType.UINT8)
                inputFeature0.loadBuffer(tensorImage.buffer)

                // Runs model inference and gets result.
                val outputs = model.process(inputFeature0)
                val outputFeature0 = outputs.outputFeature0AsTensorBuffer.floatArray

                val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
                val canvas = Canvas(mutableBitmap)
                val h = bitmap.height
                val w = bitmap.width
                var x = 0

                val leftEyeIndex = 2
                val rightEyeIndex = 3

                val leftEyeX = outputFeature0[(leftEyeIndex - 1) * 3]
                val leftEyeY = outputFeature0[((leftEyeIndex - 1) * 3) + 1]

                val rightEyeX = outputFeature0[(rightEyeIndex - 1) * 3]
                val rightEyeY = outputFeature0[((rightEyeIndex - 1) * 3) + 1]

                // Convert eye positions to bitmap coordinates
                val bitmapLeftEyeX = leftEyeX * w
                val bitmapLeftEyeY = leftEyeY * h

                val bitmapRightEyeX = rightEyeX * w
                val bitmapRightEyeY = rightEyeY * h

                // Calculate the distance between left and right eyes on the bitmap
                val bitmapEyeDistance = kotlin.math.sqrt(
                    (bitmapRightEyeX - bitmapLeftEyeX).pow(2) + (bitmapRightEyeY - bitmapLeftEyeY).pow(
                        2
                    )
                )

                val cardDistance = 85.60f
                // Calculate the ratio of the card width on the bitmap to the known card distance
                val bitmapCardWidth = cardDistance * (w / bitmap.width.toFloat())
//                val bitmapCardWidth = cardDistance * ((getScreenWidth(this@MainActivity)-20f) / bitmap.width.toFloat())
                val ratio = bitmapCardWidth / cardDistance

                // Calculate the real-world distance between left and right eyes
                val realWorldEyeDistance = ratio * bitmapEyeDistance

                while (x <= 49) {
                    if (outputFeature0[x + 2] > 0.45) {
                        canvas.drawCircle(
                            outputFeature0[x + 1] * w,
                            outputFeature0[x] * h,
                            6f,
                            paint
                        )
                        // Draw circles at left and right eye positions
//                        canvas.drawCircle(bitmapLeftEyeX, bitmapLeftEyeY, 10f, paintYellow)
//                        canvas.drawCircle(bitmapRightEyeX, bitmapRightEyeY, 10f, paintYellow)
                        Log.v("eye distance", realWorldEyeDistance.toString())

                        if (50.00f < realWorldEyeDistance && realWorldEyeDistance < 70.10f && count < 50) {
                            mDistanceList.add(PupilDistanceModel(count, realWorldEyeDistance))
                            if (count >= 10){
                                runOnUiThread {
                                    btnNext.visibility = View.VISIBLE
                                }
                            }
                            count++
                        }
                    }
                    x += 3
                }
                imageView.setImageBitmap(mutableBitmap)
            }
        }

        btnNext.setOnClickListener {
            distanceList.addAll(mDistanceList)
            mDistanceList.clear()
            Log.v("data", distanceList.toString())
            startActivity(
                Intent(this, MainActivity2::class.java)
                    .putExtra("distance", distanceList)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            )
            distanceList.clear()
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this@MainActivity, AnimationActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
    }

    override fun onDestroy() {
        super.onDestroy()
        // Releases model resources if no longer used.
        model.close()
    }

//    fun getScreenWidth(context: Context): Int {
//        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
//        val displayMetrics = DisplayMetrics()
//        windowManager.defaultDisplay.getMetrics(displayMetrics)
//        return displayMetrics.widthPixels
//    }

    @SuppressLint("MissingPermission")
    private fun openCamera() {
        cameraManager.openCamera(
            cameraManager.cameraIdList[1],
            object : CameraDevice.StateCallback() {
                override fun onOpened(p0: CameraDevice) {
                    val captureRequest = p0.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                    val surface = Surface(ttvTextureView.surfaceTexture)
                    captureRequest.addTarget(surface)

                    p0.createCaptureSession(
                        listOf(surface),
                        object : CameraCaptureSession.StateCallback() {
                            override fun onConfigured(p0: CameraCaptureSession) {
                                p0.setRepeatingRequest(captureRequest.build(), null, null)
                            }

                            override fun onConfigureFailed(p0: CameraCaptureSession) {
                            }

                        },
                        handler
                    )
                }

                override fun onDisconnected(p0: CameraDevice) {

                }

                override fun onError(p0: CameraDevice, p1: Int) {

                }

            },
            handler
        )
    }

    private fun checkPermission() {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), 100)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            checkPermission()
        }
    }
}