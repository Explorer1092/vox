/*
 * Created by free on 2015/12/15.
 */
define(["jquery", "$17", "jbox", "logger"], function ($, $17, jobx, logger) {

    var sid, trusteeType, disNo = 0;

    var createOrder = function () {
        $("#array-student").val(sid);
        $("#array-product").val(trusteeType);
        $("#orderPayForm").submit();
    };

    var verStatu = function () {
        sid = 0;
        trusteeType = "";
        $.each($('ul.js-childList>li'), function (i, item) {
            if ($(item).hasClass("active")) {
                sid = this.dataset.cid;
            }
        });

        $.each($('ul.js-sukList>li'), function (i, item) {
            if ($(item).hasClass("active")) {
                trusteeType = $(this).attr("typename");
            }
        });
        if (sid == 0) {
            $17.jqmHintBox("请选择孩子购买");
            return false;
        } else {
            if (trusteeType == "") {
                $17.jqmHintBox("请为孩子选择培训课程种类");
                return false;
            } else {
                createOrder();
            }
        }
    };

    var initStatus = function () {
        disNo = 0;
        $(".js-canPayDiv").show();
        $(".js-cannotPayDiv").hide();
        $.each($("ul.js-sukList>li"), function (i, item) {
            $(item).removeClass("disabled");
            $(item).removeClass("active");
        });
        $(".js-oldPrice").html("原价￥0");
        $(".js-neededPrice").html("0元");
    };

    $(document).on("click", "ul.js-childList>li", function () {
        var self = this;
        if (!$(this).hasClass("succeed")) {
            $(this).toggleClass("active");
            $(this).siblings("li").removeClass("active");
        }
        //getStudentData($(this).attr("data-cid"));
        initStatus();
        getStudentData(this.dataset.cid);
    });

    $(document).on("click", "ul.js-sukList>li", function () {
        var self = this;
        if (!$(this).hasClass("disabled")) {
            $(this).toggleClass("active");
            $17.tongjiTrustee("报名托管" + shopid, $(this).attr("typename"));
            logger.log({
                module: 'trustee_sign_up' + shopid,
                op: 'sku_' + $(this).attr("typename") + '_click'
            });
            $(this).siblings("li").removeClass("active");
            $(".js-oldPrice").html("原价￥" + $(this).attr("dataprice"));
            $(".js-neededPrice").html($(this).attr("datadiscountprice") + "元");
        }

        if ($("ul.js-sukList>li.active").length == 0) {
            $(".js-oldPrice").html("原价￥0");
            $(".js-neededPrice").html("0元");
        }
    });


    $(document).on("click", ".js-confirmPayBtn", function () {
        verStatu();
        $17.tongjiTrustee("购买课程" + shopid, "确认并支付");
        logger.log({
            module: 'trustee_sign_up' + shopid,
            op: 'bug_class_now_btn_click'
        });
    });

    //获取学生对应SKU购买状态
    var getStudentData = function (sid) {
        $.post("loadstudentorders.vpage", { studentId: sid}, function (result) {
            if (result.success) {
                var list = [];
                if (result.orderRecords != 0) {
                    $.each(result.orderRecords, function (i, item) {
                        list.push(item.trusteeType);
                    });
                    displaySkuItem(list);
                }
            } else {
                $17.jqmHintBox(result);
            }
        });
    };

    //控制SKU单条的渲染方式
    var displaySkuItem = function (displayArray) {
        $.each(displayArray, function (i, item) {
            $("li[typename=" + item + "]").attr({class: "disabled"});
        });
        $.each($('ul.js-sukList>li'), function (i, item) {
            if ($(item).hasClass("disabled")) {
                disNo += 1;
            }
        });

        if (disNo == trusteeTypesSize) {
            $(".js-canPayDiv").hide();
            $(".js-cannotPayDiv").show();
        }
    };

    $($("ul.js-childList>li")[0]).click();

    logger.log({
        module: 'trustee_sign_up' + shopid,
        op: 'trustee_sign_up_page_load'
    });
});