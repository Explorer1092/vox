<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="学校分析" pageJs="schoolAnalysis" footerIndex=1>
<@sugar.capsule css=['res','intoSchoEffeNew']/>
<style>
    img{display:inherit}
    .noflowback-box .aut-table thead{border-bottom:1px solid #cdd3dc!important}
    .noflowback-box .aut-table tr{border:0!important}
    .noflowback-box .aut-table tr:nth-child(even){background-color:#f9f9fa}
    .schoolAnalysis-box{background-color:#fff}
    .schoolAnalysis-box .res-autInfor .aut-table{margin-left:.95rem}
    .schoolAnalysis-box .res-autInfor .aut-table tr{cursor:pointer;border-bottom:1px solid #cdd3dc!important}
    .schoolAnalysis-box .res-autInfor .aut-table tr:last-child{border-bottom:none!important}
    .schoolAnalysis-box .res-autInfor .aut-table td{width:auto!important}
    .schoolAnalysis-box .res-autInfor .aut-table td:first-child{padding-left:0!important}
    .schoolAnalysis-box .res-autInfor .aut-table thead{border-bottom:1px solid #cdd3dc!important}
    .schoolAnalysis-box .res-autInfor .aut-table thead tr td{text-align:center}
    .sal-title{margin-left:.95rem;padding:.625rem 0;font-size:.65rem;color:#9199bb;border-bottom:1px dashed #cdd3dc}
    .sal-tag{margin:0 .25rem;padding:.05rem .175rem;display:inline-block;vertical-align:middle;text-align:center;font-size:.55rem;color:#fff;border-radius:.1rem}
    .fontGreen{color:#70bba0}
    .bgPurple{background-color:#ca96d8}
    .bgYellow{background-color:#fbbc74}
    .bgViolet{background-color:#96a1f1}
    .icon-interview{margin-left:.25rem;display:inline-block;vertical-align:middle;width:.875rem;height:.875rem;text-align:center;font-size:.55rem;color:#fff;line-height:.875rem;background:#81b3e6;border-radius:2.5rem}
</style>
<div>
<#--<div class="crmList-box">-->
    <#--<div class="res-top fixed-head">-->
        <#--<a href="/mobile/performance/index.vpage"><div class="return"><i class="return-icon"></i>返回</div></a>-->
        <#--<span class="return-line"></span>-->
        <#--<span class="res-title">学校分析</span>-->
        <#--<#if user?has_content><a href="" class="icoPersonal js-changeBtn"></a></#if>-->
        <#--<#if user?has_content><span class="nameInfo">${user.realName!""}</span></#if>-->
    <#--</div>-->
<#--</div>-->
    <div>
    <#if schoolAnalysisDataList?has_content && schoolAnalysisDataList?size gt 0>
        <#list schoolAnalysisDataList as main>
                <div class="resources-box schoolAnalysis-box" style="margin-top:0.5rem" >
                    <div class="sal-title"><a href="/mobile/resource/school/card.vpage?schoolId=${main.schoolId!'0'}">${main.schoolName}</a>
                        <#if main.visitedFlag?has_content && main.visitedFlag><span class="icon-interview">访</span></#if>
                        <#if ((main.authRate!0) lt 0.25)><span class="sal-tag bgPurple">认证率低</span></#if>
                        <#if ((main.permeabilitySasc!0) lt 0.25)><span class="sal-tag bgYellow">单活渗透低</span></#if>
                        <#if ((main.permeabilityDasc!0) lt 0.25)><span class="sal-tag bgViolet">双活渗透低</span></#if>
                    </div>
                    <div class="res-autInfor autInor-mar" schoolId ='${main.schoolId!0}'>
                        <table class="aut-table" style="text-align:center">
                            <thead>
                            <tr>
                                <td></td>
                                <td>上月</td>
                                <td>目前</td>
                                <td>差值</td>
                                <td>可挖</td>
                            </tr>
                            </thead>
                            <tbody>
                            <tr>
                                <td style="text-align:center;display: block;">单活</td>
                                <td>${main.preMonthSasc!'--'}</td>
                                <td>${main.currentSasc!'--'}</td>
                                <td><span <#if ((main.currentSasc!0) lt (main.preMonthSasc!0))>class="fontGreen" style="font-weight: bold;" </#if>><#if main.currentSasc?has_content && main.preMonthSasc?has_content>${main.currentSasc - main.preMonthSasc}<#else>--</#if></td>
                                <td>${main.potentialSasc!'--'}</td>
                                <td><span class="arrow"></span></td>
                            </tr>
                            <#if main.schoolLevel?has_content && main.schoolLevel == "JUNIOR">
                            <tr>
                                <td style="text-align:center;display: block;">双活</td>
                                <td>${main.preMonthDasc!'--'}</td>
                                <td>${main.currentDasc!'--'}</td>
                                <td><span <#if ((main.currentDasc!0) lt (main.preMonthDasc!0))>class="fontGreen" style="font-weight: bold;" </#if>><#if main.currentDasc?has_content && main.preMonthDasc?has_content>${main.currentDasc - main.preMonthDasc}<#else>--</#if></span></td>
                                <td>${main.potentialDasc!'--'}</td>
                                <td></td>
                            </tr>
                            </#if>
                            </tbody>
                        </table>
                    </div>
                </div>
            </#list>
        <#else>
            <div style="background:#fff;text-align:center;margin-top:2rem;width:100%;height:3rem;padding-top:2rem;font-size:70%" > 暂无数据 </div>
    </#if>
</div>
    </div>
<script>
    var url = "/mobile/performance/choose_agent.vpage?breakUrl=school_analysis&selectedUser=${user.id!0}&needCityManage=1";
    var userName = "${user.realName!""}";
</script>
</@layout.page>
<script>


</script>
