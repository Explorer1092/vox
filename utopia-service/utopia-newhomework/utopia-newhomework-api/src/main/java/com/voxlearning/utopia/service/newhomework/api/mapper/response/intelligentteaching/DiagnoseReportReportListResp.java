package com.voxlearning.utopia.service.newhomework.api.mapper.response.intelligentteaching;

import com.voxlearning.utopia.service.newhomework.api.mapper.response.base.BaseResp;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * @author majianxin
 * @version V1.0
 * @date 2018/6/29
 */
@Getter
@Setter
public class DiagnoseReportReportListResp extends BaseResp {
    private static final long serialVersionUID = 8815585751542500374L;
    private String reportTitle;//报告名称
    private List<ConfigType> configTypes = new LinkedList<>();//作业形式list

    @Getter
    @Setter
    @AllArgsConstructor
    public static class ConfigType implements Serializable {
        private static final long serialVersionUID = -8408670529342007465L;
        private String objectiveConfigType;//作业形式
        private String objectiveConfigTypeName;//作业形式名称
        private Long avgScore;//作业形式平均分
        private List<QuestionBox> questionBoxes;//题包
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class QuestionBox implements Serializable {
        private static final long serialVersionUID = 1070404246555873340L;
        private String questionBoxId;//题包ID
        private String questionBoxName;//题包名
        private String detailUrl;//报告详情url
    }
}
