<#import "../../rebuildViewDir/mobile/layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="判假原因" pageJs="fakeTeacher" footerIndex=2>
<@sugar.capsule css=['new_home']/>
<style>
    body{background-color:#fff;}
</style>
<div class="homeText-box">
    <div style="display: none;">
        <span class="ht-right js-saveBtn" onclick="fakeTeacher()">保存</span>
    </div>
    <form id="iform" method="post" action="/mobile/teacher_fake/fake_teacher.vpage">
        <div class="ht-text">
            <textarea id="fakeNote" name="fakeNote" cols="30" rows="10" style="font-size:0.75rem;" placeholder="此处必须填写，请写明为何判定此老师为假老师。"></textarea>
        </div>
        <input type="hidden" id="teacherId" name="teacherId" value="${teacherId!}">
        <input type="hidden" id="is17ActiveTeacher" name="is17ActiveTeacher" value="${is17ActiveTeacher?string("true", "false")}">
        <input type="hidden" id="isKLXTeacher" name="isKLXTeacher" value="${isKLXTeacher?string("true", "false")}">
    </form>
</div>
<script>
    var is17ActiveTeacher = ${is17ActiveTeacher?string("true", "false")};
    var isKLXTeacher = ${isKLXTeacher?string("true", "false")};
    function fakeTeacher() {
        if (!$("#fakeNote").val()) {
            alert("请填写判假原因！");
            return;
        }
        var fakerData = {
            fakeNote : $('#fakeNote').val(),
            teacherId : $('#teacherId').val(),
            is17ActiveTeacher: is17ActiveTeacher,
            isKLXTeacher : isKLXTeacher
        };
        if (is17ActiveTeacher && !isKLXTeacher) {//一起作业老师展示
            if (window.confirm("近期，该老师为活跃老师，请再次确认是否举报！")) {
                $.post('fake_teacher.vpage', fakerData, function(res) {
                   if (res.success) {
                       alert("提交成功");
                       window.history.go(-2);
                   } else {
                       alert(res.info);
                   }
                });
            }
        } else {
            $.post('fake_teacher.vpage', fakerData, function(res) {
                if (res.success) {
                    alert("提交成功");
                    window.history.go(-2);
                } else {
                    alert(res.info);
                }
            });
        }
    }

</script>
</@layout.page>