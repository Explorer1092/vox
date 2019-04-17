<!DOCTYPE html>
<html>
<head>
    <meta content="IE=edge" http-equiv="X-UA-Compatible">
    <meta charset="utf-8">
    <title>首页</title>
    <meta name="Keywords" content="">
    <meta name="Description" content="">
</head>
<body>
<button id="login">去登录</button>
<script type="text/javascript" src="https://libs.17zuoye.cn/jquery/1.12.4/jquery.min.js"></script>
<script type="text/javascript" src="https://libs.17zuoye.cn/json2.min.js"></script>
<script type="text/javascript"
        src="https://livecdn.17zuoye.cn/teacher/page/0.0.14/javascripts/manage/zylive.client2.js"></script>
<script type="text/javascript">
    $('#login').click(function () {
        $.post("/babyeagle/teacher/page/courselisturl.vpage", {}, function (data) {
            if (data.success) {
                var courseListUrl = data.courseListUrl;
                try {
                    zylive.client.api.loginNotify({
                        code: 0,
                        data: {
                            nick: '老师',
                            logo: 'http://tva3.sinaimg.cn/crop.0.0.180.180.180/4b29679ejw1e8qgp5bmzyj2050050aa8.jpg',
                            courseListUrl: courseListUrl,
                            helpUrl: 'https://livecdn.17zuoye.cn/help/index02.html?v=1',
                            latestVersionGetUrl: 'https://livecdn.17zuoye.cn/teacher/client/update.xml?v=1',
                            errorDetailUrl: 'https://livecdn.17zuoye.cn/help/index02.html?v=1'
                        }
                    }, function (err) {
                        if (err) {
                            alert("err1:" + err);
                        }
                    });
                } catch (e) {
                    alert("err2:" + e);
                    location.href = 'list.html';
                }
            } else {
                alert(data.info);
            }
        });
    });

</script>
</body>
</html>