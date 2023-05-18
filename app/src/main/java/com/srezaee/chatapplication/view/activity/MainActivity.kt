package com.srezaee.chatapplication.view.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

import com.srezaee.chatapplication.databinding.ActivityMainBinding

lateinit var binding: ActivityMainBinding
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}