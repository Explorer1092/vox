package com.voxlearning.utopia.service.mizar.consumer.service;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.mizar.api.service.MizarSystemConfigService;
import lombok.Getter;

import java.util.List;

/**
 * Created by alex on 2016/9/18.
 */
public class MizarSystemConfigServiceClient {
    @Getter
    @ImportService(interfaceClass = MizarSystemConfigService.class)
    private MizarSystemConfigService remoteReference;

    public MapMessage addSysPath(String functionName, String pathName, String desc, List<String> roleGroups) {
        if (StringUtils.isBlank(functionName) || StringUtils.isBlank(pathName) || CollectionUtils.isEmpty(roleGroups)) {
            return MapMessage.errorMessage("Illegal arguments, {}, {}, {}", functionName, pathName, roleGroups);
        }
        return remoteReference.addSysPath(functionName, pathName, desc, roleGroups);
    }

    public MapMessage updateSysPath(String sysPathId, String functionName, String pathName, String desc, List<String> roleGroups) {
        if (StringUtils.isBlank(sysPathId) || StringUtils.isBlank(functionName) || StringUtils.isBlank(pathName) || CollectionUtils.isEmpty(roleGroups)) {
            return MapMessage.errorMessage("Illegal arguments, {}, {}, {}, {}", sysPathId, functionName, pathName, roleGroups);
        }
        return remoteReference.updateSysPath(sysPathId, functionName, pathName, desc, roleGroups);
    }

    public MapMessage deleteSysPath(String sysPathId) {
        if (StringUtils.isBlank(sysPathId)) {
            return MapMessage.errorMessage("Illegal arguments, {}", sysPathId);
        }
        return remoteReference.deleteSysPath(sysPathId);
    }

}
