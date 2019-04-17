package com.voxlearning.utopia.mizar.controller.settlement;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.mizar.auth.MizarAuthUser;
import com.voxlearning.utopia.mizar.controller.AbstractMizarController;
import com.voxlearning.utopia.mizar.service.settlement.UserSettlementService;
import com.voxlearning.utopia.service.mizar.api.entity.settlement.SchoolSettlement;
import com.voxlearning.utopia.service.mizar.api.entity.user.MizarUserSchool;
import com.voxlearning.utopia.service.mizar.consumer.loader.MizarUserSchoolLoaderClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户收入结算
 *
 * @author song.wang
 * @date 2017/6/23
 */
@Controller
@RequestMapping(value = "/basic/settlement")
@Slf4j
public class UserSettlementController extends AbstractMizarController {

    @Inject
    private UserSettlementService userSettlementService;
    @Inject
    private MizarUserSchoolLoaderClient userSchoolLoaderClient;

    @RequestMapping("index.vpage")
    public String index(Model model){
        MizarAuthUser user = getCurrentUser();

        Integer month = requestInteger("month");
        Integer currentMonth = SafeConverter.toInt(DateUtils.dateToString(DateUtils.addDays(new Date(), -1), "yyyyMM"));
        if(month == null){
            month = currentMonth;
        }

        List<Integer> monthList = new ArrayList<>();
        monthList.add(currentMonth);

        List<MizarUserSchool> userSchoolList = userSchoolLoaderClient.loadByUserId(user.getUserId());
        Integer minMonth = currentMonth;
        // 获取最小的合同开始月份
        if(CollectionUtils.isNotEmpty(userSchoolList)){
            minMonth = userSchoolList.stream().map(MizarUserSchool::getContractStartMonth).min(Integer::compareTo).get();
        }

        for(int i = currentMonth -1; i >= minMonth; i--){
            monthList.add(i);
        }

        Integer targetMonth = month;
        List<Long> schoolIds = userSchoolList.stream().filter(p -> p.getContractStartMonth() <= targetMonth).map(MizarUserSchool::getSchoolId).collect(Collectors.toList());
        Collection<SchoolSettlement> settlementList = userSettlementService.loadSchoolSettlementData(schoolIds, month);
        Double totalPayment = 0d;  // 本月提成
        Double totalAmount = 0d;   // 本月交易额
        if(CollectionUtils.isNotEmpty(settlementList)){
            totalPayment = settlementList.stream().map(SchoolSettlement::getPayment).reduce(0d, (x, y) -> x +y);
            totalAmount = settlementList.stream().map(SchoolSettlement::getTotalAmount).reduce(0d, (x, y) -> x +y);
        }
        model.addAttribute("totalPayment", totalPayment);
        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("settlementList", settlementList);
        model.addAttribute("month", month);
        model.addAttribute("monthList", monthList);
        return "/basic/settlement/index";
    }

}
