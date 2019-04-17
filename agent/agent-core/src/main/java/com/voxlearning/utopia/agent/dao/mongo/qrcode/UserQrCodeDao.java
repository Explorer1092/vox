package com.voxlearning.utopia.agent.dao.mongo.qrcode;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.agent.constants.QRCodeBusinessType;
import com.voxlearning.utopia.agent.persist.entity.qrcode.UserQrCode;
import com.voxlearning.utopia.service.reward.api.mapper.CommentEntriesMapper;

import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 *
 * @author song.wang
 * @date 2018/12/17
 */
@Named
@CacheBean(type =  UserQrCode.class)
public class UserQrCodeDao extends StaticCacheDimensionDocumentMongoDao<UserQrCode, String> {

    @CacheMethod
    public UserQrCode loadByTypeAndUser(@CacheParameter("t")QRCodeBusinessType businessType, @CacheParameter("u")Long userId){
        Criteria criteria = Criteria.where("businessType").is(businessType).and("userId").is(userId);
        return query(Query.query(criteria)).stream().findFirst().orElse(null);
    }

    @CacheMethod
    public UserQrCode loadByRelatedId(@CacheParameter("t")QRCodeBusinessType businessType, @CacheParameter("rid")String relatedId){
        Criteria criteria = Criteria.where("businessType").is(businessType).and("relatedId").is(relatedId);
        return query(Query.query(criteria)).stream().findFirst().orElse(null);
    }

    @CacheMethod
    public Map<Long, UserQrCode> loadByUserIds(@CacheParameter("t")QRCodeBusinessType businessType, @CacheParameter(value = "u", multiple = true) Collection<Long> userIds){
        if(CollectionUtils.isEmpty(userIds)){
            return Collections.emptyMap();
        }

        Criteria criteria = Criteria.where("businessType").is(businessType).and("userId").in(userIds);
        return query(Query.query(criteria)).stream().collect(Collectors.toMap(UserQrCode::getUserId, Function.identity(), (o1, o2) -> o1));
    }
}
