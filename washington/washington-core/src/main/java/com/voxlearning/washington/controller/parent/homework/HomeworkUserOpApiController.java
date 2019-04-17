/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.washington.controller.parent.homework;

import com.google.common.collect.Sets;
import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.core.utils.ObjectUtils;
import com.voxlearning.washington.controller.open.AbstractApiController;
import com.voxlearning.washington.controller.parent.homework.util.HomeworkUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 用户操作记录
 *
 * @author Wenlong Meng
 * @since Feb 20, 2019
 */
@Controller
@RequestMapping(value = "/parent/homework/op")
@Slf4j
public class HomeworkUserOpApiController extends AbstractApiController {

    //local variables
    UtopiaCache utopiaCache = CacheSystem.CBS.getCache("flushable");

    //Logic
    /**
     * 保存用户操作
     *
     * @return
     */
    @RequestMapping(value = "student.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage userOp(Long studentId, String bizType, String key) {
        if (ObjectUtils.anyBlank(studentId, bizType, key)) {
            return MapMessage.errorMessage("参数不能为空");
        }
        Map<String, String> params = new HashMap<>();
        Set<String> ignoreNames = Sets.newHashSet("studentId", "bizType", "key");
        getRequest().getParameterMap().forEach((k,v)->{
            if(ignoreNames.contains(k)){
                return;
            }
            params.put(k, v!=null && v.length>0 ? v[0]: null);
        });
        if(!params.isEmpty()){
            utopiaCache.set(HomeworkUtil.generatorID(studentId, bizType, key),  17*60*60, params);
        }
        return MapMessage.successMessage();
    }

}
