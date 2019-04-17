package com.voxlearning.utopia.agent.bean.apply;

import com.voxlearning.utopia.agent.view.school.SchoolGradeBasicData;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @description: 加入字典学校入参
 * @author: kaibo.he
 * @create: 2019-01-21 10:45
 **/
@Data
public class DictSchoolEditParams {
    //学校id
    private Long schoolId;
    //英语起始年级
    private Integer englishStartGrade;
    //英语起始年级名称
    private String englishStartGradeDesc;
    //年级信息列表
    private List<GradeData> gradeDataList;
    //负责人id
    private Long responsibleId;
    private String responsibleName;
    //学校等级
    private String schoolPopularity;
    private String schoolPopularityDesc;
    //申请原因
    private String comment;

    @Data
    public static class GradeData {
        //年级
        private Integer grade;
        private String gradeDesc;
        //班级数
        private Integer clazzNum;
        //学生数
        private Integer studentNum;

        public static class Builder {
            public static List<GradeData> build(List<SchoolGradeBasicData> gradeBasicDataList) {
                List<GradeData> gradeDataList = new ArrayList<>();
                gradeBasicDataList.forEach(gradeBasicData -> {
                    GradeData gradeData = new GradeData();
                    BeanUtils.copyProperties(gradeBasicData, gradeData);
                    gradeDataList.add(gradeData);
                });
                return gradeDataList;
            }
        }
    }
}
