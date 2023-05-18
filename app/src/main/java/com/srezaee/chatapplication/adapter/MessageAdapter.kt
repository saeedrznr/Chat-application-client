package com.srezaee.chatapplication.adapter

import android.content.Context
import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import android.widget.SeekBar
import android.widget.VideoView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.srezaee.chatapplication.R
import com.srezaee.chatapplication.databinding.ReceiveMessageItemBinding
import com.srezaee.chatapplication.databinding.ReceiveMessageProgressItemBinding
import com.srezaee.chatapplication.databinding.SendMessageItemBinding
import com.srezaee.chatapplication.databinding.SendMessageProgressItemBinding
import com.srezaee.chatapplication.model.Message
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*


class MessageAdapter(val context: Context,val messages:ArrayList<Pair<Message,Int>>):Adapter<ViewHolder>() {
   private val RECEIVE_MESSAGE = 0
   private val SEND_MESSAGE = 1
   private val SEND_MESSAGE_PRO = 2
   private val RECEIVE_MESSAGE_PRO = 3
   private var nowPlayingMusic:MediaPlayer? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when(viewType){
            RECEIVE_MESSAGE -> ReceiveHolder(ReceiveMessageItemBinding.inflate(LayoutInflater.from(context),parent,false))
            RECEIVE_MESSAGE_PRO -> ReceiveProHolder(ReceiveMessageProgressItemBinding.inflate(LayoutInflater.from(context),parent,false))
            SEND_MESSAGE -> SendHolder(SendMessageItemBinding.inflate(LayoutInflater.from(context),parent,false))
            else -> SendProHolder(SendMessageProgressItemBinding.inflate(LayoutInflater.from(context),parent,false))
        }

    }

    override fun getItemCount(): Int {
       return messages.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val m = messages[position]
        val message = m.first
        when(holder){
            is ReceiveHolder->{
                when(message.type){
                  0->  {// text message
                      holder.binding.apply {
                          audio.visibility = View.GONE
                          video.visibility = View.GONE
                          image.visibility = View.GONE
                          text.visibility = View.VISIBLE
                      }
                        holder.binding.text.text = message.content
                        holder.binding.timeTxt.text = message.time
                    }
                    1->{//image message
                        holder.binding.apply {
                            text.visibility = View.GONE
                            audio.visibility = View.GONE
                            video.visibility = View.GONE
                            image.visibility = View.VISIBLE
                        }
                            Glide.with(context).load(message.content).into(holder.binding.image)
                            holder.binding.timeTxt.text = message.time
                        }
                    2-> {// video message
                        holder.binding.apply {
                            text.visibility = View.GONE
                            image.visibility = View.GONE
                            audio.visibility = View.GONE
                            video.visibility = View.VISIBLE
                            video.setVideoPath(message.content)
                            val mediaController = MediaController(context)
                            mediaController.setAnchorView(video)
                            video.setMediaController(mediaController)
                            video.seekTo(1)
                        }

                    }
                    3 -> {
                        holder.binding.apply {
                            text.visibility = View.GONE
                            image.visibility = View.GONE
                            video.visibility = View.GONE
                            audio.visibility = View.VISIBLE
                            val mediaplayer = MediaPlayer()
                            var timer: Timer? = null
                            mediaplayer.setDataSource(message.content)
                            mediaplayer.prepare()
                            mediaplayer.setOnPreparedListener{
                                endtimeTxt.text = String.format("%02d:%02d",mediaplayer.duration/1000/60,(mediaplayer.duration/1000)%60)
                                seekbar.max = mediaplayer.duration
                                playImg.setOnClickListener{
                                    if (mediaplayer.isPlaying){
                                        mediaplayer.pause()
                                        timer?.cancel()
                                        Glide.with(context).load(R.drawable.play_ic).into(playImg)
                                    }else{
                                        if(nowPlayingMusic != mediaplayer) nowPlayingMusic?.pause()
                                        if (mediaplayer.currentPosition==mediaplayer.duration){
                                            mediaplayer.seekTo(0)
                                        }
                                        mediaplayer.start()
                                        Glide.with(context).load(R.drawable.pause_ic).into(playImg)
                                        nowPlayingMusic = mediaplayer
                                        timer = Timer()
                                        timer!!.schedule(object : TimerTask() {
                                            override fun run() {
                                                val seconds =  (mediaplayer.currentPosition/1000)%60
                                                val minutes =  mediaplayer.currentPosition/1000/60
                                                CoroutineScope(Dispatchers.Main).launch {
                                                    currenttimeTxt.text = String.format("%02d:%02d", minutes,seconds)
                                                    seekbar.progress = mediaplayer.currentPosition
                                                    if (!mediaplayer.isPlaying){
                                                        timer?.cancel()
                                                        Glide.with(context).load(R.drawable.play_ic).into(playImg)
                                                    }
                                                }

                                            }
                                        },0,300)
                                    }
                                }
                            }
                            mediaplayer.setOnCompletionListener {
                                timer?.cancel()
                                Glide.with(context).load(R.drawable.play_ic).into(playImg)
                            }
                            seekbar.setOnSeekBarChangeListener(object :SeekBar.OnSeekBarChangeListener{
                                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                                    if(p2){
                                        mediaplayer.seekTo(p1)
                                    }
                                }

                                override fun onStartTrackingTouch(p0: SeekBar?) {

                                }

                                override fun onStopTrackingTouch(p0: SeekBar?) {

                                }

                            })



                        }
                        holder.binding.timeTxt.text = message.time
                    }

                }
            }

            is ReceiveProHolder -> {
                holder.binding.progress.progress = m.second
                holder.binding.progressTxt.text = "${m.second} %"
            }
            is SendHolder ->{
                when(message.type){
                    0-> {// text message
                        holder.binding.apply {
                            video.visibility = View.GONE
                            image.visibility = View.GONE
                            audio.visibility = View.GONE
                            text.visibility = View.VISIBLE
                        }
                        holder.binding.text.text = message.content
                        holder.binding.timeTxt.text = message.time
                    }
                    1->{//image message
                        holder.binding.apply {
                            text.visibility = View.GONE
                            video.visibility = View.GONE
                            audio.visibility = View.GONE
                            image.visibility = View.VISIBLE
                        }
                        Glide.with(context).load(message.content).into(holder.binding.image)
                        holder.binding.timeTxt.text = message.time
                    }
                    2->{// video message
                        holder.binding.apply {
                            text.visibility = View.GONE
                            image.visibility = View.GONE
                            audio.visibility = View.GONE
                            video.visibility = View.VISIBLE
                            video.setVideoPath(message.content)
                            val mediaController = MediaController(context)
                            mediaController.setAnchorView(video)
                            video.setMediaController(mediaController)
                            video.seekTo(1)
                        }
                        holder.binding.timeTxt.text = message.time
                    }
                    3 -> {
                        holder.binding.apply {
                            text.visibility = View.GONE
                            image.visibility = View.GONE
                            video.visibility = View.GONE
                            audio.visibility = View.VISIBLE
                            val mediaplayer = MediaPlayer()
                            var timer: Timer? = null
                            mediaplayer.setDataSource(message.content)
                            mediaplayer.prepare()
                            mediaplayer.setOnPreparedListener{
                                endtimeTxt.text = String.format("%02d:%02d",mediaplayer.duration/1000/60,(mediaplayer.duration/1000)%60)
                                seekbar.max = mediaplayer.duration
                                playImg.setOnClickListener{
                                    if (mediaplayer.isPlaying){
                                        mediaplayer.pause()
                                        timer?.cancel()
                                        Glide.with(context).load(R.drawable.play_ic).into(playImg)
                                    }else{
                                       if(nowPlayingMusic != mediaplayer) nowPlayingMusic?.pause()
                                        if (mediaplayer.currentPosition==mediaplayer.duration){
                                            mediaplayer.seekTo(0)
                                        }
                                        mediaplayer.start()
                                        Glide.with(context).load(R.drawable.pause_ic).into(playImg)
                                        nowPlayingMusic = mediaplayer
                                        timer = Timer()
                                        timer!!.schedule(object : TimerTask() {
                                            override fun run() {
                                                val seconds =  (mediaplayer.currentPosition/1000)%60
                                                val minutes =  mediaplayer.currentPosition/1000/60
                                                CoroutineScope(Dispatchers.Main).launch {
                                                    currenttimeTxt.text = String.format("%02d:%02d", minutes,seconds)
                                                    seekbar.progress = mediaplayer.currentPosition
                                                    if (!mediaplayer.isPlaying){
                                                        timer?.cancel()
                                                        Glide.with(context).load(R.drawable.play_ic).into(playImg)
                                                    }
                                                }

                                            }
                                        },0,300)
                                    }
                                }
                            }
                            mediaplayer.setOnCompletionListener {
                                timer?.cancel()
                                Glide.with(context).load(R.drawable.play_ic).into(playImg)
                            }
                            seekbar.setOnSeekBarChangeListener(object :SeekBar.OnSeekBarChangeListener{
                                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                                    if(p2){
                                        mediaplayer.seekTo(p1)
                                    }
                                }

                                override fun onStartTrackingTouch(p0: SeekBar?) {

                                }

                                override fun onStopTrackingTouch(p0: SeekBar?) {

                                }

                            })



                        }
                        holder.binding.timeTxt.text = message.time
                    }
                }
            }

            is SendProHolder ->{
                holder.binding.progress.progress = m.second
                holder.binding.progressTxt.text = "${m.second} %"
            }

        }

    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].first.sender == "user"){
            if(messages[position].second==100) SEND_MESSAGE
            else SEND_MESSAGE_PRO
        }
        else {
            if (messages[position].second==100) RECEIVE_MESSAGE
            else RECEIVE_MESSAGE_PRO
        }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        nowPlayingMusic?.stop()
    }


   private class ReceiveHolder(val binding: ReceiveMessageItemBinding):ViewHolder(binding.root)
   private class ReceiveProHolder(val binding:ReceiveMessageProgressItemBinding):ViewHolder(binding.root)
   private class SendHolder(val binding:SendMessageItemBinding):ViewHolder(binding.root)
   private class SendProHolder(val binding:SendMessageProgressItemBinding):ViewHolder(binding.root)

}