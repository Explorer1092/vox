<!DOCTYPE html>
<html>
<head>
    <meta name="viewport" content="width=640,initial-scale=0.5, maximum-scale=0.5">
    <title>${teacherName!}老师通知，请领取账号！</title>
    <@sugar.capsule js=["jquery", "core"]/>
    <@sugar.site_traffic_analyzer_begin />
    <style type="text/css">
        *{line-height: 100%;}
        html, body{ background-color: #f8f8f8;}
        html, body,p,ul,li, h1, h4, h5{ margin: 0; padding: 0; }
        body{ width: 640px; margin: 0 auto; font:26px/100% "微软雅黑", "Microsoft YaHei", arial; color: #333; height: 100%;}
        ul,li{ list-style: none;}
        h1, h4, h5{ font-weight: normal;}
        /*logo*/
        .logo{ background: url(<@app.link href="public/skin/teacherv3/images/logo.png"/>) no-repeat center center; width: 200px; height: 82px; clear: both; }
        .logo a{ width: 100%; height: 100%; display: block;}
        /*main*/
        .main {width: 540px; margin: 0 auto;}
        .main .header{  }
        .main .header h1{ font-size: 36px; padding: 30px 0; text-align: center;}
        .main .header p{ font-size: 26px; text-indent: 52px; line-height: 150%; margin-bottom: 50px;}
        .main .item { margin: 0 0 30px;}
        .main .item h4{ text-align: center; padding: 0 0 30px; font-size: 30px;}
        .main .item h5{ font-size: 26px; padding: 0 0 25px;}
        .main .item li{ float: left;  width: 50%; padding-bottom: 30px;}
        .pub-btn{text-decoration: none; background-color: #3c9dfc; color: #fff; font-size: 30px; padding: 20px 0; width: 220px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; border-radius: 5px; text-align: center;  display: block;  margin: 0 auto; }
        .pub-btn:hover{ background-color: #50a8ff;}
        /*formList*/
        .formList{ margin: 0 0 30px; clear: both;}
        .formList dl{}
        .formList dl dt{ font-size: 26px; line-height: 56px;}
        .formList dl dd{ margin: 0 0 15px 0;}
        .formList .w-int{ border-radius: 3px; border: 1px solid #ccc; padding: 15px; outline: none; font-size: 26px; width: 93%; font-family: "微软雅黑", "Microsoft YaHei", arial;}
        .formList .w-int:focus{ border-color: #3c9dfc;}
        .formList .btn{ padding-bottom: 20px;}
        .formList p{ font-size: 20px; text-align: center;}
        /*successItem*/
        .successItem{  background-color: #ddeff6; padding: 30px 0;}
        .successItem h4{ font-size: 30px; margin-top: 30px; text-align: center;}
        .successItem p{ font-size: 30px; padding: 10px; }
        .successItem .inLeft{ display: inline-block; width: 270px; text-align: right;}
        /*popup info*/
        .popup-info{ position: absolute; width: 100%; left: 0; top: 0; height: 100%;}
        .popup-info .back{ position: fixed; left: 0; top: 0; opacity: 0.9; filter: alpha(opacity=90);  background-color: #000; width: 100%; height: 100%; z-index: 20;}
        .popup-info .inner{ position: absolute; z-index: 21; width: 640px; margin: 0 0 0 -320px; left: 50%; top: 0; color: #fff;}
        .popup-info .inner { background: url(<@app.link href="public/skin/teacherv3/images/publicbanner/share-demo-img-v1.png"/>) no-repeat 250px 20px;  height: 400px;}
        .popup-info .inner .ic{ background-color: #468ab9; border-radius: 100px; padding: 4px 17px; display: inline-block; margin-right: 10px; }
        .popup-info .inner p{ font-size: 26px; padding: 20px 0 ; margin-left: 30px;}
        .popup-info .inner .btn{ padding: 20px 0 0 ;}
        /*error-info*/
        .error-info{ color: #f00; font-size: 20px; padding: 0; text-align: center; display: none; margin-bottom: 20px;}

        .popup-info-btn{ height: 110px; clear: both;}
        .popup-info-btn .inner-back{ position: fixed; bottom: 0; left: 0; background-color: #000; width: 100%; height: 110px; opacity: 0.8;filter: alpha(opacity=80); }
        .popup-info-btn .inner{ position: fixed; bottom: 0; left: 0; width: 100%; text-align: center;color: #fff; padding: 20px 0;}
    </style>
</head>
<body>
    <div class="logo"><a href="/"></a></div>
    <div class="main">
        <#--选择班级-->
        <div id="selectClass">
            <div class="header">
                <h1>${teacherName!}老师通知</h1>
                <p>各位家长，为提高学生学习兴趣和英语口语表达能力，拟试用国家教育课题项目“一起作业网”布置智能作业。</p>
            </div>
            <div class="item">
                <h4>请按班级领取学生账号</h4>
                <h5>我是：</h5>
                <ul>
                <#list clazzs as cl>
                    <li data-school-id=" ${cl.schoolId}" data-id=" ${cl.id}">
                        <a href="javascript:void(0);" class="pub-btn">${cl.classLevel}年${cl.className}</a>
                    </li>
                </#list>
                </ul>
                <div style="clear: both;"></div>
            </div>
        </div>
        <#--提交注册-->
        <div id="submitForm" style="display: none;">
            <div class="header">
                <h1 class="clazzName"><#--name--></h1>
            </div>
            <div class="formList">
                <p style="font-size: 30px;">提交信息，领取学生登录账号。</p>
                <dl>
                    <dt>学生姓名：</dt>
                    <dd><input type="text" value="" class="w-int" id="realNameInt" maxlength="4"/></dd>
                    <dt>设置学生登录密码：</dt>
                    <dd><input type="password" value="" class="w-int" id="passwordInt"/></dd>
                </dl>
                <div class="error-info"><#--错误提示--></div>
                <div class="btn">
                    <a href="javascript:void(0);" class="pub-btn" style="padding: 30px 0; font-size: 30px; width: 100%;">提交</a>
                </div>
                <div style="text-align: center; padding: 10px;" class="back"><a href="javascript:void(0);" style="color: #39f;">返回</a></div>
                <#--<p>领取后用电脑登录 <a href="http://www.17zuoye.com" target="_blank">www.17zuoye.com</a> 完成作业！</p>-->
            </div>
        </div>
        <#--注册成功-->
        <div id="successCallback"  style="display: none;">
            <div class="header">
                <h1 style="padding-bottom: 70px;">领取成功，赶紧记下来！</h1>
            </div>
            <div class="successItem">
                <p><span class="inLeft"><span class="successName"></span>的学号：</span><span id="successId">xxxxxx</span></p>
                <p><span class="inLeft"><span class="successName"></span>的密码：</span><span id="successPassword">xxxxxx</span></p>
                <h4 style="font-size: 24px;">请用电脑登录 <span id="successLogin"><a href="http://www.17zuoye.com" target="_blank">www.17zuoye.com</a></span> 完成作业！</h4>
            </div>
        </div>
    </div>

    <#--popup info-->
    <div class="popup-info" style="display: none;">
        <div class="back"></div>
        <div class="inner">
            <p style="margin-top: 88px; margin-bottom: 26px;"><span class="ic">1</span>老师请点击右上角</p>
            <p><span class="ic">2</span>然后选择</p>
        </div>
    </div>

    <div class="popup-info-btn" style="display: none;">
        <div class="inner-back"></div>
        <div class="inner">
            老师点击 <a href="javascript:void(0);" id="collectAccountBtn" class="pub-btn" style="background-color: #bad55e; display: inline-block; vertical-align: middle; font-size: 36px;">分享</a> 到家长群
        </div>
    </div>

    <script type="text/javascript">
        $(function(){
            var recordClassName = null;
            var templateId = {
                selectClass :  $("#selectClass"),
                submitForm :  $("#submitForm"),
                successCallback : $("#successCallback")
            };
            var recordData = {
                role  : 'ROLE_STUDENT',
                userType : 3,
                webSource : "psqqwechat",
                clazzId : null,
                password : null,
                realname : null
            };
            var browser={
                versions:function(){
                    var u = navigator.userAgent, app = navigator.appVersion;
                    return {//移动终端浏览器版本信息
                        ios: !!u.match(/\(i[^;]+;( U;)? CPU.+Mac OS X/), //ios终端
                        android: u.indexOf('Android') > -1 || u.indexOf('Linux') > -1, //android终端或者uc浏览器
                        iPhone: u.indexOf('iPhone') > -1 || u.indexOf('Mac') > -1, //是否为iPhone或者QQHD浏览器
                        iPad: u.indexOf('iPad') > -1, //是否iPad
                        webApp: u.indexOf('Safari') == -1 //是否web应该程序，没有头部与底部
                    };
                }(),
                language:(navigator.browserLanguage || navigator.language).toLowerCase()
            };

            if( browser.versions.android || browser.versions.iPhone || browser.versions.iPad){
                $(".popup-info-btn").show();
            }


            //加载进入统计
            $17.voxLog({
                module : "teacherShareWeixin",
                ext : navigator.appVersion,
                op : "load_share"
            });

            $("#collectAccountBtn").on("click", function(){
                $(".popup-info").show();

                //点击分享按钮
                $17.voxLog({
                    type : "teacherShareWeixin",
                    op : "click_share"
                });
            });

            $(".popup-info").on("click", function(){
                $(this).hide();
            });

            //选择班级
            templateId.selectClass.find(".item li").on("click", function(){
                var $this = $(this);

                recordClassName = $this.find("a").text();
                recordData.clazzId = $this.attr("data-id");

                $(".popup-info-btn").hide();
                templateId.selectClass.hide();
                templateId.submitForm.show();
                templateId.submitForm.find(".clazzName").text(recordClassName);

                //选择班级
                $17.voxLog({
                    module : "teacherShareWeixin",
                    op : "select_clazz"
                });
            });

            //返回
            templateId.submitForm.find(".back").on("click", function(){
                errorInfo();
                if( browser.versions.android || browser.versions.iPhone || browser.versions.iPad){
                    $(".popup-info-btn").show();
                }
                recordData.clazzId = null;
                templateId.selectClass.show();
                templateId.submitForm.hide();

                $17.voxLog({
                    module : "teacherShareWeixin",
                    op : "return_select_clazz"
                });
            });
            //提交注册
            templateId.submitForm.find(".btn a").on("click", function(){
                var $this = $(this);

                if($this.hasClass("dis")){
                    return false;
                }

                recordData.realname = $("#realNameInt").val().replace(/[ ]/g,"");
                recordData.password = $("#passwordInt").val();

                if( !$17.isCnString(recordData.realname) ){
                    errorInfo("请输入正确的姓名！");
                    return false;
                }

                if($17.isBlank(recordData.password)){
                    errorInfo("请输入密码！");
                    return false;
                }

                $this.addClass("dis");

                $.post("/signup/validatenp.vpage", {
                    clazzId : recordData.clazzId,
                    name    : recordData.realname,
                    pwd : recordData.password
                }, function(data){
                    if(data.success){
                        successCallBack(data.id);
                        $this.removeClass("dis");
                    }else{
                        App.postJSON('/signup/signup.vpage', recordData, function (data) {
                            if(data.success){
                                successCallBack(data.row);
                            }else{
                                var attrs = data.attributes;
                                if(attrs){
                                    for(var i in attrs){
                                        if(i != "dirty"){
                                            errorInfo(attrs[i]);
                                        }
                                    }
                                }else{
                                    errorInfo(data.info);
                                }
                            }
                            $this.removeClass("dis");
                        });
                    }
                });

                //成功返回
                function successCallBack(id){
                    templateId.submitForm.hide();
                    templateId.successCallback.show();
                    templateId.successCallback.find("#successLogin").html('<a href="http://www.17zuoye.com/?userId='+ id +'" target="_blank">www.17zuoye.com</a>')

                    $(".successName").text(recordData.realname);
                    $("#successId").text(id);
                    $("#successPassword").text(recordData.password);
                }
                $17.voxLog({                    module : "teacherShareWeixin",
                    op : "submit_reg"
                });
            });

            $(".w-int").on("keydown", function(){
                errorInfo();
            });

            function errorInfo(err){
                if(err){
                    $(".error-info").text(err).show();
                }else{
                    $(".error-info").slideUp();
                }
            }
        });
    </script>
    <@sugar.site_traffic_analyzer_end />
</body>
</html>
