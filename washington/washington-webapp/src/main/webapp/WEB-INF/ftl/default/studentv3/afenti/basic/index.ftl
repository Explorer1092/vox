<#import '../../apps/list.ftl' as apps>
<#import '../../layout/layout.ftl' as temp>
<@temp.page pageName='assignments'>
    <@sugar.capsule js=["jquery.flashswf"] />
<div class="t-app-container">
    <div class="t-app-inner">
        <!--tai-head-->
        <div class="ta-head">
            <h1>冒险岛</h1>
        </div>

        <!--ta-content-->
        <div class="ta-content">
            <dl class="ta-game-box">
                <@apps.appsList appName='冒险岛'/>
                <dd>
                    <div class="game-container-box">
                        <!--游戏区域-->
                        <div style="width:900px; margin:0 auto;">
                            <#assign file="AfentiBase"/>
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

                            <script type="text/javascript">
                                //换教材按钮
                                function changeBook(){
                                    $.get('/student/book/basic.vpage', function(data){
                                        $("#basicChangeBookBox").html(data);
                                    });
                                }

                                //购买不同周期的冒险岛（30天 90天）
                                function buyBasicByCycle(cycle){
                                    //cycle 30: thirtyDays, 90 : ninetyDays
                                    cycle = $17.isBlank(cycle) ? 'thirtyDays' : cycle;
                                    setTimeout(function(){
                                        window.location.href = '/apps/afenti/order/basic-cart.vpage?basicCycle=' + cycle + "&refer=" + cycle
                                    }, 200);
                                }

                                function jumpUrl(url){
                                    setTimeout(function(){
                                        top.location.href = url;
                                    }, 200);
                                }

                                function closeCallback(){
                                    setTimeout(function(){
                                        window.location.href = '/student/index.vpage'
                                    }, 200);
                                }


                                $(function(){
                                    var AfentiBase = 'AfentiBasic';
                                    //设置建议反馈类型
                                    feedBackInner.homeworkType = "AfentiExercise";
                                    feedBackInner.practiceName = AfentiBase;

                                    //flash参数
                                    p = ${flashVars!''};
                                    p.flashUrl = '<@flash.plugin name="${file!''}"/>'; //新的都用这个
                                    p.flashURL = '<@flash.plugin name="${file!''}"/>';
                                    p.domain = '${requestContext.getWebAppFullUrl('/')}';
                                    p.imgDomain = '<@app.link_shared href=""/>';
                                    p.debug = '${(!ProductDevelopment.isProductionEnv())?string}'; //判断当前是否线上
                                    p.flashId = AfentiBase;

                                    /** 加载Flash */
                                    $('#movie').getFlash({
                                        id       : AfentiBase,
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
<div id="basicChangeBookBox"><#--换教材--></div>
</@temp.page>
