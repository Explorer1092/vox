define(['versionCompare', 'jqPopup'], function (versionCompare) {
    $(function () {
        //报名页选择孩子列表
        $(document).on("click", ".child", function () {
            var $this = $(this);

            $this.addClass("success");
            $this.siblings().removeClass("success");

            var data = {
                activityId: $("#activityId").val(),
                studentId: $this.attr("data-id")
            };

            switch ($(this).attr("data-type")) {
                case "Reserve":
                    $.post("loadstudentreserve.vpage", data, function (res) {
                        if (res.success) {
                            successCallback(res.reserve);
                        }
                    });
                    break;
                case "Pay":
                    $.post("loadstudentorders.vpage", data, function (res) {
                        if (res.success) {
                            successCallback(res.pay);
                        }
                    });
                    break;
                default:
                    break;
            }
        });
        $(".childBox").children().first().click();

        //成功回调函数
        function successCallback(isTrue) {
            if (isTrue) {
                $(".done").show();
                $(".undone").hide();
            } else {
                $(".done").hide();
                $(".undone").show();
            }
        }

        var mobile = $("#mobile");    //手机号
        var code = $("#code");      //验证码

        //提交报名页发送验证码
        $(document).on("click", ".js-sendCode", function () {
            var $this = $(this), val = mobile.val();

            if (val) {
                $.post("sendrcode.vpage", {mobile: val}, function (res) {
                    if (res.success) {
                        $this.html("发送中...");
                        $this.addClass("disabled").removeClass("js-sendCode");

                        var clock = 60;
                        var timer = setInterval(function () {
                            $this.html(--clock + 's');
                            if (clock <= 0) {
                                clearInterval(timer);
                                $this.removeClass("disabled").addClass("js-sendCode").html("获取验证码");
                            }
                        }, 1000);
                    }
                    else {
                        $.alert(res.info);
                    }
                });
            } else {
                $.alert("手机号不能为空！");
            }
        });

        //提交报名页submit按钮
        $(document).on("click", ".js-submit[data-acType='reserve']", function () {
            //判空
            if (!mobile.val()) {
                $.alert("手机号不能为空！");
                return;
            }
            if (!code.val()) {
                $.alert("验证码不能为空");
                return;
            }

            var data = {
                mobile: mobile.val(),
                code: code.val(),
                studentId: $("li.success").eq(0).attr("data-id"),
                activityId: $(this).attr("data-id")
            };

            $.post("verifycode.vpage", data, function (res) {
                if (res.success) {
                    successCallback(res.success);
                    //报名成功打点
                    YQ.voxLogs({database: "parent", module: 'm_Gn4M35Q8', op: 'o_v4yYR2am', s0: "${activity.id}"});
                }
                else {
                    $.alert(res.info);
                }
            });
        });

        //支付确认页submit按钮
        $(document).on("click", ".js-submit[data-acType='pay']", function () {
            var $this = $(this);
            var data = {
                sid: $(".success").eq(0).attr("data-id"),
                activityId: $this.attr("data-id"),
                remark: $("#remark").val()
            };
            $.post('order.vpage', data, function (res) {
                if (res.success) {
                    if (versionCompare((PM.app_version || '0'), "1.5.5") > -1) {
                        return PM.doExternal("payOrder", JSON.stringify({
                            orderId: res.orderId,
                            orderType: "seattle",
                            payType: 1
                        }));
                    } else {
                        return PM.doExternal("payOrder", res.orderId, "seattle");
                    }
                } else {
                    $.alert(res.info);
                }
            });
        });

        //通用报名/支付/加群页免费咨询按钮
        $(document).on("click", ".js-joinGroup", function () {
            //判断App版本号是否满足（大于1.5.2）
            if (versionMeeted) {
                var $this = $(this);
                if ($this.data().qcode == '') {
                    $.alert("群号为空，加群失败！")
                }
                else {
                    var data = {
                        groupId: $this.data().qcode,
                        groupName: $this.data().qname,
                        type: 'GROUP_ADD'
                    };
                    PM.doExternal("chatGroupMethod", JSON.stringify(data));
                }
            } else {
                $.alert("加群失败，您当前所使用的App版本过低，请升级到1.5.2及以上版本后重新操作！")
            }
        });

        //获取App版本
        function getAppVersion() {
            var native_version = "";

            if (window["external"] && window.external["getInitParams"]) {
                var $params = window.external.getInitParams();

                if ($params) {
                    $params = $.parseJSON($params);
                    native_version = $params.native_version;
                }
            }

            return native_version;
        }

        //App版本>=1.5.2时才有加群功能,验证App版本是否高于1.5.2
        function versionValidate() {
            var native_version = getAppVersion(),
                version = native_version.split('.'),
                part1 = parseInt(version[0]),
                part2 = parseInt(version[1]),
                part3 = parseInt(version[2]);
            if (part1 > 1) {
                $(".globalTopbar").hide();
                return true;
            }
            else if (part1 >= 1 && part2 > 5) {
                $(".globalTopbar").hide();
                return true;
            }else if (part1 >= 1 && part2 >= 5 && part3 >= 2) {
                return true;
            }
            return false;
        }

        var versionMeeted = versionValidate();
    });
});