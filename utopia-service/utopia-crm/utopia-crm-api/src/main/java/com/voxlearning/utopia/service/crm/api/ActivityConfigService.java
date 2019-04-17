package com.voxlearning.utopia.service.crm.api;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.NoResponseWait;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.buffer.VersionedBufferData;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.utopia.entity.crm.ActivityConfig;

import java.util.*;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20181215")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
public interface ActivityConfigService {

    int TEACHER_SIGN_UP_ON_LINE_TIME = 1542214800; // 2018/11/15 01:00:00

    /**
     * 发布活动
     * @return
     */
    MapMessage publishActivity(ActivityConfig activityConfig);

    /**
     * 根据申请人查询
     * @param applicants
     * @return
     */
    List<ActivityConfig> loadByApplicants(Collection<Long> applicants,Integer role);

    default List<ActivityConfig> loadByApplicant(Long applicant, Integer role) {
        return loadByApplicants(Collections.singleton(applicant), role);
    }

    /**
     *  分页查询所有配置活动
     * @param status
     * @param page
     * @param pageSize
     * @return
     */
    Page<ActivityConfig> query(Integer status, String type, Integer clazzLevel, String name, Integer applicantRole, int page, int pageSize);

    /**
     * 通过
     * @param id
     */
    boolean agree(String id, String auditor, Set<Subject> subjectSet);

    /**
     * 拒绝
     * @param id
     */
    boolean reject(String id, String auditor, String rejectReason);

    /**
     * 查询配置详情
     * @param id
     * @return
     */
    ActivityConfig load(String id);

    /**
     * 删除活动配置
     * @param id
     * @return
     */
    boolean delete(String id);

    /**
     * 查询审核通过的正在进行中的活动并且未发送通知的活动
     * 活动开始前5分钟左右开始发
     * @return
     */
    List<ActivityConfig> loadAgreeStartingNoNotice();

    /**
     * 修改通知状态
     * @param id
     */
    boolean editNoticeStatus(String id, Boolean noticeStatus);

    /**
     * 查询所有活动
     * @return
     */
    List<ActivityConfig> loadAllActivityConfig();

    /**
     * 更新活动
     * @param activityConfig
     */
    void updateActivityConfig(ActivityConfig activityConfig);

    /**
     * 修改邮箱, 内部使用
     * @param id
     * @param email
     * @return
     */
    MapMessage updateEmail(String id, String email);

    List<ActivityConfig> loadActivityConfigListByTypeAndDate(String activityType, Date startDate);

    MapMessage incrementLatelyPassedActivityVersion();

    VersionedBufferData<List<ActivityConfig>> getLatelyPassedActivityBuffer(Long version);

    @NoResponseWait(dispatchAll = true, ignoreNoProvider = true)
    void resetLatelyPassedActivityConfigBuffer();

    MapMessage updateDisabledStatus(String activityId, Boolean disabled);

    MapMessage updateEndTime(String activityId, String yyyyMMdd);

    List<ActivityConfig> loadAllActivityConfigIncludeIsEnd();

    /**
     * 获取老师尚未报名的活动
     */
    List<ActivityConfig> loadNoSignUpActivity(Long teacherId);

    /**
     * 查询老师的报名状态
     */
    Boolean loadSignUpStatus(Long teacherId, String activityId);

    /**
     * 老师报名参与活动
     */
    MapMessage signUpActivity(Long teacherId, String activityId);

    /**
     * 取消报名
     */
    MapMessage cancelSignUpActivity(Long teacherId, String activityId);

    /**
     * 重置老师首页报名弹屏
     */
    MapMessage resetTeacherIndexPop(Long teacherId);

    MapMessage publishActivityByDev(String startTime, String endTime, String model, String level,
                                    Integer limitTime, Integer limitAmount, Integer playLimit);
}
