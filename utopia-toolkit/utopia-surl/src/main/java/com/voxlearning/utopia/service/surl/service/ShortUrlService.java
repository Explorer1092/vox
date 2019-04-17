package com.voxlearning.utopia.service.surl.service;

import java.security.NoSuchAlgorithmException;

/**
 * @author xin.xin
 * @since 9/28/15
 */
public interface ShortUrlService {

    /**
     * 将长地址转化为短地址
     *
     * @param url
     * @return
     */
    public String encodeToShortUrl(String url) throws NoSuchAlgorithmException;

    /**
     * 将短地址转化为长地址
     *
     * @param code
     * @return
     */
    public String decodeToLongUrl(String code);
}
