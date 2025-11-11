package edu.temple.myapplication

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.widget.Button

class MainActivity : AppCompatActivity() {

    lateinit var timerBinder: TimerService.TimerBinder
    lateinit var startButton: Button

    var isConnected = false

    val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(
            p0: ComponentName?,
            p1: IBinder?
        ) {
            timerBinder = p1 as TimerService.TimerBinder
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

        bindService(
            Intent(this, TimerService::class.java),
            serviceConnection,
            BIND_AUTO_CREATE
        )

        // implement START function + binds service
        startButton = findViewById(R.id.startButton)

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
            }
            if (timerBinder.paused)
                timerBinder.pause()
        }
    }

    override fun onDestroy() {
        unbindService(serviceConnection)
        super.onDestroy()
    }
}