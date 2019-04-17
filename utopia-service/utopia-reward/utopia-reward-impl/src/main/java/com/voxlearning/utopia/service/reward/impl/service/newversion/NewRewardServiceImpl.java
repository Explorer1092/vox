/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.reward.impl.service.newversion;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.jdbc.factory.UtopiaSqlFactory;
import com.voxlearning.alps.dao.jdbc.template.UtopiaSql;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.api.exception.NoEnoughBalanceException;
import com.voxlearning.utopia.api.legacy.MemcachedKeyConstants;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.Integral;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.privilege.client.PrivilegeLoaderClient;
import com.voxlearning.utopia.service.reward.api.mapper.newversion.teacher.entity.TeacherCouponEntity;
import com.voxlearning.utopia.service.reward.api.newversion.NewRewardService;
import com.voxlearning.utopia.service.reward.cache.RewardCache;
import com.voxlearning.utopia.service.reward.constant.OneLevelCategoryType;
import com.voxlearning.utopia.service.reward.constant.RewardConstants;
import com.voxlearning.utopia.service.reward.constant.RewardOrderStatus;
import com.voxlearning.utopia.service.reward.constant.TagIdLogicRelation;
import com.voxlearning.utopia.service.reward.consumer.newversion.RewardCenterClient;
import com.voxlearning.utopia.service.reward.entity.*;
import com.voxlearning.utopia.service.reward.entity.newversion.*;
import com.voxlearning.utopia.service.reward.impl.dao.*;
import com.voxlearning.utopia.service.reward.impl.dao.newversion.*;
import com.voxlearning.utopia.service.reward.impl.internal.InternalRewardCenterService;
import com.voxlearning.utopia.service.reward.impl.internal.InternalRewardSkuService;
import com.voxlearning.utopia.service.reward.impl.loader.RewardLoaderImpl;
import com.voxlearning.utopia.service.reward.impl.service.DebrisServiceImpl;
import com.voxlearning.utopia.service.reward.impl.version.RewardProductVersion;
import com.voxlearning.utopia.service.reward.mapper.RewardProductDetail;
import com.voxlearning.utopia.service.reward.mapper.product.crm.UpSertTagMapper;
import com.voxlearning.utopia.service.user.api.UserIntegralService;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.consumer.DeprecatedClazzLoaderClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Named
@ExposeService(interfaceClass = NewRewardService.class)
public class NewRewardServiceImpl extends SpringContainerSupport implements NewRewardService {

    @Inject private ProductCategoryDao productCategoryDao;
    @Inject private ProductSetDao productSetDao;
    @Inject private ProductTagDao productTagDao;
    @Inject private ProductTagRefDao productTagRefDao;
    @Inject private ProductSetRefDao productSetRefDao;
    @Inject private ProductCategoryRefDao productCategoryRefDao;
    @Inject private RewardCouponDao rewardCouponDao;

    @Inject private ProductCategoryRefVersion productCategoryRefVersion;
    @Inject private ProductTagRefVersion productTagRefVersion;
    @Inject private ProductSetRefVersion productSetRefVersion;

    @Inject private UtopiaSqlFactory utopiaSqlFactory;
    @Inject private InternalRewardSkuService internalRewardSkuService;
    @Inject private RewardSkuDao rewardSkuDao;
    @Inject private RewardProductVersion rewardProductVersion;
    @Inject private RewardProductDao rewardProductDao;

    @Inject private RewardLoaderImpl rewardLoader;
    @Inject private DebrisServiceImpl debrisService;
    @Inject private InternalRewardCenterService internalRewardCenterService;
    @Inject private NewRewardLoaderImpl newRewardLoader;
    @Inject private PrivilegeLoaderClient privilegeLoaderClient;
    @ImportService(interfaceClass = UserIntegralService.class) private UserIntegralService userIntegralService;
    @Inject private DeprecatedClazzLoaderClient deprecatedClazzLoaderClient;
    @Inject private RewardOrderPersistence rewardOrderPersistence;
    @Inject private RewardWishOrderPersistence rewardWishOrderPersistence;
    @Inject private RewardCenterSeviceImpl rewardCenterSevice;
    @Inject private TeacherLoaderClient teacherLoaderClient;

    private UtopiaSql utopiaSql;

    @Override
    public void afterPropertiesSet() throws Exception {
        utopiaSql = utopiaSqlFactory.getUtopiaSql("hs_reward");
    }

