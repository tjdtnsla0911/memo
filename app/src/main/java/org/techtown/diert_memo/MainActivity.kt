package org.techtown.diert_memo

import android.app.DatePickerDialog
import android.icu.util.GregorianCalendar

import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*
//상속
class MainActivity : AppCompatActivity() {

    var dataModelList = mutableListOf<DataModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //파이어베이스관련
        val database = Firebase.database
        //이러면 내 uid로만 가능
        val myRef = database.getReference("myMemo")

        var listView = findViewById<ListView>(R.id.mainLV)

        val adater = ListViewAdapter(dataModelList)

        listView.adapter = adater
      //현재기반으로 가져와라란뜻
        myRef.child(Firebase.auth.currentUser!!.uid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                dataModelList.clear()

                for(dataModel in dataSnapshot.children){
                    Log.e("######",dataModel.toString())
                    //이걸로 받아온데이터를 dataModelList에 때려박는다
                    dataModelList.add(dataModel.getValue(DataModel::class.java)!!)
                }
                //이걸로 새롭게돌린다는데 추후검색필요
                adater.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {

            }
        })
        val writeButton = findViewById<ImageView>(R.id.writeBtn)

        writeButton.setOnClickListener {
            //여기서부터중요함
            val mDialogView = LayoutInflater.from(this).inflate(R.layout.custom_dialog,null)

            val mBuilder = AlertDialog.Builder(this)
                .setView(mDialogView)
                .setTitle("운동 메모 다이얼로그")
            //얘가 존나 중요함
            val mAlertDialog = mBuilder.show()
            //날짜가져오기

            val DateSelectBtn =  mAlertDialog.findViewById<Button>(R.id.dateSelecBtn)

            var dataText = ""

            //날짜선택버튼
            DateSelectBtn?.setOnClickListener {

                val today = GregorianCalendar()

                var year : Int = today.get(Calendar.YEAR)
                var month : Int = today.get(Calendar.MONTH)
                var data : Int = today.get(Calendar.DATE)

                var dlg = DatePickerDialog(this,object : DatePickerDialog.OnDateSetListener{

                    override fun onDateSet(p0: DatePicker?, p1: Int, p2: Int, p3: Int) {
                        Log.e("####","${year},${month +1},${data}")

                        mAlertDialog!!.findViewById<Button>(R.id.dateSelecBtn)?.setText("${year},${month +1},${data}")
                            //여기가 날짜선택하는 곳
                        dataText = "${year}년${month +1}월${data}일"
                    }
                },year,month,data)

                dlg.show()
            }
            //저장하기버튼
           val saveBtn = mAlertDialog.findViewById<Button>(R.id.saveBtn)

            saveBtn?.setOnClickListener {

                val memo = mAlertDialog.findViewById<TextView>(R.id.healthMemo)?.text.toString()
                val database = Firebase.database
                val myRef = database.getReference("myMemo").child(Firebase.auth.currentUser!!.uid)

                val model = DataModel(dataText,memo)
                //.setValue 는 똑같은 데이터가 있으면 별이없으면 일어나지않는다
                //push().setValue 하면 있든말든 존나찍어낸다
                myRef.push().setValue(model)
                //이놈이 다이얼로그 닫히게함
                mAlertDialog.dismiss()

            }
        }
    }
}