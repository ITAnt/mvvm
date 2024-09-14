package com.miekir.mt.ui

import android.content.Intent
import com.miekir.mt.databinding.ActivityMainBinding
import com.miekir.mvvm.core.view.base.BasicBindingActivity
import com.miekir.mvvm.extension.openActivity
import com.miekir.mvvm.extension.setSingleClick

class SplashActivity : BasicBindingActivity<ActivityMainBinding>() {
    override fun onBindingInflate() = ActivityMainBinding.inflate(layoutInflater)

    override fun onInit() {
        val transitionIntent = Intent(this, SplashActivity::class.java)
        binding.btnTest.setSingleClick {
            //startActivity(transitionIntent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
            openActivity<SplashActivity>()
            //finish()
        }

//        startActivity(Intent(this, JavaActivity::class.java))
        //startActivity(Intent(this, FragmentTestActivity::class.java))
    }
}