package com.voxlearning.utopia.service.newhomework.impl.dao.ocr;

import com.mongodb.MongoNamespace;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.annotation.meta.Term;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.persistence.DynamicMongoShardPersistence;
import com.voxlearning.utopia.data.SchoolYear;
import com.voxlearning.utopia.service.newhomework.api.entity.ocr.IndependentOcrProcessResult;

import javax.inject.Named;
import java.util.*;

@Named
@CacheBean(type = IndependentOcrProcessResult.class, cacheName = "utopia-homework-cache")
@CacheDimension(CacheDimensionDistribution.ID_AND_OTHER_FIELDS)
public class IndependentOcrProcessResultDao extends DynamicMongoShardPersistence<IndependentOcrProcessResult, String> {

    @Override
    protected String calculateDatabase(String template, IndependentOcrProcessResult document) {
        return calculateDatabaseTerm(template);
    }

    @Override
    protected String calculateCollection(String template, IndependentOcrProcessResult document) {
        String[] idSplit = document.getId().split("-");
        if (idSplit.length != 3) {
            throw new IllegalArgumentException("IndependentOcrProcessResult.id is illegal.");
        }
        Long studentId = Long.parseLong(idSplit[2]);
        Objects.requireNonNull(studentId);
        return calculateCollectionByStudentId(template, studentId);
    }

    @Override
    protected void calculateCacheDimensions(IndependentOcrProcessResult document, Collection<String> dimensions) {
        dimensions.add(IndependentOcrProcessResult.ck_id(document.getId()));
        dimensions.add(IndependentOcrProcessResult.ck_studentId(document.getStudentId()));
        dimensions.add(IndependentOcrProcessResult.ck_ImgUrl(document.getOcrMentalImageDetail().getImg_url()));
    }

    private String calculateDatabaseTerm(String template) {
        SchoolYear schoolYear = SchoolYear.newInstance();
        Term term = schoolYear.currentTerm();
        return StringUtils.formatMessage(template, schoolYear.year(), term.getKey());
    }

    private String calculateCollectionByStudentId(String template, Long studentId) {
        if (studentId == null || studentId < 0) {
            throw new RuntimeException("studentId is illegal");
        }

        long studentIdEndNum = studentId % 10;
        return StringUtils.formatMessage(template, studentIdEndNum);
    }

    @CacheMethod
    public List<IndependentOcrProcessResult> loadByStudentId(@CacheParameter(value = "studentId") Long studentId) {
        Criteria criteria = Criteria.where("studentId").is(studentId).and("disabled").is(Boolean.FALSE);
        Query query = Query.query(criteria);
        String databaseName = calculateDatabaseTerm(getMongoDocument().getDatabaseName());
        String collectionName = calculateCollectionByStudentId(getMongoDocument().getCollectionName(), studentId);
        MongoNamespace mongoNamespace = new MongoNamespace(databaseName, collectionName);
        return $executeQuery(createMongoConnection(mongoNamespace), query)
                .getUninterruptibly();
    }


    @CacheMethod
    public IndependentOcrProcessResult loadByImageUrl(@CacheParameter(value = "img_url") String imgUrl, Long studentId) {
        Criteria criteria = Criteria.where("ocrMentalImageDetail.img_url").is(imgUrl).and("disabled").is(Boolean.FALSE);
        Query query = Query.query(criteria);
        String databaseName = calculateDatabaseTerm(getMongoDocument().getDatabaseName());
        String collectionName = calculateCollectionByStudentId(getMongoDocument().getCollectionName(), studentId);
        MongoNamespace mongoNamespace = new MongoNamespace(databaseName, collectionName);
        return $executeQuery(createMongoConnection(mongoNamespace), query)
                .getUninterruptibly().stream().findFirst().orElse(null);
    }

    // 只用于家长通同步删除, 故不做方法缓存
    public int deleteByImageUrls(Long studentId, List<String> imgUrls) {
        Criteria criteria = Criteria.where("ocrMentalImageDetail.img_url").in(imgUrls).and("disabled").is(Boolean.FALSE);
        Query query = Query.query(criteria);
        String databaseName = calculateDatabaseTerm(getMongoDocument().getDatabaseName());
        String collectionName = calculateCollectionByStudentId(getMongoDocument().getCollectionName(), studentId);
        MongoNamespace mongoNamespace = new MongoNamespace(databaseName, collectionName);
        List<IndependentOcrProcessResult> processResults = $executeQuery(createMongoConnection(mongoNamespace), query)
                .getUninterruptibly();
        processResults.stream().peek(process -> process.setDisabled(true)).forEach(this::upsert);
        return processResults.size();
    }

    /**
     * 批量删除
     */
    public List<String> deleteProcessResults(Collection<String> processIds, Long studentId) {
        Update update = new Update();
        update.set("disabled", true);
        update.set("updateAt", new Date());
        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions()
                .returnDocument(ReturnDocument.AFTER);

        List<String> imgUrls = new ArrayList<>();
        // 暂且循环更新.
        for (String processId : processIds) {
            Criteria criteria = Criteria.where("_id").is(processId);
            IndependentOcrProcessResult modified = this.$executeFindOneAndUpdate(createMongoConnection(calculateIdMongoNamespace(processId), processId), criteria, update, options).getUninterruptibly();
            if (modified != null) {
                if (modified.getOcrMentalImageDetail() != null) {
                    imgUrls.add(modified.getOcrMentalImageDetail().getImg_url());
                }
                getCache().createCacheValueModifier()
                        .key(IndependentOcrProcessResult.ck_id(modified.getId()))
                        .expiration(getDefaultCacheExpirationInSeconds())
                        .modifier(currentValue -> modified)
                        .execute();
                getCache().delete(IndependentOcrProcessResult.ck_studentId(modified.getStudentId()));
                getCache().delete(IndependentOcrProcessResult.ck_ImgUrl(modified.getOcrMentalImageDetail().getImg_url()));
            }
        }
        return imgUrls;
    }

}
