<ul class="nav nav-tabs" role="tablist" id="equatorUserInfoHeader">
    <li role="presentation"><a data-url="/equator/newwonderland/material" href="/equator/newwonderland/material/list.vpage?studentId=${studentId!''}">用户道具查询</a></li>
    <li role="presentation"><a data-url="/equator/mailservice/list" href="/equator/mailservice/list.vpage?studentId=${studentId!''}">用户邮件查询</a></li>
    <li role="presentation"><a data-url="/equator/newwonderland/tag" href="/equator/newwonderland/tag/user.vpage?studentId=${studentId!''}">用户标签查询</a></li>
    <li role="presentation"><a data-url="/equator/sapling/activity" href="/equator/sapling/activity/history.vpage?studentId=${studentId!''}">用户青苗参与情况</a></li>
    <li role="presentation"><a data-url="/equator/newwonderland/sapling/saplingInfo" href="/equator/newwonderland/sapling/saplingInfo.vpage?studentId=${studentId!''}">青苗乐园</a></li>
    <li role="presentation"><a data-url="/equator/newwonderland/sapling/letterproanswer" href="/equator/newwonderland/sapling/letterproanswer.vpage?studentId=${studentId!''}&saplingCommId=${saplingCommId!''}">青苗答题</a></li>
    <li role="presentation"><a data-url="/equator/newwonderland/sapling/classmatecircle" href="/equator/newwonderland/sapling/classmatecircle.vpage?studentId=${studentId!''}">校内班级圈</a></li>
    <li role="presentation"><a data-url="/equator/newwonderland/pet" href="/equator/newwonderland/pet/list.vpage?studentId=${studentId!''}">用户树灵查询</a></li>
    <li role="presentation"><a data-url="/equator/newwonderland/mathmind" href="/equator/newwonderland/mathmind/userInfo.vpage?studentId=${studentId!''}">数学思维</a></li>
    <li role="presentation"><a data-url="/equator/userwelfare" href="/equator/userwelfare/index.vpage?studentId=${studentId!''}">用户福利券</a></li>
</ul>

<script>
    $(function () {
        $("#equatorUserInfoHeader li").each(function () {
            if (window.location.href.indexOf($(this).find('a').attr("data-url")) >= 0) {
                $(this).addClass("active");
            }
        });
    });
</script>