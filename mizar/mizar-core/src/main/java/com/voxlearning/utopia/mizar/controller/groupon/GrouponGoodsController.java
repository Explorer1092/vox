package com.voxlearning.utopia.mizar.controller.groupon;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.utopia.mizar.controller.AbstractMizarController;
import com.voxlearning.utopia.mizar.utils.GrouponGoodsUtils;
import com.voxlearning.utopia.service.mizar.api.constants.GrouponGoodStatusType;
import com.voxlearning.utopia.service.mizar.api.constants.GrouponGoodsDataSourceType;
import com.voxlearning.utopia.service.mizar.api.constants.GrouponGoodsSourceType;
import com.voxlearning.utopia.service.mizar.api.entity.groupon.GoodsCategory;
import com.voxlearning.utopia.service.mizar.api.entity.groupon.GrouponGoods;
import com.voxlearning.utopia.service.mizar.consumer.loader.GrouponGoodsLoaderClient;
import com.voxlearning.utopia.service.mizar.consumer.service.GrouponGoodsServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 合作方商品分类管理,商品编辑页面
 *
 * @author xiang.lv
 * @date 2016/9/21   13:51
 */
@Controller
@RequestMapping(value = "/groupon")
@Slf4j
public class GrouponGoodsController extends AbstractMizarController {

    @Inject private GrouponGoodsLoaderClient grouponGoodsLoaderClient;
    @Inject private GrouponGoodsServiceClient grouponGoodsServiceClient;

    private static final int RESERVE_PAGE_SIZE = 10;

