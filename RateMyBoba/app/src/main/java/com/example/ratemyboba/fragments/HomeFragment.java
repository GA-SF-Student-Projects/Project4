package com.example.ratemyboba.fragments;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.ratemyboba.R;
import com.example.ratemyboba.activities.ShopActivity;
import com.example.ratemyboba.adapters.TeaAdapter;
import com.example.ratemyboba.adapters.TeaShopAdapter;
import com.example.ratemyboba.models.Tea;
import com.example.ratemyboba.util.RV_Space_Decoration;
import com.firebase.client.Firebase;
import com.github.clans.fab.FloatingActionButton;
import com.yelp.clientlib.connection.YelpAPI;
import com.yelp.clientlib.connection.YelpAPIFactory;
import com.yelp.clientlib.entities.Business;
import com.yelp.clientlib.entities.SearchResponse;
import com.yelp.clientlib.entities.options.CoordinateOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by adao1 on 5/1/2016.
 */
public class HomeFragment extends Fragment implements TeaAdapter.OnTeaClickListener, TeaShopAdapter.OnTeaShopClickListener{

    public static final String DETAIL_KEY = "DETAILKEY";
    private static final int PERMISSION_REQUEST_CODE = 12;
    private static final String TAG = "HOME FRAGMENT";
    List<Tea> teaList;
    PassClickedTeaListener teaListener;
    OnBobaFabClickListener bobaFabListener;
    FloatingActionButton bobaFab;
    FloatingActionButton distanceFab;
    FloatingActionButton ratingsFab;
    FloatingActionButton dealsFab;
    private ArrayList<Business> teaShopList;
    private RecyclerView teaRV;
    private TeaShopAdapter teaShopAdapter;
    private double latitude;
    private double longitude;
    private LocationManager locationManager;
    TeaAdapter teaAdapter;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home,container,false);
        teaRV = (RecyclerView)view.findViewById(R.id.home_RV_id);
        bobaFab = (FloatingActionButton)view.findViewById(R.id.home_fab_boba_id);
        distanceFab = (FloatingActionButton)view.findViewById(R.id.home_fab_distance_id);
        ratingsFab = (FloatingActionButton)view.findViewById(R.id.home_fab_rating_id);
        dealsFab = (FloatingActionButton)view.findViewById(R.id.home_fab_deals_id);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        teaList = new ArrayList<>();
        teaShopList = new ArrayList<>();
        fillTempList(); //TEMP/PLACEHOLDER
        locationManager = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
        setRV();
