package com.voxlearning.utopia.service.parent.homework.impl.template.submit.single;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.parent.homework.api.mapper.HomeworkParam;
import com.voxlearning.utopia.service.parent.homework.impl.template.base.Processors;
import com.voxlearning.utopia.service.parent.homework.impl.annotation.SubType;
import com.voxlearning.utopia.service.parent.homework.impl.model.HomeworkContext;
import com.voxlearning.utopia.service.parent.homework.impl.template.AbstractProcessorTemplate;
import com.voxlearning.utopia.service.parent.homework.impl.template.HomeworkSubmitTemplate;
import com.voxlearning.utopia.service.parent.homework.impl.template.submit.*;
import com.voxlearning.utopia.service.parent.homework.impl.template.submit.intelligentTeaching.UserProgressStoreProcessor;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Named;

/**
 * 做作业接口:同步习题提交结果
 *
 * @author Wenlong Meng
 * @since Jan 16, 2019
 */
@Named
@SubType({
        ObjectiveConfigType.EXAM
})
@Processors({HomeworkResultSingleInitContextProcessor.class,
        HomeworkResultBuildProcessor.class,
        HomeworkUserGroupProcessor.class,
        HomeworkProcessResultScoreProcessor.class,
        HomeworkProcessResultStoreProcessor.class,
        HomeworkResultStatisticsProcessor.class,
        HomeworkResultStoreProcessor.class,
        UserProgressStoreProcessor.class,
        HomeworkReportMQProcessor.class,
        HomeworkReport2BigDataMQProcessor.class,
        HomeworkRewardProcessor.class,
        HomeworkMapMessageProcessor.class
})
public class HomeworkSingleSubmitTemplateImpl extends AbstractProcessorTemplate implements HomeworkSubmitTemplate {

    //local variables

    /**
     * 上报结果
     *
     * @param param 作业参数
     * @return 结果信息
     */
    public MapMessage submit(HomeworkParam param){
        //初始化
        HomeworkContext hc = new HomeworkContext();
        hc.setHomeworkParam(param);

        //流程处理
        processor.accept(hc);

        return hc.getMapMessage();

    }

}
