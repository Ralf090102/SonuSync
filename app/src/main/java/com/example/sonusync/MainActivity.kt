package com.example.sonusync

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.sonusync.service.MusicService
import com.example.sonusync.service.ServiceStarter
import com.example.sonusync.viewmodel.MusicViewModel
import com.example.sonusync.ui.settings.SettingsActivity
import com.example.sonusync.viewmodel.EnsembleViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), ServiceStarter {

    private val musicViewModel: MusicViewModel by viewModels()
    private val ensembleViewModel: EnsembleViewModel by viewModels()

    private var isServiceRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        enableEdgeToEdge()

        hideStatusBar()
        addLifecycleObserver()
        if (checkPermission()) {
            musicViewModel.loadMusic()
            ensembleViewModel.loadEnsembles()
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fcvNavHostFragment) as NavHostFragment
        val navController = navHostFragment.navController

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.setupWithNavController(navController)

        bottomNav.setOnApplyWindowInsetsListener(null)
        bottomNav.setPadding(0,0,0,0)

        bottomNav.setOnItemSelectedListener { item ->
            if (item.itemId == R.id.settingsActivity) {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            } else {
                navController.navigate(item.itemId)
                true
            }
        }
    }

    override fun startMusicService() {
        startService()
    }

    private fun hideStatusBar() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        } else {
            window.insetsController?.hide(WindowInsetsCompat.Type.statusBars())
        }
    }

    private fun addLifecycleObserver() {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (!checkPermission()) {
                    requestPermission()
                }
            }
        }
        lifecycle.addObserver(observer)
    }

    private fun startService() {
        if (!isServiceRunning) {
            val intent = Intent(this, MusicService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
            isServiceRunning = true
        }
    }

    private fun checkPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, arrayOf(
                android.Manifest.permission.READ_MEDIA_AUDIO,
                android.Manifest.permission.POST_NOTIFICATIONS
            ), 100)
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 100)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                musicViewModel.insertMusic()
                ensembleViewModel.insertEnsembles()
            } else {
                Toast.makeText(this, "All permissions are needed to access media files and show notifications", Toast.LENGTH_SHORT).show()
            }
        }
    }
}