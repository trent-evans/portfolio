package com.example.knight_radiant_app;

import android.graphics.Bitmap;

public class ProfilePic {
    private static Bitmap pic;

    public ProfilePic() {
        pic = null;
    }

    public static Bitmap getPic() {
        return pic;
    }

    public static void setPic(Bitmap bmp) {
        pic = bmp;
    }
}
