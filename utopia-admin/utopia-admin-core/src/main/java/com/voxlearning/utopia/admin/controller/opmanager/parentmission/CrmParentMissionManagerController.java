package com.voxlearning.utopia.admin.controller.opmanager.parentmission;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.galaxy.service.mission.api.DPMissionLoader;
import com.voxlearning.galaxy.service.mission.api.DPMissionService;
import com.voxlearning.galaxy.service.mission.api.constant.MissionPolicy;
import com.voxlearning.galaxy.service.mission.api.constant.MissionRewardType;
import com.voxlearning.galaxy.service.mission.api.constant.MissionTag;
import com.voxlearning.galaxy.service.mission.api.constant.MissionType;
import com.voxlearning.galaxy.service.mission.api.entity.*;
import com.voxlearning.utopia.admin.controller.AbstractAdminController;
import com.voxlearning.utopia.service.user.api.constants.FinanceFlowGiveSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

/**
 * @author xin.xin
 * @since 3/18/19
 **/
@Controller
@RequestMapping(value = "/opmanager/parentmission")
@Slf4j
public class CrmParentMissionManagerController extends AbstractAdminController {
    @ImportService(interfaceClass = DPMissionLoader.class)
    private DPMissionLoader dpMissionLoader;
    @ImportService(interfaceClass = DPMissionService.class)
    private DPMissionService dpMissionService;

    @RequestMapping(value = "/categories.vpage", method = RequestMethod.GET)
    public String categories() {
        return "opmanager/parentmission/categories";
    }

    @RequestMapping(value = "/missions.vpage", method = RequestMethod.GET)
    public String missions() {
        return "opmanager/parentmission/missions";
    }

    @RequestMapping(value = "/missions/edit.vpage", method = RequestMethod.GET)
    public String missionsEdit(Model model) {
        String id = getRequestString("id");
        model.addAttribute("id", id);
        return "opmanager/parentmission/mission_edit";
    }

    @RequestMapping(value = "/missions/invite.vpage", method = RequestMethod.GET)
    public String missionInvite() {
        return "opmanager/parentmission/invite";
    }

    @RequestMapping(value = "/missions/reward.vpage", method = RequestMethod.GET)
    public String missionReward() {
        return "opmanager/parentmission/reward";
    }

    @RequestMapping(value = "/category/list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage categoryList() {
        Collection<MissionCategory> categories = dpMissionLoader.getMissionCategoriesForCRM();
        List<Map<String, String>> tags = new ArrayList<>();
        for (MissionTag value : MissionTag.values()) {
            Map<String, String> info = new HashMap<>();
            info.put("name", value.name());
            info.put("title", value.getTitle());
            info.put("prefix", value.getPrefix());
            tags.add(info);
        }
        return MapMessage.successMessage().add("categories", categories).add("tags", tags);
    }

    @RequestMapping(value = "/category/add.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addCategory() {
        String name = getRequestString("name");
        String prefix = getRequestString("prefix");
        String desc = getRequestString("desc");
        String label = getRequestString("label");
        String type = getRequestString("type");
        Integer sort = getRequestInt("sort");
        if (StringUtils.isBlank(name) || StringUtils.isBlank(prefix) || StringUtils.isBlank(desc) || StringUtils.isBlank(label) || StringUtils.isBlank(type)) {
            return MapMessage.errorMessage("参数错误");
        }

        try {
            MissionTag missionTag = MissionTag.valueOf(type);
            return dpMissionService.addMissionCategory(missionTag, prefix, name, desc, label, sort);
        } catch (Exception ex) {
            log.error("{},{},{},{}", name, desc, label, type, ex);
            return MapMessage.errorMessage(ex.getMessage());
        }
    }

