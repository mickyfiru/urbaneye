package com.urbaneye.app.viewmodels;

import androidx.lifecycle.LiveData;

import com.urbaneye.app.domain.models.VoteType;
import com.urbaneye.app.repositories.VoteRepository;
import com.urbaneye.app.utils.Resource;

public class AlertDetailViewModel extends BaseViewModel {
    private final VoteRepository repository = new VoteRepository();

    public LiveData<Resource<Void>> vote(String alertId, String userId, VoteType type) {
        return repository.vote(alertId, userId, type);
    }
}
