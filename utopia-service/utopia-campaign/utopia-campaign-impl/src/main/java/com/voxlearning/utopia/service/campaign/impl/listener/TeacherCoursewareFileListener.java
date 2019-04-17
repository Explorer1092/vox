package com.voxlearning.utopia.service.campaign.impl.listener;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.spi.queue.*;
import com.voxlearning.utopia.service.campaign.api.entity.TeacherCourseware;
import com.voxlearning.utopia.service.campaign.impl.dao.TeacherCoursewareDao;
import com.voxlearning.utopia.service.campaign.impl.support.POIPowerPointHelper;
import com.voxlearning.utopia.service.campaign.impl.support.POIWordHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Inject;
import javax.inject.Named;
import java.awt.*;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Named
@QueueConsumer(
        destinations = {
                @QueueDestination(system = QueueSystem.KFK, config = "primary", queue = "utopia.campaign.teacher.courseware.file.exchange"),
                @QueueDestination(system = QueueSystem.KFK, config = "main-backup", queue = "utopia.campaign.teacher.courseware.file.exchange")
        },
        maxPermits = 4
)
@Slf4j
public class TeacherCoursewareFileListener implements MessageListener, InitializingBean {
    @Inject
    private TeacherCoursewareDao teacherCoursewareDao;

    @Inject
    private POIPowerPointHelper pOIPowerPointHelper;

    @Inject
    private POIWordHelper poiWordHelper;

    private static GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

    @Override
    public void afterPropertiesSet() throws Exception {
        try(InputStream fontFile = TeacherCoursewareExaminingExpireListener.class.getClassLoader().getResourceAsStream("fonts/simsun.ttf")) {
            Font dynamicFont = Font.createFont(Font.TRUETYPE_FONT, fontFile);
            ge.registerFont(dynamicFont);
        } catch (Exception e) {
            log.error("TeacherCoursewareFileListener load font error.", e);
        }
    }

    @Override
    public void onMessage(Message message) {
        Object body = message.decodeBody();
        if (body == null || !(body instanceof String)) {
            return;
        }
        String json = (String) body;
        Map<String, Object> param = JsonUtils.fromJson(json);
        if (param == null || param.isEmpty()) {
            return;
        }

        String id = SafeConverter.toString(param.get("CID"));
        if (StringUtils.isBlank(id)) {
            return;
        }

        TeacherCourseware courseware = teacherCoursewareDao.load(id);
        if (courseware == null || StringUtils.isBlank(courseware.getCoursewareFile()) || !(courseware.getCoursewareFile().contains("ppt"))) {
            return;
        }

//        String coursewareFile = courseware.getCoursewareFile();

//        List<String> images = null;
//        if (coursewareFile.contains("pptx")){
//            images = pOIPowerPointHelper.converPPTXtoImage(coursewareFile,"png");
//        } else if (coursewareFile.contains("ppt")) {
//            images = pOIPowerPointHelper.converPPTtoImage(coursewareFile, "png");
//        }
        // todo word 待处理
//        else if (coursewareFile.contains("doc")){
//            images = poiWordHelper.convertDoc2Pdf(coursewareFile,"pdf");
//        } else if (coursewareFile.contains("docx")){
//            images = poiWordHelper.convertDocx2Pdf(coursewareFile,"pdf");
//        }

//        if (CollectionUtils.isNotEmpty(images))
//            teacherCoursewareDao.updateFilePreview(id, images);
        // todo word 待处理
//        if (CollectionUtils.isNotEmpty(images) && coursewareFile.contains("doc"))
//            teacherCoursewareDao.updateWordPreview(id,images);
    }

}
