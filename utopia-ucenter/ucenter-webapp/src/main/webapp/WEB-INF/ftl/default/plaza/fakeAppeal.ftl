<#import "../layout/webview.layout.ftl" as layout/>
<@layout.page
title="一起作业"
pageJs=[ "jquery"]
pageCssFile={"index" : ["/public/skin/teacherv3/css/faketeacher"]}>

<div id="page_hd">
    <div class="page_hd">

        <h1 class="logo">
            <a href="/">
                <img style="height:39px !important;" src="<@app.link href='public/skin/teacherv3/images/dean/logo-new.png'/>">
            </a>
        </h1>
        <ul class="user_nv">
            <li class="user_message">
                <a href="javascript:void(0);">
                    <i class="message_icon"></i>消息
                    <span class="v-msg-count message_count"></span>
                </a>
            </li>
            <li class="has_sub v-menu-hover">
                <span class="menu_click"><span class="">${(currentUser.profile.realname)!''}</span> <i class="arrow"></i></span>
                <ul class="sub_nav select" style="left: inherit; right: 0px; width: 83px;">
                    <li><a href="/teacher/center/index.vpage" class="" target="_blank">个人中心</a></li>
                    <li><a href="${(ProductConfig.getMainSiteBaseUrl())!''}/project/educationsubject/list.vpage" class="" target="_blank">课题相关</a></li>
                    <li><a href="${(ProductConfig.getMainSiteBaseUrl())!''}/help/kf/junior.vpage" class="" target="_blank">帮助与支持</a></li>
                    <li><a href="${(ProductConfig.getMainSiteUcenterLogoutUrl())!''}"  class="sign-out">退出</a></li>
                </ul>
            </li>
        </ul>
    </div>
</div>
<div id="page_bd" style="min-height: 680px; display: block;">
<#--左侧导航-->
    <div id="main_nav">
        <ul>
            <li class="nav_user">
                <a href="/teacher/center/index.vpage">
                    <img src="<@app.avatar href='${(currentUser.fetchImageUrl())!}'/>" alt="" class="nav_user_favicon">
                </a>
                <div class="nav_user_information">
                    <p class="nav_user_username">${(currentUser.profile.realname)!}</p>
                </div>
            </li>
            <li class="line"></li>
        </ul>
        <div>
            <div style="border-bottom: 1px solid #c3c5ca;">
                <a class="green-btn" href="javascript:void(0);" id="onlinecs_btn" style="margin: 20px 10px 20px 40px;">在线咨询</a>
            </div>
            <div>
                <p style="text-align: center;margin: 14px;">
                    免费咨询热线 <br/>
                    400-160-1717 <br/>
                    客服时间：8:00-21:00
                </p>
            </div>
        </div>
    </div>

<#--右侧主体-->
    <div id="page_body" style="min-height: 640px;">
        <div style="position:absolute; width: 812px; padding:50px 0;text-align: left;font-size:14px;color:#4d4d4d;background-color: #ffffff;" >
            <div>
                您当前的账号存在异常，为保护您的权益，已经您的账号暂时冻结，功能使用将会受到限制。<br/>
                请点击按钮填写申诉信息，我们会尽快核实并恢复误冻结账号。<br/><br/>
                如需帮助，可点击页面右侧在线咨询服务。
            </div>
            <div style="text-align: center;margin-top: 80px;">
                <a class="green-btn" href="javascript:void(0);" id="fake_apply_btn">点击申诉</a>
            </div>
        </div>
    </div>
</div>
<div class="m-footer">
    <div class="m-inner">
        <div class="m-left w-fl-left">
            <div class="copyright">
            ${(pageBlockContentGenerator.getPageBlockContentHtml('PlatformCopyright', 'webCopyright'))!''}
            </div>
            <div class="link">
                <a class="spare-icon spare-weibo" href="http://weibo.com/yiqizuoye" target="_blank" title="微博"></a>
            </div>
        </div>
        <div class="m-foot-link w-fl-right">
            <div class="m-left w-fl-left">
                <a href="${(ProductConfig.getMainSiteBaseUrl())!''}/help/aboutus.vpage" target="_blank">关于我们</a>
                <a href="${(ProductConfig.getMainSiteBaseUrl())!''}/help/uservoice.vpage" target="_blank">用户声音</a>
                <a href="${(ProductConfig.getMainSiteBaseUrl())!''}/help/kf/junior.vpage" target="_blank">帮助中心</a>
            </div>
            <div class="m-left w-fl-left">
                <a href="${(ProductConfig.getMainSiteBaseUrl())!''}/help/news/index.vpage" target="_blank">新闻中心</a>
                <a href="${(ProductConfig.getMainSiteBaseUrl())!''}/help/jobs.vpage" target="_blank">诚聘英才</a>
                <a href="${(ProductConfig.getMainSiteBaseUrl())!''}/project/educationsubject/list.vpage" target="_blank">课题相关</a>
                <!--<a href="http://help.17zuoye.com" target="_blank">帮助</a>-->
            </div>
        </div>
    </div>
</div>
<script>
    signRunScript = function () {
        $(document).on("click", function () {
            $(".menu_click").removeClass("active");
            $(".sub_nav").hide();
        }).on("click", ".menu_click", function (event) {
            event.stopPropagation();
            if ($(this).hasClass("active")) {
                $(this).removeClass("active").siblings(".sub_nav").hide();
            } else {
                $(this).addClass("active").siblings(".sub_nav").show();
            }
        }).on("click", "#fake_apply_btn", function () {
            var url = '${(ProductConfig.getMainSiteBaseUrl())!''}/ucenter/teacherfeedback.vpage',type = 'FAKE';
            window.open (type ? (url + "?" + $.param({type : type})) : url, 'feedbackwindow', 'height=500, width=700,top=200,left=450');
        }).on("click","#onlinecs_btn",function () {
            var onlinecsUrl = "${onlinecs}" + '&userId=${(currentUser!"")?string}';
            window.open(onlinecsUrl,'onlinecswindow','height=500, width=700,top=200,left=450');
        })
    };
</script>
</@layout.page>