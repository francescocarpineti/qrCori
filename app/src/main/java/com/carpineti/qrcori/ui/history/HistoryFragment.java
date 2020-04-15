package com.carpineti.qrcori.ui.history;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.carpineti.qrcori.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class HistoryFragment extends Fragment {

    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_history, container, false);

        recyclerView = root.findViewById(R.id.hist_recycler_view);

        // use a LinearLayout manager
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        // get history before dealing with RecyclerView
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference historyRef = FirebaseDatabase.getInstance().getReference("users_info")
                .child(user.getUid()).child("history");
        historyRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList history = (ArrayList) dataSnapshot.getValue();
                if (history == null){
                    // set text for empty history
                    try {
                        Toast.makeText(getContext(), "Empty history", Toast.LENGTH_LONG).show();
                    } catch (NullPointerException e){
                        // this is needed to avoid errors while HistoryFragment is in background
                    }
                }
                else {
                    // fill RecyclerView properly
                    try {
                        mAdapter = new HistoryRecyclerAdapter(getContext(), history);
                        recyclerView.setAdapter(mAdapter);
                    } catch (NullPointerException e){
                        // this is needed to avoid errors while HistoryFragment is in background
                    }
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        return root;
    }
}
