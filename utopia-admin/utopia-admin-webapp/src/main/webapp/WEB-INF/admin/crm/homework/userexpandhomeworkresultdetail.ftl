<#-- @ftlvariable name="stResultDetailList" type="java.util.Map<String, Object>" -->
<#-- @ftlvariable name="homeworkId" type="java.lang.Long" -->
<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="CRM" page_num=3>
<style>
    blockquote {
        margin: 0;
    }

    detail {

    }

</style>
<div id="main_container" class="span9">
    <fieldset>
        <legend>
            <a href="${requestContext.webAppContextPath}/crm/student/studenthomepage.vpage?studentId=${studentId!}">${realName}</a>的作业
            <a href="${requestContext.webAppContextPath}/crm/homework/expandhomeworkhomepage.vpage?homeworkId=${homeworkId!}">${homeworkId!}</a>答题详情
            <select id="select">
                <#list selectItemKey as detail>
                    <option value=${detail}>${selectItemValue[detail_index]}</option>
                </#list>
            </select>
            <button id="button">字段解析</button>
        </legend>
    </fieldset>

    <div id="content">

    </div>
    <div id="alertDiv" title="答题详情">

    </div>
    <div id="alertDiv2" title="字段解析">

    </div>

</div>

<script id="alertTemple" type="text/html">
    <%for(var i = 0; i < alertContent.length; i++){%>
    <p><%=alertContent[i]%></p>
    <%}%>
</script>


<script id="movieTemplate" type="text/html">
    <table class="table table-hover table-striped table-bordered">
        <tr>
            <th>作业类型</th>
            <th>题ID</th>
            <#--<th>标准分</th>-->
            <#--<th>实际得分</th>-->
            <th>完成时长</th>
            <th>创建时间</th>
            <th>是否掌握</th>
            <th>标准答案</th>
            <th>用户答案</th>
            <th>区域掌握</th>
            <th>明细操作</th>
            <th>客户端名称</th>
            <th>客户端类型</th>
        </tr>
        <%for(var i = 0; i < stResultDetailList.length; i++){%>
        <tr>
            <td><%=stResultDetailList[i].itemName%></td>
            <td><%=stResultDetailList[i].qId%></td>
            <#--<td><%=stResultDetailList[i].standardScore%></td>-->
            <#--<td><%=stResultDetailList[i].score%></td>-->
            <td><%=stResultDetailList[i].duration%></td>
            <td><%=stResultDetailList[i].createTime%></td>
            <td><%=stResultDetailList[i].grasp%></td>
            <td><%=stResultDetailList[i].sourceAnswer%></td>
            <td><%=stResultDetailList[i].userAnswers%></td>
            <td><%=stResultDetailList[i].subGrasp%></td>

            <td><a class="detail" data-qId="<%=stResultDetailList[i].qId%>">查看全部</a></td>
            <td><%=stResultDetailList[i].clientName%></td>
            <td><%=stResultDetailList[i].clientType%></td>
        </tr>
        <%}%>
    </table>
</script>

<script>
    $(function () {

        var stResultDetailList =${resultList};
        $("#content").html(template("movieTemplate", {
            stResultDetailList: stResultDetailList
        }));
        $('#select').change(function () {
            var selectValue = $(this).children('option:selected').val();
            var filterValue = [];
            for (var i = 0; i < stResultDetailList.length; i++) {
                if (stResultDetailList[i].selectItemKey == selectValue || selectValue == "0.0") {
                    filterValue.push(stResultDetailList[i]);
                }
            }
            $("#content").html(template("movieTemplate", {
                stResultDetailList: filterValue
            }));
            $("a[class='detail'").click(detailClick)

        });
        function correctNewHomeworkProcessDetailClick() {
            var correctNewHomeworkProcessInfo = $(this).attr("data-correctNewHomeworkProcessInfo");
            if (correctNewHomeworkProcessInfo == "") {
                correctNewHomeworkProcessInfo = "该题没有订正答题信息";
            }
            $("#alertDiv").html(template("alertTemple", {
                alertContent: [correctNewHomeworkProcessInfo]
            }));
            $("#alertDiv").dialog({
                resizable: false,
                height: "auto",
                width: 800,
                modal: true,
                buttons: {
                    "关闭": function () {
                        $(this).dialog("close");
                    }
                }
            });

        }

        function detailClick() {
            var qId = $(this).attr("data-qId");
            var target;
            for (var i = 0; i < stResultDetailList.length; i++) {
                if (stResultDetailList[i].qId == qId) {
                    target = stResultDetailList[i];
                    break;
                }
            }
            $("#alertDiv").html(template("alertTemple", {
                alertContent: [target.content]
            }));
            $("#alertDiv").dialog({
                resizable: false,
                height: "auto",
                width: 800,
                modal: true,
                buttons: {
                    "关闭": function () {
                        $(this).dialog("close");
                    }
                }
            });

        }

        $("#button").click(function () {
            $("#alertDiv2").html(template("alertTemple", {
                alertContent: ["标准分：这道题目的满分", "实际得分：学生答案的得分", "是否掌握：这道题目学生回答是否全对", "区域掌握：当用户答案为多项时，表示每项是否正确", "跟读作业：引擎分/流利程度/完整度/发音准确度"]
            }));
            $("#alertDiv2").dialog({
                resizable: false,
                height: "auto",
                width: 400,
                modal: true,
                buttons: {
                    "关闭": function () {
                        $(this).dialog("close");
                    }
                }
            });
        })
        $("a[class='correctNewHomeworkProcessDetail'").click(correctNewHomeworkProcessDetailClick)
        $("a[class='detail'").click(detailClick)
    });
</script>


</@layout_default.page>