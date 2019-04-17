package com.voxlearning.washington.controller.mobile.student.headline.executor;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.zone.api.constant.ClazzJournalType;
import com.voxlearning.utopia.service.zone.api.entity.ClazzJournal;
import com.voxlearning.washington.controller.mobile.student.headline.helper.AbstractHeadlineExecutor;
import com.voxlearning.washington.controller.mobile.student.headline.helper.HeadlineCacheKeyGenerator;
import com.voxlearning.washington.controller.mobile.student.headline.helper.HeadlineMapperContext;
import com.voxlearning.washington.mapper.studentheadline.StudentAchievementHeadlineV1Mapper;
import com.voxlearning.washington.mapper.studentheadline.StudentHeadlineMapper;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 * User: qianxiaozhi
 * Date: 2017/1/11
 * Time: 11:24
 * 个人分享mapper处理
 */
@Named
public class AchievementShareHeadlineExecutor extends AbstractHeadlineExecutor {

    @Inject private RaikouSystem raikouSystem;

    @Override
    public List<ClazzJournalType> journalTypes() {
        return Collections.singletonList(ClazzJournalType.ACHIEVEMENT_SHARE_HEADLINE);
    }

    /**
     * @param clazzJournal 动态
     * @param context      扩展参数
     */
    @Override
    public StudentHeadlineMapper generateMapper(ClazzJournal clazzJournal, HeadlineMapperContext context) {

        if (context == null || context.getCurrentUserId() == null) {
            return null;
        }
        final Long userId = context.getCurrentUserId();
        Long relevantUserId = clazzJournal.getRelevantUserId();
        String achievementJson = clazzJournal.getJournalJson();

        if (StringUtils.isBlank(achievementJson)) {
            return null;
        }
        //不是同一个group的同学 无权查看成就分享
        if (!mobileStudentClazzHelper.isClassmate(clazzJournal.getClazzId(), relevantUserId, context)) {
            return null;
        }

        StudentAchievementHeadlineV1Mapper achievement
                = JsonUtils.fromJson(achievementJson, StudentAchievementHeadlineV1Mapper.class);

        if (achievement == null) {
            return null;
        }
        //设置头像
        User relevantUser = raikouSystem.loadUser(relevantUserId);
        if (relevantUser != null) {
            fillInteractiveMapper(achievement, clazzJournal, relevantUser, context);
            achievement.setHeadIcon(relevantUser.fetchImageUrl());
        }

        //初始化按钮状态
        achievement.setDisabledBtn(false);
        achievement.setShowBtn(true);

        String vid = achievement.getVId();
        //设置鼓励人列表
        if (StringUtils.isNotBlank(vid)) {
            CacheObject<List<Map<String, Object>>> cacheObjectEncourage =
//                    washingtonCacheSystem.CBS.flushable.get(mobileStudentClazzHelper.getAchievementEncouragerKey(vid));
                    washingtonCacheSystem.CBS.flushable.get(HeadlineCacheKeyGenerator.achievementEncouragerKey(vid));
            if (cacheObjectEncourage != null && cacheObjectEncourage.getValue() != null) {
                List<Map<String, Object>> encouragerList = cacheObjectEncourage.getValue();
                for (Map<String, Object> encourager : encouragerList) {
                    if (MapUtils.isNotEmpty(encourager)) {
                        Long encouragerId = (Long) encourager.get("id");
                        encourager.put("headWearImg", mobileStudentClazzHelper.getHeadWear(encouragerId));
                    }
                }
                //按照时间倒序
                encouragerList = encouragerList.stream()
                        .sorted((e1, e2) -> ((Long) e2.get("time")).compareTo((Long) e1.get("time")))
                        .collect(Collectors.toList());
                achievement.setEncouragerList(encouragerList);
            }
        }

        if (CollectionUtils.isNotEmpty(achievement.getEncouragerList())) {
            List<Map<String, Object>> encouraged = achievement.getEncouragerList().stream()
                    .filter(m -> userId.equals(m.get("id")))
                    .collect(Collectors.toList());
            achievement.setDisabledBtn(CollectionUtils.isNotEmpty(encouraged));
        }

        //设置头饰
        achievement.setHeadWearImg(mobileStudentClazzHelper.getHeadWear(achievement.getRelevantUserId()));
        return achievement;
    }

}
