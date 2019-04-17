package com.voxlearning.utopia.service.afenti.impl.service.processor.review.ranking;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.athena.api.afenti.AfentiTermendBraveService;
import com.voxlearning.athena.api.afenti.entity.TermendBraveAllInfo;
import com.voxlearning.athena.api.afenti.entity.TermendBraveStudentInfo;
import com.voxlearning.utopia.service.afenti.api.context.FetchReviewRankingContext;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Named;

/**
 * @author songtao
 * @since 2017/12/1
 */
@Named
public class FRR_LoadRankingInfo extends SpringContainerSupport implements IAfentiTask<FetchReviewRankingContext> {

    @ImportService(interfaceClass = AfentiTermendBraveService.class)
    private AfentiTermendBraveService afentiTermendBraveService;

    @Override
    public void execute(FetchReviewRankingContext context) {
        try {
            TermendBraveAllInfo info = afentiTermendBraveService.loadRankList(context.getStudent().getId(), context.getStudent().getClazzId(), context.getStudent().getClazz().getSchoolId(), context.getSubject().name());
            if (info == null) {
                return;
            }

            TermendBraveStudentInfo stuInfo = info.getTermendBraveStudentInfo();
            if (stuInfo != null) {
                int totalNum = stuInfo.getTotalNum() != null ? stuInfo.getTotalNum() : 0;
                context.setTotalQuestions(totalNum);
                if (totalNum > 0) {
                    int rightNum = stuInfo.getRightNum() == null ? 0 : stuInfo.getRightNum();
                    int righRate = rightNum * 100 / totalNum;
                    context.setRightRate("" + righRate);
                }
            }

            if (info.getClassRankMap() != null) {
                context.getClassRankingMap().putAll(info.getClassRankMap());
            }

            if (info.getSchoolRankMap() != null) {
                context.getSchoolRankingMap().putAll(info.getSchoolRankMap());
            }
        } catch (Exception e) {
            logger.error("FRR_LoadRankingInfo error. studentId:{},clazzId:{}, schoolId:{}, subject:{}",
                    context.getStudent().getId(), context.getStudent().getClazzId(), context.getStudent().getClazz().getSchoolId(), context.getSubject(), e);
        }
    }
}
