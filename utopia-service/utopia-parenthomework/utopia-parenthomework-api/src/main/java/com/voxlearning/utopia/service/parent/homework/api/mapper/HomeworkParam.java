package com.voxlearning.utopia.service.parent.homework.api.mapper;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 作业参数
 *
 * @author Wenlong Meng
 * @version 20181111
 * @date 2018-11-13
 */
@Getter
@Setter
public class HomeworkParam implements Serializable {

    private static final long serialVersionUID = -3773765521796336377L;
    private String homeworkId; //作业id
    private Long studentId;//学生id
    private Long currentUserId;//当前用户id
    private String source; //来源
    private String command;//
    private String objectiveConfigType;
    private String subject;
    private List<String> unitIds; // 单元
    private List<String> sectionIds;// 课时
    private String bizType;
    private String bookId; // 教材id
    private Map<String, Object> data;
}
