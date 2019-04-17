package com.voxlearning.utopia.service.mizar.api.utils;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.mizar.api.constants.MizarNotifyTemplate;
import com.voxlearning.utopia.service.mizar.api.constants.MizarUserRoleType;
import com.voxlearning.utopia.service.mizar.api.entity.notify.MizarNotify;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Mizar生成消息的工具类
 * Created by Yuechen.Wang on 2016/12/7.
 */
public class MizarNotifyPostOffice {


    /**
     * 根据模板生成消息
     *
     * @param template 消息模板
     * @param params   参数列表，自己根据业务填充，暂不做校验
     * @return 生成的消息
     */
    public static MizarNotify writeNotify(MizarNotifyTemplate template, Map<String, String> params) {
        String content = template.getContent();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String token = "{#param}".replace("param", entry.getKey());
            if (content.contains(token)) {
                content = content.replace(token, entry.getValue());
            }
        }

        String url = StringUtils.formatMessage(template.getUrl(), params.get("id"));

        MizarNotify notify = new MizarNotify();
        notify.setType(template.getType().name());
        notify.setTitle(template.getTitle());
        notify.setContent(content);
        notify.setUrl(url);
        notify.setCreator(params.get("user"));
        notify.setSendTime(new Date());
        return notify;
    }

    public static void main(String[] args) {
        Map<String, String> params = new HashMap<>();
        params.put("user", "呵呵呵");
        params.put("time", DateUtils.nowToString());
        params.put("name", "测试");
        params.put("id", "sssss");
        params.put("role", MizarUserRoleType.BusinessDevelopment.getRoleName());

        String content = MizarNotifyTemplate.SHOP_CREATE.getContent();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String token = "{#param}".replace("param", entry.getKey());
            if (content.contains(token)) {
                content = content.replace(token, entry.getValue());
            }

        }
        System.out.println(content);

    }
}
