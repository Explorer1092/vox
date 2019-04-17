package com.voxlearning.utopia.service.newhomework.api.mapper.report.wordteach;

import com.voxlearning.utopia.service.newhomework.api.constant.WordTeachModuleType;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.pc.QuestionReportDetail;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * @Description: 字词训练-班级数据
 * @author: Mr_VanGogh
 * @date: 2018/12/18 上午10:24
 */
@Getter
@Setter
public class WordExerciseModuleClazzData implements Serializable {
    private static final long serialVersionUID = -401505432547029564L;

    private String moduleName;      //模块名称
    private WordTeachModuleType wordTeachModuleType;
    private List<QuestionReportDetail> questionReportDetails;

}
