package com.voxlearning.utopia.service.newhomework.impl.service;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.dao.jdbc.factory.UtopiaSqlFactory;
import com.voxlearning.alps.dao.jdbc.template.UtopiaSql;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.remote.core.support.ValueWrapperFuture;
import com.voxlearning.alps.spi.cache.Cache;
import com.voxlearning.utopia.service.newhomework.api.service.StudentAccomplishmentService;
import com.voxlearning.utopia.service.newhomework.api.entity.StudentHomeworkAccomplishment;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.loader.StudentAccomplishmentLoaderImpl;
import lombok.SneakyThrows;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Objects;

@Named
@ExposeService(interfaceClass = StudentAccomplishmentService.class)
public class StudentAccomplishmentServiceImpl implements StudentAccomplishmentService {

    @Inject private NewHomeworkLoaderImpl newHomeworkLoader;
    @Inject private StudentAccomplishmentLoaderImpl studentAccomplishmentLoader;
    @Inject private UtopiaSqlFactory utopiaSqlFactory;

    @Override
    public AlpsFuture<Boolean> finishedHomeworkWithinToday(Long studentId) {
        if (studentId == null) {
            return new ValueWrapperFuture<>(Boolean.FALSE);
        }
        DayRange today = DayRange.current();
        Cache cache = CacheSystem.CBS.getCache("flushable");
        String cacheKey = CacheKeyGenerator.generateCacheKey(StudentAccomplishmentServiceImpl.class,
                new String[]{"T", "D", "S"},
                new Object[]{"finishedHomeworkWithinToday", today.toString(), studentId});
        if (Objects.equals(cache.load(cacheKey), "1")) {
            return new ValueWrapperFuture<>(Boolean.TRUE);
        }

        boolean ret = false;
        List<StudentHomeworkAccomplishment> accomplishments = studentAccomplishmentLoader
                .findByStudentIdAndAccomplishTime(studentId, today.getStartDate());
        for (StudentHomeworkAccomplishment accomplishment : accomplishments) {
            if (accomplishment.getAccomplishTime() == null) {
                continue;
            }
            NewHomework newHomework = newHomeworkLoader.load(accomplishment.getHomeworkId());
            if (newHomework == null || newHomework.getEndTime() == null) {
                continue;
            }
            if (accomplishment.getAccomplishTime().before(newHomework.getEndTime())) {
                ret = true;
                break;
            }
        }
        if (ret) {
            cache.add(cacheKey, DateUtils.getCurrentToDayEndSecond(), "1");
        }

        return new ValueWrapperFuture<>(ret);
    }

    @Override
    @SneakyThrows(InterruptedException.class)
    public AlpsFuture<Boolean> finishedQuizOrHomeworkWithinToday(Long studentId) {
        if (studentId == null) {
            return new ValueWrapperFuture<>(Boolean.FALSE);
        }
        DayRange today = DayRange.current();
        Cache cache = CacheSystem.CBS.getCache("flushable");
        String cacheKey = CacheKeyGenerator.generateCacheKey(StudentAccomplishmentServiceImpl.class,
                new String[]{"T", "D", "S"},
                new Object[]{"finishedQuizOrHomeworkWithinToday", today.toString(), studentId});
        if (Objects.equals(cache.load(cacheKey), "1")) {
            return new ValueWrapperFuture<>(Boolean.TRUE);
        }

        boolean ret;
        UtopiaSql utopiaSql = utopiaSqlFactory.getDefaultUtopiaSql();
        String sql = "SELECT COUNT(1) FROM VOX_STUDENT_QUIZ_ACCOMPLISHMENT WHERE STUDENT_ID=? AND ACCOMPLISH_TIME>?";
        if (utopiaSql.withSql(sql).useParamsArgs(studentId, today.getStartDate()).queryValue(Integer.class) > 0) {
            ret = true;
        } else {
            ret = finishedHomeworkWithinToday(studentId).get();
        }

        if (ret) {
            cache.add(cacheKey, DateUtils.getCurrentToDayEndSecond(), "1");
        }

        return new ValueWrapperFuture<>(ret);
    }
}
