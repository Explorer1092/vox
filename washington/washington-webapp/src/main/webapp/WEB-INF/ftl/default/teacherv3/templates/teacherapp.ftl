<style type="text/css">
    .homeCode-flayer{position:fixed;z-index:101;top:420px;right:0;width:160px;height:150px;padding:25px 10px 10px 10px;font-size:14px;color:#4e5656}
    .homeCode-flayer .close-icon{position:absolute;top:0;right:30px;height:20px;line-height:20px;width:20px;cursor:pointer;font-size:28px;font-weight:400;color:#000;text-align:center}
    .homeCode-flayer .code{margin:0 auto 10px auto;width:100px;height:100px}
    .homeCode-flayer .code img{width:100px;height:100px}
    .homeCode-flayer .tips{width:100%;text-align:center;line-height:18px}
</style>
<div class="homeCode-flayer" id="teacherAppCode" style="display: none;">
    <div class="close-icon" id="appCodeClose">×</div>
    <div class="code"><img src="<@app.link href="public/skin/teacherv3/images/card-loadCode.png"/>"></div>
    <p class="tips">扫码下载老师app<br>随时随地布置、<br>检查作业</p>
</div>
<script type="text/javascript">
    $(function(){
        if(!$17.getCookieWithDefault("teacherAppCode")){
            var $teacherAppCode = $("#teacherAppCode");
            $teacherAppCode.show();
            $("#appCodeClose").on("click",function(){
                $17.setCookieOneDay("teacherAppCode","1",1);
                $17.voxLog({
                    module : "m_xk7LWgJj",
                    op  : "o_yL0JgvYO"
                });
                $teacherAppCode.hide();
            });
        }else{
            $("#teacherAppCode").hide();
        }
    });
</script>