    @RequestMapping(value = "/goods/list.vpage")
    public String goodsList(Model model) {
        //商品分类信息
        int pageIndex = getRequestInt("pageIndex", 0);           // 当前页
        pageIndex = pageIndex <= 0 ? 0 : pageIndex;
        String id = getRequestString("id");                             // 商品id
        String shortTitle = getRequestString("shortTitle");             // 商品名称短标题
        String category = getRequestString("category");                 // 商品分类
        String oos = getRequestString("oos");                           // 是否买光
        String postFree = getRequestString("postFree");                 // 是否包邮
        String dataSource = getRequestString("dataSource");             // 数据来源
        String goodsSource = getRequestString("goodsSource");            //商品来源
        String status = getRequestString("status");                      //商品状态
        String beginTime = getRequestString("beginTime");                //开始时间
        String endTime = getRequestString("endTime");                    //结束时间
        GrouponGoods paramBean = new GrouponGoods();
        paramBean.setId(id);
        paramBean.setShortTitle(shortTitle);
        paramBean.setCategoryCode(category);
        if (StringUtils.isNotBlank(oos)) {
            paramBean.setOos(StringUtils.equals("1", oos));
        }
        if (StringUtils.isNotBlank(postFree)) {
            paramBean.setPostFree(StringUtils.equals("1", postFree));
        }
        paramBean.setDataSource(dataSource);
        paramBean.setGoodsSource(goodsSource);
        paramBean.setStatus(status);
        paramBean.setBeginTime(StringUtils.isNoneBlank(beginTime) ? DateUtils.stringToDate(beginTime) : null);
        paramBean.setEndTime(StringUtils.isNoneBlank(endTime) ? DateUtils.stringToDate(endTime) : null);
        model.addAttribute("paramBean", paramBean);
        Collection<GrouponGoodsSourceType> grouponGoodsSourceTypeList = GrouponGoodsSourceType.getAllGrouponGoodsSourceType().values();
        Collection<GrouponGoodsDataSourceType> dataSourceTypeList = GrouponGoodsDataSourceType.getAllDataSourceTypes().values();//商品数据来
        List<GrouponGoods> goodsList = grouponGoodsLoaderClient.getAllGrouponGoods();//获取所有分类的商品,包括下线的商品
        Collection<GrouponGoodStatusType> statusTypeList = GrouponGoodStatusType.getAllGrouponGoodStatus().values();
        List<GoodsCategory> categoryList = grouponGoodsLoaderClient.getAllGoodsCategory();
        Map<String, String> categoryMap = categoryList.stream().collect(Collectors.toMap(GoodsCategory::getCategoryCode, GoodsCategory::getCategoryName));
        if (StringUtils.isNotBlank(beginTime)) {
            Date beginTimeDate = DateUtils.stringToDate(beginTime);
            goodsList = goodsList.stream().filter(o -> Objects.nonNull(o.getDeployTime()) && o.getDeployTime().after(beginTimeDate)).collect(Collectors.toList());
        }
        if (StringUtils.isNotBlank(endTime)) {
            Date endTimeDate = DateUtils.stringToDate(endTime);
            goodsList = goodsList.stream().filter(o -> Objects.nonNull(o.getDeployTime()) && o.getDeployTime().before(endTimeDate)).collect(Collectors.toList());
        }
        if (StringUtils.isNotBlank(status)) {
            goodsList = goodsList.stream().filter(o -> StringUtils.equals(o.getStatus(), status)).collect(Collectors.toList());
        }
        if (StringUtils.isNotBlank(id)) {
            goodsList = goodsList.stream().filter(o -> StringUtils.equals(o.getId(), id)).collect(Collectors.toList());
        }
        if (StringUtils.isNotBlank(shortTitle)) {
            goodsList = goodsList.stream().filter(o -> Objects.nonNull(o.getShortTitle()) && o.getShortTitle().contains(shortTitle)).collect(Collectors.toList());
        }
        if (StringUtils.isNotBlank(category)) {
            goodsList = goodsList.stream().filter(o -> StringUtils.equalsIgnoreCase(o.getCategoryCode(), category)).collect(Collectors.toList());
        }
        if (StringUtils.isNotBlank(dataSource)) {
            goodsList = goodsList.stream().filter(o -> StringUtils.equalsIgnoreCase(o.getDataSource(), dataSource)).collect(Collectors.toList());
        }
        if (StringUtils.isNotBlank(goodsSource)) {
            goodsList = goodsList.stream().filter(o -> StringUtils.equalsIgnoreCase(o.getGoodsSource(), goodsSource)).collect(Collectors.toList());
        }
        if (StringUtils.isNotBlank(oos)) {
            goodsList = goodsList.stream().filter(o -> Objects.nonNull(o.getOos()) && o.getOos().equals(oos.equals("1") ? true : false)).collect(Collectors.toList());
        }
        if (StringUtils.isNotBlank(postFree)) {
            goodsList = goodsList.stream().filter(o -> Objects.nonNull(o.getPostFree()) && o.getPostFree().equals(postFree.equals("1") ? true : false)).collect(Collectors.toList());
        }
        if (CollectionUtils.isNotEmpty(goodsList)) {
            goodsList = goodsList.stream().sorted(new Comparator<GrouponGoods>() {
                @Override
                public int compare(GrouponGoods o1, GrouponGoods o2) {
                    Integer orderIndex = SafeConverter.toInt(o1.getOrderIndex());
                    Integer orderIndexOther = SafeConverter.toInt(o2.getOrderIndex());
                    return Integer.compare(orderIndexOther,orderIndex);

                }
            }).collect(Collectors.toList());
        }
        //没有发布时间,则取早的那个
        goodsList = goodsList.stream().map(o->{
            if(Objects.isNull(o.getDeployTime())){
                if(Objects.isNull(o.getBeginTime())){
                    o.setDeployTime(o.getEndTime());
                }else  if(Objects.isNull(o.getEndTime())){
                    o.setDeployTime(o.getBeginTime());
                }else {
                    o.setDeployTime(o.getBeginTime().before(o.getEndTime()) ? o.getBeginTime(): o.getEndTime());
                }

            }
            return o;
        }).collect(Collectors.toList());
        Page<GrouponGoods> pager = PageableUtils.listToPage(goodsList, new PageRequest(pageIndex, RESERVE_PAGE_SIZE));
        model.addAttribute("pager", pager);
        model.addAttribute("categoryList", categoryList);
        model.addAttribute("statusTypeList", statusTypeList);
        model.addAttribute("pageIndex", pageIndex);
        model.addAttribute("categoryMap", categoryMap);
        model.addAttribute("grouponGoodsSourceTypeList", grouponGoodsSourceTypeList);
        model.addAttribute("dataSourceTypeList", dataSourceTypeList);//商品来源
        return "/groupon/goods/list";
    }

