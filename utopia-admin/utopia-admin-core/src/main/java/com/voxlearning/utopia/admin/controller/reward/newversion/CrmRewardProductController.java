package com.voxlearning.utopia.admin.controller.reward.newversion;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.admin.controller.reward.RewardAbstractController;
import com.voxlearning.utopia.admin.data.CrmRewardProductMapper;
import com.voxlearning.utopia.admin.util.AdminOssManageUtils;
import com.voxlearning.utopia.service.action.api.document.Privilege;
import com.voxlearning.utopia.service.action.api.support.PrivilegeOrigin;
import com.voxlearning.utopia.service.action.api.support.PrivilegeType;
import com.voxlearning.utopia.service.privilege.client.PrivilegeManagerClient;
import com.voxlearning.utopia.service.reward.api.CRMRewardService;
import com.voxlearning.utopia.service.reward.client.newversion.NewRewardLoaderClient;
import com.voxlearning.utopia.service.reward.client.newversion.NewRewardServiceClient;
import com.voxlearning.utopia.service.reward.constant.OneLevelCategoryType;
import com.voxlearning.utopia.service.reward.constant.RewardCouponResource;
import com.voxlearning.utopia.service.reward.constant.RewardOrderSaleGroup;
import com.voxlearning.utopia.service.reward.consumer.newversion.RewardCenterClient;
import com.voxlearning.utopia.service.reward.entity.*;
import com.voxlearning.utopia.service.reward.entity.newversion.*;
import com.voxlearning.utopia.service.reward.mapper.product.crm.GetProductDetailMapper;
import com.voxlearning.utopia.service.reward.util.RewardProductDetailUtils;
import lombok.SneakyThrows;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.inject.Inject;
import java.io.InputStream;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @description:
 * @author: kaibo.he
 * @create: 2018-10-15 21:40
 **/
@Controller
@RequestMapping("/reward/crmproduct")
public class CrmRewardProductController extends RewardAbstractController {

    private static final String DEFAULT_LINE_SEPARATOR = "\n";

    @Inject
    private NewRewardLoaderClient newRewardLoaderClient;
    @Inject
    private NewRewardServiceClient newRewardServiceClient;
    @Inject
    private RewardCenterClient rewardCenterClient;
    @Inject
    private PrivilegeManagerClient privilegeManagerClient;
    @ImportService(interfaceClass = CRMRewardService.class)
    private CRMRewardService crmRewardService;

    @RequestMapping(value = "selector.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    private MapMessage selector() {


        MapMessage message = MapMessage.successMessage();


        List<ProductSet> productSetList = newRewardLoaderClient.loadAllProductSet();
        List<ProductCategory> productCategoryList = newRewardLoaderClient.loadProductCategoryByParentId(0L);
        List<GetProductDetailMapper.SetMapper> productSetSelector = productSetList
                .stream()
                .map(set -> {
                    GetProductDetailMapper.SetMapper mapper = new GetProductDetailMapper().new SetMapper();
                    mapper.setIsSelected(false);
                    mapper.setTagMappers(null);
                    mapper.setName(set.getName());
                    mapper.setId(set.getId());
                    return mapper;
                }).collect(Collectors.toList());
        GetProductDetailMapper.SetMapper productSetMapper = new GetProductDetailMapper().new SetMapper();
        productSetMapper.setIsSelected(false);
        productSetMapper.setTagMappers(null);
        productSetMapper.setName("全部");
        productSetMapper.setId(null);
        productSetSelector.add(0, productSetMapper);
        List<GetProductDetailMapper.CategoryMapper> productCategorySelector = productCategoryList
                .stream()
                .map(category -> {
                    GetProductDetailMapper.CategoryMapper mapper = new GetProductDetailMapper().new CategoryMapper();
                    mapper.setIsSelected(false);
                    mapper.setName(category.getName());
                    mapper.setId(category.getId());
                    mapper.setOneLevelCategoryType(category.getOneLevelCategoryType());
                    return mapper;
                }).collect(Collectors.toList());
        GetProductDetailMapper.CategoryMapper mapper = new GetProductDetailMapper().new CategoryMapper();
        mapper.setIsSelected(false);
        mapper.setName("全部");
        mapper.setId(null);
        productCategorySelector.add(0, mapper);
        List<Integer> onlinedSelector = new ArrayList<>();
        onlinedSelector.add(0);
        onlinedSelector.add(1);
        onlinedSelector.add(2);

        List<Integer> schoolVisibleSelector = new ArrayList<>();
        schoolVisibleSelector.add(0);
        schoolVisibleSelector.add(1);
        schoolVisibleSelector.add(2);

        List<Integer> userVisibleSelector = new ArrayList<>();
        userVisibleSelector.add(0);
        userVisibleSelector.add(1);
        userVisibleSelector.add(2);

        message.add("onlinedSelector", onlinedSelector);
        message.add("schoolVisibleSelector", schoolVisibleSelector);
        message.add("userVisibleSelector", userVisibleSelector);
        message.add("setSelector", productSetSelector);
        message.add("oneLevelCategorySelector", productCategorySelector);

        return message;
    }

