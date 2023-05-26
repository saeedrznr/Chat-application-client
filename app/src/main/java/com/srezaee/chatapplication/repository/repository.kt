package com.srezaee.chatapplication.repository

import android.content.Context
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.os.EnvironmentCompat
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.srezaee.chatapplication.model.ReceiveMessage
import com.srezaee.chatapplication.model.SendMessage
import kotlinx.coroutines.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.Socket
import java.net.SocketException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

object repository {
    const val PORT = 9998
    const val HOST = "Your host ip"//todo:set your host ip
    const val BUFFER_SIZE = 16*1024
    var ip = ""
    var userList : List<String> = listOf()
    var socketInterface: SocketInterface? = null

    var socket: Socket? = null

    fun connect(){
        connect(socketInterface!!)
    }
    fun connect(socketInter: SocketInterface) {
        this.socketInterface = socketInter
        CoroutineScope(Dispatchers.Main).launch {
            var message: String? = null
            socket = CoroutineScope(Dispatchers.IO).async {
                try {
                    return@async Socket(HOST, PORT)
                } catch (ex: Exception) {
                    message = ex.message
                    return@async null
                }
            }.await()
            if (socket != null) {
                socketInterface!!.onConnect()
            } else {
                socketInterface!!.onFailure(message)
            }

        }
    }

    fun listenToServer(context: Context) {
        var t = ""
        CoroutineScope(Dispatchers.IO).launch {
            while (true){
                try {
                    val buffer = ByteArray(BUFFER_SIZE)
                    val inputStream = socket!!.getInputStream()
                    var bytes = inputStream.read(buffer)
                    val bufstr = String(buffer.copyOfRange(0,bytes))
                    t = bufstr.substring(3, bufstr.length - 3)
                    if (bufstr.startsWith("-->")) {
                        val message = Gson().fromJson<ReceiveMessage>(
                            bufstr.substring(3, bufstr.length - 3),
                            ReceiveMessage::class.java
                        )

                        var size = message.size
                        if (message.sender=="server"){
                            userList = message.userList!!
                            CoroutineScope(Dispatchers.Main).launch { socketInterface!!.onReceive(message.sender,100,message) }
                        }
                        else if (message.type==0){
                            var text = ""
                            bytes = inputStream.read(buffer, 0, Math.min(buffer.size.toLong(), size).toInt())
                            while (size > 0 && bytes != -1) {
                                text += String(buffer.copyOfRange(0,bytes))
                                size -= bytes
                                bytes = inputStream.read(buffer, 0, Math.min(buffer.size.toLong(), size).toInt())
                            }
                            message.content = text
                            CoroutineScope(Dispatchers.Main).launch { socketInterface!!.onReceive(message.sender,100,message) }
                        }else{
                           val fileExtension = when (message.type) {
                                1 -> "jpeg"
                                2 -> "mp4"
                                3 -> "mp3"
                                else -> "pdf"
                            }
                            val file = File(context.cacheDir.path)
                            val fullpath:String
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                                val dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
                                fullpath = file.path+"/"+LocalDateTime.now().format(dtf)+Random().nextInt().toString()+"."+fileExtension
                            }else fullpath = file.path+"/"+SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().time)+Random().nextInt().toString()+"."+fileExtension

                            val fileOutputStream = FileOutputStream(fullpath)
                            bytes = inputStream.read(buffer, 0, Math.min(buffer.size.toLong(), size).toInt())
                            while (size > 0 && bytes != -1) {
                                fileOutputStream.write(buffer, 0, bytes)
                                fileOutputStream.flush()
                                size -= bytes
                                bytes = inputStream.read(buffer, 0, Math.min(buffer.size.toLong(), size).toInt())
                                message.content = fullpath
                                withContext(Dispatchers.Main){ socketInterface!!.onReceive(message.sender,(100*(message.size-size)/message.size).toInt(),message)}
                            }
                            fileOutputStream.close()
                        }

                    }
                }catch (e:SocketException){
                    socket!!.close()
                    CoroutineScope(Dispatchers.Main).launch{ socketInterface!!.onDisconnected() }
                    break
                }catch (e:JsonSyntaxException){
                    Log.e("Json Errrrrr",t)
                }catch (e:IllegalArgumentException){

                }

            }
        }
    }


    fun sendMessage(message:SendMessage, pathOrtext:String, callBack:(progress:Int)->Unit){
        if (!socket!!.isClosed){
           CoroutineScope(Dispatchers.IO).launch {
               try {
                   val os = socket!!.getOutputStream()
                   if (message.type == 0) {
                       var indx = 0
                       val byteArray = pathOrtext.toByteArray()
                       message.size = byteArray.size.toLong()
                       val m = "-->${Gson().toJson(message)}<--".toByteArray()
                       os.write(m)
                       os.flush()
                       while (indx < byteArray.size) {
                           if (byteArray.size - indx > BUFFER_SIZE) {
                               os.write(byteArray.copyOfRange(indx, indx + BUFFER_SIZE))
                               os.flush()
                           } else {
                               os.write(byteArray.copyOfRange(indx, byteArray.size))
                               os.flush()
                           }
                           indx += BUFFER_SIZE
                       }
                       withContext(CoroutineScope(Dispatchers.Main).coroutineContext) {
                           callBack(100)
                       }
                   } else {
                       val file = File(pathOrtext)
                       message.size = file.length()
                       val m = "-->${Gson().toJson(message)}<--".toByteArray()
                       os.write(m)
                       os.flush()
                       var remaininglength = message.size
                       val fis = FileInputStream(file)
                       val buffer = ByteArray(BUFFER_SIZE)
                       var bytes = 0
                       bytes = fis.read(buffer)
                       while (bytes != -1) {
                           os.write(buffer, 0, bytes)
                           os.flush()
                           remaininglength -= bytes
                           bytes = fis.read(buffer)
                           withContext(CoroutineScope(Dispatchers.Main).coroutineContext) {
                               callBack(
                                   (100 * (message.size - remaininglength) / message.size).toInt()
                               )
                           }
                       }

                   }
               } catch (e: Exception) {
                   socket!!.close()
                   Log.e("Disconnect server",e.message.toString())
                   CoroutineScope(Dispatchers.Main).launch { socketInterface!!.onDisconnected() }
               }
           }
        }
    }


}