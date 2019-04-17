package com.voxlearning.utopia.agent.mockexam.service.dto.output;

import com.voxlearning.utopia.agent.mockexam.domain.model.ExamStudentScore;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * 学生成绩
 *
 * @author xiaolei.li
 * @version 2018/8/26
 */
@Data
public class ExamStudentScoreDto implements Serializable {
    /**
     * 学生id
     */
    private Long studentId;

    /**
     * 学生姓名
     */
    private String studentName;

    /**
     * 省
     */
    private String province;

    /**
     * 城市
     */
    private String city;

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
        public static ExamStudentScoreDto build(ExamStudentScore score) {
            ExamStudentScoreDto dto = new ExamStudentScoreDto();
            BeanUtils.copyProperties(score, dto);
            return dto;
        }
    }
}
