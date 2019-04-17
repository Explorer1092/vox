package com.voxlearning.utopia.agent.dao.mongo.activity;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.agent.persist.entity.activity.ActivityCardRedeemCode;

import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Named
@CacheBean(type =  ActivityCardRedeemCode.class)
public class ActivityCardRedeemCodeDao extends StaticCacheDimensionDocumentMongoDao<ActivityCardRedeemCode, String> {

    @CacheMethod
    public ActivityCardRedeemCode loadByRd(@CacheParameter("rd") String redeemCode){
        Criteria criteria = Criteria.where("redeemCode").is(redeemCode);
        return query(Query.query(criteria)).stream().findFirst().orElse(null);
    }

    @CacheMethod
    public Map<String, List<ActivityCardRedeemCode>> loadByRds(@CacheParameter(value = "rd", multiple = true) Collection<String> redeemCodes){
        if(CollectionUtils.isEmpty(redeemCodes)){
            return Collections.emptyMap();
        }
        Criteria criteria = Criteria.where("redeemCode").in(redeemCodes);
        return query(Query.query(criteria)).stream().collect(Collectors.groupingBy(ActivityCardRedeemCode::getRedeemCode));
    }

    @CacheMethod
    public ActivityCardRedeemCode loadByCn(@CacheParameter("cn") String cardNo){
        Criteria criteria = Criteria.where("cardNo").is(cardNo);
        return query(Query.query(criteria)).stream().findFirst().orElse(null);
    }
}
