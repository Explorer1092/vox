<!DOCTYPE html>
<html>
<head>
    <#include "../../nuwa/meta.ftl" />
    <title>贝乐英语</title>
    <@sugar.capsule js=["jquery", "core", "alert", "template"] css=["plugin.alert", "project.beile"] />
</head>
<body>
<div class="head">
    <div class="head-inner">
        <a class="logo" href="/reward/index.vpage"></a>
        <#if !myCoupon??>
            <div class="experience-box-1 experience-box">
                <div class="experience-inner">
                    <ul class="form-list">
                        <#if mobileList?size == 0>
                            <!-- 未绑定手机号 -->
                            <li class="fl-box">
                                <span class="info">手机号</span>
                                <input class="form-input" id="mobile" type="text">
                            </li>
                        <#else>
                            <li class="fl-box" style="z-index: 50">
                                <span class="info">手机号</span>
                                <div class="w-select" id="mobileSelect">
                                    <div class="current"><span class="w-content" data-value="<#if mobileList?? && mobileList?size gt 0>${mobileList[0].mobile}</#if>"><#if mobileList?? && mobileList?size gt 0>${mobileList[0].mobile!} ${mobileList[0].callName!}</span><span class="icon icon-arrow"></span></#if></div>
                                    <ul id="mobileList" style="display: none;">
                                        <#if mobileList?? && mobileList?size gt 0>
                                            <#list mobileList as m>
                                            <li class="active"><a href="javascript:void(0);" data-mobile="${m.mobile!}"> ${m.mobile!} ${m.callName!}</a></li>
                                            </#list>
                                        </#if>
                                    </ul>
                                </div>
                                <div class="fl-in">
                                    <p>号码错误？</p>
                                    <a style="color: #003bd2;" href="${(ProductConfig.getUcenterUrl())!''}/student/center/account.vpage?updateType=mobile">点击修改</a>
                                </div>
                            </li>
                        </#if>
                        <li class="error"><p></p></li>
                        <li class="fl-box">
                            <span class="info">短信验证码</span>
                            <input id="mobileCode" class="form-input" type="text" value="">
                            <a id="getVerifyCode" class="get" href="javascript:void (0)"><span>获取验证码</span></a>
                        </li>
                        <li class="error"><p></p></li>

                    </ul>
                    <div class="start-box">
                        <span id="checkboxSpend" class="icon icon-checked"></span>我愿意花费 <span class="icon icon-bean"></span>报名，成功后不退还
                        <a id="immediateExchange" class="icon-btn btn-start" href="javascript:void (0)"></a>
                    </div>
                </div>
            </div>
        <#else>
            <div class="experience-box">
                <div class="number">你的报名编号：${(myCoupon.couponNo)!}</div>
            </div>
        </#if>
    </div>
</div>
<div class="content">
    <div class="content-inner">
        <ul>
            <li>
                体验课程由贝乐学科英语免费提供，北京、深圳、上海的小学生皆可参加体验！
            </li>
            <li>
                报名完全免费，但须让爸爸妈妈知道哦！报名前先告诉爸爸妈妈，即可顺利免费体验！
            </li>
            <li>
                <p>1000 学豆奖励须知：<a id="rewarddetailbtn" href="javascript:void (0)">点击查看奖励详情</a></p>
                <p>1. 成功报名</p>
                <p>2. 客服回访，家长确认报名并愿意免费体验课程</p>
                <p>3. 奖励 1000 学豆</p>
                <p>报名成功即视为同意收到贝乐学科英语的活动及优惠资讯。</p>
            </li>
        </ul>
    </div>
</div>
<div class="column">
    <div class="ev-title">
        <a class="icon-btn btn-look" href="javascript:void (0)"></a>
        贝乐6-12岁美国小学课程——不是上课学英语，而是用英语上课
    </div>
    <div class="ev-box">
        <div class="ev-box-left">
            <h2>贝乐学科英语美国小学：为您的孩子带来纯正的美国小学课堂体验</h2>
            <p><span class="icon icon-star"></span>选用美国第一套完全按照美国共同核心州立教育标准（CCSS）编写的教材。</p>
            <p><span class="icon icon-star"></span>外教90%来自美国，均为拥有TESOL认证的全职老师；</p>
            <div class="ev-video">
                <a id="beile_video_but" class="icon icon-play" href="javascript:void (0)"></a>
            </div>
            <#--去报名-->
            <#if !myCoupon??>
            <div class="ev-btn">
                <a id="enterForBtn" class="icon-btn btn-go" href="javascript:void (0)"></a>
            </div>
            </#if>
        </div>
        <div class="ev-box-right">
            <h3>校区分布</h3>
            <dl>
                <dt>【上海】</dt>
                <dd>
                    <p>东海培训中心</p>
                    <p>天利名城培训中心</p>
                </dd>
            </dl>
            <dl>
                <dt>【深圳】</dt>
                <dd>
                    <p>东海培训中心</p>
                    <p>天利名城培训中心</p>
                </dd>
            </dl>
            <dl>
                <dt>【朝阳】</dt>
                <dd>
                    <p>SOHO尚都培训中心</p>
                    <p>安立花园培训中心</p>
                    <p>双井培训中心</p>
                    <p>望京培训中心</p>
                    <p>大悦城培训中心</p>
                </dd>
            </dl>
            <dl>
                <dt>【海淀】</dt>
                <dd>
                    <p>金源燕莎培训中心</p>
                    <p>黄庄培训中心</p>
                    <p>天行健培训中心</p>
                </dd>
            </dl>
            <dl>
                <dt>【西城】</dt>
                <dd>
                    <p>富卓培训中心</p>
                </dd>
            </dl>
            <dl>
                <dt>【昌平】</dt>
                <dd>
                    <p>回龙观培训中心</p>
                </dd>
            </dl>
        </div>
    </div>
