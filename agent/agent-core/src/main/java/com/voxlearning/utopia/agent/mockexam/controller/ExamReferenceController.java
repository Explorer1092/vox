package com.voxlearning.utopia.agent.mockexam.controller;

import com.google.common.collect.Lists;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.annotation.OperationCode;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.mockexam.service.ExamReferenceService;
import com.voxlearning.utopia.agent.mockexam.service.dto.Result;
import com.voxlearning.utopia.agent.mockexam.service.dto.input.ExamBookQueryParams;
import com.voxlearning.utopia.agent.mockexam.service.dto.input.ExamReferSchoolQueryParams;
import com.voxlearning.utopia.agent.mockexam.service.dto.output.BookDto;
import com.voxlearning.utopia.agent.mockexam.service.dto.output.ExamSchoolDto;
import com.voxlearning.utopia.agent.mockexam.service.dto.output.RegionDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.agent.mockexam.controller.ResourceCode.Operation.*;

/**
 * 引用类型rest服务
 *
 * @author xiaolei.li
 * @version 2018/8/14
 */
@Slf4j
@Controller
@RequestMapping("mockexam/refer")
public class ExamReferenceController extends AbstractAgentController {

    @Resource
    ExamReferenceService referenceService;

    /**
     * 根据城市code获取区县
     */
    @OperationCode(REFER_REGION)
    @ResponseBody
    @RequestMapping(value = "region/cities.vpage", method = RequestMethod.GET)
    public MapMessage queryCity(
            @RequestParam(name = "provinceCode") int provinceCode) {
        final Long userId = getCurrentUser().getUserId();
        Result<ArrayList<RegionDto>> result = referenceService.queryCity(userId, Lists.newArrayList(provinceCode));
        return ViewBuilder.fetch(result);
    }

    /**
     * 根据城市code获取区县
     */
    @OperationCode(REFER_REGION)
    @ResponseBody
    @RequestMapping(value = "region/counties.vpage", method = RequestMethod.GET)
    public MapMessage queryCounty(
            @RequestParam(name = "cityCode") int cityCode) {
        final Long userId = getCurrentUser().getUserId();
        Result<ArrayList<RegionDto>> result = referenceService.queryCounty(userId, Lists.newArrayList(cityCode));
        return ViewBuilder.fetch(result);
    }

    /**
     * 查询学校
     *
     * @param cityCodes 城市编码
     * @param schoolIds 学校编码
     * @return 学校列表
     */
    @OperationCode(REFER_SCHOOL)
    @RequestMapping(value = "schools.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage querySchool(
            @RequestParam(name = "cityCodes") String cityCodes,
            @RequestParam(name = "schoolIds") String schoolIds) {
        List<Integer> cityCodeList = Arrays.stream(cityCodes.split(","))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .map(Integer::valueOf)
                .collect(Collectors.toList());
        List<Long> schoolIdList = Arrays.stream(schoolIds.split(","))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .map(Long::valueOf)
                .collect(Collectors.toList());
        AuthCurrentUser user = getCurrentUser();
        ExamReferSchoolQueryParams params = new ExamReferSchoolQueryParams();
        params.setUserId(user.getUserId());
        params.setCityCodes(cityCodeList);
        params.setSchoolIds(schoolIdList);
        Result<ArrayList<ExamSchoolDto>> result = referenceService.querySchool(params);
        return ViewBuilder.fetch(result);
    }

    /**
     * 查询教材
     *
     * @param params 参数
     * @return 教材列表
     */
    @OperationCode(REFER_BOOK)
    @RequestMapping(value = "books.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage queryBook(@RequestBody ExamBookQueryParams params) {
        Result<ArrayList<BookDto>> result = referenceService.queryBook(params);
        return ViewBuilder.fetch(result);
    }

}
