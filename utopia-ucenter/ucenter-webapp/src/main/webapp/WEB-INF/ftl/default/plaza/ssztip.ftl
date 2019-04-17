<div id="jisuan-template" class="jisuan-template" style="display: none">
    <div class="jisuan-box">
        <p class="p1">尊敬的老师：</p>
        <p class="p2">目前，极算已加入到“一起教育科技"大家庭。未来将携手旗下智能教育平台“一起作业”继续为您提供更优质的教育内容和产品体验，为更好的为您提供产品服务，我们将会更新“极算”（神算子）的相关功能页面，在您的使用过程中，任何疑问，欢迎致电官方客服400-160-171，您的每一次反馈都将是我们前行的基石，期待与您相遇每一天。</p>
        <p class="p3">让科技和教育一起，实现学习美好体验！</p>
        <p class="p4">2018年5月</p>
        <div class="start-use" id="start-use">开始体验</div>
    </div>
</div>

<script>
    // 从极算官网-老师端跳转过来的显示弹窗提示（#69626）
    function frontofAlertDeadLine () {
        var deadLineTime = new Date('10/30/2018 23:59:59').getTime();
        var nowTime = new Date().getTime();
        if (nowTime < deadLineTime) return true;
        else return false;
    }
    var fromShensz = '${(isShensz!false)?string}';
    if (fromShensz ==='true' && !$17.getCookieWithDefault("is_show_ssz_teacher_login_tip")  && frontofAlertDeadLine()) {
        $('#jisuan-template').show(); // 展示神算及弹窗
    }

    $(document).on('click', '#start-use', function (){
        $('#jisuan-template').hide();
        $17.setCookieOneDay("is_show_ssz_teacher_login_tip", "1", 365);
    });
</script>