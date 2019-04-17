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

package com.voxlearning.utopia.agent.service.workspace;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.agent.bean.incomes2016.GreatRegionIncomeS2016Bean;
import com.voxlearning.utopia.agent.constants.AgentNotifyType;
import com.voxlearning.utopia.agent.persist.spring2016.AgentUserKpiResultSpring2016;
import com.voxlearning.utopia.agent.persist.spring2016.AgentUserKpiResultSpring2016Persistence;
import com.voxlearning.utopia.agent.salary.type.SalaryKpiType;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.common.BaseUserService;
import com.voxlearning.utopia.agent.service.notify.AgentNotifyService;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;

import javax.inject.Inject;
import javax.inject.Named;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 业绩计算
 * Created by Shuai.Huan on 2014/8/6.
 */
@Named
public class MarketFeeService extends AbstractAgentService {

    @Inject private BaseOrgService baseOrgService;
    @Inject private BaseUserService baseUserService;
    @Inject private AgentNotifyService agentNotifyService;
    @Inject private AgentUserKpiResultSpring2016Persistence agentUserKpiResultSpring2016Persistence;

    public Map<String, GreatRegionIncomeS2016Bean> getMarketFee(Integer salaryMonth) {
        List<AgentUserKpiResultSpring2016> kpiResultList = agentUserKpiResultSpring2016Persistence.find(salaryMonth);
        Map<String, GreatRegionIncomeS2016Bean> feeData = new LinkedHashMap<>();
        for (AgentUserKpiResultSpring2016 kpiResult : kpiResultList) {
            String greatRegionName = kpiResult.getRegionName();
            if (StringUtils.isBlank(greatRegionName)) {
                greatRegionName = "-";
            }
            GreatRegionIncomeS2016Bean regionBean = feeData.get(greatRegionName);
            if (regionBean == null) {
                regionBean = new GreatRegionIncomeS2016Bean(greatRegionName);
                feeData.put(greatRegionName, regionBean);
            }
            regionBean.appendIncome(kpiResult);
        }

        return feeData;
    }

    //财务确认
    public void financeConfirm(Integer month, Long user) {
        if (month == null || month == 0 || user == null || user == 0L) {
            return;
        }

        Map<Long, Long> notifyUsers = new HashMap<>();
        boolean sendNotify = false;
        Long totalIncome = 0L;
        List<AgentUserKpiResultSpring2016> kpiResultList = agentUserKpiResultSpring2016Persistence.findUserKpiResultsByMonth(Collections.singleton(user), month);
        for (AgentUserKpiResultSpring2016 kpiResult : kpiResultList) {
            kpiResult.setFinanceCheck(true);
            kpiResult.setUpdateDatetime(new Date());
            agentUserKpiResultSpring2016Persistence.update(kpiResult.getId(), kpiResult);

            // 如果数据已经被市场确认过，发送确认通知
            if (kpiResult.getMarketCheck()) {
                sendNotify = true;
                totalIncome += kpiResult.getCpaSalary();
            }
        }

        if (sendNotify) {
            notifyUsers.put(user, totalIncome);
        }

        sendConfirmSalaryNotify(notifyUsers);
    }

    //市场确认
    public void marketConfirm(Integer month, Long user) {
        if (month == null || month == 0 || user == null || user == 0L) {
            return;
        }

        Map<Long, Long> notifyUsers = new HashMap<>();
        boolean sendNotify = false;
        Long totalIncome = 0L;
        List<AgentUserKpiResultSpring2016> kpiResultList = agentUserKpiResultSpring2016Persistence.findUserKpiResultsByMonth(Collections.singleton(user), month);
        for (AgentUserKpiResultSpring2016 kpiResult : kpiResultList) {
            kpiResult.setMarketCheck(true);
            kpiResult.setUpdateDatetime(new Date());
            agentUserKpiResultSpring2016Persistence.update(kpiResult.getId(), kpiResult);

            // 如果数据已经被市场确认过，发送确认通知
            if (kpiResult.getFinanceCheck()) {
                sendNotify = true;
                totalIncome += kpiResult.getCpaSalary();
            }
        }

        if (sendNotify) {
            notifyUsers.put(user, totalIncome);
        }

        sendConfirmSalaryNotify(notifyUsers);
    }

