package com.voxlearning.utopia.admin.controller.mizar;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MobileRule;
import com.voxlearning.utopia.service.mizar.api.constants.MizarUserRoleType;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarShop;
import com.voxlearning.utopia.service.mizar.api.entity.user.MizarUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 商户平台用户管理 By Wyc 2016-09-09
 */

@Controller
@RequestMapping("/mizar/user")
public class CrmMizarUserController extends CrmMizarAbstractController {

    private static final String DefaultPassword = "17zy@mizar";
    private static final List<String> Modes = Arrays.asList("add", "edit");

    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String userIndex(Model model) {
        String token = getRequestString("token");
        List<MizarUser> mizarUsers = new ArrayList<>();
        if (StringUtils.isBlank(token)) {
            mizarUsers = mizarUserLoaderClient.loadAllUsers();
        } else {
            MizarUser user = mizarUserLoaderClient.loadUserByToken(token);
            if (user != null) mizarUsers.add(user);
        }
        mizarUsers = mizarUsers.stream().filter(MizarUser::isValid).collect(Collectors.toList());
        model.addAttribute("allUser", mizarUsers);
        model.addAttribute("token", token);
        model.addAttribute("allRoleMap", MizarUserRoleType.getAllMizarUserRoles());
        return "mizar/user/userindex";
    }

    @RequestMapping(value = "saveuser.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveUser() {
        String accountName = getRequestString("accountName");
        String realName = getRequestString("realName");
        String mobile = getRequestString("mobile");
        String comment = getRequestString("comment");
        String mode = getRequestString("mode");
        String userId = getRequestString("uid");
        String roleStr = getRequestString("roles");
        if (StringUtils.isBlank(accountName) || StringUtils.isBlank(realName)) {
            return MapMessage.errorMessage("【登录名】/【真实姓名】不能为空");
        }
        if (!MobileRule.isMobile(mobile)) {
            return MapMessage.errorMessage("无效的【手机号码】");
        }
        String[] roles = roleStr.split(",");
        List<Integer> userRoles = new ArrayList<>();
        for (String userRole : roles) {
            userRoles.add(SafeConverter.toInt(userRole));
        }
        try {
            // 默认是新增模式
            mode = Modes.contains(mode) ? mode : "add";
            if ("add".equals(mode)) {
                MizarUser user = new MizarUser();
                user.setAccountName(accountName);
                user.setRealName(realName);
                user.setMobile(mobile);
                user.setUserComment(comment);
                user.setPassword(DefaultPassword);
                //user.setUserRoles(userRoles);
                return mizarUserServiceClient.addMizarUser(user);
            } else {
                MizarUser user = mizarUserLoaderClient.loadUser(userId);
                if (user == null) {
                    return MapMessage.errorMessage("无效的用户信息！");
                }
                user.setAccountName(accountName);
                user.setRealName(realName);
                user.setMobile(mobile);
                user.setUserComment(comment);
                //user.setUserRoles(userRoles);
                return mizarUserServiceClient.editMizarUser(user);
            }
        } catch (Exception ex) {
            return MapMessage.errorMessage("保存用户信息失败" + ex.getMessage());
        }

    }

    @RequestMapping(value = "resetpwd.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage resetPwd() {
        String userId = getRequestString("userId");
        try {
            return mizarUserServiceClient.resetPassword(userId, DefaultPassword);
        } catch (Exception ex) {
            logger.error("Failed reset mizar user password, id={}.", userId, ex);
            return MapMessage.errorMessage("重置密码失败：" + ex.getMessage());
        }

    }

    @RequestMapping(value = "closeaccount.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage closeAccount() {
        String userId = getRequestString("userId");
        try {
            return mizarUserServiceClient.closeAccount(userId);
        } catch (Exception ex) {
            logger.error("Failed close mizar user account, id={}.", userId, ex);
            return MapMessage.errorMessage("账户关闭失败：" + ex.getMessage());
        }
    }

    @RequestMapping(value = "usershoplist.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage userShopList() {
        String userId = getRequestString("userId");
        try {
            List<String> shopIds = mizarUserLoaderClient.loadUserShopId(userId);
            Map<String, MizarShop> shopMap = mizarLoaderClient.loadShopByIds(shopIds);
            return MapMessage.successMessage().add("shopList", mapShopInfo(shopMap));
        } catch (Exception ex) {
            logger.error("Failed load mizar user shop account, id={}.", userId, ex);
            return MapMessage.errorMessage("获取用户账户失败：" + ex.getMessage());
        }
    }

    @RequestMapping(value = "saveusershop.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveUserShop() {
        String userId = getRequestString("userId");
        String shopId = getRequestString("shopId");
        List<String> shopIds = splitString(shopId);
        if (CollectionUtils.isEmpty(shopIds)) {
            return MapMessage.errorMessage("无效的机构ID");
        }
        try {
            Map<String, MizarShop> shops = mizarLoaderClient.loadShopByIds(shopIds);
            if (MapUtils.isEmpty(shops)) {
                return MapMessage.errorMessage("无效的机构ID");
            }
            return mizarUserServiceClient.saveUserShops(userId, shops.keySet()).add("shop", mapShopInfo(shops));
        } catch (Exception ex) {
            logger.error("Failed save mizar user account, id={}.", userId, ex);
            return MapMessage.errorMessage("关联用户机构失败：" + ex.getMessage());
        }
    }

    @RequestMapping(value = "delusershop.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteUserShop() {
        String userId = getRequestString("userId");
        String shopId = getRequestString("shopId");
        try {
            return mizarUserServiceClient.disableUserShop(userId, shopId);
        } catch (Exception ex) {
            logger.error("Failed save mizar user account, id={}.", userId, ex);
            return MapMessage.errorMessage("删除用户机构失败：" + ex.getMessage());
        }
    }

    private List<Map<String, Object>> mapShopInfo(Map<String, MizarShop> shopMap) {
        if (MapUtils.isEmpty(shopMap)) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (MizarShop shop : shopMap.values()) {
            Map<String, Object> info = new HashMap<>();
            info.put("shopId", shop.getId());
            info.put("shopName", shop.getFullName());
            result.add(info);
        }
        return result;
    }

}
