package com.example.skycast

import android.hardware.lights.Light
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.skycast.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.locks.Condition

//

class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        fetchWeatherData("Delhi")
        SearchCity()
    }

    private fun SearchCity() {
        val searchView = binding.searchView
        searchView.setOnQueryTextListener(object :android.widget.SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    fetchWeatherData(query)
                }
                return true
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                return true
            }

        })
    }


    private fun fetchWeatherData(cityName: String) {
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .build()
            .create(ApiInterface::class.java)
        val response = retrofit.getWeatherData(cityName , "3be41e2a359cb318dc3b06d237a9e1ee" , "metric")
        response.enqueue(object : Callback<WeatherApp>{
            override fun onResponse(call: Call<WeatherApp>, response: Response<WeatherApp>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        val temperature = responseBody.main.temp.toString()
                        val humidity = responseBody.main.humidity
                        val windSpeed = responseBody.wind.speed
                        val sunRise = responseBody.sys.sunrise.toLong()
                        val seaLevel = responseBody.main.pressure
                        val condition = responseBody.weather.firstOrNull()?.main?:"unknown"
                        val sunSet = responseBody.sys.sunset.toLong()
                        val maxTem = responseBody.main.temp_max
                        val minTem = responseBody.main.temp_min


                        Log.d("WeatherApp", "Temperature: $temperature")
                        binding.temperature.text = "$temperature°C"
                        binding.weather.text = condition
                        binding.maxtam.text = "Max Temp: $maxTem°C"
                        binding.mintam.text = "Min Temp: $minTem°C"
                        binding.humidity.text = "$humidity %"
                        binding.wind.text = "$windSpeed m/s"
                        binding.sunrise.text = "${time(sunRise)}"
                        binding.sunset.text = "${time(sunSet)}"
                        binding.sea.text = "$seaLevel hPa"
                        binding.conditions.text = condition
                        binding.day.text = dayname(System.currentTimeMillis())
                            binding.date.text=  date()
                            binding.cityname.text ="$cityName"

                        changesImagesAcoddingToWearther(condition)

                    } else {
                        Log.e("WeatherApp", "Response body is null")
                    }
                } else {
                    Log.e("WeatherApp", "Response not successful. Code: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<WeatherApp>, t: Throwable) {
                Log.e("WeatherApp", "API call failed: ${t.message}")
            }


        })
    }

    private fun changesImagesAcoddingToWearther(condition: String) {
        when(condition){

            "Clear Sky" , "Sunny" , "Clear" ->{
                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.lottieAnimationView.setAnimation(R.raw.sun)
            }

            "Haze" , "Pertly Clouds" , "Overcast" , "Mist" , "Foggy" , "Clouds" ->{
                binding.root.setBackgroundResource(R.drawable.colud_background)
                binding.lottieAnimationView.setAnimation(R.raw.cloud)
            }
            "Light Rain" , "Drizzle" , " Moderate Rain" , "Showers" , "Heavy Rain" -> {
                binding.root.setBackgroundResource(R.drawable.rain_background)
                binding.lottieAnimationView.setAnimation(R.raw.rain)
            }
            "Light Snow"  , "Moderate Snow","Heavy Snow" , " Blizzard" -> {
                binding.root.setBackgroundResource(R.drawable.rain_background)
                binding.lottieAnimationView.setAnimation(R.raw.rain)

            }
            else ->{
                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.lottieAnimationView.setAnimation(R.raw.sun)
            }

        }
        binding.lottieAnimationView.playAnimation()
    }

    private fun date(): String {
        val sdf = SimpleDateFormat("dd MMMM yyyy" , Locale.getDefault())
        return sdf.format((Date()))
    }

    fun dayname(timestamp: Long):String{
        val sdf = SimpleDateFormat("EEEE" , Locale.getDefault())
        return sdf.format((Date()))
    }

    fun time(timestamp: Long):String{
        val sdf = SimpleDateFormat("HH:mm" , Locale.getDefault())
        return sdf.format((Date(timestamp*1000)))
    }
}

