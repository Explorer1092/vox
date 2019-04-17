package com.voxlearning.utopia.mizar.service.basic;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.utopia.mizar.auth.MizarAuthUser;
import com.voxlearning.utopia.mizar.entity.MizarQueryContext;
import com.voxlearning.utopia.service.mizar.api.constants.MizarUserRoleType;

import java.util.Collections;
import java.util.List;

/**
 * 管理Mizar平台基础数据的几个功能
 * Created by Yuechen.Wang on 2016/12/1.
 */
public interface MizarManager<E> {

   Page EMPTY_PAGE = new PageImpl<>(Collections.emptyList());

    @SuppressWarnings("unchecked")
    default Page<E> emptyPage() {
        return (Page<E>) EMPTY_PAGE;
    }
    /**
     * 查询分页数据
     */
    Page<E> page(MizarAuthUser user, MizarQueryContext context);

    /**
     * 新增实体之前校验参数以及权限
     */
    default MapMessage beforeCreate(E entity, MizarAuthUser user) {
        if (entity == null || user == null) {
            return MapMessage.errorMessage("参数不能为空");
        }
        List<Integer> userRoles = user.getRoleList();
        if (CollectionUtils.isEmpty(userRoles)) {
            return MapMessage.errorMessage("很遗憾，您没有该操作的权限");
        }
        boolean match = getCreateAuthRoles().stream().anyMatch(userRoles::contains);
        if (!match) {
            return MapMessage.errorMessage("很遗憾，您没有该操作的权限");
        }
        return MapMessage.successMessage();
    }

    /**
     * 允许做新增操作的角色， 默认为运营
     */
    default List<Integer> getCreateAuthRoles() {
        return Collections.singletonList(MizarUserRoleType.Operator.getId());
    }

    /**
     * 新增实体
     */
    MapMessage create(E entity, MizarAuthUser user);

    /**
     * 编辑实体之前校验参数以及权限
     */
    default MapMessage beforeModify(E entity, MizarAuthUser user) {
        if (entity == null || user == null) {
            return MapMessage.errorMessage("参数不能为空");
        }
        List<Integer> userRoles = user.getRoleList();
        if (CollectionUtils.isEmpty(userRoles)) {
            return MapMessage.errorMessage("很遗憾，您没有该操作的权限");
        }
        boolean match = getModifyAuthRoles().stream().anyMatch(userRoles::contains);
        if (!match) {
            return MapMessage.errorMessage("很遗憾，您没有该操作的权限");
        }
        return MapMessage.successMessage();
    }

    /**
     * 允许做编辑操作的角色， 默认为运营
     */
    default List<Integer> getModifyAuthRoles() {
        return Collections.singletonList(MizarUserRoleType.Operator.getId());
    }

    /**
     * 编辑实体
     */
    MapMessage modify(E entity, MizarAuthUser user);

    /**
     * 校验输入
     */
    default MapMessage validate(E entity) {
        return MapMessage.successMessage();
    }

}
