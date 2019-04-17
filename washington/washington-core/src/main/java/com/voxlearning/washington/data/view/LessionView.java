package com.voxlearning.washington.data.view;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

import static com.voxlearning.washington.controller.teacher.TeacherCoursewareContestController.TOPIC_LESSON_ID;
import static com.voxlearning.washington.controller.teacher.TeacherCoursewareContestController.TOPIC_NAME;

/**
 * 课程信息视图
 *
 * @Author: peng.zhang
 * @Date: 2018/9/5
 */
@Data
public class LessionView {

    /**
     * 课程 ID
     */
    private String lessonId;

    /**
     * 课程真实名称
     */
    private String lessonRealName;

    /**
     * 课程别名
     */
    private String lessonAliasName;

    public static class Builder{
        public static List<LessionView> build(List<NewBookCatalog> bookCatalogs){
            List<LessionView> viewList = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(bookCatalogs)){
                for (NewBookCatalog bookCatalog : bookCatalogs){
                    LessionView lessionView = new LessionView();
                    lessionView.setLessonId(bookCatalog.getId());
                    lessionView.setLessonRealName(bookCatalog.getName());
                    lessionView.setLessonAliasName(bookCatalog.getAlias());
                    viewList.add(lessionView);
                }
            } else {
                LessionView lessionView = new LessionView();
                lessionView.setLessonId(TOPIC_LESSON_ID);
                lessionView.setLessonRealName(TOPIC_NAME);
                lessionView.setLessonAliasName(TOPIC_NAME);
                viewList.add(lessionView);
            }
            return viewList;
        }
    }
}
