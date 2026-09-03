package id.notakasir.pos.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import id.notakasir.pos.R
import id.notakasir.pos.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var b: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)
        val nav = (supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment).navController
        b.bottomNav.setupWithNavController(nav)
        // Gate onboarding/PIN di awal
        val prefs = getSharedPreferences("nk_setup", MODE_PRIVATE)
        if (!prefs.getBoolean("onboard_done", false)) nav.navigate(R.id.onboardingFragment)
        else if (!prefs.getBoolean("unlocked", false)) nav.navigate(R.id.pinFragment)
    }
}
