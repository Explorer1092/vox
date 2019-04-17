package com.voxlearning.utopia.agent.bean;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Jia HuanYin
 * @since 2015/11/19
 */
@Getter
@Setter
public class SchoolRecordSummary implements Serializable, Comparable {
    private static final long serialVersionUID = -8026371908077725880L;

    private Long schoolId;              // 学校ID
    private String schoolName;          // 学校名称
    private Date workTime;              // 最近进校记录时间
    private int count;                  // 进校记录数

    public SchoolRecordSummary(Long schoolId, String schoolName, Date workTime) {
        this.schoolId = schoolId;
        this.schoolName = schoolName;
        this.workTime = workTime;
    }

    public void increase() {
        this.count++;
    }

    @Override
    public int compareTo(Object o) {
        if (o == null || !(o instanceof SchoolRecordSummary)) {
            return -1;
        }
        SchoolRecordSummary other = (SchoolRecordSummary) o;
        return other.count - this.count;
    }
}
