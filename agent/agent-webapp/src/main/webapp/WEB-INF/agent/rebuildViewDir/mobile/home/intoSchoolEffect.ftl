<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="进校效果" pageJs="" footerIndex=1>
<@sugar.capsule css=['new_home']/>
<div class="crmList-box">
    <div class="c-list">
        <div class="pro-content clearfix">
            <div class="pro-title"><i class="icon-effect"></i>本月进校效果</div>

            <div class="pro-column">
                <p class="pro-tip">本月进校${visitResultData.visitSchoolCount!'0'}所 学校老师${visitResultData.teacherTotalCount!0}名</p>

                <#assign totalNum = (visitResultData.visitedUsedCount!0) + (visitResultData.visitedUnusedCount!0) + (visitResultData.unvisitedUnusedCount!0) + (visitResultData.unvisitedUsedCount!0)>
                <div class="pro-inner">
                    <div class="bar-1" style="width: <#if totalNum !=0 > ${(100*visitResultData.visitedUsedCount!0)/totalNum}<#else>0</#if>%"></div>
                    <div class="bar-2" style="width: <#if totalNum !=0 > ${(100*visitResultData.visitedUnusedCount!0)/totalNum}<#else>0</#if>%;"></div>
                    <div class="bar-3" style="width: <#if totalNum !=0 > ${(100*visitResultData.unvisitedUnusedCount!0)/totalNum}<#else>0</#if>%;"></div>
                    <div class="bar-4" style="width: <#if totalNum !=0 > ${(100*visitResultData.unvisitedUsedCount!0)/totalNum}<#else>0</#if>%;"></div>
                </div>
                <div class="pro-tip c-flex c-flex-4">
                    <div class="tip-1">
                        <span>${visitResultData.visitedUsedCount!0}</span>
                    </div>
                    <div class="tip-2">
                        <span>${visitResultData.visitedUnusedCount!0}</span>
                    </div>
                    <div class="tip-3">
                        <span>${visitResultData.unvisitedUnusedCount!0}</span>
                    </div>
                    <div class="tip-4">
                        <span>${visitResultData.unvisitedUsedCount!0}</span>
                    </div>
                </div>
                <div class="pro-tip c-flex c-flex-4">
                    <div class="tip-1">
                        <span><b></b>拜访使用</span>
                    </div>
                    <div class="tip-2">
                        <span><b></b>拜访未用</span>
                    </div>
                    <div class="tip-3">
                        <span><b></b>未访未用</span>
                    </div>
                    <div class="tip-4">
                        <span><b></b>未访使用</span>
                    </div>
                </div>
            </div>
        </div>
        <div class="pro-tips" style="margin-top: -.35rem;"><i class="icon-effect"></i>近7天拜访学校（橘色表示拜访过但未使用的老师）</div>
        <#if visitResultDetailList?? && visitResultDetailList?size gt 0>
            <#list visitResultDetailList as vr>
            <div class="pro-up clearfix">
                <div class="pro-right">${vr.visitDate?string("yyyy-MM-dd")!''}</div>
                <div class="left">
                    <p class="name"><a href="/mobile/resource/school/card.vpage?schoolId=${vr.schoolId!'0'}">${vr.schoolName!''}</a></p>
                    <p>数据变化：</p>
                </div>
                <div class="inline-list c-flex c-flex-4">
                    <div>
                        ${vr.addStuRegNum!0} <span>注册</span>
                    </div>
                    <div class="orange-color">
                        ${vr.addStuAuthNum!0} <span>认证</span>
                    </div>
                    <div>
                        ${vr.sascData!0} <span>单活</span>
                    </div>
                    <div>
                        ${vr.dascData!0} <span>双活</span>
                    </div>
                </div>
            </div>
            <div class="pro-down">
                <div class="pro-info">本月未使用老师<span>
                <#if vr.unusedEnglishTeacherList?? && vr.unusedEnglishTeacherList?size gte 0 && vr.unusedMathTeacherList?? && vr.unusedMathTeacherList?size gte 0>
                    （${(vr.unusedEnglishTeacherList?size!0)+ (vr.unusedMathTeacherList?size!0)}）
                <#else>
                    （0）
                </#if>
                </span></div>
                <div class="pro-show"><span class="icon-box"><i class="icon-ying"></i></span>
                    英语：
                    <#if vr.unusedEnglishTeacherList?? && vr.unusedEnglishTeacherList?size gt 0>
                        <#list vr.unusedEnglishTeacherList as uet>
                            <#if uet.visitFlg>
                                <a href="/view/mobile/crm/teacher/teacher_card_new.vpage?teacherId=${uet.teacherId!0}"><span>${uet.teacherName!''}</span></a>
                            <#else>
                                <a href="/view/mobile/crm/teacher/teacher_card_new.vpage?teacherId=${uet.teacherId!0}">${uet.teacherName!''}</a>
                            </#if>
                            <#if uet_has_next>,</#if>
                        </#list>
                    </#if>
                </div>
                <div class="pro-show"><span class="icon-box"><i class="icon-shu"></i></span>数学：
                    <#if vr.unusedMathTeacherList?? && vr.unusedMathTeacherList?size gt 0>
                        <#list vr.unusedMathTeacherList as uet>
                            <#if uet.visitFlg>
                                <span>
                                    <a href="/view/mobile/crm/teacher/teacher_card_new.vpage?teacherId=${uet.teacherId!0}">${uet.teacherName!''}</a>
                                </span>
                            <#else>
                                <a href="/view/mobile/crm/teacher/teacher_card_new.vpage?teacherId=${uet.teacherId!0}">${uet.teacherName!''}</a>
                            </#if>
                            <#if uet_has_next>,</#if>
                        </#list>
                    </#if>
                </div>
            </div>
            </#list>
        </#if>
    </div>
</div>
</@layout.page>