    @Override
    public MapMessage addRewardProduct(RewardProduct rewardProduct,  List<ProductCategoryRef> productCategoryRefList, List<ProductSetRef> productSetRefList, List<ProductTagRef> productTagRefList, List<Map<String, Object>> skus) {
        if (rewardProduct == null || skus == null) {
            return MapMessage.errorMessage("参数错误！");
        }
        if (StringUtils.isBlank(rewardProduct.getProductName())) {
            return MapMessage.errorMessage("参数错误！");
        }
        if (rewardProduct.getOneLevelCategoryId() <= 0) {
            return MapMessage.errorMessage("参数错误！");
        }
        if (Math.abs(rewardProduct.getPriceS() - 0) < 0.00001) {
            return MapMessage.errorMessage("学生原始积分不能为零！");
        }
        if (CollectionUtils.isEmpty(productCategoryRefList)) {
            return MapMessage.errorMessage("参数错误！");
        }


        try {
            final MapMessage resultMsg = MapMessage.successMessage();
            utopiaSql.withTransaction(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                    Long productId;
                    if (rewardProduct.getId() != null) {
                        productId = rewardProduct.getId();
                        rewardProductDao.replace(rewardProduct);
                    } else {
                        rewardProductDao.insert(rewardProduct);
                        productId = rewardProduct.getId();
                    }

                    //处理商品分类关系
                    ProductCategoryRef categoryRef = productCategoryRefDao.loadByProductId(productId);
                    if (Objects.nonNull(categoryRef)) {
                        if (!Objects.equals(productCategoryRefList.get(0).getCategoryId(), categoryRef.getCategoryId())) {
                            productCategoryRefDao.deleteByProductId(productId);

                            productCategoryRefList.get(0).setProductId(productId);
                            productCategoryRefDao.insert(productCategoryRefList.get(0));
                        }
                    } else {
                        productCategoryRefList.get(0).setProductId(productId);
                        productCategoryRefDao.insert(productCategoryRefList.get(0));
                    }

                    //处理商品分类集合关系
                    if (CollectionUtils.isEmpty(productSetRefList)) {
                        productSetRefDao.deleteByProductId(productId);
                    } else {
                        List<ProductSetRef> setRefs = productSetRefDao.loadByProductId(productId);
                        if (CollectionUtils.isEmpty(setRefs)) {
                            productSetRefList.stream().filter(setRef -> !Objects.equals(setRef.getSetId(), 0L)).forEach(setRef -> setRef.setProductId(productId));
                            if (CollectionUtils.isNotEmpty(productSetRefList)) {
                                productSetRefDao.inserts(productSetRefList);
                            }
                        } else {
                            Set<Long> oldSetIdSet = setRefs.stream().map(ProductSetRef::getSetId).collect(Collectors.toSet());
                            Set<Long> newSetIdSet = productSetRefList.stream().map(ProductSetRef::getSetId).collect(Collectors.toSet());

                            List<Long> deleteRefs = oldSetIdSet.stream().filter(oldTagId -> !newSetIdSet.contains(oldTagId)).collect(Collectors.toList());
                            productSetRefDao.deleteByProductIdAndSetIds(productId, deleteRefs);
                            List<ProductSetRef> insertRefs = productSetRefList.stream().filter(ref -> !oldSetIdSet.contains(ref.getSetId())).collect(Collectors.toList());
                            productSetRefDao.inserts(insertRefs);
                        }

                    }

                    //处理商品标签关系
                    if (CollectionUtils.isEmpty(productTagRefList)) {
                        productTagRefDao.deleteByProductId(productId);
                    } else {
                        List<ProductTagRef> tagRefs = productTagRefDao.loadByProductId(productId);
                        if (CollectionUtils.isEmpty(tagRefs)) {
                            productTagRefList.stream().forEach(tagRef -> tagRef.setProductId(productId));
                            productTagRefDao.inserts(productTagRefList);
                        } else {
                            Set<Long> oldTagIdSet = tagRefs.stream().map(ProductTagRef::getTagId).collect(Collectors.toSet());
                            Set<Long> newTagIdSet = productTagRefList.stream().map(ProductTagRef::getTagId).collect(Collectors.toSet());

                            List<Long> deleteRefs = oldTagIdSet.stream().filter(oldTagId -> !newTagIdSet.contains(oldTagId)).collect(Collectors.toList());
                            productTagRefDao.deleteByProductIdAndTagIds(productId, deleteRefs);
                            List<ProductTagRef> insertRef = productTagRefList.stream().filter(ref -> !oldTagIdSet.contains(ref.getTagId())).collect(Collectors.toList());
                            productTagRefDao.inserts(insertRef);
                        }

                    }

                    //单品
                    List<RewardSku> skuList = internalRewardSkuService.$findRewardSkusByProductId(productId)
                            .stream().collect(Collectors.toList());
                    List<RewardSku> delList = new ArrayList<>();
                    for (Map<String, Object> skuMap : skus) {
                        if (skuMap.get("skuId") != null && StringUtils.isNotBlank(skuMap.get("skuId").toString())) {
                            for (RewardSku sku : skuList) {
                                if (conversionService.convert(skuMap.get("skuId").toString(), Long.class).equals(sku.getId())) {
                                    delList.add(sku);
                                }
                            }
                            long skuId = SafeConverter.toLong(skuMap.get("skuId"));
                            RewardSku sku = internalRewardSkuService.$loadRewardSku(skuId);
                            sku.setSkuName(skuMap.get("skuName").toString());
                            sku.setInventorySellable(ConversionUtils.toInt(skuMap.get("skuQuantity")));
                            rewardSkuDao.replace(sku);
                        } else {
                            RewardSku sku = new RewardSku();
                            sku.setProductId(productId);
                            sku.setSkuName(skuMap.get("skuName").toString());
                            sku.setInventorySellable(ConversionUtils.toInt(skuMap.get("skuQuantity")));
                            rewardSkuDao.insert(sku);
                        }
                    }
                    skuList.removeAll(delList);
                    for (RewardSku sku : skuList) {
                        internalRewardSkuService.$removeRewardSku(sku.getId());
                    }
                }
            });
            rewardProductVersion.increase();

