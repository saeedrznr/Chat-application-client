package com.srezaee.chatapplication.view.fragment

import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.Spanned
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.srezaee.chatapplication.adapter.UserAdapter
import com.srezaee.chatapplication.databinding.FragmentHomeBinding
import com.srezaee.chatapplication.databinding.GetHostIpDialogBinding
import com.srezaee.chatapplication.model.Message
import com.srezaee.chatapplication.model.ReceiveMessage
import com.srezaee.chatapplication.repository.SocketInterface
import com.srezaee.chatapplication.repository.repository


class HomeFragment : Fragment(), SocketInterface {

  var binding:FragmentHomeBinding? = null
    var getHostIpDialog : AlertDialog? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding?: run {

            binding = FragmentHomeBinding.inflate(layoutInflater,container,false)
            binding!!.recyclerview.layoutManager = LinearLayoutManager(requireContext())
            binding!!.recyclerview.adapter = UserAdapter(requireContext(), repository.userList)
        }

        (binding!!.recyclerview.adapter as UserAdapter).apply {
            userList = repository.userList
            notifyDataSetChanged()
        }

        repository.socketInterface = this
        if (repository.socket == null || repository.socket?.isClosed == true){
            repository.connect( this@HomeFragment)
            binding!!.disconnectImg.visibility = View.INVISIBLE
            binding!!.stateProgress.visibility = View.VISIBLE
        }

        return binding!!.root
    }




    override fun onConnect() {
        repository.listenToServer(requireContext())
        binding!!.stateProgress.visibility = View.GONE
        binding!!.conndectImg.visibility = View.VISIBLE
    }

    override fun onFailure(message: String?) {
        binding!!.stateProgress.visibility = View.GONE
        binding!!.disconnectImg.visibility = View.VISIBLE
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        getHostIpDialog?.show()
    }


    override fun onReceive(sender:String,progress:Int,message:ReceiveMessage) {
        if (message.sender=="server" ){
            repository.ip = message.receiver
            binding!!.ipTxt.text = "Your ip :${message.receiver}"
            (binding!!.recyclerview.adapter as UserAdapter).apply {
                userList = repository.userList
                notifyDataSetChanged()
            }
        }
    }

    override fun onDisconnected() {
        binding!!.conndectImg.visibility = View.GONE
        binding!!.disconnectImg.visibility = View.VISIBLE
    }

}

