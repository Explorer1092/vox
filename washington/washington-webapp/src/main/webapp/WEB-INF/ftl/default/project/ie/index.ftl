<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>一起作业，一起作业网，一起作业学生</title>
    <meta name="keywords" content="">
    <meta name="description" content="">
    <@sugar.capsule js=["jquery", "core"] css=[] />
    <script type="text/javascript">
        function myBrowser(){
            var userAgent = navigator.userAgent; //取得浏览器的userAgent字符串
            var isIE = userAgent.indexOf("compatible") > -1 && (userAgent.indexOf("MSIE 5.5") > -1 || userAgent.indexOf("MSIE 6.0") > -1 || userAgent.indexOf("MSIE 7.0") > -1 || userAgent.indexOf("MSIE 8.0") > -1); //判断是否IE浏览器

            if(isIE) {
                return true;
            }else{
                return false;
            }
        }

        if( (!myBrowser() && getQueryString('ref') != "open") || $17.getCookieWithDefault("goToKillIe")){
            window.location.href = "/index.vpage";
        }

        //获取App版本
        function getQueryString(name) {
            var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
            var r = window.location.search.substr(1).match(reg);
            if (r != null) return unescape(r[2]); return null;
        }
    </script>
    <@app.css href="public/ie/css/skin.css?1.0.2"/>
</head>
<body>
    <div class="upgradeBox">
        <div class="header">
            <a href="/login.vpage"><span class="logo"></span></a>
        </div>
        <div class="main">
            <div class="section01" style="height: 160px;">
                <div class="upgrade-pic"></div>
                <div class="upgrade-text">
                    <h3>请升级您的浏览器</h3>
                    <p>您现在使用的浏览器版本过低，为了带给您更安全、<br/>
                        高效的使用体验，请升级浏览器后使用<br/>
                        （Windows XP系统推荐使用火狐浏览器）</p>
                </div>
            </div>
            <div class="section02">
                <p class="title">您可以选择：</p>
                <ul>
                    <li>
                        <a href="//cdn.17zuoye.com/download/17zuoye_liebao_20150624.exe" target="_blank" class="v-downloadBrowser child-1" data-type="LieBao">
                            <i class="browser-icons browser-icons01"></i>
                            <p class="text">猎豹浏览器</p>
                        </a>
                    </li>
                    <li>
                        <a href="//download.firefox.com.cn/releases-sha2/stub/official/zh-CN/Firefox-latest.exe" target="_blank" class="v-downloadBrowser child-1" data-type="firefox">
                            <i class="browser-icons browser-icons03"></i>
                            <p class="text">火狐浏览器</p>
                        </a>
                    </li>
                    <li>
                        <a href="http://se.360.cn/" target="_blank" class="v-downloadBrowser" data-type="360">
                            <i class="browser-icons browser-icons04"></i>
                            <p class="text">360浏览器</p>
                        </a>
                    </li>
                    <li>
                        <a href="http://down.tech.sina.com.cn/page/40975.html" target="_blank" class="v-downloadBrowser" data-type="chrome">
                            <i class="browser-icons browser-icons02"></i>
                            <p class="text">谷歌浏览器</p>
                        </a>
                    </li>
                </ul>

                <#if (currentUser.userType == 3)!false>
                    <div class="tips tips-stu" >
                        <img src="<@app.link href="public/ie/images/code.png"/>" class="code">
                        <div class="text" style="margin-left: 115px; padding: 43px 0;"> <span class="font18">扫描二维码下载</span> 一起作业手机版<span class="textBlue">(支持Ios/Android)</span></div>
                    </div>
                <#else>
                    <div class="tips">
                        温馨提示：<br>
                        1.如果猎豹浏览器仍出现此页面，请在页面点击鼠标右键，左键选择“切换到极速模式”<br>
                        2.如果360浏览器仍出现此页面，请左键点击浏览器地址栏右侧绿色e“<i class="ie-icon"></i>标志，选择<br/>“极速模式(推荐)”
                    </div>
                </#if>
            </div>
            <!--[if gte IE 8]>
            <div style="clear: both; padding: 40px; text-align: center; position: absolute; right: -20px; bottom: -30px; line-height: 160%;">
                <a class="v-goToDownload" href="javascript:void(0);" style="color: #39f; font-size: 14px; text-decoration: underline;">
                暂不升级，进入首页>><br/>(部分页面不兼容)
                </a>
            </div>
            <![endif]-->
        </div>
    </div>

    <script type="text/javascript">
        $(function(){
            var $userType = ${(currentUser.userType)!0};
            var $userId = ${(currentUser.id)!0};
            $17.voxLog({
                userType : $userType,
                userId : $userId,
                module : "killIEDownloadPage",
                op : "load"
            });

            $(".v-downloadBrowser").on("click", function(){
                $17.voxLog({
                    userType : $userType,
                    userId : $userId,
                    module : "killIEDownloadPage",
                    browserType : $(this).attr("data-type"),
                    op : "click"
                });
            });

            $(".v-goToDownload").on("click", function(){
                $17.voxLog({
                    userType : $userType,
                    userId : $userId,
                    module : "killIEDownloadPage",
                    op : "goToDownload"
                });

                $17.setCookieOneDay("goToKillIe", "1", 1);

                setTimeout(function(){
                    location.href = "/index.vpage";
                }, 200);
            });
        });
    </script>
</body>
</html>