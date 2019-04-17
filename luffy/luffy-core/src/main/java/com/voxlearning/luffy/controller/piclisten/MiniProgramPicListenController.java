package com.voxlearning.luffy.controller.piclisten;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.piclisten.api.MiniProgramCheckService;
import com.voxlearning.utopia.service.piclisten.api.MiniProgramGroupService;
import com.voxlearning.utopia.service.piclisten.api.MiniProgramReadService;
import com.voxlearning.utopia.service.piclisten.api.entity.MiniProgramRead;
import com.voxlearning.utopia.service.piclisten.client.MiniProgramCheckServiceClient;
import com.voxlearning.utopia.service.piclisten.client.MiniProgramGroupServiceClient;
import com.voxlearning.utopia.service.piclisten.client.MiniProgramReadServiceClient;
import com.voxlearning.utopia.service.user.api.entities.ChannelCUserAttribute;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * <a href="http://wiki.17zuoye.net/pages/viewpage.action?pageId=38903565">wiki</a>
 *
 * @author RA
 */
@Controller
@Slf4j
@RequestMapping(value = "/xcx/piclisten")
public class MiniProgramPicListenController extends AbstractXcxPicListenController {


    @Inject
    private MiniProgramCheckServiceClient miniProgramCheckServiceClient;
    @Inject
    private MiniProgramReadServiceClient miniProgramReadServiceClient;

    @Inject
    private MiniProgramGroupServiceClient miniProgramGroupServiceClient;


    @RequestMapping(value = "/check.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage check() {
        return wrapper((mm) -> {
            mm.putAll(checkService().doCheck(pid(), uid()));
        });
    }

    @RequestMapping(value = "/read_plan.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage setReadPlan() {
        return wrapper((mm -> {

            Integer planMinutes = getRequestInt("plan_minutes", 10);
            Integer remind = getRequestInt("is_remind", 0);
            String remindTime = getRequestString("remind_time");
            mm.putAll(readService().setUserDayPlan(pid(), uid(), planMinutes, remind, remindTime));

        }));
    }

    @RequestMapping(value = "/read_plan.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getReadPlan() {
        return wrapper((mm -> {
            mm.putAll(readService().getUserDayPlan(pid(), uid()));
        }));
    }


    @RequestMapping("/rank.vpage")
    @ResponseBody
    public MapMessage rank() {
        return wrapper((mm -> {
            Long uid = uid();
            mm.add("week_read_times", readService().getWeekReadTimes(uid).stream().mapToLong(i -> i).sum());
            mm.add("week_checking", checkService().getWeekContinuousCheckCount(uid));
            mm.add("total_read_times", readService().getTotalReadTimes(uid));
            mm.add("total_checked", checkService().getTotalCheckCount(uid));
        }));
    }


    @RequestMapping("/group/week.vpage")
    @ResponseBody
    public MapMessage groupWeek() {
        return wrapper((mm -> {
            mm.putAll(groupService().loadWeekGroupRank(uid(), pid(), gid()));

        }));
    }

    @RequestMapping("/group/total.vpage")
    @ResponseBody
    public MapMessage groupTotal() {
        return wrapper((mm -> {
            mm.putAll(groupService().loadTotalGroupRank(uid(), pid(), gid()));

        }));
    }


    @RequestMapping("/my.vpage")
    @ResponseBody
    public MapMessage my() {
        return wrapper((mm) -> {

            Long pid = pid();
            Long uid = uid();

            mm.putAll(readService().getUserDayPlan(pid, uid));
            MiniProgramRead read = readService().loadByUid(uid);
            long readTimes = 0;
            int readWords = 0;
            String readSince, readTill;
            if (read != null) {
                readTimes = TimeUnit.MILLISECONDS.toMinutes(read.getReadTimes());
                readWords = read.getReadWords();
                readSince = DateUtils.dateToString(read.getCreateTime(), DateUtils.FORMAT_SQL_DATE);
                readTill = DateUtils.dateToString(read.getUpdateTime(), DateUtils.FORMAT_SQL_DATE);
            } else {
                readSince = DateUtils.getTodaySqlDate();
                readTill = readSince;
            }
            mm.add("read_since", readSince);
            mm.add("read_till", readTill);
            mm.add("read_times", readTimes);
            mm.add("read_words", readWords);
            mm.add("checked", checkService().getTotalCheckCount(uid));
            mm.add("week_read_times", readService().getWeekReadTimes(uid));
            mm.add("is_checked", checkService().isChecked(uid));
        });
    }


    @RequestMapping(value = "/form_id.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage formId() {

        String formId = getRequestString("form_id");
        return wrapper((mm -> {
            readService().addNoticeFormId(pid(), formId);
        }));
    }


    @RequestMapping(value = "/children.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadStudent() {

        return wrapper(mm -> {
            List<User> student = studentLoaderClient.loadParentStudents(pid());
            List<Map<String, Object>> list = new ArrayList<>();

            if (student.size() > 0) {

                for (User user : student) {
                    Map<String, Object> map = new HashMap<>();
                    String name = user.fetchRealname();
                    boolean editable = true;
                    if (StringUtils.hasText(name)) {
                        editable = false;
                    }
                    map.put("editable", editable);
                    map.put("name", user.fetchRealname());
                    map.put("gender", user.fetchGender().getCode());
                    map.put("avatar", user.fetchImageUrl());
                    Integer clazzLevel = 0;
                    String clazzName = "";

                    ChannelCUserAttribute attr = studentLoaderClient.loadStudentChannelCAttribute(user.getId());
                    if (attr != null) {
                        ChannelCUserAttribute.ClazzCLevel classLevel = ChannelCUserAttribute.getClazzCLevelByClazzJie(attr.getClazzJie());
                        if (classLevel != null) {
                            clazzLevel = classLevel.getLevel();
                            clazzName = classLevel.getDescription();
                        }
                    }

                    map.put("clazz_level", clazzLevel);
                    map.put("clazz_name", clazzName);
                    int age = 1;
                    Integer year = user.getProfile().getYear();
                    if (null != year) {
                        age = LocalDate.now().getYear() - year;
                        age = age > 0 ? age : 1;

                    }
                    map.put("age", age);
                    map.put("uid", user.getId());
                    list.add(map);
                }

            }
            mm.put("children", list);
        });


    }


    @RequestMapping(value = "/children.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage editStudent() {

        return wrapper(mm -> {

            String name = getRequestString("name");
            String gender = getRequestString("gender");
            Integer clazz = getRequestInt("clazz_level");
            Integer age = getRequestInt("age");

            Long uid = uid();
            StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(uid);
            // Only user first time update user profile
            if (!StringUtils.hasText(studentDetail.fetchRealname())) {

                if (nb(name)) {
                    userServiceClient.changeName(uid, name);
                }

                if (nb(gender)) {
                    userServiceClient.changeGender(uid, gender);
                }

                if (clazz > 0) {
                    studentServiceClient.updateChannelCStudentClazzLevel(uid, clazz);
                }

                if (age > 0) {
                    Integer year = LocalDate.now().getYear() - age;
                    userServiceClient.changeUserBirthday(uid, year, null, null);
                }


            }

        });


    }


    private MiniProgramGroupService groupService() {
        return miniProgramGroupServiceClient.getRemoteReference();
    }

    private MiniProgramCheckService checkService() {
        return miniProgramCheckServiceClient.getRemoteReference();
    }

    private MiniProgramReadService readService() {
        return miniProgramReadServiceClient.getRemoteReference();
    }
}