    @RequestMapping(value = "/category/edit.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage editCategory() {
        String id = getRequestString("id");
        String name = getRequestString("name");
        String prefix = getRequestString("prefix");
        String desc = getRequestString("desc");
        String label = getRequestString("label");
        String type = getRequestString("type");
        Integer sort = getRequestInt("sort");
        if (StringUtils.isBlank(id) || StringUtils.isBlank(prefix) || StringUtils.isBlank(name) || StringUtils.isBlank(desc) || StringUtils.isBlank(label) || StringUtils.isBlank(type)) {
            return MapMessage.errorMessage("参数错误");
        }

        try {
            MissionTag missionTag = MissionTag.valueOf(type);
            return dpMissionService.updateMissionCategory(id, missionTag, prefix, name, desc, label, sort);
        } catch (Exception ex) {
            log.error("{},{},{},{},{}", id, name, desc, label, type, ex);
            return MapMessage.errorMessage(ex.getMessage());
        }
    }

    @RequestMapping(value = "/mission/categories.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage missionCategories() {
        Collection<MissionCategory> categories = dpMissionLoader.getMissionCategoriesForCRM();
        List<Map<String, String>> categoriesInfo = new ArrayList<>();
        for (MissionCategory cat : categories) {
            Map<String, String> info = new HashMap<>();
            info.put("title", cat.getTitle());
            info.put("value", cat.getId());
            info.put("prefix", cat.getTag() == null ? "" : cat.getTag().getPrefix() + "-" + cat.getPrefix());
            categoriesInfo.add(info);
        }

        List<Map<String, String>> policyInfos = new ArrayList<>();
        for (MissionPolicy policy : MissionPolicy.values()) {
            Map<String, String> info = new HashMap<>();
            info.put("title", policy.getTitle());
            info.put("value", policy.name());
            policyInfos.add(info);
        }

        List<Map<String, String>> typeInfos = new ArrayList<>();
        for (MissionType type : MissionType.values()) {
            Map<String, String> info = new HashMap<>();
            info.put("title", type.getTitle());
            info.put("value", type.name());
            typeInfos.add(info);
        }

        List<Map<String, String>> rewardTypeInfos = new ArrayList<>();
        for (MissionRewardType type : MissionRewardType.values()) {
            Map<String, String> info = new HashMap<>();
            info.put("title", type.getTitle());
            info.put("value", type.name());
            rewardTypeInfos.add(info);
        }

        List<Map<String, String>> financeSourceInfos = new ArrayList<>();
        for (FinanceFlowGiveSource source : FinanceFlowGiveSource.values()) {
            Map<String, String> info = new HashMap<>();
            info.put("title", source.name());
            info.put("value", source.name());
            financeSourceInfos.add(info);
        }

        return MapMessage.successMessage().add("categories", categoriesInfo)
                .add("rewardTypes", rewardTypeInfos)
                .add("financeSources", financeSourceInfos)
                .add("types", typeInfos)
                .add("policies", policyInfos);
    }

    @RequestMapping(value = "/mission/list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage missionList() {
        String categoryId = getRequestString("cid");
        if (StringUtils.isBlank(categoryId)) {
            return MapMessage.errorMessage("参数错误");
        }

        try {
            Collection<Mission> missions = dpMissionLoader.getMissionsForCRM(categoryId);
            return MapMessage.successMessage().add("missions", missions);
        } catch (Exception ex) {
            log.error("{}", categoryId, ex);
            return MapMessage.errorMessage(ex.getMessage());
        }
    }

