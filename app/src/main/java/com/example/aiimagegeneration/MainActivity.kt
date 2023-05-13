package com.example.aiimagegeneration

import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.aiimagegeneration.databinding.ActivityMainBinding
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.net.URL
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private val apiKey = ""
    private val endpointUrl = "https://api.openai.com/v1/images/generations"

    lateinit var binding: ActivityMainBinding

    private var workerHandler = Handler(Looper.getMainLooper())
    private var workerThread: ExecutorService = Executors.newCachedThreadPool()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.button.setOnClickListener {
            Log.d("myClick", "Click calling ")
            workerThread.execute {
                callingApi()
            }
        }
    }

    //Size 256x256, 512x512, or 1024x1024
    private fun callingApi() {
        val client = OkHttpClient()

        val requestBody = JSONObject()
        requestBody.put("prompt", "A futuristic space station orbiting a planet, a sophisticated robot in the foreground, jewel tones, cinematic lighting, photorealistic, ultra photoreal.")
        requestBody.put("size", "1024x1024")
        requestBody.put("n", 1)

        val request = Request.Builder()
            .url("https://api.openai.com/v1/images/generations")
            .post(requestBody.toString().toRequestBody("application/json".toMediaTypeOrNull()))
            .header("Authorization", "Bearer $apiKey")
            .build()

        showProgressBar()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle failure
              hideProgressBar()
                Toast.makeText(this@MainActivity, "Error", Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(call: Call, response: Response) {

                if (response.isSuccessful) {

                    val responseBody = response.body?.string()

                    if (responseBody != null) {

                        val responseJson = JSONObject(responseBody)
                        val imageData =
                            responseJson.getJSONArray("data").getJSONObject(0).getString("url")
                        val imageUrl = URL(imageData)

                        val bitmap = BitmapFactory.decodeStream(imageUrl.openConnection().getInputStream())

                        workerHandler.post {
                            /*Glide.with(App.context)
                                .load(imageData)
                                .into(binding.imageView)*/
                            binding.imageView.setImageBitmap(bitmap)
                            hideProgressBar()
                        }

                        /* val bitmap =
                             BitmapFactory.decodeStream(imageUrl.openConnection().getInputStream())

                         // Update the UI with the generated image
                         workerHandler.post {
                             binding.imageView.setImageBitmap(bitmap)
                         }*/
                    }

                } else {

                    // Handle error
                }
            }
        })

    }

    fun showProgressBar(){
        workerHandler.post {
            binding.progressBar.visibility = View.VISIBLE
        }
    }

    fun hideProgressBar(){
        workerHandler.post {
            binding.progressBar.visibility = View.GONE
        }
    }

}