package com.miekir.mt.ui.fragment

import com.miekir.mt.databinding.FragmentTestBinding
import com.miekir.mvvm.core.view.base.BasicActivity
import com.miekir.mvvm.core.view.base.BasicBindingFragment
import com.miekir.mvvm.core.view.base.withLoadingDialog
import com.miekir.mvvm.core.vm.base.viewModel
import com.miekir.mvvm.extension.setSingleClick

class TestFragment: BasicBindingFragment<FragmentTestBinding>() {
    override fun onBindingInflate() = FragmentTestBinding.inflate(layoutInflater)

    private val viewModel by viewModel<TestViewModel>()

    override fun onInit() {
        binding.btnFragment.setSingleClick {
            (requireActivity() as? BasicActivity)?.run {
                withLoadingDialog {
                    viewModel.go()
                }
            }
        }
    }
}