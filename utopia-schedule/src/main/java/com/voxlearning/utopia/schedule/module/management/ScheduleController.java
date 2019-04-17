/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.schedule.module.management;

import com.voxlearning.alps.annotation.common.Singleton;
import com.voxlearning.alps.api.context.ApplicationContextScanner;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.utopia.schedule.journal.JobJournalDao;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Singleton
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@RequestMapping("/schedule_service")
public class ScheduleController {

    public static final ScheduleController INSTANCE = new ScheduleController();

    @RequestMapping(value = "/index.do", method = RequestMethod.GET)
    public String index() {
        return "schedule_service/index";
    }

    @RequestMapping("/query_job_journal.do")
    public String queryJobJournal(Model model,
                                  @RequestParam(name = "date") String date) {

        if (StringUtils.isBlank(date)) {
            date = DayRange.current().toString();
        }
        JobJournalDao dao = ApplicationContextScanner.getInstance().getBean(JobJournalDao.class);
        model.addAttribute("jobJournals", dao.findByStartDate(date.trim()));
        return "schedule_service/job_journal";
    }
}
