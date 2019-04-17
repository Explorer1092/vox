package com.voxlearning.washington.controller.mobile.parent;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.athena.api.AppUsingNumService;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.vendor.api.ClazzExpandSellService;
import com.voxlearning.utopia.service.vendor.api.mapper.ClazzExpandSellInfoMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author: wei.jiang
 * @Date: Created on 2018/3/14
 */
@Controller
@RequestMapping(value = "/parentMobile/clazz_expand")
@Slf4j
public class MobileClazzExpandSellController extends AbstractMobileParentController {

    @ImportService(interfaceClass = ClazzExpandSellService.class)
    private ClazzExpandSellService clazzExpandSellService;
    @ImportService(interfaceClass = AppUsingNumService.class)
    private AppUsingNumService appUsingNumService;

    @RequestMapping(value = "/student_info_list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getUsedStudentInfoList() {
        User parent = currentParent();
        if (parent == null) {
            return noLoginResult;
        }
        long sid = getRequestLong("sid");
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(sid);
        if (studentDetail == null || studentDetail.getClazz() == null) {
            return MapMessage.successMessage().add("has_clazz", Boolean.FALSE);
        }
        List<Map<String, Object>> returnList = new ArrayList<>();
        List<OrderProductServiceType> expandClazzList = clazzExpandSellService.getExpandClazzList();
        List<String> orderProductNames = expandClazzList.stream().map(OrderProductServiceType::name).collect(Collectors.toList());
        Map<String, ClazzExpandSellInfoMapper> sellInfoMapperMap = clazzExpandSellService.getUsedStudentList(studentDetail.getClazzId()).stream().collect(Collectors.toMap(ClazzExpandSellInfoMapper::getOrderProductType, Function.identity()));
        Map<String, Integer> queryNational = appUsingNumService.queryNational(orderProductNames);
        for (OrderProductServiceType productServiceType : expandClazzList) {
            Map<String, Object> returnMap = new HashMap<>();
            returnMap.put("national_use_count", SafeConverter.toInt(queryNational.get(productServiceType.name())));
            ClazzExpandSellInfoMapper sellInfoMapper = sellInfoMapperMap.get(productServiceType.name());
            List<String> userNameList = new ArrayList<>();
            if (sellInfoMapper != null) {
                userNameList = sellInfoMapper.getStudentName();
            }
            returnMap.put("use_student_name", userNameList);
            returnMap.put("product_name", productServiceType.name());
            returnList.add(returnMap);
        }
        return MapMessage.successMessage().add("has_clazz", Boolean.TRUE).add("expand_product_list", returnList);
    }

}
