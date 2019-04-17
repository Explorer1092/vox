package com.voxlearning.utopia.service.newhomework.api.mapper.termplan;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author zhangbin
 * @since 2018/3/5
 */

@Setter
@Getter
public class TermPlanKnowledgeBO implements Serializable {
    private static final long serialVersionUID = -8450037831371077206L;

    private Integer totalKnowledgePointNum;
    private Integer doneKnowledgePointNum;
    private Integer clazzRightRate;
    private Integer cityRightRate;
    private Integer cityTopTenRightRate;
    private List<Map<String, Object>> unitDetails;
    private List<UnitTest> unitTests;


    @Setter
    @Getter
    public static class UnitTest implements Serializable {
        private static final long serialVersionUID = 6979470290743745507L;

        private String paperId;
        private String paperName;
        private String newExamId;
        private Integer score;
    }
}
