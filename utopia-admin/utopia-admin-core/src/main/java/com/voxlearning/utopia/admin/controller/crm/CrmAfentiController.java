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

package com.voxlearning.utopia.admin.controller.crm;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.time.FastDateFormat;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiLearningPlanPushExamHistory;
import com.voxlearning.utopia.service.afenti.consumer.AfentiLoaderClient;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.inject.Inject;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Shuai Huan
 * @since 2014/11/21
 */
@Controller
@RequestMapping("/crm/afenti")
public class CrmAfentiController extends CrmAbstractController {
    @Inject private AfentiLoaderClient afentiLoaderClient;

    @RequestMapping(value = "learningplanhistory.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    String learningPlanHistoryIndex(Model model) {
        Date startDate = null;
        Date endDate = null;
        Long userId = getRequestLong("userId", -1L);

        FastDateFormat sdf = FastDateFormat.getInstance("yyyy-MM-dd");
        String startDateStr = getRequestParameter("startDate", "").trim();
        try {
            if (StringUtils.isNotBlank(startDateStr)) startDate = sdf.parse(startDateStr);
            String endDateStr = getRequestParameter("endDate", "").trim();
            if (StringUtils.isNotBlank(endDateStr)) endDate = sdf.parse(endDateStr);
        } catch (ParseException e) {
            throw new RuntimeException("parse date error.", e);
        }

        if (startDate != null) model.addAttribute("startDate", sdf.format(startDate));
        if (endDate != null) model.addAttribute("endDate", sdf.format(endDate));
        if (userId >= 0) {
            model.addAttribute("userId", userId);
            model.addAttribute("datas", getLearningPlanHistoryList(userId, startDate, endDate));
        }
        return "crm/afenti/learningplanindex";
    }

    private List<Map<String, Object>> getLearningPlanHistoryList(Long userId, final Date startDate, final Date endDate) {
        List<AfentiLearningPlanPushExamHistory> histories = afentiLoaderClient
                .loadAfentiLearningPlanPushExamHistoryByUserId(userId);

        if (startDate != null) {
            histories = histories.stream()
                    .filter(source -> source.getCreatetime().getTime() >= startDate.getTime())
                    .collect(Collectors.toList());
        }
        if (endDate != null) {
            histories = histories.stream()
                    .filter(source -> source.getCreatetime().getTime() <= endDate.getTime())
                    .collect(Collectors.toList());
        }

        Collections.sort(histories, (o1, o2) -> o2.getCreatetime().compareTo(o1.getCreatetime()));

        List<Map<String, Object>> result = new ArrayList<>();
        for (AfentiLearningPlanPushExamHistory history : histories) {
            Map<String, Object> map = JsonUtils.fromJson(JsonUtils.toJson(history));
            // 处理下book
            String bookId = StringUtils.replace(history.getNewBookId(), "N_", "");
            Map<String, NewBookProfile> bookMap = newContentLoaderClient
                    .loadBookProfilesIncludeDisabled(Collections.singletonList(bookId));

            NewBookProfile book = bookMap.get(bookId);
            map.put("newBookId", book != null ? book.getId() : bookId);
            map.put("bookName", book != null ? book.getName() : "课本无效");
            map.put("subject", Subject.safeParse(history.getSubject()));
            map.put("createtime", DateUtils.dateToString(history.getCreatetime()));
            map.put("updatetime", DateUtils.dateToString(history.getUpdatetime()));
            result.add(map);
        }

        return result;
    }
}
