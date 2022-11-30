package com.abdul.chatsapp.Activitys;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.abdul.chatsapp.MainActivity;
import com.abdul.chatsapp.databinding.ActivityPhoneBinding;
import com.google.firebase.auth.FirebaseAuth;

public class PhoneActivity extends AppCompatActivity {

    ActivityPhoneBinding binding;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPhoneBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        getSupportActionBar().hide();

        if (auth.getCurrentUser() != null){
            startActivity(new Intent(PhoneActivity.this, MainActivity.class));
            finish();
        }
        binding.numver.requestFocus();
        binding.continewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String number = binding.numver.getText().toString();

                if (!number.trim().isEmpty()){

                        Intent intent = new Intent(PhoneActivity.this,OTPActivity.class);
                        intent.putExtra("num",number);
                        startActivity(intent);

                }else {
                    Toast.makeText(PhoneActivity.this, "Enter Mobile Number", Toast.LENGTH_SHORT).show();
                }


            }
        });
    }
}