<#import "../../layout_default.ftl" as layout_default />
<#import "../pager.ftl" as pager />
<@layout_default.page page_title="Mizar Manager" page_num=17>
<div id="main_container" class="span9">
    <legend>
        <strong>机构管理</strong>
    </legend>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <form id="query_frm" class="form-horizontal" method="get" action="index.vpage">
                    <input type="hidden" id="page" name="page" value="${currentPage!'1'}"/>
                    <ul class="inline">
                        <li>
                            机构ID：<input type="text" id="shopId" name="shopId" value="<#if shopId??>${shopId}</#if>" placeholder="输入机构ID">
                        </li>
                    </ul>
                </form>
                <@pager.pager/>
                <table class="table table-striped table-bordered">
                    <thead>
                    <tr>
                        <th style="text-align: center; width: 100px;">用户ID</th>
                        <th style="text-align: center; width: 100px;">用户名称</th>
                        <th style="text-align: center; width: 100px;">评论星级</th>
                        <th style="text-align: center;">评论内容</th>
                        <th style="text-align: center; width: 100px;">操作</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#if ratingList?? && ratingList?has_content>
                            <#list ratingList as rating>
                            <tr>
                                <td>${rating.userId!''}</td>
                                <td>${rating.userName!''}</td>
                                <td>${rating.rating!0}</td>
                                <td><pre>${rating.ratingContent!''}</pre></td>
                                <td>
                                    <a href="info.vpage?ratingId=${rating.id!''}" class="btn btn-info">
                                        <i class="icon-pencil icon-white"></i> 编  辑
                                    </a>
                                </td>
                            </tr>
                            </#list>
                        <#else>
                        <tr><td colspan="5" style="text-align: center;"><strong>No Data Found</strong></td></tr>
                        </#if>
                    </tbody>
                </table>
                <@pager.pager/>
            </div>
        </div>
    </div>
</div>
<script>
    $(function () {

    });
</script>
</@layout_default.page>