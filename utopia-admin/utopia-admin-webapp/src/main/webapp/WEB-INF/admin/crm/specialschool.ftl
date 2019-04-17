<input type="hidden" value="<#if isCjlSchool?? && isCjlSchool>1<#else>0</#if>" id="isCjlSchool">
<input type="hidden" value="<#if isSeiueSchool?? && isSeiueSchool>1<#else>0</#if>" id="isSeiueSchool">

<script type="text/javascript">
    function checkSpecialSchool() {
        var isCjlSchool = $('#isCjlSchool').val();
        var isSeiueSchool = $('#isSeiueSchool').val();
        if (isCjlSchool == 1){
            if (!confirm("陈经纶中学高中部已接入校园内部数据平台，会自动更新老师、学生及班级数据！操作前请确认信息正确无误！！")) {
                return false;
            }
        }
        if (isSeiueSchool == 1){
            if (!confirm("大附中朝阳未来学校高中部已接入希悦数据平台，会自动更新老师、学生及班级数据！操作前请确认信息正确无误！！")) {
                return false;
            }
        }
        return true;
    }
</script>
