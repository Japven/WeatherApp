package com.example.weatherapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;


public class MainActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener {

    private ViewPager viewPager;

    private boolean CelsiusOn = true;

    private String CountryNameByCode;
    private String placeName;
    Button searchButton;
    TextView resultPlace, resultPlace2, result, temperatureView, moreDetails, cityName;
    TextView localTime;
    ImageView imageWeather;



    @SuppressLint("SetTextI18n")
    public void searchForActualWeather(View view) {

        result = findViewById(R.id.weatherViewTab1);
        resultPlace = findViewById(R.id.placeNameViewTab1);
        resultPlace2 = findViewById(R.id.placeNameViewTab2);
        moreDetails = findViewById(R.id.moreDetails);
        temperatureView = findViewById(R.id.temperatureView);
        imageWeather = findViewById(R.id.imageWeather);
        localTime = findViewById(R.id.localTime);
        String cName = cityName.getText().toString();
        String unitType = "";
        String content;
        String API_KEY = "9a7f653436dc4ac4ce27508e8bac6c8a";
        Weather weather = new Weather();
        if (CelsiusOn) {
            unitType = "&units=metric";
        } else {
            unitType = "&units=imperial";
        }
        try {

            content = weather.execute("https://api.openweathermap.org/data/2.5/weather?q="
                    + cName + "&appid=" + API_KEY + "&lang=pl" + unitType).get();
            Log.i("contentData", content);

            //JSON
            JSONObject jsonObject = new JSONObject(content);
            String weatherData = jsonObject.getString("weather");
            String mainTemp = jsonObject.getString("main");
            placeName = jsonObject.getString("name");
            String mainSys = jsonObject.getString("sys");
            String dateTime = jsonObject.getString("dt");
            String timezone = jsonObject.getString("timezone");


            Log.i("weatherData", weatherData);
            //dane pogodowe w tablicy
            JSONArray array = new JSONArray(weatherData);

            String mainString = "main";
            String main = "";
            String description = "";
            String temperatureUnit = "";
            String iconWeather = "";

            for (int i = 0; i < array.length(); i++) {
                JSONObject weatherPart = array.getJSONObject(i);
                main = weatherPart.getString(mainString);

                description = weatherPart.getString("description");
                iconWeather = weatherPart.getString("icon");

            }


            JSONObject mainPart = new JSONObject(mainTemp);
            String temperature = mainPart.getString("temp");
            String temperatureWithoutRound = temperature;
            temperature = String.valueOf(Math.round(Double.parseDouble(temperature)));

            String humidity = mainPart.getString("humidity");
            String pressure = mainPart.getString("pressure");
            String tempFeelsLike = mainPart.getString("feels_like");


            JSONObject sysPart = new JSONObject(mainSys);

            String sunrise = sysPart.getString("sunrise");
            String sunset = sysPart.getString("sunset");
            sunset = unixToDateWithoutDay(sunset, timezone);
            sunrise = unixToDateWithoutDay(sunrise, timezone);
            dateTime = unixToDate(dateTime, timezone);


            String countryCode = sysPart.getString("country");
            Locale loc = new Locale("pl", countryCode);
            CountryNameByCode = loc.getDisplayCountry();

            //tłumaczenie danych, które nie tłumaczy API OpenWeatherMaps
            main = setObjectToLangPL(main);

            setDayNight(iconWeather);

            if (CelsiusOn) {
                temperatureUnit = "°C";
            } else {
                temperatureUnit = "°F";
            }


            Log.i("main", main);
            Log.i("description", description);
            Picasso.get().load("https://openweathermap.org/img/wn/" + iconWeather + "@4x.png").resize(200, 200).into(imageWeather);
            resultPlace.setText(placeName + ", " + countryCode);
            resultPlace2.setText(placeName + ", " + countryCode);
            temperatureView.setText(temperature + temperatureUnit);
            result.setText(main + "\n" + description);

            //ostatni update pogody w bazie danych dla czasu lokalnego
            localTime.setText("Ostatnia aktualizacja\n(czas lokalny):\n" +dateTime);
            moreDetails.setText("Wschód słońca: "+ sunrise +
                    "\nZachód słońca: "+ sunset +
                    "\nTemperatura: "+temperatureWithoutRound + temperatureUnit +
                    "\nOdczuwalna: " + tempFeelsLike + temperatureUnit+
                    "\nCiśnienie: "+pressure+" hpa"+
                    "\nWilgotność: "+humidity+"%");


        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Wprowadzono nieprawidłową nazwę miejscowości", Toast.LENGTH_SHORT).show();
        }
    }

