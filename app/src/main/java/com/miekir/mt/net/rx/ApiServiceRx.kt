//package com.miekir.mt.net.rx
//
//import com.miekir.mt.net.BaseResponse
//import io.reactivex.Observable
//import okhttp3.ResponseBody
//import retrofit2.http.GET
//
///**
// * 请求接口，统一规则，BaseUrl以/结尾，具体请求不能以/开头
// * 如果使用NetObserver，则接口的返回类型必须是Observable<BaseResponse></BaseResponse><...>>
// */
//interface ApiServiceRx {
//    @GET("articles/rank")
//    fun testCommonBean(): Observable<ResponseBody>
//
//    @GET("test")
//    fun testNetBean(): Observable<BaseResponse<String>>
//}