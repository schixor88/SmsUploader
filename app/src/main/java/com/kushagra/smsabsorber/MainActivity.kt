package com.kushagra.smsabsorber

import android.content.DialogInterface
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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import java.util.jar.Manifest



private const val SMS_PERMISSION = 1004


class MainActivity : AppCompatActivity() {



    lateinit var searchInput: EditText
    lateinit var button: Button
    lateinit var recycler: RecyclerView
    lateinit var adapter:SimpleSmsListAdapter

    val shortCodes = arrayOf("CTZN_ALERT","THE_Alert","SP_OTP","Hello")

    var responseData:ArrayList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        actionBar?.hide()

        searchInput = findViewById(R.id.input)
        button = findViewById(R.id.btn_search)
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

        return returnData

    }




}