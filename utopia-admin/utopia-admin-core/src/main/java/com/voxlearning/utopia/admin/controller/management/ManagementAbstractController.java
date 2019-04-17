package com.voxlearning.utopia.admin.controller.management;

import com.voxlearning.utopia.admin.controller.AbstractAdminController;
import com.voxlearning.utopia.admin.persist.*;
import com.voxlearning.utopia.admin.service.management.ManagementService;

import javax.inject.Inject;

/**
 * @author Longlong Yu
 * @since 下午4:59,13-11-22.
 */
public abstract class ManagementAbstractController extends AbstractAdminController {
    /**
     * persistence
     */
    @Inject protected AdminAppSystemMasterPersistence adminAppSystemMasterPersistence;
    @Inject protected AdminDepartmentMasterPersistence adminDepartmentMasterPersistence;
    @Inject protected AdminDepartmentPersistence adminDepartmentPersistence;
    @Inject protected AdminGroupMasterPersistence adminGroupMasterPersistence;
    @Inject protected AdminGroupPersistence adminGroupPersistence;
    @Inject protected AdminGroupUserPersistence adminGroupUserPersistence;
    @Inject protected AdminPathPersistence adminPathPersistence;
    @Inject protected AdminPathRoleGroupPersistence adminPathRoleGroupPersistence;
    @Inject protected AdminPathRolePersistence adminPathRolePersistence;
    /**
     * service
     */
    @Inject protected ManagementService managementService;
}
