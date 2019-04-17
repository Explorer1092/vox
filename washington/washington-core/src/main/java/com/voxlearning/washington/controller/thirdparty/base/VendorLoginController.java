package com.voxlearning.washington.controller.thirdparty.base;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.core.utils.LoggerUtils;
import com.voxlearning.utopia.service.user.api.ThirdPartyService;
import com.voxlearning.utopia.service.user.api.entities.LandingSource;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.washington.controller.open.AbstractApiController;

import java.util.Date;

import static com.voxlearning.washington.controller.open.ApiConstants.REQ_APP_KEY;

/**
 * 第三方登录
 * @author chongfeng.qi
 * @data 20181207
 */
public abstract class VendorLoginController extends AbstractApiController {

    @ImportService(interfaceClass = ThirdPartyService.class)
    protected ThirdPartyService thirdPartyService;

    /**
     * 构建用户Context
     * @return
     */
    protected void userContext(VendorUserContext context) {
        VendorApps apiRequestApp = vendorApps();
        if (apiRequestApp == null || apiRequestApp.getAppKey() == null) {
            context.setMapMessage(failMessage("应用状态错误"));
            return;
        }
        context.setAppKey(apiRequestApp.getAppKey());
        // 获取 token
        token(context);
        if (context.getMapMessage() != null) {
            return;
        }
        // 初始化 initUser
        initUser(context);

        if (context.getMapMessage() != null) {
            return;
        }

        LandingSource landingSource = thirdPartyLoaderClient.loadLandingSource(appKey(), context.getVendorUserId());
        if (landingSource != null &&  landingSource.getUserId() != null) {
            context.setIsBand(true);
            context.setUserId(landingSource.getUserId());
            LoggerUtils.info("landing_login_"+ appKey(), landingSource.getUserId(), new Date());
        } else {
            if (StringUtils.isNotBlank(context.getMobile())) {
                UserAuthentication userAuthentication = loadMobileAuthentication(context.getMobile());
                if (userAuthentication != null) {
                    context.setIsRegister(true);
                    context.setUserId(userAuthentication.getId());
                }
            }
        }
        // 构建返回值
        result(context);
    }

    public VendorApps vendorApps() {
        return vendorLoaderClient.getExtension().loadVendorApp(getRequestString(REQ_APP_KEY));
    }

    public abstract UserAuthentication loadMobileAuthentication(String mobile);
    /**
     * 获取token
     *
     */
    public abstract void token(VendorUserContext context);

    /**
     * 初始化用户信息
     */
    public abstract void initUser(VendorUserContext context);

    /**
     * 构建返回值值
     */
    public abstract void result(VendorUserContext context);

    /**
     * 获取appKey
     *
     * @return
     */
    public abstract String appKey();
}
