package edu.temple.myapplication

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.os.Looper
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    lateinit var timerBinder: TimerService.TimerBinder
    lateinit var startButton: Button
    lateinit var displayTextView: TextView

    var isConnected = false

    val timerHandler = Handler(Looper.getMainLooper()) {
        displayTextView.text = it.what.toString()
        true
    }

    val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(
            p0: ComponentName?,
            p1: IBinder?
        ) {
            timerBinder = p1 as TimerService.TimerBinder
            timerBinder.setHandler(timerHandler)
            isConnected = true
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            startButton.text = "Start"
            isConnected = false
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // implement START function + binds service
        startButton = findViewById(R.id.startButton)
        displayTextView = findViewById(R.id.textView)

        bindService(
            Intent(this, TimerService::class.java),
            serviceConnection,
            BIND_AUTO_CREATE
        )


        startButton.setOnClickListener {
            if (isConnected) {
                with(timerBinder) {
                    // START a countdown with the service if nothing is running
                    if (!isRunning && !paused) {
                        timerBinder.start(5)
                        startButton.text = "Pause"
                    }

                    // should allow PAUSE while TimerThread isRunning
                    else if (isRunning && !paused) {
                        timerBinder.pause()
                        startButton.text = "Resume"
                    }

                    // should allow RESUME while TimerThread isRunning + paused
                    else if (paused) {
                        timerBinder.pause()
                        startButton.text = "Pause"
                    }
                }
            }

        }

        // implement STOP function + unbind service
        findViewById<Button>(R.id.stopButton).setOnClickListener {
            if (isConnected) {
                timerBinder.stop()
                startButton.text = "Start"
                displayTextView.text = "0"
            }
            if (timerBinder.paused)
                timerBinder.pause()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)

        return super.onCreateOptionsMenu(menu)
    }


    override fun onDestroy() {
        unbindService(serviceConnection)
        super.onDestroy()
    }
}