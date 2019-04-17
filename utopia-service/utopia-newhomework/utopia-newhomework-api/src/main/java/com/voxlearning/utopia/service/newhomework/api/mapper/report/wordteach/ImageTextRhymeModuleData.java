package com.voxlearning.utopia.service.newhomework.api.mapper.report.wordteach;

import com.voxlearning.utopia.service.newhomework.api.constant.WordTeachModuleType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * @Description: 图文入韵模块Data
 * @author: Mr_VanGogh
 * @date: 2018/12/14 下午4:43
 */
@Getter
@Setter
public class ImageTextRhymeModuleData implements Serializable{
    private static final long serialVersionUID = -8573379176479715978L;

    private WordTeachModuleType moduleType;
    private String moduleName;
    private List<ImageTextRhymeData> imageTextRhymeDataList;

    @Getter
    @Setter
    public static class ImageTextRhymeData implements Serializable {
        private static final long serialVersionUID = 5728460062940324166L;

        private String chapterId;       //篇章ID
        private String title;           //篇章名称
        private Double score;           //分数
        private Integer star;           //星级
        private String flashvarsUrl;      //预览地址
    }
}