    @RequestMapping(value = "productlist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    private MapMessage categoryList(Model model) {

        Long productId = getRequestLong("productId");
        String productName = getRequestParameter("productName", "");
        Long oneLevelCategoryId = getRequestLong("oneLevelCategoryId", 0L);
        Long twoLevelCategoryId = getRequestLong("twoLevelCategoryId", 0L);
        Long setId = getRequestLong("setId", 0L);
        Integer onlined = getRequestInt("onlined", 0);
        Integer schoolVisible = getRequestInt("schoolVisible", 0);
        Integer userVisible = getRequestInt("userVisible", 0);

        String orderBy = getRequestParameter("orderBy", "");
        String upDown = getRequestParameter("upDown", "up");
        Integer pageNumber = getRequestInt("pageNumber", 1);
        Pageable pageable = new PageRequest(pageNumber - 1, 10);


        MapMessage message = MapMessage.successMessage();

        List<RewardProduct> rewardProductList = crmRewardService.$loadRewardProducts();
        Stream<RewardProduct> stream = rewardProductList.stream();

        // 商品ID过滤
        if (productId != 0) {
            stream = stream.filter(e -> Objects.equals(e.getId(), productId));
        }

        // 商品名称过滤
        if (StringUtils.isNotBlank(productName)) {
            stream = stream.filter(e -> e.getProductName() != null)
                    .filter(e -> e.getProductName().contains(productName));
        }

        // 上架状态过滤
        switch (onlined) {
            case 1:
                stream = stream.filter(e -> SafeConverter.toBoolean(e.getOnlined()));
                break;
            case 2:
                stream = stream.filter(e -> !SafeConverter.toBoolean(e.getOnlined()));
                break;
            default:
                break;
        }

        // 用户可见性过滤
        switch (userVisible) {
            case 1:
                stream = stream.filter(e -> SafeConverter.toBoolean(e.getStudentVisible()));
                break;
            case 2:
                stream = stream.filter(e -> SafeConverter.toBoolean(e.getTeacherVisible()));
                break;
            default:
                break;
        }

        // 学段可见性过滤
        switch (schoolVisible) {
            case 1:
                stream = stream.filter(e -> SafeConverter.toBoolean(e.getPrimarySchoolVisible()));
                break;
            case 2:
                stream = stream.filter(e -> SafeConverter.toBoolean(e.getJuniorSchoolVisible()));
                break;
            default:
                break;
        }

        // 分类集合过滤
        if (!Objects.equals(oneLevelCategoryId, 0L)) {
            // 一级分类
            stream = stream.filter(product -> Objects.equals(product.getOneLevelCategoryId(), oneLevelCategoryId));

            // 二级分类
            if (!Objects.equals(twoLevelCategoryId, 0L)) {
                List<Long> productIds = newRewardLoaderClient.loadProductIdListByCategoryId(twoLevelCategoryId);
                stream = stream.filter(e -> productIds.contains(e.getId()));
            } else {
                List<Long> productIds = newRewardLoaderClient.loadProductIdListByCategoryParentId(oneLevelCategoryId);
                if (CollectionUtils.isNotEmpty(productIds)) {
                    stream = stream.filter(e -> productIds.contains(e.getId()));
                }
            }
        }

        // 分类集合过滤
        if (setId != 0) {
            List<Long> productIds = newRewardLoaderClient.loadProductIdListBySetId(setId);
            stream = stream.filter(e -> productIds.contains(e.getId()));
        }

        rewardProductList = stream.collect(Collectors.toList());

        List<Map<String, Object>> indexList = new ArrayList<>();
        for (RewardProduct product : rewardProductList) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", product.getId());
            map.put("productName", product.getProductName());
            // 显示兑换积分
            map.put("priceS", product.getPriceOldS());
            map.put("soldQuantity", product.getSoldQuantity());
            map.put("studentVisible", product.getStudentVisible());
            map.put("teacherVisible", product.getTeacherVisible());
            map.put("onlined", product.getOnlined());
            map.put("studentOrderValue", product.getStudentOrderValue());
            map.put("teacherOrderValue", product.getTeacherOrderValue());
            map.put("buyingPrice", product.getBuyingPrice());
            map.put("oneLevelCategoryId", product.getOneLevelCategoryId());

            indexList.add(map);
        }

