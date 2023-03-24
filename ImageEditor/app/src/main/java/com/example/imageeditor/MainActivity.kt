package com.example.imageeditor

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.imageeditor.databinding.ActivityMainBinding
import com.example.imageeditor.fragments.MainFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val fragment: MainFragment = MainFragment.newInstance()

        if(savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, fragment, "MainFragment")
                .commit()
        }
    }

    companion object {
        // Used to load the 'imageeditor' library on application startup.
        init {
            System.loadLibrary("imageeditor")
        }
    }
}