package com.urbaneye.app.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.urbaneye.app.R;
import com.urbaneye.app.ui.main.MainActivity;
import com.urbaneye.app.viewmodels.AuthViewModel;

public class RegisterActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        AuthViewModel viewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        ProgressBar progress = findViewById(R.id.progress);
        EditText username = findViewById(R.id.usernameInput);
        EditText email = findViewById(R.id.emailInput);
        EditText password = findViewById(R.id.passwordInput);
        Button createAccountButton = findViewById(R.id.createAccountButton);
        createAccountButton.setOnClickListener(v -> viewModel.register(username.getText().toString(), email.getText().toString(), password.getText().toString()).observe(this, resource -> {
            boolean loading = resource.status.name().equals("LOADING");
            progress.setVisibility(loading ? View.VISIBLE : View.GONE);
            createAccountButton.setEnabled(!loading);
            if (resource.status.name().equals("SUCCESS")) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
            if (resource.status.name().equals("ERROR")) Toast.makeText(this, resource.message == null ? "No se pudo crear la cuenta." : resource.message, Toast.LENGTH_LONG).show();
        }));
    }
}
