package com.voxlearning.utopia.service.ai.impl.support;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.service.user.api.entities.User;

public class UserInfoSupport {
    public static String DEFAULT_USER_IMAGE_NAME = "https://cdn-portrait.17zuoye.cn/upload/images/avatar/avatar_normal.png";

    private static String TEST_HOST = "https://cdn-portrait.test.17zuoye.net/gridfs/";

    private static String ONLINE_HOST = "https://cdn-portrait.17zuoye.cn/gridfs/";

    public static String getUserRoleImage(User user) {
       return getUserRoleImage(user, DEFAULT_USER_IMAGE_NAME);
    }

    public static String getUserRoleImage(User user, String defaultImage) {
        if (user == null || StringUtils.isBlank(user.fetchImageUrl())) {
            return defaultImage;
        }

        if (user.fetchImageUrl().contains("http")) {
            return user.fetchImageUrl();
        }

        return RuntimeMode.le(Mode.TEST) ? TEST_HOST + user.fetchImageUrl() : ONLINE_HOST + user.fetchImageUrl();
    }
}
