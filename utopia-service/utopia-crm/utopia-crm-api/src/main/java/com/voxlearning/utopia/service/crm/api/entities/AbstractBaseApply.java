package com.voxlearning.utopia.service.crm.api.entities;

import com.voxlearning.alps.annotation.common.Legacy;
import com.voxlearning.alps.annotation.dao.DocumentField;
import com.voxlearning.alps.annotation.dao.DocumentFieldIgnore;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import com.voxlearning.utopia.service.crm.api.constants.ApplyStatus;
import com.voxlearning.utopia.service.crm.api.constants.ApplyType;
import com.voxlearning.utopia.service.crm.api.constants.SystemPlatformType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * AbstractBaseApply
 *
 * @author song.wang
 * @date 2016/12/29
 */
@Legacy
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractBaseApply extends AbstractDatabaseEntity{

    @Getter
    @Setter
    @UtopiaSqlColumn(name = "APPLY_TYPE")
    @DocumentField("APPLY_TYPE")
    protected ApplyType applyType;

    @Getter
    @Setter
    @UtopiaSqlColumn(name = "USER_PLATFORM")
    @DocumentField("USER_PLATFORM")
    protected SystemPlatformType userPlatform; // 用户平台（ADMIN, AGENT）

    @Getter
    @Setter
    @UtopiaSqlColumn(name = "ACCOUNT")
    @DocumentField("ACCOUNT")
    protected String account; // 创建者账号

    @Getter
    @Setter
    @UtopiaSqlColumn(name = "ACCOUNT_NAME")
    @DocumentField("ACCOUNT_NAME")
    protected String accountName; // 创建者名字

    @Getter
    @Setter
    @UtopiaSqlColumn(name = "STATUS")
    @DocumentField("STATUS")
    protected ApplyStatus status; // 申请的审核状态

    @Getter
    @Setter
    @UtopiaSqlColumn(name = "WORKFLOW_ID")
    @DocumentField("WORKFLOW_ID")
    protected Long workflowId; // 工作流ID

    @Getter
    @Setter
    @DocumentFieldIgnore
    protected Boolean canRevoke; // 是否可以撤销申请

    abstract protected String generateSummary();

}
