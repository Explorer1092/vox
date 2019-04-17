package com.voxlearning.utopia.admin.controller.equator.generalconfig;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.equator.service.configuration.api.entity.generalconfig.GeneralConfig;
import com.voxlearning.equator.service.configuration.client.GeneralConfigServiceClient;
import com.voxlearning.utopia.admin.controller.equator.AbstractEquatorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author fugui.chang
 * @since 2018/6/20.
 */
@Controller
@RequestMapping(value = "equator/config/generalconfig/manage")
public class GeneralConfigManagerController extends AbstractEquatorController {
    @Inject
    private GeneralConfigServiceClient generalConfigServiceClient;

    @RequestMapping(value = "index.vpage", method = {RequestMethod.GET})
    public String index(Model model) {
        String generalConfigKey = getRequestString("generalConfigKey");

        boolean byKey = StringUtils.equals(getRequestString("selectType"), "byKey");

        Pageable pageable = buildPageRequest(5);
        Page<GeneralConfig> generalConfigList = generalConfigServiceClient.getGeneralConfigService()
                .loadAllGeneralConfigs(pageable, byKey ? generalConfigKey : null, byKey ? null : generalConfigKey)
                .getUninterruptibly();
        model.addAttribute("generalConfigList", generalConfigList);

        model.addAttribute("generalConfigKey", generalConfigKey);
        return "equator/config/generalconfig/manage/index";
    }


    @RequestMapping(value = "editconfig.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage editConfig() {
        String id = getRequestString("id").trim();
        String key = getRequestString("key").trim();
        String value = getRequestString("value").trim();
        String desc = getRequestString("desc").trim();
        String beginTimeStr = getRequestString("beginTime").trim();
        String endTimeStr = getRequestString("endTime").trim();
        boolean isInsert = getRequestBool("isInsert");


        if (StringUtils.isBlank(key)) {
            return MapMessage.errorMessage("key不能为空");
        }
        if (StringUtils.isBlank(beginTimeStr)) {
            beginTimeStr = DateUtils.dateToString(DayRange.current().getStartDate());
        }
        if (StringUtils.isBlank(endTimeStr)) {
            endTimeStr = "2029-12-31 23:59:59";
        }
        Date beginTime = DateUtils.stringToDate(beginTimeStr);
        Date endTime = DateUtils.stringToDate(endTimeStr);


        if (beginTime == null || endTime == null) {
            return MapMessage.errorMessage("生效时间的格式有误");
        }
        if (endTime.before(beginTime)) {
            return MapMessage.errorMessage("生效时间开始和结束顺序有误");
        }


        List<GeneralConfig> commKeyConfigList = generalConfigServiceClient.getGeneralConfigService()
                .loadGeneralConfigsByKeyFromDb(key).getUninterruptibly();
        if (isInsert) {//新增数据
            if (StringUtils.isNotBlank(id)) {
                return MapMessage.errorMessage("id=" + id + "存在,不能新增");
            }
            GeneralConfig generalConfig = new GeneralConfig(key, value, desc, beginTime, endTime);
            //校验配置 commKeyConfigList
            if (CollectionUtils.isNotEmpty(commKeyConfigList)) {
                commKeyConfigList.add(generalConfig);
                if (isTimeCross(commKeyConfigList)) {
                    return MapMessage.errorMessage("相同key的配置时间不能交叉");
                }
            }
            return generalConfigServiceClient.getGeneralConfigService()
                    .insertGeneralConfig(generalConfig).getUninterruptibly();

        } else {//更新数据
            GeneralConfig generalConfig = generalConfigServiceClient.getGeneralConfigService().loadGeneralConfigFromDb(id).getUninterruptibly();
            if (generalConfig == null) {
                return MapMessage.errorMessage("id=" + id + "不存在,不能更新");
            }
            generalConfig.setKey(key);
            generalConfig.setValue(value);
            generalConfig.setDesc(desc);
            generalConfig.setBeginTime(beginTime);
            generalConfig.setEndTime(endTime);

            //校验配置 commKeyConfigList
            commKeyConfigList = commKeyConfigList.stream().filter(g -> !StringUtils.equals(g.getId(), id)).collect(Collectors.toList());
            commKeyConfigList.add(generalConfig);
            if (isTimeCross(commKeyConfigList)) {
                return MapMessage.errorMessage("相同key的配置时间不能交叉");
            }

            return generalConfigServiceClient.getGeneralConfigService()
                    .replaceGeneralConfig(generalConfig)
                    .getUninterruptibly();
        }
    }

    //判断时间是否有交叉
    private boolean isTimeCross(List<GeneralConfig> commKeyConfigList) {
        if (commKeyConfigList == null || commKeyConfigList.size() == 1) {
            return false;
        }

        if (commKeyConfigList.stream().filter(GeneralConfig::hasBeenBetweenTime).count() > 1) {
            return true;
        }


        commKeyConfigList = commKeyConfigList.stream()
                .filter(g -> g.getBeginTime() != null && g.getEndTime() != null)
                .sorted(Comparator.comparing(GeneralConfig::getBeginTime))
                .collect(Collectors.toList());
        GeneralConfig previousGeneralConfig = commKeyConfigList.get(0);//上一条数据

        for (int index = 1; index < commKeyConfigList.size(); index++) {
            GeneralConfig generalConfig = commKeyConfigList.get(index);
            if (generalConfig.getBeginTime().before(previousGeneralConfig.getEndTime())) {
                return true;
            }
            previousGeneralConfig = generalConfig;
        }

        return false;
    }

}
