package com.urbaneye.app.viewmodels;

import androidx.lifecycle.LiveData;

import com.google.firebase.Timestamp;
import com.urbaneye.app.domain.models.Alert;
import com.urbaneye.app.domain.models.AlertType;
import com.urbaneye.app.repositories.AlertRepository;
import com.urbaneye.app.utils.Resource;

public class PublishAlertViewModel extends BaseViewModel {
    private final AlertRepository repository = new AlertRepository();

    public LiveData<Resource<String>> publish(AlertType type, String title, String description, double lat, double lng, String userId, int greenMinutes) {
        Timestamp expiresAt = repository.expirationFor(type, greenMinutes);
        Alert alert = new Alert(type, title, description, lat, lng, userId, expiresAt);
        return repository.createAlert(alert);
    }
}
