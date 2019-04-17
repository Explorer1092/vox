package com.voxlearning.utopia.service.mizar.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MobileRule;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.beanutils.BeanUtils;
import com.voxlearning.utopia.service.mizar.api.entity.oa.OfficialAccounts;
import com.voxlearning.utopia.service.mizar.api.entity.user.*;
import com.voxlearning.utopia.service.mizar.api.service.MizarUserService;
import com.voxlearning.utopia.service.mizar.impl.dao.user.*;
import org.apache.commons.codec.digest.DigestUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by wangshichao on 16/9/6.
 */

@Named
@Service(interfaceClass = MizarUserService.class)
@ExposeService(interfaceClass = MizarUserService.class)
public class MizarUserServiceImpl implements MizarUserService {

    @Inject private MizarUserDao mizarUserDao;
    @Inject private MizarUserLogDao mizarUserLogDao;
    @Inject private MizarUserShopDao mizarUserShopDao;
    @Inject private MizarDepartmentDao mizarDepartmentDao;
    @Inject private MizarUserDepartmentDao mizarUserDepartmentDao;
    @Inject private MizarUserOfficialAccountsDao mizarUserOfficialAccountsDao;


    @Override
    public MapMessage editMizarUserPassWord(String userId, String passWord) {

        MizarUser mizarUser = mizarUserDao.findById(userId);
        MizarUser newMizarUser = null;
        try {
            newMizarUser = (MizarUser) BeanUtils.cloneBean(mizarUser);
        } catch (Exception e) {
            return MapMessage.errorMessage("更改密码失败:" + e.getMessage());
        }
        String codecPassWord = genCodecPassWord(passWord, newMizarUser.getPasswordSalt());
        newMizarUser.setPassword(codecPassWord);
        newMizarUser.setStatus(1);
        mizarUserDao.upsert(newMizarUser);
        Map<String, Object> map = new HashMap<>();
        map.put("before", mizarUser);
        map.put("after", newMizarUser);
        MizarUserLog mizarUserLog = new MizarUserLog();
        mizarUserLog.setContent(map);
        mizarUserLog.setUserId(mizarUser.getId());
        mizarUserLogDao.insert(mizarUserLog);
        return MapMessage.successMessage();
    }

    @Override
    public MizarUser login(String token, String passWord) {
        if (StringUtils.isBlank(token)) {
            return null;
        }
        MizarUser mizarUser;
        if (MobileRule.isMobile(token)) {
            mizarUser = mizarUserDao.findByMobile(token);
        } else {
            mizarUser = mizarUserDao.findByAccount(token);
        }
        if (mizarUser == null) {
            return null;
        }
        if (checkPassWord(passWord, mizarUser)) {
            return mizarUser;
        }
        return null;
    }

    @Override
    public MapMessage addMizarUser(MizarUser mizarUser) {
        try {
            String salt = genSalt(mizarUser);
            String codecPassWord = genCodecPassWord(mizarUser.getPassword(), salt);
            mizarUser.setPassword(codecPassWord);
            mizarUser.setStatus(0);
            mizarUser.setPasswordSalt(salt);
            mizarUserDao.insert(mizarUser);
            return MapMessage.successMessage().add("newId", mizarUser.getId());
        } catch (Exception ex) {
            return MapMessage.errorMessage("添加用户失败: " + ex.getMessage());
        }
    }

    @Override
    public MapMessage editMizarUser(MizarUser mizarUser) {
        try {
            mizarUserDao.upsert(mizarUser);
            return MapMessage.successMessage();
        } catch (Exception ex) {
            return MapMessage.errorMessage("编辑用户失败: " + ex.getMessage());
        }
    }

    @Override
    public MapMessage closeAccount(String userId) {
        try {
            MapMessage msg = new MapMessage();
            MizarUser user = mizarUserDao.closeAccount(userId);
            msg.setSuccess(user != null);
            msg.setInfo(msg.isSuccess() ? "操作成功" : "操作失败");
            return msg;
        } catch (Exception ex) {
            return MapMessage.errorMessage("关闭账户失败: " + ex.getMessage());
        }
    }

    @Override
    public MapMessage disableUserShop(String userId, String shopId) {
        if (StringUtils.isBlank(userId) || StringUtils.isBlank(shopId)) {
            return MapMessage.errorMessage("参数错误");
        }
        try {
            MapMessage msg = new MapMessage();
            MizarUserShop userShop = mizarUserShopDao.disableUserShop(userId, shopId);
            msg.setSuccess(userShop != null);
            msg.setInfo(msg.isSuccess() ? "操作成功" : "操作失败");
            return msg;
        } catch (Exception ex) {
            return MapMessage.errorMessage("解除用户机构失败：" + ex.getMessage());
        }
    }

    @Override
    public MapMessage disableUserOfficialAccounts(String userId, String accountsKey) {
        if (StringUtils.isBlank(userId) || StringUtils.isBlank(accountsKey)) {
            return MapMessage.errorMessage("参数错误");
        }
        try {
            MapMessage msg = new MapMessage();
            MizarUserOfficialAccounts userOfficialAccounts = mizarUserOfficialAccountsDao.disableUserOfficialAccounts(userId, accountsKey);
            msg.setSuccess(userOfficialAccounts != null);
            msg.setInfo(msg.isSuccess() ? "操作成功" : "操作失败");
            return msg;
        } catch (Exception ex) {
            return MapMessage.errorMessage("解除用户公众号失败：" + ex.getMessage());
        }
    }

