<div id="jisuan-template" class="jisuan-template" style="display: none">
    <div class="jisuan-box">
        <p class="p1">尊敬的用户：</p>
        <p class="p2">非常感谢您一直以来对极算长期的支持和关爱！目前，极算已加入到“一起科技”大家庭，“一起科技”旗下“一起作业”将携手“极算”为您提供更好的服务。在账户迁移过程中，欢迎致电官方客服400-160-1717解决您的疑问，对此造成的不便，尽请谅解！未来，我们将为您提供更好的服务。</p>
        <p class="p3">让科技和教育一起，实现学习美好体验！</p>
        <p class="p4">2018年5月</p>
        <div class="start-use" id="start-use">开始体验</div>
    </div>
</div>

<script>
    $(document).on('click', '#start-use', function (){
        $('#jisuan-template').hide();
        $17.setCookieOneDay("is_show_ssz_student_tip", "1", 365);
        checkNameError();
    });
</script>