package com.example.android_task

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android_task.database.TaskDatabase
import com.example.android_task.database.TaskEntity
import com.example.android_task.viewmodel.TaskViewModel
import com.example.android_task.viewmodel.TaskViewModelFactory
import org.json.JSONArray

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var taskViewModel: TaskViewModel
    private val apiClient = ApiClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar) // This ensures the menu is shown

//        // Remove the default title completely
//        supportActionBar?.setDisplayShowTitleEnabled(false)
//        toolbar.title = ""

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize the adapter and set it to the RecyclerView
        taskAdapter = TaskAdapter(emptyList())
        recyclerView.adapter = taskAdapter

        // Create the database, DAO, repository, and ViewModel factory
        val taskDao = TaskDatabase.getDatabase(application).taskDao()
        val repository = TaskRepository(taskDao)
        val factory = TaskViewModelFactory(repository)

        // Initialize ViewModel
        taskViewModel = ViewModelProvider(this, factory)[TaskViewModel::class.java]

        // Observe tasks from ViewModel
        taskViewModel.tasks.observe(this) { tasks ->
            tasks?.let {
                taskAdapter.updateTasks(it)
            }
        }

        // Fetch tasks from API and insert into Room
        apiClient.login("365", "1") { token ->
            token?.let {
                apiClient.getTasks(it) { tasksResponse ->
                    tasksResponse?.let { response ->
                        val tasks = parseTasks(response).map {
                            TaskEntity(
                                task = it.task,
                                title = it.title,
                                description = it.description,
                                sort = it.sort,
                                wageType = it.wageType,
                                BusinessUnitKey = it.BusinessUnitKey,
                                businessUnit = it.businessUnit,
                                parentTaskID = it.parentTaskID,
                                preplanningBoardQuickSelect = it.preplanningBoardQuickSelect,
                                colorCode = it.colorCode,
                                workingTime = it.workingTime,
                                isAvailableInTimeTrackingKioskMode = it.isAvailableInTimeTrackingKioskMode
                            )
                        }
                        // Insert tasks into Room using ViewModel
//                        taskViewModel.insertAll(tasks)
                        taskViewModel.deleteAllAndInsert(tasks)
                    }
                }
            }
        }
    }

    // Parse the JSON response into a list of Task objects
    private fun parseTasks(response: String): List<Task> {
        val jsonArray = JSONArray(response)
        val tasks = mutableListOf<Task>()

        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            val task = Task(
                task = jsonObject.getString("task"),
                title = jsonObject.getString("title"),
                description = jsonObject.getString("description"),
                sort = jsonObject.optString("sort", ""), // Using optString to avoid null values
                wageType = jsonObject.optString("wageType", ""),
                BusinessUnitKey = jsonObject.optString("BusinessUnitKey", ""),
                businessUnit = jsonObject.optString("businessUnit", ""),
                parentTaskID = jsonObject.optString("parentTaskID", null),
                preplanningBoardQuickSelect = jsonObject.optString("preplanningBoardQuickSelect", null),
                colorCode = jsonObject.getString("colorCode"),
                workingTime = jsonObject.optString("workingTime", null),
                isAvailableInTimeTrackingKioskMode = jsonObject.optBoolean("isAvailableInTimeTrackingKioskMode", false)
            )
            tasks.add(task)
        }
        return tasks
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        val searchItem = menu?.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as SearchView

        // Set up the search functionality
        searchView.queryHint = "Search tasks..."

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

        // Optional: Handle the collapse/expand of SearchView
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

}

