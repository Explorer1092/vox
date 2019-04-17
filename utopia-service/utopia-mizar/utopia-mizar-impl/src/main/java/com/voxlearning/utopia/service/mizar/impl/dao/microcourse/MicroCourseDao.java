package com.voxlearning.utopia.service.mizar.impl.dao.microcourse;

import com.mongodb.WriteConcern;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.core.helper.StringRegexUtils;
import com.voxlearning.utopia.service.mizar.api.constants.microcourse.MicroCourseStatus;
import com.voxlearning.utopia.service.mizar.api.entity.microcourse.MicroCourse;
import org.bson.BsonDocument;
import org.bson.conversions.Bson;

import javax.inject.Named;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 微课堂-课程 DAO
 * Created by Yuechen.Wang on 2016/12/08.
 */
@Named
@CacheBean(type = MicroCourse.class, useValueWrapper = true)
public class MicroCourseDao extends StaticCacheDimensionDocumentMongoDao<MicroCourse, String> {

    public Page<MicroCourse> findCourseByName(String courseName, MicroCourseStatus status, Pageable pageable) {
        Criteria basic = Criteria.or(Criteria.where("disabled").exists(false), Criteria.where("disabled").is(false));
        Criteria extension = new Criteria();
        courseName = StringRegexUtils.escapeExprSpecialWord(courseName);
        if (StringUtils.isNotBlank(courseName)) {
            extension.and("name").regex(Pattern.compile(".*" + courseName + ".*"));
        }
        if (status != null) {
            extension.and("status").is(status.getOrder());
        }
        Sort sort = new Sort(Sort.Direction.ASC, "status")
                .and(new Sort(Sort.Direction.DESC, "_id"));
        Criteria criteria = Criteria.and(basic, extension);
        Query query = Query.query(criteria);
        return new PageImpl<>(query(query.with(pageable).with(sort)), pageable, count(query));
    }

    @CacheMethod
    public List<MicroCourse> findCourseByCategory(@CacheParameter("CAT") String category) {
        if (StringUtils.isBlank(category)) {
            return Collections.emptyList();
        }
        Criteria criteria = Criteria.where("category").is(category);
        return query(Query.query(criteria));
    }

    public boolean updateStatus(String courseId, MicroCourseStatus status) {
        Criteria criteria = Criteria.where("_id").is(courseId);
        Bson filter = criteriaTranslator.translate(criteria);
        Update update = Update.update("status", status.getOrder())
                .set("updateTime", new Date());
        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions()
                .upsert(false)
                .returnDocument(ReturnDocument.AFTER);
        BsonDocument document = createMongoConnection().collection
                .withWriteConcern(WriteConcern.ACKNOWLEDGED)
                .findOneAndUpdate(filter, updateTranslator.translate(update), options);
        MicroCourse course = convertBsonDocument(document);
        if (course != null) {
            evictDocumentCache(course);
        }
        return course != null && status == MicroCourseStatus.parse(course.getStatus());
    }

    public void disable(String courseId) {
        if (StringUtils.isBlank(courseId)) {
            return;
        }
        MicroCourse course = load(courseId);
        course.setDisabled(true);
        upsert(course);
    }

    public Set<String> loadOnlineCourses() {
        Query query = Query.query(Criteria.where("status").is(MicroCourseStatus.ONLINE.getOrder()));
        query.field().includes("_id");
        return query(query).stream().map(MicroCourse::getId).collect(Collectors.toSet());
    }
}
