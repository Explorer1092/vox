package com.voxlearning.utopia.agent.bean.apply;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @description:
 * @author: kaibo.he
 * @create: 2019-01-21 14:02
 **/
@Data
@Builder
public class DictSchoolVo {
    //英语起始年级
    private Integer englishStartGrade;
    //年级信息
    private List<DictSchoolEditParams.GradeData> gradeDataList;
    //负责人
    private Long responsibleId;
    private String responsibleName;
    //字典学校等级
    private String schoolPopularity;
}
