package com.voxlearning.washington.controller.mobile.student.headline.executor;

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.zone.api.constant.ClazzJournalType;
import com.voxlearning.utopia.service.zone.api.entity.ClazzJournal;
import com.voxlearning.washington.controller.mobile.student.headline.helper.AbstractHeadlineExecutor;
import com.voxlearning.washington.controller.mobile.student.headline.helper.HeadlineMapperContext;
import com.voxlearning.washington.mapper.studentheadline.ClassBossHeadlineMapper;
import com.voxlearning.washington.mapper.studentheadline.StudentHeadlineMapper;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 班级Boss活动相关动态处理
 *
 * @author yuechen.wang
 * @since 2017/11/01
 */
@Named
public class ClassBossHeadlineExecutor extends AbstractHeadlineExecutor {

    @Inject private RaikouSystem raikouSystem;

    @Override
    public List<ClazzJournalType> journalTypes() {
        return Collections.singletonList(ClazzJournalType.CLASS_BOSS_CHALLENGE_RANK);
    }

    @Override
    public StudentHeadlineMapper generateMapper(ClazzJournal clazzJournal, HeadlineMapperContext context) {
        User user = raikouSystem.loadUser(clazzJournal.getRelevantUserId());
        if (null == user) return null;

        ClassBossHeadlineMapper mapper = new ClassBossHeadlineMapper();

        fillInteractiveMapper(mapper, clazzJournal, user, context);

        Map<String, Object> extInfo = JsonUtils.fromJson(clazzJournal.getJournalJson());

        mapper.setBossName(SafeConverter.toString(extInfo.get("bossName")));
        mapper.setRank(SafeConverter.toInt(extInfo.get("rank")));
        mapper.setBossImg(SafeConverter.toString(extInfo.get("imgUrl")));
        mapper.setRankImg(SafeConverter.toString(extInfo.get("rankImg")));

        return mapper;
    }
}
