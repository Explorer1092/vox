<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta http-equiv="Cache-Control" content="no-siteapp" />
<meta http-equiv="X-UA-Compatible" content="IE=Edge, chrome=1"/>
<meta name="keywords" content="${keywords!'17作业，作业，一起作业下载，一起作业学生，学生APP，学生端下载，在线教育平台'}">
<meta name="description" content="<#if description??><#if description?length gt 200>${description?substring(0,200)}...<#else>${ description!}</#if><#else>一起作业网是一个学生、老师和家长三方互动的作业平台，老师轻松布置作业，学生快乐做作业，家长可以定期查看孩子的学习进度及报告，情景交融的学习模式，让孩子轻松搞定各科学习！一起作业，让学习成为美好体验。</#if>">
<meta property="qc:admins" content="2501263117617257156375" />
<link rel="shortcut icon" href="/favicon.ico" type="image/x-icon" />
<script type="text/javascript">
    (function(){
        var tid = setInterval(function(){
            if(typeof($) == "function" && typeof($17) == "function"){
                var uid = "${(currentUser.id)!''}",cUid = $.cookie("uid");
            <#--如果当前页面有登录用户，但是与cookie记录的登录用户不一样，则跳转-->
                if(uid && uid != cUid) {
                    clearInterval(tid);
                    var goHome = function(){ location.href="/"; return false; };
                    $17.alert("您同时用了两个不同账号登录，请刷新页面", goHome, goHome);
                }
            }
        }, 5000);
    })();
</script>