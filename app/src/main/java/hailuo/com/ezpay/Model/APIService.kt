package hailuo.com.ezpay.Model

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface APIService {
    @FormUrlEncoded
    @Headers("Content-Type: application/x-www-form-urlencoded; charset=utf-8")
    @POST("/Api/invoice_search")
    fun createEmployee(@Field("MerchantID_") MerchantID_:String, @Field("PostData_") PostData_:String): Call<ResponseBody>

    @FormUrlEncoded
    @Headers("Content-Type: application/x-www-form-urlencoded; charset=utf-8")
    @POST("/API/QueryTradeInfo")
    fun postQueryTradeInfo(@Field("MerchantID") MerchantID:String, @Field("Version") Version:String,@Field("RespondType") RespondType:String,@Field("CheckValue")CheckValue:String,@Field("TimeStamp")TimeStamp:String,@Field("MerchantOrderNo")MerchantOrderNo:String,@Field("Amt")Amt:Int): Call<ResponseBody>
}