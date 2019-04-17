<#-- @ftlvariable name="detail" type="com.voxlearning.utopia.service.reward.mapper.RewardProductDetail" -->
<#-- @ftlvariable name="detail" type="com.voxlearning.utopia.service.reward.mapper.RewardProductDetail" -->
<#if (currentUser.userType) == 3>
    <#assign userType="STUDENT"/>
<#elseif (currentUser.userType) == 1>
    <#assign userType="TEACHER"/>
<#elseif (currentUser.userType) == 8>
    <#assign userType="RSTAFF"/>
</#if>
<#import "../../layout/layout.ftl" as temp />
<@temp.page index='' columnType="empty">
    <#if detail?has_content>
    <p class="bread_crumb_nav clearfix">
    <a href="/reward/product/present/index.vpage">爱心捐赠</a><span class="active">></span>
    ${detail.productName!''}
    </p>
    <div class="home_sales_box clearfix">
        <div class="home_sales_left">
            <div class="large_pic_box">
                <div class="inner_pic_box">
                    <ul id="p_big_imgs_box" class="clearfix">
                        <#list detail.images as img>
                            <li data-img_index="${img_index}"><i></i>
                                <#if img.location?? && img.location?index_of("oss-image.17zuoye.com")!=-1>
                                    <img src="${img.location!''}" style="max-width: 85%;"/>
                                <#else>
                                    <img src="<@app.avatar href="${img.location!''}"/>" style="max-width: 85%;"/>
                                </#if>
                            </li>
                        </#list>
                    </ul>
                </div>
            </div>
            <div class="small_pic_box">
                <a class="prev_btn prevBtn"><</a>
                <a class="next_btn nextBtn">></a>

                <div class="pic_inner_box">
                    <ul id="p_small_imgs_box" class="clearfix">
                        <#list detail.images as img>
                            <li <#if img_index == 0>class="active"</#if>
                                <#if img_index gt 4>style="display: none;" </#if> data-img_index="${img_index}">
                                <#if img.location?? && img.location?index_of("oss-image.17zuoye.com")!=-1>
                                    <img src="${img.location!''}"/>
                                <#else>
                                    <img src="<@app.avatar href="${img.location!''}"/>"/>
                                </#if>
                            </li>
                        </#list>
                    </ul>
                </div>
            </div>
        </div>
        <div class="home_sales_right">
            <p class="home_sales_name" style="height: auto; line-height: 1.5em;">${detail.productName!''}</p>

            <div class="vip_price_box clearfix">
                <span class="vip_price"
                      style=" background-color: #fff;  border: 1px solid #ff9732; color:#ff9732;">捐赠</span>
                <span class="vip_price_number w-orange">${detail.discountPrice!''}</span>
                <span class="J_red price_unit"><i class="w-gold-icon w-gold-icon-8"></i></span>
            </div>
            <dl class="clearfix" style="display: none;">
                <dt>选择款式：</dt>
                <dd id="p_style_list_box">
                    <#list detail.skus as s>
                        <#if s.inventorySellable gt 0>
                            <a class="allowed" href="javascript:void(0);"
                               data-skus_id="${s.id!''}" data-skus_sellable="${s.inventorySellable!0}"
                               id="skusId">${s.skuName!''}</a>
                        <#else>
                            <a href="javascript:void(0);"
                               style="border:1px dashed #ccc;color:#ccc;pointer-events:none;">${s.skuName!''}</a>
                        </#if>
                    </#list>
                </dd>
            </dl>
            <dl class="clearfix">
                <dt>兑换数量：</dt>
                <dd>
                    <strong class="minusBtn disabled">-</strong>
                    <input type="text" class="J_gray tempNum" name="productNumber" value="${(detail.minBuyNums)!'1'}" maxlength="3"/>
                    <strong class="plusBtn">+</strong>
                </dd>
            </dl>
            <#assign needHide = false />
            <#list detail.skus as s>
                <#if s_index == 0 && s.inventorySellable == 0>
                    <#assign needHide = true />
                </#if>
            </#list>
            <div class="clearfix">
                <div class="J_btn exchange_btn btn_disable isZero" <#if !needHide>style="display: none;"</#if>>抢光了</div>
                <a id="p_exchange_but" href="javascript:void(0);" class="w-but exchange_btn isNotZero"
                   <#if needHide>style="display: none;"</#if>>我要捐赠</a>
                <p class="J_gray">
                    已有<strong class="w-orange">${detail.soldQuantity!''}</strong>人捐赠。
                </p>
            </div>
        </div>
    </div>
    <div class="home_sales_detail" style="margin-top:50px;">
        <p class="title deep_gray">奖品详情</p>

        <div class="home_sales_detail_content">
            <#if (detail.description)?has_content>
            ${detail.description}
            <#else>
                暂无奖品介绍
            </#if>
        </div>
    </div>
    </#if>

    <#include "../drawlottery.ftl"/>
