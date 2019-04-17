package com.voxlearning.utopia.service.newhomework.api.mapper.report.wordteach;

import com.voxlearning.utopia.service.newhomework.api.constant.WordTeachModuleType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * @Description: 汉字文化-班级数据
 * @author: Mr_VanGogh
 * @date: 2018/12/18 上午10:25
 */
@Getter
@Setter
public class ChineseCharacterCultureModuleClazzData implements Serializable{
    private static final long serialVersionUID = 5528291888088047429L;

    private WordTeachModuleType wordTeachModuleType;
    private String moduleName;
    private List<ChineseCharacterCultureCourseData> courseDatas;

    @Getter
    @Setter
    public static class ChineseCharacterCultureCourseData implements Serializable {
        private static final long serialVersionUID = 4242184960089176329L;

        private String courseId;        //课程ID
        private String title;      //课程名称
        private Integer finishNum;      //完成人数
        private List<StudentHomeworkData> studentHomeworkDatas;  //学生答题详情
    }
}
