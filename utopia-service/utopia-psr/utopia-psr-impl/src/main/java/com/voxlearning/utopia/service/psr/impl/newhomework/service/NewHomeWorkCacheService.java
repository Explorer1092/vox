package com.voxlearning.utopia.service.psr.impl.newhomework.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.voxlearning.alps.annotation.common.ObjectCacheKeyGenerator;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.service.psr.entity.newhomework.MathQuestionProfile;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: hotallen
 * Date: 2016/8/19
 * Time: 10:24
 * To change this template use File | Settings | File Templates.
 */
@Named
public class NewHomeWorkCacheService {
    private static final String EXPOSURE_NO_PREFIX = "ENP#";
    private static final String MATH_SECTION_EXPOSURE_PREFIX = "MSE#";

    @Inject
    private NewHomeWorkCacheSystem newHomeWorkCacheSystem;

    public void increaseValue(String key) {
        if (key != null)
            newHomeWorkCacheSystem.CBS.newHomeWork.incr(key,1,1,0);
    }

    public Long getValueFromKey(String key) {
        Long value = 0L;
        String val = null;
        if (key != null)
            val = newHomeWorkCacheSystem.CBS.newHomeWork.load(key);

        if (val != null)
            value = Long.parseLong(val);
        return value;
    }

    public Map<String,Long> getValuesFromKeys(List<String> keys) {
        if (CollectionUtils.isEmpty(keys))
            return Collections.emptyMap();
        List<String> cacheKeys = Lists.newArrayList();
        keys.stream().forEach(key->cacheKeys.add(ObjectCacheKeyGenerator.generate( EXPOSURE_NO_PREFIX + key )));

        Map<String, Long> values = Maps.newHashMap();
        if (CollectionUtils.isNotEmpty(cacheKeys)) {
            Map<String, String> tmpValues = newHomeWorkCacheSystem.CBS.newHomeWork.loads(cacheKeys);
            tmpValues.entrySet().stream().forEach(e->{
                values.put(StringUtils.split(e.getKey(),"#")[1],Long.parseLong(e.getValue()));
            });
        }
        return values;
    }

    public Map.Entry<String,Long> getBiggestValue(List<String> keys) {
        Map<String, Long> values = getValuesFromKeys(keys);
        if (!values.isEmpty()){
            Map.Entry<String,Long> ret = Collections.max(values.entrySet(), Map.Entry.comparingByValue());
            return ret;
        } else {
            return null;
        }
    }

    public void put(String key, List<MathQuestionProfile> value, int expired) {
        String jsonValue = JsonUtils.toJson(value);
        if (jsonValue != null) {
            newHomeWorkCacheSystem.CBS.newHomeWork.add(ObjectCacheKeyGenerator.generate(key), expired, jsonValue);
        }
    }

    public void puts(Map<String, List<MathQuestionProfile>> values, int expired) {
        Map<String, String> strValues = Maps.newHashMap();
        values.entrySet().stream().forEach(e->{
            if (e.getValue() != null && e.getValue().size() > 0) {
                String jsonValue = JsonUtils.toJson(e.getValue());
                if (jsonValue != null) {
                    strValues.put(ObjectCacheKeyGenerator.generate(e.getKey()), jsonValue);
                }
            }
        });
        newHomeWorkCacheSystem.CBS.newHomeWork.adds(strValues,expired);
    }


    public List<MathQuestionProfile> get(String key) {
        if (StringUtils.isEmpty(key)) return Collections.emptyList();
        List<MathQuestionProfile> value = Lists.newArrayList();
        String jsonValue = newHomeWorkCacheSystem.CBS.newHomeWork.load(ObjectCacheKeyGenerator.generate(key));
        if (jsonValue != null) {
            value = JsonUtils.fromJsonToList(jsonValue, MathQuestionProfile.class);
        }
        return value;
    }

    public Map <String, List<MathQuestionProfile>> gets(List<String> keys) {
        if (CollectionUtils.isEmpty(keys)) return Collections.emptyMap();
        Map<String, List<MathQuestionProfile>> values = Maps.newHashMap();

        Map<String, String> keyMap = Maps.newHashMap();

        keys.stream().forEach(key->{
            String cacheKey = ObjectCacheKeyGenerator.generate(key);
            keyMap.put(cacheKey,key);
        });

        Map<String,String> jsonValues = newHomeWorkCacheSystem.CBS.newHomeWork.loads(keyMap.keySet());
        if (jsonValues != null && !jsonValues.isEmpty()) {
            jsonValues.entrySet().stream().forEach(e-> {
                if (e.getValue() != null) {
                    values.put(keyMap.get(e.getKey()), JsonUtils.fromJsonToList(e.getValue(), MathQuestionProfile.class));
                } else {
                    values.put(keyMap.get(e.getKey()),null);
                }
            });
            if (keyMap.keySet().size() > jsonValues.keySet().size()) {
                keyMap.keySet().stream().filter(q -> !jsonValues.keySet().contains(q)).forEach(q -> values.put(keyMap.get(q), null));
            }
        } else {
            keys.stream().forEach(e->{
                values.put(e,null);
            });
        }
        return values;
    }

    public List<String> getSectionExposureQuestions(String key) {
        List<String> value = Lists.newArrayList();
        String jsonValue = newHomeWorkCacheSystem.CBS.newHomeWork.load(ObjectCacheKeyGenerator.generate(MATH_SECTION_EXPOSURE_PREFIX+key));
        if (jsonValue != null) {
            value = JsonUtils.fromJsonToList(jsonValue, String.class);
        }
        return value;
    }

    public Map <String, List<String>> getSectionsExposureQuestions(List<String> keys) {
        Map<String, List<String>> values = Maps.newHashMap();

        Map<String, String> keyMap = Maps.newHashMap();

        keys.stream().forEach(key->{
            String cacheKey = ObjectCacheKeyGenerator.generate(MATH_SECTION_EXPOSURE_PREFIX+key);
            keyMap.put(cacheKey,key);
        });

        Map<String,String> jsonValues = newHomeWorkCacheSystem.CBS.newHomeWork.loads(keyMap.keySet());
        if (jsonValues != null && !jsonValues.isEmpty()) {
            jsonValues.entrySet().stream().forEach(e-> {
                if (e.getValue() != null) {
                    values.put(keyMap.get(e.getKey()), JsonUtils.fromJsonToList(e.getValue(), String.class));
                } else {
                    values.put(keyMap.get(e.getKey()),null);
                }
            });
            if (keyMap.keySet().size() > jsonValues.keySet().size()) {
                keyMap.keySet().stream().filter(q -> !jsonValues.keySet().contains(q)).forEach(q -> values.put(keyMap.get(q), null));
            }
        } else {
            keys.stream().forEach(e->{
                values.put(e,null);
            });
        }
        return values;
    }
}
