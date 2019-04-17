package com.voxlearning.utopia.service.mizar.api.utils;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.mizar.api.entity.groupon.GrouponGoods;
import com.voxlearning.utopia.service.mizar.api.mapper.GrouponGoodsMapper;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by xiang.lv on 2016/10/18.
 *
 * @author xiang.lv
 * @date 2016/10/18   20:05
 */
final public class GrouponGoodsUtil {
    private static final Pattern htmlPattern = Pattern.compile("<[^>]*>", Pattern.CASE_INSENSITIVE);

    private static String removeHtmlTag(final String content) {
        if (StringUtils.isBlank(content)) {
            return content;
        }
        Matcher htmlMatcher = htmlPattern.matcher(content);
        return htmlMatcher.replaceAll(""); //过滤html标签
    }

    public static GrouponGoodsMapper convert(final GrouponGoods grouponGoods) {
        GrouponGoodsMapper grouponGoodsMapper = new GrouponGoodsMapper();
        if (Objects.isNull(grouponGoods)) {
            return grouponGoodsMapper;
        }
        //copy attribute
        Date now = new Date();
        grouponGoodsMapper.setId(grouponGoods.getId());
        grouponGoodsMapper.setOuterGoodsId(grouponGoods.getOuterGoodsId());
        grouponGoodsMapper.setBeginTime(grouponGoods.getBeginTime());
        grouponGoodsMapper.setEndTime(grouponGoods.getEndTime());
        grouponGoodsMapper.setCurrentTime(now);
        grouponGoodsMapper.setCategoryCode(grouponGoods.getCategoryCode());
        grouponGoodsMapper.setPostFree(grouponGoods.getPostFree());
        grouponGoodsMapper.setOos(grouponGoods.getOos());
        grouponGoodsMapper.setImage(grouponGoods.getImage());
        // grouponGoodsMapper.setRecommend(grouponGoods.getRecommend());
        grouponGoodsMapper.setRecommend(removeHtmlTag(grouponGoods.getRecommend()));

        if (Objects.isNull(grouponGoods.getDeployTime())) {
            Date earlyTime = getEarlyTime(grouponGoods);//startTime或endTime早的那个时间
            if (Objects.nonNull(earlyTime)) {
                grouponGoodsMapper.setDeployTime(earlyTime);
            }
        }
        if (Objects.nonNull(grouponGoods.getDeployTime())) {
            grouponGoodsMapper.setDeployDay(DateUtils.dateToString(grouponGoods.getDeployTime(), "MM-dd"));//
        }
        grouponGoodsMapper.setPrice(grouponGoods.getPrice());
        grouponGoodsMapper.setOriginalPrice(grouponGoods.getOriginalPrice());
        grouponGoodsMapper.setDataSource(grouponGoods.getDataSource());
        grouponGoodsMapper.setTitle(grouponGoods.getTitle());
        grouponGoodsMapper.setShortTitle(grouponGoods.getShortTitle());
        grouponGoodsMapper.setSaleCount(grouponGoods.getSaleCount());
        grouponGoodsMapper.setOrderIndex(grouponGoods.getOrderIndex());
        grouponGoodsMapper.setUrl(grouponGoods.getUrl());
        grouponGoodsMapper.setGoodsSource(grouponGoods.getGoodsSource());
        grouponGoodsMapper.setRecommend(grouponGoods.getRecommend());
        grouponGoodsMapper.setGoodsTag(grouponGoods.getGoodsTag());
        grouponGoodsMapper.setSpecialTag(grouponGoods.getSpecialTag());

        return grouponGoodsMapper;
    }

    /**
     * 获取开始时间和结束时间两者中早的一个时间
     *
     * @param grouponGoods
     * @return
     */
    public static Date getEarlyTime(final GrouponGoods grouponGoods) {
        if (Objects.nonNull(grouponGoods.getBeginTime()) && Objects.nonNull(grouponGoods.getEndTime())) {
            return grouponGoods.getBeginTime().before(grouponGoods.getEndTime()) ? grouponGoods.getBeginTime() : grouponGoods.getEndTime();
        } else if (Objects.nonNull(grouponGoods.getBeginTime())) {
            return grouponGoods.getBeginTime();
        } else {
            return grouponGoods.getEndTime();
        }
    }

    public static List<GrouponGoodsMapper> convert(final List<GrouponGoods> grouponGoodsList) {
        if (CollectionUtils.isEmpty(grouponGoodsList)) {
            return Collections.emptyList();
        }
        List<GrouponGoodsMapper> grouponGoodsMapperList = new ArrayList<GrouponGoodsMapper>(grouponGoodsList.size());
        for (GrouponGoods goods : grouponGoodsList) {
            if (Objects.nonNull(goods)) {
                grouponGoodsMapperList.add(convert(goods));
            }
        }
        return grouponGoodsMapperList;
    }


    /**
     * 先按发布时间排序,再按排序值(同一天按排序值排序)
     *
     * @param grouponGoods
     * @return
     */
    public static List<GrouponGoods> sort(List<GrouponGoods> grouponGoods) {
        Collections.sort(grouponGoods, (g1, g2) -> {
            if (Objects.isNull(g1.getDeployTime())) {
                return 1;
            }
            if (Objects.isNull(g2.getDeployTime())) {
                return -1;
            }
            if (StringUtils.equals(DateUtils.dateToString(g1.getDeployTime(), "yyyy-MM-dd"), DateUtils.dateToString(g2.getDeployTime(), "yyyy-MM-dd"))) {
                //同一天内的,按排序值,大值在前
                Integer orderIndex = SafeConverter.toInt(g1.getOrderIndex());
                Integer orderIndexOther = SafeConverter.toInt(g2.getOrderIndex());
                return Integer.compare(orderIndexOther, orderIndex);
            } else {
                //不在同一天的,按时间早的在前
                return Long.compare(g2.getDeployTime().getTime(), g1.getDeployTime().getTime());
            }
        });
        return grouponGoods;
    }
}
