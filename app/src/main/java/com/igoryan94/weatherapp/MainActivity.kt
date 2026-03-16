package com.igoryan94.weatherapp

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.igoryan94.weatherapp.databinding.ActivityMainBinding
import kotlin.math.hypot

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Статическая переменная для хранения скриншота между перезапусками Activity
    companion object {
        var bitmapSnapshot: Bitmap? = null
        var animationOrigin: IntArray? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_forecast, R.id.navigation_settings
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // ПРОВЕРКА: Если мы только что перезапустились для смены темы
        if (bitmapSnapshot != null) {
            runThemeAnimation()
        }
    }

    /**
     * Этот метод вызывается из Фрагмента.
     * Он только готовит почву и перезапускает Activity.
     */
    fun changeThemeWithAnimation(newMode: Int, sourceView: View) {
        val root = window.decorView as ViewGroup

        // 1. Делаем скриншот текущего состояния (СТАРАЯ ТЕМА)
        val bitmap = Bitmap.createBitmap(root.width, root.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        root.draw(canvas)

        // 2. Сохраняем данные во временное хранилище
        bitmapSnapshot = bitmap
        val location = IntArray(2)
        sourceView.getLocationOnScreen(location)
        animationOrigin = intArrayOf(
            location[0] + sourceView.width / 2,
            location[1] + sourceView.height / 2
        )

        // 3. Меняем тему в системе
        val mode = when (newMode) {
            1 -> AppCompatDelegate.MODE_NIGHT_NO
            2 -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(mode)

        // 4. ПЕРЕЗАПУСКАЕМ Activity немедленно.
        // ВАЖНО: В Манифесте НЕ должно быть configChanges="uiMode" для этого метода.
        // Если configChanges ЕСТЬ, то вызови recreate() вручную:
        recreate()
    }

    /**
     * Этот метод запускается уже в НОВОЙ Activity, где все цвета стали новыми.
     */
    // FIXME круг анимации должен исходить от точки касания, плюс именно исходить от, а не приходить в точку...
    private fun runThemeAnimation() {
        val root = window.decorView as ViewGroup
        val overlay = ImageView(this)
        overlay.setImageBitmap(bitmapSnapshot)

        root.addView(overlay)

        overlay.post {
            val x = animationOrigin?.get(0) ?: (root.width / 2)
            val y = animationOrigin?.get(1) ?: (root.height / 2)
            val finalRadius = hypot(root.width.toDouble(), root.height.toDouble()).toFloat()

            // Анимируем ИСЧЕЗНОВЕНИЕ старого скриншота, открывая под ним новую тему
            val anim = ViewAnimationUtils.createCircularReveal(overlay, x, y, finalRadius, 0f)
            anim.duration = 1000
            anim.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    root.removeView(overlay)
                    // Очищаем память
                    bitmapSnapshot = null
                    animationOrigin = null
                }
            })
            anim.start()
        }
    }
}