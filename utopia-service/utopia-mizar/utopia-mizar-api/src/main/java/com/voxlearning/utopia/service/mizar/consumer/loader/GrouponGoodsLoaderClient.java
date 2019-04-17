package com.voxlearning.utopia.service.mizar.consumer.loader;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.utopia.service.mizar.api.constants.GrouponGoodsDataSourceType;
import com.voxlearning.utopia.service.mizar.api.entity.groupon.GoodsCategory;
import com.voxlearning.utopia.service.mizar.api.entity.groupon.GrouponGoods;
import com.voxlearning.utopia.service.mizar.api.loader.GrouponGoodsLoader;
import com.voxlearning.utopia.service.mizar.api.mapper.GrouponGoodsMapper;

import java.util.Collection;
import java.util.List;

/**
 * Created by xiang.lv on 2016/9/21.
 * 前台相关接口api
 *
 * @author xiang.lv
 * @date 2016/9/21   11:52
 */
public class GrouponGoodsLoaderClient {

    @ImportService(interfaceClass = GrouponGoodsLoader.class)
    private GrouponGoodsLoader grouponGoodsLoader;

    /**
     * @return 商品所有分类列表
     */
    public List<GoodsCategory> getAllGoodsCategory() {
        return grouponGoodsLoader.getAllGoodsCategory();
    }


    /**
     * 根据  grouponGoods 的标签,随机获取n个商品
     *
     * @param grouponGoods
     * @return
     */
    public List<GrouponGoods> loadRecommendGrouponGoods(final Integer count, final GrouponGoods grouponGoods) {
        return grouponGoodsLoader.loadRecommendGrouponGoods(count, grouponGoods);
    }


    public GoodsCategory getGoodsCategoryById(final String id) {
        return grouponGoodsLoader.getGoodsCategoryById(id);
    }

    public GoodsCategory getGoodsCategoryByCode(final String code) {
        return grouponGoodsLoader.getGoodsCategoryByCode(code);
    }

    public GrouponGoods getGroupGoods(String id) {
        return grouponGoodsLoader.getGrouponGoodsById(id);
    }


    public List<GrouponGoods> loadGroupGoods(Collection<String> goodsIdList) {
        return grouponGoodsLoader.loadGroupGoods(goodsIdList);
    }

    public List<GrouponGoods> getAllGrouponGoods() {
        return grouponGoodsLoader.getAllGrouponGoods();
    }

    public List<GrouponGoods> getGrouponGoods(String categoryCode) {
        return grouponGoodsLoader.getGrouponGoods(categoryCode);
    }

    public List<GrouponGoods> getGrouponGoodsByDataSouce(final GrouponGoodsDataSourceType dataSourceType) {
        return grouponGoodsLoader.getGrouponGoodsByDataSouce(dataSourceType);
    }

    public PageImpl<GrouponGoodsMapper> getOnlineGrouponGoods(String categoryCode, String orderField, final String orderType, final Integer pageSize, final Integer pageNum) {
        return grouponGoodsLoader.getOnlineGrouponGoods(categoryCode, orderField, orderType, pageSize, pageNum);
    }

}
