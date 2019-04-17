package com.voxlearning.utopia.admin.entity;

import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * @author fugui.chang
 * @since 2017/2/13
 */

@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-crm")
@DocumentDatabase(database = "vox-crm")
@DocumentCollection(collection = "vox_teacher_transfer_school_record")
public class CrmTeacherTransferSchoolRecord implements Serializable {
    private static final long serialVersionUID = -1383443661132760679L;
    @DocumentId private String id;
    @DocumentField("teacherId") private Long teacherId;//老师id
    @DocumentField("authenticationState") private Boolean authenticationState;//老师是否认证
    @DocumentField("changeType") private ChangeType changeType;//转校类型 带班转校或不带班转校
    @DocumentField("sourceSchoolId") private Long sourceSchoolId;//转出校
    @DocumentField("isSourceSchoolDict") private Boolean isSourceSchoolDict;//转出校
    @DocumentField("targetSchoolId") private Long targetSchoolId;//转入校
    @DocumentField("operator") private String operator;//转校的操作人
    @DocumentField("operationTime") private Date operationTime;//操作时间
    @DocumentField("changeSchoolDesc") private String changeSchoolDesc;//转校时的操作描述

    @DocumentField("checkOperator") private String checkOperator;//二次审核人
    @DocumentField("checkResult") CheckResult checkResult;//二次审核结果  待处理 正确 错误
    @DocumentField("checkDesc") String checkDesc;//二次审核备注

    @DocumentCreateTimestamp @DocumentField("ct") protected Date createTime;
    @DocumentUpdateTimestamp @DocumentField("ut") protected Date updateTime;//审核时间


    public CrmTeacherTransferSchoolRecord(Long teacherId, Boolean authenticationState, ChangeType changeType,
                                          Long sourceSchoolId, Boolean isSourceSchoolDict, Long targetSchoolId,
                                          String operator, Date operationTime, String changeSchoolDesc,
                                          CheckResult checkResult){
        this.teacherId = teacherId;
        this.authenticationState = authenticationState;
        this.changeType = changeType;
        this.sourceSchoolId = sourceSchoolId;
        this.isSourceSchoolDict = isSourceSchoolDict;
        this.targetSchoolId = targetSchoolId;
        this.operator = operator;
        this.operationTime = operationTime;
        this.changeSchoolDesc = changeSchoolDesc;
        this.checkResult = checkResult;
    }

    public enum  ChangeType{
        UNKNOW,
        WITHCLAZZS,//带班
        WITHOUTCLAZZS //不带班
    };

    public enum  CheckResult{
        UNKNOWN,
        NOTHANDLED, //未处理
        TRUE, //正确
        FALSE //错误
    };

}
