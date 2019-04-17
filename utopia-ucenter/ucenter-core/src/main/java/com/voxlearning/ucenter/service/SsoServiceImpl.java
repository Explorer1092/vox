/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2018 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.ucenter.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.cipher.AesUtils;
import com.voxlearning.alps.config.manager.ConfigManager;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.ucenter.cache.UcenterWebCacheSystem;
import com.voxlearning.ucenter.support.constants.SsoConstants;
import com.voxlearning.utopia.api.service.SsoService;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.utopia.service.vendor.consumer.VendorLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@Named
@ExposeService(interfaceClass = SsoService.class)
public class SsoServiceImpl extends SpringContainerSupport implements SsoService {

    @Inject private UcenterWebCacheSystem ucenterWebCacheSystem;
    @Inject private UserLoaderClient userLoaderClient;
    @Inject private VendorLoaderClient vendorLoaderClient;

    public static String generateTicketCacheKey(String ticket) {
        return SsoConstants.CONST_SSO_TICKE_CACHE_KEY_PREFIX + ticket;
    }

    @Override
    public MapMessage validateTicket(String dataJson) {
        Map<String, Object> data = JsonUtils.fromJson(dataJson);
        String encryptedTicket = SafeConverter.toString(data.get(SsoConstants.TICKET));
        String appKey = SafeConverter.toString(data.get(SsoConstants.APP_KEY));

        if (StringUtils.isBlank(encryptedTicket) || StringUtils.isBlank(appKey)) {
            return MapMessage.errorMessage();
        }

        CacheObject<String> ticketCache = ucenterWebCacheSystem.CBS.flushable.get(generateTicketCacheKey(encryptedTicket));
        if (null == ticketCache || null == ticketCache.getValue()) {
            return MapMessage.errorMessage();
        } else {
            ucenterWebCacheSystem.CBS.flushable.delete(generateTicketCacheKey(encryptedTicket));
        }

        String sso_ticket_secret_key = ConfigManager.instance().getCommonConfig().getConfigs().get("sso_ticket_secret_key");
        if (sso_ticket_secret_key == null) {
            return MapMessage.errorMessage("No 'sso_ticket_secret_key' configured");
        }

        String ticket = AesUtils.decryptHexString(sso_ticket_secret_key, encryptedTicket);
        if (!ticket.equals(ticketCache.getValue())) {
            return MapMessage.errorMessage();
        }

        //较验一下cache里的信息是否已经失效
        String[] ts = ticket.split(":"); //appKey:uid:pwd:timestamp
        if (ts.length != 4) {
            return MapMessage.errorMessage();
        }
        String ticketAppKey = ts[0];
        Long ticketUid = Long.valueOf(ts[1]);
        String ticketPwd = ts[2];
        String ticketTimestamp = ts[3];

        if (Instant.now().minusMillis(SsoConstants.TICKET_EXPIRE_TIMESTAMP).isAfter(Instant.ofEpochMilli(Long.parseLong(ticketTimestamp)))
                || !appKey.equals(ticketAppKey)) {
            return MapMessage.errorMessage();
        }
        //app数据量比较小,只有几十条,未来也不会爆增,全部放到缓存里了 2015-12-23
        Optional<VendorApps> app = vendorLoaderClient.loadVendorAppsIncludeDisabled().values().stream()
                .filter(t -> t.isVisible(RuntimeMode.current().getLevel()) && t.getAppKey().equals(ticketAppKey))
                .findFirst();
        if (!app.isPresent()) {
            return MapMessage.errorMessage();
        }

        UserAuthentication ua = userLoaderClient.loadUserAuthentication(ticketUid);
        if (null == ua || !ticketPwd.equals(ua.getPassword())) {
            return MapMessage.errorMessage();
        }
        return MapMessage.successMessage().add("uid", ua.getId());
    }
}
