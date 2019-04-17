package com.voxlearning.utopia.service.newhomework.api.mapper.baiscreview.report.crm;

import com.voxlearning.alps.annotation.meta.Subject;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
public class PackageHomeworkDetail implements Serializable {
    private static final long serialVersionUID = 5494073052529292750L;
    private String packageId;
    private Subject subject;
    private String subjectName;
    private List<StageDetail> stages = new LinkedList<>();              // 对应的关卡信息

    @Getter
    @Setter
    public static class StageDetail implements Serializable {
        private static final long serialVersionUID = 5118566067274619013L;
        private boolean finished;
        private String homeworkId;
        private String stageName;
        private Long userId;
    }
}
