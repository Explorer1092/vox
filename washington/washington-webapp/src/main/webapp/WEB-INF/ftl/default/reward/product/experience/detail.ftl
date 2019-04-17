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
        <#if tagType?has_content>
            <#--<#if tagType =='ambassador'>-->
                <#--<a href="/reward/product/ambassador/index.vpage">大使专区</a><span class="active">></span>-->
            <#--<#elseif tagType == 'exclusive'>-->
                <#--<a href="/reward/product/exclusive/index.vpage">一起专属</a><span class="active">></span>-->
            <#--<#elseif tagType == 'boutique'>-->
                <#--<a href="/reward/product/boutique/index.vpage">限量精品</a><span class="active">></span>-->
            <#--<#else>-->
                <#--<a href="/reward/product/categories/index.vpage">全部奖品</a><span class="active">></span>-->
            <#--</#if>-->
        <#--<#else>-->
            <#--<a href="/reward/product/categories/index.vpage">全部奖品</a><span class="active">></span>-->
            <a href="/reward/product/exclusive/index.vpage">商品列表</a><span class="active">></span>${detail.productName!''}
        </#if>

    </p>
    <div class="home_sales_box clearfix">
        <div class="home_sales_left">
            <div class="large_pic_box">
            <#--<div class="J_sprites home_sales_hot"></div>-->
                <div class="inner_pic_box">
                    <#if userType == 'TEACHER' && detail.tags?has_content && detail.tags != '公益'>
                        <div class="coupon-tag-gaoj">${detail.tags!''}</div>
                    <#elseif userType == 'STUDENT' && detail.tags?has_content && detail.tags == '公益'>
                        <div class="coupon-tag-gaoj">公益</div>
                    </#if>
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
        </div>
        <div class="home_sales_right">
            <p class="home_sales_name" style="height:auto;line-height: 1.5em;">${detail.productName!''}</p>

            <div class="original_price_box clearfix">
                <span class="original_price">原价</span>
                <span class="original_price_number J_light_gray" style="text-decoration: line-through;">${detail.originPrice!''}</span>
                <span class="J_light_gray price_unit"><i class="w-gold-icon w-gold-icon-8"></i></span>
            </div>
            <#--<#if userType == 'STUDENT'>
                <div class="vip_price_box clearfix" style="margin-bottom: -1px;">
                    <span class="vip_price" style=" background-color: #fff;  border: 1px solid #ff9732; color:#ff9732;">VIP专享价</span>
                    <span class="vip_price_number w-orange" style="font-size: 18px;">${detail.vipPrice!''}</span>
                    <span class="J_red price_unit"><i class="w-gold-icon w-gold-icon-8"></i></span>
                <span class="J_red price_unit">
                    <a href="/apps/afenti/order/exam-cart.vpage?vip=0" target="_blank" class="J_light_gray clearfix" id="priceInfo">
                        <span class="float_left">/开通阿分题即可享受</span>
                    </a>
                </span>
                </div>
            </#if>-->
            <#if (userType == 'TEACHER' && detail.ambassadorLevel gt 0)!false>
                <div class="vip_price_box clearfix" style="margin-bottom: -1px;">
                    <span class="vip_price">兑换等级</span>
                <span class="vip_price_number w-orange" style="font-size: 18px;">
                    <#if (detail.ambassadorLevel gt 0)!false>
                    ${(detail.ambassadorLevelName)!'实习大使'}
                    <#else>
                        <#--LV ${(detail.teacherLevel)!0}-->
                    </#if>
                </span>
                    <div class="float_left J_light_gray vip_advice">
                        <a href="http://help.17zuoye.com/?p=963" target="_blank" class="J_light_gray clearfix" id="priceInfo">
                            <span class="float_left">兑换等级说明<i class=" w-arrow w-miarrow"></i></span>
                        </a>
                    </div>
                </div>
            </#if>
            <div class="vip_price_box clearfix">
                <#if userType == 'STUDENT'>
                    <span class="vip_price" style=" background-color: #fff;  border: 1px solid #ff9732; color:#ff9732;">兑换价格</span>
                    <span class="vip_price_number w-orange JS-discountPrice"></span>
                <#else>
                <#--当老师是校园大使时显示VIP价格 其他老师显示显示正常价格 -->
                    <span class="vip_price">兑换价格</span>
                    <span class="vip_price_number w-orange JS-discountPrice"></span>
                </#if>
                <span class="J_red price_unit"><i class="w-gold-icon w-gold-icon-8"></i></span>
            </div>

            <#if userType == 'TEACHER' && !(currentTeacherDetail.isJuniorTeacher())!false>
                <div class="discount-box">
                    <#if teacherNewLevel?? && teacherNewLevel gt 2>
                        <#if teacherCouponList?? && teacherCouponList?size gt 0>
                            <p class="text-box">
                                <span class="txt">使用<i>${teacherCouponList[0].discount * 10}折</i>教师等级特权优惠券</span>
                                <span class="txt-tip JS-tipshow"></span>
                                <span class="txt overage">(剩余<i class="JS-couponNumber" data-coupon="${teacherCouponList[0].discount}" data-refId="${teacherCouponList[0].couponUserRefId}">${teacherCouponList?size}</i>次)</span>
                            </p>
                            <p class="prompt">优惠券一旦使用，取消后不予返还</p>
                            <!-- selected 选中 -->
                            <div class="checkBox selected JS-selectCoupon"></div>
                        <#else>
                            <p class="text-box">
                                <span class="txt">使用<i><#if teacherNewLevel == 3>9<#elseif teacherNewLevel == 4>8.5<#else>8</#if>折</i>教师等级特权优惠券</span>
                                <span class="txt-tip JS-tipshow"></span>
                                <span class="txt overage">(剩余<i class="JS-couponNumber">0</i>次)</span>
                            </p>
                            <p class="prompt">优惠券一旦使用，取消后不予返还</p>
                        </#if>

                    <#else>
                        <p class="text-box"><span class="txt">中级及以上教师可获得优惠折扣</span></p>
                        <p class="prompt">快去升级等级获得更多优惠券吧</p>
                    </#if>
                </div>
            </#if>
            <#--<#if showInventory!>-->
            <#--<dl class="clearfix">-->
                <#--<dt>库存数量：</dt>-->
                <#--<dd>-->
                    <#--<span class="J_gray">-->
                     <#--<#if inventory ?? && inventory gt 10000>-->
                        <#--10000+-->
                        <#--<#else>-->
                        <#--${inventory}-->
                    <#--</#if>-->
                    <#--</span>-->
                <#--</dd>-->
            <#--</dl>-->
            <#--</#if>-->
            <#--<dl class="clearfix">-->
                <#--<dt>兑换数量：</dt>-->
                <#--<dd>-->
                    <#--<span class="J_gray">1</span>-->
                <#--</dd>-->
            <#--</dl>-->
            <div class="clearfix">
                <a id="p_exchange_but" href="javascript:void(0);" class="w-but exchange_btn">我要兑换</a>
                <span class="point">兑换成功后，<@ftlmacro.garyBeansText/>不再退还！</span>
            </div>
        </div>
    </div>
    <div class="home_sales_detail">
        <p class="title deep_gray">奖品详情</p>

        <div class="home_sales_detail_content">
            <#if (detail.description)?has_content>
            ${detail.description}
            <#else>
                暂无奖品介绍
            </#if>
        </div>
    </div>
    <script id="t:bandingMobile" type="text/html">
        <div class="exchange_alert">
            <p class="J_deep_red btn_box font_twenty clearfix"
               style="border-bottom:1px solid #f0f0f0; padding:10px 0 20px 70px; margin-bottom:26px;"><i
                    class="J_sprites"></i><span>你还没有绑定手机，请先绑定！</span></p>
            <ul id="banding_box">
                <li class="clearfix" style="margin-bottom:20px;">
                    <input id="mobile_box" placeholder="填写手机号码"/>
                    <a href="javascript:void (0);" id="send_validate_code" class="img sendCode border_radius"><span>获取验证码</span></a>
                    <strong id="mobileTip">提示错误</strong>
                </li>
                <li class="clearfix" style="margin-bottom:10px;">
                    <input id="captcha_box" placeholder="手机验证码"/>
                    <strong id="captchaTip">提示错误</strong>
                </li>
            </ul>
        </div>

    </script>

    <script id="t:hasMobile" type="text/html">
        <div class="exchange_alert">
            <p class="J_deep_red btn_box font_twenty" style="padding:10px 0 5px 0;">兑换后券号将发送到你的手机</p>
            <p style="font-size:12px;text-align: center;border-bottom:1px solid #f0f0f0;margin-bottom:26px;padding-bottom:10px;">
                券码发送有延迟，为避免多次兑换，请勿反复提交验证码
            </p>
            <dl class="popup-form-table">
                <dt>手机号</dt>
                <dd>
                    <select id="mobile_select">
                        <#list mobileList as ml>
                            <option value="${ml.mobile}">${ml.mobile}</option>
                        </#list>
                    </select>
                    <a id="send_validate_code" href="javascript:void(0);" class="w-btn"><span>获取验证码</span></a>
                </dd>
                <dt>验证码</dt>
                <dd>
                    <input id="captcha_box" type="text" value=""/>
                </dd>
                <dt></dt>
                <dd id="captchaTip"></dd>
                <dd>
                    <span class="J_gray">号码错误？</span>
                    <#if temp.currentUserType == 'STUDENT'>
                        <a href="${(ProductConfig.getUcenterUrl())!''}/student/center/account.vpage?updateType=mobile" target="_blank">点击修改</a>
                    <#elseif temp.currentUserType == 'TEACHER'>
                        <a href="${(ProductConfig.getUcenterUrl())!''}/teacher/center/index.vpage#/teacher/center/securitycenter.vpage" target="_blank">点击修改</a>
                    <#elseif temp.currentUserType == 'RSTAFF'>
                        <a href="/rstaff/center/edit.vpage" target="_blank">点击修改</a>
                    </#if>
                </dd>
            </dl>
        </div>
        <a id="submit_but" class="J_btn border_radius font_eighteen" href="javascript:void (0)"
           style="padding:8px 50px;">确定</a>
    </script>

    </#if>

