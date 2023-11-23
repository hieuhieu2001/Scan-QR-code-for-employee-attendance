package com.example.myapplication.Fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.SparseArray
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
import com.example.myapplication.Search.Adapter_items
import com.example.myapplication.Search.items
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class fragment_scan_qr : Fragment() {

    private lateinit var cameraSource: CameraSource
    private lateinit var barcodeDetector: BarcodeDetector
    private lateinit var cameraPreview: SurfaceView
    private lateinit var db: FirebaseFirestore
    private var isProcessing = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        db = FirebaseFirestore.getInstance()
        val view = inflater.inflate(R.layout.fragment_scan_qr, container, false)
        cameraPreview = view.findViewById(R.id.camera_preview)
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
                val barcodes: SparseArray<Barcode> = detections.detectedItems
                if (barcodes.size() > 0) {
                    val qrCode_maNV = barcodes.valueAt(0).displayValue
                    // Xử lý dữ liệu mã QR tại đây
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), " $qrCode_maNV", Toast.LENGTH_LONG).show()
                        isValidQRCode(qrCode_maNV) { isValid ->
                            if (isValid) {
                                Toast.makeText(
                                    requireContext(),
                                    "Nhan Vien co ID : $qrCode_maNV Hop le",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                                if (qrCode_maNV != null) {
                                    handleValidQRCode(qrCode_maNV)
                                }

                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "Mã không hợp lệ",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                                requireActivity().finish()
                            }
                        }
                    }
                }
            }
        })


        /* barcodeDetector.setProcessor(object : Detector.Processor<Barcode> {
             override fun release() {}

             override fun receiveDetections(detections: Detector.Detections<Barcode>) {
                 val barcodes = detections.detectedItems
                 if (barcodes.size() > 0) {
                     val qrCode_maNV = barcodes.valueAt(0).displayValue
                     Toast.makeText(
                         requireContext(),
                         "Nhan Vien co ID : $qrCode_maNV Hop le",
                         Toast.LENGTH_SHORT
                     )
                         .show()


                     isValidQRCode(qrCode_maNV) { isValid ->
                         if (isValid) {
                             Toast.makeText(
                                 requireContext(),
                                 "Nhan Vien co ID : $qrCode_maNV Hop le",
                                 Toast.LENGTH_SHORT
                             )
                                 .show()
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
         })  */
    }


    private fun startCameraPreview() {
        try {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.CAMERA
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
        db.collection("NhanVien")
            .document(qrCode)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    val isValid = document?.exists() ?: false
                    callback(isValid)
                } else {
                    callback(false)
                }
            }
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


    private fun handleValidQRCode(qrCode_maNV: String) {
        val currentTimestamp = Timestamp.now()
        val collectionReference = db.collection("NgayDiemDanh")

        collectionReference.limit(1).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val querySnapshot = task.result
                if (querySnapshot != null && !querySnapshot.isEmpty) {
                    val documentSnapshot = querySnapshot.documents[0]
                    val time = documentSnapshot.getTimestamp("time")

                    if (time != null) {
                        if (isAfter(currentTimestamp, time)) {
                            isProcessing = true
                            checkAndDeleteDocumentsThenAdd(qrCode_maNV)
                        } else {
                            checkEmployeeExistence(qrCode_maNV)
                        }
                    }
                }
            }
        }
    }

    private fun checkAndDeleteDocumentsThenAdd(qrCode_maNV: String) {
        val collectionReference = db.collection("NgayDiemDanh")

        collectionReference.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                for (document in task.result!!) {
                    document.reference.delete()
                }
                addEmployeeToCollection(qrCode_maNV)
            } else {
                showToast("Điểm danh thất bại")
                isProcessing = false
            }
        }
    }

    private fun checkEmployeeExistence(qrCode_maNV: String) {
        val collectionReference = db.collection("NgayDiemDanh")

        collectionReference.document(qrCode_maNV).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document.exists()) {
                    showToast("$qrCode_maNV Đã điểm danh trong ngày")
                } else {
                    addEmployeeToCollection(qrCode_maNV)
                }
            } else {
                showToast("Điểm danh thất bại")
            }
        }
    }

    private fun addEmployeeToCollection(qrCode_maNV: String) {
        val data = HashMap<String, Any>()
        data["time"] = FieldValue.serverTimestamp()

        val documentReference = db.collection("NgayDiemDanh").document(qrCode_maNV)
        documentReference.set(data)
            .addOnSuccessListener {
                addNgayDiemDanhtoCollection(qrCode_maNV)
            }
            .addOnFailureListener {
                showToast("Điểm danh thất bại")
            }
    }

    private fun addNgayDiemDanhtoCollection(qrCode_maNV: String) {
        val data = HashMap<String, Any>()
        val currentDate = java.util.Date()

        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val formattedDate = dateFormat.format(currentDate)

        data["idNgay"] = formattedDate
        data["time"] = FieldValue.serverTimestamp()
        data["MaNV"] = qrCode_maNV

        db.collection("DiemDanh")
            .add(data)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    showToast("$qrCode_maNV Đã điểm danh")
                    isProcessing = false
                } else {
                    showToast("Điểm danh thất bại")
                    isProcessing = false
                }
            }


    }

    private fun isAfter(timestamp1: Timestamp, timestamp2: Timestamp): Boolean {
        val cal1 = Calendar.getInstance().apply {
            time = timestamp1.toDate()
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val cal2 = Calendar.getInstance().apply {
            time = timestamp2.toDate()
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        return cal1.after(cal2)
    }

    private fun showToast(message: String) {
        Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show()
    }


}