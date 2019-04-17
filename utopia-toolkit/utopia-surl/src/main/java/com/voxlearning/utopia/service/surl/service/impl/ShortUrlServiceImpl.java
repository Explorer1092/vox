/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.surl.service.impl;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.surl.dao.ShortUrlRefPersistence;
import com.voxlearning.utopia.service.surl.entity.ShortUrlRef;
import com.voxlearning.utopia.service.surl.service.ShortUrlService;
import com.voxlearning.utopia.service.surl.utils.ShortUrlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;

/**
 * @author xin.xin
 * @since 9/28/15
 */
@Named
@Slf4j
public class ShortUrlServiceImpl implements ShortUrlService {
    private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    @Inject
    private ShortUrlRefPersistence shortUrlRefPersistence;

    @Override
    public String encodeToShortUrl(String url) throws NoSuchAlgorithmException {
        if (StringUtils.isBlank(url)) {
            return null;
        }

        ShortUrlRef shortUrlRef = shortUrlRefPersistence.getByLongUrl(url);
        if (null != shortUrlRef) {
            return shortUrlRef.getShortUrl();
        }
        String candidateUrl = url;
        boolean first = true;
        while (true) {
            if (!first) {
                candidateUrl = url + "#" + System.nanoTime();  //其实加不加#无所谓
            }
            List<String> codes = ShortUrlUtils.encode(candidateUrl, DEFAULT_CHARSET);
            for (String code : codes) {
                try {
                    ShortUrlRef ref = new ShortUrlRef();
                    ref.setLongUrl(url);
                    ref.setShortUrl(code);
                    ref.setSign(DigestUtils.md5Hex(url));
                    ref.setDisabled(false);
                    ref.setCreateDatetime(new Date());
                    ref.setUpdateDatetime(new Date());
                    shortUrlRefPersistence.insert(ref);
                    return code;
                } catch (Exception ex) {
                    //如果code已存在库里会触发唯一索引错误
                }
            }
            first = false;
        }
    }

    @Override
    public String decodeToLongUrl(String code) {
        if (!ShortUrlUtils.isShortUrl(code)) {
            return null;
        }

        ShortUrlRef ref = shortUrlRefPersistence.getByShortUrl(code);
        if (null == ref) {
            return null;
        }

        return ref.getLongUrl();
    }
}
