package com.voxlearning.utopia.service.reward.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.utopia.service.reward.entity.RewardProductTarget;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Dao of {@link com.voxlearning.utopia.service.reward.entity.RewardProductTarget}
 * Created by haitian.gan on 2017/3/23.
 */
@Named
@CacheBean(type = RewardProductTarget.class)
public class RewardProductTargetDao extends AlpsStaticJdbcDao<RewardProductTarget,Long>{

    @Inject
    private RewardProductTargetVersion productTargetVersion;

    @Override
    protected void calculateCacheDimensions(RewardProductTarget document, Collection<String> dimensions) {
        dimensions.add(RewardProductTarget.ck_productId(document.getProductId()));
        dimensions.add(RewardProductTarget.ck_targetPid(document.getProductId()));
        dimensions.add(RewardProductTarget.ck_allGroupByPid());
    }

    @Override
    public void insert(RewardProductTarget document) {
        super.insert(document);
        productTargetVersion.increment();
    }

    @Override
    public void inserts(Collection<RewardProductTarget> documents) {
        super.inserts(documents);
        productTargetVersion.increment();
    }

    @Override
    public RewardProductTarget upsert(RewardProductTarget document) {
        RewardProductTarget upsert = super.upsert(document);
        productTargetVersion.increment();
        return upsert;
    }

    @CacheMethod
    public List<RewardProductTarget> findByProductId(@CacheParameter("PRODUCT_ID")Long productId){
        Criteria criteria = Criteria.where("PRODUCT_ID").is(productId).and("DISABLED").is(false);
        return query(Query.query(criteria));
    }

    public List<RewardProductTarget> loadAll() {
        Criteria criteria = Criteria.where("DISABLED").is(false);
        return query(Query.query(criteria));
    }

    @CacheMethod(key = "ALL_GROUPBY_PID")
    public Map<Long,List<RewardProductTarget>> findAllGrupByProductId(){
        Criteria criteria = Criteria.where("DISABLED").is(false);
        return query(Query.query(criteria)).stream().collect(
                Collectors.groupingBy(
                        RewardProductTarget::getProductId,
                        Collectors.toList()
                ));
    }

    public int clearProductTarget(Long productId,Integer type){

        Update update = Update.update("DISABLED",true);
        Criteria criteria = Criteria.where("PRODUCT_ID").is(productId).and("TARGET_TYPE").is(type);

        int rows = (int) $update(update,criteria);
        if(rows > 0){
            RewardProductTarget target = new RewardProductTarget();
            target.setProductId(productId);

            Set<String> keys = new HashSet<>();
            calculateCacheDimensions(target, keys);

            getCache().delete(keys);
            productTargetVersion.increment();
        }

        return rows;
    }

    public int disable(Long id){
        RewardProductTarget original = $load(id);
        if(original == null){
            return 0;
        }

        Update update = Update.update("DISABLED",true);
        Criteria criteria = Criteria.where("ID").is(id);
        int rows = (int)$update(update,criteria);
        if(rows > 0){
            Set<String> keys = new HashSet<>();
            calculateCacheDimensions(original, keys);

            getCache().delete(keys);
            productTargetVersion.increment();
        }
        return rows;
    }
}
