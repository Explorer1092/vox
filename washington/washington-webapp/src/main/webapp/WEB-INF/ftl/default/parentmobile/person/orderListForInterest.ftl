<#import '../layout.ftl' as layout>

<#include "../testPay.ftl">

<@layout.page className='OrderList' pageJs="second" title="趣味学习订单列表"  extraJs=extraJs![] specialCss="skin2" specialHead='
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, user-scalable=no" />
    <meta name="format-detection" content="telephone=no" />
    <meta name="format-detection" content="email=no" />
    <meta name="apple-mobile-web-app-status-bar-style" content="black" />
    <title>趣味学习订单列表</title>
'>
<script><#--改版的样式，不适用adapt-->
    window.notUseAdapt=true;
</script>
	<#escape x as x?html>
      <div class="orderList-box doTabBlock do_orderlist_page">
        <div class="ol-tab"> <!--相当老的p标签-->
            <#assign  baseUrl = "/parentMobile/ucenter/doOrderlist.vpage?_=1" >

            <#assign tabInfo = [
                {
                    "name" : '未支付',
                    "data" : {
                    "ajaxUrl"  : '${baseUrl}&t=unPaid',
                    "tabTargetEl" : '#tabContent',
                    "tabTemplateEl" : '#orderListTemp'
                    }
                },
                {
                    "name" : '已开通',
                    "data" : {
                        "ajaxUrl"  : '${baseUrl}&t=paid',
                        "tabTargetEl" : '#tabContent',
                        "tabTemplateEl" : '#orderListTemp'
                    }
                }
            ]>
            <#assign isPaid = isPaid!"1">

            <#if isPaid == "1">
                <#assign tabInfo = tabInfo?reverse>
            </#if>

            <#list tabInfo as tab >
                <span
                        class="doTab <#if tab_index == 0 >active</#if>"
                        data-tab_ajax_url = "${tab.data.ajaxUrl!''}"
                        data-tab_template_el = "${tab.data.tabTemplateEl!''}"
                        data-tab_target_el = "${tab.data.tabTargetEl!''}"
                        >${tab.name}
                </span>
            </#list>
        </div>


        <!--order list div-->
        <div id="tabContent">

        </div>
        </div>


        <!--order list template-->
        <script type="text/html" id="orderListTemp">
        <% if(!success){ %>
        <%= message %>
        <% } %>
        <div>
            <% if(orders.length == 0){ %>
                <div class="null-box">
                    <div class="no-order"></div>
                    <div class="null-text">暂无订单</div>
                </div>
            <% }else{ %>
            <%
            var isPaidTab = t === "paid";
            %>
            <% orders.forEach(function(order){ %>
            <div class="ol-table bg-fff">
                <table cellpadding="0" cellspacing="0" colspan="0">
                    <thead>
                        <tr>
                            <td colspan="4"><%= order.productName %></td>
                        </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td>订单号：<br> <%= order.id %> </td>
                        <td>价格</td>
                        <#--<td>有效期</td>-->
                        <td>操作</td>
                    </tr>
                    <tr>
                        <td><span class="name"><%= order.name %></span></td>
                        <td>￥<%=priceFormat(order.price, 2)%></td>
                        <#--<td> <%= order.period %>天 </td>-->
                        <td>
                            <% if(isPaidTab){ %>
                            已支付
                            <% }else{ %>
                            <a href="javascript:;" data-order_price="<%= order.price %>" data-order_id="<%= order.id %>" data-order_type = "<%= order.orderType %>"  class="${doPayClassName} unPay-btn">支付</a>
                            <% } %>
                        </td>
                    </tr>
                    <%if(order.productName.trim().search("索尼第三届") > -1){%>
                    <tr>
                        <td colspan="4">请登录17作业学生PC端进入参赛页面</td>
                    </tr>
                    <%}%>
                    </tbody>
                </table>
            </div>
            <% }); %>
            <div style="width:94%; margin:0 auto; text-align:center; padding-bottom:20px;" class="doTabAjaxPage">
                <% var pageBaseUrl = '${baseUrl}&t=' + t + '&pageIndex=' %>
                <% if(pageIndex> 1){ %>
                <a style="color:#41bb54;font-size:15px;margin:0 40px;" href="<%= pageBaseUrl %><%= pageIndex - 1 %>" class="ui-btn ui-btn-b ui-corner-all">上一页</a>
                <% } %>

                <% if(haveMore){ %>
                <a style="color:#41bb54;font-size:15px;margin:0 40px;" href="<%= pageBaseUrl %><%= +pageIndex + 1 %>" class="ui-btn ui-btn-b ui-corner-all">下一页</a>
                <% } %>
            </div>
            <% } %>
        </div>
      </script>
	</#escape>
</@layout.page>