<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="线索" pageJs="" footerIndex=1>
<@sugar.capsule css=['new_home']/>
<#function formatterDate num>
<#local forDate = num?string>
<#return forDate?substring(0,4) + "-"+forDate?substring(4,6)+"-"+forDate?substring(6,8)>
</#function>
<div class="primary-box">
    <#--<div class="res-top fixed-head" style="border-bottom:1px solid #cdd3dc;">-->
        <#--<a href="javascript:window.history.back();"><div class="return"><i class="return-icon"></i>返回</div></a>-->
        <#--<span class="return-line"></span>-->
        <#--<span class="res-title">线索</span>-->
    <#--</div>-->

    <div class="t-list">
        <#if dataList?? && dataList?size gt 0>
            <#list dataList as dl>
                <#--<div>-->
                <#if dl.needFollowList?? && dl.needFollowList?size gt 0>
                    <#list dl.needFollowList as nfl>
                        <p class="name">
                            <#if nfl.type == 1>
                                新注册老师
                            <#elseif nfl.type == 2>
                                不活跃老师
                            <#elseif nfl.type == 3>
                                满足条件未认证老师
                            </#if>
                            <span class="icon-box" style="top:0.2rem;"></span><var>${formatterDate(dl.day)!''}</var>
                        </p>
                            <#if nfl.schoolList?? && nfl.schoolList?size gt 0>
                                <#list nfl.schoolList as sl>
                                    <#assign teacher_list = sl.teacherList![] >
                                    <#if teacher_list?? && teacher_list?size gt 0>
                                        <div style="border-bottom: 1px solid #cdd3dc">
                                            <p class="content">${sl.schoolName!''}
                                                <#if nfl.type == 1>
                                                    未拜访但是有 ${teacher_list?size!0}位注册老师啦
                                                <#elseif nfl.type == 2>
                                                    ${teacher_list?size!0}位老师已有20天未布置作业了
                                                <#elseif nfl.type == 3>
                                                    有${teacher_list?size!0}位老师满足条件未认证
                                                </#if>
                                                </p>
                                            <p class="teachers">
                                                <#list teacher_list as tl>
                                                    <a href="/view/mobile/crm/teacher/teacher_card_new.vpage?teacherId=${tl.teacherId!0}">
                                                        <span><span class="icon-box" style="top:0.2rem;margin-right:0.2rem;">
                                                            <#if tl.subject == "ENGLISH">
                                                                <i class="icon-ying"></i>
                                                            <#elseif tl.subject == "MATH">
                                                                <i class="icon-shu"></i>
                                                            </#if>
                                                        </span>${tl.teacherName!''}</span>
                                                    </a>
                                                </#list>
                                            </#if>
                                            </p>
                                            <p class="see-detail">
                                                <a href="/mobile/resource/school/card.vpage?schoolId=${sl.schoolId}">点此查看学校详情 制定拜访计划吧</a>
                                            </p>
                                        </div>
                                </#list>
                            </#if>
                        </p>
                    </#list>
                </#if>
                <#--</div>-->
            </#list>
        </#if>
    </div>
</div>
</@layout.page>