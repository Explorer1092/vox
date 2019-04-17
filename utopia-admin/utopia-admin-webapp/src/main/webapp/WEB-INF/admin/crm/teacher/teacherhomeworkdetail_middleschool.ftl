<#-- @ftlvariable name="day" type="java.lang.Integer" -->
<#-- @ftlvariable name="userId" type="java.lang.Long" -->
<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="CRM" page_num=3>
<div id="main_container" class="span9">
    <div>
        <fieldset>
            <legend>作业详情(${day!''}天内，按作业截止时间)</legend>
        </fieldset>
        <ul class="inline">
            <li>
                <form method="post" action="teacherhomeworkdetail.vpage?day=30&userId=${userId!''}">
                    <button class="btn btn-primary" type="submit">查看30天内记录</button>
                </form>
            </li>
            <li>
                <form method="post" action="teacherhomeworkdetail.vpage?day=365&userId=${userId!''}">
                    <button class="btn btn-primary" type="submit">查看1年内记录</button>
                </form>
            </li>
        </ul>
        <table class="table table-hover table-striped table-bordered">
            <tr id="title">
                <th> 开始时间</th>
                <th> 截止时间</th>
                <th> 作业ID</th>
                <th> 教师ID</th>
                <th> 学校名称</th>
                <th> 班级名称</th>
                <th> 学生总人数</th>
                <th> 参与人数</th>
                <th> 完成人数</th>
                <!--th>IP数量</th-->
                <!--th>消耗学豆</th-->
                <th>操作</th>
            </tr>
            <#if teacherHomeworkHistoryList?has_content>
                <#list teacherHomeworkHistoryList as teacherHomeworkHistory>
                    <tr>
                        <td>${teacherHomeworkHistory.homeworkStartDate!""}</td>
                        <td>${teacherHomeworkHistory.homeworkEndDate!""}</td>
                        <td><a href="../homework/homeworkhomepage.vpage?category=middle&homeworkId=${teacherHomeworkHistory.homeworkId!}">${teacherHomeworkHistory.homeworkId!""}</a>
                        </td>
                        <td>
                            <a href="../user/userhomepage.vpage?userId=${userId!""}"> ${userId!""}</a>
                        </td>
                        <td><a href="../school/schoolhomepage.vpage?schoolId=${teacherHomeworkHistory.schoolId!""}">${teacherHomeworkHistory.schoolName!""}</a></td>
                        <td>${teacherHomeworkHistory.clazzName!""}(${teacherHomeworkHistory.clazzId!""})</td>
                        <td>${teacherHomeworkHistory.studentCount!""}</td>
                        <td>${teacherHomeworkHistory.joinCount!""}</td>
                        <td>${teacherHomeworkHistory.completeCount!""}</td>
                        <#if teacherHomeworkHistory.handcheck == true>
                            <td><a class="check_ip" href="javascript:void(0)" data-homework_id="${teacherHomeworkHistory.homeworkId!}" data-homework_subject="${teacherHomeworkHistory.homeworkSubject!}">查询</a></td>
                        <#else>
                            <!-- td>${teacherHomeworkHistory.ipcount!''}
                                <#if teacherHomeworkHistory.possibleCheat??>
                                    <i class=" icon-exclamation-sign" title="${teacherHomeworkHistory.possibleCheat.reason!''}"></i>
                                    <#if teacherHomeworkHistory.possibleCheat.isAddIntegral?has_content && teacherHomeworkHistory.possibleCheat.isAddIntegral>
                                        已补加
                                    <#else>
                                        <a class="add_Integral" href="javascript:void(0)" data-homework_id="${teacherHomeworkHistory.possibleCheat.id!}" data-teacher_id="${userId!""}">补加</a>
                                    </#if>
                                </#if>
                            </td -->
                        </#if>
                        <td style="display:none"><a name="usedPrize" role="button"
                               data-homework_id="${teacherHomeworkHistory.homeworkId!}" data-homework_subject="${teacherHomeworkHistory.homeworkSubject!}"
                               id="${teacherHomeworkHistory.homeworkId!}"
                               class="btn btn-primary">查看</a>
                        </td>
                        <td>
                            <#if !requestContext.getCurrentAdminUser().isCsosUser()>
                                <a name="deleteIndex" role="button" data-content-id="${teacherHomeworkHistory.homeworkId!}" class="btn btn-primary">删除</a>
                            </#if>
                        </td>
                        <td style="display:none"><a name="addIntegral" role="button" data-content-id="${teacherHomeworkHistory.homeworkId!}" class="btn btn-primary">补发园丁豆</a></td>
                    </tr>
                </#list>
            </#if>
        </table>
    </div>
