package com.urbaneye.app.viewmodels;

import androidx.lifecycle.LiveData;

import com.urbaneye.app.domain.models.Alert;
import com.urbaneye.app.repositories.AlertRepository;
import com.urbaneye.app.utils.Resource;

import java.util.List;

public class MainMapViewModel extends BaseViewModel {
    private final AlertRepository alertRepository = new AlertRepository();

    public LiveData<Resource<List<Alert>>> observeActiveAlerts() {
        return alertRepository.observeActiveAlerts();
    }
}
