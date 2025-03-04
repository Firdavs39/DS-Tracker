package ui.admin

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.decoroomsteel.dstracker.R
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import ui.admin.employees.EmployeesFragment
import ui.admin.locations.LocationsFragment
import ui.admin.reports.ReportsFragment
import ui.admin.sessions.SessionsFragment
import ui.auth.LoginActivity

/**
 * Основной экран администратора с вкладками для управления сотрудниками, локациями и сменами
 */
class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    // Заголовки вкладок
    private val tabTitles = arrayOf(
        "Сотрудники",
        "Рабочие зоны",
        "Смены",
        "Отчеты"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        // Инициализация Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Настройка заголовка
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.title = "Decoroom Steel Time"

        // Инициализация ViewPager и TabLayout
        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)

        // Настройка ViewPager с фрагментами
        val pagerAdapter = AdminPagerAdapter(this)
        viewPager.adapter = pagerAdapter

        // Связывание TabLayout с ViewPager
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
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