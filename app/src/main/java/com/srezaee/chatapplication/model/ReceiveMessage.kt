package com.srezaee.chatapplication.model
data class ReceiveMessage(
    val sender:String,
    val receiver:String,
    val type:Int?,// 0 -> text , 1 -> image , 2 -> video , 3 -> voice
    val size:Long,
    var content:String?,
    val userList:List<String>?
)
