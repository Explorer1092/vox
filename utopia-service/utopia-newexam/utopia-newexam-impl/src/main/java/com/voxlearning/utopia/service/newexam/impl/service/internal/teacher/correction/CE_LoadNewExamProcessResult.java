package com.voxlearning.utopia.service.newexam.impl.service.internal.teacher.correction;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newexam.api.context.CorrectNewExamContext;
import com.voxlearning.utopia.service.newexam.api.entity.NewExamProcessResult;
import com.voxlearning.utopia.service.newexam.impl.dao.NewExamProcessResultDao;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by tanguohong on 2016/3/23.
 */
@Named
public class CE_LoadNewExamProcessResult extends SpringContainerSupport implements CorrectNewExamTask {
    @Inject private NewExamProcessResultDao newExamProcessResultDao;

    @Override
    public void execute(CorrectNewExamContext context) {

        Map<Long, NewExamProcessResult> newExamProcessResultMap = new HashMap<>();
        for (NewExamProcessResult newExamProcessResult : newExamProcessResultDao.loads(context.getNewExamProcessResultIds()).values()) {
            newExamProcessResultMap.put(newExamProcessResult.getUserId(), newExamProcessResult);
        }
        context.setNewExamProcessResultMap(newExamProcessResultMap);
    }
}
