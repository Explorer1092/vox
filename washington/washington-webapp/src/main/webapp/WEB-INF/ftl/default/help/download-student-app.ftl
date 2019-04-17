<#import "../layout/project.module.student.ftl" as com>
<@com.page title="一起作业手机版">
<@sugar.capsule js=[] css=["new_student.widget"] />
<style type="text/css">
    html, body{ background-color: #d3f6fe;}
    .special-footer .m-inner{ display: none;}
    .special-footer{ background: url(<@app.link href="public/skin/studentv3/images/studentApp/student-app_2.jpg"/>) no-repeat center 0; height: 180px; border: none;}
    /*.special-footer .copyright, .special-footer .m-foot-link a, .special-footer .m-service .c-title{ color: #fff;}*/
    /*.special-footer .m-inner{ background: none; height: auto;}*/

    .app-main, .app-main .ap-head{ background: url(<@app.link href="public/skin/studentv3/images/studentApp/student-app_1.jpg"/>) no-repeat center 0; height: 557px;}
    .app-main, .app-main .ap-head.nobtn{ background: url(<@app.link href="public/skin/studentv3/images/studentApp/student-app_1_1.jpg"/>) no-repeat center 0;}
    .app-main .ap-head{ width: 1000px; margin: 0 auto; position: relative;}

    .app-main .ap-code {text-align: center; width: 158px; position: absolute; right: 104px; top: 121px;}
    .app-main .ap-code .code{ margin-bottom: 80px;}
    .app-main .ap-code .download-btn{ display: block; height: 65px; overflow: hidden; text-indent: -500px; width: 100%; float: left;}

    .app-content-popup{ text-align: center;}
    .app-content-popup h3{ font-size: 14px; color: #333; font-weight: normal;}
    .app-content-popup p{ margin-bottom: 10px;}
    .app-content-popup .left,
    .app-content-popup .right{ float: left; width: 50%;}
</style>
<div class="app-main">
    <div class="ap-head <#if currentUser?? && (currentUser.id+"")?substring((currentUser.id+"")?length-2) == "17">nobtn</#if>">
        <div class="ap-code">

                <p class="code"><img style="width:136px" src="<@app.link href="public/skin/studentv3/images/studentApp/appCode-cid-102016.png"/>"/></p>
                <a href="javascript:void(0);" class="download-btn">立即下载</a>



        </div>
    </div>
</div>
<script type="text/javascript">
    $(function(){
        if(window.location.search.indexOf("subjectivehomework") != -1){
            $17.voxLog({
                module: "zhuguanzuoye",
                op : "phone_downloadbtn_click"
            });
        }

        $(document).on("click", ".download-btn", function(){
            $.prompt(template("T:content", {}), {
                title: "下载一起作业学生端",
                buttons: {},
                position: {width: 600}
            });
            $17.voxLog({
                app: 'student',
                module: 'downloadpage',
                op: 'click_btn1'
            }, 'student');
        });

        $(document).on("click", ".downloadinpop", function(){
            $17.voxLog({
                app: 'student',
                module: 'downloadpage',
                op: 'click_btn2'
            }, 'student');
        })

    });
</script>
<script type="text/html" id="T:content">
    <div class="app-content-popup">
        <div class="left">
            <p style="margin-top: 10px;"><img style="width:136px" src="<@app.link href="public/skin/studentv3/images/studentApp/appCode-cid-102016.png"/>"/></p>
            <p >支持安卓和苹果手机</p>
        </div>
        <div class="right" style="text-align: left;">
            <h3><strong style="font-size:19px;">扫描左侧二维码，下载学生端</strong></h3>
            <p>方法一：使用手机qq右上角的“扫一扫”</p>
            <p style="padding-left:57px;">或微信发现中的“扫一扫”</p>
            <p>方法二：使用其他工具扫码</p>
            <p>方法三：<a href="http://wx.17zuoye.com/download/17studentapp?cid=102016">安卓手机还可以下载到电脑</a></p>
        </div>
        <div class="w-clear"></div>
    </div>
</script>
</@com.page>