package com.example.smsclient;
import static com.example.smsclient.Global.messages;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.os.Bundle;
import android.os.Message;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.widget.Toast;
import android.Manifest;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.checkerframework.checker.units.qual.A;
import org.checkerframework.checker.units.qual.C;
import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Dictionary;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;


public class SmsReceiver extends BroadcastReceiver {
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 9;
//    public List<String> contacts = new ArrayList<>();
    float[][] intoms = new float[1][24];
    Context context;
    public String[] symptoms = {
            "Depression", "Painless lumps", "Loss of appetite", "Swelling in limb",
            "Crackling sound", "Shortness of breath", "Sweats", "Chills", "Chest discomfort",
            "Swelling in extremities", "Sores on hooves", "Lameness", "Difficulty walking",
            "Blisters on gums", "Fatigue", "Swelling in muscle", "Sores on gums",
            "Blisters on hooves", "Swelling in abdomen", "Blisters on mouth", "Swelling in neck",
            "Blisters on tongue", "Sores on mouth", "Sores on tongue"
            };
    String[] keywords = symptoms;

    Messages currentMessage = new Messages();
    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            // Retrieve the SMS message
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                // Extract SMS messages from the intent
                Object[] pdus = (Object[]) bundle.get("pdus");
                if (pdus != null) {
                    for (int i = 0; i < pdus.length; i++) {
                        SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdus[i]);
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                            // If the permission has not been granted, request it
                            Toast.makeText(context, "Permission not granted", Toast.LENGTH_SHORT).show();
//                            ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.SEND_SMS}, MY_PERMISSIONS_REQUEST_SEND_SMS);
                            return;
                        } else {
                            Toast.makeText(context, "Permission granted", Toast.LENGTH_SHORT).show();
                            // If the permission has already been granted, you can send the SMS message
//                            sendSmsMessage(sender, "Hello, cattle farmer.");
                        }
                        Toast.makeText(context, "Message received", Toast.LENGTH_SHORT).show();
                        // Extract sender phone number and message body
                        String sender = sms.getDisplayOriginatingAddress();
                        String message = sms.getMessageBody();
                        if (sender.length()<10) {
                            Toast.makeText(context, "Sender is not a phone number", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Toast.makeText(context, "Saved Convos are "+ messages.size(), Toast.LENGTH_SHORT).show();
                        ArrayList<Messages> temp = messages.stream().filter(x -> x.sender.equals(sender)).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);



//                        Toast.makeText(context, "New conversation", Toast.LENGTH_SHORT).show();
                        currentMessage = new Messages();
                        currentMessage.sender = sender;
//                        currentMessage.stage = Stage.UserStarted;
//                        messages.add(currentMessage);

                        String symtoms = synthesiseSymptoms(message);
                        int symptomsCount = symtoms.split(",").length;
                        if (countOnes(checkStringForKeywords(message, keywords)) == 0) {
                            currentMessage.message = "Hello, cattle farmer. I am your cattle health assistant. " +
                                    "Here is how i work. Send your Symptoms and i will respond with the likelihood " +
                                    "of your animal having foot and mouth or not, here are a lit of symptoms I will recognize:" +
                                    "Depression, Painless lumps, Loss of appetite, Swelling in limb," +
                                    "Crackling sound, Shortness of breath, Sweats, Chills, Chest discomfort," +
                                    "Swelling in extremities, Sores on hooves, Lameness, Difficulty walking," +
                                    "Blisters on gums, Fatigue, Swelling in muscle, Sores on gums," +
                                    "Blisters on hooves, Swelling in abdomen, Blisters on mouth, Swelling in neck," +
                                    "Blisters on tongue, Sores on mouth, Sores on tongue";
                            sendSmsMessage(currentMessage);
//                        currentMessage.stage = Stage.AskedForSymptom;
//                        updateMessages(currentMessage);
                        }
                        currentMessage.message = "I have received your symptoms. Please wait while I analyse your symptoms.";
                        sendSmsMessage(currentMessage);
                        Toast.makeText(context, symtoms, Toast.LENGTH_SHORT).show();
                        String result = predict(checkStringForKeywords(message, keywords));
                        currentMessage.message = result;
                        sendSmsMessage(currentMessage);
                        return;
                    }
                }
            }
        }
    }

    private String synthesiseSymptoms(String message) {


        for (int i = 0; i < symptoms.length; i++) {

            String symptom = symptoms[i];
            symptom = symptom.toLowerCase();
            String temp = symptom.toLowerCase();

//            Pattern pattern = Pattern.compile(symptom);
//            Matcher matcher = pattern.matcher(inputString);
//            if (matcher.find()) {
//                System.out.println(keyword);
//            }
//
            message = message.toLowerCase();
            if (message.contains(symptom)) {
                intoms[0][i] = 1;
            } else {
                intoms[0][i] = 0;
            }
        }
        String symptom = "";
        for (int i = 0; i < intoms.length; i++) {
            if (intoms[0][i] == 1) {
                symptom += symptoms[i] + ", ";
            }
        }
        return symptom;
    }
    

    private void updateMessages(Messages currentMessage) {
//        messages.remove(currentMessage);
        messages.add(currentMessage);
    }

    public void sendSmsMessage(Messages message) {
        Toast.makeText(context, "Sending message", Toast.LENGTH_SHORT).show();
        Toast.makeText(context, message.message, Toast.LENGTH_SHORT).show();
        SmsManager smsManager = SmsManager.getDefault();
        String word = message.message;
        smsManager.sendTextMessage(message.sender, null, word + " .", null, null);

    }

    class Messages {
        public String sender;
        public String message;
        public MessageTypes messageType;
        public Date date;
        public Messages() {

        }
        public Messages(String sender, String message, MessageTypes messageType) {
            this.sender = sender;
            this.message = message;
            this.messageType = messageType;
            this.date = new Date();
        }
    }

    enum MessageTypes {
        Sender,
        Receiver,
    }


    public float[][] checkStringForKeywords(String inputString, String[] keywords) {
        // create an array of 15 integers to store the output
        float[][] output = new float[1][24];
        // convert the input string to lowercase using the toLowerCase() method[^1^][1]
        inputString = inputString.toLowerCase();
        // loop through the keywords array
        for (int i = 0; i < keywords.length; i++) {
            // convert the current keyword to lowercase
            String keyword = keywords[i].toLowerCase();
            // check if the input string contains the keyword using the contains() method
            if (inputString.contains(keyword)) {
                // if yes, set the corresponding element in the output array to 1
                output[0][i] = 1;
            } else {
                // if no, set the corresponding element in the output array to 0
                output[0][i] = 0;
            }
        }
        // return the output array
        return output;
    }

    public String predict(float[][] symptoms){

        String[] diseases = {
                "Foot and Mouth"
                ,"no Foot and Mouth"};

//        int inputSize = 15; // The number of features in your input data
        int outputSize = 2; // The number of classes in your output data
//        float[][] inputTensor = new float[1][inputSize]; // A 2D array to hold the input data
        float[][] outputTensor = new float[1][outputSize]; // A 2D array to hold the output data


        // Load the model file from the assets folder
        try {
            AssetFileDescriptor fileDescriptor = context.getAssets().openFd("disease_model.tflite");


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
            Toast.makeText(context, "The output tensor is: " + Arrays.toString(outputTensor[0]), Toast.LENGTH_SHORT).show();
            System.out.println("The output tensor is: " + Arrays.toString(outputTensor[0]));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        int maxIndex = IntStream.range(0, outputTensor[0].length)
                .reduce((i, j) -> outputTensor[0][i] > outputTensor[0][j] ? i : j)
                .getAsInt();
        String result;

        return "Given your symptom, there is a " + outputTensor[0][maxIndex]*100 + "% chance that you have " + diseases[maxIndex] + ". However, you are advised to visit your local vet " ;

//        return diseases[maxIndex] + " " + outputTensor[0][maxIndex]*100 + "%";
//        return "The output tensor is: " + Arrays.toString(outputTensor[0]);
    }

    static int countOnes(float[][] output) {
        // create a variable to store the count
        int count = 0;
        // loop through the output array
        for (float element : output[0]) {
            // check if the current element is 1
            if ((int)element == 1) {
                // if yes, increment the count by 1
                count++;
            }
        }
        // return the count
        return count;
    }


}
