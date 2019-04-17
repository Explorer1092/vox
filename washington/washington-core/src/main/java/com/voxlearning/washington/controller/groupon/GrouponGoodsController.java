package com.voxlearning.washington.controller.groupon;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.utopia.service.mizar.api.constants.GrouponGoodStatusType;
import com.voxlearning.utopia.service.mizar.api.entity.groupon.GoodsCategory;
import com.voxlearning.utopia.service.mizar.api.entity.groupon.GrouponGoods;
import com.voxlearning.utopia.service.mizar.api.entity.groupon.SpecialTopic;
import com.voxlearning.utopia.service.mizar.api.mapper.GrouponGoodsMapper;
import com.voxlearning.utopia.service.mizar.api.utils.GrouponGoodsUtil;
import com.voxlearning.utopia.service.mizar.api.utils.SpecialTopicUtil;
import com.voxlearning.utopia.service.mizar.consumer.loader.GrouponGoodsLoaderClient;
import com.voxlearning.utopia.service.mizar.consumer.loader.SpecialTopicLoaderClient;
import com.voxlearning.washington.support.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.service.mizar.api.constants.SpecialTopicPosition.*;

/**
 * 合作方商品展示相关接口
 *
 * @author xiang.lv
 * @date 2016/9/21   13:51
 */
@Controller
@RequestMapping(value = "/groupon")
public class GrouponGoodsController extends AbstractController {
    @Inject
    GrouponGoodsLoaderClient grouponGoodsLoaderClient;

    @Inject
    SpecialTopicLoaderClient specialTopicLoaderClient;


    @RequestMapping(value = "/list.vpage", method = RequestMethod.GET)
    public String specialTopic(Model model) {
        List<SpecialTopic> activeSpecialTopicList =  specialTopicLoaderClient.loadActiveSpecialTopic();
        model.addAttribute("bannerSpecialTopicList", SpecialTopicUtil.filterAndSort(5,BANNER,activeSpecialTopicList));
        List<SpecialTopic> leftSpecialTopicList = SpecialTopicUtil.filterAndSort(1,LEFT,activeSpecialTopicList);
        if (CollectionUtils.isNotEmpty(leftSpecialTopicList)) {
            List<SpecialTopic> rightTopSpecialTopicList = SpecialTopicUtil.filterAndSort(1,RIGHT_TOP,activeSpecialTopicList);
            if (CollectionUtils.isNotEmpty(rightTopSpecialTopicList)) {
                List<SpecialTopic> rightBottomSpecialTopicList = SpecialTopicUtil.filterAndSort(1,RIGHT_BOTTOM,activeSpecialTopicList);
                if (CollectionUtils.isNotEmpty(rightBottomSpecialTopicList)) {
                    model.addAttribute("leftSpecialTopicList", leftSpecialTopicList);
                    model.addAttribute("rightTopSpecialTopicList",rightTopSpecialTopicList);
                    model.addAttribute("rightBottomSpecialTopicList", rightBottomSpecialTopicList);
                }
            }
        }
        List<GoodsCategory> allGoodsCategory = grouponGoodsLoaderClient.getAllGoodsCategory();
        allGoodsCategory = allGoodsCategory.stream().filter(c -> !c.getDisabled()).collect(Collectors.toList());
        model.addAttribute("categoryList", allGoodsCategory);
        return "groupon/list";
    }

    /**
     * 具体专题(详情)
     */
    @RequestMapping(value = "/special.vpage")
    public String specialTopicDetail(Model model) {
        String stId = getRequestString("stId");
        if (StringUtils.isNotBlank(stId)) {
            SpecialTopic specialTopic = specialTopicLoaderClient.loadById(stId);
            if(Objects.nonNull(specialTopic)){
                List<String> grouponGoodsIdList = specialTopic.getGrouponGoodsIdList();
                if (CollectionUtils.isNotEmpty(grouponGoodsIdList)) {
                    List<GrouponGoods> grouponGoodsList = grouponGoodsLoaderClient.loadGroupGoods(grouponGoodsIdList);
                    if (CollectionUtils.isNotEmpty(grouponGoodsList)) {
                        Date now = new Date();
                        grouponGoodsList = grouponGoodsList.stream().filter(o -> StringUtils.equalsIgnoreCase(o.getStatus(), GrouponGoodStatusType.ONLINE.name()))
                                .filter(o -> Objects.nonNull(o.getDeployTime()) && o.getDeployTime().before(now))
                                .collect(Collectors.toList());
                        grouponGoodsList =  GrouponGoodsUtil.sort(grouponGoodsList);//排序
                        model.addAttribute("grouponGoodsList", GrouponGoodsUtil.convert(grouponGoodsList));//商品列表
                    }
                }
                model.addAttribute("specialTopic", specialTopic);
            }

        }
        return "groupon/special";//专题详情页面
    }

    //书本(商品 goodsdetail?)详情
    @RequestMapping(value = "/goodsdetail.vpage", method = RequestMethod.GET)
    public String bookDetail(Model model) {
        String goodsId = getRequestString("goodsId");
        if(StringUtils.isNotBlank(goodsId)){
            List<GrouponGoods> grouponGoodsList =   grouponGoodsLoaderClient.loadGroupGoods(Arrays.asList(goodsId));
            if(CollectionUtils.isNotEmpty(grouponGoodsList)){
                model.addAttribute("grouponGoods",GrouponGoodsUtil.convert(grouponGoodsList.get(0)));
                List<GrouponGoods> recommendGrouponGoodsList =   grouponGoodsLoaderClient.loadRecommendGrouponGoods(5,grouponGoodsList.get(0));
                if(CollectionUtils.isNotEmpty(recommendGrouponGoodsList)){
                    recommendGrouponGoodsList =  GrouponGoodsUtil.sort(recommendGrouponGoodsList);//排序
                    model.addAttribute("recommendList",GrouponGoodsUtil.convert(recommendGrouponGoodsList));
                }
            }
        }
        return "groupon/goodsdetail";
    }

    @RequestMapping(value = "/ecsg.vpage", method = {RequestMethod.GET})
    public String ecsg(Model model) {
        return "redirect:/groupon/list.vpage";
    }

    @RequestMapping(value = "getgoods.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage getGood() {
        String category = getRequestParameter("category", "");                              //分类标识,传空串取所有分类,传具体分类标识则取具体分类
        String orderDimension = getRequestParameter("orderDimension", "recommend");       //排序维度  推荐排序:recommend 销量:saleCount 时间:time
        String orderType = getRequestParameter("orderType", "desc");                         //排序方式 升序asc,降序 desc

        int pageSize = getRequestInt("pageSize", 10);
        int pageNum = getRequestInt("pageNum", 0);
        PageImpl<GrouponGoodsMapper> pageImpl = grouponGoodsLoaderClient.getOnlineGrouponGoods(category, orderDimension, orderType, pageSize, pageNum);
        return MapMessage.successMessage()
                .add("pageNum", pageImpl.getNumber())
                .add("pageSize", pageImpl.getSize())
                .add("goodsList", pageImpl.getContent())
                .add("totalPage", pageImpl.getTotalPages())
                .add("totalSize", pageImpl.getTotalElements());
    }

}
