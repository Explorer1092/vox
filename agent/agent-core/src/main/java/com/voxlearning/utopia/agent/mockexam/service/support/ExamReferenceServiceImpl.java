package com.voxlearning.utopia.agent.mockexam.service.support;

import com.google.common.collect.Lists;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.cache.AgentCacheSystem;
import com.voxlearning.utopia.agent.mockexam.domain.exception.BusinessException;
import com.voxlearning.utopia.agent.mockexam.service.ExamReferenceService;
import com.voxlearning.utopia.agent.mockexam.service.dto.ErrorCode;
import com.voxlearning.utopia.agent.mockexam.service.dto.Result;
import com.voxlearning.utopia.agent.mockexam.service.dto.input.ExamBookQueryParams;
import com.voxlearning.utopia.agent.mockexam.service.dto.input.ExamReferSchoolQueryParams;
import com.voxlearning.utopia.agent.mockexam.service.dto.output.BookDto;
import com.voxlearning.utopia.agent.mockexam.service.dto.output.ExamSchoolDto;
import com.voxlearning.utopia.agent.mockexam.service.dto.output.RegionDto;
import com.voxlearning.utopia.agent.mockexam.service.dto.output.ReportUrlDto;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.service.content.api.NewContentLoader;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupRegion;
import com.voxlearning.utopia.service.region.api.constant.RegionType;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.user.api.entities.School;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 应用类型服务实现
 *
 * @author xiaolei.li
 * @version 2018/8/13
 */
@Service
public class ExamReferenceServiceImpl implements ExamReferenceService {

    @Inject private RaikouSystem raikouSystem;

    @Inject
    BaseOrgService orgService;

    @ImportService(interfaceClass = NewContentLoader.class)
    NewContentLoader bookClient;

    @Inject
    AgentCacheSystem cache;

    @Override
    public Result<ArrayList<RegionDto>> queryProvince(long userId) {
        try {
            List<RegionDto> result;
            // 获取用户授权信息
            AuthCurrentUser authUser = cache.getAuthCurrentUser(userId);
            List<Integer> roles = authUser.getRoleList();
            Collection<ExRegion> allRegions = raikouSystem.getRegionBuffer().loadAllRegions().values().stream()
                    .filter(i -> !i.getDisabled()).collect(Collectors.toList());
            if (roles.contains(AgentRoleType.Admin.getId()) || roles.contains(AgentRoleType.Country.getId())) {
                // 系统管理员 或者 全国总监 可以查看所有区域
                result = allRegions.stream()
                        .filter(i -> RegionType.PROVINCE == i.fetchRegionType())
                        .map(RegionDto.Builder::build)
                        .collect(Collectors.toList());
            } else {
                // 其他角色，目前至少包括：市场专员、市经理、各级代理
                // 根据所在部门的管辖区域
                List<Long> groupIds = orgService.getGroupIdListByUserId(userId);
                List<Integer> regionCodes = orgService.getGroupRegionsByGroupSet(groupIds).stream()
                        .map(AgentGroupRegion::getRegionCode)
                        .collect(Collectors.toList());

                result = raikouSystem.getRegionBuffer().loadRegions(regionCodes).values().stream()
                        .filter(i -> !i.getDisabled())
                        .flatMap(i -> {
                            switch (i.fetchRegionType()) {
                                case PROVINCE:
                                    return Stream.of(i);
                                case CITY:
                                    return Stream.of(raikouSystem.loadRegion(i.getParentRegionCode()));
                                case COUNTY:
                                    return Stream.of(raikouSystem.loadRegion(i.getProvinceCode()));
                                default:
                                    return Stream.empty();
                            }
                        })
                        .map(RegionDto.Builder::build)
                        .collect(Collectors.toList());
            }

            result = result.stream()
                    .distinct()
                    .sorted(Comparator.comparing(RegionDto::getId)).collect(Collectors.toList());
            ArrayList _result = (ArrayList) result;
            return Result.success(_result);
        } catch (Exception e) {
            return Result.error(ErrorCode.REFERENCE_REGION);
        }
    }

    @Override
    public Result<ArrayList<RegionDto>> queryCity(long userId, Collection<Integer> provinceCodes) {
        try {
            // 获取用户授权信息
            AuthCurrentUser authUser = cache.getAuthCurrentUser(userId);
            List<Integer> roles = authUser.getRoleList();

            List<RegionDto> result;

            if (roles.contains(AgentRoleType.Admin.getId()) || roles.contains(AgentRoleType.Country.getId())) {
                // 系统管理员 或者 全国总监 可以查看所有区域
                result = raikouSystem.getRegionBuffer().loadChildRegions(provinceCodes).values().stream()
                        .flatMap(List::stream)
                        .filter(i -> !i.getDisabled())
                        .map(RegionDto.Builder::build)
                        .collect(Collectors.toList());
            } else {
                // 其他角色，目前至少包括：市场专员、市经理、各级代理
                // 根据所在部门的管辖区域
                List<Long> groupIds = orgService.getGroupIdListByUserId(userId);
                List<Integer> regionCodes = orgService.getGroupRegionsByGroupSet(groupIds).stream()
                        .map(AgentGroupRegion::getRegionCode)
                        .collect(Collectors.toList());
                result = raikouSystem.getRegionBuffer().loadRegions(regionCodes).values().stream()
                        .filter(i -> !i.getDisabled())
                        .flatMap(i -> {
                            switch (i.fetchRegionType()) {
                                case PROVINCE:
                                    return provinceCodes.contains(i.getProvinceCode()) ? i.getChildren().stream() : Stream.empty();
                                case CITY:
                                    return provinceCodes.contains(i.getProvinceCode()) ? Stream.of(i) : Stream.empty();
                                case COUNTY:
                                    return provinceCodes.contains(i.getProvinceCode()) ? Stream.of(raikouSystem.loadRegion(i.getParentRegionCode())) : Stream.empty();
                                default:
                                    return Stream.empty();
                            }
                        })
                        .map(RegionDto.Builder::build)
                        .collect(Collectors.toList());
            }
            result = result.stream()
                    .distinct()
                    .sorted(Comparator.comparing(RegionDto::getId)).collect(Collectors.toList());
            ArrayList _result = (ArrayList) result;
            return Result.success(_result);
        } catch (Exception e) {
            return Result.error(ErrorCode.REFERENCE_REGION);
        }
    }

