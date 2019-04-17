<div class="w-popup-info-small">
    <div class="is-close"><span class="w-icon-arrow w-icon-arrow-white"></span></div>
    <h3>帮助</h3>
    <div class="is-content">
        ${authContent!"遇到问题了？<br/>看看认证任务攻略吧！"}
    </div>
    <div class="is-btn">
        <a href="${authLink!"http://help.17zuoye.com/?p=1"}" target="_blank" class="w-btn w-btn-orange w-btn-mini">查看详情</a>
    </div>
</div>
<script type="text/javascript">
    $(function(){
        $(".w-popup-info-small .is-close").on("click", function(){
            $(this).parent().remove();
        });
    });
</script>
