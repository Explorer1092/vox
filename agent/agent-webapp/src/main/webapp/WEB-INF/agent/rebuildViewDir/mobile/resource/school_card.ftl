<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="学校" pageJs="schoolOffline" footerIndex=2>
<@sugar.capsule css=['res',"home"]/>
    <#assign shortIconTail = "?x-oss-process=image/resize,w_300,h_375/auto-orient,1">
<div class="crmList-box resources-box">
    <div class="feedbackList-pop show_now" style="display:none;z-index:100;position:fixed;top:0;background-color:rgba(125,125,125,.5);width:100%;height:100%">
        <ul style="background:#fff;margin-top:-0.1rem">
            <li class="tab_row" style="padding:.2rem 0"><a id="applwao  yService" style="display:inline-block;height:2.5rem;line-height:2.5rem;width:100%;color:#000;padding-left:1rem;font-size: .75rem;border-bottom: .05rem solid #cdd3d3"  href="card.vpage?schoolId=${schoolId!}&modeType=online" class="active">online模式</a></li>
            <li class="tab_row" style="padding:.2rem 0"><a id="retroAction" style="height:2.5rem;line-height:2.5rem;display:inline-block;width:100%;color:#000;padding-left:1rem;font-size: .75rem;"   href="javascript:;" class="active">offline模式</a></li>
        </ul>
    </div>
    <div class="c-head fixed-head">
        <a class="the">学校</a>
        <a href="/view/mobile/crm/school/school_grade.vpage?schoolId=${schoolId!0}&modeType=offline">年级</a>
        <a href="teacher_list.vpage?schoolId=${schoolId!0}&modeType=offline">老师</a>
    </div>
    <#if repetition?? && repetition?size gt 0>
        <div class="schoolParticular-point">请及时填写：<#list repetition as repet>${repet}<#if repet_has_next>、</#if></#list></div>
    </#if>
    <div class="c-main tab-main">
        <div>
            <!--学校信息-->
            <div class="res-content" id="schoolDetail" style="margin-top:0">
                <dl class="res-column">
                    <#if schoolBasicInfo.hasThumbnail??><dt><img width="100%" height="100%" src="${schoolBasicInfo.hasThumbnail?string('${schoolBasicInfo.thumbnailUrl!0}?x-oss-process=image/resize,w_96,h_96','/public/images/mobileCRM-V2-info-message.png')}" alt="学校缩略图"></dt><#else><dt><img width="100%" height="100%" src="/public/images/mobileCRM-V2-info-message.png" alt="学校缩略图"></dt></#if>
                    <dd>
                        <span class="name" style="width: 100%;font-size:.7rem"><a href="javascript:void(0)" style="float:right;margin-right:1rem; color:#ff7d5a;">&gt</a>${schoolBasicInfo.schoolName!''}（${schoolBasicInfo.schoolId!0}）<span></span>
                        <div class="res-info" style="margin-top:.5rem">
                            <#if schoolBasicInfo.schoolLevel??>
                                <span style="margin-left:.1rem"><#if schoolBasicInfo.schoolLevel == "JUNIOR"><i class="icon-junior"></i><#elseif schoolBasicInfo.schoolLevel == "MIDDLE"><i class="icon-middle"></i><#elseif schoolBasicInfo.schoolLevel == "HIGH"><i class="icon-high"></i><#elseif schoolBasicInfo.schoolLevel == "INFANT"><i class="icon-infant"></i></#if></span>
                            </#if>
                            <#if schoolBasicInfo.schoolPopularityType??><span><i class="icon-${schoolBasicInfo.schoolPopularityType!""}"></i></span></#if>
                            <#if schoolBasicInfo.permeabilityType??><span><i class="icon-${schoolBasicInfo.permeabilityType!""}"></i></span></#if>
                            <span><#if schoolBasicInfo.authState?? && schoolBasicInfo.authState == "SUCCESS"><#else><i class="icon-unjian"></i></#if></span>
                            <#if schoolBasicInfo.schoolLevel == "MIDDLE" || schoolBasicInfo.schoolLevel == "HIGH"><span><#if schoolBasicInfo.scannerFlag?? && !schoolBasicInfo.scannerFlag><i class="icon-unyiqi"></i></#if></span></#if>
                            <div style="float:right"><#if schoolBasicInfo.isDictSchool?? && schoolBasicInfo.isDictSchool><#if schoolBasicInfo.hasBd?? && schoolBasicInfo.hasBd>${schoolBasicInfo.bdName!''}<#else>市经理未分配</#if><#else>未加入字典表</#if></div>
                        </div>
                    </dd>
                </dl>
            </div>
            <#if schoolKpInfo?? && schoolKpInfo?size gt 0>
                <#include "school/kp_templete.ftl">
            </#if>
            <div class="vacation_box">
                <a onclick="openSecond('/mobile/resource/school/oto_auth.vpage?schoolId=${schoolId!0}')">
                    <div class="vacTitle" style="font-size: .7rem;color:#636880">
                        快乐学扫描权限设置 <div class="more_btn"> > </div>
                    </div>
                </a>
            </div>
            <ul style="width:100%;height:2rem;background: #fff;line-height: 2rem;font-size:.7rem;">
                <li class="_active tab_school" style="float:left;width:49%;text-align:center">扫描数据</li>
                <li class="tab_school" data-info="school" style="float:left;width:49%;text-align: center;">学校动态</li>
            </ul>
            <div>
                <div class="teacher_detail" style="margin-top:.5rem">
                    <div class="res-content scanData">

                    </div>
                    <div class="vacation_box js_exam">
                        <a>
                            <div class="vacTitle" style="font-size: .7rem;color:#636880">
                                大考管理 <div class="more_btn"> > </div>
                            </div>
                        </a>
                    </div>
                    <div class="vacation_box">
                        <div class="vacInfo">历史数据</div>
                        <#--<ul class="listNav1 listNav" style="width:100%;display:block;">-->
                            <#--<li class="school_data" data-index="1" data-name="普通扫描(≥1次)" style="width:22%;height:1.5rem;line-height:1.5rem;display: block;float:left;">普通扫描(≥1次)</li>-->
                        <#--</ul>-->
                        <div class="container" style="width:94%;height:10rem;background:#fff;clear:both;padding:0 3%;text-align:center;margin-top:.5rem"></div>
                    </div>
                </div>
                <div class="teacher_detail" style="margin-top:.5rem" hidden>
                    <div class="school_information"></div>
                </div>
        </div>
    </div>
