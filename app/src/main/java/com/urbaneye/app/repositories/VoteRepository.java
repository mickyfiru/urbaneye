package com.urbaneye.app.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.urbaneye.app.domain.models.AlertStatus;
import com.urbaneye.app.domain.models.Vote;
import com.urbaneye.app.domain.models.VoteType;
import com.urbaneye.app.utils.ReputationRules;
import com.urbaneye.app.utils.Resource;

public class VoteRepository {
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public LiveData<Resource<Void>> vote(String alertId, String userId, VoteType type) {
        MutableLiveData<Resource<Void>> result = new MutableLiveData<>(Resource.loading());
        DocumentReference alertRef = firestore.collection("alerts").document(alertId);
        DocumentReference voteRef = firestore.collection("votes").document(alertId + "_" + userId);

        firestore.runTransaction(transaction -> {
            if (transaction.get(voteRef).exists()) {
                throw new IllegalStateException("Ya votaste esta alerta.");
            }
            Long denies = transaction.get(alertRef).getLong("denies");
            Long reports = transaction.get(alertRef).getLong("reports");
            Vote vote = new Vote(alertId, userId, type);
            vote.id = voteRef.getId();
            transaction.set(voteRef, vote);
            String counter = type == VoteType.CONFIRM ? "confirmations" : type == VoteType.DENY ? "denies" : "reports";
            transaction.update(alertRef, counter, FieldValue.increment(1));
            long newDenies = (denies == null ? 0 : denies) + (type == VoteType.DENY ? 1 : 0);
            long newReports = (reports == null ? 0 : reports) + (type == VoteType.REPORT_ABUSE ? 1 : 0);
            if (newDenies >= ReputationRules.RED_ALERT_DENY_REMOVAL_THRESHOLD || newReports >= ReputationRules.ABUSE_REPORT_THRESHOLD) {
                transaction.update(alertRef, "status", AlertStatus.REMOVED.name());
            }
            return null;
        }).addOnSuccessListener(unused -> result.setValue(Resource.success(null)))
          .addOnFailureListener(e -> result.setValue(Resource.error(e.getMessage())));
        return result;
    }
}
