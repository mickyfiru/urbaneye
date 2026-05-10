package com.urbaneye.app.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.urbaneye.app.domain.models.Alert;
import com.urbaneye.app.domain.models.AlertType;
import com.urbaneye.app.utils.Resource;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AlertRepository {

    private final FirebaseFirestore firestore;
    private final CollectionReference collectionReference;

    public AlertRepository() {
        firestore = FirebaseFirestore.getInstance();
        collectionReference = firestore.collection("alerts");
    }

    public LiveData<Resource<List<Alert>>> observeActiveAlerts() {
        MutableLiveData<Resource<List<Alert>>> liveData = new MutableLiveData<>();
        liveData.setValue(Resource.loading());

        collectionReference
                .whereEqualTo("status", "ACTIVE")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        liveData.setValue(Resource.error(error.getMessage()));
                        return;
                    }

                    if (value != null) {
                        List<Alert> alerts = new ArrayList<>();
                        for (DocumentSnapshot document : value.getDocuments()) {
                            Alert alert = document.toObject(Alert.class);
                            if (alert != null) {
                                alert.id = document.getId();
                                alerts.add(alert);
                            }
                        }
                        liveData.setValue(Resource.success(alerts));
                    }
                });

        return liveData;
    }

    public LiveData<Resource<String>> createAlert(Alert alert) {
        MutableLiveData<Resource<String>> liveData = new MutableLiveData<>();
        liveData.setValue(Resource.loading());

        collectionReference.add(alert)
                .addOnSuccessListener(documentReference -> {
                    liveData.setValue(Resource.success(documentReference.getId()));
                })
                .addOnFailureListener(e -> {
                    liveData.setValue(Resource.error(e.getMessage()));
                });

        return liveData;
    }

    public Timestamp expirationFor(AlertType type, int greenMinutes) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());

        switch (type) {
            case RED:
                // Persistent alerts - set to a long duration (e.g., 7 days)
                calendar.add(Calendar.DAY_OF_YEAR, 7);
                break;
            case YELLOW:
                calendar.add(Calendar.HOUR, 3);
                break;
            case GREEN:
                calendar.add(Calendar.MINUTE, greenMinutes > 0 ? greenMinutes : 15);
                break;
        }

        return new Timestamp(calendar.getTime());
    }
}
