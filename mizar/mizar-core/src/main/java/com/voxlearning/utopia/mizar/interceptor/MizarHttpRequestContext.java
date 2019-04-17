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

package com.voxlearning.utopia.mizar.interceptor;

import com.voxlearning.alps.webmvc.support.context.UtopiaHttpRequestContext;
import com.voxlearning.utopia.mizar.auth.HbsAuthUser;
import com.voxlearning.utopia.mizar.auth.MizarAuthUser;
import lombok.Getter;
import lombok.Setter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Mizar context
 * Created by Alex on 2016/7/3.
 */
public class MizarHttpRequestContext extends UtopiaHttpRequestContext {


    @Setter
    @Getter
    private MizarAuthUser mizarAuthUser;

    @Getter
    @Setter
    private HbsAuthUser hbsAuthUser;

    @Setter
    @Getter
    private String relativeUriPath;
    @Setter
    @Getter
    private String webAppContextPath;


    public MizarHttpRequestContext(HttpServletRequest request, HttpServletResponse response) {
        super(request, response);
    }

    @Override
    public Object getRootModelAttribute(String key) {
        switch (key) {
            case "currentUser":
                return getMizarAuthUser();
        }
        return null;
    }

    public boolean isLoggedIn() {
        return mizarAuthUser != null;
    }

}
