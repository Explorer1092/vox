package com.voxlearning.utopia.service.zone.impl.dao;

import com.voxlearning.alps.dao.aerospike.persistence.StaticAsyncAerospikePersistence;
import com.voxlearning.utopia.entity.like.RecordLikeInfo;
import com.voxlearning.utopia.service.action.api.support.UserLikeType;

import javax.inject.Named;
import java.util.Date;

/**
 *
 * Created by alex on 2017/12/26.
 */
@Named
public class RecordLikeInfoDao extends StaticAsyncAerospikePersistence<RecordLikeInfo, String> {

    public RecordLikeInfo loadRecordLikeInfo(UserLikeType likeType, String recordId) {
        String key = RecordLikeInfo.generateId(likeType, recordId);
        return load(key);
    }

    public void liked(UserLikeType likeType, String recordId, Long likerId, String likerName, Date actionTime) {
        String key = RecordLikeInfo.generateId(likeType, recordId);
        RecordLikeInfo info = load(key);
        if (info == null) {
            info = RecordLikeInfo.newInstance(likeType, recordId);
        }

        info.liked(likerId, likerName, actionTime);

        super.upsert(info);
    }

}