package com.example.kk;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,LocationListener {
    private static final int TAG_CODE_PERMISSION_LOCATION = 12;
    private FirebaseAuth mAuth;
    Button btnLogOut;
    private GoogleMap mMap;
    private static HashMap<String, Number> korrddinat = new HashMap<>();
    private static Circle mCircle;
    private DatabaseReference db = FirebaseDatabase.getInstance("https://kk3131-48548-default-rtdb.europe-west1.firebasedatabase.app").getReference();
    private static List<Marker> mMarker = new ArrayList<>();
    private static Marker marker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        btnLogOut = findViewById(R.id.btnLogout);
        mAuth = FirebaseAuth.getInstance();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        btnLogOut.setOnClickListener(view -> {
            mAuth.signOut();
            startActivity(new Intent(MapsActivity.this, LoginActivity.class));
        });
        try {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    generateRandomMarkers();

                }
            }, 30000*99);
        } catch (Exception e) {
            e.printStackTrace();
        }


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
    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);

        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    TAG_CODE_PERMISSION_LOCATION);
        }

            LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, new LocationListener() {
                    @Override
                    public void onLocationChanged(@NonNull Location location) {
                        if (ContextCompat.checkSelfPermission(MapsActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                                PackageManager.PERMISSION_GRANTED &&
                                ContextCompat.checkSelfPermission(MapsActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                                        PackageManager.PERMISSION_GRANTED) {
                            Criteria criteria = new Criteria();
                            String bestProvider = String.valueOf(lm.getBestProvider(criteria, true)).toString();
                            location = lm.getLastKnownLocation(bestProvider);
                            if (location != null) {
                                Log.e("TAG", "GPS is on");
                                double latitude = location.getLatitude();
                                double longitude = location.getLongitude();
                                drawCircle(location);
                                float[] distance = new float[2];
                                for(int m = 0; m < mMarker.size(); m++){
                                    marker = mMarker.get(m);
                                    LatLng position = marker.getPosition();
                                    double lat = position.latitude;
                                    double lon = position.longitude;

                                    Location.distanceBetween(lat, lon, mCircle.getCenter().latitude,
                                            mCircle.getCenter().longitude, distance);
                                    if(distance[0] <= mCircle.getRadius()){
                                        marker.setTitle("xd");
                                        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                                            @Override
                                            public boolean onMarkerClick(@NonNull Marker marker) {
                                                if(marker.getTitle().equals("xd")){
                                                    marker.remove();
                                                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                                    db.child(user.getUid()).addValueEventListener(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            for (DataSnapshot s : dataSnapshot.getChildren()) {
                                                                Double collect = s.child("collect").getValue(Double.class);
                                                                double lat = s.child("latitude").getValue(Double.class);
                                                                double lag = s.child("longitude").getValue(Double.class);
                                                                LatLng position = marker.getPosition();
                                                                double latm = position.latitude;
                                                                double lagm = position.longitude;
                                                                if(collect.equals(0.0)){
                                                                    if(lat == latm && lag == lagm){
                                                                        s.child("collect").getRef().setValue(1.0);




                                                                    }

                                                                }



                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError error) {
                                                            throw error.toException();

                                                        }
                                                    });




                                                }
                                                return false;
                                            }
                                        });


                                    }
                                }



                            }


                        } else {
                            ActivityCompat.requestPermissions(MapsActivity.this, new String[]{
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION},
                                    TAG_CODE_PERMISSION_LOCATION);
                        }
                        }
                });



        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String currentUserID = user.getUid(); //Do what you need to do with the id
        }
        db.child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot s : dataSnapshot.getChildren()) {
                    Double collect = s.child("collect").getValue(Double.class);
                    if(collect.equals(0.0)) {
                        double lat = s.child("latitude").getValue(Double.class);
                        double lag = s.child("longitude").getValue(Double.class);
                        marker = mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lag)).title("mid point").snippet("Snippet").icon(bitmapDescriptorFromVector(MapsActivity.this, R.drawable.ic_starbucks__1_)));
                        mMarker.add(marker);

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                throw error.toException();

            }
        });
    }


    public LatLng generateRandomCoordinates(int min, int max) {

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {


            Location location = new Location("");
            location.setLatitude(39.7792496);
            location.setLongitude(32.8130252);
            double currentLong = location.getLongitude();
                double currentLat = location.getLatitude();

                // 1 KiloMeter = 0.00900900900901° So, 1 Meter = 0.00900900900901 / 1000
                double meterCord = 0.00900900900901 / 1000;

                //Generate random Meters between the maximum and minimum Meters
                Random r = new Random();
                int randomMeters = r.nextInt(max + min);

                //then Generating Random numbers for different Methods
                int randomPM = r.nextInt(6);

                //Then we convert the distance in meters to coordinates by Multiplying number of meters with 1 Meter Coordinate
                double metersCordN = meterCord * (double) randomMeters;

                //here we generate the last Coordinates
                if (randomPM == 0) {
                    return new LatLng(currentLat + metersCordN, currentLong + metersCordN);
                } else if (randomPM == 1) {
                    return new LatLng(currentLat - metersCordN, currentLong - metersCordN);
                } else if (randomPM == 2) {
                    return new LatLng(currentLat + metersCordN, currentLong - metersCordN);
                } else if (randomPM == 3) {
                    return new LatLng(currentLat - metersCordN, currentLong + metersCordN);
                } else if (randomPM == 4) {
                    return new LatLng(currentLat, currentLong - metersCordN);
                } else {
                    return new LatLng(currentLat - metersCordN, currentLong);
                }



        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    TAG_CODE_PERMISSION_LOCATION);
        }

        return null;
    }

    void generateRandomMarkers() {
        //set your own minimum distance here
        int minimumDistanceFromMe = 10;
        //set your own maximum distance here
        int maximumDistanceFromMe = 1000;
        //set number of markers you want to generate in Map/
        int markersToGenerate = 5;
        for (int position = 1; position <= markersToGenerate; position++) {
            LatLng coordinates = generateRandomCoordinates(minimumDistanceFromMe, maximumDistanceFromMe);

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            korrddinat.put("latitude", coordinates.latitude);
            korrddinat.put("longitude", coordinates.longitude);
            korrddinat.put("collect",0.0);
            if (user != null) {
                String userid = user.getUid();
                db.child(userid).push().setValue(korrddinat);

            }

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            startActivity(new Intent(MapsActivity.this, LoginActivity.class));
        }

    }


    @Override
    protected void onResume() {
        super.onResume();


    }

    private void drawCircle(Location location)
    {
        if(mCircle != null){
            mCircle.remove();
        }
        LatLng currentPosition = new LatLng(location.getLatitude(),location.getLongitude());
        CircleOptions circleOptions = new CircleOptions()
                .center(currentPosition)
                .radius(750)
                .strokeWidth(2)
                .strokeColor(Color.BLUE)
                .fillColor(Color.parseColor("#500084d3"));
        mCircle = mMap.addCircle(circleOptions);


    }

    @Override
    public void onLocationChanged(@NonNull Location location) {

    }
}