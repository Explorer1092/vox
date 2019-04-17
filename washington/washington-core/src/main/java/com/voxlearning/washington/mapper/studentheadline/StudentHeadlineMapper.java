package com.voxlearning.washington.mapper.studentheadline;

import com.voxlearning.alps.calendar.DateUtils;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * @author xinxin
 * @since 17/8/2016
 */
@Getter
@Setter
public abstract class StudentHeadlineMapper implements Serializable {
    private static final long serialVersionUID = 3294541884530502256L;

    private Long journalId;       // 对应记录的ID
    private String type;          // 类型放这里比较合适
    private String dateTime;
    private Long timestamp;
    private Boolean wmflag;

    public void initDateTime(Date date) {
        if (date == null) {
            return;
        }
        dateTime = DateUtils.dateToString(date, "yyyy-MM-dd HH:mm");
        timestamp = date.getTime();
    }

    public boolean valid() {
        return true;
    }

}
