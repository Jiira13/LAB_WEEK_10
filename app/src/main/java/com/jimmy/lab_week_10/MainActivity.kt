package com.jimmy.lab_week_10

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.jimmy.lab_week_10.database.Total
import com.jimmy.lab_week_10.database.TotalDatabase
import com.jimmy.lab_week_10.database.TotalObject
import com.jimmy.lab_week_10.viewmodels.TotalViewModel
import java.util.Date

class MainActivity : AppCompatActivity() {
    private val db by lazy { prepareDatabase() }

    private val viewModel by lazy {
        ViewModelProvider(this)[TotalViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initializeValueFromDatabase()
        prepareViewModel()
    }

    override fun onStart() {
        super.onStart()
        Thread {
            val total = db.totalDao().getTotal(ID)
            if (total.isNotEmpty()) {
                val date = total.first().total.date
                runOnUiThread {
                    Toast.makeText(this, "Last updated: $date", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }

    override fun onPause() {
        super.onPause()
        val currentDate = Date().toString()
        val totalObject = TotalObject(viewModel.total.value!!, currentDate)
        db.totalDao().update(Total(ID, totalObject))
    }

    private fun updateText(total: Int) {
        findViewById<TextView>(R.id.text_total).text =
            getString(R.string.text_total, total)
    }

    private fun prepareViewModel(){
        viewModel.total.observe(this) {
            updateText(it)
        }
        findViewById<Button>(R.id.button_increment).setOnClickListener {
            viewModel.incrementTotal()
        }
    }

    private fun prepareDatabase() : TotalDatabase {
        return Room.databaseBuilder(
            applicationContext,
            TotalDatabase::class.java, "total-database"
        )
            .fallbackToDestructiveMigration()
            .allowMainThreadQueries()
            .build()
    }

    private fun initializeValueFromDatabase(){
        Thread {
            val total = db.totalDao().getTotal(ID)
            if(total.isEmpty()){
                val totalObject = TotalObject(0, Date().toString())
                db.totalDao().insert(Total(id = 1, total = totalObject))
            } else {
                viewModel.setTotal(total.first().total.value)
            }
        }.start()
    }

    companion object{
        const val ID : Long = 1
    }
}
