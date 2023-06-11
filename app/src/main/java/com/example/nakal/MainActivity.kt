package com.example.nakal

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.example.nakal.R
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var lblStatus: TextView
    private lateinit var btnFree: Button
    private lateinit var btnAvoider: Button
    private lateinit var btnLine: Button
    private lateinit var btnUp: Button
    private lateinit var btnDown: Button
    private lateinit var btnLeft: Button
    private lateinit var btnRight: Button
    private lateinit var btnConnect: Button
    private lateinit var loader: ProgressBar

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothDevice: BluetoothDevice
    private lateinit var bluetoothSocket: BluetoothSocket

    private val hc05UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private val hc05Address: String = "98:D3:41:F6:62:15" // ini punyaku

    private val REQUEST_BLUETOOTH_PERMISSION = 1
    private var isConnecting = false
    private var warna_konek: Drawable? = null

private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action

            if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                when (state) {
                    BluetoothAdapter.STATE_OFF -> {
                        // Koneksi Bluetooth terputus atau perangkat Bluetooth mati
                        showToast("Koneksi Bluetooth Terputus")
                        // Panggil fungsi atau lakukan tindakan yang sesuai saat koneksi terputus
                        lblStatus.text = "Tidak Terkoneksi"
                        merah()
                    }
                }
            }
        }
    }

    fun merah() {
        val navLayout = findViewById<LinearLayout>(R.id.nav)
        navLayout.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.merah))

    }

    fun hover_free() {
        val free = findViewById<RelativeLayout>(R.id.hover_free)
        val abu = ContextCompat.getColor(this, R.color.abu)
        ViewCompat.setBackgroundTintList(free, ColorStateList.valueOf(abu))
    }

    fun hover_avoider() {
        val avoider = findViewById<RelativeLayout>(R.id.hover_avoider)
        val abu = ContextCompat.getColor(this, R.color.abu)
        ViewCompat.setBackgroundTintList(avoider, ColorStateList.valueOf(abu))
    }

    fun hover_line() {
        val line = findViewById<RelativeLayout>(R.id.hover_line)
        val abu = ContextCompat.getColor(this, R.color.abu)
        ViewCompat.setBackgroundTintList(line, ColorStateList.valueOf(abu))
    }

    fun normal_color_free() {
        val freeputih = findViewById<RelativeLayout>(R.id.hover_free)
        val putih1 = ContextCompat.getColor(this, R.color.white)
        ViewCompat.setBackgroundTintList(freeputih, ColorStateList.valueOf(putih1))
    }

    fun normal_color_avoider() {
        val avoiderputih = findViewById<RelativeLayout>(R.id.hover_avoider)
        val putih2 = ContextCompat.getColor(this, R.color.white)
        ViewCompat.setBackgroundTintList(avoiderputih, ColorStateList.valueOf(putih2))
    }

    fun normal_color_line() {
        val lineputih = findViewById<RelativeLayout>(R.id.hover_line)
        val putih3 = ContextCompat.getColor(this, R.color.white)
        ViewCompat.setBackgroundTintList(lineputih, ColorStateList.valueOf(putih3))
    }



    private var upButtonPressed = false
    private var downButtonPressed = false
    private var leftButtonPressed = false
    private var rightButtonPressed = false

    private lateinit var seekBar: SeekBar
    private lateinit var view_speed: TextView
    private var speed: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        view_speed = findViewById(R.id.view_speed)
        seekBar = findViewById(R.id.speed)
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Update nilai speed saat seekbar berubah
                speed = progress
                // Kirim nilai speed ke Arduino melalui Bluetooth
                sendBluetoothData("S$speed")
                // Tampilkan nilai speed di TextView atau lakukan sesuai kebutuhan

                 view_speed.text = "$speed"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Kosongkan jika tidak ada tindakan yang perlu dilakukan saat seekbar disentuh
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Kosongkan jika tidak ada tindakan yang perlu dilakukan saat sentuhan pada seekbar berhenti
            }
        })
        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        registerReceiver(bluetoothReceiver, filter)
        warna_konek = ColorDrawable(Color.GREEN)
        loader = findViewById(R.id.loader)
        lblStatus = findViewById(R.id.lblStatus)
        btnFree = findViewById(R.id.btnFree)
        btnAvoider = findViewById(R.id.btnAvoider)
        btnLine = findViewById(R.id.btnLine)
        btnUp = findViewById(R.id.btnUp)
        btnDown = findViewById(R.id.btnDown)
        btnLeft = findViewById(R.id.btnLeft)
        btnRight = findViewById(R.id.btnRight)
        btnConnect = findViewById(R.id.btnConnect)

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        btnConnect.setOnClickListener {
            requestBluetoothPermission()
        }

        btnFree.setOnClickListener {
            sendBluetoothData("X")
            hover_free()
            normal_color_line()
            normal_color_avoider()

        }

        btnAvoider.setOnClickListener {
            sendBluetoothData("Y")
            hover_avoider()
            normal_color_free()
            normal_color_line()
        }

        btnLine.setOnClickListener {
            sendBluetoothData("Z")
            hover_line()
            normal_color_avoider()
            normal_color_free()
        }
        btnUp.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Tombol ditekan
                    upButtonPressed = true
                    sendContinuously("F", 100)
                }
                MotionEvent.ACTION_UP -> {
                    // Tombol dilepas
                    upButtonPressed = false
                }
            }
            true
        }

        btnDown.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Tombol ditekan
                    downButtonPressed = true
                    sendContinuously("B", 100)
                }
                MotionEvent.ACTION_UP -> {
                    // Tombol dilepas
                    downButtonPressed = false
                }
            }
            true
        }

        btnLeft.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Tombol ditekan
                    leftButtonPressed = true
                    sendContinuously("L", 100)
                }
                MotionEvent.ACTION_UP -> {
                    // Tombol dilepas
                    leftButtonPressed = false
                }
            }
            true
        }

        btnRight.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Tombol ditekan
                    rightButtonPressed = true
                    sendContinuously("R", 100)
                }
                MotionEvent.ACTION_UP -> {
                    // Tombol dilepas
                    rightButtonPressed = false
                }
            }
            true
        }





    }

    private fun sendContinuously(data: String, interval: Long) {
        Thread {
            while (upButtonPressed || downButtonPressed || leftButtonPressed || rightButtonPressed) {
                sendBluetoothData(data)
                Thread.sleep(interval)
            }
        }.start()
    }

    private fun requestBluetoothPermission() {
        val permissions = arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        ActivityCompat.requestPermissions(
            this,
            permissions,
            REQUEST_BLUETOOTH_PERMISSION
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_BLUETOOTH_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                connectToBluetooth()
            } else {
                Toast.makeText(this, "Izin Bluetooth ditolak.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun connectToBluetooth() {
        if (isConnecting) {
            return
        }

        isConnecting = true
        showLoader()
        bluetoothDevice = bluetoothAdapter.getRemoteDevice(hc05Address)

        Thread {
            try {
                bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(hc05UUID)
                bluetoothSocket.connect()
                runOnUiThread {
                    lblStatus.text = "Berhasil Terkoneksi"
                    showToast("Berhasil Terhubung")
                    val navLayout = findViewById<LinearLayout>(R.id.nav)
                    navLayout.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.hijau))

                }
            } catch (e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    lblStatus.text = "Gagal Terkoneksi"
                    showToast("Gagal Terhubung")
                }
            } finally {
                runOnUiThread {
                    hideLoader()
                    isConnecting = false

                }
            }
        }.start()
    }


    private fun sendBluetoothData(data: String) {
        if (::bluetoothSocket.isInitialized && bluetoothSocket.isConnected) {
            val outputStream = bluetoothSocket.outputStream
            try {
                outputStream.write(data.toByteArray())
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun showLoader() {
        btnConnect.isEnabled = false
        loader.visibility = View.VISIBLE
    }

    private fun hideLoader() {
        btnConnect.isEnabled = true
        loader.visibility = View.INVISIBLE
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::bluetoothSocket.isInitialized) {
            try {
                bluetoothSocket.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}


