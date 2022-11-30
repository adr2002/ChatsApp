package com.abdul.chatsapp.Activitys;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import com.abdul.chatsapp.Adatpters.MessagesAdapter;
import com.abdul.chatsapp.Models.Message;
import com.abdul.chatsapp.R;
import com.abdul.chatsapp.databinding.ActivityChatBinding;
import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    ActivityChatBinding binding;
    MessagesAdapter messagesAdapter;
    ArrayList<Message> messages;
    String senderRoom , receiverRoom;
    FirebaseDatabase database;
    FirebaseStorage storage;
    ProgressDialog dialog;
    String receiverUid;
    String senderUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        setSupportActionBar(binding.toolbar);

        dialog = new ProgressDialog(this);
        dialog.setMessage("Uploading image");
        dialog.setCancelable(false);

        String name = getIntent().getStringExtra("name");
        String profile = getIntent().getStringExtra("profile");
        String token = getIntent().getStringExtra("token");

        Toast.makeText(this, "", Toast.LENGTH_SHORT).show();

        binding.userNaemToolbar.setText(name);

        binding.cameraIc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ChatActivity.this, "Camera is not available", Toast.LENGTH_SHORT).show();
            }
        });

        Glide.with(this)
                .load(profile)
                .placeholder(R.drawable.user)
                .into(binding.userImageToolbar);

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        receiverUid = getIntent().getStringExtra("uid");
        senderUid = FirebaseAuth.getInstance().getUid();

        database.getReference().child("presence")
                .child(receiverUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            String status = snapshot.getValue(String.class);
                           if (status.equals("Online")){
                               binding.statuType.setText(status);
                           }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        senderRoom = senderUid + receiverUid;
        receiverRoom = receiverUid + senderUid;

        messages = new ArrayList<>();
        messagesAdapter = new MessagesAdapter(this,messages,senderRoom,receiverRoom);
        binding.magRecicale.setAdapter(messagesAdapter);





        database.getReference()
                        .child("chats")
                                .child(senderRoom)
                                        .child("messages")
                                                .addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        messages.clear();
                                                        for (DataSnapshot snapshot1: snapshot.getChildren()){
                                                            Message message = snapshot1.getValue(Message.class);
                                                            message.setMessageId(snapshot1.getKey());
                                                            messages.add(message);
                                                        }
                                                        messagesAdapter.notifyDataSetChanged();
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                    }
                                                });
        binding.sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.msgBox.getText().toString().isEmpty()) {
                    binding.msgBox.setError("Enter a message");
                    binding.msgBox.setFocusable(true);
                } else {
                    String msg = binding.msgBox.getText().toString();

                    Date currentDate = new Date();

                    Message message = new Message(msg, senderUid, currentDate.getTime());
                    binding.msgBox.setText("");

                    String randomKey = database.getReference().push().getKey();

                    HashMap<String, Object> lstMsgObj = new HashMap<>();
                    lstMsgObj.put("lastMsg", message.getMessage());
                    lstMsgObj.put("lastMsgTime", currentDate.getTime());

                    database.getReference()
                            .child("chats")
                            .child(senderRoom)
                            .updateChildren(lstMsgObj);

                    database.getReference()
                            .child("chats")
                            .child(receiverRoom)
                            .updateChildren(lstMsgObj);

                    database.getReference()
                            .child("chats")
                            .child(senderRoom)
                            .child("messages")
                            .child(randomKey)
                            .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {

                                    database.getReference()
                                            .child("chats")
                                            .child(receiverRoom)
                                            .child("messages")
                                            .child(randomKey)
                                            .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {

                                                    sendNotification(name, message.getMessage(), token);
                                                }
                                            });


                                }
                            });
                }
            }
        });

        binding.imageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,25);
            }
        });
//        getSupportActionBar().setTitle(name);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Handler handler = new Handler();
        binding.msgBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                database.getReference().child("presence").child(senderUid).setValue("typing...");
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(userStopTyping,1000);
            }
            Runnable userStopTyping = new Runnable() {
                @Override
                public void run() {
                    database.getReference().child("presence").child(senderUid).setValue("Online");
                }
            };
        });
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    void sendNotification(String name,String msg, String token){
        try {
            RequestQueue queue = Volley.newRequestQueue(this);

            String url = "https://fcm.googleapis.com/fcm/send";

            JSONObject data = new JSONObject();
            data.put("title", name);
            data.put("body", msg);
            JSONObject notificationData = new JSONObject();
            notificationData.put("notification", data);
            notificationData.put("to",token);

            JsonObjectRequest request = new JsonObjectRequest(url, notificationData
                    , new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
//                     Toast.makeText(ChatActivity.this, "success", Toast.LENGTH_SHORT).show();

            }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(ChatActivity.this, error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> map = new HashMap<>();
                    String key = "Key=BOSzaHDQrkN6O2bkTxQt8oh3J2YzUx-JCEIAaKOdDtmj06Gr1CCDUtTwqKN7nkBuVNoCejaQtQFr29b_y_DBIo8";
                    map.put("Content-Type", "application/json");
                    map.put("Authorization", key);

                    return map;
                }
            };

            queue.add(request);


        } catch (Exception ex) {

        }

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 25){
            if (data != null){
                if (data.getData() != null){
                    Uri selectImage = data.getData();
                    Calendar calendar = Calendar.getInstance();
                    StorageReference reference = storage.getReference()
                            .child("chats")
                            .child(calendar.getTimeInMillis() + "");
                    dialog.show();

                    reference.putFile(selectImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            dialog.dismiss();
                            if (task.isSuccessful()){
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String filePath = uri.toString();


                                        String msg = binding.msgBox.getText().toString();

                                        Date currentDate = new Date();

                                        Message message = new Message(msg,senderUid,currentDate.getTime());
                                        message.setMessage("photo");
                                        message.setImageUrl(filePath);
                                        binding.msgBox.setText("");

                                        String  randomKey = database.getReference().push().getKey();

                                        HashMap<String, Object> lstMsgObj = new HashMap<>();
                                        lstMsgObj.put("lastMsg",message.getMessage());
                                        lstMsgObj.put("lastMsgTime",currentDate.getTime());

                                        database.getReference()
                                                .child("chats")
                                                .child(senderRoom)
                                                .updateChildren(lstMsgObj);

                                        database.getReference()
                                                .child("chats")
                                                .child(receiverRoom)
                                                .updateChildren(lstMsgObj);

                                        database.getReference()
                                                .child("chats")
                                                .child(senderRoom)
                                                .child("messages")
                                                .child(randomKey)
                                                .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {

                                                        database.getReference()
                                                                .child("chats")
                                                                .child(receiverRoom)
                                                                .child("messages")
                                                                .child(randomKey)
                                                                .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void unused) {

                                                                    }
                                                                });



                                                    }
                                                });


//                                        Toast.makeText(ChatActivity.this, filePath, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    });
                }
            }
        }
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
}