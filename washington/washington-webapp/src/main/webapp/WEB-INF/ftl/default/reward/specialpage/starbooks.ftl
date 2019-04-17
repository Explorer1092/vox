<!DOCTYPE html>
<html>
<head>
    <#include "../../nuwa/meta.ftl" />
    <title>Starbooks英文原版绘本教学体验课</title>
    <@sugar.capsule js=["jquery", "core", "alert", "template"] css=["plugin.alert", "specialskin", "project.starbooks"] />
    <@sugar.site_traffic_analyzer_begin />
</head>
<body>
    <div id="specialHeader" data-val="student"></div>
    <!--//start-->
    <div class="header">
        <div class="inner">
            <h1 class="logo"></h1>
            <div class="tel">
                <p>starbooks凯安—专业的青少年英文阅读机构</p>
            </div>
        </div>
    </div>
    <div class="main">
        <div class="section">
            <div class="s-1">
                <div class="inner-one">
                    <div class="inner-two">
                        <div class="fixed-box">
                            <div class="login-box">
                            <#if !myCoupon??>
                                <div class="step step-1" style="display: ;">
                                    <h3>
                                        <span>免费领取原版英文绘本</span>
                                        免费申请你的外教体验课，<br/>
                                        让专业的外教陪你享读经典
                                    </h3>
                                    <dl>
                                        <dt>
                                            手机号
                                        </dt>
                                        <#if mobileList?size == 0>
                                            <!-- 未绑定手机号 -->
                                            <dd>
                                                <input type="text" value="" class="int" id="mobile" >
                                                <a href="javascript:void(0);" class="btn-orange" id="getVerifyCode"><span>获取验证码</span></a>
                                            <#--<p class="init">号码错误</p>-->
                                            </dd>
                                        <#else>
                                            <dd>
                                                <div class="mobile-box int" id="mobileSelect">
                                        <span class="mobile-name w-content" data-value="<#if mobileList?? && mobileList?size gt 0>${mobileList[0].mobile}</#if>">
                                            <#if mobileList?? && mobileList?size gt 0>${mobileList[0].mobile!} ${mobileList[0].callName!}</#if>
                                        </span>
                                                    <span class="mobile-arrow"></span>
                                                    <ul id="mobileList" style="display: none;">
                                                        <#if mobileList?? && mobileList?size gt 0>
                                                            <#list mobileList as m>
                                                                <li data-mobile="${m.mobile!}"> ${m.mobile!} ${m.callName!}</li>
                                                            </#list>
                                                        </#if>
                                                    </ul>
                                                </div>
                                                <a href="javascript:void(0);" class="btn-orange" id="getVerifyCode"><span>获取验证码</span></a>
                                            <#--<p class="init">号码错误</p>-->
                                            </dd>
                                        </#if>
                                        <dt>
                                            验证码
                                        </dt>
                                        <dd>
                                            <input type="text" value=""  class="int" id="mobileCode">
                                <span class="event-edit">
                                    号码错误？<br/><a href="${(ProductConfig.getUcenterUrl())!''}/student/center/account.vpage?updateType=mobile" target="_blank">点击修改</a>
                                </span>
                                        <#--<p class="init">验证码错误</p>-->
                                        </dd>
                                    </dl>
                                    <ul class="list">
                                        <li class="check-box check-box-active" id="checkboxSpend">我愿意花费<strong>${detail.price}<@ftlmacro.garyBeansText/></strong>报名，成功后不退还，</li>
                                        <li>并接受来自starbooks的活动与优惠信息</li>
                                    </ul>
                                    <div class="btn-box">
                                        <a href="javascript:void(0);" id="immediateExchange">报名试听</a>
                                    </div>
                                    <h5>报名后体验课程，还可获赠<strong>1000学豆</strong>奖励</h5>
                                </div>
                            <#else>
                                <div class="step step-2">
                                    <h1></h1>
                                    <h3>
                                        <span>免费领取原版英文绘本</span>
                                        免费申请你的外教体验课，<br/>
                                        让专业的外教陪你享读经典
                                    </h3>
                                    <h4>你的报名编号</h4>
                                    <div class="code-box">
                                    ${(myCoupon.couponNo)!}
                                    </div>
                                    <h5>报名后体验课程，还可获赠<strong>1000学豆</strong>奖励</h5>
                                </div>
                            </#if>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="s-2">
                <a href="http://p.bokecc.com/playvideo.bo?uid=1C53DE60C18DBC5D&playerid=C140EA7E85766C8C&playertype=1&autoStart=false&vid=4EC02895951BA40E9C33DC5901307461" target="_blank"></a>
            </div>
            <div class="s-3"></div>
        </div>
    </div>
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
                op:"starbooks-open",
                userId : "${currentUser.id}"
            }, "student");

            /*进入确定*/
            if(!$17.getCookieWithDefault("starbooks")){
                $17.setCookieOneDay("starbooks", "1", 1);
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
            $('#checkboxSpend').on("click", function(){
                var $this = $(this);
                $this.toggleClass('check-box-active');
            });

            //手机下拉框
            var $mobileList = $("#mobileList");
            var $mobileSelect = $("#mobileSelect");
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

            var mobileInt = $("#mobile");
            var mobileCode = $("#mobileCode");
            var mobile = "";
            //立即报名
            $('#immediateExchange').on("click", function(){
                var $this = $(this);

                if(!$('#checkboxSpend').hasClass("check-box-active")){
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
                                    $17.tongji('starbooks凯安-报名成功');
                                    if($17.isBlank(mobileCode.val())){
                                        YQ.voxLogs({module : 'coupon', op : 'starbooks_submit', userId : '${(currentUser.id)!''}'},'student');
                                    }else{
                                        YQ.voxLogs({module : 'coupon', op : 'starbooks_bind_submit', userId : '${(currentUser.id)!''}'},'student');
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
            $('#getVerifyCode').on("click", function(){
                YQ.voxLogs({
                    module: "coupon",
                    op:"starbooks-getVerifyCode",
                    userId : "${currentUser.id}"
                }, "student");

                var $this = $(this);

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
                YQ.voxLogs({module : 'coupon', op : 'starbooks_bind', userId : '${(currentUser.id)!''}'},'student');

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