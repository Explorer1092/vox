<#--邀请激活老师-->
<#if potentialTeacherList?? && potentialTeacherList?size gt 0>
    <div class="top_class">
        <span>唤醒一个老师，奖励200学豆 <a class="viewRulesBut" data-rt="teacherRules" href="javascript:void (0);" style="font-size: 12px;color: blue;">查看规则</a></span>
    </div>

<div class="rule">
    <div class="summary" style="padding-bottom: 50px;">
        <ul id="show_teacher_list" class="userListBox" style="margin: 0 auto; width: 220px;">
            <#list potentialTeacherList as t>
                <li data-can_activate_today="${t.canActivateToday?string!''}">
                    <div class="avatar">
                        <img src="<@app.avatar href='${t.userAvatar!}'/>" style="width: 70px;height: 71px;">
                        <i data-teacher_id="${t.userId!}" class="checkboxs selectTeacherCheckbox"></i>
                    </div>
                    <div class="title">
                        <#if t.subject?string =="ENGLISH">
                            英语老师
                        <#elseif t.subject?string =="MATH">
                            数学老师
                        <#elseif t.subject?string =="CHINESE">
                            语文老师
                        </#if><br/>
                        ${t.userName!''}
                    </div>
                </li>
            </#list>
        </ul>

        <div class="spacing_vox" style="clear: both; text-align: center;">
            <a id="invite_teacher_but" data-val="0" href="javascript:void(0);" class="w-btn w-btn-blue">
                激活老师
            </a>
        </div>
    </div>
    <div class="bot"></div>
</div>
</#if>


<script type="text/html" id="t:学生邀请老师">
    <div id="success_s_box" style=" font:14px/1.125 arial; text-align:center; padding:0 0 30px; color:#333;">
        <b>邀请已发送！</b>
        <p style="padding:10px 0 20px;">记得当面告诉老师登录一起作业<br/>先勾选你再布置作业哦~</p>
        <a href="javascript:window.location.reload();" class="w-btn w-btn-green">知道了</a>
    </div>
</script>

<script type="text/html" id="t:teacherRules">
    <div style=" font:12px/1.125 arial; padding:0 0 30px; color:#333;">
        <p>如果老师两周没布置作业，你可以发起邀请，当老师选择你为邀请人布置并检查了作业后超过10人完成，你可以获得200学豆！</p>
    </div>
</script>

<script type="text/html" id="t:studentRules">
    <div style=" font:12px/1.125 arial; padding:0 0 30px; color:#333;">
        <p>1.当有同学从未做作业，如果你发起激活邀请被他接受，在他完成累计3次作业后，你可获得50学豆；同时，被激活的同学也会得到5个学豆奖励；</p>
        <p>2.当有同学超过一个月没做作业，如果你发起激活邀请被他接受，在他完成作业后你可获得30学豆；</p>
        <p>3.当有同学超过两周没有做作业，如果你发起激活邀请被他接受，在他完成作业后你可以获得10学豆。</p>
    </div>
</script>

<script type="text/html" id="t:giftBox">
    <div style=" font:12px/1.125 arial; padding:0 0 30px; color:#333;">
        <p>邀请同学完成作业可获得如下奖励：</p>
        <p>学豆</p>
        <p>免费转职次数</p>
        <p>14天PK经验加成</p>
        <p>详情见游戏内帮助页</p>
    </div>
</script>

<script type="text/javascript">
    $(function () {
        // 选择要激活的老师
        $('#show_teacher_list').on('click', 'li', function () {
            $(this).find('.selectTeacherCheckbox').toggleClass('checkboxs_active');
        });

        //todo
        var liLength = $('#show_teacher_list li').length;
        var canActivateTodayLenght = $('#show_teacher_list li[data-can_activate_today=false]').length;
        if(liLength == canActivateTodayLenght){
            $("#show_teacher_list").html('<li>你今天已经邀请过所有老师，改天再来吧~记得当面邀请你的老师回来布置作业哟~！</li>').css({width: '481px'});
            $('#invite_teacher_but').hide();
        }


        //激活老师
        $("#invite_teacher_but").on("click", function () {
            var teacherIds = $("#show_teacher_list li i.checkboxs_active");
            var teacherId = [];
            $.each(teacherIds, function (i) {
                teacherId.push({
                    userId : $(this).data('teacher_id'),
                    type : 'STUDENT_ACTIVATE_TEACHER'
                });
            });
            if ($17.isBlank(teacherId)) {
                $17.alert("请选择要激活的老师。");
                return false;
            }
            App.postJSON("/student/invite/studentactivatestudent.vpage", {userList: teacherId, classId: "${(clazzId)!'0'}"}, function (data) {
                if (data.success) {
                    var inviteTeacher = {
                        state: {
                            title: "邀请老师",
                            html: template("t:学生邀请老师", {}),
                            position: { width: 400 },
                            buttons: {}
                        }
                    };
                    $.prompt(inviteTeacher);
                } else {
                    $17.alert(data.info);
                }
            });
        });

        //查看规则
        $(".viewRulesBut").each(function () {
            var $this = $(this);
            var rulesContent = $this.data('rt');

            $this.qtip({
                content: {
                    title : '查看规则',
                    text: template("t:"+rulesContent, {})
                },
                hide: {
                    fixed: true,
                    delay: 150
                },
                position: {
                    at: 'bottom right',
                    my: 'center left',
                    viewport: $(window),
                    effect: false,
                    adjust: {
                        y: -8
                    }
                },
                style: {
                    classes: 'qtip-bootstrap',
                    width: 300
                }
            });
        });
    });
</script>