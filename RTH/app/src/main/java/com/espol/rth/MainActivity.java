package com.espol.rth;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.espol.rth.databinding.ActivityMainBinding;

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

        Button btn = findViewById(R.id.mybtn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("LISTENER ========> ", "listener");
                gotoPreviewActivity();
            }
        });

    }

    /**
     * A native method that is implemented by the 'rth' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    public void gotoPreviewActivity(){
        Log.d("click listener====>", "gotoPreview()");
        Intent i = new Intent(this, CamPreviewActivity.class);
        startActivity(i);
    }

}