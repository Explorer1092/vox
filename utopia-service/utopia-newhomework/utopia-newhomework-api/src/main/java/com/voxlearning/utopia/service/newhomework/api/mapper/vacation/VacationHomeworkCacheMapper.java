package com.voxlearning.utopia.service.newhomework.api.mapper.vacation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;

/**
 * @author xuesong.zhang
 * @since 2016/12/1
 */
@Getter
@Setter
@EqualsAndHashCode(of = {"clazzGroupId", "studentId"})
@RequiredArgsConstructor
public class VacationHomeworkCacheMapper implements Serializable {

    private static final long serialVersionUID = 4644337048116648044L;

    private Long clazzGroupId;
    private Long studentId;
    private NewHomeworkType type;

    private String packageId;
    private String bookId;
    private Subject subject;
    private Long teacherId;
    private Date finishAt;
    private Integer finishPackageCount;

    @Deprecated // 可能没用
    private Boolean unlockAll;

    // 格式<homeworkId, detail>
    private LinkedHashMap<String, VacationHomeworkDetailCacheMapper> homeworkDetail;

    @JsonIgnore
    public boolean isFinished() {
        return finishAt != null;
    }

}
