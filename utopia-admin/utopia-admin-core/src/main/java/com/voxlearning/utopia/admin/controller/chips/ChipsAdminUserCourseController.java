package com.voxlearning.utopia.admin.controller.chips;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.admin.controller.AbstractAdminController;
import com.voxlearning.utopia.service.ai.api.ChipsEnglishUserService;
import com.voxlearning.utopia.service.ai.client.AiOrderProductServiceClient;
import com.voxlearning.utopia.service.ai.data.ChipsUserCourseMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Summer on 2019/1/14
 */
@Controller
@RequestMapping("/chips/course")
public class ChipsAdminUserCourseController extends AbstractAdminController {

    @Inject private AiOrderProductServiceClient aiOrderProductServiceClient;
    @ImportService(interfaceClass = ChipsEnglishUserService.class)
    private ChipsEnglishUserService chipsEnglishUserService;
    // 用户课程查询首页
    @RequestMapping(value = "index.vpage", method = {RequestMethod.GET})
    public String chipsIndex(Model model) {
        return "chips/course/index";
    }

    // 查询用户课程
    @RequestMapping(value = "list.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage userCourseList() {
        Long userId = getRequestLong("userId");
        if (userId == 0L) {
            return MapMessage.errorMessage("请输入用户ID");
        }
        List<ChipsUserCourseMapper> userCourseMapperList = aiOrderProductServiceClient.getRemoteReference().loadUserAllCourseInfo(userId);
        return MapMessage.successMessage().add("data", userCourseMapperList);
    }


    @RequestMapping(value = "openCourseIndex.vpage", method = {RequestMethod.GET})
    public String openCourseIndex(Model model) {
        return "chips/course/openCourseIndex";
    }

    // 查询用户课程
    @RequestMapping(value = "openCourseSave.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage openCourseSave() {
        String phones = getRequestString("phones");
        String products = getRequestString("products");
        Set<String> mobileList = checkPhone(phones);
        Set<String> productList = checkProduct(products);
        return chipsEnglishUserService.openSuperUser(mobileList, productList);
    }

    private Set<String> checkPhone(String phones) {
        if (StringUtils.isBlank(phones)) {
            return null;
        }
        String[] split = phones.split(",");
        return Arrays.stream(split).filter(StringUtils::isNotBlank).map(String::trim).collect(Collectors.toSet());
    }

    private Set<String> checkProduct(String products) {
        if (StringUtils.isBlank(products)) {
            return null;
        }
        String[] split = products.split(",");
        return Arrays.stream(split).filter(StringUtils::isNotBlank).map(String::trim).collect(Collectors.toSet());
    }
}
