package com.voxlearning.utopia.service.newexam.impl.service.internal.student.result;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.newexam.api.context.NewExamResultContext;
import com.voxlearning.utopia.service.newexam.impl.dao.NewExamProcessResultDao;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by tanguohong on 2016/3/10.
 */
@Named
public class ER_CreateNewExamProcessResult extends SpringContainerSupport implements NewExamResultTask {
    @Inject private NewExamProcessResultDao newExamProcessResultDao;
    @Override
    public void execute(NewExamResultContext context) {
        String processId = newExamProcessResultDao.insert(context.getCurrentProcessResult());
        if (StringUtils.isBlank(processId)) {
            logger.error("Cannot persist newExamProcessResult {}", JsonUtils.toJson(context.getCurrentProcessResult()));
            context.errorResponse();
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_SAVE_HOMEWORK_PROCESS_RESULT);
        }

    }
}
