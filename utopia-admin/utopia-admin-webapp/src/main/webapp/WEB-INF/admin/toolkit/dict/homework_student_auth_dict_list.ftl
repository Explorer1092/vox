<#-- @ftlvariable name="homeworkStudentAuthDicts" type="java.util.List<com.voxlearning.utopia.service.newhomework.api.entity.HomeworkStudentAuthDict>" -->
<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='VOX_HOMEWORK_DICT LIST' page_num=4>

<!--/span-->
<div class="span9">
<#--//start-->
    <div style="padding: 0 0 10px;">
        <a href='upsertHomeworkStudentAuthDict.vpage' class="btn"><i class="icon-plus"></i> 添加</a>
    </div>
    <table class="table table-hover table-striped table-bordered ">
        <thead>
        <tr>
            <td>ID</td>
            <td>HomeworkType</td>
            <td>HomeworkType_name</td>
            <td>HomeworkFormType</td>
            <td>Homeworkform_name</td>
            <td>操作</td>
        </tr>
        </thead>
        <tbody>
            <#list homeworkStudentAuthDicts![] as homeworkStudentAuthDict>
            <tr>
                <td>${homeworkStudentAuthDict.id}</td>
                <td>${homeworkStudentAuthDict.homeworkType}</td>
                <td>${homeworkStudentAuthDict.homeworkFormName}</td>
                <td>${homeworkStudentAuthDict.homeworkFormType}</td>
                <td>${homeworkStudentAuthDict.homeworkFormName}</td>
                <td>
                    <a href='upsertHomeworkStudentAuthDict.vpage?id=${homeworkStudentAuthDict.id}' class="btn btn-success"><i
                            class="icon-edit icon-white"></i> 编辑</a>
                    <a id="delete_${homeworkStudentAuthDict.id}" href='javascript:void(0);' class="btn btn-inverse"><i
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
            $.post('deleteHomeworkStudentAuthDict.vpage',{
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