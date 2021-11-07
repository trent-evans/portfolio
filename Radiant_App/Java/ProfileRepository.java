package com.example.knight_radiant_app;

import android.app.Application;
import android.os.AsyncTask;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import java.util.List;

public class ProfileRepository {

    private LiveData<List<User>> allUsers;

    private UserDao userDao;

    ProfileRepository(Application app){
        UserDatabase db = UserDatabase.getDatabase(app);
        userDao = db.userDao();
        allUsers = userDao.getAllUsers();
    }

    public LiveData<List<User>> getData() {
        return allUsers;
    }

    public void insert(User newUser){
        new insertDataAsync(userDao).execute(newUser);
    }

    private static class insertDataAsync extends AsyncTask<User,Void,Void> {
        private UserDao asyncUserDao;

        insertDataAsync(UserDao dao){
            asyncUserDao = dao;
        }

        @Override
        protected Void doInBackground(User... users) {
            asyncUserDao.insert(users[0]);

            return null;
        }
    }

}
