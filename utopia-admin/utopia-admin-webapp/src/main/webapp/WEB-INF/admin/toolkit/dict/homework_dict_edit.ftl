<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="VOX_HOMEWORK_DICT EDIT" page_num=4>

    <!--/span-->
 <fieldset>
     <legend>VOX_HOMEWORK_DICT</legend>
 </fieldset>
    <div class="span9">
            <ul class="inline">
                <li>
                    <label>
                        ID
                        <input type="text" id="id" value="${homeworkDict.id!''}" />
                    </label>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <label>
                        NAME
                        <input type="text" id="name"  value="${homeworkDict.name!''}" />
                    </label>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <label>
                        LEVEL
                        <input type="text" id="level" value="${homeworkDict.level!''}" />
                    </label>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <label>
                        LEVEL_ID
                        <input type="text" id="levelId" value="${homeworkDict.levelId!''}" />
                    </label>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <label>
                        PARENT_ID
                        <input type="text" id="parentId" value="${homeworkDict.parentId!''}" />
                    </label>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <label>
                        PREFIX
                        <input type="text" id="prefix" value="${homeworkDict.prefix!''}" />
                    </label>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <label>
                        ENUM_VALUE
                        <input type="text" id="enumValue" value="${homeworkDict.enumValue!''}" />
                    </label>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <label>
                        BE_AUTHED
                        <input type="text" id="beAuthed" value="${homeworkDict.beAuthed!''}" />
                    </label>
                </li>
            </ul>

            <p><input type="submit" id ="btn_sub" class="btn btn-primary btn-large" value="提交" /></p>
    </div>
    <!--/span-->
<script type="text/javascript">
    $(function(){
        $("#btn_sub").on('click', function(){
            $.post('upsertHomeworkDict.vpage',{
                id:$.trim($("#id").val()),
                name:$.trim($("#name").val()),
                level:$.trim($("#level").val()),
                levelId:$.trim($("#levelId").val()),
                parentId:$.trim($("#parentId").val()),
                prefix:$.trim($("#prefix").val()),
                enumValue:$.trim($("#enumValue").val()),
                beAuthed:$.trim($("#beAuthed").val())
            },function(data){
                if (data.success) {
                    window.location.href = "/toolkit/homework/fetchHomeworkDictList.vpage";
                } else {
                    alert(data.info)
                }
            });
        });
    });
</script>
</@layout_default.page>