package com.example.sichacks

import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream
import android.content.Intent as Intent1

class MainActivity : AppCompatActivity() {
    private lateinit var select_image_button: Button
    private lateinit var imageview: ImageView
    private lateinit var camerabtn: Button
    lateinit var Upload: Button
    private lateinit var Results: Button
    private var uri: String = ""
    private var bitmap: Bitmap? = null
    private var encodedImage: String? = null
    lateinit var textresult: TextView

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent1?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 250) {
            imageview.setImageURI(data?.data)

            var uuri: Uri? = data?.data
            uri = uuri.toString()
            bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uuri)
            val imageBytes = runBlocking {
                val baos = ByteArrayOutputStream()
                bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                baos.toByteArray()
            }
            encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT)


        } else if (requestCode == 200 && resultCode == Activity.RESULT_OK) {
            bitmap = data?.extras?.get("data") as Bitmap
            imageview.setImageBitmap(bitmap)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        select_image_button = findViewById(R.id.selectbtn)
        imageview = findViewById(R.id.imageview)
        camerabtn = findViewById(R.id.capturebtn)
        Upload = findViewById(R.id.upload)
        textresult = findViewById(R.id.textresult)
        Results = findViewById(R.id.results)
        checkandGetpermissions()

        select_image_button.setOnClickListener {
            Log.d("msg", "button pressed")
            var intent: Intent1 = Intent1(Intent1.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, 250)
        }

        camerabtn.setOnClickListener {
            var camera = Intent1(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(camera, 200)
        }

        Upload.setOnClickListener {
            UploadImage()
        }
        Results.setOnClickListener {
            FetchResults()
        }

    }

    fun checkandGetpermissions() {
        if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA), 100)
        } else {
            Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show()
        }
    }

    private fun UploadImage() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://dummyjson.com/posts/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(
                OkHttpClient.Builder()
                    .addInterceptor(HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    })
                    .build()
            )
            .build()
        val myApi = retrofit.create(MyApi::class.java)

        val imageRequest = encodedImage?.let { it1 -> ImageRequest(it1) }
        if (imageRequest != null) {
            myApi.uploadImage(imageRequest).enqueue(object : Callback<ApiResponse> {
                override fun onResponse(
                    call: Call<ApiResponse>,
                    response: Response<ApiResponse>
                ) {
                    Toast.makeText(this@MainActivity, "Image posted", Toast.LENGTH_SHORT).show()

                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    Toast.makeText(this@MainActivity, t.message, Toast.LENGTH_SHORT).show()
                }
            })


        }
    }

    private fun FetchResults() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://2b22-2401-4900-5ac4-fd3e-8cf4-1df1-7b73-800a.ngrok-free.app/predict/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(
                OkHttpClient.Builder()
                    .addInterceptor(HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    })
                    .build()
            )
            .build()
        val myApi = retrofit.create(MyApi::class.java)
        myApi.getFloat().enqueue(object : Callback<Float> {
            override fun onResponse(call: Call<Float>, response: Response<Float>) {
                var result = response.body()
                textresult.text = result.toString()
            }

            override fun onFailure(call: Call<Float>, t: Throwable) {
                Toast.makeText(this@MainActivity, t.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }


}