package com.example.joel.brgyambulance;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.joel.brgyambulance.Interaction.Common;
import com.example.joel.brgyambulance.Model.Barangay;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

import dmax.dialog.SpotsDialog;

public class MainActivity extends AppCompatActivity {

    Button signinbtn,registerbtn;
    FirebaseAuth auth;
    FirebaseDatabase db;
    DatabaseReference ambulance;
    RelativeLayout rootLayout;
     public AlertDialog waitingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        ambulance = db.getReference(Common.barangay_ambulance);
        signinbtn= findViewById(R.id.btn_sign_in);
        registerbtn = findViewById(R.id.btn_register);
        rootLayout = findViewById(R.id.rootLayout);

        registerbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRegisterDialog();
            }
        });

        signinbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLoginDialog();
            }
        });

    }

    private void showLoginDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("SIGN IN");
        dialog.setMessage("Please use email to Sign in");

        LayoutInflater inflater = LayoutInflater.from(this);
        View login_layout = inflater.inflate(R.layout.layout_login,null);

        final EditText edittext_email = login_layout.findViewById(R.id.edittext_email);
        final EditText edittext_password = login_layout.findViewById(R.id.edittext_password);

        dialog.setView(login_layout);

        dialog.setPositiveButton("SIGN IN", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        signinbtn.setEnabled(false);
                        if (TextUtils.isEmpty(edittext_email.getText().toString())) {
                            Snackbar.make(rootLayout, "Please enter your email address", Snackbar.LENGTH_SHORT).show();
                            return;

                        }
                        if (TextUtils.isEmpty(edittext_password.getText().toString())) {
                            Snackbar.make(rootLayout, "Please enter your password", Snackbar.LENGTH_SHORT).show();
                            return;
                        }

                        if (edittext_password.getText().toString().length() < 6) {
                            Snackbar.make(rootLayout, "Password too short!", Snackbar.LENGTH_SHORT).show();
                            return;
                        }
                        Toast.makeText(MainActivity.this, "Loading...", Toast.LENGTH_SHORT).show();
                        auth.signInWithEmailAndPassword(edittext_email.getText().toString(),edittext_password.getText().toString())
                                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                    @Override
                                    public void onSuccess(AuthResult authResult) {

                                        startActivity(new Intent(MainActivity.this,MapAmbulance.class));
                                        finish();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Snackbar.make(rootLayout,"Failed"+e.getMessage(),Snackbar.LENGTH_SHORT).show();
                                        signinbtn.setEnabled(true);
                                    }
                                });
                    }
                });
        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                 dialogInterface.dismiss();
            }
        });

        dialog.show();
    }

    private void showRegisterDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("REGISTER");
        dialog.setMessage("Please use email to register");

        LayoutInflater inflater = LayoutInflater.from(this);
        View register_layout = inflater.inflate(R.layout.layout_register,null);

        final EditText edittext_email = register_layout.findViewById(R.id.edittext_email);
        final EditText edittext_password = register_layout.findViewById(R.id.edittext_password);
        final EditText edittext_name = register_layout.findViewById(R.id.edittext_name);
        final EditText edittext_phone = register_layout.findViewById(R.id.edittext_phone);

        dialog.setView(register_layout);

        dialog.setPositiveButton("REGISTER", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                if(TextUtils.isEmpty(edittext_email.getText().toString())){
                    Snackbar.make(rootLayout,"Please enter your email address", Snackbar.LENGTH_SHORT).show();
                    return;

                }
                if(TextUtils.isEmpty(edittext_password.getText().toString())){
                    Snackbar.make(rootLayout,"Please enter your password", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(edittext_name.getText().toString())){
                    Snackbar.make(rootLayout,"Please enter your Name", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(edittext_phone.getText().toString())){
                    Snackbar.make(rootLayout,"Please enter your phone", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if(edittext_password.getText().toString().length() < 6) {
                    Snackbar.make(rootLayout,"Password too short!", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                auth.createUserWithEmailAndPassword(edittext_email.getText().toString()
                        ,edittext_password.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                Barangay brgy = new Barangay();
                                brgy.setEmail(edittext_email.getText().toString());
                                brgy.setPassword(edittext_password.getText().toString());
                                brgy.setName(edittext_name.getText().toString());
                                brgy.setPhone(edittext_phone.getText().toString());

                                ambulance.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .setValue(brgy)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Snackbar.make(rootLayout,"Registered!", Snackbar.LENGTH_SHORT).show();

                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Snackbar.make(rootLayout,"Failed in Registration" +e.getMessage(), Snackbar.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Snackbar.make(rootLayout,"Failed", Snackbar.LENGTH_SHORT).show();
                            }
                        });
            }
        });
        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        dialog.show();

    }
}
