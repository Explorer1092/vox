package com.voxlearning.washington.data.view;

import com.voxlearning.alps.core.util.CollectionUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 达人榜排行视图
 *
 * @Author: peng.zhang
 * @Date: 2018/10/29
 */
@Data
public class TalentRankView {

    /**
     * 老师ID
     */
    public Long teacherId;

    /**
     * 达人榜分数
     */
    public Long num;

//    public String awardLevelName;
//
//    public Integer awardLevelId;
//
//    public String title;
//
//    public Integer totalScore;
//
    public String teacherName;

    public String schoolName;

    /**
     * 头像
     */
    public String portrait;

    /**
     * up:上升,down:下降,new:新作品,flat:不变
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
        public static List<TalentRankView> build(List<Map<String, Object>> data){
            List<TalentRankView> talentRankViewList = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(data)){
                data.stream().forEach(e->{
                    TalentRankView view = new TalentRankView();
                    view.setTeacherId((Long) e.get("TEACHER_ID"));
                    view.setNum(Long.valueOf(e.get("TOTALNUM").toString()));
                    talentRankViewList.add(view);
                });
            }
            return talentRankViewList;
        }
    }
}
