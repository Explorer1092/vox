<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=9>
<script src="${requestContext.webAppContextPath}/public/js/pagenation.js"></script>
<div class="span9">
    <fieldset>
        <legend>古诗配置</legend>
    </fieldset>

    <form id="op-query" class="form-horizontal" method="get"
          action="">
        <input type="hidden" id="pageNum" name="page" value="${page!'1'}"/>
        <div>
            <span style="white-space: nowrap;">
                古诗ID：<input type="text" id="poetryId" name="poetryId" style="width: 100px;" value="${poetryId!}"/>
            </span>
            <span style="white-space: nowrap;">
                古诗名称：<input type="text" id="poetryName" name="poetryName" value="${poetryName!''}"/>
            </span>
            <span style="white-space: nowrap;">
                古诗描述：<input type="text" id="poetryDesc" name="poetryDesc" value="${poetryDesc!''}"/>
            </span>
            <button class="btn btn-primary" type="button" id="searchBtn">查询</button>
        </div>
    </form>
    <a class="btn btn-primary" target="_blank" href="/opmanager/poetry/poetry_create_or_view.vpage?edit=1">新建古诗</a>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <table class="table table-hover table-striped table-bordered">
                    <thead>
                    <tr>
                        <th>古诗ID</th>
                        <th>古诗名称</th>
                        <th>古诗描述</th>
                        <th>创建人</th>
                        <th>操作</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#if poetryList?? && poetryList?size gt 0>
                            <#list poetryList as  poetryObj>
                            <tr>
                                <td>${poetryObj.id!}</td>
                                <td>${poetryObj.title!}</td>
                                <td>${poetryObj.description!}</td>
                                <td>${poetryObj.createUserId!}</td>
                                <td>
                                    <a class="btn btn-primary" target="_blank" href="/opmanager/poetry/poetry_create_or_view.vpage?poetryId=${poetryObj.id!}">详情</a>
                                    <a class="btn btn-success" target="_blank" href="/opmanager/poetry/poetry_create_or_view.vpage?edit=1&poetryId=${poetryObj.id!}">修改</a>
                                    <#--<a class="btn btn-warning" href="/opmanager/studyTogether/template/change_log_list.vpage?template_id=${template.id!''}&change_log_type=PictureBookTemplate">日志</a>-->
                                </td>
                            </tr>
                            </#list>
                        <#else >
                        <tr>
                            <td colspan="5" style="text-align: center">暂无数据</td>
                        </tr>
                        </#if>
                    </tbody>
                </table>
                <ul class="message_page_list">
                </ul>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript">
    $(function () {
        $(".message_page_list").page({
            total: ${totalPage!1},
            current: ${page!1},
            autoBackToTop: false,
            maxNumber: 20,
            jumpCallBack: function (index) {
                $("#pageNum").val(index);
                $("#op-query").submit();
            }
        });

        $("#searchBtn").on('click', function () {
            $("#pageNum").val(1);
            $("#op-query").submit();
        });
    });
</script>
</@layout_default.page>