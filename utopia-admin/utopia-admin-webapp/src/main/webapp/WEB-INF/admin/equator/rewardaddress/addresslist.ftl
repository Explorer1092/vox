<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='奖励地址查询' page_num=24>
<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/pagenation.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/knockout/dist/knockout.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/knockout.mapping/knockout.mapping.js"></script>

<div id="main_container" class="span9">
    <div>
        <#if error??>
            <div class="alert alert-error">
                <button type="button" class="close" data-dismiss="alert">×</button>
                <strong>${error!}</strong>
            </div>
        </#if>
    </div>
    <div>
        <#if successInfo??>
            <div class="alert">
                <button type="button" class="close" data-dismiss="alert">×</button>
                <strong>${successInfo!}</strong>
            </div>
        </#if>
    </div>


    <form class="form-horizontal" action="/equator/reward/address/addresslist.vpage" method="post"
          id="addressListForm">
        <ul class="inline">
            <textarea id="userList" name="userList" class="form-control push-target target-user" rows="20"
                      autofocus="autofocus" placeholder="一行输入一条学生ID，超过500条请分批处理">${userList?default("")}</textarea>

            <input type="button" class="btn btn-default" id="submit_query" name="submit_query" value="执行批量查询">
        </ul>
    </form>




    <#if userAddressInfoList ?? && userAddressInfoList?size gt 0 >
        <div>
            <table class="table table-hover table-striped table-bordered" style="font-size: 12px;">
                <tr>
                    <th width="50px;">学生ID</th>
                    <th width="50px;">学生姓名</th>
                    <th width="50px;">学生手机号</th>
                    <th width="50px;">老师姓名</th>
                    <th width="50px;">老师电话</th>

                    <th width="50px;">省</th>
                    <th width="50px;">市</th>
                    <th width="50px;">区</th>
                    <th width="50px;">学校名</th>

                    <th width="50px;">老师地址</th>
                </tr>

                <tbody id="tbody">
                    <#list userAddressInfoList as info>
                    <tr>
                        <td>${info.studentId!""}</td>
                        <td>${info.studentName!""}</td>
                        <td>${info.studentPhone!""}</td>
                        <td>${info.teacherName!""}</td>
                        <td>${info.teacherPhone!""}</td>
                        <td>${info.province!""}</td>
                        <td>${info.city!""}</td>
                        <td>${info.country!""}</td>
                        <td>${info.school!""}</td>
                        <td>${info.detailAddress!""}</td>

                    </tr>
                    </#list>

                </tbody>
            </table>
        </div>
    </#if>
</div>

<script>
    function isBlank(str) {
        return typeof str == 'undefined' || String(str) == 'null' || $.trim(str) == '';
    }

    $(function () {
        $("#submit_query").on("click", function () {
            $("#addressListForm").submit();
        });
    });
</script>
</@layout_default.page>