    /**
     * 商品详情页面
     *
     * @param model
     * @return
     */
    @RequestMapping(value = "/goods/detail.vpage", method = RequestMethod.GET)
    public String goodsDetail(Model model) {
        String inputUrl = getRequestString("inputUrl");
        String goodsId = getRequestString("id");
        if (StringUtils.isNotBlank(inputUrl)) {
            //抓取第三方数据
            GrouponGoods grouponGoods = GrouponGoodsUtils.getDDBookByUrl(inputUrl);
            model.addAttribute("goods", grouponGoods);
            model.addAttribute("isNew", true);
        } else if (StringUtils.isNotBlank(goodsId)) {
            GrouponGoods grouponGoods = grouponGoodsLoaderClient.getGroupGoods(goodsId);
            model.addAttribute("goods", grouponGoods);
            model.addAttribute("isNew", false);
        } else {
            model.addAttribute("isNew", true);
        }
        List<GoodsCategory> categoryList = grouponGoodsLoaderClient.getAllGoodsCategory();
        Map<String, GrouponGoodsSourceType> goodsSourceTypeMap = GrouponGoodsSourceType.getAllGrouponGoodsSourceType();//商品来源
        Map<String, GrouponGoodStatusType> statusTypeMap = GrouponGoodStatusType.getAllGrouponGoodStatus();//商品状态
        Map<String, GrouponGoodsDataSourceType> dataSourceTypeMap = GrouponGoodsDataSourceType.getAllDataSourceTypes();//数据来源
        if (null != statusTypeMap && statusTypeMap.size() > 0) {
            List<GrouponGoodStatusType> goodStatusTypeList = statusTypeMap.values().stream().collect(Collectors.toList());
            model.addAttribute("goodStatusTypeList", goodStatusTypeList);
        }
        if (null != goodsSourceTypeMap && goodsSourceTypeMap.size() > 0) {
            //商品来源
            List<GrouponGoodsSourceType> goodsSourceTypeList = goodsSourceTypeMap.values().stream().collect(Collectors.toList());
            model.addAttribute("goodsSourceTypeList", goodsSourceTypeList);
        }
        if (null != dataSourceTypeMap && dataSourceTypeMap.size() > 0) {
            //数据来源
            List<GrouponGoodsDataSourceType> dataSourceTypeList = dataSourceTypeMap.values().stream().collect(Collectors.toList());
            model.addAttribute("dataSourceTypeList", dataSourceTypeList);
        }

        model.addAttribute("categoryList", categoryList);

        return "/groupon/goods/detail";
    }

    @RequestMapping(value = "/goods/addgoods.vpage")
    @ResponseBody
    public MapMessage addGoods() {
        String id = requestString("id");
        String outerGoodsId = requestString("outerGoodsId");
        String shortTitle = requestString("shortTitle");
        String title = requestString("title");
        Integer orderIndex = requestInteger("orderIndex");
        String categoryCode = requestString("categoryCode");
        Double price = requestString("price") == null ? null : getRequestDouble("price");
        Double originalPrice = requestString("originalPrice") == null ? null : getRequestDouble("originalPrice");
        String image = requestString("image");
        Integer saleCount = requestInteger("saleCount");
        String status = requestString("status");
        Boolean postFree = getRequestBool("postFree");
        Boolean oos = getRequestBool("oos");
        String dataSource = requestString("dataSource");
        String goodsSource = requestString("goodsSource");
        Date deployTime = getRequestDate("deployTime",new Date());
        String url = requestString("url");
        String originUrl = requestString("originUrl");
        String recommend = requestString("recommend");
        String[] specialTag = getRequest().getParameterValues("specialTag");
        String goodsTag = requestString("goodsTag");
        GrouponGoods grouponGoods = new GrouponGoods();
        grouponGoods.setOuterGoodsId(outerGoodsId);
        grouponGoods.setTitle(title);
        grouponGoods.setRecommend(recommend);
        grouponGoods.setGoodsTag(goodsTag);
        String specialTagValue = "";
        if (null != specialTag) {
            specialTagValue = Stream.of(specialTag).collect(Collectors.joining(","));
        }
        //如果前台没有传,就设置没有标签
        grouponGoods.setSpecialTag(specialTagValue);
        grouponGoods.setCategoryCode(categoryCode);
        grouponGoods.setShortTitle(shortTitle);
        grouponGoods.setDataSource(dataSource);
        grouponGoods.setGoodsSource(goodsSource);
        grouponGoods.setPrice(price);
        grouponGoods.setOriginalPrice(originalPrice);
        grouponGoods.setDeployTime(deployTime);
        grouponGoods.setOos(oos);
        grouponGoods.setStatus(status);
        grouponGoods.setOrderIndex(orderIndex);
        grouponGoods.setSaleCount(saleCount);
        grouponGoods.setId(id);
        grouponGoods.setUrl(url);
        grouponGoods.setOriginUrl(originUrl);
        if (StringUtils.contains(specialTagValue, "postFree")) {
            grouponGoods.setPostFree(true);
        }
        if (StringUtils.isNotBlank(image)) {
            grouponGoods.setImage(image);
        }
        if (StringUtils.isBlank(id)) {
            //新增的数据
            grouponGoods.setOuterGoodsId(UUID.randomUUID().toString());
        }
        try {
            grouponGoodsServiceClient.saveGrouponGoods(grouponGoods);
        } catch (Exception e) {
            logger.error("保存商品失败" + grouponGoods, e);
            return MapMessage.errorMessage("保存商品失败~");
        }
        return MapMessage.errorMessage("保存商品成功");
    }


    /********************************************商品分类相关**********************************************/
    @RequestMapping(value = "/category/list.vpage")
    public String index(Model model) {
        //商品分类信息
        List<GoodsCategory> categoryList = grouponGoodsLoaderClient.getAllGoodsCategory();
        model.addAttribute("categoryList", categoryList);
        return "/groupon/category/list";
    }

