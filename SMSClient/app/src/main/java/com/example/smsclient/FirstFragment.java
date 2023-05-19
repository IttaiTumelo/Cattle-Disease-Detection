package com.example.smsclient;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.gsm.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.smsclient.databinding.FragmentFirstBinding;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
            }
        });
        binding.textviewTensor.setText("Hello World! from Ittai");
//        String tensor = ((MainActivity) getActivity()).predict();
//        binding.textviewTensor.setText(tensor);
        readSMS();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void readSMS() {
        Cursor cursor = getActivity().getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);
        cursor.moveToFirst();
        binding.textviewFirst.setText(cursor.getString(12));
        sendSMS();
    }

    public void sendSMS() {
        String phoneNumber = "+260971638056";
        String message = "Hello World! from Ittai";
        SmsManager smsManager = SmsManager.getDefault();
//        smsManager.sendTextMessage(phoneNumber, null, message, null, null);
    }




}