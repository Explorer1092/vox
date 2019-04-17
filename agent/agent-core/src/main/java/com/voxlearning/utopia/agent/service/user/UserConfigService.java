/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.agent.service.user;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.Password;
import com.voxlearning.alps.random.RandomGenerator;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.agent.service.common.BaseUserService;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by Shuai.Huan on 2014/7/10.
 */
@Named
public class UserConfigService extends AbstractAgentService {

    @Inject
    private BaseUserService baseUserService;
//    @Inject
//    private AgentNotifyService agentNotifyService;
//    @Inject
//    private AgentRegionService agentRegionService;
//    @Inject
//    private BaseGroupService baseGroupService;
//    @Inject
//    private BaseOrderService baseOrderService;
//    @Inject
//    private WorkFlowEngine workFlowEngine;
//
//    /**
//     * 获取所在群组的子群组中的所有用户，并组装数据
//     *
//     * @param curUser current login user
//     * @return 组装数据
//     */
//    public List<Map<String, Object>> getChargedUserMapList(AuthCurrentUser curUser, Long userId) {
//        // 检查用户是否有管理该User的权限
//        if (userId > 0 && !curUser.isAdmin()) {
//            List<AgentUser> allManagedUserList = baseUserService.deepLoadUserMemebers(curUser.getUserId());
//            boolean noManageRight = true;
//            for (AgentUser subUser : allManagedUserList) {
//                if (subUser.getId().equals(userId)) {
//                    noManageRight = false;
//                    break;
//                }
//            }
//            if (noManageRight) {
//                logger.warn("User " + curUser.getUserName() + " " + curUser.getUserId() + "try to view user " + userId);
//                return Collections.emptyList();
//            }
//        }
//
//        List<AgentUser> userList;
//        if (userId == 0) {
//            if (curUser.isAdmin()) {  // 管理员可以查看所有根用户
//                userList = baseUserService.getAllRootGroupUserList();
//            } else { // 其他人员看直属组员
//                userList = baseUserService.getDirectManagedUserList(curUser.getUserId());
//            }
//        } else { // 查看指定用户下的直属组员
//            userList = baseUserService.getDirectManagedUserList(userId);
//        }
//
//        if (userList == null || userList.size() == 0) {
//            return Collections.emptyList();
//        }
//
//        List<Map<String, Object>> retUserMapList = new ArrayList<>();
//        for (AgentUser user : userList) {
//            MiscUtils.addNonNullElement(retUserMapList, getUserInfoMap(user.getId()));
//        }
//
//        return retUserMapList;
//    }
//
//
//    // 组装画面显示用的用户信息数据
//    private Map<String, Object> getUserInfoMap(Long userId) {
//        AgentUser user = baseUserService.getById(userId);
//        if (user == null) {
//            return null;
//        }
//
//        Map<String, Object> retMap = new HashMap<>();
//
//        // user basic information
//        retMap.put("id", String.valueOf(user.getId()));
//        retMap.put("accountName", user.getAccountName());
//        retMap.put("realName", user.getRealName());
//        retMap.put("userComment", user.getUserComment());
//        retMap.put("contractStartDate", user.getContractStartDate());
//        retMap.put("contractEndDate", user.getContractEndDate());
//        retMap.put("cashDeposit", user.getCashDeposit());
//        retMap.put("cashDepositReceived", user.getCashDepositReceived());
//
//        // 用户所在群组
//        List<AgentGroup> groups = baseGroupService.getAffiliationGroups(userId);
//        String groupName = StringUtils.EMPTY;
//        for (AgentGroup group : groups) {
//            groupName += group.getGroupName() + "<br/>";
//        }
//        retMap.put("groupName", groupName);
//
//        // 用户负责学校
////        List<AgentUserSchool> agentUserSchools = baseUserService.getUserSchoolsByUserId(userId);
////        if(CollectionUtils.isNotEmpty(agentUserSchools)){
////            String schoolName = StringUtils.EMPTY;
////            for(AgentUserSchool agentUserSchool : agentUserSchools){
////                School school = internalSchoolLoader.loadSchool(agentUserSchool.getSchoolId());
////                if(school == null) continue;
////                schoolName += school.getCname() + "<br/>";
////            }
////            retMap.put("schoolName", schoolName);
////        }
//
//        // 用户是否有下属
//        List<AgentUser> memberList = baseUserService.getDirectManagedUserList(userId);
//        retMap.put("memberList", memberList);
//
//        return retMap;
//    }
//
//    /**
//     * 用户名是否存在
//     *
//     * @param accountName 用户名
//     * @return 是否存在
//     */
//    public boolean sysUserExist(String accountName) {
//        return baseUserService.getByAccountName(accountName) != null;
//    }
//
//
//    /**
//     * 创建新用户，设置用户群组，学校
//     *
//     * @param accountName       用户名
//     * @param realName          真实姓名
//     * @param password          密码
//     * @param tel               电话
//     * @param email             邮箱
//     * @param imAccount         通讯号(qq号)
//     * @param address           地址
//     * @param userComment       用户备注
//     * @param cashDeposit       保证金
//     * @param bankName          开户行名称
//     * @param bankHostname      开户人姓名
//     * @param bankAccount       银行账号
//     * @param contractStartDate 合同开始时间
//     * @param contractEndDate   合同结束时间
//     * @param groups            所属群组列表
//     * @return 用户id
//     */
//    public Long addAgentUser(String accountName, String realName, String password, String tel, String email, String imAccount, String address,
//                             Integer cashDeposit, String bankName, String bankHostname, String bankAccount, String userComment,
//                             Date contractStartDate, Date contractEndDate, String contractNumber, List<Long> groups, Integer schoolLevel) {
//        if (StringUtils.isEmpty(accountName) || StringUtils.isEmpty(realName) || StringUtils.isEmpty(password) || sysUserExist(accountName)) {
//            return 0L;
//        }
//
//        AgentUser agentUser = new AgentUser();
//        agentUser.setAccountName(accountName);
//        agentUser.setRealName(realName);
//        if (!StringUtils.isEmpty(userComment)) {
//            agentUser.setUserComment(userComment);
//        }
//        Password passWd = encryptPassword(password);
//        agentUser.setPasswd(passWd.getPassword());
//        agentUser.setPasswdSalt(passWd.getSalt());
//        agentUser.setContractStartDate(contractStartDate);
//        agentUser.setContractEndDate(contractEndDate);
//        agentUser.setContractNumber(contractNumber);
//        agentUser.setStatus(AgentUserType.INITIAL.getStatus());
//        agentUser.setCashAmount(0f);
//        agentUser.setPointAmount(0f);
//        agentUser.setUsableCashAmount(0f);
//        agentUser.setUsablePointAmount(0f);
//        agentUser.setTel(tel);
//        agentUser.setEmail(email);
//        agentUser.setImAccount(imAccount);
//        agentUser.setAddress(address);
//        agentUser.setCashDeposit(cashDeposit);
//        agentUser.setBankName(bankName);
//        agentUser.setBankHostName(bankHostname);
//        agentUser.setBankAccount(bankAccount);
//        //agentUser.setSchoolLevel(schoolLevel);
//        Long userId = baseUserService.createAgentUser(agentUser);
//
//        if (CollectionUtils.isNotEmpty(groups)) {
//            for (Long groupId : groups) {
//                if (groupId == null) continue;
//                AgentGroupUser agentGroupUser = new AgentGroupUser();
//                agentGroupUser.setGroupId(groupId);
//                agentGroupUser.setUserId(userId);
//                baseGroupService.saveGroupUser(agentGroupUser);
//            }
//        }
//
//        return userId;
//    }
//
//    public String detechDupliateAgent(Long userId, List<Long> groups) {
//        StringBuilder buffer = new StringBuilder();
//        List<AgentGroup> groupList = baseUserService.detectDuplicateGroupAgent(userId, groups);
//        if (groupList != null && groupList.size() > 0) {
//            for (AgentGroup group : groupList) {
//                buffer.append(group.getGroupName()).append(",");
//            }
//        }
//
//        if (buffer.toString().length() > 0) {
//            buffer.deleteCharAt(buffer.toString().length() - 1);
//        }
//
//        return buffer.toString();
//    }
//
//    /**
//     * 更新用户信息，用户群组信息，用户学校信息
//     *
//     * @param userId            用户id
//     * @param realName          真实姓名
//     * @param userComment       用户备注
//     * @param tel               电话
//     * @param email             邮箱
//     * @param imAccount         通讯号(qq号)
//     * @param address           地址
//     * @param cashDeposit       保证金
//     * @param bankName          开户行名称
//     * @param bankHostname      开户人姓名
//     * @param bankAccount       银行账号
//     * @param contractStartDate 合同开始时间
//     * @param contractEndDate   合同结束时间
//     * @param groups            所属群组列表
//     * @return 更新成功标志
//     */
//    public boolean updateSysUser(final Long userId, final String realName, final String userComment,
//                                 final String tel, final String email, final String imAccount, final String address,
//                                 final Integer cashDeposit, final String bankName, final String bankHostname, final String bankAccount,
//                                 final Date contractStartDate, final Date contractEndDate, final String contractNumber, final List<Long> groups, Integer schoolLevel) {
//        if (userId <= 0) {
//            return false;
//        }
//
//        AgentUser agentUser = new AgentUser();
//        agentUser.setId(userId);
//        agentUser.setRealName(StringUtils.trim(realName));
//        if (!StringUtils.isEmpty(userComment)) {
//            agentUser.setUserComment(userComment);
//        }
//        agentUser.setContractStartDate(contractStartDate);
//        agentUser.setContractEndDate(contractEndDate);
//        agentUser.setContractNumber(contractNumber);
//        agentUser.setTel(tel);
//        agentUser.setEmail(email);
//        agentUser.setImAccount(imAccount);
//        agentUser.setAddress(address);
//        agentUser.setCashDeposit(cashDeposit);
//        agentUser.setBankName(bankName);
//        agentUser.setBankHostName(bankHostname);
//        agentUser.setBankAccount(bankAccount);
//        // agentUser.setSchoolLevel(schoolLevel);
//        baseUserService.updateAgentUser(agentUser);
//
//        //更新user group
//        if (CollectionUtils.isNotEmpty(groups)) {
//            List<AgentGroupUser> agentGroupUsers = baseGroupService.getGroupUsersByUserId(userId);
//            for (AgentGroupUser agentGroupUser : agentGroupUsers) {
//                baseGroupService.deleteGroupUser(agentGroupUser.getId());
//            }
//
//            for (Long groupId : groups) {
//                if (groupId == null) continue;
//                AgentGroupUser agentGroupUser = new AgentGroupUser();
//                agentGroupUser.setGroupId(groupId);
//                agentGroupUser.setUserId(userId);
//                baseGroupService.saveGroupUser(agentGroupUser);
//            }
//        }
//
//        return true;
//    }
//
//    /**
//     * 删除用户，由于后续还要继续计算工资，所以需要保留群组关系
//     *
//     * @param userId 用户id
//     * @return 删除成功标志
//     */
//    public boolean deleteSysUser(Long userId) {
//        if (userId < 0) {
//            return false;
//        }
//
//        baseUserService.deleteAgentUser(userId);
//
////        List<AgentGroupUser> agentGroupUsers = baseGroupService.getGroupUsersByUserId(userId);
////        for (AgentGroupUser agentGroupUser : agentGroupUsers) {
////            baseGroupService.deleteGroupUser(agentGroupUser.getId());
////        }
//
//        return true;
//    }
//
    /**
     * 重设密码
     *
     * @param userId      用户id
     * @param password    密码
     * @param newPassword 重复密码
     * @param settingFlag false:上级重设密码  true:自己重设密码
     * @return 重设密码结果
     */
    public MapMessage resetPassword(Long userId, String password, String newPassword, boolean settingFlag) {

        MapMessage mapMessage = new MapMessage();

        AgentUser agentUser = baseUserService.getById(userId);
        if (agentUser != null) {
            if (settingFlag && (StringUtils.isEmpty(password) || !Password.obscurePassword(password, agentUser.getPasswdSalt()).equals(agentUser.getPasswd()))) {
                mapMessage.setSuccess(false);
                mapMessage.setInfo("输入的当前密码错误!");
                return mapMessage;
            }
            Password passwd = encryptPassword(newPassword);
            agentUser.setPasswd(passwd.getPassword());
            agentUser.setPasswdSalt(passwd.getSalt());
            agentUser.setStatus(1);
            baseUserService.updateAgentUser(agentUser);
            mapMessage.setSuccess(true);
            mapMessage.setInfo("重设密码成功!");
        }
        return mapMessage;
    }

