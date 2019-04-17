package com.voxlearning.utopia.service.afenti.base.cache.managers;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.lang.calendar.WeekRange;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import static com.voxlearning.utopia.service.afenti.api.constant.UtopiaAfentiConstants.AVAILABLE_SUBJECT;

/**
 * 记录是否使用过,上周使用过的用户才会弹排行榜总结
 * <p>
 * 比如30013在2016-07-18~2016-07-24日之间使用了阿分题英语，
 * 缓存的key是根据30013，ENGLISH，以及做题日期所在的星期的下个星期一的日期2016-07-25生成的
 * <p>
 * 所以这个缓存就表示了2016-07-25所在星期的上一个星期，30013使用过阿分题英语
 *
 * @author peng.zhang.a
 * @since 16-7-29
 */
public class AfentiLastWeekUsedCacheManager extends PojoCacheObject<AfentiLastWeekUsedCacheManager.StudentWithSubjectAndWeek, String> {

    public AfentiLastWeekUsedCacheManager(UtopiaCache cache) {
        super(cache);
    }

    public void record(Long userId, Subject subject) {
        if (null == userId || !AVAILABLE_SUBJECT.contains(subject)) return;
        WeekRange wr = WeekRange.current().next();
        String date = DateUtils.dateToString(wr.getStartDate(), DateUtils.FORMAT_SQL_DATE);

        set(new StudentWithSubjectAndWeek(userId, subject, date), "dummy");
    }

    public boolean fetch(Long userId, Subject subject) {
        if (null == userId || !AVAILABLE_SUBJECT.contains(subject)) return false;
        WeekRange wr = WeekRange.current();
        String date = DateUtils.dateToString(wr.getStartDate(), DateUtils.FORMAT_SQL_DATE);
        StudentWithSubjectAndWeek ck = new StudentWithSubjectAndWeek(userId, subject, date);
        if (load(ck) != null) {
            // 阅后即焚
            evict(ck);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 测试 随时可以删掉
     */
    public void recordLastWeek(Long userId, Subject subject) {
        if (null == userId || !AVAILABLE_SUBJECT.contains(subject)) return;
        WeekRange wr = WeekRange.current();
        String date = DateUtils.dateToString(wr.getStartDate(), DateUtils.FORMAT_SQL_DATE);
        String key = cacheKey(new StudentWithSubjectAndWeek(userId, subject, date));
        int expirationInSeconds = 120;
        cache.set(key, expirationInSeconds, "dummy");
    }

    @Override
    public int expirationInSeconds() {
        WeekRange wr = WeekRange.current().next();
        Long nowTime = System.currentTimeMillis();
        return (int) (wr.getEndTime() - nowTime) / 1000;

    }

    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(of = {"userId", "subject", "week"})
    public static class StudentWithSubjectAndWeek {
        public Long userId;
        public Subject subject;
        public String week;

        @Override
        public String toString() {
            return "UID=" + userId + ",S=" + subject.name() + ",W=" + week;
        }
    }
}
