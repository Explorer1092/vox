package com.voxlearning.utopia.service.mizar.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.mizar.api.constants.MizarRatingActivity;
import com.voxlearning.utopia.service.mizar.api.entity.shop.*;
import com.voxlearning.utopia.service.mizar.api.service.MizarService;
import com.voxlearning.utopia.service.mizar.impl.dao.shop.*;
import com.voxlearning.utopia.service.user.api.UserIntegralService;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by Summer Yang on 2016/8/15.
 */
@Named
@Service(interfaceClass = MizarService.class)
@ExposeService(interfaceClass = MizarService.class)
public class MizarServiceImpl extends SpringContainerSupport implements MizarService {

    @Inject private AsyncMizarCacheServiceImpl asyncMizarCacheService;

    @Inject private MizarBrandDao mizarBrandDao;
    @Inject private MizarShopDao mizarShopDao;
    @Inject private MizarRatingDao mizarRatingDao;
    @Inject private MizarShopGoodsDao mizarShopGoodsDao;
    @Inject private MizarReserveRecordPersistence mizarReserveRecordPersistence;
    @Inject private MizarShopLikeDao mizarShopLikeDao;
    @Inject private StudentLoaderClient studentLoaderClient;
    @Inject private MizarCourseDao mizarCourseDao;
    @Inject private MizarCourseTargetDao mizarCourseTargetDao;
    @Inject private MizarCategoryDao mizarCategoryDao;

    @ImportService(interfaceClass = UserIntegralService.class) private UserIntegralService userIntegralService;

    //------------------------------------------------------------------------------
    //-------------------------          BRAND        ------------------------------
    //------------------------------------------------------------------------------
    @Override
    public MapMessage saveMizarBrand(MizarBrand brand) {
        try {
            MizarBrand upsert = mizarBrandDao.upsert(brand);
            MapMessage msg = new MapMessage();
            msg.setSuccess(upsert != null);
            msg.setInfo(upsert != null ? "保存成功" : "保存失败");
            msg.add("bid", upsert != null ? upsert.getId() : null);
            return msg;
        } catch (Exception ex) {
            return MapMessage.errorMessage("保存失败: " + ex.getMessage());
        }
    }

    //------------------------------------------------------------------------------
    //-------------------------           SHOP        ------------------------------
    //------------------------------------------------------------------------------
    @Override
    public MapMessage saveMizarShop(MizarShop shop) {
        try {
            MizarShop upsert = mizarShopDao.upsert(shop);
            MapMessage msg = new MapMessage();
            msg.setSuccess(upsert != null);
            msg.setInfo(upsert != null ? "保存成功" : "保存失败");
            msg.add("sid", upsert != null ? upsert.getId() : null);
            return msg;
        } catch (Exception ex) {
            return MapMessage.errorMessage("保存失败: " + ex.getMessage());
        }
    }

    @Override
    public void updateMizarShop(MizarShop mizarShop) {
        mizarShopDao.upsert(mizarShop);
    }

    @Override
    public MapMessage likedShop(Long parentId, String shopId, Integer activityId) {
        MizarShop shop = mizarShopDao.load(shopId);
        if (shop == null) {
            return MapMessage.errorMessage("投票的机构不存在");
        }
        // 投票
        MizarShopLike shopLike = new MizarShopLike();
        shopLike.setActivityId(activityId);
        shopLike.setShopId(shopId);
        shopLike.setShopName(shop.getFullName());
        shopLike.setUserId(parentId);
        List<String> firstCategory = shop.getFirstCategory();
        if (CollectionUtils.isNotEmpty(firstCategory)) {
            shopLike.setFirstCategory(firstCategory.get(0));
        }
        mizarShopLikeDao.insert(shopLike);
        if (Objects.equals(MizarRatingActivity.Collect_1.getId(), activityId)) {
            // 添加学豆 10个 给家长的第一个孩子
            List<User> childList = studentLoaderClient.loadParentStudents(parentId);
            if (CollectionUtils.isNotEmpty(childList)) {
                User child = childList.get(0);
                // 添加学豆
                IntegralHistory integralHistory = new IntegralHistory(child.getId(), IntegralType.STUDENT_MIZAR_LIKED_REWARD, 10);
                integralHistory.setComment("家长投票机构奖励学豆");
                userIntegralService.changeIntegral(integralHistory);
            }
        }
        return MapMessage.successMessage("投票成功");
    }

    //------------------------------------------------------------------------------
    //-------------------------          GOODS        ------------------------------
    //------------------------------------------------------------------------------
    @Override
    public MapMessage saveMizarShopGoods(MizarShopGoods goods) {
        try {
            MizarShopGoods upsert = mizarShopGoodsDao.upsert(goods);
            MapMessage msg = new MapMessage();
            msg.setSuccess(upsert != null);
            msg.setInfo(upsert != null ? "保存成功" : "保存失败");
            msg.add("gid", upsert != null ? upsert.getId() : null);
            return msg;
        } catch (Exception ex) {
            return MapMessage.errorMessage("保存失败: " + ex.getMessage());
        }
    }

