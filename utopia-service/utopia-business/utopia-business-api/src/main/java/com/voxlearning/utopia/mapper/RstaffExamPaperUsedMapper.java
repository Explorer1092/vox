package com.voxlearning.utopia.mapper;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Maofeng Lu
 * @since 13-8-13 上午9:52
 */
@Data
public class RstaffExamPaperUsedMapper implements Serializable {
    private static final long serialVersionUID = -3347900644444798281L;

    private Long schoolId;
    private String schoolName;
    private int authTeacherCntInSchool;
    private int authTeacherCntHasExamPaper;
    private int studentCntInAuthClazz;
    private int completeStudentCnt;
    private boolean displayMessageBtn; //是否显示发送通知按钮 true:显示，false:不显示
}
