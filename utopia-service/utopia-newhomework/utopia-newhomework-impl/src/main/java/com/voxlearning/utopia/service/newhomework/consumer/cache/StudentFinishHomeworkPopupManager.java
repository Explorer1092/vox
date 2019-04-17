package com.voxlearning.utopia.service.newhomework.consumer.cache;

import com.voxlearning.alps.annotation.cache.UtopiaCachePrefix;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.cache.CacheValueModifierExecutor;
import com.voxlearning.alps.spi.cache.ChangeCacheObject;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.service.newhomework.api.mapper.StudentFinishHomeworkPopup;

import java.util.*;

/**
 * 学生完成作业时生成家长端一个弹窗信息的管理
 * 有效期7天
 *
 * @author shiwei.liao
 * @since 2017-3-6
 */
@UtopiaCachePrefix(prefix = "STUDENT_FINISH:POP")
public class StudentFinishHomeworkPopupManager extends PojoCacheObject<String, StudentFinishHomeworkPopup> {


    public StudentFinishHomeworkPopupManager(UtopiaCache cache) {
        super(cache);
    }

    //学生完成作业时记录pop
    public Boolean addStudentFinishHomeworkPopup(Long studentId, Subject subject, String homeworkId, String content) {
        if (studentId == null || studentId == 0 || StringUtils.isBlank(homeworkId) || StringUtils.isBlank(content) || subject == null) {
            return Boolean.FALSE;
        }
        StudentFinishHomeworkPopup popup = new StudentFinishHomeworkPopup();
        popup.setStudentId(studentId);
        popup.setHomeworkId(homeworkId);
        popup.setSubject(subject.name());
        popup.setContent(content);
        popup.setExpireDate(DateUtils.addDays(new Date(), 7));
        popup.setShowedParentIds(new HashSet<>());
        String cacheKey = cacheKey(toCacheKey(studentId, subject.name()));
        //使用unix 时间戳作为过期时间。
        Integer expire = SafeConverter.toInt(popup.getExpireDate().getTime() / 1000);
        return getCache().set(cacheKey, expire, popup);
    }

    //根据学生ID获取一个popup
    public StudentFinishHomeworkPopup loadStudentPopup(Long studentId, Long parentId) {
        if (studentId == null || studentId == 0) {
            return null;
        }
        List<Subject> showPopupSubjectList = new ArrayList<>(Arrays.asList(Subject.ENGLISH, Subject.MATH, Subject.CHINESE));
        for (Subject subject : showPopupSubjectList) {
            String cacheKey = cacheKey(toCacheKey(studentId, subject.name()));
            CacheObject<Object> cacheObject = getCache().get(cacheKey);
            if (cacheObject == null || cacheObject.getValue() == null) {
                continue;
            }
            StudentFinishHomeworkPopup popup = (StudentFinishHomeworkPopup) cacheObject.getValue();
            if (popup == null) {
                continue;
            }
            //作业已检查。这个popup过期。直接删掉
            if (isHomeworkCheck(popup.getHomeworkId())) {
                getCache().delete(cacheKey);
                continue;
            }
            //这个家长已经看过了。忽略
            if (CollectionUtils.isNotEmpty(popup.getShowedParentIds()) && popup.getShowedParentIds().contains(parentId)) {
                continue;
            }
            recordParentForPopup(studentId, subject, parentId);
            return popup;
        }
        return null;
    }

    //判断作业是否已检查
    private Boolean isHomeworkCheck(String homeworkId) {
        if (StringUtils.isBlank(homeworkId)) {
            return Boolean.FALSE;
        }
        CacheObject<Object> cacheObject = getCache().get(cacheKey(homeworkId));
        if (cacheObject != null && cacheObject.getValue() != null) {
            return (Boolean) cacheObject.getValue();
        } else {
            return Boolean.FALSE;
        }
    }

    //记录查看过的家长id
    private void recordParentForPopup(Long studentId, Subject subject, Long parentId) {
        if (studentId == null || studentId == 0 || parentId == null || parentId == 0 || subject == null) {
            return;
        }
        String cacheKey = cacheKey(toCacheKey(studentId, subject.name()));
        CacheObject<StudentFinishHomeworkPopup> cacheObject = getCache().get(cacheKey);
        if (cacheObject != null && cacheObject.getValue() != null) {
            //使用unix 时间戳作为过期时间。所以修改的时候直接再把这个时间拿出来作为过期时间
            Integer expire = SafeConverter.toInt(cacheObject.getValue().getExpireDate().getTime() / 1000);
            CacheValueModifierExecutor<StudentFinishHomeworkPopup> executor = getCache().createCacheValueModifier();
            ChangeCacheObject<StudentFinishHomeworkPopup> modifier = currentValue -> {
                if (currentValue != null) {
                    Set<Long> showedParentIds;
                    if (currentValue.getShowedParentIds() != null) {
                        showedParentIds = new HashSet<>(currentValue.getShowedParentIds());
                    } else {
                        showedParentIds = new HashSet<>();
                    }
                    showedParentIds.add(parentId);
                    currentValue.setShowedParentIds(showedParentIds);
                }
                return currentValue;
            };
            executor.key(cacheKey)
                    .modifier(modifier)
                    .expiration(expire)
                    .execute();
        }

    }

    //记录作业已检查
    public Boolean addHomeworkCheckTag(String homeworkId) {
        if (StringUtils.isBlank(homeworkId)) {
            return Boolean.FALSE;
        }
        return getCache().add(cacheKey(homeworkId), 86400 * 7, Boolean.TRUE);
    }

    private String toCacheKey(Long studentId, String subject) {
        return "SID=" + studentId + ",S=" + subject;
    }

}
