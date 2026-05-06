package com.urbaneye.app.ui.alerts;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.urbaneye.app.R;
import com.urbaneye.app.domain.models.AlertType;
import com.urbaneye.app.viewmodels.PublishAlertViewModel;

public class PublishAlertActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish_alert);
        PublishAlertViewModel viewModel = new ViewModelProvider(this).get(PublishAlertViewModel.class);
        Spinner typeSpinner = findViewById(R.id.typeSpinner);
        Spinner durationSpinner = findViewById(R.id.durationSpinner);
        typeSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, new String[]{"GREEN", "YELLOW", "RED"}));
        durationSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, new String[]{"15", "30", "60"}));
        EditText title = findViewById(R.id.titleInput);
        EditText description = findViewById(R.id.descriptionInput);
        EditText latitude = findViewById(R.id.latitudeInput);
        EditText longitude = findViewById(R.id.longitudeInput);
        ProgressBar progress = findViewById(R.id.progress);

        findViewById(R.id.publishButton).setOnClickListener(v -> {
            String userId = FirebaseAuth.getInstance().getUid();
            if (userId == null) {
                Toast.makeText(this, "Debes iniciar sesión.", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                AlertType type = AlertType.valueOf(typeSpinner.getSelectedItem().toString());
                int greenMinutes = Integer.parseInt(durationSpinner.getSelectedItem().toString());
                double lat = Double.parseDouble(latitude.getText().toString());
                double lng = Double.parseDouble(longitude.getText().toString());
                viewModel.publish(type, title.getText().toString(), description.getText().toString(), lat, lng, userId, greenMinutes).observe(this, resource -> {
                    progress.setVisibility(resource.status.name().equals("LOADING") ? View.VISIBLE : View.GONE);
                    if (resource.status.name().equals("SUCCESS")) {
                        Toast.makeText(this, "Alerta publicada.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    if (resource.status.name().equals("ERROR")) Toast.makeText(this, resource.message, Toast.LENGTH_LONG).show();
                });
            } catch (NumberFormatException ex) {
                Toast.makeText(this, "Ingresa coordenadas válidas.", Toast.LENGTH_LONG).show();
            }
        });
    }
}
