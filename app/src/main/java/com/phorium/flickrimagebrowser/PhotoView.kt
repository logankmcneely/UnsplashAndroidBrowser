package com.phorium.flickrimagebrowser

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.GestureDetector
import android.view.View
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_photo_view.*
import java.lang.Exception
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import android.util.Log
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GestureDetectorCompat

private const val COLOR_DIVIDER = 40

class PhotoView : AppCompatActivity(), GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {


    private var fullscreen: Boolean = false
    private var gDetector: GestureDetectorCompat? = null
    private var colorPosition: Int = COLOR_DIVIDER
    private var colorArray = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_view)

        val bundle: Bundle? = intent.extras
        val url: String? = bundle!!.getString("url")
        val user: String? = bundle.getString("user")
        val description: String? = bundle.getString("description")
        val createdAt: String? = bundle.getString("createdAt")
        val color: String? = bundle.getString("color")

        colorArray = getColorArray(color!!)

        this.gDetector = GestureDetectorCompat(this, this)
        gDetector?.setOnDoubleTapListener(this)

        tvUser.text = if( user != "null") user else "Unknown"
        tvDescription.text = if (description != "null") description else "No description provided"
        tvCreatedAt.text = formatCreatedAt(createdAt!!)

        linearLayoutPhotoView.setBackgroundColor(Color.parseColor(color))

        try {
            Picasso.get().load(url)
                .error(R.drawable.placeholder)
                .placeholder(R.drawable.placeholder)
                .into(ivPhoto)
        } catch (e: Exception) {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        hideSystemUI()
    }

    private fun hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                    // Set the content to appear under the system bars so that the
                    // content doesn't resize when the system bars hide and show.
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    // Hide the nav bar and status bar
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN)
        }
    }

    private fun showSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
    }

    private fun formatCreatedAt(createdAt: String): String {
        val created = createdAt.substring(0, createdAt.length-9)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm", Locale.ENGLISH)
            val outputFormatter = DateTimeFormatter.ofPattern("LLL dd, yyyy", Locale.ENGLISH)
            val date = LocalDate.parse(created, inputFormatter)
            return outputFormatter.format(date)
        } else {
            TODO("VERSION.SDK_INT < O")
        }


    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        this.gDetector?.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    override fun onShowPress(e: MotionEvent?) {
    }

    override fun onSingleTapUp(e: MotionEvent?): Boolean {
        return true
    }

    override fun onDown(e: MotionEvent?): Boolean {
        return true
    }

    override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
//        if (abs(velocityY) > 7000) {
//            val direction = if (velocityY < 0) 1 else -1
//            colorWalk(direction)
//        }
//        Toast.makeText(this, "$velocityY", Toast.LENGTH_SHORT).show()
        return true
    }

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
        if (distanceY > 5) {
            colorPosition += 1
            checkColorPosition()
            linearLayoutPhotoView.setBackgroundColor(Color.parseColor(colorArray[colorPosition]))
        }
        if (distanceY < -5) {
            colorPosition -= 1
            checkColorPosition()
            linearLayoutPhotoView.setBackgroundColor(Color.parseColor(colorArray[colorPosition]))
        }
        return true
    }

    override fun onLongPress(e: MotionEvent?) {
        finish()
    }

    override fun onDoubleTap(e: MotionEvent?): Boolean {
        return true
    }

    override fun onDoubleTapEvent(e: MotionEvent?): Boolean {
        return true
    }

    override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
        fullscreen = !fullscreen
        if (fullscreen) showSystemUI() else hideSystemUI()
        return true
    }

    private fun getColorArray(color: String): ArrayList<String> {
        val red = color.substring(1,3)
        val green = color.substring(3,5)
        val blue = color.substring(5,7)
        Log.d("TEST", "red:$red, green:$green, blue:$blue")

        val redInt = red.toLong(radix = 16)
        val greenInt = green.toLong(radix = 16)
        val blueInt = blue.toLong(radix = 16)

        val redLowerStep = redInt/COLOR_DIVIDER
        val greenLowerStep = greenInt/COLOR_DIVIDER
        val blueLowerStep = blueInt/COLOR_DIVIDER

        val redUpperStep = (255 - redInt)/COLOR_DIVIDER
        val greenUpperStep = (255 - greenInt)/COLOR_DIVIDER
        val blueUpperStep = (255 - blueInt)/COLOR_DIVIDER

        val colorArray = ArrayList<String>()
        colorArray.add("#000000")

        for (i in 1 until COLOR_DIVIDER) {
            var newColor = "#"
            newColor += colorPadding((redLowerStep * i).toString(16))
            newColor += colorPadding((greenLowerStep * i).toString(16))
            newColor += colorPadding((blueLowerStep * i).toString(16))
            colorArray.add(newColor)
        }

        // Add original color
        colorArray.add(color)

        for (i in 1 until COLOR_DIVIDER) {
            var newColor = "#"
            newColor += colorPadding((redInt + (redUpperStep * i)).toString(16))
            newColor += colorPadding((greenInt + (greenUpperStep * i)).toString(16))
            newColor += colorPadding((blueInt + (blueUpperStep * i)).toString(16))
            colorArray.add(newColor)
        }

        colorArray.add("#FFFFFF")

        Log.d("TEST", colorArray.toString())
        return colorArray

    }

    private fun checkColorPosition(): Boolean {
//        if (colorArray[colorPosition+1] == "#000000") {
//            colorPosition += 1
//        }
//        if (colorArray[colorPosition-1].toLowerCase() == "#ffffff") {
//            colorPosition -=1
//        }

        if (colorPosition > COLOR_DIVIDER * 2) {
            colorPosition = COLOR_DIVIDER * 2
            return false
        }
        if (colorPosition < 0) {
            colorPosition = 0
            return false
        }
        return true
    }

    private fun colorPadding(color: String): String {
        return if (color.length == 1) {
            "0$color"
        } else {
            color
        }
    }

//    private fun colorWalk(direction: Int) {
//
//        Log.d("colorWalk", "colorWalk called")
//        val stepRatio: Double = 1.67
//        var stepTime: Long = 15
//        val startingPosition: Int = colorPosition
//        var modifier: Int = 1
//        var status = true
//        val handler = Handler()
//
//        while(status) {
//            Log.d("colorWalk", "inside Do")
//
//            handler.postDelayed({
//                colorPosition += (direction * modifier)
//                if(!checkColorPosition()) modifier = -1
//                if (startingPosition == colorPosition) status = false
//                Log.d("colorWalk", "inside handler")
//                linearLayoutPhotoView.setBackgroundColor(Color.parseColor(colorArray[colorPosition]))
//
//            }, stepTime)
//            stepTime *= stepRatio.toLong()
//
//            Log.d("colorWalk", "colorPosition: $colorPosition")
//        }
//
//        Log.d("colorWalk", "colorWalk ended")
//    }

}
