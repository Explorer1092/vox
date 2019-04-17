package com.voxlearning.utopia.agent.mockexam.service;

import com.voxlearning.utopia.agent.mockexam.service.dto.Result;
import com.voxlearning.utopia.agent.mockexam.service.dto.input.ExamBookQueryParams;
import com.voxlearning.utopia.agent.mockexam.service.dto.input.ExamReferSchoolQueryParams;
import com.voxlearning.utopia.agent.mockexam.service.dto.output.BookDto;
import com.voxlearning.utopia.agent.mockexam.service.dto.output.ExamSchoolDto;
import com.voxlearning.utopia.agent.mockexam.service.dto.output.RegionDto;
import com.voxlearning.utopia.agent.mockexam.service.dto.output.ReportUrlDto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 应用类型服务
 *
 * @author xiaolei.li
 * @version 2018/8/13
 */
public interface ExamReferenceService {

    /**
     * 获取管辖的城市
     *
     * @param userId 用户id
     * @return 结果
     */
    Result<ArrayList<RegionDto>> queryProvince(long userId);

    /**
     * 获取管辖的城市
     *
     * @param userId 用户id
     * @return 结果
     */
    Result<ArrayList<RegionDto>> queryCity(long userId, Collection<Integer> provinceCodes);

    /**
     * 获取管辖的区
     *
     * @param userId    用户id
     * @param cityCodes 城市编码
     * @return 区域列表
     */
    Result<ArrayList<RegionDto>> queryCounty(long userId, List<Integer> cityCodes);

    /**
     * 根据城市列表、学校列表查询学校合法性，给出学校列表
     *
     * @param params 参数
     * @return 结果
     */
    Result<ArrayList<ExamSchoolDto>> querySchool(ExamReferSchoolQueryParams params);

    /**
     * 教材查询
     *
     * @param params 参数
     * @return 教材列表
     */
    Result<ArrayList<BookDto>> queryBook(ExamBookQueryParams params);

    /**
     * 返回报告列表
     *
     * @param regionCode 区域编码
     * @param planId     测评id
     * @return url
     */
    Result<ReportUrlDto> reportUrl(String regionCode, Long planId);
}
