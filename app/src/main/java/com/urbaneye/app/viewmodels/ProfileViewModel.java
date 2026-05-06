package com.urbaneye.app.viewmodels;

import androidx.lifecycle.LiveData;

import com.urbaneye.app.domain.models.UserProfile;
import com.urbaneye.app.repositories.UserRepository;
import com.urbaneye.app.utils.Resource;
import com.urbaneye.app.utils.TokenRules;

public class ProfileViewModel extends BaseViewModel {
    private final UserRepository repository = new UserRepository();

    public LiveData<Resource<UserProfile>> observeUser(String userId) { return repository.observeUser(userId); }
    public LiveData<Resource<Integer>> rewardAdTokens(String userId) { return repository.addTokens(userId, TokenRules.REWARDED_AD_TOKENS, "REWARDED_AD"); }
}
