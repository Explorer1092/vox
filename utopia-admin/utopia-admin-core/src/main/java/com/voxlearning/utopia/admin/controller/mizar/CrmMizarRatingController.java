package com.voxlearning.utopia.admin.controller.mizar;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarRating;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarShop;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.vendor.api.constant.ParentMessageType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * @author Summer Yang
 * @since 2016/9/7
 */
@Controller
@RequestMapping("/mizar/rating")
public class CrmMizarRatingController extends CrmMizarAbstractController {

    @Inject private MessageCommandServiceClient messageCommandServiceClient;

    private static final int SHOP_PAGE_SIZE = 10;

    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        int page = getRequestInt("page", 1);
        page = page <= 0 ? 1 : page;
        String shopId = getRequestString("shopId");
        MizarShop shop = mizarLoaderClient.loadShopById(shopId);
        Pageable pageable = new PageRequest(page - 1, SHOP_PAGE_SIZE);
        Page<MizarRating> mizarRatings = mizarLoaderClient.loadRatingPage(shopId, pageable);
        model.addAttribute("ratingList", mizarRatings.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPage", mizarRatings.getTotalPages());
        model.addAttribute("hasPrev", mizarRatings.hasPrevious());
        model.addAttribute("hasNext", mizarRatings.hasNext());
        model.addAttribute("shopName", shop.getFullName());
        model.addAttribute("shopId", shopId);
        return "mizar/rating/ratinglist";
    }

    @RequestMapping(value = "info.vpage", method = RequestMethod.GET)
    public String ratingInfo(Model model) {
        String ratingId = getRequestString("ratingId");
        String shopId = getRequestString("shopId");
        MizarRating rating = mizarLoaderClient.loadRatingById(ratingId);
        if (StringUtils.isNotBlank(ratingId) && rating == null) {
            getAlertMessageManager().addMessageError("无效的点评ID : " + ratingId);
            return "mizar/rating/ratinglist";
        }
        model.addAttribute("ratingId", ratingId);
        model.addAttribute("shopId", shopId);
        model.addAttribute("new", rating == null);
        model.addAttribute("rating", rating);
        return "mizar/rating/ratinginfo";
    }

    @RequestMapping(value = "saverating.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveRating() {
        String ratingId = getRequestString("ratingId");
        String shopId = getRequestString("shopId");
        String userName = getRequestString("userName");
        String ratingContent = getRequestString("ratingContent");
        Integer ratingStar = getRequestInt("rating");
        Double cost = getRequestDouble("cost", 0D);
        Long ratingTime = getRequestLong("ratingTime");
        Integer activityId = getRequestInt("activityId");
        boolean goodRating = getRequestBool("goodRating");
        Long userId = getRequestLong("userId");
        try {
            MizarRating rating = null;
            if (StringUtils.isNotBlank(ratingId)) {
                rating = mizarLoaderClient.loadRatingById(ratingId);
            }
            if (rating == null) {
                rating = new MizarRating();
            }
            rating.setUserId(userId);
            rating.setShopId(shopId);
            rating.setActivityId(activityId);
            rating.setCost(cost);
            rating.setGoodRating(goodRating);
            rating.setRatingTime(ratingTime);
            rating.setRating(ratingStar);
            rating.setRatingContent(ratingContent);
            rating.setUserName(userName);
            MapMessage message = mizarServiceClient.saveMizarRating(rating);
            if (message.isSuccess() && rating.getGoodRating() && rating.getUserId() != null && rating.getUserId() != 0) {
                if (new Date().before(DateUtils.stringToDate("2016-10-01 00:00:00"))) {
                    // 添加学豆 100个 给家长的第一个孩子
                    List<User> childList = studentLoaderClient.loadParentStudents(rating.getUserId());
                    if (CollectionUtils.isNotEmpty(childList)) {
                        User child = childList.get(0);
                        // 添加学豆
                        IntegralHistory integralHistory = new IntegralHistory(child.getId(), IntegralType.STUDENT_MIZAR_LIKED_REWARD, 100);
                        integralHistory.setUniqueKey("mizar_good_rating:" + rating.getUserId());
                        integralHistory.setComment("优质点评机构奖励学豆");
                        MapMessage mm = userIntegralService.changeIntegral(integralHistory);
                        if (mm.isSuccess()) {
                            // app消息
                            AppMessage am = new AppMessage();
                            am.setUserId(rating.getUserId());
                            am.setMessageType(ParentMessageType.REMINDER.getType());
                            am.setTitle("系统通知");
                            am.setContent("恭喜，您的评论被选为优质评论，赠送您的孩子" + child.fetchRealname() + "100学豆！");
                            am.setImageUrl("");
                            am.setIsTop(false);
                            am.setTopEndTime(0L);
                            am.setExtInfo(new HashMap<>());
                            messageCommandServiceClient.getMessageCommandService().createAppMessage(am);
                        }
                    }
                }
            }
            return message;
        } catch (Exception ex) {
            logger.error("Save Mizar rating failed.", ex);
            return MapMessage.errorMessage("保存评论失败：" + ex.getMessage());
        }
    }

    @RequestMapping(value = "uploadphoto.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage uploadRatingPhoto() {
        String ratingId = getRequestString("ratingId");
        String field = getRequestString("field");
        try {
            MizarRating rating = mizarLoaderClient.loadRatingById(ratingId);
            if (ratingId == null) {
                return MapMessage.errorMessage("无效的评论信息");
            }
            // 上传文件
            String fileName = uploadPhoto("file");
            if (StringUtils.isBlank(fileName)) {
                return MapMessage.errorMessage("文件上传失败");
            }
            switch (field) {
                case "banner":
                    List<String> photo = rating.getPhoto() == null ? new ArrayList<>() : rating.getPhoto();
                    photo.add(fileName);
                    rating.setPhoto(photo);
                    break;
                case "avatar":
                    rating.setUserAvatar(fileName);
                    break;
                default:
                    return MapMessage.errorMessage("上传类型错误");
            }

            return mizarServiceClient.saveMizarRating(rating);
        } catch (Exception ex) {
            logger.error("Upload Mizar rating photo failed, shop={}", ratingId, ex);
            return MapMessage.errorMessage("上传失败");
        }
    }

    @RequestMapping(value = "deletephoto.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteRatingPhoto() {
        String ratingId = getRequestString("ratingId");
        String fileName = getRequestString("file");
        String field = getRequestString("field");
        if (StringUtils.isBlank(fileName)) {
            return MapMessage.errorMessage("无效的图片信息");
        }
        try {
            MizarRating rating = mizarLoaderClient.loadRatingById(ratingId);
            if (ratingId == null) {
                return MapMessage.errorMessage("无效的评论信息");
            }
            // 删除文件
            // 删除文件
            switch (field) {
                case "banner":
                    List<String> photo = rating.getPhoto();
                    if (CollectionUtils.isEmpty(photo) || !photo.contains(fileName)) {
                        return MapMessage.errorMessage("无效的图片信息");
                    }
                    photo.removeIf(file -> StringUtils.equals(file, fileName));
                    rating.setPhoto(photo);
                    break;
                case "avatar":
                    rating.setUserAvatar("");
                    break;
                default:
                    return MapMessage.errorMessage("图片类型错误");
            }
            MapMessage msg = mizarServiceClient.saveMizarRating(rating);
            if (msg.isSuccess()) {
                deletePhoto(fileName);
            }
            return msg;
        } catch (Exception ex) {
            logger.error("Delete Mizar rating photo failed, shop={}, file={}", ratingId, fileName, ex);
            return MapMessage.errorMessage("图片删除失败：" + ex.getMessage());
        }
    }
}
