package com.urbaneye.app.ui.alerts;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.urbaneye.app.R;
import com.urbaneye.app.domain.models.VoteType;
import com.urbaneye.app.viewmodels.AlertDetailViewModel;

public class AlertDetailActivity extends AppCompatActivity {
    public static final String EXTRA_ALERT_ID = "alert_id";
    private AlertDetailViewModel viewModel;
    private String alertId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert_detail);
        viewModel = new ViewModelProvider(this).get(AlertDetailViewModel.class);
        alertId = getIntent().getStringExtra(EXTRA_ALERT_ID);
        findViewById(R.id.confirmButton).setOnClickListener(v -> vote(VoteType.CONFIRM));
        findViewById(R.id.denyButton).setOnClickListener(v -> vote(VoteType.DENY));
        findViewById(R.id.reportButton).setOnClickListener(v -> vote(VoteType.REPORT_ABUSE));
    }

    private void vote(VoteType type) {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null || alertId == null) {
            Toast.makeText(this, "No se puede votar esta alerta.", Toast.LENGTH_SHORT).show();
            return;
        }
        viewModel.vote(alertId, userId, type).observe(this, resource -> {
            if (resource.status.name().equals("SUCCESS")) {
                Toast.makeText(this, "Voto registrado.", Toast.LENGTH_SHORT).show();
                finish();
            }
            if (resource.status.name().equals("ERROR")) Toast.makeText(this, resource.message, Toast.LENGTH_LONG).show();
        });
    }
}
