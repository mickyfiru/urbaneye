package com.urbaneye.app.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.Query;
import com.urbaneye.app.domain.models.Alert;
import com.urbaneye.app.domain.models.AlertStatus;
import com.urbaneye.app.domain.models.AlertType;
import com.urbaneye.app.utils.AntiSpamRules;
import com.urbaneye.app.utils.Resource;
import com.urbaneye.app.utils.TokenRules;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AlertRepository {
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public LiveData<Resource<List<Alert>>> observeActiveAlerts() {
        MutableLiveData<Resource<List<Alert>>> result = new MutableLiveData<>(Resource.loading());
        firestore.collection("alerts")
                .whereEqualTo("status", AlertStatus.ACTIVE.name())
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(250)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        result.setValue(Resource.error(permissionMessage(error)));
                        return;
                    }
                    List<Alert> alerts = new ArrayList<>();
                    if (snapshots != null) {
                        snapshots.forEach(document -> {
                            Alert alert = document.toObject(Alert.class);
                            alert.id = document.getId();
                            if (!isExpired(alert)) alerts.add(alert);
                        });
                    }
                    result.setValue(Resource.success(alerts));
                });
        return result;
    }

    public LiveData<Resource<String>> createAlert(Alert alert) {
        MutableLiveData<Resource<String>> result = new MutableLiveData<>(Resource.loading());
        String userId = alert.createdBy;
        int cost = TokenRules.costFor(alert.type);
        DocumentReference userRef = firestore.collection("users").document(userId);
        DocumentReference alertRef = firestore.collection("alerts").document();
        alert.id = alertRef.getId();

        firestore.runTransaction(transaction -> {
            com.google.firebase.firestore.DocumentSnapshot userSnapshot = transaction.get(userRef);
            Long tokens = userSnapshot.getLong("tokens");
            Timestamp lastAlertAt = userSnapshot.getTimestamp("lastAlertAt");
            int currentTokens = tokens == null ? 0 : tokens.intValue();
            if (currentTokens < cost) {
                throw new IllegalStateException("Necesitas " + cost + " tokens. Ver anuncio para ganar 20");
            }
            if (AntiSpamRules.isCoolingDown(lastAlertAt)) {
                throw new IllegalStateException("Espera unos minutos antes de publicar otra alerta.");
            }
            transaction.update(userRef, "tokens", FieldValue.increment(-cost));
            transaction.update(userRef, "xp", FieldValue.increment(10));
            transaction.update(userRef, "alertsPublished", FieldValue.increment(1));
            transaction.update(userRef, "lastAlertAt", Timestamp.now());
            transaction.set(alertRef, alert);
            return alert.id;
        }).addOnSuccessListener(id -> result.setValue(Resource.success(id)))
          .addOnFailureListener(e -> result.setValue(Resource.error(permissionMessage(e))));
        return result;
    }

    public Timestamp expirationFor(AlertType type, int greenMinutes) {
        if (type == AlertType.RED) return null;
        long minutes = type == AlertType.YELLOW ? TimeUnit.HOURS.toMinutes(3) : greenMinutes;
        return new Timestamp(new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(minutes)));
    }

    private String permissionMessage(Exception e) {
        String message = e == null ? "Error de Firestore." : e.getMessage();
        if (message != null && message.toLowerCase().contains("permission")) {
            return "No tienes permisos para actualizar Firestore. Revisa las reglas de seguridad.";
        }
        return message == null ? "Error de Firestore." : message;
    }

    private boolean isExpired(Alert alert) {
        return alert.expiresAt != null && alert.expiresAt.toDate().before(new Date());
    }
}
