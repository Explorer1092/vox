package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.check;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.homework.api.constant.CheatingTeacherStatus;
import com.voxlearning.utopia.service.newhomework.api.context.CheckHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.PossibleCheatingTeacher;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.service.NewHomeworkServiceImpl;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;

import static com.voxlearning.utopia.service.homework.api.constant.CheatingTeacherStatus.*;

/**
 * @author Ruib
 * @version 0.1
 * @since 2016/1/27
 */
@Named
public class CH_RecordCheatingTeacher extends SpringContainerSupport implements CheckHomeworkTask {
    @Inject private NewHomeworkLoaderImpl newHomeworkLoader;
    @Inject private NewHomeworkServiceImpl homeworkService;

    @Override
    public void execute(CheckHomeworkContext context) {
        if (!context.isCheated()) return;

        // 判断类型进行记录
        PossibleCheatingTeacher pct = newHomeworkLoader.loadByTeacherId(context.getTeacherId());
        if (pct == null) {
            // 没有作弊记录 直接添加
            pct = PossibleCheatingTeacher.newInstance(context.getTeacherId(), BLACK, "系统反作弊记录", new Date());
            homeworkService.persistPossibleCheatingTeacher(pct);
        } else {
            // 判断老师作弊的状态
            CheatingTeacherStatus status;
            switch (pct.getStatus()) {
                case WHITE:
                case WARN:
                case GOLD_WAIT:
                    status = GOLD_WAIT;
                    break;
                case GOLD_DELETE:
                case AUTH_WAIT:
                    status = AUTH_WAIT;
                    break;
                case AUTH_DELETE:
                    status = AUTH_DELETE;
                    break;
                default:
                    status = BLACK;
            }
            homeworkService.updateLastCheatDateAndStatus(pct.getId(), status);
        }
    }
}
