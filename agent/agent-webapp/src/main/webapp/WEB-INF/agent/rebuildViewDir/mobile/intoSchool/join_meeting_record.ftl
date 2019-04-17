<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="参与组会详情" pageJs="" footerIndex=4 navBar="hidden">
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
                    <p class="txt">签到时间</p>
                    <p class="item">${joinMeetingRecord.workTime!''}&nbsp;</p>
                </li>
                <li>
                    <p class="txt">签到地址</p>
                    <p class="item">
                        <#if joinMeetingRecord.address??>${meetingRecord.address!''}</#if>&nbsp;
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
                    <p class="txt">会议组织人</p>
                    <p class="item">
                    ${meetingRecord.workerName!''}
                        &nbsp;
                    </p>
                </li>
                <li>
                    <p class="txt">会议内容及效果</p>
                    <p class="item">
                        <#if joinMeetingRecord.meetingNote?has_content>${joinMeetingRecord.meetingNote!''}</#if>
                    </p>
                </li>
            </ul>
        </div>
    </#if>
</@layout.page>
