package com.urbaneye.app.viewmodels;

import androidx.lifecycle.LiveData;

import com.google.firebase.auth.FirebaseUser;
import com.urbaneye.app.repositories.AuthRepository;
import com.urbaneye.app.utils.Resource;

public class AuthViewModel extends BaseViewModel {
    private final AuthRepository repository = new AuthRepository();

    public FirebaseUser getCurrentUser() { return repository.getCurrentUser(); }
    public LiveData<Resource<FirebaseUser>> login(String email, String password) { return repository.login(email, password); }
    public LiveData<Resource<FirebaseUser>> register(String username, String email, String password) { return repository.register(username, email, password); }
    public void logout() { repository.logout(); }
}
