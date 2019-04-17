<div class="p_menu_nav_box p_menu_nav_box_wx">
    <div class="mn_list mb_list">
        <ul style="width: 33.5%;">
            <li class="dropdownlink">
                <a class="ui-link" href="javascript:void (0);">
                    <span class="wx-menu">学习&报告</span>
                </a>
                <div class="more dropdownmenuitem" style="top: 0px;">
                    <p><a href="/parent/homework/common.vpage?page=reportindex">作业报告</a></p>
                    <#--<p><a href="/parent/product/list.vpage?_from=menu">课外乐园</a></p>-->
                    <p><a href="javascript:void(0);" id="outdooryald">趣味学习</a></p>
                    <i class="w-arrow-icon"></i>
                </div>
            </li>
        </ul>
        <ul style="width: 33%;">
            <li>
                <a class="ui-link" href="/parent/homework/common.vpage?page=smart">
                    <span class="wx-menu">课堂表现</span>
                </a>
            </li>
        </ul>
        <ul style="width: 33.5%;">
            <li class="dropdownlink">
                <a class="ui-link" href= "${(ProductConfig.getMainSiteBaseUrl())!''}/view/wx/parent/reading/index">
                    <span class="wx-menu">点读机</span>
                </a>
            </li>
        </ul>
    </div>
</div>
<script type="text/javascript">
    //添加学豆奖励菜单
    function getCookie(name) {
        var pattern = RegExp(name + "=.[^;]*");
        var matched = document.cookie.match(pattern);
        if (matched) {
            var cookie = matched[0].split('=');
            return cookie[1]
        }
        return ''
    }

    var _tempGetCookieId = getCookie("ssid");

    menuBeanReward();

    setInterval(function(){
        if(_tempGetCookieId != getCookie("ssid")){
            _tempGetCookieId = getCookie("ssid");
            menuBeanReward();
        }
    }, 1000);

    function menuBeanReward(){
        document.getElementById("menuBeanBtn").innerHTML = '<p><a href="${(ProductConfig.getMainSiteBaseUrl())!''}/parentMobile/rank/classes.vpage?sid='+ getCookie("ssid") +'">学豆奖励</a></p>';
        document.getElementById("outdooryald").href = '${(ProductConfig.getMainSiteBaseUrl())!''}/parentMobile/ucenter/shoppinginfolist.vpage?sid='+ getCookie("ssid");
    }
</script>
