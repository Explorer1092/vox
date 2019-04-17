package com.voxlearning.utopia.business.api.context.teachingdiagnosis;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.utopia.business.api.context.AbstractContext;
import com.voxlearning.utopia.entity.teachingdiagnosis.experiment.TeachingDiagnosisExperimentConfig;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

/**
 * @author songtao
 * @since 2018/2/8
 */
@Getter
@Setter
public class PreQuestionResultContext extends AbstractContext<PreQuestionResultContext> {
    private static final long serialVersionUID = 6070441048736945998L;

    // in
    private StudentDetail student; // 学生
    private Subject subject; // 学科
    private String questionId; // 试题id
    private Boolean master; // 是否做对了
    private Boolean last; // 是否是最后一题
    private List<List<String>> answer;//用户答案
    private Long finishTime;
    private Date createTime;
    private String experimentId;

    // middle
    private Integer wrongNum = 0;
    private Integer totalNum;
    private List<String> courseIds = new ArrayList<>();
    private TeachingDiagnosisExperimentConfig config = null;

    // out
    private Map<String, Object> result = new HashMap<>();

}
