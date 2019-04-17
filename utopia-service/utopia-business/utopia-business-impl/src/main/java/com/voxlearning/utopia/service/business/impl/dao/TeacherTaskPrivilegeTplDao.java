package com.voxlearning.utopia.service.business.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.entity.task.TeacherTaskPrivilegeTpl;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Named;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Named
@CacheBean(type = TeacherTaskPrivilegeTpl.class)
@Slf4j
public class TeacherTaskPrivilegeTplDao extends AlpsStaticMongoDao<TeacherTaskPrivilegeTpl,Long>{

    private final String jsonOnline;

    private final String jsonStaging;

    public TeacherTaskPrivilegeTplDao() {
        jsonOnline = readToString( "/config/teacher_task_privilege_tpl.json");
        jsonStaging = readToString( "/config/teacher_task_privilege_tpl_staging.json");
    }

    private String readToString(String fileName) {
        try {
            @Cleanup InputStream is = getClass().getResourceAsStream(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("TeacherTaskPrivilegeTpl failed to load {}", fileName, e);
            return null;
        }
    }

    @Override
    protected void calculateCacheDimensions(TeacherTaskPrivilegeTpl document, Collection<String> dimensions) {
        dimensions.addAll(Arrays.asList(document.generateCacheDimensions()));
    }

    public List<TeacherTaskPrivilegeTpl> loadAll(){
        if (RuntimeMode.isProduction() || RuntimeMode.isStaging()) {
            return JsonUtils.fromJsonToList(jsonOnline, TeacherTaskPrivilegeTpl.class);
        } else {
            return JsonUtils.fromJsonToList(jsonStaging, TeacherTaskPrivilegeTpl.class);
        }
    }

}
