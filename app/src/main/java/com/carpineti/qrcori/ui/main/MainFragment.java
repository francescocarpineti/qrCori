package com.carpineti.qrcori.ui.main;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.carpineti.qrcori.R;

import java.util.Objects;

public class MainFragment extends Fragment {
    private onFragmentBtnSelected listener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState){
        // set fragment layout
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        Button scanQrCode = view.findViewById(R.id.scan_qrcode_btn);
        scanQrCode.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                listener.onButtonSelected(v);
            }
        });
        return view;
    }

    public void onAttach(@NonNull Context context){
        super.onAttach(context);
        Log.w("CONTEXT: ", context.toString());
        if (context instanceof onFragmentBtnSelected) {
            listener = (onFragmentBtnSelected) context;
        } else {
            throw new ClassCastException(context.toString() + " must implement listener");
        }
    }

    public interface onFragmentBtnSelected{
        void onButtonSelected(View v);
    }
}
