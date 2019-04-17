package com.voxlearning.utopia.service.zone.impl.persistence.boss;

import com.mongodb.MongoNamespace;
import com.mongodb.async.client.MongoClient;
import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.client.MongoShardClientPool;
import com.voxlearning.alps.dao.mongo.client.MongoShardClientPoolManager;
import com.voxlearning.alps.dao.mongo.connection.IMongoConnection;
import com.voxlearning.alps.dao.mongo.hash.MongoShardCalculator;
import com.voxlearning.alps.dao.mongo.hash.MongoShardCalculatorFactory;
import com.voxlearning.alps.dao.mongo.persistence.StaticMongoShardPersistence;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.zone.api.entity.boss.ClazzBossAward;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author : kai.sun
 * @version : 2018-11-05
 * @description :
 **/

@Repository
@CacheBean(type = ClazzBossAward.class,cacheName = "columb-zone-cache",useValueWrapper = true)
@CacheDimension(CacheDimensionDistribution.ID_AND_OTHER_FIELDS)
public class ClazzBossAwardPersistence extends StaticMongoShardPersistence<ClazzBossAward,String> {

    private static final String ID_SEP = "_";

    @Override
    protected void calculateCacheDimensions(ClazzBossAward document, Collection<String> dimensions) {
        dimensions.add(ClazzBossAward.ck_id(document.getId()));
        dimensions.add(ClazzBossAward.ck_clazzBossAwardList());
        dimensions.add(ClazzBossAward.ck_clazzBossAwardList(SafeConverter.toInt(document.getId().split("_")[0])));
        dimensions.add(ClazzBossAward.ck_clazzBossAwardList(SafeConverter.toInt(document.getId().split("_")[0]),document.getSelfOrClazz()));
    }

    public boolean deleteById(String id){
        return remove(id);
    }

//    @CacheMethod(key = "clazzBossAwardList")
//    public List<ClazzBossAward> getList(){
//        return query();
//    }

    @CacheMethod
    public List<ClazzBossAward> getList(@CacheParameter("activityId") Integer activityId){
        Pattern pattern = Pattern.compile("^" + activityId + ID_SEP);
        Criteria criteria = Criteria.where("_id").regex(pattern);
        List<ClazzBossAward> list = query(new Query(criteria));
        list.sort(Comparator.comparing(ClazzBossAward::getType));
        return list;
    }

    @CacheMethod
    public List<ClazzBossAward> getList(@CacheParameter("activityId") Integer activityId,@CacheParameter("selfOrClazz") Integer selfOrClazz){
        Pattern pattern = Pattern.compile("^" + activityId + ID_SEP+selfOrClazz+ID_SEP);
        Criteria criteria = Criteria.where("_id").regex(pattern);
        List<ClazzBossAward> list = query(new Query(criteria));
        list.sort(Comparator.comparing(ClazzBossAward::getType));
        return list;
    }

    @Override
    protected MongoShardCalculator getShardCalculator(int shardSize) {
        return new MongoShardCalculator() {
            @Override
            public int calculate(Object id) {
                String[] idParts = StringUtils.split(SafeConverter.toString(id), "_");
                if(idParts.length==0){
                    return MongoShardCalculatorFactory.getInstance().getCalculator(shardSize).calculate(id);
                }else{
                    return MongoShardCalculatorFactory.getInstance().getCalculator(shardSize).calculate(idParts[0]);
                }
            }

            @Override
            public void close() {
            }
        };
    }

    public ClazzBossAward detail(String id){
        return load(id);
    }

    /**更新或者插入*/
    public ClazzBossAward updateOrInsert(ClazzBossAward clazzBossAward){
        if(clazzBossAward==null) return null;
        if(clazzBossAward.getId()==null) {
            if(clazzBossAward.getActivityId()==null||clazzBossAward.getSelfOrClazz()==null || clazzBossAward.getType()==null) return null;
            clazzBossAward.generateId(); //生成主键id
        }
        ClazzBossAward result = upsert(clazzBossAward);
        if(result!=null){
            getCache().safeAdd(ClazzBossAward.ck_id(result.getId()), getDefaultCacheExpirationInSeconds(), result); //增加 id -> cache
        }
        return result;
    }

}
