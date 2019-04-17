package com.voxlearning.utopia.service.afenti.impl.dao;

import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.service.afenti.api.entity.PicBookStat;

import javax.inject.Named;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Named
@CacheBean(type = PicBookStat.class,expiration = @UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.this_month))
public class PicBookStatDao extends AlpsStaticMongoDao<PicBookStat,String>{

    @Override
    protected void calculateCacheDimensions(PicBookStat document, Collection<String> dimensions) {

    }

    @CacheMethod(key = "ALL")
    public List<PicBookStat> loadAll(){
        return query();
    }

    public PicBookStat updatePicBookStat(PicBookStat stat){
        PicBookStat modified = upsert(stat);
        updateCache(modified);

        return modified;
    }

    public void incSales(String bookId,int delta){
        Criteria criteria = Criteria.where("bookId").is(bookId);
        Update update = new Update();

        update.set("lastBuyTime", new Date());
        update.inc("sales", delta);

        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions()
                .upsert(true)
                .returnDocument(ReturnDocument.AFTER);

        PicBookStat modified = executeFindOneAndUpdate(createMongoConnection(),criteria,update,options);
        updateCache(modified);
    }

    private void updateCache(PicBookStat modified){
        // 更新缓存
        getCache().<List<PicBookStat>>createCacheValueModifier()
                .key(CacheKeyGenerator.generateCacheKey(PicBookStat.class,"ALL"))
                .expiration(DateUtils.getCurrentToMonthEndSecond())
                .modifier(orgList -> {
                    int existIndex = orgList.indexOf(modified);
                    if(existIndex >= 0)
                        orgList.set(existIndex,modified);
                    else
                        orgList.add(modified);

                    return orgList;
                })
                .execute();
    }
}
