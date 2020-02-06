package com.example.remotetelegramclient

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.remotetelegramclient.api.RestController
import com.example.remotetelegramclient.api.response.ResponseListener
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.tg_code_view.view.*
import kotlinx.android.synthetic.main.tg_phone_view.view.*


class MainActivity : AppCompatActivity(), View.OnClickListener,
    ResponseListener {
    private var mGoogleSignInClient: GoogleSignInClient? = null
    private var gDriveServerAuthCode: String? = null
    private var gDriveIdToken: String? = null
    private val restController: RestController = RestController(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.statusBarColor)
            window.setFlags(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN, View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        }

        button_sign_in.setOnClickListener(this)

        val gso =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestServerAuthCode(getString(R.string.server_client_id))
                .requestScopes(Scope(Scopes.DRIVE_APPFOLDER))
                .requestEmail()
                .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        button_sign_in.setSize(SignInButton.SIZE_STANDARD)
    }

    public override fun onStart() {
        super.onStart()
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null && GoogleSignIn.hasPermissions(
                account,
                Scope(Scopes.DRIVE_APPFOLDER)
            )
        ) {
            gDriveIdToken = account.idToken ?: gDriveIdToken
            gDriveServerAuthCode = account.serverAuthCode ?: gDriveServerAuthCode

            button_sign_out.visibility = View.VISIBLE
            tg_first_view.visibility   = View.INVISIBLE
        } else {
            tg_first_view.visibility   = View.VISIBLE
            button_sign_out.visibility = View.INVISIBLE
        }

        restController.start()
    }

    public override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result.isSuccess) {
                val account = result.signInAccount
                gDriveIdToken = account?.idToken ?: gDriveIdToken
                gDriveServerAuthCode = account?.serverAuthCode ?: gDriveServerAuthCode

                restController.sendGDriveCredentials(gDriveIdToken, gDriveServerAuthCode)
            }
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>?) {
        try {
            val account = completedTask?.getResult(ApiException::class.java)
            onResponseReceived(account.toString())
        } catch (e: ApiException) {
            Log.w(TAG, "handleSignInResult:error", e)
            onErrorReceived(e.message)
        }
    }

    private fun signIn() {
        val signInIntent = mGoogleSignInClient?.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun signOut() {
        mGoogleSignInClient!!.signOut()
            .addOnCompleteListener(this) {
//                restController.sendSignOut()
            }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.button_sign_in    -> signIn()
            R.id.button_sign_out   -> signOut()
            R.id.button_send_phone -> sendPhoneToAPI()
            R.id.button_send_code  -> sendCodeToAPI()
        }
    }

    private fun sendPhoneToAPI() {
        val phone = tg_first_view.editTextPhone.text.toString()
        if (phone.isNotEmpty()) {
            restController.sendPhone(phone)
        }
    }

    private fun sendCodeToAPI() {
        val code = tg_second_view.editTextCode.text.toString()
        if (code.isNotEmpty()) {
            restController.sendCode(code)
        }
    }

    override fun onResponseReceived(data: String?) {
        if (tg_first_view.visibility  == View.VISIBLE) {
            tg_first_view.visibility  = View.GONE
            tg_second_view.visibility = View.VISIBLE
            return
        }
        if (tg_second_view.visibility == View.VISIBLE) {
            tg_second_view.visibility = View.GONE
            button_sign_in.visibility = View.VISIBLE
            return
        }
        if (button_sign_in.visibility  == View.VISIBLE) {
            button_sign_in.visibility  = View.INVISIBLE
            button_sign_out.visibility = View.VISIBLE
            return
        }
        if (button_sign_out.visibility == View.VISIBLE) {
            button_sign_out.visibility = View.INVISIBLE
            tg_first_view.visibility   = View.VISIBLE
            return
        }
    }

    override fun onErrorReceived(data: String?) {
        if (!data.isNullOrEmpty()) {
            Toast.makeText(
                applicationContext,
                "Received an error: $data",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val RC_SIGN_IN = 9001
    }
}