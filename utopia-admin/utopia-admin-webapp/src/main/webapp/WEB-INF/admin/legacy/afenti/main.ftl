<#import "../../layout_default.ftl" as layout_default>

<@layout_default.page page_title="用户订单中心" page_num=3>
<script src="${requestContext.webAppContextPath}/public/js/knockout/dist/knockout.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/knockout.mapping/knockout.mapping.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/vue2.4.2.min.js"></script>
    <#assign causeList = [
    "家长不知情，孩子私自购买",
    "购买错产品/时间",
    "内容和教材不符/无教材",
    "重复购买/购买过于频繁",
    "购买后孩子不使用申请退款",
    "使用不流畅，出现卡顿bug",
    "产品形式跟家长需求不相符，对孩子帮助不大",
    "所购买产品内容不够",
    "内容过难或过于简单",
    "使用不方便，无PC端/无APP端",
    "购买后没有获得赠品",
    "用户原因无法连续使用产品",
    "用户设备问题",
    "老师要求退掉/老师不让使用",
    "学生误操作",
    "支付方式选错",
    "用户购买没有使用优惠券",
    "内部测试"
    ]/>
<div>
    <style>
        table {
            font-size:14px;
        }
        .table .active td {
            background: #ff0 !important;
        }
        .table th, .table td{line-height:26px;}
        .JS-sevenDays{
            width:50%;
        }
        .JS-sevenDays td{
            text-align:center;
        }
        .historyTr td{
            width:11%;
        }
        .button-bg{
            display:inline-block;
            margin:0 5px;
            width:80px;
            height:30px;
            line-height:30px;
            text-align: center;
            -webkit-border-radius:6px;
            -moz-border-radius:6px;
            border-radius:6px;
        }
        .button-bg:link,.button-bg:visited,.button-bg:hover,.button-bg:active {color:#fff;}
        .button-green{
            color: #ffffff;
            background:green;
        }
        .button-red{
            color: #ffffff;
            background:red;
        }
        .button-blue{
            color: #ffffff;
            background:#169bd5;
        }
        .display-ib{
            display: inline-block;
        }
        .input-refund-amount{
            display: block;
            width: 50px;
        }
        .input-refund-error{
            border: 1px solid #f00 !important;
            color: #f00 !important;
        }
    </style>

    <form method="get" action="main.vpage" class="form-horizontal span9" id="searchOrder">
        <legend>用户订单中心查询</legend>
        <ul class="inline">
            <li>
                <label for="accountInput">
                    用户ID：
                    <input id="accountInput" name="userId" type="text" value="${(user.id)!''}"/>
                </label>
            </li>
            <li>
                <label for="orderId">
                    订单号：
                    <input id="orderId" name="orderId" type="text" value="${orderId!''}"/>
                </label>
            </li>
            <li>
                <label for="type">
                    产品类型：
                    <select id="type" name="type">
                        <option value="">--所有类型--</option>
                        <#if productTypeList??>
                            <#list productTypeList as pt>
                                <option value="${pt}" <#if type?? && type==pt> selected </#if>>${pt}</option>
                            </#list>
                        </#if>
                    </select>
                </label>
            </li>
            <li>
                <label for="orderStatus">
                    支付状态：
                    <select id="paymentStatus" name="paymentStatus">
                        <option value="">全部</option>
                        <#if paymentStatusList??>
                            <#list paymentStatusList as st>
                                <option value="${st.name()}"
                                <#if paymentStatus?? && paymentStatus?has_content>
                                    <#if paymentStatus==st.name()>
                                        selected
                                    </#if>
                                <#else>
                                    <#if !user?? && st.name() == 'Paid'>
                                        selected
                                    </#if>
                                </#if> >
                                ${st.getDesc()}
                                </option>
                            </#list>
                        </#if>
                    </select>
                </label>
            </li>
            <li>
                <label for="startDate">
                    更新时间
                    <input name="startDate" value="${startDate!''}" id="startDate" type="text" placeholder="格式：2013-11-04" readonly="readonly"/>
                </label>
            </li>
            <li>
                <label for="endDate">
                    至
                    <input name="endDate" value="${endDate!''}" id="endDate" type="text" placeholder="格式：2013-11-04" readonly="readonly"/>
                </label>
            </li>
            <li>
                <a href="#myModal" role="button" class="btn btn-primary" data-toggle="modal" id="searchBtn">搜索</a>
                <a href="#myModal" role="button" class="btn" id="addOrderBtn" data-toggle="modal">新增订单</a>
                <a role="button" class="btn" id="addLoisticsBtn" data-toggle="modal">导入订单物流信息</a>
            </li>
        </ul>
    </form>

    <#if user??>
        <p style="clear:both;">
            <span class="label label-important">用户ID: ${user.id}</span>
            <span class="label label-info">用户名: ${(user.profile.realname)!''}</span>
        </p>
        <p style="clear:both;">
            <span style="color: red">*申请退款的时候请留意：字体颜色为红色的为过期订单*</span>
        </p>
    </#if>
    <table class="table hide table-hover table-striped  table-bordered JS-sevenDays">

    </table>
    <div>
        <ul class="nav nav-tabs" id="myTab" style="width:99%;">
            <#if userOrderList?has_content>
                <li class="active"><a href="#home">订单</a></li></#if>
            <#if userOldOrderList?has_content>
                <li><a href="#old">历史订单</a></li></#if>
            <#if userActivatedProducts?has_content>
                <li><a href="#profile">激活历史</a></li></#if>
            <#if userOrderPaymentHistoryList?has_content>
                <li><a href="#messages">支付记录</a></li>  </#if>
            <#if batchRefundOrderList?has_content>
                <li><a href="#batchrefunds">批量退款</a></li>  </#if>
        </ul>
        <div class="tab-content" style="width:100%;">
            <#if userOrderList?has_content>
                <div class="tab-pane active" id="home">
                    <table class="table table-hover table-striped  table-bordered" style="width: 100%;">
                        <thead>
                        <tr>
                            <th>订单号</th>
                            <th>产品类型</th>
                            <th>产品名称</th>
                            <th>创建时间</th>
                            <th>更新时间</th>
                            <th>订单状态</th>
                            <th>付款状态</th>
                            <th>订单金额</th>
                            <th>可退款金额</th>
                            <th>优惠劵</th>
                            <th  style="max-width: 300px;">订单来源</th>
                            <th nowrap="1">操作</th>
                        </tr>
                        </thead>
                        <#list userOrderList as one>
                            <tr id='order-${one.id!''}' <#if one.outOfDate> style="color: red" </#if>>
                                <td data-multiRefund="${(one.multiRefund!false)?string}">${one.id!''}</td>
                                <td>${one.productServiceType!''}</td>
                                <td>${one.productName!''}</td>
                                <td>${one.createDatetime!''}</td>
                                <td>${one.updateDatetime!''}</td>
                                <td>${one.orderStatus!''}</td>
                                <td>${one.payStatus!''}</td>
                                <td>${one.totalPrice!''}</td>
                                <td>${one.refundAmount!0}</td>
                                <td>
                                    <#if one.bindCoupon>
                                        <a href="javascript:void(0);" id="check_coupon_${one.id!''}">查看</a>
                                    <#else>
                                        -
                                    </#if>
                                </td>
                                <td style="max-width: 300px;">${one.orderReferer!''}</td>
                                <td nowrap="1">
                                    <#if one.canBePaid>
                                        <a href='manuallypay.vpage?orderId=${one.id!''}' class="button-bg button-blue">付款</a>
                                        <a href='javascript:void(0);' id="cancel_afenti_order_${one.id!''}" class="button-bg button-blue">取消</a>
                                    </#if>
                                    <#if one.canBeRefund>
                                        <a href="javascript:void(0);" id="refund_afenti_order_${one.id!''}" user="${one.userId!''}" class="button-bg button-blue">申请退款</a>
                                    </#if>
                                    <#if one.payStatus == '已支付' || one.payStatus == '已退款'>
                                        <a href="javascript:void(0);" id="payment_history_${one.id!''}" user="${one.userId!''}" class="button-bg button-green">支付记录</a>
                                    </#if>
                                    <#if one.payStatus == '已退款'>
                                        <a href="javascript:void(0);" id="refund_history_${one.id!''}" user="${one.userId!''}" class="button-bg button-red">退款进度</a>
                                    </#if>
                                    <#if one.canChangeBook >
                                        <a href="javascript:void(0);" id="change_book"
                                           onclick="changeBook('${one.id!''}')" class="button-bg button-blue">换购教材</a>
                                    </#if>
                                </td>
                            </tr>
                        </#list>
                    </table>
                </div>
            </#if>

            <#if userOldOrderList?has_content>
                <div class="tab-pane" id="old">
                    <table class="table table-hover table-striped  table-bordered" style="width: auto;">
                        <thead>
                        <tr>
                            <th>订单号</th>
                            <th>产品类型</th>
                            <th>产品名称</th>
                            <th>创建时间</th>
                            <th>更新时间</th>
                            <th>订单状态</th>
                            <th>付款状态</th>
                            <th>订单金额</th>
                            <th>可退款金额</th>
                            <th>订单来源</th>
                            <th>支付方式</th>
                            <th>流水号</th>
                            <th>服务开始时间</th>
                            <th>服务结束时间</th>
                            <th nowrap="1">操作</th>
                        </tr>
                        </thead>
                        <#list userOldOrderList as one>
                            <tr id='order-${one.id!''}' <#if one.outOfDate> style="color: red" </#if>>
                                <td>${one.id!''}</td>
                                <td>${one.productServiceType!''}</td>
                                <td>${one.productName!''}</td>
                                <td>${one.createDatetime!''}</td>
                                <td>${one.updateDatetime!''}</td>
                                <td>${one.orderStatus!''}</td>
                                <td>${one.payStatus!''}</td>
                                <td>${one.totalPrice!''}</td>
                                <td>${one.refundAmount!0}</td>
                                <td>${one.orderReferer!''}</td>
                                <td>${one.payMethod!''}</td>
                                <td>${one.outTradeNo!''}</td>
                                <td>${one.serviceStartTime!''}</td>
                                <td>${one.serviceEndTime!''}</td>
                                <td nowrap="1">
                                    <#if one.canBeRefund>
                                        <a href="javascript:void(0);" id="refund_afenti_order_${one.id!''}" user="${one.userId!''}" class="button-bg button-blue">申请退款</a>
                                    </#if>
                                    <#if one.payStatus == '已退款'>
                                        <a href="javascript:void(0);" id="refund_history_${one.id!''}" user="${one.userId!''}" class="button-bg button-red">退款进度</a>
                                    </#if>
                                </td>
                            </tr>
                        </#list>
                    </table>
                </div>
            </#if>

            <#if userActivatedProducts?has_content>
                <div class="tab-pane" id="profile">
                    <table class="table table-hover table-striped  table-bordered" style="width: auto;">
                        <tr>
                            <td>学号</td>
                            <td>产品</td>
                            <td>子产品ID</td>
                            <td>创建时间</td>
                            <td>开始时间</td>
                            <td>结束时间</td>
                            <td>操作</td>
                        </tr>
                        <#list userActivatedProducts as one>
                            <tr>
                                <td>${one.userId!''}</td>
                                <td>${one.productServiceType!''}</td>
                                <td>${one.productItemId!''}</td>
                                <td>${one.createDatetime!''}</td>
                                <td>${one.serviceStartTime!''}</td>
                                <td>${one.serviceEndTime!''}</td>
                                <td align="center">
                                    <#if .now?datetime< one.serviceEndTime?datetime>
                                        <input id="delayButton${one.id}" type="button" name="delayActivationButton"
                                               value="延期" historyid="${one.id}"/>
                                    </#if>
                                </td>
                            </tr>
                        </#list>
                    </table>
                </div>
            </#if>

            <#if userOrderPaymentHistoryList?has_content>
            <div class="tab-pane" id="messages">
                <table class="table table-hover table-striped  table-bordered" style="width: auto;">
                    <tr>
                        <td>学号</td>
                        <td>创建时间</td>
                        <td>订单号</td>
                        <td>类型</td>
                        <td>金额</td>
                        <td>支付时间</td>
                        <td>支付方法</td>
                        <td>外部ID</td>
                        <td>备注</td>
                        <td>服务开始时间</td>
                        <td>服务结束时间</td>
                    </tr>
                    <#list userOrderPaymentHistoryList as one>
                        <tr>
                            <td>${one.userId!''}</td>
                            <td>${one.createDatetime!''}</td>
                            <td>${one.orderId!''}</td>
                            <td>${one.paymentStatus.getDesc()!''}</td>
                            <td>${one.payAmount!''}</td>
                            <td>${one.payDatetime!''}</td>
                            <td>${one.payMethod!''}</td>
                            <td>${one.outerTradeId!''}</td>
                            <td>${one.comment!''}</td>
                            <td>${one.serviceStartTime!''}</td>
                            <td>${one.serviceEndTime!''}</td>
                        </tr>
                    </#list>
                </table>
            </div>
        </#if>

        <#if batchRefundOrderList?has_content>
            <div class="tab-pane" id="batchrefunds">
                <p style="clear:both;">
                    <span style="color: red">*支付时间一年内的订单*</span>
                </p>
                <div style="padding: 10px 5px">
                    <input type="radio" name="batchRefundType" id="batchRefundType1" /><label for="batchRefundType1" style="display: inline-block;">折算退</label>
                    <input type="radio" name="batchRefundType" id="batchRefundType2" /><label for="batchRefundType2" style="display: inline-block;">全额退</label>
                </div>
                <table class="table table-hover table-striped  table-bordered" style="width: auto;">
                    <tr>
                        <th><input type="checkbox" class="batchChoiceAll" /></th>
                        <td>订单ID</td>
                        <td>商品信息</td>
                        <td>支付方法</td>
                        <td>支付金额</td>
                        <td>支付时间</td>
                        <td>服务开始时间</td>
                        <td>服务结束时间</td>
                        <td>已退金额</td>
                        <td>可退金额</td>
                        <td>折算金额</td>
                        <td>退款金额</td>
                    </tr>
                    <#list batchRefundOrderList as one>
                        <tr data-refundtype="${one.refundType!}"
                            data-refundamount="${one.refundAmount!0}"
                            data-inputrefundamount="${one.refundAmount!0}"
                            data-orderid="${one.orderId!''}"
                            data-itemid="${one.itemId!''}"
                            class="refundTableTd"
                            <#if one.red=="1">style="color: red"</#if>>
                            <td><input type="checkbox" class="batchChoiceSingle" /></td>
                            <td>${one.orderId!''}</td>
                            <td>${one.itemName!''}</td>
                            <td>${one.payMethod!''}</td>
                            <td>${one.payAmount!''}</td>
                            <td>${one.payDate!''}</td>
                            <td>${one.serviceStartTime!''}</td>
                            <td>${one.serviceEndTime!''}</td>
                            <td>${one.refundedAmount!''}</td>
                            <td>${one.refundableAmount!''}</td>
                            <td>${one.convertAmount!''}</td>
                            <#--<td>${one.refundAmount!''}</td>-->
                            <td><input type="text" value="${one.refundAmount!''}" class="input-refund-amount inputRefundAmount"></td>
                        </tr>
                    </#list>
                    <tr>
                        <td colspan="12" style="text-align:right">
                            退款金额合计：<span id="totalRefundAmount">0</span>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="12" style="text-align:right">
                            <label for="" class="display-ib" style="font-size: 16px;">
                                退款原因：
                                <select name="" id="batchRefundCauseSelect" >
                                    <option value="">请选择退款原因</option>
                                    <#if causeList??>
                                        <#list causeList as mt>
                                            <option value="${mt}" <#if type?? && type==mt> selected </#if>>${mt}</option>
                                        </#list>
                                    </#if>
                                </select>
                            </label>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="12" style="text-align:right">
                            <a href="javascript:void(0);" id="batchRefundButton"  class="button-bg button-blue">申请退款</a>
                        </td>
                    </tr>
                </table>
            </div>
        </#if>
        </div>
    </div>

    <div id="myModal" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
            <h5>温馨提示</h5>
        </div>
        <div class="modal-body">
            <p>请输入有效的用户ID。</p>
        </div>
        <div class="modal-footer">
            <button type="button" class="btn btn-primary" data-dismiss="modal" aria-hidden="true">关闭</button>
        </div>
    </div>

    <div id="cancelAfentiActivationHistoryModal" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
            <h5>温馨提示</h5>
        </div>
        <div class="modal-body">此操作前请确认用户已退款，是否继续</div>
        <div class="modal-footer">
            <button id="cancelAfentiActivationHistorySubmit" historyid="" type="button" class="btn btn-primary"
                    data-dismiss="modal" aria-hidden="true">是
            </button>
            <button type="button" class="btn" data-dismiss="modal" aria-hidden="true">否</button>
        </div>
    </div>

    <div id="delayAfentiActivationHistoryModal" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>激活有效时间延长</h3>
        </div>
        <div class="modal-body">
            <dl class="dl-horizontal">
                <ul class="inline">
                    <li>
                        <dt>延长天数</dt>
                        <dd><input id="delayDays" type='text'/></dd>
                        (不得大于365天)
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>延期原因</dt>
                        <dd><textarea id="delayReason" cols="35" rows="4"></textarea></dd>
                    </li>
                </ul>
            </dl>
        </div>
        <div class="modal-footer">
            <button id="delaySubmit" class="btn btn-primary">确 定</button>
            <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
        </div>
    </div>

    <div id="loadrefundprocessModal" style="width:1000px;margin-left:-500px;" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>退款进度查询</h3>
        </div>
        <div style="margin:20px;"><span style="display:none;" class="JS-payStyle">支付方式：微信</span>     <span style="display:none;" class="JS-errorCode"></span></div>
        <table class="table table-hover table-striped  table-bordered JS-refundBox" style="display:none;width:96%; margin-left:2%;">
            <tr>
                <td>订单号</td>
                <td class="JS-orderNo"></td>
                <td>微信流水号</td>
                <td class="JS-wechatNo"></td>
                <td>订单金额</td>
                <td class="JS-orderMo"></td>
            </tr>
        </table>
        <table class="table table-hover table-striped  table-bordered JS-refundBox JS-refundProcess" style="display:none;width:96%; margin-left:2%;">
            <thead>
                <th>退款渠道</th>
                <th>退款金额</th>
                <th>退款状态</th>
                <th>退款入账账户</th>
                <th>退款成功时间</th>
            </thead>
        </table>

        <div class="modal-footer">
            <button class="btn" data-dismiss="modal" aria-hidden="true">知道了</button>
        </div>
    </div>


    <div id="loadpaidhistoryModal" style="width:1000px;margin-left:-500px;" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>订单支付记录</h3>
        </div>
        <table class="table table-hover table-striped  table-bordered" style="width:96%; margin-top:20px; margin-left:2%;">
            <thead>
                <th>创建时间</th>
                <th>类型</th>
                <th>金额</th>
                <th>支付时间</th>
                <th>支付方法</th>
                <th>外部ID</th>
                <th>退款原因</th>
                <th>服务开始时间</th>
                <th>服务结束时间</th>
            </thead>
        </table>
        <div class="modal-footer">
            <button class="btn" data-dismiss="modal" aria-hidden="true">知道了</button>
        </div>
    </div>

    <#--申请退款时，multirefund字段为false时出现的弹窗(第一版)-->
    <div id="refundModal" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
            <h4>申请退款 <label style="color: red">订单金额将由财务退款给用户（学贝直接退回到用户账户）</label></h4>
        </div>
        <div class="modal-body">
            <div>
                <dl class="dl-horizontal">
                    <ul class="inline" id="ul_refund_days">
                        <li>
                            <dt>
                                <input type="radio" id="chk_refund_days" name="chk_refund"/>
                                按天退
                            </dt>
                            <dd>
                                <select id="sl_refund_days" style="width:80px;"></select>
                            </dd>
                        </li>
                    </ul>
                    <ul class="inline">
                        <li>
                            <dt>
                                <input type="radio" id="chk_full_refund" name="chk_refund"/>
                                全额退:
                            </dt>
                            <dd></dd>
                        </li>
                    </ul>
                    <ul class="inline">
                        <li>
                            <dt>退款金额:</dt>
                            <dd>
                                <label id="refund_amount"></label>
                            </dd>
                        </li>
                    </ul>
                    <ul class="inline">
                        <li>
                            <dt>退款原因:</dt>
                            <dd>
                                <select id="sl_refund_cause">
                                    <option value="">请选择退款原因</option>
                                    <#if causeList??>
                                        <#list causeList as mt>
                                            <option value="${mt}" <#if type?? && type==mt> selected </#if>>${mt}</option>
                                        </#list>
                                    </#if>
                                </select>

                            </dd>
                        </li>
                    </ul>

                    <ul class="inline">
                        <li>
                            <label style="color: red;margin-left: 100px;" id="notifyInfo"></label>
                        </li>
                    </ul>
                </dl>
            </div>
        </div>
        <div class="modal-footer">
            <button type="button" class="btn btn-primary" data-dismiss="modal" aria-hidden="true">关闭</button>
            <button type="button" class="btn btn-primary" id="btn_submit_refund">提交</button>
        </div>
    </div>

    <#--点击申请退款时，multirefund字段为true时出现的弹窗(第二版，结合第一版使用)-->
    <div id="refundProductModal" class="modal hide fade" style="width: 1000px; margin-left: -350px">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
            <h4>申请退款 <label style="color: red">订单金额将由财务退款给用户（学贝直接退回到用户账户）</label></h4>
        </div>
        <div class="modal-body">
            <table class="table table-hover table-striped  table-bordered" id="refundProductTable">
                <thead>
                    <th>选择退款产品</th>
                    <th>是否退过款</th>
                    <th>选择退款方式</th>
                    <th>退款金额</th>
                </thead>
                <tr v-for="(refundProduct, index) in refundProductList">
                    <td>
                       <label :for="'refund_' + refundProduct.productId" class="display-ib"><input type="checkbox" :checked="refundProduct._checked" :disabled="refundProduct.hasRefund ? true : false" :id="'refund_' + refundProduct.productId" @change="checkRefund(refundProduct, index, $event)"> {{refundProduct.productName}}</label>
                       <label style="color:red">{{refundProduct.notifyInfo}}</label>
                    </td>
                    <td>{{refundProduct.hasRefund ? '是' : '否'}}</td>
                    <td>
                        <select style="width: 100px;" v-model="refundProduct._refundTypeValue" id="" @change="selectRefundType(refundProduct, $event)">
                            <option v-for="refundType in refundProduct._refundTypeList" :value="refundType.typeValue" :key="refundType.typeValue">{{refundType.typeName}}</option>
                        </select>
                    </td>
                    <td>{{refundProduct._payAmount}}元</td>
                </tr>
            </table>
            <div>
                <p>退款总金额：{{allRefundAmount}}元</p>
            </div>
            <div>
                <label for="" class="display-ib">
                    退款原因：
                    <select name="" id="" v-model="refundCause" @change="choiceRefundCause($event)">
                        <option value="">请选择退款原因</option>
                        <#if causeList??>
                            <#list causeList as mt>
                                <option value="${mt}" <#if type?? && type==mt> selected </#if>>${mt}</option>
                            </#list>
                        </#if>
                    </select>
                </label>
            </div>
        </div>
        <div class="modal-footer">
            <button type="button" class="btn btn-primary" data-dismiss="modal" aria-hidden="true">关闭</button>
            <button type="button" class="btn btn-primary" @click="submitMultiRefund()">提交</button>
        </div>
    </div>

    <#--点击申请退款出现的弹窗(第三版，一二不用)-->
    <div id="newRefundModal" class="modal hide fade" style="width: 700px; margin-left: -350px">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
            <h4>申请退款 <label style="color: red">支付宝支付的订单金额将由财务退款给用户，微信支付的订单申请退款后系统会在30分钟内自动退款（学贝直接退回到用户账户）</label></h4>
        </div>
        <div class="modal-body">
            <p>用户ID: {{newRefundDataInfo.userId}}</p>
            <p>订单号: {{newRefundDataInfo.orderId}}</p>
            <p v-if="newRefundDataInfo.outerTrade">外部订单号: {{newRefundDataInfo.outerTrade}}</p>
            <p>订单状态: {{newRefundDataInfo.payStatus}}</p>
            <p>支付方式: {{newRefundDataInfo.payMethod}}</p>
            <p>创建时间: {{newRefundDataInfo.createTime}}</p>
            <p>付款时间: {{newRefundDataInfo.payTime}}</p>
            <table class="table table-hover table-striped  table-bordered">
                <thead>
                    <th>选择退款产品</th>
                    <th>产品类型</th>
                    <th>付款金额(元)</th>
                    <th>可退金额(元)</th>
                    <th>退款金额(元)</th>
                </thead>
                <tr v-for="(refundItem, index) in newRefundItemList">
                    <td>
                        <label class="display-ib" :for="'refund_' + refundItem.itemId">
                            <input
                                type="checkbox"
                                :checked="refundItem._checked"
                                :disabled="refundItem.refunded ? true : false"
                                :id="'refund_' + refundItem.itemId"
                                @change="newCheckRefund(refundItem, $event)"> {{refundItem.itemName}}
                        </label>
                    </td>
                    <td>{{refundItem.itemType}}</td>
                    <td>{{refundItem.payAmount}}</td>
                    <td>{{refundItem.refundableAmount}}</td>
                    <td>
                        <input
                            class="refund-amount-input"
                            type="text"
                            placeholder="请输入退款金额"
                            style="width: 100px;"
                            :style="refundItem._isInputError ? 'border: 1px solid #f00;' : ''"
                            v-model="refundItem._refundValue"
                            @blur="checkInputRefundAmount(refundItem, index)">
                    </td>
                </tr>
            </table>
            <p style="color: red">{{ chipsRefundedText }}</p>
            <div>
                <label for="" class="display-ib" style="font-size: 16px;">
                    退款原因：
                    <select name="" id="" v-model="newRefundCause">
                        <option value="">请选择退款原因</option>
                        <#if causeList??>
                            <#list causeList as mt>
                                <option value="${mt}" <#if type?? && type==mt> selected </#if>>${mt}</option>
                            </#list>
                        </#if>
                    </select>
                </label>
                <p v-if="newRefundDataInfo.notifyInfo" style="color:#f00;">{{newRefundDataInfo.notifyInfo}}</p>
            </div>
            <ul class="inline" id="student_entity_reward">
                <li>
                    <label style="color: red;margin-left: 100px;">（用户有实物奖励发放记录，请核实后再继续退款流程）</label>
                </li>
                <li>
                    <dd>
                        <a href="/fairyland/studententityrewardrecordlist.vpage?" target="_blank" id="student_entity_reward_detail" style="color: red;">点击查询详情>></a>
                    </dd>
                </li>
            </ul>
        </div>
        <div class="modal-footer">
            <button type="button" class="btn btn-primary" data-dismiss="modal" aria-hidden="true">关闭</button>
            <button type="button" class="btn btn-primary" @click="submitNewRefund()">提交</button>
        </div>
    </div>

    <div id="couponModal" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
            <h5>优惠劵信息</h5>
        </div>
        <div class="modal-body">
            <div>
                <dl class="dl-horizontal">
                    <ul class="inline">
                        <li>
                            <dt>优惠劵ID</dt>
                            <dd>
                                <label id="couponId"></label>
                            </dd>
                        </li>
                    </ul>
                    <ul class="inline">
                        <li>
                            <dt>名称</dt>
                            <dd>
                                <label id="couponName"></label>
                            </dd>
                        </li>
                    </ul>
                    <ul class="inline">
                        <li>
                            <dt>类型</dt>
                            <dd>
                                <label id="couponType"></label>
                            </dd>
                        </li>
                    </ul>
                    <ul class="inline">
                        <li>
                            <dt>折扣力度</dt>
                            <dd>
                                <label id="typeValue"></label>
                            </dd>
                        </li>
                    </ul>
                </dl>
            </div>
        </div>
    </div>

    <div id="modal_change_book" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
            <h5>教材换购</h5>
        </div>
        <div class="modal-body">
            <div>
                <dl class="dl-horizontal">
                    <ul class="inline">
                        <li>
                            <dt>新教材：</dt>
                            <dd>
                                <select id="sl_books"
                                        data-bind="options:books,optionsText:'name',optionsValue:'id',value:selectedBook,optionsCaption:'请选择新教材',enable:hasAvaliableBooks"></select>
                            </dd>
                        </li>
                    </ul>
                </dl>
            </div>
        </div>
        <div class="modal-footer">
            <button type="button" class="btn btn-primary" data-dismiss="modal" aria-hidden="true">关闭</button>
            <button type="button" class="btn btn-primary" data-dismiss="modal" aria-hidden="true"
                    id="btn_submit_change_book" data-bind="click:changeBook,enable:canSubmitChangeBook">提交
            </button>
        </div>
    </div>

    <div id="batchRefundSureModal" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
            <h4>批量退款</h4>
        </div>
        <div class="modal-body">
            <div id="batchRefundPara"></div>
        </div>
        <div class="modal-footer">
            <button type="button" class="btn btn-primary" data-dismiss="modal" aria-hidden="true">关闭</button>
            <button type="button" class="btn btn-primary" data-dismiss="modal" aria-hidden="true" id="btn_batch_refund">提交</button>
        </div>
    </div>

    <#--导入订单物流信息弹窗-->
    <div id="addLoisticsModal" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>导入订单物流信息</h3>
        </div>
        <div class="modal-body">
            请导入订单物流表：<input type="file"  accept=".xls, .xlsx" id="importLoisticsExcel" />
            <br>
            <button class="btn" id="uploadLoisticsBtn">提交</button>
            <br><br>
            模板下载:
            <a href="/legacy/afenti/downloadExample.vpage">物流信息填写模板</a>
        </div>
        <div class="modal-footer">
            <button class="btn btn-primary" data-dismiss="modal" aria-hidden="true">关闭</button>
        </div>
    </div>

    <script>
        $(function(){
            $("#startDate").datepicker({
                dateFormat      : 'yy-mm-dd',  //日期格式，自己设置
                monthNames      : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
                monthNamesShort : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
                dayNamesMin     : ["日", "一", "二", "三", "四", "五", "六"],
                defaultDate     : new Date(),
                numberOfMonths  : 1,
                changeMonth: false,
                changeYear: false,
                onSelect : function (selectedDate){}
            });

            $("#endDate").datepicker({
                dateFormat      : 'yy-mm-dd',  //日期格式，自己设置
                monthNames      : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
                monthNamesShort : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
                dayNamesMin     : ["日", "一", "二", "三", "四", "五", "六"],
                defaultDate     : new Date(),
                numberOfMonths  : 1,
                changeMonth: false,
                changeYear: false,
                onSelect : function (selectedDate){}
            });
        });

        $(document).keydown(function (evt) {
            if (evt.keyCode === 13) {
                $('#searchBtn').click();
            }
        });
        $(function () {
            //新增订单
            $("#addOrderBtn").on('click', function () {
                var postUserId = $('#accountInput').val();
                if (!isNaN(parseInt(postUserId, 10)) && postUserId != "") {
                    postUserId = "addorder.vpage?userId=" + postUserId;
                    location.href = postUserId;
                    return false;
                }
            });

            //导入物流信息
            $("#addLoisticsBtn").on('click', function () {
                $('#addLoisticsModal').modal('show');
                //
            });

            // 导入物流信息-上传excel
            $('#uploadLoisticsBtn').on('click', function () {
                var file = $('#importLoisticsExcel')[0].files[0];
                if (!file){
                    alert('请先上传文件哦~');
                    return ;
                }
                if (['.xls', '.xlsx'].indexOf(file.name.substring(file.name.lastIndexOf('.'))) === -1) {
                    alert('你选择的文件格式有误哦~');
                    return ;
                }
                var uploadLoisticsFormData = new FormData();
                uploadLoisticsFormData.append('file', file);
                $.ajax({
                    url: '/legacy/afenti/uploadLoisticsInfo.vpage',
                    type: 'POST',
                    data: uploadLoisticsFormData,
                    processData: false,
                    contentType: false,
                    async: true,
                    timeout: 0,
                    success: function (res) {
                        if (res.success) {
                            alert('导入成功');
                        } else {
                            alert(res.info);
                        }
                    }
                })
            });

            //订单查询
            $("#searchBtn").click(function () {
                var postUserId = $('#accountInput').val();
                var orderId = $('#orderId').val();
                if (!isNaN(parseInt(postUserId, 10)) && postUserId != "" || !isNaN(parseInt(orderId, 10)) && orderId != "") {
                    $('#searchOrder').submit();
                    return false;
                }
            });

            //切换
            $('#myTab a:first').tab('show');
            $('#myTab a').click(function (e) {
                e.preventDefault();
                $(this).tab('show');
            });

            $('#order-' + '${(orderId?js_string)!''}').addClass("active");
            $('.order-activate').click(function () {
                var orderId = $(this).attr('data-order-id');
                $.post('activate.vpage', {orderId: orderId}, function (data) {
                    alert(data.message);
                    window.location.href = 'main.vpage?orderId=' + orderId;
                });
                return false;
            });

            //激活历史取消按键
            $('input[name="cancelActivationButton"]').click(function () {
                var historyid = $(this).attr('historyid');

                var dialog = $('#cancelAfentiActivationHistoryModal');
                dialog.find('#cancelAfentiActivationHistorySubmit').attr('historyid', historyid);
                dialog.modal('show');
            });
            $('#cancelAfentiActivationHistorySubmit').click(function () {
                var historyid = $(this).attr('historyid');
                $.post(
                        'cancelActivationHistory.vpage',
                        {historyId: historyid},
                        function (data) {
                            $('#cancelAfentiActivationHistorySubmit').attr('historyid', '');
                            if (data.status == 'false') {
                                alert(data.info);
                            } else {
                                window.location.href = 'main.vpage?userId=' + data.userid + '&activeTab=' + data.activeTab;
                            }
                        }
                );


            });

            $('a[id^="cancel_afenti_order_"]').click(function () {
                var orderId = $(this).attr('id').substring("cancel_afenti_order_".length);
                if (confirm("确定要取消订单？")) {
                    $.post(
                            'cancelAfentiOrder.vpage',
                            {orderId: orderId},
                            function (data) {
                                if (data.status == 'false') {
                                    alert(data.message);
                                } else {
                                    window.location.reload();
                                }
                            }
                    );
                }
            });

            $('a[id^="cancel_app_order_"]').on("click", function () {
                var orderId = $(this).attr("id").substring("cancel_app_order_".length);
                if (confirm("确定要退款这个订单吗")) {
                    $.post(
                            "cancelappvoxpayorder.vpage",
                            {orderId: orderId},
                            function (data) {
                                if (data.success) {
                                    window.location.reload();
                                } else {
                                    alert(data.info);
                                }
                            });
                }
            });
            // 获取七日统计
            $.post('loadusersummaryweek.vpage', {userId: '${(user.id)!''}'}, function (data) {
                if (data.success){
                    var sevenDays = "<tr><td>用户ID："+data.userId+"</td><td>用户姓名："+data.userName+"</td><td>今日付费："+data.count+"次/" + data.sumPrice + "元</td><td>七日付费："+data.count7+"次/"+data.sumPrice7 +"元</td><td>当日退款："+data.refundPriceSumDay+"元</td></tr>";
                    $(".JS-sevenDays").show().append(sevenDays);
                }
            });
            function getMyDate(str){
                if (str == null){
                    return "";
                }
                var oDate = new Date(str),
                        oYear = oDate.getFullYear(),
                        oMonth = oDate.getMonth()+1,
                        oDay = oDate.getDate(),
                        oHour = oDate.getHours(),
                        oMin = oDate.getMinutes(),
                        oSen = oDate.getSeconds(),
                        oTime = oYear +'-'+ getzf(oMonth) +'-'+ getzf(oDay) +' '+ getzf(oHour) +':'+ getzf(oMin) +':'+getzf(oSen);//最后拼接时间
                return oTime;
            }
            //补0操作
            function getzf(num){
                if(parseInt(num) < 10){
                    num = '0'+num;
                }
                return num;
            }
            // 退款进度
            $('a[id^="refund_history_"]').on("click", function () {
                var orderId = $(this).attr('id').substring("refund_history_".length);
                $.post('loadrefundprocess.vpage', {orderId: orderId}, function (data) {
                    var dialog = $('#loadrefundprocessModal');
                    dialog.modal('show');
                    if (data.success) {
                        $(".JS-refundBox").show();
                        $(".JS-payStyle").show();
                        $(".JS-orderNo").text(data.out_trade_no);
                        $(".JS-wechatNo").text(data.transaction_id);
                        $(".JS-orderMo").text(data.total_fee);
                        var refundProcessContent = ('<tr class="JS-processTr"><td>'+data.refund_channel_0+'</td>' +
                        '<td>'+data.refund_fee_0+'</td>' +
                        '<td>'+data.refund_status_0+'</td>' +
                        '<td>'+data.refund_recv_accout_0+'</td>' +
                        '<td>'+data.refund_success_time_0+'</td></tr>');
                        $(".JS-refundProcess").find(".JS-processTr").remove();
                        $(".JS-refundProcess").find("thead").append(refundProcessContent);
                    }else{
                        $(".JS-refundBox").hide();
                        $(".JS-errorCode").show().text("错误码：" + data.info);

                    }
                });
            });
            function hisListFun(hisObj){
                if (hisObj == null){
                    return "";
                }else{
                    return hisObj;
                }
            }
            //支付记录
            $('a[id^="payment_history_"]').on("click", function () {
                var orderId = $(this).attr('id').substring("payment_history_".length);
                $.post('loadpaidhistory.vpage', {orderId: orderId}, function (data) {
                    if (data.success) {
                        var dialog = $('#loadpaidhistoryModal');
                        dialog.find(".historyTr").html("");
                        var foreachContent = "";
                        var hisList = data.historyList;
                        for (var i = 0 ;i<hisList.length;i++){
                            foreachContent += ('<tr class="historyTr"><td>'+getMyDate(hisList[i].createDatetime)+'</td>' +
                            '<td>'+hisListFun(hisList[i].paymentStatus)+'</td>' +
                            '<td>'+hisListFun(hisList[i].payAmount)+'</td>' +
                            '<td>'+getMyDate(hisList[i].payDatetime)+'</td>' +
                            '<td>'+hisListFun(hisList[i].payMethodName)+'</td>' +
                            '<td>'+hisListFun(hisList[i].outerTradeId)+'</td>' +
                            '<td>'+hisListFun(hisList[i].comment)+'</td>' +
                            '<td>'+getMyDate(hisList[i].serviceStartTime)+'</td>' +
                            '<td>'+getMyDate(hisList[i].serviceEndTime)+'</td></tr>');
                        }
                        dialog.find("thead").append(foreachContent);
                        dialog.modal('show');
                    }else{
                        alert(data.info);
                    }
                });
            });

            // 申请退款(已废用，新版见newVM)
            var vm = new Vue({
                el: '#refundProductModal',
                data: {
                    refundProductList: '',
                    allRefundAmount: 0, // 退款总金额
                    refundCause: ''
                },
                mounted: function () {
//                    var _this = this;
//                    // 点击申请退款按钮
//                    $(document).on("click", 'a[id^="refund_afenti_order_"]', function () {
//                        var orderId = $(this).attr('id').substring("refund_afenti_order_".length);
//
//                        // 根据multirefund字段的值来决定是否走不同的逻辑
//                        if ($(this).parents('tr').find('td').eq(0).attr('data-multirefund') == 'true') {
//                            $.post('/legacy/afenti/refundproducts.vpage', { oid: orderId }, function (data) {
//                                if (data.success) {
//                                    _this.refundProductList = data.data;
//                                    _this.allRefundAmount = 0;
//                                    _this.refundCause = '';
//                                    _this.addListCheckParam();
//                                    var dialog = $('#refundProductModal');
//                                    dialog.modal('show').attr('data-orderid', orderId);
//                                } else {
//                                    alert(data.info);
//                                }
//                            });
//                        } else {
//                            // 走老流程
//                            $('#refund_amount').html(''); // 退款金额
//                            $('#btn_submit_refund, #chk_full_refund, #chk_refund_days').attr('oid', orderId);
//                            $('#refundModal').modal('show');
//                            $('#sl_refund_days').html('');
//                            $('#chk_full_refund, #chk_refund_days, #sl_refund_cause').removeAttr('checked');
//                            var userId = $(this).attr('user');
//                            $('#student_entity_reward_detail').attr('href', '/fairyland/studententityrewardrecordlist.vpage?studentId=' + userId);
//                            // 查出可退天数
//                            getRefundDays(orderId);
//                        }
//                    });
                },
                methods: {
                    // check radio
                    checkRefund: function (refundProduct, index, event) {
                        var _this = this;
                        var checked = $(event.target).is(":checked");
                        refundProduct._checked = checked;
                        if (checked) {
                            _this.allRefundAmount = _this.getAllRefundAmount();
                            $.post('/legacy/afenti/multirefunddays.vpage', {
                                oid: refundProduct.orderId,
                                productId: refundProduct.productId
                            }, function (data) {
                                if (data.success) {
                                    if (data.max !== data.min) { // 展示按天选
                                        var arr = [];
                                        for (var i = 0; i <= data.max - data.min; i++) {
                                            arr.push({
                                                typeName: (i + data.min) + '天',
                                                typeValue: i + data.min
                                            });
                                        }
                                        refundProduct._refundTypeList = refundProduct._refundTypeList.concat(arr);
                                        _this.refundProductList.splice(index, 1, refundProduct);
                                        console.log('1111', _this.refundProductList)
                                    }
                                } else {
                                    alert(data.info);
                                }
                            });
                        } else {
                            refundProduct._refundTypeValue = 0;
                            refundProduct._refundTypeList = [{
                                typeName: '全额退',
                                typeValue: 0
                            }];
                            _this.refundProductList.splice(index, 1, refundProduct);
                            _this.allRefundAmount = _this.getAllRefundAmount();
                        }
                    },
                    addListCheckParam: function (list) {
                        for (var i = 0; i < this.refundProductList.length; i++) {
                            this.refundProductList[i]._checked = false; // 选中退款产品
                            this.refundProductList[i]._refundTypeValue = 0; // 退款产品
                            this.refundProductList[i]._payAmount = this.refundProductList[i].payAmount; // 支付金额（增加字段表示动态金额，原有后端字段不动）
                            this.refundProductList[i]._refundTypeList = [{ // 退款下拉list
                                typeName: '全额退',
                                typeValue: 0
                            }];
                        }
                    },
                    // 选择退款方式
                    selectRefundType: function (refundProduct, event) {
                        refundProduct._refundTypeValue = parseInt($(event.target).val());
                        if (refundProduct._refundTypeValue == 0) { // 全额退时不走接口，直接取默认值
                            refundProduct._payAmount = refundProduct.payAmount;
                            this.allRefundAmount = this.getAllRefundAmount();
                        } else {
                            var _this = this;
                            $.post('/legacy/afenti/multirefundamount.vpage', {
                                oid: refundProduct.orderId,
                                productId: refundProduct.productId,
                                days: refundProduct._refundTypeValue
                            }, function (data) {
                                if (data.success) {
                                    refundProduct._payAmount = data.refundAmount;
                                    _this.allRefundAmount = _this.getAllRefundAmount();
                                } else {
                                    alert(data.info);
                                }
                            });
                        }
                    },
                    // 选择退款原因
                    choiceRefundCause: function (event) {
                        this.refundCause = $(event.target).val();
                    },
                    // 获取总金额
                    getAllRefundAmount: function () {
                        var allAmount = 0;
                        for (var i = 0; i < this.refundProductList.length; i++) {
                            if (this.refundProductList[i]._checked) {
                                allAmount = this.accAdd(allAmount, this.refundProductList[i]._payAmount);
                            }
                        }
                        return allAmount;
                    },
                    // 最后提交
                    submitMultiRefund: function () {
                        var hasChoiceRefund = false; // 是否勾选过产品
                        var lastContactData = ''; // 提交data
                        for (var i = 0; i < this.refundProductList.length; i++) {
                            if (this.refundProductList[i]._checked) {
                                hasChoiceRefund = true;
                                lastContactData = lastContactData + ',' + (this.refundProductList[i].productId + '|' + this.refundProductList[i]._payAmount);
                            }
                        }
                        if (!hasChoiceRefund) {
                            alert('您还未选择任何退款产品')
                            return false;
                        }

                        if (this.refundCause == '') {
                            alert('您还未选择退款原因')
                            return false;
                        }
                        $.post('/legacy/afenti/multirefund.vpage', {
                            oid: $('#refundProductModal').attr('data-orderid'),
                            data: lastContactData.substr(1),
                            memo: this.refundCause
                        }, function (data) {
                            if (data.success) {
                                alert('提现申请已提交');
                            } else {
                                alert(data.info);
                            }
                        });
                    },
                    // 解决浮点数相加精度丢失bug
                    accAdd: function (arg1, arg2) {
                        var r1, r2, m, c;
                        try {
                            r1 = arg1.toString().split(".")[1].length;
                        }
                        catch (e) {
                            r1 = 0;
                        }
                        try {
                            r2 = arg2.toString().split(".")[1].length;
                        }
                        catch (e) {
                            r2 = 0;
                        }
                        c = Math.abs(r1 - r2);
                        m = Math.pow(10, Math.max(r1, r2));
                        if (c > 0) {
                            var cm = Math.pow(10, c);
                            if (r1 > r2) {
                                arg1 = Number(arg1.toString().replace(".", ""));
                                arg2 = Number(arg2.toString().replace(".", "")) * cm;
                            } else {
                                arg1 = Number(arg1.toString().replace(".", "")) * cm;
                                arg2 = Number(arg2.toString().replace(".", ""));
                            }
                        } else {
                            arg1 = Number(arg1.toString().replace(".", ""));
                            arg2 = Number(arg2.toString().replace(".", ""));
                        }
                        return (arg1 + arg2) / m;
                    }
                }
            });

            var newVM = new Vue({
                el: '#newRefundModal',
                data: {
                    newRefundDataInfo: '',
                    newRefundItemList: '',
                    newRefundCause: '',
                    chipsRefundedText:''
                },
                mounted: function () {
                    var _this = this;
                    // newVM实际对应申请退款的弹窗，但是申请退款的绑定事件必须放在此处
                    $(document).on("click", 'a[id^="refund_afenti_order_"]', function () {
                        var orderId = $(this).attr('id').substring("refund_afenti_order_".length);
                        $.post('/legacy/afenti/loadrefund.vpage', {
                            oid: orderId
                        }, function (data) {
                            if (data.success) {
                                _this.newRefundDataInfo = data;
                                _this.newRefundItemList = data.itemList;
                                _this.newRefundCause = '';
                                _this.addListCheckParam();
                                _this.chipsRefundedText = data.chipsRefundedText;
                                $('#newRefundModal').modal('show');
                                var entityReward = data.hasEntityReward;
                                if (entityReward) {
                                    $('#student_entity_reward').show();
                                } else {
                                    $('#student_entity_reward').hide();
                                }
                            } else {
                                alert(data.info);
                            }
                        });
                    });

                    // 阻止全局的keydown触发表单提交
                    $(document).on('keydown', '.refund-amount-input', function (evt) {
                        if (evt.keyCode === 13) {
                            return false;
                        }
                    });
                },
                methods: {
                    // 增加字段
                    addListCheckParam: function () {
                        var newRefundItemList = this.newRefundItemList;
                        for (var i = 0, len = newRefundItemList.length; i < len; i++) {
                            this.$set(newRefundItemList[i], '_checked', false); // 选中退款产品
                            this.$set(newRefundItemList[i], '_refundValue', ''); // 退款金额
                            this.$set(newRefundItemList[i], '_isInputError', false); // 输入框报错变红
                        }
                    },
                    newCheckRefund: function (refundItem, event) {
                        var checked = $(event.target).is(":checked");
                        refundItem._checked = checked;
                    },
                    checkInputRefundAmount: function (refundItem) {
                        if (parseInt(refundItem._refundValue) > refundItem.payAmount
                            || refundItem._checked && !refundItem._refundValue) {
                            refundItem._isInputError = true;
                        } else {
                            refundItem._isInputError = false;
                        }
                    },
                    submitNewRefund: function () {
                        var newRefundItemList = this.newRefundItemList;
                        var hasError = false;
                        var hasChoice = false;
                        for (var i = 0, len = newRefundItemList.length; i < len; i++) {
                            if (newRefundItemList[i]._checked){
                                hasChoice = true;
                            }
                            // 勾选，但未输入退款金额
                            if (newRefundItemList[i]._checked && !newRefundItemList[i]._refundValue) {
                                newRefundItemList[i]._isInputError = true;
                                hasError = true;
                            }
                            // 勾选，但退款金额 > 付款金额
                            if (newRefundItemList[i]._checked && parseFloat(newRefundItemList[i]._refundValue) > parseFloat(newRefundItemList[i].payAmount)) {
                                newRefundItemList[i]._isInputError = true;
                                hasError = true;
                            }
                            //一去学的退款金额不能大于可退款金额
                            if(newRefundItemList[i].itemType=="YiQiXue"){
                                if (newRefundItemList[i]._checked && parseFloat(newRefundItemList[i]._refundValue) > parseFloat(newRefundItemList[i].refundableAmount)) {
                                    newRefundItemList[i]._isInputError = true;
                                    hasError = true;
                                }
                            }

                        }
                        if (!hasChoice) {
                            alert('您还未选择退款产品');
                            return;
                        }
                        if (hasError) {
                            return;
                        }
                        if (!this.newRefundCause) {
                            alert('您还未选择退款原因');
                            return;
                        }
                        this.requestRefund();
                    },
                    requestRefund: function () {
                        var newRefundItemList = this.newRefundItemList;
                        var refundType = this.newRefundDataInfo.type;
                        var checkedListInfo = [];
                        for (var i = 0, len = newRefundItemList.length; i < len; i++) {
                            if (newRefundItemList[i]._checked) {
                                checkedListInfo.push({
                                    itemId: newRefundItemList[i].itemId,
                                    refundAmount: newRefundItemList[i]._refundValue,
                                    itemList: newRefundItemList[i].orderItems,
                                    refundType : refundType
                                });
                            }
                        }
                        if (confirm("确定申请退款订单吗？退款申请成功后不可撤回，系统会立即进行自动退款！")) {
                            $.post('/legacy/afenti/refundorder.vpage', {
                                oid: this.newRefundDataInfo.orderId,
                                memo: this.newRefundCause,
                                orderData: JSON.stringify(checkedListInfo)
                            }, function (data) {
                                if (data.success) {
                                    alert('退款申请已提交，微信支付订单已在退款中');
                                    $('#newRefundModal').modal('hide');
                                    window.location.reload();
                                } else {
                                    alert(data.info);
                                }
                            });
                        }
                    }
                }
            });

            // 查看优惠劵弹窗
            $('a[id^="check_coupon_"]').on("click", function () {
                $('#couponId').html('');
                $('#couponName').html('');
                $('#couponType').html('');
                $('#typeValue').html('');

                var orderId = $(this).attr('id').substring("check_coupon_".length);
                // 获取信息
                $.post('loadcoupon.vpage', {oid: orderId}, function (data) {
                    if (data.success) {
                        $('#couponId').html(data.couponId);
                        $('#couponName').html(data.couponName);
                        $('#couponType').html(data.couponType);
                        $('#typeValue').html(data.typeValue);
                    }
                });
                var dialog = $('#couponModal');
                dialog.modal('show');
            });

            //提交退款申请
            $('#btn_submit_refund').on('click', function () {
                if (!$('#chk_refund_days').is(':checked') && !$('#chk_full_refund').is(':checked')) {
                    alert('请选择退款方式');
                    return;
                }

                if ($('#sl_refund_cause option:selected').val() == ""){
                    alert('请选择退款原因');
                    return;
                }

                var orderId = $(this).attr('oid');
                var refundCause = $('#sl_refund_cause option:selected').val();
                var refundDays = 0;
                if ($('#chk_refund_days').is(':checked')) {
                    refundDays = $('#sl_refund_days option:selected').val();
                }

                $.post('refund.vpage', {oid: orderId, memo: refundCause, days: refundDays}, function (data) {
                    if (data.success) {
                        alert('提现申请已提交');
                    } else {
                        alert(data.info);
                    }
                    $('#searchBtn').click(); // 模拟点击，更新表格数据
                });
            });

            $('#myTab a').each(function (i) {
                if (i == ${activeTab}) {
                    $(this).tab('show');
                }
            });

            //延期按钮
            $('input[name="delayActivationButton"]').click(function () {
                var historyId = $(this).attr('historyid');

                var dialog = $('#delayAfentiActivationHistoryModal');
                dialog.find('#delaySubmit').attr('historyid', historyId);
                dialog.modal('show');
            });
            $('#delaySubmit').click(function () {
                var historyId = $(this).attr('historyid');
                var delayDays = $('#delayDays').val();
                var delayReason = $('#delayReason').val();

                if (!checkDelayParas(delayDays, delayReason)) {
                    return false;
                }

                $.post(
                        'delayafentiactivationhistory.vpage',
                        {
                            historyId: historyId,
                            delayDays: delayDays,
                            delayReason: delayReason
                        },
                        function (data) {
                            $('#delaySubmit').attr('historyid', '');
                            if (data.status == 'false') {
                                alert(data.message);
                            } else {
                                window.location.href = 'main.vpage?userId=' + data.userid + '&activeTab=' + data.activeTab;
                            }
                        }
                );
            });
            var checkDelayParas = function (delayDays, delayReason) {
                try {
                    if (delayDays == '') {
                        throw('请输入延期天数！');
                    }
                    if(!isNumber(delayDays)){
                        throw('延长天数只能为纯数字');
                    }
                    var days = parseInt(delayDays);
                    if (days <= -365 || days > 365 || days == 0) {
                        throw('延期天数只能输入-365 - 365天！');
                    }
                    if (delayReason == '') {
                        throw('请输入延期原因！');
                    }
                } catch (err) {
                    alert(err);
                    return false;
                }
                return true;
            }
        });

        function isNumber(value){
            var reg = /^(\-|\+)?\d+$/;
            if($.trim(value) == '' || !reg.test(value)){
                return false;
            }
            return true;
        }

        // 获取申请退款弹窗数据
        function getRefundDays() {
            var orderId = $('#chk_refund_days').attr('oid');
            $.post('refunddays.vpage', {oid: orderId}, function (data) {
                if (data.success) {
                    var max = data.max;
                    var min = data.min;
                    var refundType = data.refundType;
                    $('#notifyInfo').text("");
                    if (data.notifyInfo != null) {
                        $('#notifyInfo').text(data.notifyInfo);
                    }
                    if (max == min || refundType == 'times' || refundType == 'allRefund') {
                        $('#ul_refund_days').hide();
                    } else {
                        $('#ul_refund_days').show();
                        $('#sl_refund_days').html('');
                        for (var i = min; i <= max; i++) {
                            $('#sl_refund_days').append('<option value="' + i + '">' + i + '天</option>');
                        }
                    }

                    var entityReward = data.entityReward;
                    if (entityReward) {
                        $('#student_entity_reward').show();
                    } else {
                        $('#student_entity_reward').hide();
                    }
                } else {
                    alert(data.info);
                    $('#refundModal').modal('hide');
                }
            });
        }

        // 操作按天退、全额退、选中天数后更新退款金额
        function getRefundAmount(oid, days) {
            $.post('refundamount.vpage', {oid: oid, days: days}, function (data) {
                if (data.success) {
                    $('#refund_amount').html(data.refundAmount + '元');
                } else {
                    alert(data.info);
                    $('#refundModal').modal('hide');
                }
            });
        }

        $('#chk_full_refund').on('click', function () {
            var orderId = $(this).attr('oid');
            getRefundAmount(orderId, 0);
        });
        $('#chk_refund_days').on('click', function () {
            var orderId = $(this).attr('oid');
            var refundDays = $('#sl_refund_days option:selected').val();
            getRefundAmount(orderId, refundDays);
        });

        $('#sl_refund_days').on('change', function () {
            var orderId = $('#chk_refund_days').attr('oid');
            getRefundAmount(orderId, $('#sl_refund_days option:selected').val());
        });
    </script>

    <script type="text/javascript">
        function booksModelView() {
            var self = this;

            self.orderId = ko.observable();
            self.books = ko.observableArray();
            self.selectedBook = ko.observable();
            self.hasAvaliableBooks = ko.pureComputed(function () {
                return self.books().length > 0;
            }, self);
            self.canSubmitChangeBook = ko.pureComputed(function () {
                return self.selectedBook() != undefined;
            });

            //提交换购请求
            self.changeBook = function () {
                console.log(self.orderId() + ' ' + self.selectedBook());

                $.post('changebook.vpage', {oid: self.orderId(), pid: self.selectedBook()}, function (data) {
                    if (data.success) {
                        alert('操作成功');
                        window.location.reload();
                    } else {
                        alert(data.info);
                    }
                });
            }
        }
        ;
        var bookView = new booksModelView();
        ko.applyBindings(bookView, document.getElementById('modal_change_book'));


        //换购教材入口
        function changeBook(orderId) {
            $('#modal_change_book').modal('show');
            bookView.books.removeAll();

            bookView.orderId(orderId);

            $.post('booksforchange.vpage', {oid: orderId}, function (data) {
                if (data.success) {
                    var books = data.books;
                    for (var i = 0; i < books.length; i++) {
                        bookView.books.push(books[i]);
                    }
                } else {
                    $('#modal_change_book').modal('hide');
                    alert(data.info);
                }
            });
        }
    </script>

    <script type="text/javascript">
        var batchRefundOrderIdList = []; ; // 退款订单lsit（前台展示）
        var batchRefundList = []; // 退款信息列表（真实提交）
        var batchRefundAmount = 0; // 总退款金额（前台展示）
        var batchRefundType = 1;  // 默认折算退
        var hasError = false;

        // 过滤
        var filterBatchRefundType = function (type) {
            if (type === 1) { // 过滤折算退
                $(".refundTableTd[data-refundtype='1']").show();
                $(".refundTableTd[data-refundtype='2']").hide();
            } else if (type === 2) { // 过滤全额退
                $(".refundTableTd[data-refundtype='1']").hide();
                $(".refundTableTd[data-refundtype='2']").show();
            }
        };

        // 点击过滤类型（折算退、全额退）
        $("input[name='batchRefundType'").on('change', function () {
            if ($(this).attr('id') === 'batchRefundType1') { // 折算退
                filterBatchRefundType(1);
                batchRefundType = 1;
            } else { // 全额退
                filterBatchRefundType(2);
                batchRefundType = 2;
            }

            // 切换过滤条件时清空选中的
            $('.batchChoiceAll').prop("checked", false);
            toChoiceAll(false);
            // 重置输入的值
            $('.inputRefundAmount').each(function () {
                $(this).removeClass('input-refund-error').val($(this).parents('tr').attr('data-refundamount')); // 重置输入框
                $(this).parents('tr').attr('data-inputrefundamount', $(this).parents('tr').attr('data-refundamount')); // 重置data绑定
            });
        });

        // 点击全选复选框
        $('.batchChoiceAll').on('click', function () {
            batchRefundAmount = 0;
            $('#totalRefundAmount').text(batchRefundAmount);
            batchRefundOrderIdList = [];
            batchRefundList = [];
            hasError = false;

            if ($(this).is(":checked")) {
                toChoiceAll(true);
            } else {
                toChoiceAll(false);
            }
        });
        // 全选 或 取消全选
        var toChoiceAll = function (isChecked) {
            if (isChecked) { // 全选
                if (batchRefundType === 1) { // 折算退
                    $("tr[data-refundtype='1']").each(function () { // each 所有的折算退list
                        $(this).find("input[type='checkbox']").prop("checked", isChecked);

                        if (!$(this).find(".inputRefundAmount").hasClass('input-refund-error')) {
                            batchRefundAmount = accAdd(batchRefundAmount, +$(this).attr('data-inputrefundamount')); // 追加 所有选中的金额
                            batchRefundOrderIdList.push($(this).attr('data-orderid')) // 收集 所有的orderId(未去重)
                            batchRefundList.push({
                                orderId: $(this).attr('data-orderid'),
                                itemId: $(this).attr('data-itemid'),
                                refundAmount: $(this).attr('data-inputrefundamount')
                            }); // 收集具体的退款信息
                        } else {
                            hasError = true;
                        }
                    });
                } else { // 全额退
                    $("tr[data-refundtype='2']").each(function () { // each 所有的全额退list
                        $(this).find("input[type='checkbox']").prop("checked", isChecked);

                        if (!$(this).find(".inputRefundAmount").hasClass('input-refund-error')) {
                            batchRefundAmount = accAdd(batchRefundAmount, +$(this).attr('data-inputrefundamount')); // 追加 所有选中的金额
                            batchRefundOrderIdList.push($(this).attr('data-orderid')) // 收集 所有的orderId(未去重)
                            batchRefundList.push({
                                orderId: $(this).attr('data-orderid'),
                                itemId: $(this).attr('data-itemid'),
                                refundAmount: $(this).attr('data-inputrefundamount')
                            }); // 收集具体的退款信息
                        } else {
                            hasError = true;
                        }
                    });
                }

                $('#totalRefundAmount').text(batchRefundAmount);
            } else { // 取消全选
                $(".batchChoiceSingle").each(function () {
                    $(this).prop("checked", isChecked);
                });

                batchRefundAmount = 0;
                $('#totalRefundAmount').text(batchRefundAmount);
                batchRefundOrderIdList = [];
                batchRefundList = [];
                hasError = false;
            }
        };

        // 单选
        $('.batchChoiceSingle').on('click', function () {
            toChoiceSingle();
        });

        // 操作某一条的回调
        var toChoiceSingle = function () {
            // 计算总金额 和 收集选中的orderId
            batchRefundAmount = 0;
            $('#totalRefundAmount').text(batchRefundAmount);
            batchRefundOrderIdList = [];
            batchRefundList = [];
            hasError = false;

            if (batchRefundType === 1) { // 折算退
                $("tr[data-refundtype='1']").each(function () { // each 所有的折算退list
                    if ($(this).find("input[type='checkbox']").is(":checked")) {
                        if (!$(this).find(".inputRefundAmount").hasClass('input-refund-error')) {
                            batchRefundAmount = accAdd(batchRefundAmount, +$(this).attr('data-inputrefundamount')); // 追加 所有选中的金额
                            batchRefundOrderIdList.push($(this).attr('data-orderid')); // 收集 所有选中的orderId(未去重)
                            batchRefundList.push({
                                orderId: $(this).attr('data-orderid'),
                                itemId: $(this).attr('data-itemid'),
                                refundAmount: $(this).attr('data-inputrefundamount')
                            }); // 收集具体的退款信息
                        } else {
                            hasError = true;
                        }
                    } else { // 只要有一个子级checkbox被取消勾选，就把全选的checkbox取消勾选
                        $('.batchChoiceAll').prop("checked", false);
                    }
                });
            } else { // 全额退
                $("tr[data-refundtype='2']").each(function () { // each 所有的全额退list
                    if ($(this).find("input[type='checkbox']").is(":checked")) {
                        if (!$(this).find(".inputRefundAmount").hasClass('input-refund-error')) {
                            batchRefundAmount = accAdd(batchRefundAmount, +$(this).attr('data-inputrefundamount')); // 追加 所有选中的金额
                            batchRefundOrderIdList.push($(this).attr('data-orderid')); // 收集 所有选中的orderId(未去重)
                            batchRefundList.push({
                                orderId: $(this).attr('data-orderid'),
                                itemId: $(this).attr('data-itemid'),
                                refundAmount: $(this).attr('data-inputrefundamount')
                            }); // 收集具体的退款信息
                        } else {
                            hasError = true;
                        }
                    } else { // 只要有一个子级checkbox被取消勾选，就把全选的checkbox取消勾选
                        $('.batchChoiceAll').prop("checked", false);
                    }
                });
            }

            $('#totalRefundAmount').text(batchRefundAmount);
        };

        // 监测手动输入的退款金额
        $('.inputRefundAmount').on('input', function () {
            var $this = $(this);

            // 只允许用户输入小数点后面两位
            if ($this.val().split('.').length == 2 && $this.val().split('.')[1].length >= 2) {
                $this.val(parseFloat($this.val()).toFixed(2));
            }

            $this.parents('tr').attr('data-inputrefundamount', $this.val()); // 记录当前输入的值
            if (isNaN(+$this.val()) || +$this.val() <= 0 || +$this.val() > +($this.parents('tr').attr('data-refundamount'))) {
                $this.addClass('input-refund-error');
            } else {
                $this.removeClass('input-refund-error');
            }

            toChoiceSingle();
        });

        // 点击申请退款按钮
        $('#batchRefundButton').on('click', function () {
            if (hasError) {
                alert('您输入的退款金额有误，请修改后再提交申请');
                return ;
            }
            if (!batchRefundList.length) {
                alert('请勾选订单后，再提交申请');
                return ;
            }
            if (!$('#batchRefundCauseSelect').val()) {
                alert('请先选择退款原因，再提交申请');
                return ;
            }


            $('#batchRefundPara').html('您已勾选' + removeArrRepeat(batchRefundOrderIdList).length + '条订单，退款总金额为' + batchRefundAmount + '元，确认退款吗？退款申请成功后不可撤回，系统会立即进行自动退款！')
            $('#batchRefundSureModal').modal('show');
        });

        // 请求接口批量退款
        $('#btn_batch_refund').on('click', function () {
            $.post('/legacy/afenti/batchrefundorder.vpage', {
                orderData: JSON.stringify(batchRefundList),
                memo: $('#batchRefundCauseSelect').val()
            }, function (res) {
                if (res.success) {
                    alert(res.info);
                    window.location.reload();
                } else {
                    alert("批量退款申请失败");
                    window.location.reload();
                }
            });
        });

        // 解决浮点数相加精度丢失
        function accAdd(arg1, arg2) {
            var r1, r2, m, c;
            try {
                r1 = arg1.toString().split(".")[1].length;
            } catch (e) {
                r1 = 0;
            }
            try {
                r2 = arg2.toString().split(".")[1].length;
            } catch (e) {
                r2 = 0;
            }
            c = Math.abs(r1 - r2);
            m = Math.pow(10, Math.max(r1, r2));
            if (c > 0) {
                var cm = Math.pow(10, c);
                if (r1 > r2) {
                    arg1 = Number(arg1.toString().replace(".", ""));
                    arg2 = Number(arg2.toString().replace(".", "")) * cm;
                } else {
                    arg1 = Number(arg1.toString().replace(".", "")) * cm;
                    arg2 = Number(arg2.toString().replace(".", ""));
                }
            } else {
                arg1 = Number(arg1.toString().replace(".", ""));
                arg2 = Number(arg2.toString().replace(".", ""));
            }
            return (arg1 + arg2) / m;
        }

        // 数组去重
        function removeArrRepeat(myArray){
            var newArray = []; //一个新的临时数组
            for(var i = 0; i < myArray.length; i++){ //遍历当前数组
                //如果当前数组的第i项已经保存进了临时数组，那么跳过，
                //否则把当前项push到临时数组里面
                if (newArray.indexOf(myArray[i]) === -1){ //在新数组中查找原数组的每一项是否存在
                    newArray.push(myArray[i]); //如果不存在就加到新数组中
                }
            }
            return newArray;
        }

        // 默认勾选折算退
        $("#batchRefundType1").prop('checked', true);
        filterBatchRefundType(1);
    </script>
</div>

</@layout_default.page>
