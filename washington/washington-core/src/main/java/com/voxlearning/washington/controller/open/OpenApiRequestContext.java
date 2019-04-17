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

package com.voxlearning.washington.controller.open;

import com.voxlearning.alps.webmvc.support.context.UtopiaHttpRequestContext;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.utopia.service.vendor.api.entity.VendorAppsUserRef;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Open Platform API Request Context.
 *
 * @author Zhilong Hu
 * @since 2014-06-6
 */
@Slf4j
public class OpenApiRequestContext extends UtopiaHttpRequestContext {

    @Setter @Getter private VendorApps requestVendorApp;
    @Setter @Getter private User requestUser;
    @Setter @Getter private VendorAppsUserRef vendorAppsUserRef;

    public OpenApiRequestContext(HttpServletRequest request, HttpServletResponse response) {
        super(request, response);
    }
}
