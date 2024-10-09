package com.example.android_task

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android_task.database.TaskDatabase
import com.example.android_task.viewmodel.TaskViewModel
import com.example.android_task.viewmodel.TaskViewModelFactory
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.widget.Toast
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var taskViewModel: TaskViewModel

    private val QR_SCAN_REQUEST_CODE = 1001
    private val CAMERA_REQUEST_CODE = 1000

    private var lastBackPressedTime: Long = 0
    private val BACK_PRESS_INTERVAL = 2000 // 2 seconds

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        swipeRefreshLayout = findViewById(R.id.swipeRefresh)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar) // This ensures the menu is shown

        setupRecyclerView()
        setupViewModel()
        observeTasks()

        // Setup the PeriodicWorker to requests the resources every 20 minutes instead of 60
        // because the access_token from the login response expires in 1200 seconds (20 minutes)
        setupPeriodicWorker()
        //taskViewModel.loginAndFetchTasks("365", "1")
        swipeRefreshLayout.setOnRefreshListener {
            taskViewModel.refreshTasks()
        }
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        // Initialize the adapter
        taskAdapter = TaskAdapter()
        recyclerView.adapter = taskAdapter
    }

    private fun setupViewModel() {
        // Create the database, DAO, repository, and ViewModel factory
        val taskDao = TaskDatabase.getDatabase(application).taskDao()
        val repository = TaskRepository(taskDao, ApiClient(applicationContext), applicationContext)
        val factory = TaskViewModelFactory(repository)
        // Initialize ViewModel
        taskViewModel = ViewModelProvider(this, factory)[TaskViewModel::class.java]
    }

    private fun observeTasks() {
        taskViewModel.tasks.observe(this) { tasks ->
            tasks?.let {
                taskAdapter.submitList(it)
            }
        }
        taskViewModel.isRefreshing.observe(this) { isRefreshing ->
            swipeRefreshLayout.isRefreshing = isRefreshing == true
        }
        taskViewModel.searchResults.observe(this) { searchResults ->
            taskAdapter.submitList(searchResults)
        }
    }

    private fun setupPeriodicWorker() {
        val workRequest = PeriodicWorkRequestBuilder<FetchTasksWorker>(20, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "FetchTasksWorker",
            ExistingPeriodicWorkPolicy.KEEP,  // Prevents duplicate work requests
            workRequest
        )
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        val searchItem = menu?.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as SearchView

        // Customize SearchView
        searchView.apply {
            queryHint = "Search tasks..."
            setIconifiedByDefault(false) // This ensures if the search view starts collapsed or not
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    taskViewModel.searchTasks(query) // Trigger the search in ViewModel
                    searchView.clearFocus()
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let {
                    taskViewModel.searchTasks(newText) // Trigger the search on text change
                }
                return true
            }
        })

        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                // Called when SearchView is expanded
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                // Called when SearchView is collapsed
                taskViewModel.searchTasks("") // Reset to show all items
                return true
            }
        })

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_scan_qr -> {
                checkCameraPermission()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun startQrScanner() {
        val scannerIntent = Intent(this, QrScannerActivity::class.java)
        startActivityForResult(scannerIntent, QR_SCAN_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == QR_SCAN_REQUEST_CODE && resultCode == RESULT_OK) {
            val qrCode = data?.getStringExtra("QR_CODE")
            qrCode?.let {
                // Use the scanned QR code as a search query
                taskViewModel.searchTasks(it)
            }
        }
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST_CODE)
        } else {
            startQrScanner()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startQrScanner()
        }
    }

    override fun onBackPressed() {
        val currentTime = System.currentTimeMillis()

        if (currentTime - lastBackPressedTime < BACK_PRESS_INTERVAL) {
            // Exit the app
            super.onBackPressed()
        } else {
            taskViewModel.searchTasks("")
            lastBackPressedTime = currentTime
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show()
        }
    }
}
