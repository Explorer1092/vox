<#import "../../layout_default.ftl" as layout_default />
<#import "../pager.ftl" as pager />
<@layout_default.page page_title="Mizar Manager" page_num=17>
<div id="main_container" class="span9">
    <legend>
        <strong>品牌管理</strong>
        <a title="添加" href="info.vpage" class="btn btn-success" style="float: right;">
            <i class="icon-plus icon-white"></i> 添加品牌
        </a>
    </legend>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <form id="query_frm" class="form-horizontal" method="get" action="index.vpage">
                    <input type="hidden" id="page" name="page" value="${currentPage!'1'}"/>
                    <ul class="inline">
                        <li>
                            品牌名称：<input type="text" id="brand" name="brand" value="<#if brand??>${brand!}</#if>" placeholder="输入品牌名称">
                        </li>
                        <li>
                            <button type="submit" id="filter" class="btn btn-primary">
                                <i class="icon-search icon-white"></i> 查  询
                            </button>
                        </li>
                    </ul>
                </form>
                <@pager.pager/>
                <table class="table table-striped table-bordered">
                    <thead>
                    <tr>
                        <th style="text-align: center;">LOGO</th>
                        <th style="text-align: center; width: 200px;">名称</th>
                        <th style="text-align: center;">介绍</th>
                        <th style="text-align: center; width: 100px;">操作</th>
                    </tr>
                    </thead>
                    <tbody>
                    <#if brandList?? && brandList?has_content>
                        <#list brandList as brand>
                        <tr>
                            <td><img alt="无Logo" src="${brand.brandLogo!}" class="img-rounded"></td>
                            <td>${brand.brandName!""}</td>
                            <td><pre>${brand.introduction!""}</pre></td>
                            <td>
                                <a href="info.vpage?bid=${brand.id!}" class="btn btn-info">
                                    <i class="icon-pencil icon-white"></i> 编  辑
                                </a>
                            </td>
                        </tr>
                        </#list>
                    <#else>
                        <tr><td colspan="4" style="text-align: center;"><strong>No Data Found</strong></td></tr>
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