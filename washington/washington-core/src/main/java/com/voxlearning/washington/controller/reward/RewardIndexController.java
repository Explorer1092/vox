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

package com.voxlearning.washington.controller.reward;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DateRange;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.TeacherTaskType;
import com.voxlearning.utopia.service.reward.api.CRMRewardService;
import com.voxlearning.utopia.service.reward.client.TeacherTaskRewardHistoryServiceClient;
import com.voxlearning.utopia.service.reward.constant.RewardProductType;
import com.voxlearning.utopia.service.reward.entity.RewardCategory;
import com.voxlearning.utopia.service.reward.entity.RewardIndex;
import com.voxlearning.utopia.service.reward.entity.RewardLogistics;
import com.voxlearning.utopia.service.reward.entity.RewardProduct;
import com.voxlearning.utopia.service.reward.mapper.RewardProductDetail;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import org.jsoup.helper.Validate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;

/**
 * Reward index controller implementation.
 *
 * @author Xiaopeng Yang
 * @author Xiaohai Zhang
 * @since Jul 21, 2014
 */
@Controller
@RequestMapping("/reward")
public class RewardIndexController extends AbstractRewardController {

    @ImportService(interfaceClass = CRMRewardService.class)
    private CRMRewardService crmRewardService;

    @Inject private TeacherTaskRewardHistoryServiceClient teacherTaskRewardHistoryServiceClient;

    /**
     * 奖品中心首页
     */
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String rewardIndex(Model model) {
        User user = currentRewardUser();

        // 黑名单控制
        if (user.isTeacher()) {
            TeacherDetail teacher = teacherLoaderClient.loadTeacherDetail(user.getId());
            if (grayFunctionManagerClient.getTeacherGrayFunctionManager().isWebGrayFunctionAvailable(teacher, "Reward", "Close")) {
                return "redirect:/";
            }
        }

        //愿望盒
        List<Map<String, Object>> wishProduct = rewardLoaderClient.getWishDetails(user);
        model.addAttribute("wishProduct", wishProduct);
        //推荐列表
        //所有分类
        List<Long> productIds = new ArrayList<>();
        List<RewardCategory> allCategorys = new ArrayList<>(rewardLoaderClient.loadRewardCategories(RewardProductType.JPZX_SHIWU, user.fetchUserType()));
        Set<RewardCategory> tyCategorys = new HashSet<>(rewardLoaderClient.loadRewardCategories(RewardProductType.JPZX_TIYAN, user.fetchUserType()));
        allCategorys.addAll(tyCategorys);
        allCategorys.stream().sorted((o1, o2) -> Integer.compare(o1.getDisplayOrder(), o2.getDisplayOrder()));
        for (RewardIndex index : rewardLoaderClient.loadRewardIndices()) {
            productIds.add(index.getProductId());
        }
        Collection<RewardProductDetail> indexProductList = rewardLoaderClient.generateUserRewardProductDetails(user, productIds);
        List<RewardProductDetail> details = new LinkedList<>();
        for (RewardProductDetail detail : indexProductList) {
            RewardProduct product = (RewardProduct) detail.getExtenstionAttributes().get("product");
            if (Boolean.TRUE.equals(product.getOnlined())) {
                details.add(detail);
            }
        }
        model.addAttribute("categories", allCategorys);
        model.addAttribute("indexProductList", details);

        if (user.fetchUserType() == UserType.STUDENT) {
            if (studentLoaderClient.isStudentForbidden(user.getId())) {
                model.addAttribute("stuforbidden", true);
            }
        }
        return "reward/index";
    }

    @RequestMapping(value = "getordercount.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getOrderCount() {
        Long userId = currentUserId();
        int count = rewardLoaderClient.getRewardOrderLoader().loadUserSubmitRewardOrderCount(userId);
        return MapMessage.successMessage().add("orderCount", count);
    }

    /**
     * 奖品中心 - 查看老师代收学生包裹奖励的领取状态
     * @return
     */
    @RequestMapping(value = "collection_reward_status.vpage",method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getCollectionRewardStatus(){
        try{
            User user = currentUser();
            Validate.notNull(user,"未登录，无法操作!");

            Validate.isTrue(user.fetchUserType() == UserType.TEACHER,"非老师身份，无法操作!");

            Long logisticsId = getRequestLong("logisticId");
            RewardLogistics logistics = crmRewardService.$loadRewardLogistics(logisticsId);
            Validate.notNull(logistics,"订单不存在!");

            Validate.isTrue(Objects.equals(logistics.getReceiverId(),user.getId()),"未查到奖励信息!");

            Date now = new Date();
            Date expireDate = DateUtils.addDays(logistics.getCreateDatetime(),30);
            boolean expired = expireDate.before(now);

            DateRange range = new DateRange(logistics.getCreateDatetime(),expireDate);

            boolean received = teacherTaskRewardHistoryServiceClient.getTeacherTaskRewardHistoryService()
                    .findTeacherTaskRewardHistories(user.getId(),TeacherTaskType.REWARD_COLLECTION.name())
                    .getUninterruptibly()
                    .stream()
                    .anyMatch(h -> range.contains(h.getCreateDatetime()));

            TeacherDetail td = (TeacherDetail)user;

            String unit = "园丁豆";
            if(td.isJuniorTeacher())
                unit = "学豆";

            return MapMessage.successMessage()
                    .add("received",received)
                    .add("expired",expired)
                    .add("unit",unit);

        }catch (Exception e){
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    //老师领取任务奖励
    @RequestMapping(value = "getreward.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getReward() { 
        User user = currentUser();
        if (user.fetchUserType() != UserType.TEACHER) {
            return MapMessage.errorMessage("只有老师才能参与此活动");
        }
        String taskType = getRequestParameter("taskType", "");
        if (StringUtils.isBlank(taskType)) {
            return MapMessage.errorMessage("任务类型错误");
        }
        TeacherTaskType type = TeacherTaskType.valueOf(taskType);
        if (type == null) {
            return MapMessage.errorMessage("任务类型错误");
        }
        String rewardName = getRequestParameter("rewardName", "");
        if (StringUtils.isBlank(rewardName)) {
            return MapMessage.errorMessage("奖品不存在");
        }
        return atomicLockManager.wrapAtomic(businessTeacherServiceClient)
                .expirationInSeconds(30)
                .keyPrefix("RECEIVE_TEACHER_TASK_REWARD")
                .keys(user.getId())
                .proxy()
                .receiveTeacherTaskReward(user.getId(), type, rewardName);
    }

    /**
     * 老师代领学生奖品，领取园丁豆页（入口消息中心）
     */
    @RequestMapping(value = "getcollectreward.vpage", method = RequestMethod.GET)
    public String getCollectReward(Model model) {
        return "/reward/getcollectreward";
    }

}
