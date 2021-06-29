package com.BSLCommunity.onlinefilmstracker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.BSLCommunity.onlinefilmstracker.fragments.FilmsListFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager.beginTransaction().replace(R.id.main_fragment_container, FilmsListFragment()).commit()
    }
}