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
                            品牌ID：<input type="text" id="bid" name="bid" value="<#if bid??>${bid}</#if>" placeholder="输入品牌ID">
                        </li>
                        <li>
                            POI名称：<input type="text" id="shop" name="shop" value="<#if shop??>${shop}</#if>" placeholder="输入POI名称">
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
                        <th style="text-align: center; width: 100px;">机构名称</th>
                        <th style="text-align: center; width: 100px;">所属商圈</th>
                        <th style="text-align: center;">机构介绍</th>
                        <th style="text-align: center; width: 80px;">机构类型</th>
                        <th style="text-align: center; width: 100px;">操作</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#if shopList?? && shopList?has_content>
                            <#list shopList as shop>
                            <tr>
                                <td>${shop.fullName}</td>
                                <td>${shop.tradeArea}</td>
                                <td><pre>${shop.introduction}</pre></td>
                                <td>${shop.shopType}</td>
                                <td>
                                    <a href="info.vpage?sid=${shop.id}" class="btn btn-info">
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
</@layout_default.page>