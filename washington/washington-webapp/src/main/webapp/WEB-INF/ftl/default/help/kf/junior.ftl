<#import "module.ftl" as com>
<@com.page title="">
    <@app.css href="public/skin/helpkf/css/skin.css" />
<!--//start-->
<div class="help-wrapper">
    <div class="help-left">
        <div class="help-tab">
            <ul>
                <li class="active" data-type="teacher">中学老师</li>
            </ul>
        </div>
        <div class="help-content">
            <div class="section-box">
                <div class="section01">
                    <ul>
                        <li><a href="junior-teacher.vpage?type=认证问题" class="bg-orange js-clickLeftTypeBtn"><div class="bg"><span class="icon icon07 PNG_24"></span></div><p>认证相关</p></a></li>
                        <li><a href="junior-teacher.vpage?type=找回账号密码" class="bg-red"><div class="bg"><span class="icon icon01 PNG_24"></span></div><p>账号密码</p></a></li>
                        <li><a href="junior-teacher.vpage?type=转班问题" class="bg-purple"><div class="bg"><span class="icon icon06 PNG_24"></span></div><p>班级管理</p></a></li>
                        <li><a href="junior-teacher.vpage?type=作业相关问题" class="bg-blue"><div class="bg"><span class="icon icon02 PNG_24"></span></div><p>作业问题</p></a></li>
                        <li><a href="junior-teacher.vpage?type=其他问题" class="bg-green"><div class="bg"><span class="icon icon08 PNG_24"></span></div><p>其他问题</p></a></li>
                    </ul>
                    <a href="/help/kf/junior-teacher.vpage" class="fr-more">更多问题</a>
                </div>
                <div class="section02">
                    <ul>
                        <li><a href="junior-teacher.vpage?type=认证问题&count=1">怎么才能认证？</a></li>
                        <li><a href="junior-teacher.vpage?type=修改绑定手机号或密码&count=0">怎么修改绑定的手机号码？</a></li>
                        <li><a href="junior-teacher.vpage?type=作业相关问题&count=1">怎么调整作业？</a></li>
                        <li><a href="junior-teacher.vpage?type=学生管理&count=2">如何添加学生？</a></li>
                        <li><a href="junior-teacher.vpage?type=作业相关问题&count=3">怎么查看学生的作业情况？</a></li>
                        <li><a href="junior-teacher.vpage?type=添加班级&count=0">怎么添加班级？</a></li>
                        <li><a href="junior-teacher.vpage?type=转班问题&count=2">怎么把我的班转给其他老师？</a></li>
                        <li><a href="junior-teacher.vpage?type=转班问题&count=0">加错班级怎么修改？</a></li>
                        <li><a href="junior-teacher.vpage?type=教学班&count=1">怎么创建教学班？</a></li>
                    </ul>
                </div>
            </div>
        </div>
    </div>
    <div class="help-right">
        <h2>常见工具</h2>
        <ul>
            <li><a href="http://www.17zuoye.com/help/download-student-app.vpage" target="_blank">一起小学学生app</a></li>
            <li><a href="//cdn.17zuoye.com/download/17zuoye_liebao_20150624.exe" target="_blank">猎豹浏览器</a></li>
            <li><a href="http://get.adobe.com/cn/flashplayer/" target="_blank">下载安装flash插件</a></li>
        </ul>
    </div>
</div>
</@com.page>