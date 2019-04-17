/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.business.impl.service.teacher.internal.certification;

import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import lombok.*;

import java.io.Serializable;
import java.util.List;

/**
 * @author RuiBao
 * @version 0.1
 * @since 4/11/2015
 */
@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor(staticName = "newInstance")
public class TeacherCertificationContext implements Serializable {
    private static final long serialVersionUID = -7412031091239593301L;

    // in
    @NonNull private Teacher user;

    // middle
    private boolean rewardSkipped = false; // 是否跳过奖励相关

    private List<Long> subTeacherIds;   // 副账号集合
}
