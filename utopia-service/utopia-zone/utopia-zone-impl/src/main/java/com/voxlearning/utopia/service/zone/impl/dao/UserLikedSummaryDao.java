package com.voxlearning.utopia.service.zone.impl.dao;

import com.voxlearning.alps.dao.aerospike.persistence.StaticAsyncAerospikePersistence;
import com.voxlearning.utopia.entity.like.UserLikedSummary;

import javax.inject.Named;
import java.util.Date;

/**
 *
 * Created by alex on 2017/12/26.
 */
@Named
public class UserLikedSummaryDao extends StaticAsyncAerospikePersistence<UserLikedSummary, String> {

    public UserLikedSummary loadUserLikedSummary(Long userId, Date actionTime) {
        String key = UserLikedSummary.generateId(userId, actionTime);
        return load(key);
    }

    public void liked(Long userId, Long likerId, String recordId, Date actionTime) {
        String key = UserLikedSummary.generateId(userId, actionTime);
        UserLikedSummary summary = load(key);
        if (summary == null) {
            summary = UserLikedSummary.newInstance(userId, actionTime);
        }

        summary.liked(likerId, recordId);

        super.upsert(summary);
    }

}