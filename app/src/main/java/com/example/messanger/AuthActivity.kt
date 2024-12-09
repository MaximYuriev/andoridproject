package com.example.messanger

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayout.Tab
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.launch
import request.Client




class AuthActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId", "ShowToast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_auth)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val client = Client
        val blue: Int = Color.parseColor("#007bff")
        val grey: Int = Color.parseColor("#000000")
        val tabLay: TabLayout = findViewById(R.id.tabLayout1)
        val authPage: FrameLayout = findViewById(R.id.authPage)
        val regPage: FrameLayout = findViewById(R.id.regPage)
        val btnAuth: Button = findViewById(R.id.btnAuth)
        val btnReg: Button = findViewById(R.id.btnReg)
        val authTab: Tab? = tabLay.getTabAt(0)
        val regTab: Tab? = tabLay.getTabAt(1)
        val textAuth: TextView = findViewById(R.id.textAuth)
        val textReg: TextView = findViewById(R.id.textReg)
        val email: EditText = findViewById(R.id.emailInput)
        val password: EditText = findViewById(R.id.passwordInput)
        val emailReg: EditText = findViewById(R.id.emailReg)
        val username: EditText = findViewById(R.id.username)
        val firstname: EditText = findViewById(R.id.firstname)
        val lastname: EditText = findViewById(R.id.lastname)
        val passwordReg: EditText = findViewById(R.id.pswReg)
        val passwordRegRep: EditText = findViewById(R.id.pswRegRep)
        val intent = Intent(this, MainActivity::class.java)
        tabLay.setTabTextColors(grey, blue)
        tabLay.setSelectedTabIndicatorColor(blue)

        btnAuth.setOnClickListener{
            if (email.text.toString() == "")
                textAuth.text = "E-mail не может быть пустым"
            else if (password.text.toString() == "")
                textAuth.text = "Пароль не может быть пустым"
            else
                lifecycleScope.launch {
                    val authResponse = client.authRequest(email.text.toString(),password.text.toString())
                    if (authResponse["status"] != HttpStatusCode.OK)
                        textAuth.text = authResponse["detail"].toString()
                    else {
                        startActivity(intent)
                        finish()
                    }
                }
        }

        btnReg.setOnClickListener{
            if (emailReg.text.toString() == "")
                textReg.text = "E-mail не может быть пустым"
            else if(username.text.toString() == "")
                textReg.text = "Имя пользователя не может быть пустым"
            else if(passwordReg.text.toString() == "")
                textReg.text = "Пароль не может быть пустым"
            else if(passwordRegRep.text.toString() == "")
                textReg.text = "Поле повторите пароль не может быть пустым"
            else if(passwordReg.text.toString() != passwordRegRep.text.toString())
                textReg.text = "Пароли должны совпадать"
            else
                lifecycleScope.launch {
                    val regResponse = client.regRequest(
                        emailReg.text.toString(),
                        username.text.toString(),
                        passwordReg.text.toString(),
                        firstname.text.toString(),
                        lastname.text.toString()
                    )
                    textReg.text = regResponse
                }
        }

        tabLay.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: Tab) {
                if(tab == authTab) {
                    regPage.visibility = FrameLayout.GONE
                    authPage.visibility = FrameLayout.VISIBLE
                }
                else{
                    authPage.visibility = FrameLayout.GONE
                    regPage.visibility = FrameLayout.VISIBLE
                }
            }

            override fun onTabUnselected(tab: Tab) {
            }

            override fun onTabReselected(tab: Tab) {
            }
        })
    }

}