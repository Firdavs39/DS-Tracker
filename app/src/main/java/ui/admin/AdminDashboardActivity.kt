package com.decoroomsteel.dstracker.ui.admin

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.decoroomsteel.dstracker.R
import com.decoroomsteel.dstracker.databinding.ActivityAdminDashboardBinding
import com.decoroomsteel.dstracker.ui.admin.employees.EmployeesFragment
import com.decoroomsteel.dstracker.ui.admin.locations.LocationsFragment
import com.decoroomsteel.dstracker.ui.admin.reports.ReportsFragment
import com.decoroomsteel.dstracker.ui.admin.sessions.SessionsFragment
import com.decoroomsteel.dstracker.ui.auth.LoginActivity
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth

/**
 * Основной экран администратора с вкладками для управления сотрудниками, локациями и сменами
 */
class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminDashboardBinding
    private lateinit var auth: FirebaseAuth
    
    // Заголовки вкладок
    private val tabTitles = arrayOf(
        "Сотрудники",
        "Рабочие зоны",
        "Смены",
        "Отчеты"
    )
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Инициализация Firebase Auth
        auth = FirebaseAuth.getInstance()
        
        // Настройка заголовка
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Decoroom Steel Time"
        
        // Настройка ViewPager с фрагментами
        val pagerAdapter = AdminPagerAdapter(this)
        binding.viewPager.adapter = pagerAdapter
        
        // Связывание TabLayout с ViewPager
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_admin, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                // Выход из аккаунта
                auth.signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    /**
     * Adapter для ViewPager с фрагментами администратора
     */
    private inner class AdminPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        
        override fun getItemCount(): Int = tabTitles.size
        
        override fun createFragment(position: Int): Fragment {
            // Создание соответствующего фрагмента для каждой вкладки
            return when (position) {
                0 -> EmployeesFragment()
                1 -> LocationsFragment()
                2 -> SessionsFragment()
                3 -> ReportsFragment()
                else -> throw IllegalArgumentException("Unknown tab position $position")
            }
        }
    }
}