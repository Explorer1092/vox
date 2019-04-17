package com.voxlearning.washington.controller.mobile.student.headline.executor;

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.zone.api.constant.ClazzJournalType;
import com.voxlearning.utopia.service.zone.api.entity.ClazzJournal;
import com.voxlearning.washington.controller.mobile.student.headline.helper.AbstractHeadlineExecutor;
import com.voxlearning.washington.controller.mobile.student.headline.helper.HeadlineMapperContext;
import com.voxlearning.washington.mapper.studentheadline.ClassCompetitionHeadlineMapper;
import com.voxlearning.washington.mapper.studentheadline.StudentHeadlineMapper;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 班级竞技邀请好友动态处理
 *
 * @author yuechen.wang
 * @since 2017/12/01
 */
@Named
public class ClassCompetitionHeadlineExecutor extends AbstractHeadlineExecutor {

    @Inject private RaikouSystem raikouSystem;

    @Override
    public List<ClazzJournalType> journalTypes() {
        return Arrays.asList(
                ClazzJournalType.NORMAL_CLASS_COMPETITION_INVITE_MATE,
                ClazzJournalType.RECESSIVE_CLASS_COMPETITION_INVITE_MATE
        );
    }

    @Override
    public StudentHeadlineMapper generateMapper(ClazzJournal clazzJournal, HeadlineMapperContext context) {
        User user = raikouSystem.loadUser(clazzJournal.getRelevantUserId());
        if (null == user) return null;

        ClassCompetitionHeadlineMapper mapper = new ClassCompetitionHeadlineMapper();

        fillInteractiveMapper(mapper, clazzJournal, user, context);

        Map<String, Object> extInfo = JsonUtils.fromJson(clazzJournal.getJournalJson());

        mapper.setUrl(SafeConverter.toString(extInfo.get("url")));

        return mapper;
    }
}
