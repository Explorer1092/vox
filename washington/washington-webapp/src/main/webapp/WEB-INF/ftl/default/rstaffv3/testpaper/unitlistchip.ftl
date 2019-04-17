<#if bookExamPaperList?? && bookExamPaperList?size gt 0>
<ul class="putToTest">
    <#list bookExamPaperList as mapper>
        <li>
            <p>${mapper.examPaperName!''}</p>
            <a target="_blank" href="/rstaff/exampaper/preview.vpage?paperId=${mapper.examPaperId!'0'}" class="btn_vox btn_vox_small" style="width: 60px;">预览</a>
        </li>
    </#list>

</ul>
<#else>
<div style="border-bottom: 1px solid #ccdbea; padding: 15px; margin-bottom: 15px;">
    <span>未查询到结果</span>
</div>
</#if>
