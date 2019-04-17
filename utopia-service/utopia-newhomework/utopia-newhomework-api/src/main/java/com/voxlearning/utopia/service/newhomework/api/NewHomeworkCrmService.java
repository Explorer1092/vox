package com.voxlearning.utopia.service.newhomework.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.newhomework.api.mapper.RepairHomeworkDataParam;

import java.util.Collection;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 此接口是专门给CRM用的，查询数据会有一定的延迟，其他线上业务禁止使用
 *
 * @author xuesong.zhang
 * @since 2016/11/8
 */
@ServiceVersion(version = "20190214")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries()
@CyclopsMonitor("utopia")
public interface NewHomeworkCrmService extends IPingable {

    /**
     * 用于延期作业到期时间
     *
     * @param searchStartDate 作业结束时间的起始时间查询条件
     * @param searchEndDate   作业结束时间的结束时间查询条件
     * @param endTime         需要延期到的时间
     * @return MapMessage
     */
    MapMessage changeHomeworkEndTime(Date searchStartDate, Date searchEndDate, Date endTime);

    /**
     * 用于延期作业到期时间
     *
     * @param homeworkId 作业id
     * @param endTime    需要延期到的时间
     * @return MapMessage
     */
    MapMessage changeHomeworkEndTime(String homeworkId, Date endTime);

    MapMessage addHomeworkRewardInParentApp(Long userId, String homeworkId, Long groupId, Integer integralCount, Date expire);

    /**
     * 修复未生成自学错题订正的作业
     *
     * @param homeworkId 作业id
     * @param studentId  学生id
     * @return MapMessage
     */
    MapMessage repairSelfStudyCorrectHomework(String homeworkId, Long studentId);

    /**
     * 替换纸质口算涉黄图片
     */
    boolean repairOcrMentalPractiseImage(String homeworkId,Long userId,String processId);

    /**
     * 替换纸质听写涉黄图片
     */
    boolean repairOcrDictationPracticeImage(String homeworkId, Long userId, String processId);

    MapMessage resumeNewHomework(String homeworkId);

    MapMessage crmResendDubbingSynthetic(Collection<String> ids);

    MapMessage repairHomeworkData(RepairHomeworkDataParam param);

    MapMessage resumeBasicReviewHomework(String packageId);

    boolean addNewHomeworkBlackWhiteList(String businessType, String idType, String blackWhiteId);

    boolean deleteNewHomeworkBlackWhiteList(String id);

}
