package com.voxlearning.utopia.service.afenti.impl.service.processor.learningRank;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiRankType;
import com.voxlearning.utopia.service.afenti.api.context.LearningRankContext;
import com.voxlearning.utopia.service.afenti.impl.service.internal.AfentiLearningRankService;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

import static com.voxlearning.utopia.service.afenti.api.constant.AfentiRankType.national;
import static com.voxlearning.utopia.service.afenti.api.constant.AfentiRankType.school;

/**
 * @author peng.zhang.a
 * @since 16-7-27
 */
@Named
public class ALRP_LoadRankList extends SpringContainerSupport implements IAfentiTask<LearningRankContext> {

    @Inject AfentiLearningRankService afentiLearningRankService;

    @Override
    public void execute(LearningRankContext context) {
        Subject subject = context.getSubject();
        Date calculateDate = context.getCalculateDate();
        Long userId = context.getUser().getId();
        Long schoolId = context.getUser() == null || context.getUser().getClazz() == null ? 0 : context.getUser().getClazz().getSchoolId();
        Map<AfentiRankType, List<Map<String, Object>>> rankList = afentiLearningRankService.getRank(subject, calculateDate, schoolId);
        Map<AfentiRankType, Map<Long, Integer>> userRankFlag = afentiLearningRankService.getUserRankFlag(subject, calculateDate, schoolId);
        List<Map<String, Object>> schoolRankList = rankList.getOrDefault(school, new ArrayList<>());
        List<Map<String, Object>> nationalRankList = rankList.getOrDefault(national, new ArrayList<>());
        Integer selfSchoolRank = userRankFlag.getOrDefault(school, Collections.emptyMap()).getOrDefault(userId, 0);
        Integer selfNationalRank = afentiLearningRankService.getUserNationRank(rankList, userRankFlag, userId);

        context.setSelfSchoolRank(selfSchoolRank);
        context.setSelfNationalRank(selfNationalRank);
        context.setSchoolList(schoolRankList);
        context.setNationalList(nationalRankList);
    }
}