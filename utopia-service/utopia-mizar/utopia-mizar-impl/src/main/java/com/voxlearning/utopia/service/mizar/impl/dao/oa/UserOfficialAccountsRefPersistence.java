package com.voxlearning.utopia.service.mizar.impl.dao.oa;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.jdbc.dao.DynamicCacheDimensionDocumentJdbcDao;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.service.mizar.api.entity.oa.UserOfficialAccountsRef;

import javax.inject.Named;
import java.util.List;
import java.util.Objects;

/**
 * Created by Summer Yang on 2016/10/24.
 */
@Named
@CacheBean(type = UserOfficialAccountsRef.class)
public class UserOfficialAccountsRefPersistence extends DynamicCacheDimensionDocumentJdbcDao<UserOfficialAccountsRef, Long> {

    @Override
    protected String calculateTableName(String template, UserOfficialAccountsRef document) {
        Objects.requireNonNull(document);
        Objects.requireNonNull(document.getUserId());
        long mod;
        if (RuntimeMode.isDevelopment() || RuntimeMode.isTest()) {
            // keep back compatibility
            mod = document.getUserId() % 2;
        } else {
            mod = document.getUserId() % 100;
        }
        return StringUtils.formatMessage(template, mod);
    }

    @CacheMethod
    public List<UserOfficialAccountsRef> loadByUserId(@CacheParameter("userId") Long userId) {
        Criteria criteria = Criteria.where("USER_ID").is(userId);
        UserOfficialAccountsRef mock = new UserOfficialAccountsRef();
        mock.setUserId(userId);
        String tableName = getDocumentTableName(mock);
        return executeQuery(Query.query(criteria), tableName);
    }


    public void updateFollowStatus(UserOfficialAccountsRef ref) {
        if (ref.getId() == null || ref.getUserId() == null) {
            return;
        }
        UserOfficialAccountsRef mock = new UserOfficialAccountsRef();
        mock.setUserId(ref.getUserId());
        String tableName = getDocumentTableName(mock);
        Criteria criteria = Criteria.where("ID").is(ref.getId());
        Update update = Update.update("STATUS", ref.getStatus())
                .set("UPDATE_DATETIME", ref.getUpdateDatetime());
        long rows = executeUpdate(update, criteria, tableName);
        if (rows > 0) {
            evictDocumentCache(ref);
        }
    }
}
