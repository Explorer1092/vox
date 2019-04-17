package com.voxlearning.utopia.agent.mockexam.middleschool.controller;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.StringUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.mockexam.controller.ViewBuilder;
import com.voxlearning.utopia.agent.mockexam.controller.view.ExamPaperListView;
import com.voxlearning.utopia.agent.mockexam.middleschool.service.MiddleSchoolExamPaperService;
import com.voxlearning.utopia.agent.mockexam.middleschool.service.dto.input.MiddleSchoolExamPaperPageQueryParams;
import com.voxlearning.utopia.agent.mockexam.middleschool.service.dto.output.MiddleSchoolBookDto;
import com.voxlearning.utopia.agent.mockexam.middleschool.service.dto.output.MiddleSchoolExamPaperDto;
import com.voxlearning.utopia.agent.mockexam.service.dto.PageInfo;
import com.voxlearning.utopia.agent.mockexam.service.dto.PageResult;
import com.voxlearning.utopia.agent.service.AgentApiAuth;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;

import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.agent.mockexam.controller.ViewBuilder.KEY_DATA;
import static com.voxlearning.utopia.agent.mockexam.controller.ViewBuilder.KEY_ERROR_DESC;

/**
 * @description: 中学试卷管理
 * @author: kaibo.he
 * @create: 2019-03-18 14:57
 **/
@Slf4j
@Controller
@RequestMapping("middleschool/mockexam/paper")
public class MiddleSchoolExamPaperController extends AbstractAgentController{

    @Inject
    private MiddleSchoolExamPaperService middleSchoolExamPaperService;
    /**
     * 跳转到查询页
     *
     * @param model
     * @return
     */
    @RequestMapping(value = "forquery.vpage")
    public String forQuery(Model model) {
        return "/mockexam/middleschoolpaper/list";
    }

    /**
     * 试卷分页查询
     *
     * @param params 参数
     * @return 结果
     */
    @ResponseBody
    @RequestMapping(value = "querypage.vpage", method = RequestMethod.POST)
    public MapMessage queryPage(@RequestBody MiddleSchoolExamPaperPageQueryParams params) {
        PageInfo pageInfo = new PageInfo();
        pageInfo.setPage(params.getPage() == null ? 1 : params.getPage());
        pageInfo.setSize(params.getSize());

        MapMessage message;

        PageResult<MiddleSchoolExamPaperDto> result = middleSchoolExamPaperService.queryPage(params, pageInfo);
        if (result.isSuccess()) {
            message = ViewBuilder.fetch(result);
            message.set("size", result.getSize());
            message.set("page", result.getPage());
            message.set("totalSize", result.getTotalSize());
            message.set(KEY_DATA, result.getData());
            setPaperPreviewUrl(result.getData());
        } else {
            message = new MapMessage();
            message.setErrorCode(result.getErrorCode());
            message.setInfo(result.getErrorMessage());
            message.set("page", result.getPage());
            message.set("size", result.getSize());
            message.set(KEY_ERROR_DESC, result.getErrorMessage());
        }
        return message;
    }

    /**
     * 设置试卷预览Url
     * @param dtos 列表
     */
    private void setPaperPreviewUrl(List<MiddleSchoolExamPaperDto> dtos){
        if(CollectionUtils.isEmpty(dtos)){
            return;
        }
        final String secretKey;
        final String paperDomain;
        if(RuntimeMode.lt(Mode.STAGING)){
            secretKey = AgentApiAuth.PLATFORM_SECRET_KEY_TEST;
            paperDomain = "http://www.test.17zuoye.net";
        }else {
            secretKey = AgentApiAuth.PLATFORM_SECRET_KEY;
            if(RuntimeMode.current() == Mode.STAGING){
                paperDomain = "http://www.staging.17zuoye.net";
            }else {
                paperDomain = "https://www.17zuoye.com";
            }
        }
        dtos.forEach(p -> {
            Map<String, String> paramMap = new HashMap<>();
            paramMap.put("paperIds", p.getId());
            String sig = AgentApiAuth.generateAppKeySig(paramMap, secretKey);
            p.setPaperPreviewUrl(paperDomain + "/newexamv2/previewpaper.vpage?paperIds=" + p.getId()
                    + "&sig=" + sig + "&app_key=" + AgentApiAuth.APP_KEY);
        });

    }

    /**
     * 中学教材查询
     *
     * @return 结果
     */
    @ResponseBody
    @RequestMapping(value = "books.vpage", method = RequestMethod.POST)
    public MapMessage querybooks(@RequestBody Map<String, String> p) {
        PageResult<MiddleSchoolBookDto> result = middleSchoolExamPaperService.queryBooks();
        List<MiddleSchoolBookDto> dtos = Optional.ofNullable(result.getData()).orElse(new ArrayList<>())
                .stream()
                .filter(dto -> StringUtils.isBlank(p.get("p")) || dto.getName().contains(p.get("p")))
                .collect(Collectors.toList());
        return ViewBuilder.fetch(PageResult.success((ArrayList) dtos, null, 0));
    }
}
