package com.voxlearning.utopia.admin.controller.site;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.user.cache.UserCache;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Xiaochao.Wei
 * @since 2018/1/4
 */

@Controller
@RequestMapping("/site/cache")
public class SiteClearCacheController extends SiteAbstractController {

    @RequestMapping(value = "batchclearcache.vpage", method = RequestMethod.GET)
    String batchClearCache() {
        return "site/batch/batchclearcache";
    }

    @RequestMapping(value = "clearcache.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage clearCache() {

        String keyList = getRequestString("keyList");

        if (StringUtils.isBlank(keyList)) {
            return MapMessage.errorMessage("请输入正确的缓存key");
        }

        String[] keys = keyList.split("\\n");

        StringBuilder failedList = new StringBuilder();

        for (String key : keys) {
            Boolean flag = UserCache.getUserCache().delete(key);
            if (!flag) {
                failedList.append(key).append("\n");
            }
        }

        if (failedList.length() > 0) {
            return MapMessage.errorMessage("以下缓存删除失败:").add("failedList", failedList);
        }

        return MapMessage.successMessage();
    }
}
