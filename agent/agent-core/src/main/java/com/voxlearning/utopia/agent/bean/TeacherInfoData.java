package com.voxlearning.utopia.agent.bean;

import com.voxlearning.alps.lang.convert.ConversionUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by yaguang.wang on 2016/4/25.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TeacherInfoData implements Comparable, Serializable {

    private String realName;
    private Long teacherId;
    private Long latestAssignHomeworkTime;
    private String latestAssignHomeworkTimeStr;
    private String subjectValue;
    private Integer authState;
    private Long registerTime;
    private String  registerTimeStr;
    private Integer registerStudentCount;

    @Override
    public int compareTo(Object o) {
        if (o == null || !(o instanceof TeacherInfoData)) {
            return -1;
        }
        TeacherInfoData other = (TeacherInfoData) o;
        long  value = ConversionUtils.toLong(other.registerTime) - ConversionUtils.toLong(this.registerTime);
        return ConversionUtils.toInt(value);
    }
}
