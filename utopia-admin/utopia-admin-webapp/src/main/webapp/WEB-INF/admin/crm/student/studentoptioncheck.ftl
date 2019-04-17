<#-- @ftlvariable name="studentHomeworkList" type="java.util.List<com.voxlearning.utopia.admin.data.StudentHomeworkResultOptionCheck>" -->
<#-- @ftlvariable name="resultDetail" type="java.util.List<com.voxlearning.utopia.service.homework.api.entity.StudentHomeworkResultDetail>" -->
<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="CRM" page_num=3>
<div id="main_container" class="span9">
    <div>
        <form method="post" action="?" class="form-horizontal">
            <fieldset>
                <legend><a href="../user/userhomepage.vpage?userId=${userId!}">${userName!}</a>(${userId!})</legend>
            </fieldset>
            <ul class="inline form_datetime">
                <li>
                    <label for="startDate">
                        查询时间
                        <input name="date" id="date" type="text" placeholder="格式：2015-06-16" value="${date!}"/>
                    </label>
                </li>
                <li>
                    <button type="submit" class="btn btn-success">查询</button>
                </li>
            </ul>
            <input name="userId" type="hidden" value="${userId!}">
        </form>
        <span>
            一共完成了的${a}个作业，完成了${b}个应用，其中${c}个是语音作业。<br />
            一共有${d}个flash加载失败的情况/加载超时的记录，共有${e}次重新加载的记录，进行了${f}次录音，有${g}个录音失败的记录。
            <a id="checkHistory" href="javascript:void(0);">查看记录</a>
        </span>
        <table class="table table-hover table-striped table-bordered">
            <tr id="title">
                <th>作业ID</th>
                <th>完成时间</th>
                <th>得分</th>
                <th>得到园丁豆（仅供参考）</th>
            </tr>
            <#if studentHomeworkList?has_content>
                <#list studentHomeworkList as sh>
                    <tr>
                        <td>${sh.id!""}</td>
                        <td>${sh.finishAt!""}</td>
                        <td>${sh.score!""}</td>
                        <td>${sh.silver!""}</td>
                    </tr>
                    <tr><td colspan="4">
                        <table class="table">
                            <th>应用ID</th>
                            <th>提交时间</th>
                            <th>得分</th>
                            <th>听读模式</th>
                            <#list sh.resultDetail![] as rd>
                                <tr>
                                    <td>应用ID -> ${rd.practiceId!""}</td>
                                    <td>${rd.createAt!""}</td>
                                    <td>${rd.score!""}</td>
                                    <td>${rd.correct?string('听读模式','非听读模式')}</td>
                                </tr>
                            </#list>
                        </table>
                    </td></tr>
                </#list>
            </#if>
        </table>
    </div>
</div>
<script type="text/javascript">
    $(function() {
        $("#date").datepicker({
            dateFormat: 'yy-mm-dd',  //日期格式，自己设置
            monthNames: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
            monthNamesShort: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
            dayNamesMin: ["日", "一", "二", "三", "四", "五", "六"],
            defaultDate: new Date(),
            numberOfMonths: 1,
            changeMonth: false,
            changeYear: false,
            onSelect: function (selectedDate) {
            }
        });

        $("#checkHistory").click(function(){
            var date = $("#date").val();
            window.location.href = "http://stat.log.17zuoye.net/orallog/?date=" + date + "&uid=" + ${userId!};
        });
    });
</script>
</@layout_default.page>