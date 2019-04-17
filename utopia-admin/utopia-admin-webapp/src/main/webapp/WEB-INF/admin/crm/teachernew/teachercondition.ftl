<#macro teacherCondition>

<div>
    <form id="teacherCondition" method="post" action="teacherlist.vpage" class="form-horizontal">
        <fieldset>
            <legend>老师查询</legend>
            <ul class="inline">推荐查询
                <#--<lable><input id="all" name="all" type="checkbox" class="pop_condition" <#if inputConditionMap?? && inputConditionMap["all"]== true>checked="checked" value="1" <#else >value="0" </#if>  style="margin-top: -3px;"/>全部</lable>-->
                <#--<#if unusualStatus?? && unusualStatus?has_content>-->
                    <#--<#list unusualStatus?keys as status>-->
                        <#--<lable><input id="${status}" name="${status}" type="checkbox" class="pop_condition"   <#if inputConditionMap?? && inputConditionMap[status]== true>checked="checked" value="1" <#else >value="0" </#if> style="margin-top: -3px;"/>${unusualStatus[status].desc!''}</lable>-->
                    <#--</#list>-->
                <#--</#if>-->
                <lable><input id="keycity" name="keycity" type="checkbox" class="pop_condition" <#if inputConditionMap?? && inputConditionMap["keycity"]== true>checked="checked" value="1" <#else >value="0" </#if>  style="margin-top: -3px;"/>重点城市</lable>
                <#--<lable><input id="authPostponed" name="authPostponed" type="checkbox" class="pop_condition" <#if inputConditionMap?? && inputConditionMap["authPostponed"]== true>checked="checked" value="1" <#else >value="0" </#if>  style="margin-top: -3px;"/>自动认证未通过</lable>-->
                <lable><input id="used90Days" name="used90Days" type="checkbox" class="pop_condition" <#if inputConditionMap?? && inputConditionMap["used90Days"]== true>checked="checked" value="1" <#else >value="0" </#if>  style="margin-top: -3px;"/>90天内使用过</lable>
            </ul>
            <ul class="inline">
                <li><label for="teacherId">老师学号 <input name="teacherId" id="teacherId" <#if inputConditionMap?has_content>value="${(inputConditionMap["teacherId"]?html)!''}" </#if>type="text"/></label></li>
                <li><label for="teacherName">老师姓名 <input name="teacherName" id="teacherName" <#if inputConditionMap?has_content>value="${(inputConditionMap["teacherName"]?html)!''}" </#if>type="text"/></label></li>
                <li><label for="teacherMobile">老师手机 <input name="teacherMobile" id="teacherMobile" <#if inputConditionMap?has_content>value="${(inputConditionMap["teacherMobile"]?html)!''}" </#if>type="text" /></label></li>
            </ul>
            <ul class="inline">
                <#--<li><label for="fakeTeacher">排假标识-->
                    <#--<select id="fakeTeacher" name="fakeTeacher">-->
                        <#--<option value="">--请选择--</option>-->
                        <#--<option <#if inputConditionMap?has_content><#if inputConditionMap["fakeTeacher"]?string == "false">selected="selected"</#if></#if> value="false">未判定</option>-->
                        <#--<option <#if inputConditionMap?has_content><#if inputConditionMap["fakeTeacher"]?string == "AUTO_VALIDATION_JOB">selected="selected"</#if></#if> value="AUTO_VALIDATION_JOB">系统判定(姓名/学校等)</option>-->
                        <#--<option <#if inputConditionMap?has_content><#if inputConditionMap["fakeTeacher"]?string == "AUTO_VALIDATION_ONLINE">selected="selected"</#if></#if> value="AUTO_VALIDATION_ONLINE">系统判定(作业/IP等)</option>-->
                        <#--<option <#if inputConditionMap?has_content><#if inputConditionMap["fakeTeacher"]?string == "MANUAL_VALIDATION">selected="selected"</#if></#if> value="MANUAL_VALIDATION">人工判定</option>-->
                    <#--</select>-->
                <#--</label>-->
                <#--</li>-->
                <li><label for="regTime">注册时间
                    <input id="regTimeStart" name="regTimeStart" style="width: 6em;" <#if inputConditionMap?has_content>value="${(inputConditionMap["regTimeStart"]?html)!''}" </#if> data-role="date" data-inline="true" type="text" placeholder="2014-11-26"/>----
                    <input id="regTimeEnd" name="regTimeEnd" style="width: 6em;" <#if inputConditionMap?has_content>value="${(inputConditionMap["regTimeEnd"]?html)!''}" </#if> data-role="date" data-inline="true" type="text" placeholder="2014-11-26"/>
                </label></li>
                <#--<li><label for="regType">注册方式-->
                    <#--<select id="webSourceCategory" name="webSourceCategory">-->
                        <#--<option value="">--请选择--</option>-->
                        <#--<option <#if inputConditionMap?has_content><#if inputConditionMap["webSourceCategory"]?string == "SELF_REG">selected="selected"</#if></#if> value="SELF_REG">自主注册</option>-->
                        <#--<option <#if inputConditionMap?has_content><#if inputConditionMap["webSourceCategory"]?string == "INVITE_REG">selected="selected"</#if></#if> value="INVITE_REG">邀请注册</option>-->
                        <#--<option <#if inputConditionMap?has_content><#if inputConditionMap["webSourceCategory"]?string == "BATCH_REG">selected="selected"</#if></#if> value="BATCH_REG">批量注册</option>-->
                        <#--<option <#if inputConditionMap?has_content><#if inputConditionMap["webSourceCategory"]?string == "APP_REG">selected="selected"</#if></#if> value="APP_REG">第三方注册</option>-->
                        <#--<option <#if inputConditionMap?has_content><#if inputConditionMap["webSourceCategory"]?string == "OTHER_REG">selected="selected"</#if></#if> value="OTHER_REG">其他</option>-->
                    <#--</select>-->
                <#--</label></li>-->
            </ul>
            <ul class="inline">
                <li><label for="school">老师学校 <input name="school" id="school" <#if inputConditionMap?has_content>value="${(inputConditionMap["school"]?html)!''}" </#if> type="text"/></label></li>
                <li><label for="subject">老师学科
                    <select id="subject" name="subject">
                        <option value="">--请选择--</option>
                        <option <#if inputConditionMap?has_content><#if inputConditionMap["subject"]?string == "ENGLISH">selected="selected"</#if></#if> value="ENGLISH">英语</option>
                        <option <#if inputConditionMap?has_content><#if inputConditionMap["subject"]?string == "MATH">selected="selected"</#if></#if> value="MATH">数学</option>
                        <option <#if inputConditionMap?has_content><#if inputConditionMap["subject"]?string == "CHINESE">selected="selected"</#if></#if> value="CHINESE">语文</option>
                    </select>
                </label></li>
                <li>
                    <label for="schoolLevel">
                        学校类别：
                        <select id="schoolLevel" data-init='false' name="schoolLevel" class="multiple district_select">
                            <option value="-1">全部</option>
                            <option value="1" <#if inputConditionMap?has_content><#if inputConditionMap.schoolLevel?? && inputConditionMap.schoolLevel == "JUNIOR">selected="selected"</#if></#if>>小学</option>
                            <option value="2" <#if inputConditionMap?has_content><#if inputConditionMap.schoolLevel?? && inputConditionMap.schoolLevel == "MIDDLE">selected="selected"</#if></#if>>中学</option>
                        </select>
                    </label>
                </li>
                <li><label for="authCondReached">认证条件
                    <select id="authCondReached" name="authCondReached">
                        <option value="">--请选择--</option>
                        <option  <#if inputConditionMap?has_content><#if inputConditionMap["authCondReached"]?string == "authCond1Reached">selected="selected"</#if></#if> value="authCond1Reached">仅未满足一：8个学生3次作业</option>
                        <option  <#if inputConditionMap?has_content><#if inputConditionMap["authCondReached"]?string == "authCond2Reached">selected="selected"</#if></#if> value="authCond2Reached">仅未满足二：设置姓名并绑定手机</option>
                        <option  <#if inputConditionMap?has_content><#if inputConditionMap["authCondReached"]?string == "authCond3Reached">selected="selected"</#if></#if> value="authCond3Reached">仅未满足三: 至少3名学生，每人绑定了家长手机或自己手机</option>
                        <option  <#if inputConditionMap?has_content><#if inputConditionMap["authCondReached"]?string == "reachedButNotAuthed">selected="selected"</#if></#if> value="reachedButNotAuthed">满足条件但未认证</option>
                        <option  <#if inputConditionMap?has_content><#if inputConditionMap["authCondReached"]?string == "sysAutoAuthed">selected="selected"</#if></#if> value="sysAutoAuthed">系统自动认证</option>
                    </select>
                </label>
                </li>
            </ul>
            <ul class="inline">
                <li><label for="provCode">所属地区
                    <select id="provCode" name="provCode" class="multiple district_select" next_level="cityCode">
                        <option value="-1">全国</option>
                        <#if provinceList??>
                            <#list provinceList as p>
                                <option value="${p.key}"<#if inputConditionMap?? && p.key ==inputConditionMap["provCode"]>selected</#if>>${p.value}</option>
                            </#list>
                        </#if>
                    </select>
                </label>
                </li>
                <li>
                    <label for="cityCode">
                        所在市：
                        <select id="cityCode" data-init='false' name="cityCode" class="multiple district_select" next_level="countyCode">
                            <option value="-1">全部</option>
                        </select>
                    </label>
                </li>
                <li>
                    <label for="countyCode">
                        所在区：
                        <select id="countyCode" data-init='false' name="countyCode" class="multiple district_select">
                            <option value="-1">全部</option>
                        </select>
                    </label>
                </li>
            </ul>
            <ul class="inline">
                <li><input id='submit' type="submit" class="btn btn-primary" value="查 询">
                    <input id='reset_condition' type="button" class="btn btn-primary" value="重置查询条件">
                    <input type="hidden" id="currentPage" name="currentPage" <#if currentPage??> value="${currentPage}" <#else > value="1"</#if> />
                    <input type="hidden" id="totalPage" name="totalPage" value="${totalPage!1}"/>
                    <input type="hidden" id="provCodeCon" <#if inputConditionMap??>value="${inputConditionMap["provCode"]!}"</#if>>
                    <input type="hidden" id="cityCodeCon" <#if inputConditionMap??>value="${inputConditionMap["cityCode"]!}"</#if>>
                    <input type="hidden" id="countyCodeCon" <#if inputConditionMap??>value="${inputConditionMap["countyCode"]!}"</#if>>
                <#--呼叫排序的参数-->
                    <input type="hidden" id="queryParam" name="queryParam" value="${queryParam!''}">
                </li>
                &nbsp;&nbsp;&nbsp;&nbsp;
                <li>
                    <input id="unusualStart" name="startTime" type="text" style="width: 6em;" placeholder="开始日期" class="date"/> --
                    <input id="unusualEnd" name="unusualEnd" type="text" style="width: 6em;" placeholder="截止日期" class="date"/>
                </li>
                <li>
                    <input type="button" class="btn btn-primary" value="导出异常老师" onclick="exportUnusualTeacher()">
                </li>
            </ul>
        </fieldset>
    </form>
