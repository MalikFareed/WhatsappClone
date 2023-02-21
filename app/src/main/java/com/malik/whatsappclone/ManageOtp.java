package com.malik.whatsappclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ManageOtp extends AppCompatActivity {

    EditText t2;
    Button b2;
    String phNumber;
    String otpId;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manageotp);

        phNumber = getIntent().getStringExtra("mobile").toString();
        t2 = (EditText) findViewById(R.id.t2);
        b2 = (Button) findViewById(R.id.b2);

        mAuth = FirebaseAuth.getInstance();
        InitiateOTP();

        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(t2.getText().toString().isEmpty()){
                    Toast.makeText(getApplicationContext(), "Blank field!", Toast.LENGTH_SHORT).show();
                }
                else if(t2.getText().toString().length() != 6){
                    Toast.makeText(getApplicationContext(), "Invalid OTP!", Toast.LENGTH_SHORT).show();
                }
                else{
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(otpId, t2.getText().toString());
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });
    }

    private void InitiateOTP() {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                ManageOtp.this,               // Activity (for callback binding)
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks()
                {
                    @Override
                    public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        otpId = s;
                    }

                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                        signInWithPhoneAuthCredential(phoneAuthCredential);
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();

                    }
                });        // OnVerificationStateChangedCallbacks

    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            if(user != null){
                                final DatabaseReference mUserDB = FirebaseDatabase.getInstance().getReference().child("user").child(user.getUid());
                                mUserDB.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (!dataSnapshot.exists()){
                                            Map<String,Object> userMap = new HashMap<>();
                                            userMap.put("phone", user.getPhoneNumber());
                                            userMap.put("name", user.getPhoneNumber());
                                            mUserDB.updateChildren(userMap);
                                        }
                                        userLoggedIn();
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                                });
                            }
                            //
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "Sign-in Failed", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void userLoggedIn(){
        //FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
       // if (user != null){}
            startActivity(new Intent(getApplicationContext(), MainPageActivity.class));
            finish();
            return;

    }
}