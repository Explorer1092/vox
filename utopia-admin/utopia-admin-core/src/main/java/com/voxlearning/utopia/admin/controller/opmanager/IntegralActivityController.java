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

package com.voxlearning.utopia.admin.controller.opmanager;


import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.data.ActivityDeptType;
import com.voxlearning.utopia.data.ActivityPaymentType;
import com.voxlearning.utopia.data.ActivityStatusType;
import com.voxlearning.utopia.data.ActivityUsageType;
import com.voxlearning.utopia.entity.misc.IntegralActivity;
import com.voxlearning.utopia.entity.misc.IntegralActivityRule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@Slf4j
@RequestMapping(value = "/opmanager/integralactivity")
public class IntegralActivityController extends OpManagerAbstractController {

    @RequestMapping(value = "activitylist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String activityList(Model model) {
        Integer department = getRequestInt("department");
        Integer status = getRequestInt("status");
        List<IntegralActivity> activities = miscLoaderClient.loadAllIntegralActivities();
        if (CollectionUtils.isEmpty(activities)) {
            model.addAttribute("activities", Collections.emptyList());
        } else {
            // 处理查询条件
            if (department != 0L) {
                activities = activities.stream().filter(activity -> Objects.equals(department, activity.getDepartment())).collect(Collectors.toList());
                model.addAttribute("department", department);
            }
            if (status != 0L) {
                activities = activities.stream().filter(activity -> Objects.equals(status, activity.getStatus())).collect(Collectors.toList());
                model.addAttribute("status", status);
            }
            model.addAttribute("activities", packageActivities(activities));
        }
        return "site/integralactivity/list";
    }

    @RequestMapping(value = "activitypage.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    private String activityPage(Model model) {
        Integer pageNumber = getRequestInt("pageNumber", 1);
        Integer department = getRequestInt("department", 0);
        Integer status = getRequestInt("status", 0);
        Pageable pageable = new PageRequest(pageNumber - 1, 10);
        setActivityListModel(model, pageNumber, pageable, department, status);
        model.addAttribute("department", department);
        model.addAttribute("status", status);
        model.addAttribute("departmentList", ActivityDeptType.toKeyValuePairs());
        return "opmanager/integralactivity/list";
    }

    @RequestMapping(value = "activityinfo.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String editActivity(Model model) {
        Long activityId = getRequestLong("id");
        boolean edit = getRequestBool("edit");
        if (activityId != 0L) {
            setActivityEditModel(model, activityId, edit);
        } else {
            model.addAttribute("actEditable", true);
            model.addAttribute("ruleEditable", false);
            model.addAttribute("finished", false);
        }
        model.addAttribute("departmentList", ActivityDeptType.toKeyValuePairs());
        return "opmanager/integralactivity/edit";
    }

    @RequestMapping(value = "saveactivity.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveIntegralActivity() {
        Long activityId = getRequestLong("activityId");
        Date startDate;
        Date endDate;
        try {
            IntegralActivity activity = new IntegralActivity();
            activity.setActivityName(getRequestString("activityName"));
            // 此处注意！每次修改之后创建人实际意味着最后修改人
            activity.setCreatorId(getCurrentAdminUser().getFakeUserId());
            activity.setCreatorName(getCurrentAdminUser().getRealName());
            activity.setDepartment(getRequestInt("department"));
            startDate = DateUtils.stringToDate(getRequestString("actStartDate"));
            endDate = DateUtils.stringToDate(getRequestString("actEndDate"));
            String validation = checkDateParam(startDate, endDate);
            if (!StringUtils.isBlank(validation)) {
                return MapMessage.errorMessage(validation);
            }
            // 活动的时间仅供展示用，暂不做任何控制
            activity.setStartDate(startDate);
            activity.setEndDate(endDate);
            // 此处表示修改活动信息
            if (activityId != 0L) {
                activity.setId(activityId);
                miscServiceClient.updateIntegralActivity(activityId, activity);
                addAdminLog("修改积分活动信息", activityId, "User: " + getCurrentAdminUser().getRealName());
            } else {
                activityId = miscServiceClient.addIntegralActivity(activity);
                addAdminLog("新增积分活动", activityId, "User: " + getCurrentAdminUser().getRealName());
            }
        } catch (Exception ignored) {
            MapMessage.errorMessage("保存积分活动任务失败！");
        }
        return MapMessage.successMessage().add("activityId", activityId);
    }

