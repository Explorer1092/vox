package com.voxlearning.utopia.data;

import com.voxlearning.utopia.api.constant.CrmContactType;
import com.voxlearning.utopia.api.constant.CrmTaskRecordCategory;
import com.voxlearning.utopia.entity.crm.CrmTaskRecord;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Jia HuanYin
 * @since 2016/2/4
 */
@Getter
@Setter
@NoArgsConstructor
public class UserRecordSnapshot implements Serializable, Comparable {
    private static final long serialVersionUID = -6693974396048196909L;

    private String recordId;
    private String recorder;
    private String recorderName;
    private String recordType;
    private Date recordTime;
    private String recordTitle;
    private String recordContent;
    private String recordNote;

    public UserRecordSnapshot(CrmTaskRecord taskRecord) {
        recordId = taskRecord.getId();
        recorder = taskRecord.getRecorder();
        recorderName = taskRecord.getRecorderName();
        CrmContactType contactType = taskRecord.getContactType();
        recordType = contactType == null ? null : contactType.name();
        recordTime = taskRecord.getCreateTime();
        CrmTaskRecordCategory firstCategory = taskRecord.getFirstCategory();
        CrmTaskRecordCategory secondCategory = taskRecord.getSecondCategory();
        CrmTaskRecordCategory thirdCategory = taskRecord.getThirdCategory();
        recordTitle = niceCategoryName(firstCategory) + "/" + niceCategoryName(secondCategory) + "/" + niceCategoryName(thirdCategory);
        recordContent = taskRecord.getContent();
    }

    @Override
    public int compareTo(Object other) {
        if (other == null || !(other instanceof UserRecordSnapshot)) {
            return -1;
        }
        UserRecordSnapshot bean = (UserRecordSnapshot) other;
        return bean.recordTime.compareTo(recordTime);
    }

    public static String niceCategoryName(CrmTaskRecordCategory category) {
        return category == null ? "" : category.name();
    }
}