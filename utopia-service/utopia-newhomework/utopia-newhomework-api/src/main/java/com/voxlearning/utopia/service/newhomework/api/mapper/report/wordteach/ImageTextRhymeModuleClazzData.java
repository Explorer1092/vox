package com.voxlearning.utopia.service.newhomework.api.mapper.report.wordteach;

import com.voxlearning.utopia.service.newhomework.api.constant.WordTeachModuleType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * @Description: 图文入韵-班级数据
 * @author: Mr_VanGogh
 * @date: 2018/12/18 上午10:25
 */
@Getter
@Setter
public class ImageTextRhymeModuleClazzData implements Serializable {
    private static final long serialVersionUID = -3547412040404055795L;

    private WordTeachModuleType wordTeachModuleType;
    private String moduleName;      //模块名称
    private List<ImageTextRhymeChapterData> imageTextRhymeChapterDatas;

    @Getter
    @Setter
    public static class ImageTextRhymeChapterData implements Serializable {
        private static final long serialVersionUID = 7375684256535564213L;
        private String chapterId;       //篇章ID
        private String title;           //名称
        private Integer finishNum;      //完成人数
        private List<StudentHomeworkData> studentHomeworkDatas;  //学生答题详情
    }
}
