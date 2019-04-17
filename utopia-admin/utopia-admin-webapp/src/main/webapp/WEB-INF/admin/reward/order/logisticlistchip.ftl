<ul class="pager">
<#if (logisticsPage.hasPrevious())>
    <li><a href="#" onclick="pagePost(${pageNumber-1})" title="Pre">上一页</a></li>
<#else>
    <li class="disabled"><a href="#">上一页</a></li>
</#if>
<#if (logisticsPage.hasNext())>
    <li><a href="#" onclick="pagePost(${pageNumber+1})" title="Next">下一页</a></li>
<#else>
    <li class="disabled"><a href="#">下一页</a></li>
</#if>
    <li>当前第 ${pageNumber!} 页 |</li>
    <li>共 ${logisticsPage.totalPages!} 页</li>
</ul>
<div id="data_table_journal">
    <table class="table table-striped table-bordered">
        <tr>
            <td>快递单ID</td>
            <td>物流公司</td>
            <td>物流单号</td>
            <td>类型</td>
            <td>配送方式</td>
            <td>是否导回</td>
            <td>物流价格</td>
            <td>收货人</td>
            <#--<td>收货人电话</td>-->
            <td>学校名称</td>
            <td>省市区</td>
            <td>详细地址</td>
        </tr>
    <#if logisticsPage.content?? >
        <#list logisticsPage.content as order >
            <tr>
                <td>${order.id!}</td>
                <td>${order.companyName!}</td>
                <td>${order.logisticNo!}</td>
                <td>${order.type.description!}</td>
                <td>${order.logisticType!}</td>
                <td>
                    <#if order.isBack!false>
                        是
                    <#else>
                        否
                    </#if>
                </td>
                <td>${order.price!}</td>
                <td>${order.receiverName!}(${order.receiverId!})</td>
                <td>${order.schoolName!}</td>
                <td>${order.provinceName!} ${order.cityName!} ${order.countyName!}</td>
                <td>${order.detailAddress!}</td>
            </tr>
        </#list>
    </#if>
    </table>
</div>
<ul class="pager">
<#if (logisticsPage.hasPrevious())>
    <li><a href="#" onclick="pagePost(${pageNumber-1})" title="Pre">上一页</a></li>
<#else>
    <li class="disabled"><a href="#">上一页</a></li>
</#if>
<#if (logisticsPage.hasNext())>
    <li><a href="#" onclick="pagePost(${pageNumber+1})" title="Next">下一页</a></li>
<#else>
    <li class="disabled"><a href="#">下一页</a></li>
</#if>
    <li>当前第 ${pageNumber!} 页 |</li>
    <li>共 ${logisticsPage.totalPages!} 页</li>
</ul>

<script type="text/javascript">
    function pagePost(pageNumber){
        $('#logistic_list_chip').load('getlogisticlist.vpage',
                {   logisticId : $('#logisticId').val(),
                    logisticNo: $("#logisticNo").val(),
                    isBack: $("#isBack").val(),
                    month: $("#month").val(),
                    pageNumber : pageNumber
                }
        );
    }

</script>