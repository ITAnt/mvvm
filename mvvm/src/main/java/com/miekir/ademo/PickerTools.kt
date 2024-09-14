//package com.miekir.backup
//
//import android.app.Dialog
//import android.content.Context
//import android.view.Gravity
//import android.view.ViewGroup
//import android.view.Window
//import android.widget.FrameLayout
//import com.bigkoo.pickerview.builder.OptionsPickerBuilder
//import com.bigkoo.pickerview.builder.TimePickerBuilder
//import com.blankj.utilcode.util.ToastUtils
//import java.util.*
//import kotlin.collections.ArrayList
//
///**
// * @date : 2021/3/23 10:50
// * @author : 詹子聪
// *
// */
//object PickerUtil {
//    /**
//     * @param isFrom true：起始时间，false：结束时间
//     */
//    fun showDatePicker(context:Context,  fromDate: Date, toDate: Date, isFrom: Boolean = true, datePickerListener:()->Unit) {
//        val pvTime = TimePickerBuilder(context) { date, _ ->
//            if (isFrom) {
//                fromDate.time = date.time
//                showDatePicker(context, fromDate, toDate,false, datePickerListener)
//            } else {
//                toDate.time = date.time
//                if (toDate.before(fromDate)) {
//                    ToastUtils.showShort("结束日期不能比起始日期早")
//                } else {
//                    datePickerListener.invoke()
//                }
//            }
//        }
//            //分别对应年月日时分秒，默认全部显示
//            .setType(booleanArrayOf(true, true, true, false, false, false))
//            .isDialog(true)
//            .setOutSideCancelable(false).apply {
//                val calendar = Calendar.getInstance();
//                if (isFrom) {
//                    calendar.time = fromDate
//                } else {
//                    calendar.time = toDate
//                }
//                setDate(calendar)
//            }.setRangDate(Calendar.getInstance().apply { add(Calendar.YEAR, -100) }, Calendar.getInstance())
//            .setTitleText(if (isFrom) "起始时间" else "结束时间")
//            .build()
//
//        val mDialog: Dialog? = pvTime.dialog
//        if (mDialog != null) {
//            val params = FrameLayout.LayoutParams(
//                ViewGroup.LayoutParams.MATCH_PARENT,
//                ViewGroup.LayoutParams.WRAP_CONTENT,
//                Gravity.BOTTOM
//            )
//            params.leftMargin = 0
//            params.rightMargin = 0
//            pvTime.dialogContainerLayout.layoutParams = params
//            val dialogWindow: Window? = mDialog.window
//            if (dialogWindow != null) {
//                dialogWindow.setWindowAnimations(com.bigkoo.pickerview.R.style.picker_view_slide_anim)
//                dialogWindow.setGravity(Gravity.BOTTOM)
//            }
//        }
//        pvTime.show()
//    }
//
//    /**
//     * 选择开始时间和结束时间
//     */
//    fun showDatePicker(context:Context, datePickerListener:(start:Date, end:Date)->Unit, fromDate: Date? = null) {
//        val pvTime = TimePickerBuilder(context) { date, _ ->
//            if (fromDate == null) {
//                val startDate = Date()
//                startDate.time = date.time
//                showDatePicker(context, datePickerListener, startDate)
//            } else {
//                val endDate = Date()
//                endDate.time = date.time
//                if (endDate.before(fromDate)) {
//                    ToastUtils.showShort("结束日期不能比起始日期早")
//                } else {
//                    datePickerListener.invoke(fromDate, endDate)
//                }
//            }
//        }
//            //分别对应年月日时分秒，默认全部显示
//            .setType(booleanArrayOf(true, true, true, false, false, false))
//            .isDialog(true)
//            .setOutSideCancelable(false)
//            .setRangDate(Calendar.getInstance().apply
//            {
//                if (fromDate == null) {
//                    add(Calendar.YEAR, -100)
//                } else {
//                    time = fromDate
//                }
//            },  Calendar.getInstance())
//            .setTitleText(if (fromDate == null) "起始时间" else "结束时间")
//            .build()
//        pvTime.setDate(Calendar.getInstance())
//
//        val mDialog: Dialog? = pvTime.dialog
//        if (mDialog != null) {
//            val params = FrameLayout.LayoutParams(
//                ViewGroup.LayoutParams.MATCH_PARENT,
//                ViewGroup.LayoutParams.WRAP_CONTENT,
//                Gravity.BOTTOM
//            )
//            params.leftMargin = 0
//            params.rightMargin = 0
//            pvTime.dialogContainerLayout.layoutParams = params
//            val dialogWindow: Window? = mDialog.window
//            if (dialogWindow != null) {
//                dialogWindow.setWindowAnimations(com.bigkoo.pickerview.R.style.picker_view_slide_anim)
//                dialogWindow.setGravity(Gravity.BOTTOM)
//            }
//        }
//        pvTime.show()
//    }
//
//    /**
//     * 选择一个日期
//     */
//    fun showOneDatePicker(context:Context,  datePickerListener:(selectDate: Date)->Unit) {
//        val pvTime = TimePickerBuilder(context) { date, _ ->
//            datePickerListener.invoke(date)
//        }
//            //分别对应年月日时分秒，默认全部显示
//            .setType(booleanArrayOf(true, true, true, false, false, false))
//            .isDialog(true)
//            .setOutSideCancelable(false)
//            .build()
//        pvTime.setDate(Calendar.getInstance())
//
//        val mDialog: Dialog? = pvTime.dialog
//        if (mDialog != null) {
//            val params = FrameLayout.LayoutParams(
//                ViewGroup.LayoutParams.MATCH_PARENT,
//                ViewGroup.LayoutParams.WRAP_CONTENT,
//                Gravity.BOTTOM
//            )
//            params.leftMargin = 0
//            params.rightMargin = 0
//            pvTime.dialogContainerLayout.layoutParams = params
//            val dialogWindow: Window? = mDialog.window
//            if (dialogWindow != null) {
//                dialogWindow.setWindowAnimations(com.bigkoo.pickerview.R.style.picker_view_slide_anim)
//                dialogWindow.setGravity(Gravity.BOTTOM)
//            }
//        }
//        pvTime.show()
//    }
//
//    fun showSexPicker(context: Context, listener: (sex:String)->Unit) {
//        val sexList = ArrayList<String>()
//        sexList.add("男");
//        sexList.add("女")
//        showSinglePicker(context, sexList, listener)
//    }
//    /**
//     * 单选对话框
//     */
//    fun showSinglePicker(context: Context, itemList:List<String>, listener: (selectedString:String)->Unit) {
//        val pickerView = OptionsPickerBuilder(context) { options1, _, _, _ ->
//            listener.invoke(itemList[options1])
//        }.build<String>()
//        pickerView.setPicker(itemList)
//        pickerView.show()
//    }
//}