        // 已经有库存预警邮件, 这里就不让按照库存排序了
        if (!Objects.equals(orderBy, "inventory")) {
            indexList = RewardProductDetailUtils.orderSimbleProduct(indexList, orderBy, upDown);
        }
        Page<Map<String, Object>> productPage = PageableUtils.listToPage(indexList, pageable);
        setIndexList(productPage.getContent());

        message.add("productPage", productPage);
        message.add("pageNumber", pageNumber);
        message.add("productId", productId);
        message.add("productName", productName);
        message.add("oneLevelCategoryId", oneLevelCategoryId);
        message.add("twoLevelCategoryId", twoLevelCategoryId);
        message.add("setId", setId);
        message.add("onlined", onlined);
        message.add("schoolVisible", schoolVisible);
        message.add("userVisible", userVisible);
        message.add("orderBy", orderBy);
        message.add("upDown",upDown);

        return message;
    }

    private void setIndexList(List<Map<String, Object>> indexList) {
        int inventory;

        // 获取图片
        List<RewardImage> images = crmRewardService.$loadRewardImages();

        List<RewardSku> rewardSkuList = newRewardLoaderClient.loadAllRewardSku();
        Map<Long, List<RewardSku>> productIdSkuMap = rewardSkuList.stream().collect(Collectors.groupingBy(RewardSku::getProductId));

        List<ProductSetRef> productSetRefList = newRewardLoaderClient.loadAllProductSetRef();
        List<ProductSet> productSets = newRewardLoaderClient.loadAllProductSet();

        if (CollectionUtils.isNotEmpty(indexList)) {
            for (Map<String, Object> bean : indexList) {
                Long proId = SafeConverter.toLong(bean.get("id"));
                Long mapOneLevelCategoryId = SafeConverter.toLong(bean.get("oneLevelCategoryId"));

                List<RewardImage> productImages = images.stream()
                        .filter(e -> e.getProductId() != null)
                        .filter(e -> Objects.equals(e.getProductId(), proId))
                        .collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(productImages)) {
                    String imgUrl = productImages.get(0).getLocation();
                    if (imgUrl.toLowerCase().startsWith("http")) {
                        bean.put("img", imgUrl);
                    } else {
                        String prePath = RuntimeMode.isUsingProductionData() ? "http://www.17zuoye.com" : "http://www.test.17zuoye.net";
                        bean.put("img", prePath + "/gridfs/" + imgUrl);
                    }
                }

                Set<Long> setIds = productSetRefList.stream().filter(ref -> !ref.getDisabled()).filter(ref -> Objects.equals(ref.getProductId(), proId)).map(ProductSetRef::getSetId).collect(Collectors.toSet());
                List<ProductSet> sets = productSets.stream().filter(p -> setIds.contains(p.getId())).collect(Collectors.toList());
                Map<Long, ProductSet> productSetMap = sets.stream().collect(Collectors.toMap(ProductSet::getId, Function.identity()));

                if (MapUtils.isNotEmpty(productSetMap)) {
                    String setNames = "";
                    for (ProductSet set : productSetMap.values()) {
                        setNames = setNames + set.getName() + ",";
                    }
                    bean.put("setNames", setNames);
                }

                if (newRewardLoaderClient.isSHIWU(mapOneLevelCategoryId)) {
                    List<RewardSku> rewardSkus = productIdSkuMap.get(proId);
                    if (rewardSkus != null) {
                        inventory = rewardSkus.stream()
                                .mapToInt(RewardSku::getInventorySellable)
                                .sum();
                        bean.put("inventory", inventory);
                    } else {
                        bean.put("inventory", 0);
                    }
                } else if (newRewardLoaderClient.isCoupon(mapOneLevelCategoryId)) {
                    Map<Long, List<RewardCouponDetail>> map = rewardLoaderClient.loadProductRewardCouponDetails(Arrays.asList(proId));
                    if (MapUtils.isEmpty(map) || CollectionUtils.isEmpty(map.get(proId))) {
                        bean.put("inventory", 0);
                        continue;
                    }
                    inventory = map.get(proId).stream().filter(e -> !Boolean.TRUE.equals(e.getExchanged())).collect(Collectors.toList()).size();
                    bean.put("inventory", inventory);
                } else {
                    bean.put("inventory", -1);
                }
            }
        }
    }

    @RequestMapping(value = "products.vpage")
    public String productlist() {
        return "reward/product/productlistnew";
    }

    @RequestMapping(value = "taglist.vpage")
    public String taglist() {
        return "reward/category/taglistnew";
    }

    @RequestMapping(value = "categorylist.vpage")
    public String categorylist() {
        return "reward/category/categorylistnew";
    }

    @RequestMapping(value = "editproduct.vpage")
    public String editproduct() {
        return "reward/product/addproductnew";
    }

    /**
     * 获取详情
     */
    @RequestMapping(value = "detail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage detail() {
        Long productId = getRequestLong("productId", 0L);
        MapMessage message = MapMessage.successMessage();
        List<GetProductDetailMapper.CategoryMapper> oneLevelCategoryMapper = newRewardLoaderClient.loadProductCategoryMapperByParentId(0L);
        message.add("oneLevelCategoryMapper", oneLevelCategoryMapper);
        message.add("couponResourceTypes", RewardCouponResource.values());
        if (Objects.equals(productId, 0L)) {
            return message;
        }

        RewardProduct product = crmRewardService.$loadRewardProduct(productId);
        List<RewardSku> skus = crmRewardService.$findRewardSkusByProductId(productId);
        this.biuldCategory(oneLevelCategoryMapper, product, message);
        this.biuldProductSet(productId, message);

        message.add("product", product);
        message.add("skus", skus);
        message.add("saleGroup", RewardOrderSaleGroup.values());
        String prePath = RuntimeMode.isUsingProductionData() ? "http://www.17zuoye.com" : "http://www.test.17zuoye.net";
        List<RewardImage> images = crmRewardService.$loadRewardImages();
        images = images.stream()
                .filter(e -> e.getProductId() != null)
                .filter(e -> Objects.equals(e.getProductId(), productId))
                .collect(Collectors.toList());
        message.add("images", images);
        message.add("prePath", prePath);
        Map<String, String> headWear = getHeadWears(product.getOneLevelCategoryId(), product.getRelateVirtualItemId());
        message.add("headWear", headWear);

        // 加载兑换券数据
        RewardCoupon coupon = crmRewardService.loadRewardCouponByPID(productId);
        if (coupon != null) {
            message.add("sendSms", coupon.getSendSms());
            message.add("sendMsg", coupon.getSendMsg());
            message.add("msgContent", coupon.getMsgTpl());
            message.add("smsContent", coupon.getMsgTpl());
            message.add("smsTpl", coupon.getSmsTpl());
        }
        return message;
    }

    /**
     * 编辑详情
     */
    @RequestMapping(value = "upsert.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage upsert(@RequestBody final CrmRewardProductMapper mapper) {
        RewardProduct rewardProduct = CrmRewardProductMapper.convert(mapper);

        if (rewardProduct.getOneLevelCategoryId() != null) {
            ProductCategory category = newRewardLoaderClient.loadProductCategoryById(mapper.getOneLevelCategoryId());
            OneLevelCategoryType typeEnum = OneLevelCategoryType.of(category.getOneLevelCategoryType());
            if (typeEnum == OneLevelCategoryType.JPZX_SHIWU) {
                rewardProduct.setProductType("JPZX_SHIWU");
            } else {
                rewardProduct.setProductType("JPZX_TIYAN");
            }
        }

        MapMessage resultMsg;

        try {
            String operation = rewardProduct.getId() != null ? "编辑奖品" : "添加奖品";String comment = getCurrentAdminUser().getAdminUserName() + "设置进货价为: " + rewardProduct.getBuyingPrice();
            addAdminLog(operation, rewardProduct.getId(), comment);

            List<ProductCategoryRef> productCategoryRefList = new ArrayList<>();
            ProductCategoryRef twoCategoryRef = new ProductCategoryRef();
            twoCategoryRef.setProductId(mapper.productId);
            twoCategoryRef.setCategoryId(mapper.getTwoLevelCategoryMapper().getTwoLevelCategoryId());
            productCategoryRefList.add(twoCategoryRef);

            List<ProductSetRef> productSetRefList = null;
            List<ProductTagRef> productTagRefList = new ArrayList<>();
            List<Map<String, Object>> setMapperList = mapper.getSetMapperList();
            if (CollectionUtils.isNotEmpty(setMapperList)) {
                productSetRefList = setMapperList
                        .stream()
                        .map(setMapper -> {
                            ProductSetRef productSetRef = new ProductSetRef();
                            productSetRef.setProductId(mapper.getProductId());
                            productSetRef.setSetId(SafeConverter.toLong(setMapper.get("setId")));
                            return productSetRef;
                        }).collect(Collectors.toList());
                setMapperList .stream() .forEach(setMapper -> {
                    if (SafeConverter.toLong(setMapper.get("tagId")) != 0) {
                        ProductTagRef tagRef = new ProductTagRef();
                        tagRef.setTagId(SafeConverter.toLong(setMapper.get("tagId")));
                        tagRef.setProductId(mapper.productId);
                        productTagRefList.add(tagRef);
                    }
                });
            }
            if (!Objects.equals(mapper.getTwoLevelCategoryMapper().getTagId(), 0L) && !Objects.isNull(mapper.getTwoLevelCategoryMapper().getTagId())) {
                ProductTagRef tagRef = new ProductTagRef();
                tagRef.setTagId(mapper.getTwoLevelCategoryMapper().getTagId());
                tagRef.setProductId(mapper.productId);
                productTagRefList.add(tagRef);
            }
            MapMessage messageHeadWear = upsertHeadWear(mapper);
            if (messageHeadWear.isSuccess()) {
                String relateVirtualItemId = messageHeadWear.get("id")==null ? null:messageHeadWear.get("id").toString();
                if (StringUtils.isNotBlank(relateVirtualItemId)) {
                    rewardProduct.setRelateVirtualItemId(relateVirtualItemId);
                }
            }
            resultMsg = newRewardServiceClient.addRewardProduct(rewardProduct, productCategoryRefList, productSetRefList, productTagRefList, mapper.getSkus());
            if (resultMsg.isSuccess()) {
                if (SafeConverter.toLong(resultMsg.get("productId"), 0) != 0) {
                    mapper.setProductId(SafeConverter.toLong(resultMsg.get("productId")));
                }
                String info = importCouponNo(mapper.getCouponNo(), mapper.productId);
                MapMessage messageImg = upsertImg(mapper);

                Long productId = SafeConverter.toLong(resultMsg.get("productId"));
                RewardCoupon newCoupon = CrmRewardProductMapper.extractCoupon(mapper);
                if (newCoupon != null) {
                    newCoupon.setProductId(productId);
                    // 学豆扣除备注设置成和产品名字一样
                    newCoupon.setIntegralComment(mapper.getProductName());

                    RewardCoupon existCoupon = crmRewardService.loadRewardCouponByPID(productId);
                    if (existCoupon != null) {
                        newCoupon.setId(existCoupon.getId());
                    }
                    rewardManagementClient.addRewardCoupon(newCoupon);
                }
                if (!messageHeadWear.isSuccess() && !messageImg.isSuccess()) {
                    resultMsg.setInfo("保存商品成功！" + messageHeadWear.getInfo() + "并且" + messageImg.getInfo() + info);
                } else if (!messageHeadWear.isSuccess()) {
                    resultMsg.setInfo("保存商品成功！" + messageHeadWear.getInfo() + info);
                } else {
                    resultMsg.setInfo("保存商品成功！" + messageImg.getInfo() + info);
                }
            }
        } catch (Exception ex) {
            logger.error("Failed to add reward product", ex);
            return MapMessage.errorMessage("编辑失败！");
        }
        return resultMsg;
    }

    public  Map<String, String> getHeadWears(Long oneLevelCategoryId, String relateVirtualItemId) {
        // 只显示可兑换的那些
        Map<String, String> headWears = new HashMap<>();
        if (oneLevelCategoryId==null || oneLevelCategoryId==0L) {
            return headWears;
        }
        ProductCategory oneLevelCategory = newRewardLoaderClient.loadProductCategoryById(oneLevelCategoryId);
        if (oneLevelCategory == null) {
            return headWears;
        }
        if (Objects.equals(oneLevelCategory.getOneLevelCategoryType(), OneLevelCategoryType.JPZX_TOBY.intType())) {
            TobyDress tobyDress = rewardCenterClient.loadTobyDressById(SafeConverter.toLong(relateVirtualItemId));
            if (tobyDress != null) {
                headWears.put("id", tobyDress.getId().toString());
                headWears.put("location", tobyDress.getUrl());
            }
            return headWears;
        } else {
            privilegeManagerClient.getPrivilegeManager()
                    .loadAllPrivilegesFromDB()
                    .getUninterruptibly()
                    .stream()
                    .filter(p -> Objects.equals(relateVirtualItemId, p.getId()))
                    .forEach(p -> {
                        headWears.put("id", p.getId());
                        headWears.put("location", p.getImg());
                    });

            return headWears;
        }
    }

    private MapMessage upsertHeadWear(final CrmRewardProductMapper mapper) {
        if (!Objects.isNull(mapper.getHeadwearMapper()) && StringUtils.isNotBlank(mapper.getHeadwearMapper().getFileName())) {
            ProductCategory oneLevelCategory = newRewardLoaderClient.loadProductCategoryById(mapper.getOneLevelCategoryId());
            String id = mapper.getHeadwearMapper().getId();
            if (Objects.equals(oneLevelCategory.getOneLevelCategoryType(), OneLevelCategoryType.JPZX_TOBY.intType())) {
                TobyDress tobyDress = rewardCenterClient.loadTobyDressById(SafeConverter.toLong(id));
                ProductCategory twoLevelCategory = newRewardLoaderClient.loadProductCategoryById(mapper.getTwoLevelCategoryMapper().getTwoLevelCategoryId());
                if (tobyDress == null) {
                    tobyDress = new TobyDress();
                }
                tobyDress.setName(mapper.getProductName());
                tobyDress.setType(twoLevelCategory.getTwoLevelCategoryType());
                tobyDress.setUrl(mapper.getHeadwearMapper().getFileName());

                tobyDress = rewardCenterClient.upsertTobyDress(tobyDress);
                if (tobyDress != null) {
                    return MapMessage.successMessage().add("id", tobyDress.getId());
                }
                return MapMessage.errorMessage("上传素材失败！");
            } else {
                if (StringUtils.isNotBlank(id)) {
                    Privilege headwear = privilegeManagerClient.getPrivilegeManager()
                            .loadAllPrivilegesFromDB()
                            .getUninterruptibly()
                            .stream()
                            .filter(e -> Objects.equals(e.getId(), id))
                            .findFirst()
                            .orElse(null);
                    if (!Objects.isNull(headwear)) {
                        if (!Objects.equals(headwear.getImg(), mapper.getHeadwearMapper().getFileName())) {
                            headwear.setImg(mapper.getHeadwearMapper().getFileName());
                            Privilege privilege = privilegeManagerClient.getPrivilegeManager().upsertPrivilege(headwear).awaitUninterruptibly().getUninterruptibly();
                            if (privilege != null) {
                                return MapMessage.successMessage();
                            }
                            return MapMessage.errorMessage("上传素材失败！");
                        }
                    }
                } else {
                    final String theId = null;
                    Privilege existHeadWear = new Privilege();
                    existHeadWear.setId(theId);
                    existHeadWear.setName(mapper.getProductName());
                    existHeadWear.setCode("DEFAULT");
                    existHeadWear.setType(PrivilegeType.Head_Wear);
                    existHeadWear.setOrigin(PrivilegeOrigin.REWARD);
                    existHeadWear.setDisplayInCenter(false);
                    existHeadWear.setAcquireCondition("DEFAULT");
                    existHeadWear.setImg(mapper.getHeadwearMapper().getFileName());
                    Privilege privilege = privilegeManagerClient.getPrivilegeManager()
                            .upsertPrivilege(existHeadWear)
                            .getUninterruptibly();
                    if (privilege != null) {
                        return MapMessage.successMessage().add("id", privilege.getId());
                    }
                    return MapMessage.errorMessage("上传素材失败！");
                }
            }
        }
        return MapMessage.successMessage();
    }

    private MapMessage upsertImg(final CrmRewardProductMapper mapper) {
        if (CollectionUtils.isEmpty(mapper.getImgMapperList())) {
            return MapMessage.successMessage();
        }
        MapMessage message = MapMessage.successMessage();
        List<Map<String, Object>> imgMapperList = mapper.getImgMapperList();
        for (Map<String, Object> imgMapper : imgMapperList) {
            RewardImage img = crmRewardService.$loadRewardImage(SafeConverter.toLong(imgMapper.get("id")));
            if (Objects.isNull(img) || !Objects.equals(img.getLocation(), imgMapper.get("fileName").toString())) {
                RewardImage image = new RewardImage();
                image.setProductId(SafeConverter.toLong(mapper.getProductId()));
                image.setLocation(imgMapper.get("fileName").toString());
                img = crmRewardService.$upsertRewardImage(image);
                if (Objects.isNull(img)) {
                    message = MapMessage.errorMessage("上传封面图片失败！");
                }
            }
        }
        return message;
    }

    //上传图片 奖品图片
    @RequestMapping(value = "uploadproductimage.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage uploadProductImage(MultipartFile file) {
        if (file.isEmpty()) {
            return MapMessage.errorMessage("没有文件上传");
        }

        try {
            String filePath = AdminOssManageUtils.upload(file, "reward");
            if (StringUtils.isBlank(filePath)) {
                return MapMessage.errorMessage("图片上传失败");
            }
            return MapMessage.successMessage().add("fileName", filePath);
        } catch (Exception ex) {
            logger.warn("upload file failed", ex);
            return MapMessage.errorMessage("图片上传失败");
        }
    }

    //删除图片
    @RequestMapping(value = "deleteproductimage.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteProductImage() {
        Long imageId = getRequestLong("imageId");
        RewardImage image = crmRewardService.$loadRewardImage(imageId);
        if (image == null) {
            return MapMessage.errorMessage("奖品图片{}不存在", imageId);
        }
        try {
            crmImageUploader.deletePhotoByFilename(image.getLocation());
            crmRewardService.$removeRewardImage(imageId);
            return MapMessage.successMessage();
        } catch (Exception ex) {
            logger.error("删除奖品图片异常", ex);
            return MapMessage.errorMessage("删除失败");
        }
    }

    @RequestMapping(value = "uploadheadwearimg.vpage", method = RequestMethod.POST)
    @ResponseBody
    @SneakyThrows
    public MapMessage uploadHeadWearImg(MultipartFile file) {
        try {
            String originalFileName = file.getOriginalFilename();
            String prefix = "hw-" + DateUtils.dateToString(new Date(), "yyyyMMdd");
            InputStream inStream = file.getInputStream();
            String filename = crmImageUploader.upload(prefix, originalFileName, inStream);
            if (StringUtils.isNotBlank(filename)) {
                return MapMessage.successMessage().add("fileName", filename);
            }
            return MapMessage.errorMessage("上传图片失败！");
        } catch (Exception ex) {
            logger.warn("upload file failed", ex);
            return MapMessage.errorMessage("上传图片失败！");
        }
    }

    @RequestMapping(value = "uploadtobyimg.vpage", method = RequestMethod.POST)
    @ResponseBody
    @SneakyThrows
    public MapMessage uploadTobyImg(MultipartFile file) {
        try {
            String filePath = AdminOssManageUtils.upload(file, "reward");
            if (StringUtils.isBlank(filePath)) {
                return MapMessage.errorMessage("图片上传失败");
            }
            return MapMessage.successMessage().add("fileName", filePath);
        } catch (Exception ex) {
            logger.warn("upload file failed", ex);
            return MapMessage.errorMessage("上传图片失败！");
        }
    }

    /**
     * 在列表页修改商品排序值
     * @return
     */
    @RequestMapping(value = "updateordervalue.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage updateOrderValue() {
        long productId = getRequestLong("productId");
        if (productId <= 0) {
            return MapMessage.errorMessage();
        }

        String valueType = getRequestString("valueType");
        Integer orderValue = getRequestInt("orderValue");

        RewardProduct product = crmRewardService.$loadRewardProduct(productId);
        if (product == null) {
            return MapMessage.errorMessage();
        }

        if (valueType.contains("student")) {
            product.setStudentOrderValue(orderValue);
        } else {
            product.setTeacherOrderValue(orderValue);
        }

        product.setUpdateDatetime(new Date());
        product = crmRewardService.$upsertRewardProduct(product);
        if (product != null) {
            return MapMessage.successMessage().setInfo("操作成功");
        } else {
            return MapMessage.errorMessage().setInfo("操作失败");
        }
    }

    /**
     * 上架下架
     * @return
     */
    @RequestMapping(value = "updownlined.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage upDownLined() {
        long productId = getRequestLong("productId");
        if (productId <= 0) {
            return MapMessage.errorMessage();
        }
        boolean onLined = getRequestBool("onLined");

        RewardProduct product = crmRewardService.$loadRewardProduct(productId);
        if (product == null) {
            return MapMessage.errorMessage();
        }
        product.setOnlined(onLined);
        if (onLined) {
            product.setOnlineDatetime(new Date());
        } else {
            product.setOfflineDatetime(new Date());
        }

        product = crmRewardService.$upsertRewardProduct(product);
        if (product != null) {
            return MapMessage.successMessage().setInfo("操作成功");
        } else {
            return MapMessage.errorMessage().setInfo("操作失败");
        }
    }

    private String importCouponNo(String couponNo, Long productId) {
        if (StringUtils.isBlank(couponNo)) {
            return StringUtils.EMPTY;
        }
        String[] couponNos = couponNo.trim().split("\n");
        List<String> lstSuccess = new ArrayList<>();
        List<String> lstFailed = new ArrayList<>();
        for (String no : couponNos) {
            String realNo = no.replaceAll(" ", "").trim();
            try {
                if (StringUtils.isNotBlank(realNo)) {
                    RewardCouponDetail detail = new RewardCouponDetail();
                    detail.setCouponNo(realNo);
                    detail.setProductId(productId);
                    if (rewardManagementClient.persistRewardCouponDetail(detail).isSuccess()) {
                        lstSuccess.add(realNo);
                    } else
                        lstFailed.add(realNo);
                } else {
                    lstFailed.add(realNo);
                }
            } catch (Exception ex) {
                lstFailed.add(realNo);
            }
        }
        List<RewardCouponDetail> detailList = rewardLoaderClient.getRewardCouponDetailLoader().loadProductRewardCouponDetails(productId);
        String info = String.format("\n导入兑换码成功条数：%s \n导入兑换码失败条数：%s \n当前兑换码条数：%s \n", lstSuccess.size(), lstFailed.size(), detailList.size());
        return info;
    }

    private List<GetProductDetailMapper.TagMapper> biuldProductTag(Integer tagParentType, Long tagParentId, Long productId) {
        List<ProductTag> productTagList = newRewardLoaderClient.loadProductTagByParent(tagParentId, tagParentType);
        List<Long> tagIdList = newRewardLoaderClient.loadTagIdListByProductId(productId);
        List<GetProductDetailMapper.TagMapper> tagMappers = productTagList
                .stream()
                .map(tag -> {
                    GetProductDetailMapper.TagMapper tagMapper = new GetProductDetailMapper().new TagMapper();
                    tagMapper.setId(tag.getId());
                    tagMapper.setName(tag.getName());
                    if (tagIdList.contains(tag.getId())) {
                        tagMapper.setIsSelected(true);
                    }
                    return tagMapper;})
                .collect(Collectors.toList());
        return tagMappers;
    }

    private void biuldProductSet(Long productId, MapMessage message) {
        Map<Long, ProductSet> productSetMap = newRewardLoaderClient.loadProductSetByProductId(productId);
        if (MapUtils.isNotEmpty(productSetMap)) {
            List<ProductSet> allProductSet = newRewardLoaderClient.loadAllProductSet();
            List<List<GetProductDetailMapper.SetMapper>> setMappers = productSetMap.values()
                    .stream()
                    .map(productSet -> {
                        List<GetProductDetailMapper.SetMapper> subSetMappers = allProductSet
                                .stream()
                                .map(allSetItem -> {
                                    GetProductDetailMapper.SetMapper setMapper = new GetProductDetailMapper().new SetMapper();
                                    setMapper.setId(allSetItem.getId());
                                    setMapper.setName(allSetItem.getName());
                                    if (Objects.equals(allSetItem.getId(), productSet.getId())) {
                                        List<GetProductDetailMapper.TagMapper> tagMappers = this.biuldProductTag(ProductTag.ParentType.SET.getType(), allSetItem.getId(), productId);
                                        if (CollectionUtils.isNotEmpty(tagMappers)) {
                                            setMapper.setTagMappers(tagMappers);
                                        }
                                        setMapper.setIsSelected(true);
                                    }
                                    return setMapper;
                                })
                                .collect(Collectors.toList());
                        return subSetMappers;
                    }).collect(Collectors.toList());
            message.add("setMappers", setMappers);
        }
    }

    private void biuldCategory(List<GetProductDetailMapper.CategoryMapper> oneLevelCategoryMapper, RewardProduct product,MapMessage message) {
        ProductCategoryRef twoLevelCategory = newRewardLoaderClient.loadProductCategoryRefByProductId(product.getId());
        List<GetProductDetailMapper.CategoryMapper> twoLevelCategoryMappers = newRewardLoaderClient.loadProductCategoryMapperByParentId(product.getOneLevelCategoryId());
        oneLevelCategoryMapper
                .stream()
                .filter(categoryMapper -> Objects.equals(categoryMapper.getId(), product.getOneLevelCategoryId()))
                .forEach(categoryMapper -> categoryMapper.setIsSelected(true));
        if (CollectionUtils.isNotEmpty(twoLevelCategoryMappers) && twoLevelCategory != null) {
            twoLevelCategoryMappers
                    .stream()
                    .filter(categoryMapper -> Objects.equals(categoryMapper.getId(), twoLevelCategory.getCategoryId()))
                    .forEach(categoryMapper -> categoryMapper.setIsSelected(true));
        }
        message.add("twoLevelCategoryMappers", twoLevelCategoryMappers);
        if (twoLevelCategory != null) {
            List<GetProductDetailMapper.TagMapper> tagMappers = this.biuldProductTag(ProductTag.ParentType.CATEGPRY.getType(), twoLevelCategory.getCategoryId(), product.getId());
            if (CollectionUtils.isNotEmpty(tagMappers)) {
                message.add("categoryTagMappers", tagMappers);
            }
        }
    }

}
