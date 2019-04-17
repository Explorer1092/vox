<#-- @ftlvariable name="homeworkDicts" type="java.util.List<com.voxlearning.utopia.service.newhomework.api.entity.HomeworkDict>" -->
<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='VOX_HOMEWORK_DICT LIST' page_num=4>

<!--/span-->
<div class="span9">
<#--//start-->
    <div style="padding: 0 0 10px;">
        <a href='upsertHomeworkDict.vpage' class="btn"><i class="icon-plus"></i> 添加</a>
    </div>
    <table class="table table-hover table-striped table-bordered ">
        <thead>
        <tr>
            <td>ID</td>
            <td>NAME</td>
            <td>LEVEL</td>
            <td>LEVEL_ID</td>
            <td>PARENT_ID</td>
            <td>PREFIX</td>
            <td>ENUM_VALUE</td>
            <td>BE_AUTHED</td>
            <td>操作</td>
        </tr>
        </thead>
        <tbody>
            <#list homeworkDicts![] as homeworkDict>
            <tr>
                <td>${homeworkDict.id}</td>
                <td>${homeworkDict.name}</td>
                <td>${homeworkDict.level!"null"}</td>
                <td>${homeworkDict.levelId!"null"}</td>
                <td>${homeworkDict.parentId!"null"}</td>
                <td>${homeworkDict.prefix}</td>
                <td>${homeworkDict.enumValue!"null"}</td>
                <td>${homeworkDict.beAuthed!"null"}</td>
                <td>
                    <a href='upsertHomeworkDict.vpage?id=${homeworkDict.id}' class="btn btn-success"><i
                            class="icon-edit icon-white"></i> 编辑</a>
                    <a id="delete_${homeworkDict.id}" href='javascript:void(0);' class="btn btn-inverse"><i
                            class="icon-remove icon-white"></i> 删除</a>
                </td>
            </tr>

            </#list>
        </tbody>
    </table>
<#--end//-->
</div>
<!--/span-->
<script type="text/javascript">
    $(function(){
        $('[id^="delete_"]').on('click', function(){
            if(!confirm("确定要删除吗？")){
                return false;
            }
            var id = $(this).attr("id").substring("delete_".length);
            $.post('deleteHomeworkDict.vpage',{
                id:id
            },function(data){
                if(!data.success){
                    alert(data.info);
                }else{
                    window.location.reload();
                }
            });
        });
    });
</script>
</@layout_default.page>