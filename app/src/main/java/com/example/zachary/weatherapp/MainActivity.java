package com.example.zachary.weatherapp;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public static final String API_KEY = "&key=AIzaSyDuo9-DNqIY8LAVvIVCfRicNHRfmMMxM5I";
    public static final String base_URL = "https://maps.googleapis.com/maps/api/geocode/json?address=";

    MapView mapView;
    GoogleMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final LatLng utAustin = new LatLng(30.286219, -97.73946);

        Button submitButton = findViewById(R.id.submitButton);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String addr = ((EditText)findViewById(R.id.addressBox)).getText().toString();
                String[] addrComps = addr.split(",");
                String url_str = base_URL;
                for(int i = 0; i < addrComps.length; i++){
                    addrComps[i] = addrComps[i].replace(" ", "+");
                    url_str += addrComps[i];
                    if(i != addrComps.length - 1){
                        url_str += ",";
                    }
                }
                url_str += API_KEY;
                System.out.println(url_str);
                Geocoding task = new Geocoding(MainActivity.this);
                task.execute(url_str);

            }
        });

        mapView = findViewById(R.id.mapView);
        if(mapView != null){
            mapView.onCreate(null);
            mapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    map = googleMap;
                    map.setMapType(2);
                    map.animateCamera(CameraUpdateFactory.newLatLng(utAustin));
                    System.out.println("animated camera");
                    System.out.println(map.getMapType());
                }
            });
        }
    }
}

class Geocoding extends AsyncTask<String, Integer, String> {

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        MapView mapView = context.findViewById(R.id.mapView);
    }

    String url_str;
    Activity context;
    double lat;
    double lng;


    public Geocoding(Activity ctxt){
        context = ctxt;
    }

    @Override
    protected String doInBackground(String...link) {

        URL url;
        HttpURLConnection urlConnection = null;
        url_str = link[0];
        String response = "";

        try {
            url = new URL(url_str);

            urlConnection = (HttpURLConnection) url.openConnection();
            int responseCode = urlConnection.getResponseCode();

            if(responseCode == HttpURLConnection.HTTP_OK){
                InputStream in = urlConnection.getInputStream();
                response = readStream(in);
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            JSONObject jsonObj = new JSONObject(response);
            JSONArray results = jsonObj.getJSONArray("results");
            Object obj = results.get(0);
            JSONObject geometry = new JSONObject(obj.toString()).getJSONObject("geometry");
            JSONObject coords = geometry.getJSONObject("location");
            lat = coords.getDouble("lat");
            lng = coords.getDouble("lng");

        }
        catch(JSONException e){
            e.printStackTrace();
        }

        DataFetcher fetch = new DataFetcher(context);
        fetch.execute(Double.toString(lat), Double.toString(lng));

        return null;
    }

    private String readStream(InputStream in){

        BufferedReader reader = null;
        StringBuffer resp = new StringBuffer();

        try{
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";

            while((line = reader.readLine()) != null){
                resp.append(line);
            }

            if(reader != null){
                reader.close();
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }

        return resp.toString();

    }
}

class DataFetcher extends AsyncTask<String, Integer, ArrayList<String>> {

    ArrayList<String> conditions;
    Activity context;

    public DataFetcher(Activity ctxt){
        context = ctxt;
    }

    @Override
    protected void onPostExecute(ArrayList<String> strings) {
        super.onPostExecute(strings);
        TextView precip = context.findViewById(R.id.precipitationText);
        precip.setText("Precipitation Prob.: " + conditions.get(0));
        TextView humidity = context.findViewById(R.id.humidityText);
        humidity.setText("Humidity: " + conditions.get(2));
        TextView temp = context.findViewById(R.id.tempText);
        temp.setText("Temperature: " + conditions.get(1));
        TextView windSpeed = context.findViewById(R.id.windspeedText);
        windSpeed.setText("Wind Speed: " + conditions.get(3));
    }

    @Override
    protected ArrayList<String> doInBackground(String...vals) {
        return latLon(vals[0], vals[1]);
    }


    private ArrayList<String> latLon(String lat, String lon) {
        ArrayList<String> rval = new ArrayList<>();

        try {
            URL addr = new URL("https://api.darksky.net/forecast/" + "493c3a0424ba8f4ce5b324112baf2a13" + "/" + lat.toString() + "," + lon.toString());
            HttpURLConnection request = (HttpURLConnection) addr.openConnection();
            request.setRequestMethod("GET");
            request.setRequestProperty("Accept", "application/json");

            if (request.getResponseCode() != 200) {
                throw new RuntimeException("Error: " + request.getResponseCode() + " with message " + request.getResponseMessage());
            }
            BufferedReader inp = new BufferedReader(new InputStreamReader(request.getInputStream()));
            String fulldata = "";
            while (true) {
                String nl = inp.readLine();
                if (nl == null)
                    break;
                fulldata += nl;
                System.out.println(nl);
            }

            JSONObject object = (JSONObject) new JSONTokener(fulldata).nextValue();
            JSONObject currently = object.getJSONObject("currently");
            String precip = currently.getDouble("precipProbability")*100 + "%";
            System.out.println(precip);
            rval.add(precip);
            rval.add(Double.toString(currently.getDouble("temperature")) + " degrees F");
            String humidity = Double.toString(currently.getDouble("humidity")*100) + "%";
            rval.add(humidity);
            String windSpeed = currently.getDouble("windSpeed") + " mph";
            rval.add(windSpeed);
            System.out.println(rval);

            request.disconnect();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        conditions = rval;
        return rval;
    }
}