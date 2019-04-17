package com.voxlearning.utopia.service.afenti.impl.service.activity;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;

import java.util.Map;

/**
 * @author Ruib
 * @since 2016/8/15
 */
public interface AfentiActivityDataAssembler {
    Map<String, Object> assemble(StudentDetail student, Subject subject);
}
