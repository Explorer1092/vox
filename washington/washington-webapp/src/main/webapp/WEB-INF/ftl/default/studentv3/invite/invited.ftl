<script type="text/html" id="t:学生被邀请">
    <#if activateList?? && activateList?size gt 0 >
    <div id="student_invite_teacher_box">
        <div class="sharedctn">
            <h5 class="blueclr">${realname!}同学：</h5>
            <p>欢迎来到“一起作业”！这里有超级弹跳、声波守卫、打地鼠、海底世界等好多有趣的英语练习等你来挑战！做作业，学英语，还能一起玩PK！快来和我一起作业吧！</p>
            <p>
                有${activateList?size} 位同学邀请你一起做作业，选择一位同学接受他（她）的邀请。如果老师还没有布置新作业，你可以去“学习中心”补做未完成的作业！
            </p>
        </div>
        <div class="userlistboxs">
            <ul>
                <#list activateList as s>
                    <li class="invit">
                        <s <#if s.choose>class="i_receiving w-spot w-check-1-current"<#else>class="i_receiving w-spot w-check-1" </#if> value="${s.userId!}"></s>
                        <div class="picture">
                            <img src="<@app.avatar href="${s.userAvatar!}"/>"/>
                        </div>
                        <div class="nameid">${s.userName!}</div>
                    </li>
                </#list>
            </ul>
            <div class="sharedbtn">
                <a id="start_to_homework_but" href="javascript:void(0);"
                   class="w-btn-dic w-btn-green-new"><strong>开始作业</strong></a>
            </div>
            <div class="clear"></div>
        </div>
    </div>
    </#if>
</script>

<script type="text/javascript">
    $(function () {
        /*学生被邀请弹窗*/
        var invited = {
            state: {
                title: "学生被邀请",
                html: template("t:学生被邀请", {}),
                position: { width: 540 },
                buttons: {}
            }
        };
//        $.prompt(invited);

        $(document).on("click", "#student_invite_teacher_box .picture", function () {
            var _this = $(this);
            var _s = _this.siblings("s");
            _s.addClass("w-check-1-current");
            _this.parent().siblings().find("s").removeClass("w-check-1-current");
            $17.tongji("学生邀请学生_关闭开始作业弹窗", "");
        });

        //开始作业
        $("#start_to_homework_but").on("click", function () {
            var studentIds = [];
            $('#student_invite_teacher_box .w-check-1-current').each(function () {
                studentIds.push({userId: $(this).attr("value")});
            });

            App.postJSON("/student/invite/acceptstudentactivatestudent.vpage", {userList: studentIds}, function (data) {
            });
            $.prompt.close();
            $17.tongji("学生邀请学生_开始作业", "");
        });
    });
</script>
