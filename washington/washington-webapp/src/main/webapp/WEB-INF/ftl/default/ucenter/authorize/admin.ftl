<!DOCTYPE html>
<html lang="en">
<head>
    <title>17zuoye Manage System</title>
    <#include "../../nuwa/meta.ftl" />

    <script src="http://libs.baidu.com/jquery/1.9.0/jquery.js"></script>
    <script src="http://libs.baidu.com/bootstrap/2.0.4/js/bootstrap.min.js"></script>
    <link href="http://libs.baidu.com/bootstrap/2.0.4/css/bootstrap.min.css" rel="stylesheet">

    <!-- Fav and touch icons -->
    <style type="text/css">
        body { padding-top: 60px; padding-bottom: 40px; font-family:  'Helvetica Neue',Helvetica,Arial,'Microsoft Yahei UI','Microsoft YaHei',SimHei,'宋体',simsun,sans-serif;}
        .sidebar-nav { padding: 9px 0; }
        @media (max-width: 980px) {
            /* Enable use of floated navbar text */
            .navbar-text.pull-right { float: none; padding-left: 5px; padding-right: 5px; }
        }
        body { padding-top: 40px; padding-bottom: 40px; background-color: #f5f5f5; }
        .form-signin { max-width: 300px; padding: 19px 29px 29px; margin: 0 auto 20px; background-color: #fff; border: 1px solid #e5e5e5; -webkit-border-radius: 5px; -moz-border-radius: 5px; border-radius: 5px; -webkit-box-shadow: 0 1px 2px rgba(0,0,0,.05); -moz-box-shadow: 0 1px 2px rgba(0,0,0,.05); box-shadow: 0 1px 2px rgba(0,0,0,.05); }
        .form-signin .form-signin-heading,  .form-signin .checkbox { margin-bottom: 10px; }
        .form-signin input[type="text"],  .form-signin input[type="password"] { font-size: 16px; height: auto; margin-bottom: 15px; padding: 7px 9px; }
    </style>
</head>

<body>
    <div class="container">
        <div class="form-signin text-center">
            <h2 class="form-signin-heading">17zuoye </h2>
            <p><span class="label label-success">已认证：</span></p>
            <hr/>
            <p><a href= "/ucenter/math.vpage" class="btn btn-large btn-info btn-block" target="_blank">小学数学</a></p>
            <p><a href= "/ucenter/exam.vpage" class="btn btn-large btn-success btn-block" target="_blank">小学英语</a></p>
            <#if adminFlag?? && adminFlag>
            <p><a href="/ucenter/partner.vpage?url=${ProductConfig.getRewardSiteBaseUrl()}" class="btn btn-large btn-danger btn-block" target="_blank">积分商城</a></p>
            </#if>
            <hr/>
            <p><a href="/ucenter/logout.vpage" class="btn">退出</a></p>
        </div>
    </div>
</body>
</html>
