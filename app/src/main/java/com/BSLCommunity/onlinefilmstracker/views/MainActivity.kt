package com.BSLCommunity.onlinefilmstracker.views

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.util.ArrayMap
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.BSLCommunity.onlinefilmstracker.R
import com.BSLCommunity.onlinefilmstracker.views.OnFragmentInteractionListener.Action
import com.BSLCommunity.onlinefilmstracker.views.fragments.NewestFilmsFragment


class MainActivity : AppCompatActivity(), OnFragmentInteractionListener {
    private lateinit var newestFragment: Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

            // Инициализация менеджера смены фрагментов
            newestFragment = NewestFilmsFragment()

            // Открытие фрагмента главного меню
            supportFragmentManager.beginTransaction()
                .add(R.id.main_fragment_container, newestFragment)
                .addToBackStack("newest")
                .commit()
        }
    }

    override fun onFragmentInteraction(fragmentSource: Fragment?, fragmentReceiver: Fragment?, action: Action?, data: Bundle?, backStackTag: String?) {
        val fTrans = supportFragmentManager.beginTransaction()

        fragmentReceiver?.arguments = data

        val animIn: Int = R.anim.fade_in
        val animOut: Int = R.anim.fade_out

        fTrans.setCustomAnimations(animIn, animOut, animIn, animOut)

        when (action) {
            Action.NEXT_FRAGMENT_NO_BACK_STACK -> {
                fTrans.replace(R.id.main_fragment_container, fragmentReceiver!!)
                fTrans.commit()
            }
            Action.NEXT_FRAGMENT_HIDE -> {
                if (newestFragment.isVisible) fTrans.hide(newestFragment)
                else fTrans.hide(fragmentSource!!)
                fTrans.add(R.id.main_fragment_container, fragmentReceiver!!)
                fTrans.addToBackStack(backStackTag) // Добавление изменнений в стек
                fTrans.commit()
            }
            Action.NEXT_FRAGMENT_REPLACE -> {
                fTrans.replace(R.id.main_fragment_container, fragmentReceiver!!)
                fTrans.addToBackStack(backStackTag) // Добавление изменнений в стек
                fTrans.commit()
            }
            Action.RETURN_FRAGMENT_BY_TAG -> supportFragmentManager.popBackStack(backStackTag, 0)
            Action.POP_BACK_STACK -> supportFragmentManager.popBackStack()
        }
    }
}