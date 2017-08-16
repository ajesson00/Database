package tutorial.databasecontact;

import android.Manifest;
import android.content.Context;
import android.icu.util.GregorianCalendar;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    RequestQueue queue;
    LocationManager lm;
    Location location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        queue = Volley.newRequestQueue(this);
        lm  = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        final String pn = "";


        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                1);

        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                double lng = location.getLongitude();
                double lat = location.getLatitude();
                Calendar rightNow = Calendar.getInstance();
                String time = rightNow.get(Calendar.HOUR) + ":" + rightNow.get(Calendar.MINUTE) + ":" + rightNow.get(Calendar.SECOND);
                postNewComment(getApplicationContext(), pn, lat, lng, time);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };

        try {
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 200, 10, locationListener);
        } catch (SecurityException e) {
            Toast.makeText(getApplicationContext(), "Failed to update", Toast.LENGTH_LONG).show();
        }
    }



    private static void contactServer(final Context context) {
        MainActivity mainActivity = (MainActivity) context;
        String url = "http://jaguar.cs.pdx.edu/~rfeng/test/?pn=7895254555";
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>(){
            @Override
            public void onResponse(String response) {
                Toast.makeText(context, "Received the following data: " + response, Toast.LENGTH_LONG).show();
                try {
                    JSONObject reader = new JSONObject(response);
                    JSONObject loc = reader.getJSONObject("location");
                    double lat, lng;
                    lat = loc.getDouble("latitude");
                    lng = loc.getDouble("longitude");
                    String pn, time;
                    pn = reader.getString("number");
                    time = reader.getString("time");
                    Toast.makeText(context, "latitude:" + lat + " longitude:" + lng + " phone number:" + pn + " time:" + time, Toast.LENGTH_LONG).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            //program throws this even when it successfully updates the server
            //really not sure why
        }
        });

        mainActivity.queue.add(request);

    }

    private static void postNewComment(final Context context, final String pn, final double lat, final double lng, final String time) {
        MainActivity mainActivity = (MainActivity) context;
        JSONObject params = new JSONObject();
        String url = "http://jaguar.cs.pdx.edu/~rfeng/test/"; //+ pn;

        try {
            params.put("pn", pn);
            params.put("lat", lat);
            params.put("long", lng);
            params.put("time", time);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject o) {
                Toast.makeText(context, "Logged location to database" + o, Toast.LENGTH_LONG).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError e) {
                Toast.makeText(context, "Failed to log location", Toast.LENGTH_LONG).show();
            }
        });


        mainActivity.queue.add(jsonRequest);
    }
}
