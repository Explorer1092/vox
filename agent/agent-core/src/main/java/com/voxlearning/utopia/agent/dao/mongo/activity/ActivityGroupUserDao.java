package com.voxlearning.utopia.agent.dao.mongo.activity;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.agent.persist.entity.activity.ActivityGroupUser;

import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Named
@CacheBean(type =  ActivityGroupUser.class)
public class ActivityGroupUserDao extends StaticCacheDimensionDocumentMongoDao<ActivityGroupUser, String> {

    @CacheMethod
    public List<ActivityGroupUser> loadByGid(@CacheParameter("gid") String groupId){
        Criteria criteria = Criteria.where("groupId").is(groupId);
        return query(Query.query(criteria));
    }

    @CacheMethod
    public Map<String, List<ActivityGroupUser>> loadByGids(@CacheParameter(value = "gid", multiple = true) Collection<String> groupIds){
        if(CollectionUtils.isEmpty(groupIds)){
            return Collections.emptyMap();
        }
        Criteria criteria = Criteria.where("groupId").in(groupIds);
        return query(Query.query(criteria)).stream().collect(Collectors.groupingBy(ActivityGroupUser::getGroupId));
    }
}
