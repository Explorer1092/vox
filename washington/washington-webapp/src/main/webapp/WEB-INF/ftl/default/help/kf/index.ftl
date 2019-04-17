<#import "module.ftl" as com>
<@com.page title="">
    <@app.css href="public/skin/helpkf/css/skin.css" />
<!--//start-->
<div class="help-wrapper">
    <div class="help-left">
        <div class="help-tab">
            <ul>
                <li class="active js-clickMenuTab" data-type="student">小学学生</li>
                <li class="js-clickMenuTab" data-type="teacher">小学老师</li>
                <li class="js-clickMenuTab" data-type="middle-teacher">中学老师</li>
            </ul>
        </div>
        <div class="help-content">
            <div class="section-box">
                <div class="section01">
                    <ul>
                        <li><a href="student.vpage?type=账号密码" class="bg-orange"><div class="bg"><span class="icon icon01 PNG_24"></span></div><p>账号密码</p></a></li>
                        <li><a href="student.vpage?type=作业测验" class="bg-blue"><div class="bg"><span class="icon icon02 PNG_24"></span></div><p>作业测验</p></a></li>
                        <li><a href="student.vpage?type=奖品相关" class="bg-red"><div class="bg"><span class="icon icon03 PNG_24"></span></div><p>学习用品</p></a></li>
                        <li><a href="student.vpage?type=学豆奖励" class="bg-yellow"><div class="bg"><span class="icon icon04 PNG_24"></span></div><p>学豆奖励</p></a></li>
                    </ul>
                    <a href="/help/kf/student.vpage" class="fr-more">更多问题</a>
                </div>
                <div class="section02">
                    <ul>
                        <li><a href="student.vpage?type=账号密码&count=6">如何绑定手机？</a></li>
                        <li><a href="student.vpage?type=奖品相关&count=1">学习用品收到了，坏了怎么办?</a></li>
                        <li><a href="student.vpage?type=奖品相关&count=10">我兑换了奖品什么时候能收到？</a></li>
                        <li><a href="student.vpage?type=班级相关&count=2">我怎么加入老师的班级？</a></li>
                        <li><a href="student.vpage?type=学豆奖励&count=2">学豆减少了怎么办？</a></li>
                        <li><a href="student.vpage?type=作业测验&count=20">如何测试麦克风有没有声音？</a></li>
                        <li><a href="student.vpage?type=学豆奖励&count=6">家长奖励学豆规则是什么？</a></li>
                        <li><a href="student.vpage?type=作业测验&count=3">怎么查看老师布置的作业？</a></li>
                    </ul>
                </div>
            </div>
            <div class="section-box" style="display: none;">
                <div class="section01">
                    <ul>
                        <li><a href="teacher.vpage?type=认证相关" class="bg-orange js-clickLeftTypeBtn"><div class="bg"><span class="icon icon07 PNG_24"></span></div><p>认证相关</p></a></li>
                        <li><a href="teacher.vpage?type=班级管理" class="bg-purple"><div class="bg"><span class="icon icon06 PNG_24"></span></div><p>班级管理</p></a></li>
                        <li><a href="teacher.vpage?type=奖品相关" class="bg-red"><div class="bg"><span class="icon icon03 PNG_24"></span></div><p>教学用品</p></a></li>
                        <li><a href="teacher.vpage?type=作业测验" class="bg-blue"><div class="bg"><span class="icon icon02 PNG_24"></span></div><p>作业测验</p></a></li>
                        <li><a href="teacher.vpage?type=园丁豆" class="bg-yellow"><div class="bg"><span class="icon icon04 PNG_24"></span></div><p>园丁豆</p></a></li>
                    </ul>
                    <a href="/help/kf/teacher.vpage" class="fr-more">更多问题</a>
                </div>
                <div class="section02">
                    <ul>
                        <li><a href="teacher.vpage?type=认证相关&count=1">怎么才能认证？</a></li>
                        <li><a href="teacher.vpage?type=账号密码&count=3">怎么修改我绑定的手机号码？</a></li>
                        <li><a href="teacher.vpage?type=奖品相关&count=6">为什么很多教学用品没有了？</a></li>
                        <li><a href="teacher.vpage?type=班级管理&count=5">我如何转让班级？</a></li>
                        <li><a href="teacher.vpage?type=其他&count=12">我能查看别的老师的课件吗？</a></li>
                        <li><a href="teacher.vpage?type=班级管理&count=11">我如何创建班级？</a></li>
                        <li><a href="teacher.vpage?type=其他&count=5">怎么申请校园大使？</a></li>
                        <li><a href="teacher.vpage?type=奖品相关&count=1">怎么修改教学用品中心的收货地址？</a></li>
                    </ul>
                </div>
            </div>
            <div class="section-box" style="display: none;">
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
            <li><a href="http://www.17zuoye.com/help/downloadApp.vpage?refrerer=pc" target="_blank">一起小学学生/家长app</a></li>
            <li><a href="http://www.17zuoye.com/project/professorstudent/index.vpage" target="_blank">小学生注册教程</a></li>
            <li><a href="http://www.17zuoye.com/static/project/network/index.html" target="_blank">儿童电脑上网管家</a></li>
            <li><a href="http://www.17zuoye.com/clazz/downloadletter.vpage" target="_blank">家长使用说明文档</a></li>
            <li><a href="//cdn.17zuoye.com/download/17zuoye_liebao_20150624.exe" target="_blank">猎豹浏览器</a></li>
            <li><a href="http://get.adobe.com/cn/flashplayer/" target="_blank">下载安装flash插件</a></li>
        </ul>
    </div>
    <#--<a href="javascript:void(0);" class="btn-online chat js-clickServerPopupOne" data-usertype="teacher" data-questiontype="question_advice" data-origin="PC-帮助中心首页">反馈建议</a>-->
    <#include "serverinfo.ftl"/>
    <#if (.now lt "2016-02-12 00:00:00"?datetime("yyyy-MM-dd HH:mm:ss"))!false>
        <div class="help-right">
            <h2>网站公告</h2>
            <div style="font-size: 15px; color: #999; line-height: 160%; padding: 20px 8px;">
                由于春节放假,将暂停人工咨询,于2月11日恢复,感谢您的理解。
            </div>
        </div>
    </#if>
</div>
<!--end//-->
<script type="text/javascript">
    $(function(){
        var userType = +"${(currentUser.userType)!3}"; // 设置未登录情况下跳转学生客服
        if (userType === 1) { // 老师
            $('.js-clickServerPopupOne').attr('data-usertype', 'teacher');
        } else if (userType === 3) { // 学生
            $('.js-clickServerPopupOne').attr('data-usertype', 'student');
        }

        ysf.on({
            'onload': function(){

            }
        });

        // tab切换
        $(document).on("click", ".js-clickMenuTab" ,function(){
            var $this = $(this);
            var $index = $this.index();
            if($this.hasClass("active")){return false;}

            $this.addClass("active").siblings("li").removeClass("active");
            $(".section-box").eq($index).show().siblings(".section-box").hide();
        });

        $(".js-clickMenuTab[data-type='" + $17.getQuery("menu") + "']").click();
    });
</script>
</@com.page>