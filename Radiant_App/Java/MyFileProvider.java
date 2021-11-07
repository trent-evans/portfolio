package com.example.knight_radiant_app;

import androidx.core.content.FileProvider;

public class MyFileProvider extends FileProvider {}

    /**
     * I added this custom class because I was getting an exception thrown due to exposing
     * a `file://` URI to "another app" (I think all of the other apps); Android doc's say that
     * they discourage that because "the receiving app may not have access to the shared path",
     * so we changed it from `file://` to `content://`, per the docs listed best practice
     */