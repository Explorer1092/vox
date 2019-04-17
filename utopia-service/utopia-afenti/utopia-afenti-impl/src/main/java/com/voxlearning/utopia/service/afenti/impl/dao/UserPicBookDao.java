package com.voxlearning.utopia.service.afenti.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.DynamicCacheDimensionDocumentMongoDao;
import com.voxlearning.alps.dao.mongo.dao.support.MongoConnection;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.service.afenti.api.entity.UserPicBook;
import org.jsoup.helper.Validate;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Named;
import java.util.List;

/**
 * Dao of UserPicBook
 * Created by ganhaitian on 2018/1/22.
 */
@Named
@CacheBean(type = UserPicBook.class,expiration = @UtopiaCacheExpiration(value = UtopiaCacheExpiration.MAX_TTL_IN_SECONDS))
public class UserPicBookDao extends DynamicCacheDimensionDocumentMongoDao<UserPicBook,String> {

    @Override
    protected void afterPropertiesSetCallback() throws Exception {
        super.afterPropertiesSetCallback();

        registerBeforeInsertListener( documents ->
                documents.stream()
                .filter(d -> d.getId() == null)
                .forEach(UserPicBook::generateId)
        );
    }

    @CacheMethod
    public List<UserPicBook> loadUserPicBookList(@CacheParameter("userId") Long userId){
        Criteria criteria = Criteria.where("userId").is(userId);
        return executeQuery(getMongoConnection(userId),Query.query(criteria));
    }

    @Override
    protected String calculateDatabase(String template, UserPicBook document) {
        return null;
    }

    @Override
    protected String calculateCollection(String template, UserPicBook document) {
        Validate.notNull(document);
        Validate.notNull(document.getId());

        String[] idParts = document.getId().split("-");
        Validate.isTrue(idParts.length >= 2);

        Long userId = SafeConverter.toLong(idParts[0]);
        long routeNum = userId % 100;

        return StringUtils.formatMessage(template,routeNum);
    }

    private MongoConnection getMongoConnection(Long userId){
        String mockId = userId + "-0000000";
        return createMongoConnection(calculateIdMongoNamespace(mockId));
    }

    public void upsertUsrPicBook(UserPicBook book){
        if(book == null)
            return;

        UserPicBook modified = super.$upsert(book);

        String cacheKey = CacheKeyGenerator.generateCacheKey(
                UserPicBook.class,
                new String[]{"userId"},
                new Object[]{book.getUserId()});

        getCache().<List<UserPicBook>>createCacheValueModifier()
                .key(cacheKey)
                .expiration(UtopiaCacheExpiration.MAX_TTL_IN_SECONDS)
                .modifier(picBooks -> {
                    // 如果已经存在则覆盖，否则是新增
                    int existIndex = picBooks.indexOf(modified);
                    if(existIndex >= 0){
                        picBooks.set(existIndex,modified);
                    }else
                        picBooks.add(modified);

                    return picBooks;
                })
                .execute();
    }
}