</div>

<script src="${requestContext.webAppContextPath}/public/legacy/common.js"></script>
<script type="text/javascript">
    $(function () {
        <#if teacherMapList?has_content && teacherMapList?size == 1>
            <#assign teacher = teacherMapList[0]>
            window.open("teacherdetail.vpage?teacherId=${teacher["teacherId"]!}", "_blank");
        </#if>
        $(document).keydown(function (evt) {
            if (evt.keyCode === 13) {
                $('#submit').click();
            }
        });
        <#--var unusualStatusNames = ${unusualStatusNames};-->
        var totalCount = ${totalCount!};
        $('[id^="regTime"]').datepicker({
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
        $(".pop_condition").on("click", function () {
            var box = this;
            if (box.id == "all") {
                if (box.checked == false) {
                    // for (var key in unusualStatusNames) {
                    //     var tmpDom = document.getElementById(unusualStatusNames[key]);
                    //     box.value = 0;
                    //     tmpDom.value = 0
                    //     tmpDom.checked = false;
                    // }
                } else {
                    // for (var key in unusualStatusNames) {
                    //     var tmpDom = document.getElementById(unusualStatusNames[key]);
                    //     box.value = 1;
                    //     tmpDom.value = 1;
                    //     tmpDom.checked = true;
                    // }
                }
            } else {
                if (box.checked == true) {
                    box.value = 1;
                    var allchecked = true;
                    // for (var key in unusualStatusNames) {
                    //     var tmpDom = document.getElementById(unusualStatusNames[key]);
                    //     if (tmpDom.checked == false) {
                    //         allchecked = false;
                    //         break;
                    //     }
                    // }
                    if (allchecked) {
                        var allDom = document.getElementById("all");
                        allDom.value = 0;
                        allDom.checked = true;
                    }
                } else {
                    box.value = 0;
                    var allDom = document.getElementById("all");
                    allDom.value = 0;
                    allDom.checked = false;
                }
            }
        });

        function clearNextLevel(obj) {
            if (obj.attr("next_level")) {
                clearNextLevel($("#" + obj.attr("next_level")).html('<option value=""></option>'));
            }
        }

        $("#reset_condition").on("click", function () {
            $("#teacherId").val("");
            $("#teacherName").val("");
            $("#teacherMobile").val("");
            $("#fakeTeacher").val("");
            $("#regTimeStart").val("");
            $("#regTimeEnd").val("");
            $("#webSourceCategory").val("");
            $("#school").val("");
            $("#subject").val("");
            $("#authCondReached").val("");
            $("#provCode").val(-1);
            $("#cityCode").val(-1);
            $("#countyCode").val(-1);
            $("#lifeCycle").val("");
            $("#currentPage").val(1);
            $("#queryParam").val("");
            // for (var key in unusualStatusNames) {
            //     var tmpDom = document.getElementById(unusualStatusNames[key]);
            //     tmpDom.value = 0;
            //     tmpDom.checked = false;
            // }
            var keyCity = document.getElementById("keycity");
            keyCity.value = 0;
            keyCity.checked = false;
            var authPostponed = document.getElementById("authPostponed");
            authPostponed.value = 0;
            authPostponed.checked = false;
        });


        //分页部分
        var currentPage = $("#currentPage").val() * 1;
        var totalPage = $("#totalPage").val() * 1;
        var provCode = $("#provCodeCon").val();
        var cityCode = $("#cityCodeCon").val();
        var countyCode = $("#countyCodeCon").val();

        $("#submit").on("click", function (e) {
            if (e.hasOwnProperty('originalEvent')) {
                //由于有那个排序了，分成了两个请求。点查询之前确认查询的action
                $("#teacherCondition").attr("action", "teacherlist.vpage");
                $("#currentPage").val(1);
            } else {
                $("#currentPage").val(currentPage);
            }
            var teacherId = $("#teacherId").val();
            var teacherName = $("#teacherName").val();
            var teacherMobile = $("#teacherMobile").val();
            var fakeTeacher = $("#fakeTeacher").val();
            var regTimeStart = $("#regTimeStart").val();
            var regTimeEnd = $("#regTimeEnd").val();
            var webSourceCategory = $("#webSourceCategory").val();
            var school = $("#school").val();
            var subject = $("#subject").val();
            var authCondReached = $("#authCondReached").val();
            var provCode = $("#provCode").val();
            var cityCode = $("#cityCode").val();
            var countyCode = $("#countyCode").val();
            if (teacherId == "" && teacherName == "" && teacherMobile == "" && fakeTeacher == "" && regTimeStart == "" && regTimeEnd == ""
                    && webSourceCategory == "" && school == "" && subject == "" && authCondReached == "" && provCode == -1
                    && cityCode == -1 && countyCode == -1) {
                alert("请输入查询条件");
                return false;
            } else {
                return true;
            }

        });

        $("#next_page").click(function () {
            if (currentPage < totalPage) {
                currentPage++;
            }
            $("#submit").trigger("click");
        });

        $("#pre_page").click(function () {
            if (currentPage > 1) {
                currentPage--;
            }
            $("#submit").trigger("click");
        });

        $("#goto_page").click(function () {
            var gotoPage = $("#goto_page_num").val();
            if (gotoPage == undefined || gotoPage < 1 || gotoPage > totalPage || gotoPage == "") {
                alert("请输入正确的跳转页数");
                return false;
            }
            currentPage = gotoPage;
            $("#submit").trigger("click");
        });

        $("#first_page").click(function () {
            if (currentPage != 1) {
                currentPage = 1;
            }
            $("#submit").trigger("click");
        });

        $("#last_page").click(function () {
            if (currentPage != totalPage) {
                currentPage = totalPage;
            }
            $("#submit").trigger("click");
        });

        //呼叫记录排序
        $('[id^="array_"]').on("click", function () {
            if (totalCount > 500) {
                alert("查询结果过多(>500)，请细化条件后再排序");
                return false;
            } else if (totalCount == 1) {
                alert("就一条记录你还要我排序？");
                return false;
            }
            var id = $(this).attr("id");
            var queryParam = id.substr("array_".length);
            $("#queryParam").val(queryParam);
            currentPage = 1;
            //呼叫记录的form提交的action
            // $("#teacherCondition").attr("action", "query_teacher_list.vpage");
            $("#submit").trigger("click");
        });


        //地区部分
        $(".district_select").on("change", function () {
            var html = null;
            var $this = $(this);
            var next_level = $this.attr("next_level");
            var regionCode = $this.val();
            if (next_level) {
                next_level = $("#" + next_level);
                clearNextLevel($this);
                $.ajax({
                    type: "post",
                    url: "regionlist.vpage",
                    data: {
                        regionCode: regionCode
                    },
                    success: function (data) {
                        html = '';
                        var regionList = data;
                        for (var i in regionList) {
                            html += '<option value="' + regionList[i]["code"] + '"';
                            if (regionList[i]["code"] == provCode || regionList[i]["code"] == cityCode || regionList[i]["code"] == countyCode) {
                                html += 'selected="selected"';
                            }
                            html += '>' + regionList[i]["name"] + '</option>';
                        }
                        next_level.html(html);
                        next_level.trigger('change');
                    }
                });
            }
        });
        $("#provCode").trigger('change');

        dater.render();
    });

    function exportUnusualTeacher() {
        var startTime = $("#unusualStart").val();
        var endTime = $("#unusualEnd").val();
        if (blankString(startTime) || blankString(endTime)) {
            alert("请指定开始日期和截止日期！");
            return false;
        }
        var start = dater.parse("yy-mm-dd", startTime);
        var end = dater.parse("yy-mm-dd", endTime);
        if (start == null || end == null) {
            alert("开始日期或截止日期有误！");
            return false;
        }
        var maxTime = 1000 * 60 * 60 * 24 * 7;
        if (end.getTime() - start.getTime() > maxTime) {
            alert("单次导出，最多可导出7天的数据！");
            return false;
        }
        window.location.href = "/crm/teachernew/unusual_teacher_export.vpage?startTime=" + startTime + "&endTime=" + endTime;
    }
</script>
</#macro>