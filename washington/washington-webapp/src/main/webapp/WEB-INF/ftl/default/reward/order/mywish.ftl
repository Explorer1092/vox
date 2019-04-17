<#import "module.ftl" as temp />
<@temp.page title='mywish'>
    <#if temp.currentUserType == 'STUDENT'>
        <#assign userIntegralftl = currentStudentDetail.userIntegral.usable />
    <#elseif temp.currentUserType == 'TEACHER'>
        <#assign userIntegralftl = currentTeacherDetail.userIntegral.usable />
    <#elseif temp.currentUserType == 'RSTAFF'>
        <#assign userIntegralftl = currentResearchStaffDetail.userIntegral.usable />
    </#if>
    <#assign countBean = 5/>
    <#if (currentUser.userType) == 1>
        <#if (currentTeacherDetail.isJuniorTeacher())!false>
            <#assign countBean = 50/>
        <#else>
            <#assign countBean = 5/>
        </#if>
    </#if>
    <#assign userTryNum = 1/>
    <#if (currentUser.userType) == 1>
        <#if (currentTeacherDetail.isJuniorTeacher())!false>
            <#assign userTryNum = 10/>
        <#else>
            <#assign userTryNum = 1/>
        </#if>
    </#if>

<div data-bind="foreach: mywishList,visible:mywishList().length > 0" style="display:none;">
    <div class="my_wish_box clearfix" style="margin-top:20px;">
        <div class="my_wish_img float_left">
            <i></i>
            <!--ko if: image && image.indexOf("oss-image.17zuoye.com") > -1 -->
            <img src="" data-bind="attr:{ src: image}" style="height:90%;">
            <!--/ko-->
            <!--ko if: image && image.indexOf("oss-image.17zuoye.com") == -1 -->
            <img src="" data-bind="attr:{ src: '<@app.avatar href="/"/>' + image}" style="height:90%;">
            <!--/ko-->
        </div>
        <div class="my_wish_msg_box float_right">
            <p class="title" data-bind="text: productName"></p>
            <div class="my_wish_instruction">
                <p class="clearfix">
                    <span class="float_left">已集齐：</span>
                    <strong class="float_left J_red" id="alreadyHaveBeans">${userIntegralftl}</strong>
                    <span class="float_left" style="margin-right:20px;"><@ftlmacro.garyBeansText/></span>
                    <!--ko if: ${userIntegralftl} < price -->
                        <span class="float_left">还需要：</span>
                        <strong class="float_left J_red" id="needBeans" data-bind="text: price - ${userIntegralftl}"></strong>
                        <span class="float_left"><@ftlmacro.garyBeansText/></span>
                    <!--/ko-->
                </p>

                <div class="my_wish_msg_process">
                    <div class="my_wish_process_bg">
                        <div class="my_wish_status_box">
                            <div class="my_wish_status ProgressBarAnimate" data-bind="style:{width : ${userIntegralftl}/price >= 1 ? '100%': ${userIntegralftl}/price *100 +'%' }">
                                <div class="my_wish_status_inner"></div>
                                <div class="bean_point have_beans"><i class="J_sprites have_beans_bg"></i><p class="showIntegralNum">${userIntegralftl}<@ftlmacro.garyBeansText/></p></div>
                            </div>
                        </div>
                        <div class="bean_point need_beans"><p><!-- ko text:price--><!--/ko--><@ftlmacro.garyBeansText/></p><i class="J_sprites need_beans_bg"></i></div>
                    </div>
                </div>
            </div>
            <p class="btn_box">
                <#if temp.currentUserType == 'TEACHER'>
                <a href="javascript:void(0);" class="J_btn" style="background-color: #ff6f48; margin-right: 15px; padding:15px 20px" data-bind="click: $root.drawlottery">${((currentTeacherDetail.isJuniorTeacher())!false)?string("50", "5")}<@ftlmacro.garyBeansText/>试手气</a>
                </#if>
                <!--ko if: ${userIntegralftl} < price -->
                <a href="javascript:void(0);" class="J_btn_disabled" title="你需要足够学豆才能实现愿望！">我要兑换</a>
                <!--/ko-->
                <!--ko if: ${userIntegralftl} >= price -->
                <a id="w_exchange_but" href="javascript:void(0);" class="J_btn" data-bind="click: $root.exchange_wish">我要兑换</a>
                <!--/ko-->
                <a id="w_delete_but" href="javascript:void(0);" class="delete_btn" data-bind="click: $root.delete_wish">删 除</a>
            </p>
        </div>
    </div>
