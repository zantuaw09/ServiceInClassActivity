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
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    lateinit var timerBinder: TimerService.TimerBinder
    lateinit var displayTextView: TextView
    lateinit var actionStartTimer: MenuItem

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
            actionStartTimer.setIcon(android.R.drawable.ic_media_play)
            isConnected = false
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // references display text view + binds service
        displayTextView = findViewById(R.id.displayTextView)

        bindService(
            Intent(this, TimerService::class.java),
            serviceConnection,
            BIND_AUTO_CREATE
        )

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        actionStartTimer = menu!!.findItem(R.id.action_start_timer)


        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.action_start_timer -> {
                if (isConnected) {
                    with(timerBinder) {
                        // START a countdown with the service if nothing is running
                        if (!isRunning && !paused) {
                            timerBinder.start(5)
                            item.setIcon(android.R.drawable.ic_media_pause)
                        }

                        // should allow PAUSE while TimerThread isRunning
                        else if (isRunning && !paused) {
                            timerBinder.pause()
                            item.setIcon(android.R.drawable.ic_media_play)
                        }

                        // should allow RESUME while TimerThread isRunning + paused
                        else if (paused) {
                            timerBinder.pause()
                            item.setIcon(android.R.drawable.ic_media_pause)
                        }
                    }
                }
            }

            R.id.action_stop_timer -> {
                if (isConnected) {
                    timerBinder.stop()
                    displayTextView.text = "0"
                    actionStartTimer.setIcon(android.R.drawable.ic_media_play)
                }
                if (timerBinder.paused)
                    timerBinder.pause()
            }
        }

        return super.onOptionsItemSelected(item)
    }


    override fun onDestroy() {
        unbindService(serviceConnection)
        super.onDestroy()
    }
}