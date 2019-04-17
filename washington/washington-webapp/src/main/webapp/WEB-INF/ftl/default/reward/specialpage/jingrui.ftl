<!DOCTYPE html>
<html>
<head>
    <#include "../../nuwa/meta.ftl" />
    <title>精锐1对1 免费试听</title>
    <@sugar.capsule js=["jquery", "core", "alert", "template"] css=["plugin.alert", "specialskin", "project.jingrui"] />
    <@sugar.site_traffic_analyzer_begin />
</head>
<body>
<div id="specialHeader" data-val="student"></div>
<!--//start-->
<div class="header">
    <div class="inner">
        <h1 class="logo"></h1>
        <div class="tel"></div>
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
                                <h3></h3>
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
                                    <li class="check-box check-box-active" id="checkboxSpend">我愿意花费<strong>${detail.price}<@ftlmacro.garyBeansText/></strong>报名，成功后不退还</li>
                                </ul>
                                <div class="btn-box">
                                    <a href="javascript:void(0);" id="immediateExchange">报名试听</a>
                                </div>
                                <h5>报名后体验课程，还可获赠<strong>1000学豆</strong>奖励</h5>
                            </div>
                        <#else>
                            <div class="step step-2">
                                <h1></h1>
                                <h3></h3>
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
        <div class="s-tel">
            <div class="tel-inner">
                <div class="tel-title">精锐教育全国咨询热线！</div>
                <div class="tel-con" id="serviceTelBox">
                    <div class="tc-box">
                        <ul>
                            <li class="active" data-title="beijing">
                                <a href="javascript:void (0)">北京</a>
                            </li>
                            <li data-title="nanjing">
                                <a href="javascript:void (0)">南京</a>
                            </li>
                            <li data-title="guangzhou">
                                <a href="javascript:void (0)">广州</a>
                            </li>
                            <li data-title="changzhou">
                                <a href="javascript:void (0)">常州</a>
                            </li>
                            <li data-title="shenzhen">
                                <a href="javascript:void (0)">深圳</a>
                            </li>
                            <li data-title="wuxi">
                                <a href="javascript:void (0)">无锡</a>
                            </li>
                            <li data-title="hangzhou">
                                <a href="javascript:void (0)">杭州</a>
                            </li>
                            <li data-title="shanghai">
                                <a href="javascript:void (0)">上海</a>
                            </li>
                        </ul>
                    </div>
                    <div class="tc-num">
                        <p data-title="beijing" style="display: block;">
                            海淀黄庄学习中心 400-0066-911转分机30829<br/>
                            公主坟学习中心 400-0066-911转分机30830<br/>
                            大钟寺学习中心 400-0066-911转分机30831<br/>
                            牡丹园学习中心 400-0066-911转分机30832<br/>
                            朝阳门学习中心 400-0066-911转分机30833<br/>
                            王府井学习中心 400-0066-911转分机30834<br/>
                            交道口学习中心 400-0066-911转分机30835<br/>
                            和平里学习中心 400-0066-911转分机30836<br/>
                            月坛学习中心 400-0066-911转分机30837<br/>
                            白广路学习中心 400-0066-911转分机30838<br/>
                            方庄学习中心 400-0066-911转分机30839<br/>
                            马家堡学习中心 400-0066-911转分机30840<br/>
                            马家堡学习中心 400-0066-911转分机30840<br/>
                            大兴学习中心 400-0066-911转分机30842
                        </p>
                        <p data-title="nanjing">
                            南京 　 400-0066-911转分机30827
                        </p>
                        <p data-title="guangzhou">
                            广州 　 400-0066-911转分机30828
                        </p>
                        <p data-title="changzhou">
                            万达广场学习中心 400-0066-911转分机30820<br/>
                            南大街学习中心 400-0066-911转分机30821<br/>
                            清潭学习中心 400-0066-911转分机30822
                        </p>
                        <p data-title="shenzhen">
                            深圳南山校区 400-0066-911转分机30843<br/>
                            深圳景田校区 400-0066-911转分机30844<br/>
                            深圳高级中学校区 400-0066-911转分机30845<br/>
                            深圳宝安校区 400-0066-911转分机30846<br/>
                            深圳百花校区 400-0066-911转分机30847
                        </p>
                        <p data-title="wuxi">
                            无锡 崇安学习中心 400-0066-911转分机30823<br/>
                            哥伦布学习中心 400-0066-911转分机30824<br/>
                            南长学习中心 400-0066-911转分机30825
                        </p>
                        <p data-title="hangzhou">
                            杭州 　 400-0066-911转分机30826
                        </p>
                        <p data-title="shanghai">
                            上海 　 400-0066-911转分机30819
                        </p>
                    </div>
                </div>
            </div>
        </div>
        <div class="s-2"></div>
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
            op:"jingrui-open",
            userId : "${currentUser.id}"
        }, "student");

        /*进入确定*/
        if(!$17.getCookieWithDefault("jingrui")){
            $17.setCookieOneDay("jingrui", "1", 1);
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
                                $17.tongji('精锐1对1-报名成功');
                                if($17.isBlank(mobileCode.val())){
                                    YQ.voxLogs({module : 'coupon', op : 'jingrui_submit', userId : '${(currentUser.id)!''}'},'student');
                                }else{
                                    YQ.voxLogs({module : 'coupon', op : 'jingrui_bind_submit', userId : '${(currentUser.id)!''}'},'student');
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
                op:"jingrui-getVerifyCode",
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
            YQ.voxLogs({module : 'coupon', op : 'jingrui_bind', userId : '${(currentUser.id)!''}'},'student');

            $.post(getMobilecodeUrl, { mobile : mobile}, function(data){
                if(!data.success){
                    $17.alert(data.info);
                    $this.removeClass("btn_disable");
                }
                $17.getSMSVerifyCode($this, data);
            });
        });

        //电话切换
        var serviceTelBox = $("#serviceTelBox");
        serviceTelBox.find(".tc-box li").on("click", function(){
            var $this = $(this);

            $this.addClass("active").siblings().removeClass("active")
            serviceTelBox.find(".tc-num p[data-title='"+ $this.data("title") +"']").show().siblings().hide();
        });
    });
</script>
<!--end//-->
<div id="footerPablic"></div>
<script src="//cdn.17zuoye.com/static/project/module/js/project-plug.js"></script>
</body>
</html>