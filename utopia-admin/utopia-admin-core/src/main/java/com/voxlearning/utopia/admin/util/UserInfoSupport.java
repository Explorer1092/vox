package com.voxlearning.utopia.admin.util;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.service.user.api.entities.User;

public class UserInfoSupport {
    private static String DEFAULT_USER_IMAGE_NAME = "https://cdn-portrait.17zuoye.cn/upload/images/avatar/avatar_normal.png";

    private static String TEST_HOST = "https://cdn-portrait.test.17zuoye.net/gridfs/";

    private static String ONLINE_HOST = "https://cdn-portrait.17zuoye.cn/gridfs/";

    public static String getUserRoleImage(User user) {
        if (user == null || StringUtils.isBlank(user.fetchImageUrl())) {
            return DEFAULT_USER_IMAGE_NAME;
        }

        if (user.fetchImageUrl().contains("http")) {
            return user.fetchImageUrl();
        }

        return RuntimeMode.le(Mode.TEST) ? TEST_HOST + user.fetchImageUrl() : ONLINE_HOST + user.fetchImageUrl();
    }

}