    @RequestMapping(value = "/category/detail.vpage")
    public String detail(Model model) {
        String categoryId = getRequestString("id");
        if (StringUtils.isNotBlank(categoryId)) {
            //编辑已有分类
            GoodsCategory goodsCategory = grouponGoodsLoaderClient.getGoodsCategoryById(categoryId);
            model.addAttribute("category", goodsCategory);
            model.addAttribute("isNew", false);
        } else {
            model.addAttribute("isNew", true);
        }
        return "/groupon/category/detail";
    }

    @RequestMapping(value = "/category/checkcategorycode.vpage")
    @ResponseBody
    public MapMessage checkCategoryCode(final String categoryCode) {
        if (StringUtils.isNotBlank(categoryCode)) {
            GoodsCategory goodsCategory = grouponGoodsLoaderClient.getGoodsCategoryByCode(categoryCode);
            if (null != goodsCategory) {
                return MapMessage.errorMessage("分类标识已存在~");
            }
        }
        return MapMessage.successMessage("分类标识可用~");
    }

    @RequestMapping(value = "/category/addcategory.vpage")
    @ResponseBody
    public MapMessage addCategory(final String categoryName, final String categoryCode, Integer orderIndex, Boolean disabled) {
        GoodsCategory goodsCategory = new GoodsCategory();
        goodsCategory.setCategoryName(categoryName);
        goodsCategory.setCategoryCode(categoryCode);
        goodsCategory.setOrderIndex(orderIndex);
        goodsCategory.setDisabled(disabled);
        goodsCategory.setCreateAt(new Date());
        try {
            if(StringUtils.isNotBlank(categoryCode)){
                GoodsCategory goodsCategoryDB =  grouponGoodsLoaderClient.getGoodsCategoryByCode(categoryCode);
                if(Objects.nonNull(goodsCategoryDB)){
                    return MapMessage.errorMessage("分类标识已存在~");
                }
            }
            grouponGoodsServiceClient.saveGoodsCategory(goodsCategory);
        } catch (Exception e) {
            logger.error("保存分类失败" + goodsCategory, e);
            return MapMessage.errorMessage("保存失败~");
        }
        return MapMessage.errorMessage("保存成功");
    }

    @RequestMapping(value = "category/updatecategory.vpage")
    @ResponseBody
    public MapMessage updateCategory(final String id, final String categoryName, final String categoryCode, Integer orderIndex, Boolean disabled) {
        GoodsCategory goodsCategory = new GoodsCategory();
        goodsCategory.setId(id);
        goodsCategory.setCategoryName(categoryName);
        goodsCategory.setCategoryCode(categoryCode);
        goodsCategory.setOrderIndex(orderIndex);
        goodsCategory.setDisabled(disabled);
        goodsCategory.setUpdateAt(new Date());
        try {
            if(StringUtils.isNotBlank(categoryCode)){
                GoodsCategory goodsCategoryDB =  grouponGoodsLoaderClient.getGoodsCategoryByCode(categoryCode);
                if(Objects.nonNull(goodsCategoryDB) && !StringUtils.equalsIgnoreCase(id,goodsCategoryDB.getId())){
                    return MapMessage.errorMessage("分类标识已存在~");
                }
            }
            grouponGoodsServiceClient.saveGoodsCategory(goodsCategory);
        } catch (Exception e) {
            logger.error("保存分类失败" + goodsCategory, e);
            return MapMessage.errorMessage("保存失败~");
        }
        return MapMessage.errorMessage("保存成功");
    }

    /**
     * ueditor  配置
     *
     * @return
     */
    @RequestMapping(value = "/goods/ueditorcontroller.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage ueditorController() {
        String action = getRequestString("action");
        MapMessage mapMessage = new MapMessage();
        switch (action) {
            case "config":
                mapMessage.set("imageActionName", "uploadimage");
                mapMessage.set("imageFieldName", "upfile");
                mapMessage.set("imageInsertAlign", "none");
                mapMessage.set("imageMaxSize", 2048000);
                mapMessage.set("imageUrlPrefix", "");
                mapMessage.setSuccess(true);
                break;
            case "uploadimage":
                MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) getRequest();
                MultipartFile imgFile = multipartRequest.getFile("upfile");
                if (imgFile.isEmpty()) {
                    return MapMessage.errorMessage("没有文件上传");
                }
                String originalFileName = imgFile.getOriginalFilename();
                try {
                    String url = $uploadFile("upfile");
                    mapMessage.add("url", url);
                    mapMessage.add("title", imgFile.getName());
                    mapMessage.add("state", "SUCCESS");
                    mapMessage.add("original", originalFileName);
                    mapMessage.setSuccess(true);
                } catch (Exception ex) {
                    mapMessage.setSuccess(false);
                    log.error("上传图片异常： " + ex.getMessage());
                }
        }
        return mapMessage;
    }
}
