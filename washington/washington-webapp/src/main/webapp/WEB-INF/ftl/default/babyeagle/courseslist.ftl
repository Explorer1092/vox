<#import "../layout/webview.layout.ftl" as layout/>
<@layout.page fastClickFlag=false pageJs=['jquery'] >
<script type="text/javascript" src="https://libs.17zuoye.cn/jquery/1.12.4/jquery.min.js"></script>
<script type="text/javascript" src="https://libs.17zuoye.cn/blueimp-md5/2.7.0/js/md5.min.js"></script>
<script type="text/javascript" src="https://livecdn.17zuoye.cn/teacher/page/0.0.14/javascripts/manage/zylive.client2.js"></script>
<div>
    <#if loginFlag!false>
        <div class="table_soll">
            <table width="100%" border="1" cellspacing="1" cellpadding="1">
                <tr>
                    <td>学科</td>
                    <td>课程</td>
                    <td>当天课时</td>
                    <td>操作</td>
                </tr>
                <tbody id="tbody">
                    <#if teacherClassHours?? && teacherClassHours?size gt 0>
                        <#list teacherClassHours as classHourInfo>
                        <tr>
                            <td>${(classHourInfo.subjectName)!''}</td>
                            <td>${(classHourInfo.courseName)!''}</td>
                            <td>${(classHourInfo.startTime?string("HH:mm"))!''}
                                ~ ${(classHourInfo.endTime?string("HH:mm"))!''}</td>
                            <td><input type="button" class="enterLive" value="进入房间" data-liveId="${(classHourInfo.liveId)!''}" data-emptycourseslist="${(emptycourseslist)!''}"></td>
                        </tr>
                        </#list>
                    </#if>
                </tbody>
            </table>
        </div>
    <#else>
        <#if errorMsg??>
        ${errorMsg!''}
        </#if>
        <form method="post" action="/babyeagle/teacher/page/courseslist.vpage" enctype="multipart/form-data">
            <div class="control-group">
            </div>
            账号:<input id="account"  name="account" type="text"/><br/>
            密码:<input id="password" name="password" type="password"/>
            <input class="btn btn-primary btn-danger" type="submit" value="提交"/>
        </form>
    </#if>
</div>

</@layout.page>
<#if loginFlag!false>
<script type="text/javascript">
    var SECRET_KEY = '77f24a05b6b0a945cfaf8c1a14d4383e';
    var CUSTOMER = 'xyxt';

    function sign(secretKey, liveInfo, userInfo) {
        var timestamp = new Date().getTime();
        var account = userInfo.account;
        var k = md5(liveInfo.customer + timestamp + secretKey + account + liveInfo.liveId);
        return k + '|' + timestamp + '|' + account + '|' + encodeURIComponent(userInfo.nick) + '|' + userInfo.userType;
    }

    $('.enterLive').click(function () {
        var $this = $(this);
        var LIVEID = $this.attr("data-liveId");
        var emptycourseslist = $this.attr("data-emptycourseslist");
        if(isBlank(LIVEID)){
            alert("课程创建失败,liveId为空");
            return;
        }
        if(isBlank(emptycourseslist)){
            emptycourseslist = "https://www.17zuoye.com";
        }

        var p = sign(SECRET_KEY, {customer: CUSTOMER, liveId: LIVEID}, {
            account: 'test_teacher',
            nick: 'iteacher',
            userType: 2
        });

        /*
        {
            "liveId" {String} 直播ID，使用课程信息中的`liveId`属性
            "p" {String} 直播登陆接口所需的p参数
            "courseDetailUrl" {String} 课程详情页url
            "customer" {String} 使用课程信息中`liveInfo.customer`属性
            "logoUrl" {String} 当前登录用户的logo地址
            "userType" {Number} 用户类型 `2`老师，`3`助教
            "from" {Number} 当前请求来源，对于第三方用户，这个地方传`3`
        }
        */
        zylive.client.api.notifyCourseInfo({
            liveId: LIVEID,
            p: p,
            courseDetailUrl: emptycourseslist,
            customer: CUSTOMER,
            logoUrl: 'http://tva3.sinaimg.cn/crop.0.0.180.180.180/4b29679ejw1e8qgp5bmzyj2050050aa8.jpg',
            userType: 2,
            from: 3
        }, function (err) {
            if (err) {
                alert(err);
            }
        });
    });

    function isBlank(str) {
        return typeof str == 'undefined' || String(str) == 'null' || $.trim(str) == '';
    }
</script>
</#if>