</div>
<!--翻页-->
<div class="products_page_box" data-bind="visible:mywishList().length > 0" style="display:none;">
    <div class="message_page_list">
    <#--分页-->
    </div>
</div>
<div class="my_wish_box" style="margin-top:20px;display:none;" data-bind="visible:mywishList().length == 0">
    <div class="no_wish_bg"></div>
    <p class="font_twenty J_deep_gray no_wish_text">愿望盒里还没有添加奖品哦</p>
    <p class="J_light_gray no_wish_text">积累${temp.integarlType!''}，实现愿望！</p>
    <#if !((currentUser.userType) == 3 && ((currentStudentWebGrayFunction.isAvailable("Reward", "OfflineShiWu",true))!false))>
        <p class="btn_box go_add"><a href="/reward/product/exclusive/index.vpage" class="J_blue_btn">去添加</a></p>
    </#if>
</div>
<#if temp.currentUserType == 'TEACHER'>
<div class="null-popup" style="display:none;position:fixed;left:0;top:0;width:100%;height:100%;background:rgba(0,0,0,.6);z-index:999;overflow:hidden;" data-bind="visible: lotteryShow">
    <div class="t-rewordShopLottery-box" style="position:fixed;left:50%;top:45%;margin:-274px 0 0 -424px;">
        <div class="rs-close" data-bind="click: lotteryClose">x</div>
        <div class="rs-lottery">
            <ul>
                <li name="lottery" method="1" data-bind="click: $root.lotteryTry.bind($data,$element)"></li>
                <li name="lottery" method="2" data-bind="click: $root.lotteryTry.bind($data,$element)"></li>
                <li name="lottery" method="3" data-bind="click: $root.lotteryTry.bind($data,$element)"></li>
            </ul>
        </div>
        <div class="rs-btn">
            <a href="javascript:void(0);" class="btn-share" style="display: none;" id="resubmit" method="" data-bind="click: $root.lotteryTryAgain.bind($data,$element)">${countBean!}<@ftlmacro.garyBeansText/>再试一次</a>
        </div>
    </div>
</div>
</#if>

<script id="t:hasMobile" type="text/html">
    <div class="exchange_alert">
        <p class="J_deep_red btn_box font_twenty" style="padding:10px 0 5px 0;">兑换后券号将发送到你的手机</p>
        <p style="font-size:12px;text-align: center;border-bottom:1px solid #f0f0f0;margin-bottom:26px;padding-bottom:10px;">
            由于本奖品是电子兑换码，兑换成功后不能退换，敬请理解！
        </p>
        <dl class="popup-form-table">
            <dt>手机号</dt>
            <dd>
                <select id="mobile_select">
                    <%for(var i = 0; i < mobileList.length; i++){%>
                    <option value="<%==mobileList[i].mobile%>"><%==mobileList[i].mobile%></option>
                    <%}%>
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

