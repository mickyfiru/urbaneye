package com.urbaneye.app.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.urbaneye.app.domain.models.UserProfile;
import com.urbaneye.app.utils.Resource;

public class AuthRepository {
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public LiveData<Resource<FirebaseUser>> login(String email, String password) {
        MutableLiveData<Resource<FirebaseUser>> result = new MutableLiveData<>(Resource.loading());
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> result.setValue(Resource.success(authResult.getUser())))
                .addOnFailureListener(e -> result.setValue(Resource.error(e.getMessage())));
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
                    UserProfile profile = new UserProfile(user.getUid(), username, email);
                    firestore.collection("users").document(user.getUid()).set(profile)
                            .addOnSuccessListener(unused -> result.setValue(Resource.success(user)))
                            .addOnFailureListener(e -> result.setValue(Resource.error(e.getMessage())));
                })
                .addOnFailureListener(e -> result.setValue(Resource.error(e.getMessage())));
        return result;
    }

    public void logout() {
        auth.signOut();
    }
}
