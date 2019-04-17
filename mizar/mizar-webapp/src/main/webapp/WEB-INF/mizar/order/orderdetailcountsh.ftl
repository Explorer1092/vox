<#import "../module.ftl" as module>
<@module.page
title="单日订单详情查询"
leftMenu="单日订单详情查询"
pageJsFile={"orderdetailcount" : "public/script/basic/order/orderdetailcount"}
pageJs=["orderdetailcount"]
>
    <@app.script href="/public/plugin/jquery/jquery-1.7.1.min.js"/>
<style>
    a:hover {
        color: #ffffff !important;
    }

    .uploadBox .addBox img {
        width: 150px;
        height: 150px;
    }

    .form-horizontal .controls table th .control-label {
        float: left;
        margin-left: -180px;
        width: 160px;
        text-align: right;
    }

    .form-horizontal .controls table th label {
        text-align: left;
    }

    #colorpickerbox input {
        width: 50px;
    }

    input[type="text"] {
        height: 30px;
    }

    .articleClass a:hover {
        background: red
    }
</style>

<div class="op-wrapper clearfix">
    <span class="title-h1">一起作业—单日订单数据查询</span>
</div>

<div style="float: left;">
    <div class="item time-region" style="width: auto;">
        <div class="matManage-box">
            <div class="time-select input-control">
                <p>开始时间</p>
                <input title="" id="queryTime" class="v-select" value="${(queryDate!'')}"/>
                <input type="hidden" name="page" value="${pageIndex!1}" id="pageIndex">
                <input type="text" hidden value="SHANG_HAI" id="publisher_flag"/>
                <a id="detail_search" class="blue-btn" href="javascript:void(0)">查询</a>
            </div>
            <a id="download_data" class="blue-btn" href="javascript:void(0)">下载订单</a>
        </div>
    </div>

    <div>
        <table class="data-table">
            <thead>
            <tr>
                <th>订单ID</th>
                <th>用户姓名</th>
                <th>产品名称</th>
                <th>订单状态</th>
                <th>订单金额</th>
                <th>教材年级</th>
                <th>教材学期</th>
                <th>教材科目</th>
                <th>订单有效期（天）</th>
                <th>订单创建时间</th>
                <th>用户所在城市</th>
                <th>商品类型</th>
            </tr>
            </thead>
            <tbody>
                <#if returnList?? && returnList?size gt 0>
                    <#list returnList as orderDetail>
                    <tr>
                        <td>${orderDetail.orderId!''}</td>
                        <td style="width: 65px">${orderDetail.userName!''}</td>
                        <td>${orderDetail.productName!''}</td>
                        <td>${orderDetail.payStatus!''}</td>
                        <td>${orderDetail.payAmount!0}</td>
                        <td>${orderDetail.clazzLevel!''}</td>
                        <td>${orderDetail.termType!''}</td>
                        <td>${orderDetail.subject!''}</td>
                        <td>${orderDetail.serviceTime!''}</td>
                        <td style="width: 120px">${orderDetail.orderCreateTime!''}</td>
                        <td style="width: 80px">${orderDetail.cityName!''}</td>
                        <td><#if orderDetail.productType=='PicListenBook'>点读机<#elseif orderDetail.productType=='WalkerMan'>随身听<#else></#if></td>
                    </tr>
                    </#list>
                <#else>
                <tr>
                    <td>暂无订单数据</td>
                </tr>
                </#if>
            </tbody>
        </table>
        <div id="paginator" pageIndex="${(pageIndex!1)}" class="paginator clearfix"
             totalPages="<#if totalPages??>${totalPages}<#else>1</#if>"></div>
    </div>
</div>
</@module.page>