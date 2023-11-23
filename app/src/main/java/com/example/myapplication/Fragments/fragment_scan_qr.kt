package com.example.myapplication.Fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.firebase.firestore.FirebaseFirestore

class fragment_scan_qr : Fragment() {

    private val PICK_IMAGE_REQUEST = 1
    private lateinit var cameraSource: CameraSource
    private lateinit var barcodeDetector: BarcodeDetector
    private lateinit var cameraPreview: SurfaceView
    private lateinit var db: FirebaseFirestore
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_scan_qr, container, false)
        cameraPreview = view.findViewById<SurfaceView>(R.id.camera_preview)

        //   if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
        //      ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.CAMERA), 100)
        //  }
        // Kiểm tra quyền CAMERA và yêu cầu quyền nếu chưa được cấp
        db = FirebaseFirestore.getInstance()
        if (checkCameraPermission()) {
            initializeCamera()
        } else {
            requestCameraPermission()
        }
        return view

    }

    private fun initializeCamera() {
        barcodeDetector =
            BarcodeDetector.Builder(requireContext()).setBarcodeFormats(Barcode.QR_CODE).build()
        cameraSource =
            CameraSource.Builder(requireContext(), barcodeDetector).setAutoFocusEnabled(true)
                .build()

        cameraPreview.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
            }

            override fun surfaceCreated(holder: SurfaceHolder) {
                startCameraPreview()
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                stopCameraPreview()
            }
        })

        barcodeDetector.setProcessor(object : Detector.Processor<Barcode> {
            override fun release() {}

            override fun receiveDetections(detections: Detector.Detections<Barcode>) {
                val barcodes = detections.detectedItems
                if (barcodes.size() > 0) {
                    val qrCode_maNV = barcodes.valueAt(0).displayValue

                    isValidQRCode(qrCode_maNV) { isValid ->
                        if (isValid) {
                            val intent = Intent(requireActivity(), fragment_attendance::class.java)
                            intent.putExtra("result", qrCode_maNV)
                            startActivity(intent)

                        } else {
                            Toast.makeText(requireContext(), "Mã không hợp lệ", Toast.LENGTH_SHORT)
                                .show()
                            requireActivity().finish()
                        }
                    }
                }
            }
        })
    }

    private fun startCameraPreview() {
        try {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            cameraSource.start(cameraPreview.holder)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopCameraPreview() {
        cameraSource.stop()
    }

    private fun isValidQRCode(qrCode: String, callback: (Boolean) -> Unit) {
        // Implement your logic for validating QR code
    }


    private fun checkCameraPermission(): Boolean {
        val cameraPermission =
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
        return cameraPermission == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.CAMERA),
            100
        )
    }

}