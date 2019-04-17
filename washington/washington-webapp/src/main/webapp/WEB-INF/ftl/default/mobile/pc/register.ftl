<!doctype html>
<html>
<head>
    <#include "../../nuwa/meta.ftl" />
    <title>一起作业，一起作业网，一起作业学生</title>
    <@sugar.capsule js=["jquery", "core"] css=["plugin.mobile"] />
    <@sugar.site_traffic_analyzer_begin />
    <script type="text/javascript">
        var browser={
            versions:function(){
                var u = navigator.userAgent, app = navigator.appVersion;
                return {//移动终端浏览器版本信息
                    trident: u.indexOf('Trident') > -1, //IE内核
                    presto: u.indexOf('Presto') > -1, //opera内核
                    webKit: u.indexOf('AppleWebKit') > -1, //苹果、谷歌内核
                    gecko: u.indexOf('Gecko') > -1 && u.indexOf('KHTML') == -1, //火狐内核
                    mobile: !!u.match(/AppleWebKit.*Mobile.*/)||!!u.match(/AppleWebKit/), //是否为移动终端
                    ios: !!u.match(/\(i[^;]+;( U;)? CPU.+Mac OS X/), //ios终端
                    android: u.indexOf('Android') > -1 || u.indexOf('Linux') > -1, //android终端或者uc浏览器
                    iPhone: u.indexOf('iPhone') > -1 || u.indexOf('Mac') > -1, //是否为iPhone或者QQHD浏览器
                    iPad: u.indexOf('iPad') > -1, //是否iPad
                    webApp: u.indexOf('Safari') == -1 //是否web应该程序，没有头部与底部
                };
            }(),
            language:(navigator.browserLanguage || navigator.language).toLowerCase()
        }

        if( browser.versions.android || browser.versions.iPhone || browser.versions.iPad){
            //移动设备
        }else{
            window.location.href = "/";
        }

        var browserType = navigator.appVersion;

        if(browser.versions.android){
            browserType = "android";
        }else if(browser.versions.iPhone){
            browserType = "iPhone";
        }else if(browser.versions.iPad){
            browserType = "iPad";
        }
    </script>
</head>
<body>
    <!--注册-->
    <div class="main" id="step-register">
        <!--logo-->
        <h1 class="logo"></h1>
        <#--menu-->
        <div class="menu-tab">
            <#--请选择你的身份-->
            <div class="subject-title">请选择你的身份</div>
            <ul>
                <li data-vox-tongji="WAP端-选择身份-学生">我是学生</li>
                <li data-vox-tongji="WAP端-选择身份-家长">我是家长</li>
                <li class="current" data-vox-tongji="WAP端-选择身份-老师">我是老师</li>
            </ul>
            <div class="service-tab">
                遇到问题，我们帮您！</br>
                免费客服电话：<@ftlmacro.hotline/>
            </div>
        </div>
        <!--//表单 start-->
        <div class="content-tab" style="display: none;">
            <div class="content-tab-box new_reg" style="display: none;">
                <p style="font-size: 38px;">同学你好</p>
                <p>登录学生账号请使用电脑访问</p>
                <p>一起作业的官方网站<a href="http://www.17zuoye.com"><strong>www.17zuoye.com</strong></a></p>
                <p>暂不支持移动端访问</p>
                <div class="service-tab">
                    遇到问题，我们帮您！</br>
                    免费客服电话：<@ftlmacro.hotline/>
                </div>
                <#--<div class="publicBtnBack">
                    <a href="javascript:void(0);" class="btn_mark btn_mark_green btn_mark_block " data-vox-tongji="WAP端-老师注册-返回">返回</a>
                </div>-->
            </div>
            <div class="content-tab-box new_reg" style="display: none;">
                <p style="font-size: 38px;">家长你好</p>
                <p>登录学生账号请使用电脑访问</p>
                <p>一起作业的官方网站<a href="http://www.17zuoye.com"><strong>www.17zuoye.com</strong></a></p>
                <p>暂不支持移动端访问</p>
                <p style="color: #333; text-align: center; margin-top: 30px; margin-bottom: 15px;">
                    想要随时了解孩子的作业情况，错题知识点<br/>请下载APP家长通
                </p>
                <div>
                    <a href="javascript:void(0);" class="btn_mark btn_mark_block" id="download-btn" data-vox-tongji="WAP端-家长-下载">立即下载</a>
                </div>
                <#--<dl>
                    <dt><img src="<@app.link href="public/skin/mobile/pc/images/icon-small.png"/>"></dt>
                    <dd>
                        <a href="javascript:void(0);" class="btn_mark btn_mark_block" id="download-btn" data-vox-tongji="WAP端-家长-下载">立即下载</a>
                    </dd>
                </dl>-->
                <div class="service-tab">
                    遇到问题，我们帮您！</br>
                    免费客服电话：<@ftlmacro.hotline/>
                </div>
                <#--<div class="publicBtnBack">
                    <a href="javascript:void(0);" class="btn_mark btn_mark_green btn_mark_block " data-vox-tongji="WAP端-老师注册-返回">返回</a>
                </div>-->
            </div>
            <div class="content-tab-box new_reg" style="display: none;">
                <p style="font-size: 38px;">老师你好</p>
                <p>如果您不是真实老师请不要注册老师账号</p>
                <p>注册后会对老师身份核实，电话回访。</p>
                <div style="padding: 20px 0 0;">
                    <div class="publicBtnReg" style="margin-bottom: 20px;">
                        <a href="javascript:void(0);" class="btn_mark  btn_mark_block " data-vox-tongji="WAP端-老师注册-我要注册老师账号">我要注册老师账号</a>
                    </div>
                    <div class="publicBtnBack">
                        <a href="javascript:void(0);" class="btn_mark btn_mark_green btn_mark_block " data-vox-tongji="WAP端-老师注册-不注册老师账号">不注册老师账号</a>
                    </div>
                </div>
                <div class="service-tab">
                    遇到问题，我们帮您！</br>
                    免费客服电话：<@ftlmacro.hotline/>
                </div>
            </div>
            <div class="form_main content-tab-box">
                <div class="subject-title">请选择注册老师账号</div>
                <h2>省份：</h2>
                <ul>
                    <li>
                        <select id="province" style="height: 65px;">
                            <option value="">请输入您所在省份</option>
                            <option value="110000">北京</option>
                            <option value="120000">天津</option>
                            <option value="130000">河北</option>
                            <option value="140000">山西</option>
                            <option value="150000">内蒙古</option>
                            <option value="210000">辽宁</option>
                            <option value="220000">吉林</option>
                            <option value="230000">黑龙江</option>
                            <option value="310000">上海</option>
                            <option value="320000">江苏</option>
                            <option value="330000">浙江</option>
                            <option value="340000">安徽</option>
                            <option value="350000">福建</option>
                            <option value="360000">江西</option>
                            <option value="370000">山东</option>
                            <option value="410000">河南</option>
                            <option value="420000">湖北</option>
                            <option value="430000">湖南</option>
                            <option value="440000">广东</option>
                            <option value="450000">广西</option>
                            <option value="460000">海南</option>
                            <option value="500000">重庆</option>
                            <option value="510000">四川</option>
                            <option value="520000">贵州</option>
                            <option value="530000">云南</option>
                            <option value="540000">西藏</option>
                            <option value="610000">陕西</option>
                            <option value="620000">甘肃</option>
                            <option value="630000">青海</option>
                            <option value="640000">宁夏</option>
                            <option value="650000">新疆</option>
                            <option value="710000">台湾</option>
                            <option value="810000">香港</option>
                            <option value="820000">澳门</option>
                        </select>
                    </li>
                </ul>
                <h2>姓名：</h2>
                <ul>
                    <li>
                        <input type="text" value="" id="username" placeholder="请输入您的姓名" />
                    </li>
                </ul>
                <h2>手机：</h2>
                <ul>
                    <li>
                        <input type="text" value="" id="mobile" placeholder="请输入您的手机号码" />
                    </li>
                    <li>
                        <a href="javascript:void(0);" class="btn_mark btn_mark_block" style="padding: 5px 0;" id="getCheckCodeBtn"><span>点击获取验证码</span></a>
                    </li>
                </ul>
                <h2>验证码：</h2>
                <ul>
                    <li>
                        <input type="text" value="" id="captchaCode" placeholder="请输入验证码" />
                        <#--<img id='captchaImage' style="display: inline-block; vertical-align: middle;"/>
                        看不清？<a href="javascript:void(0);" onclick="refreshCaptcha()">换一个</a>-->
                    </li>
                </ul>
                <div class="submit_box">
                    <a href="javascript:void(0);" id="submitBtn" class="btn_mark btn_mark_block" data-vox-tongji="WAP端-老师注册-注册">注册</a>
                </div>
            </div>
        </div>
        <!--表单 end//-->
    </div>

    <div class="main js-hide" id="step-success">
        <!--注册成功-->
        <div class="r-success">
            <div class="info"><#--<i class="icon icon_2"></i>--><span class="d-ibm">注册老师账号成功</span></div>
            <p class="text">账号和密码稍后将发送至您的手机<br/>请使用电脑访问官方网站<br/>www.17zuoye.com</p>
        </div>
    </div>
    <script type="text/javascript">
        $(function(){
            function afterValue(idx, info, num){
                num = num ? '' : '<i class="icon icon_1"></i>';
                if(!idx.hasClass("error")){
                    idx.addClass("error");
                    idx.after('<div class="info">'+ num +'<span class="d-ibm">'+ info +'</span></div>')
                }
            }

            $("#username, #mobile, #captchaCode").on("keyup", function(){
                var $this = $(this);

                $this.removeClass("error");
                $this.siblings(".info").remove();
                $("#submitBtn").removeClass("error").siblings(".info").remove();
            });

            $("#province").on("change", function(){
                var $this = $(this);

                $this.removeClass("error");
                $this.siblings(".info").remove();
                $("#submitBtn").removeClass("error").siblings(".info").remove();
            });

            $(".publicBtnBack").on("click", function(){
                $(".menu-tab").show();
                $(".content-tab").hide();
            });

            $(".publicBtnReg").on("click", function(){
                $(".content-tab-box").eq(2).hide();
                $(".content-tab-box").eq(3).show();
            });

            $("#download-btn").on("click", function(){
                $17.voxLog({
                    module : "mobile_reg",
                    op : "parent-downloadBtn"
                });

                setTimeout(function(){
                    window.location.href = "http://wx.17zuoye.com/jzt_app/download";
                }, 400);
            });

            $(".menu-tab li").on("click", function(){
                var $this = $(this);
                var $index = $this.prevAll().length;

//                $this.addClass("current").siblings().removeClass("current");
                $this.closest(".menu-tab").hide();
                $(".content-tab").show();
                $(".content-tab .content-tab-box").eq($index).show().siblings().hide();

                if($index == 2){
                    $17.voxLog({
                        module : "mobile_reg",
                        op : "parent-clickTab"
                    });
                }
            });

            $("#getCheckCodeBtn").on("click", function(){
                var $this = $(this);
                var mobileNumber = $("#mobile");

                if($this.hasClass("btn_disable")){
                    return false;
                }

                if( !$17.isMobile(mobileNumber.val()) ){
                    afterValue(mobileNumber, "请输入正确的手机号", "hide");
                    return false;
                }

                $.post("/signup/tmsignsvc-m.vpage", {mobile : mobileNumber.val(), cid: "${contextId}"}, function(data){
                    afterValue(mobileNumber, data.info, "hide");
                    $17.getSMSVerifyCode($this, data);
                });
            });

            $("#submitBtn").on("click", function(){
                var $this = $(this);
                if(!$this.hasClass("btn_mark_gray")){
                    var province = $("#province");
                    var username = $("#username");
                    var mobile = $("#mobile");
                    var captchaCode = $("#captchaCode");

                    if($17.isBlank(province.val())){
                        afterValue(province, "省份不能为空");
                        return false;
                    }
                    var usernameValue = username.val().replace(/\s+/g, "");

                    if($17.isBlank(username.val()) || (usernameValue.match(/[^\u4e00-\u9fa5]/g)) ){
                        afterValue(username, "姓名不能为空，必须为中文");
                        return false;
                    }

                    if($17.isBlank(mobile.val()) || !$17.isMobile(mobile.val())){
                        afterValue(mobile, "请输入正确的手机号");
                        return false;
                    }

                    if( $17.isBlank(captchaCode.val()) ){
                        afterValue(captchaCode, "请输入正确的验证码");
                        return false;
                    }

                    if($17.isBlank(mobile.val()) || !$17.isMobile(mobile.val()) || $17.isBlank(username.val()) || $17.isBlank(province.val()) ){
                        return false;
                    }

                    $this.text("提交中...").addClass("btn_mark_gray");

                    $.post("/signup/mobile/teacherregister.vpage", {
                        province : province.val(),
                        userName : username.val(),
                        mobile  : mobile.val(),
                        code  : captchaCode.val()
                    }, function(data){
                        if(data.success){
                            $("#step-register").hide();
                            $("#step-success").show();
                            $17.voxLog({
                                "module" : "mobile_reg",
                                "op" : "success",
                                device: browserType
                            });

                            $17.tongji('WAP端-老师注册-注册成功');
                        }else{
                            afterValue($this, data.info);
                            $this.text("注册").removeClass("btn_mark_gray");

                            if(data.info.indexOf("验证码输入错误") >= 0){
                                captchaCode.val("");
                            }
                        }
                    });
                }
            });

            $17.voxLog({
                "module" : "mobile_reg",
                "op" : "load",
                device: browserType
            });

            $("[data-vox-tongji]").on("click", function(){
                $17.tongji($(this).data("vox-tongji"));
            });
        });
    </script>
    <@sugar.site_traffic_analyzer_end />
</body>
</html>