</div>
<div class="mask"></div>
<#include "school/dynamics.ftl" />


<script type="text/html" id="listNav1">
    <%for(var key in res.scanDataMap){%>
        <li class="school_data" data-index="1" data-name="<%=key%>" style="width:22%;height:1.5rem;line-height:1.5rem;display: block;float:left;"><%=key%></li>
    <%}%>
</script>
<script type="text/html" id="listNav2">
    <%for(var key in res.mauDataMap){%>
        <li class="school_data" data-index="2" data-name="<%=key%>" style="width:22%;height:1.5rem;line-height:1.5rem;display: block;float:left;"><%=key%></li>
    <%}%>
</script>
<script type="text/html" id="scanData">
    <ul class="res-list list-dif clearfix" style="clear:both;">
        <li  style="width:25%">
            <div class="sub"><%= res.schoolKlxPerformance.stuScale%></div>
            <div class="volume">规模</div>
        </li>
        <li  style="width:25%">
            <div class="sub"><%= res.schoolKlxPerformance.klxTnCount%></div>
            <div class="volume">考号</div>
        </li>
        <li  style="width:25%">
            <div class="sub"><%= res.schoolKlxPerformance.tmFinTpGte1StuCount%></div>
            <div class="volume">普通扫描(≥1次)</div>
        </li>
        <li  style="width:25%">
            <div class="sub"><%= res.schoolKlxPerformance.tmFinTpGte3StuCount%></div>
            <div class="volume">普通扫描(≥3次)</div>
        </li>
    </ul>
</script>
<script src="/public/rebuildRes/lib/echarts/echarts.min.js"></script>
<script>
    var schoolMode = "offline";
    var needOldEcharts = true;
    var AT = new agentTool();
    var schoolId = ${schoolId!0}
    var userId = "${requestContext.getCurrentUser().getUserId()!0}";
    $(document).ready(function () {
        var setTopBar = {
            show:true,
            rightText:schoolMode + "模式",
            rightTextColor:"ff7d5a",
            needCallBack:true
        } ;
        var topBarCallBack = function () {
            $(".show_now").toggle();
        };
        setTopBarFn(setTopBar,topBarCallBack);

        $('.js_exam').on('click',function () {
            var schoolName = encodeURI('${schoolBasicInfo.schoolName!""}');
            openSecond('/view/mobile/crm/examnation/exam_list.vpage?schoolId=${schoolId!0}&schoolName='+schoolName)
        });
        $(document).on('click','.add_into_school',function () {
            var schoolId = encodeURI('${schoolBasicInfo.schoolName!''}');
            openSecond('/view/mobile/crm/workrecord/into_school.vpage?schoolId=${schoolId!0}&schoolName=' + schoolId);
        })

    })
</script>
</@layout.page>
