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

package com.voxlearning.utopia.service.business.impl.service.student.internal;

import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkPackage;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.client.GrayFunctionManagerClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Ruib
 * @version 0.1
 * @since 2015/12/28
 */
@Named
public class LoadStudentVacationHomeworkCard extends AbstractStudentIndexDataLoader {
    @Inject private GrayFunctionManagerClient grayFunctionManagerClient;

    @Override
    protected StudentIndexDataContext doProcess(StudentIndexDataContext context) {
        Date currentDate = new Date();
        if (currentDate.after(NewHomeworkConstants.VH_END_DATE_LATEST)) return context;

        boolean evh_exist = false;
        boolean mvh_exist = false;
        boolean cvh_exist = false;
        String evh_id = "";
        String mvh_id = "";
        String cvh_id = "";
        List<Long> groupIds = context.__studentGroups.stream().map(GroupMapper::getId).collect(Collectors.toList());
        Map<Long, List<VacationHomeworkPackage.Location>> groupVacationHomeworkPackage = vacationHomeworkLoaderClient.loadVacationHomeworkPackageByClazzGroupIds(groupIds);
        List<VacationHomeworkPackage.Location> vacationHomeworks = new ArrayList<>();
        for (List<VacationHomeworkPackage.Location> locations : groupVacationHomeworkPackage.values()) {
            vacationHomeworks.addAll(locations);
        }
        for (VacationHomeworkPackage.Location location : vacationHomeworks) {

            boolean started = currentDate.getTime() > location.getStartTime();
            if (RuntimeMode.isTest() || started
                    || grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(context.getStudent(), "VacationHW", "WhiteList")) {
                switch (location.getSubject()) {
                    case ENGLISH: {
                        evh_exist = true;
                        evh_id = location.getId();
                        break;
                    }
                    case MATH: {
                        mvh_exist = true;
                        mvh_id = location.getId();
                        break;
                    }
                    case CHINESE: {
                        cvh_exist = true;
                        cvh_id = location.getId();
                        break;
                    }
                    default:
                }
            }

        }
        context.getParam().put("showVacationCard", evh_exist || mvh_exist || cvh_exist);
        context.getParam().put("evh_id", evh_id);
        context.getParam().put("mvh_id", mvh_id);
        context.getParam().put("cvh_id", cvh_id);
        context.getParam().put("evhExist", evh_exist);
        context.getParam().put("mvhExist", mvh_exist);
        context.getParam().put("cvhExist", cvh_exist);
        return context;
    }
}
