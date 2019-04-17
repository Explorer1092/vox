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

package com.voxlearning.washington.controller.ucenter;

import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.washington.support.AbstractController;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 给天津一些自建班级学校用的controller
 * 单独新建的文件，为了不需要的时候好删。。。
 * 如果以后这块逻辑仍然需要的话，可以统一到SignUpController中
 *
 * @author changyuan.liu
 */
@Controller
@RequestMapping("/signup_tianjin")
public class SignUpController_Tianjin extends AbstractController {

    private static List<IdNamePair> schoolList = new ArrayList<IdNamePair>() {
        {
            add(IdNamePair.newInstance(78345, "天津市塘沽区浙江路小学上海道校区"));
            add(IdNamePair.newInstance(243300, "天津市塘沽区浙江路小学福州道校区"));
            add(IdNamePair.newInstance(380307, "天津市塘沽区浙江路小学南益校区"));
            add(IdNamePair.newInstance(79365, "天津市塘沽区向阳第三小学"));
            add(IdNamePair.newInstance(79448, "天津市塘沽区向阳第一小学"));

            add(IdNamePair.newInstance(79374, "天津市塘沽区广州道小学"));
            add(IdNamePair.newInstance(78354, "天津市塘沽区贻成小学"));
            add(IdNamePair.newInstance(78347, "天津市塘沽区中心庄小学"));
            add(IdNamePair.newInstance(243303, "天津市塘沽区宁车沽小学"));
            add(IdNamePair.newInstance(106477, "天津市塘沽区宁波里小学"));

            add(IdNamePair.newInstance(79359, "天津市塘沽区新港第二小学"));
            add(IdNamePair.newInstance(79401, "天津市塘沽区新港第四小学"));
            add(IdNamePair.newInstance(243318, "天津市塘沽区博才小学"));
            add(IdNamePair.newInstance(3448, "天津市塘沽区新湖学校"));
            add(IdNamePair.newInstance(78359, "天津市塘沽区黄圈小学"));

            add(IdNamePair.newInstance(79402, "天津市塘沽区徐州道小学"));
            add(IdNamePair.newInstance(78350, "天津市塘沽区新城小学"));
            add(IdNamePair.newInstance(79405, "天津市塘沽区大庆道小学"));
            add(IdNamePair.newInstance(106473, "天津市塘沽区工农村小学"));
            add(IdNamePair.newInstance(79341, "天津市塘沽区草场街小学"));

            add(IdNamePair.newInstance(79387, "天津市塘沽区桂林路小学"));
            add(IdNamePair.newInstance(243186, "天津市河西区天津师范大学附属小学"));
            add(IdNamePair.newInstance(79397, "天津市塘沽区朝阳小学"));
            add(IdNamePair.newInstance(79306, "天津市塘沽区岷江里小学"));
            add(IdNamePair.newInstance(243307, "天津市塘沽区于庄子小学"));

            add(IdNamePair.newInstance(78355, "天津市塘沽区馨桥园小学"));
            add(IdNamePair.newInstance(106471, "天津市塘沽区大梁子小学"));
            add(IdNamePair.newInstance(78363, "天津市大港区同盛小学"));
            add(IdNamePair.newInstance(243299, "天津市塘沽区渤海石油第二小学"));
            add(IdNamePair.newInstance(243314, "天津市塘沽区渤海石油第一小学"));

            add(IdNamePair.newInstance(106480, "天津市塘沽区新港第一小学"));
            add(IdNamePair.newInstance(243016, "天津市大港区滨海新区大港实验小学"));
            add(IdNamePair.newInstance(243292, "天津市塘沽区塘沽上海道小学"));
            add(IdNamePair.newInstance(78346, "天津市塘沽区第二中心小学"));
            add(IdNamePair.newInstance(106475, "天津市塘沽区胡家园小学"));

            add(IdNamePair.newInstance(106516, "天津市大港区育才学校"));
            add(IdNamePair.newInstance(394519, "天津市滨海新区塘沽三中心小学"));
            add(IdNamePair.newInstance(394520, "天津市塘沽区紫云小学"));
            add(IdNamePair.newInstance(394521, "天津市塘沽区兴华里学校"));
            add(IdNamePair.newInstance(79435, "天津市塘沽区一中心小学"));

            add(IdNamePair.newInstance(243305, "天津市塘沽区盐场小学"));
            add(IdNamePair.newInstance(243309, "天津市塘沽区刘庄南窑小学"));
            add(IdNamePair.newInstance(3436, "天津市塘沽区塘沽体育学校"));
            add(IdNamePair.newInstance(3443, "天津市塘沽区北塘学校"));
            add(IdNamePair.newInstance(243320, "天津市塘沽区河头小学"));
            add(IdNamePair.newInstance(79221, "天津市塘沽区善门口小学"));
        }
    };

    @AllArgsConstructor(staticName = "newInstance")
    private static class IdNamePair {
        @Getter @Setter long id;
        @Getter @Setter String name;
    }

    @Inject private RaikouSDK raikouSDK;

    /**
     * 读取学校列表
     *
     * @return
     * @author changyuan.liu
     */
    @RequestMapping(value = "schoollist.vpage", method = RequestMethod.GET)
    @ResponseBody
    public List<IdNamePair> getSchoolList() {
        return schoolList;
    }

    /**
     * 读取班级列表
     * 参数为schoolId
     *
     * @return 返回格式为{
     * "1":[{id:xxx,name:xxx},...],
     * "2":[{id:xxx,name:xxx},...],
     * ...
     * }
     * @author changyuan.liu
     */
    @RequestMapping(value = "clazzlist.vpage", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, List<IdNamePair>> getClazzList() {
        long schoolId = getRequestLong("schoolId");
        if (schoolId == 0) {
            logger.error("schoolId cannot be null.");
            return null;
        }

        List<Clazz> clazzs = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadSchoolClazzs(schoolId).enabled().toList();
        Map<String, List<IdNamePair>> result = clazzs.stream()
                .collect(Collectors.groupingBy(Clazz::getClassLevel))
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().stream()
                                .map(c -> IdNamePair.newInstance(c.getId(), c.getClassName()))
                                .collect(Collectors.toList())));
        return result;
    }
}
