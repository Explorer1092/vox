package com.voxlearning.utopia.core.helper.classify.images;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;

/**
 * \* Created: liuhuichao
 * \* Date: 2018/10/25
 * \* Time: 12:05 PM
 * \* Description:鉴黄请求body
 * \
 */
@Getter
@Setter
public class JinShanClassifyImagesPostBody {

    private List<String> business= Arrays.asList("porn"); //业务类型  ---> porn 色情图片识别

    @JSONField(name="image_urls")
    private List<String> imageUrls;
}
