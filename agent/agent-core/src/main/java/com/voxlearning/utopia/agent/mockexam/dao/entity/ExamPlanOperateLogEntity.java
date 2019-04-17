package com.voxlearning.utopia.agent.mockexam.dao.entity;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

import static com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlPrimaryKeyGeneratorType.AUTO_INC;

/**
 * 审核日志
 *
 * @Author: peng.zhang
 * @Date: 2018/8/9 15:33
 */
@Data
@DocumentConnection(configName = "agent")
@DocumentTable(table = "AGENT_MEXAM_PLAN_OPERATE_LOG")
public class ExamPlanOperateLogEntity implements Serializable {

    @UtopiaSqlColumn(name = "ID", primaryKey = true, primaryKeyGeneratorType = AUTO_INC)
    protected Long id;

    /**
     * 计划id
     */
    @UtopiaSqlColumn
    private Long planId;

    /**
     * 上一个状态
     */
    @UtopiaSqlColumn
    private String prevStatus;

    /**
     * 当前状态
     */
    @UtopiaSqlColumn
    private String currentStatus;

    /**
     * 备注
     */
    @UtopiaSqlColumn
    private String note;

    /**
     * 操作人ID
     */
    @UtopiaSqlColumn
    private Long operatorId;

    /**
     * 操作人姓名
     */
    @UtopiaSqlColumn
    private String operatorName;

    /**
     * 创建时间
     */
    @UtopiaSqlColumn
    @DocumentCreateTimestamp
    private Date createDatetime;

    //生成缓存 key
    public static String ck_eid(Long examId) {
        return CacheKeyGenerator.generateCacheKey(ExamPlanOperateLogEntity.class, "eid", examId);
    }
}
