package com.voxlearning.utopia.service.reward.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.utopia.service.reward.entity.RewardActivity;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * 奖品中心活动表数据持久层实现
 * Created by haitian.gan on 2017/2/4.
 */
@Named
@CacheBean(type = RewardActivity.class)
public class RewardActivityDao extends AlpsStaticJdbcDao<RewardActivity,Long>{

    @Inject
    private RewardActivityVersion rewardActivityVersion;

    @Override
    protected void calculateCacheDimensions(RewardActivity document, Collection<String> dimensions) {
        dimensions.addAll(Arrays.asList(document.generateCacheDimensions()));
    }

    public List<RewardActivity> findAll(){
        return query();
    }

    @Override
    public void insert(RewardActivity document) {
        rewardActivityVersion.increment();
        super.insert(document);
    }

    @Override
    public RewardActivity upsert(RewardActivity document) {
        rewardActivityVersion.increment();
        return super.upsert(document);
    }

    /**
     * 更新活动的进度数据
     * @param id 活动id
     * @param deltaNums 参加人数
     * @param deltaMoney 筹款金额
     * @return
     */
    public int updateProgressData(Long id,int deltaNums,int deltaMoney){
        RewardActivity original = $load(id);
        if(original == null){
            return 0;
        }

        if(deltaMoney <= 0 || deltaNums <= 0)
            return 0;

        Criteria criteria = Criteria.where("ID").is(id);
        Update update =  new Update()
                .inc("PARTAKE_NUMS",deltaNums)
                .inc("RAISED_MONEY",deltaMoney);

        int rows = (int)$update(update,criteria);
        return rows;
    }

}
