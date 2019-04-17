/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.ucenter.support.gray;

import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.client.GrayFunctionManagerClient;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class StudentWebGrayFunction implements WebGrayFunction {

    private final GrayFunctionManagerClient grayFunctionManagerClient;
    private final StudentDetail studentDetail;

    @Override
    public boolean isAvailable(String mainFunctionName, String subFunctionName) {
        return grayFunctionManagerClient.getStudentGrayFunctionManager()
                .isWebGrayFunctionAvailable(studentDetail, mainFunctionName, subFunctionName, false);
    }

    @Override
    public boolean isAvailable(String mainFunctionName, String subFunctionName, boolean withSchoolLevel) {
        return grayFunctionManagerClient.getStudentGrayFunctionManager()
                .isWebGrayFunctionAvailable(studentDetail, mainFunctionName, subFunctionName, withSchoolLevel);
    }

    @Override
    public boolean isAvailableWithSchoolLevel(String mainFunctionName, String subFunctionName) {
        return grayFunctionManagerClient.getStudentGrayFunctionManager()
                .isWebGrayFunctionAvailable(studentDetail, mainFunctionName, subFunctionName, true);
    }
}
