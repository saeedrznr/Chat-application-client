package com.srezaee.chatapplication.view.fragment

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.srezaee.chatapplication.adapter.MessageAdapter
import com.srezaee.chatapplication.databinding.FragmentChatBinding
import com.srezaee.chatapplication.databinding.SelectFileDialogBinding
import com.srezaee.chatapplication.model.Message
import com.srezaee.chatapplication.model.ReceiveMessage
import com.srezaee.chatapplication.model.SendMessage
import com.srezaee.chatapplication.repository.PathUtil
import com.srezaee.chatapplication.repository.SocketInterface
import com.srezaee.chatapplication.repository.repository


class ChatFragment : Fragment(), SocketInterface {
    lateinit var binding:FragmentChatBinding
    var senderIp:String? = ""
    var selectFileLauncher:ActivityResultLauncher<Intent>? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatBinding.inflate(inflater,container,false)
        repository.socketInterface = this
        selectFileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result->
            val uri = result.data?.data
            if (uri !=null ){
                var type = requireActivity().contentResolver.getType(uri)
                type = type!!.substring(0,type.indexOf('/'))
                val t = when(type) {
                    "image" -> 1
                    "video" ->2
                    "audio" -> 3
                    else -> 0
                }

                val message = SendMessage(senderIp!!, t)
                val path = PathUtil.getPath(requireContext(),uri)
                if (path!=null)
                    sendMessage(message,path)

            }
        }
        (requireActivity() as AppCompatActivity).apply {
            setSupportActionBar(binding.toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowHomeEnabled(true)
        }
        binding.userIpTxt.text = "your ip : ${repository.ip}"
        senderIp = arguments?.getString("ip")
        binding.senderIpTxt.text = "his/her ip : $senderIp"

        binding.recyclerview.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerview.adapter = MessageAdapter(requireContext(), arrayListOf() )
        inintListenners()
        return binding.root
    }



    private fun inintListenners(){

        binding.toolbar.setNavigationOnClickListener{
            it.findNavController().navigateUp()
        }

        binding.messageEtx.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                if (p0.toString().isEmpty()){
                    binding.sendBtn.visibility = View.INVISIBLE
                }else if (repository.socket!!.isClosed){
                    Snackbar.make(binding.relativelayout,"You are disconnected from server",Snackbar.LENGTH_INDEFINITE)
                        .setAction("Try again") {
                            repository.connect()
                            binding.progressbar.visibility = View.VISIBLE
                            binding.sendBtn.visibility = View.INVISIBLE
                        }.show()
                }else{
                    binding.sendBtn.visibility = View.VISIBLE
                }
            }
        })

        binding.sendBtn.setOnClickListener{
            val message = SendMessage(senderIp!!,0)
           sendMessage(message,binding.messageEtx.text.toString().trim())
        }

        binding.fileBtn.setOnClickListener{
            if (Build.VERSION.SDK_INT >= 33){
//                ActivityCompat.requestPermissions(requireActivity(),arrayOf(Manifest.permission.READ_MEDIA_IMAGES,Manifest.permission.READ_MEDIA_AUDIO,Manifest.permission.READ_MEDIA_VIDEO),0)
                val intent = Intent(Intent.ACTION_PICK)
                val selectFileBinding = SelectFileDialogBinding.inflate(layoutInflater)
                val dialog = AlertDialog.Builder(requireContext())
                    .setView(selectFileBinding.root)
                    .create()
                selectFileBinding.apply {
                    imagevideoImg.setOnClickListener{
                        intent.type = "image/* video/*"
                        selectFileLauncher!!.launch(intent)
                        dialog.dismiss()
                    }
                    musicImg.setOnClickListener{
                        intent.type = "audio/*"
                        selectFileLauncher!!.launch(intent)
                        dialog.dismiss()
                    }
                }

                dialog.window?.apply {
                    val wlp = attributes
                    wlp.verticalMargin = 0.35f
                    attributes = wlp
                    setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
                }
                dialog.show()

            }else if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),1)
            }else{
                val intent = Intent(Intent.ACTION_PICK)
                val selectFileBinding = SelectFileDialogBinding.inflate(layoutInflater)
                val dialog = AlertDialog.Builder(requireContext())
                    .setView(selectFileBinding.root)
                    .create()
                selectFileBinding.apply {
                    imagevideoImg.setOnClickListener{
                        intent.type = "image/* video/*"
                        selectFileLauncher!!.launch(intent)
                        dialog.dismiss()
                    }
                    musicImg.setOnClickListener{
                        intent.type = "audio/*"
                        selectFileLauncher!!.launch(intent)
                        dialog.dismiss()
                    }
                }

                dialog.window?.apply {
                    val wlp = attributes
                    wlp.verticalMargin = 0.35f
                    attributes = wlp
                    setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
                }
                dialog.show()

            }
        }
    }


    private fun sendMessage(message:SendMessage, pathOrText:String){
        repository.sendMessage(message,pathOrText) {
            (binding.recyclerview.adapter as MessageAdapter).apply {
                if(messages.isEmpty()){
                    binding.messageEtx.text.clear()
                    messages.add(Message(message.type, pathOrText, "user") to it)
                    notifyDataSetChanged()
                }else if (it < 100) {
                    if (messages[messages.size - 1].second < 100) {
                        messages[messages.size - 1] = Message(null, null, "user") to it
                        notifyItemChanged(messages.size - 1)
                    } else {
                        messages.add(Message(message.type, "", "user") to it)
                    }
                } else {
                    binding.messageEtx.text.clear()
                    if (messages[messages.size-1].second<100){
                        messages[messages.size - 1] = Message(message.type, pathOrText, "user") to 100
                        notifyItemChanged(messages.size - 1)
                    }else messages.add(Message(message.type, pathOrText, "user") to 100)

                    binding.recyclerview.scrollToPosition(messages.size - 1)
                }

            }
        }
    }
    override fun onConnect() {
        repository.listenToServer(requireContext())
        binding.progressbar.visibility = View.GONE
        if (binding.messageEtx.text.toString().isNotEmpty()){
            binding.sendBtn.visibility = View.VISIBLE
        }
    }

    override fun onFailure(message: String?) {
        binding.progressbar.visibility = View.GONE
        Snackbar.make(binding.relativelayout,"Connection Faild",Snackbar.LENGTH_INDEFINITE)
            .setAction("Try again") {
                repository.connect()
                binding.progressbar.visibility = View.VISIBLE
            }.show()
    }


    override fun onReceive(sender:String,progress:Int,content:ReceiveMessage) {
        if (sender==senderIp){
            (binding.recyclerview.adapter as MessageAdapter).apply {
                if (messages.isEmpty()){
                    messages.add(Message(content.type,content.content,sender) to progress)
                     notifyItemChanged(0)
                }else if (progress<100){
                    if (messages[messages.size-1].second<100){
                        messages[messages.size-1] = Message(null,null,sender) to progress
                        notifyItemChanged(messages.size-1)

                    }else{
                        messages.add(Message(null,null,sender) to progress)
                        binding.recyclerview.scrollToPosition(messages.size -1)
                        notifyItemChanged(messages.size-1)
                    }
                }else{
                    if (messages[messages.size-1].second<100){
                        messages[messages.size-1] = (Message(
                            content.type,
                            content.content,
                            content.sender
                        ) to 100)
                        notifyItemChanged(messages.size-1)
                    }else{
                        messages.add(Message(content.type, content.content, content.sender) to 100)
                    }
                }
                binding.recyclerview.scrollToPosition(messages.size -1)
            }
        }
            }


    override fun onDisconnected() {
        binding.sendBtn.visibility = View.INVISIBLE
        Snackbar.make(binding.relativelayout,"Disconnected from server",Snackbar.LENGTH_INDEFINITE)
            .setAction("connect") {
                repository.connect()
                binding.progressbar.visibility = View.VISIBLE
            }.show()
    }


    override fun onDestroy() {
        super.onDestroy()
        binding.recyclerview.adapter = null
    }
}