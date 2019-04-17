package com.voxlearning.utopia.agent.service.partner.outerfetch.dto;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.agent.service.AgentApiAuth;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @description: 联系人数据传输对象
 * @author: kaibo.he
 * @create: 2019-04-02 20:00
 **/
public interface LinkManHttpDto {

    @lombok.Data
    class ReqeustDto {
        private String appKey;
        private String userIds;
        private String sig;

        public static class Builder {
            public static ReqeustDto build(List<Long> userIds) {
                StringBuilder userIdsStr = new StringBuilder();
                if (CollectionUtils.isNotEmpty(userIds)) {
                    for (int i=0; i<userIds.size(); i++) {
                        userIdsStr.append(userIds.get(i));
                        if (i < userIds.size()-1) {
                            userIdsStr.append(",");
                        }
                    }
                }

                Map<String, String> dataMap = new HashMap<>();
                dataMap.put("app_key", "HoneyComb");
                dataMap.put("userIds", userIdsStr.toString());
                String sig = AgentApiAuth.generateAppKeySig(dataMap, getHoneycombSecretKey());

                ReqeustDto dto = new ReqeustDto();
                dto.setAppKey("HoneyComb");
                dto.setUserIds(userIdsStr.toString());
                dto.setSig(sig);
                return dto;
            }

            public static Map<Object, Object> build(ReqeustDto dto) {
                Map<Object, Object> params = new HashMap<>();
                params.put("userIds", dto.getUserIds());
                params.put("sig", dto.getSig());
                params.put("app_key", dto.getAppKey());
                return params;
            }
        }
    }

    static String getHoneycombSecretKey(){
        if(RuntimeMode.lt(Mode.STAGING)){
            return "ASDF78XF";
        }else {
            return "0zns1UZTljhH";
        }
    }

    @lombok.Data
    class ResponseDto {
        private boolean success;
        private String data;                    //json数据

        /**
         * 返回数据，用蜂巢id作为map的key...不转换用map当做业务对象很烦，转换也挺烦
         * @return
         */
        public Map<String, List<Item>> fetchDataMap() {
            Map<String, List<Item>> map = new HashMap<>();
            Map<String, Object> objectMap = JsonUtils.fromJson(data);
            List<Item> items = new ArrayList<>();
            objectMap.forEach((k, v) -> {
                List<Item> temp = Optional.ofNullable((List<Map<String, Object>>)v).orElse(new ArrayList<>())
                        .stream()
                        .map(m -> JsonUtils.safeConvertMapToObject(m, Item.class))
                        .collect(Collectors.toList());
                items.addAll(temp);
                map.put(k, items);
            });
            return map;
        }
    }

    @lombok.Data
    class Item {
        private Long id;
        private String nickName;
        private String bindTime;
        private String mobile;
        private String headPortrait;
    }
}
