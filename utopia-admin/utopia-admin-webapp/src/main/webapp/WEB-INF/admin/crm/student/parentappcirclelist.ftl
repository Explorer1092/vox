<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="CRM" page_num=3>
<div id="main_container" class="span9">
    <div>
        <fieldset>
            <legend>${userName!}(${userId!})作业和通知列表</legend>
            <li>
                <form action="groupcircle.vpage" method="get">
                    每个Group查询条数：<input name="limit" value="${limist!}"/>
                    <input type="hidden" name="userId" value="${userId!}"/>
                    <input type="submit" class="btn" value="查询"/>
                </form>
            </li>

        </fieldset>
        <strong>作业和通知</strong>
        <table class="table table-hover table-striped table-bordered">
            <tr id="title">
                <th> 班组ID</th>
                <th> 学科</th>
                <th> 消息类型</th>
                <th> 消息ID</th>
                <th> 生成时间</th>
                <th> 数据库是否存在</th>
                <th> 操作</th>
            </tr>
            <#if circleList?has_content>
                <#list circleList as circle>
                    <tr>
                        <td>${circle.groupId!''}</td>
                        <td>${circle.subject!''}</td>
                        <td>${circle.groupCircleType!''}</td>
                        <td>${circle.typeId!''}</td>
                        <td>${(circle.createDate)?number_to_datetime}</td>
                        <td>
                            <#if circle.db_exists == true>是 <#else ><span style="color: red">否</span></#if>
                        </td>
                        <td><button class="delete_circle" data-name="${circle.groupId!''}#${circle.groupCircleType!''}#${circle.typeId!''}#${circle.createDate!''}">删除</button></td>
                    </tr>
                </#list>
            </#if>
        </table>
    </div>
</div>
<script>
    $(".delete_circle").on("click", function () {
        var str = $(this).attr("data-name");
        var param = str.split("#");
        if (param.length != 4) {
            alert("参数错误");
        }
        $.post("delete_group_circle.vpage",{
            groupId:param[0],
            groupCircleType:param[1],
            typeId:param[2],
            createDate:param[3]
        },function (msg) {
            if(msg.success){
                if(confirm("删除成功")){
                    location.reload();
                }
            }else{
                alert(msg.info);
            }
        })
    });
</script>
</@layout_default.page>