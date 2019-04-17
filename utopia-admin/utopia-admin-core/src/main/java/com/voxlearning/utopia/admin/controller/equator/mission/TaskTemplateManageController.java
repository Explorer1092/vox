package com.voxlearning.utopia.admin.controller.equator.mission;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.equator.service.mission.api.constants.mission.MissionEventType;
import com.voxlearning.equator.service.mission.api.constants.mission.MissionTemplateStatus;
import com.voxlearning.equator.service.mission.api.constants.task.TaskCycleType;
import com.voxlearning.equator.service.mission.api.constants.task.TaskSource;
import com.voxlearning.equator.service.mission.api.data.mission.MissionRule;
import com.voxlearning.equator.service.mission.api.entity.buffer.TaskTemplate;
import com.voxlearning.equator.service.mission.api.support.task.TaskCycle;
import com.voxlearning.equator.service.mission.client.buffer.TaskTemplateServiceClient;
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

/**
 * @author Ruib
 * @since 2018/7/20
 */
@Controller
@RequestMapping("/equator/mission/task/")
public class TaskTemplateManageController extends AbstractEquatorController {
    @Inject private TaskTemplateServiceClient client;

    @RequestMapping(value = "index.vpage", method = {RequestMethod.GET})
    public String index(Model model) {
        // 获取TaskSource
        List<Map<String, Object>> sources = new ArrayList<>();
        Field[] fields = TaskSource.class.getDeclaredFields();
        for (Field field : fields) {
            if (field.isEnumConstant() && field.getAnnotation(Deprecated.class) == null
                    && !StringUtils.equals(field.getName(), "unknown")) {
                TaskSource source = TaskSource.safeParse(field.getName());
                sources.add(MapUtils.map("key", field.getName(), "value", source.getDescription()));
            }
        }
        model.addAttribute("sources", sources);
        return "/equator/mission/task/index";
    }

    @RequestMapping(value = "loadtasktemplate.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage loadTaskTemplate() {
        TaskSource source = TaskSource.safeParse(getRequestString("source"));

        List<TaskTemplate> templates = client.getService().loadEntityFromDatabase()
                .getUninterruptibly()
                .stream()
                .filter(t -> t.getSource() == source)
                .sorted((Comparator.comparingInt(TaskTemplate::getRank)))
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

        List<Map<String, Object>> statuses = new ArrayList<>();
        statuses.add(MapUtils.m("key", MissionTemplateStatus.ONLINE, "value", "上线"));
        statuses.add(MapUtils.m("key", MissionTemplateStatus.OFFLINE, "value", "下线"));
        model.addAttribute("statuses", statuses);

        List<Map<String, Object>> subjects = new ArrayList<>();
        subjects.add(MapUtils.m("key", Subject.ENGLISH, "value", Subject.ENGLISH.getValue()));
        subjects.add(MapUtils.m("key", Subject.MATH, "value", Subject.MATH.getValue()));
        subjects.add(MapUtils.m("key", Subject.CHINESE, "value", Subject.CHINESE.getValue()));
        subjects.add(MapUtils.m("key", Subject.ENCYCLOPEDIA, "value", Subject.ENCYCLOPEDIA.getValue()));
        subjects.add(MapUtils.m("key", Subject.UNKNOWN, "value", Subject.UNKNOWN.getValue()));
        model.addAttribute("subjects", subjects);

        List<Map<String, Object>> cycleTypes = new ArrayList<>();
        cycleTypes.add(MapUtils.m("key", TaskCycleType.day, "value", "以天为周期"));
        cycleTypes.add(MapUtils.m("key", TaskCycleType.week, "value", "以周为周期"));
        cycleTypes.add(MapUtils.m("key", TaskCycleType.month, "value", "以月为周期"));
        cycleTypes.add(MapUtils.m("key", TaskCycleType.fixed, "value", "固定的开始截止日期"));
        model.addAttribute("types", cycleTypes);

        List<Map<String, Object>> sources = new ArrayList<>();
        Field[] fields = TaskSource.class.getDeclaredFields();
        for (Field field : fields) {
            if (field.isEnumConstant() && field.getAnnotation(Deprecated.class) == null
                    && !StringUtils.equals(field.getName(), "unknown")) {
                TaskSource source = TaskSource.safeParse(field.getName());
                sources.add(MapUtils.map("key", field.getName(), "value", source.getDescription()));
            }
        }
        model.addAttribute("sources", sources);

        return "/equator/mission/task/upsertpage";
    }

    @RequestMapping(value = "loadtasktemplatebytemplateid.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage loadTaskTemplateByTemplateId() {
        String templateId = getRequestString("templateId");
        if (StringUtils.isEmpty(templateId)) return MapMessage.errorMessage();

        TaskTemplate template = client.getService().loadEntityFromDatabase(templateId).getUninterruptibly();
        return MapMessage.successMessage().add("template", template);
    }

    @RequestMapping(value = "upserttasktemplate.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage upsertTaskTemplate() {
        try {
            Map<String, Object> template = JsonUtils.fromJson(getRequestString("template"));
            String id = SafeConverter.toString(template.get("id"));

            TaskTemplate entity = new TaskTemplate();
            if (StringUtils.isNotBlank(id)) entity.setId(id); // 更新

            entity.setTitle(SafeConverter.toString(template.get("title")));
            entity.setSubtitle(SafeConverter.toString(template.get("subtitle")));
            entity.setLable(SafeConverter.toString(template.get("lable")));
            entity.setDesc(SafeConverter.toString(template.get("desc")));

            Mode env = Mode.getMode(SafeConverter.toString(template.get("env")));
            if (env == null) return MapMessage.errorMessage("Env error.");
            entity.setEnv(env);

            Date from = DateUtils.stringToDate(SafeConverter.toString(template.get("from")));
            Date to = DateUtils.stringToDate(SafeConverter.toString(template.get("to")));
            if (to.before(from)) return MapMessage.errorMessage("From should before to.");
            entity.setFrom(from);
            entity.setTo(to);

            try {
                MissionTemplateStatus status = MissionTemplateStatus.valueOf(SafeConverter.toString(template.get("status")));
                entity.setStatus(status);
            } catch (IllegalArgumentException e) {
                return MapMessage.errorMessage("MissionTemplateStatus error.");
            }

            entity.setSubject(Subject.ofWithUnknown(SafeConverter.toString(template.get("subject"))));
            entity.setRank(SafeConverter.toInt(template.get("rank")));

            TaskSource source = TaskSource.safeParse(SafeConverter.toString(template.get("source")));
            if (source == TaskSource.unknown) return MapMessage.errorMessage("TaskSource error.");
            entity.setSource(source);

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

            TaskCycleType cycleType = TaskCycleType.safeParse(SafeConverter.toString(template.get("cycleType")));
            String startTime = SafeConverter.toString(template.get("startTime"));
            String endTime = SafeConverter.toString(template.get("endTime"));
            TaskCycle cycle = new TaskCycle(cycleType, startTime, endTime);
            if (!cycle.isValid()) return MapMessage.errorMessage("任务时间错误");

            entity.setTaskCycle(cycle);

            return client.getService().upsertEntity(entity);
        } catch (Exception e) {
            return MapMessage.errorMessage("操作失败");
        }
    }

    @RequestMapping(value = "removetasktemplate.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage removeTaskTemplate() {
        String templateId = getRequestString("templateId");
        if (StringUtils.isEmpty(templateId)) return MapMessage.errorMessage("模板id不能为空");

        return client.getService().removeEntity(templateId);
    }
}
