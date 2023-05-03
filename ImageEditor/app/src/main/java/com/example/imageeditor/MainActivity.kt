package com.example.imageeditor

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.Fragment
import com.example.imageeditor.database.RecordingsDatabase
import com.example.imageeditor.databinding.ActivityMainBinding
import com.example.imageeditor.fragments.CameraFragment
import com.example.imageeditor.fragments.RecordingsFragment
import com.example.imageeditor.fragments.SettingsFragment
import com.example.imageeditor.fragments.SplashFragment
import com.example.imageeditor.utils.AppSettingsProvider

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var splashFragment: SplashFragment

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen().apply {
            setKeepVisibleCondition {
                false
            }
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        database = RecordingsDatabase.getInstance(this)

        AppSettingsProvider.initializeSharedPreferences(this)
        setupFragmentNavigation()
    }

    private fun setupFragmentNavigation() {
        val cameraFragment = CameraFragment()
        val settingsFragment = SettingsFragment()
        val recordingsFragment = RecordingsFragment()

        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container, cameraFragment)
            .add(R.id.fragment_container,settingsFragment)
            .add(R.id.fragment_container,recordingsFragment)
            .hide(cameraFragment)
            .hide(settingsFragment)
            .hide(recordingsFragment)
            .commit()

        binding.bottomNavigationView.visibility = View.GONE

        splashFragment = SplashFragment(
            onExit = {
                supportFragmentManager.beginTransaction()
                    .hide(splashFragment)
                    .show(recordingsFragment)
                    .commit()
                binding.bottomNavigationView.visibility = View.VISIBLE
            }
        )

        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container, splashFragment)
            .commit()


        //binding.bottomNavigationView.selectedItemId = R.id.camera

        binding.bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.camera -> {
                    supportFragmentManager.beginTransaction()
                        .hide(settingsFragment)
                        .hide(recordingsFragment)
                        .show(cameraFragment)
                        .commit()
                }
                R.id.settings -> {
                    supportFragmentManager.beginTransaction()
                        .hide(cameraFragment)
                        .hide(recordingsFragment)
                        .show(settingsFragment)
                        .commit()
                }
                R.id.recordings -> {
                    supportFragmentManager.beginTransaction()
                        .hide(cameraFragment)
                        .hide(settingsFragment)
                        .show(recordingsFragment)
                        .commit()
                }
            }
            true
        }
    }

    companion object {
        init {
            System.loadLibrary("imageeditor")
        }
        lateinit var database: RecordingsDatabase
    }
}