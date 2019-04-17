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
    <title>我爱一年级 - 一起作业为一年级节目点赞活动</title>
    <@sugar.capsule js=["jquery", "core", "alert", "template", "DD_belatedPNG"] css=["plugin.alert", "new_student.widget", "specialskin","firstgrade"] />
    <@sugar.site_traffic_analyzer_begin />
</head>
<body>
    <div class="head">
        <div class="inner">
            <div class="logo"><a href="/"></a></div>
            <div class="num">
                <p class="count"><span class="yellow" id="supportCount"><#--${count!0}-->6765326</span><span class="person">次</span></p>
            </div>
            <a class="play-btn" onclick="$17.tongji('我爱一年级-视频播放')" href="http://www.hunantv.com/v/1/56649/" target="_blank"></a>
            <p class="btn "><a id="support_but" class="spot-btn spot-btn-s PNG_24" href="javascript:void(0);"></a></p>
        </div>
    </div>
    <div class="slide">
        <div class="inner">
            <div class="share">
                <div class="share-inner" id="showShareBox">

                </div>
            </div>
        </div>
    </div>
    <div class="content">
        <div class="inner">
            <div class="support support5">
                <div class="support-inner support_single_but" data-name="AQE" data-cname="安淇尔">
                    <p>元气苹果妹，<span class="f_count"><#--${AQE!0}-->432829</span>支持</p>
                    <a class="spot-btn spot-btn-l" href="javascript:void(0);"></a>
                </div>
            </div>
            <div class="support support4">
                <div class="support-inner support_single_but" data-name="XMZ" data-cname="西蒙子">
                    <p>混血小满哥,<br><span class="f_count"><#--${XMZ!0}-->199649</span>支持</p>
                    <a class="spot-btn spot-btn-l" href="javascript:void(0);"></a>
                </div>
            </div>
            <div class="support support6">
                <div class="support-inner support_single_but" data-name="LHY" data-cname="李昊煜">
                    <p>鬼马小精灵，<span class="f_count"><#--${LHY!0}-->481761</span>支持</p>
                    <a class="spot-btn spot-btn-l" href="javascript:void(0);"></a>
                </div>
            </div>
            <div class="support support2">
                <div class="support-inner support_single_but" data-name="LYL" data-cname="陆煜琳">
                    <p>霸气小陆总，<span class="f_count"><#--${LYL!0}-->70995</span>支持</p>
                    <a class="spot-btn spot-btn-l" href="javascript:void(0);"></a>
                </div>
            </div>
            <div class="support support3">
                <div class="support-inner support_single_but" data-name="CSC" data-cname="陈思成">
                    <p>韩范小暖男，<span class="f_count"><#--${CSC!0}-->212674</span>支持</p>
                    <a class="spot-btn spot-btn-l" href="javascript:void(0);"></a>
                </div>
            </div>
            <div class="support support1">
                <div class="support-inner support_single_but" data-name="WZX" data-cname="王梓璇">
                    <p>发箍小公主，<span class="f_count"><#--${WZX!0}-->668638</span>支持</p>
                    <a class="spot-btn spot-btn-l" href="javascript:void(0);"></a>
                </div>
            </div>
            <div class="support support7">
                <div class="support-inner support_single_but" data-name="MHX" data-cname="马皓轩">
                    <p>倔强小王子，<span class="f_count"><#--${MHX!0}-->200521</span>支持</p>
                    <a class="spot-btn spot-btn-l" href="javascript:void(0);"></a>
                </div>
            </div>
        </div>
    </div>
    <div class="footer-class">
        <div class="inner">
            <div class="tip-num">目前共有 <span id="messageCount"><#--${messageCount!0}-->55680</span>条留言</div>
            <div class="messages-box">
                <div class="messages-box-inner">
                    <div class="meg-up">
                        <textarea id="send_content_box" disabled maxlength="140" placeholder="活动已结束,谢谢你的参与。"></textarea>
                        <p> <span id="word_limit_box">还能输入140个字</span> <a class="disabled" id="message_save_but" href="javascript:void(0);"></a></p>
                    </div>
                    <div class="meg-down" id="messageListBox">

                    </div>
                </div>
            </div>
        </div>
    </div>
    <!--end//-->
    <div id="footerPablic"></div>

    <script id="t:shareBox" type="text/html">
        <div class="center">
            <div class="jiathis">
                <p class="line"></p>
                <!-- JiaThis Button BEGIN -->
                <div class="jiathis_style">
                    <span class="jiathis_txt">分享到：</span>
                    <a class="jiathis_button_qzone" title="QQ空间"></a>
                    <a class="jiathis_button_tsina" title="新浪微博"></a>
                    <a class="jiathis_button_weixin" title="微信"></a>
                    <a href="http://www.jiathis.com/share?uid=1613716" class="jiathis jiathis_txt jiathis_separator jtico jtico_jiathis" target="_blank" title="更多"></a>
                </div>
                <!-- JiaThis Button END -->
            </div>
        </div>
        <div style="clear: both;"></div>
        <script type="text/javascript" src="http://v3.jiathis.com/code/jia.js?uid=1613716" charset="utf-8"></script>
    </script>

    <script src="//cdn.17zuoye.com/static/project/module/js/project-plug.js?1.0.1"></script>

    <script id="t:留言板内容" type="text/html">
        <%if (data.length > 0){%>
            <ul>
                <%for(var i = 0; i < data.length; i++ ){%>
                    <li>
                        <p>
                            <span><%=data[i].userSchool%></span>
                            <span><%=data[i].userName%></span>
                            <span class="time"><%=data[i].commentTime%></span>
                        </p>
                        <p><%=data[i].content%></p>
                    </li>
                <%}%>
            </ul>
            <div class="page_btn clear">
                <span class="page_box">
                    <a class="prev">上一页</a><span class="num"><span class="current_page">1</span><span style="padding:0 3px;">/</span><span class="total"></span></span><a class="next">下一页</a>
                </span>
            </div>
        <%}else{%>
            <div>暂无留言</div>
        <%}%>
    </script>
    <script type="text/javascript">
        var imgUrl = 'http://cdn-cc.17zuoye.com/public/skin/project/firstgrade/images/logo.png';
        var lineLink = 'http://www.17zuoye.com/project/firstgrade/index.vpage';
        var descContent = "一起作业网与湖南卫视联合推出，快来为你喜欢的童星投票";
        var shareTitle = '全国百万小学生为《一年级》节目点赞';
        var appid = '';

        function shareFriend() {
            $17.tongji("我爱一年级-微信发送好友");
            WeixinJSBridge.invoke('sendAppMessage',{
                "appid": appid,
                "img_url": imgUrl,
                "img_width": "200",
                "img_height": "200",
                "link": lineLink,
                "desc": descContent,
                "title": shareTitle
            }, function(res) {
                //_report('send_msg', res.err_msg);
            })
        }
        function shareTimeline() {
            $17.tongji("我爱一年级-微信分享朋友圈");
            WeixinJSBridge.invoke('shareTimeline',{
                "img_url": imgUrl,
                "img_width": "200",
                "img_height": "200",
                "link": lineLink,
                "desc": descContent,
                "title": shareTitle
            }, function(res) {
                //_report('timeline', res.err_msg);
            });
        }
        function shareWeibo() {
            $17.tongji("我爱一年级-微信分享微博");
            WeixinJSBridge.invoke('shareWeibo',{
                "content": descContent,
                "url": lineLink
            }, function(res) {
                //_report('weibo', res.err_msg);
            });
        }
        function sendShare(){
            // 发送给好友
            WeixinJSBridge.on('menu:share:appmessage', function(argv){
                shareFriend();
            });
            // 分享到朋友圈
            WeixinJSBridge.on('menu:share:timeline', function(argv){
                shareTimeline();
            });
            // 分享到微博
            WeixinJSBridge.on('menu:share:weibo', function(argv){
                shareWeibo();
            });
        }

        // 当微信内置浏览器完成内部初始化后会触发WeixinJSBridgeReady事件。
        if(navigator.appName.indexOf("Microsoft") > -1){
            document.attachEvent('WeixinJSBridgeReady', function onBridgeReady() {
                sendShare();
            }, false);
        }else{
            document.addEventListener('WeixinJSBridgeReady', function onBridgeReady() {
                sendShare();
            }, false);
        }
    </script>

    <script type="text/javascript">
        var jiathis_config = {
            data_track_clickback:true,
            title: "我爱一年级",
            summary:"一起作业网与湖南卫视携手，邀请全国百万小学生为《一年级》节目点赞、为喜欢的小明星投票，点赞还能得学豆呢！我已经参与了，你也快来吧！",
            pic:"//cdn.17zuoye.com/static/project/student/classSpBanner.jpg",
            shortUrl:false,
            hideMore:false
        };

        function mustBeLogin(){
            $17.alert('<div style="line-height: 30px;">点赞需要登录一起作业学生账号才可以哦！点击此处 <a href="/login.vpage" style="color:#12BCED;">立刻登录</a> <br /> 还不是一起作业的学生用户？点击此处<a href="/signup/htmlchip/student.vpage" style="color:#12BCED;">立刻注册</a></div>');
        }

        function refreshPage(){
            $("#messageListBox ul li:gt(4)").hide();
            var total_q = $("#messageListBox ul li").index() + 1;
            var current_page = 5;
            var current_num = 1;
            var total_page = Math.ceil(total_q/current_page);
            var next = $("#messageListBox .next");
            var prev = $("#messageListBox .prev");
            $(".total").text(total_page);
            $(".current_page").text(current_num);

            //下一页
            $("#messageListBox .next").click(function(){
                if(current_num == total_page){
                    return false;
                }
                else{
                    $(".current_page").text(++current_num);
                    $.each($('#messageListBox ul li'),function(index,item){
                        var start = current_page * (current_num-1);
                        var end = current_page * current_num;
                        if(index >= start && index < end){
                            $(this).show();
                        }else {
                            $(this).hide();
                        }
                    });
                }
            });
            //上一页
            $("#messageListBox .prev").click(function(){
                if(current_num == 1){
                    return false;
                }else{
                    $(".current_page").text(--current_num);
                    $.each($('#messageListBox ul li'),function(index){
                        var start = current_page* (current_num-1);
                        var end = current_page * current_num;
                        if(index >= start && index < end){
                            $(this).show();
                        }else {
                            $(this).hide();
                        }
                    });
                }
            });
        }

        $(function(){

            //我要点赞
            $("#support_but").on('click',function(){
                $17.alert("活动已结束,谢谢你的参与。");
                return false;

                var countBox = $("#supportCount");
                var $this = $(this);
                if($this.hasClass('loading')){return false}
                $this.addClass('loading');
                $.get('/project/firstGrade/lfg.vpage',function(data){
                    if(data.success){
                        countBox.text(countBox.text() * 1 + 1);
                        $.prompt('<div style="line-height: 25px;">'+data.info+' <br /><br /> '+template("t:shareBox",{})+'</div>', {
                            title: "系统提示",
                            position:{width : 500},
                            loaded : function(){
                                jiathis_config.summary = "一起作业网与湖南卫视携手，邀请全国百万小学生为《一年级》节目点赞、为喜欢的小明星投票，点赞还能得学豆呢！我已经参与了，你也快来吧！";

                            }
                        });
                        $17.tongji("我爱一年级-我要点赞");
                    }else{
                        if(data.code == 'NEED_LOGIN'){
                            mustBeLogin();
                            $17.tongji("我爱一年级-我要点赞-未登录");
                        }else{
                            $.prompt(data.info, {
                                title: "系统提示",
                                buttons : {},
                                position:{width : 500}
                            });
                        }
                    }
                    $this.removeClass('loading');
                });
            });

            //给学生点赞
            $(".support_single_but").on('click',function(e){
                $17.alert("活动已结束,谢谢你的参与。");
                return false;

                var $this = $(this);
                if ($(e.target).closest("p").length == 1) {
                    return false;
                }
                var countBox = $this.children().find('span.f_count');
                var name = $this.data('name');
                var cName = $this.data('cname');

                if($this.hasClass('loading')){return false}
                $this.addClass('loading');
                $.get('/project/firstGrade/lfgc.vpage?name='+name,function(data){
                    if(data.success){
                        $.prompt('<div style="line-height: 25px;">'+data.info+' <br /><br /> '+template("t:shareBox",{})+'</div>', {
                            title: "系统提示",
                            position:{width : 500},
                            loaded : function(){
                                jiathis_config.summary = "一起作业网与湖南卫视携手，邀请全国百万小学生为《一年级》节目点赞、为喜欢的小明星投票，我投票给了"+cName+"，你也快来吧！";
                            }
                        });
                        countBox.text(countBox.text() * 1 + 1);
                        $17.tongji("我爱一年级-童星点赞");

                    }else{
                        if(data.code == 'NEED_LOGIN'){
                            mustBeLogin();
                            $17.tongji("我爱一年级-童星点赞-未登录");
                        }else{
                            $.prompt(data.info, {
                                title: "系统提示",
                                buttons : {},
                                position:{width : 500}
                            });
                        }
                    }
                    $this.removeClass('loading');
                });
            });

            /*留言*/
            $("#send_content_box").on("keyup", function () {
                $("#word_limit_box").html($17.wordLengthLimit($(this).val().length,140));
            });

            //获取留言内容
            $("#messageListBox").html('<div style="padding: 50px 0; text-align: center;"><img src="<@app.link href="public/app/default/images/loadding.gif"/>" alt="加载中..." /> 数据加载中…</div>');
            $.get('/project/firstGrade/fgcomments.vpage',function(data){
                $("#messageListBox").html(template("t:留言板内容", {
                    data : data.comments
                }));
                oldDate = data.comments;
                refreshPage();
            });

            /*留言提交*/
            var oldDate = {};
            $("#message_save_but").on("click", function () {
                $17.alert("活动已结束,谢谢你的参与。");
                return false;
                var commentContent = $("#send_content_box").val();
                if(commentContent.length == 0 || commentContent.length > 140){
                    $17.alert("填写正确的留言内容");
                    return false;
                }

                $.post('/project/firstGrade/fglm.vpage',{content : commentContent},function(data){
                    if(data.success){
                        /*更新留言总数*/
                        var messageCount = $("#messageCount");
                        messageCount.text(messageCount.text() * 1 + 1);

                        var currentDate = [{
                            commentTime : $17.DateUtils("%Y-%M-%d %h:%m:%s",1,new Date().getTime()) ,
                            userName : '${(currentUser.profile.realname)!'匿名'}',
                            userSchool : "<#if currentUser?? && (currentUser.userType == 3) >${(currentStudentDetail.studentSchoolName)!}</#if>  " ,
                            content : commentContent
                        }];

                        //把刚输入成功的留言添加到Json中去
                        oldDate = currentDate.concat(oldDate);

                        $("#messageListBox").html(template("t:留言板内容", {
                            data : oldDate
                        }));

                        refreshPage();

                        $("#send_content_box").val('');
                        $("#word_limit_box").html($17.wordLengthLimit(0,140));
                        $17.tongji("我爱一年级-发表留言");
                    }else{
                        $17.alert(data.info);
                    }
                });
            });
            $("#showShareBox").html(template("t:shareBox",{}));
        });
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