package com.voxlearning.utopia.mizar.listener.handler;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.mizar.service.settlement.UserSettlementService;
import com.voxlearning.utopia.service.mizar.api.entity.user.MizarUserSchool;
import com.voxlearning.utopia.service.mizar.consumer.loader.MizarUserSchoolLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * UserSettlementHandler
 *
 * @author song.wang
 * @date 2017/6/27
 */
@Named
public class UserSettlementHandler extends SpringContainerSupport {

    @Inject
    private UserSettlementService userSettlementService;

    @Inject
    private MizarUserSchoolLoaderClient mizarUserSchoolLoaderClient;


    public void handle(List<Long> schoolIds, Integer day) {
        Set<Long> schoolSet = new HashSet<>();
        List<MizarUserSchool> userSchoolList = mizarUserSchoolLoaderClient.loadAll();
        // 过滤出指定的学校
        if(CollectionUtils.isNotEmpty(schoolIds)){
            userSchoolList = userSchoolList.stream().filter(p -> schoolIds.contains(p.getSchoolId())).collect(Collectors.toList());
        }

        // 过滤出合同开始月份在指定日期之前的学校， 合同还未生效的学校不参与计算
        Integer month = SafeConverter.toInt(DateUtils.dateToString(DateUtils.addDays(new Date(), -1), "yyyyMM"));
        if(day != null){
            month = SafeConverter.toInt(DateUtils.dateToString(DateUtils.stringToDate(String.valueOf(day), "yyyyMMdd"), "yyyyMM"));
        }
        Integer targetMonth = month;
        userSchoolList = userSchoolList.stream().filter(p -> p.getContractStartMonth() != null && p.getContractStartMonth() <= targetMonth).collect(Collectors.toList());
        userSchoolList.forEach(p -> schoolSet.add(p.getSchoolId()));

        userSettlementService.calAndSaveSettlementData(schoolSet, day);
    }
}
