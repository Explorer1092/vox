<#import "../layout.ftl" as ucenter>
<@ucenter.page title='个人中心' pageJs="ucenter">
<@sugar.capsule css=['jbox'] />
<div class="main pb_30">
    <div class="page_title_box">
        <span>家长号${pid!""}</span>
    </div>
    <h2 class="title_info_box title_info_green_box">绑定手机：${mobile!""}<a href="javascript:void(0);" class="js-changeMobile">更换</a></h2>
    <div class="content_box pb-60">
        <div class="inner">
            <ul>
            <#--<#if trusteeAvailable!false>-->
            <#--<li>-->
            <#--<a href="/parent/trustee/orderlist.vpage?utm_source=gudingrukou&utm_medium=huiyuanzhongxin-wodetuoguanban&utm_campaign=weixino2otuoguan-2first">-->
            <#--<i class="icon_center icon_center_09"></i>-->
            <#--<strong>我的托管班</strong>-->
            <#--</a>-->
            <#--</li>-->
            <#--</#if>-->
                <li>
                    <a href="/parent/ucenter/childreninfo.vpage">
                        <i class="icon_center icon_center_02"></i>
                        <strong>我的孩子</strong>
                        <span class="prompt" style="right: 257px;">${childCount!0}</span>
                    </a>
                </li>
                <li>
                    <a href="/parent/ucenter/orderlist.vpage">
                        <i class="icon_center icon_center_03"></i>
                        <strong>我的订单</strong>
                        <#--<span class="prompt" style="right: 257px;">${orderCount!0}</span>-->
                    </a>
                </li>
                <li>
                    <a href="/parent/ucenter/msgcenter.vpage">
                        <i class="icon_center icon_center_msg"></i>
                        <strong>我的消息</strong>
                    </a>
                </li>
                <li>
                    <a href="/parent/ucenter/resetstudentpwd.vpage">
                        <i class="icon_center icon_center_07"></i>
                        <strong>重置孩子密码</strong>
                    </a>
                </li>
            </ul>
        </div>
    </div>
</div>
<div style=" height: 125px;"></div>
<#include "../menu.ftl">
<script type="text/javascript">
    function pageLog(){
        require(['logger'], function(logger) {
            logger.log({
                module: 'ucenter',
                op: 'ucenter_pv_index'
            })
        })
    }
</script>
</@ucenter.page>