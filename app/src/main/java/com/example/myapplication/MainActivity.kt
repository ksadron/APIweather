package com.example.myapplication

import android.annotation.SuppressLint
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.example.myapplication.R
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var weatherTextView: TextView
    private lateinit var cityEditText: EditText
    private lateinit var fetchWeatherButton: Button
    private lateinit var weatherImageView: ImageView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        weatherTextView = findViewById(R.id.weatherTextView)
        cityEditText = findViewById(R.id.cityEditText)
        fetchWeatherButton = findViewById(R.id.fetchWeatherButton)
        weatherImageView = findViewById(R.id.weatherImageView)

        fetchWeatherButton.setOnClickListener {
            val city = cityEditText.text.toString()
            val apiKey = "37cdc0ffa46245ba8f6d6194bf612e0d"
            val url = "https://api.openweathermap.org/data/2.5/weather?q=$city&appid=$apiKey"
            WeatherTask().execute(url)
        }
    }


    inner class WeatherTask : AsyncTask<String, Void, String>() {
        override fun doInBackground(vararg params: String?): String? {
            var response: String?
            try {
                val url = URL(params[0])
                val connection = url.openConnection() as HttpURLConnection
                connection.connect()

                val inputStream: InputStream = connection.inputStream
                val reader = BufferedReader(InputStreamReader(inputStream))
                val stringBuilder = StringBuilder()

                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    stringBuilder.append(line)
                }

                response = stringBuilder.toString()
                inputStream.close()
                connection.disconnect()
            } catch (e: Exception) {
                response = null
            }
            return response
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            try {
                val jsonObj = JSONObject(result)
                val main = jsonObj.getJSONObject("main")
                val temperatureKelvin = main.getDouble("temp")
                val temperatureCelsius = temperatureKelvin - 273.15

                val weatherArray = jsonObj.getJSONArray("weather")
                val weatherObj = weatherArray.getJSONObject(0)
                val description = weatherObj.getString("description")


                val drawableId = when {
                    description.contains("sunny", ignoreCase = true) -> R.drawable.pobrane1
                    description.contains("cloud", ignoreCase = true) -> R.drawable.images
                    description.contains("rain", ignoreCase = true) -> R.drawable.pobrane
                    else -> R.drawable.pobrane1
                }

                weatherImageView.setImageResource(drawableId)

                val cityName = jsonObj.getString("name")

                val formattedTemperature = String.format(Locale.getDefault(), "%.1f", temperatureCelsius)

                val weatherText = "$cityName\nOpis: $description\nTemperatura: $formattedTemperature °C"
                val spannable = SpannableString(weatherText)
                spannable.setSpan(RelativeSizeSpan(1.5f), weatherText.indexOf(formattedTemperature), weatherText.indexOf(formattedTemperature) + formattedTemperature.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                weatherTextView.text = spannable
            } catch (e: Exception) {
                weatherTextView.text = "Błąd podczas pobierania danych"
            }
        }
    }
}
