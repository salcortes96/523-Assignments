package com.example.mybusinessapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    lateinit var toggle : ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val drawerLayout : DrawerLayout = findViewById(R.id.drawerLayout)
        val navView : NavigationView = findViewById(R.id.nav_view)

        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        navView.setNavigationItemSelectedListener {

            when(it.itemId){

                R.id.nav_home -> Toast.makeText(applicationContext, "Clicked Home", Toast.LENGTH_SHORT).show()
                R.id.nav_services -> Toast.makeText(applicationContext, "Clicked Services", Toast.LENGTH_SHORT).show()
                R.id.nav_maint_plans -> Toast.makeText(applicationContext, "Clicked Maintenance Plans", Toast.LENGTH_SHORT).show()
                R.id.nav_gallery -> Toast.makeText(applicationContext, "Clicked Gallery", Toast.LENGTH_SHORT).show()
                R.id.nav_my_story -> Toast.makeText(applicationContext, "Clicked My Story", Toast.LENGTH_SHORT).show()
                R.id.nav_my_account -> Toast.makeText(applicationContext, "Clicked My Account", Toast.LENGTH_SHORT).show()
                R.id.nav_settings -> Toast.makeText(applicationContext, "Clicked Settings", Toast.LENGTH_SHORT).show()
                R.id.nav_login -> Toast.makeText(applicationContext, "Clicked Login", Toast.LENGTH_SHORT).show()
                R.id.nav_share -> Toast.makeText(applicationContext, "Clicked Share", Toast.LENGTH_SHORT).show()
                R.id.nav_us -> Toast.makeText(applicationContext, "Clicked Contact Us", Toast.LENGTH_SHORT).show()

            }
            true
        }


    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (toggle.onOptionsItemSelected(item))
            return true


        return super.onOptionsItemSelected(item)
    }


}