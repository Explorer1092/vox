package com.voxlearning.utopia.mizar.controller.biz;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.mizar.controller.AbstractMizarController;
import com.voxlearning.utopia.service.mizar.api.constants.MizarRatingStatus;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarRating;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarShop;
import com.voxlearning.utopia.service.mizar.consumer.loader.MizarRatingLoaderClient;
import com.voxlearning.utopia.service.mizar.consumer.service.MizarRatingServiceClient;
import com.voxlearning.utopia.service.user.consumer.SensitiveUserDataServiceClient;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 评论管理-用户点评 Controller
 * Created by Yuechen.Wang on 2016/9/17.
 */
@Controller
@RequestMapping(value = "/biz/rating")
public class RatingManageController extends AbstractMizarController {

    private static final int RATING_PAGE_SIZE = 10;
    @Inject private MizarRatingLoaderClient mizarRatingLoaderClient;
    @Inject private MizarRatingServiceClient mizarRatingServiceClient;
    @Inject private SensitiveUserDataServiceClient sensitiveUserDataServiceClient;

    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        String shopToken = getRequestString("shopToken"); // 机构名称
        String status = getRequestString("status"); // 评论内容
        String content = getRequestString("content"); // 评论状态
        Integer ratingStar = getRequestInt("ratingStar"); // 评论星数
        Integer page = getRequestInt("page", 1); // 当前页

        Map<String, Object> conditionMap = new HashMap<>();
        conditionMap.put("ratingStar", ratingStar);
        if (StringUtils.isBlank(status)) status = MizarRatingStatus.PENDING.name();
        conditionMap.put("status", status);
        conditionMap.put("content", content);
        model.addAllAttributes(conditionMap);
        model.addAttribute("page", page);
        model.addAttribute("shopToken", shopToken);

        Map<String, MizarShop> shopMap;
        Map<String, List<MizarRating>> shopRatings;
        if (StringUtils.isBlank(shopToken)) {
            List<MizarRating> ratingList = mizarRatingLoaderClient.loadRatingByParam(ratingStar, status, content);
            shopRatings = ratingList.stream().collect(Collectors.groupingBy(MizarRating::getShopId));
            Set<String> shopId = ratingList.stream().map(MizarRating::getShopId).collect(Collectors.toSet());
            shopMap = mizarLoaderClient.loadShopByIds(shopId);
        } else {
            List<MizarShop> shopList = new ArrayList<>();
            // 输入的是shopId
            if (ObjectId.isValid(shopToken)) {
                MizarShop shop = mizarLoaderClient.loadShopById(shopToken);
                if (shop != null) shopList.add(shop);
            } else if (StringUtils.isNotBlank(shopToken)) {
                shopList = mizarRatingLoaderClient.loadShopByName(shopToken, 10);
            }
            shopMap = shopList.stream().collect(Collectors.toMap(MizarShop::getId, Function.identity()));
            shopRatings = mizarRatingLoaderClient.loadAllRatingByShop(shopMap.keySet());
        }

        List<Map<String, Object>> ratingInfo = mapRatingInfo(shopMap, shopRatings, conditionMap);
        model.addAttribute("ratingPage", splitList(ratingInfo, RATING_PAGE_SIZE));

