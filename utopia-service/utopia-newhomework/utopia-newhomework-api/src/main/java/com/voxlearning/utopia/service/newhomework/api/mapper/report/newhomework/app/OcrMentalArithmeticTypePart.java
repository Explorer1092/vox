package com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.app;

import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;


@Getter
@Setter
public class OcrMentalArithmeticTypePart  implements Serializable {
    private static final long serialVersionUID = 7988050696983394531L;
    private ObjectiveConfigType type;
    private String typeName;
    private boolean showUrl = false;
    private String url;
    private int tapType = 3;
    private boolean hasFinishUser = false;
    private String subContent;
    private List<OcrMentalType> tabs;

    @Getter
    @Setter
    public static class OcrMentalType implements Serializable {
        private static final long serialVersionUID = -4910379146370570544L;
        private String tabName;
        private List<TabObject> tabs;
    }

    @Getter
    @Setter
    public static class TabObject implements Serializable {
        private static final long serialVersionUID = -5196683017400829221L;
        private String tabName;
        private String tabValue;
        private String subValue;
        private boolean showUrl;
        private String url;
    }
}
