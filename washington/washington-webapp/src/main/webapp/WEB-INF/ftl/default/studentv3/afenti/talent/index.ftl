<#import '../../apps/list.ftl' as apps>
<#import '../../layout/layout.ftl' as temp>
<@temp.page pageName='assignments'>
    <@sugar.capsule js=["jquery.flashswf"] />
<div class="t-app-container">
    <div class="t-app-inner">
        <!--tai-head-->
        <div class="ta-head">
            <h1>单词达人</h1>
        </div>

        <!--ta-content-->
        <div class="ta-content">
            <dl class="ta-game-box">
                <@apps.appsList appName='单词达人'/>
                <dd>
                    <div class="game-container-box">
                        <!--游戏区域-->
                        <div style="width:900px; margin:0 auto;">
                            <#assign file="AfentiTalent"/>
                            <div id="movie">
                                <div id="install_flash_player_box" style="margin:20px;">
                                        <span id="install_download_tip"
                                              style="font:16px/1.125 '微软雅黑', 'Microsoft YaHei', Arial, '黑体'; color:#333; background-color:#eee; display:block; text-align:center; padding:70px 0; border:2px solid #ccc;">
                                            您未安装Flash Player插件，请 <a
                                                href="http://down.tech.sina.com.cn/content/1149.html" target="_blank">［点击这里］</a> 下载并安装。
                                            <br/><br/>
                                            <span>
                                                如果已经是最新版，<a href="http://get.adobe.com/flashplayer" target="_top">请允许加载flash</a>
                                            </span>
                                        </span>
                                </div>
                            </div>

                            <#include "../../../flash/prepareflashloadercdntypes.ftl"/>

                            <script type="text/javascript">
                                //退出按钮
                                function closeCallback(){
                                    setTimeout(function(){
                                        location.href = "/student/index.vpage";
                                    }, 200);
                                }

                                $(function(){
                                    var AfentiTalent = 'AfentiTalent';
                                    //设置建议反馈类型
                                    feedBackInner.homeworkType = "AfentiExercise";
                                    feedBackInner.practiceName = AfentiTalent;

                                    //flash参数
                                    p = ${flashVars!''};
                                    p.flashURL = '<@flash.plugin name="${file!''}"/>';
                                    p.domain = '${requestContext.getWebAppFullUrl('/')}';
                                    p.imgDomain = '<@app.link_shared href=""/>';
                                    p.close = 'closeCallback';
                                    p.completedUrl = '/${completedUrl!}';
                                    p.debug = '${(!ProductDevelopment.isProductionEnv())?string}'; //判断当前是否线上
                                    p.flashId = AfentiTalent;

                                    /** 加载Flash */
                                    $('#movie').getFlash({
                                        id       : AfentiTalent,
                                        width    : '900',
                                        height   : '600',
                                        movie    : '<@flash.plugin name="Preloader"/>',
                                        flashvars: p
                                    });
                                });
                            </script>
                        </div>
                    </div>
                </dd>
            </dl>
        </div>
    </div>
</div>
</@temp.page>
