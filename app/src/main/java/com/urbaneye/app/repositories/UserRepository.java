package com.urbaneye.app.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.urbaneye.app.domain.models.TokenTransaction;
import com.urbaneye.app.domain.models.UserProfile;
import com.urbaneye.app.utils.Resource;

public class UserRepository {
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public LiveData<Resource<UserProfile>> observeUser(String userId) {
        MutableLiveData<Resource<UserProfile>> liveData = new MutableLiveData<>(Resource.loading());
        firestore.collection("users").document(userId).addSnapshotListener((snapshot, error) -> {
            if (error != null) {
                liveData.setValue(Resource.error(error.getMessage()));
                return;
            }
            if (snapshot != null && snapshot.exists()) {
                liveData.setValue(Resource.success(snapshot.toObject(UserProfile.class)));
            } else {
                liveData.setValue(Resource.error("Perfil no encontrado."));
            }
        });
        return liveData;
    }

    public LiveData<Resource<Integer>> addTokens(String userId, int amount, String reason) {
        return updateTokens(userId, amount, reason);
    }

    public LiveData<Resource<Integer>> spendTokens(String userId, int amount, String reason) {
        return updateTokens(userId, -amount, reason);
    }

    private LiveData<Resource<Integer>> updateTokens(String userId, int delta, String reason) {
        MutableLiveData<Resource<Integer>> result = new MutableLiveData<>(Resource.loading());
        DocumentReference userRef = firestore.collection("users").document(userId);
        firestore.runTransaction(transaction -> {
            UserProfile profile = transaction.get(userRef).toObject(UserProfile.class);
            if (profile == null) throw new IllegalStateException("Perfil no encontrado.");
            int newBalance = profile.tokens + delta;
            if (newBalance < 0) throw new IllegalStateException("Tokens insuficientes.");
            transaction.update(userRef, "tokens", newBalance);
            DocumentReference txRef = userRef.collection("tokenTransactions").document();
            transaction.set(txRef, new TokenTransaction(userId, delta, reason));
            return newBalance;
        }).addOnSuccessListener(balance -> result.setValue(Resource.success(balance)))
          .addOnFailureListener(e -> result.setValue(Resource.error(e.getMessage())));
        return result;
    }
}
