package com.srezaee.chatapplication.adapter

import android.content.Context
import android.graphics.Path.Direction
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.srezaee.chatapplication.R
import com.srezaee.chatapplication.databinding.UserItemBinding
import com.srezaee.chatapplication.view.fragment.ChatFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class UserAdapter(val context: Context, var userList:List<String>):Adapter<UserAdapter.Holder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(UserItemBinding.inflate(LayoutInflater.from(context),parent,false))
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.binding.ip = userList[position]
        holder.binding.rootRelative.setOnClickListener{
            runBlocking{
                launch {
                    it.isPressed = true
                    delay(100)
                    it.isPressed = false
                }
                launch {


                   it.findNavController().navigate(R.id.action_homeFragment_to_chatFragment,
                       bundleOf("ip" to userList[position])
                   )
                }
            }
        }
    }

    class Holder(val binding:UserItemBinding):ViewHolder(binding.root)
}