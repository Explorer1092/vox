package com.voxlearning.utopia.agent.bean.resource;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class GradeResourceCard implements Serializable {
    private Integer gradeLevel;                    // 年级                 2017/5/22
    private String gradeName;                      // 年级名称             2017/5/22
    private Long gradeScale = 0L;                  // 年级规模             2017/5/22
    private Integer authNum = 0;                   // 年级认证学生数量      2017/5/22
    private Integer monthActive = 0;               // 年级月活             2017/5/22
    private Integer tmCsAnshEq2StuCount = 0;       // 年级数扫
    private List<ClazzResource> clazzList;         // 班级信息列表         2017/5/22

    //=========快乐学
    private Integer stuKlxTnCount; //考号数
//
//    transient private Set<Long> authStudent = new HashSet<>();       // 认证学生ID
//    transient private Set<Long> sascStudent = new HashSet<>();       // 单活学生ID
//    transient private Set<Long> dascStudent = new HashSet<>();       // 双活学生ID

   /* // mode=1 : 17模式  mode=2:快乐学模式
    public void appendStudentStatistic(ClazzTeacherResource clazzTeacherResource, int mode) {
        if(clazzTeacherResource == null){
            return;
        }

        if(mode == 1){
            //authNum += SafeConverter.toInt(clazzTeacherResource.getAuthNum());
            if(Subject.ENGLISH == clazzTeacherResource.getSubject()){
                engMauc += SafeConverter.toInt(clazzTeacherResource.getSaNum());
            }else if(Subject.MATH == clazzTeacherResource.getSubject()){
                mathMauc += SafeConverter.toInt(clazzTeacherResource.getSaNum());
            }
        }else if(mode == 2){


        }
    }*/
}
