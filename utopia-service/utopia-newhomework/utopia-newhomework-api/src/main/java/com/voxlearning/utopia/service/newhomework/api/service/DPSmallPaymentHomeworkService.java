package com.voxlearning.utopia.service.newhomework.api.service;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkTag;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 这个类曾经是给
 *
 * @author xuesong.zhang
 * @since 2017/5/8
 */
@ServiceVersion(version = "20170524")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries
@CyclopsMonitor("utopia")
public interface DPSmallPaymentHomeworkService extends IPingable {

    /**
     * 用于给CRM布置作业用的接口
     *
     * @param weekTime   所在周任意一天的时间戳，周一到周日
     * @param clazzLevel 年级
     * @param subject    学科
     * @param docIds     需要布置的题的docId
     * @param tag        所属任务表示
     * @return MapMessage
     */
    MapMessage generateSmallPaymentHomework(Long weekTime, Integer clazzLevel, Subject subject, HomeworkTag tag, Set<String> docIds);

    /**
     * 第二版黄金任务
     */
    MapMessage generateSmallPaymentHomeworkNewAurum(Long generateTime,
                                                    Integer clazzLevel,
                                                    Subject subject,
                                                    Integer provinceCode,
                                                    Integer cityCode,
                                                    Integer countyCode,
                                                    Collection<String> docIds);


    /**
     * 第三版黄金任务，后两个参数不是必须的
     */
    MapMessage generateAurumHomework(Long studentId, Long orderPayTime, Subject subject, String productId, String orderId);
}