    //工资确认完毕，给代理发送通知。（需全国总监和财务确认）
    private void sendConfirmSalaryNotify(Map<Long, Long> notifyUserMap) {
        Set<Long> userIds = notifyUserMap.keySet();
        DecimalFormat df = new DecimalFormat("###,##0.00");
        for (Long userId : userIds) {
            // fix IllegalFormatConversionException
            String content = "您的工资已经结算完毕。工资金额:" + df.format(notifyUserMap.get(userId));
            agentNotifyService.sendNotify(AgentNotifyType.SALARY_CONFIRM.getType(), content, Collections.singletonList(userId));
        }
    }

    public List<List<String>> getPartnerData(Integer salaryMonth) {
        // 找出所有代理角色的人员
        Set<Long> agentIds = baseOrgService.getGroupUserByRole(AgentRoleType.CityAgent.getId())
                .stream()
                .map(AgentGroupUser::getUserId)
                .collect(Collectors.toSet());
        Set<Long> agentLimitedIds = baseOrgService.getGroupUserByRole(AgentRoleType.CityAgentLimited.getId())
                .stream()
                .map(AgentGroupUser::getUserId)
                .collect(Collectors.toSet());
        List<String> juniorType = Arrays.asList(SalaryKpiType.JUNIOR_MEETING_INTERRUPTED.getDesc(), SalaryKpiType.JUNIOR_MEETING_COUNTY.getDesc(), SalaryKpiType.JUNIOR_MEETING_CITY.getDesc());
        List<String> middleType = Arrays.asList(SalaryKpiType.MIDDLE_MEETING_INTERRUPTED.getDesc(), SalaryKpiType.MIDDLE_MEETING_COUNTY.getDesc(), SalaryKpiType.MIDDLE_MEETING_CITY.getDesc());
        Set<Long> userIdSet = new HashSet<>();
        userIdSet.addAll(agentIds);
        userIdSet.addAll(agentLimitedIds);
        Map<Long, AgentUser> users = baseUserService.getUsers(userIdSet);
        List<List<String>> partnerData = new ArrayList<>();
        for (Long userId : userIdSet) {
            AgentUser agentUser = users.get(userId);
            if (agentUser == null) continue;
            List<AgentUserKpiResultSpring2016> kpiResult = agentUserKpiResultSpring2016Persistence.findUserIncome(salaryMonth, userId);
            List<String> junior = new ArrayList<>();
            junior.add(agentUser.getRealName());  // 合作伙伴名称
            junior.add(String.valueOf(salaryMonth)); // 月份
            junior.add("小学"); // 中小学
            junior.add("-"); // 城市
            junior.add("-"); // 城市级别
            AgentUserKpiResultSpring2016 jTemp = kpiResult.stream().findFirst().orElse(null);
            junior.add(jTemp == null ? "-" : SafeConverter.toString(jTemp.getRegionName()));
            junior.add(jTemp == null ? "-" : SafeConverter.toString(jTemp.getProvinceName()));
            AgentUserKpiResultSpring2016 juniorResult = kpiResult.stream().filter(t -> SalaryKpiType.JUNIOR_CLUE_SUPPORT.getDesc().equals(t.getCpaType())).findFirst().orElse(null);
            Map<String, String> juniorNotes = null;
            if (juniorResult != null && StringUtils.isNotBlank(juniorResult.getCpaNote())) {
                juniorNotes = Stream.of(SafeConverter.toString(juniorResult.getCpaNote()).replaceAll("\\s", "").split(","))
                        .filter(s -> StringUtils.isNotBlank(s) && s.contains("："))
                        .collect(Collectors.toMap(k -> k.substring(0, k.indexOf("：")), v -> v.substring(v.indexOf("：") + 1, v.length()), (s1, s2) -> s2));
            }
            boolean empty = MapUtils.isEmpty(juniorNotes);
            junior.add(empty ? "-" : SafeConverter.toString(juniorNotes.get("学生基数"))); // 学生基数
            junior.add(empty ? "-" : SafeConverter.toString(juniorNotes.get("基数")));  // 基数对应金额
            junior.add(empty ? "-" : Double.toString(SafeConverter.toDouble(juniorNotes.get("单活完成率")) * 100D) + "%"); // 完成率
            junior.add(empty ? "-" : SafeConverter.toString(juniorNotes.get("提供有效线索数")));   // 当月线索数量
            // 线索费用
            junior.add(juniorResult == null ? "-" : String.valueOf(juniorResult.getCpaSalary()));  // 线索费用
            junior.add(SafeConverter.toString(kpiResult.stream().filter(t -> juniorType.contains(t.getCpaType())).mapToLong(AgentUserKpiResultSpring2016::getCpaSalary).sum()));   // 组会费用
            junior.add(SafeConverter.toString(kpiResult.stream().filter(t -> juniorType.contains(t.getCpaType()) || SalaryKpiType.JUNIOR_CLUE_SUPPORT.getDesc().equals(t.getCpaType())).mapToLong(AgentUserKpiResultSpring2016::getCpaSalary).sum()));  // 本月费用合计
            partnerData.add(junior);


            List<String> middle = new ArrayList<>();
            middle.add(agentUser.getRealName());  // 合作伙伴名称
            middle.add(String.valueOf(salaryMonth)); // 月份
            middle.add("中学"); // 中小学
            middle.add("-"); // 城市
            middle.add("-"); // 城市级别
            middle.add(jTemp == null ? "-" : SafeConverter.toString(jTemp.getRegionName()));
            middle.add(jTemp == null ? "-" : SafeConverter.toString(jTemp.getProvinceName()));
            AgentUserKpiResultSpring2016 result = kpiResult.stream().filter(t -> SalaryKpiType.MIDDLE_CLUE_SUPPORT.getDesc().equals(t.getCpaType())).findFirst().orElse(null);
            Map<String, String> notes = null;
            if (result != null && StringUtils.isNotBlank(result.getCpaNote())) {
                notes = Stream.of(SafeConverter.toString(result.getCpaNote()).replaceAll("\\s", "").split(","))
                        .collect(Collectors.toMap(k -> k.substring(0, k.indexOf("：")), v -> v.substring(v.indexOf("：") + 1, v.length())));
            }
            empty = MapUtils.isEmpty(notes);
            middle.add(empty ? "-" : SafeConverter.toString(notes.get("学生基数"))); // 学生基数
            middle.add(empty ? "-" : SafeConverter.toString(notes.get("基数")));  // 基数对应金额
            middle.add(empty ? "-" : Double.toString(SafeConverter.toDouble(notes.get("单活完成率")) * 100D) + "%"); // 完成率
            middle.add(empty ? "-" : SafeConverter.toString(notes.get("提供有效线索数")));   // 当月线索数量
            middle.add(result == null ? "-" : String.valueOf(result.getCpaSalary()));  // 线索费用
            middle.add(SafeConverter.toString(kpiResult.stream().filter(t -> middleType.contains(t.getCpaType())).mapToLong(AgentUserKpiResultSpring2016::getCpaSalary).sum()));   // 组会费用
            middle.add(SafeConverter.toString(kpiResult.stream().filter(t -> middleType.contains(t.getCpaType()) || SalaryKpiType.MIDDLE_CLUE_SUPPORT.getDesc().equals(t.getCpaType())).mapToLong(AgentUserKpiResultSpring2016::getCpaSalary).sum()));  // 本月费用合计
            partnerData.add(middle);
        }
        return partnerData;
    }

}
