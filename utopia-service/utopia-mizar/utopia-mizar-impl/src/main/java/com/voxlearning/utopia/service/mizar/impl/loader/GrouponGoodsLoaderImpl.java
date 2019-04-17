package com.voxlearning.utopia.service.mizar.impl.loader;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.utopia.service.mizar.api.constants.GrouponGoodsDataSourceType;
import com.voxlearning.utopia.service.mizar.api.entity.groupon.GoodsCategory;
import com.voxlearning.utopia.service.mizar.api.entity.groupon.GrouponGoods;
import com.voxlearning.utopia.service.mizar.api.loader.GrouponGoodsLoader;
import com.voxlearning.utopia.service.mizar.api.mapper.GrouponGoodsMapper;
import com.voxlearning.utopia.service.mizar.api.utils.GrouponGoodsUtil;
import com.voxlearning.utopia.service.mizar.impl.dao.groupon.GoodsCategoryDao;
import com.voxlearning.utopia.service.mizar.impl.dao.groupon.GrouponGoodsDao;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.service.mizar.api.constants.GrouponGoodStatusType.ONLINE;

/**
 * Created by xiang.lv on 2016/9/21.
 *
 * @author xiang.lv
 * @date 2016/9/21   11:48
 */
@Named
@Service(interfaceClass = GrouponGoodsLoader.class)
@ExposeService(interfaceClass = GrouponGoodsLoader.class)
public class GrouponGoodsLoaderImpl extends SpringContainerSupport implements GrouponGoodsLoader {
    @Inject
    private GoodsCategoryDao goodsCategoryDao;

    @Inject
    private GrouponGoodsDao grouponGoodsDao;

    public GoodsCategory getGoodsCategoryByCode(final String categoryCode) {
        if (StringUtils.isBlank(categoryCode)) {
            return null;
        }
        List<GoodsCategory> goodsCategoryList = getAllGoodsCategory();
        if (CollectionUtils.isEmpty(goodsCategoryList)) {
            return null;
        }
        return goodsCategoryList.stream().filter(c -> StringUtils.equals(categoryCode, c.getCategoryCode())).findFirst().orElse(null);
    }

    public GoodsCategory getGoodsCategoryById(final String id) {
        if (StringUtils.isBlank(id)) {
            return null;
        }
        List<GoodsCategory> goodsCategoryList = getAllGoodsCategory();
        if (CollectionUtils.isEmpty(goodsCategoryList)) {
            return null;
        }
        return goodsCategoryList.stream().filter(c -> StringUtils.equals(id, c.getId())).findFirst().orElse(null);
    }

    /**
     * @return 商品所有分类列表
     */
    @Override
    public List<GoodsCategory> getAllGoodsCategory() {
        List<GoodsCategory> goodsCategoryList = goodsCategoryDao.getAllGoodsCategory();
        if (CollectionUtils.isEmpty(goodsCategoryList)) {
            return Collections.emptyList();
        }
        Collections.sort(goodsCategoryList, (o1, o2) -> {
            if (null != o2.getOrderIndex() && null != o1.getOrderIndex()) {
                return o2.getOrderIndex().compareTo(o1.getOrderIndex());
            }
            return 0;
        });
        return goodsCategoryList;
    }

    public List<GrouponGoods> getAllGrouponGoods() {
        List<GoodsCategory> goodsCategoryList = goodsCategoryDao.getAllGoodsCategory();
        if (CollectionUtils.isEmpty(goodsCategoryList)) {
            return Collections.emptyList();
        }
        List<String> categoryCodeList = goodsCategoryList.stream().map(GoodsCategory::getCategoryCode).collect(Collectors.toList());
        return loadGrouponGoodsByCategoryCode(categoryCodeList);
    }

    public List<GrouponGoods> getGrouponGoods(String categoryCode) {
        if (StringUtils.isBlank(categoryCode)) {
            return Collections.emptyList();
        } else {
            return grouponGoodsDao.loadByCategory(categoryCode);
        }
    }

    @Override
    public List<GrouponGoods> loadGrouponGoodsByCategoryCode(final List<String> categoryCodeList) {
        if (CollectionUtils.isEmpty(categoryCodeList)) {
            return Collections.emptyList();
        }
        Map<String, List<GrouponGoods>> mapList = grouponGoodsDao.loadByCategory(categoryCodeList);
        if (null == mapList || mapList.size() == 0) {
            return Collections.emptyList();
        }
        return mapList.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
    }

    @Override
    public GrouponGoods getGrouponGoodsById(String id) {
        if (StringUtils.isBlank(id)) {
            return null;
        }
        return grouponGoodsDao.load(id);
    }

    public List<GrouponGoods> loadGroupGoods(final List<String> goodsIdList) {
        Map<String, GrouponGoods> listMap = grouponGoodsDao.loads(goodsIdList);
        return listMap.entrySet().stream().map(o -> o.getValue()).collect(Collectors.toList());
    }

    private List<GrouponGoods> getMatchedGrouponGoods(final String goodsTag, List<GrouponGoods> grouponGoodsList) {
        if (StringUtils.isBlank(goodsTag)) {
            return grouponGoodsList;
        }
        String[] tags = goodsTag.split(",");
        List<String> goodsTagList = Arrays.asList(goodsTag.split(","));
        if (CollectionUtils.isNotEmpty(goodsTagList)) {
            grouponGoodsList = grouponGoodsList.stream().filter(o -> matched(goodsTagList, o)).collect(Collectors.toList());
        }
        return grouponGoodsList;
    }

