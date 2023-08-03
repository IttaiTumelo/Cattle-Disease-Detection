package com.example.smsclient;

import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.smsclient.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;

import android.Manifest;
import android.widget.Toast;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.stream.IntStream;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);


        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        // Check if the SEND_SMS permission has been granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            // If the permission has not been granted, request it
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 1);
        } else {
            // If the permission has already been granted, you can send the SMS message
            Toast.makeText(this, "Permission already granted", Toast.LENGTH_SHORT).show();
        }

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAnchorView(R.id.fab)
                        .setAction("Action", null).show();
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

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public String predict(float[][] symptoms){

        String[] diseases = {
                "Foot and Mouth"
                ,"No Foot and Mouth"};

//        int inputSize = 24; // The number of features in your input data
        int outputSize = 2; // The number of classes in your output data
//        float[][] inputTensor = new float[1][inputSize]; // A 2D array to hold the input data
        float[][] outputTensor = new float[1][outputSize]; // A 2D array to hold the output data


        // Load the model file from the assets folder
        try {
            AssetFileDescriptor fileDescriptor = getAssets().openFd("disease_model.tflite");


//            // Fill the input tensor with some example symptoms
//            inputTensor[0][0] = 1; // fever
//            inputTensor[0][1] = 0; // cough
//            inputTensor[0][2] = 0; // sore throat
//            inputTensor[0][3] = 1; // headache
//            inputTensor[0][4] = 0; // runny nose
//            inputTensor[0][5] = 0; // shortness of breath
//            inputTensor[0][6] = 0; // chest pain
//            inputTensor[0][7] = 0; // nausea
//            inputTensor[0][8] = 0; // vomiting
//            inputTensor[0][9] = 1; // fatigue
//            inputTensor[0][10] = 0; // diarrhea
//            inputTensor[0][11] = 0; // rash
//            inputTensor[0][12] = 0; // muscle pain
//            inputTensor[0][13] = 0; // muscle pain
//            inputTensor[0][14] = 0; // muscle pain
            FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
            FileChannel fileChannel = inputStream.getChannel();
            long startOffset = fileDescriptor.getStartOffset();
            long declaredLength = fileDescriptor.getDeclaredLength();
            MappedByteBuffer modelBuffer;
            // Declare the interpreter variable
            Interpreter interpreter;
            modelBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);

// Initialize the interpreter variable with the model buffer
            interpreter = new Interpreter(modelBuffer);
            // Run the inference and get the output data
            interpreter.run(symptoms, outputTensor);
            Toast.makeText(this, "The output tensor is: " + Arrays.toString(outputTensor[0]), Toast.LENGTH_SHORT).show();
            System.out.println("The output tensor is: " + Arrays.toString(outputTensor[0]));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        int maxIndex = IntStream.range(0, outputTensor[0].length)
                .reduce((i, j) -> outputTensor[0][i] > outputTensor[0][j] ? i : j)
                .getAsInt();
        return diseases[maxIndex] + " " + outputTensor[0][maxIndex]*100 + "%";
//        return "The output tensor is: " + Arrays.toString(outputTensor[0]);
    }


}