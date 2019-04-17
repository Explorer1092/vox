package com.voxlearning.utopia.service.newhomework.api.service;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.spi.common.IPingable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author xuesong.zhang
 * @since 2017/2/11
 */
@ServiceVersion(version = "20170211")
@ServiceTimeout(timeout = 60, unit = TimeUnit.SECONDS)
@ServiceRetries
@CyclopsMonitor("utopia")
public interface SelfStudyHomeworkGenerateService extends IPingable {


    /**
     * 第一版单词巩固任务调用方，已废弃，用下面那个
     * 生成词汇巩固作业，定时任务专属调用
     *
     * @param clazzGroupId 班组id
     * @param bookToKpMap  一个可怕的map
     */
    @Deprecated
    void selfStudyWordsIncreaseHomework(Long clazzGroupId, Map<String, Map<String, List<String>>> bookToKpMap);


    /**
     * 第二版单词巩固任务，发到kafka里面去折腾
     *
     * @param clazzGroupId 班组id
     * @param bookToKpMap  一个可怕的map
     */
    void sendToKafkaSelfStudyWordsIncreaseHomework(Long clazzGroupId, Map<String, Map<String, List<String>>> bookToKpMap);
}
