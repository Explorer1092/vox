<div id="page_hd">
    <div class="page_hd">

        <h1 class="logo">
            <!--已登录用户只能跳转至快乐学首页-->
            <a href="/">
                <img style="height:39px !important;" src="<@app.link href='public/skin/specialteacher/images/dean/logo-new.png'/>">
            </a>
        </h1>
        <ul class="user_nv">
        <#if currentUser.id?has_content>
            <#--<li class="user_message">-->
                <#--<a href="#messageCenter">-->
                    <#--<i class="message_icon"></i>消息-->
                    <#--<span class="v-msg-count message_count"></span>-->
                <#--</a>-->
            <#--</li>-->
            <li class="has_sub v-menu-hover">
                <span class="menu_click"><span class="">${(currentUser.profile.realname)!''}</span> <i class="arrow"></i></span>
                <ul class="sub_nav select" style="left: inherit; right: 0px; width: 83px;">
                    <li><a href="/specialteacher/center/teachercenter.vpage"  class="">个人中心</a></li>
                    <li><a href="${(ProductConfig.getMainSiteUcenterLogoutUrl())!''}"  class="sign-out">退出</a></li>
                </ul>
            </li>
        <#else>
            <li>
                <a href="" class="user_nv_reglogin">注册</a>
            </li>
            <li class="dotted"></li>
            <li>
                <a href="" class="user_nv_reglogin">登录</a>
            </li>
        </#if>
        </ul>
    </div>
</div>
<script type="text/javascript">
    signRunScript = function () {
        $(document).on("click",function () {
            $(".menu_click").removeClass("active");
            $(".sub_nav").hide();
        }).on("click",".menu_click",function (event) {
            event.stopPropagation();
            if ($(this).hasClass("active")){
                $(this).removeClass("active").siblings(".sub_nav").hide();
            }else{
                $(this).addClass("active").siblings(".sub_nav").show();
            }
        });
    }
</script>