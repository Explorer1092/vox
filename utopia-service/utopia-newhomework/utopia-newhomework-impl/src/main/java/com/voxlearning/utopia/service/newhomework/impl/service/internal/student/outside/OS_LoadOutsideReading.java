package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.outside;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.newhomework.api.context.outside.OutsideReadingContext;
import com.voxlearning.utopia.service.newhomework.api.entity.outside.OutsideReading;
import com.voxlearning.utopia.service.newhomework.impl.dao.outside.OutsideReadingDao;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author majianxin
 */
@Named
public class OS_LoadOutsideReading extends SpringContainerSupport implements OutsideReadingResultTask {

    @Inject private OutsideReadingDao outsideReadingDao;

    @Override
    public void execute(OutsideReadingContext context) {
        OutsideReading outsideReading = outsideReadingDao.load(context.getReadingId());
        if (outsideReading == null) {
            logger.error("OutsideReading {} not found", context.getReadingId());
            context.errorResponse();
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_NEW_HOMEWORK_TYPE_NOT_EXIST); //不太合适, 新建错误码
            return;
        }

        context.setOutsideReading(outsideReading);
        context.setBookId(outsideReading.findBookId());
        context.setClazzGroupId(outsideReading.getClazzGroupId());
        context.setSubject(outsideReading.getSubject());
    }
}
