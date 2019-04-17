<#import '../../apps/list.ftl' as apps>
<#import '../../layout/layout.ftl' as temp>
<@temp.page pageName='assignments'>
    <@sugar.capsule js=["jquery.flashswf"] />

<div class="t-app-container">
    <div class="t-app-inner">
        <!--tai-head-->
        <div class="ta-head">
            <h1>通天塔</h1>
            <#if (.now < '2014-11-03 00:00:00'?datetime('yyyy-MM-dd HH:mm:ss')) >
                <div class="btn-area" style="position: relative;">
                    <a href="//cdn.17zuoye.com/static/project/halloweentowelv1/index.html" target="_blank"
                       class="t-halloween-btn" title="一起作业Halloween狂欢季"
                       style="position: absolute; right: 0; top: -15px;"></a>
                </div>
            </#if>
            <div class="ctn-area">
                <div class="ta-info">
                <#--start-->
                    <div class="v-textScrollInfo">
                        <#if battleOpen>
                                ${pageBlockContentGenerator.getPageBlockContentHtml('StudentIndex', 'BabelBattleOpenBanner')}
                            <#else>
                        ${pageBlockContentGenerator.getPageBlockContentHtml('StudentIndex', 'BabelBattleCloseBanner')}
                        </#if>
                    </div>
                    <script type="text/javascript">
                        $(function(){
                            $(".v-textScrollInfo").textScroll({
                                line : 1,
                                speed: 1000,
                                timer: 3000
                            });
                        });
                    </script>
                <#--end-->
                </div>
            </div>
        </div>

        <!--ta-content-->
        <div class="ta-content">
            <dl class="ta-game-box">
                <@apps.appsList appName='通天塔'/>
                <dd>
                    <div class="game-container-box">
                        <!--游戏区域-->
                        <div style="width:900px; margin:0 auto;" class="spacing_vox_tb">
                            <div id="movie">
                                <div id="install_flash_player_box" style="margin:20px;">
                                    <span id="install_download_tip" style="font:16px/1.125 '微软雅黑', 'Microsoft YaHei', Arial, '黑体'; color:#333; background-color:#eee; display:block; text-align:center; padding:70px 0; border:2px solid #ccc;">
                                        您未安装Flash Player插件，请 <a href="http://down.tech.sina.com.cn/content/1149.html" target="_blank">［点击这里］</a> 下载并安装。
                                        <br/><br/>
                                        <span>
                                            如果已经是最新版，<a href="http://get.adobe.com/flashplayer" target="_top">请允许加载flash</a>
                                        </span>
                                    </span>
                                </div>
                            </div>

                            <#include "../../../flash/prepareflashloadercdntypes.ftl"/>

                            <script type="text/javascript">

                                //换教材 subject == ENGLISH / MATH
                                function changeBook(subject){
                                    $.get('/student/babel/api/babel.vpage?subject=' + subject, function(data){
                                        $("#babelChangeBookBox").html(data);
                                    });
                                }

                                //根据通天塔-flash的高度 调整页面的高度
                                function updateHeight(height){
                                    $("#movie").closest('dd').css({ height: height + "px" });
                                    $("#movie").find('object').css({ height: height + "px" });
                                }

                                $(function(){

                                    var babel = 'Babel';

                                    //设置建议反馈类型
                                    feedBackInner.homeworkType = "babel";
                                    feedBackInner.practiceName = babel;

                                    //flash参数
                                    p = {};

                                    p.flashUrl = '<@app.link href="/resources/apps/flashv1/babel/Babel.swf" />';
                                    p.domain = '${requestContext.getWebAppFullUrl('/')}';
                                    p.imgDomain = '<@app.link_shared href="" />';
                                    p.debug = '${(!ProductDevelopment.isProductionEnv())?string}'; //判断当前是否线上
                                    p.flashId = babel;

                                    /** 加载Flash */
                                    //pk 维护公告
                                    <#if (.now gte '2099-08-11 13:00:00'?datetime('yyyy-MM-dd HH:mm:ss')) >
                                        $('#movie').html('<h1 style="padding: 20px 0; text-align: center;">PK维护中...</h1>');
                                        $17.tongji("学生",'pk','维护页');
                                    <#else>
                                        $('#movie').getFlash({
                                            id       : babel,
                                            width    : '900',
                                            height   : '600',
                                            movie    : '<@flash.plugin name="Preloader"/>',
                                            flashvars: p
                                        });
                                    </#if>
                                });
                            </script>
                        </div>
                    </div>
                </dd>
            </dl>
        </div>
    </div>
</div>
<div id="babelChangeBookBox"><#--换教材--></div>
</@temp.page>
