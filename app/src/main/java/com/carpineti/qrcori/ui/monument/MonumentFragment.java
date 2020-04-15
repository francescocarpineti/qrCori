package com.carpineti.qrcori.ui.monument;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.carpineti.qrcori.R;
import com.google.android.gms.common.data.DataBufferSafeParcelable;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class MonumentFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_monument, container, false);
        Objects.requireNonNull(((AppCompatActivity) Objects.requireNonNull(getActivity())).getSupportActionBar()).hide();
        CollapsingToolbarLayout toolbarLayout = root.findViewById(R.id.toolbar_layout);

        // monument name
        String monumentName = getArguments().getString("qrCodeText");

        // set monument image and description
        TextView mText = root.findViewById(R.id.monum_text);
        switch (monumentName) {
            case "Tempio d'Ercole":
                root.findViewById(R.id.app_bar).setBackground(ContextCompat.getDrawable(getContext(), R.drawable.tempiodercole));
                mText.setText(R.string.ercole_text);
                break;
            case "Tempio di Castore e Polluce":
                root.findViewById(R.id.app_bar).setBackground(ContextCompat.getDrawable(getContext(), R.drawable.tempiocastorepolluce));
                mText.setText(R.string.castore_text);
                break;
            case "Chiesa di S.Oliva":
                root.findViewById(R.id.app_bar).setBackground(ContextCompat.getDrawable(getContext(), R.drawable.chiesasoliva));
                mText.setText(R.string.soliva_text);
                break;
        }

        // add monument to user history
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference historyRef = FirebaseDatabase.getInstance().getReference("users_info")
                .child(user.getUid()).child("history");
        historyRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList history = (ArrayList) dataSnapshot.getValue();
                if (history == null){
                    // first time scanning monuments
                    List<String> h = new ArrayList<>();
                    h.add(monumentName);
                    historyRef.setValue(h);
                }
                else {
                    // add monument to history if not present
                    if (!history.contains(monumentName)){
                        history.add(monumentName);
                        historyRef.setValue(history);
                    }
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });


        // set title using scanned QR code value
        toolbarLayout.setTitle(monumentName);

        // set toolbar and navigation logic
        Toolbar collapsingToolbar = root.findViewById(R.id.collapsingToolbar);
        collapsingToolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_action_back));
        collapsingToolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        Objects.requireNonNull(((AppCompatActivity) Objects.requireNonNull(getActivity())).getSupportActionBar()).hide();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        Objects.requireNonNull(((AppCompatActivity) Objects.requireNonNull(getActivity())).getSupportActionBar()).show();
    }
}
