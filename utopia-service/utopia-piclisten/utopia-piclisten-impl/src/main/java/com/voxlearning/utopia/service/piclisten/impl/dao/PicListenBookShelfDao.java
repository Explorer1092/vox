package com.voxlearning.utopia.service.piclisten.impl.dao;

import com.mongodb.client.result.UpdateResult;
import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.service.vendor.api.entity.PicListenBookShelf;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author jiangpeng
 * @since 2017-03-02 下午2:47
 **/
@Named
@CacheBean(type = PicListenBookShelf.class)
public class PicListenBookShelfDao extends AlpsStaticMongoDao<PicListenBookShelf, String> {

    @Override
    protected void calculateCacheDimensions(PicListenBookShelf document, Collection<String> dimensions) {
        dimensions.addAll(Arrays.asList(document.generateCacheDimensions()));
    }

    @CacheMethod(
            type = PicListenBookShelf.class
    )
    public Map<Long, List<PicListenBookShelf>> loadParentPicListenShelves(@CacheParameter(multiple = true, value = "PID") Collection<Long> parentIds){
        if (CollectionUtils.isEmpty(parentIds))
            return Collections.emptyMap();
        Criteria criteria = Criteria.where("parentId").in(parentIds);
        Criteria criteria1 = Criteria.where("disabled").is(false);
        Query query = Query.query(Criteria.and(criteria, criteria1));
        List<PicListenBookShelf> picListenBookShelfs = executeQuery(createMongoConnection(), query);
        return picListenBookShelfs.stream().collect(Collectors.groupingBy(PicListenBookShelf::getParentId));
    }

    public void deletePicListenBookShelf(Long parentId, String bookId){
        if (parentId == null || parentId == 0 || StringUtils.isBlank(bookId))
            return;
        Criteria criteriaParentId = Criteria.where("parentId").is(parentId);
        Criteria criteriaBookId = Criteria.where("bookId").is(bookId);
        Criteria criteriaDisabled = Criteria.where("disabled").is(false);
        Update update = Update.update("disabled", true).set("updateTime", new Date());
        UpdateResult updateResult = updateOne(createMongoConnection(), Criteria.and(criteriaParentId, criteriaBookId, criteriaDisabled), update);
        if (updateResult.getModifiedCount() > 0) {
            getCache().delete(PicListenBookShelf.ck_parentId(parentId));
            getCache().delete(PicListenBookShelf.ck_parentIdWithDisabledCount(parentId));
        }
    }


    public void deletePicListenBooksShelf(Long parentId, List<String> bookIds){
        if (parentId == null || parentId == 0 || bookIds.size()<=0)
            return;
        Criteria criteriaParentId = Criteria.where("parentId").is(parentId);
        Criteria criteriaBookId = Criteria.where("bookId").in(bookIds);

        Criteria criteriaDisabled = Criteria.where("disabled").is(false);
        Update update = Update.update("disabled", true).set("updateTime", new Date());
        UpdateResult updateResult = updateMany(createMongoConnection(), Criteria.and(criteriaParentId, criteriaBookId, criteriaDisabled), update);
        if (updateResult.getModifiedCount() > 0) {
            getCache().delete(PicListenBookShelf.ck_parentId(parentId));
            getCache().delete(PicListenBookShelf.ck_parentIdWithDisabledCount(parentId));
        }
    }

    public void addPicListenBookShelf(Long parentId, String bookId){
        PicListenBookShelf picListenBookShelf = new PicListenBookShelf(parentId, bookId);
        insert(picListenBookShelf);
    }

    @CacheMethod(
            type = Long.class
    )
    public Long countParentShelfBook(@CacheParameter("PIDWD") Long parentId){
        if (parentId == null || parentId == 0)
            return 0L;
        Criteria criteriaParentId = Criteria.where("parentId").is(parentId);
        Query query = Query.query(criteriaParentId);
        return executeCount(createMongoConnection(), query);
    }


}
