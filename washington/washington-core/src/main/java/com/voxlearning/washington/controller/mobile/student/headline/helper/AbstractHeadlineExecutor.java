package com.voxlearning.washington.controller.mobile.student.headline.helper;

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.entity.comment.UserRecordEcho;
import com.voxlearning.utopia.entity.like.RecordLikeInfo;
import com.voxlearning.utopia.service.action.api.support.UserLikeType;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import com.voxlearning.utopia.service.zone.api.constant.ClazzJournalType;
import com.voxlearning.utopia.service.zone.api.entity.ClazzJournal;
import com.voxlearning.utopia.service.zone.client.UserLikeServiceClient;
import com.voxlearning.washington.cache.WashingtonCacheSystem;
import com.voxlearning.washington.mapper.studentheadline.StudentBirthdayHeadlineMapper;
import com.voxlearning.washington.mapper.studentheadline.StudentHeadlineMapper;
import com.voxlearning.washington.mapper.studentheadline.StudentInteractiveHeadline;

import javax.inject.Inject;
import java.util.*;

/**
 * 抽象的执行类
 *
 * @author yuechen.wang
 * @since 2017/11/01
 */
abstract public class AbstractHeadlineExecutor extends SpringContainerSupport {

    @Inject protected MobileStudentClazzHelper mobileStudentClazzHelper;
    @Inject protected UserLoaderClient userLoaderClient;
    @Inject protected WashingtonCacheSystem washingtonCacheSystem;
    @Inject private UserLikeServiceClient userLikeServiceClient;

    private HeadlineMapperCacheManager headlineMapperCacheManager;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        headlineMapperCacheManager = new HeadlineMapperCacheManager(washingtonCacheSystem.CBS.flushable);
    }

    abstract public List<ClazzJournalType> journalTypes();

    /**
     * 默认从缓存读取
     *
     * @param clazzJournal 动态
     * @param context      扩展参数
     */
    public StudentHeadlineMapper toMapper(ClazzJournal clazzJournal, HeadlineMapperContext context) {
        if (clazzJournal == null || clazzJournal.getJournalType() == null || clazzJournal.getId() == null) {
            return null;
        }

        String cacheKey = headlineMapperCacheManager.getCacheKey(clazzJournal.getJournalType().getId(), clazzJournal.getId());

        StudentHeadlineMapper mapper = headlineMapperCacheManager.load(cacheKey);
        if (mapper == null) {
            mapper = generateMapper(clazzJournal, context);
            if (mapper == null) {
                return null;
            }
            mapper.setWmflag(false);
            if (ClazzJournalType.HOMEWORK_HEADLINE == clazzJournal.getJournalType()) {
                // 作业的缓存10分钟
                headlineMapperCacheManager.getCache().set(cacheKey, 10 * 60, mapper);
            } else {
                headlineMapperCacheManager.set(cacheKey, mapper);
            }
        }

        if (!mapper.valid()) {
            return null;
        }

        if (mapper instanceof StudentInteractiveHeadline) {
            $fillEncourageAndComment((StudentInteractiveHeadline) mapper, context.getCurrentUserId());
        }
        return mapper;
    }

    public boolean clearMapper(ClazzJournal clazzJournal) {
        if (clazzJournal == null || clazzJournal.getJournalType() == null || clazzJournal.getId() == null) {
            return false;
        }
        String cacheKey = headlineMapperCacheManager.getCacheKey(clazzJournal.getJournalType().getId(), clazzJournal.getId());
        Boolean delete = headlineMapperCacheManager.getCache().delete(cacheKey);
        return Boolean.TRUE.equals(delete);
    }

    public abstract StudentHeadlineMapper generateMapper(ClazzJournal clazzJournal, HeadlineMapperContext context);

    protected void fillInteractiveMapper(StudentInteractiveHeadline mapper, ClazzJournal clazzJournal, User user, HeadlineMapperContext context) {
        mapper.initDateTime(clazzJournal.getCreateDatetime());
        if (mapper.getType() == null) mapper.setType(clazzJournal.getJournalType().name());
        mapper.setJournalId(clazzJournal.getId());

        mapper.setUserId(user.getId());
        mapper.setUserName(user.fetchRealname());
        mapper.setAvatar(user.fetchImageUrl());
        mapper.setHeadWear(mobileStudentClazzHelper.getHeadWear(user.getId()));
    }

    private void $fillBlessList(StudentBirthdayHeadlineMapper mapper, final Long currentUserId) {
        // 个人不展示祝福按钮
        mapper.setShowBtn(!Objects.equals(mapper.getUserId(), currentUserId));

        RecordLikeInfo likeInfo = userLikeServiceClient.loadRecordLikeInfo(UserLikeType.BIRTHDAY_BLESS_HEADLINE, SafeConverter.toString(mapper.getRelevantUserId()));
        if (likeInfo != null) {
            List<Map<String, Object>> blessedList = new ArrayList<>();
            for (String likerName : likeInfo.getLikerNames()) {
                Map<String, Object> item = new HashMap<>();
                item.put("name", likerName);
                blessedList.add(item);
            }

            mapper.setBlessedList(blessedList);
            mapper.setDisabledBtn(likeInfo.getLikeTime().containsKey(currentUserId));
        } else {
            mapper.setBlessedList(new ArrayList<>());
            mapper.setDisabledBtn(false);
        }
    }

    private void $fillEncourageAndComment(StudentInteractiveHeadline mapper, final Long currentUserId) {

        // 设置删除按钮
        mapper.setDeleteBtn(Objects.equals(mapper.getUserId(), currentUserId));

        String journalId = SafeConverter.toString(mapper.getJournalId());

        // 设置评论点赞列表
        UserRecordEcho echo = userLikeServiceClient.loadLikeRecord(UserLikeType.CLAZZ_JOURNAL, journalId);
        mapper.resetBtnStatusWithEcho(echo, currentUserId);

        RecordLikeInfo likeInfo = userLikeServiceClient.loadRecordLikeInfo(UserLikeType.CLAZZ_JOURNAL, journalId);
        mapper.resetBtnStatusWithLike(likeInfo, currentUserId);

        if (mapper instanceof StudentBirthdayHeadlineMapper) {
            $fillBlessList((StudentBirthdayHeadlineMapper) mapper, currentUserId);
        }
    }

}
