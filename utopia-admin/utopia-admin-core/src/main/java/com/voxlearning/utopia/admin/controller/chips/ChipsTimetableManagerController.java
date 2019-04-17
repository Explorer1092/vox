package com.voxlearning.utopia.admin.controller.chips;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.admin.controller.AbstractAdminSystemController;
import com.voxlearning.utopia.admin.data.ChipsTimetablePojo;
import com.voxlearning.utopia.admin.service.chips.ChipsTimetableManagerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author guangqing
 * @since 2018/10/17
 */
@Controller
@RequestMapping("/chips/chips/coursemanager")
public class ChipsTimetableManagerController extends AbstractAdminSystemController {

    @Inject
    private ChipsTimetableManagerService chipsTimetableManagerService;

    @RequestMapping(value = "list.vpage", method = RequestMethod.GET)
    public String list(Model model) {
        List<ChipsTimetablePojo> courseList = chipsTimetableManagerService.loadAllChipsProduct();
        model.addAttribute("courseList", courseList);
        return "chips/course/courseList";
    }

    @RequestMapping(value = "edit.vpage", method = RequestMethod.GET)
    public String edit(Model model) {
        String productId = getRequestString("productId");
        ChipsTimetablePojo coursePojo = chipsTimetableManagerService.loadChipsTimetablePojo(productId);
        List<String> bookIdList = coursePojo.getEditPojoList().stream().map(ChipsTimetablePojo.EditPojo::getBookId).collect(Collectors.toList());
        model.addAttribute("bookList", StringUtils.join(bookIdList.toArray(), ","));
        model.addAttribute("coursePojo", coursePojo);
        return "chips/course/courseEdit";
    }

    @RequestMapping(value = "save.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage save() {
        String productId = getRequestString("productId");
        String bookList = getRequestString("bookList");
        String[] split = bookList.split(",");
        String beginDate = getRequestString("beginDate");
        String endDate = getRequestString("endDate");
        Map<String, List<Date>> bookDateMap = new HashMap<>();
        for (String bookId : split) {
            if (StringUtils.isBlank(bookId)) {
                continue;
            }
            String dateString = getRequestString("dateList_" + bookId).trim();
            List<Date> dateList = new ArrayList<>();
            if (StringUtils.isNotBlank(dateString)) {
                String[] dateSplit = dateString.trim().split(",");
                for (String dateStr : dateSplit) {
                    dateList.add(DateUtils.stringToDate(dateStr, DateUtils.FORMAT_SQL_DATE));
                }
            }
            dateList.sort(Comparator.comparing(Date::getTime));
            bookDateMap.put(bookId, dateList);
        }
        MapMessage checkMessage = chipsTimetableManagerService.checkValidParm(beginDate, endDate, bookDateMap);
        if (!checkMessage.isSuccess()) {
            return checkMessage;
        }
        return chipsTimetableManagerService.save(bookDateMap, productId, beginDate, endDate);
    }

    @RequestMapping(value = "initdate.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage initDateList() {
        String beginDate = getRequestString("initDate");
        int unitNum = getRequestInt("unitNum");
        boolean skipSun = getRequestBool("skipSun");
        boolean skipSat = getRequestBool("skipSat");
        String bookId = getRequestString("bookId");
        List<String> list = chipsTimetableManagerService.buildUnitDateList(unitNum, beginDate, skipSat, skipSun);
        return MapMessage.successMessage().add("dateList", StringUtils.join(list.toArray(), ",")).add("id", "dateList_" + bookId);
    }

}
