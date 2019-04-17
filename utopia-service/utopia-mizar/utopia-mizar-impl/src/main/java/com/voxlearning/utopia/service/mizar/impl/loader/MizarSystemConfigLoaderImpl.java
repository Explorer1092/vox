package com.voxlearning.utopia.service.mizar.impl.loader;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.mizar.api.entity.sys.MizarSysPath;
import com.voxlearning.utopia.service.mizar.api.entity.sys.MizarSysPathRole;
import com.voxlearning.utopia.service.mizar.api.loader.MizarSystemConfigLoader;
import com.voxlearning.utopia.service.mizar.impl.dao.sys.MizarSysPathDao;
import com.voxlearning.utopia.service.mizar.impl.dao.sys.MizarSysPathRoleDao;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by alex on 2016/9/18.
 */
@Named
@Service(interfaceClass = MizarSystemConfigLoader.class)
@ExposeService(interfaceClass = MizarSystemConfigLoader.class)
public class MizarSystemConfigLoaderImpl extends SpringContainerSupport implements MizarSystemConfigLoader {

    @Inject private MizarSysPathDao mizarSysPathDao;
    @Inject private MizarSysPathRoleDao mizarSysPathRoleDao;

    @Override
    public List<MizarSysPath> loadAllSysPath() {
        List<MizarSysPath> allPath = mizarSysPathDao.findAll();
        if (CollectionUtils.isEmpty(allPath)) {
            return Collections.emptyList();
        }

        List<MizarSysPathRole> allPathRole = mizarSysPathRoleDao.findAll();
        Map<String, List<MizarSysPathRole>> pathRoles = allPathRole.stream().collect(Collectors.groupingBy(p -> p.getPath(), Collectors.toList()));

        for (MizarSysPath sysPath : allPath) {
            if (pathRoles.containsKey(sysPath.getId())) {
                sysPath.setAuthRoleList(pathRoles.get(sysPath.getId()));
            }
        }

        return allPath;
    }

    @Override
    public MizarSysPath loadSysPath(String sysPathId) {
        MizarSysPath sysPath = mizarSysPathDao.load(sysPathId);
        if (sysPath != null) {
            sysPath.setAuthRoleList(mizarSysPathRoleDao.findByPath(sysPathId));
        }

        return sysPath;
    }

    @Override
    public List<String> loadRolePath(Collection<String> roleGroups) {
        if (CollectionUtils.isEmpty(roleGroups)) {
            return Collections.emptyList();
        }

        List<MizarSysPathRole> sysPathRoles = mizarSysPathRoleDao.findAll();
        sysPathRoles = sysPathRoles.stream().filter(p -> roleGroups.contains(p.getRoleGroupId())).collect(Collectors.toList());
        Set<String> pathIds = sysPathRoles.stream().map(MizarSysPathRole::getPath).collect(Collectors.toSet());
        List<MizarSysPath> pathList = mizarSysPathDao.findAll();
        pathList = pathList.stream().filter(p -> pathIds.contains(p.getId())).collect(Collectors.toList());

        return pathList.stream().map(p -> StringUtils.join("/", p.getAppName(), "/", p.getPathName())).collect(Collectors.toList());
    }

}
