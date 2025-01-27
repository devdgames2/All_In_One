package com.example.All_In_One

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import com.example.all_in_one.R
import com.khs.myutils.MyFileUtils


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        MyFileUtils.showToast(this,"hell0")

    }
}