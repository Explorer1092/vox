package com.voxlearning.utopia.admin.controller.equator.mission;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.SchoolYearPhase;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.equator.common.api.enums.mission.AssignmentConfigType;
import com.voxlearning.equator.service.mission.api.constants.mission.MissionEventType;
import com.voxlearning.equator.service.mission.api.constants.mission.MissionTemplateStatus;
import com.voxlearning.equator.service.mission.api.data.mission.MissionRule;
import com.voxlearning.equator.service.mission.api.entity.buffer.AssignmentTemplate;
import com.voxlearning.equator.service.mission.api.entity.buffer.TaskTemplate;
import com.voxlearning.equator.service.mission.client.buffer.AssignmentTemplateServiceClient;
import com.voxlearning.utopia.admin.controller.equator.AbstractEquatorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.alps.annotation.meta.ClazzLevel.FIFTH_GRADE;
import static com.voxlearning.alps.annotation.meta.ClazzLevel.FIRST_GRADE;
import static com.voxlearning.alps.annotation.meta.ClazzLevel.FOURTH_GRADE;
import static com.voxlearning.alps.annotation.meta.ClazzLevel.SECOND_GRADE;
import static com.voxlearning.alps.annotation.meta.ClazzLevel.SIXTH_GRADE;
import static com.voxlearning.alps.annotation.meta.ClazzLevel.THIRD_GRADE;

/**
 * @author Ruib
 * @since 2018/7/20
 */
@Controller
@RequestMapping("/equator/mission/assignment/")
public class AssignmentTemplateManageController extends AbstractEquatorController {
    @Inject private AssignmentTemplateServiceClient client;

    // 年级 学科 学期
    @RequestMapping(value = "index.vpage", method = {RequestMethod.GET})
    public String index(Model model) {
        List<Map<String, Object>> grades = new ArrayList<>();
        grades.add(MapUtils.m("key", "FIRST_GRADE", "value", "一年级", "number", 1));
        grades.add(MapUtils.m("key", "SECOND_GRADE", "value", "二年级", "number", 2));
        grades.add(MapUtils.m("key", "THIRD_GRADE", "value", "三年级", "number", 3));
        grades.add(MapUtils.m("key", "FOURTH_GRADE", "value", "四年级", "number", 4));
        grades.add(MapUtils.m("key", "FIFTH_GRADE", "value", "五年级", "number", 5));
        grades.add(MapUtils.m("key", "SIXTH_GRADE", "value", "六年级", "number", 6));
        model.addAttribute("grades", grades);

        List<Map<String, Object>> subjects = new ArrayList<>();
        subjects.add(MapUtils.m("key", Subject.ENGLISH, "value", Subject.ENGLISH.getValue()));
        subjects.add(MapUtils.m("key", Subject.MATH, "value", Subject.MATH.getValue()));
        subjects.add(MapUtils.m("key", Subject.CHINESE, "value", Subject.CHINESE.getValue()));
        subjects.add(MapUtils.m("key", Subject.ENCYCLOPEDIA, "value", Subject.ENCYCLOPEDIA.getValue()));
        model.addAttribute("subjects", subjects);

        List<Map<String, Object>> terms = new ArrayList<>();
        terms.add(MapUtils.m("key", "LAST_TERM", "value", "上学期"));
        terms.add(MapUtils.m("key", "NEXT_TERM", "value", "下学期"));
        model.addAttribute("terms", terms);

        return "/equator/mission/assignment/index";
    }

    @RequestMapping(value = "loadassignmenttemplate.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage loadAssignmentTemplate() {
        ClazzLevel grade = ClazzLevel.valueOf(getRequestString("grade"));
        Subject subject = Subject.ofWithUnknown(getRequestString("subject"));
        SchoolYearPhase term = SchoolYearPhase.valueOf(getRequestString("terms"));

        List<AssignmentTemplate> templates = client.getService().loadEntityFromDatabase()
                .getUninterruptibly()
                .stream()
                .filter(t -> t.getGrade() == grade)
                .filter(t -> t.getSubject() == subject)
                .filter(t -> t.getTerm() == term)
                .sorted((Comparator.comparingInt(AssignmentTemplate::getRank)))
                .collect(Collectors.toList());

        return MapMessage.successMessage().add("templates", templates);
    }

