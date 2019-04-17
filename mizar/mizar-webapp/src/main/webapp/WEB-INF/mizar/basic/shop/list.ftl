<#import "../../module.ftl" as module>
<#import "../../common/pager.ftl" as pager />
<@module.page
title="机构管理"
pageJsFile={"siteJs" : "public/script/basic/shop"}
pageJs=["siteJs"]
leftMenu="机构管理"
>
<div class="op-wrapper orders-wrapper clearfix">
    <form id="query-form" class="form-table" action="/basic/shop/index.vpage" method="get">
        <input name="pageIndex" id="pageIndex" value="${currentPage!1}" type="hidden">
        <div class="item">
            <p>机构名称</p>
            <input value="${shopName!}" name="shopName" class="v-select" placeholder="请输入机构名称"/>
        </div>
        <#if !currentUser.isShopOwner()>
        <div class="item">
            <p>合作机构</p>
            <select name="cooperator" class="v-select">
                <option value="">全部</option>
                <option <#if cooperator?? && cooperator>selected</#if> value="1">是</option>
                <option <#if cooperator?? && !cooperator>selected</#if> value="0">否</option>
            </select>
        </div>
        <div class="item">
            <p>VIP机构</p>
            <select name="vip" class="v-select">
                <option value="">全部</option>
                <option <#if vip?? && vip>selected</#if> value="1">是</option>
                <option <#if vip?? && !vip>selected</#if> value="0">否</option>
            </select>
        </div>
        <div class="item" style="width:auto; float: right;">
            <p style="color:transparent;">.</p>
            <a class="blue-btn" href="/basic/shop/add.vpage">新增机构</a>
        </div>
        </#if>
        <div class="item" style="width:auto;">
            <p style="color:transparent;">.</p>
            <a class="blue-btn" id="query-btn" style="float:left;" href="javascript:void(0)">搜索</a>
        </div>
    </form>
</div>
<table class="data-table one-page displayed">
    <thead>
        <tr>
            <th>名称</th>
            <th>类型</th>
            <#if !currentUser.isShopOwner()>
            <th>是否合作机构</th>
            <th>是否付费商家</th>
            </#if>
            <th>状态</th>
            <th style="width: 150px;">操作</th>
        </tr>
    </thead>
    <tbody>
        <#if shopList?? && shopList?has_content>
            <#list shopList as shop>
            <tr>
                <td>${shop.fullName!''}</td>
                <td>${shop.shopType!''}</td>
                <#if !currentUser.isShopOwner()>
                <td><#if shop.cooperator??>${shop.cooperator?string('是','否')}<#else>否</#if></td>
                <td><#if shop.vip??>${shop.vip?string('是','否')}<#else>否</#if></td>
                </#if>
                <td>
                    <#if shop.shopStatus == 'ONLINE'>在线
                    <#elseif shop.shopStatus == 'OFFLINE'>离线
                    <#elseif shop.shopStatus == 'PENDING'>待审核
                    <#else>离线</#if>
                </td>
                <td>
                    <#--<#if changeShopIdList?? && !changeShopIdList?seq_contains("${shop.id}")>-->
                        <#if shop.shopStatus != 'ONLINE'>
                        <a class="op-btn" href="/basic/shop/edit.vpage?id=${shop.id!''}" style="margin-right:0;">编辑</a>
                        &nbsp; &nbsp;
                        </#if>
                    <#--</#if>-->
                    <a class="op-btn " href="/basic/shop/edit.vpage?type=detail&id=${shop.id!''}" style="margin-right:0;">详情</a>&nbsp; &nbsp;
                    <#--运营人员有上下线的操作权限-->
                    <#if currentUser.isOperator()>
                        <#if shop.shopStatus == 'ONLINE'>
                            <a class="op-btn op-status" data-status="OFFLINE" data-sid="${shop.id!}" href="javascript:void(0);" style="margin-right:0;float: right;">下线</a>
                        <#else>
                            <a class="op-btn op-status" data-status="ONLINE" data-sid="${shop.id!}" href="javascript:void(0);" style="margin-right:0;float: right;">上线</a>
                        </#if>
                    </#if>
                </td>
            </tr>
            </#list>
        <#else>
        <tr><td colspan="<#if currentUser.isShopOwner()>4<#else>6</#if>" style="text-align: center;"><strong>没有数据</strong></td></tr>
        </#if>
    </tbody>
</table>
<div id="paginator" data-startPage="${currentPage!1}" class="paginator clearfix" totalPage="${totalPage!1}" currentPage="${currentPage!1}"></div>
</@module.page>