    @RequestMapping(value = "/mission/add.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addMission() {
        String userTypes = getRequestString("userTypes");
        String identification = getRequestString("identification");
        String categoryId = getRequestString("categoryId");
        String type = getRequestString("type");
        String policies = getRequestString("policies");
        String rewardType = getRequestString("rewardType");
        String reward = getRequestString("reward");
        String title = getRequestString("title");
        String desc = getRequestString("desc");
        String progress = getRequestString("progress");
        String url = getRequestString("url");
        String icon = getRequestString("icon");
        String thumb = getRequestString("thumb");
        String expireDays = getRequestString("expireDays");
        Boolean needReceive = getRequestBool("needReceive");
        String inviteeCoupon = getRequestString("inviteeCoupon");
        String expireDate = getRequestString("expireDate");
        String financeSource = getRequestString("financeSource");

        try {
            Collection<Mission> missions = dpMissionLoader.getMissionsForCRM(categoryId);
            long count = missions.stream().filter(mission -> Objects.equals(mission.getIdentification(), identification)).count();
            if (count > 0) {
                return MapMessage.errorMessage("任务标识重复");
            }

            MissionType missionType = MissionType.valueOf(type);
            List<Integer> uTypes = new ArrayList<>();
            String[] uts = userTypes.split(",");
            for (String ut : uts) {
                uTypes.add(SafeConverter.toInt(ut));
            }
            List<MissionPolicy> lstPolicy = new ArrayList<>();
            String[] pss = policies.split(",");
            for (String ps : pss) {
                lstPolicy.add(MissionPolicy.valueOf(ps));
            }
            MissionRewardType missionRewardType = MissionRewardType.valueOf(rewardType);
            int exDays = SafeConverter.toInt(expireDays);

            Date epDate = StringUtils.isBlank(expireDate) ? null : DayRange.newInstance(DateUtils.stringToDate(expireDate, "yyyy-MM-dd").getTime()).getEndDate();
            dpMissionService.addMission(categoryId, identification, title, desc, icon, thumb, url, null, missionType, lstPolicy, missionRewardType, reward, exDays, needReceive, uTypes, progress, inviteeCoupon, epDate, financeSource);
            return MapMessage.successMessage();
        } catch (Exception ex) {
            log.error("{},{},{},{},{},{},{},{},{},{},{},{},{},{}", userTypes, identification, categoryId, type, policies, rewardType, reward, title, desc, url, icon, thumb, expireDays, needReceive, ex);
            return MapMessage.errorMessage(ex.getMessage());
        }
    }

    @RequestMapping(value = "/mission/edit.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage editMission() {
        String id = getRequestString("id");
        String userTypes = getRequestString("userTypes");
        String identification = getRequestString("identification");
        String categoryId = getRequestString("categoryId");
        String type = getRequestString("type");
        String policies = getRequestString("policies");
        String rewardType = getRequestString("rewardType");
        String reward = getRequestString("reward");
        String title = getRequestString("title");
        String desc = getRequestString("desc");
        String progress = getRequestString("progress");
        String url = getRequestString("url");
        String icon = getRequestString("icon");
        String thumb = getRequestString("thumb");
        String expireDays = getRequestString("expireDays");
        Boolean needReceive = getRequestBool("needReceive");
        String inviteeCoupon = getRequestString("inviteeCoupon");
        String expireDate = getRequestString("expireDate");
        String financeSource = getRequestString("financeSource");

        try {
            Collection<Mission> missions = dpMissionLoader.getMissionsForCRM(categoryId);
            long count = missions.stream().filter(mission -> Objects.equals(mission.getIdentification(), identification) && !mission.getId().equals(id)).count();
            if (count > 0) {
                return MapMessage.errorMessage("任务标识重复");
            }

            MissionType missionType = MissionType.valueOf(type);
            List<Integer> uTypes = new ArrayList<>();
            String[] uts = userTypes.split(",");
            for (String ut : uts) {
                uTypes.add(SafeConverter.toInt(ut));
            }
            List<MissionPolicy> lstPolicy = new ArrayList<>();
            String[] pss = policies.split(",");
            for (String ps : pss) {
                lstPolicy.add(MissionPolicy.valueOf(ps));
            }
            MissionRewardType missionRewardType = MissionRewardType.valueOf(rewardType);
            int exDays = SafeConverter.toInt(expireDays);

            Date epDate = StringUtils.isBlank(expireDate) ? null : DayRange.newInstance(DateUtils.stringToDate(expireDate, "yyyy-MM-dd").getTime()).getEndDate();
            dpMissionService.updateMission(id, categoryId, identification, title, desc, icon, thumb, url, null, missionType, lstPolicy, missionRewardType, reward, exDays, needReceive, uTypes, progress, inviteeCoupon, epDate, financeSource);
            return MapMessage.successMessage();
        } catch (Exception ex) {
            log.error("{},{},{},{},{},{},{},{},{},{},{},{},{},{},{}", id, userTypes, identification, categoryId, type, policies, rewardType, reward, title, desc, url, icon, thumb, expireDays, needReceive, ex);
            return MapMessage.errorMessage(ex.getMessage());
        }
    }

