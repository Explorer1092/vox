package com.voxlearning.utopia.agent.bean;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * VisitSchoolResultData
 *
 * @author song.wang
 * @date 2016/7/26
 */
@Getter
@Setter
@NoArgsConstructor
public class VisitSchoolResultData implements Serializable{

    private Integer visitSchoolCount = 0;    // 进校数量
    private Integer teacherTotalCount = 0;   // 老师总数
    private Integer visitedUsedCount = 0;    // 拜访已使用老师数
    private Integer visitedUnusedCount = 0;  // 拜访未使用老师数
    private Integer unvisitedUsedCount = 0;  // 未拜访已使用的老师数
    private Integer unvisitedUnusedCount = 0;// 未拜访未使用老师数

    public void appendData(SchoolVisitResultInfo data) {
        if (data == null) {
            return;
        }

        this.teacherTotalCount += data.getSchoolTeacherCount();
        this.visitedUsedCount += data.getVisitedUsedCount();
        this.visitedUnusedCount += data.getVisitedUnusedCount();
        this.unvisitedUsedCount += data.getUnvisitedUsedCount();
        this.unvisitedUnusedCount += data.getUnvisitedUnusedCount();
    }
}
