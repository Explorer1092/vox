<div class="mobileCRM-V2-header">
    <div class="inner">
        <div class="box">
            <div class="headerSearch" style="margin-right: 2.8rem;">
                <div class="select">
                    <select name="" id="search_type">
                        <option value="school"  <#if searchType?? && searchType == 1>selected = "selected" </#if>>学校</option>
                        <option value="teacher" <#if searchType?? && searchType == 2>selected = "selected" </#if>>老师</option>
                    </select>
                </div>

                <form action="/mobile/school/school_list.vpage" id="school" class="margin-l" <#if searchType?? && searchType == 2> style="display: none"</#if>>
                    <input id="searchType" name="searchType" type="hidden" value="1">
                    <input id="schoolKey" name="schoolKey" type="text" <#if schoolKey??> value="${schoolKey}" <#else>placeholder="请输入学校名/ID" </#if>>
                </form>
                <form action="/mobile/teacher/v2/teacher_list.vpage" id="teacher" class="margin-l" <#if searchType?? && searchType == 1> style="display: none"</#if>>
                    <input id="searchType" name="searchType" type="hidden" value="2">
                    <input id="teacherKey" name="teacherKey" type="text"  <#if teacherKey??> value="${teacherKey}" <#else> placeholder="请输入老师手机号/名称/ID"</#if>>
                </form>
            </div>
            <a href="javascript:void(0)" class="headerBtn">搜索</a>
        </div>
    </div>
</div>

<script type="text/javascript">
    $("#search_type").change(function(){
        $(".mobileCRM-V2-list").hide();
        if($('select#search_type option:selected').val()==="teacher"){
            window.location.href="/mobile/school/index.vpage?searchType=2";
        }
        else if($('select#search_type option:selected').val()==="school"){
            window.location.href="/mobile/school/index.vpage?searchType=1";
//            $("#school").show();
//            $("#search_area").show();
//            $("#teacher").hide();
        }
    });
    $("form:visible").on("submit",function(){
        var ele = $(this);
        var isSubmit = false;
        if(ele.attr("id")==="school"){
            var schoolValue = $("#schoolKey").val();
            if(schoolValue) {
                isSubmit = true;
            }else{
                alert("请输入学校名或ID")
            }
        }
        else if(ele.attr("id")==="teacher"){
            var teacherValue = $("#teacherKey").val();
            if(teacherValue) {
                isSubmit = true;
            }else{
                alert("请输入老师名称或ID");
            }
        }
        return isSubmit;
    });
    $(".headerBtn").click(function(){
        $("form:visible").submit();
    })
</script>