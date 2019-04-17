package com.voxlearning.utopia.service.parent.homework.impl.template;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.parent.homework.api.mapper.HomeworkParam;

/**
 * 做作业接口:提交结果
 *
 * @author Wenlong Meng
 * @since Jan 16, 2019
 */
public interface HomeworkSubmitTemplate {

    /**
     * 上报结果
     *
     * @param param 作业参数
     * @return
     */
    MapMessage submit(HomeworkParam param);

}
