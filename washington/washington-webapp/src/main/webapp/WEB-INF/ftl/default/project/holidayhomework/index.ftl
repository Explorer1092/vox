<!doctype html>
<!--
<html>
<head>
<script type="text/javascript" src="/main.js">
</script>
<style>
</style>
</head>
<body>
</body>
</html>
-->
<html>
<head>
    <#include "../../nuwa/meta.ftl" />
    <title>一起作业，一起作业网，一起作业学生 </title>
    <@sugar.capsule js=["jquery"] css=["specialskin", "project.holidayhomework"] />
    <@sugar.site_traffic_analyzer_begin />
</head>
<body>
    <div id="specialHeader" data-val="student" data-toggle="hide"></div>
    <!--//start-->
    <div class="head">
        <div class="head_inner">
            <h1 class="logo"><a href="/"></a></h1>
            <div class="code-box" id="weiXinSideDetail">
                <div class="message">
                    <div class="con hide" data-title="学生">
                        <p>
                            <b>绑定微信</b><br>
                            随时随地关心孩子学习
                        </p>
                        <a class="btn_sub" href="javascript:void (0);">获取二维码</a>
                    </div>
                    <div class="code" data-title="老师">
                        <img src="<@app.link href="public/skin/project/holidayhomework/info.png"/>" width="132" height="132"/>
                    </div>
                </div>
                <div class="text">
                    <span data-title="老师">扫一扫，关注官方微信号</span>
                    <span class="hide" data-title="学生-key">扫一扫，关心孩子的学习</span>
                </div>
            </div>
        </div>
    </div>
    <div class="content">
        <div class="content_inner">
            <div class="content_left">
                <div class="homework_time">
                    <p class="title_yellow">
                        <span class="left arrow"></span>
                        <span class="center"><strong class="text_orange">暑假作业时间：</strong><strong data-title="老师">2014年6月15日—8月28日</strong><strong data-title="学生" class="hide">2014年7月1日—8月28日</strong> </span>
                        <span class="right arrow"></span>
                    </p>
                    <div class="title">
                        <h2 data-title="老师">暑假期间，请老师根据以上时间，安排暑假作业进度，并请注意以下三点事项：</h2>
                        <h2 data-title="学生" class="hide">暑假期间，请学生根据以上时间，做暑假作业，并请注意以下三点事项：</h2>
                    </div>
                    <ul>
                        <li>1、暑假作业到期后，学生不能补做；</li>
                        <li>2、暑假作业礼包，需经过老师检查后才能发放；</li>
                        <li>3、老师检查后的暑假作业，学生可以在学习中心最下方查看得分情况。</li>
                    </ul>
                </div>
                <div class="teacher_box">
                    <div class="teacher_up">
                        <span class="teacher_info right">注：学生作业未全部完成的，不能计入完成人数</span>
                        <p class="title_yellow">
                            <span class="left arrow"></span>
                            <span class="center"><strong class="text_orange">老师奖励：</strong> </span>
                            <span class="right arrow"></span>
                        </p>
                    </div>
                    <div class="teacher_down">
                        <div class="title_blue">
                            <span class="left arrow"></span>
                            <div class="center">
                                <div class="top">
                                    <span style="text-align: left; padding-left: 50px; width:147px">班级完成人数</span>
                                    <span style="text-align: left;">园丁豆奖励</span>
                                </div>
                                <div class="bottom">
                                    <span style="text-align: left; padding-left: 43px; width:147px">1—20人</span>
                                    <span style="text-align: left;">+N（N为完成人数）</span>
                                    <span style="text-align: left; padding-left: 34px; width:156px">20人以上</span>
                                    <span style="text-align: left; margin-left: -5px;">超过20人的部分获得双倍园丁豆</span>
                                </div>
                            </div>
                            <span class="right arrow"></span>
                        </div>
                    </div>
                </div>
            </div>
            <div class="content_right">
                <div class="share title_share" style="position: absolute;top: 60px; left:210px; width: 99%;">
                    <span class="left arrow"></span>
                    <div class="center">
                        <div class="jiathis">
                            <p class="line"></p>
                            <!-- JiaThis Button BEGIN -->
                            <div class="jiathis_style">
                                <span class="jiathis_txt">分享到：</span>
                                <a class="jiathis_button_qzone" title="QQ空间"></a>
                                <a class="jiathis_button_tsina" title="新浪微博"></a>
                                <a class="jiathis_button_tqq" title="腾讯微博"></a>
                                <a class="jiathis_button_renren" title="人人网"></a>
                                <a class="jiathis_button_weixin" title="微信"></a>
                                <a class="jiathis_button_xiaoyou" title="朋友网"></a>
                                <a href="http://www.jiathis.com/share?uid=1613716" class="jiathis jiathis_txt jiathis_separator jtico jtico_jiathis" target="_blank" title="更多"></a>
                            </div>
                            <script type="text/javascript" >
                                var jiathis_config={
                                    data_track_clickback:true,
                                    title: "#快乐假期，双倍奖励#",
                                    summary:"今年暑假流行啥？大家都在玩“一起作业”！登录网站做暑假作业，学习游戏两不误，还能和小伙伴PK赢园丁豆，爸爸妈妈再也不用担心我的学习！快来看看吧！",
                                    pic:"//cdn.17zuoye.com/static/project/student/holidayhomework_shareBanner.jpg",
                                    shortUrl:false,
                                    hideMore:false
                                }
                            </script>
                            <script type="text/javascript" src="http://v3.jiathis.com/code/jia.js?uid=1613716" charset="utf-8"></script>
                            <!-- JiaThis Button END -->
                        </div>
                    </div>
                    <span class="right arrow"></span>
                </div>
                <div class="teacher_box teacher_position"  style="position: relative;right:-26px; top: 24px">
                    <div class="teacher_up" style="margin: 232px 0px 0 0;">
                        <p class="title_yellow">
                            <span class="left arrow"></span>
                            <span class="center"><strong class="text_orange">学生奖励：</strong> </span>
                            <span class="right arrow"></span>
                        </p>
                    </div>
                    <div class="teacher_down" style="width: 444px;">
                        <div class="title_blue">
                            <span class="left arrow"></span>
                            <div class="center">
                                <div class="top">
                                    <span style="width: 170px">完成任务量</span>
                                    <span style="width: 170px">学豆奖励</span>
                                </div>
                                <div class="bottom">
                                    <span style="text-align: left; padding-left: 41px; width:125px">全部任务</span>
                                    <span style="text-align: left; padding-left: 50px; width:120px">+100</span>
                                    <span style="text-align: left; padding-left: 35px; width:135px">50%以上任务</span>
                                    <span style="text-align: left;  padding-left: 35px; width:135px">+50</span>
                                </div>
                            </div>
                            <span class="right arrow"></span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <!--end//-->
    <div id="footerPablic" class="footer_summer" data-type="1" ></div>
    <script src="//cdn.17zuoye.com/static/project/module/js/project-plug.js?1.0.1"></script>
    <script type="text/javascript">
        (function(){
            if(getQuery("type") == "student"){
                $("[data-title='学生']").removeClass("hide");
                $("[data-title='老师']").addClass("hide");


                var codeBox = $("#weiXinSideDetail");
                codeBox.find(".btn_sub").on("click", function(){
                    var $this = $(this);
                    var qrCodeUrl = "<@app.link href="public/skin/studentv3/images/2dbarcode.jpg"/>";

                    if($(this).hasClass("btn_sub_dis")){
                        return false;
                    }

                    $this.addClass("btn_sub_dis").html("获取中...");
                    $.get("/student/qrcode.vpage", function(data){
                        if(data.success){
                            qrCodeUrl = data.qrcode_url
                        }else{
                            if(data.info == "请返回首页重新登录"){
                                location.href = "/";
                            }
                        }
                        codeBox.find(".con").addClass("hide");
                        codeBox.find(".code").addClass("show").html("<img src='"+ qrCodeUrl +"' width='132' height='132'/>");
                        codeBox.find("[data-title='学生-key']").removeClass("hide");
                    });
                });
            }

            //获得地址栏参数
            function getQuery(item){
                var svalue = location.search.match(new RegExp('[\?\&]' + item + '=([^\&]*)(\&?)', 'i'));
                return svalue ? decodeURIComponent(svalue[1]) : '';
            }
        }());
    </script>
</body>
</html>
<!--
<html>
<head>
<script type="text/javascript" src="/main.js">
</script>
<style>
</style>
</head>
<body>
</body>
</html>