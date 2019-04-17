<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="查看进校记录" pageJs="" footerIndex=4 navBar="hidden">
    <@sugar.capsule css=['intoSchool']/>
<div class="visit_content" style="margin-top:.5rem">
    <div class="title">基础信息</div>
    <div class="list">
        <ul>
            <li>
                <p class="txt">时间</p>
                <p class="item">${workTime?string("yyyy-MM-dd HH:mm")!''}&nbsp;</p>
            </li>
            <li>
                <p class="txt">学校名称</p>
                <p class="item"><#if schoolName??>${schoolName!''}<#else>请选择</#if></p>
            </li>
            <li>
                <p class="txt">签到地址</p>
                <p class="item">${address!''}&nbsp;</p>
            </li>
            <li>
                <p class="txt">拜访主题</p>
                <p class="item">
                    <#if workTitle??>
                        <#if workTitle == "11">
                            促进注册
                        <#elseif workTitle== "12">
                            促进签约
                        <#elseif workTitle== "13">
                            促进月活
                        <#elseif workTitle== "14">
                            寻求介绍
                        <#elseif workTitle== "15">
                            确认基本信息
                        </#if>
                    </#if>
                    &nbsp;
                </p>
            </li>
            <li>
                <p class="txt">拜访老师</p>
                <p class="item">
                    <#if subjectVisitTeacherMap?has_content>
                        <#list subjectVisitTeacherMap?keys as key>
                        <#assign visitTeacher = subjectVisitTeacherMap[key]>
                            <#if visitTeacher?? && visitTeacher?size gt 0>
                                <#if key == 'ENGLISH'>
                                    英语：
                                <#elseif key == 'MATH'>
                                    数学：
                                <#else>
                                    其他：
                                </#if>
                                <#list visitTeacher as vt>
                                    ${vt.teacherName!""}
                                </#list>
                                <#if key_has_next><br/></#if>
                            </#if>
                        </#list>
                    </#if>
                    &nbsp;
                </p>
            </li>
        </ul>
    </div>
    <#--判断备忘信息是否存在-->
    <#if schoolMemorandumInfo?has_content || (visitTeacherList?? && visitTeacherList?size gt 0)>
        <#if schoolMemorandumInfo?has_content>
            <#assign hasMemorandumInfo = true>
        </#if>
        <#if visitTeacherList??>
            <#if visitTeacherList?size gt 0>
                <#list visitTeacherList as vt>
                    <#if vt.visitInfo?? && vt.visitInfo != "">
                        <#assign hasMemorandumInfo = true>
                    </#if>
                </#list>
            </#if>
        </#if>
    </#if>
    <div class="title">备忘信息</div>
    <#if hasMemorandumInfo?? && hasMemorandumInfo>
        <#if schoolMemorandumInfo?has_content>
            <div class="content">
                <div class="headTxt">学校备忘录</div>
                <div class="info">${schoolMemorandumInfo!""}</div>
            </div>
        </#if>
        <#if visitTeacherList??>
            <#if visitTeacherList?size gt 0>
                <#list visitTeacherList as vt>
                    <#if vt.visitInfo?? && vt.visitInfo != "">
                        <div class="content">
                            <div class="headTxt"> ${vt.teacherName!""}</div>
                            <div class="info">${vt.visitInfo!""}</div>
                        </div>
                    </#if>
                </#list>
            </#if>
        </#if>
    <#else>
        <div class="content">
            <div class="info">无</div>
        </div>
    </#if>

    <#if partnerSuggest??>
        <div class="content">
            <div class="headTxt">陪访建议</div>
            <div class="info">${partnerSuggest!""}</div>
        </div>
    </#if>
    <#if visitSchoolRecords?? && visitSchoolRecords?size gt 0>
        <div class="title">陪访反馈</div>
        <div class="column">
            <ul>
                <#list visitSchoolRecords as visit>
                    <li>
                        <a class="item js-workId" data-id="${visit.id!0}" href="javascript:void(0);">
                            <div>${visit.workerName!""}</div>
                        </a>
                    </li>
                </#list>
            </ul>
        </div>
    </#if>
    <#if recordId?? && isYour?? && (isYour!false) && (.now?string("yyyyMMdd") == (workTime?string("yyyyMMdd")!''))>
        <a href="javascript:void(0);" class="inner-right js-editSchoolSRecord" data-rid="${recordId!""}" style="display: inline-block;text-align: center;color: #fff;background-color: #ff7d5a;font-size: .75rem;padding: .6rem 0;margin: .5rem 0;width:100%">
            修改备忘信息
        </a>
    </#if>
</div>
<script>
    $(document).ready(function () {
       reloadCallBack();
    });
    $(".js-editSchoolSRecord").on("click",function(){
        var rid = $(this).data("rid");
        openSecond("/mobile/work_record/modificationSchoolRecord.vpage?schoolRecordId="+rid)
    });
    $(".js-workId").on("click",function(){
        openSecond("/mobile/work_record/show_visit_school_record.vpage?recordId="+$(this).data("id"));
    });
</script>
</@layout.page>