    @RequestMapping(value = "changestatus.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage changeActivityStatus() {
        Long activityId = getRequestLong("activityId");
        Integer status = getRequestInt("status");
        try {
            if (activityId != 0L && ActivityStatusType.of(status) != ActivityStatusType.EXCEPTION) {
                miscServiceClient.updateIntegralActivityStatus(activityId, status);
                addAdminLog("更新积分活动状态:" + status, activityId, "User: " + getCurrentAdminUser().getRealName());
            } else {
                return MapMessage.errorMessage();
            }
        } catch (Exception ignored) {
            log.error("更新积分活动失败！ activityId:{} status:{} ", activityId, status);
            return MapMessage.errorMessage("参数异常！");
        }
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "saverule.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveActivityRule() {
        Long activityId;
        Long ruleId;
        Date startDate;
        Date endDate;
        Integer department;
        Integer userType;
        Integer integralType;
        Integer paymentType;
        Integer usageType;
        String description;
        StringBuffer checkMsg = new StringBuffer();
        try {
            activityId = Long.parseLong(getRequestParameter("activityId", "0"));
            ruleId = Long.parseLong(getRequestParameter("selectRule", "0"));
            department = getRequestInt("department", 0);
            if (activityId == 0L) {
                return MapMessage.errorMessage("请检查积分活动是否已经创建！");
            }
            boolean isNew = (ruleId == 0L);
            startDate = DateUtils.stringToDate(getRequestString("ruleStartDate"));
            endDate = DateUtils.stringToDate(getRequestString("ruleEndDate"));
            integralType = getRequestInt("integralType", -1);
            userType = getRequestInt("userType", 0);
            paymentType = getRequestInt("paymentType", 0);
            usageType = getRequestInt("usageType", 0);
            description = getRequestString("description");
            // 校验各项数据的合理性
            checkMsg.append(checkDateParam(startDate, endDate))
                    .append(checkIntegralType(integralType, ruleId))
                    .append(checkEnumParam(userType, "生效对象"))
                    .append(checkEnumParam(paymentType, "积分支付类型"))
                    .append(checkEnumParam(usageType, "积分用途类型"))
                    .append(checkTextParam(description, 200));
            if (!StringUtils.isBlank(checkMsg)) {
                return MapMessage.errorMessage(checkMsg.toString());
            }
            IntegralActivityRule rule = IntegralActivityRule.newInstance(activityId, startDate, endDate,
                    userType, paymentType, usageType,
                    integralType, description);

            if (isNew) {
                ruleId = miscServiceClient.addIntegralActivityRule(rule, department);
                addAdminLog("积分活动增加一条规则:" + ruleId, activityId, "User: " + getCurrentAdminUser().getRealName());
            } else {
                rule.setId(ruleId);
                rule.setIntegralType(integralType);
                miscServiceClient.updateIntegralActivityRule(rule);
                addAdminLog("积分活动修改一条规则:" + ruleId, activityId, "User: " + getCurrentAdminUser().getRealName());

            }
        } catch (Exception ignored) {
            return MapMessage.errorMessage("增加积分活动规则失败:\n{}", ignored.getMessage());
        }
        return MapMessage.successMessage("积分活动规则添加成功。");
    }

    @RequestMapping(value = "delrule.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage delActivityRule() {
        try {
            Long ruleId = getRequestLong("ruleId");
            if (ruleId != 0L) {
                miscServiceClient.disableIntegralActivityRule(ruleId);
                addAdminLog("积分活动删除一条规则:", ruleId, "User: " + getCurrentAdminUser().getRealName());

            } else {
                return MapMessage.errorMessage("规则ID异常!");
            }
        } catch (Exception ignored) {
            return MapMessage.errorMessage("积分活动规则删除失败！");
        }
        return MapMessage.successMessage();
    }

    //---------------------------------------------------------
    // Private Methods
    //---------------------------------------------------------

    private void setActivityListModel(Model model, Integer pageNumber, Pageable pageable, Integer department, Integer status) {
        Page<IntegralActivity> activityPage = miscLoaderClient.loadIntegralActivityPage(pageable, department, status);
        model.addAttribute("totalPages", activityPage.getTotalPages());
        model.addAttribute("hasPrev", activityPage.hasPrevious());
        model.addAttribute("hasNext", activityPage.hasNext());
        model.addAttribute("pageNumber", pageNumber);
        model.addAttribute("activities", packageActivities(activityPage.getContent()));
    }

    private void setActivityEditModel(Model model, Long activityId, boolean edit) {
        Map<String, Object> activityDetail = miscLoaderClient.loadIntegralActivityDetail(activityId);
        IntegralActivity savedActivity = (IntegralActivity) activityDetail.get("activity");
        model.addAttribute("activity", packageActivity(savedActivity));
        boolean actEditable = edit;
        boolean ruleEditable = !edit;
        boolean finished = false;
        if (ActivityStatusType.of(savedActivity.getStatus()) == ActivityStatusType.ONGOING) {
            actEditable = false;
            ruleEditable = true;
            finished = true;
        }
        if (ActivityStatusType.of(savedActivity.getStatus()) == ActivityStatusType.FINISHED) {
            actEditable = false;
            ruleEditable = false;
            finished = true;
        }
        @SuppressWarnings("unchecked")
        List<IntegralActivityRule> rules = (List<IntegralActivityRule>) activityDetail.get("rules");
        model.addAttribute("rules", packageRules(rules));
        model.addAttribute("actEditable", actEditable);
        model.addAttribute("ruleEditable", ruleEditable);
        model.addAttribute("finished", finished);
    }

