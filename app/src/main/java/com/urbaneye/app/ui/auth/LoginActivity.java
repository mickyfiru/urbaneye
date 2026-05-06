package com.urbaneye.app.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.urbaneye.app.R;
import com.urbaneye.app.ui.main.MainActivity;
import com.urbaneye.app.viewmodels.AuthViewModel;

public class LoginActivity extends AppCompatActivity {
    private AuthViewModel viewModel;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        if (viewModel.getCurrentUser() != null) openMain();
        progress = findViewById(R.id.progress);
        EditText email = findViewById(R.id.emailInput);
        EditText password = findViewById(R.id.passwordInput);
        findViewById(R.id.loginButton).setOnClickListener(v -> viewModel.login(email.getText().toString(), password.getText().toString()).observe(this, resource -> {
            progress.setVisibility(resource.status.name().equals("LOADING") ? View.VISIBLE : View.GONE);
            if (resource.status.name().equals("SUCCESS")) openMain();
            if (resource.status.name().equals("ERROR")) Toast.makeText(this, resource.message, Toast.LENGTH_LONG).show();
        }));
        findViewById(R.id.registerButton).setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void openMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
