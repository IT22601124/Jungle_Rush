package com.example.junglerush

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var highScoreButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val playButton = findViewById<ImageButton>(R.id.Play)
        val exitButton = findViewById<Button>(R.id.exit)
        highScoreButton = findViewById(R.id.button2)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("HighScore", MODE_PRIVATE)

        // Retrieve the high score
        val highScore = sharedPreferences.getInt("highScore", 0)

        // Set the high score text on button2
        highScoreButton.text = "High Score: $highScore"

        playButton.setOnClickListener {
            val intent = Intent(this@MainActivity, playGame::class.java)
            startActivity(intent)
        }
        exitButton.setOnClickListener {
            finish()
        }
    }
}