    @Override
    public Result<ArrayList<RegionDto>> queryCounty(long userId, List<Integer> cityCodes) {
        try {
            // 获取用户授权信息
            AuthCurrentUser authUser = cache.getAuthCurrentUser(userId);
            List<Integer> roles = authUser.getRoleList();

            List<RegionDto> result;
            if (roles.contains(AgentRoleType.Admin.getId()) || roles.contains(AgentRoleType.Country.getId())) {
                // 系统管理员 或者 全国总监 可以查看所有区域
                result = raikouSystem.getRegionBuffer().loadChildRegions(cityCodes).values().stream()
                        .flatMap(List::stream)
                        .filter(i -> !i.getDisabled())
                        .map(RegionDto.Builder::build)
                        .collect(Collectors.toList());
            } else {
                // 其他角色，目前至少包括：市场专员、市经理、各级代理
                // 根据所在部门的管辖区域
                List<Long> groupIds = orgService.getGroupIdListByUserId(userId);
                List<Integer> regionCodes = orgService.getGroupRegionsByGroupSet(groupIds).stream()
                        .map(AgentGroupRegion::getRegionCode)
                        .collect(Collectors.toList());
                result = raikouSystem.getRegionBuffer().loadRegions(regionCodes).values().stream()
                        .filter(i -> !i.getDisabled())
                        .flatMap(i -> {
                            switch (i.fetchRegionType()) {
                                case PROVINCE:
                                    return i.getChildren().stream()
                                            .filter(j -> cityCodes.contains(j.getCityCode()))
                                            .flatMap(j -> j.getChildren().stream());
                                case CITY:
                                    return cityCodes.contains(i.getCityCode()) ? i.getChildren().stream() : Stream.empty();
                                case COUNTY:
                                    return cityCodes.contains(i.getCityCode()) ? Stream.of(i) : Stream.empty();
                                default:
                                    return Stream.empty();
                            }
                        })
                        .map(RegionDto.Builder::build)
                        .collect(Collectors.toList());
            }
            result = result.stream()
                    .distinct()
                    .sorted(Comparator.comparing(RegionDto::getId)).collect(Collectors.toList());
            ArrayList _result = (ArrayList) result;
            return Result.success(_result);
        } catch (Exception e) {
            return Result.error(ErrorCode.REFERENCE_REGION);
        }
    }

    @Override
    public Result<ArrayList<ExamSchoolDto>> querySchool(ExamReferSchoolQueryParams params) {

        // 根据用户id、城市code获取管辖的所有区
        Result<ArrayList<RegionDto>> _counties = queryCounty(params.getUserId(), params.getCityCodes());
        if (!_counties.isSuccess())
            return Result.error(ErrorCode.of(_counties.getErrorCode()));
        try {
            // 根据学校id查询学校
            List<Integer> countyCodes = _counties.getData().stream().map(i -> i.getId()).collect(Collectors.toList());
            Map<Long, School> schoolMap = raikouSystem.loadSchools(params.getSchoolIds());
            if (schoolMap.isEmpty())
                return Result.success(Lists.newArrayList());

            // 学校上只有区code，根据code进行过滤
            List<ExamSchoolDto> schools = schoolMap.values().stream()
                    .filter(i -> countyCodes.contains(i.getRegionCode()))
                    .map(ExamSchoolDto.Builder::build)
                    .collect(Collectors.toList());
            return Result.success((ArrayList) schools);
        } catch (Exception e) {
            return Result.error(ErrorCode.REFERENCE_SCHOOL, e.getMessage());
        }
    }

    @Override
    public Result<ArrayList<BookDto>> queryBook(ExamBookQueryParams params) {
        try {

            if (null == params.getSubject())
                throw new BusinessException(ErrorCode.REFERENCE_BOOK, "查询条件缺少【学科】");

            List<NewBookProfile> books;
            if (null == params.getGrade())
                books = bookClient.loadBooksBySubjectId(params.getSubject().subject.getId());
            else
                books = bookClient.loadBooksByClassLevelWithSortByRegionCode(
                        params.getSubject().subject,
                        0,
                        params.getGrade().clazzLevel);
            String q = params.getQ();
            List<BookDto> dtos;
            if (StringUtils.isNotBlank(q)) {
                dtos = books.stream().filter(book -> !Objects.equals(book.getStatus(), "OFFLINE")).map(BookDto.Builder::build)
                        .filter(i -> StringUtils.containsIgnoreCase(i.getId(), q)
                                || StringUtils.containsIgnoreCase(i.getName(), q))
                        .collect(Collectors.toList());
            } else {
                dtos = books.stream().filter(book -> !Objects.equals(book.getStatus(), "OFFLINE")).map(BookDto.Builder::build).collect(Collectors.toList());
            }
            return Result.success((ArrayList) dtos);
        } catch (Exception e) {
            return Result.error(ErrorCode.REFERENCE_BOOK, e.getMessage());
        }
    }

    @Override
    public Result<ReportUrlDto> reportUrl(String regionCode, Long planId) {
        //todo 调用报告方接口
        ReportUrlDto reportUrlDto = new ReportUrlDto();
        return Result.success(reportUrlDto);
    }
}
