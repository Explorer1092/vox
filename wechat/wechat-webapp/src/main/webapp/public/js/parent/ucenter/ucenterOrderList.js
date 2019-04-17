/*
 * 会员中心-订单列表
 */
define(["jquery", "$17", "knockout", "logger"], function ($, $17, knockout, logger) {
    /****************变量声明***********/
    var typeDefault = $17.getQuery('_to');

    var ordersModalAndView = {
        ordersPaid: knockout.observableArray([]),
        ordersUnPaid: knockout.observableArray([]),
        ordersPaidNumber: knockout.observable(1),
        ordersUnPaidNumber: knockout.observable(1),
        ordersPaidCount: knockout.observable(0),
        ordersUnPaidCount: knockout.observable(0),
        orderType: knockout.observable("unpaid"),
        orderPaidLoaded: knockout.observable(false),
        orderUnPaidLoaded: knockout.observable(false),
        shouldPaidMoreBtn: knockout.observable(true),
        shouldUnPaidMoreBtn: knockout.observable(true),
        getPaidOrders: getPaidOrders,
        getUnPaidOrders: getUnPaidOrders,
        showUnPaidMore: showUnPaidMore,
        showPaidMore: showPaidMore
    };

    /****************方法声明***********/
    function getPaidOrders() {
        ordersModalAndView.orderType("paid");
        if (!ordersModalAndView.orderPaidLoaded()) {
            loadOrderList(1, "paid");
        }
    }

    function getUnPaidOrders() {
        ordersModalAndView.orderType("unpaid");
        if (!ordersModalAndView.orderUnPaidLoaded()) {
            loadOrderList(1, "unpaid");
        }
    }

    function showPaidMore() {
        var numberCurrent = ordersModalAndView.ordersPaidNumber();
        loadOrderList(numberCurrent, "paid");
    }

    function showUnPaidMore() {
        var numberCurrent = ordersModalAndView.ordersUnPaidNumber();
        loadOrderList(numberCurrent, "unpaid");
    }

    function loadOrderList(pageNumber, orderType) {
        var param = {
            type: orderType,
            index: pageNumber
        };
        $.post('/parent/ucenter/orders.vpage?' + $.param(param), function (data) {
            if (data.success) {
                if (orderType == "paid") {
                    for (var i = 0; i < 2; i++) {
                        if (data.orders[i]) {
                            data.orders[i]["priceStr"]  = parseFloat(data.orders[i].price).toFixed(2);
                            ordersModalAndView.ordersPaid.push(data.orders[i]);
                        }
                    }
                    if (ordersModalAndView.ordersPaid().length == data.count) {
                        ordersModalAndView.shouldPaidMoreBtn(false);
                    }
                    ordersModalAndView.ordersPaidNumber(ordersModalAndView.ordersPaidNumber() + 1);
                    ordersModalAndView.orderPaidLoaded(true);
                } else {
                    for (var i = 0; i < 2; i++) {
                        if (data.orders[i]) {
                            debugger;
                            data.orders[i]["priceStr"]  = parseFloat(data.orders[i].price).toFixed(2);
                            ordersModalAndView.ordersUnPaid.push(data.orders[i]);
                        }
                    }
                    if (ordersModalAndView.ordersUnPaid().length == data.count) {
                        ordersModalAndView.shouldUnPaidMoreBtn(false);
                    }
                    ordersModalAndView.ordersUnPaidNumber(ordersModalAndView.ordersUnPaidNumber() + 1);
                    ordersModalAndView.orderUnPaidLoaded(true);
                }
            } else {
                $17.jqmHintBox(data.info);
            }
        });
    }

    /****************事件交互***********/

    knockout.applyBindings(ordersModalAndView);

    /**
     * 默认加载判断
     */
    if (typeDefault && typeDefault == "paid") {
        ordersModalAndView.orderType("paid");
        loadOrderList(1, "paid");
    } else {
        loadOrderList(1, "unpaid");
    }

    $(document).on("click", ".js-payOrder", function () {
        var self = this;
        setTimeout(function () {
            location.href = $(self).attr("data-href") + "&&_from=unpay_order";
        }, 1000);
    })
});