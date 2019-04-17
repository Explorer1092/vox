<!DOCTYPE html>
<html>
<head>
    <#include "../nuwa/meta.ftl" />
    <title>一起作业，一起作业网，一起作业学生</title>
    <@sugar.capsule js=["jquery", "toolkit", "core", "alert"] css=["plugin.alert", "teacher.widget", "plugin.register"] />
    <@sugar.site_traffic_analyzer_begin />
    <style>
        .resetNav { padding-bottom: 50px; }
        .resetNav .reg_type { padding: 0;}
        .resetNav .reg_type li a { margin-bottom: 20px !important;}
        .resetNav .reg_type li a:hover, .resetNav .reg_type li a.active{ padding-bottom: 0 !important;}
        .resetNav .levelNav{ margin: 0 0 40px 94px; clear: both;}
        .resetNav .levelNav h4{ padding: 0 0 20px; font-size: 18px; color: #383a4c;}
        .resetNav .levelNav li{ float: left; padding-right: 80px; width: 210px;}
    </style>
</head>
<body>
    <#include "../layout/project.header.ftl"/>
    <div class="register_box resetNav">
        <h1 class="reg_title">找回账号密码</h1>
        <div class="reg_type">
            <h4>选择身份：</h4>
            <ul class="mainNav">
                <li class="teacher"><a href="javascript:void(0);" class="mytc v-forgetStaticLog" data-op="click-forget-teacher" title="老师"><strong>我是<span>老师</span></strong>请选择这里</a></li>
                <li class="parents"><a href="/ucenter/forgotPassword.vpage?userType=2" class="myst v-forgetStaticLog" data-op="click-forget-parents" data-title="忘记密码-家长" title="家长"><strong>我是<span>家长</span></strong>请选择这里</a></li>
                <li class="student"><a href="javascript:void(0);" class="mypt v-forgetStaticLog" data-op="click-forget-student" title="学生"><strong>我是<span>学生</span></strong>请选择这里</a></li>
            </ul>
        </div>
        <div class="levelNav" style=" display: none;">
            <h4><b></b>问题类型：</h4>
            <ul>
                <li><a href="/ucenter/forgotPassword.vpage?userType=1" class="reg_btn reg_btn_block" data-title="忘记密码-老师" >忘记密码</a></li>
                <li><a href="/ucenter/forgotaccount.vpage?url=forgotPassword&userType=1" class="reg_btn reg_btn_block" data-title="忘记账号-老师" >忘记账号</a></li>
                <li style="padding: 0;"><a href="/ucenter/forgotaccount.vpage?url=forgotPassword&userType=1&codeType=all" class="reg_btn reg_btn_block" data-title="忘记账号和密码-老师" >账号密码都忘了</a></li>
            </ul>
            <ul>
                <li></li>
            </ul>
            <ul>
                <li><a href="/ucenter/forgotPassword.vpage?userType=3" class="reg_btn reg_btn_block" data-title="忘记密码-学生" >忘记密码</a></li>
                <li><a href="/ucenter/forgotaccount.vpage?url=forgotPassword&userType=3" class="reg_btn reg_btn_block" data-title="忘记账号-学生" >忘记账号</a></li>
                <li style="padding: 0;"><a href="/ucenter/forgotaccount.vpage?url=forgotPassword&userType=3&codeType=all" class="reg_btn reg_btn_block" data-title="忘记账号和密码-学生" >账号密码都忘了</a></li>
            </ul>
            <div class="clear"></div>
        </div>
    </div>
    <script type="text/javascript">
        $(function(){
            var navLi = $(".resetNav .mainNav li");
            var navUl = $(".resetNav .levelNav");

            navLi.each(function(index){
                navLi.eq(index).on("click", function(){
                    navUl.find("h4 b").text($(this).find("a").attr("title"));
                    navUl.show();
                    navUl.find("ul").eq(index).show().siblings("ul").hide();
                    $(this).find("a").addClass("active").end().siblings("li").find("a").removeClass("active");
                });
            });

            $(".resetNav a[data-title]").on("click", function(){
                $17.tongji($(this).attr("data-title"));
            });
        });

        //忘记密码统计事件
        $(document).on("click", ".v-forgetStaticLog", function(){
            var $this = $(this);
            var $op = $this.attr("data-op");
            if( $op != ""){
                $17.voxLog({
                    app : "teacher",
                    module: "forgetStaticRefLog",
                    op: $op
                });
            }
        });
    </script>
    <#include "../layout/project.footer.ftl"/>
    <@sugar.site_traffic_analyzer_end />
</body>
</html>
