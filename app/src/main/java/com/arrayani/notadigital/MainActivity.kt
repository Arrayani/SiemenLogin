package com.arrayani.notadigital

import android.content.ContentValues.TAG
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.arrayani.notadigital.databinding.ActivityMainBinding
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private var auth: FirebaseAuth? = null
    private var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks? = null

    private var storedVerificationId:String? =null
    //private var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    private var mResendToken: PhoneAuthProvider.ForceResendingToken? = null

    private var     mVerificationId: String? = null

    private var mVerificationInProgress = false

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        //val phoneNumber = "+16505554567"
        val phoneNumber = "+628111901081"
        val smsCode = "123456"

        auth= FirebaseAuth.getInstance()
        val startBtn = binding.buttonStartVerification

        startBtn.setOnClickListener{
            val phoneNumber = binding.fieldPhoneNumber.text
            if (!validatePhoneNumber()) { // TODO 1
             //   return
                Toast.makeText(this,"phone number invalid",Toast.LENGTH_SHORT).show()
            }
            startPhoneNumberVerification(phoneNumber.toString())// TODO 2
        }

        val verifyBtn = binding.buttonVerifyPhone
        verifyBtn.setOnClickListener{
            val code = binding.fieldVerificationCode.text.toString()
            if (TextUtils.isEmpty(code)) {
                binding.fieldVerificationCode.error = "Cannot be empty."
                //return
            }
            //jika tidak empty
            verifyPhoneNumberWithCode(mVerificationId, code)
            //ketika di kirim pertama kali, mVerificationId hanya variabel string kosong
        }





        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() { // TODO 5
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d(TAG, "onVerificationCompleted:$credential")
                mVerificationInProgress = false
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e)
                mVerificationInProgress = false

                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    binding.fieldPhoneNumber.error = "Invalid phone number."
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    Toast.makeText(applicationContext, "Quota exceeded", Toast.LENGTH_SHORT).show()
                }

            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                Log.d(TAG, "onCodeSent:$verificationId")
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:" + verificationId!!)

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId // dari sini mVerification muncul
                mResendToken = token


            }

        }


    }
    //Cek phone is alright???
    private fun validatePhoneNumber(): Boolean {
        //val phoneNumber = fieldPhoneNumber.text.toString()
        val phoneNumber = binding.fieldPhoneNumber.text.toString()
        if (TextUtils.isEmpty(phoneNumber)) {
            binding.fieldPhoneNumber.error = "Invalid phone number." //baru lihat ada parameter error
            return false
        }
        //startPhoneNumberVerification(fieldPhoneNumber.text.toString())
        //startPhoneNumberVerification(phoneNumber)
        return true
    }


    // GET TEXT CODE SENT SO YOU CAN USE IT TO SIGN IN
    private fun startPhoneNumberVerification(phoneNumber: String) { //TODO 3
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber, // Phone number to verify
            60, // Timeout duration
            TimeUnit.SECONDS, // Unit of timeout
            this, // Activity (for callback binding)
            callbacks!!)        // OnVerificationStateChangedCallbacks TODO 4 panggil callbacks nya

        mVerificationInProgress = true //boleaan  kenapa jadi truee????
    }
    //
    /////
    private fun verifyPhoneNumberWithCode(verificationId: String?, code: String) { //TODO 6
        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
        signInWithPhoneAuthCredential(credential)
    }
    //


    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential){
        auth!!.signInWithCredential(credential)
            .addOnCompleteListener(this){ task->
                if (task.isSuccessful){
                    Log.d(TAG, "signInWithCredential:success")
                    val user = task.result?.user
                }else{
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if(task.exception is FirebaseAuthInvalidCredentialsException){
                        //ini untuk menghandle verification code yang tidak benar
                    }

                }

            }
    }
}