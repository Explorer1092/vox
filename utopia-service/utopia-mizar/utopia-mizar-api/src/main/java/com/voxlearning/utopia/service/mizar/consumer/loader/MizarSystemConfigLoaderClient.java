package com.voxlearning.utopia.service.mizar.consumer.loader;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.mizar.api.entity.sys.MizarSysPath;
import com.voxlearning.utopia.service.mizar.api.loader.MizarSystemConfigLoader;
import lombok.Getter;

import java.util.*;

/**
 * Created by alex on 2016/9/18.
 */
public class MizarSystemConfigLoaderClient {

    @Getter
    @ImportService(interfaceClass = MizarSystemConfigLoader.class)
    private MizarSystemConfigLoader remoteReference;

    public List<MizarSysPath> loadAllSysPath() {
        return remoteReference.loadAllSysPath();
    }

    public List<MizarSysPath> loadSysPathByName(String functionName) {
        List<MizarSysPath> allSysPath = loadAllSysPath();
        if (StringUtils.isBlank(functionName)) {
            return allSysPath;
        }

        List<MizarSysPath> retSysPath = new ArrayList<>();
        for (MizarSysPath sysPath : allSysPath) {
            if (sysPath.getAppName().contains(functionName)) {
                retSysPath.add(sysPath);
            }
        }

        return retSysPath;
    }

    public MizarSysPath loadSysPath(String sysPathId) {
        if (StringUtils.isBlank(sysPathId)) {
            return null;
        }

        return remoteReference.loadSysPath(sysPathId);
    }

    public MizarSysPath loadSysPath(String functionName, String pathName) {
        List<MizarSysPath> allSysPath = loadAllSysPath();
        for (MizarSysPath sysPath : allSysPath) {
            if (Objects.equals(functionName, sysPath.getAppName()) && Objects.equals(pathName, sysPath.getPathName())) {
                return sysPath;
            }
        }

        return null;
    }

    public boolean sysPathExist(String functionName, String pathName) {
        List<MizarSysPath> allSysPath = loadAllSysPath();
        for (MizarSysPath sysPath : allSysPath) {
            if (Objects.equals(functionName, sysPath.getAppName()) && Objects.equals(pathName, sysPath.getPathName())) {
                return true;
            }
        }

        return false;
    }

    public List<String> loadRolePaths(Collection<String> roleGroups) {
        if (CollectionUtils.isEmpty(roleGroups)) {
            return Collections.emptyList();
        }
        return remoteReference.loadRolePath(roleGroups);
    }

}
