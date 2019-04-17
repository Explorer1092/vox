package com.voxlearning.utopia.service.newhomework.api.mapper.report;

import com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.app.OcrNewHomeworkStudentDetail;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @Description: 纸质作业报告分享
 * @author: Mr_VanGogh
 * @date: 2019/2/15 上午11:43
 */
@Getter
@Setter
public class OcrHomeworkShareReport implements Serializable {
    private static final long serialVersionUID = 5757743472434802224L;

    private String homeworkId; // 作业ID
    private boolean share = true;// 是否分享
    private String homeworkDate;//作业日期
    private Long teacherId;
    private String teacherName;
    private String teacherUrl;
    private String teacherShareMsg;
    private Long clazzGroupId;
    private String clazzGroupName;
    private Integer useYQXNum;
    private List<Integer> channels;//分享渠道
    private FinishedStudent finishedStudent;//表现优秀(完成学生)
    private List<OcrNewHomeworkStudentDetail.WrongQuestionPart> wrongQuestionParts = new ArrayList<>(); //错因信息

    @Getter
    @Setter
    public static class FinishedStudent implements Serializable {
        private static final long serialVersionUID = -8973358694359235018L;

        private String typeName;
        private Integer finishedStudentNum;
        private Set<String> studentNames;
    }
}
