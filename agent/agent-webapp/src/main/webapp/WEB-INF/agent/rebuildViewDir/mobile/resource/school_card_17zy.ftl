<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="学校" pageJs="schoolCard" footerIndex=2>
<script src="/public/rebuildRes/js/mobile/school/school_17zy.js"></script>
<@sugar.capsule css=['res',"home"]/>
    <#assign shortIconTail = "?x-oss-process=image/resize,w_300,h_375/auto-orient,1">
<div class="crmList-box resources-box">
    <div class="c-head fixed-head">
        <a class="the">学校</a>
        <a href="/view/mobile/crm/school/school_grade.vpage?schoolId=${schoolId!0}">年级</a>
        <a href="teacher_list.vpage?schoolId=${schoolId!0}">老师</a>
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
                                <span style="margin-left:.1rem"><#if schoolBasicInfo.schoolLevel == "JUNIOR"><i class="icon-junior"></i><#elseif schoolBasicInfo.schoolLevel == "INFANT"><i class="icon-infant"></i></#if></span>
                            </#if>
                            <#if schoolBasicInfo.schoolPopularityType??><span><i class="icon-${schoolBasicInfo.schoolPopularityType!""}"></i></span></#if>
                            <#if schoolBasicInfo.permeabilityType??><span><i class="icon-${schoolBasicInfo.permeabilityType!""}"></i></span></#if>

                            <span><#if schoolBasicInfo.authState?? && schoolBasicInfo.authState == "SUCCESS"><#else><i class="icon-unjian"></i></#if></span>
                            <div style="float:right"><#if schoolBasicInfo.isDictSchool?? && schoolBasicInfo.isDictSchool><#if schoolBasicInfo.hasBd?? && schoolBasicInfo.hasBd>${schoolBasicInfo.bdName!''}<#else>市经理未分配</#if><#else>未加入字典表</#if></div>
                        </div>
                    </dd>
                </dl>
            </div>
            <#if schoolKpInfo?? && schoolKpInfo?size gt 0>
                <#include "school/kp_templete.ftl">
            </#if>
            <ul style="width:100%;height:2rem;background: #fff;line-height: 2rem;font-size:.7rem;">
                <#if schoolBasicInfo.schoolLevel?? && (schoolBasicInfo.schoolLevel == "JUNIOR" || schoolBasicInfo.schoolLevel == "INFANT")>
                    <li class="_active tab_school" style="float:left;width:50%;text-align:center">数据看板</li>
                    <li class="tab_school" data-info="school" style="float:left;width:50%;text-align: center;">学校动态</li>
                </#if>
            </ul>
            <#include "school/school_card_container.ftl" />
        </div>
    </div>
</div>
    <div class="alertDialog showTips" style="display: none;">
        <div class="clazz-popup tip">
          <div class="text" style="text-align: left">
              <p>
                  1、渗透率=当月完成1套及以上当前科目作业的认证学生数/学校规模
              </p>
              <p>
                  2、科目月活：当月完成3套及以上相应科目作业的认证学生数
              </p>
              <p>
                  3、次月留存=本月活跃且上月活跃的学生/上月活跃的学生数（活跃：达成科目月活）
              </p>
          </div>
        </div>
        <div class="popup-mask js-remove"></div>
    </div>
<#include "school/dynamics.ftl" />
<#include "school/school_card_info.ftl" />
<script src="/public/rebuildRes/lib/echarts/echarts.min.js"></script>
<script>
    var schoolMode = "17zy"
    var needOldEcharts = false;
    var AT = new agentTool();
    var schoolId = ${schoolId!0}
    var userId = "${requestContext.getCurrentUser().getUserId()!0}";
    $(document).on('click','.add_into_school',function () {
    var schoolId = encodeURI('${schoolBasicInfo.schoolName!''}');
    openSecond('/view/mobile/crm/workrecord/into_school.vpage?schoolId=${schoolId!0}&schoolName=' + schoolId);
    })
</script>
</@layout.page>
