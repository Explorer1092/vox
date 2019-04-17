<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=9>
<script src="${requestContext.webAppContextPath}/public/js/pagenation.js" xmlns="http://www.w3.org/1999/html"></script>
<div class="span9">
    <fieldset>
        <legend><font color="#00bfff">学习币商城</font>/操作日志</legend>
    </fieldset>
    <form id="commodity-query" class="form-horizontal" method="get"
          action="${requestContext.webAppContextPath}/opmanager/commodity/commodityLog.vpage">
        <input type="hidden" id="pageNum" name="page" value="${currentPage!'1'}"/>
        <input type="hidden" id="commodityId" name="id" value="${commodityId!'0'}"/>
    </form>

    <div class="row-fluid">
        <div class="span12">
            <div class="well" style="font-size: 12px;">
                <table class="table table-striped table-bordered">
                    <thead>
                    <tr>
                        <th>操作内容</th>
                        <th>操作时间</th>
                        <th>操作人</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#if logPage?? && logPage.content??>
                            <#list logPage.content as log>
                                <tr>
                                    <td>
                                        <#if log.opList??>
                                            <#list log.opList as op>
                                                <label>${op}</label>
                                            </#list>
                                        </#if>
                                    </td>
                                    <td>${log.date!''}</td>
                                    <td>${log.operator!''}</td>
                                </tr>
                            </#list>
                        </#if>
                    </tbody>
                </table>
                <ul class="pager">
                    <li><a href="#" onclick="pagePost(1)" title="Pre">首页</a></li>
                    <#if hasPrev>
                        <li><a href="#" onclick="pagePost(${currentPage-1})" title="Pre">&lt;</a></li>
                    <#else>
                        <li class="disabled"><a href="#">&lt;</a></li>
                    </#if>
                    <li class="disabled"><a>第 ${currentPage!} 页</a></li>
                    <li class="disabled"><a>共 <#if totalPage==0>1<#else>${totalPage!}</#if> 页</a></li>
                    <#if hasNext>
                        <li><a href="#" onclick="pagePost(${currentPage+1})" title="Next">&gt;</a></li>
                    <#else>
                        <li class="disabled"><a href="#">&gt;</a></li>
                    </#if>
                    <li><a href="#" onclick="pagePost(${totalPage!})" title="Pre">尾页</a></li>
                </ul>
            </div>
        </div>
    </div>
</div>


<script type="text/javascript">
    function pagePost(pageNumber) {
        $("#pageNum").val(pageNumber);
        $("#commodityId").val(${commodityId!0});
        $("#commodity-query").submit();
    }
</script>
</@layout_default.page>