    /**
     * 生成密码
     *
     * @param password 密码串
     * @return 密码实体
     */
    public Password encryptPassword(String password) {
        try {
            return RandomGenerator.generatePassword(password);
        } catch (Exception ex) {
            logger.error("Payment password is invalid: {}", password);
        }
        return null;
    }
//
//    public List<AgentUser> getChargedUsers(AuthCurrentUser user) {
//        List<AgentGroup> agentGroupList = baseGroupService.getManagedGroupList(user, 0L); // TODO
//        Set<AgentUser> agentUsers = new HashSet<>();
//        for (AgentGroup agentGroup : agentGroupList) {
//            agentUsers.addAll(agentGroup.getAgentUserList());
//        }
//        return new ArrayList<>(agentUsers);
//    }
//
//
//    /**
//     * 获取我所在group的所有parentGroup里的users
//     *
//     * @param userId 用户ID
//     * @return leaders
//     */
//    private List<Long> getAllLeaders(Long userId) {
//        Set<Long> leaderIds = new HashSet<>();
//        List<Long> groupIds = new ArrayList<>();
//        List<AgentGroup> agentGroups = baseGroupService.getAffiliationGroups(userId);
//        for (AgentGroup agentGroup : agentGroups) {
//            groupIds.addAll(baseGroupService.getParentGroupIds(agentGroup.getParentId()));
//        }
//        for (Long groupId : groupIds) {
//            AgentGroup agentGroup = baseGroupService.getComposedAgentGroupById(groupId);
//            if (agentGroup != null) {
//                List<AgentUser> agentUsers = agentGroup.getAgentUserList();
//                for (AgentUser agentUser : agentUsers) {
//                    leaderIds.add(agentUser.getId());
//                }
//            }
//        }
//        return new ArrayList<>(leaderIds);
//    }
//
//    public void sendUserNotify(Long userId, String content) {
//        List<Long> receivers = getAllLeaders(userId);
//        agentNotifyService.sendNotify(AgentNotifyType.ADD_NEW_USER.getType(), content, receivers);
//    }
//
//    public void initDepositCashWorkflow(AuthCurrentUser user, Integer cashDeposit, String accountName, Long addedUserId) {
//
//        AgentUser agentUser = baseUserService.getById(addedUserId);
//        //如果保证金还没有被财务确认，并且保证金的金额发生了变化，则删除之前的保证金订单流程，重新建立新流程
//        if (agentUser != null && (agentUser.getCashDepositReceived() == null || !agentUser.getCashDepositReceived())) {
//            AgentOrder order = baseOrderService.getOrderByNotes(accountName);
//            //已经产生过order，将其删除
//            if (order != null) {
//                order.setOrderStatus(AgentOrderStatus.DELETED.getStatus());
//                baseOrderService.saveOrder(order);
//                baseOrderService.deleteProcessByOrderId(order.getId());
//            }
//
//            AgentOrder agentOrder = new AgentOrder();
//            agentOrder.setCreator(user.getUserId());
//            agentOrder.setCreatorGroup(0L);
//            agentOrder.setLatestProcessor(user.getUserId());
//            agentOrder.setLatestProcessorGroup(0L);
//            agentOrder.setOrderAmount(cashDeposit.floatValue());
//            agentOrder.setOrderStatus(AgentOrderStatus.INIT.getStatus());
//            agentOrder.setOrderType(AgentOrderType.DEPOSIT.getType());
//            agentOrder.setOrderNotes(accountName);
//
//            Long orderId = baseOrderService.saveOrder(agentOrder);
//            agentOrder.setId(orderId);
//
//            WorkFlowContext workFlowContext = new WorkFlowContext(agentOrder, user);
//            workFlowEngine.getWorkFlowProcessorFactory(workFlowContext).getProcessor(workFlowContext).agree(workFlowContext);
//        }
//    }
//
//    public void addCashDeposit(AuthCurrentUser creator, Long userId, Long cashDeposit, String cashDepositDesc) {
//        AgentUser agentUser = baseUserService.getById(userId);
//        if (agentUser == null) {
//            throw new RuntimeException("该账户已经被关闭!");
//        }
//        // 追加保证金直接发起新流程
//        AgentOrder agentOrder = new AgentOrder();
//        agentOrder.setCreator(creator.getUserId());
//        agentOrder.setCreatorGroup(0L);
//        agentOrder.setLatestProcessor(creator.getUserId());
//        agentOrder.setLatestProcessorGroup(0L);
//        agentOrder.setOrderAmount(cashDeposit.floatValue());
//        agentOrder.setOrderStatus(AgentOrderStatus.INIT.getStatus());
//        agentOrder.setOrderType(AgentOrderType.DEPOSIT.getType());
//        agentOrder.setOrderNotes(agentUser.getAccountName() + "#" + cashDepositDesc);
//
//        Long orderId = baseOrderService.saveOrder(agentOrder);
//        agentOrder.setId(orderId);
//
//        WorkFlowContext workFlowContext = new WorkFlowContext(agentOrder, creator);
//        workFlowEngine.getWorkFlowProcessorFactory(workFlowContext).getProcessor(workFlowContext).agree(workFlowContext);
//    }

}
