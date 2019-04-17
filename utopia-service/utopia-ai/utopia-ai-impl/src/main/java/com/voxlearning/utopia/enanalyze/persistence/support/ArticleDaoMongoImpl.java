package com.voxlearning.utopia.enanalyze.persistence.support;

import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.voxlearning.alps.api.concurrent.UninterruptiblyFuture;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.persistence.AsyncStaticMongoPersistence;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.enanalyze.entity.ArticleEntity;
import com.voxlearning.utopia.enanalyze.persistence.ArticleDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * 作文持久层mongodb实现
 *
 * @author xiaolei.li
 * @version 2018/7/21
 */
@Slf4j
@Repository
public class ArticleDaoMongoImpl extends AsyncStaticMongoPersistence<ArticleEntity, String>
        implements ArticleDao {

    @Override
    public void update(ArticleEntity entity) {
        Criteria criteria = Criteria.where("_id").is(entity.getId());
        Update update = new Update();
        if (null != entity.getImageUrl())
            update.set("imageUrl", entity.getImageUrl());
        if (null != entity.getNlpResult())
            update.set("nlpResult", entity.getNlpResult());
        if (null != entity.getText())
            update.set("text", entity.getText());
        if (null != entity.getUpdateDate())
            update.set("updateDate", entity.getText());
        if (null != entity.getDisable()) {
            update.set("disable", entity.getDisable());
        }
        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions()
                .upsert(true)
                .returnDocument(ReturnDocument.AFTER);
        $executeFindOneAndUpdate(createMongoConnection(), criteria, update, options).getUninterruptibly();
    }

    @Override
    public List<ArticleEntity> findByOpenId(String openId) {
        Criteria criteria = Criteria.where("openId").is(openId)
                .and("disable").is(false);
        Query query = new Query(criteria);
        try {
            UninterruptiblyFuture<List<ArticleEntity>> future = $executeQuery(createMongoConnection(), query);
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("数据执行错误");
        }
    }

    @Override
    public ArticleEntity findById(String id) {
        Criteria criteria = Criteria.where("_id").is(id);
        Query query = new Query(criteria);
        try {
            UninterruptiblyFuture<List<ArticleEntity>> future = $executeQuery(createMongoConnection(), query);
            List<ArticleEntity> rs = future.get();
            if (null != rs && !rs.isEmpty())
                return rs.get(0);
            else {
                return null;
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("数据执行错误");
        }
    }

    @Override
    public void disable(String id) {
        Criteria criteria = Criteria.where("_id").is(id);
        Update update = new Update();
        update.set("disable", true);
        update.set("updateDate", new Date());
        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions()
                .upsert(true)
                .returnDocument(ReturnDocument.AFTER);
        $executeFindOneAndUpdate(createMongoConnection(), criteria, update, options).getUninterruptibly();
    }

    @Override
    public ArticleEntity findLast(String openId) {
        Criteria criteria = Criteria.where("openId").is(openId)
                .and("disable").is(false);
        Sort sort = new Sort(new Sort.Order(Sort.Direction.DESC, "updateDate"));
        Query query = new Query(criteria).with(sort);
        try {
            UninterruptiblyFuture<List<ArticleEntity>> future = $executeQuery(createMongoConnection(), query);
            List<ArticleEntity> rs = future.get();
            if (null != rs && !rs.isEmpty())
                return rs.get(0);
            else {
                return null;
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("数据执行错误");
        }
    }

    @Override
    public Long count(String openId) {
        Criteria criteria = Criteria.where("openId").is(openId)
                .and("disable").is(false);
        Query query = new Query(criteria);
        try {
            UninterruptiblyFuture<Long> future = $executeCount(createMongoConnection(), query);
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("数据执行错误");
        }
    }

    @Override
    protected void calculateCacheDimensions(ArticleEntity document, Collection<String> dimensions) {

    }
}
