<#import "../module.ftl" as com>
<@com.page t=2 s=6>
<ul class="breadcrumb_vox">
    <li><a href="javascript:void(0);">报 告</a> <span class="divider">/</span></li>
    <li>试卷全区成绩对比 <span class="divider">/</span></li>
    <li class="active">
        ${paperName}
    </li>
</ul>

<a style="float: right;" class="btn_vox btn_vox_primary edge_vox_tb" href="/rstaff/report/index.vpage">
    重新选择试卷
</a>
<table class="table_vox table_vox_bordered table_vox_striped">
    <thead>
    <tr>
        <td></td>
        <td>区平均</td>
        <td>${bizExamPaperRegionStat.avgScore}</td>
        <td>${bizExamPaperRegionStat.listeningScoreAvg}</td>
        <td>${bizExamPaperRegionStat.writtenScoreAvg}</td>
        <#assign completeTime = (bizExamPaperRegionStat.completeTimeAvg/1000)?int/>
        <td>${(completeTime/60)?int}分${(completeTime%60)?int}秒</td>
    </tr>
    <tr>
        <td>排名</td>
        <td>学校 </td>
        <td>成绩</td>
        <td>听力成绩</td>
        <td>笔试成绩</td>
        <td>完成时间</td>
    </tr>
    </thead>
    <tbody>
        <#list examPaperRegionSchoolStats as regionSchoolStat>
            <tr>
                <td>${regionSchoolStat_index + 1}</td>
                <td>${regionSchoolStat.schoolName}</td>
                <td>${regionSchoolStat.avgScore}</td>
                <td>${regionSchoolStat.listeningScoreAvg}</td>
                <td>${regionSchoolStat.writtenScoreAvg}</td>
                <#assign completeTime = (regionSchoolStat.completeTimeAvg/1000)?int/>
                <td>${(completeTime/60)?int}分${(completeTime%60)?int}秒</td>
            </tr>
        </#list>
    </tbody>
</table>
</@com.page>