package com.urbaneye.app.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.urbaneye.app.R;
import com.urbaneye.app.ads.RewardedAdManager;
import com.urbaneye.app.ui.auth.LoginActivity;
import com.urbaneye.app.viewmodels.AuthViewModel;
import com.urbaneye.app.viewmodels.ProfileViewModel;

public class ProfileActivity extends AppCompatActivity {
    private final RewardedAdManager adManager = new RewardedAdManager();
    private ProfileViewModel profileViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        AuthViewModel authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        TextView name = findViewById(R.id.nameText);
        TextView stats = findViewById(R.id.statsText);
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId != null) {
            profileViewModel.observeUser(userId).observe(this, resource -> {
                if (resource.status.name().equals("SUCCESS") && resource.data != null) {
                    name.setText(resource.data.username);
                    stats.setText("Tokens: " + resource.data.tokens + "\nXP: " + resource.data.xp + "\nReputación: " + resource.data.reputation + "%");
                }
            });
        }
        adManager.loadRewardedAd(this);
        findViewById(R.id.rewardButton).setOnClickListener(v -> adManager.showRewardedAd(this, new RewardedAdManager.RewardCallback() {
            @Override
            public void onRewardEarned() {
                if (userId != null) profileViewModel.rewardAdTokens(userId).observe(ProfileActivity.this, resource -> {
                    if (resource.status.name().equals("SUCCESS")) Toast.makeText(ProfileActivity.this, "+20 tokens", Toast.LENGTH_SHORT).show();
                    if (resource.status.name().equals("ERROR")) Toast.makeText(ProfileActivity.this, resource.message, Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onAdUnavailable(String message) {
                Toast.makeText(ProfileActivity.this, message, Toast.LENGTH_LONG).show();
            }
        }));
        findViewById(R.id.logoutButton).setOnClickListener(v -> {
            authViewModel.logout();
            startActivity(new Intent(this, LoginActivity.class));
            finishAffinity();
        });
    }
}
