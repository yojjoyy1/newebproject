package hailuo.com.ezpay

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import hailuo.com.ezpay.Model.APIService
import hailuo.com.ezpay.Model.ChCrypto
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import java.security.MessageDigest
import java.util.*

class MainActivity : AppCompatActivity() {
    var chCrypto = ChCrypto()
//    var merchantOrderNo:String? = "461608711503020008"
//    var totalAmt:Int? = 299
    var merchantOrderNoEditText:EditText? = null
    var amtEditText:EditText? = null
    var sendBtn:Button? = null
    var resultTextView:TextView? = null
    var resultStr = ""
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
//        sdf.timeZone = TimeZone.getTimeZone("Asia/Taipei")
//        val timeStemp = sdf.format(Date())
        val time = Date().time
        val timeStr = (time/1000).toString()
        merchantOrderNoEditText = findViewById<EditText>(R.id.merchantOrderNoEditText)
        amtEditText = findViewById<EditText>(R.id.amtEditText)
        sendBtn = findViewById<Button>(R.id.sendBtn)
        sendBtn!!.setOnClickListener(View.OnClickListener {
            checkEbpay(timeStr)
        })
        resultTextView = findViewById<TextView>(R.id.resultTextView)


    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val imm: InputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(this.currentFocus!!.windowToken,InputMethodManager.HIDE_NOT_ALWAYS)
        return super.onTouchEvent(event)
    }
    fun sha256(str:String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val result = digest.digest(str.toByteArray())
        return toHex(result)
    }
    fun toHex(byteArray: ByteArray): String {
        val result = with(StringBuilder()) {
            byteArray.forEach {
                val hex = it.toInt() and (0xFF)
                val hexStr = Integer.toHexString(hex)
                if (hexStr.length == 1) {
                    this.append("0").append(hexStr)
                } else {
                    this.append(hexStr)
                }
            }
            this.toString()
        }
        //转成16进制后是32字节
        return result
    }
    fun checkEbpay(timeStamp:String){
        resultStr = ""
        val checkValueStr = "IV=${chCrypto.newBPayIV}&Amt=${amtEditText!!.text}&MerchantID=MS3393834692&MerchantOrderNo=${merchantOrderNoEditText!!.text}&Key=${chCrypto.newNPayKey}"
//        println("checkValueStr:${checkValueStr}")
        val sha256CheckValue = sha256(checkValueStr).toUpperCase()
//        println("sha256CheckValue:${sha256CheckValue}")
        val retrofit = Retrofit.Builder()
            .baseUrl("https://core.newebpay.com")
            .build()
        val service = retrofit.create(APIService::class.java)
        val call = service.postQueryTradeInfo("MS3393834692","1.2","JSON",sha256CheckValue,timeStamp,merchantOrderNoEditText!!.text.toString(),amtEditText!!.text.toString().toInt())
        call.enqueue(object:retrofit2.Callback<ResponseBody>{
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                TODO("Not yet implemented")
            }

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
//                println("result:${response.body()!!.string()}")
                val json = JSONObject(response.body()!!.string())
                println("json:${json}")
                val status = json.getString("Status")
                if (status == "SUCCESS"){
                    val resultJson = json.getJSONObject("Result")
                    val tradeNo = resultJson.getString("TradeNo")
                    val tradeStatus = resultJson.getInt("TradeStatus")
                    var tradeStatusStr = "--"
                    if (tradeStatus == 0){
                        tradeStatusStr = "未付款"
                    }else if (tradeStatus == 1){
                        tradeStatusStr = "付款成功"
                    }else if (tradeStatus == 2){
                        tradeStatusStr = "付款失敗"
                    }else if (tradeStatus == 3){
                        tradeStatusStr = "取消付款"
                    }
                    resultStr = "藍新交易金流交易狀態:${tradeStatusStr}\n"
                    val dataStr = "RespondType=JSON&Version=1.2&TimeStamp=${timeStamp}&SearchType=1&MerchantOrderNo=${tradeNo}&TotalAmt=${amtEditText!!.text}"
                    post(dataStr)
                }else{
                    resultStr = ""
                    resultTextView!!.text = "藍新查詢失敗"
                }
            }

        })
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun post(postDataStr:String) {

        val encode = chCrypto.aesEncode(chCrypto.key,chCrypto.IV,postDataStr)
        println("postDataStr:${encode}")
        // Create Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("https://inv.ezpay.com.tw")
            .build()

        // Create Service
        val service = retrofit.create(APIService::class.java)
        val call = service.createEmployee("317370353",encode!!)
        call.enqueue(object: retrofit2.Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
//                println("response:${response.body()!!.string()}")
                val json = JSONObject(response.body()!!.string())
//                println("json:${json}")
                val status = json.getString("Status")
                if (status == "SUCCESS"){
                    val resultJSStr = json.getString("Result")
                    val resultJS = JSONObject(resultJSStr)
                    val invoiceNumber = resultJS.getString("InvoiceNumber")
                    val buyerName = resultJS.getString("BuyerName")
                    val email = resultJS.getString("BuyerEmail")
                    val invoiceStatus = resultJS.getString("InvoiceStatus")
                    val createTime = resultJS.getString("CreateTime")
                    resultStr += "發票號碼:${invoiceNumber}\n"
                    resultStr += "購買人名稱:${buyerName}\n"
                    resultStr += "購買人信箱:${email}\n"
                    resultStr += "發票狀態:${if (invoiceStatus == "1") "已立開" else "已作廢"}\n"
                    resultStr += "發票時間:${createTime}"
                }else{
                    val resultMsg = json.getString("Message")
                    resultStr += "發票查詢錯誤訊息:${resultMsg}"
                }
                resultTextView!!.text = resultStr

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                TODO("Not yet implemented")
            }
        })
    }
}
