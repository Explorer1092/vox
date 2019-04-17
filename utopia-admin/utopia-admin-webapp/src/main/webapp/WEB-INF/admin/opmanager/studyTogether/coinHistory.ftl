<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=9>
<script src="${requestContext.webAppContextPath}/public/js/pagenation.js"></script>
<div class="span9">
    <div>
        <form id="history-query" class="form-horizontal" method="get"
              action="${requestContext.webAppContextPath}/opmanager/studyTogether/coinHistory.vpage">
            <input type="hidden" id="pageNum" name="page" value="${currentPage!'1'}"/>
            <input type="hidden" id="studentId" name="studentId">
        </form>
        <span style="font-size: 24px">学习币累计记录</span>
        <table id="rewards" class="table table-hover table-striped table-bordered" style="margin-top: 20px">
            <thead>
            <tr>
                <th>学习币类型</th>
                <th>新增币数</th>
                <th>时间</th>
                <th>操作人</th>
            </tr>
            </thead>
            <tbody>
                <#if mapperPage?? && mapperPage.content??>
                    <#list mapperPage.content as mapper>
                        <tr>
                            <td>${mapper.coinType!}</td>
                            <td>${mapper.count!}</td>
                            <td>${mapper.createTime!}</td>
                            <td>${mapper.operator!'平台系统'}</td>
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

<script type="text/javascript">
    function pagePost(pageNumber) {
        $("#pageNum").val(pageNumber);
        $("#studentId").val(${studentId});
        $("#history-query").submit();
    }
</script>
</@layout_default.page>