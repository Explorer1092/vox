<#-- @ftlvariable name="detail" type="com.voxlearning.utopia.service.reward.mapper.RewardProductDetail" -->
<#-- @ftlvariable name="detail" type="com.voxlearning.utopia.service.reward.mapper.RewardProductDetail" -->
<#if (currentUser.userType) == 3>
    <#assign userType="STUDENT"/>
<#elseif (currentUser.userType) == 1>
    <#assign userType="TEACHER"/>
<#elseif (currentUser.userType) == 8>
    <#assign userType="RSTAFF"/>
</#if>
<#import "../layout/layout.ftl" as temp />
<@temp.page index='' columnType="empty">
    <#if detail?has_content>
    <p class="bread_crumb_nav clearfix">
        <#--<#if tagType?has_content>-->
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
        <#--</#if>-->
        <a href="/reward/product/exclusive/index.vpage">商品列表</a><span class="active">></span>>${detail.productName!''}
    </p>
    <div class="home_sales_box clearfix">
        <div class="home_sales_left">
            <div class="large_pic_box">
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
            <div class="original_price_box clearfix">
                <span class="original_price">原价</span>
                <span class="original_price_number J_light_gray"
                      style="text-decoration: line-through;">${detail.originPrice!''}</span>
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
                    <span class="vip_price_number w-orange JS-discountPrice" id="disCountPrice"></span>
                </#if>
                <span class="J_red price_unit"><i class="w-gold-icon w-gold-icon-8"></i></span>
                <#if userType == 'TEACHER'>
                    <#assign hide = false />
                    <#list detail.skus as s>
                        <#if s_index == 0 && s.inventorySellable == 0>
                            <#assign hide = true />
                        </#if>
                    </#list>
                   <#if detail.discountPrice?? && detail.discountPrice gt 4>
                       <#if hide>
                           <span style="cursor: pointer;float: right; background-color: #d6d6d6; width: auto; padding: 0 10px;" class="vip_price">${((currentTeacherDetail.isJuniorTeacher())!false)?string("50", "5")}<@ftlmacro.garyBeansText/>试手气</span>
                       <#else>
                           <span style="cursor: pointer;float: right; background-color: #ff6f48; width: auto; padding: 0 10px;" class="vip_price" id="drawlottery">${((currentTeacherDetail.isJuniorTeacher())!false)?string("50", "5")}<@ftlmacro.garyBeansText/>试手气</span>
                       </#if>
                   </#if>
                </#if>
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
            <dl class="clearfix">
                <dt>选择款式：</dt>
                <dd id="p_style_list_box">
                    <#list detail.skus as s>
                        <#if s.inventorySellable gt 0>
                            <a class="allowed" href="javascript:void(0);"
                               data-skus_id="${s.id!''}" data-skus_sellable="${s.inventorySellable!0}" id="skusId">${s.skuName!''}</a>
                        <#else>
                            <a href="javascript:void(0);" style="border:1px dashed #ccc;color:#ccc;pointer-events:none;">${s.skuName!''}</a>
                        </#if>
                    </#list>
                </dd>
            </dl>
            <dl class="clearfix">
                <dt>库存数量：</dt>
                <dd>
                    <span class="J_gray JS-inventory"> <#if inventory ?? && inventory gt 10000>
                        10000+
                    <#else>${inventory}
                    </#if></span>
                </dd>
            </dl>
            <dl class="clearfix">
                <dt>兑换数量：</dt>
                <dd>
                    <strong class="minusBtn">-</strong>
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
                <a id="p_exchange_but" href="javascript:void(0);" class="w-but exchange_btn isNotZero" <#if needHide>style="display: none;"</#if>>我要兑换</a>
                <a id="p_addRewardBox_but" href="javascript:void(0);" class="add_wish_box"><strong>+</strong>加入愿望盒</a>
                <p class="J_gray">
                    已有<strong class="w-orange">${detail.soldQuantity!''}</strong>人兑换，
                    <strong class="w-green">${detail.wishQuantity!''}</strong>人加入愿望盒
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
    <#include "./drawlottery.ftl"/>
