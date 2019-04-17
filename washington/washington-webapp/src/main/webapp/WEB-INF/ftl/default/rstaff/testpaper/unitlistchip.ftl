<#if bookExamPaperList?? && bookExamPaperList?size gt 0>
    <ul class="putToTest">
        <#list bookExamPaperList as mapper>
            <li>
                <p>${mapper.examPaperName!''}</p>
                <a target="_blank" href="/rstaff/exampaper/preview/${mapper.examPaperId!'0'}.vpage" class="btn_vox btn_vox_small" style="width: 60px;">预览</a>
            </li>
        </#list>

    </ul>
    <#--<div class="common_pagination message_page_list" style="float: right; display: none;"></div>-->
<#else>
   <span>未查询到结果</span>
</#if>

<#--//查看已组试卷分页-->
<#--$(".message_page_list").page({-->
<#--total           : 10,-->
<#--current         : 1,-->
<#--jumpCallBack    : function(index){-->


<#--}-->
<#--});-->