package com.voxlearning.utopia.entity.activity;

import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 老师奖学金活动记录
 */
@Getter
@Setter
@DocumentConnection(configName = "hs_misc")
@DocumentTable(table = "VOX_TEACHER_SCHOLARSHIP_RECORD")
@UtopiaCacheRevision("20181204")
public class TeacherScholarshipRecord extends AbstractDatabaseEntity{

    private static final long serialVersionUID = -3482873371916702641L;

    @UtopiaSqlColumn private Long teacherId;
    @UtopiaSqlColumn private String subjects;         // 科目
    @UtopiaSqlColumn private Date lastAssignDate;     // 最近一次布置的日期
    @UtopiaSqlColumn private Integer termReviewNum;   // 期末复习的布置次数
    @UtopiaSqlColumn private Integer termReviewChecked;//期末复习检查的次数
    @UtopiaSqlColumn private Integer basicReviewNum;  // 基础必过的布置次数
    @UtopiaSqlColumn private Double finishRate;       // 期末复习的完成比例平均值
    @UtopiaSqlColumn private Double score;            // 期末复习作业的平均分数
    @UtopiaSqlColumn private Integer maxGroupFinishNum;//老师名下完成人数最多的组的完成数量
    @UtopiaSqlColumn private Boolean dailyLottery;    // 每日奖学金参与标志
    @UtopiaSqlColumn private Boolean weekLottery;     //每周奖学金参与标志
    @UtopiaSqlColumn private Boolean finalLottery;    // 最终奖品金参与标志

    public static String ck_teacher_id(Long teacherId){
        return CacheKeyGenerator.generateCacheKey(TeacherScholarshipRecord.class,"TEACHER_ID",teacherId);
    }

    public void addTermReviewNum(int add){
        if(this.termReviewNum == null)
            this.termReviewNum = 0;

        this.termReviewNum += add;
    }

    public void addBasicReviewNum(int add){
        if(this.basicReviewNum == null)
            this.basicReviewNum = 0;

        this.basicReviewNum+= add;
    }

}
