package com.voxlearning.washington.data.view;

import com.voxlearning.utopia.service.campaign.api.entity.TeacherCourseware;
import lombok.Data;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: peng.zhang
 * @Date: 2018/10/25
 */
@Data
public class ExcellentCourseView {

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

    /**
     * 综合评分
     */
    public Long compositeScore;

    public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    public static class Builder {

        public static List<ExcellentCourseView> build(List<TeacherCourseware> teacherCoursewareList) {
            List<ExcellentCourseView> dtos = new ArrayList<>();
            for (TeacherCourseware teacherCourseware : teacherCoursewareList) {
                ExcellentCourseView dto = new ExcellentCourseView();
                dto.setCreateDate(sdf.format(teacherCourseware.getUpdateTime()));
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
                dtos.add(dto);
            }
            return dtos;
        }
    }
}
