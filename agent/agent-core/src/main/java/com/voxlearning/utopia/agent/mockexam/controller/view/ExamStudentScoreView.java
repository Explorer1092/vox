package com.voxlearning.utopia.agent.mockexam.controller.view;

import com.voxlearning.alps.repackaged.org.apache.commons.lang3.StringUtils;
import com.voxlearning.utopia.agent.mockexam.service.dto.output.ExamStudentScoreDto;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * 学生成绩视图
 *
 * @author xiaolei.li
 * @version 2018/8/26
 */
@Data
public class ExamStudentScoreView implements Serializable {

    /**
     * 学生id
     */
    private Long studentId;

    /**
     * 学生姓名
     */
    private String studentName;

    /**
     * 区
     */
    private String region;

    /**
     * 学校
     */
    private String school;

    /**
     * 年级和班级
     */
    private String className;

    /**
     * 参与类型
     */
    private String examType;

    /**
     * 考试开始时间
     */
    private Date startAt;

    /**
     * 交卷时间
     */
    private Date submitAt;

    /**
     * 考试时长，单位分钟
     */
    private Long duration;

    /**
     * 成绩
     */
    private Double score;

    public static class Builder {
        public static ExamStudentScoreView build(ExamStudentScoreDto dto) {
            ExamStudentScoreView view = new ExamStudentScoreView();
            if (null != dto) {
                BeanUtils.copyProperties(dto, view);
                if (StringUtils.isNotBlank(dto.getProvince()) && StringUtils.isNotBlank(dto.getCity()) && StringUtils.isNotBlank(dto.getRegion()))
                    view.setRegion(String.format("%s %s %s", dto.getProvince(), dto.getCity(), dto.getRegion()));
            }
            return view;
        }
    }
}
