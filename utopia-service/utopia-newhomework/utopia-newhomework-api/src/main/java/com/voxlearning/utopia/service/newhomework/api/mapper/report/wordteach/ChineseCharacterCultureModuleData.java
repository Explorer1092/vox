package com.voxlearning.utopia.service.newhomework.api.mapper.report.wordteach;

import com.voxlearning.utopia.service.newhomework.api.constant.WordTeachModuleType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * @Description: 汉字文化模块Data
 * @author: Mr_VanGogh
 * @date: 2018/12/14 下午4:45
 */
@Getter
@Setter
public class ChineseCharacterCultureModuleData implements Serializable{

    private static final long serialVersionUID = 5281952129657135520L;

    private WordTeachModuleType moduleType;
    private String moduleName;
    private List<ChineseCharacterCultureData> chineseCharacterCultureData;

    @Getter
    @Setter
    public static class ChineseCharacterCultureData implements Serializable {
        private static final long serialVersionUID = -1195563076429922013L;

        private String courseId;    //课程ID
        private String courseName;  //课程名称
        private Boolean finished;   //完成状态(当前作业形式完成，则该完成)
    }
}
