package com.voxlearning.washington.data.view;

import com.voxlearning.utopia.service.campaign.api.entity.TeacherCourseware;
import lombok.Data;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.voxlearning.washington.controller.teacher.TeacherCourseEvaluationController.COMMENT_NUM_WHICH_SHOW_TOTAL_SCORE;

/**
 * 课件传输视图
 *
 * @Author: peng.zhang
 * @Date: 2018/9/3
 */
@Data
public class TeacherCoursewarView {

    public Long teacherId;

    public String teacherName;

    public String createDate;

    public String schoolName;

    public String fileName;

    public String pictureUrl;

    public String coverUrl;

    public String title;

    public Integer visitNum;

    public Integer commentNum;

    public Integer totalScore;

    public Integer downloadNum;

    public String courseId;

    public String subject;

    public String bookName;

    public Integer clazzLevel;

    public Integer term;

    public String seriesId;

    public String awardLevelName;

    public Integer awardLevelId;

    public Boolean weekPopularityTop3;

    public Integer weekPopularityRank;

    public Boolean monthExcellentTop3;

    public Integer monthExcellentRank;

    public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    public static class Builder{

        public static List<TeacherCoursewarView> build(List<TeacherCourseware> teacherCoursewareList){
            List<TeacherCoursewarView> dtos = new ArrayList<>();
            for (TeacherCourseware teacherCourseware : teacherCoursewareList){
                TeacherCoursewarView dto = new TeacherCoursewarView();
                Date now = new Date();
                String time = calculateDate(now,teacherCourseware.getUpdateTime());
                dto.setCreateDate(time);
                dto.setTeacherId(teacherCourseware.getTeacherId());
                dto.setTitle(teacherCourseware.getTitle());
                dto.setCoverUrl(teacherCourseware.getCoverUrl());
                dto.setVisitNum(teacherCourseware.getVisitNum());
                dto.setCommentNum(teacherCourseware.getCommentNum() == null ? 0
                        : teacherCourseware.getCommentNum());
                dto.setTotalScore(teacherCourseware.getTotalScore() == null ? 0
                        : teacherCourseware.getTotalScore());
                dto.setDownloadNum(teacherCourseware.getDownloadNum() == null ? 0
                        : teacherCourseware.getDownloadNum());
                dto.setCourseId(teacherCourseware.getId());
                dto.setSubject(teacherCourseware.getSubject().name());
                dto.setClazzLevel(teacherCourseware.getClazzLevel());
                dto.setTerm(teacherCourseware.getTermType());
                dto.setSeriesId(teacherCourseware.getBookId());
                dto.setAwardLevelId(teacherCourseware.getAwardLevelId());
                dto.setAwardLevelName(teacherCourseware.getAwardLevelName());
                dtos.add(dto);
            }
            return dtos;
        }

        public static List<TeacherCoursewarView> buildForAll(List<TeacherCourseware> teacherCoursewareList){
            List<TeacherCoursewarView> dtos = new ArrayList<>();
            for (TeacherCourseware teacherCourseware : teacherCoursewareList){
                TeacherCoursewarView dto = new TeacherCoursewarView();
                dto.setCreateDate(sdf.format(teacherCourseware.getExamineUpdateTime()));
                dto.setTeacherId(teacherCourseware.getTeacherId());
                dto.setTitle(teacherCourseware.getTitle());
                dto.setCoverUrl(teacherCourseware.getCoverUrl());
                dto.setVisitNum(teacherCourseware.getVisitNum());
                dto.setCommentNum(teacherCourseware.getCommentNum() == null ? 0
                        : teacherCourseware.getCommentNum());
                // 评论数小于 3 , 分数是 0
                dto.setTotalScore(teacherCourseware.getTotalScore() == null ||
                        teacherCourseware.getCommentNum() < COMMENT_NUM_WHICH_SHOW_TOTAL_SCORE ? 0
                        : teacherCourseware.getTotalScore());
                dto.setDownloadNum(teacherCourseware.getDownloadNum() == null ? 0
                        : teacherCourseware.getDownloadNum());
                dto.setCourseId(teacherCourseware.getId());
                dto.setSubject(teacherCourseware.getSubject().name());
                dto.setClazzLevel(teacherCourseware.getClazzLevel());
                dto.setTerm(teacherCourseware.getTermType());
                dto.setSeriesId(teacherCourseware.getBookId());
                dto.setAwardLevelId(teacherCourseware.getAwardLevelId());
                dto.setAwardLevelName(teacherCourseware.getAwardLevelName());
                dtos.add(dto);
            }
            return dtos;
        }

        public static String calculateDate(Date now,Date other){
            // 是今天,显示小时和分钟
            if (sdf.format(now).equals(sdf.format(other))){
                long between = now.getTime() - other.getTime();
                long day = between / Builder.DEFAULT_VALUES.ONE_DAY;
                long hour = (between / Builder.DEFAULT_VALUES.ONE_HOUR - day * 24);
                long min = ((between / Builder.DEFAULT_VALUES.ONE_MINUTE) - day * 24 * 60 - hour * 60);
                String result = "";
                if (hour == 0 && min ==0){
                    return result;
                } else if (hour == 0){
                    return min + Builder.DEFAULT_VALUES.MINUTE;
                } else if (min == 0){
                    return hour + "小时前";
                } else {
                    return hour + Builder.DEFAULT_VALUES.HOUR + min + Builder.DEFAULT_VALUES.MINUTE;
                }
            } else {
                return sdf.format(other);
            }
        }

        interface DEFAULT_VALUES{

            Integer ONE_DAY = 60 * 60 * 24 * 1000;

            Integer ONE_HOUR = 60 * 60 * 1000;

            Integer ONE_MINUTE = 60 * 1000;

            String MINUTE = "分钟前";

            String HOUR = "小时";
        }
    }
}
