package com.example.myapplication.Fragments

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.TextUtils
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.google.firebase.firestore.FirebaseFirestore

class fragment_searcg: Fragment() {

    private  lateinit var edtID2: EditText
    private  lateinit var txtID2: TextView

    private lateinit var txtName2: TextView
    private  lateinit var txtDate2: TextView
    private lateinit var qrCodeImageView2 : ImageView
    private lateinit var search : ImageView

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance() // Khởi tạo Firebase Firestore
    private lateinit var MaNV: String
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        edtID2 = view.findViewById(R.id.search_id)

        txtID2 = view.findViewById(R.id.textView6)

        txtName2 = view.findViewById(R.id.textView7)
        txtDate2 = view.findViewById(R.id.textView8)
        qrCodeImageView2 = view.findViewById(R.id.imageView_Search)
        search = view.findViewById(R.id.QR_NV)


        qrCodeImageView2.setOnClickListener()
        {
            MaNV = edtID2.text.toString().trim()

            if (TextUtils.isEmpty(MaNV)) {
                Toast.makeText(requireContext(), "Điền mã nhân viên", Toast.LENGTH_SHORT).show()
            }

            val docRef = db.collection("NhanVien").document(MaNV)

            docRef.get().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val document = task.result
                        if (document!!.exists()) {

                            txtID2.text = "ID: $MaNV"

                            val ngaySinh = document.getString("ngaySinh")
                            val hoTen = document.getString("hoTen")
                            val qrCode = document.getString("dataQR")
                            if (hoTen != null) {
                                // Thực hiện xử lý với hoTen
                                txtName2.text = "Name: $hoTen"
                            }

                            if (ngaySinh != null) {
                                // Thực hiện xử lý với ngaySinh
                                txtDate2.text = "Date: $ngaySinh"
                            }

                            if (qrCode != null) {
                                val qrBit = convertBase64ToBitmap(qrCode)
                                search.setImageBitmap(qrBit)
                            }
                            // ID đã tồn tại trong collection
                            // Thực hiện xử lý tương ứng

                        } else {
                            // ID không tồn tại trong collection
                            // Thực hiện xử lý tương ứng
                            Toast.makeText(requireContext(), "Không tồn tại mã nhân viên", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // Xử lý khi có lỗi xảy ra
                        Toast.makeText(requireContext(), "Lỗi", Toast.LENGTH_SHORT).show()
                    }
                }

        }


        return view
    }
    fun convertBase64ToBitmap(base64String: String?): Bitmap? {
        if (base64String == null || base64String.isEmpty()) {
            return null
        }
        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }
}