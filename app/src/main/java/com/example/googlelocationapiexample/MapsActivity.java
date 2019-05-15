package com.example.googlelocationapiexample;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
                                                                GoogleMap.OnMarkerClickListener {

    private final String TAG = "MainActivity";
    private GoogleMap mMap;
    private ArrayList<Contato> contactList = new ArrayList<>(5);
    private Cursor cursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void getContacts() {
        // Try get the data from web
        try {
            JSONObject pokemonJson = readJson("http://www.mocky.io/v2/5cdb4544300000640068cc7b");
            JSONArray list = pokemonJson.getJSONArray("pessoas");
            JSONObject jsonLineItem;
            String name, email;
            double lat, lon;
            DAL dal = new DAL(this);
            for (int i = 0; i < list.length(); i++) {
                jsonLineItem = (JSONObject) list.get(i);
                name = jsonLineItem.getString("nome");
                email = jsonLineItem.getString("email");
                lat = jsonLineItem.getDouble("latitude");
                lon = jsonLineItem.getDouble("longitude");
                Log.i(TAG, "Name: " + name + " - Email: " + email + " - lat: " + lat + " - lon: " + lon);
                contactList.add(new Contato(name, email, lat, lon));

                if (dal.insert(name, email, lat, lon)) {
                    Log.i(TAG, "Usuário inserido com sucesso!");
                }

            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private JSONObject readJson(String url) {
        JsonTask readPokemon = new JsonTask();

        try {
            String data = readPokemon.execute(url).get();
            JSONObject pokemonJson = new JSONObject(data);
            return pokemonJson;
        } catch (Exception e) {
            Log.e(TAG, "readJson: Erro buscar json: " + e.getMessage());
        }

        return null;
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        String[] fields = new String[] {CreateDatabase.ID, CreateDatabase.NAME};
        Log.i(TAG, String.valueOf(fields));
        DAL dal = new DAL(this);
        cursor = dal.loadAll();
        Log.i(TAG, " " + cursor.getCount());
        if (cursor.getCount() == 0) {
            getContacts();
        } else {
            if (cursor.moveToFirst()) {
                while (cursor.isAfterLast()) {
                    String name = cursor.getString(cursor.getColumnIndex(CreateDatabase.NAME));
                    Log.i(TAG, name);
                }
            }
        }

        LatLng user = new LatLng(0, 0);
        for (Contato i : contactList) {
            Log.i(TAG, " a" + i);
            user = new LatLng(i.getLat(), i.getLon());
            mMap.addMarker(new MarkerOptions().position(user).title(i.getName() + " - " + i.getEmail()));
        }

        mMap = googleMap;
        googleMap.setOnMarkerClickListener(this);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(user));
    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        Log.i(TAG, marker.getTitle());
        AlertDialog alert;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Contato info");
        builder.setMessage(marker.getTitle());

        builder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {

            }
        });
        alert = builder.create();
        alert.show();

        return false;
    }


    private class JsonTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected String doInBackground(String... params) {

            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) buffer.append(line+"\n");

                return buffer.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }
    }
}
