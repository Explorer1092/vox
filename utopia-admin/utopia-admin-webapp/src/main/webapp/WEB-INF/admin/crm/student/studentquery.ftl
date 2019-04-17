<#-- @ftlvariable name="conditionMap" type="java.util.LinkedHashMap" -->
<#macro queryPage>
<link href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/bootstrap.autocomplete.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/vue2.4.2.min.js"></script>
<div>
    <form id="query_info_form" method="post" action="studentlist.vpage" class="form-horizontal" >
        <fieldset>
            <legend>学生精确查询</legend>
            <ul class="inline" id="query_info_module">
                <li>
                    <label for="studentId">
                        学生学号
                        <input name="studentId" id="studentId" type="text"/>
                    </label>
                </li>
                <li>
                    <label for="userMobile">
                        学生或家长手机号
                        <input name="userMobile" id="userMobile" type="text"/>
                    </label>
                </li>
                <li>
                    <button id="query_info_btn" type="button" class="btn btn-primary">查 询</button>
                </li>
            </ul>

            <#--快乐学id查找，但不合上面的input一样从表单提交-->
            <ul class="inline" id="klx_search_info_module">
                <li>
                    <label for="klxId">
                        klxId
                        <input id="klxId" type="text">
                    </label>
                </li>
                <li>
                    <button id="klx_search_btn" type="button" class="btn btn-primary">查 询</button>
                </li>
            </ul>
        </fieldset>
    </form>
    <form id="wild_query_info_form" method="post" action="studentlist.vpage" class="form-horizontal" >
        <fieldset>
            <legend>学生模糊查询</legend>
            <ul class="inline" id="wild_query_info_module">
                <li>
                    <label for="schoolName">
                        所属地区
                        <input name="regionName" id="regionName" type="text" placeholder="模糊查询，请点选城市或地区" autocomplete="off"/>
                        <input name="regionId" id="regionId" type="hidden" value=""/>
                    </label>
                </li>
                <li>
                    <label for="schoolName">
                        所属学校
                        <input name="schoolName" id="schoolName" type="text" placeholder="模糊查询，请点选学校" autocomplete="off"/>
                        <input name="schoolId" id="schoolId" type="hidden" value=""/>
                        <input name="candidateSchoolIds" id="candidateSchoolIds" type="hidden" value=""/>
                    </label>
                </li>
                <li>
                    <label for="studentName">
                        学生姓名
                        <input name="studentName" id="studentName" type="text"/>
                    </label>
                </li>
                <li>
                    <button id="wild_query_info_btn" type="button" class="btn btn-primary">查 询</button>
                </li>
            </ul>
        </fieldset>
    </form>
</div>
<script>
    $(function () {
        <#if studentList?has_content && studentList?size == 1>
            window.open("studenthomepage.vpage?studentId=${studentList[0].studentId!''}", "_blank");
        </#if>
        // 学生精确查询
        $('#query_info_module input').keydown(function (evt) {
            if (evt.keyCode === 13) {
                $('#query_info_btn').click();
            }
        });
        // 学生精确查询-klxId查询
        $('#klx_search_info_module input').keydown(function (evt) {
            if (evt.keyCode === 13) {
                $('#klx_search_btn').click();
            }
        });
        // 学生模糊查询
        $('#wild_query_info_module input').keydown(function (evt) {
            if (evt.keyCode === 13) {
                $('#wild_query_info_btn').click();
            }
        });
        <#if conditionMap?has_content>
            $('#studentId').val('${(conditionMap.studentId?html)!''}');
            $('#studentName').val('${(conditionMap.studentName?html)!''}');
            $('#teacherName').val('${(conditionMap.teacherName?html)!''}');
            $('#parentName').val('${(conditionMap.parentName?html)!''}');
            $('#userMobile').val('${(conditionMap.userMobile?html)!''}');
            $('#schoolName').val('${(conditionMap.schoolName?html)!''}');
            $('#classLevel').val('${(conditionMap.classLevel?html)!''}');
        </#if>
        var limit = 15;
        $('#schoolName').autocomplete({
            source: function (query, process) {
                var regionName = $("#regionName").val();
                var regionCode = $("#regionName").attr("real-value") || "";
                if (regionName == '') {
                    regionCode = 0
                }
                $.post("searchschool.vpage", {
                    "schoolName": query,
                    "regionCode": regionCode,
                    "limit": limit
                }, function (respData) {
                    var schools = respData.schools;
                    var resultIds = new Array();
                    for (j = 0; j < schools.length; j++) {
                        resultIds.push(schools[j].ID)
                    }

                    $("#candidateSchoolIds").val(resultIds.join(","));

                    return process(respData.schools);
                });
            },
            formatItem: function (item) {
                return item["CNAME"];
            },
            setValue: function (item) {
                return {'data-value': item["CNAME"], 'real-value': item["ID"]};
            },
            items: limit
        });

        $('#regionName').autocomplete({
            source: function (query, process) {
                $.post("searchregion.vpage", {"regionName": query, "limit": limit}, function (respData) {
                    return process(respData.regions);
                });
            },
            formatItem: function (item) {
                return item["NAME"];
            },
            setValue: function (item) {
                return {'data-value': item["NAME"], 'real-value': item["CODE"]};
            },
            items: limit
        });
        $("#query_info_btn").click(function () { //获取文本框的实际值
            $("#query_info_form").submit();
        });
        $("#wild_query_info_btn").click(function () { //获取文本框的实际值
            var regionId = $("#regionName").attr("real-value") || "";
            $("#regionId").val(regionId);

            var schoolId = $("#schoolName").attr("real-value") || "";
            $("#schoolId").val(schoolId);

            if ($("#studentName").val() == "") {
                alert("请输入学生姓名!");
                return;
            }

            $("#wild_query_info_form").submit();
        });
        // klxID serach
        var vm = new Vue({
            el: '#klxStuInfoTable',
            data: {
                klxStuInfo: ''
            },
            mounted: function () {
                var _this = this;
                $(document).on('click', '#klx_search_btn', function () {
                    $.ajax({
                        url: '/crm/student/searchklxstudent.vpage',
                        type: 'GET',
                        data: {
                            klxId: $('#klxId').val()
                        },
                        success: function (res) {
                            if (res.success) {
                                $('#klxStuInfoTable').show();
                                _this.klxStuInfo = res.studentMap;
                            } else {
                                window.alert(res.info);
                            }
                        }
                    })
                });
            }
        });
    });
</script>
</#macro>