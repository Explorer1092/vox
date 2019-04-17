package com.voxlearning.utopia.service.crm.api.loader;

import com.voxlearning.alps.annotation.remote.Idempotent;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.utopia.service.crm.api.bean.ApplyWithProcessResultData;
import com.voxlearning.utopia.service.crm.api.constants.ApplyStatus;
import com.voxlearning.utopia.service.crm.api.constants.ApplyType;
import com.voxlearning.utopia.service.crm.api.constants.SystemPlatformType;
import com.voxlearning.utopia.service.crm.api.entities.AbstractBaseApply;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentModifyDictSchoolApply;

import java.util.List;
import java.util.concurrent.TimeUnit;

import java.util.Date;

import java.util.concurrent.TimeUnit;

/**
 * ApplyManagementLoader
 *
 * @author song.wang
 * @date 2017/1/4
 */
@ServiceVersion(version = "20170214")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 2)
public interface ApplyManagementLoader {

    /**
     * 获取用户的申请列表（包括所有是申请类型）
     * @param platformType 系统平台，标志用户是哪个平台的
     * @param userAccount 用户账号
     * @param status 状态
     * @param includeRevokeData 是否包含可撤销的数据
     * @return 申请列表
     */
    @Idempotent
    Page<AbstractBaseApply> fetchUserApplyList(SystemPlatformType platformType, String userAccount, ApplyStatus status, Boolean includeRevokeData, int pageNo, int pageSize);

    /**
     * 获取申请详情
     * @param applyType 申请类型
     * @param applyId 申请ID
     * @return 申请详情
     */
    @Idempotent
    AbstractBaseApply fetchApplyDetail(ApplyType applyType, Long applyId);

    /**
     * 判断申请是否可以取消
     */
    @Idempotent
    Boolean judgeCanRevokeApply(ApplyType applyType, Long applyId);

    /**
     * 根据applyId获取申请详情及处理情况
     * @param applyType  申请类型
     * @param applyId  申请ID
     * @param withCurrentProcess 是否包含当前待处理数据
     * @return data
     */
    @Idempotent
    ApplyWithProcessResultData fetchApplyWithProcessResultByApplyId(ApplyType applyType, Long applyId, Boolean withCurrentProcess);

    /**
     * 根据workflowId 获取申请详情及处理情况
     * @param applyType 申请类型
     * @param workflowId 工作流ID
     * @param withCurrentProcess 是否包含当前待处理数据
     * @return data
     */
    @Idempotent
    ApplyWithProcessResultData fetchApplyWithProcessResultByWorkflowId(ApplyType applyType, Long workflowId, Boolean withCurrentProcess);


    /**
     * 根据申请类型及时间查询所有状态的申请
     * @param applyType 申请类型
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @param pageNo 页码
     * @param pageSize 页面大小
     * @return 分页数据
     */
    @Idempotent
    Page<AbstractBaseApply> fetchApplyListByType(ApplyType applyType, Date startDate, Date endDate, int pageNo, int pageSize);

    /**
     * 查询指定用户相应类型，相应状态的申请信息， 包括处理结果
     * @param platformType 系统平台，标志用户是哪个平台的
     * @param userAccount 用户账号
     * @param applyType 申请类型     可为空
     * @param status 审核状态     可为空
     * @param startDate 开始时间     可为空
     * @param endDate 结束时间     可为空
     * @param withCurrentProcess 是否包含当前待审核人员信息       可为空
     * @param pageNo 页码
     * @param pageSize 页面大小
     * @return 分页数据
     */
    Page<ApplyWithProcessResultData> fetchUserApplyWithProcessResult(SystemPlatformType platformType, String userAccount, ApplyType applyType, ApplyStatus status, Date startDate, Date endDate, Boolean withCurrentProcess, int pageNo, int pageSize);

    /**
     * 根据时间查询所有状态的字典表学校申请
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @return
     */
    List<AgentModifyDictSchoolApply> fetchDictSchoolApplyListByUpdateDate(Date startDate, Date endDate);
}
