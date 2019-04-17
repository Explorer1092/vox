package com.voxlearning.utopia.service.parent.homework.impl.template.base.correct;

import com.google.common.collect.Maps;
import com.voxlearning.alps.api.context.ApplicationContextScanner;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.core.utils.LoggerUtils;
import com.voxlearning.utopia.core.utils.ObjectUtils;
import com.voxlearning.utopia.service.parent.homework.api.model.Command;
import com.voxlearning.utopia.service.parent.homework.api.model.CorrectParam;
import com.voxlearning.utopia.service.parent.homework.api.model.DoType;
import com.voxlearning.utopia.service.parent.homework.impl.template.base.AbstractTemplate;
import com.voxlearning.utopia.service.parent.homework.impl.template.base.ITemplate;
import com.voxlearning.utopia.service.parent.homework.impl.template.base.SupportCommand;
import com.voxlearning.utopia.service.parent.homework.impl.template.base.SupportDoType;
import lombok.extern.slf4j.Slf4j;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 订正基础模板
 *
 * @author Wenlong Meng
 * @since Mar 18, 2019
 */
@Slf4j
@SupportDoType(DoType.CORRECT)
public class CorrectBaseTemplate extends AbstractTemplate<CorrectContext> {

    private static EnumMap<Command,ITemplate<CorrectContext, MapMessage>> templates = Maps.newEnumMap(Command.class);

    /**
     * 执行
     *
     * @param cp
     */
    public static MapMessage exec(CorrectParam cp){
        try {
            LoggerUtils.debug("begin", cp);
            CorrectContext c = new CorrectContext();
            c.setParam(cp);

            MapMessage result = get(cp.getCommand()).process(c);
            LoggerUtils.debug("end", result);
            return result;
        }catch (Exception e){
            log.error("{}", JsonUtils.toJson(cp), e);
            return MapMessage.errorMessage();
        }

    }

    /**
     * 获取作业流程模板
     *
     * @param command
     */
    protected static ITemplate<CorrectContext, MapMessage> get(Command command) {
        if(templates.isEmpty()){
            ApplicationContextScanner.getInstance().getBeansOfType(CorrectBaseTemplate.class).values().forEach(b->{
                SupportCommand supportCommand = b.getClass().getAnnotation(SupportCommand.class);
                if(supportCommand != null){
                    log.info("init template: {} = {}", supportCommand.value(), b.getClass());
                    ITemplate<CorrectContext, MapMessage> ctemplate = templates.get(supportCommand.value());
                    if(ctemplate == null){
                        templates.put(supportCommand.value(), b);
                    }else{
                        log.error("duplicate template: {}", ctemplate);
                    }
                }
            });
        }
        return templates.get(command);
    }

    /**
     * 生成url
     *
     * @param objectiveConfigType
     * @param command
     * @param homeworkId
     * @param studentId
     * @return
     */
    public String url(String objectiveConfigType, Command command, String homeworkId, Long studentId, String homeworkResultId){
        return UrlUtils.buildUrlQuery("/parent/homework/correct/index.api",
                MapUtils.m("command",command, "objectiveConfigType", objectiveConfigType, "homeworkId", homeworkId,
                        "homeworkResultId",homeworkResultId,"studentId", studentId));
    }

    /**
     * 生成url
     *
     * @param objectiveConfigType
     * @param command
     * @param homeworkId
     * @param studentId
     * @return
     */
    public String url(String objectiveConfigType, Command command, String homeworkId, Long studentId, String homeworkResultId,String courseId){
        return UrlUtils.buildUrlQuery("/parent/homework/correct/index.api",
                MapUtils.m("command",command, "objectiveConfigType", objectiveConfigType, "homeworkId", homeworkId,
                        "homeworkResultId",homeworkResultId,"studentId", studentId, "courseId", courseId));
    }

}
