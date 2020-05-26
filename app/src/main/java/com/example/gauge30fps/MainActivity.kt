package com.example.gauge30fps

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var init = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val speedGauge = findViewById<SpeedGaugeView>(R.id.speedTestGauge)
        start_button_ft.text = "Launch"
        start_button_ft.setOnClickListener {
            speedGauge.setActive(true)
            speedGauge.startAnim()
            when (init) {
                0 -> for (i in 0..89) {
                    speedGauge.value = i
                    init = i
                }
                89 -> {
                    for (y in 89 downTo 0) {
                        speedGauge.value = y
                        init = y
                    }
                }
            }
        }
    }
}