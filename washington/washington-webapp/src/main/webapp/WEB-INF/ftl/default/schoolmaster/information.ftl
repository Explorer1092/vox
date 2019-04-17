<#macro schoolmasterInformation>
<span class="line_box" id="message_info_box">
    <span style="cursor: pointer;" class="picture">
            <img width="30px" height="30px" src="<@app.avatar href='${(currentUser.fetchImageUrl())!}'/>" title=""
                 alt="" class="avatar_img">
    </span>
    <span class="name">
            <span class="realname" id="nav_real_name">${(currentUser.profile.realname)!}</span> (${(currentUser.id)!})
    </span>
</span>
<a class="exit" title="退出" href="javascript:void(0);" id="logout">退 出</a>
<script type="text/javascript">
    $(function () {
    <#--教研员退出系统-->
        $("#logout").on("click", function () {
            $17.tongji("教研员_退出系统", "");
            setTimeout(function () {
                location.href = "/ucenter/logout.vpage";
            }, 200);
            return false;
        });
    });
</script>
</#macro>