    //------------------------------------------------------------------------------
    //-------------------------         RATING        ------------------------------
    //------------------------------------------------------------------------------
    @Override
    public MapMessage saveMizarRating(MizarRating rating) {
        try {
            // 需要加上限制 单个用户最多对一个店铺评论3条
            List<MizarRating> ratingList = mizarRatingDao.loadByUserId(rating.getUserId());
            if (CollectionUtils.isNotEmpty(ratingList)) {
                ratingList = ratingList.stream().filter(r -> StringUtils.equals(rating.getShopId(), r.getShopId()))
                        .collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(ratingList) && ratingList.size() >= 3) {
                    return MapMessage.errorMessage("您已经评论过了，谢谢您的支持");
                }
            }
            MizarRating upsert = mizarRatingDao.upsert(rating);
            MapMessage msg = new MapMessage();
            msg.setSuccess(upsert != null);
            msg.setInfo(upsert != null ? "保存成功" : "保存失败");
            msg.add("ratingId", upsert != null ? upsert.getId() : null);
            if (msg.isSuccess()) {
                // 记录评论次数每个月
                asyncMizarCacheService.MizarLikeShopMonthCountManager_increaseCount(rating.getUserId())
                        .awaitUninterruptibly();
            }
            return msg;
        } catch (Exception ex) {
            return MapMessage.errorMessage("保存失败: " + ex.getMessage());
        }
    }

    //------------------------------------------------------------------------------
    //-------------------------     RESERVE RECORD    ------------------------------
    //------------------------------------------------------------------------------

    @Override
    public MapMessage saveMizarReserve(MizarReserveRecord reserveRecord) {
        mizarReserveRecordPersistence.upsert(reserveRecord);
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage updateReservationStatus(Collection<Long> records, MizarReserveRecord.Status status) {
        mizarReserveRecordPersistence.updateStatus(records, status);
        return MapMessage.successMessage();
    }

    //------------------------------------------------------------------------------
    //-------------------------          COURSE       ------------------------------
    //------------------------------------------------------------------------------

    @Override
    public MapMessage saveMizarCourse(MizarCourse course) {
        try {
            MizarCourse upsert = mizarCourseDao.upsert(course);
            MapMessage msg = new MapMessage();
            msg.setSuccess(upsert != null);
            msg.setInfo(upsert != null ? "保存成功" : "保存失败");
            msg.add("courseId", upsert != null ? upsert.getId() : null);
            return msg;
        } catch (Exception ex) {
            return MapMessage.errorMessage("保存失败: " + ex.getMessage());
        }
    }

    @Override
    public MapMessage saveCourseTargets(String courseId, Integer type, Collection<String> targetList, Boolean isAppend) {
        if (StringUtils.isBlank(courseId) || type == 0 || CollectionUtils.isEmpty(targetList)) {
            return MapMessage.errorMessage("参数异常！");
        }
        MizarCourse course = mizarCourseDao.load(courseId);
        if (course == null) {
            return MapMessage.errorMessage("信息异常！");
        }
        // 追加模式不清除之前的数据
        if (!isAppend) {
            mizarCourseTargetDao.clearTarget(courseId, type);
            // 更新时间
            course.setUpdateAt(new Date());
            mizarCourseDao.upsert(course);
        }
        targetList = CollectionUtils.toLinkedHashSet(targetList);
        List<MizarCourseTarget> list = targetList.stream()
                .filter(StringUtils::isNotBlank)
                .map(target -> {
                    MizarCourseTarget at = new MizarCourseTarget();
                    at.setCourseId(courseId);
                    at.setTargetType(type);
                    at.setTargetStr(target);
                    at.setDisabled(false);
                    return at;
                })
                .collect(Collectors.toList());
        mizarCourseTargetDao.inserts(list);
        // 更新时间
        course.setUpdateAt(new Date());
        mizarCourseDao.upsert(course);
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage clearCourseTargets(String courseId, Integer type) {
        mizarCourseTargetDao.clearTarget(courseId, type);
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage removeMizarCourse(String courseId) {
        boolean success = mizarCourseDao.remove(courseId);
        if (success) {
            return MapMessage.successMessage();
        } else {
            return MapMessage.errorMessage("删除失败");
        }
    }

    @Override
    public MapMessage saveMizarCategory(MizarCategory category) {
        MizarCategory upsert = mizarCategoryDao.upsert(category);
        MapMessage retMsg = new MapMessage();
        retMsg.setSuccess(upsert == null);
        retMsg.setInfo(upsert == null ? "保存失败" : "保存成功");
        return retMsg;
    }

    @Override
    public MapMessage removeMizarCategory(String categoryId) {
        boolean success = mizarCategoryDao.remove(categoryId);
        if (success) {
            return MapMessage.successMessage();
        } else {
            return MapMessage.errorMessage("删除失败");
        }
    }
}
