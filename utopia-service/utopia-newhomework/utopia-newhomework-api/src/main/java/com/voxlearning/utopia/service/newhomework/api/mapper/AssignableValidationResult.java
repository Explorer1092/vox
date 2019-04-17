package com.voxlearning.utopia.service.newhomework.api.mapper;

import com.voxlearning.utopia.service.user.api.mappers.ClazzGroup;
import lombok.Getter;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author guoqiang.li
 * @version 0.1
 * @since 2016/1/8
 */
public class AssignableValidationResult implements Serializable {
    private static final long serialVersionUID = -8221963558804326705L;

    @Getter private final Set<ClazzGroup> assignables = new LinkedHashSet<>();
    @Getter private final Set<ClazzGroup> nonAssignables = new LinkedHashSet<>();
}
