package com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.app;

import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * \* Created: liuhuichao
 * \* Date: 2019/1/25
 * \* Time: 4:35 PM
 * \* Description: 纸质口语
 * \
 */
@Getter
@Setter
public class OcrDictationTypePart implements Serializable {
    private static final long serialVersionUID = 1976428764395825269L;
    private ObjectiveConfigType type;
    private String typeName;
    private boolean showUrl = false;
    private String url;
    private int tapType = 3;
    private boolean hasFinishUser = false;
    private String subContent;
    private List<OcrDictationType> tabs;

    @Getter
    @Setter
    public static class OcrDictationType implements Serializable{
        private static final long serialVersionUID = 2730577544638691885L;
        private String tabName;
        private List<TabObject> tabs;
    }

    @Getter
    @Setter
    public static class TabObject implements Serializable {
        private static final long serialVersionUID = -8444481682984709990L;
        private String tabName;
        private String tabValue;
        private String subValue;
        private boolean showUrl;
        private String url;
    }

}
