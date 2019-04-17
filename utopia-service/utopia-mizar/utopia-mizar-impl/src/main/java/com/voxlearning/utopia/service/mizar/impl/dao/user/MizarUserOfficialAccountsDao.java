package com.voxlearning.utopia.service.mizar.impl.dao.user;

import com.mongodb.WriteConcern;
import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.bson.BsonConverter;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.service.mizar.api.entity.user.MizarUserOfficialAccounts;
import org.bson.BsonDocument;
import org.bson.conversions.Bson;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * MizarUserOfficialAccounts DAO class
 * Created by xiang.lv on 2016/11/04.
 */
@Named
@CacheBean(type = MizarUserOfficialAccounts.class)
public class MizarUserOfficialAccountsDao extends AlpsStaticMongoDao<MizarUserOfficialAccounts, String> {

    @Override
    protected void calculateCacheDimensions(MizarUserOfficialAccounts document, Collection<String> dimensions) {
        dimensions.add(MizarUserOfficialAccounts.ck_user(document.getUserId()));
        dimensions.add(MizarUserOfficialAccounts.ck_accounts_key(document.getAccountsKey()));
    }

    @CacheMethod
    public List<MizarUserOfficialAccounts> loadByUserId(@CacheParameter(value = "uid") String userId) {
        if (StringUtils.isBlank(userId)) {
            return Collections.emptyList();
        }
        Criteria criteria = Criteria.where("userId").is(userId).and("disabled").is(false);
        Query query = Query.query(criteria);
        return query(query).stream().collect(Collectors.toList());
    }

    @CacheMethod
    public List<MizarUserOfficialAccounts> loadByOfficialAccountsKey(@CacheParameter(value = "accounts_key") String officialAccountsKey) {
        if (StringUtils.isBlank(officialAccountsKey)) {
            return Collections.emptyList();
        }
        Criteria criteria = Criteria.where("accountsKey").is(officialAccountsKey).and("disabled").is(false);
        Query query = Query.query(criteria);
        return query(query).stream().collect(Collectors.toList());
    }

    public MizarUserOfficialAccounts disableUserOfficialAccounts(final String userId, final String accountsKey) {
        if(StringUtils.isBlank(userId) ||StringUtils.isBlank(accountsKey)){
                return null;
        }
        Criteria criteria = Criteria.where("userId").is(userId).and("accountsKey").is(accountsKey).and("disabled").is(false);
        Bson filter = criteriaTranslator.translate(criteria);
        Update update = Update.update("disabled", true);
        BsonDocument document = createMongoConnection().collection
                .withWriteConcern(WriteConcern.ACKNOWLEDGED).findOneAndUpdate(filter, updateTranslator.translate(update));
        MizarUserOfficialAccounts userOfficialAccounts = BsonConverter.fromBsonDocument(document, getDocumentClass());
        if (userOfficialAccounts != null) {
            Set<String> cacheKeys = new HashSet<>();
            cacheKeys.add(MizarUserOfficialAccounts.ck_accounts_key(userOfficialAccounts.getAccountsKey()));
            cacheKeys.add(MizarUserOfficialAccounts.ck_user(userOfficialAccounts.getUserId()));
            getCache().delete(cacheKeys);
        }
        return userOfficialAccounts;
    }

}
