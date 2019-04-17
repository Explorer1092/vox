package com.voxlearning.utopia.service.newhomework.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.remote.core.support.ValueWrapperFuture;
import com.voxlearning.utopia.service.newhomework.api.entity.outside.OutsideReading;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkBook;
import com.voxlearning.utopia.service.newhomework.api.mapper.avenger.AvengerHomework;
import com.voxlearning.utopia.service.newhomework.api.service.AsyncAvengerHomeworkService;
import com.voxlearning.utopia.service.newhomework.impl.service.helper.AvengerHomeworkHelper;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author xuesong.zhang
 * @since 2017/7/5
 */
@Named
@ExposeService(interfaceClass = AsyncAvengerHomeworkService.class)
public class AsyncAvengerHomeworkServiceImpl extends SpringContainerSupport implements AsyncAvengerHomeworkService {

    @Inject private AvengerHomeworkHelper avengerHomeworkHelper;

    @Override
    public AlpsFuture<AvengerHomework> informHomeworkToBigData(NewHomework newHomework, NewHomeworkBook newHomeworkBook) {
        AvengerHomework avengerHomework = avengerHomeworkHelper.generateAvengerHomework(newHomework, newHomeworkBook);
        return new ValueWrapperFuture<>(avengerHomework);
    }

    @Override
    public AlpsFuture<AvengerHomework> informBasicReviewHomeworkToBigData(NewHomework newHomework) {
        AvengerHomework avengerHomework = avengerHomeworkHelper.generateAvengerHomeworkForBasicReview(newHomework);
        return new ValueWrapperFuture<>(avengerHomework);
    }

    @Override
    public AlpsFuture<AvengerHomework> informOutsideReadingToBigData(OutsideReading outsideReading) {
        AvengerHomework avengerHomework = avengerHomeworkHelper.generateAvengerHomeworkForOutsideReading(outsideReading);
        return new ValueWrapperFuture<>(avengerHomework);
    }
}
