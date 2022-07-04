package com.kushagra.smsabsorber

import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kushagra.smsabsorber.sms.SmsService
import org.json.JSONObject
import java.lang.reflect.Type


private const val SMS_PERMISSION = 1004


class MainActivity : AppCompatActivity() {

    lateinit var searchInput: EditText
    lateinit var button: Button
    lateinit var sendSms: Button
    lateinit var recycler: RecyclerView
    lateinit var adapter:SimpleSmsListAdapter

    val shortCodes = arrayOf("CTZN_ALERT","THE_Alert","SP_OTP","Hello")

    private val smsService = SmsService.create()

    var responseData:ArrayList<String> = ArrayList()

    var smsCollectionData:ArrayList<HashMap<String, ArrayList<String>>> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        actionBar?.hide()

        searchInput = findViewById(R.id.input)
        button = findViewById(R.id.btn_search)
        sendSms = findViewById(R.id.sendSms)
        recycler = findViewById(R.id.recycler)

        var s:String = shortCodes.reduce { acc, s -> "$acc, $s" }
        searchInput.setText(s)
        searchInput.isFocusable = false


        recycler.layoutManager = LinearLayoutManager(this@MainActivity, RecyclerView.VERTICAL, false)
        adapter = SimpleSmsListAdapter(this@MainActivity,responseData)
        recycler.adapter = adapter

        button.setOnClickListener {
            checkPermission()
        }

        sendSms.setOnClickListener {
            if(!smsCollectionData.isNullOrEmpty()){
                    callSmsRequestAPI(smsCollectionData)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_SMS
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
           Log.d("logs",getAggregatedSms().toString())

        } else {
            requestPermissions(
                arrayOf(
                    android.Manifest.permission.READ_SMS
                ), SMS_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == SMS_PERMISSION) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                searchForSms()
            } else {
                Toast.makeText(
                    this,
                    "Please check SMS permission from settings",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun searchForSms(){

        responseData = ArrayList()
        var searchValue = "";

        if(searchInput.text.toString().isNullOrEmpty()){
            Toast.makeText(this,"Invalid Input", Toast.LENGTH_SHORT).show()
            return
        }

        searchValue = searchInput.text.toString();

        val projection = arrayOf("_id","address","body","date")
        val selection = "address='$searchValue'"

        val cursor: Cursor? =
            contentResolver.query(Uri.parse("content://sms/inbox"), projection, selection, null, null)

        if (cursor?.moveToFirst() == true) { // must check the result to prevent exception
            do {
                responseData.add(cursor.getString(2))
            } while (cursor.moveToNext())
        } else {
            Toast.makeText(this, "Not Found", Toast.LENGTH_SHORT).show()
        }
        adapter = SimpleSmsListAdapter(this@MainActivity,responseData)
        recycler.adapter = adapter

    }


    private fun getListOfSMS(shortCode:String):HashMap<String, ArrayList<String>>{

        var returnValue:HashMap<String, ArrayList<String>> = HashMap<String, ArrayList<String>>()
        responseData = ArrayList()
        if(shortCode.isEmpty()){
//            Toast.makeText(this,"Invalid Input", Toast.LENGTH_SHORT).show()
            return returnValue
        }

        val projection = arrayOf("_id","address","body","date")
        val selection = "address='$shortCode'"

        val cursor: Cursor? =
            contentResolver.query(Uri.parse("content://sms/inbox"), projection, selection, null, null)

        if (cursor?.moveToFirst() == true) { // must check the result to prevent exception
            do {
                responseData.add(cursor.getString(2))
            } while (cursor.moveToNext())
        } else {
            Toast.makeText(this, "Not Found", Toast.LENGTH_SHORT).show()
        }
        adapter = SimpleSmsListAdapter(this@MainActivity,responseData)
        recycler.adapter = adapter

        returnValue[shortCode] = responseData

        return returnValue

    }

    private fun getAggregatedSms():ArrayList<HashMap<String, ArrayList<String>>>{

        var returnData:ArrayList<HashMap<String, ArrayList<String>>> = ArrayList()

        for (value in shortCodes){
            returnData.add(getListOfSMS(value))
        }

        var capture = Gson().toJson(returnData)

        Log.d("toGson",capture)
        smsCollectionData = returnData

      return returnData
//        return capture
//        val listType: Type = object : TypeToken<ArrayList<HashMap<String, ArrayList<String>>>>() {}.type
    }

    fun callSmsRequestAPI(data:ArrayList<HashMap<String, ArrayList<String>>>){
        lifecycleScope.launchWhenCreated {
            smsService.sendSmsData(data)
        }
    }






}

data class SmsDataModel(
    var data:HashMap<String, ArrayList<String>>
)