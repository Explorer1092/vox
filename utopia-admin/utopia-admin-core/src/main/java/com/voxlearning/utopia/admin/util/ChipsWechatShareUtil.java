package com.voxlearning.utopia.admin.util;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.runtime.RuntimeMode;

public class ChipsWechatShareUtil {
    public static String shareVideoUrl(String userVideoId) {
        return getWechatDomain() + "/chips/center/chipsshare.vpage?id=" + userVideoId;
    }

    public static String getWechatDomain() {
        return RuntimeMode.lt(Mode.STAGING) ? "https://wechat.test.17zuoye.net" : "https://wechat.17zuoye.com";

    }
}
