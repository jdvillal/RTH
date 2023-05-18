package com.example.rth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.rth.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'rth' library on application startup.
    static {
        System.loadLibrary("rth");
    }

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Example of a call to a native method
        TextView tv = binding.sampleText;
        tv.setText(stringFromJNI());

        Button btn = binding.button;
        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d("BUTTONS", "User tapped the Supabutton");
                startActivity(new Intent(MainActivity.this, CamPreview.class));
            }
        });

    }

    /**
     * A native method that is implemented by the 'rth' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();



}
