<#import "../layout_new.ftl" as layout>
<@layout.page group="搜索" title="班级信息">
<div class="mobileCRM-V2-header">
    <div class="inner">
        <div class="box">
            <div class="headerBack"><a href="/mobile/school/statistics_clazz.vpage?schoolId=${schoolId}&clazzLevel=${clazzLevel}&type=${type}">&lt;&nbsp;返回</a></div>
            <div class="headerText">班级信息</div>
        </div>
    </div>
</div>
<#if data??>
<div class="mobileCRM-V2-box mobileCRM-V2-info mobileCRM-V2-mt">
    <div class="message">
        <div class="class"></div>
        <div class="name"><#if data.statisticsData??>${data.statisticsData.name!''}</#if></div>
        <div class="name side-gray">班级ID：<#if data.statisticsData??>${data.statisticsData.clazzId!''}</#if></div>
        <div class="id" style="color:#999;"><#if data.statisticsData??><a href="/mobile/school/school_info.vpage?schoolId=${data.statisticsData.schoolId!''}">${data.schoolName!''}</a></#if></div>
    </div>
</div>
<div class="mobileCRM-V2-box mobileCRM-V2-info mobileCRM-V2-mt">
    <a href="javascript:void(0);">
        <div class="hdLink">注册学生 <span style="float: right; padding:0 20px; font-size: 20px; color: #666;"><#if data.statisticsData?? && data.statisticsData.totalCount??>${data.statisticsData.totalCount!}</#if></span></div>
        <div class="list">
            <div>
                <div class="hd red"><#if data.statisticsData?? && data.statisticsData.totalCount?? && data.statisticsData.usedCount??>${data.statisticsData.totalCount - data.statisticsData.usedCount}</#if></div>
                <div class="ft">未使用</div>
            </div>
            <div>
                <div class="hd red"><#if data.statisticsData?? && data.statisticsData.usedCount?? && data.statisticsData.authedCount??>${data.statisticsData.usedCount - data.statisticsData.authedCount}</#if></div>
                <div class="ft">使用未认证</div>
            </div>
            <div>
                <div class="hd red"><#if data.statisticsData?? && data.statisticsData.authedCount??>${data.statisticsData.authedCount!}</#if></div>
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
                <div class="side-fr side-orange"><#if data.statisticsData?? && data.statisticsData.hcaActiveCount??>${data.statisticsData.hcaActiveCount!}</#if></div>
            </div>
        </li>
        <li>
            <div class="box">
                <div class="side-fl ">双科认证学生</div>
                <div class="side-fr side-orange"><#if data.statisticsData?? && data.statisticsData.doubleSubjectCount??>${data.statisticsData.doubleSubjectCount!}</#if></div>
            </div>
        </li>
    </ul>
</div>

<div class="mobileCRM-V2-box mobileCRM-V2-info mobileCRM-V2-mt">
    <div class="hdLink">老师名单</div>
    <ul class="mobileCRM-V2-list">
        <#if data.groupTeacherList??>
            <#list data.groupTeacherList as item>
                <li>
                    <a href="/mobile/school/group_info.vpage?schoolId=${schoolId}&clazzLevel=${clazzLevel}&clazzId=${clazzId}&groupId=${item.groupId!}&type=${type!}" class="link link-ico">
                        <div class="side-fl ">${item.realName!''}&nbsp;&nbsp;
                            <span class="id" style="background-color: #67cd67; display: inline-block; color: #fff; font-size: 12px; vertical-align: middle; padding: 0 4px; line-height: 20px;">
                                <#if item.subject?? && item.subject.value?? && item.subject.value?length gt 0>${item.subject.value[0..0]}</#if>
                            </span>
                        </div>
                    </a>
                </li>
            </#list>
        </#if>
    </ul>
</div>


<script>


    $(function () {

    });
</script>
</#if>
</@layout.page>