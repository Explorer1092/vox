package com.voxlearning.washington.data.view;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.campaign.api.entity.TeacherCoursewareComment;
import lombok.Data;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 课件传输视图
 *
 * @Author: peng.zhang
 * @Date: 2018/9/3
 */
@Data
public class TeacherCoursewarEvaluationView {

    private String coursewareId;
    private Long teacherId;
    private Integer star;
    private List<String> commentList;
    private Date createTime;
    private String keyWord;

    public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    public static class Builder{

        public static TeacherCoursewarEvaluationView build(TeacherCoursewareComment teacherCoursewareComment){
            TeacherCoursewarEvaluationView view = new TeacherCoursewarEvaluationView();
            view.setCoursewareId(teacherCoursewareComment.getCourseware_id());
            view.setTeacherId(teacherCoursewareComment.getTeacher_id()
            );
            view.setStar(teacherCoursewareComment.getStar());
            view.setCreateTime(teacherCoursewareComment.getCreateTime());
            view.setKeyWord(teacherCoursewareComment.getKey_word());
            List<String> labelList = new ArrayList<>();
            if (StringUtils.isNotEmpty(teacherCoursewareComment.getComment_one())){
                labelList.add(teacherCoursewareComment.getComment_one());
            }
            if (StringUtils.isNotEmpty(teacherCoursewareComment.getComment_two())){
                labelList.add(teacherCoursewareComment.getComment_two());
            }
            if (StringUtils.isNotEmpty(teacherCoursewareComment.getComment_three())){
                labelList.add(teacherCoursewareComment.getComment_three());
            }
            if (StringUtils.isNotEmpty(teacherCoursewareComment.getComment_four())){
                labelList.add(teacherCoursewareComment.getComment_four());
            }
            if (StringUtils.isNotEmpty(teacherCoursewareComment.getComment_five())){
                labelList.add(teacherCoursewareComment.getComment_five());
            }
            view.setCommentList(labelList);
            return view;
        }

    }
}
