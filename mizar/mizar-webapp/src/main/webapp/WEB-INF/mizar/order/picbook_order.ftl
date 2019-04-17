<#import "../module.ftl" as module>
<@module.page
title="绘本订单数据查询"
leftMenu="${leftMenu!''}"
pageJsFile={"ordercount" : "public/script/basic/order/picbook_order"}
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

    label{
        width:64px;
    }

    .data-table th,.data-table td{
        text-align: center;
    }

</style>

<div class="op-wrapper clearfix">
    <span class="title-h1">绘本订单查询</span>
</div>

<div style="float: left;">
    <div class="item time-region" style="width: auto;">
        <div class="matManage-box">
            <div class="time-select input-control">
                <label style="width:64px;">开始时间:</label>
                <input title="" id="startTime" class="v-select" readonly="readonly" value="${startTime!''}"/>
                <label style="width:64px;">截止时间:</label>
                <input title="" id="endTime" class="v-select" readonly="readonly" value="${endTime!''}"/>
                <input id = "series" value = "${series!''}" hidden />
            </div>
            <input type="text" hidden value="REN_JIAO" id="publisher_flag"/>
            <a id="ordercount_search" class="blue-btn" href="javascript:void(0)">查询</a>
        </div>
    </div>

    <div>
        <table class="data-table conTable">
            <thead>
                <tr>
                    <th>订单数量</th>
                    <th>订单金额</th>
                    <th>退款数量</th>
                    <th>退款金额</th>
                </tr>
            </thead>
            <tbody>
                <tr>
                    <td>${(orderCount!'')}</td>
                    <td>${(totalMoney!'')}</td>
                    <td>${(orderRefundCount!'')}</td>
                    <td>${(totalRefundMoney!'')}</td>

                </tr>
            </tbody>
        </table>
    </div>
</div>
</@module.page>