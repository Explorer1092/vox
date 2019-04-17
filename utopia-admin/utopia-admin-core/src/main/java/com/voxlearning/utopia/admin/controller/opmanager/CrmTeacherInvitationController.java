package com.voxlearning.utopia.admin.controller.opmanager;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.Maps;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.admin.controller.AbstractAdminController;
import com.voxlearning.utopia.entity.misc.TeacherInvitationConfig;
import com.voxlearning.utopia.service.invitation.client.AsyncInvitationServiceClient;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

@Controller
@RequestMapping("/opmanager/teacherinvitation")
public class CrmTeacherInvitationController extends AbstractAdminController {

    @Inject private RaikouSystem raikouSystem;
    @Inject private AsyncInvitationServiceClient asyncInvitationServiceClient;

    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index() {
        return "opmanager/teacherinvitation/teacherinvitation_index";
    }

    @RequestMapping(value = "query.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage query(Model model) {
        List<TeacherInvitationConfig> list = asyncInvitationServiceClient.getAsyncInvitationService().queryTeacherInvitationConfig().getUninterruptibly();
        return MapMessage.successMessage().add("data", list);
    }

    @RequestMapping(value = "upsert.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage upsert(@RequestBody TeacherInvitationConfig config) {
        if (config.getId() == null) {
            List<TeacherInvitationConfig> list = asyncInvitationServiceClient.getAsyncInvitationService().queryTeacherInvitationConfig().getUninterruptibly();
            if (list.contains(config)) {
                return MapMessage.errorMessage("保存失败, 城市已存在配置信息, 请直接更改");
            }
        }

        try {
            asyncInvitationServiceClient.getAsyncInvitationService().upsertTeacherInvitationConfig(config).getUninterruptibly();
            return MapMessage.successMessage();
        } catch (Exception e) {
            return MapMessage.errorMessage(e.getMessage());
        }
    }


    @RequestMapping(value = "delete.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage delete(Model model) {
        try {
            long id = getRequestLong("id");
            asyncInvitationServiceClient.getAsyncInvitationService().disableTeacherInvitationConfig(id).getUninterruptibly();
            return MapMessage.successMessage();
        } catch (Exception e) {
            return MapMessage.errorMessage();
        }
    }

    @ResponseBody
    @RequestMapping(value = "getregion.vpage", method = RequestMethod.GET)
    public MapMessage test() {
        int pcode = getRequestInt("pcode", 0);
        List<Map<String, Object>> collect = new ArrayList<>();
        if (pcode == 0) {
            List<ExRegion> exRegions = raikouSystem.getRegionBuffer().loadProvinces();
            collect = exRegions.stream().map(i -> Maps.m("code", i.getCode(), "name", i.getProvinceName())).collect(toList());
        } else {
            List<ExRegion> exRegions = raikouSystem.getRegionBuffer().loadChildRegions(pcode);
            collect = exRegions.stream().map(i -> Maps.m("code", i.getCityCode(), "name", i.getCityName())).collect(toList());
        }
        return MapMessage.successMessage().add("region", collect);
    }
}
