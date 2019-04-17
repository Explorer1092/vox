package com.voxlearning.washington.controller.open.v1;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.washington.controller.open.AbstractApiController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

import static com.voxlearning.washington.controller.open.ApiConstants.*;

/**
 * 下载信息相关
 * Created by Shuai Huan on 2015/11/6.
 */
@Controller
@RequestMapping(value = "/v1/download")
public class DownloadApiController extends AbstractApiController {

    @RequestMapping(value = "/info.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage info() {
        MapMessage resultMap = new MapMessage();

        try {
            validateRequest(REQ_PRODUCT_ID);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        String productId = getRequestString(REQ_PRODUCT_ID);
        String config = getPageBlockContentGenerator().getPageBlockContentHtml("client_app_publish", "app_download_info");
        config = config.replace("\r", "").replace("\n", "").replace("\t", "");
        Map<String, Object> configMap = JsonUtils.fromJson(config);
        if (configMap != null) {
            Map infoMap = (Map) configMap.get(productId);
            resultMap.add(RES_VERSION, infoMap.get("version"));
            resultMap.add(RES_SIZE, infoMap.get("size"));
            resultMap.add(RES_URL, infoMap.get("url"));
            resultMap.add(RES_MD5, infoMap.get("md5"));
        }
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;
    }
}
