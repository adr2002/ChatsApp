package com.abdul.chatsapp;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;

import android.os.Bundle;

import android.view.Menu;
import android.view.MenuItem;

import android.widget.Toast;



import com.abdul.chatsapp.Activitys.SettingActivity;
import com.abdul.chatsapp.Adatpters.UserAdapter;

import com.abdul.chatsapp.Models.Users;
import com.abdul.chatsapp.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    FirebaseDatabase database;
    ArrayList<Users> users;
    UserAdapter adapter;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

//        FirebaseRemoteConfig config = FirebaseRemoteConfig.getInstance();
//
//        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
//                .setMinimumFetchIntervalInSeconds(0)
//                .build();
//        config.setConfigSettingsAsync(configSettings);
//
//        config.fetchAndActivate().addOnSuccessListener(new OnSuccessListener<Boolean>() {
//            @Override
//            public void onSuccess(Boolean aBoolean) {
////                String toolbar = config.getString("ToolBarColor");
////                getSupportActionBar().setBackgroundDrawable(Drawable.createFromPath(toolbar));
//            }
//        });


        FirebaseMessaging.getInstance()
                .getToken()
                .addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String token) {
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("token",token);
                        database.getReference()
                                        .child("Users")
                                                .child(auth.getUid())
                                                        .updateChildren(map);

                    }
                });



//        users.add(new Users("","Abdul","",""));
//        users.add(new Users("","rohan","",""));
//        users.add(new Users("","saifa","",""));
//        users.add(new Users("","rinku","",""));
//        users.add(new Users("","raju","",""));
//        users.add(new Users("","robiul","",""));
//        users.add(new Users("","roini","",""));
//        users.add(new Users("","rohan","",""));
//        users.add(new Users("","mohan","",""));
//        users.add(new Users("","Ramjan","",""));
//        users.add(new Users("","Rohit","",""));
//        users.add(new Users("","Iron Man","",""));

        users = new ArrayList<>();
        adapter = new UserAdapter(this,users);
        binding.recicaler.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.recicaler.setLayoutManager(layoutManager);


        binding.recicaler.showShimmerAdapter();

        database.getReference().child("Users")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        users.clear();
                        for (DataSnapshot snapshot1 : snapshot.getChildren()){
                            Users user = snapshot1.getValue(Users.class);
                            if (!user.getUid().equals(FirebaseAuth.getInstance().getUid()))
                                users.add(user);
                        }
                        binding.recicaler.hideShimmerAdapter();
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    @Override
    protected void onResume() {
        super.onResume();
        String cId = FirebaseAuth.getInstance().getUid();
        database.getReference().child("presence")
                .child(cId)
                .setValue("Online");
    }




    @Override
    protected void onPause() {
        super.onPause();
                String cId = FirebaseAuth.getInstance().getUid();
        database.getReference().child("presence")
                .child(cId)
                .setValue("Offline");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_menu,menu);
        MenuItem item = menu.findItem(R.id.search_menu);

        SearchView searchView = (SearchView) item.getActionView();
        searchView.setMaxWidth(Integer.MAX_VALUE);
        
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.getFilter().filter(query);
                return false;

            }

            @Override
            public boolean onQueryTextChange(String newText) {

                adapter.getFilter().filter(newText);
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }



    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case  R.id.setting_menu:
                startActivity(new Intent(MainActivity.this, SettingActivity.class));
                break;
            case  R.id.logOut_menu:
                Toast.makeText(this, "Log out functionality not available", Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}