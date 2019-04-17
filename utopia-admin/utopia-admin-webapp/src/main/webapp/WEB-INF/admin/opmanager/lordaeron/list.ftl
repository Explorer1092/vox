<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='商品详情页配置' page_num=9>
<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<div id="main_container" class="span9">
    <legend style="font-weight: 700;">
        商品详情页配置
    </legend>
    <div class="row-fluid">
        <div class="span12">
            <ul class="inline">
                <li>
                    <a href="detail.vpage">新建商品页</a>
                </li>
            </ul>
        </div>
        <table class="table table-striped table-bordered">
            <thead>
            <tr>
                <th>商品分类</th>
                <th>商品名称</th>
                <th>副标题</th>
                <th width="400px">地址</th>
                <th>创建时间</th>
                <th>操作</th>
            </tr>
            </thead>
            <tbody>
            <#list  maps as map>
            <tr>
                <td>${map.productType!''}</td>
                <td>${map.productName!''}</td>
                <td>${map.subhead!''}</td>
                <td>${map.url!''}</td>
                <td>${map.updateTime!''}</td>
                <td><a href="detail.vpage?id=${map.id!''}">编辑</a> <button data-value="${map.id!''}" data-status="${map.status!''}" data-action="changeStatus">${map.button}</button></td>
            </tr>
            </#list>
            </tbody>
        </table>
        <ul class="pager" data-index="${pageIndex}">
            <#if pageCount?exists && pageCount gt 0>
                <#if pageIndex?exists && pageIndex gt 10>
                    <li data-index="1"><a href="${query}1">首页</a></li>
                    <li data-index="${start - 1}"><a href="${query}${start - 1}">前十页</a></li>
                </#if>
                <#list start .. end as page>
                    <li data-index="${page}"><a href="${query}${page}">${page}</a></li>
                </#list>
                <#if pageIndex?exists && pageCount gt 10 && pageIndex lt (pageCount / 10) * 10>
                    <li data-index="${end + 1}"><a href="${query}${end + 1}">后十页</a></li>
                    <li data-index="${pageCount}"><a href="${query}${pageCount}">尾页</a></li>
                </#if>
            </#if>

        </ul>
    </div>
</div>

<script language="javascript" type="application/javascript">
    $(document).ready(function () {
        $("[data-action='changeStatus']").click(function () {
            if(confirm("are you sure")) {
                var id = $(this).attr("data-value");
                var status = $(this).attr("data-status");
                $.post("changeStatus.vpage", {"id": id, "status": status}, function () {
                    window.location.href = location.href;
                });
            }
        });
    })
</script>

</@layout_default.page>