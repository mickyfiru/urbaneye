package com.urbaneye.app.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
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
                    name.setText(resource.data.displayName == null ? resource.data.username : resource.data.displayName);
                    stats.setText("Nivel " + resource.data.level + "  ·  " + resource.data.reputation + "% reputación\n" + Math.max(0, resource.data.tokens) + " tokens  ·  " + resource.data.xp + " XP\n" + resource.data.reportsConfirmed + " confirmadas  ·  " + resource.data.reportsRejected + " rechazadas");
                }
                if (resource.status.name().equals("ERROR")) showSnack(resource.message);
            });
        }
        adManager.loadRewardedAd(this);
        findViewById(R.id.rewardButton).setOnClickListener(v -> adManager.showRewardedAd(this, new RewardedAdManager.RewardCallback() {
            @Override
            public void onRewardEarned() {
                if (userId != null) profileViewModel.rewardAdTokens(userId).observe(ProfileActivity.this, resource -> {
                    if (resource.status.name().equals("SUCCESS")) showSnack("+20 tokens agregados");
                    if (resource.status.name().equals("ERROR")) showSnack(resource.message);
                });
            }

            @Override
            public void onAdUnavailable(String message) {
                showSnack("No se pudo cargar el anuncio, intenta nuevamente");
            }
        }));
        findViewById(R.id.logoutButton).setOnClickListener(v -> {
            authViewModel.logout();
            startActivity(new Intent(this, LoginActivity.class));
            finishAffinity();
        });
    }

    private void showSnack(String message) {
        Snackbar.make(findViewById(android.R.id.content), message == null ? "Ocurrió un problema." : message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(getColor(R.color.urban_surface_high))
                .setTextColor(getColor(R.color.urban_text))
                .show();
    }
}
