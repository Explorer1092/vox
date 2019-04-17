<#import "../../layout/project.module.student.ftl" as temp />
<@temp.page title="免费送学豆啦！我和孩子一起作业有奖征文" header="hide">
    <@sugar.capsule css=["project.makelearningbeans"] />
    <!--//start-->
    <div class="head">
        <div class="head_inner">
            <a class="logo" href="/"></a>
        </div>
    </div>
    <div class="content_rule">
        <div class="content_rule_inner"></div>
    </div>
    <div class="content_condition">
        <div class="content_condition_inner">
            <div class="content_condition_box">
                <p>
                    已经绑定微信的同学直接晒成绩，未绑定的同学绑定后晒成绩
                </p>
                <p>
                    同一个学号：<br/>
                    绑定一个微信号，奖励2个学豆;<br/>
                    绑定两个微信号，奖励10个学豆;<br/>
                    绑定三个及以上微信号，奖励50个学豆
                </p>
                <p>
                    一定要不同的微信号才可以哦，多绑多赚
                </p>
            </div>
            <div class="content_condition_info" id="getCode">
                <div class="before">
                    <a class="click_before" href="javascript:void (0)" title="点击获得二维码"></a>
                </div>
                <div class="after" style="display: none;"></div>
            </div>
        </div>
    </div>
    <div class="content_got">
        <div class="content_got_inner">
            <div class="content_got_box">
                <p>第一时间获取老师布置作业通知</p>
                <p>随时查看孩子作业、测验成绩</p>
                <p>每周通报孩子在班里的水平</p>
                <p>孩子进步了，晒晒宝贝成绩单</p>
                <p>重置密码功能再也不担心忘记账号密码了</p>
                <p>专属客服解答疑难问题</p>
            </div>
            <div class="content_got_info">
                <ul>
                    <li>姓名</li>
                    <li style="width:108px">学豆</li>
                    <li>姓名</li>
                    <li style="width:108px">学豆</li>
                </ul>
                <div class="content_infomation">
                    ${pageBlockContentGenerator.getPageBlockContentHtml('StudentIndex', 'WinnersListItemsP1')}
                </div>
            </div>
        </div>
    </div>
    <div class="foot">
        <div class="foot_inner">
            <div id="footerPablic" class="footer_summer" data-type="1" data-service="教师QQ群：235401380"></div>
            <script src="http://cdn.17zuoye.com/static/project/module/js/project-plug.js?1.0.1"></script>
        </div>
    </div>
    <!--end//-->
    <script type="text/javascript">
        $(function(){
            $("#getCode .before").on("click", function(){
                var $this       = $(this);
                var $thisSi     = $this.siblings(".after");
                var weiXinCode  = "<div class='loading_vox' style='height: 100%;'></div>";
                var qrCodeUrl = "<@app.link href="public/skin/studentv3/images/2dbarcode.jpg"/>";

                $this.addClass("dis").html(weiXinCode);

                $.get("/student/qrcode.vpage", function(data){
                    if(data.success){
                        qrCodeUrl = data.qrcode_url;
                    }else{
                        if("请返回首页重新登录" == data.info){
                            $17.alert(data.info, function(){
                                location.href = "/";
                            });
                        }
                    }
                    $this.hide();
                    weiXinCode = "<img src='"+ qrCodeUrl +"' width='132' height='132'/><p>微信扫一扫<br/>随时了解孩子学习</p>";
                    $thisSi.show().html(weiXinCode);
                    $17.tongji("学生端二维码_绑定_专题页");
                });
            });
        });
    </script>
</@temp.page>