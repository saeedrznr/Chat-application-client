package com.srezaee.chatapplication.model

import android.os.Build
import java.time.LocalDateTime
import java.util.*

data class Message(
    val type:Int?,// 0 -> text , 1 -> image , 2 -> video , 3 -> voice
    val content:String? ,// text of message
    val sender:String ,
    val time:String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
        "${LocalDateTime.now().hour }:${LocalDateTime.now().minute}"
    }else
        "${Calendar.getInstance().time.hours}:${Calendar.getInstance().time.minutes}"
)
