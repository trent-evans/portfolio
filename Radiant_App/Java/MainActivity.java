package com.example.knight_radiant_app;

import android.content.Intent;
import android.os.Bundle;

import com.amplifyframework.AmplifyException;
import com.amplifyframework.core.Amplify;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.util.Log;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    EditText userNameEditText, passwordEditText;
    ProfileViewModel model;
    FloatingActionButton newUserButton;
    Button logInButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("Radiant App");


        userNameEditText = findViewById(R.id.userNameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        logInButton = findViewById(R.id.logInButton);
        newUserButton = findViewById(R.id.newUserButton);

        System.out.println("\n\n\n\n\n\n");
        System.out.println(System.getProperty("user.dir"));
        System.out.println("\n\n\n\n\n\n");

        model = ViewModelProviders.of(this).get(ProfileViewModel.class);
        LiveData<List<User>> nullCheck = model.getUserData();
        if(nullCheck == null){
            model.createNewUserEntry("blackthorn","Dalinar","Kholin",
                    68,"Male","Kholinar","Alethkar",193.0,90,
                    true, "password");
            nullCheck = model.getUserData();
        }
        if(nullCheck != null){
            model.getUserData().observe(this, new Observer<List<User>>() {
                @Override
                public void onChanged(List<User> users) {
                    logInButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String userName = userNameEditText.getText().toString();
                            String password = passwordEditText.getText().toString();
                            if(userName.equals("") || password.equals("")){
                                Toast.makeText(getApplicationContext(),"Enter a username and password",Toast.LENGTH_SHORT).show();
                                return;
                            }
                            if(model.verifyUserLogin(userName,password)){
                                Intent goToBase = new Intent(getApplicationContext(),BaseActivity.class);
                                goToBase.putExtra("username",userName);
                                startActivity(goToBase);
                            }else{
                                Toast.makeText(getApplicationContext(),"Login failed\nUser credentials were incorrect\nPlease try again",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            });
        }


//        logInButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String userName = userNameEditText.getText().toString();
//                String password = passwordEditText.getText().toString();
//                if(userName.equals("") || password.equals("")){
//                    Toast.makeText(getApplicationContext(),"Enter a username and password",Toast.LENGTH_SHORT).show();
//                    return;
//                }
//
////                if(model.verifyUserLogin(userName,password)){
//                    Intent goToBase = new Intent(getApplicationContext(),BaseActivity.class);
//                    goToBase.putExtra("username",userName);
//                    startActivity(goToBase);
////                }else{
////                    Toast.makeText(getApplicationContext(),"Login failed\nUser credentials were incorrect\nPlease try again",Toast.LENGTH_SHORT).show();
////                }
//
//
//
//            }
//        });

        newUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToNewUser = new Intent(getApplicationContext(),GenericActivity.class);
                goToNewUser.putExtra("flag","new_user");
                startActivity(goToNewUser);
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}