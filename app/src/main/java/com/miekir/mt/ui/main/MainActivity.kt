package com.miekir.mt.ui.main

import android.Manifest
import com.miekir.mt.databinding.ActivityMainBinding
import com.miekir.mvvm.core.view.base.BasicBindingActivity
import com.miekir.mvvm.core.view.base.withLoadingDialog
import com.miekir.mvvm.core.vm.base.viewModel
import com.miekir.mvvm.extension.openActivity
import com.miekir.mvvm.log.L
import com.miekir.mvvm.tools.ToastTools

class MainActivity : BasicBindingActivity<ActivityMainBinding>(), IMainView {

    private val mainPresenter: MainViewModel by viewModel()

    override fun onInit() {
//        ActivityTools.swipeActivity(this)

        /*this.javaClass::class.java.declaredFields.forEach {
            L.d("ssss ${it.type.name}")
        }*/
        requestPermissionsForResult(arrayListOf(Manifest.permission.CAMERA)) { grant, temp, detail ->
            if (grant) {
                ToastTools.showShort("granted")
            } else {
                ToastTools.showShort("deny")
            }
        }

        binding.btnTest.setOnClickListener {
            withLoadingDialog {
                mainPresenter.testFast()
            }
        }

        openActivity<WebActivity>()
    }

    override fun onMainTaskCallback() {

    }

    /**
     * 如果一切正常，则errorMessage为空
     */
    override fun onLoginResult(
        success: Boolean,
        result: String?,
        code: Int,
        errorMessage: String?) {
        if (!success) {
            ToastTools.showShort(errorMessage)
            return
        }
        L.d("result:$result")
        ToastTools.showShort(errorMessage)
    }

    override fun onBindingInflate() = ActivityMainBinding.inflate(layoutInflater)

    override fun onDestroy() {
        super.onDestroy()
    }
}