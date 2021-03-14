package io.e4x.exliver

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Switch

class RecordForgroundServiceActivity : AppCompatActivity() {
    lateinit var btnRecord: Switch
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record_forground_service)
        btnRecord = findViewById(R.id.recordSwitcher)
    }
}