//        setTeaRV();
//        setTeaShopRV();
        teaRV.setAdapter(teaAdapter);
        setFabListener();
    }

    private void setRV(){
        teaAdapter = new TeaAdapter(teaList,this);
        teaShopAdapter = new TeaShopAdapter(teaShopList,this,latitude,longitude);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(),2);
        teaRV.setLayoutManager(gridLayoutManager);
        RV_Space_Decoration decoration = new RV_Space_Decoration(14);
        teaRV.addItemDecoration(decoration);
    }

    private void setTeaRV(){
        fillTempList();
        teaAdapter = new TeaAdapter(teaList,this);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(),2);
        teaRV.setLayoutManager(gridLayoutManager);
        RV_Space_Decoration decoration = new RV_Space_Decoration(16);
        teaRV.addItemDecoration(decoration);
    }

    private void setTeaShopRV(){
        teaShopAdapter = new TeaShopAdapter(teaShopList,this,latitude,longitude);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(),2);
        teaRV.setLayoutManager(gridLayoutManager);
        RV_Space_Decoration decoration = new RV_Space_Decoration(14);
        teaRV.addItemDecoration(decoration);
    }

    private void initFirebase(){
        Firebase firebaseRef = new Firebase("https://rate-my-boba.firebaseio.com/");
        Firebase firebaseChildShop = firebaseRef.child("Shops").child("i-tea-san-francisco-3");
        if (firebaseChildShop!=null) {

        }
    }

    private void setYelpApi(char c){
        Log.i(TAG, "setYelpApi: inside");
        YelpAPIFactory yelpAPIFactory = new YelpAPIFactory(
                getString(R.string.YELP_CONSUMER_KEY), getString(R.string.YELP_CONSUMER_SECRET),
                getString(R.string.YELP_TOKEN_KEY),getString(R.string.YELP_TOKEN_SECRET));
        YelpAPI yelpAPI = yelpAPIFactory.createAPI();
        Map<String, String> params = new HashMap<>();
        params.put("category_filter","bubbletea");
//        params.put("term", "Boba");
        params.put("limit","20");
        if (c == 'r') params.put("sort","2");
        else params.put("sort","1");
        if (c == '$') params.put("deals_filter","true");
        CoordinateOptions coordinate = CoordinateOptions.builder()
                .latitude(latitude)
                .longitude(longitude).build();
        Call<SearchResponse> call = yelpAPI.search(coordinate, params);
        call.enqueue(new Callback<SearchResponse>() {
            @Override
            public void onResponse(Call<SearchResponse> call, Response<SearchResponse> response) {
                ArrayList<Business> responseList = response.body().businesses();
                teaShopList.clear();
                teaShopList.addAll(responseList);
                Log.d(TAG, "onResponse: Lat & Long" + latitude + longitude);
                for (Business teaShop : teaShopList) {
                    Log.i(TAG, "onResponse: " + teaShop.name());
//                    Log.i(TAG, "onResponse: " + teaShop.deals().get(0).title());
                }
                teaShopAdapter.notifyDataSetChanged();
                //setTeaShopRV();
                //teaShopAdapter.notifyItemRangeInserted(0,teaShopList.size()-1);
            }

            @Override
            public void onFailure(Call<SearchResponse> call, Throwable t) {
                Log.i(TAG, "onFailure: " + t.getMessage());
                t.printStackTrace();
            }
        });
    }

    private void fillList(){
        for (int i = 1; i <= 25; i++){
            teaList.add(new Tea("Boba " + i));
        }
    }

    private void fillTempList(){
//        for (int i = 0; i<25; i++){
//            teaList.add(new Tea("Boba Tea " + i));
//            firebaseTeas.push().setValue(new Tea("Boba Tea " + i));
//        }
        teaList.add(new Tea("Milk Tea", "http://www.tapiocaexpress.com/wp-content/uploads/2014/06/Milk-Tea.jpg"));
        teaList.add(new Tea("Taro Tea", "http://www.tapiocaexpress.com/wp-content/uploads/2014/06/Taro1.jpg"));
        teaList.add(new Tea("Oolong Milk Tea", "http://www.tapiocaexpress.com/wp-content/uploads/2014/06/Oolong-Green.jpg"));
        teaList.add(new Tea("Almond Milk Tea", "http://www.tapiocaexpress.com/wp-content/uploads/2014/06/Almond.jpg"));
        teaList.add(new Tea("Jasmine Milk Tea", "http://www.tapiocaexpress.com/wp-content/uploads/2014/06/Jasmine.jpg"));
        teaList.add(new Tea("Honey Milk Tea", "http://www.tapiocaexpress.com/wp-content/uploads/2014/06/Honey.jpg"));
    }

    public interface OnBobaFabClickListener {
        void onDistanceFabClick();
    }

    public interface PassClickedTeaListener{
        void passClickedTea(Tea tea);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        teaListener = (PassClickedTeaListener)getActivity();
        bobaFabListener = (OnBobaFabClickListener)getActivity();
    }

    private void setFabListener(){
        bobaFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                teaRV.setAdapter(teaAdapter);

//                bobaFabListener.onDistanceFabClick();
            }
        });
        distanceFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                teaRV.setAdapter(teaShopAdapter);
                if (checkLocationOn()) getLocation();
                setYelpApi('d');
            }
        });
        ratingsFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                teaRV.setAdapter(teaShopAdapter);
                if (checkLocationOn())getLocation();
                setYelpApi('r');
            }
        });
        dealsFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                teaRV.setAdapter(teaShopAdapter);
                if (checkLocationOn()) getLocation();
                setYelpApi('$');
            }
        });
    }

    @Override
    public void onTeaClick(Tea tea) {
        teaListener.passClickedTea(tea);
    }

    @Override
    public void onTeaShopClick(Business teaShop) {
        Intent detailIntent = new Intent(getActivity(), ShopActivity.class);
        detailIntent.putExtra(DETAIL_KEY, teaShop.id());
        startActivity(detailIntent);
    }

    public void setLocation(double[] location){
        this.latitude = location[0];
        this.longitude = location[1];
    }

    private double[] getLocation(){
        double[] location = new double[2];
        if (permissionExists()){
            String locationProvider = LocationManager.NETWORK_PROVIDER;
            Location lastKnownLocation;
            try {
                lastKnownLocation= locationManager.getLastKnownLocation(locationProvider);
                Log.i(TAG, "getLocation: Lat: " + lastKnownLocation.getLatitude());
                Log.i(TAG, "getLocation: Long: " + lastKnownLocation.getLongitude());
                latitude = lastKnownLocation.getLatitude();
                longitude = lastKnownLocation.getLongitude();
                location[0] = lastKnownLocation.getLatitude();
                location[1] = lastKnownLocation.getLongitude();
            }catch (SecurityException e) {
                Toast.makeText(getContext(), "You need to grant location permission", Toast.LENGTH_SHORT).show();
            }
            return location;
        } else{
            requestUserForPermission();
        }
        return location;
    }

    @TargetApi(23)
    private boolean permissionExists(){
        int currentApiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentApiVersion < Build.VERSION_CODES.M){

            // Permissions are already granted during INSTALL TIME for older OS version
            return true;
        }

        int granted = getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
        if (granted == PackageManager.PERMISSION_GRANTED){
            return true;
        }
        return false;
    }

    @TargetApi(23)
    private void requestUserForPermission(){
        int currentApiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentApiVersion < Build.VERSION_CODES.M){
            // This OS version is lower then Android M, therefore we have old permission model and should not ask for permission
            return;
        }
        String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        requestPermissions(permissions, PERMISSION_REQUEST_CODE);
    }

    private boolean checkLocationOn(){
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            //All location services are disabled
            Toast.makeText(getContext(),"Please Enable Location",Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (permissions.length < 0) {
                    return; // no permissions were returned, nothing to process here
                }
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // contacts permission was granted! Lets now grab contacts or show them!
                    String locationProvider = LocationManager.NETWORK_PROVIDER;
                    Location lastKnownLocation;
                    try {
                        lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
                        Log.i(TAG, "getLocation: Lat: " + lastKnownLocation.getLatitude());
                        Log.i(TAG, "getLocation: Long: " + lastKnownLocation.getLongitude());
                        double[] location = {lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()};

                    } catch (SecurityException e) {
                        Toast.makeText(getContext(), "You need to grant location permission", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // contacts permission was denied, lets warn the user that we need this permission!
                    Toast.makeText(getContext(), "You need to grant location permission", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }
}