</div>
<div class="foot-info">
    <div class="ev-title">
        活动奖励须知
    </div>
    <div class="con">
        <div class="con-inner">
            <p class="font">提醒：只有爸爸妈妈同意到店体验才能获得奖励！</p>
        </div>
    </div>
</div>
<div class="foot">
    <div class="inner">
        <div class="nav">
            <ul>
                <li><a target="_blank" href="http://www.17zuoye.com/help/aboutus.vpage">关于我们</a><span class="line">|</span></li>
                <li><a target="_blank" href="http://www.17zuoye.com/help/jobs.vpage">诚聘英才</a><span class="line">|</span></li>
                <li><a target="_blank" href="http://www.17zuoye.com/help/contactus.vpage">联系我们</a><span class="line">|</span></li>
                <li><a target="_blank" href="http://www.17zuoye.com/help/parentsguidelines.vpage">家长须知</a><span class="line">|</span></li>
                <li><a target="_blank" href="http://www.17zuoye.com/help/childrenhealthonline.vpage">儿童健康上网</a></li>
                <li class="service">
                    教师QQ群：<span>235401380</span>
                </li>
            </ul>
            <div class="clear"></div>
        </div>
        <div class="copyright">
            ${(pageBlockContentGenerator.getPageBlockContentHtml('PlatformCopyright', 'webCopyright'))!''}
        </div>
    </div>
</div>
<script id="t:告诉父母提示框" type="text/html">
    <div class="best-alert">
        <div class="ba-title">
            <a id="promptclose" class="close" href="javascript:void (0)">×</a>
            请确认报名流程
        </div>
        <div class="ba-main">
            <div class="ba-con"></div>
            <div class="btn-foot">
                <a id="tellparent" class="icon-btn btn-know" href="javascript:void (0)"></a>
                <a id="okBtn" class="icon-btn btn-sure" href="javascript:void (0)"></a>
            </div>
        </div>
    </div>
</script>
<script type="text/javascript">
    $(function(){
        YQ.voxLogs({
            module: "coupon",
            op:"beiLe-open",
            userId : "${currentUser.id}"
        }, "student");

        //视频播放
        $("#beile_video_but").click(function(){
            /*嵌入beile视频*/
            var data = '<embed id="single" width="800" height="350" flashvars="file=http://www.beile.com/upload/contents/2013/12/user_cus.flv&image=http://www.beile.com/upload/contents/2014/05/537c5b02efcb1.jpg&autostart=true&width=800&height=350" wmode="transparent" allowfullscreen="true" quality="high" name="single" style="undefined" src="http://www.beile.com/flash/flvplayer.swf" type="application/x-shockwave-flash">';
            <@ftlmacro.flashWind data="data" title="视频" wsize=820 />
            $17.tongji('贝乐视频播放');
            return false;
        });

        //是否有手机列表
        var mobileListSize = ${(mobileList?size)!0};
        //我愿意花费复选框
        $('#checkboxSpend').on("click", function(){
            var $this = $(this);
            $this.toggleClass('icon-check');
            $this.toggleClass('icon-checked');
        });

        //手机下拉框
        var $mobileList = $("#mobileList");
        var $mobileSelect = $("#mobileSelect");
        $mobileSelect.toggle(function(){
            $mobileList.show();
        },function(){
            $mobileList.hide();
        });

        $("a",$mobileList).on("click",function(){
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

            if(!$('#checkboxSpend').hasClass("icon-checked")){
                $17.alert("请勾选：我愿意花费200学豆报名");
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
                    //关闭
                    $("#promptclose").on("click",function(){
                        $.prompt.close();
                    });
                    //告诉父母
                    $("#tellparent").on("click",function(){
                        $.prompt.close();
                    });
                    //确认
                    $("#okBtn").on("click",function(){
                        $.post("/reward/order/exchangedcoupon.vpage", {
                            mobile : mobile,
                            smsCode : mobileCode.val(),
                            productName : "${(detail.productName)!}"
                        }, function(data){
                            if(data.success){
                                //统计-点击立即报名的点击数，其中已经绑定过的手机直接点击报名数和先绑定后点击报名数
                                $17.tongji('贝乐-报名成功');
                                if($17.isBlank(mobileCode.val())){
                                    YQ.voxLogs({module : 'coupon', op : 'beile_submit', userId : '${(currentUser.id)!''}'},'student');
                                }else{
                                    YQ.voxLogs({module : 'coupon', op : 'beile_bind_submit', userId : '${(currentUser.id)!''}'},'student');
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

        //获取验证码
        $('#getVerifyCode').on("click", function(){
            YQ.voxLogs({
                module: "coupon",
                op:"beiLe-getVerifyCode",
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

            if($this.hasClass("get-disable")){return false;}

            //统计-点击绑定手机号按钮的点击数
            YQ.voxLogs({module : 'coupon', op : 'beile_bind', userId : '${(currentUser.id)!''}'},'student');

            $this.addClass("get-disable");

            $.post(getMobilecodeUrl, { mobile : mobile}, function(data){
                if(!data.success){
                    $17.alert(data.info);
                    $this.removeClass("get_disable");
                }
                $17.getSMSVerifyCode($this, data);
            });
        });

        //去报名
        $("#enterForBtn").on("click",function(){
            $17.backToTop(500);
        });

        //查看奖励详情
        $("#rewarddetailbtn").on("click",function(){
            top.$('html, body').animate({scrollTop: $(".foot-info").offset().top}, 500);
        });
    });
</script>
</body>
</html>