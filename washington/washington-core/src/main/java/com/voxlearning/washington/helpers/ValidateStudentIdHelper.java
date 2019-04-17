package com.voxlearning.washington.helpers;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.washington.cache.WashingtonCacheSystem;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Objects;

/**
 * 在绑定家长-孩子关系之前，客户端使用的后端上一个接口返回的sid可能被篡改。这里临时缓存一下。提供校验
 *
 * @author shiwei.liao
 * @since 2018-4-18
 */
@Named
public class ValidateStudentIdHelper {

    @Inject
    private WashingtonCacheSystem washingtonCacheSystem;

    //绑定身份之前验证StudentID是否是前置接口返回给客户端的值。防止被篡改
    public MapMessage validateBindRequestStudentIdWithParentId(Long parentId, Long studentId) {
        if (parentId == null || studentId == null || studentId == 0) {
            return MapMessage.errorMessage("学生ID或家长ID错误");
        }
        String cacheKey = cacheKey(parentId);
        Long storedStudentId = SafeConverter.toLong(washingtonCacheSystem.CBS.persistence.load(cacheKey));
        if (Objects.equals(storedStudentId, studentId)) {
            return MapMessage.successMessage();
        } else {
            LogCollector.info("backend-general", MapUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "op", "validateStudentError",
                    "parentId", parentId,
                    "studentId", studentId
            ));
            return MapMessage.errorMessage("学生ID错误");
        }
    }

    public MapMessage storeBindStudentIdWithParentId(Long parentId, Long studentId) {
        if (parentId == null || studentId == null || studentId == 0) {
            return MapMessage.errorMessage("学生ID或家长ID错误");
        }
        String cacheKey = cacheKey(parentId);
        Boolean add = washingtonCacheSystem.CBS.persistence.set(cacheKey, 3600 * 24, studentId);
        return add != null && add ? MapMessage.successMessage() : MapMessage.errorMessage("绑定失败，请重试");
    }

    //用uuid来存储
    public MapMessage validateBindRequestStudentIdWithUUID(String uuid, Long studentId) {
        if (StringUtils.isBlank(uuid) || studentId == null || studentId == 0) {
            return MapMessage.errorMessage("学生ID或家长ID错误");
        }
        String cacheKey = cacheKey(uuid);
        Long storedStudentId = SafeConverter.toLong(washingtonCacheSystem.CBS.persistence.load(cacheKey));
        if (Objects.equals(storedStudentId, studentId)) {
            return MapMessage.successMessage();
        } else {
            LogCollector.info("backend-general", MapUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "op", "validateStudentError",
                    "uuid", uuid,
                    "studentId", studentId
            ));
            return MapMessage.errorMessage("学生ID错误");
        }
    }

    public MapMessage storeBindStudentIdWithUUID(String uuid, Long studentId) {
        if (StringUtils.isBlank(uuid) || studentId == null || studentId == 0) {
            return MapMessage.errorMessage("学生ID或家长ID错误");
        }
        String cacheKey = cacheKey(uuid);
        Boolean add = washingtonCacheSystem.CBS.persistence.set(cacheKey, 3600 * 24, studentId);
        return add != null && add ? MapMessage.successMessage() : MapMessage.errorMessage("绑定失败，请重试");
    }

    private String cacheKey(Object o) {
        Objects.requireNonNull(o);
        return "GALAXY_PARENT_BIND_STUDENT_" + SafeConverter.toString(o);
    }
}
