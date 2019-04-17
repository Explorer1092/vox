<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='大咖讲座' page_num=9>
<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<div id="main_container" class="span9">
    <legend style="font-weight: 700;">
        大咖讲座
    </legend>
    <div class="row-fluid">
        <div class="span12">
            <ul class="inline">
                <form class="well form-horizontal" method="get" action="?">
                    <table>
                        <tr>
                            <td>
                                <input type="text" name="title" value="${title!''}" placeholder="标题"/>
                            </td>
                            <td>
                                <select name="onlineStatus" id="onlineStatus" data-value="${online!''}">
                                    <option value="">上线状态</option>
                                    <option value="true">上线</option>
                                    <option value="false">下线</option>
                                </select>
                            </td>
                            <td>
                                <input type="text" name="admin" value="${admin!''}" placeholder="创建人"/>
                            </td>
                            <td>
                                <button type="submit" class="btn-mini btn-primary">搜索</button>
                            </td>
                        </tr>
                    </table>

                </form>
            </ul>
            <ul class="inline">
                <a class="btn btn-primary btn-sm" href="details.vpage">创建课程</a>
            </ul>

        </div>
        <table class="table table-striped table-bordered">
            <thead>
            <tr>
                <th>课程ID</th>
                <th>课程名称</th>
                <th>期数</th>
                <th>入口开放时间</th>
                <th>入口结束时间</th>
                <th>创建人</th>
                <th>创建时间</th>
                <th>报名人数</th>
                <th>上线状态</th>
                <th>操作</th>
            </tr>
            </thead>
            <tbody>
            <#if telecasts?exists>
            <#list telecasts as telecase>
                <tr>
                    <td>${telecase.id!''}</td>
                    <td>${telecase.title!''}</td>
                    <td>${telecase.issue!''}</td>
                    <td>${telecase.start!''}</td>
                    <td>${telecase.end!''}</td>
                    <td>${telecase.admin!''}</td>
                    <td>${telecase.create!''}</td>
                    <td>${telecase.total!''}</td>
                    <td><#if telecase.status>已上线<#else>已下线</#if></td>
                    <td>
                        <a href="#" data-value="<#if telecase.status>下线<#else>上线</#if>" data-action="status" data-id="${telecase.id}">
                            <#if  telecase.status>下线<#else>上线</#if>
                        </a>
                        <a href="details.vpage?id=${telecase.id}">编辑</a>
                        <#if telecase.status == false>
                        <a href="#" data-action="delete" data-value="${telecase.id}">删除</a>
                        </#if>
                    </td>
                </tr>
            </#list>
            </#if>
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

<script language="JavaScript" type="application/javascript">
    $(document).ready(function () {
        $("#onlineStatus").val($("#onlineStatus").attr("data-value"));

        $("[data-action='status']").click(function () {
            var value = $(this).attr("data-value");
            var str = value == "上线"?"确认上线？":"确认下线？下线后用户将无法看到课程";
            if(confirm(str)){
                var id = $(this).attr("data-id");
                $.post("online.vpage", {"id": id}, function (result) {
                    document.location.reload();
                });
            }
        });

        $("[data-action='delete']").click(function () {
            if(confirm("确认删除？删除后将无法保存创建的信息")){
                var id = $(this).attr("data-value");
                $.post("delete.vpage", {"id": id}, function (result) {
                    document.location.reload();
                });
            }
        });
    })
</script>

</@layout_default.page>