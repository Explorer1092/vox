<#import "../layout/webview.layout.ftl" as layout/>
<@layout.page
title="一起作业-教务老师"
pageJs=["jquery"]
pageCssFile={"index" : ["/public/skin/specialteacher/css/skin"]}>

    <#include "header.ftl">
<div id="page_bd" style="min-height: 680px;" style="display: none;">
    <div id="main_nav">
        <ul>
            <li class="nav_user">
            <#--<a href="//ucenter.17zuoye.com/teacher/center/index.vpage" target="_blank">-->
                <img src="<@app.avatar href='${(currentUser.fetchImageUrl())!}'/>" alt="" class="nav_user_favicon">
            <#--</a>-->
                <div class="nav_user_information">
                    <p class="nav_user_username">
                    ${(currentUser.profile.realname)!}
                    </p>
                <#--<a href="//ucenter.17zuoye.com/teacher/center/index.vpage#/teacher/center/myauthenticate.vpage">-->
                    <i class="nav_user_no_certified"></i>
                <#--</a>-->
                </div>
            </li>
            <li class="line"></li>

            <li class=" nav_plan">
                <a href="/specialteacher/clazzmanage.vpage"><i class="icon"></i>班级管理<i class="arrow"></i></a>
            </li>
            <li class=" nav_school_bank">
                <a href="/specialteacher/admin/index.vpage"><i class="icon"></i>老师学生管理<i class="arrow"></i></a>
            </li>
            <li class=" nav_my_favorites">
                <#--<a href="${(ProductConfig.getUcenterUrl())!''}/teacher/center/index.vpage" target="_blank"><i class="icon "></i>个人中心<i class="arrow"></i></a>-->
                <a href="/specialteacher/center/teachercenter.vpage"><i class="icon "></i>个人中心<i class="arrow"></i></a>
            </li>
        </ul>
    </div>
    <div id="page_body" style="min-height: 640px;">
        <div id="t_index_welcome">
            <i class="time_icon JS-timeIcon"></i><span class="JS-dateStr"></span>，${(currentUser.profile.realname)!}
        </div>
        <ul id="t_index_msg">
            <li>暂时没有新消息哦&nbsp;^_^</li>
        </ul>
    </div>
</div>

<script>
    function checkoutData() {
        var dateHour = new Date().getHours();
        var _icon = document.getElementsByClassName('JS-dateStr')[0];
        var _dateStr = document.getElementsByClassName('JS-timeIcon')[0];
        if (dateHour >= 6 && dateHour <= 12) { // 06 ~ 12 上午
            _icon.innerText = '上午好';
            _dateStr.className = "time_icon JS-timeIcon day";
        } else if (dateHour > 12 && dateHour <=18) { // 12 ~ 18
            _icon.innerText = '下午好';
            _dateStr.className = "time_icon JS-timeIcon day";
        } else { // 18 ~ 05
            _icon.innerText = '晚上好';
            _dateStr.className = "time_icon JS-timeIcon night";
        }
    }
    checkoutData();
</script>

    <#include "footer.ftl">
</@layout.page>