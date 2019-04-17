package com.voxlearning.utopia.enanalyze.assemble;

import com.alibaba.fastjson.JSON;
import com.voxlearning.alps.core.util.Base64Utils;
import com.voxlearning.alps.lang.cipher.CommonCipherUtils;
import com.voxlearning.utopia.enanalyze.assemble.model.WxUserInfo;
import com.voxlearning.utopia.enanalyze.exception.support.ThirdPartyServiceException;
import lombok.extern.slf4j.Slf4j;

/**
 * 微信相关工具
 *
 * @author xiaolei.li
 * @version 2018/7/21
 */
public class WxUtil {

    /**
     * 解密
     *
     * @param encryptedData 加密数据
     * @param iv            偏移向量
     * @param sessionKey    秘钥
     * @return 明文
     */
    public static String decrypt(String encryptedData, String iv, String sessionKey) {
        try {
            CommonCipherUtils commonCipherUtils = new CommonCipherUtils("AES/CBC/PKCS5Padding", "AES");
            byte[] ivBytes = Base64Utils.decodeBase64(iv);
            byte[] keyBytes = Base64Utils.decodeBase64(sessionKey);
            byte[] encodeBytes = Base64Utils.decodeBase64(encryptedData);
            byte[] decodeBytes = commonCipherUtils.decrypt(keyBytes, encodeBytes, ivBytes);
            if (decodeBytes != null && decodeBytes.length > 0) {
                return new String(decodeBytes);
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new ThirdPartyServiceException("解密微信用户信息时发生异常", e);
        }
    }
}
