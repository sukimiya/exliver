package io.e4x.exliver

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import io.e4x.exliver.model.ActivityBean
import io.e4x.exliver.model.MessageBus
import org.greenrobot.eventbus.EventBus

class ActivityHidden: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val window = window
        window.setGravity(Gravity.LEFT or Gravity.TOP)
        val layoutParams = window.attributes
        layoutParams.x = 0
        layoutParams.y = 0
        layoutParams.width = 1
        layoutParams.height = 1
        layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
        window.attributes = layoutParams
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.e("Activity1", "onActivityResult")
        EventBus.getDefault()
            .post(MessageBus(MessageBus.BACK, ActivityBean(requestCode, resultCode, data)))
        finish()
    }
}