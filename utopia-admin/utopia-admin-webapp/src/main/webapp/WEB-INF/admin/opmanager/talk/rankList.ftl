<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='17说' page_num=9>
<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<div id="main_container" class="span9">
    <legend style="font-weight: 700;">
        排行榜
    </legend>
    <div class="row-fluid">
        <div class="span12">
            <form class="well form-horizontal" method="get" action="/opmanager/talk/rankList.vpage">
                <ul class="inline">

                    <li>
                        <label>话题ID
                            <input type="text" id="topicId" name="topicId" value="${topicId!''}" placeholder="话题ID"/>
                        </label>
                    </li>
                    <li>
                        <label>最大点赞数
                            <input type="text" name="max" value="${max!''}" placeholder="最大点赞数"/>
                        </label>
                    </li>
                    <li>
                        <label>查询条数
                            <input type="text" name="limit" value="${limit!''}" placeholder="查询条数"/>
                        </label>
                    </li>
                    <li>
                        <label>
                            <select id="userType" name="userType">
                                <option value="2" <#if userType == 2>selected</#if>>家长</option>
                                <option value="3" <#if userType == 3>selected</#if>>学生</option>
                            </select>
                        </label>
                    </li>
                    <li>
                        <button type="submit" id="filter" class="btn btn-primary">查 询</button>
                    </li>
                    <li>
                        <button  id="force_reload" class="btn btn-primary">强制重载列表</button>
                    </li>

                </ul>
            </form>
        </div>
        <table class="table table-striped table-bordered">
            <thead>
            <tr>
                <th>序号</th>
                <th>话题ID</th>
                <th>用户ID</th>
                <th>学校ID</th>
                <th>话题ID</th>
                <th>话题选项ID</th>
                <th>点赞数</th>
            </tr>
            </thead>
            <tbody>
                <#if rank_list?? && rank_list?size gt 0>
                    <#assign index = 0>
                    <#list rank_list as reply>
                    <#assign  index = index+1>
                    <tr>
                        <td>${index}</td>
                        <td>${reply.id!''}</td>
                        <td>${reply.userId!''}</td>
                        <td>${reply.schoolId!''}</td>
                        <td>${reply.topicId!''}</td>
                        <td>${reply.optionId!''}</td>
                        <td>${reply.score!''}</td>
                    </tr>
                    </#list>
                </#if>
            </tbody>
        </table>
    </div>
</div>
<script>
    $("#force_reload").on("click",function(){
        var topicId = $("#topicId").val();
        $.post("/opmanager/talk/force_reload_rank.vpage",{
            "topicId":topicId,
            "userType":$("#userType").val()
        },function (msg) {
            if(!msg.success){
                alert(msg.info);
            }else{
                alert("重载成功");
            }
        });
    });
</script>
</@layout_default.page>