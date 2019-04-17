package com.voxlearning.washington.controller.mobile.student.headline.executor;

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.zone.api.constant.ClazzJournalType;
import com.voxlearning.utopia.service.zone.api.entity.ClazzJournal;
import com.voxlearning.washington.controller.mobile.student.headline.helper.AbstractHeadlineExecutor;
import com.voxlearning.washington.controller.mobile.student.headline.helper.HeadlineMapperContext;
import com.voxlearning.washington.mapper.studentheadline.StudentHeadlineMapper;
import com.voxlearning.washington.mapper.studentheadline.WonderlandMedalHeadlineMapper;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 成长世界伙伴动态处理
 *
 * @author yuechen.wang
 * @since 2017/11/01
 */
@Named
public class WonderlandMedalHeadlineExecutor extends AbstractHeadlineExecutor {

    @Inject private RaikouSystem raikouSystem;

    @Override
    public List<ClazzJournalType> journalTypes() {
        return Arrays.asList(
                ClazzJournalType.WONDERLAND_NEW_MEDAL,
                ClazzJournalType.WONDERLAND_MEDAL_GRADE
        );
    }

    @Override
    public StudentHeadlineMapper generateMapper(ClazzJournal clazzJournal, HeadlineMapperContext context) {
        User user = raikouSystem.loadUser(clazzJournal.getRelevantUserId());
        if (null == user) return null;

        WonderlandMedalHeadlineMapper mapper = new WonderlandMedalHeadlineMapper();

        fillInteractiveMapper(mapper, clazzJournal, user, context);

        Map<String, Object> extInfo = JsonUtils.fromJson(clazzJournal.getJournalJson());

        mapper.setMedalImg(SafeConverter.toString(extInfo.get("medalImg")));
        mapper.setMedalName(SafeConverter.toString(extInfo.get("medal")));
        mapper.setMedalRank(SafeConverter.toString(extInfo.get("medalRank")));

        if (ClazzJournalType.WONDERLAND_MEDAL_GRADE == clazzJournal.getJournalType()) {
            mapper.setMedalImg(SafeConverter.toString(extInfo.get("imgUrl")));
        }

        return mapper;
    }
}
