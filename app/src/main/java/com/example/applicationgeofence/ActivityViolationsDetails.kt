package com.example.applicationgeofence

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ActivityViolationsDetails : AppCompatActivity() {
    private var firebaseDatabase : FirebaseDatabase?=null
    private var databasereference : DatabaseReference?=null

    private lateinit var rv_violations_Details:RecyclerView

    private lateinit var adapter:AdapterClass

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_violations_details)
        this.setFinishOnTouchOutside(false)
        firebaseDatabase = FirebaseDatabase.getInstance()
        databasereference = firebaseDatabase!!.getReference("UserInfo")
        rv_violations_Details = findViewById(R.id.rv_violations_Details)

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rv_violations_Details.layoutManager = layoutManager

        getData()

    }

    private fun getData() {
        val query= databasereference!!.child("User Details")
        Log.i("User Data", "${query}")
        val options :FirebaseRecyclerOptions<UserInfo>  =
            FirebaseRecyclerOptions.Builder<UserInfo>()
                .setQuery(query,UserInfo::class.java).build()

        adapter = AdapterClass(options)
        rv_violations_Details.adapter = adapter


    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        //finish()
        startActivity(Intent(this, MainActivity::class.java))
    }

}