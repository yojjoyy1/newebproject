package hailuo.com.ezpay.Model

import android.os.Build
import android.text.TextUtils
import android.util.Base64.encode
import android.util.Log
import androidx.annotation.RequiresApi
import java.math.BigInteger
import java.security.MessageDigest

import java.security.SecureRandom
import java.security.Security
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
class ChCrypto {
    // 記得定義一下你的 key
    val key: String = ""
    // 這裡是宣告加解密的方法
    private val transformation = "AES/CBC/PKCS7Padding"
    private val AES = "AES"
    val IV = ""
    val newBPayIV = ""
    val newNPayKey = ""
    fun ByteArray.toHexString() : String {
        return this.joinToString("") {
            java.lang.String.format("%02x", it)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun aesEncode(key:String, iv:String, dataStr:String): String? {
        val cipher = Cipher.getInstance(transformation)
        val keySpec = SecretKeySpec(key.toByteArray(), "AES")
        val ivSpec = IvParameterSpec(iv.toByteArray())
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
        val result = cipher.doFinal(dataStr.toByteArray())
//        Log.v("aaaaa","result hex:${result.toHexString()}")
        return result.toHexString()
    }

    @Throws(Exception::class)
    fun getRawKey(seed: ByteArray?): ByteArray {
        val kgen: KeyGenerator = KeyGenerator.getInstance(AES)
        //for android
        var sr: SecureRandom? = null
        // 在4.2以上版本中，SecureRandom获取方式发生了改变
        //crypto 加密
        sr = SecureRandom.getInstance(transformation)
        sr!!.setSeed(seed)
        kgen.init(256, sr) //256 bits or 128 bits,192bits
        //AES中128位密钥版本有10个加密循环，192比特密钥版本有12个加密循环，256比特密钥版本则有14个加密循环。
        val skey: SecretKey = kgen.generateKey()
        return skey.getEncoded()
    }
    private fun toHex(buf: ByteArray?,iv:String): String? {
        if (buf == null) return ""
        val result = StringBuffer(2 * buf.size)
        for (i in buf.indices) {
            appendHex(result, buf[i],iv)
        }
        return result.toString()
    }

    private fun appendHex(sb: StringBuffer, b: Byte,iv:String) {
        sb.append(iv[b.toInt() shr 4 and 0x0f]).append(iv[b.toInt() and 0x0f])
    }
    private fun parseByte2HexStr(buf: String?): String? {
        val sb = StringBuilder()
        for (i in buf!!.indices) {
            var hex = Integer.toHexString(buf[i].toInt() and 0xFF)
            if (hex.length == 1) {
                hex = "0$hex"
            }
            sb.append(hex.toUpperCase(Locale.ROOT))
        }
        return sb.toString()
    }
    private fun parseHexStr2Byte(hexStr: String): ByteArray? {
        if (hexStr.isEmpty()) return null
        val result = ByteArray(hexStr.length / 2)
        for (i in 0 until hexStr.length / 2) {
            val high = hexStr.substring(i * 2, i * 2 + 1).toInt(16)
            val low = hexStr.substring(i * 2 + 1, i * 2 + 2).toInt(16)
            result[i] = (high * 16 + low).toByte()
        }
        return result
    }
}