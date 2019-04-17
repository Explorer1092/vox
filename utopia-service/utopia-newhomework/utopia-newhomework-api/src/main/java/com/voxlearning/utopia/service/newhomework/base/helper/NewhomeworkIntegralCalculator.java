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

package com.voxlearning.utopia.service.newhomework.base.helper;

import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

/**
 * Migrate from user api integral calculator.
 * Here is the right place.
 */
abstract public class NewhomeworkIntegralCalculator {

    public static int calculateStudentNewHomeworkIntegralAmount(Integer score, ObjectiveConfigType objectiveConfigType) {
        if (ObjectiveConfigType.getSubjectives().contains(objectiveConfigType)) {
            score = 100;
        }
        //根据新规则计算加银币方法
        int silver;
        if (score < 20) {
            silver = 0;
        } else if (score >= 20 && score < 40) {
            silver = 1;
            if (ObjectiveConfigType.getQuizs().contains(objectiveConfigType) || ObjectiveConfigType.getBasics().contains(objectiveConfigType)) silver = 1;
        } else if (score >= 40 && score < 60) {
            silver = 2;
            if (ObjectiveConfigType.getQuizs().contains(objectiveConfigType) || ObjectiveConfigType.getBasics().contains(objectiveConfigType)) silver = 3;
        } else if (score >= 60 && score < 80) {
            silver = 3;
            if (ObjectiveConfigType.getQuizs().contains(objectiveConfigType) || ObjectiveConfigType.getBasics().contains(objectiveConfigType)) silver = 5;
        } else if (score >= 80 && score < 100) {
            silver = 4;
            if (ObjectiveConfigType.getQuizs().contains(objectiveConfigType) || ObjectiveConfigType.getBasics().contains(objectiveConfigType)) silver = 8;
        } else {
            silver = 5;
            if (ObjectiveConfigType.getQuizs().contains(objectiveConfigType) || ObjectiveConfigType.getBasics().contains(objectiveConfigType)) silver = 10;
        }
        return silver;
    }

    public static int calculateStudentRepairNewHomeworkIntegralAmount(Integer score, ObjectiveConfigType objectiveConfigType) {
        if (ObjectiveConfigType.getSubjectives().contains(objectiveConfigType)) {
            score = 100;
        }
        return score >= 60 ? 1 : 0;
    }
}
