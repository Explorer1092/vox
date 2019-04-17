package com.voxlearning.utopia.agent.bean;

import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.utopia.service.crm.api.constants.agent.CrmWorkRecordType;
import com.voxlearning.utopia.service.crm.api.entities.agent.CrmWorkRecord;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author Jia HuanYin
 * @since 2016/3/28
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegionMemberRecord implements Comparable, Serializable {
    private static final long serialVersionUID = 2175085012838382663L;

    public static final String USER_TYPE = "USER";
    public static final String GROUP_TYPE = "GROUP";

    private String key;
    private String name;
    private String type;
    private String note;
    private long schoolCount;
    private long meetingCount;
    private long visitCount;

    public RegionMemberRecord(String key, String name, String type) {
        this.key = key;
        this.name = name;
        this.type = type;
    }

    public RegionMemberRecord(String key, String name, String type, String note) {
        this.key = key;
        this.name = name;
        this.type = type;
        this.note = note;
    }

    public void increase(CrmWorkRecord workRecord) {
        CrmWorkRecordType workType = workRecord.getWorkType();
        if (workType != null) {
            switch (workType) {
                case SCHOOL:
                    this.schoolCount++;
                    break;
                case MEETING:
                    this.meetingCount++;
                    break;
                case VISIT:
                    this.visitCount++;
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public int compareTo(Object o) {
        if (o == null || !(o instanceof RegionMemberRecord)) {
            return -1;
        }
        RegionMemberRecord other = (RegionMemberRecord) o;
        long value = other.schoolCount - this.schoolCount;
        if (value == 0) {
            value = other.meetingCount - this.meetingCount;
            if (value == 0) {
                value = other.visitCount - this.visitCount;
            }
        }
        return ConversionUtils.toInt(value);
    }
}
