package com.voxlearning.utopia.service.ai.impl.persistence;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mysql.hql.MySQLInsertOperationHelper;
import com.voxlearning.alps.dao.mysql.persistence.StaticMySQLPersistence;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.ai.constant.WechatUserType;
import com.voxlearning.utopia.service.ai.entity.ChipsWechatUserEntity;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;


@Named
@CacheBean(type = ChipsWechatUserEntity.class)
public class ChipsWechatUserPersistence extends StaticMySQLPersistence<ChipsWechatUserEntity, Long> {

    @Override
    protected void calculateCacheDimensions(ChipsWechatUserEntity document, Collection<String> dimensions) {
        dimensions.add(ChipsWechatUserEntity.ck_openId_type(document.getOpenId(), document.getType()));
        dimensions.add(ChipsWechatUserEntity.ck_id(document.getId()));
        dimensions.add(ChipsWechatUserEntity.ck_user(document.getUserId()));
    }

    @CacheMethod
    public ChipsWechatUserEntity loadByOpenIdAndType(@CacheParameter("O") String openId, @CacheParameter("T") Integer type) {
        Criteria criteria = Criteria.where("OPEN_ID").is(openId).and("TYPE").is(type).and("DISABLED").is(false);
        Sort sort = new Sort(Sort.Direction.DESC, "UPDATETIME");
        Query query = Query.query(criteria).with(sort).limit(1);
        List<ChipsWechatUserEntity> list = query(query);
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }


    @CacheMethod
    public List<ChipsWechatUserEntity> loadByUserId(@CacheParameter("U") Long userId) {
        Criteria criteria = Criteria.where("DISABLED").is(false).and("USER_ID").is(userId);
        return query(Query.query(criteria));
    }
    /**
     * 取每个用户的一条记录
     * @param userIds
     * @return
     */
    @CacheMethod
    public Map<Long, List<ChipsWechatUserEntity>> loadByUserIds(@CacheParameter(value = "U", multiple = true) Collection<Long> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return Collections.emptyMap();
        }
        Criteria criteria = Criteria.where("DISABLED").is(false).and("USER_ID").in(userIds);
        return query(Query.query(criteria)).stream()
                .collect(Collectors.groupingBy(ChipsWechatUserEntity::getUserId));
    }

    public void insertOrUpdate(String openId, WechatUserType type) {
        ChipsWechatUserEntity wechatUserEntity = new ChipsWechatUserEntity();
        wechatUserEntity.setType(type.getCode());
        wechatUserEntity.setOpenId(openId);
        wechatUserEntity.setDisabled(false);
        wechatUserEntity.setCreateTime(new Date());
        wechatUserEntity.setUpdateTime(new Date());
        doInsertOrUpdate(wechatUserEntity);
    }

    public void insertOrUpdate(String openId, WechatUserType type, String nickName, String avatar, Long userId) {
        ChipsWechatUserEntity wechatUserEntity = new ChipsWechatUserEntity();
        wechatUserEntity.setType(type.getCode());
        wechatUserEntity.setOpenId(openId);
        wechatUserEntity.setNickName(nickName);
        wechatUserEntity.setAvatar(avatar);
        wechatUserEntity.setUserId(userId);
        wechatUserEntity.setDisabled(false);
        wechatUserEntity.setCreateTime(new Date());
        wechatUserEntity.setUpdateTime(new Date());
        doInsertOrUpdate(wechatUserEntity);
    }

    public void update(Long id, String nickName, String avatar) {
        Criteria criteria = Criteria.where("ID").is(id);
        Update update = new Update();
        update.set("AVATAR", avatar).set("NICK_NAME", nickName);

        long res = $update(update, criteria);
        if (res > 0) {
            ChipsWechatUserEntity entity = $load(id);
            cleanCache(entity);
        }

    }

    private void doInsertOrUpdate(ChipsWechatUserEntity wechatUserEntity) {
        MySQLInsertOperationHelper insertOperationHelper = new MySQLInsertOperationHelper(getDocumentMapping(), wechatUserEntity, getTableName());
        StringBuilder sql = new StringBuilder(insertOperationHelper.generateSQL());
        sql.append(" ON DUPLICATE KEY UPDATE ").append("`UPDATETIME`=NOW(),`DISABLED`=0");
        if (StringUtils.isNotBlank(wechatUserEntity.getAvatar())) {
            sql.append(", `AVATAR`=").append("\"").append(wechatUserEntity.getAvatar()).append("\"");
        }
        if (StringUtils.isNotBlank(wechatUserEntity.getNickName())) {
            sql.append(", `NICK_NAME`=").append("\"").append(wechatUserEntity.getNickName()).append("\"");
        }
        if (wechatUserEntity.getUserId() != null) {
            sql.append(", `USER_ID`=").append(wechatUserEntity.getUserId());
        }
        int res = getJdbcTemplate().update(sql.toString(), insertOperationHelper.toParams());
        if (res > 0) {
            ChipsWechatUserEntity userEntity = loadByOpenIdAndType(wechatUserEntity.getOpenId(), wechatUserEntity.getType());
            cleanCache(userEntity);
        }
    }

    private void cleanCache(ChipsWechatUserEntity userEntity) {
        if (userEntity == null) {
            return;
        }
        Set<String> cacheKeys = new HashSet<>();
        calculateCacheDimensions(userEntity, cacheKeys);
        getCache().delete(cacheKeys);
    }
}
