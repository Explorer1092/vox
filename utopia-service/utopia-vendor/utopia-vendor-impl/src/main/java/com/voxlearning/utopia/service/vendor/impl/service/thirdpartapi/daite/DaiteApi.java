package com.voxlearning.utopia.service.vendor.impl.service.thirdpartapi.daite;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.DigestSignUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.utopia.service.vendor.impl.service.thirdpartapi.ThirdPart;
import com.voxlearning.utopia.service.vendor.impl.service.VendorAppsServiceImpl;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by zhouwei on 2018/8/2
 **/
public class DaiteApi implements InitializingBean {

    //校验token是否在daite方登录
    public static final String checkLoginUrl = "/api/login/checkUserlogin";

    //写入事件至daite
    public static final String zyDateUrl = "/api/sync/zydata";

    @Inject
    private VendorAppsServiceImpl vendorAppsServiceImpl;

    private String secretKey;

    /**
     * bean初始化的时候，设置好key
     * @throws Exception
     * @author zhouwei
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        List<VendorApps> apps = vendorAppsServiceImpl.loadAllVendorAppsFromDB().get();
        for (VendorApps app : apps) {
            if (Objects.equals(app.getAppKey(), ThirdPart.DAITE.getAppKey())) {
                this.secretKey = app.getSecretKey();
                break;
            }
        }
    }

    /**
     * 根据环境，获取dt的domain
     * @return
     * @author zhouwei
     */
    public String getDomain() {
        if (RuntimeMode.isProduction() || RuntimeMode.isStaging()) {
            return ThirdPart.DAITE.getOnlineDomain();
        } else {
            return ThirdPart.DAITE.getTestDomain();
        }
    }

    /**
     * 获取秘钥
     * @return
     * @author zhouwei
     */
    public String getSecretKey() {
        return this.secretKey;
    }

    /**
     * 计算对方所需要的sig信息
     * @param appKey
     * @param paramsMap
     * @return
     * @author zhouwei
     */
    public String generateRequestSig(String appKey, Map<String,Object> paramsMap) {
        Map<String, String> sigParams = paramsMap.entrySet().stream().collect(Collectors.toMap((p -> p.getKey()), (p -> SafeConverter.toString(p.getValue()))));
        return DigestSignUtils.signMd5(sigParams, this.secretKey);
    }

}
