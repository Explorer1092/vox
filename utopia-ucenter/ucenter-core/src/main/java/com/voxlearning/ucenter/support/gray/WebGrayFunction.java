/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
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

public interface WebGrayFunction {

    /**
     * Check the web gray function is available for current user or not, the condition without school level
     * @param mainFunctionName the main function name
     * @param subFunctionName the sub function name
     * @return true if the web gray function is available for current user
     */
    boolean isAvailable(String mainFunctionName, String subFunctionName);

    boolean isAvailable(String mainFunctionName, String subFunctionName, boolean withSchoolLevel);

    /**
     * Check the web gray function is available for current user or not, the condition contains school level
     * @param mainFunctionName the main function name
     * @param subFunctionName the sub function name
     * @return true if the web gray function is available for current user
     */
    boolean isAvailableWithSchoolLevel(String mainFunctionName, String subFunctionName);

}