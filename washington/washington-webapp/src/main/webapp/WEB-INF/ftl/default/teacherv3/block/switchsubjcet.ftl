<#--是否多学科-->
<#assign multiSubject = ((currentTeacherDetail.subjects?size gt 1)!false)/>
<#assign detailSubjectsList = (currentTeacherDetail.subjects)![]/>
<#assign curSubject = curSubject!((currentTeacherDetail.subject)!'')/>
<#assign curSubjectText = curSubjectText!((currentTeacherDetail.subject.value)!'')/>

<#--智慧课堂班级List-->
<#if specifiedSubjects?? >
    <#assign detailSubjectsList = (specifiedSubjects)![]/>
</#if>

<#if multiSubject>
    <#--<span style="font-size: 12px;">切换学科：</span>-->
    <select id="subjectSelector" class="w-int" style="width: 100px;">
        <#list detailSubjectsList as subject>
            <option value="${subject}" <#if subject == curSubject>selected="selected" </#if>>${subject.value}</option>
        </#list>
    </select>
    <script type="text/javascript">
        $(function(){
            $(document).on('change', '#subjectSelector', function(){
                var subject = $(this).val();
                if(subject == 0){
                    return false;
                }

                if(location.pathname.indexOf("systemclazz/clazzindex.vpage") > -1){
                    location.href = "#/teacher/clazz/managedclazzlist.vpage?subject=" + subject;
                }else{
                    location.href = setSubject() + "subject=" + subject;
                }
            });

            function setSubject () {
                var $query = location.search;

                if($query && $query.indexOf("subject=") > -1){
                    $query = $query.replace(new RegExp('[\?\&]subject=([^\&]*)(\&?)', 'i'), "");
                }

                if($query == ""){
                    return location.pathname + "?";
                }else{
                    if($query.indexOf("?") > -1){
                        return location.pathname + $query + "&";
                    }else{
                        return location.pathname + "?" + $query + "&";
                    }
                }
            }
        });
    </script>
</#if>