</div>

<div id="check_ip_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>真实性检验</h3>
    </div>
    <div class="modal-body">
        <dl class="dl-horizontal">
            <ul class="inline">
                <li>
                    <p id="check_ip_head"></p>
                </li>
            </ul>
            </ul>
            <ul class="inline">
                <li>
                    <div id="check_ip_content"></div>
                </li>
            </ul>
        </dl>
    </div>
    <div class="modal-footer">
        <button data-dismiss="modal" aria-hidden="true" class="btn">确 定</button>
    </div>
</div>

<script>
    $(function () {
        $('.check_ip').on('click', function () {
            var $this = $(this);
            var postData = {
                homeworkId: $this.data('homework_id'),
                homeworkSubject: $this.data('homework_subject')
            };
            $.post('checkipamount.vpage', postData, function (data) {
                $('#check_ip_head').text('用户ID：${userId!},   作业ID：' + $this.data('homework_id'));
                var $check_ip_content = $('#check_ip_content');
                $check_ip_content.empty();
                if (data.success) {
                    var ipAmount = parseInt(data.ipAmount);
                    ipAmount = ipAmount || 0;
                    if (ipAmount < 5) {
                        $check_ip_content.append('累计IP数量：' + '<span style="color: RED;">' + ipAmount + '</span>');
                    } else {
                        $check_ip_content.text('累计IP数量： ' + ipAmount);
                    }
                } else {
                    $check_ip_content.text(data.info);
                }
                $('#check_ip_dialog').modal('show');
            });
        });
        $('i.icon_vox_orange').each(function () {
            $(this).click(function () {

            });
        });

        $(".add_Integral").on("click", function () {
            if (confirm("确定为该作业补加金银币吗？")) {
                var $this = $(this);
                var postData = {
                    cheateId: $this.data('homework_id')
                };
                $.ajax({
                    type: 'post',
                    url: 'addIntegral.vpage',
                    data: postData,
                    success: function (data) {
                        if (data.success) {
                            window.location.href = "teacherhomeworkdetail.vpage?day=30&userId=${userId!''}";
                        } else {
                            alert("补加失败，请联系管理员")
                        }
                    }
                });
            }
        });

        $("a[name='usedPrize']").on("click", function () {
            var homeworkId = $(this).attr("data-homework_id");
            var homeworkType = $(this).attr("data-homework_subject");
            var item = $(this);
            $.ajax({
                type: "get",
                url: "getusedhomeworkprize.vpage",
                data: {
                    homeworkId: homeworkId,
                    homeworkType: homeworkType
                },
                success: function (data) {
                    if (data.success) {
                        item.before(data.usedprize);
                    } else {
                        item.before(0);
                    }
                    item.remove();
                }
            });
        });

        $("a[name='deleteIndex']").on("click", function () {
            var homeworkId = $(this).attr("data-content-id");
            if (confirm("确定删除ID为" + homeworkId + "的作业吗？")) {
                $.ajax({
                    type: "post",
                    url: "disablehomework.vpage",
                    data: {
                        homeworkId: homeworkId,
                        teacherId: ${userId!}
                    },
                    success: function (data) {
                        if (data.success) {
                            window.location.href = "teacherhomeworkdetail.vpage?day=30&userId=${userId!''}";
                        } else {
                            alert(data.info);
                        }
                    }
                });
            }
        });

        $("a[name='addIntegral']").on("click", function () {
            var homeworkId = $(this).attr("data-content-id");
            if (confirm("确定要补发园丁豆么？教师将按照完成学生数量的1.5倍发放，每个完成的学生将获得11学豆")) {
                $.ajax({
                    type: "post",
                    url: "addhomeworkintegral.vpage",
                    data: {
                        homeworkId: homeworkId,
                        teacherId: ${userId!}
                    },
                    success: function (data) {
                        if (data.success) {
                            window.location.href = "teacherhomeworkdetail.vpage?day=30&userId=${userId!''}";
                        } else {
                            alert(data.info);
                        }
                    }
                });
            }
        });
    });
</script>
</@layout_default.page>