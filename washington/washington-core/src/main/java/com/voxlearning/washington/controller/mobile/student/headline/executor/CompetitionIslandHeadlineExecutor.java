package com.voxlearning.washington.controller.mobile.student.headline.executor;

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.zone.api.constant.ClazzJournalType;
import com.voxlearning.utopia.service.zone.api.entity.ClazzJournal;
import com.voxlearning.washington.controller.mobile.student.headline.helper.AbstractHeadlineExecutor;
import com.voxlearning.washington.controller.mobile.student.headline.helper.HeadlineMapperContext;
import com.voxlearning.washington.mapper.studentheadline.CompetitionIslandHeadlineMapper;
import com.voxlearning.washington.mapper.studentheadline.StudentHeadlineMapper;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 竞技岛动态处理
 *
 * @author yuechen.wang
 * @since 2017/11/01
 */
@Named
public class CompetitionIslandHeadlineExecutor extends AbstractHeadlineExecutor {

    @Inject private RaikouSystem raikouSystem;

    @Override
    public List<ClazzJournalType> journalTypes() {
        return Arrays.asList(
                ClazzJournalType.COMPETITION_ISLAND_LEVEL_UP,
                ClazzJournalType.COMPETITION_ISLAND_SEASON_CLASS_TOP3
        );
    }

    @Override
    public StudentHeadlineMapper generateMapper(ClazzJournal clazzJournal, HeadlineMapperContext context) {
        User user = raikouSystem.loadUser(clazzJournal.getRelevantUserId());
        if (null == user) return null;

        CompetitionIslandHeadlineMapper mapper = new CompetitionIslandHeadlineMapper();

        fillInteractiveMapper(mapper, clazzJournal, user, context);

        Map<String, Object> extInfo = JsonUtils.fromJson(clazzJournal.getJournalJson());

        mapper.setRank(SafeConverter.toString(extInfo.get("rank")));
        mapper.setCredit(SafeConverter.toInt(extInfo.get("competitionCredit")));
        mapper.setLevelUpImg(SafeConverter.toString(extInfo.get("imgUrl")));

        return mapper;
    }

}
