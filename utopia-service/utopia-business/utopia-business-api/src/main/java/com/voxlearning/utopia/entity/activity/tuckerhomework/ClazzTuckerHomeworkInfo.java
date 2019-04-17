package com.voxlearning.utopia.entity.activity.tuckerhomework;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.*;

import static com.voxlearning.alps.calendar.DateUtils.FORMAT_SQL_DATE;
import static com.voxlearning.alps.calendar.DateUtils.dateToString;

@Getter
@Setter
public class ClazzTuckerHomeworkInfo implements Serializable {

    private static final long serialVersionUID = 3156917583879214547L;

    public static final int AccomplishCount = RuntimeMode.isProduction() ? 20 : 1;

    private String clazzName;     // 班级名称
    private Long clazzId;         // 班级ID
    private Long groupId;         // 班组ID
    private Integer studentCount; // 学生数量

    private List<TuckerHomeworkInfo> homeworkList;  // 作业信息

    /**
     * [至少一个班检查作业>=3次]&[每次作业完成人数>=20人]
     */
    public boolean accomplish() {
        return homeworkList != null
                && homeworkList.stream().filter(hw -> hw.getAccomplishCount() >= AccomplishCount).count() >= 3;
    }

    public long getAccomplishCount(){
        Set<String> assignTimeSet = new HashSet<>();
        return Optional.ofNullable(homeworkList)
                .orElse(Collections.emptyList())
                .stream()
                .filter(hw -> hw.getAccomplishCount() >= AccomplishCount)
                // 同一天只记录一次
                .filter(hw -> assignTimeSet.add(dateToString(hw.getAssignTime(),FORMAT_SQL_DATE)))
                .count();
    }

    public int totalAccomplishCount() {
        if (CollectionUtils.isEmpty(homeworkList)) {
            return 0;
        }
        return (int) homeworkList.stream()
                .filter(TuckerHomeworkInfo::accomplished)
                .count();
    }
}