<script type="text/javascript">
    $(function(){
        function MyWishModule() {
            var $this = this,mobileList = [];
            $this.mywishList = ko.observableArray([]);
            $this.lotteryShow = ko.observable(false);
            $this.selectShow = ko.observable(false);
            $this.setLotteryData = ko.observable();
            $this.selectStyle = ko.observable();
            getProductDetail();
            function getProductDetail(pageNum) {
                pageNum = $17.isBlank(pageNum) ? 0 : pageNum;
                var pageBox = $(".message_page_list");
                $.ajax({
                    url: "/reward/order/mywishpc.vpage",
                    type: "GET",
                    dataType:"json",
                    data: {
                        page: pageNum,
                        pageSize: 5
                    },
                    success: function (data) {
                        if(data.success){
                            $this.mywishList(data.content);
                            mobileList = data.mobileList;
                            //分页
                            pageBox.page({
                                total           : data.totalPage,
                                current         : data.currPage + 1,
                                autoBackToTop   : false,
                                jumpCallBack    : function(index){
                                    getProductDetail(index-1)
                                }
                            }).show();
                        }else{
                            pageBox.hide();
                            box.html('<div id="reload_ftl_but" style="padding: 50px 0; text-align: center; font-size: 14px;"><a href="javascript:void(0);">重新加载数据</a></div>');
                        }
                    }
                });
            }

            $this.exchange_wish = function (arg) {
                if (!arg.online) {
                    $17.alert("该商品已下架，请您关注其他商品！");
                    return false;
                }
                <#if ProductDevelopment.isStagingEnv()>
                    $17.alert("奖品中心尚未开放，敬请期待");
                    return false;
                </#if>
                <#if fakeTeacher!false>
                    $.prompt("您的账号使用存在异常，该功能受限 如有疑议，请进行申诉", {
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
                //库存为空时弹窗提示
                var wishDetail=${json_encode(arg)},num=0;
                for(var i=0;i<arg.skus.length;++i){
                    num+=arg.skus[i].inventorySellable;
                }
                if(num){
                    if (arg.oneLevelCategoryType && arg.oneLevelCategoryType == 1) {
                        location.href = '/reward/product/detail.vpage?productId=' + arg.productId;
                    } else {
                        location.href = '/reward/product/experience/detail.vpage?productId=' + arg.productId;
                    }
                    /*$this.selectStyle(arg.skus[0].id);
                    if (arg.isCouponType || arg.needMobileVerify){
                        if (arg.isCouponType) {
                            exchangedCouponFun(arg,'exchangedcoupon');
                        }else {
                            exchangedCouponFun(arg,'achievewishorder');
                        }
                    } else{
                        //当“选择款式”只有一种时，不弹窗提示，当大于一种时，弹框选择
                        if (arg.skus.length == 1){
                            addOrderById(arg, arg.skus[0].id);
                        }else{
                            $this.selectShow(true);
                        }
                    }*/

                }else{
                    $.prompt("奖品数量不足！",{
                        buttons:{"知道了":false}
                    });
                }
            };

            $this.delete_wish = function (arg) {
                $.post('/reward/order/removewishorder.vpage', {productId:arg.productId,wishOrderId : arg.wishOrderId}, function(data){
                    if(data.success){
                        $.prompt('我的收藏奖品已删除', {
                            buttons: { "知道了": true },
                            position:{width : 400},
                            submit : function(){
                                location.reload();
                            }
                        });
                    }else{
                        $.prompt(data.info,{
                            buttons: {'知道了':false  },
                            focus: 0,
                            submit:function(){
                                location.reload();
                            }
                        });
                    }
                });
            };
            var beanNum=0;
            <#if (currentUser.userType) == 1>
                beanNum = ${currentTeacherDetail.userIntegral.usable!0};
            <#elseif (currentUser.userType) == 8>
                beanNum = ${currentResearchStaffDetail.userIntegral.usable!0};
            </#if>
            $this.drawlottery = function (arg) {
                $this.setLotteryData(arg);
                var discountPrice = beanNum;
                discountPrice = parseInt(discountPrice);
                if(discountPrice >= ${countBean}){
                    $this.lotteryShow(true);
                }
                else{
                    $17.alert("每次抽奖需要消耗掉${countBean}个<@ftlmacro.garyBeansText/>，您的<@ftlmacro.garyBeansText/>数不足${countBean}个。")
                }

                YQ.voxLogs({module: "m_2ekTvaNe", op: "o_VZmeaKkE", s0: arg.productId, s1: "${(currentUser.userType)!0}"});
            };
            var tipArray={};
            tipArray.box ={
                "big":'<div class="open-big open-big-1"><div class="ob-content"> <p>运气爆棚啦！</p> <p>奖品将在<span>下月20号左右</span></p> <p>寄到您手里(如遇寒暑假，则开学后发货)</p> </div> </div>',
                "small":'<div class="open-small open-small-1"><div class="ob-content"></div></div>'
            };
            tipArray.bean = {
                "big":'<div class="open-big open-big-2"><div class="ob-content"> <p>手气不错</p> <p>获得<span>{#beanNum}</span><@ftlmacro.garyBeansText/></p> </div> </div>',
                "small":'<div class="open-small open-small-2"><div class="ob-content"></div> </div>'
            };
            tipArray.empty = {
                "big": '<div class="open-big open-big-3" ><div class="ob-content"> <p>很遗憾</p> <p>里面是空的</p> </div> </div>',
                "small":'<div class="open-small open-small-3"><div class="ob-content"></div> </div>'
            };
            $this.lotteryTryAgain = function (element) {
                var method = $(element).attr("method");
                if(method&&method.indexOf("http:")!==-1){
                    window.location.href = method;
                    return ;
                }
                $("#resubmit").hide();
                $(".rs-lottery").find("li").html("").removeClass("disabled");
            };
            $this.lotteryTry = function (element) {
                var curElement = $(element);
                var method = curElement.attr("method");
                if (curElement.hasClass("disabled")){
                    return ;
                }
                var reqParams = {
                    "productId":$this.setLotteryData().productId,
                    "skuId":$this.setLotteryData().skus[0].id
                };
                $.ajax({
                    url:"/reward/order/openmoonlightbox.vpage",
                    type:"POST",
                    data:reqParams,
                    success:function(data){
                        var result = data.success;
                        if(result){
                            $(".rs-lottery").find("li").addClass("disabled");
                            var type = data.box&&data.box.awardId;
                            switch(type){
                                case 1:
                                    curElement.html(tipArray.box.big);
                                    $("#resubmit").html("分享到论坛");
                                    $("#resubmit").attr("method","http://www.17huayuan.com/forum.php?mod=post&action=newthread&fid=2");
                                    type="box";
                                    break;
                                case 2:
                                    curElement.html(tipArray.bean.big.replace("{#beanNum}",${countBean!}));
                                    type="bean";
                                    $("#resubmit").html("${countBean!}<@ftlmacro.garyBeansText/>试一次");
                                    $("#resubmit").attr("method","");
                                    break;
                                case 3:
                                    curElement.html(tipArray.bean.big.replace("{#beanNum}",${userTryNum!}));
                                    type="bean";
                                    $("#resubmit").html("${countBean!}<@ftlmacro.garyBeansText/>再试一次");
                                    $("#resubmit").attr("method","");
                                    break;
                                case 4:
                                    curElement.html(tipArray.empty.big);
                                    type="empty";
                                    $("#resubmit").html("${countBean!}<@ftlmacro.garyBeansText/>试一次");
                                    $("#resubmit").attr("method","");
                                    break;
                            }
                            $("#resubmit").show();
                            renderDefault(method,type);

                        }else{
                            var infoBtn = {"知道了" : true};
                            var infoUrl = function(){$.prompt.close();};

                            if(data['authentication']){
                                infoBtn = {"去认证" : true};
                                infoUrl = function(){
                                    window.open('${(ProductConfig.getUcenterUrl())!''}/teacher/center/index.vpage#/teacher/center/myauthenticate.vpage',"_blank");
                                };
                            }else if(data['bindMobile']){
                                infoBtn = {"去填写" : true};
                                infoUrl = function(){
                                    window.open('${(ProductConfig.getUcenterUrl())!''}/teacher/center/securitycenter.vpage',"_blank");
                                };
                            }else if(data['address']){
                                infoBtn = {"去填写" : true};
                                infoUrl = function(){
                                    window.open('${(ProductConfig.getUcenterUrl())!''}/teacher/center/index.vpage#/teacher/center/myprofile.vpage',"_blank");
                                };
                            }
                            $.prompt(data.info, {
                                title : "",
                                buttons : infoBtn,
                                submit : infoUrl
                            });

                        }
                    },error:function(data){
                        $17.alert(data.info);
                    }
                });
            };
           $this.lotteryClose = function () {
               $("#resubmit").hide();
               $(".rs-lottery").find("li").html("").removeClass("disabled");
                $this.lotteryShow(false);
           };

            function renderDefault(method,type){
                var map1=["1","2","3"];
                var map2=['box','bean','empty'];
                var resultMap1 = reduceArray(map1,method);
                var resultMap2 = reduceArray(map2,type);
                for(var i=0;i<resultMap1.length;i++){
                    $("li[method='"+resultMap1[i]+"']").html(tipArray[resultMap2[i]].small);
                }
            }
            function reduceArray(arry,value){
                var result = [];
                for(var i=0;i<arry.length;i++){
                    if(arry[i]!==value){
                        result.push(arry[i]);
                    }
                }
                return result;
            }
        }
        ko.applyBindings(new MyWishModule());
    });
</script>
</@temp.page>