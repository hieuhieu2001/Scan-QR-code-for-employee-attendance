package com.example.myapplication.Fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.Search.Adapter_items
import com.example.myapplication.Search.items
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class fragment_attendance: Fragment() {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance() // Khởi tạo Firebase Firestore
    private lateinit var txtDate : TextView
    private lateinit var lst : ListView
    private lateinit var datee : ImageView

    private var dateee123 : String = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_attendance, container, false)
        txtDate = view.findViewById(R.id.tx_date)
        lst = view.findViewById(R.id.lst_e)
        datee = view.findViewById(R.id.imageView12)

        datee.setOnClickListener(){showDatePickerDialog()}



        return view

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
            DatePickerDialog.OnDateSetListener { _: DatePicker?, selectedYear: Int, selectedMonth: Int, selectedDayOfMonth: Int ->
                // Xử lý khi người dùng chọn ngày
                handleDateSelection(selectedYear, selectedMonth, selectedDayOfMonth)





            },
            year, month, dayOfMonth
        )

        // Hiển thị DatePickerDialog
        datePickerDialog.show()
    }
    private fun handleDateSelection(year: Int, month: Int, dayOfMonth: Int) {
        // Xử lý dữ liệu ngày tháng
        var selectedDate = "$dayOfMonth-${month + 1}-$year"
        dateee123 = selectedDate
        txtDate.setText(dateee123)

        val itemss: ArrayList<items> = ArrayList<items>()

        db.collection("DiemDanh")
            .whereEqualTo("idNgay", dateee123)  // formattedDate là giá trị ngày bạn quan tâm
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // 2. Lưu trữ danh sách maNV từ bảng DiemDanh
                    val maNVList = mutableListOf<String>()
                    for (document in task.result!!) {
                        val maNV = document.getString("MaNV")
                        maNVList.add(maNV!!)
                    }

                    // 3. Truy vấn tất cả bản ghi từ bảng NhanVien
                    db.collection("NhanVien")
                        .get()
                        .addOnCompleteListener { nvTask ->
                            if (nvTask.isSuccessful) {
                                // 4. Duyệt qua danh sách bản ghi từ bảng NhanVien
                                for (nvDocument in nvTask.result!!) {
                                    val maNV = nvDocument.id
                                    val ten = nvDocument.getString("hoTen")!!
                                    val diemDanh =
                                        if (maNVList.contains(maNV)) "Có mặt" else "Vắng mặt"
                                    itemss.add(items(maNV!!, ten!!, diemDanh!!))
                                }
                                requireActivity().runOnUiThread {
                                    val ddTheoNgayAdapter = Adapter_items(
                                        requireContext(),
                                        R.layout.items,
                                        itemss
                                    )
                                    lst.adapter = ddTheoNgayAdapter
                                }
                                // val ddTheoNgayAdapter = Adapter_items(
                                //     requireContext(),
                                //      R.layout.items,
                                //     itemss
                                // )
                                //    lst.adapter = ddTheoNgayAdapter

                            } else {
                                // Xử lý lỗi khi truy vấn bảng NhanVien
                                Toast.makeText(requireContext(), "Lỗi", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    // Xử lý lỗi khi truy vấn bảng DiemDanh
                    Toast.makeText(requireContext(), "Ngày nghỉ", Toast.LENGTH_SHORT).show()
                }
            }

    }
}