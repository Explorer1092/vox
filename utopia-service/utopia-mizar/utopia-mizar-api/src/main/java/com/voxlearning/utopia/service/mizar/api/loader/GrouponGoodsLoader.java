package com.voxlearning.utopia.service.mizar.api.loader;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.mizar.api.constants.GrouponGoodsDataSourceType;
import com.voxlearning.utopia.service.mizar.api.entity.groupon.GoodsCategory;
import com.voxlearning.utopia.service.mizar.api.entity.groupon.GrouponGoods;
import com.voxlearning.utopia.service.mizar.api.mapper.GrouponGoodsMapper;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 商品Dao
 *
 * @author xiang.lv
 * @date 2016/9/21   11:32
 */
@ServiceVersion(version = "20160815")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface GrouponGoodsLoader extends IPingable {

    /**
     * @return 商品所有分类列表
     */
    List<GoodsCategory> getAllGoodsCategory();

    /**
     * 根据分类标识,查询分类信息
     *
     * @param categoryCode 分类标识
     * @return 分类信息
     */
    GoodsCategory getGoodsCategoryByCode(final String categoryCode);

    GoodsCategory getGoodsCategoryById(final String id);


    public List<GrouponGoods> loadGroupGoods(Collection<String> goodsIdList);

    List<GrouponGoods> getGrouponGoodsByDataSouce(final GrouponGoodsDataSourceType dataSourceType);

    List<GrouponGoods> getGrouponGoods(String categoryCode);

    List<GrouponGoods> getAllGrouponGoods();

    GrouponGoods getGrouponGoodsById(final String id);

    List<GrouponGoods> loadGroupGoods(final List<String> goodsIdList);

    public List<GrouponGoods> loadRecommendGrouponGoods(final Integer count, final GrouponGoods grouponGoods);

    public List<GrouponGoods> loadGrouponGoodsByCategoryCode(final List<String> codeList);

    PageImpl<GrouponGoodsMapper> getOnlineGrouponGoods(String categoryCode, String orderDimension, final String orderType, final Integer pageSize, final Integer pageNum);

}
