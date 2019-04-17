<div class="m-header">
    <div class="m-inner">
        <div class="logo">
            <a href="/">
                <img src="<@app.link href='public/skin/adminteacher/images/common/logo-17.png'/>">
            </a>
        </div>
        <ul class="user_nv" style="display: block">
            <#if currentUser.id?has_content>
                <#if idType! == 'schoolmaster'>
                <li id="switchBtn"><a style="margin-right: 10px;color: rgba(0,0,0,0.65);cursor: pointer" href="/schoolmasterHomepage/index.vpage">切换看板</a></li>
                </#if>
                <li class="has_sub v-menu-hover">
                    <span class="menu_click active"><span class="">${(currentUser.profile.realname)!''}</span> <i class="arrow"></i></span>
                    <ul class="sub_nav select">
                        <li><a href="/${idType!'schoolmaster'}/admincenter.vpage">个人中心</a></li>
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