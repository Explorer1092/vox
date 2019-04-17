<#import "../layout_new.ftl" as layout>
<@layout.page group="搜索" title="班级信息">
<div class="mobileCRM-V2-header">
    <div class="inner">
        <div class="box">
            <div class="headerBack"><a href="javascript:window.history.back()">&lt;&nbsp;返回</a></div>
            <div class="headerText">班组信息</div>
        </div>
    </div>
</div>
<#if statisticsData??>
<div class="mobileCRM-V2-box mobileCRM-V2-info mobileCRM-V2-mt">
    <div class="message">
        <div class="group"></div>
        <div class="name"><#if statisticsData??>${statisticsData.name!''}</#if></div>
        <div class="name">班组ID：${groupId!''}</div>
        <div class="id" >
            <#if groupTeacherData??>
                <a href="/mobile/teacher/v2/teacher_info.vpage?teacherId=${groupTeacherData.teacherId!}">${groupTeacherData.realName!''}
                    <span class="id" style="background-color: #67cd67; display: inline-block; color: #fff; font-size: 12px; vertical-align: middle; padding: 0 4px; line-height: 20px;">
                        <#if groupTeacherData.subject?? && groupTeacherData.subject.value?? && groupTeacherData.subject.value?length gt 0>${groupTeacherData.subject.value[0..0]}</#if>
                    </span>
                </a>
            </#if>
        </div>
    </div>
</div>
<div class="mobileCRM-V2-box mobileCRM-V2-info mobileCRM-V2-mt">
    <a href="javascript:void(0);">
        <div class="hdLink">注册学生 <span style="float: right; padding:0 20px; font-size: 20px; color: #666;"><#if statisticsData.totalCount??>${statisticsData.totalCount!}</#if></span></div>
        <div class="list">
            <div>
                <div class="hd red"><#if statisticsData.totalCount?? && statisticsData.usedCount??>${statisticsData.totalCount - statisticsData.usedCount}</#if></div>
                <div class="ft">未使用</div>
            </div>
            <div>
                <div class="hd red"><#if statisticsData.usedCount?? && statisticsData.authedCount??>${statisticsData.usedCount - statisticsData.authedCount}</#if></div>
                <div class="ft">使用未认证</div>
            </div>
            <div>
                <div class="hd red"><#if statisticsData.authedCount??>${statisticsData.authedCount!}</#if></div>
                <div class="ft">已认证</div>
            </div>
        </div>
    </a>
</div>
<div class="mobileCRM-V2-box mobileCRM-V2-info mobileCRM-V2-mt">
    <ul class="mobileCRM-V2-list">
        <li>
            <div class="box">
                <div class="side-fl ">本月高质量学生</div>
                <div class="side-fr side-orange"><#if statisticsData.hcaActiveCount??>${statisticsData.hcaActiveCount!}</#if></div>
            </div>
        </li>
        <li>
            <div class="box">
                <div class="side-fl ">双科认证学生</div>
                <div class="side-fr side-orange"><#if statisticsData.doubleSubjectCount??>${statisticsData.doubleSubjectCount!}</#if></div>
            </div>
        </li>
    </ul>
</div>

<script>


    $(function () {

    });
</script>
</#if>
</@layout.page>