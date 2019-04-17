package com.voxlearning.utopia.service.newhomework.impl.dao;

import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.Term;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.mongo.dao.StaticMongoDao;
import com.voxlearning.alps.dao.mongo.mql.Update;
import com.voxlearning.utopia.data.SchoolYear;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.context.AssignmentRecordContext;
import com.voxlearning.utopia.service.newhomework.api.entity.TeacherAssignmentRecord;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author guoqiang.li
 * @since 2016/3/1
 */
@Named
@UtopiaCacheSupport(TeacherAssignmentRecord.class)
@CacheDimension(CacheDimensionDistribution.ID_FIELD)
public class TeacherAssignmentRecordDao extends StaticMongoDao<TeacherAssignmentRecord, String> {
    @Override
    protected void calculateCacheDimensions(TeacherAssignmentRecord source, Collection<String> dimensions) {
        dimensions.add(TeacherAssignmentRecord.ck_id(source.getId()));
    }

    @Override
    protected void preprocessEntity(TeacherAssignmentRecord entity) {
        super.preprocessEntity(entity);
        entity.initializeIfNecessary();
    }

    public void updateTeacherAssignmentRecord(Subject subject, Long userId, String bookId, List<NewHomeworkPracticeContent> practices, Integer clazzGroupSize) {
        String id = "{}-{}-{}-{}-{}";
        SchoolYear schoolYear = SchoolYear.newInstance();
        Integer year = schoolYear.year();
        Term term = schoolYear.currentTerm();
        id = StringUtils.formatMessage(id, year, term, subject, userId, bookId);
        if (CollectionUtils.isNotEmpty(practices)) {
            AssignmentRecordContext assignmentRecordContext = new AssignmentRecordContext(practices);
            Update update = updateBuilder.build();
            update.set("subject", subject);
            update.set("userId", userId);
            update.set("bookId", bookId);
            update.set("term", term);
            update.set("year", year);
            for (Map.Entry<String, Integer> entry : assignmentRecordContext.getQuestionMap().entrySet()) {
                update = update.inc("questionInfo." + entry.getKey(), entry.getValue() * clazzGroupSize);
            }
            for (String packageId : assignmentRecordContext.getPackageSet()) {
                update = update.inc("packageInfo." + packageId, clazzGroupSize);
            }
            for (String paperId : assignmentRecordContext.getPaperSet()) {
                update = update.inc("paperInfo." + paperId, clazzGroupSize);
            }
            for (String appId : assignmentRecordContext.getAppSet()) {
                update = update.inc("appInfo." + appId, clazzGroupSize);
            }
            for (String pictureBookId : assignmentRecordContext.getPictureBookSet()) {
                update = update.inc("pictureBookInfo." + pictureBookId, clazzGroupSize);
            }
            for (String mentalKpId : assignmentRecordContext.getMentalKpIdSet()) {
                update = update.inc("mentalKpInfo." + mentalKpId, clazzGroupSize);
            }
            if (!update.toBsonDocument().isEmpty()) {
                TeacherAssignmentRecord inst = __upsert_OTF(id, update);
                if (inst != null) {
                    String key = TeacherAssignmentRecord.ck_id(id);
                    getCache().getCacheObjectModifier().modify(key, entityCacheExpirationInSeconds(),
                            currentValue -> inst);
                }
            }
        }
    }

    public void updateOutsideReadingTeacherAssignmentRecord(Subject subject, Long userId, List<String> bookIds) {
        String id = "{}-{}-{}-{}-{}";
        SchoolYear schoolYear = SchoolYear.newInstance();
        Integer year = schoolYear.year();
        Term term = schoolYear.currentTerm();
        id = StringUtils.formatMessage(id, year, term, subject, userId, NewHomeworkConstants.OUT_SIDE_READING_DEFAULT_BOOK_ID);
        if (CollectionUtils.isNotEmpty(bookIds)) {
            Update update = updateBuilder.build();
            update.set("subject", subject);
            update.set("userId", userId);
            update.set("bookId", NewHomeworkConstants.OUT_SIDE_READING_DEFAULT_BOOK_ID);
            update.set("term", term);
            update.set("year", year);
            for (String bookId : bookIds) {
                update = update.inc("appInfo." + bookId, 1);
            }
            if (!update.toBsonDocument().isEmpty()) {
                TeacherAssignmentRecord inst = __upsert_OTF(id, update);
                if (inst != null) {
                    String key = TeacherAssignmentRecord.ck_id(id);
                    getCache().getCacheObjectModifier().modify(key, entityCacheExpirationInSeconds(),
                            currentValue -> inst);
                }
            }
        }
    }
}