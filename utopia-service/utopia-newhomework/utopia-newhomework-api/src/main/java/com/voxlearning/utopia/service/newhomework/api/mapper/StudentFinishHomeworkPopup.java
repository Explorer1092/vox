package com.voxlearning.utopia.service.newhomework.api.mapper;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

/**
 * 学生完成作业在家长端的弹窗消息
 *
 * @author shiwei.liao
 * @since 2017-3-6
 */
@Getter
@Setter
@NoArgsConstructor
public class StudentFinishHomeworkPopup implements Serializable {

    private static final long serialVersionUID = -6572996857483756313L;
    private Long studentId;
    private String subject;
    private String homeworkId;
    private String content;
    private Date expireDate;
    private Set<Long> showedParentIds;
}
