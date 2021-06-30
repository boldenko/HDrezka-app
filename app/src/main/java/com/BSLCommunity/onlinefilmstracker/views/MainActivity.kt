package com.BSLCommunity.onlinefilmstracker.views

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.BSLCommunity.onlinefilmstracker.R
import com.BSLCommunity.onlinefilmstracker.views.fragments.NewestFilmsFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager.beginTransaction().replace(R.id.main_fragment_container, NewestFilmsFragment()).commit()
    }
}