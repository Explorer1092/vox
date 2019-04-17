<#import "../layout_new.ftl" as layout>
<@layout.page group="work_record" title="选择下属拜访记录">
<div class="mobileCRM-V2-header">
    <div class="inner">
        <div class="box">
            <a href="javascript:history.back();" class="headerBack">&lt;&nbsp;返回</a>
            <div class="headerText">选择下属拜访记录</div>
        </div>
    </div>
</div>
<div class="mobileCRM-V2-box mobileCRM-V2-info mobileCRM-V2-mt">
    <ul class="mobileCRM-V2-list" id="schoolContainer">

        <#if visitRecord?? && visitRecord?size gt 0>
            <#list visitRecord as vr>
                ${vr.workTime?string("yyyy-MM-dd")!''}
                <li>
                    <p>
                        <div>${vr.partnerName!''} (${vr.schoolName!""})</div>
                    </p>
                    <p>
                       <div>
                            ${vr.workTitle!""} | 拜访了 ${vr.instructorCount!0}位关键人
                        </div>
                    </p>
                    <p>
                        拜访效果及详情：
                    </p>
                    <p>
                        <#if vr.visitTeacherList??>
                            <#if vr.visitTeacherList?size gt 0>
                                <#list vr.visitTeacherList as vt>
                                        <div>${vt.teacherName!""} ：${vt.visitInfo!''}</div>
                                </#list>
                            </#if>
                        </#if>
                    </p>
                    <#if vr.followingPlan??>
                        <p>
                            待办：${vr.followingPlan!''}
                        </p>
                    </#if>

                    <#if vr.followingTime??>
                        <p>
                            下次计划进校时间：${vr.followingTime?string("yyyy年MM月dd日")!''}
                        </p>
                    </#if>
                    
                    <div>
                        <a href="addVisit.vpage?schoolRecordId=${vr.schoolRecordId!0}" style="background-color: #2ecc71;color: white;padding: 10px;text-align: center;">填写陪访建议</a>
                    </div>
                </li>
            </#list>
        <#else>
            <p style="text-align: center;font-size: 18px;">
                没有可选择的拜访记录，快请您的下属去填写吧
            </p>
        </#if>
    </ul>
</div>
</@layout.page>
