<#macro rstaffInformation>
<span class="line_box" id="message_info_box">
    <span style="cursor: pointer;" class="picture">
        <a href="${(ProductConfig.getUcenterUrl())!''}/rstaff/center/index.vpage" class="change_avatar">
            <img width="30px" height="30px" src="<@app.avatar href='${(currentUser.fetchImageUrl())!}'/>" title="" alt="" class="avatar_img">
        </a>
    </span>
    <span class="name">
        <a href="${(ProductConfig.getUcenterUrl())!''}/rstaff/center/index.vpage" class="change_avatar">
            <span class="realname" id="nav_real_name">${(currentUser.profile.realname)!}</span> (${(currentUser.id)!})
        </a>
    </span>
    <a>
        <div style="width: 160px; display: none;" class="popupBox box_info">
            <div class="arrow"></div>
            <p class="text_center spacing_vox">点击可进入个人设置页</p>
        </div>
    </a>
</span>
<a class="exit" title="退出" href="javascript:void(0);" id="logout">退 出</a>
<script type="text/javascript">
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