    private boolean matched(final List<String> goodsTagList, GrouponGoods grouponGoods) {
        if (Objects.isNull(grouponGoods.getGoodsTag())) {
            return false;
        }
        if (StringUtils.isNotBlank(grouponGoods.getGoodsTag())) {
            List<String> tagList = new ArrayList<>(Arrays.asList(grouponGoods.getGoodsTag().split(",")));
            tagList.retainAll(goodsTagList);//取交集
            return CollectionUtils.isNotEmpty(tagList);
        }
        return false;
    }

    public List<GrouponGoods> loadRecommendGrouponGoods(final Integer count, final GrouponGoods grouponGoods) {
        List<GrouponGoods> grouponGoodsList = getAllGrouponGoods();
        if (CollectionUtils.isEmpty(grouponGoodsList)) {
            return Collections.emptyList();
        }
        grouponGoodsList = grouponGoodsList.stream().filter(p -> ONLINE.name().equals(p.getStatus())).collect(Collectors.toList());
        String goodsTag = grouponGoods.getGoodsTag();
        if (grouponGoodsList.size() < count) {
            return grouponGoodsList;
        }

        if (StringUtils.isNotBlank(goodsTag)) {
            List<String> goodsTagList = new ArrayList<>(Arrays.asList(goodsTag.split(",")));
            grouponGoodsList = getMatchedGrouponGoods(goodsTag, grouponGoodsList);
            if (CollectionUtils.isEmpty(grouponGoodsList)) {
                //如果根据标签没有匹配到,则用全部商品
                grouponGoodsList = getAllGrouponGoods();
            }
        }
        if (CollectionUtils.isNotEmpty(grouponGoodsList)) {
            //过滤自己
            grouponGoodsList = grouponGoodsList.stream().filter(o -> !StringUtils.equalsIgnoreCase(grouponGoods.getId(), o.getId())).collect(Collectors.toList());
        }
        if (grouponGoodsList.size() < count) {
            return grouponGoodsList;
        }
        Collections.shuffle(grouponGoodsList);
        return grouponGoodsList.subList(0, count);
    }

    private boolean getActiveGrouponGoods(final GrouponGoods grouponGoods, Date now) {

        if (Objects.nonNull(grouponGoods.getDeployTime())) {
            return grouponGoods.getDeployTime().before(now);
        }
        Date earlyTime = GrouponGoodsUtil.getEarlyTime(grouponGoods);
        if (Objects.nonNull(earlyTime)) {
            grouponGoods.setDeployTime(earlyTime);
            return earlyTime.before(now);
        }
        return false;
    }

    public List<GrouponGoods> getGrouponGoodsByDataSouce(final GrouponGoodsDataSourceType dataSourceType) {
        return grouponGoodsDao.getGrouponGoodsByDataSouce(dataSourceType);
    }

    public List<GrouponGoods> loadGroupGoods(Collection<String> goodsIdList) {
        Map<String, GrouponGoods> grouponGoodsMap = grouponGoodsDao.$loads(goodsIdList);
        return grouponGoodsMap.entrySet().stream().map(o -> o.getValue()).collect(Collectors.toList());
    }

    @Override
    public PageImpl<GrouponGoodsMapper> getOnlineGrouponGoods(String categoryCode, String orderDimension, String orderType, Integer pageSize, Integer pageNum) {
        List<GrouponGoods> grouponGoods = new ArrayList<>();
        if (StringUtils.isBlank(categoryCode)) {
            grouponGoods = getAllGrouponGoods();
        } else {
            grouponGoods = grouponGoodsDao.loadByCategory(categoryCode);
        }
        if (CollectionUtils.isEmpty(grouponGoods)) {
            return new PageImpl<>(Collections.emptyList());
        }
        Date now = new Date();
        grouponGoods = grouponGoods.stream().filter(g -> getActiveGrouponGoods(g, now))//发布时间在当前时间之前的
                .filter(g -> StringUtils.isNotBlank(g.getUrl()))//没有url的商品就不返回了
                .filter(g -> Objects.nonNull(g.getOos()) && !g.getOos())//过滤卖光的了
                .filter(o -> StringUtils.equalsIgnoreCase(o.getStatus(), ONLINE.getCode()))//过滤下线的商品
                .collect(Collectors.toList());
        long total = grouponGoods.size();
        if (pageNum * pageSize > total) {
            // 请正确填写页码
            return new PageImpl<>(Collections.emptyList());
        }
        grouponGoods = GrouponGoodsUtil.sort(grouponGoods);
        int start = pageNum * pageSize;
        int end = Math.min((int) total, ((pageNum + 1) * pageSize));
        List<GrouponGoods> grouponGoodsPage = new LinkedList<>(grouponGoods.subList(start, end));// include start ,exclusive end
        return new PageImpl<>(
                GrouponGoodsUtil.convert(grouponGoodsPage),
                new PageRequest(pageNum, pageSize),
                total);
    }
}
