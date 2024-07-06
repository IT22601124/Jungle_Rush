package com.example.junglerush

import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.SharedPreferences
import android.graphics.Rect
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.os.Handler
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.*
import kotlin.concurrent.schedule

class playGame : AppCompatActivity() {
    private val jumpHeightPx = 700f // Adjust as needed
    private val jumpDurationMs = 1100L // Adjust as needed
    private val obstacleDrawableResources = intArrayOf(
        R.drawable.obstacle1,
        // Add more obstacle drawable resource IDs as needed
    )

    private lateinit var imageViewRunningPerson: ImageView
    private lateinit var obstacle1: ImageView
    private lateinit var scoreTextView: TextView
    private lateinit var sharedPreferences: SharedPreferences
    private val random = Random()
    private var obstacleGenerated = false
    private var score = 0
    private var highScore = 0
    private var obstacleAnimationStarted = false
    private var initialSpeed = 40 // Initial speed of obstacles

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_game)

        imageViewRunningPerson = findViewById(R.id.imageViewRunningPerson)
        obstacle1 = findViewById(R.id.obstacle1)
        scoreTextView = findViewById(R.id.scoreTextView)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("HighScore", MODE_PRIVATE)
        // Retrieve the high score
        highScore = sharedPreferences.getInt("highScore", 0)

        // Start animation of the running person
        val animation = imageViewRunningPerson.background as AnimationDrawable
        animation.start()

        // Start the game with the initial speed
        startGameWithInitialSpeed()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                jumpAnimation()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun addObstacle(drawableResource: Int) {
        val obstacleImageView = ImageView(this)
        obstacleImageView.setImageResource(drawableResource)

        val layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        obstacleImageView.layoutParams = layoutParams

        val screenWidth = resources.displayMetrics.widthPixels
        val randomX = Random().nextInt(screenWidth - obstacleImageView.width)
        obstacleImageView.x = randomX.toFloat()

        val constraintLayout = findViewById<ViewGroup>(R.id.constraintLayout)
        constraintLayout.addView(obstacleImageView)
    }

    private fun jumpAnimation() {
        val jumpUpAnimator = ObjectAnimator.ofFloat(
            imageViewRunningPerson,
            "translationY",
            imageViewRunningPerson.translationY,
            imageViewRunningPerson.translationY - jumpHeightPx
        )

        jumpUpAnimator.duration = jumpDurationMs / 2
        jumpUpAnimator.interpolator = AccelerateDecelerateInterpolator()

        val jumpDownAnimator = ObjectAnimator.ofFloat(
            imageViewRunningPerson,
            "translationY",
            imageViewRunningPerson.translationY,
            imageViewRunningPerson.translationY
        )

        jumpDownAnimator.startDelay = 1000L
        jumpDownAnimator.duration = 700L
        jumpDownAnimator.interpolator = AccelerateDecelerateInterpolator()

        jumpUpAnimator.start()
        jumpDownAnimator.start()
    }

    private fun startGameWithInitialSpeed() {
        // Start the game with the initial speed
        initialSpeed = 40
        startObstacleAnimation()
        startSpeedIncrementTimer()
    }

    private fun startSpeedIncrementTimer() {
        // Start timer to increase obstacle speed after 20 seconds
        val speedIncrementDelayMs: Long = 20 * 1000 // 20 seconds in milliseconds
        Timer().schedule(speedIncrementDelayMs) {
            increaseObstacleSpeed()
        }
    }

    private fun startObstacleAnimation() {
        if (!obstacleAnimationStarted) {
            obstacleAnimationStarted = true
            val handler = Handler()
            var currentX = 2500
            var passedObstacle = false // Flag to track if the obstacle has been successfully passed

            handler.post(object : Runnable {
                override fun run() {
                    if (obstacle1.translationX <= -1200) {
                        obstacleGenerated = false
                        currentX = 2500
                        passedObstacle = false // Reset the flag when the obstacle goes out of the screen
                    }

                    currentX -= initialSpeed // Use initial speed
                    obstacle1.translationX = currentX.toFloat()

                    // Check if the obstacle is within the player's reach
                    if (obstacle1.translationX <= imageViewRunningPerson.translationX) {
                        // Check if the obstacle has already been passed
                        if (!passedObstacle) {
                            // Increment the score only once when the obstacle is successfully avoided
                            score++
                            // Update the score TextView
                            scoreTextView.text = "Score: $score"
                            passedObstacle = true // Set the flag to true to indicate that the obstacle has been passed
                        }
                    }

                    // Check for collision between the running person and the obstacle
                    if (isColliding(imageViewRunningPerson, obstacle1)) {
                        // Game over
                        gameOver()
                        return
                    }

                    handler.postDelayed(this, 15)
                }
            })
        }
    }

    private fun isColliding(view1: View, view2: View): Boolean {
        val view1Rect = Rect()
        view1.getHitRect(view1Rect)
        val view2Rect = Rect()
        view2.getHitRect(view2Rect)
        return view1Rect.intersect(view2Rect)
    }

    private fun gameOver() {
        // Pause the running animation
        val animation = imageViewRunningPerson.background as AnimationDrawable
        animation.stop()

        // Stop obstacle animation
        obstacleAnimationStarted = false

        // Update high score if the current score is greater
        if (score > highScore) {
            highScore = score
            // Save the high score to SharedPreferences
            val editor = sharedPreferences.edit()
            editor.putInt("highScore", highScore)
            editor.apply()
        }

        // Show game over dialog
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setMessage("Game Over!\nScore: $score\nHigh Score: $highScore")
        dialogBuilder.setPositiveButton("Restart") { dialog, which ->
            // Restart game
            score = 0
            scoreTextView.text = "Score: $score"
            startGameWithInitialSpeed() // Restart with initial speed

            // Start the running animation again
            animation.start()
        }
        dialogBuilder.setNegativeButton("Exit") { dialog, which ->
            finish()
        }
        val gameOverDialog = dialogBuilder.create()
        gameOverDialog.show()
    }

    private fun increaseObstacleSpeed() {
        // Increase obstacle speed after 20 seconds
        initialSpeed += 3 // Adjust as needed
    }
}
