package com.arrayani.notadigital

import android.content.ContentValues.TAG
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
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
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    private var mVerificationInProgress = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //val phoneNumber = "+16505554567"
        val phoneNumber = "+628111901081"
        val smsCode = "123456"

        auth= FirebaseAuth.getInstance()
        val veri=findViewById<Button>(R.id.veriBtn)
        veri.setOnClickListener{
            //verifyPhoneNumberWithCode(smsCode,phoneNumber)
            startPhoneNumberVerification(phoneNumber)

        }



        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Log.d(TAG, "onVerificationCompleted:$credential")
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.w(TAG, "onVerificationFailed", e)
                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                }

            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                Log.d(TAG, "onCodeSent:$verificationId")
                storedVerificationId = verificationId //storeVerification itu variabel buatan
                resendToken = token // variabel resendtoken mengakses class PhoneAuthProvider.ForceResendingToken
            }

        }


    }

    /////
    private fun verifyPhoneNumberWithCode(verificationId: String?, code: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
        signInWithPhoneAuthCredential(credential)
    }
    //
    // GET TEXT CODE SENT SO YOU CAN USE IT TO SIGN IN
    private fun startPhoneNumberVerification(phoneNumber: String) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber, // Phone number to verify
            60, // Timeout duration
            TimeUnit.SECONDS, // Unit of timeout
            this, // Activity (for callback binding)
            callbacks!!)        // OnVerificationStateChangedCallbacks

        mVerificationInProgress = true //boleaan
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