<script type="text/javascript">

    $(function () {
        var roleTypes;
        var productPrice = $(".JS-discountPrice");
        var discountPrice = ${detail.discountPrice!''};
        productPrice.text(discountPrice);

        <#if userType == 'STUDENT'>
            roleTypes = "web_student_logs";
        <#elseif userType == 'TEACHER'>
            <#if teacherNewLevel?? && teacherNewLevel gt 2>
                <#if teacherCouponList?? && teacherCouponList?size gt 0>
                    var teaBox = $(".JS-couponNumber");
                    var teaCouponLen = teaBox.text() - 1;
                    var teaCoupon = teaBox.attr("data-coupon");
                    teaBox.text(teaCouponLen);
                    roleTypes = "web_teacher_logs";
                    module_log = 'm_2ekTvaNe';

                    productPrice.text(discountPrice > 0 ? Math.ceil(discountPrice * teaCoupon) : 0);

                    $(".JS-selectCoupon").on('click', function () {
                        if ($(this).hasClass("selected")) {
                            productPrice.text(discountPrice);
                            $(this).removeClass("selected");
                            teaCouponLen = Number(teaCouponLen) + 1;
                            teaBox.text(teaCouponLen);
                            teaCoupon = '';
                        } else {
                            teaCoupon = teaBox.attr("data-coupon");
                            productPrice.text(Math.ceil(discountPrice * teaCoupon));
                            teaCouponLen = Number(teaCouponLen) - 1;
                            teaBox.text(teaCouponLen);
                            $(this).addClass("selected");
                        }
                    });

                    $(".JS-tipshow").on('click', function () {
                        $.prompt('<p style="line-height: 23px; font-size: 16px;">（等级有效期内）<br>高级教师可享受8.5折优惠兑换5次；<br>特级教师可享受8折优惠兑换10次。<br>（优惠券一旦使用，不予返还）</p>', {
                            title: '教师等级特权奖励规则说明',
                            buttons: {"知道了": true},
                            submit: function () {
                                $.prompt.close();
                            }
                        });
                    });
                </#if>
            </#if>
        </#if>

        //选择款式
        $("#p_style_list_box a").on('click', function () {
            $(this).addClass('active').siblings().removeClass('active');
        });

        $("#priceInfo").hover(function () {
            $(this).find(".price_point_box").show();
        }, function () {
            $(this).find(".price_point_box").hide();
        });

        $("[data-go-url]").on("click", function(){
            var $this = $(this);
            setTimeout(function(){
                window.open($this.data("go-url"), "_blank");
            }, 200);
        });

        //我要兑换
        $("#p_exchange_but").on('click', function () {
            var $this = $(this);
            <#if ProductDevelopment.isStagingEnv()>
                $17.alert("教学用品中心尚未开放，敬请期待");
                return false;
            </#if>
            <#if fakeTeacher!false>
                $.prompt("您的账号使用存在异常，该功能受限 如有疑议，请进行申诉 【知道了/去申诉】", {
                    buttons: {'知道了': false,'去申诉':true},
                    focus: 1,
                    submit: function (e, v) {
                        e.preventDefault();
                        if (v) {
                            var url = '${(ProductConfig.getMainSiteBaseUrl())!''}/ucenter/teacherfeedback.vpage',type = 'FAKE';
                            window.open (type ? (url + "?" + $.param({type : type})) : url, 'feedbackwindow', 'height=500, width=700,top=200,left=450');
                        }else{
                            $.prompt.close();
                        }
                    }
                });
                return false;
            </#if>
            var p_type_id = $("#p_style_list_box a.active").data('skus_id');
            var integralCount = $(".vip_price_number").text() * 1;
    <#if detail.oneLevelCategoryType?has_content>
        <#if detail.oneLevelCategoryType == 6>
            var tipPrice = productPrice.text();

            $.prompt("确认花" + tipPrice + "<@ftlmacro.garyBeansText/>兑换这个奖品吗？<br/>由于本奖品兑换成功后不能退换，敬请理解！", {
                buttons: {'取消': false ,'兑换': true },
                focus: 1,
                submit: function (e, v, m, f) {
                    e.preventDefault();
                    if (v) {
                        var refId = '';
                        if ($(".JS-selectCoupon").hasClass("selected")) {
                            refId = $(".JS-couponNumber").attr("data-refId");
                        }
                        // $.post("/reward/order/exchangedcoupon.vpage",{
                        $.post("/reward/order/createorder.vpage",{
                            <#--productName : "${(detail.productName)!''}",-->
                            // ignoreMobile: true,
                            productId: ${detail.id!''},
                            quantity: 1,
                            couponUserRefId: refId
                        }).done(function (data) {
                                if (data.success) {
                                    YQ.voxLogs({database:roleTypes, module : "m_2ekTvaNe", op : "button_confirm_exchange_click", s0: "${(detail.id)!0}",s1: "${(detail.productType)!}"});
                                    $.prompt("兑换成功，您可以去“虚拟兑换”中查看！", {
                                        buttons: {'知道了': false,'去查看': true  },
                                        focus: 1,
                                        submit: function (e, v, m, f) {
                                            e.preventDefault();
                                            if (v) {
                                                location.href = '/reward/order/myexperience.vpage';

                                            } else {
                                                $.prompt.close();
                                            }
                                        }
                                    });

                                }else{
                                    $17.alert(data.info);
                                }
                            })
                            .fail(function () {
                                $17.alert('数据提交失败！');
                            });
                    } else {
                        $.prompt.close();
                    }
                }
            });
       <#elseif detail.oneLevelCategoryType == 5>

            //根据手机绑定情况 显示不同页面
            <#if mobileList?size == 0>
                <#if temp.currentUserType == 'RSTAFF'>
                    $.prompt("", {
                        buttons: {'去填写': true},
                        focus: 1,
                        submit: function (e, v) {
                            e.preventDefault();
                            if (v) {
                                location.href = '/rstaff/center/edit.vpage';
                            }
                        }
                    });
                <#else>
                    $.prompt(template("t:bandingMobile", {}), {
                        title: '',
                        buttons: {"绑定手机并兑换": true},
                        submit: function (e, v) {
                            e.preventDefault();
                            if (v) {
                                var mobileBox = $("#mobile_box");
                                var captchaBox = $("#captcha_box");
                                var captchaTip = $("#captchaTip");

                                if (!$17.isBlank(mobileBox.val())) {
                                    if (!$17.isMobile(mobileBox.val())) {
                                        $("#mobileTip").show().html('输入正确的手机号');
                                        return false;
                                    } else {
                                        $("#mobileTip").hide().html('');
                                    }
                                    if ($17.isBlank(captchaBox.val())) {
                                        captchaTip.show().html('输入短信验证码');
                                        captchaBox.focus();
                                        return false;
                                    } else {
                                        captchaTip.hide().html('');
                                        captchaBox.siblings('span.init').text("");
                                    }
                                } else {
                                    $("#mobileTip").show().html('请输入正确的手机号');
                                    mobileBox.focus();
                                    return false;
                                }
                                var refId = '';
                                if ($(".JS-selectCoupon").hasClass("selected")) {
                                    refId = $(".JS-couponNumber").attr("data-refId");
                                }
                                $.post("/reward/order/exchangedcoupon.vpage",
                                        {
                                            smsCode: captchaBox.val(),
                                            mobile: mobileBox.val(),
                                            productName: "${(detail.productName)!''}",
                                            productId: ${detail.id!''},
                                            couponUserRefId: refId
                                        })
                                        .done(function (data) {
                                            if (data.success) {
                                                YQ.voxLogs({database:roleTypes, module : "m_2ekTvaNe", op : "button_confirm_exchange_click", s0: "${(detail.id)!0}",s1: "${(detail.productType)!}"});
                                                $.prompt("兑换成功，您可以去“虚拟兑换”中查看！", {
                                                    buttons: {'知道了': false,'去查看': true  },
                                                    focus: 1,
                                                    submit: function (e, v, m, f) {
                                                        e.preventDefault();
                                                        if (v) {
                                                            location.href = '/reward/order/myexperience.vpage';

                                                        } else {
                                                            $.prompt.close();
                                                        }
                                                    }
                                                });
                                            } else {
                                                captchaTip.show().html(data.info);
                                                captchaBox.focus();
                                            }
                                        })
                                        .fail(function () {
                                            $17.alert('数据提交失败！');
                                        });

                            } else {
                                $.prompt.close();
                            }
                        },
                        loaded: function () {
                            $("#send_validate_code").on('click', function () {
                                var $this = $(this);
                                var mobileBox = $("#mobile_box");
                                var mobileTip = $("#mobileTip");
                                if (!$17.isMobile(mobileBox.val())) {
                                    mobileTip.show().html('输入正确的手机号');
                                    mobileBox.focus();
                                    return false;
                                } else {
                                    mobileTip.hide().html("");
                                }

                                if ($this.hasClass("btn_disable"))return false;
                                $this.addClass("btn_disable");
                                $.post("/reward/order/sendmobilecodecoupon.vpage", {mobile: mobileBox.val()}, function (data) {
                                    if (data.success) {
                                        mobileTip.show().html('验证码已发送');
                                    } else {
                                        mobileTip.show().html(data.info);
                                        $this.removeClass("btn_disable");
                                    }
                                    $17.getSMSVerifyCode($this, data);
                                });
                            });

                            $("body").on("keydown", "#banding_box input", function () {
                                $("#mobileTip, #captchaTip").html('').hide();
                            });
                        }
                    });
                </#if>
            <#else>
                <#if temp.currentUserType == 'STUDENT'>
                    <#if detail.saleGroup == 'VIP'>
                        $17.alert('这个奖品是VIP专享的，你还不能兑换。');
                        return false;
                    </#if>
                <#elseif temp.currentUserType == 'TEACHER'>
                    <#if currentUser.fetchCertificationState() != "SUCCESS">
                        $17.alert('你还没有认证，不能兑换哦！');
                        return false;
                    </#if>
                    <#if !currentTeacherDetail.schoolAmbassador && detail.saleGroup == 'VIP'>
                        $17.alert('这个奖品是VIP专享的，你还不能兑换。');
                        return false;
                    </#if>
                </#if>

                $.prompt(template("t:hasMobile", {}), {
                    title: '',
                    buttons: {},
                    position : { width : 600},
                    focus: 1,
                    loaded: function () {
                        $("#submit_but").on('click', function () {
                            var mobile = $("#mobile_select option:selected").val();
                            var captchaBox = $("#captcha_box");
                            var captchaTip = $("#captchaTip");
                            if ($17.isBlank(captchaBox.val())) {
                                captchaTip.html('输入短信验证码');
                                captchaBox.focus();
                                return false;
                            } else {
                                captchaTip.html('');
                            }
                            var refId = '';
                            if ($(".JS-selectCoupon").hasClass("selected")) {
                                refId = $(".JS-couponNumber").attr("data-refId");
                            }
                            $.post("/reward/order/exchangedcoupon.vpage",
                                    {
                                        mobile: mobile,
                                        smsCode: captchaBox.val(),
                                        productName: "${(detail.productName)!''}",
                                        productId: "${detail.id!''}",
                                        couponUserRefId: refId
                                    })
                                    .done(function (data) {
                                        if (data.success) {
                                            YQ.voxLogs({database:roleTypes, module : "m_2ekTvaNe", op : "button_confirm_exchange_click", s0: "${(detail.id)!0}",s1: "${(detail.productType)!}"});
                                            $.prompt("恭喜，兑换成功！", {
                                                buttons: {'知道了': false,'去查看': true  },
                                                focus: 1,
                                                submit: function (e, v, m, f) {
                                                    e.preventDefault();
                                                    if (v) {
                                                        location.href = '/reward/order/myexperience.vpage';

                                                    } else {
                                                        $.prompt.close();
                                                        window.location.reload();
                                                    }
                                                }
                                            });
                                        } else {
                                            var infoBtn = {"知道了": true};
                                            var infoUrl = function () {
                                                $.prompt.close();
                                            };
                                            if (!$17.isBlank(data.authentication)) {
                                                infoBtn = {"去认证": true};
                                                infoUrl = function () {
                                                    window.open('${(ProductConfig.getUcenterUrl())!}/teacher/center/index.vpage#/teacher/center/myauthenticate.vpage', "_blank");
                                                };
                                            }
                                            $.prompt(data.info, {
                                                title: "",
                                                buttons: infoBtn,
                                                submit: infoUrl
                                            });
                                        }

                                    })
                                    .fail(function (data) {
                                        $.prompt('数据提交失败！', {
                                            title: "",
                                            buttons: {"知道了": true},
                                            submit: function () {
                                                $.prompt.close();
                                            }
                                        });
                                    });
                        });

                        $("#send_validate_code").on('click', function () {
                            var $this = $(this);
                            var mobileSel = $("#mobile_select");
                            var captchaTip = $("#captchaTip");

                            if ($this.hasClass("btn_disable"))return false;
                            $this.addClass("btn_disable");
                            $.post("/reward/order/sendmobilecodemessage.vpage", {mobile: mobileSel.val()}, function (data) {
                                if (!data.success) {
                                    captchaTip.show().html(data.info);
                                    $this.removeClass("btn_disable");
                                }
                                $17.getSMSVerifyCode($this, data);
                            });
                        });

                        $("body").on("keydown", "#banding_box input", function () {
                            $("#captchaTip").html('').hide();
                        });
                    }
                });
            </#if>
        <#else>
            $17.alert("该商品暂不支持PC端兑换，请去APP端兑换");
        </#if>
    <#else>
        $17.alert("奖品异常操作");
    </#if>
            YQ.voxLogs({ database:roleTypes,module : "m_2ekTvaNe", op : "o_vJCuwRSv", s0: "${(detail.id)!0}", s1: "${(currentUser.userType)!0}"});
        });
    });
</script>
</@temp.page>