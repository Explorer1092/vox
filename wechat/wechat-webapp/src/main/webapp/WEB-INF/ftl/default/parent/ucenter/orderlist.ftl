<#import "../layout.ftl" as ucenterOrderList>
<@ucenterOrderList.page title='个人中心' pageJs="ucenterOrderList">
<div class="main">
    <div class="page_title_box">
        <span>家长号${pid!""}</span>
    </div>

    <div class="pay_change_box">
        <p id="state_box">
            <a data-type="paid" href="javascript:void (0);" data-bind="click: getPaidOrders,css:{active:orderType() == 'paid',load:orderPaidLoaded}">已开通</a>
            <a data-type="unpaid" href="javascript:void (0);" data-bind="click: getUnPaidOrders,css:{active:orderType() == 'unpaid',load:orderUnPaidLoaded}">未支付</a>
        </p>
    </div>
    <div id="paid_box" data-bind="style: { display: orderType() == 'paid'?'block':'none'}">
        <div data-bind="if:!ordersPaid().length">
            <div class="empty-list" style="text-align:center;color:#999;border:1px solid #e1e1e1;margin:0 20px;line-height:4em">还没有开通任何服务</div>
        </div>
        <div data-bind="foreach: ordersPaid">
            <div data-widget="table" class="w_table ml-20 mr-20 mb_30">
                <table>
                    <tbody>
                    <tr class="odd">
                        <td style="width: 230px;" >
                            <#--订单号：<br>
                            <span data-bind="text: id"></span>-->
                            <span data-bind="text: productName"></span>
                        </td>
                        <td style="width: 100px;">价格</td>
                        <#--<td style="width: 100px;">有效期</td>-->
                        <td>操作</td>
                    </tr>
                    <tr>
                        <td data-bind="text: name"></td>
                        <td data-bind="text: '￥'+priceStr"></td>
                        <#--<td data-bind="text: period"></td>-->
                        <td>已开通</td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </div>
        <div style="width:94%; margin:0 auto; text-align:center; padding-bottom:20px;">
            <a style="font-size:30px;" data-bind="visible: shouldPaidMoreBtn,click: showPaidMore,attr: { data_num:ordersPaidNumber,data_order_t: 'paid' }" href="javascript:void (0);" class="ui-btn ui-btn-b ui-corner-all showMoreOrderBut">查看更多</a>
            <img src="" alt="加载中..." style="display: none;">
        </div>
    </div>
    <div id="unpaid_box" data-bind="style: { display: orderType() == 'unpaid'?'block':'none'}">
        <div data-bind="if:!ordersUnPaid().length">
            <div class="empty-list" style="text-align:center;color:#999;border:1px solid #e1e1e1;margin:0 20px;line-height:4em">暂时不存在未支付的订单</div>
        </div>
        <div data-bind="foreach: ordersUnPaid">
            <div data-widget="table" class="w_table ml-20 mr-20 mb_30">
                <table>
                    <tbody>
                    <tr class="odd">
                        <td style="width: 230px;" >
                           <#-- 订单号：<br>
                            <span data-bind="text: id"></span>-->
                            <span data-bind="text: productName"></span>
                        </td>
                        <td style="width: 100px;">价格</td>
                        <#--<td style="width: 100px;">有效期</td>-->
                        <td>操作</td>
                    </tr>
                    <tr>
                        <td data-bind="text: name"></td>
                        <td data-bind="text: '￥'+priceStr"></td>
                        <#--<td data-bind="text: period"></td>-->
                        <td>
                            <!-- ko if: orderType == 'trustee' -->
                                <div class="btn_mark btn_green js-payOrder" data-bind="attr : {'data-href' : '/parent/wxpay/trustee_confirm.vpage?oid='+$data.id}">支付</div>
                            <!-- /ko -->
                            <!-- ko if: orderType == 'afenti' -->
                                <div class="btn_mark btn_disable" data-bind="visible: productName == 'PICARO(纯正英式英语)'">暂停支付</div>
                                <div class="btn_mark btn_green js-payOrder" data-bind="visible: productName != 'PICARO(纯正英式英语)', attr : {'data-href' : '${(ProductConfig.getMainSiteBaseUrl())!''}/parentMobile/order/loadorder.vpage?oid='+$data.id}">支付</div>
                            <!-- /ko -->
                        </td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </div>
        <div style="width:94%; margin:0 auto; text-align:center; padding-bottom:20px;">
            <a style="font-size:30px;" data-bind="visible: shouldUnPaidMoreBtn,click: showUnPaidMore,attr: { data_num:ordersUnPaidNumber,data_order_t: 'unpaid' }" href="javascript:void (0);" class="ui-btn ui-btn-b ui-corner-all">查看更多</a>
            <img src="" alt="加载中..." style="display: none;">
        </div>
    </div>
</div>
<script type="text/javascript">
    function pageLog(){
        require(['logger'], function(logger) {
            logger.log({
                module: 'ucenter',
                op: 'ucenter_pv_orderlist'
            })
        })
    }
</script>
</@ucenterOrderList.page>