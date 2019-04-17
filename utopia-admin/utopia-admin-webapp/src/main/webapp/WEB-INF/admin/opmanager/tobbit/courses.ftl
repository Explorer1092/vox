<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='托比同步课堂' page_num=9>
<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<div id="main_container" class="span9">
    <legend style="font-weight: 700;">
        托比同步课堂
    </legend>
    <div class="row-fluid">
        <div class="span12">
            <ul class="inline">
                <form class="well form-horizontal" method="get" action="/opmanager/tobbit/courses.vpage">
                    <table>
                        <tr>
                            <td>
                                <select name="subject" id="subject" data-value="${subject!''}">
                                    <option value="">学科</option>
                                    <#list subjects as subject>
                                    <option value="${subject.name()}">${subject.value}</option>
                                    </#list>
                                </select>
                            </td>
                            <td>
                                <select name="clazzLevel" id="clazzLevel" data-value="${level!''}">
                                    <option value="">年级</option>
                                    <#list levels as level>
                                    <option value="${level.getLevel()}">${level.getDescription()}</option>
                                    </#list>
                                </select>
                            </td>
                            <td>
                                <select name="trail" id="trail" data-value="${trail!''}">
                                    <option value="">试听</option>
                                    <option value="true">是</option>
                                    <option value="false">否</option>
                                </select>
                            </td>
                            <td>
                                <input type="text" name="name" value="${name!''}" placeholder="课程名称"/>
                            </td>
                            <td>
                                <button class="btn-mini btn-primary">搜索</button>
                            </td>
                        </tr>
                    </table>

                </form>
            </ul>
            <ul class="inline">
                <a class="btn btn-primary btn-sm" href="editor.vpage?subject=CHINESE">增加语文</a>
                <a class="btn btn-success btn-sm" href="editor.vpage?subject=MATH">增加数学</a>
                <a class="btn btn-inverse btn-sm" href="editor.vpage?subject=ENGLISH">增加英语</a>
            </ul>

        </div>
        <table class="table table-striped table-bordered">
            <thead>
            <tr>
                <th>序列</th>
                <th>学科</th>
                <th>课节名称</th>
                <th>年级</th>
                <th>是否试听</th>
                <th>游戏</th>
                <th>删除</th>
                <th>编辑</th>
            </tr>
            </thead>
            <tbody>
            <#if courses?exists>
            <#list courses as course>
                <tr>
                    <td>${course.sequence!''}</td>
                    <td>${course.subject!''}</td>
                    <td>${course.name!''}</td>
                    <td>${course.level!''}</td>
                    <td>${course.trail!''}</td>
                    <td>
                        <select style="margin-bottom: 0px;" data-action="game" data-value="${course.game!''}"
                                data-id="${course.id}">
                            <option value="">未设置</option>
                            <#list games as g>
                            <option value="${g.name()}">${g.text!''}</option>
                            </#list>
                        </select>
                        <button data-action="setting">设置</button>
                    </td>
                    <td><a href="#" data-action="delete" data-value="${course.id}">删除</a></td>
                    <td><a href="editor.vpage?id=${course.id}">编辑</a></td>
                </tr>
            </#list>
            </#if>
            </tbody>
        </table>
        <ul class="pager" data-index="${pageIndex}">
            <#if pageCount?exists && pageCount gt 0>
                <#if pageIndex?exists && pageIndex gt 10>
                    <li data-index="1"><a href="${query}1">首页</a></li>
                    <li data-index="${start - 1}"><a href="${query}${start - 1}">前十页</a></li>
                </#if>
                <#list start .. end as page>
                    <li data-index="${page}"><a href="${query}${page}">${page}</a></li>
                </#list>
                <#if pageIndex?exists && pageCount gt 10 && pageIndex lt (pageCount / 10) * 10>
                    <li data-index="${end + 1}"><a href="${query}${end + 1}">后十页</a></li>
                    <li data-index="${pageCount}"><a href="${query}${pageCount}">尾页</a></li>
                </#if>
            </#if>
        </ul>
    </div>
</div>

<script language="JavaScript" type="application/javascript">
    $(document).ready(function () {
        $("#subject").val($("#subject").attr("data-value"));
        $("#clazzLevel").val($("#clazzLevel").attr("data-value"));
        $("#trail").val($("#trail").attr("data-value"));
        $("[data-action='delete']").click(function () {
            var id = $(this).attr("data-value");
            if (confirm("确实要删除数据么")) {
                $.post("/opmanager/tobbit/delete.vpage", {"id": id}, function (data) {
                    if (data.success) {
                        document.location.reload();
                    } else {
                        alert(data.info);
                    }
                });
            }
        });

        $("[data-action='game']").each(function () {
            $(this).val($(this).attr("data-value"));
        });
        $("[data-action='setting']").click(function () {

            if ($(this).prev().val() == "") {
                alert("请选择游戏类型")
                return;
            }
            window.open("game.vpage?id=" + $(this).prev().attr("data-id") + "&type=" + $(this).prev().val());

        });
    })
</script>

</@layout_default.page>