<#import "../module.ftl" as com>
<@com.page t=2 s=6>
<ul class="breadcrumb_vox">
    <li><a href="javascript:void(0);">报 告</a> <span class="divider">/</span></li>
    <li class="active">试卷全区成绩对比</li>
</ul>
<table class="table_vox table_vox_bordered table_vox_striped">
    <thead>
    <tr>
        <td>试卷名称</td>
        <td>创建时间</td>
        <td>题 量</td>
        <td>全区成绩对比</td>
    </tr>
    </thead>
    <tbody>
        <#if examPaperForUnits?? && examPaperForUnits?has_content>
            <#list  examPaperForUnits as e>
                <tr>
                    <th>${(e.name)!'--'}</th>
                    <th>${(e.createAt)!}</th>
                    <td>${(e.questionNum)!''}</td>
                    <th><a id="research_but" class="btn_vox btn_vox_small" href="/rstaff/report/viewpaperdetail.vpage?paperId=${e.id}"> 查 看 </a></th>
                </tr>
            </#list>
        <#else>
            <tr>
                <th colspan="4" class="text_gray_9">暂无数据</th>
            </tr>
        </#if>
    </tbody>
</table>
</@com.page>