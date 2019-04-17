package com.voxlearning.washington.data.view;

import com.voxlearning.alps.core.util.CollectionUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 人气榜视图
 *
 * @Author: peng.zhang
 * @Date: 2018/10/29
 */
@Data
public class PopularityView implements Serializable {

    /**
     * 课件ID
     */
    private String courseId;

    /**
     * 人气值
     */
    private Long num;

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
     * 封面图片url
     */
    public String coverUrl;

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

    public static class Builder{
        public static List<PopularityView> build(List<Map<String, Object>> data){
            List<PopularityView> popularityViewList = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(data)){
                data.stream().forEach(e->{
                    PopularityView view = new PopularityView();
                    view.setCourseId((String) e.get("COURSEWARE_ID"));
                    view.setNum((Long) e.get("NUM"));
                    popularityViewList.add(view);
                });
            }
            return popularityViewList;
        }
    }
}
