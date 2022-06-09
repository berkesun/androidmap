package com.searchmap.app;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
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
import com.searchmap.app.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {
    private static final int TAG_CODE_PERMISSION_LOCATION = 12;
    private static final HashMap<Object, Object> korrddinat = new HashMap<>();
    private static final List<Marker> mMarker = new ArrayList<>();
    private static Circle mCircle;
    private static Marker marker;
    private final DatabaseReference db = FirebaseDatabase.getInstance("https://searchmap-7d89f-default-rtdb.europe-west1.firebasedatabase.app").getReference();
    public String bestProvider;
    public Criteria criteria;
    Button btnLogOut;
    TextView countdown;
    private FirebaseAuth mAuth;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        btnLogOut = findViewById(R.id.btnLogout);
        mAuth = FirebaseAuth.getInstance();
        countdown = findViewById(R.id.txtMessage);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        btnLogOut.setOnClickListener(view -> {
            mAuth.signOut();
            startActivity(new Intent(MapsActivity.this, LoginActivity.class));
        });
        try {
            new Handler().postDelayed(this::generateRandomMarkers, 500);
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
        Objects.requireNonNull(vectorDrawable).setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
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

        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, location -> {
            if (ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                            PackageManager.PERMISSION_GRANTED) {
                criteria = new Criteria();
                bestProvider = String.valueOf(lm.getBestProvider(criteria, false));
                location = lm.getLastKnownLocation(bestProvider);
                if (location != null) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(location.getLatitude(), location.getLongitude()), 13
                    ));

                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(new LatLng(location.getLatitude(), location.getLongitude()))
                            .zoom(17)
                            .bearing(90)
                            .tilt(40)
                            .build();
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));


                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    Log.e("Latitude", String.valueOf(latitude));
                    Log.e("Longitude", String.valueOf(longitude));
                    drawCircle(location);
                    float[] distance = new float[2];
                    for (int m = 0; m < mMarker.size(); m++) {
                        marker = mMarker.get(m);
                        LatLng position = marker.getPosition();
                        double lat = position.latitude;
                        double lon = position.longitude;

                        Location.distanceBetween(lat, lon, mCircle.getCenter().latitude,
                                mCircle.getCenter().longitude, distance);
                        if (distance[0] <= mCircle.getRadius()) {
                            marker.setTitle("xd");

                            mMap.setOnMarkerClickListener(marker -> {
                                if (Objects.equals(marker.getTitle(), "xd")) {
                                    for (Marker otherMarker : mMarker) {
                                        otherMarker.remove();
                                    }

                                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                    if (user != null) {
                                        db.child(user.getUid()).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                for (DataSnapshot s : dataSnapshot.getChildren()) {
                                                    Double collect = s.child("collect").getValue(Double.class);
                                                    Double lat1 = s.child("latitude").getValue(Double.class);
                                                    Double lag1 = s.child("longitude").getValue(Double.class);
                                                    if (lat1 != null && lag1 != null) {
                                                        User.getInstance().setLat(lat1);
                                                        User.getInstance().setLon(lag1);
                                                    }
                                                    LatLng position1 = marker.getPosition();
                                                    double latm = position1.latitude;
                                                    double lagm = position1.longitude;
                                                    if (collect != null && collect.equals(0.0)) {
                                                        if (User.getInstance().getLat() == latm && User.getInstance().getLon() == lagm) {
                                                            s.child("collect").getRef().setValue(1.0);
                                                        }
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                throw error.toException();
                                            }
                                        });
                                    }
                                }
                                return false;
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
        });

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            db.child(user.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot s : dataSnapshot.getChildren()) {
                        Double collect = s.child("collect").getValue(Double.class);
                        if (collect != null && collect.equals(0.0)) {
                            Double lat = s.child("latitude").getValue(Double.class);
                            Double lag = s.child("longitude").getValue(Double.class);
                            if (lat != null && lag != null) {
                                User.getInstance().setLat(lat);
                                User.getInstance().setLon(lag);
                            }
                            marker = mMap.addMarker(new MarkerOptions().position(new LatLng(User.getInstance().getLat(), User.getInstance().getLon())).title("mid point").snippet("Snippet").icon(bitmapDescriptorFromVector(MapsActivity.this, R.drawable.ic_starbucks__1_)));
                            mMarker.add(marker);
                            marker.setVisible(true);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    throw error.toException();
                }
            });
        }
        if (user != null) {
            db.child(user.getUid()).orderByChild("collect").equalTo(1).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    long size = (int) snapshot.getChildrenCount();
                    countdown.setText(String.valueOf(size));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    public LatLng generateRandomCoordinates(int min, int max) {

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {
            criteria = new Criteria();
            LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            bestProvider = String.valueOf(lm.getBestProvider(criteria, true));
            if (location != null) {
                User.getInstance().setLon(location.getLongitude());
                User.getInstance().setLat(location.getLatitude());
            } else {
                lm.requestLocationUpdates(bestProvider, 1000, 0, this);
            }

            // 1 KiloMeter = 0.00900900900901° So, 1 Meter = 0.00900900900901 / 1000
            double meterCord = 0.00900900900901 / 1000;

            //Generate random Meters between the maximum and minimum Meters
            Random r = new Random();
            int randomMeters = r.nextInt(max + min);

            //then Generating Random numbers for different Methods
            int randomPM = r.nextInt(6);

            //Then we convert the distance in meters to coordinates by Multiplying number of meters with 1 Meter Coordinate
            double metersCordN = meterCord * (double) randomMeters;

            if (User.getInstance().getLat() != null && User.getInstance().getLon() != null) {
                //here we generate the last Coordinates
                if (randomPM == 0) {
                    return new LatLng(User.getInstance().getLat() + metersCordN, User.getInstance().getLon() + metersCordN);
                } else if (randomPM == 1) {
                    return new LatLng(User.getInstance().getLat() - metersCordN, User.getInstance().getLon() - metersCordN);
                } else if (randomPM == 2) {
                    return new LatLng(User.getInstance().getLat() + metersCordN, User.getInstance().getLon() - metersCordN);
                } else if (randomPM == 3) {
                    return new LatLng(User.getInstance().getLat() - metersCordN, User.getInstance().getLon() + metersCordN);
                } else if (randomPM == 4) {
                    return new LatLng(User.getInstance().getLat(), User.getInstance().getLon() - metersCordN);
                } else {
                    return new LatLng(User.getInstance().getLat() - metersCordN, User.getInstance().getLon());
                }
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
            korrddinat.put("collect", 0.0);
            if (user != null) {
                String userid = user.getUid();
                db.child(userid).push().setValue(korrddinat);
            }
        }
    }

    private void drawCircle(Location location) {
        if (mCircle != null) {
            mCircle.remove();
        }
        LatLng currentPosition = new LatLng(location.getLatitude(), location.getLongitude());
        CircleOptions circleOptions = new CircleOptions()
                .center(currentPosition)
                .radius(75000)
                .strokeWidth(2)
                .strokeColor(Color.RED)
                .fillColor(Color.parseColor("#500084d3"));
        mCircle = mMap.addCircle(circleOptions);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {

    }

    @Override
    public void onLocationChanged(@NonNull List<Location> locations) {
        LocationListener.super.onLocationChanged(locations);
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        LocationListener.super.onProviderEnabled(provider);
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        LocationListener.super.onProviderDisabled(provider);
    }
}