    @RequestMapping(value = "upsertpage.vpage", method = {RequestMethod.GET})
    public String upsertPage(Model model) {
        List<Map<String, Object>> modes = new ArrayList<>();
        modes.add(MapUtils.m("key", Mode.TEST, "value", "测试环境"));
        modes.add(MapUtils.m("key", Mode.STAGING, "value", "预发布环境"));
        modes.add(MapUtils.m("key", Mode.PRODUCTION, "value", "生产环境"));
        model.addAttribute("modes", modes);

        List<Map<String, Object>> grades = new ArrayList<>();
        grades.add(MapUtils.m("key", "FIRST_GRADE", "value", "一年级"));
        grades.add(MapUtils.m("key", "SECOND_GRADE", "value", "二年级"));
        grades.add(MapUtils.m("key", "THIRD_GRADE", "value", "三年级"));
        grades.add(MapUtils.m("key", "FOURTH_GRADE", "value", "四年级"));
        grades.add(MapUtils.m("key", "FIFTH_GRADE", "value", "五年级"));
        grades.add(MapUtils.m("key", "SIXTH_GRADE", "value", "六年级"));
        model.addAttribute("grades", grades);

        List<Map<String, Object>> subjects = new ArrayList<>();
        subjects.add(MapUtils.m("key", Subject.ENGLISH, "value", Subject.ENGLISH.getValue()));
        subjects.add(MapUtils.m("key", Subject.MATH, "value", Subject.MATH.getValue()));
        subjects.add(MapUtils.m("key", Subject.CHINESE, "value", Subject.CHINESE.getValue()));
        subjects.add(MapUtils.m("key", Subject.ENCYCLOPEDIA, "value", Subject.ENCYCLOPEDIA.getValue()));
        model.addAttribute("subjects", subjects);

        List<Map<String, Object>> terms = new ArrayList<>();
        terms.add(MapUtils.m("key", "LAST_TERM", "value", "上学期"));
        terms.add(MapUtils.m("key", "NEXT_TERM", "value", "下学期"));
        model.addAttribute("terms", terms);

        List<Map<String, Object>> status = new ArrayList<>();
        status.add(MapUtils.m("key", MissionTemplateStatus.ONLINE, "value", "上线"));
        status.add(MapUtils.m("key", MissionTemplateStatus.OFFLINE, "value", "下线"));
        model.addAttribute("status", status);

        List<Map<String, Object>> acts = new ArrayList<>();
        Field[] fields = AssignmentConfigType.class.getDeclaredFields();
        for (Field field : fields) {
            if (field.isEnumConstant() && field.getAnnotation(Deprecated.class) == null) {
                AssignmentConfigType act = AssignmentConfigType.of(field.getName());
                acts.add(MapUtils.map("key", field.getName(), "value", act.getName()));
            }
        }
        model.addAttribute("acts", acts);

        return "/equator/mission/assignment/upsertpage";
    }

    @RequestMapping(value = "loadassignmenttemplatebytemplateid.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage loadAssignmentTemplateByTemplateId() {
        String templateId = getRequestString("templateId");
        if (StringUtils.isEmpty(templateId)) return MapMessage.errorMessage();

        AssignmentTemplate template = client.getService().loadEntityFromDatabase(templateId).getUninterruptibly();
        return MapMessage.successMessage().add("template", template);
    }

    @RequestMapping(value = "upsertassignmenttemplate.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage upsertAssignmentTemplate() {
        try {
            Map<String, Object> template = JsonUtils.fromJson(getRequestString("template"));
            String id = SafeConverter.toString(template.get("id"));

            AssignmentTemplate entity = new AssignmentTemplate();
            if (StringUtils.isNotBlank(id)) entity.setId(id); // 更新

            entity.setTitle(SafeConverter.toString(template.get("title")));
            entity.setSubtitle(SafeConverter.toString(template.get("subtitle")));
            entity.setLable(SafeConverter.toString(template.get("lable")));
            entity.setDesc(SafeConverter.toString(template.get("desc")));

            Mode env = Mode.getMode(SafeConverter.toString(template.get("env")));
            if (env == null) return MapMessage.errorMessage("Env error.");
            entity.setEnv(env);

            ClazzLevel grade;
            try {
                grade = ClazzLevel.valueOf(SafeConverter.toString(template.get("grade")));
                if (!Arrays.asList(FIRST_GRADE, SECOND_GRADE, THIRD_GRADE, FOURTH_GRADE, FIFTH_GRADE, SIXTH_GRADE)
                        .contains(grade)) return MapMessage.errorMessage("Grade error.");
                entity.setGrade(grade);
            } catch (IllegalArgumentException e) {
                return MapMessage.errorMessage("Grade error.");
            }

            SchoolYearPhase term;
            try {
                term = SchoolYearPhase.valueOf(SafeConverter.toString(template.get("term")));
                if (!Arrays.asList(SchoolYearPhase.LAST_TERM, SchoolYearPhase.NEXT_TERM).contains(term))
                    return MapMessage.errorMessage("Term error.");
                entity.setTerm(term);
            } catch (IllegalArgumentException e) {
                return MapMessage.errorMessage("Term error.");
            }

            Subject subject = Subject.ofWithUnknown(SafeConverter.toString(template.get("subject")));
            if (subject == Subject.UNKNOWN) return MapMessage.errorMessage("Subject error.");
            entity.setSubject(subject);

            AssignmentConfigType act = AssignmentConfigType.of(SafeConverter.toString(template.get("act")));
            if (act == null) return MapMessage.errorMessage("Act error.");
            entity.setAct(act);

            try {
                MissionTemplateStatus status = MissionTemplateStatus.valueOf(SafeConverter.toString(template.get("status")));
                entity.setStatus(status);
            } catch (IllegalArgumentException e) {
                return MapMessage.errorMessage("MissionTemplateStatus error.");
            }

            entity.setRank(SafeConverter.toInt(template.get("rank")));
            entity.setMultiple(SafeConverter.toInt(template.get("multiple"), 1));
            entity.setIcon(SafeConverter.toString(template.get("icon")));

            // noinspection unchecked
            MissionRule rule = new MissionRule((Map<String, Object>) template.get("rule"));
            if (rule.getEventType() == MissionEventType.unknown || rule.getTotalTimes() <= 0)
                return MapMessage.errorMessage("MissionRule error");
            entity.setRule(rule);

            // noinspection unchecked
            Map<String, Object> attachment = (Map<String, Object>) template.get("attachment");
            if (attachment == null) attachment = new HashMap<>();
            entity.setAttachment(attachment);

            return client.getService().upsertEntity(entity);
        } catch (Exception e) {
            return MapMessage.errorMessage("操作失败");
        }
    }

    @RequestMapping(value = "removeassignmenttemplate.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage removeAssignmentTemplate() {
        String templateId = getRequestString("templateId");
        if (StringUtils.isEmpty(templateId)) return MapMessage.errorMessage("模板id不能为空");

        return client.getService().removeEntity(templateId);
    }
}
