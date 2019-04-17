<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="工作记录详情" pageJs="" footerIndex=4 navBar="hidden">
    <@sugar.capsule css=['school']/>
<script src="/public/rebuildRes/js/common/common.js"></script>
    <#if recordDetails?? && recordDetails?has_content>
        <#if recordDetails.workRecordType == 'SCHOOL'>
            <div class="flow">
                <div class="item tip">
                    日期
                    <span class="inner-right">${recordDetails.workRecordTime!''}</span>
                </div>
                <div class="item">
                    学校名称
                    <div class="inner-right" style="background:#fff">
                        <#if recordDetails.intoSchoolName??>${recordDetails.intoSchoolName!''}</#if>
                    </div>
                </div>
                <div class="item">
                    地址
                    <div class="inner-right" style="font-size:.65rem;width:60%;overflow:hidden;text-overflow: ellipsis;white-space: nowrap;background:#fff">
                        ${recordDetails.intoSchooAddress!''}
                    </div>
                </div>
                <div class="item tip">
                    <i class="icon theme"></i>拜访主题
                    <div class="inner-right" id="partnerDisplay">
                        <#if recordDetails.intoSchoolTitle == '11'>促进注册
                        <#elseif recordDetails.intoSchoolTitle == '12'>促进签约
                        <#elseif recordDetails.intoSchoolTitle == '13'>促进月活
                        <#elseif recordDetails.intoSchoolTitle == '14'>寻求介绍
                        <#elseif recordDetails.intoSchoolTitle == '15'>确认基本信息
                        </#if>
                    </div>
                </div>
                <div class="item tip t-visit clearfix">
                    <i class="icon visit"></i>拜访老师
                    <div class="inner-right" style="background:#fff" >
                        <div class="t-list" id="visitTeachListDiv">
                            <#if recordDetails.intoSchoolTeacher?? &&recordDetails.intoSchoolTeacher?size gt 0>
                                <#list recordDetails.intoSchoolTeacher as vt>
                                    <#if vt.teacherName??>
                                    ${vt.teacherName!""}<#if vt_has_next>，</#if>
                                    <#else>
                                    ${vt.teacherId!''}<#if vt_has_next>，</#if>
                                    </#if>
                                </#list>
                            <#else>
                                请选择
                            </#if>
                        </div>
                    </div>
                </div>
                <div class="item select-partner" style="position: relative">
                    陪访人
                    <div class="inner-right" id="partnerDisplay"  style="background:#fff">
                    ${recordDetails.intoSchoolVisit!''}
                    </div>
                </div>
                <#if recordDetails.intoSchoolAgent?has_content>
                    <div class="item select-partner" style="position: relative">
                        代理人员
                        <div class="inner-right" id="agencyDisplay" style="background:#fff">
                            ${recordDetails.intoSchoolAgent!""}
                        </div>
                    </div>
                </#if>
                <div class="item visit-result">
                    <i class="icon effect"></i>
                    拜访效果及详情
                </div>
                <#if schoolMemorandumInfo?? && (schoolMemorandumInfo!"")!="">
                    <div class="item">
                        学校备忘录
                        <span class="inner-right" style="background: none;"> ${schoolMemorandumInfo!""}</span>
                    </div>
                </#if>
                <#if visitTeacherList??>
                    <#if visitTeacherList?size gt 0>
                        <#list visitTeacherList as vt>
                            <#if vt.visitInfo?? && vt.visitInfo != "">
                                <div data-tid="${vt.teacherId!''}" class="item clearfix">
                                ${vt.teacherName!""}
                                    <span class="inner-right" style="background: none;">${vt.visitInfo!""}</span>
                                </div>
                            </#if>
                        </#list>
                    </#if>
                </#if>
                <#if recordDetails.partnerSuggest??>
                    <div class="item tip">
                        陪访建议<textarea readonly="readonly" name="conclusion" maxlength="100" class="js-need" data-einfo="请填写达成结果">${recordDetails.partnerSuggest!""}</textarea>
                    </div>
                </#if>
            </div>

    <#elseif recordDetails.workRecordType == 'MEETING'>
    <div class="flow">
        <div class="item tip">
            日期
            <span class="inner-right">${recordDetails.workRecordTime!''}</span>
        </div>
        <div class="item">
            地点
            <div class="inner-right">
                <#if recordDetails.conferenceAddress??>${recordDetails.conferenceAddress!''}</#if>
            </div>
        </div>
        <div class="item">
            主题
            <div class="inner-right">
            ${recordDetails.workRecordTitle!''}
            </div>
        </div>
        <div class="item">
            参会人数
            <div class="inner-right">
            ${recordDetails.meeteeCount!''}
            </div>
        </div>
        <div class="item">
            讲师
            <div class="inner-right">
            ${recordDetails.instructor!''}
            </div>
        </div>
        <div class="item">
            级别
            <div class="inner-right">
                <#if recordDetails.meetingLevel == 'PROVINCE_LEVEL'>省级会议
                <#elseif recordDetails.meetingLevel == 'CITY_LEVEL'>市级会议
                <#elseif recordDetails.meetingLevel == 'COUNTY_LEVEL'>区级会议
                </#if>
            </div>
        </div>
        <div class="item">
            宣讲时长
            <div class="inner-right" id="partnerDisplay">
                <#if recordDetails.meetingTime == 1>小于15分钟
                <#elseif recordDetails.meetingTime == 2>15-60分钟
                <#elseif recordDetails.meetingTime == 3>大于1个小时
                </#if>
            </div>
        </div>
        <div class="item">
            类型
            <div class="inner-right" id="partnerDisplay">
                <#if recordDetails.conferenceType?has_content && recordDetails.conferenceType == 1>专场
                <#elseif recordDetails.conferenceType?has_content && recordDetails.conferenceType == 2>插播
                </#if>
            </div>
        </div>
        <div class="item">
            代理人员
            <div class="inner-right">
                <#if recordDetails.conferenceAgent?has_content>
                    ${recordDetails.conferenceAgent!''}
                </#if>
            </div>
        </div>

        <div class="item">
            教研员
            <div class="inner-right">
                <#if recordDetails.instructorName?has_content>
                    ${recordDetails.instructorName!''}
                </#if>
            </div>
        </div>
        <div class="item">
            电话
            <div class="inner-right">
                <#if recordDetails.mobile?has_content>
                    ${recordDetails.mobile!''}
                </#if>
            </div>
        </div>
        <div class="item">
            教研员是否在场
            <div class="inner-right">
                <#if recordDetails.instructorAttend?? && recordDetails.instructorAttend>
                    是
                <#else>
                    否
                </#if>
            </div>
        </div>
        <div class="item tip">
            会议内容及效果(选填)
            <textarea readonly="readonly" name="conclusion" id="conclusion" maxlength="100" class="js-need" data-einfo="请填写达成结果"><#if recordDetails.conferenceContent?has_content>${recordDetails.conferenceContent!''}</#if></textarea>
            <span name="" id="" cols="30" rows="10"></span>
        </div>
        <div class="item tip">
            <div class="photo clearfix photo2">
                <div class="shot">
                    现场照片
                </div>
                <div class="pick">
                    <div class="file" id="getSchoolGate"><img src="<#if recordDetails.scenePhotoUrl?has_content>${recordDetails.scenePhotoUrl!''}?x-oss-process=image/auto-orient,1</#if>" width="100%" height="100%" ></div>
                    <input type="hidden" id="photoUrl" name="photoUrl" value="">
                </div>
            </div>
        </div>
    </div>
    <#elseif recordDetails.workRecordType == 'VISIT'>

        <div class="flow">
            <div class="item tip">
                日期
                <span class="inner-right">${recordDetails.workRecordTime!''}</span>
            </div>
            <div class="item">
                学校名称
                <div class="inner-right">
                    <#if recordDetails.intoSchoolName??>${recordDetails.intoSchoolName!''}</#if>
                </div>
            </div>
            <div class="item">
                地址
                <div class="inner-right" style="font-size:.65rem;width:60%;overflow:hidden;text-overflow: ellipsis;white-space: nowrap;">
                ${recordDetails.intoSchooAddress!''}
                </div>
            </div>
            <div class="item">
                陪访对象
                <div class="inner-right">
                    <#if recordDetails.workRecordCreator??>${recordDetails.workRecordCreator!''}</#if>
                </div>
            </div>
            <div class="item">
                陪访目的
                <div class="inner-right" style="font-size:.65rem;overflow:hidden;text-overflow: ellipsis;white-space: nowrap;">
                    <#if recordDetails.workRecordTitle?has_content>
                        <#if recordDetails.workRecordTitle == '1'>
                            专员技能辅导
                        <#elseif recordDetails.workRecordTitle == '2'>
                        重点学校跟进
                        <#elseif recordDetails.workRecordTitle == '3'>
                        市场情况了解
                        </#if>
                    </#if>
                </div>
            </div>
            <div class="item">
                陪访建议<textarea style="color:#76797e" readonly="readonly" name="conclusion" id="conclusion" maxlength="99" class="js-need" data-einfo="请填写达成结果"><#if recordDetails.partnerSuggest?has_content>${recordDetails.partnerSuggest!''}</#if></textarea>
            </div>
        <#elseif recordDetails.workRecordType == 'TEACHING'>
            <div class="flow">
                <div class="item tip">
                    日期
                    <span class="inner-right">${recordDetails.workRecordTime!''}</span>
                </div>
                <div class="item tip">
                    拜访人员
                    <span class="inner-right">${recordDetails.researchers!''}</span>
                </div>
                <div class="item tip">
                    拜访目的
                    <div class="inner-right" id="partnerDisplay">
                        <#if recordDetails.researchersVisitedIntention == 1>初次接洽
                        <#elseif recordDetails.researchersVisitedIntention == 2>客情维护
                        <#elseif recordDetails.researchersVisitedIntention == 3>促进组会
                        <#elseif recordDetails.researchersVisitedIntention == 4>寻求介绍
                        </#if>
                    </div>
                </div>
                <div class="item tip">
                    地点
                    <span class="inner-right">${recordDetails.researchersVisitAddress!''}</span>
                </div>
                <div class="item tip">
                    拜访过程
                    <textarea style="color:#76797e;padding:0" readonly="readonly" name="conclusion" id="conclusion" maxlength="100" class="js-need" data-einfo="请填写达成结果">${recordDetails.researchersVisitContent!''}</textarea>
                    <span name="" id="" cols="30" rows="10"></span>
                </div>
                <div class="item tip">
                    拜访结果
                    <textarea style="color:#76797e;padding:0" readonly="readonly" name="conclusion" id="conclusion" maxlength="100" class="js-need" data-einfo="请填写达成结果">${recordDetails.researchersVisitResult!''}</textarea>
                    <span name="" id="" cols="30" rows="10"></span>
                </div>
            </div>
        </#if>
    </#if>
</@layout.page>
