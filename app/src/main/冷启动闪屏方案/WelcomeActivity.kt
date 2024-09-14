package com.ba.wm.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.ba.wm.R

package com.ba.wm.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.ba.wm.R

/**
 * 欢迎界面
 */
class WelcomeActivity: AppCompatActivity() {
    // 采用AutoSize屏幕适配方案会闪动一下
//class WelcomeActivity: BasicActivity<ActivityWelcomeBinding>() {
    private lateinit var view_welcome: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)
        view_welcome = findViewById<View>(R.id.view_welcome)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            val mainIntent = Intent(this, MachineActivity::class.java)
            // 必须加载完毕再启动，否则会闪屏
            startActivity(mainIntent)
            overridePendingTransition(0, 0)
            finish()
        }
    }

    override fun onBackPressed() {}
}