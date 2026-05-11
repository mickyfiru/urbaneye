package com.urbaneye.app.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.urbaneye.app.domain.models.TokenTransaction;
import com.urbaneye.app.domain.models.UserProfile;
import com.urbaneye.app.utils.Resource;

import java.util.HashMap;
import java.util.Map;

public class UserRepository {
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public LiveData<Resource<Void>> ensureCurrentUserProfile() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        MutableLiveData<Resource<Void>> result = new MutableLiveData<>(Resource.loading());
        if (user == null) {
            result.setValue(Resource.error("Debes iniciar sesión."));
            return result;
        }
        ensureUserProfile(user.getUid(), user.getEmail(), user.getDisplayName()).observeForever(resource -> {
            if (resource.status == Resource.Status.SUCCESS) result.setValue(Resource.success(null));
            if (resource.status == Resource.Status.ERROR) result.setValue(Resource.error(resource.message));
        });
        return result;
    }

    public LiveData<Resource<Void>> ensureUserProfile(String userId, String email, String displayName) {
        MutableLiveData<Resource<Void>> result = new MutableLiveData<>(Resource.loading());
        if (userId == null || userId.trim().isEmpty()) {
            result.setValue(Resource.error("Usuario inválido."));
            return result;
        }
        DocumentReference userRef = firestore.collection("users").document(userId);
        userRef.get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        userRef.set(identityDefaults(userId, email, displayName), SetOptions.merge())
                                .addOnSuccessListener(unused -> result.setValue(Resource.success(null)))
                                .addOnFailureListener(e -> result.setValue(Resource.error(permissionMessage(e))));
                    } else {
                        userRef.set(profileDefaults(userId, email, displayName, true), SetOptions.merge())
                                .addOnSuccessListener(unused -> result.setValue(Resource.success(null)))
                                .addOnFailureListener(e -> result.setValue(Resource.error(permissionMessage(e))));
                    }
                })
                .addOnFailureListener(e -> result.setValue(Resource.error(permissionMessage(e))));
        return result;
    }

    public LiveData<Resource<UserProfile>> observeUser(String userId) {
        MutableLiveData<Resource<UserProfile>> liveData = new MutableLiveData<>(Resource.loading());
        firestore.collection("users").document(userId).addSnapshotListener((snapshot, error) -> {
            if (error != null) {
                liveData.setValue(Resource.error(permissionMessage(error)));
                return;
            }
            if (snapshot != null && snapshot.exists()) {
                UserProfile profile = snapshot.toObject(UserProfile.class);
                if (profile == null) profile = new UserProfile(userId, "Usuario UrbanEye", "");
                if (profile.id == null) profile.id = userId;
                if (profile.uid == null) profile.uid = userId;
                if (profile.username == null || profile.username.trim().isEmpty()) profile.username = profile.displayName == null ? "Usuario UrbanEye" : profile.displayName;
                if (profile.displayName == null || profile.displayName.trim().isEmpty()) profile.displayName = profile.username;
                liveData.setValue(Resource.success(profile));
            } else {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                String email = user == null ? "" : user.getEmail();
                String displayName = user == null ? "Usuario UrbanEye" : user.getDisplayName();
                ensureUserProfile(userId, email, displayName).observeForever(resource -> {
                    if (resource.status == Resource.Status.ERROR) liveData.setValue(Resource.error(resource.message));
                });
                liveData.setValue(Resource.success(new UserProfile(userId, displayName, email)));
            }
        });
        return liveData;
    }

    public LiveData<Resource<Integer>> addTokens(String userId, int amount, String reason) {
        MutableLiveData<Resource<Integer>> result = new MutableLiveData<>(Resource.loading());
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String email = user == null ? "" : user.getEmail();
        String displayName = user == null ? "Usuario UrbanEye" : user.getDisplayName();
        ensureUserProfile(userId, email, displayName).observeForever(resource -> {
            if (resource.status == Resource.Status.ERROR) {
                result.setValue(Resource.error(resource.message));
                return;
            }
            if (resource.status != Resource.Status.SUCCESS) return;
            DocumentReference userRef = firestore.collection("users").document(userId);
            firestore.runTransaction(transaction -> {
                Long currentTokens = transaction.get(userRef).getLong("tokens");
                int newBalance = (currentTokens == null ? 0 : currentTokens.intValue()) + amount;
                if (newBalance < 0) throw new IllegalStateException("Tokens insuficientes.");
                if (amount == 20) {
                    transaction.update(userRef, "tokens", FieldValue.increment(20));
                } else {
                    transaction.update(userRef, "tokens", FieldValue.increment(amount));
                }
                DocumentReference txRef = userRef.collection("tokenTransactions").document();
                transaction.set(txRef, new TokenTransaction(userId, amount, reason));
                return newBalance;
            }).addOnSuccessListener(balance -> result.setValue(Resource.success(balance)))
              .addOnFailureListener(e -> result.setValue(Resource.error(permissionMessage(e))));
        });
        return result;
    }

    public LiveData<Resource<Integer>> spendTokens(String userId, int amount, String reason) {
        return addTokens(userId, -amount, reason);
    }

    public int safeTokens(UserProfile profile) {
        return profile == null ? 0 : Math.max(0, profile.tokens);
    }

    private Map<String, Object> identityDefaults(String userId, String email, String displayName) {
        Map<String, Object> data = new HashMap<>();
        data.put("uid", userId);
        data.put("id", userId);
        data.put("email", email == null ? "" : email);
        if (displayName != null && !displayName.trim().isEmpty()) {
            data.put("displayName", displayName.trim());
            data.put("username", displayName.trim());
        }
        return data;
    }

    private Map<String, Object> profileDefaults(String userId, String email, String displayName, boolean includeCreatedAt) {
        String safeDisplayName = displayName == null || displayName.trim().isEmpty() ? "Usuario UrbanEye" : displayName;
        Map<String, Object> data = new HashMap<>();
        data.put("uid", userId);
        data.put("id", userId);
        data.put("email", email == null ? "" : email);
        data.put("displayName", safeDisplayName);
        data.put("username", safeDisplayName);
        data.put("photoUrl", "");
        data.put("tokens", 0);
        data.put("reputation", 0);
        data.put("xp", 0);
        data.put("level", 1);
        data.put("reportsConfirmed", 0);
        data.put("reportsRejected", 0);
        data.put("alertsPublished", 0);
        if (includeCreatedAt) data.put("createdAt", Timestamp.now());
        return data;
    }

    private String permissionMessage(Exception e) {
        String message = e == null ? "Error de Firestore." : e.getMessage();
        if (message != null && message.toLowerCase().contains("permission")) {
            return "No tienes permisos para actualizar Firestore. Revisa las reglas de seguridad.";
        }
        return message == null ? "Error de Firestore." : message;
    }
}
