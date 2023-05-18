package com.srezaee.chatapplication.model
data class SendMessage(
    val receiver:String,
    val type:Int,// 0 -> text , 1 -> image , 2 -> video , 3 -> audio
    var size:Long = 0
)
