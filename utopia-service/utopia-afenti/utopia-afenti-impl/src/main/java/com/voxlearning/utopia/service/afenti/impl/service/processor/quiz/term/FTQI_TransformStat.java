package com.voxlearning.utopia.service.afenti.impl.service.processor.quiz.term;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.context.FetchTermQuizInfoContext;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiQuizStat;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Named;

import static com.voxlearning.alps.annotation.meta.Subject.CHINESE;
import static com.voxlearning.alps.annotation.meta.Subject.ENGLISH;
import static com.voxlearning.alps.annotation.meta.Subject.MATH;

/**
 * @author Ruib
 * @since 2016/12/20
 */
@Named
public class FTQI_TransformStat extends SpringContainerSupport implements IAfentiTask<FetchTermQuizInfoContext> {

    @Override
    public void execute(FetchTermQuizInfoContext context) {
        int ic = 1; // 奖励
        int qc = 6; // 题量
        String status_english = "QUIZ";
        String status_math = "QUIZ";
        String status_chinese = "QUIZ";

        for (AfentiQuizStat stat : context.getStats()) {
            switch (stat.getSubject()) {
                case ENGLISH: {
                    status_english = "REPORT";
                    break;
                }
                case MATH: {
                    status_math = "REPORT";
                    break;
                }
                case CHINESE: {
                    status_chinese = "REPORT";
                    break;
                }
                default:
            }
        }

        context.getResult().put(ENGLISH.name().toLowerCase(), MiscUtils.m("ic", ic, "qc", qc, "status", status_english));
        context.getResult().put(MATH.name().toLowerCase(), MiscUtils.m("ic", ic, "qc", qc, "status", status_math));
        context.getResult().put(CHINESE.name().toLowerCase(), MiscUtils.m("ic", ic, "qc", qc, "status", status_chinese));
    }
}