<script type="text/javascript">
    $(function () {
        var detail = new $17.Model({
            maxIndex: $("#p_small_imgs_box li:last").data('img_index') * 1,
            showImgNum: 5,
            productNumber: $(".tempNum"),
            maxExchangeNum: 100, //单次兑换数量最大值
            minExchangeNum: 1 //单次兑换数量最小值
        });

        detail.extend({
            tagsAddClass: function (index, tagName) {
                if (detail.maxIndex == 0) {
                    return false
                }
                var visible = $("#p_small_imgs_box li[data-img_index=" + index + "]").is(':visible');
                if (tagName == 'nextBut') {
                    if (!visible) {
                        detail.imgHide(index * 1 - detail.showImgNum);
                    }
                    if (index == 0) {
                        $("#p_small_imgs_box li:lt(" + (index * 1 + detail.showImgNum) + ")").show();
                        $("#p_small_imgs_box li:gt(" + (index * 1 + detail.showImgNum - 1) + ")").hide();
                    }
                } else {
                    if (!visible && index == detail.maxIndex) {
                        $("#p_small_imgs_box li:gt(" + (index * 1 - detail.showImgNum) + ")").show();
                        $("#p_small_imgs_box li:lt(" + (index * 1 - detail.showImgNum) + ")").hide();
                    } else if (index == detail.maxIndex) {
                        detail.imgHide(index * 1 - detail.showImgNum);
                    } else if (!visible) {
                        detail.imgHide(index * 1 + detail.showImgNum);
                    }
                }
                $("#p_small_imgs_box li[data-img_index=" + index + "]").show().addClass('active').siblings().removeClass('active');
                detail.showProductImgsByIndex(index);
            },
            showProductImgsByIndex: function (index) {
                $("#p_big_imgs_box li[data-img_index=" + index + "]").show().siblings().hide();
            },
            imgHide: function (index) {
                $("#p_small_imgs_box li[data-img_index=" + index + "]").hide();
            },
            checkNum: function () {
                var maxNum = detail.maxExchangeNum; //单次兑换数量最大量
                var changeNum = detail.minExchangeNum; //加减奖品数差值

                if (arguments[0] > maxNum || !$17.isNumber(arguments[0])) {
                    return 10;
                }


                if (arguments[1] == "minus") {
                    if (arguments[0] - changeNum < 0 || arguments[0] - changeNum > maxNum) {
                        return 1;
                    } else if (arguments[0] - changeNum == 0) {
                        return 1;
                    } else {
                        return arguments[0] - changeNum;
                    }
                } else {
                    if (arguments[0] + changeNum < 0 || arguments[0] + changeNum > maxNum) {
                        return arguments[0];
                    } else {
                        return arguments[0] + changeNum;
                    }
                }
            },
            init: function () {
                var $this = this;

                //选择款式
                $("#p_style_list_box a").on('click', function () {
                    $(this).addClass('active').siblings().removeClass('active');

                    var skus_sellable = $(this).attr("data-skus_sellable");
                    if (skus_sellable == 0) {
                        $(".isZero").show();
                        $(".isNotZero").hide();
                    } else {
                        $(".isZero").hide();
                        $(".isNotZero").show();
                    }
                });

                //兑换数量
                $(".minusBtn").on('click', function () {
                    var tempNumVal = $this.productNumber.val() * 1;
                    $this.productNumber.val($this.checkNum(tempNumVal, 'minus'));
                    //按钮样式
                    $(this).siblings().closest('strong.plusBtn').removeClass("disabled");
                    if (tempNumVal == detail.minExchangeNum + 1) {
                        $(this).addClass("disabled");
                    }
                    YQ.voxLogs({
                        module: "m_2ekTvaNe",
                        op: "o_3Gdnbe7z",
                        s0: "${(detail.id)!0}",
                        s1: "${(currentUser.userType)!0}"
                    });
                });

                $(".plusBtn").on('click', function () {
                    var tempNumVal = $this.productNumber.val() * 1;
                    $this.productNumber.val($this.checkNum(tempNumVal));
                    //按钮样式
                    $(this).siblings().closest('strong.minusBtn').removeClass("disabled");
                    if (tempNumVal == detail.maxExchangeNum - 1) {
                        $(this).addClass("disabled");
                    }
                    YQ.voxLogs({
                        module: "m_2ekTvaNe",
                        op: "o_3Gdnbe7z",
                        s0: "${(detail.id)!0}",
                        s1: "${(currentUser.userType)!0}"
                    });
                });

                var maxNumberCount = 1;
                $this.productNumber.on("keyup", function () {
                    var $that = $(this);
                    if (!$17.isNumber($that.val())) {
                        $that.val(maxNumberCount);
                        return false;
                    }

                    if ($that.val() > detail.maxExchangeNum) {
                        $that.val(maxNumberCount);
                        return false;
                    }

                    maxNumberCount = $that.val();

                    YQ.voxLogs({
                        module: "m_2ekTvaNe",
                        op: "o_3Gdnbe7z",
                        s0: "${(detail.id)!0}",
                        s1: "${(currentUser.userType)!0}"
                    });
                });

                //图片查看
                $("#p_small_imgs_box li").on('mouseenter', function () {
                    var imgIndex = $(this).data('img_index');
                    detail.tagsAddClass(imgIndex);
                });

                $("a.prevBtn").on('click', function () {
                    var index = $("#p_small_imgs_box li.active").data('img_index') * 1;
                    index = (index == 0) ? $this.maxIndex + 1 : index;
                    detail.tagsAddClass(--index, 'prevBut');
                });

                $("a.nextBtn").on('click', function () {
                    var index = $("#p_small_imgs_box li.active").data('img_index') * 1;
                    index = (index == $this.maxIndex) ? -1 : index
                    detail.tagsAddClass(++index, 'nextBut');
                });

                //默认选中第一个库存不为0的款式
                $("#p_style_list_box").children(".allowed").first().click();

                //我要兑换
                $("#p_exchange_but").on('click', function () {
                    var p_type_id = $("#p_style_list_box a.active").data('skus_id');
                    var p_num = detail.productNumber.val() * 1;
                    var tipStr = '确认花' + (parseInt("${detail.discountPrice!0}") * p_num) + '学豆捐贈这个奖品吗？';
                    var $promptButtons = {"取消": false, "捐赠": true};

                    if (true) {
                        var states = {
                            state0: {
                                html: tipStr,
                                title: '',
                                buttons: $promptButtons,
                                position: {width: 550},
                                focus: 1,
                                submit: function (e, v) {
                                    e.preventDefault();
                                    if (v) {
                                        $.post('/reward/order/createpresentorder.vpage', {
                                            productId: '${detail.id!''}',
                                            skuId: p_type_id,
                                            quantity: p_num
                                        }, function (data) {
                                            if (data.success) {
                                                updateMyRewardCount('plus');
                                                $.prompt.goToState('state1');
                                            } else {
                                                var infoBtn = {"知道了": true};
                                                var infoUrl = function () {
                                                    $.prompt.close();
                                                };
                                                if (!$17.isBlank(data.bindMobile)) {
                                                    infoBtn = {"去绑定": true};
                                                    infoUrl = function () {
                                                        <#if userType == 'STUDENT'>
                                                            window.open('${(ProductConfig.getUcenterUrl())!}/student/center/account.vpage?updateType=mobile', "_blank");
                                                        <#elseif userType == 'TEACHER'>
                                                            window.open('${(ProductConfig.getUcenterUrl())!}/teacher/center/index.vpage#/teacher/center/securitycenter.vpage', "_blank");
                                                        <#elseif userType == 'RSTAFF'>
                                                            window.open('/rstaff/center/edit.vpage', "_blank");
                                                        </#if>
                                                    };
                                                }

                                                if (!$17.isBlank(data.authentication)) {
                                                    infoBtn = {"去认证": true};
                                                    infoUrl = function () {
                                                        window.open('${(ProductConfig.getUcenterUrl())!}/teacher/center/index.vpage#/teacher/center/myauthenticate.vpage', "_blank");
                                                    };
                                                }

                                                if (!$17.isBlank(data.address)) {
                                                    infoBtn = {"去填写": true};
                                                    infoUrl = function () {
                                                        <#if userType == 'TEACHER'>
                                                            window.open("${(ProductConfig.getUcenterUrl())!''}/teacher/center/index.vpage#/teacher/center/myprofile.vpage", "_blank");
                                                        <#elseif userType == 'RSTAFF'>
                                                            window.open('/rstaff/center/edit.vpage', "_blank");
                                                        </#if>
                                                    };
                                                }
                                                $.prompt(data.info, {
                                                    title: "",
                                                    buttons: infoBtn,
                                                    submit: infoUrl
                                                });
                                            }
                                        });
                                    } else {
                                        $.prompt.close();
                                    }
                                }
                            },
                            state1: {
                                title: '',
                                html: '感谢您对捐赠贫困学校活动的支持！',
                                buttons: {'去查看': true, '知道了': false},
                                focus: 1,
                                submit: function (e, v, m, f) {
                                    e.preventDefault();
                                    if (v) {
                                        location.href = '/reward/order/history.vpage';
                                    } else {
                                        $.prompt.close();
                                    }
                                }
                            }
                        };
                        $.prompt(states);


                        if (p_num == 0) {
                            $.prompt('请选择您要兑换奖品的数量', {
                                title: "",
                                buttons: {"知道了": true},
                                submit: function (e, v) {
                                    location.reload();
                                }
                            });
                            return false;
                        }

                        if (p_num > detail.maxExchangeNum) {
                            $.prompt('您输入的奖品兑换数量不正确，请重新输入', {
                                title: "",
                                buttons: {"知道了": true},
                                submit: function (e, v) {
                                    location.reload();
                                }
                            });
                            return false;
                        }
                    }

                    YQ.voxLogs({
                        module: "m_2ekTvaNe",
                        op: "o_vJCuwRSv",
                        s0: "${(detail.id)!0}",
                        s1: "${(currentUser.userType)!0}"
                    });
                });
            }
        }).init();
    });
</script>
</@temp.page>