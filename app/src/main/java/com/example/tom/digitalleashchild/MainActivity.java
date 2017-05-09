package com.example.tom.digitalleashchild;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks{
    private Button updateLocButton;
    private EditText userNameEditText;
    private String url;
    private LocationRequest locationRequest;
    private Location currentLocation;
    private long LOCATION_INTERVAL;
    private GoogleApiClient googleApiClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        url = "https://turntotech.firebaseio.com/digitalleash/";
        userNameEditText = (EditText) findViewById(R.id.userNameEditText);
        updateLocButton = (Button) findViewById(R.id.updateLocButton);
        updateLocButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("user_name", userNameEditText.getText());
                    jsonObject.put("child_latitude", currentLocation.getLatitude());
                    jsonObject.put("child_longitude", currentLocation.getLongitude());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                new UpdateLocationTask().execute(jsonObject);
            }
        });

        LOCATION_INTERVAL = 1000;
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(LOCATION_INTERVAL);
        locationRequest.setFastestInterval(LOCATION_INTERVAL);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        googleApiClient.connect();

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    1);
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
            return;
        }
        currentLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(getApplicationContext(), "onConnectionSuspended", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(getApplicationContext(), "onConnectionFailed", Toast.LENGTH_SHORT).show();
    }

    private class UpdateLocationTask extends AsyncTask<JSONObject, Void, Void> {
            @Override
            protected Void doInBackground(JSONObject... params) {
                URL urlObj;
                try {
                    String urlString = url + params[0].get("user_name") + ".json";
                    urlObj = new URL(urlString);
                    HttpURLConnection httpCon = (HttpURLConnection) urlObj.openConnection();
                    httpCon.setDoOutput(true);
                    httpCon.setInstanceFollowRedirects(false);
                    httpCon.setRequestMethod("PATCH");
                    httpCon.setRequestProperty("Accept", "application/json");
                    httpCon.setRequestProperty("Content-Type", "application/json");
                    OutputStreamWriter wr = new OutputStreamWriter(httpCon.getOutputStream());
                    wr.write(params[0].toString());
                    wr.flush();
                    wr.close();
                    httpCon.getInputStream();
                    if (httpCon.getResponseCode() < 200 || httpCon.getResponseCode() > 299) {
                        int responseCode = httpCon.getResponseCode();
                        Log.v("responsecode", String.valueOf(responseCode));
                    } else {
                        int responseCode = httpCon.getResponseCode();
                        Log.v("responsecode", String.valueOf(responseCode));
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return null;
            }
    }
}