    // 处理活动列表用于前端展示
    private List<Map<String, Object>> packageActivities(List<IntegralActivity> activities) {
        if (CollectionUtils.isEmpty(activities)) {
            return Collections.emptyList();
        }
        return activities.stream().map(this::packageActivity).collect(Collectors.toList());
    }

    // 处理规则列表用于前端展示
    private List<Map<String, Object>> packageRules(List<IntegralActivityRule> rules) {
        List<Map<String, Object>> rulesInfo = new ArrayList<>();
        for (IntegralActivityRule rule : rules) {
            rulesInfo.add(packageRule(rule));
        }
        return rulesInfo;
    }

    // 处理活动信息用于前端展示
    private Map<String, Object> packageActivity(IntegralActivity activity) {
        Map<String, Object> infoMap = new HashMap<>();
        infoMap.put("id", activity.getId());
        infoMap.put("activityName", activity.getActivityName());
        infoMap.put("department", ActivityDeptType.of(activity.getDepartment()));
        infoMap.put("creatorName", activity.getCreatorName());
        infoMap.put("startDate", convertDate(activity.getStartDate()));
        infoMap.put("endDate", convertDate(activity.getEndDate()));
        infoMap.put("status", activity.getStatus());
        infoMap.put("statusDesc", ActivityStatusType.of(activity.getStatus()).getDescription());
        return infoMap;
    }

    // 处理规则信息用于前端展示
    private Map<String, Object> packageRule(IntegralActivityRule rule) {
        Map<String, Object> infoMap = new HashMap<>();
        infoMap.put("ruleId", rule.getId());
        infoMap.put("ruleStartDate", convertDate(rule.getStartDate()));
        infoMap.put("ruleEndDate", convertDate(rule.getEndDate()));
        infoMap.put("integralType", rule.getIntegralType());
        infoMap.put("userType", UserType.of(rule.getUserType()));
        infoMap.put("usageType", ActivityUsageType.of(rule.getUsageType()));
        infoMap.put("paymentType", ActivityPaymentType.of(rule.getPaymentType()));
        infoMap.put("description", rule.getDescription());
        infoMap.put("hasBegin", rule.getStartDate() != null && new Date().before(rule.getStartDate()));
        return infoMap;
    }

    /**
     * 检查积分类型变量
     *
     * @param type 积分类型值
     */
    private String checkIntegralType(Integer type, Long ruleId) {
        if (ruleId != 0L && type == 0) return "积分类型不能为空！";
//        if (type < 0 || type > 999999) return "积分类型参数异常!(0~999,999)\n";
        // 校验积分类型不能重复
        if (miscLoaderClient.checkIntegralType(ruleId, type)) {
            return "积分类型曾经使用过，不能再次使用！\n";
        }
        return "";
    }

    /**
     * 检查传入的起始时间和结束时间变量
     *
     * @param startDate 开始时间
     * @param endDate   结束时间
     */
    private String checkDateParam(Date startDate, Date endDate) {
        Date current = new Date();
        if (startDate != null) {
            // FIXME 暂时不需要去掉这个时间控制，希望添加的积分类型可以马上生效 By Wyc 2015-03-10
//            if (current.after(startDate)) {
//                return "开始时间不得早于当前时间！";
//            }
//            if (DateUtils.hourDiff(startDate, current) < 24) {
//                return "开始时间至少要在当前时间1天之后";
//            }
            if (endDate != null) {
                if (startDate.after(endDate)) {
                    return "结束时间不能早于开始时间！\n";
                }
                if (DateUtils.minuteDiff(endDate, startDate) < 5L) {
                    return "开始时间与结束时间间隔不能少于 5 分钟！";
                }
            }
        } else {
            return "开始时间不得为空";
        }
        return "";
    }

    /**
     * 检查传入的文本变量是否符合规则
     *
     * @param text   文本
     * @param length 文本长度
     */
    private String checkTextParam(String text, int length) {
        if (!StringUtils.isBlank(text) && text.length() > length) {
            return "场景描述最多只能有30个字！";
        }
        return "";
    }

    /**
     * 检查传入的枚举型变量是否符合规则
     *
     * @param val  枚举值
     * @param type 枚举类型名称
     */
    private String checkEnumParam(Integer val, String type) {
        if (val == 0) {
            return type + "参数异常！\n";
        }
        return "";
    }

    /**
     * 转换日期格式
     *
     * @param date 日期
     * @return 如果日期为null，认为是长期有效
     */
    private String convertDate(Date date) {
        if (date != null) {
            return DateUtils.dateToString(date, DateUtils.FORMAT_SQL_DATETIME);
        }
        return "长期有效";
    }

}