<script type="text/javascript">
$(function () {
    var detail = new $17.Model({
        maxIndex: $("#p_small_imgs_box li:last").data('img_index') * 1,
        showImgNum: 5,
        productNumber: $(".tempNum"),
        productPrice: $(".JS-discountPrice"),
        discountPrice: ${detail.discountPrice!''},
        maxExchangeNum: 100, //单次兑换数量最大值
        minExchangeNum: ${(detail.minBuyNums)!'1'} //单次兑换数量最小值
    });
    $("[data-go-url]").on("click", function(){
        var $this = $(this);
        setTimeout(function(){
            window.open($this.data("go-url"), "_blank");
        }, 200);
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
            var changeNum = 1; //加减奖品数差值

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
        hasNoEnoughIntegral: function (integralNum) {
            var integralUsable;
            //根据登录者的角色 获取相对应的 学豆
            <#if userType == "STUDENT">
                integralUsable = "${currentStudentDetail.userIntegral.usable}";
            <#elseif userType == "TEACHER">
                integralUsable = "${currentTeacherDetail.userIntegral.usable}";
            <#elseif userType == "RSTAFF">
                integralUsable = "${currentResearchStaffDetail.userIntegral.usable}";
            </#if>

            if (integralUsable - integralNum < 0) {
                return true;
            } else {
                return false;
            }
        },
        isVipUser: function () {
            var vip;
            //根据登录者的角色 判断是否是VIP
            <#if userType == "STUDENT">
                vip = true;
            <#elseif userType == "TEACHER">
                <#if (currentTeacherDetail.schoolAmbassador)?? && currentTeacherDetail.schoolAmbassador >
                    vip = true;
                <#else>
                    vip = false;
                </#if>
            <#elseif userType == "RSTAFF">
                vip = true;
            </#if>

            var saleGroup = '${detail.saleGroup}'; // VIP NORMAL
            if (vip) {
                return true;
            } else {
                if (saleGroup.toUpperCase() == 'VIP') {
                    return false;
                } else {
                    return true;
                }
            }
        },
        wishSubmit: function (productId) {
            $.post('/reward/order/addwishorder.vpage', {productId: productId}, function (data) {
                if (data.success) {
                    $.prompt("恭喜，加入我的收藏成功！", {
                        title: "",
                        buttons: { "去查看": false, "知道了": true },
                        focus: 1,
                        submit: function (e, v) {
                            e.preventDefault();
                            if (v) {
                                $.prompt.close();
                            } else {
                                location.href = '/reward/order/mywish.vpage';
                            }
                        }
                    });
                } else {
                    $17.alert(data.info);
                }
            });
        },
        init: function () {
            var $this = this;
            var roleTypes;
            var module_log = '';
            detail.productPrice.text(detail.discountPrice);
            <#if userType == 'STUDENT'>
                roleTypes = "web_student_logs";
                module_log = 'm_wVdGfet6';
            <#elseif userType == 'TEACHER'>
                <#if teacherNewLevel?? && teacherNewLevel gt 2>
                    <#if teacherCouponList?? && teacherCouponList?size gt 0>
                        var teaBox = $(".JS-couponNumber");
                        var teaCouponLen = teaBox.text() - 1;
                        var teaCoupon = teaBox.attr("data-coupon");
                        teaBox.text(teaCouponLen);
                        roleTypes = "web_teacher_logs";
                        module_log = 'm_2ekTvaNe';
                        detail.productPrice.text(detail.discountPrice > 0 ? Math.ceil(detail.discountPrice * teaCoupon) : 0);

                        $(".JS-selectCoupon").on('click', function () {
                            if ($(this).hasClass("selected")) {
                                detail.productPrice.text(detail.discountPrice);
                                $(this).removeClass("selected");
                                teaCouponLen = Number(teaCouponLen) + 1;
                                teaBox.text(teaCouponLen);
                                teaCoupon = '';
                            } else {
                                teaCoupon = teaBox.attr("data-coupon");
                                detail.productPrice.text(Math.ceil(detail.discountPrice * teaCoupon));
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

                var skus_sellable = $(this).attr("data-skus_sellable");
                $(".JS-inventory").text(skus_sellable);
                if(skus_sellable == 0){
                    $(".isZero").show();
                    $(".isNotZero").hide();
                }else{
                    $(".isZero").hide();
                    $(".isNotZero").show();
                }
            });

            var p_num = ${(detail.minBuyNums)!'1'};
            if(p_num <= detail.minExchangeNum){
                $(".minusBtn").addClass("disabled");
            }
            if(p_num >= detail.maxExchangeNum){
                $(".plusBtn").addClass("disabled");
            }
            //兑换数量
            $(".minusBtn").on('click', function () {
                if($(this).hasClass("disabled")){
                    return false;
                }else{
                    var tempNumVal = $this.productNumber.val() * 1;
                    $this.productNumber.val($this.checkNum(tempNumVal, 'minus'));
                    //按钮样式
                    if(tempNumVal <= detail.maxExchangeNum){
                        $(this).siblings().closest('strong.plusBtn').removeClass("disabled");
                    }
                    if (tempNumVal <= detail.minExchangeNum+1) {
                        $(this).addClass("disabled");
                    }
                }

                YQ.voxLogs({ database:roleTypes, module : module_log, op : "o_3Gdnbe7z", s0: "${(detail.id)!0}", s1: "${(currentUser.userType)!0}"});
            });

            $(".plusBtn").on('click', function () {
               if($(this).hasClass("disabled")){
                   return false;
               }else{
                   var tempNumVal = $this.productNumber.val() * 1;
                   $this.productNumber.val($this.checkNum(tempNumVal));
                   //按钮样式
                   if(tempNumVal >= detail.minExchangeNum){
                       $(this).siblings().closest('strong.minusBtn').removeClass("disabled");
                   }
                   if (tempNumVal >= detail.maxExchangeNum-1) {
                       $(this).addClass("disabled");
                   }
               }
                YQ.voxLogs({database:roleTypes, module : module_log, op : "o_3Gdnbe7z", s0: "${(detail.id)!0}", s1: "${(currentUser.userType)!0}"});
            });

            var maxNumberCount = detail.minExchangeNum;
            $this.productNumber.on("keyup", function(){
                var $that = $(this);
                if( !$17.isNumber($that.val()) ){
                    $that.val(maxNumberCount);
                    return false;
                }
                if($that.val() > detail.maxExchangeNum){
                    $that.val(maxNumberCount);
                    return false;
                }

                maxNumberCount = $that.val();
                YQ.voxLogs({database:roleTypes, module : module_log, op : "o_3Gdnbe7z", s0: "${(detail.id)!0}", s1: "${(currentUser.userType)!0}"});
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
                index = (index == $this.maxIndex) ? -1 : index;
                detail.tagsAddClass(++index, 'nextBut');
            });

            //默认选中第一个库存不为0的款式
            $("#p_style_list_box").children(".allowed").first().click();


            //我要兑换
            $("#p_exchange_but").on('click', function () {
                var $this = $(this);
                <#if ProductDevelopment.isStagingEnv()>
                    $17.alert("奖品中心尚未开放，敬请期待");
                    return false;
                </#if>

                var p_type_id = $("#p_style_list_box a.active").data('skus_id');
                var p_num = detail.productNumber.val() * 1;
                var integralCount = $(".vip_price_number").text() * 1 * p_num;
                var date = new Date();
                var tipStr = '';
                var showDeduct = '';
                var $promptButtons = {"取消": false, "兑换": true};
                $.get('/reward/order/notify.vpage',{},function(res){
                    if (res.success){
                        var tipPrice = detail.productPrice.text() * p_num;
                        // 设置弹窗上的发货规则文案
                        <#if userType == 'STUDENT'>
                            showDeduct = '';
                        <#else>
                            // 默认小学园丁豆
                            var insufficientAmount = '500园丁豆';
                            var spendAmount = '200园丁豆';
                            // 初中为学豆
                            <#if (currentTeacherDetail.isJuniorTeacher())!false>
                                insufficientAmount = '5000学豆';
                                spendAmount = '2000学豆';
                            </#if>
                            showDeduct = '<p style="margin:10px 25px 0;font-size:16px;text-align:;">教学用品中心实行阶梯包邮制度：如当月累计兑换实物奖品不足' + insufficientAmount + '，需额外使用' + spendAmount + '兑换包邮服务一次（下月发货时自动扣除，余额不足' + spendAmount + '，则全部扣除）；如累计实物奖品超过' + insufficientAmount + '，则自动包邮。</p>';
                        </#if>

                        <#if ftlmacro.isInSummerRange >
                            <#if userType == 'STUDENT'>
                                tipStr = '确认花'+  (tipPrice)+'学豆兑换这个奖品吗？<br/>现在兑换，将于9月15日到20日左右寄到你的老师手里。';
                            <#else>
                                tipStr = '确认花'+  (tipPrice)+'<@ftlmacro.garyBeansText/>兑换这个奖品吗？<br/>现在兑换，将于9月15日到20日左右寄到您的手里。';
                            </#if>
                        <#elseif ftlmacro.isInWinterRange>
                            <#if userType == 'STUDENT'>
                                tipStr = '确认花'+  (tipPrice)+'学豆兑换这个奖品吗？<br/>现在兑换，将于3月15日到20日左右寄到你的老师手里。';
                            <#else>
                                tipStr = '确认花'+  (tipPrice)+'<@ftlmacro.garyBeansText/>兑换这个奖品吗？<br/>现在兑换，将于3月15日到20日左右寄到您的手里。';
                            </#if>
                        <#else>
                            <#if userType == 'STUDENT'>
                                tipStr = '确认花'+  (tipPrice)+'学豆兑换这个奖品吗？<br/>现在兑换，将于' + (date.getMonth() + 2 > 12 ? 1 : date.getMonth() + 2) + '月15日到20日左右寄到你的老师手里。';
                            <#else>
                                tipStr = '确认花'+  (tipPrice)+'<@ftlmacro.garyBeansText/>兑换这个奖品吗？<br/>现在兑换，将于' + (date.getMonth() + 2 > 12 ? 1 : date.getMonth() + 2) + '月15日到20日左右寄到您的手里。';
                            </#if>
                        </#if>

                        $.ajax({
                            type : "post",
                            url : "/reward/order/checkteacherexistforstu.vpage",
                            success : function(checkdata){
                                if(checkdata.success){
                                    var states = {
                                        <#if userType == 'STUDENT'>
                                            state0: {
                                                html: tipStr + showDeduct,
                                                title: '',
                                                buttons: $promptButtons,
                                                position : {width: 550},
                                                focus: 1,
                                                submit: function (e, v) {
                                                    YQ.voxLogs({database:roleTypes, module : module_log, op : "pop_confirm_exchange_show", s0: "${(detail.id)!0}",s1:"${(detail.productType)!0}"} );
                                                    e.preventDefault();
                                                    if (v) {
                                                        $.post('/reward/order/createorder.vpage', {productId: '${detail.id!''}', skuId: p_type_id, quantity: p_num}, function (data) {
                                                            if (data.success) {
                                                                updateMyRewardCount('plus');
                                                                $.prompt.goToState('state1');
                                                                YQ.voxLogs({database:roleTypes,module : module_log, op : "button_confirm_exchange_click", s0: "${(detail.id)!0}",s1:"${(detail.productType)!0}"});
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
                                        <#else>
                                            state0: {
                                                html: tipStr + showDeduct,
                                                title: '',
                                                buttons: $promptButtons,
                                                position : {width: 550},
                                                focus: 1,
                                                submit: function (e, v) {
                                                    e.preventDefault();
                                                    YQ.voxLogs({database:roleTypes, module : module_log, op : "pop_confirm_exchange_show", s0: "${(detail.id)!0}",s1:"${(detail.productType)!0}"});
                                                    if (v) {
                                                        var refId = '', couponNumber = 0;
                                                        if ($(".JS-selectCoupon").hasClass("selected")) {
                                                            refId = $(".JS-couponNumber").attr("data-refId");
                                                            couponNumber = $(".JS-couponNumber").attr("data-coupon") * 10;
                                                        }
                                                        var params = {
                                                            productId: ${detail.id!''},
                                                            discountPrice: detail.productPrice.text(),
                                                            couponNumber: couponNumber,
                                                            RefId: refId,
                                                            skuId: p_type_id,
                                                            num: p_num
                                                        };
                                                        location.href="/reward/product/orderconfirm.vpage?" + $.param(params);
                                                    } else {
                                                        $.prompt.close();
                                                    }
                                                }
                                            },
                                        </#if>
                                        state1: {
                                            title: '',
                                            html: '恭喜，兑换成功！请耐心等待寄送吧。',
                                            buttons: {'去查看': true, '知道了': false },
                                            focus: 1,
                                            submit: function (e, v, m, f) {
                                                e.preventDefault();
                                                if (v) {
                                                    location.href = '/reward/order/myorder.vpage';

                                                } else {
                                                    $.prompt.close();
                                                    window.location.reload();
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
                                }else{
                                    $.prompt(checkdata.info, {
                                        title: "",
                                        buttons: {"知道了": true},
                                        submit: function () {
                                            $.prompt.close();
                                        }
                                    });
                                }
                            }
                        });
                    }
                });

                YQ.voxLogs({database: roleTypes, module : module_log, op : "o_vJCuwRSv", s0: "${(detail.id)!0}", s1: "${(currentUser.userType)!0}"});
            });

            //加入愿望盒
            var weiXinCode;
            var currentUserType = "${userType!''}";
            $("#p_addRewardBox_but").on('click', function () {
                publicHaswishOrder();
                <#--<#if userType == "STUDENT">
                    //学生要求绑定微信才能使用
                    $.post("/reward/order/hasbindwechat.vpage", {}, function (data) {
                        if(data.success && !data.bindFlag){
                            if(weiXinCode != undefined){
                                $.prompt(template("t:weiXinSideDetail", { weiXinCode : weiXinCode}),{
                                    title : "扫一扫二维码",
                                    buttons : {}
                                });
                                return false;
                            }

                            $.get("/student/qrcode.vpage?campaignId=4", function(data){
                                if(data.success){
                                    weiXinCode = data.qrcode_url;
                                }else{
                                    weiXinCode = "<@app.link href="public/skin/studentv3/images/2dbarcode.jpg"/>";
                                }

                                $.prompt(template("t:weiXinSideDetail", { weiXinCode : "//www.17zuoye.com/qrcode?m=http%3A%2F%2Fwww.17zyw.cn%2FAR3aIf"}),{
                                    title : "扫一扫二维码",
                                    buttons : {}
                                });

                                $17.tongji("奖品中心-加入许愿盒-点击获取二维码");
                            });
                        }else{
                            publicHaswishOrder();
                        }
                    });
                <#else>
                    publicHaswishOrder();
                </#if>-->

                //如果愿望盒中已存在奖品 提示替换，反之添加
                function publicHaswishOrder(){
                    var haswishorder = true;
                    $.post("/reward/order/haswishorder.vpage", {}, function (data) {
                        if (!data.success) {
                            haswishorder = false
                        }
                        $.prompt("确定加入到我的收藏吗？", {
                            title: "",
                            focus: 1,
                            buttons: { "取消": false, "确定": true },
                            submit: function (e, v) {
                                e.preventDefault();
                                if (v) {
                                    detail.wishSubmit('${detail.id!''}');
                                } else {
                                    $.prompt.close();
                                }
                            }
                        });

                    });
                }

            });
        }
    }).init();
});
</script>
<script type="text/html" id="t:weiXinSideDetail">
    <div class='weiXinSideDetail' style='text-align: center'>
        <dl>
            <dd>
                许愿前，先下载一起作业家长通吧！<br/>
                绑定家长通成功，奖品才可放入愿望盒
            </dd>
            <dt><img src='<%=weiXinCode%>' width='200' height='200'/></dt>
            <dd>扫一扫下载家长通<br>
                还有更多专属活动参与 </dd>
        </dl>
        <div style="clear:both;"></div>
    </div>
</script>
</@temp.page>