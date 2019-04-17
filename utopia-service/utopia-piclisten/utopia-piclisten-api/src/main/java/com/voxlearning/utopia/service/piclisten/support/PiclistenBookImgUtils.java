package com.voxlearning.utopia.service.piclisten.support;

import com.voxlearning.alps.core.util.StringUtils;

/**
 * 为了压缩点读机教材封面图片
 * Created by Summer on 2018/9/13
 */
public class PiclistenBookImgUtils {
    private final static String cdnUrl = "https://cdn-mirror.17zuoye.cn/";
    private final static String compressParam = "?x-oss-process=image/quality,q_75/resize,w_240";

    public static String getCompressBookImg(String imgUrl) {
        if (StringUtils.isBlank(imgUrl)) {
            return "";
        }
        return cdnUrl + imgUrl + compressParam;
    }
}
