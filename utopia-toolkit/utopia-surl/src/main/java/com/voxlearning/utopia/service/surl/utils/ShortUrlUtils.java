package com.voxlearning.utopia.service.surl.utils;

import com.voxlearning.alps.core.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * @author xin.xin
 * @since 9/25/15
 */
@Slf4j
public class ShortUrlUtils {
    public static final List<Integer> SHORT_URL_CODE_LENGTH = Arrays.asList(6, 8);

    public static String[] chars = {"a", "b", "c", "d", "e", "f", "g", "h",
            "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t",
            "u", "v", "w", "x", "y", "z", "0", "1", "2", "3", "4", "5",
            "6", "7", "8", "9", "A", "B", "C", "D", "E", "F", "G", "H",
            "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
            "U", "V", "W", "X", "Y", "Z"
    };

    /**
     * 对长网址进行编码
     *
     * @param longUrl 需要转换的长网址
     * @param charset
     * @return 得到四组短地址码，任取一组使用既可作为短网址
     * @throws NoSuchAlgorithmException
     */
    public static List<String> encode(String longUrl, Charset charset) throws NoSuchAlgorithmException {
        String md5 = buildMD5(longUrl, charset);
        return buildShortCode(md5);
    }

    private static String buildMD5(String url, Charset charset) throws NoSuchAlgorithmException {
        Objects.requireNonNull(url, "url must not be null.");

        MessageDigest digest = MessageDigest.getInstance("MD5");
        byte[] bs = digest.digest(url.getBytes(charset));//生成一组length=16的byte数组

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bs.length; i++) {
            int c = bs[i] & 0xFF; //byte转int为了不丢失符号位， 所以&0xFF
            if (c < 16) { //如果c小于16，就说明，可以只用1位16进制来表示， 那么在前面补一个0
                sb.append("0");
            }
            sb.append(Integer.toHexString(c));
        }
        return sb.toString();
    }

    private static List<String> buildShortCode(String md5) {
        Objects.requireNonNull(md5, "md5 must not be null.");

        List<String> codes = new LinkedList<>();
        //将32个字符的md5码分成4段处理，每段8个字符
        for (int i = 0; i < 4; i++) {
            int offset = i * 8;
            String sub = md5.substring(offset, offset + 8);
            long sub16 = Long.parseLong(sub, 16); //将sub当作一个16进制的数，转成long
//             & 0X3FFFFFFF，去掉最前面的2位，只留下30位
//            sub16 &= 0X3FFFFFFF;
            StringBuilder sb = new StringBuilder();
            //32位分8段处理，每段4位
            for (int j = 0; j < 8; j++) {
                //得到一个 <= 61的数字
                long t = sub16 & 0x0000003D;
                sb.append(chars[(int) t]);
                sub16 >>= 4;  //将sub16右移5位
            }
            codes.add(sb.toString());
        }
        return codes;
    }

    public static String getShortUrl(HttpServletRequest request) {
        String shortUrl = request.getRequestURL().toString();
        return shortUrl.substring(shortUrl.lastIndexOf("/") + 1);
    }

    public static String getRemoteAddr(HttpServletRequest request) {
        String addr = request.getHeader("X-Forwarded-For");
        if (null == addr) {
            addr = request.getRemoteAddr();
        }
        return addr;
    }

    public static boolean isShortUrl(String code) {
        return !StringUtils.isBlank(code) && SHORT_URL_CODE_LENGTH.contains(code.length());
    }
}
