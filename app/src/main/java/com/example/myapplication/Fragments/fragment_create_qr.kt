package com.example.myapplication.Fragments

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.zxing.BarcodeFormat
import com.google.zxing.Writer
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.util.Calendar


class fragment_create_qr : Fragment() {
    private lateinit var created: Button
    private lateinit var edtID: EditText
    private lateinit var edtName: EditText
    private lateinit var edtDate: EditText
    private lateinit var qrCodeImageView: ImageView
    private val db: FirebaseFirestore =
        FirebaseFirestore.getInstance() // Khởi tạo Firebase Firestore
    private lateinit var qrCodeString: String
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_create_qr, container, false)
        created = view.findViewById(R.id.created)
        edtID = view.findViewById(R.id.id)
        edtName = view.findViewById(R.id.name)
        edtDate = view.findViewById(R.id.date)
        qrCodeImageView = view.findViewById(R.id.imgvQR)
        // Sự kiện khi nhấn vào EditTextNgaySinh
        edtDate.setOnClickListener(View.OnClickListener { v: View? -> showDatePickerDialog() })

        created.setOnClickListener {
            val ten = edtName.text.toString()
            val Ngaysinh = edtDate.text.toString()
            val maNV = edtID.text.toString()

            if (ten.isNotBlank() && Ngaysinh.isNotBlank() && maNV.isNotBlank()) {
                val qrCode = generateQRCode(maNV, 300, 300)
                if (qrCode != null) {
                    qrCodeImageView.setImageBitmap(qrCode)
                    val qrCodeString = convertBitmapToBase64(qrCode)

                    lifecycleScope.launch {
                        try {
                            val document = db.collection("NhanVien").document(maNV).get().await()

                            // Kiểm tra nếu tài liệu đã tồn tại
                            if (document.exists()) {
                                Toast.makeText(
                                    requireContext(),
                                    "Đã tồn tại mã nhân viên",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                // Tài liệu không tồn tại, thực hiện thêm dữ liệu
                                val data = hashMapOf(
                                    "hoTen" to ten,
                                    "ngaySinh" to Ngaysinh,
                                    "dataQR" to qrCodeString
                                )

                                db.collection("NhanVien").document(maNV).set(data)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            // Xử lý khi dữ liệu được thêm thành công
                                            Toast.makeText(
                                                requireContext(),
                                                "Đã thêm dữ liệu thành công",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            // Xử lý khi có lỗi xảy ra
                                            Toast.makeText(
                                                requireContext(),
                                                "Lỗi khi thêm: ${task.exception?.message}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                            }
                        } catch (e: Exception) {
                            // Xử lý khi có lỗi xảy ra
                            Toast.makeText(
                                requireContext(),
                                "Lỗi khi thêm: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }


                } else {
                    Toast.makeText(requireContext(), "Lỗi khi tạo QRCode", Toast.LENGTH_SHORT)
                        .show()
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    "Điền đầy đủ các trường dữ liệu",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        return view


    }


    private fun generateQRCode(data: String, width: Int, height: Int): Bitmap? {
        val qrCodeWriter: Writer = QRCodeWriter() //tao doi tuong trong thu vien de tao ma QR
        return try { //thu
            val bitMatrix: BitMatrix = qrCodeWriter.encode(
                data,
                BarcodeFormat.QR_CODE,
                width,
                height
            ) // ma hoa du lieu da thanh qrcode voi chieu dai + chieu rong mac dinh
            val bitMatrixWidth: Int = bitMatrix.width // lay chieu dai
            val bitMatrixHeight: Int =
                bitMatrix.height // lay chieu rong duoi dang  ma trận  BitMatrix
            val pixels =
                IntArray(bitMatrixWidth * bitMatrixHeight) // de luu tru mau sac tung mang anh trong QR
            for (y in 0 until bitMatrixHeight) { // dung vong lap for de duyet qua tung diem anh
                for (x in 0 until bitMatrixWidth) {
                    pixels[y * bitMatrixWidth + x] = if (bitMatrix[x, y]) {
                        -0x1000000 // mau den
                    } else {
                        -0x1 //mau trang
                    }
                }
            }
            Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight, Bitmap.Config.ARGB_8888).apply {
                setPixels(
                    pixels,
                    0,
                    bitMatrixWidth,
                    0,
                    0,
                    bitMatrixWidth,
                    bitMatrixHeight
                )
            }
        } catch (e: WriterException) {
            e.printStackTrace()
            null
        }
    }


    fun convertBitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        return try {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
            val byteArray: ByteArray = byteArrayOutputStream.toByteArray()
            android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT)
        } finally {
            byteArrayOutputStream.close()
        }
    }

    private fun showDatePickerDialog() {
        // Lấy ngày, tháng, năm hiện tại
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        // Tạo DatePickerDialog
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { view, selectedYear, selectedMonth, selectedDayOfMonth ->
                // Xử lý khi người dùng chọn ngày
                val selectedDate =
                    "$selectedDayOfMonth/${selectedMonth + 1}/$selectedYear"
                edtDate.setText(selectedDate)
            },
            year, month, dayOfMonth
        )

        // Hiển thị DatePickerDialog
        datePickerDialog.show()
    }


}


