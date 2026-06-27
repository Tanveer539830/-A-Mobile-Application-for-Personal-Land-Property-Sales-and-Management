package com.example.finalyearproject.fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.finalyearproject.adapter.AdapterChats;
import com.example.finalyearproject.databinding.FragmentChatListBinding;
import com.example.finalyearproject.models.ModelChats;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class ChatListFragment extends Fragment {
    private FragmentChatListBinding binding;
    private static final String TAG="CHAT_TAG";
    private String myUid;
    private Context mContext;
    private ArrayList<ModelChats> chatsArrayList;
    private AdapterChats adapterChats;

    @Override
    public void onAttach(@NonNull Context context) {
        mContext=context;
        super.onAttach(context);

    }

    public ChatListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding=FragmentChatListBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        myUid= firebaseAuth.getUid();

        Log.d(TAG, "onViewCreated: myUid"+myUid);
        loadChats();
        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapterChats != null) {
                    try {
                        String query = s.toString();
                        adapterChats.getFilter().filter(query);
                    } catch (Exception e) {
                        Log.e(TAG, "onTextChanged: ", e);
                    }
                }

            }
        });
    }
    private void loadChats(){
        Log.d(TAG, "loadChats: loading chats....");
        chatsArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Chats");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatsArrayList.clear();
                for(DataSnapshot ds : snapshot.getChildren()) {
                    // CRASH FIX: getValue() ki jagah ds.getKey() use karein kyunki chat path hi key hoti hai
                    String chatKey = ds.getKey();

                    if(chatKey != null && chatKey.contains(myUid)){
                        Log.d(TAG, "onDataChange: Chat found for me");
                        ModelChats modelChats = new ModelChats();
                        modelChats.setChatKey(chatKey);

                        // Dummy Timestamp taake sorting crash na kare
                        modelChats.setTimestamp(System.currentTimeMillis());

                        chatsArrayList.add(modelChats);
                    }
                }
                // Adapter initialize karne se pehle check
                if (mContext != null) {
                    adapterChats = new AdapterChats(mContext, chatsArrayList);
                    binding.chatRv.setAdapter(adapterChats);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "onCancelled: " + error.getMessage());
            }
        });
    }

    private void sortChats(){
        Log.d(TAG, "sortChats: Sorting Chats....");
        new Handler(Looper.getMainLooper())
                .postDelayed(() -> {
                    chatsArrayList.sort((model1, model2) -> Long.compare(model2.getTimestamp(), model1.getTimestamp()));
                    adapterChats.notifyDataSetChanged();
                },1000);
    }

}