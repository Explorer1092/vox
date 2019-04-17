package com.voxlearning.washington.mapper.activity;

import com.voxlearning.alps.calendar.DateFormatUtils;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.utopia.entity.crm.ActivityConfig;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class TeacherActivityIndexMapper extends ActivityConfig {

    /**
     * 总人数
     */
    private Long sumCount;

    /**
     * 参加人数
     */
    private Long participantsCount;

    public Integer getActivityStatus() {
        Date now = new Date();
        return super.isUnStart(now) ? 1 : (isStarting(now) ? 2 : 3);
    }

    public String getStartTimeStr() {
        return DateFormatUtils.format(super.getStartTime(), "yyyy/MM/dd");
    }

    public String getEndTimeStr() {
        return DateFormatUtils.format(super.getEndTime(), "yyyy/MM/dd");
    }

    /**
     * 距离结束时间还剩多少天
     */
    public long getEndDays() {
        return DateUtils.dayDiff(super.getEndTime(), new Date()) + 1;
    }

    /**
     * 距离开始时间还剩多少天
     */
    public long getStartDays() {
        return DateUtils.dayDiff(super.getStartTime(), new Date()) + 1;
    }

}
