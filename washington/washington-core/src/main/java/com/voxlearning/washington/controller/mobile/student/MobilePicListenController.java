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

package com.voxlearning.washington.controller.mobile.student;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.content.api.entity.Book;
import com.voxlearning.utopia.service.content.api.entity.Lesson;
import com.voxlearning.utopia.service.content.api.entity.Unit;
import com.voxlearning.utopia.service.question.api.entity.PicListen;
import com.voxlearning.washington.controller.mobile.AbstractMobileController;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.washington.controller.open.v1.content.ContentApiConstants.*;

/**
 * Created by Summer Yang on 2015/10/16.
 *
 *  点读机接口 学生APP ，不过上线不到一天就下线了。不清楚以后还用不用， 先留着吧。
 *  //fixme:学生的Controller命名规则都是MobileStudent*****Controller,这个名字不规范
 *
 */
@Controller
@RequestMapping("/studentMobile/piclisten")
@NoArgsConstructor
@Slf4j
public class MobilePicListenController extends AbstractMobileController {

    @RequestMapping(value = "loadbyunitid.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadByUnitId(@RequestBody Map<String, Object> param) {
        Long unitId = ConversionUtils.toLong(param.get("unitId"));
        if (unitId == 0) {
            return MapMessage.errorMessage("参数错误");
        }
        List<Lesson> lessonList = englishContentLoaderClient.loadEnglishUnitLessons(unitId);
        List<Long> lessonIdList = lessonList.stream()
                .filter(e -> e != null && e.getId() != null)
                .map(Lesson::getId)
                .collect(Collectors.toList());
        Map<Long, List<PicListen>> dataMap = questionLoaderClient.loadPicListenByLessonIds(lessonIdList);
        if (MapUtils.isNotEmpty(dataMap)) {
            //排序
            for (Map.Entry<Long, List<PicListen>> entry : dataMap.entrySet()) {
                entry.getValue().stream().sorted((o1, o2) -> o1.getRank().compareTo(o2.getRank())).collect(Collectors.toList());
            }
        }
        return MapMessage.successMessage().add("dataMap", dataMap);
    }

    @RequestMapping(value = "loadunitbybookid.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadUnitByBookId(@RequestBody Map<String, Object> param) {
        Long bookId = ConversionUtils.toLong(param.get("bookId"));
        if (bookId == 0) {
            return MapMessage.errorMessage("参数错误");
        }
        Book book = englishContentLoaderClient.loadEnglishBook(bookId);
        if (book == null) {
            return MapMessage.errorMessage("教材不存在");
        }
        Map<String, Object> dataBook = new HashMap<>();
        dataBook.put("bookId", bookId);
        dataBook.put("cname", book.getCname());
        dataBook.put("ename", book.getEname());
        dataBook.put("url", book.getImgUrl());
        //获取单元
        List<Unit> units = englishContentLoaderClient.loadEnglishBookUnits(bookId);
        if (book.getBookStructure() == 3) {
            List<Map> unitMapList = new LinkedList<>();
            for (Unit unit : units) {
                Map<String, Object> unitMap = new LinkedHashMap<>();
                unitMap.put(RES_UNIT_ID, unit.getId());
                unitMap.put(RES_RANK, unit.getRank());
                unitMap.put(RES_UNIT_CNAME, unit.getCname());
                unitMap.put(RES_UNIT_ENAME, unit.getEname());
                unitMapList.add(unitMap);
            }
            dataBook.put(RES_UNIT_LIST, unitMapList);
            dataBook.put(RES_GROUP_FLAG, false);
            dataBook.put(RES_GROUP_LIST, null);
        } else {
            dataBook.put(RES_UNIT_LIST, null);
            dataBook.put(RES_GROUP_FLAG, true);
            List<Map> unitGroupList = new LinkedList<>();
            Map<String, Object> groupInfo = new LinkedHashMap<>();
            List<Map> groupInfoList = new LinkedList<>();
            for (Unit unit : units) {
                Map<String, Object> unitGroupInfo = new LinkedHashMap<>();
                unitGroupInfo.put(RES_UNIT_ID, unit.getId());
                unitGroupInfo.put(RES_UNIT_CNAME, unit.getCname());
                unitGroupInfo.put(RES_UNIT_ENAME, unit.getEname());
                unitGroupInfo.put(RES_RANK, unit.getRank());
                unitGroupInfo.put(RES_GROUP_CNAME, unit.getGroupCname());
                unitGroupInfo.put(RES_GROUP_ENAME, unit.getGroupEname());

                if (groupInfo.get(RES_GROUP_CNAME) == null) {
                    groupInfo.put(RES_GROUP_CNAME, unit.getGroupCname());
                    groupInfo.put(RES_GROUP_ENAME, unit.getGroupEname());
                    groupInfoList.add(unitGroupInfo);
                    groupInfo.put(RES_GROUP_INFO_LIST, groupInfoList);
                } else {

                    if (groupInfo.get(RES_GROUP_CNAME).equals(unit.getGroupCname())) {
                        groupInfoList.add(unitGroupInfo);
                        groupInfo.put(RES_GROUP_INFO_LIST, groupInfoList);
                    } else {
                        unitGroupList.add(groupInfo);
                        groupInfo = new LinkedHashMap<>();
                        groupInfoList = new LinkedList<>();
                        groupInfo.put(RES_GROUP_CNAME, unit.getGroupCname());
                        groupInfo.put(RES_GROUP_ENAME, unit.getGroupEname());
                        groupInfoList.add(unitGroupInfo);
                        groupInfo.put(RES_GROUP_INFO_LIST, groupInfoList);
                    }
                }
            }
            if (!groupInfo.isEmpty()) {
                unitGroupList.add(groupInfo);
            }
            dataBook.put(RES_GROUP_LIST, unitGroupList);
        }
        return MapMessage.successMessage().add("book", dataBook);
    }
}
