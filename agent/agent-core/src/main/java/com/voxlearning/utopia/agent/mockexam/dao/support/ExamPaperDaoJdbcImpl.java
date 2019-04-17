package com.voxlearning.utopia.agent.mockexam.dao.support;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.agent.mockexam.dao.ExamPaperDao;
import com.voxlearning.utopia.agent.mockexam.dao.entity.ExamPaperEntity;
import com.voxlearning.utopia.agent.mockexam.dao.utils.DaoUtil;
import com.voxlearning.utopia.agent.mockexam.service.dto.PageInfo;
import com.voxlearning.utopia.agent.mockexam.service.dto.input.ExamPaperQueryParams;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 考卷持久层mysql实现
 *
 * @author xiaolei.li
 * @version 2018/8/3
 */
@Named
@CacheBean(type = ExamPaperEntity.class)
public class ExamPaperDaoJdbcImpl extends AlpsStaticJdbcDao<ExamPaperEntity, Long> implements ExamPaperDao {

    @Override
    public void insert(ExamPaperEntity entity) {
        super.insert(entity);
    }

    @Override
    public void update(ExamPaperEntity entity) {
        replace(entity);
    }

    @Override
    public ExamPaperEntity findById(Long id) {
        return query(Query.query(Criteria.where("ID").is(id))).stream().findFirst().orElse(null);
    }

    @Override
    @CacheMethod
    public ExamPaperEntity findByPaperId(@CacheParameter("pid")String paperId) {
        return query(Query.query(Criteria.where("PAPER_ID").is(paperId))).stream().findFirst().orElse(null);
    }

    @Override
    @CacheMethod
    public Map<String, ExamPaperEntity> findByPaperId(@CacheParameter(value = "pid", multiple = true) Set<String> paperIds) {
        return query(Query.query(Criteria.where("PAPER_ID").in(paperIds)))
                .stream().collect(Collectors.toMap(ExamPaperEntity::getPaperId, i -> i));
    }

    @Override
    public List<ExamPaperEntity> query(ExamPaperQueryParams params) {
        Criteria criteria = new Criteria();
        DaoUtil.is(criteria, "NAME", params.getPaperName());
        DaoUtil.is(criteria, "REGION", params.getRegionCode());
        DaoUtil.is(criteria, "SUBJECT", params.getSubject());
        DaoUtil.is(criteria, "BOOK_CATALOG_ID", params.getBookId());
        return query(Query.query(criteria));
    }

    @Override
    public List<ExamPaperEntity> query(ExamPaperQueryParams params, PageInfo pageInfo) {
        Criteria criteria = new Criteria();
        Query query = Query.query(criteria);
        criteria.and("PAPER_ID").like(params.getPaperId());
        criteria.and("PAPER_NAME").like(params.getPaperName());
        DaoUtil.is(criteria, "SOURCE", params.getSource());
        DaoUtil.is(criteria, "REGION", params.getRegionCode());
        DaoUtil.is(criteria, "SUBJECT", params.getSubject());
        DaoUtil.is(criteria, "BOOK_ID", params.getBookId());
        DaoUtil.is(criteria, "BOOK_NAME", params.getBookName());
        DaoUtil.is(criteria, "STATUS", params.getStatus());
        if (pageInfo != null) {
            query.limit(pageInfo.getSize()).skip((pageInfo.getPage()) * (pageInfo.getSize()));
        }
        Sort sort = new Sort(Sort.Direction.ASC, "ID");
        query.with(sort);
        return query(query);
    }

    @Override
    public List<ExamPaperEntity> queryAll() {
        return Optional.ofNullable(query()).orElse(new ArrayList<>());
    }

    @Override
    public long count(ExamPaperQueryParams params) {
        Criteria criteria = new Criteria();
        DaoUtil.is(criteria, "PAPER_ID", params.getPaperId());
        DaoUtil.is(criteria, "PAPER_NAME", params.getPaperName());
        DaoUtil.is(criteria, "REGION", params.getRegionCode());
        DaoUtil.is(criteria, "SUBJECT", params.getSubject());
        DaoUtil.is(criteria, "BOOK_ID", params.getBookId());
        DaoUtil.is(criteria, "BOOK_NAME", params.getBookName());
        DaoUtil.is(criteria, "RESOURCE", params.getSource());
        DaoUtil.is(criteria, "STATUS", params.getStatus());
        return count(Query.query(criteria));
    }

    @Override
    protected void calculateCacheDimensions(ExamPaperEntity examPaperEntity, Collection<String> collection) {
        collection.add(ExamPaperEntity.ck_paperId(examPaperEntity.getPaperId()));
    }
}
