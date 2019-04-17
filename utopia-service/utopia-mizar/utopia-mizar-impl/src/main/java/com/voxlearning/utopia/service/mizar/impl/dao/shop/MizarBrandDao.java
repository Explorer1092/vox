package com.voxlearning.utopia.service.mizar.impl.dao.shop;

import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.core.helper.StringRegexUtils;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarBrand;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by Summer Yang on 2016/8/15.
 */
@Named
@UtopiaCacheSupport(MizarBrand.class)
@CacheDimension(CacheDimensionDistribution.ID_FIELD)
public class MizarBrandDao extends AlpsStaticMongoDao<MizarBrand, String> {

    @Override
    protected void calculateCacheDimensions(MizarBrand document, Collection<String> dimensions) {
        dimensions.add(MizarBrand.ck_id(document.getId()));
    }

    public Page<MizarBrand> loadByPage(Pageable page, String brandName) {
        Criteria criteria = new Criteria();
        brandName = StringRegexUtils.escapeExprSpecialWord(brandName);
        if (StringUtils.isNotBlank(brandName)) {
            criteria = Criteria.where("brand_name").regex(Pattern.compile(".*" + brandName + ".*"));
        }
        Query query = Query.query(criteria);
        return new PageImpl<>(query(query.with(page)), page, count(query));
    }

    /**
     * 查询显示在品牌馆的品牌列表
     * @return 品牌列表
     */
    public List<MizarBrand> loadBrandHall(){
        Criteria criteria = Criteria.where("show_list").is(true);
        return query(Query.query(criteria));
    }

    // 获取全部ID 任务执行调用
    public Set<String> loadAllBrandIds() {
        Query query = Query.query(new Criteria());
        query.field().includes("_id");
        return query(query).stream().map(MizarBrand::getId).collect(Collectors.toSet());
    }
}
