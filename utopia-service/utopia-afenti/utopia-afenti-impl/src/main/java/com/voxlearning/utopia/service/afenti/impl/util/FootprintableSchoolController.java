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

package com.voxlearning.utopia.service.afenti.impl.util;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.School;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Objects;

import static com.voxlearning.alps.annotation.meta.AuthenticationState.SUCCESS;

@Named
public class FootprintableSchoolController extends SpringContainerSupport {

    @Inject private SchoolLoaderClient schoolLoaderClient;

    private UtopiaCache cache;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        cache = CacheSystem.EHC.getCache("afenti");
        Objects.requireNonNull(cache);
    }

    /**
     * 这里不做并发处理了，有点并发请求也无所谓。缓存1小时
     * 如果学校不是小学的，或者没有认证，就不要足迹了，防九防十防两千
     */
    public boolean isFootprintable(Long schoolId) {
        if (schoolId == null) {
            return false;
        }
        String id = "FootprintableSchool:" + schoolId;
        Boolean ret = cache.load(id);
        if (ret != null) {
            return ret;
        }
        School school = schoolLoaderClient.getSchoolLoader()
                .loadSchool(schoolId)
                .getUninterruptibly();
        ret = (school != null && !school.isJuniorSchool() && school.getSchoolAuthenticationState() == SUCCESS);
        cache.add(id, 3600, ret);
        return ret;

    }
}
