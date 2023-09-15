package com.snuzj.weatherapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.android.volley.NetworkError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.snuzj.weatherapp.databinding.ActivityMainBinding;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    ArrayList<WeatherRVModel> weatherRVModelArrayList;
    WeatherRVAdapter weatherRVAdapter;
    LocationManager locationManager;

    private String TAG = "WEATHER";

    private int PERMISSION_CODE = 1;

    private String cityName;

    @SuppressLint("ServiceCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set flags for fullscreen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        weatherRVModelArrayList = new ArrayList<>();
        weatherRVAdapter = new WeatherRVAdapter(this, weatherRVModelArrayList);
        binding.weatherRv.setAdapter(weatherRVAdapter);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //init picasso
        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttp3Downloader(this, Integer.MAX_VALUE));
        Picasso built = builder.build();
        Picasso.setSingletonInstance(built);


        // Check location permissions and request them if necessary
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkLocationPermissions()) {
                // Permissions are granted; now, you can access the last known location
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (location != null) {
                    cityName = getCityName(location.getLongitude(), location.getLatitude());
                    getWeatherInfo(cityName);
                } else {
                    // Handle the case when last known location is not available
                    getWeatherInfo("Hanoi");
                }
            }
        }

        // Set a click listener for the search button
        binding.searchIv.setOnClickListener(view -> {
            String city = binding.cityNameEt.getText().toString();
            if (city.isEmpty()) {
                Toast.makeText(this, "Hãy nhập tên thành phố (liền không dấu)", Toast.LENGTH_SHORT).show();
            } else {
                binding.cityNameTv.setText(cityName);
                getWeatherInfo(city);
            }
        });
    }

    // Check location permissions and request them if necessary
    private boolean checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSION_CODE);
            return false;
        }
        return true;
    }

    // Handle permission request results
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permissions granted; you can proceed with location-based actions
                Toast.makeText(this, "Truy cập thành công", Toast.LENGTH_SHORT).show();
            } else {
                // Permissions denied; inform the user and possibly close the app
                Toast.makeText(this, "Truy cập thất bại", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    // Get city name based on latitude and longitude
    private String getCityName(double longitude, double latitude) {
        String cityName = "Not found";
        Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
        try {
            List<Address> addresses = gcd.getFromLocation(latitude, longitude, 1); // Limit results to 1
            if (addresses != null && !addresses.isEmpty()) {
                cityName = addresses.get(0).getLocality();
            } else {
                Log.d(TAG, "getCityName: CITY NOT FOUND");
                Toast.makeText(this, "Không tìm thấy thành phố bạn yêu cầu", Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error fetching city name", e);
            Toast.makeText(this, "Có lỗi xảy ra khi tìm thành phố", Toast.LENGTH_LONG).show();
        }
        return cityName;
    }

    // Fetch weather information for a given city
    public void getWeatherInfo(String cityName) {
        String url = "http://api.weatherapi.com/v1/forecast.json?key=0edbf69d773d445db8b152930231409&q=" + cityName + "&days=1&aqi=yes&alerts=yes";
        binding.cityNameTv.setText(cityName);
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

        @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"}) JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
            binding.loadingPb.setVisibility(View.GONE);
            binding.idRLHome.setVisibility(View.VISIBLE);
            weatherRVModelArrayList.clear();

            try {
                // Set temperature
                String temperature = response.getJSONObject("current").getString("temp_c");
                binding.temperatureTv.setText(temperature + "°C");

                // Set background for day and night
                int isDay = response.getJSONObject("current").getInt("is_day");
                if (isDay == 1) {
                    // Day
                    binding.backIv.setImageResource(R.drawable.background2);
                } else {
                    // Night
                    binding.backIv.setImageResource(R.drawable.background1);
                }

                // Set weather condition
                String condition = response.getJSONObject("current").getJSONObject("condition").getString("text");
                String conditionIcon = response.getJSONObject("current").getJSONObject("condition").getString("icon");
                Picasso.get().load("https:" + conditionIcon).into(binding.iconIv);
                binding.conditionTv.setText(condition);

                JSONObject forecastObj = response.getJSONObject("forecast");
                JSONObject forecastO = forecastObj.getJSONArray("forecastday").getJSONObject(0);
                JSONArray hourArray = forecastO.getJSONArray("hour");

                for (int i = 0; i < hourArray.length(); i++) {
                    JSONObject hourObj = hourArray.getJSONObject(i);
                    String time = hourObj.getString("time");
                    String temper = hourObj.getString("temp_c");
                    String img = hourObj.getJSONObject("condition").getString("icon");
                    String wind = hourObj.getString("wind_kph");
                    weatherRVModelArrayList.add(new WeatherRVModel(time, temper, img, wind));
                }
                weatherRVAdapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
            Log.e(TAG, "Error fetching weather data", error);
            if (error instanceof NetworkError) {
                Toast.makeText(MainActivity.this, "Lỗi mạng, vui lòng kiểm tra kết nối internet của bạn", Toast.LENGTH_SHORT).show();
            } else if (error instanceof VolleyError) {
                Toast.makeText(MainActivity.this, "Có lỗi xảy ra khi tải dữ liệu thời tiết", Toast.LENGTH_SHORT).show();
            }
        });

        requestQueue.add(jsonObjectRequest);
    }

    public boolean isInternetAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
