<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="组织组会详情" pageJs="" footerIndex=4 navBar="hidden">
    <@sugar.capsule css=['intoSchool']/>
<style>
    body{background:#f1f2f5}
    body .visit_content .list li img{width:2rem;height:2rem}
    body .visit_content .list li .txt{width:5rem}
    .visit_content{padding:.15rem 0 0 0}
    .item{overflow:hidden}
</style>
<div class="visit_content">
    <#if meetingRecord?? && meetingRecord?has_content>
    <div class="list">
        <ul>
            <li>
                <p class="txt">时间</p>
                <p class="item">${meetingRecord.workTime?string('yyyy-MM-dd')!''}&nbsp;</p>
            </li>
            <li>
                <p class="txt">会议级别</p>
                <p class="item">
                    <#if meetingRecord.meetingType == 'PROVINCE_LEVEL'>省级
                    <#elseif meetingRecord.meetingType == 'CITY_LEVEL'>市级
                    <#elseif meetingRecord.meetingType == 'COUNTY_LEVEL'>区级
                    <#elseif meetingRecord.meetingType == 'SCHOOL_LEVEL'>校级
                    </#if>
                </p>
            </li>
            <li>
                <#if meetingRecord.meetingType == 'SCHOOL_LEVEL'>
                    <p class="txt">学校名称</p>
                    <p class="item"> ${meetingRecord.schoolName!''}&nbsp;</p>
                <#else>
                    <p class="txt">主题</p>
                    <p class="item">${meetingRecord.workTitle!''}&nbsp;</p>
                </#if>
            </li>
            <li>
                <p class="txt">签到地址</p>
                <p class="item">
                    <#if meetingRecord.address??>${meetingRecord.address!''}</#if>
                    &nbsp;
                </p>
            </li>
            <#if meetingRecord.meetingType == 'SCHOOL_LEVEL'>
                <li>
                    <p class="txt">参会老师</p>
                    <p class="item">
                        <#if meetingRecord.visitTeacherList?? && meetingRecord.visitTeacherList?size gt 0>
                            <#list meetingRecord.visitTeacherList as list>
                            ${list.teacherName!""}<#--<#if list.teacherName?has_next>、</#if>-->
                            </#list>
                        </#if>
                        &nbsp;
                    </p>
                </li>
                <#else>
                <li>
                    <p class="txt">参会人数</p>
                    <p class="item">
                    ${meetingRecord.meeteeCount!''}
                        &nbsp;
                    </p>
                </li>
                <li>
                    <p class="txt">教研员</p>
                    <p class="item">
                        <#if meetingRecord.researchersName?has_content>
                        ${meetingRecord.researchersName!''}
                        </#if>
                        &nbsp;
                    </p>
                </li>
                <li>
                    <p class="txt">教研员是否在场</p>
                    <p class="item">
                        <#if meetingRecord.instructorAttend?? && meetingRecord.instructorAttend>
                            是
                        <#else>
                            否
                        </#if>
                        &nbsp;
                    </p>
                </li>
            </#if>
            <li>
                <p class="txt">讲师</p>
                <p class="item">
                ${meetingRecord.meetingNote!''}
                    &nbsp;
                </p>
            </li>
            <li>
                <p class="txt">宣讲时长</p>
                <p class="item">
                    <#if meetingRecord.meetingTime == 1>小于15分钟
                    <#elseif meetingRecord.meetingTime == 2>15-60分钟
                    <#elseif meetingRecord.meetingTime == 3>大于1个小时
                    </#if>
                    &nbsp;
                </p>
            </li>
            <li>
                <p class="txt">类型</p>
                <p class="item">
                    <#if meetingRecord.showFrom?has_content && meetingRecord.showFrom == 1>专场
                    <#elseif meetingRecord.showFrom?has_content && meetingRecord.showFrom == 2>插播
                    </#if>
                    &nbsp;
                </p>
            </li>
            <li>
                <p class="txt">现场照片</p>
                <p class="item">
                    <img class="show_img" src="<#if meetingRecord.scenePhotoUrl?has_content>${meetingRecord.scenePhotoUrl!''}?x-oss-process=image/auto-orient,1</#if>" width="100%" height="100%" >
                </p>
            </li>
            <li>
                <p class="txt">会议内容及效果</p>
                <p class="item">
                    <#if meetingRecord.workContent?has_content>${meetingRecord.workContent!''}</#if>
                </p>
            </li>
            <li>
                <p class="txt">其他参与人</p>
                <p class="item">
                <#if joinMeetingRecords?has_content>
                    <#list joinMeetingRecords as list>
                        <span class="js-showJoinMeetingRecord" data-id="${list.id!0}">${list.workerName!''}<span style="float:right">&gt;</span></span>
                    </#list>
                </#if>
                </p>
            </li>
        </ul>
    </div>
    <div class="img_info" style="width:100%;height:100%;display: none;position:fixed;top:0;text-align: center;;background:rgba(0 ,0 ,0 ,1)">
        <img style="max-width: 100%;height:auto" src="<#if meetingRecord.scenePhotoUrl?has_content>${meetingRecord.scenePhotoUrl!''}?x-oss-process=image/auto-orient,1</#if>" >
    </div>
    </#if>
<script>
   $(document).on("click",".js-showJoinMeetingRecord",function () {
        openSecond("/mobile/work_record/showJoinMeetingRecord.vpage?recordId="+$(this).data("id"))
    });
   $(document).on("click",".show_img",function () {
        $('.img_info').show();
    });
    $(document).on("click",'.img_info',function(){
        $('.img_info').hide()
    })
</script>
</@layout.page>

