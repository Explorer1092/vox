<#import "guideLayout.ftl" as temp />
<@temp.page>
<div class="register_box">
    <div id="signup_form_box">
        <div class="containerDiscipline">
            <div class="inner">
                <div class="infoBox">
                    <h4><span class="correct"></span>恭喜注册成功！ 您的一起作业学号是：<strong>${currentUser.id}</strong>  <a href="/ucenter/fetchaccount.vpage">下载学号</a></h4>
                    <p>您下次可以使用 学号 <strong>${currentUser.id}</strong> 或者您的邮箱 <strong>${currentUserProfileEmail!}</strong> 登录一起作业</p>
                </div>
                <div class="btnBox edge_vox_top">
                    <a href="${(ProductConfig.getMainSiteBaseUrl())!''}/teacher/index.vpage" class="btn_mark btn_mark_primary">开始一起作业</a>
                </div>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript">
    $(function(){
        var subject = null;

        $("#subject a").on("click", function(){
            var $self = $(this);
            $("#subject a").removeClass("active");
            $self.addClass("active");
            subject = $self.attr("data-subject");
            $("#saveBtn").addClass("btn_mark_primary");
        });

        $("#saveBtn").on("click", function(){
            var $self = $(this);
            if($self.hasClass("btn_mark_primary")){
                setTimeout(function(){
                    location.href = "/teacher/guide/selectsubject.vpage?subject=" + subject;
                }, 500);
            }else{
                $17.alert("请选择学科");
            }
        });
    });
</script>
</@temp.page>