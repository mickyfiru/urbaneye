package com.urbaneye.app.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.urbaneye.app.utils.Resource;

public class AuthRepository {
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final UserRepository userRepository = new UserRepository();

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public LiveData<Resource<FirebaseUser>> login(String email, String password) {
        MutableLiveData<Resource<FirebaseUser>> result = new MutableLiveData<>(Resource.loading());
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user == null) {
                        result.setValue(Resource.error("No se pudo iniciar sesión."));
                        return;
                    }
                    ensureProfileThenReturn(user, result);
                })
                .addOnFailureListener(e -> result.setValue(Resource.error(clearMessage(e))));
        return result;
    }

    public LiveData<Resource<FirebaseUser>> register(String username, String email, String password) {
        MutableLiveData<Resource<FirebaseUser>> result = new MutableLiveData<>(Resource.loading());
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user == null) {
                        result.setValue(Resource.error("No se pudo crear el usuario."));
                        return;
                    }
                    String displayName = username == null || username.trim().isEmpty() ? "Usuario UrbanEye" : username.trim();
                    user.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(displayName).build())
                            .addOnCompleteListener(task -> userRepository.ensureUserProfile(user.getUid(), user.getEmail(), displayName).observeForever(resource -> {
                                if (resource.status == Resource.Status.SUCCESS) result.setValue(Resource.success(user));
                                if (resource.status == Resource.Status.ERROR) result.setValue(Resource.error(resource.message));
                            }));
                })
                .addOnFailureListener(e -> result.setValue(Resource.error(clearMessage(e))));
        return result;
    }

    public void logout() {
        auth.signOut();
    }

    private void ensureProfileThenReturn(FirebaseUser user, MutableLiveData<Resource<FirebaseUser>> result) {
        userRepository.ensureUserProfile(user.getUid(), user.getEmail(), user.getDisplayName()).observeForever(resource -> {
            if (resource.status == Resource.Status.SUCCESS) result.setValue(Resource.success(user));
            if (resource.status == Resource.Status.ERROR) result.setValue(Resource.error(resource.message));
        });
    }

    private String clearMessage(Exception e) {
        return e == null || e.getMessage() == null ? "Ocurrió un error de autenticación." : e.getMessage();
    }
}