    @Override
    public MapMessage saveUserShops(String userId, Collection<String> shopIds) {
        if (StringUtils.isBlank(userId) || CollectionUtils.isEmpty(shopIds)) {
            return MapMessage.errorMessage("参数错误");
        }
        try {
            shopIds.forEach(shop -> {
                MizarUserShop userShop = new MizarUserShop(userId, shop);
                mizarUserShopDao.insert(userShop);
            });
            return MapMessage.successMessage();
        } catch (Exception ex) {
            return MapMessage.errorMessage("解除用户机构失败：" + ex.getMessage());
        }
    }

    @Override
    public MapMessage saveUserOfficialAccounts(String userId, List<OfficialAccounts> officialAccountsList) {
        if (StringUtils.isBlank(userId) || CollectionUtils.isEmpty(officialAccountsList)) {
            return MapMessage.errorMessage("参数错误");
        }
        try {
            officialAccountsList.forEach(officialAccounts -> {
                MizarUserOfficialAccounts mizarUserOfficialAccounts = new MizarUserOfficialAccounts(userId, officialAccounts.getAccountsKey());
                mizarUserOfficialAccountsDao.insert(mizarUserOfficialAccounts);
            });
            return MapMessage.successMessage("保存成功");
        } catch (Exception ex) {
            return MapMessage.errorMessage("保存用户公众号失败：" + ex.getMessage());
        }
    }

    @Override
    public MapMessage upsertDepartment(MizarDepartment department) {
        try {
            mizarDepartmentDao.upsert(department);
            return MapMessage.successMessage();
        } catch (Exception ex) {
            return MapMessage.errorMessage("添加部门失败: " + ex.getMessage());
        }
    }

    @Override
    public MapMessage closeDepartment(String departmentId) {
        try {
            MizarDepartment department = mizarDepartmentDao.load(departmentId);
            if (department == null || !department.isValid()) {
                return MapMessage.errorMessage("未知的部门ID:" + departmentId);
            }

            List<MizarUserDepartment> departmentUsers = mizarUserDepartmentDao.findByDepartment(departmentId);
            // 如果有任意一个状态有效的用户在这个部门下，就不能关闭部门
            if (CollectionUtils.isNotEmpty(departmentUsers) &&
                    departmentUsers.stream().anyMatch(ud -> mizarUserDao.load(ud.getUserId()).isValid())) {
                return MapMessage.errorMessage("请先将部门用户关闭或者转移到其他部门!");
            }

            department.setDisabled(true);
            mizarDepartmentDao.upsert(department);

            return MapMessage.successMessage();
        } catch (Exception ex) {
            return MapMessage.errorMessage("关闭部门失败: " + ex.getMessage());
        }
    }

    @Override
    public MapMessage addDepartmentUser(String departmentId, String userId, List<Integer> roles) {
        try {
            MizarDepartment department = mizarDepartmentDao.load(departmentId);
            if (department == null || !department.isValid()) {
                return MapMessage.errorMessage("未知的部门ID:" + departmentId);
            }

            MizarUser user = mizarUserDao.load(userId);
            if (user == null || !user.isValid()) {
                return MapMessage.errorMessage("未知的用户ID:" + userId);
            }

            List<MizarUserDepartment> userDepartments = mizarUserDepartmentDao.findByUser(userId);
            userDepartments = userDepartments.stream()
                    .filter(p -> departmentId.equals(p.getDepartmentId()))
                    .collect(Collectors.toList());

            MizarUserDepartment userDepartment;
            if (CollectionUtils.isNotEmpty(userDepartments)) {
                userDepartment = userDepartments.get(0);
            } else
                userDepartment = new MizarUserDepartment(userId, departmentId);

            userDepartment.setRoles(roles);
            mizarUserDepartmentDao.upsert(userDepartment);

            return MapMessage.successMessage();
        } catch (Exception ex) {
            return MapMessage.errorMessage("加入部门失败: " + ex.getMessage());
        }
    }

    @Override
    public MapMessage removeDepartmentUser(String departmentId, String userId) {
        try {
            MizarDepartment department = mizarDepartmentDao.load(departmentId);
            if (department == null || !department.isValid()) {
                return MapMessage.errorMessage("未知的部门ID:" + departmentId);
            }

            MizarUser user = mizarUserDao.load(userId);
            if (user == null || !user.isValid()) {
                return MapMessage.errorMessage("未知的用户ID:" + userId);
            }

            List<MizarUserDepartment> userDepartments = mizarUserDepartmentDao.findByUser(userId);
            userDepartments = userDepartments.stream()
                    .filter(p -> departmentId.equals(p.getDepartmentId()))
                    .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(userDepartments)) {
                return MapMessage.successMessage();
            }

            MizarUserDepartment userDepartment = userDepartments.get(0);
            userDepartment.setDisabled(true);
            mizarUserDepartmentDao.upsert(userDepartment);

            return MapMessage.successMessage();
        } catch (Exception ex) {
            return MapMessage.errorMessage("从部门中删除用户失败: " + ex.getMessage());
        }
    }

    private String genSalt(MizarUser mizarUser) {
        String mobile = mizarUser.getMobile();
        String salt;
        // 如果没有传入手机号，随机生成一个字符串作为salt
        if (StringUtils.isBlank(mobile)) {
            salt = RandomUtils.randomString(8);
        } else {
            if (mobile.length() > 8) {
                salt = mobile.substring(0, 8);
            } else {
                salt = mobile;
            }
        }
        return salt;
    }

    private boolean checkPassWord(String passWord, MizarUser mizarUser) {
        String codecPassWord = genCodecPassWord(passWord, mizarUser.getPasswordSalt());
        return codecPassWord.equals(mizarUser.getPassword());
    }

    private String genCodecPassWord(String passWord, String salt) {
        return DigestUtils.sha1Hex(passWord + salt);
    }

}