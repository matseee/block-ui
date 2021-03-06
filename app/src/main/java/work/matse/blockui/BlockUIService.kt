package work.matse.blockui

import android.app.ActivityManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.TableLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.doOnAttach

class BlockUIService : Service() {
    private var layoutOverlay: ConstraintLayout? = null
    private var layoutKeyboard: TableLayout? = null
    private var textViewKey: TextView? = null

    private var windowManager: WindowManager? = null
    private var viewOverlay: View? = null

    private var key: String = ""
    private var currentInput: String = ""

    private var sharedPreferences: SharedPreferences? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (viewOverlay == null) {
            windowManager =
                applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager?

            windowManager!!.addView(createView(), generateLayoutParams())
        }
        return START_NOT_STICKY
    }

    private fun processInput(char: String) {
        currentInput += char
        if (isFalseInput()) resetOverlay()
         else if (isComplete()) closeOverlay()
    }

    private fun isFalseInput(): Boolean {
        for (index in currentInput.indices) {
            if (currentInput[index] != key[index]) return true
        }
        return false
    }

    private fun isComplete(): Boolean {
        return currentInput == key
    }

    private fun closeOverlay() {
        resetOverlay()
        windowManager!!.removeView(viewOverlay)
        viewOverlay = null
    }

    private fun toggleVisibility() {
        if (layoutKeyboard!!.visibility == View.VISIBLE) {
            layoutKeyboard!!.visibility = View.INVISIBLE
            textViewKey!!.visibility = View.INVISIBLE

            val color = sharedPreferences!!.getInt("darkMode", 0)
            layoutOverlay!!.setBackgroundColor(Color.argb(color, 0, 0, 0))
        } else {
            generateKey()
            layoutKeyboard!!.visibility = View.VISIBLE
            textViewKey!!.visibility = View.VISIBLE
            layoutOverlay!!.background = getDrawable(R.color.black_overlay)
        }
    }

    private fun generateKey() {
        key = ""
        do {
            key += (1..9).random().toString()
        } while (key.length < 4)

        textViewKey!!.text = key
    }

    private fun resetOverlay() {
        currentInput = ""
        toggleVisibility()
    }

    private fun createView(): View {
        sharedPreferences = baseContext.getSharedPreferences("BlockUI", Context.MODE_PRIVATE)
        val inflater =
                baseContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        viewOverlay = inflater.inflate(R.layout.overlay, null)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            viewOverlay!!.doOnAttach {
                viewOverlay!!.setOnApplyWindowInsetsListener { v, insets ->
                    viewOverlay!!.windowInsetsController!!.hide(WindowInsets.Type.statusBars())
                    return@setOnApplyWindowInsetsListener insets
                }
            }
        }

        layoutOverlay = viewOverlay!!.findViewById(R.id.lyOverlay)

        val color = sharedPreferences!!.getInt("darkMode", 0)
        layoutOverlay!!.setBackgroundColor(Color.argb(color, 0, 0, 0))

        textViewKey = viewOverlay!!.findViewById(R.id.tvKey)
        layoutKeyboard = viewOverlay!!.findViewById(R.id.tlKeyboard)
        viewOverlay!!.findViewById<Button>(R.id.btn1).setOnClickListener { processInput("1") }
        viewOverlay!!.findViewById<Button>(R.id.btn2).setOnClickListener { processInput("2") }
        viewOverlay!!.findViewById<Button>(R.id.btn3).setOnClickListener { processInput("3") }
        viewOverlay!!.findViewById<Button>(R.id.btn4).setOnClickListener { processInput("4") }
        viewOverlay!!.findViewById<Button>(R.id.btn5).setOnClickListener { processInput("5") }
        viewOverlay!!.findViewById<Button>(R.id.btn6).setOnClickListener { processInput("6") }
        viewOverlay!!.findViewById<Button>(R.id.btn7).setOnClickListener { processInput("7") }
        viewOverlay!!.findViewById<Button>(R.id.btn8).setOnClickListener { processInput("8") }
        viewOverlay!!.findViewById<Button>(R.id.btn9).setOnClickListener { processInput("9") }

        viewOverlay!!.findViewById<Button>(R.id.btnBack).setOnClickListener {
            toggleVisibility()
        }

        return viewOverlay!!
    }

    private fun generateLayoutParams(): WindowManager.LayoutParams {
        val layoutParams =
                WindowManager.LayoutParams(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN and WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                        PixelFormat.TRANSLUCENT
                )

        layoutParams.gravity = Gravity.TOP or Gravity.RIGHT
        layoutParams.x = 0
        layoutParams.y = 0

        return layoutParams
    }
}