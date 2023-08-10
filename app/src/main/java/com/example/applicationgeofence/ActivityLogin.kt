package com.example.applicationgeofence

import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.location.Location
import android.os.Bundle
import android.os.SharedMemory
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class ActivityLogin : AppCompatActivity() {
    private val TAG = "ActivityLogin"
    private var etUserEmail:EditText?=null
    private var etpassword:EditText?=null
    private var etconfirmpassword:EditText?=null
    private var etenteruseremail:EditText?=null
    private var etenterpassword:EditText?=null
    private var btnsignup:Button?=null
    private var btnsignin:Button?=null
    private var tvSignUp:TextView?=null
    private var tvSignIn:TextView?=null

    private var firebaseDatabase :FirebaseDatabase?=null
    private var databasereference : DatabaseReference?=null

    private lateinit var auth:FirebaseAuth
    private lateinit var llsignup:LinearLayout
    private lateinit var llsignin:LinearLayout


    private lateinit var sp:SharedPreferences
    private lateinit var speditor:SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        Log.i(TAG, "${TAG} :onCreate")

        auth= FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
        databasereference = firebaseDatabase!!.getReference("UserInfo")
        sp = getSharedPreferences("myPrefs", MODE_PRIVATE);
        speditor = sp.edit()

        val currentUser = auth.currentUser

        if(currentUser !=null){
            reload()
            speditor.putString("userEMail", currentUser.email)
            speditor.apply()
        }


        etUserEmail = findViewById(R.id.et_user_email)
        etpassword = findViewById(R.id.et_user_password)
        etconfirmpassword = findViewById(R.id.et_confirm_user_password)
        etenteruseremail = findViewById(R.id.et_enter_user_email)
        etenterpassword = findViewById(R.id.et_enter_user_password)
        btnsignup = findViewById(R.id.btn_sign_up)
        btnsignin = findViewById(R.id.btn_sign_in)
        llsignup = findViewById(R.id.llsignup)
        llsignin = findViewById(R.id.llsignin)
        tvSignUp = findViewById(R.id.tv_sign_up)
        tvSignIn = findViewById(R.id.tv_sign_in)


        tvSignUp!!.setOnClickListener {
            llsignin.visibility = View.INVISIBLE
            llsignup.visibility = View.VISIBLE
        }

        tvSignIn!!.setOnClickListener {
            llsignin.visibility = View.VISIBLE
            llsignup.visibility = View.INVISIBLE
        }

        btnsignup!!.setOnClickListener {
            val userEmail  = etUserEmail!!.text.toString()
            val userPassword = etpassword!!.text.toString()
            val userConfirmPassword = etconfirmpassword!!.text.toString()

            if(TextUtils.isEmpty(userEmail) || TextUtils.isEmpty(userPassword) || TextUtils.isEmpty(userConfirmPassword)){
                Toast.makeText(this, "Please add some data", Toast.LENGTH_SHORT).show()
            }else if(!userPassword.equals(userConfirmPassword)){
                Toast.makeText(this, "password and confirm password mismatched", Toast.LENGTH_SHORT).show()

            }else{
                registerUser(userEmail, userPassword)
            }

        }

        btnsignin!!.setOnClickListener {
            val enterUserEmail = etenteruseremail!!.text.toString()
            val enterUserPassword = etenterpassword!!.text.toString()

            if(!enterUserEmail.isEmpty() && !enterUserPassword.isEmpty() && enterUserEmail!=null && enterUserPassword!=null && enterUserEmail!="" && enterUserPassword!=""){
                auth.signInWithEmailAndPassword(enterUserEmail, enterUserPassword)
                    .addOnCompleteListener {
                        if(it.isSuccessful){
                            speditor.putString("userEMail",enterUserEmail)
                            speditor.apply()
                            startActivity(Intent(this,MainActivity::class.java))


                        }else{
                            Toast.makeText(applicationContext, "Email or password is invalid", Toast.LENGTH_SHORT).show()
                        }

                    }
            }else{
                Toast.makeText(applicationContext, "Enter User Details", Toast.LENGTH_SHORT).show()
            }

        }

     }

    private fun reload() {
        startActivity(Intent(this,MainActivity::class.java))

    }

    private fun registerUser(userEmail: String, userPassword: String) {
              auth.createUserWithEmailAndPassword(userEmail, userPassword)
                  .addOnCompleteListener {
                      if(it.isSuccessful){
                          llsignin.visibility = View.VISIBLE
                          llsignup.visibility = View.INVISIBLE
                          Toast.makeText(this,"Registration Successfull", Toast.LENGTH_SHORT).show()
                      }else{
                          val status = getErrorString(it)
                          llsignin.visibility = View.INVISIBLE
                          llsignup.visibility = View.VISIBLE
                          Toast.makeText(this,"${status}", Toast.LENGTH_SHORT).show()

                      }
                  }
    }


    fun clearData(){
        etUserEmail!!.text = null
        etpassword!!.text = null
        etconfirmpassword!!.text = null
        etenteruseremail!!.text = null
        etenterpassword!!.text = null
    }

    private fun getErrorString(task: Task<AuthResult>): String ?{
        try {
            throw task.exception!!
        } catch (e: FirebaseAuthWeakPasswordException) {
            return "Error Weak Password"        }
        catch (e: FirebaseAuthInvalidCredentialsException)
        {            return "Error Invalid Email"        }
        catch (e: FirebaseAuthUserCollisionException)
        {            return "Error User Exists"        }
        catch (e: Exception) {
            Log.e("TAG", e.message!!)
        }
        catch (e: FirebaseAuthInvalidCredentialsException){
            return "invalid email or password"
        }

        return task.exception.toString()
    }

    override fun onResume() {
        super.onResume()
        Log.i(TAG, "${TAG} :onResume")

    }

    override fun onRestart() {
        super.onRestart()
        Log.i(TAG, "${TAG} :onRestart")


    }

}