package com.voxlearning.utopia.service.parent.homework.impl.model;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.athena.api.recom.entity.EliteSchoolPackage;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.parent.homework.api.entity.*;
import com.voxlearning.utopia.service.parent.homework.api.mapper.HomeworkParam;
import com.voxlearning.utopia.service.parent.homework.api.mapper.QuestionPackage;
import com.voxlearning.utopia.service.parent.homework.api.mapper.StudentInfo;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 作业上下文
 *
 * @author Wenlong Meng
 * @version 20181111
 * @date 2018-11-15
 */
@Data
public class HomeworkContext {

    private Homework homework;//作业
    private HomeworkPractice homeworkPractice;//作业详情
    private HomeworkResult homeworkResult;//作业结果
    private List<HomeworkProcessResult> homeworkProcessResults;//作业结果详情
    private HomeworkParam homeworkParam;//参数
    private Long groupId;//班组id
    private MapMessage mapMessage;//结果信息
    private HomeworkUserPreferences userPreferences;//偏好设置
    private List<EliteSchoolPackage> eliteSchoolPackages;//题包
    private List<QuestionPackage> questionPackages;//题包
    private StudentInfo studentInfo;//学生信息
    private List<String> bookIds; // 教材列表
    private String unitId; // 默认单元
    private String bookId; // 默认教材
    private List<NewBookCatalog> units; // 单元列表
    private BookQuestionNode bookQuestionNode; // 教材的题
    private HomeworkUserProgress progress; // 用户进度
    private Map<String, Object> data;
}
