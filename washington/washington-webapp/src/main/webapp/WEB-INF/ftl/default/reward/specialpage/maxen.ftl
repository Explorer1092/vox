<!DOCTYPE html>
<html>
<head>
    <#include "../../nuwa/meta.ftl" />
    <title>迈格森英语</title>
    <@sugar.capsule js=["jquery", "core", "alert", "template"] css=["plugin.alert", "specialskin", "project.maxen"] />
    <@sugar.site_traffic_analyzer_begin />
</head>
<body>
    <div id="specialHeader" data-val="student"></div>
    <!--//start-->
    <!--//start-->
    <div class="main">
        <div class="header">
            <div class="inner">
                <h1 class="logo"></h1>
                <div class="tel">
                    <p>免费咨询：400-0066-911           转分机  北京：30870   天津：30871</p>
                    <p>成都：30872   广州：30873   重庆：30874   沈阳：30875</p>
                </div>
            </div>
        </div>
        <div class="section">
            <div class="s-1">
                <div class="inner-one">
                    <div class="inner-two">
                        <div class="fixed-box">
                            <div class="login-box" data-main="up">
                            <#if !myCoupon??>
                                <div class="step step-1" style="display: ;">
                                    <h3>
                                        <span>免费预约即可获赠</span>
                                        赠“新东方高端少儿英语体验课”<br/>
                                        赠“个性化英语国际测评”
                                    </h3>
                                    <dl>
                                        <dt>
                                            手机号
                                        </dt>
                                        <#if mobileList?size == 0>
                                            <!-- 未绑定手机号 -->
                                            <dd>
                                                <input type="text" value="" class="int mobile" >
                                                <a href="javascript:void(0);" class="btn-orange getVerifyCode" data-title="up"><span>获取验证码</span></a>
                                            <#--<p class="init">号码错误</p>-->
                                            </dd>
                                        <#else>
                                            <dd>
                                                <div class="mobile-box int mobileSelect">
                                        <span class="mobile-name w-content" data-value="<#if mobileList?? && mobileList?size gt 0>${mobileList[0].mobile}</#if>">
                                            <#if mobileList?? && mobileList?size gt 0>${mobileList[0].mobile!} ${mobileList[0].callName!}</#if>
                                        </span>
                                                    <span class="mobile-arrow"></span>
                                                    <ul class="mobileList" style="display: none;">
                                                        <#if mobileList?? && mobileList?size gt 0>
                                                            <#list mobileList as m>
                                                                <li data-mobile="${m.mobile!}"> ${m.mobile!} ${m.callName!}</li>
                                                            </#list>
                                                        </#if>
                                                    </ul>
                                                </div>
                                                <a href="javascript:void(0);" class="btn-orange getVerifyCode" data-title="up"><span>获取验证码</span></a>
                                            <#--<p class="init">号码错误</p>-->
                                            </dd>
                                        </#if>
                                        <dt>
                                            验证码
                                        </dt>
                                        <dd>
                                            <input type="text" value=""  class="int mobileCode">
                                            <span class="event-edit">
                                                号码错误？<br/><a href="${(ProductConfig.getUcenterUrl())!''}/student/center/account.vpage?updateType=mobile" target="_blank">点击修改</a>
                                            </span>
                                        <#--<p class="init">验证码错误</p>-->
                                        </dd>
                                    </dl>
                                    <ul class="list">
                                        <li class="check-box check-box-active checkboxSpend">我愿意花费<strong>${detail.price}<@ftlmacro.garyBeansText/></strong>报名，成功后不退还，</li>
                                        <li>并接受来自迈格森的活动与优惠信息</li>
                                    </ul>
                                    <div class="btn-box">
                                        <a href="javascript:void(0);" class="immediateExchange" data-title="up">报名试听</a>
                                    </div>
                                    <h5>成功体验试听课程，还可获得<strong>1000学豆</strong>奖励</h5>
                                </div>
                            <#else>
                                <div class="step step-2">
                                    <h3>
                                        <span>免费预约即可获赠</span>
                                        赠“新东方高端少儿英语体验课”<br/>
                                        赠“个性化英语国际测评”
                                    </h3>
                                    <h4>你的报名编号</h4>
                                    <div class="code-box">
                                    ${(myCoupon.couponNo)!}
                                    </div>
                                    <h5>成功体验试听课程，还可获得<strong>1000学豆</strong>奖励</h5>
                                </div>
                            </#if>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="s-2">
                <p class="p-1"></p>
                <p class="p-2"></p>
            </div>
            <div class="s-3">
                <div class="inner">
                    <p class="p-1"></p>
                    <p class="p-2"></p>
                </div>
            </div>
        </div>
        <div class="footer">
            <div class="fixed-box">
                <div class="login-box" data-main="down">
                <#if !myCoupon??>
                    <div class="step step-1" style="display: ;">
                        <h3>
                            <span>免费预约即可获赠</span>
                            赠“新东方高端少儿英语体验课”<br/>
                            赠“个性化英语国际测评”
                        </h3>
                        <dl>
                            <dt>
                                手机号
                            </dt>
                            <#if mobileList?size == 0>
                                <!-- 未绑定手机号 -->
                                <dd>
                                    <input type="text" value="" class="int mobile" >
                                    <a href="javascript:void(0);" class="btn-orange getVerifyCode" data-title="down"><span>获取验证码</span></a>
                                <#--<p class="init">号码错误</p>-->
                                </dd>
                            <#else>
                                <dd>
                                    <div class="mobile-box int mobileSelect">
                                        <span class="mobile-name w-content" data-value="<#if mobileList?? && mobileList?size gt 0>${mobileList[0].mobile}</#if>">
                                            <#if mobileList?? && mobileList?size gt 0>${mobileList[0].mobile!} ${mobileList[0].callName!}</#if>
                                        </span>
                                        <span class="mobile-arrow"></span>
                                        <ul class="mobileList" style="display: none;">
                                            <#if mobileList?? && mobileList?size gt 0>
                                                <#list mobileList as m>
                                                    <li data-mobile="${m.mobile!}"> ${m.mobile!} ${m.callName!}</li>
                                                </#list>
                                            </#if>
                                        </ul>
                                    </div>
                                    <a href="javascript:void(0);" class="btn-orange getVerifyCode" data-title="down"><span>获取验证码</span></a>
                                <#--<p class="init">号码错误</p>-->
                                </dd>
                            </#if>
                            <dt>
                                验证码
                            </dt>
                            <dd>
                                <input type="text" value=""  class="int mobileCode">
                                            <span class="event-edit">
                                                号码错误？<br/><a href="${(ProductConfig.getUcenterUrl())!''}/student/center/account.vpage?updateType=mobile" target="_blank">点击修改</a>
                                            </span>
                            <#--<p class="init">验证码错误</p>-->
                            </dd>
                        </dl>
                        <ul class="list">
                            <li class="check-box check-box-active checkboxSpend">我愿意花费<strong>${detail.price}<@ftlmacro.garyBeansText/></strong>报名，成功后不退还，</li>
                            <li>并接受来自迈格森的活动与优惠信息</li>
                        </ul>
                        <div class="btn-box">
                            <a href="javascript:void(0);" class="immediateExchange" data-title="down">报名试听</a>
                        </div>
                        <h5>成功体验试听课程，还可获得<strong>1000学豆</strong>奖励</h5>
                    </div>
                <#else>
                    <div class="step step-2">
                        <h3>
                            <span>免费预约即可获赠</span>
                            赠“新东方高端少儿英语体验课”<br/>
                            赠“个性化英语国际测评”
                        </h3>
                        <h4>你的报名编号</h4>
                        <div class="code-box">
                        ${(myCoupon.couponNo)!}
                        </div>
                        <h5>成功体验试听课程，还可获得<strong>1000学豆</strong>奖励</h5>
                    </div>
                </#if>
                </div>
            </div>
        </div>
    </div>
    <!--end//-->


    <script id="t:进入确定" type="text/html">
        <div class="popup-box-1">
            <div class="promptclose promptclose-skin"></div>
            <div class="p-btn">
                <a href="javascript:void(0);" id="tellparent" class="promptclose">确定</a>
            </div>
        </div>
    </script>
    <script id="t:告诉父母提示框" type="text/html">
        <div class="popup-box-2">
            <div class="promptclose promptclose-skin"></div>
            <div class="p-btn">
                <a href="javascript:void(0);" class="m-1" id="tellparent">告诉爸爸妈妈</a>
                <a href="javascript:void(0);" class="m-2" id="okBtn">确认报名</a>
            </div>
        </div>
    </script>


    <script type="text/javascript">
        $(function(){
            YQ.voxLogs({
                module: "coupon",
                op:"maxen-open",
                userId : "${currentUser.id}"
            }, "student");

            /*进入确定*/
            if(!$17.getCookieWithDefault("MAXEN")){
                $17.setCookieOneDay("MAXEN", "1", 1);
                $.prompt(template("t:进入确定", {}), {
                    title : '',
                    buttons : {},
                    position : {width : 780},
                    focus : 1
                });
            }

            //是否有手机列表
            var mobileListSize = ${(mobileList?size)!0};

            //我愿意花费复选框
            $('.checkboxSpend').on("click", function(){
                var $this = $(this);
                $this.toggleClass('check-box-active');
            });

            //手机下拉框
            var $mobileList = $(".mobileList");
            var $mobileSelect = $(".mobileSelect");
            $mobileSelect.toggle(function(){
                $mobileList.show();
            },function(){
                $mobileList.hide();
            });

            $("li",$mobileList).on("click",function(){
                var $dark = $mobileSelect.find("span.w-content");
                $dark.attr("data-value", $(this).data("mobile"));
                $dark.text($(this).text());
                $mobileList.hide();
                return false;
            });

            var mobileInt = $(".mobile");
            var mobileCode = $(".mobileCode");
            var checkboxSpend = $(".checkboxSpend");
            var mobile = "";
            //立即报名
            $('.immediateExchange').on("click", function(){
                var $this = $(this);
                mobileInt = $this.closest("[data-main='"+ $this.data("title") +"']").find(".mobile");
                $mobileSelect = $this.closest("[data-main='"+ $this.data("title") +"']").find(".mobileSelect");
                mobileCode = $this.closest("[data-main='"+ $this.data("title") +"']").find(".mobileCode");
                checkboxSpend = $this.closest("[data-main='"+ $this.data("title") +"']").find(".checkboxSpend");

                if(!checkboxSpend.hasClass("check-box-active")){
                    $17.alert("请勾选：我愿意花费${detail.price}<@ftlmacro.garyBeansText/>报名");
                    return false;
                }

                if(mobileListSize == 0){
                    mobile = mobileInt.val();
                    if($17.isBlank(mobileInt.val()) || !$17.isMobile(mobileInt.val())){
                        mobile = "";
                        $17.alert("请填写正确的手机号码",function(){
                            mobileInt.focus();
                        });
                        return false;
                    }
                }else{
                    mobile = $mobileSelect.find("span.w-content").attr("data-value");
                    if($17.isBlank(mobile) || !$17.isMobile(mobile)){
                        mobile = "";
                        $17.alert("请选择正确的手机号码");
                        return false;
                    }
                }

                if($17.isBlank(mobileCode.val()) || !$17.isNumber(mobileCode.val())){
                    $17.alert("请输入验证码");
                    return false;
                }
                var states = {
                    state0: {
                        title : '',
                        html : template("t:告诉父母提示框",{}),
                        buttons : {},
                        position : {width : 720},
                        focus : 1
                    }
                };
                $.prompt(states,{
                    loaded : function(){
                        $("div.jqiclose").hide();
                        //确认
                        $("#okBtn").on("click",function(){
                            $.post("/reward/order/exchangedcoupon.vpage", {
                                mobile : mobile,
                                smsCode : mobileCode.val(),
                                productName : "${(detail.productName)!}"
                            }, function(data){
                                if(data.success){
                                    //统计-点击立即报名的点击数，其中已经绑定过的手机直接点击报名数和先绑定后点击报名数
                                    $17.tongji('迈格森-报名成功');
                                    if($17.isBlank(mobileCode.val())){
                                        YQ.voxLogs({module : 'coupon', op : 'maxen_submit', userId : '${(currentUser.id)!''}'},'student');
                                    }else{
                                        YQ.voxLogs({module : 'coupon', op : 'maxen_bind_submit', userId : '${(currentUser.id)!''}'},'student');
                                    }
                                    $17.alert("报名成功",function(){
                                        location.reload();
                                    });
                                }else{
                                    $17.alert(data.info);
                                }
                            });
                        });
                    }
                });
            });

            //关闭
            $(document).on("click", ".promptclose",function(){
                $.prompt.close();
            });
            //告诉父母
            $(document).on("click", "#tellparent",function(){
                $.prompt.close();
            });

            //获取验证码
            $('.getVerifyCode').on("click", function(){
                YQ.voxLogs({
                    module: "coupon",
                    op:"maxen-getVerifyCode",
                    userId : "${currentUser.id}"
                }, "student");

                var $this = $(this);
                mobileInt = $this.closest("[data-main='"+ $this.data("title") +"']").find(".mobile");
                $mobileSelect = $this.closest("[data-main='"+ $this.data("title") +"']").find(".mobileSelect");

                var getMobilecodeUrl = "";
                if(mobileListSize == 0){
                    mobile = mobileInt.val();
                    if($17.isBlank(mobileInt.val()) || !$17.isMobile(mobileInt.val())){
                        $17.alert("请填写正确的手机号码",function(){
                            mobile = "";
                            mobileInt.focus();
                        });
                        return false;
                    }
                    getMobilecodeUrl = "/reward/order/sendmobilecodecoupon.vpage";
                }else{
                    getMobilecodeUrl = "/reward/order/sendmobilecodemessage.vpage";
                    mobile = $mobileSelect.find("span.w-content").attr("data-value");
                    if($17.isBlank(mobile) || !$17.isMobile(mobile)){
                        $17.alert("请选择正确的手机号码");
                        return false;
                    }
                }

                if($17.isBlank(getMobilecodeUrl)){
                    $17.alert("请刷新页面重试");
                    return false;
                }

                if($this.hasClass("btn_disable")){return false;}

                //统计-点击绑定手机号按钮的点击数
                YQ.voxLogs({module : 'coupon', op : 'maxen_bind', userId : '${(currentUser.id)!''}'},'student');

                $.post(getMobilecodeUrl, { mobile : mobile}, function(data){
                    if(!data.success){
                        $17.alert(data.info);
                        $this.removeClass("btn_disable");
                    }
                    $17.getSMSVerifyCode($this, data);
                });
            });
        });
    </script>
    <!--end//-->
    <div id="footerPablic"></div>
    <script src="//cdn.17zuoye.com/static/project/module/js/project-plug.js"></script>
</body>
</html>