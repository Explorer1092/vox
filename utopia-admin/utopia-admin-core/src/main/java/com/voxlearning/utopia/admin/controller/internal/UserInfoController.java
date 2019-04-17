package com.voxlearning.utopia.admin.controller.internal;

import com.voxlearning.alps.cipher.AesUtils;
import com.voxlearning.alps.config.manager.ConfigManager;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.ICharset;
import com.voxlearning.alps.webmvc.cookie.AuthCookieMappingInfo;
import com.voxlearning.utopia.admin.controller.AbstractAdminController;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.voxlearning.alps.webmvc.cookie.CookieManager.ENCRYPTION_TAG;

/**
 * @author changyuan
 * @since 2017/3/22
 */
@Controller
@RequestMapping("/internal")
public class UserInfoController extends AbstractAdminController {

    @Inject private UserLoaderClient userLoaderClient;

    private static byte[] CEK;

    static {
        String cek = ConfigManager.instance().getCommonConfig().getConfigs().get("cookie_encryption_key");
        CEK = SafeConverter.stringToBytes(cek);
    }

    @Getter
    @Setter
    private static class UserInfo implements Serializable {

        private static final long serialVersionUID = 892076908803328765L;
        String voxauth;
        String va_sess;
        String uid;
    }

    @RequestMapping(value = "getuserinfo.vpage", method = {RequestMethod.POST})
    @ResponseBody
    private MapMessage getUserInfo(@RequestBody Map<String, Object> map) {
        List ids = (List)map.get("ids");
        List<Long> userIds = (List<Long>) ids.stream().map(e -> SafeConverter.toLong(e)).collect(Collectors.toList());

        Map<Long, User> users = userLoaderClient.loadUsers(userIds);
        Map<Long, UserInfo> result = new HashMap<>();
        users.forEach((id, u) -> {
            UserAuthentication ua = userLoaderClient.loadUserAuthentication(u.getId());
            AuthCookieMappingInfo mappingInfo = new AuthCookieMappingInfo(u.getId(), new ArrayList<>(userLoaderClient.loadUserRoles(u)), ua.getPassword());
            String cookieValue = encryptCookie(mappingInfo.toCookieValue());

            UserInfo userInfo = new UserInfo();
            userInfo.uid = u.getId().toString();
            userInfo.va_sess = cookieValue;
            userInfo.voxauth = cookieValue;
            result.put(u.getId(), userInfo);
        });

        return MapMessage.successMessage().add("data", result);
    }

    private String encryptCookie(String value) {
        String s = AesUtils.encryptBase64String(CEK, (ENCRYPTION_TAG + value).getBytes(ICharset.defaultCharset()));
        return StringUtils.stripEnd(s, "=");
    }
}