    //dwie funkcje realizujące konwersję czasu unixowego na datę w ciągu tekstowym
    private String unixToDate(String unix_timestamp, String unix_timezone) throws ParseException {
        long timestamp = Long.parseLong(unix_timestamp);
        long timezone = Long.parseLong(unix_timezone);
        long timestampFinal = (timestamp+timezone)*1000;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd, HH:mm", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String date = sdf.format(timestampFinal);

        return date;
    }

    private String unixToDateWithoutDay(String unix_timestamp, String unix_timezone) throws ParseException {
        long timestamp = Long.parseLong(unix_timestamp);
        long timezone = Long.parseLong(unix_timezone);
        long timestampFinal = (timestamp+timezone)*1000;

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String date = sdf.format(timestampFinal);

        return date;
    }



    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchButton = findViewById(R.id.searchButton);
        resultPlace = findViewById(R.id.placeNameViewTab1);
        //Adding toolbar to the activity
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Initializing the tablayout
        //This is our tablayout
        TabLayout tabLayout = findViewById(R.id.tabLayout);

        //Adding the tabs using addTab() method
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        //Initializing viewPager
        viewPager = findViewById(R.id.pager);

        //Creating our pager adapter
        Pager adapter = new Pager(getSupportFragmentManager(), tabLayout.getTabCount());

        //Adding adapter to pager
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

        //Adding onTabSelectedListener to swipe views
//        tabLayout.addOnTabSelectedListener(this);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        final TextView edittext = (TextView) findViewById(R.id.placeName);
        edittext.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    searchForActualWeather(v);
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });

        searchButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                searchForActualWeather(v);
                //tą metodą ukrywamy klawiaturę
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                return true;
            }
        });


        cityName = (TextView) findViewById(R.id.placeName);

    }

    //konwersja kodu kraju na kraj
    public void showCountryByCode(View view) {
        if (!(CountryNameByCode=="")) {
            String showCountry = placeName + ", " + CountryNameByCode;
            Toast.makeText(this, showCountry, Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menuoptions, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){
            case R.id.aboutApp:
                Toast.makeText(this, "Aplikacja pogodowa - Konrad Ledwa", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.unitCelsius:
                Toast.makeText(this, "Wybrałeś jednostkę w stopniach Celsjusza", Toast.LENGTH_SHORT).show();
                CelsiusOn=true;
                return true;
            case R.id.unitFahrenheit:
                Toast.makeText(this, "Wybrałeś jednostkę w stopniach Fahrenheita", Toast.LENGTH_SHORT).show();
                CelsiusOn=false;
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    public String setObjectToLangPL(String main){
        if (main.equals("Clear")){
            main ="Brak chmur";
        }
        else if (main.equals("Clouds")){
            main ="Chmury";
        }
        else if (main.equals("Rain")){
            main ="Deszcz";
        }
        else if (main.equals("Thunderstorm")){
            main ="Burza";
        }
        else if (main.equals("Mist")||main.equals("Fog")||main.equals("Haze")){
            main ="Mgła";
        }
        else if (main.equals("Snow")){
            main ="Śnieg";
        }
        else if (main.equals("Drizzle")){
            main ="Mżawka";
        }
        else if (main.equals("Dust")||main.equals("Ash")||main.equals("Sand")){
            main ="Pył";
        }
        else if (main.equals("Smoke")){
            main ="Smog";
        }
        return main;
    }

    public void setDayNight(String iconWeather){
        if (iconWeather.contains("n")) {
            viewPager.setBackgroundColor(Color.parseColor("#150F47"));
            resultPlace.setTextColor(Color.parseColor("#FFFFFF"));
            resultPlace2.setTextColor(Color.parseColor("#FFFFFF"));
            result.setTextColor(Color.parseColor("#FFFFFF"));
            temperatureView.setTextColor(Color.parseColor("#FFFFFF"));
            moreDetails.setTextColor(Color.parseColor("#FFFFFF"));
            localTime.setTextColor(Color.parseColor("#FFFFFF"));

        }
        else{
            viewPager.setBackgroundColor(Color.parseColor("#ADA4FD"));
            resultPlace.setTextColor(Color.parseColor("#000000"));
            resultPlace2.setTextColor(Color.parseColor("#000000"));
            result.setTextColor(Color.parseColor("#000000"));
            temperatureView.setTextColor(Color.parseColor("#000000"));
            moreDetails.setTextColor(Color.parseColor("#000000"));
            localTime.setTextColor(Color.parseColor("#000000"));

        }
    }

}


