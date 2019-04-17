<#import "../module.ftl" as module>
<@module.page
title="订单数据查询"
leftMenu="订单数据查询"
pageJsFile={"ordercount" : "public/script/basic/order/ordercount"}
pageJs=["ordercount"]
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
    <span class="title-h1">一起作业—订单数据查询</span>
</div>

<div style="float: left;">
    <div class="item time-region" style="width: auto;">
        <div class="matManage-box">
            <div class="time-select input-control">
                <p>开始时间</p>
                <input title="" id="startTime" class="v-select" value="${(startDate!'')}"/>
                <div style="margin:0 5px;line-height:30px;">截止时间</div>
                <input title="" id="endTime" class="v-select" value="${(endDate!'')}"/>
            </div>
            <input type="text" hidden value="SHAN_DONG" id="publisher_flag"/>
            <a id="ordercount_search" class="blue-btn" href="javascript:void(0)">查询</a>
        </div>
    </div>

    <div>
        <table class="data-table conTable">
            <tbody>
            <tr>
                <td>订单总数</td>
                <td>${(orderCount!'')}</td>
                <td>退款订单</td>
                <td>${(backOrderCount!'')}</td>
            </tr>
            <tr>
                <td>订单总收入</td>
                <td>${(orderMoneyCount!'')}</td>
                <td>退款金额</td>
                <td>${(backMoneyCount!'')}</td>
            </tr>
            <tr>
                <td>实际收入</td>
                <td>${(realIncome!'')}</td>
            </tr>
            </tbody>
        </table>
    </div>
</div>
</@module.page>