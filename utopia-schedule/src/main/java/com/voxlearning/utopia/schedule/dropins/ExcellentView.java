package com.voxlearning.utopia.schedule.dropins;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * 优秀作品视图
 *
 * @Author: peng.zhang
 * @Date: 2018/10/30
 */
@Data
public class ExcellentView implements Serializable {

    /**
     * 课件ID
     */
    private String courseId;

    /**
     * 得分
     */
    private Long score;

    /**
     * 奖项名称
     */
    public String awardLevelName;

    /**
     * 奖项级别
     */
    public Integer awardLevelId;

    /**
     * 课件标题
     */
    public String title;

    /**
     * 课件总分
     */
    public Integer totalScore;

    /**
     * 老师名称
     */
    public String teacherName;

    /**
     * 老师ID
     */
    public Long teacherId;

    /**
     * 课件学科名
     */
    public String subject;

    /**
     * 学校名
     */
    public String schoolName;

    /**
     * 课件创建时间
     */
    public String createDate;

    /**
     * 排名较上次变化情况 : up:上升,down:下降,new:新作品,flat:不变
     */
    private String dynamicRank;

    @AllArgsConstructor
    public enum Dynamic{
        UP("上升"),
        DOWN("下降"),
        FLAT("不变"),
        NEW_COURSE("新增");
        public String desc;
    }
}
