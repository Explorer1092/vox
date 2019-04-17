<#macro rstaffInfomation>
<style type="text/css">
    div.jqi{font-size: 12px;}
    div.jqi button.jqidefaultbutton{ cursor: pointer;}
</style>
<span id="message_info_box" class="line_box">
    <span class="picture" style="cursor: pointer;">
        <a class="change_avatar" href="${(ProductConfig.getUcenterUrl())!''}/rstaff/center/index.vpage">
            <img class="avatar_img" width="30px" height="30px" alt="" title="" src="<@app.avatar href='${(currentUser.fetchImageUrl())!}'/>">
        </a>
    </span>
    <span class="name">
        <a class="change_avatar" href="${(ProductConfig.getUcenterUrl())!''}/rstaff/center/index.vpage">
            <span id="nav_real_name" class="realname">${(currentUser.profile.realname)!}</span> (${(currentUser.id)!})</span>
        <a/>
    <div class="popupBox box_info" style="display:none;  width:  160px;">
        <div class="arrow"></div>
        <p class="text_center spacing_vox">点击可进入个人设置页</p>
    </div>
</span>
<a id="logout" href="javascript:void(0);" title="退出" class="exit">退 出</a>
<script>
   $(function(){
       $("#message_info_box").hover(function(){
           $("#message_info_box .box_info").fadeIn(200);
       }, function(){
           $(".popupBox").hide();
       });

       <#--教研员退出系统-->
       $("#logout").on("click", function(){
           $17.tongji("教研员_退出系统", "");
           setTimeout(function(){ location.href = "/ucenter/logout.vpage"; }, 200);
           return false;
       });
   });
</script>
</#macro>