            productCategoryRefVersion.increment();
            productTagRefVersion.increment();
            productSetRefVersion.increment();

            // 放上商品id，供后面兑换券生成的时候关联用
            resultMsg.add("productId", rewardProduct.getId());
            return resultMsg.setInfo("编辑成功");
        } catch (Exception ex) {
            logger.error("Failed to add reward product", ex);
            return MapMessage.errorMessage("编辑失败！");
        }
    }
    @Override
    public ProductCategory upsertCategory(ProductCategory productCategory) {
        return productCategoryDao.upsert(productCategory);
    }

    @Override
    public ProductSet upsertSet(ProductSet upsertSet) {
        return productSetDao.upsert(upsertSet);
    }

    @Override
    public Boolean deleteCategoryById(Long id) {
        return productCategoryDao.disable(id);
    }

    @Override
    public Boolean deleteSetById(Long id) {
        return productSetDao.disable(id);
    }

    @Override
    public MapMessage upsertTag(UpSertTagMapper upSertTagMapper) {
        MapMessage message = MapMessage.successMessage();
        ProductTag tag = new ProductTag();
        BeanUtils.copyProperties(upSertTagMapper, tag);
        tag = productTagDao.upsert(tag);
        if (tag == null) {
            message = MapMessage.errorMessage("保存标签信息失败！");
        }
        return message;
    }

    @Override
    public Boolean deleteTagById(Long id) {
        return productTagDao.disable(id);
    }

    @Override
    public Integer deleteProductTagRefByTagId(Long tagId) {
        return productTagRefDao.deleteByTagId(tagId);
    }

    @Override
    public MapMessage createRewardOrder(final User user,
                                        final RewardProductDetail productDetail,
                                        final RewardSku sku,
                                        final int quantity,
                                        final RewardWishOrder wishOrder,
                                        RewardOrder.Source source) {
        return createRewardOrder(user, productDetail, sku, quantity, wishOrder, source, null);
    }

    @Override
    public MapMessage createRewardOrder(User user, RewardProductDetail productDetail, RewardSku sku, int quantity, RewardWishOrder wishOrder, RewardOrder.Source source, TeacherCouponEntity coupon) {
        if (user == null || productDetail == null) {
            return MapMessage.errorMessage();
        }

        Set<Long> tagIds = newRewardLoader.getProductTagIdByProductId(productDetail.getId());
        if (user.isStudent() && tagIds.contains(TagIdLogicRelation.PUBLIC_GOOD.getNumber())) {
            StudentDetail studentDetail = (StudentDetail)user;
            if (studentDetail.isPrimaryStudent()) {//只限制小学
                Long donationCount = internalRewardCenterService.getDonationCount(user.getId());
                if (donationCount < 5) {
                    return MapMessage.errorMessage("至少参加过五次公益活动才可兑换！");
                }
            }
        }

        try {
            // 如果是实物这里先修改库存 防止并发的情况
            if (Objects.nonNull(sku)) {
                if (rewardSkuDao.decreaseInventorySellable(sku.getId(), quantity) == 0) {
                    logger.warn("Failed to descrease reward sku {} inventory, rollback", sku.getId());
                    return MapMessage.errorMessage("奖品数量不足！");
                }
            }

            // 如果是虚拟头饰，校验不能重复购买
            MapMessage validateResult = validateTopknot(user, productDetail);
            if (!validateResult.isSuccess()) {
                return validateResult;
            }

            int cost = getDiscountPrice(quantity, productDetail, coupon);
            int totalCost = cost;
            if (user.isTeacher()) {
                TeacherDetail teacher = (TeacherDetail) user;
                if (teacher.isPrimarySchool()) {
                    cost = cost * 10;
                }
            }
            if (user.isResearchStaff()) {
                cost = cost * 10;
            }

            String comment = "兑换奖品";
            if (user.isStudent()) {
                comment = RewardConstants.STUSENT_REWARD_NAME + comment;
            } else {
                comment = RewardConstants.TEACHER_REWARD_NAME + comment;
            }
            if (productDetail.getSpendType() != null && productDetail.getSpendType() == RewardProduct.SpendType.FRAGMENT.intValue()) {
                MapMessage result = debrisService.changeDebris(user.getId(), DebrisType.TOBY.getType(), -cost *1L, comment);
                if (!result.isSuccess()) {
                    throw new NoEnoughBalanceException();
                }
            } else {
                // 赠品来源的不扣学豆  |  ??? 这注释过期了吧
                // 区分学豆类型,方便后期针对不同类型累加能量值
                IntegralType integralType = this.fetchIntegralByExchangProduct(productDetail.getOneLevelCategoryId());

                IntegralHistory integralHistory = new IntegralHistory(user.getId(), integralType, -cost);
                integralHistory.setComment(fetchRewardIntegralComment(integralHistory.toIntegralType()));
                if (!userIntegralService.changeIntegral(integralHistory).isSuccess()) {
                    throw new NoEnoughBalanceException();
                }
            }

            RewardOrder order = new RewardOrder();
            if (Objects.nonNull(sku)) {
                order.setSkuName(sku.getSkuName());
                order.setSkuId(sku.getId());
            } else {
                order.setSkuName(StringUtils.EMPTY);
                order.setSkuId(0L);
            }
            order.setCode("");//订单号暂时没用， 先留着
            order.setProductId(productDetail.getId());
            order.setProductName(productDetail.getProductName());
            order.setSaleGroup(productDetail.getSaleGroup());
            order.setUnit(productDetail.getUnit());
            order.setTotalPrice(SafeConverter.toDouble(totalCost)); // 取四舍五入后的值
            order.setBuyerId(user.getId());
            order.setBuyerType(user.getUserType());
            order.setBuyerName(user.fetchRealname());
            order.setPrice(productDetail.getDiscountPrice());
            order.setQuantity(quantity);
            order.setProductType(String.valueOf(newRewardLoader.getOneLevelCategoryType(productDetail.getOneLevelCategoryId())));
            order.setProductCategory(String.valueOf(productDetail.getOneLevelCategoryId()));

            // 设置班级ID
            Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(user.getId());
            if (user.isStudent() && clazz != null) {
                order.setClazzId(clazz.getId());
            }

            // 设置子类别编码字段

            // 如果是体验类的商品，没有发货流程，直接是结束状态
            if (newRewardLoader.isSHIWU(productDetail.getOneLevelCategoryId())) {
                order.setStatus(RewardOrderStatus.SUBMIT.name());
            } else {
                order.setStatus(RewardOrderStatus.DELIVER.name());
            }

            order.setDiscount(this.getDiscount(productDetail, coupon));
            order.setSource(source);
            order.setSpendType(productDetail.getSpendType());
            rewardOrderPersistence.insert(order);
            // 修改产品卖出量
            if (rewardProductDao.increaseSoldQuantity(productDetail.getId(), quantity) == 0) {
                logger.warn("Failed to increase reward product {} sold quantity, rollback", productDetail.getId());
                throw new RuntimeException();
            }
            rewardProductVersion.increase();

            Collection<String> keys = new LinkedHashSet<>();
            keys.add(CacheKeyGenerator.generateCacheKey(MemcachedKeyConstants.REWARD_USER_ORDER_COUNT, null, new Object[]{user.getId()}));
            if (wishOrder != null) {
                if (rewardWishOrderPersistence.achievedWishOrderById(wishOrder.getId()) > 0) {
                    keys.add(CacheKeyGenerator.generateCacheKey(MemcachedKeyConstants.REWARD_USER_WISH_ORDER, null, new Object[]{user.getId()}));
                }
            }
            RewardCache.getRewardCache().delete(keys);
            return MapMessage.successMessage().add("orderId",order.getId());
        } catch (Exception ex) {
            String errorMessage;
            if (ex instanceof NoEnoughBalanceException) {
                errorMessage = "Failed to create reward order (userId={},productId={},skuId={},quantity={}): no enough balance";
            } else {
                errorMessage = "Failed to create reward order (userId={},productId={},skuId={},quantity={})";
            }
            logger.error(errorMessage, user.getId(), productDetail.getId(), Objects.isNull(sku) ? 0:sku.getId(), quantity);
            return MapMessage.errorMessage("兑换失败，请重试");
        }
    }

    @Override
    public MapMessage sendTeachingResourceMsg(User user, Long productId) {
        RewardCoupon coupon = rewardCouponDao.loadRewardCouponByPID(productId);
        if (user.isTeacher()) {
            teacherLoaderClient.sendTeacherMessage(user.getId(), coupon.getMsgTpl());
        }
        return MapMessage.successMessage();
    }

    /**
     * 通过商品的一级分类得到商品的兑换积分类型
     * @param oneLevelCategoryId
     * @return
     */
    public IntegralType fetchIntegralByExchangProduct(Long oneLevelCategoryId) {
        if (newRewardLoader.isSHIWU(oneLevelCategoryId)) {
            return IntegralType.of(OneLevelCategoryType.JPZX_SHIWU.getIntegralExchangeType());
        } else if (newRewardLoader.isHeadWear(oneLevelCategoryId)) {
            return IntegralType.of(OneLevelCategoryType.JPZX_HEADWEAR.getIntegralExchangeType());
        } else if (newRewardLoader.isTeachingResources(oneLevelCategoryId)) {
            return IntegralType.of(OneLevelCategoryType.JPZX_TEACHING_RESOURCES.getIntegralExchangeType());
        } else if (newRewardLoader.isTobyWear(oneLevelCategoryId)) {
            return IntegralType.of(OneLevelCategoryType.JPZX_TOBY.getIntegralExchangeType());
        } else if (newRewardLoader.isFlowPacket(oneLevelCategoryId)) {
            return IntegralType.of(OneLevelCategoryType.JPZX_FLOW_PACKET.getIntegralExchangeType());
        } else if (newRewardLoader.isCoupon(oneLevelCategoryId)) {
            return IntegralType.of(OneLevelCategoryType.JPZX_COUPON.getIntegralExchangeType());
        } else if (newRewardLoader.isMiniCourse(oneLevelCategoryId)) {
            return IntegralType.of(OneLevelCategoryType.JPZX_MINI_COURSE.getIntegralExchangeType());
        } else {
            return IntegralType.UNKNOWN;
        }
    }

    /**
     * 通过商品的一级分类得到商品的取消积分类型
     * @param oneLevelCategoryId
     * @return
     */
    public IntegralType fetchIntegralByCancelProduct(Long oneLevelCategoryId) {
        if (newRewardLoader.isSHIWU(oneLevelCategoryId)) {
            return IntegralType.of(OneLevelCategoryType.JPZX_SHIWU.getIntegralIncomeType());
        } else if (newRewardLoader.isHeadWear(oneLevelCategoryId)) {
            return IntegralType.of(OneLevelCategoryType.JPZX_HEADWEAR.getIntegralIncomeType());
        } else if (newRewardLoader.isTeachingResources(oneLevelCategoryId)) {
            return IntegralType.of(OneLevelCategoryType.JPZX_TEACHING_RESOURCES.getIntegralIncomeType());
        } else if (newRewardLoader.isTobyWear(oneLevelCategoryId)) {
            return IntegralType.of(OneLevelCategoryType.JPZX_TOBY.getIntegralIncomeType());
        } else if (newRewardLoader.isFlowPacket(oneLevelCategoryId)) {
            return IntegralType.of(OneLevelCategoryType.JPZX_FLOW_PACKET.getIntegralIncomeType());
        } else if (newRewardLoader.isCoupon(oneLevelCategoryId)) {
            return IntegralType.of(OneLevelCategoryType.JPZX_COUPON.getIntegralIncomeType());
        } else if (newRewardLoader.isMiniCourse(oneLevelCategoryId)) {
            return IntegralType.of(OneLevelCategoryType.JPZX_MINI_COURSE.getIntegralIncomeType());
        } else {
            return IntegralType.UNKNOWN;
        }
    }

    /**
     * 通过商品的一级分类得到商品的取消积分类型
     * @param integralType
     * @return
     */
    public String fetchRewardIntegralComment(IntegralType integralType) {
        if (Objects.equals(integralType, IntegralType.TEACHER_OPEN_REWARD_BOX)) {
            return "教学用品中心试手气获得";
        } else if (Objects.equals(integralType, IntegralType.REWARD_CENTER_OPEN_BOX)) {
            return "教学用品中心试手气支出";
        } else if (Objects.equals(integralType, IntegralType.REWARD_COLLECTION_REWARD)) {
            return "代全校学生收货奖励";
        } else if (Objects.equals(integralType, IntegralType.REWARD_EXCHANGE_ACTUAL_ITEMS_REDUCTION_INTEGRAL)) {
            return "累积兑换不满足包邮条件扣除物流积分";
        } else if (Objects.equals(integralType, IntegralType.PRIMARY_STUDENT_APP_LOTTERY)) {
            return "学习用品中心抽奖活动积分";
        } else if (Objects.equals(integralType, IntegralType.REWARD_TOBY_INTEGRAL)) {
            return "托比装扮兑换";
        } else if (Objects.equals(integralType, IntegralType.REWARD_GAME_CLAW_GOT_INTEGRAL)) {
            return "学习用品中心小游戏获得";
        } else if (Objects.equals(integralType, IntegralType.REWARD_GAME_CLAW_PAY_INTEGRAL)) {
            return "学习用品中心小游戏支出";
        } else if (Objects.equals(integralType, IntegralType.REWARD_PUBLIC_GOOD_INTEGRAL)) {
            return "一起公益捐赠";
        } else if (Objects.equals(integralType, IntegralType.REWARD_SWGOOD_EXCHANGE_INTEGRAL)) {
            return "实物商品兑换";
        } else if (Objects.equals(integralType, IntegralType.REWARD_XNGOOD_EXCHANGE_INTEGRAL)) {
            return "学习用品中心头饰兑换";
        } else if (Objects.equals(integralType, IntegralType.REWARD_XNGOOD_INCOME_INTEGRAL)) {
            return "学习用品中心头饰取消";
        } else if (Objects.equals(integralType, IntegralType.REWARD_SWGOOD_INCOME_INTEGRAL)) {
            return "实物商品取消";
        } else if (Objects.equals(integralType, IntegralType.REWARD_MINICOURSE_EXCHANGE_INTEGRAL)) {
            return "微课兑换";
        } else if (Objects.equals(integralType, IntegralType.REWARD_TEACHINGRESOURCES_EXCHANGE_INTEGRAL)) {
            return "教学资源兑换";
        } else if (Objects.equals(integralType, IntegralType.REWARD_FLOWPACKET_EXCHANGE_INTEGRAL)) {
            return "流量包兑换";
        } else if (Objects.equals(integralType, IntegralType.REWARD_COUPON_EXCHANGE_INTEGRAL)) {
            return "奖品中心优惠券兑换";
        } else if (Objects.equals(integralType, IntegralType.REWARD_MINICOURSE_INCOME_INTEGRAL)) {
            return "微课取消";
        } else if (Objects.equals(integralType, IntegralType.REWARD_TOBY_INCOME_INTEGRAL)) {
            return "托比装扮取消";
        } else if (Objects.equals(integralType, IntegralType.REWARD_TEACHINGRESOURCES_INCOME_INTEGRAL)) {
            return "教学资源取消";
        } else if (Objects.equals(integralType, IntegralType.REWARD_FLOWPACKET_INCOME_INTEGRAL)) {
            return "流量包取消";
        } else if (Objects.equals(integralType, IntegralType.REWARD_COUPON_INCOME_INTEGRAL)) {
            return "奖品中心优惠券取消";
        } else {
            return "奖品中心积分";
        }
    }

    /**
     * 如果是虚拟头饰，校验不能重复购买
     * @param user
     * @param productDetail
     * @return
     */
    private MapMessage validateTopknot(final User user,
                                       final RewardProductDetail productDetail) {
        if (newRewardLoader.isHeadWear(productDetail.getOneLevelCategoryId())) {
            // 取现在拥有的在有效期以内的头饰
            if (privilegeLoaderClient.existValidPrivilege(user.getId(), productDetail.getRelateVirtualItemId())) {
                // logger.warn("Failed to create order,the headdress already exists!");
                return MapMessage.errorMessage("不能重复购买头饰!");
            }

        } else if (newRewardLoader.isMiniCourse(productDetail.getOneLevelCategoryId())) {
            // 微课，查询历史的订单记录
            Map<Long, List<RewardOrder>> historyOrderMap = rewardLoader.loadUserRewardOrders(Collections.singleton(user.getId()));
            if (historyOrderMap != null) {

                List<RewardOrder> historyOrders = historyOrderMap.get(user.getId());
                if (CollectionUtils.isNotEmpty(historyOrders)) {
                    // 买过的不能重复购买
                    RewardOrder boughtOrder = historyOrders
                            .stream()
                            .filter(o -> Objects.equals(o.getProductId(), productDetail.getId()))
                            .filter(o -> Objects.equals(o.getStatus(), RewardOrderStatus.DELIVER.name()))
                            .findAny()
                            .orElse(null);
                    if (boughtOrder != null) {
                        return MapMessage.errorMessage("不能重复购买课程!");
                    }
                }
            }
        } else if (newRewardLoader.isTeachingResources(productDetail.getOneLevelCategoryId())) {
            // 微课，查询历史的订单记录
            Map<Long, List<RewardOrder>> historyOrderMap = rewardLoader.loadUserRewardOrders(Collections.singleton(user.getId()));
            if (historyOrderMap != null) {

                List<RewardOrder> historyOrders = historyOrderMap.get(user.getId());
                if (CollectionUtils.isNotEmpty(historyOrders)) {
                    // 买过的不能重复购买
                    RewardOrder boughtOrder = historyOrders
                            .stream()
                            .filter(o -> Objects.equals(o.getProductId(), productDetail.getId()))
                            .filter(o -> Objects.equals(o.getStatus(), RewardOrderStatus.DELIVER.name()))
                            .findAny()
                            .orElse(null);
                    if (boughtOrder != null) {
                        return MapMessage.errorMessage("不能重复购买教学资源!");
                    }
                }
            }
        } else if (newRewardLoader.isTobyWear(productDetail.getOneLevelCategoryId())) {
            if (rewardCenterSevice.isOwnTobyDress(user.getId(), productDetail.getId())) {
                return MapMessage.errorMessage("不能重复购买托比装扮!");
            }
        }
        return MapMessage.successMessage();
    }

    private Integer getDiscountPrice(int quantity, RewardProductDetail productDetail, TeacherCouponEntity coupon) {
        Double price = coupon==null || coupon.getDiscount()==null ? productDetail.getDiscountPrice():productDetail.getDiscountPrice() * coupon.getDiscount();
        BigDecimal total = new BigDecimal(price).multiply(new BigDecimal(quantity));

        //托比装扮两件9折
        if (Objects.equals(quantity, 2) && newRewardLoader.isTobyWear(productDetail.getOneLevelCategoryId())) {
            total = total.multiply(new BigDecimal(0.9));
        }

        return total.setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
    }

    private Double getDiscount(RewardProductDetail productDetail, TeacherCouponEntity coupon) {
        Double result = null;
        if (productDetail.getDiscount()!=null && coupon!=null && coupon.getDiscount()!=null) {
            result = productDetail.getDiscount() * coupon.getDiscount();
        } else if (productDetail.getDiscount()!=null) {
            result = productDetail.getDiscount();
        } else if (coupon!=null && coupon.getDiscount()!=null) {
            result = coupon.getDiscount();
        }
        return result;
    }
}
