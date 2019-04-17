package com.voxlearning.utopia.service.newhomework.api.mapper;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.utopia.service.user.api.mappers.ClazzGroup;
import lombok.Getter;
import lombok.Setter;

/**
 * @author guoqiang.li
 * @version 0.1
 * @since 2016/1/8
 */
@Getter
@Setter
public class ExtensionClazzGroup extends ClazzGroup {
    private static final long serialVersionUID = -1448239914225410016L;

    private Subject subject;

    @Override
    public String toString() {
        // Don't change this method
        long cid = getClazzId() == null ? 0 : getClazzId();
        long gid = getGroupId() == null ? 0 : getGroupId();
        String type = subject == null ? Subject.UNKNOWN.name() : subject.name();
        return "CID=" + cid + ",GID=" + gid + ",subject=" + type;
    }
}