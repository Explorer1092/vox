package com.voxlearning.utopia.service.newhomework.api.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.core.AbstractDatabaseEntityWithDisabledField;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkType;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkLocation;
import lombok.*;

import java.util.Date;

/**
 * To present when specified student accomplishes homework.
 * move from homework by xuesong.zhang
 *
 * @author Xiaohai Zhang
 * @serial
 * @since 2013-08-06 10:23
 */
@Getter
@Setter
@DocumentTable(table = "VOX_STUDENT_HOMEWORK_ACCOMPLISHMENT")
@DocumentConnection(configName = "homework")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20150611")
@NoArgsConstructor
@RequiredArgsConstructor(staticName = "newInstance")
public class StudentHomeworkAccomplishment extends AbstractDatabaseEntityWithDisabledField {
    private static final long serialVersionUID = -9130299579943002491L;

    @UtopiaSqlColumn(name = "STUDENT_ID") @NonNull private Long studentId;
    @UtopiaSqlColumn(name = "HOMEWORK_ID") @NonNull private String homeworkId;
    @UtopiaSqlColumn(name = "SUBJECT") @NonNull private Subject subject;
    @UtopiaSqlColumn(name = "HOMEWORK_TYPE") @NonNull private HomeworkType homeworkType;
    @UtopiaSqlColumn(name = "ACCOMPLISH_TIME") @NonNull private Date accomplishTime;
    @UtopiaSqlColumn(name = "IP") private String ip;
    @UtopiaSqlColumn(name = "REPAIR") private Boolean repair; // true表示补做

    public static String ck_location(HomeworkLocation location) {
        return CacheKeyGenerator.generateCacheKey(StudentHomeworkAccomplishment.class, "L", location);
    }

    public HomeworkLocation toHomeworkLocation() {
        return HomeworkLocation.newInstance(subject, homeworkId);
    }

    /**
     * Create a mock instance for supporting unit tests.
     */
    public static StudentHomeworkAccomplishment mockInstance() {
        StudentHomeworkAccomplishment inst = new StudentHomeworkAccomplishment();
        inst.homeworkId = "";
        inst.subject = Subject.UNKNOWN;
        inst.studentId = 0L;
        inst.accomplishTime = new Date();
        return inst;
    }
}
