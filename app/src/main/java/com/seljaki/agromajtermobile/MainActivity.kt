package com.seljaki.agromajtermobile

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.seljaki.agromajtermobile.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        if(checkNotificationPermissions(this)) {
            val serviceIntent: Intent = Intent(this, BlockchainService::class.java)
            ContextCompat.startForegroundService(this, serviceIntent)
        }

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController: NavController = navHostFragment.navController
        //binding.bottomNavigationView.setupWithNavController(navController)

        binding.bottomNavigationView.setOnNavigationItemSelectedListener {
            val navController = binding.fragmentContainerView.findNavController()
            when(it.itemId) {
                R.id.chainListFragment -> navController.navigate(R.id.action_global_mainFragment)
                R.id.mapFragment -> navController.navigate(R.id.action_global_mapsFragment)
            }
            true
        }


        navController.addOnDestinationChangedListener { _, destination, _ ->
            Log.d("nav", destination.label.toString())
            when (destination.id) {
                R.id.mainFragment, R.id.mapsFragment -> binding.bottomNavigationView.visibility = View.VISIBLE
                else -> binding.bottomNavigationView.visibility = View.GONE
            }
        }
    }

    private fun checkNotificationPermissions(context: Context): Boolean {
        // Check if notification permissions are granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val isEnabled = notificationManager.areNotificationsEnabled()

            if (!isEnabled) {
                // Open the app notification settings if notifications are not enabled
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                context.startActivity(intent)

                return false
            }
        } else {
            val areEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()

            if (!areEnabled) {
                // Open the app notification settings if notifications are not enabled
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                context.startActivity(intent)

                return false
            }
        }

        // Permissions are granted
        return true
    }
}