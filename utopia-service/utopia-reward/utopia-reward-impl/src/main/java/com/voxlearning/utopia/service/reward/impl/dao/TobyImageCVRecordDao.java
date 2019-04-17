package com.voxlearning.utopia.service.reward.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.service.reward.entity.newversion.TobyImageCVRecord;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

@Named
@CacheBean(type = TobyImageCVRecord.class)
public class TobyImageCVRecordDao extends AlpsStaticMongoDao<TobyImageCVRecord, String> {
    @Override
    protected void calculateCacheDimensions(TobyImageCVRecord document, Collection<String> dimensions) {
        dimensions.add(TobyImageCVRecord.ck_userId(document.getUserId()));
        dimensions.add(TobyImageCVRecord.ck_imageId(document.getImageId()));
    }

    @CacheMethod
    public List<TobyImageCVRecord> loadByUserId(@CacheParameter(value = "USER_ID") long userId) {
        Criteria criteria = Criteria.where("userId").is(userId);
        return query(Query.query(criteria));
    }

    @CacheMethod
    public List<TobyImageCVRecord> loadByImgId(@CacheParameter(value = "IMAGE_ID") Long imageId) {
        Criteria criteria = Criteria.where("imageId").is(imageId);
        return query(Query.query(criteria));
    }

}
