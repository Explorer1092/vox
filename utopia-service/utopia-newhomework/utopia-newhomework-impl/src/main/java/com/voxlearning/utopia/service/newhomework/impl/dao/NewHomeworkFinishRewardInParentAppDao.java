package com.voxlearning.utopia.service.newhomework.impl.dao;

import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.newhomework.api.entity.NewHomeworkFinishRewardInParentApp;

import javax.inject.Named;
import java.util.*;

/**
 * @author shiwe.liao
 * @since 2016-8-26
 */
@Named
@CacheBean(type = NewHomeworkFinishRewardInParentApp.class, useValueWrapper = true)
@CacheDimension(CacheDimensionDistribution.ID_FIELD)
public class NewHomeworkFinishRewardInParentAppDao extends AlpsStaticMongoDao<NewHomeworkFinishRewardInParentApp, Long> {

    @Override
    protected void calculateCacheDimensions(NewHomeworkFinishRewardInParentApp document, Collection<String> dimensions) {
        dimensions.addAll(Arrays.asList(document.generateCacheDimensions()));
    }

    //新增学豆奖励
    public MapMessage addRewardInteger(Long userId, String homeworkId, Long groupId, Integer integralCount, Date expire) {
        if (userId == null) {
            return MapMessage.errorMessage("userId 不能为空");
        }
        if (StringUtils.isBlank(homeworkId)) {
            return MapMessage.errorMessage("作业Id不能为空");
        }
        if (groupId == null) {
            return MapMessage.errorMessage("groupId不能为空");
        }
        if (integralCount == null || integralCount == 0) {
            return MapMessage.errorMessage("学豆奖励数量不能为空");
        }
        NewHomeworkFinishRewardInParentApp rewardInParentApp = load(userId);
        Set<String> existHomeworkIds = new HashSet<>();
        if (rewardInParentApp != null) {
            if (MapUtils.isNotEmpty(rewardInParentApp.getNotReceivedRewardMap())) {
                existHomeworkIds.addAll(rewardInParentApp.getNotReceivedRewardMap().keySet());
            }
            if (MapUtils.isNotEmpty(rewardInParentApp.getHadReceivedRewardMap())) {
                existHomeworkIds.addAll(rewardInParentApp.getHadReceivedRewardMap().keySet());
            }
            if (MapUtils.isNotEmpty(rewardInParentApp.getTimeoutRewardMap())) {
                existHomeworkIds.addAll(rewardInParentApp.getTimeoutRewardMap().keySet());
            }
        }
        if (existHomeworkIds.contains(homeworkId)) {
            return MapMessage.errorMessage("该用户已经获得过此作业的奖励");
        }
        Criteria criteria = Criteria.where("_id").is(userId);
        Update update = new Update();
        Map<String, NewHomeworkFinishRewardInParentApp.RewardDetail> rewardDetailMap = new HashMap<>();
        if (rewardInParentApp != null && rewardInParentApp.getNotReceivedRewardMap() != null) {
            rewardDetailMap = rewardInParentApp.getNotReceivedRewardMap();
        }
        NewHomeworkFinishRewardInParentApp.RewardDetail detail = new NewHomeworkFinishRewardInParentApp.RewardDetail();
        detail.setRewardCount(integralCount);
        detail.setExpire(expire);
        detail.setGroupId(groupId);
        rewardDetailMap.put(homeworkId, detail);
        update.setOnInsert("_id", userId)
                .setOnInsert("createTime", new Date())
                .setOnInsert("disabled", Boolean.FALSE)
                .set("notReceivedRewardMap", rewardDetailMap)
                .currentDate("updateTime");
        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER);
        NewHomeworkFinishRewardInParentApp modify = executeFindOneAndUpdate(createMongoConnection(), criteria, update, options);
        if (modify == null || !modify.getNotReceivedRewardMap().containsKey(homeworkId)) {
            return MapMessage.errorMessage("增加学豆奖励失败");
        } else {
            getCache().getCacheObjectModifier().modify(NewHomeworkFinishRewardInParentApp.ck_id(userId), getDefaultCacheExpirationInSeconds(), currentValue -> modify);
        }
        return MapMessage.successMessage();
    }

    //这里先来删除可领取的作业id。再去增加学豆。至少不会重复领取。后面增加学豆的步骤失败了再手动补。
    public MapMessage updateBeforeReceivedInteger(Long userId, String homeworkId) {
        if (userId == null) {
            return MapMessage.errorMessage("userId 不能为空");
        }
        if (StringUtils.isBlank(homeworkId)) {
            return MapMessage.errorMessage("作业Id不能为空");
        }
        NewHomeworkFinishRewardInParentApp rewardInParentApp = load(userId);
        if (rewardInParentApp == null) {
            return MapMessage.errorMessage("该学生已没有可领取的奖励");
        }
        if (MapUtils.isNotEmpty(rewardInParentApp.getHadReceivedRewardMap()) && rewardInParentApp.getHadReceivedRewardMap().containsKey(homeworkId)) {
            return MapMessage.errorMessage("该学生已领取过该作业的奖励");
        }
        if (MapUtils.isEmpty(rewardInParentApp.getNotReceivedRewardMap()) || !rewardInParentApp.getNotReceivedRewardMap().containsKey(homeworkId)) {
            return MapMessage.errorMessage("该学生已没有可领取的奖励");
        }
        Criteria criteria = Criteria.where("_id").is(userId);
        Update update = new Update();
        //先把需要领取的数量取出来
        Integer count = rewardInParentApp.getNotReceivedRewardMap().get(homeworkId).getRewardCount();
        update.unset("notReceivedRewardMap." + homeworkId)
                .inc("hadReceivedRewardMap." + homeworkId, count)
                .currentDate("updateTime");
        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER);
        NewHomeworkFinishRewardInParentApp modify = executeFindOneAndUpdate(createMongoConnection(), criteria, update, options);
        if (modify == null || modify.getNotReceivedRewardMap().containsKey(homeworkId) || !modify.getHadReceivedRewardMap().containsKey(homeworkId)) {
            return MapMessage.errorMessage("领取学豆奖励失败");
        } else {
            getCache().getCacheObjectModifier().modify(NewHomeworkFinishRewardInParentApp.ck_id(userId), getDefaultCacheExpirationInSeconds(), currentValue -> modify);
        }
        return MapMessage.successMessage();
    }

    //这里先来删除可领取的作业id。再去增加学豆。至少不会重复领取。后面增加学豆的步骤失败了再手动补。
    public MapMessage updateTimeoutInteger(Long userId, String homeworkId) {
        if (userId == null) {
            return MapMessage.errorMessage("userId 不能为空");
        }
        if (StringUtils.isBlank(homeworkId)) {
            return MapMessage.errorMessage("作业Id不能为空");
        }
        NewHomeworkFinishRewardInParentApp rewardInParentApp = load(userId);
        if (rewardInParentApp == null) {
            return MapMessage.errorMessage("该学生已没有可领取的奖励");
        }
        if (MapUtils.isNotEmpty(rewardInParentApp.getHadReceivedRewardMap()) && rewardInParentApp.getHadReceivedRewardMap().containsKey(homeworkId)) {
            return MapMessage.errorMessage("该学生已领取过该作业的奖励");
        }
        if (MapUtils.isEmpty(rewardInParentApp.getNotReceivedRewardMap()) || !rewardInParentApp.getNotReceivedRewardMap().containsKey(homeworkId)) {
            return MapMessage.errorMessage("该学生已没有可领取的奖励");
        }
        Criteria criteria = Criteria.where("_id").is(userId);
        Update update = new Update();
        //先把需要领取的数量取出来
        Integer count = rewardInParentApp.getNotReceivedRewardMap().get(homeworkId).getRewardCount();
        update.unset("notReceivedRewardMap." + homeworkId)
                .inc("timeoutRewardMap." + homeworkId, count)
                .currentDate("updateTime");
        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER);
        NewHomeworkFinishRewardInParentApp modify = executeFindOneAndUpdate(createMongoConnection(), criteria, update, options);
        if (modify == null || modify.getNotReceivedRewardMap().containsKey(homeworkId) || !modify.getTimeoutRewardMap().containsKey(homeworkId)) {
            return MapMessage.errorMessage("处理过期学豆奖励失败");
        } else {
            getCache().getCacheObjectModifier().modify(NewHomeworkFinishRewardInParentApp.ck_id(userId), getDefaultCacheExpirationInSeconds(), currentValue -> modify);
        }
        return MapMessage.successMessage();
    }

    public MapMessage updateChangeGroupRewardInteger(Long userId, Set<String> timeOutHomeworkIds, Set<String> changeGroupHomeworkIds) {
        if (userId == null) {
            return MapMessage.errorMessage("userId 不能为空");
        }
        if (CollectionUtils.isEmpty(timeOutHomeworkIds) && CollectionUtils.isEmpty(changeGroupHomeworkIds)) {
            return MapMessage.errorMessage("作业Id不能为空");
        }
        NewHomeworkFinishRewardInParentApp rewardInParentApp = load(userId);
        if (rewardInParentApp == null) {
            return MapMessage.errorMessage("该学生已没有可领取的奖励");
        }
        if (MapUtils.isEmpty(rewardInParentApp.getNotReceivedRewardMap())) {
            return MapMessage.errorMessage("该学生已没有可领取的奖励");
        }
        Set<String> allHomeworkIds = new HashSet<>();
        if (CollectionUtils.isNotEmpty(timeOutHomeworkIds)) {
            allHomeworkIds.addAll(timeOutHomeworkIds);
        }
        if (CollectionUtils.isNotEmpty(changeGroupHomeworkIds)) {
            allHomeworkIds.addAll(changeGroupHomeworkIds);
        }
        Criteria criteria = Criteria.where("_id").is(userId);
        Update update = new Update();
        for (String homeworkId : allHomeworkIds) {
            update = update.unset("notReceivedRewardMap." + homeworkId);
        }
        //处理已经过期的奖励
        for (String homeworkId : timeOutHomeworkIds) {
            NewHomeworkFinishRewardInParentApp.RewardDetail detail = rewardInParentApp.getNotReceivedRewardMap().get(homeworkId);
            if (detail == null) {
                logger.error("NewHomeworkFinishRewardInParentAppDao error,userId={},homeworkId={}", userId, homeworkId);
                continue;
            }
            Integer count = detail.getRewardCount();
            update = update.inc("timeoutRewardMap." + homeworkId, count);
        }
        //处理换班需要处理的奖励
        Map<String, NewHomeworkFinishRewardInParentApp.RewardDetail> changeGroupRewardMap = new HashMap<>();
        if (rewardInParentApp.getChangeGroupRewardMap() != null) {
            changeGroupRewardMap = rewardInParentApp.getChangeGroupRewardMap();
        }
        for (String homeworkId : changeGroupHomeworkIds) {
            if (rewardInParentApp.getNotReceivedRewardMap().get(homeworkId) != null)
                changeGroupRewardMap.put(homeworkId, rewardInParentApp.getNotReceivedRewardMap().get(homeworkId));
        }
        update.set("changeGroupRewardMap", changeGroupRewardMap)
                .currentDate("updateTime");
        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER);
        NewHomeworkFinishRewardInParentApp modify = executeFindOneAndUpdate(createMongoConnection(), criteria, update, options);
        if (modify == null) {
            return MapMessage.errorMessage("处理过期学豆奖励失败");
        } else {
            getCache().getCacheObjectModifier().modify(NewHomeworkFinishRewardInParentApp.ck_id(userId), getDefaultCacheExpirationInSeconds(), currentValue -> modify);
        }
        return MapMessage.successMessage();
    }

}
