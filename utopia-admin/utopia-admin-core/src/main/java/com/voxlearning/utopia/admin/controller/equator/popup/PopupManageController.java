package com.voxlearning.utopia.admin.controller.equator.popup;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.equator.common.api.enums.popup.PopupRange;
import com.voxlearning.equator.service.configuration.api.entity.popup.GlobalPopup;
import com.voxlearning.equator.service.configuration.client.GlobalPopupConfigServiceClient;
import com.voxlearning.utopia.admin.controller.equator.AbstractEquatorController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Ruib
 * @since 2018/7/31
 */
@Slf4j
@Controller
@RequestMapping("/equator/popup/")
public class PopupManageController extends AbstractEquatorController {
    @Inject private GlobalPopupConfigServiceClient client;

    @RequestMapping(value = "index.vpage", method = {RequestMethod.GET})
    public String index(Model model) {
        // 获取PopupRange
        List<Map<String, Object>> ranges = new ArrayList<>();
        Field[] fields = PopupRange.class.getDeclaredFields();
        for (Field field : fields) {
            if (field.isEnumConstant() && field.getAnnotation(Deprecated.class) == null
                    && !StringUtils.equals(field.getName(), "unknown")) {
                PopupRange source = PopupRange.safeParse(field.getName());
                ranges.add(MapUtils.map("key", field.getName(), "value", source.getDesc()));
            }
        }
        model.addAttribute("ranges", ranges);

        List<Map<String, Object>> types = new ArrayList<>();
        types.add(MapUtils.m("key", 0, "value", "图片类弹窗"));
        types.add(MapUtils.m("key", 1, "value", "文本类弹窗"));
        model.addAttribute("types", types);

        List<Map<String, Object>> cycles = new ArrayList<>();
        cycles.add(MapUtils.m("key", 0, "value", "一辈子一次"));
        cycles.add(MapUtils.m("key", 1, "value", "一天一次"));
        cycles.add(MapUtils.m("key", 2, "value", "每次登陆一次"));
        model.addAttribute("cycles", cycles);

        return "/equator/popup/index";
    }

    // 加载所有弹窗
    @RequestMapping(value = "load.vpage", method = {RequestMethod.POST})
    @ResponseBody
    @SuppressWarnings("unchecked")
    public MapMessage load() {
        List<GlobalPopup> popups = client.getRemoteReference().loadEntityFromDatabase().getUninterruptibly();
        Collections.sort(popups, ((o1, o2) -> Long.compare(o2.getCreateTime().getTime(), o1.getCreateTime().getTime())));
        return MapMessage.successMessage().add("popups", popups);
    }

    // 加载指定弹窗
    @RequestMapping(value = "loadbyid.vpage", method = {RequestMethod.POST})
    @ResponseBody
    @SuppressWarnings("unchecked")
    public MapMessage load_id() {
        String id = getRequestString("id");
        if (StringUtils.isBlank(id)) return MapMessage.errorMessage();

        GlobalPopup popup = client.getRemoteReference().loadEntityFromDatabase().getUninterruptibly()
                .stream().filter(p -> StringUtils.equals(p.getId(), id)).findFirst().orElse(null);

        return (null == popup) ? MapMessage.errorMessage() : MapMessage.successMessage().add("popup", popup);
    }

    // 添加或者更新弹窗
    @RequestMapping(value = "upsert.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage upsertPopup() {
        Map<String, Object> map = JsonUtils.fromJson(getRequestString("popup"));
        if (MapUtils.isEmpty(map)) return MapMessage.errorMessage();

        GlobalPopup popup = new GlobalPopup();

        String id = SafeConverter.toString(map.get("id"));
        if (StringUtils.isNotBlank(id)) {
            popup.setId(id);
        }

        popup.setRange(SafeConverter.toString(map.get("range")));
        popup.setDescription(SafeConverter.toString(map.get("description")));
        popup.setUrl(SafeConverter.toString(map.get("url")));
        popup.setContent(SafeConverter.toString(map.get("content")));
        int type = SafeConverter.toInt(map.get("type"));
        if (!Arrays.asList(0, 1).contains(type)) return MapMessage.errorMessage("弹窗类型错误");
        popup.setType(type);
        int cycle = SafeConverter.toInt(map.get("cycle"));
        if (!Arrays.asList(0, 1, 2).contains(type)) return MapMessage.errorMessage("弹窗周期错误");
        popup.setCycle(cycle);
        popup.setRank(SafeConverter.toInt(map.get("rank")));
        Date startDatetime = DateUtils.stringToDate(SafeConverter.toString(map.get("startDatetime"), ""));
        if (startDatetime == null) return MapMessage.errorMessage("开始时间错误");
        popup.setStartDatetime(startDatetime);
        Date endDatetime = DateUtils.stringToDate(SafeConverter.toString(map.get("endDatetime"), ""));
        if (endDatetime == null) return MapMessage.errorMessage("结束时间错误");
        popup.setEndDatetime(endDatetime);
        popup.setExtension(SafeConverter.toString(map.get("extension")));

        MapMessage mesg = client.getRemoteReference().upsertEntity(popup);
        if (mesg.isSuccess()) mesg.add("popup", popup);
        return mesg;
    }

    // 删除弹窗
    @RequestMapping(value = "delete.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage delete() {
        String id = getRequestString("id");
        if (StringUtils.isBlank(id)) return MapMessage.errorMessage("删除失败");
        return client.getRemoteReference().removeEntity(id);
    }
}
