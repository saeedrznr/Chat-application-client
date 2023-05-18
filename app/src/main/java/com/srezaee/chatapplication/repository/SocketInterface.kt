package com.srezaee.chatapplication.repository

import com.srezaee.chatapplication.model.Message
import com.srezaee.chatapplication.model.ReceiveMessage

interface SocketInterface {
    fun onConnect()
    fun onFailure(message:String?)
    fun onReceive(sender:String,progress:Int,content:ReceiveMessage)
    fun onDisconnected()
}