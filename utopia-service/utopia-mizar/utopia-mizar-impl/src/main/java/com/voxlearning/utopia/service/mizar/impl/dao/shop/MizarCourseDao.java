package com.voxlearning.utopia.service.mizar.impl.dao.shop;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.core.helper.StringRegexUtils;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarCourse;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by Summer Yang on 2016/9/21.
 */
@Named
@CacheBean(type = MizarCourse.class)
public class MizarCourseDao extends AlpsStaticMongoDao<MizarCourse, String> {

    @Override
    protected void calculateCacheDimensions(MizarCourse document, Collection<String> dimensions) {
        dimensions.add(MizarCourse.ck_id(document.getId()));
        dimensions.add(MizarCourse.ck_all());
    }

    public Page<MizarCourse> loadPageByParams(String category, String title, String status, Pageable page) {
        Criteria criteria = new Criteria();
        if (StringUtils.isNotBlank(title)) {
            title = StringRegexUtils.escapeExprSpecialWord(title);
            criteria.and("title").regex(Pattern.compile(".*" + title + ".*"));
        }
        if (StringUtils.isNotBlank(category)) {
            criteria.and("category").is(category);
        }
        if (StringUtils.isNotBlank(status)) {
            criteria.and("status").is(status);
        }
        Query query = Query.query(criteria);
        Sort sort = new Sort(Sort.Direction.DESC, "createAt")
                .and(new Sort(Sort.Direction.DESC, "priority"));
        return new PageImpl<>(query(query.with(page).with(sort)), page, count(query));
    }

    @CacheMethod(key = "All")
    public List<MizarCourse> loadAll() {
        return query();
    }

}
