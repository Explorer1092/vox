/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.wechat.support;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.webmvc.cookie.CookieManager;
import com.voxlearning.alps.webmvc.support.DefaultContext;
import com.voxlearning.utopia.service.config.api.entity.PageBlockContent;
import com.voxlearning.utopia.service.config.client.PageBlockContentServiceClient;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class PageBlockContentGenerator {

    private final PageBlockContentServiceClient pageBlockContentServiceClient;
    private final Date now;

    public PageBlockContentGenerator(PageBlockContentServiceClient pageBlockContentServiceClient) {
        this.pageBlockContentServiceClient = Objects.requireNonNull(pageBlockContentServiceClient);
        this.now = new Date();
    }

    public String getPageBlockContentHtml(String pageName, String blockName) {
        List<PageBlockContent> contents = pageBlockContentServiceClient.getPageBlockContentBuffer()
                .findByPageName(pageName)
                .stream()
                .filter(e -> e.getDisabled() == null || !e.getDisabled())
                .collect(Collectors.toList());
        contents = CollectionUtils.toLinkedList(contents);

        StringBuilder sbuf = new StringBuilder();
        // 注意！ 这里直接沿用了 CdnBaseTag.java 的 cookie 规则。如果要调整，两边要一起改
        CookieManager cookieManager = DefaultContext.get().getCookieManager();
        boolean isSkipCdn = cookieManager.getCookie("cdntype", "").equals("skip");

        for (PageBlockContent pbc : contents) {
            if (pbc.getStartDatetime().before(now) && pbc.getEndDatetime().after(now)
                    && !pbc.getDisabled() && blockName.equals(pbc.getBlockName())
                    ) {
                String content = pbc.getContent();
                if (isSkipCdn) {
                    content = content.replace("//cdn.17zuoye.com/", "//www.17zuoye.com/");
                    content = content.replace("//cdn.17zuoye.com/", "//www.17zuoye.com/");
                }
                sbuf.append(content);
                sbuf.append("\r\n");
            }
        }
        return sbuf.toString();
    }
}