        return "biz/ratinglist";
    }

    @RequestMapping(value = "online.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage ratingOnline() {
        String ratingId = getRequestString("rid");
        try {
            MizarRating mizarRating = mizarLoaderClient.loadRatingById(ratingId);
            if (mizarRating == null) {
                return MapMessage.errorMessage("无效的评论ID:" + ratingId);
            }
            MizarRatingStatus status = MizarRatingStatus.parse(mizarRating.getStatus());
            if (status == null) {
                return MapMessage.errorMessage("评论状态异常：" + mizarRating.getStatus());
            }
            if (status == MizarRatingStatus.ONLINE) {
                return MapMessage.errorMessage("该评论已经处理，请刷新页面");
            }
            if (status != MizarRatingStatus.OFFLINE && status != MizarRatingStatus.PENDING) {
                return MapMessage.errorMessage("该评论状态({})不能做下线操作", status);
            }
            return mizarRatingServiceClient.updateRatingStatus(ratingId, MizarRatingStatus.ONLINE);
        } catch (Exception ex) {
            logger.error("Failed online MizarRating, id={}", ratingId, ex);
            return MapMessage.errorMessage("评论上线失败：" + ex.getMessage());
        }
    }

    @RequestMapping(value = "offline.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage ratingOffline() {
        String ratingId = getRequestString("rid");
        try {
            MizarRating mizarRating = mizarLoaderClient.loadRatingById(ratingId);
            if (mizarRating == null) {
                return MapMessage.errorMessage("无效的评论ID:" + ratingId);
            }
            MizarRatingStatus status = MizarRatingStatus.parse(mizarRating.getStatus());
            if (status == null) {
                return MapMessage.errorMessage("评论状态异常：" + mizarRating.getStatus());
            }
            if (status == MizarRatingStatus.OFFLINE) {
                return MapMessage.errorMessage("该评论已经处理，请刷新页面");
            }
            if (status == MizarRatingStatus.PENDING || status == MizarRatingStatus.DELETED) {
                return MapMessage.errorMessage("该评论状态({})不能做下线操作", status);
            }
            return mizarRatingServiceClient.updateRatingStatus(ratingId, MizarRatingStatus.OFFLINE);
        } catch (Exception ex) {
            logger.error("Failed offline MizarRating , id={}, ", ratingId, ex);
            return MapMessage.errorMessage("评论下线失败：" + ex.getMessage());
        }
    }

    @RequestMapping(value = "delete.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage ratingDelete() {
        String ratingId = getRequestString("rid");
        try {
            MizarRating mizarRating = mizarLoaderClient.loadRatingById(ratingId);
            if (mizarRating == null) {
                return MapMessage.errorMessage("无效的评论ID:" + ratingId);
            }
            MizarRatingStatus status = MizarRatingStatus.parse(mizarRating.getStatus());
            if (status == null) {
                return MapMessage.errorMessage("评论状态异常：" + mizarRating.getStatus());
            }
            if (status == MizarRatingStatus.ONLINE) {
                return MapMessage.errorMessage("不能删除已上线的评论");
            }
            return mizarRatingServiceClient.updateRatingStatus(ratingId, MizarRatingStatus.DELETED);
        } catch (Exception ex) {
            logger.error("Failed delete MizarRating , id={}", ratingId, ex);
            return MapMessage.errorMessage("评论删除失败：" + ex.getMessage());
        }
    }

    private List<Map<String, Object>> mapRatingInfo(Map<String, MizarShop> shopMap,
                                                    Map<String, List<MizarRating>> shopRatings,
                                                    Map<String, Object> conditionMap) {
        if (MapUtils.isEmpty(shopMap) || MapUtils.isEmpty(shopRatings)) {
            return Collections.emptyList();
        }
        Map<Long, String> userMobile = new HashMap<>();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, List<MizarRating>> entry : shopRatings.entrySet()) {
            MizarShop shop = shopMap.get(entry.getKey());
            if (shop == null) continue;
            List<MizarRating> ratings = entry.getValue();
            // 根据搜索条件过滤
            ratings = ratings.stream()
                    .filter(rating -> filterRatings(rating, conditionMap))
                    .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(ratings)) continue;
            ratings.forEach(rating -> {
                Map<String, Object> info = new HashMap<>();
                info.putAll(shop.simpleInfo()); // 机构信息
                info.putAll(MizarRating.toRatingMap(rating)); // 评论基本信息
                Long userId = rating.getUserId();
                info.put("userId", userId);
                String mobile = null;
                if (userId != null && userId != 0L) {
                    if (userMobile.containsKey(userId)) {
                        mobile = userMobile.get(userId);
                    } else {
                        mobile = sensitiveUserDataServiceClient.loadUserMobileObscured(userId);
                        userMobile.put(userId, mobile);
                    }
                }
                info.put("mobile", mobile);
                info.put("rid", rating.getId());
                info.put("ratingTime", new Date(rating.getRatingTime()));
                info.put("status", rating.fetchStatus() == null ? "未知" : rating.fetchStatus());
                info.put("photoList", CollectionUtils.isEmpty(rating.getPhoto()) ? "" : StringUtils.join(rating.getPhoto(), ","));
                result.add(info);
            });
        }
        return result;
    }

    private boolean filterRatings(MizarRating rating, Map<String, Object> conditionMap) {
        if (rating == null) return false;
        int starFilter = SafeConverter.toInt(conditionMap.get("ratingStar"));
        if (starFilter != 0 && Integer.compare(starFilter, rating.getRating()) != 0) {
            return false;
        }
        String contentFilter = SafeConverter.toString(conditionMap.get("content"));
        if (StringUtils.isNotBlank(contentFilter) && !StringUtils.contains(rating.getRatingContent(), contentFilter)) {
            return false;
        }
        String statusFilter = SafeConverter.toString(conditionMap.get("status"));
        return "ALL".equals(statusFilter) || StringUtils.equals(statusFilter, rating.getStatus());
    }

}
