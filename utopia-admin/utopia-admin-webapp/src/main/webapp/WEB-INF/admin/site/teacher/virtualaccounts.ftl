<#--随便写个页面，不做跳转了-->
<script src="${requestContext.webAppContextPath}/public/js/jquery-1.9.1.min.js"></script>
<head>创建虚拟帐号</head>
<hr />
添加虚拟老师
<hr width="20%" align="left"/>
<hr width="50%" align="left"/>
添加虚拟学生
<hr width="20%" align="left"/>
老师ID<input id="teacherId" /><br>
年级<input id="grade" /><br>
班级<input id="clazzName" />(多个班级按逗号分割)<br>
学生数<input id="studentNum" /><br>
学生姓名前缀<input id="studentNamePrefix"/><br>
<button id="addStudents" style="height=40px;width=100px;" >添加学生</button>

<script type="text/javascript">
    $(function(){
        $("#addStudents").click(function() {
            var teacherId = $("#teacherId").val();
            var grade = $("#grade").val();
            var clazzName = $("#clazzName").val();
            var studentNum = $("#studentNum").val();
            var studentNamePrefix = $("#studentNamePrefix").val();

            $.post("/site/teacher/addvirtualstudents.vpage",
                    {
                        teacherId: teacherId,
                        grade: grade,
                        clazzName: clazzName,
                        studentNum: studentNum,
                        studentNamePrefix: studentNamePrefix
                    },
            function(e) {
                if (e.success) {
                    alert("创建学生成功");
                } else {
                    alert(e.info);
                }
            })
        })
    });
</script>
