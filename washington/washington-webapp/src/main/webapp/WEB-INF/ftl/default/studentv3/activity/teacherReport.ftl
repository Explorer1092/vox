<script type="text/html" id="T:学生验证老师是否取消大使">
    <style>
        .ifcancelteacher label{width: 110px;display: inline-block;text-align: center;}
        .ifcancelteache input{ margin-right: 3px !important;}
        .ifcancelteacher p{margin-bottom: 20px;}
        .ifcancelteacher .subjectli{text-align: left; }
        .ifcancelteacher .tipinfo{display: none;font-size: 12px;color: red; margin-top: 20px; }
    </style>
    <div  class='w-ag-center ifcancelteacher'>
        <p style="color: #fa7252;">安全提示：账号异常，请验证真实信息</p>
    <#if (data.reportTeachers?size gt 0)!false>
        <#list data.reportTeachers as t>
            <div class="subjectli" data-teacherId="${t.teacherId}" data-subject="${t.subject}" data-teacherName="${t.teacherName}">
                <p>问题：你的<span ><#if t.subject=="ENGLISH">英语<#elseif t.subject=="MATH">数学</#if></span>老师是<span >${t.teacherName}</span>吗？</p>
                <label><input type="radio" name="ifteacher" value="true">是</label><label><input type="radio" name="ifteacher" value="false">否</label>
            </div>
        </#list>
    </#if>
        <p class="tipinfo">请选择是否为你的老师</p>
    </div>
</script>
<script type="text/javascript">
    (function($){
        function ifCancelOrStopTeacher(){
            $17.setCookieOneDay("ifcanter", "1", 1 );
        <#if (data.reportTeachers?size gt 0)!false>
            var englishId = "", mathId = "", englishFlag = "", mathFlag = "";
            $.prompt(template("T:学生验证老师是否取消大使", {}), {
                title: "安全提示",
                buttons: { "提交": true },
                position: {width: 500},
                loaded: function(){
                    $(document).on("click", ".subjectli", function(){
                        $("p.tipinfo").hide();
                        if($(this).attr("data-subject") == "ENGLISH"){
                            englishId = $(this).attr("data-teacherId");
                            englishFlag = $(this).find("input:checked").val();
                            if(englishFlag!="true" && englishFlag !="false"){englishFlag="";}
                        }else if($(this).attr("data-subject") == "MATH"){
                            mathId = $(this).attr("data-teacherId");
                            mathFlag = $(this).find("input:checked").val();
                            if(mathFlag!="true" && mathFlag !="false"){mathFlag="";}
                        }
                    });
                },
                submit: function(e, v){
                    if((englishFlag != "") ||  (mathFlag != "")){
                        App.postJSON("/student/collectreportinfo.vpage",{
                            englishId : englishId,
                            mathId : mathId,
                            englishFlag : englishFlag,
                            mathFlag : mathFlag
                        }, function(data){
                            if(data.success){
                                $17.setCookieOneDay("ifcanter", "3", 3 );
                            }
                        });
                    }else{
                        e.preventDefault();
                        $("p.tipinfo").show();
                    }
                }
            });
        </#if>
        }

        $.extend($, {
            ifCancelOrStopTeacher : ifCancelOrStopTeacher
        });
    }($));
</script>