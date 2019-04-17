<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="CRM" page_num=3>
<link href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>

<div class="span9">
    <legend> ${(school.cname)!} (${(school.shortName)!})</legend>

    <#setting number_format="0.##">
    <div>
        <strong>1、UGC</strong>
        <table class="table table-bordered" id="triggerType">
            <tr>
                <th>序号</th>
                <th>答案</th>
                <th>回答人数</th>
                <th>答案占比</th>
            </tr>
            <tbody>
                <#if ugcSchoolDetailList?has_content>
                    <#list ugcSchoolDetailList as schoollist>
                    <tr>
                        <td>${schoollist_index+1}</td>
                        <#assign ugcSchoolName = schoollist.ugcSchoolName!"">
                        <td>${((ugcSchoolName=='NA')?string('',ugcSchoolName))!}</td>
                        <td>${(schoollist.count)!}</td>
                        <td>${((schoollist.percentage)!0) * 100}%</td>
                    </tr>
                    </#list>
                </#if>
            </tbody>
        </table>
    </div>

    <br>

    <div>
        <strong>2、校园大使</strong>
        <table>
            <#if schoolAmbassadorList?has_content>
                <#list schoolAmbassadorList as schoolAmbassador>
                    <tbody>
                    <tr>
                        <td class="info_td_txt">校名：<span class="info_td_txt">${schoolAmbassador.schoolName!} </span></td>
                        <td class="info_td_txt">分校：<span class="info_td_txt">${schoolAmbassador.isFx?string("是", "否")}</span></td>
                    </tr>
                    <tr>
                        <td class="info_td">省份：<span class="info_td_txt">${schoolAmbassador.provinceName!} </span></td>
                        <td class="info_td">城市：<span class="info_td_txt">${schoolAmbassador.city!}</span></td>
                        <td class="info_td">地区：<span class="info_td_txt">${schoolAmbassador.county!}</span></td>
                    </tr>
                    <tr>
                        <td class="info_td">地址：<span class="info_td_txt">${schoolAmbassador.address!}</span></td>
                    </tr>
                    <tr>
                        <td class="info_td">姓名：<span class="info_td_txt">${schoolAmbassador.userName!}</span></td>
                        <td class="info_td">电话：<span class="info_td_txt">${schoolAmbassador.phone!}</span></td>
                    </tr>
                    </tbody>
                </#list>
            </#if>
        </table>
    </div>

    <br>
    <#if trigger?? && trigger == 0>
        <legend></legend>
        <form id="shortname-form" action="/crm/ugc/updateSchoolShortName.vpage" method="post">
            学校简称：<input id="shortName" type="text" name="shortName">
            <input type="hidden" name="schoolId" value="${schoolId!}">
            <input id="PAGE" name="PAGE" type="hidden" value="${page!}"/>
            &nbsp;&nbsp;
            <a href="javascript:updateShortName();">提交</a>
            <br><br>
            <a href="javascript:dispatchTask(${schoolId!}, false);">普通任务</a>
            &nbsp;&nbsp;
            <a href="javascript:dispatchTask(${schoolId!}, true);">分校任务</a>
        </form>
    </#if>

    <script type="text/javascript">
        function updateShortName() {
            if ($.trim($("#shortName").val()) == "") {
                alert("学校简称填写有误！");
            } else {
                $("#shortname-form").submit();
            }
        }

        function dispatchTask(schoolId, branchSchool) {
            if (window.confirm("确定下发学校【" + schoolId + "】任务吗？")) {
                $.ajax({
                    url: "/crm/ugc/dispatch_task.vpage",
                    type: "POST",
                    data: {"schoolId": schoolId, "branchSchool": branchSchool},
                    success: function (data) {
                        if (data) {
                            alert("任务下发成功！");
                            window.location.href = "/crm/ugc/schoolname_notonly.vpage?trigger=0&PAGE=" + ${page!};
                        } else {
                            alert("任务下发失败！");
                        }
                    }
                });
            }
        }
    </script>
</div>
</@layout_default.page>