    @RequestMapping(value = "/mission/info.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage missionInfo() {
        String id = getRequestString("id");
        if (StringUtils.isBlank(id)) {
            return MapMessage.errorMessage("参数错误");
        }

        try {
            Mission mission = dpMissionLoader.getMissionById(id);
            if (null == mission) {
                return MapMessage.errorMessage("未查询到任务信息");
            }

            return MapMessage.successMessage().add("info", mission);
        } catch (Exception ex) {
            log.error("{}", id, ex);
            return MapMessage.errorMessage(ex.getMessage());
        }
    }

    @RequestMapping(value = "/mission/inviter/search.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage inviterSearch() {
        Long inviter = getRequestLong("inviter");
        if (0 == inviter) {
            return MapMessage.errorMessage("参数错误");
        }

        try {
            Collection<Mission> missions = dpMissionLoader.getMissions();
            Collection<MissionInviterRef> inviterRefs = dpMissionLoader.getMissionInviterRefs(inviter);
            return MapMessage.successMessage().add("refs", inviterRefs).add("missions", missions);
        } catch (Exception ex) {
            log.error("{}", inviter, ex);
            return MapMessage.errorMessage(ex.getMessage());
        }
    }

    @RequestMapping(value = "/mission/invitee/search.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage inviteeSearch() {
        Long invitee = getRequestLong("invitee");
        if (0 == invitee) {
            return MapMessage.errorMessage("参数错误");
        }

        try {
            Collection<Mission> missions = dpMissionLoader.getMissions();
            Collection<MissionInviteeRef> inviteeRefs = dpMissionLoader.getMissionInviteeRefs(invitee);
            return MapMessage.successMessage().add("refs", inviteeRefs).add("missions", missions);
        } catch (Exception ex) {
            log.error("{}", invitee, ex);
            return MapMessage.errorMessage(ex.getMessage());
        }
    }

    @RequestMapping(value = "/missions/reward/query.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage queryReward() {
        Long userId = getRequestLong("userId");
        if (0 == userId) {
            return MapMessage.errorMessage("参数错误");
        }

        try {
            List<Map<String, Object>> rewardTypeInfos = new ArrayList<>();
            for (MissionRewardType t : MissionRewardType.values()) {
                Map<String, Object> info = new HashMap<>();
                info.put("title", t.getTitle());
                info.put("value", t.name());
                rewardTypeInfos.add(info);
            }
            Collection<MissionCategory> categories = dpMissionLoader.getMissionCategoriesForCRM();
            List<Map<String, Object>> categoryInfos = new ArrayList<>();
            for (MissionCategory category : categories) {
                Map<String, Object> info = new HashMap<>();
                info.put("title", category.getTitle());
                info.put("id", category.getId());
                categoryInfos.add(info);
            }
            Collection<MissionReward> rewards = dpMissionLoader.getMissionReward(userId);
            Collection<MissionRewardCategoryStatistics> categoryStatistics = dpMissionLoader.getMissionRewardCategoryStatistics(userId);
            return MapMessage.successMessage()
                    .add("categories", categoryInfos)
                    .add("rewardTypes", rewardTypeInfos)
                    .add("rewards", rewards).add("categoryRewards", categoryStatistics);
        } catch (Exception ex) {
            log.error("{}", userId, ex);
            return MapMessage.errorMessage(ex.getMessage());
        }
    }
}
