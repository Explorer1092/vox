package com.voxlearning.utopia.service.parent.homework.impl.template.activity.ocrMAv20190304.dnf;

import com.voxlearning.utopia.service.parent.homework.impl.template.base.Processors;
import com.voxlearning.utopia.service.parent.homework.impl.template.base.AbstractTemplate;
import com.voxlearning.utopia.service.parent.homework.impl.template.base.ActivityContext;

import javax.inject.Named;

/**
 * 纸质口算运营活动
 *
 * @author Wenlong Meng
 * @since Feb 20, 2019
 */
@Named("activity.ocrMAv20190304.dnf.OcrMAv20190304Template")
@Processors({
        CheckProcessor.class,
        DNFOcrMAv20190304UserProcessor.class,
        DNFNotifyMessageProcessor.class,
})
public class DNFOcrMAv20190304Template extends AbstractTemplate<ActivityContext> {

}
