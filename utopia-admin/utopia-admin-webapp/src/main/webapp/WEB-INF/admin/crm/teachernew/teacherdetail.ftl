<#import "../../layout_default.ftl" as layout_default>
<#import "../headsearch.ftl" as headsearch>
<#import "./teacherinfoheader.ftl" as info_header/>
<@layout_default.page page_title="${teacherInfoHeaderMap.teacherName!''}(${teacherId!''})">


<div class="span9">
    <@headsearch.headSearch/>
    <@info_header.teacherHead/>
    <iframe id="teacherNew" name="teacherNew" src="/crm/teachernew/teacherinfo.vpage?teacherId=${teacherId!}" height="600" width="100%"></iframe>
</div>
<script type="text/javascript">
    function createMainSubTeacher(teacherId, clazzId, subject) {
        var jsonData = {teacherId: teacherId, clazzId: clazzId, subject: subject};
        $.ajax({
            type: 'post',
            url: "/crm/user/createmainsubaccount.vpage",
            data: JSON.stringify(jsonData),
            success: function(data){
                if(data.success){
                    alert("生成主副账号成功！");
                }else{
                    alert(data.info);
                }
            },
            contentType: 'application/json;charset=UTF-8'
        });
    }
    function queryStudentAuth() {
        $.get('authstuquery.vpage', {teacherId: ${teacherId!''}}, function (res) {
            console.info(res);
        });
    }
</script>
</@layout_default.page>