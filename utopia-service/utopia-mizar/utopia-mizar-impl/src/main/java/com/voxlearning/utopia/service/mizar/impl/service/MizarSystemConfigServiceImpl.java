package com.voxlearning.utopia.service.mizar.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.mizar.api.entity.sys.MizarSysPath;
import com.voxlearning.utopia.service.mizar.api.entity.sys.MizarSysPathRole;
import com.voxlearning.utopia.service.mizar.api.service.MizarSystemConfigService;
import com.voxlearning.utopia.service.mizar.impl.dao.sys.MizarSysPathDao;
import com.voxlearning.utopia.service.mizar.impl.dao.sys.MizarSysPathRoleDao;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

/**
 * Created by alex on 2016/9/18.
 */
@Named
@Service(interfaceClass = MizarSystemConfigService.class)
@ExposeService(interfaceClass = MizarSystemConfigService.class)
public class MizarSystemConfigServiceImpl extends SpringContainerSupport implements MizarSystemConfigService {

    @Inject private MizarSysPathDao mizarSysPathDao;
    @Inject private MizarSysPathRoleDao mizarSysPathRoleDao;

    @Override
    public MapMessage addSysPath(String functionName, String pathName, String desc, List<String> roleGroups) {
        if (StringUtils.isBlank(functionName) || StringUtils.isBlank(pathName) || CollectionUtils.isEmpty(roleGroups)) {
            return MapMessage.errorMessage("Illegal arguments, {}, {}, {}", functionName, pathName, roleGroups);
        }

        MizarSysPath sysPath = new MizarSysPath();
        sysPath.setAppName(functionName);
        sysPath.setPathName(pathName);
        sysPath.setDescription(desc);
        mizarSysPathDao.insert(sysPath);
        String sysPathId = sysPath.getId();

        addSysPathRoles(sysPathId, roleGroups);

        return MapMessage.successMessage();
    }

    @Override
    public MapMessage updateSysPath(String sysPathId, String functionName, String pathName, String desc, List<String> roleGroups) {
        if (StringUtils.isBlank(sysPathId) || StringUtils.isBlank(functionName) || StringUtils.isBlank(pathName) || CollectionUtils.isEmpty(roleGroups)) {
            return MapMessage.errorMessage("Illegal arguments, {}, {}, {}, {}", sysPathId, functionName, pathName, roleGroups);
        }

        MizarSysPath sysPath = mizarSysPathDao.load(sysPathId);
        if (sysPath == null) {
            return MapMessage.errorMessage("Unknown sys path id: {}", sysPathId);
        }

        sysPath.setAppName(functionName);
        sysPath.setPathName(pathName);
        sysPath.setDescription(desc);
        mizarSysPathDao.upsert(sysPath);

        List<MizarSysPathRole> sysPathRoles = mizarSysPathRoleDao.findByPath(sysPathId);
        if (CollectionUtils.isNotEmpty(sysPathRoles)) {
            for (MizarSysPathRole sysPathRole : sysPathRoles) {
                mizarSysPathRoleDao.delete(sysPathRole.getId());
            }
        }

        addSysPathRoles(sysPathId, roleGroups);

        return MapMessage.successMessage();
    }

    private void addSysPathRoles(String sysPathId, List<String> roleGroups) {
        for (String roleGroup : roleGroups) {
            MizarSysPathRole sysPathRole = new MizarSysPathRole();
            sysPathRole.setPath(sysPathId);
            sysPathRole.setRoleGroupId(roleGroup);
            mizarSysPathRoleDao.insert(sysPathRole);
        }
    }

    private void removeSysPathRoles(String sysPathId) {
        List<MizarSysPathRole> sysPathRoles = mizarSysPathRoleDao.findByPath(sysPathId);
        if (CollectionUtils.isNotEmpty(sysPathRoles)) {
            for (MizarSysPathRole sysPathRole : sysPathRoles) {
                mizarSysPathRoleDao.delete(sysPathRole.getId());
            }
        }
    }

    @Override
    public MapMessage deleteSysPath(String sysPathId) {
        if (StringUtils.isBlank(sysPathId)) {
            return MapMessage.errorMessage("Illegal arguments, {}", sysPathId);
        }

        removeSysPathRoles(sysPathId);
        mizarSysPathDao.delete(sysPathId);

        return MapMessage.successMessage();
    }
}
