package com.voxlearning.utopia.service.mizar.impl.dao.groupon;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.service.mizar.api.entity.groupon.GoodsCategory;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * Created by xiang.lv on 2016/9/21.
 *
 * @author xiang.lv
 * @date 2016/9/21   11:52
 */
@Named
@CacheBean(type = GoodsCategory.class)
public class GoodsCategoryDao extends AlpsStaticMongoDao<GoodsCategory, String> {

    @Override
    protected void calculateCacheDimensions(GoodsCategory document, Collection<String> dimensions) {
        dimensions.add(GoodsCategory.ck_all());
    }

    @CacheMethod(key = "ALL")
    public List<GoodsCategory> getAllGoodsCategory() {
        return query();
    }


}
