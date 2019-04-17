package com.voxlearning.utopia.service.ai.impl.persistence;

import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.result.UpdateResult;
import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishUserExtSplit;

import javax.inject.Named;
import java.util.*;


@Named
@CacheBean(type = ChipsEnglishUserExtSplit.class)
public class ChipsEnglishUserExtSplitDao extends AlpsStaticMongoDao<ChipsEnglishUserExtSplit, Long> {

    @Override
    protected void calculateCacheDimensions(ChipsEnglishUserExtSplit document, Collection<String> dimensions) {
        dimensions.add(ChipsEnglishUserExtSplit.ck_id(document.getId()));
    }

    /**
     * 是否显示电子教材
     *
     * @param userIdList
     * @param showDisplay
     */
    public void updateShowDisplay(List<Long> userIdList, boolean showDisplay) {
        Criteria criteria = Criteria.where("_id").in(userIdList);
        Update update = new Update().set("show_play", showDisplay)
                .set("ut", new Date());
        long count = executeUpdateMany(createMongoConnection(), criteria, update);
        if (count > 0) {
            cleanCache(userIdList);
        }
    }

    /**
     * 是否加微信
     *
     * @param userId
     * @param wxAddStatus
     */
    public void updateWxAddStatus(Long userId, boolean wxAddStatus) {
        Criteria criteria = Criteria.where("_id").is(userId);
        Update update = new Update().set("wx_add", wxAddStatus)
                .set("ut", new Date());
        long count = executeUpdateMany(createMongoConnection(), criteria, update);
        if (count > 0) {
            cleanCache(userId);
        }
    }
    /**
     * 是否加企业微信
     */
    public void updateEpWxAddStatus(Long userId, boolean epWxAddStatus) {
        Criteria criteria = Criteria.where("_id").is(userId);
        Update update = new Update().set("ep_wx_add", epWxAddStatus)
                .set("ut", new Date());
        long count = executeUpdateMany(createMongoConnection(), criteria, update);
        if (count > 0) {
            cleanCache(userId);
        }
    }

    public void updateUserStudyNumber(Long userId, int num) {
        Criteria criteria = Criteria.where("_id").is(userId);
        Update update = new Update().inc("sentence_learn", num).set("ut", new Date());
        ChipsEnglishUserExtSplit res = executeFindOneAndUpdate(createMongoConnection(), criteria, update, new FindOneAndUpdateOptions().upsert(true));
        if (res != null) {
            cleanCache(userId);
        }
    }

    /**
     * 更新微信号 和 学习年限
     *
     * @param userId
     * @param wechatNumber 微信号
     * @param duration     学习年限
     */
    public void upsert(Long userId, String wechatNumber, String duration) {
        ChipsEnglishUserExtSplit userExt = new ChipsEnglishUserExtSplit();
        userExt.setId(userId);
        userExt.setWxCode(wechatNumber);
        userExt.setStudyDuration(duration);
        userExt.setUpdateTime(new Date());
        upsert(userExt);
        if (duration == null) {
            Criteria criteria = Criteria.where("_id").is(userId);
            Update update = new Update();
            update.set("study_duration", null);
            UpdateResult updateResult = updateOne(createMongoConnection(), criteria, update);
            long modifiedCount = updateResult.getModifiedCount();
            if (modifiedCount > 0) {
                cleanCache(userId);
            }
        }
    }

    /**
     * 更新微信号 和 学习年限
     *
     * @param userId
     * @param wxCode 微信号
     * @param wxName 微信昵称
     */
    public void updateWx(Long userId, String wxCode, String wxName) {
        Criteria criteria = Criteria.where("_id").is(userId);
        Update update = new Update().set("wx_code", wxCode).set("wx_name", wxName);
        ChipsEnglishUserExtSplit res = executeFindOneAndUpdate(createMongoConnection(), criteria, update, new FindOneAndUpdateOptions().upsert(true));
        if (res != null) {
            cleanCache(userId);
        }

    }


    /**
     * 更新收货信息
     *
     * @param userId
     * @param recipientName
     * @param recipientName
     * @param recipientName
     */
    public void updateRecipient(Long userId, String recipientName, String recipientTel, String recipientAddr) {
        Criteria criteria = Criteria.where("_id").is(userId);
        Update update = new Update().set("recipient_name", recipientName).set("recipient_tel", recipientTel).set("recipient_addr", recipientAddr);
        ChipsEnglishUserExtSplit res = executeFindOneAndUpdate(createMongoConnection(), criteria, update, new FindOneAndUpdateOptions().upsert(true));
        if (res != null) {
            cleanCache(userId);
        }
    }

    public void updateMailAddrAndCourseLevel(Long userId, String recipientName, String recipientTel, String recipientAddr, String courseLevel) {
        Criteria criteria = Criteria.where("_id").is(userId);
        Update update = new Update().set("recipient_name", recipientName).set("recipient_tel", recipientTel).set("recipient_addr", recipientAddr)
                .set("course_level", courseLevel).set("ut", new Date());
        ChipsEnglishUserExtSplit res = executeFindOneAndUpdate(createMongoConnection(), criteria, update, new FindOneAndUpdateOptions().upsert(true));
        if (res != null) {
            cleanCache(userId);
        }
    }

    /**
     * 清空缓存
     *
     * @param userIdList
     */
    private void cleanCache(List<Long> userIdList) {
        if (CollectionUtils.isEmpty(userIdList)) {
            return;
        }
        Set<String> cacheIds = new HashSet<>();
        for (Long userID : userIdList) {
            cacheIds.add(ChipsEnglishUserExtSplit.ck_id(userID));
        }
        getCache().deletes(cacheIds);
    }

    /**
     * 清空缓存
     *
     * @param userId
     */
    private void cleanCache(Long userId) {
        Set<String> cacheIds = new HashSet<>();
        cacheIds.add(ChipsEnglishUserExtSplit.ck_id(userId));
        getCache().deletes(cacheIds);
    }

}
