package com.voxlearning.utopia.service.newhomework.impl.dao;

import com.mongodb.MongoNamespace;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;
import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.persistence.DynamicMongoShardPersistence;
import com.voxlearning.utopia.service.newhomework.api.entity.NewAccomplishment;

import javax.inject.Named;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;

@Named
@CacheBean(type = NewAccomplishment.class, useValueWrapper = true)
public class NewAccomplishmentShardDao extends DynamicMongoShardPersistence<NewAccomplishment, String> {

    @Override
    protected void calculateCacheDimensions(NewAccomplishment source, Collection<String> dimensions) {
        dimensions.add(NewAccomplishment.ck_id(source.getId()));
    }

    @Override
    protected String calculateDatabase(String template, NewAccomplishment entity) {
        return null;
    }

    @Override
    protected String calculateCollection(String template, NewAccomplishment entity) {
        NewAccomplishment.ID id = entity.parseID();
        return StringUtils.formatMessage(template, id.getMonth());
    }

    /**
     * 学生完成指定的作业
     *
     * @param id             Accomplishment的ID，包含作业详情
     * @param studentId      学号
     * @param accomplishDate 完成时间，如果为null则取当前时间
     * @param ip             学生完成作业时前端送来的IP地址(optional)
     * @param repair         是否补做(optional)
     * @param clientType     端类型，参见HomeworkSourceType
     * @param clientName     端名称
     */
    public void studentFinished(NewAccomplishment.ID id,
                                Long studentId,
                                Date accomplishDate,
                                String ip,
                                Boolean repair,
                                String clientType,
                                String clientName) {
        if (id == null || studentId == null) return;
        if (accomplishDate == null) accomplishDate = new Date();

        Date at = accomplishDate;
        Criteria criteria = Criteria.where("_id").is(id.toString());
        Update update = new Update();
        update = update.set("details." + studentId.toString() + ".accomplishTime", at);
        if (StringUtils.isNotBlank(ip)) {
            update = update.set("details." + studentId.toString() + ".ip", ip);
        }
        if (repair != null) {
            update = update.set("details." + studentId.toString() + ".repair", repair);
        }
        update.set("details." + studentId.toString() + ".clientType", clientType);
        update.set("details." + studentId.toString() + ".clientName", clientName);

        UpdateOptions options = new UpdateOptions().upsert(true);
        MongoNamespace namespace = calculateIdMongoNamespace(id.toString());
        UpdateResult result = $executeUpdateOne(createMongoConnection(namespace, id.toString()), criteria, update, options).getUninterruptibly();
        if (result.getUpsertedId() != null) {
            String ck = NewAccomplishment.ck_id(id.toString());
            getCache().delete(ck);
        } else if (result.getModifiedCount() > 0) {
            getCache().createCacheValueModifier()
                    .key(NewAccomplishment.ck_id(id.toString()))
                    .expiration(getDefaultCacheExpirationInSeconds())
                    .modifier(currentValue -> {
                        if (!(currentValue instanceof NewAccomplishment)) {
                            throw new UnsupportedOperationException();
                        }
                        NewAccomplishment accomplishment = (NewAccomplishment) currentValue;
                        if (accomplishment.getDetails() == null) {
                            accomplishment.setDetails(new LinkedHashMap<>());
                        }
                        NewAccomplishment.Detail detail = accomplishment.getDetails()
                                .computeIfAbsent(studentId.toString(), k -> new NewAccomplishment.Detail());
                        detail.setAccomplishTime(at);
                        if (StringUtils.isNotBlank(ip)) {
                            detail.setIp(ip);
                        }
                        if (repair != null) {
                            detail.setRepair(repair);
                        }
                        detail.setClientType(clientType);
                        detail.setClientName(clientName);
                        return accomplishment;
                    })
                    .execute();
        }
    }
}
