package com.voxlearning.utopia.service.newhomework.api.mapper.baiscreview.report;

import com.voxlearning.alps.annotation.meta.Subject;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedHashMap;

@Getter
@Setter
@EqualsAndHashCode(of = {"clazzGroupId", "studentId"})
@RequiredArgsConstructor
public class BasicReviewHomeworkCacheMapper implements Serializable {
    private static final long serialVersionUID = 7824855403563545433L;
    private Long clazzGroupId;
    private Long studentId;
    private String packageId;
    private String bookId;
    private Subject subject;
    private Long teacherId;
    private Boolean finished;
    private Integer finishPackageCount;

    // 格式<homeworkId, detail>
    private LinkedHashMap<String, BasicReviewHomeworkDetailCacheMapper> homeworkDetail = new LinkedHashMap<>();


}
