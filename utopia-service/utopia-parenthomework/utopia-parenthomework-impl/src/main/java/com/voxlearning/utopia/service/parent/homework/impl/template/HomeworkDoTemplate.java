package com.voxlearning.utopia.service.parent.homework.impl.template;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.parent.homework.api.mapper.HomeworkParam;

import javax.inject.Named;

/**
 * 做作业接口
 *
 * @author Wenlong Meng
 * @version 20181111
 * @date 2018-11-13
 */
@Named
public interface HomeworkDoTemplate {

    /**
     * 作业首页
     *
     * @param param 作业参数
     * @return
     */
    MapMessage index(HomeworkParam param);

    /**
     * do
     *
     * @param param 作业参数
     * @return
     */
    MapMessage od(HomeworkParam param);


    /**
     * 题目
     *
     * @param param 作业参数
     * @return
     */
    MapMessage questions(HomeworkParam param);

    /**
     * 获取答案
     *
     * @param param 作业参数
     * @return
     */
    MapMessage answers(HomeworkParam param);

    /**
     * 上报结果
     *
     * @param param 作业参数
     * @return
     */
    MapMessage submit(HomeworkParam param);

}
