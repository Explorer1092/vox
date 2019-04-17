<#import "../layout.ftl" as homework>
<@homework.page title="老师的感谢" pageJs="">
    <style>
        /*m-head*/
        .m-head{ background-color: #359bff; padding: 1rem 0;}
        .m-main{ }
        .m-content{ margin: 0 0.75rem;}
        /*w-footer-menu*/
        .w-footer-menu{ height: 4.74rem; width: 100%; }
        .w-footer-menu .foot-inner{ z-index: 10; position: fixed; border: 1px solid #dbdcde; left: 0; bottom: 0; width: 100%; padding:1rem 0;  background-color: rgba(255,255,255,.9);}
        .w-footer-menu-static .foot-inner{ position: static;}
        .w-footer-menu .btn_invite_mark_blue{ margin: 0 2%;}
        /*btn_mark*/
        .btn_invite_mark{ background-color: #06cb9f; border-bottom: 4px solid #da344f; font-size: 1rem; color: #fff; border-radius: 6px; cursor: pointer; display: inline-block; text-align: center; text-decoration: none; padding: 0.8rem 0;}
        .btn_invite_mark_blue{background-color: #359bff;}
        .btn_mark_block{ display: block;}
        .btn_mark_noBorder{ border: 0;}
        .w-clear{ width: 100%; height: 0; clear: both; font: 0/0 "";}
        /*t-thanksParentFont-box*/
        .t-thanksParentFont-box{}
        .t-thanksParentFont-box p{ font-size: 0.75rem; color: #fff; text-align: center; line-height: 150%;}
        .t-thanksParentFont-box p.bigFont{ font-size: 1.125rem;}
        /*t-somePerson-list*/
        .t-somePerson-list{ border: 1px solid #ebebeb; line-height: 150%; border-radius: 2px; height: 21rem; background-color: #fff; padding: 1rem; margin: 1rem 0;}
        .t-somePerson-list textarea{width: 100%; height: 100%; display: block; border: 0; font-size: 0.9rem; line-height: 150%; color: #464646;}
    </style>
    <div class="m-main">
        <!--头部信息-->
        <div class="m-head">
            <div class="t-thanksParentFont-box">
                <p class="bigFont">感谢家长</p>
            </div>
        </div>
        <div class="m-content">
            <div class="t-somePerson-list">
                <textarea class="txt" readonly>${gratitude!""}</textarea>
            </div>
        </div>
        <!--底部按钮-->
        <div class="w-footer-menu w-footer-menu-static">
            <div class="foot-inner">
                <a class="btn_invite_mark btn_invite_mark_blue btn_mark_block btn_mark_noBorder" href="/parent/homework/index.vpage?_from=flowerthanks"  style="color: #EFF2F6;">查看该作业</a>
            </div>
        </div>
    </div>
</@homework.page>
<script type="text/javascript">
    require(['logger'], function(logger) {
        logger.log({
            s0: !!LoggerProxy && LoggerProxy.openId,
            module: 'flower',
            op: 'flower_parent_see_thanks_pv',
            userId: logger.getCookie('uid')
        })
    })
</script>