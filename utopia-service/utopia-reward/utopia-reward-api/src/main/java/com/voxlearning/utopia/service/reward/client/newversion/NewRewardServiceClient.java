package com.voxlearning.utopia.service.reward.client.newversion;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.utopia.service.integral.api.mapper.UserIntegral;
import com.voxlearning.utopia.service.reward.api.DebrisService;
import com.voxlearning.utopia.service.reward.api.mapper.newversion.teacher.entity.TeacherCouponEntity;
import com.voxlearning.utopia.service.reward.api.newversion.NewRewardService;
import com.voxlearning.utopia.service.reward.cache.RewardCache;
import com.voxlearning.utopia.service.reward.consumer.RewardServiceClient;
import com.voxlearning.utopia.service.reward.entity.*;
import com.voxlearning.utopia.service.reward.entity.newversion.*;
import com.voxlearning.utopia.service.reward.mapper.RewardProductDetail;
import com.voxlearning.utopia.service.reward.mapper.product.crm.UpSertTagMapper;
import com.voxlearning.utopia.service.user.api.entities.TeacherExtAttribute;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.ResearchStaffDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class NewRewardServiceClient{

    private static final Logger logger = LoggerFactory.getLogger(NewRewardServiceClient.class);

    @ImportService(interfaceClass = NewRewardService.class)
    private NewRewardService remoteReference;
    @ImportService(interfaceClass = DebrisService.class) private DebrisService debrisService;
    @Inject
    private TeacherLoaderClient teacherLoaderClient;
    @Inject private NewRewardLoaderClient newRewardLoaderClient;

    public Debris loadDebris(Long userId) {
        return debrisService.loadDebrisByUserId(userId);
    }

    public MapMessage addRewardProduct(
            RewardProduct rewardProduct,
            List<ProductCategoryRef> productCategoryRefList,
            List<ProductSetRef> productSetRefList,
            List<ProductTagRef> productTagRefList,
            List<Map<String, Object>> skus) {
        return remoteReference.addRewardProduct(rewardProduct, productCategoryRefList, productSetRefList, productTagRefList, skus);
    }

    public ProductCategory upsertCategory(ProductCategory productCategory) {
        return remoteReference.upsertCategory(productCategory);
    }

    public ProductSet upsertSet(ProductSet productSet) {
        return remoteReference.upsertSet(productSet);
    }

    public Boolean deleteCategoryById(Long id) {
        return remoteReference.deleteCategoryById(id);
    }

    public Boolean deleteSetById(Long id) {
        return remoteReference.deleteSetById(id);
    }

    public MapMessage upsertTag(UpSertTagMapper upSertTagMapper) {
        return remoteReference.upsertTag(upSertTagMapper);
    }
    public Boolean deleteTagById(Long id) {
        return remoteReference.deleteTagById(id);
    }

    public Integer deleteProductTagRefByTagId(Long tagId) {
        return remoteReference.deleteProductTagRefByTagId(tagId);
    }

    public Integer getDiscountPrice(int quantity, RewardProductDetail productDetail, TeacherCouponEntity coupon) {
        Double price = coupon==null || coupon.getDiscount()==null ? productDetail.getDiscountPrice():productDetail.getDiscountPrice() * coupon.getDiscount();
        BigDecimal total = new BigDecimal(price).multiply(new BigDecimal(quantity));

        //托比装扮两件9折
        if (Objects.equals(quantity, 2) && newRewardLoaderClient.isTobyWear(productDetail.getOneLevelCategoryId())) {
            total = total.multiply(new BigDecimal(0.9));
        }
        return total.setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
    }

    protected Boolean isTeacherFreeZuoye(TeacherDetail teacher) {
        Boolean result = false;

        if (!teacher.isSchoolAmbassador() && !teacher.isJuniorTeacher()) {
            TeacherExtAttribute extAttribute = teacherLoaderClient.loadTeacherExtAttribute(teacher.getId());
            Integer teacherRal = extAttribute == null ? null : extAttribute.getRewardActiveLevel();
            if (teacherRal != null && teacherRal == 0) {
                result = true;
                Set<Long> allRelIds = teacherLoaderClient.loadRelTeacherIds(teacher.getId());
                Map<Long, TeacherExtAttribute> details = teacherLoaderClient.loadTeacherExtAttributes(allRelIds);
                for (TeacherExtAttribute detail : details.values()) {
                    if(detail.getRewardActiveLevel() != null && SafeConverter.toInt(detail.getRewardActiveLevel()) > 0) {
                        result = false;
                        break;
                    }
                }
            }
        }
        return result;
    }

    /**
     * 记录用户访当月是否访问过奖品中心
     * @param userId
     */
    public void recordAccessTrace(Long userId){
        // 存起来是给下个月看的，过期时间置成下个月月末
        MonthRange mr = MonthRange.current();
        long expireTime = mr.next().getEndTime();
        int ttl = (int)(expireTime - System.currentTimeMillis());

        String cacheKey = CacheKeyGenerator.generateCacheKey(
                "Reward:hadVisitedLastMonth",
                new String[]{"userId","month"},
                new Object[]{userId,mr.getMonth()});

        RewardCache.getPersistent().set(cacheKey, ttl, Boolean.TRUE);
    }

    /**
     * 返回上次穿戴商品过期提示日期
     * @param userId
     * @return
     */
    public Long fetchWearProductExpireTipTime(Long userId) {
        String cacheKey = CacheKeyGenerator.generateCacheKey(
                "Reward:WearProductExpireTipTime",
                new String[]{"userId"},
                new Object[]{userId});
        CacheObject<Long> cacheObject = RewardCache.getPersistent().get(cacheKey);
        if (Objects.nonNull(cacheObject)) {
            return cacheObject.getValue();
        }
        return 0L;
    }

    /**
     * 返回上次穿戴商品过期提示日期,默认5天过期
     * @param userId
     * @return
     */
    public Boolean setWearProductExpireTipTime(Long userId, Long tipTime) {
        Long expireDay =  DayRange.current().next().next().next().next().next().getStartDate().getTime();
        Long today = DayRange.current().getStartTime();
        Integer ttl = (int)(expireDay - today);

        String cacheKey = CacheKeyGenerator.generateCacheKey(
                "Reward:WearProductExpireTipTime",
                new String[]{"userId"},
                new Object[]{userId});

        return RewardCache.getPersistent().set(cacheKey, ttl, tipTime);
    }

    public MapMessage createRewardOrder(User user, RewardProductDetail productDetail, RewardSku sku, int quantity, RewardWishOrder wishOrder, RewardOrder.Source source, TeacherCouponEntity coupon) {
        if (user == null || productDetail == null) {
            return MapMessage.errorMessage();
        }
        int totalPrice = getDiscountPrice(quantity, productDetail, coupon);
        switch (user.fetchUserType()) {
            case TEACHER: {
                if (!(user instanceof TeacherDetail)) {
                    throw new IllegalArgumentException("User must be TeacherDetail");
                }
                TeacherDetail teacher = (TeacherDetail) user;
                if (isTeacherFreeZuoye(teacher)) {
                    return MapMessage.errorMessage("由于您超过30天未布置作业，园丁豆已经暂时冻结，检查作业且有学生完成即可解冻。");
                }
                // 判断余额
                UserIntegral integral = teacher.getUserIntegral();
                if (integral == null || integral.getUsable() < totalPrice) {
                    String unit = teacher.isPrimarySchool() ? "园丁豆" : "学豆";
                    return MapMessage.errorMessage("你的" + unit + "数量不足，请检查一下价格和数量吧！");
                }

                // 记录轨迹
                recordAccessTrace(user.getId());
                break;
            }
            case STUDENT: {
                if (!(user instanceof StudentDetail)) {
                    throw new IllegalArgumentException("User must be StudentDetail");
                }
                StudentDetail student = (StudentDetail) user;

                if (Objects.equals(productDetail.getSpendType(), RewardProduct.SpendType.FRAGMENT.intValue())) {
                    Debris debris = loadDebris(user.getId());
                    if (Objects.isNull(debris) || debris.getUsableDebris() < totalPrice) {
                        return MapMessage.errorMessage("你的碎片数量不足，请检查一下价格和数量吧！");
                    }
                } else {
                    // 判断余额
                    UserIntegral integral = student.getUserIntegral();
                    if (integral == null || integral.getUsable() < totalPrice) {
                        return MapMessage.errorMessage("你的学豆数量不足，请检查一下价格和数量吧！");
                    }
                }
                break;
            }
            default: {
                return MapMessage.errorMessage("角色错误");
            }
        }

        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("RewardService:createRewardOrder")
                    .keys(user.getId(), productDetail.getId(), Objects.isNull(sku) ? 0:sku.getId())
                    .callback(() -> remoteReference.createRewardOrder(user, productDetail, sku, quantity, wishOrder, source, coupon))
                    .build()
                    .execute();
        } catch (CannotAcquireLockException ex) {
            logger.error("Failed to create reward order (user={},product={},sku={},quantity={}): DUPLICATED OPERATION",
                    user.getId(), productDetail.getId(), Objects.isNull(sku) ? 0:sku.getId(), quantity);
            return MapMessage.errorMessage();
        } catch (Exception ex) {
            logger.error("Failed to create reward order (user={},product={},sku={},quantity={})",
                    user.getId(), productDetail.getId(), Objects.isNull(sku) ? 0:sku.getId(), quantity, ex);
            return MapMessage.errorMessage();
        }
    }

    public MapMessage sendTeachingResourceMsg(User user, Long productId) {
        return remoteReference.sendTeachingResourceMsg(user, productId);
    }
}
