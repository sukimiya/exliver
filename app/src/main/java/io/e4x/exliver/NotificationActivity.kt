package io.e4x.exliver

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Switch

class NotificationActivity : AppCompatActivity() {
    private lateinit var btnRecord:Switch
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)
    }
}