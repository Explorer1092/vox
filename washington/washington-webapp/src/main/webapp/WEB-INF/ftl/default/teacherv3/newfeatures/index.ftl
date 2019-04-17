<#import "../../nuwa/teachershellv3.ftl" as temp />
<@temp.page>
<style>
    *{ padding: 0; margin: 0; list-style: none;}
    .new-feature-box{}
    .new-feature-box dl{ color: #383a4b;overflow: hidden; clear: both; padding: 0 30px; background-color: #fff; *zoom: 1; height: 220px;}
    .new-feature-box dl.gray{background-color: #fafafa;}
    .new-feature-box dl dt{ float: left; width: 390px;}
    .new-feature-box dl .featureBg{ background: url(<@app.link href="public/skin/teacherv3/images/newfeatures/feature.png"/>) no-repeat 3000px 3000px; width: 355px; height: 210px; display: inline-block;}
    .new-feature-box dl .featureBg_01{ background-position: 0 0;}
    .new-feature-box dl .featureBg_02{ background-position: 0 -210px;}
    .new-feature-box dl .featureBg_03{ background-position: 0 -420px;}
    .new-feature-box dl .featureBg_04{ background-position: 0 -630px;}
    .new-feature-box dl dd{ margin-left: 390px;}
    .new-feature-box dl dd h4{ font-size: 18px; padding: 20px 0 10px 0; font-weight: normal;}
    .new-feature-box dl dd p{ font-size: 13px;}
    .new-feature-box dl dd p.orange{color: #ff9900; }
    .new-feature-box dl dd .more{ margin:15px 0 0 0; }
    .new-feature-box dl dd .btn_blue{ width: 150px; padding: 7px 0; background-color: #189cfb; border-radius: 3px; color: #fff; display: inline-block; text-decoration: none;}
    .new-feature-box dl dd .btn_blue:hover{ background-color: #2fa7fe;}
    .new-feature-box dl dd .btn_blue:active{background-color: #0083e2;}
    .new-feature-box dl dd .btn_blue i.icon_go{ background: url(<@app.link href="public/skin/teacherv3/images/newfeatures/feature_go.png"/>) no-repeat 0 0; width: 49px; height: 25px; border-right: 1px solid #5ebbfd; display: inline-block; vertical-align: middle;}
    .new-feature-box dl dd .btn_blue span{ width: 90px; text-align: center; font-size: 16px; line-height: 25px; display: inline-block;}
</style>
<div class="v-time-bar w-base">
    <div class="w-base-title">
        <h3>新功能</h3>
    </div>
    <!--template container-->
    <div class="w-base-container">
        <!--//start-->
        <div class="new-feature-box">
            <dl>
                <dt><i class="featureBg featureBg_01"></i></dt>
                <dd>
                    <h4>听力材料TTS <span class="w-ft-well">(仅限英语老师)</span></h4>
                    <p class="orange">上线时间：2014年9月1日</p>
                    <p>免费把文字制作成发音标准的声音文件啦</p>
                    <p>男音、女音、童音任选，生成流畅纯正的发音</p>
                    <div class="more">
                        <a class="btn_blue" href="/tts/listening.vpage" data-tongji-content="新功能-听力材料-去体验" onclick="$17.tongji('新功能-听力材料-去体验');">
                            <i class="icon_go"></i>
                            <span>去体验</span>
                        </a>
                    </div>
                </dd>
            </dl>
            <dl class="gray">
                <dt><i class="featureBg featureBg_03"></i></dt>
                <dd>
                    <h4>智慧课堂</h4>
                    <p class="orange">上线时间：2014年9月1日</p>
                    <p>活跃课堂气氛、调动学生积极性的神器</p>
                    <p>系统免费赠送每班100个学豆哦</p>
                    <div class="more">
                        <a class="btn_blue" href="/teacher/smartclazz/list.vpage" data-tongji-content="新功能-智慧课堂-去体验" onclick="$17.tongji('新功能-智慧课堂-去体验');">
                            <i class="icon_go"></i>
                            <span>去体验</span>
                        </a>
                    </div>
                </dd>
            </dl>
            <dl>
                <dt><i class="featureBg featureBg_04"></i></dt>
                <dd>
                    <h4>绘本阅读
                        <span class="w-ft-well">(仅限认证英语老师)</span>
                        <#if currentUser.fetchCertificationState() != "SUCCESS" ><a href="${(ProductConfig.getUcenterUrl())!''}/teacher/center/index.vpage#/teacher/center/myauthenticate.vpage" class="w-blue">去认证</a></#if>
                    </h4>
                    <p class="orange">上线时间：2014年9月1日</p>
                    <p>原汁原味阅读，图文并茂、外教发音</p>
                    <p>配有重点词注释，教材同步推送配套练习</p>
                    <div class="more">
                        <a class="btn_blue showViewContent" href="javascript:void(0);" data-type="readBooks" data-tongji-content="新功能-阅读绘本-去体验">
                            <i class="icon_go"></i>
                            <span>去体验</span>
                        </a>
                    </div>
                </dd>
            </dl>
            <dl class="gray">
                <dt><i class="featureBg featureBg_02"></i></dt>
                <dd>
                    <h4>语音引擎 <span class="w-ft-well">(仅限英语老师)</span></h4>
                    <p class="orange">上线时间：2014年9月1日</p>
                    <p>全面升级，打分更智能，更准确，更专业</p>
                    <p>评测覆盖音素、音节、单词、句子、段落各个范围</p>
                    <div class="more">
                        <a class="btn_blue showViewContent" href="javascript:void (0);" data-url="/flash/loader/selfstudy-114-0-100130-100491-80800550014110.vpage" data-tongji-content="新功能-语音引擎-去体验">
                            <i class="icon_go"></i>
                            <span>去体验</span>
                        </a>
                    </div>
                </dd>
            </dl>
        </div>
        <!--end//-->
    </div>
</div>
<script id="t:预览" type="text/html">
    <div id="showViewContent">
        <div id="install_flash_player_box" style="margin:20px; display: none;">
            <div id="install_download_tip" style="font:16px/1.125 '微软雅黑', 'Microsoft YaHei', Arial, '黑体'; color:#333; background-color:#eee; display:block; text-align:center; padding:70px 0; border:2px solid #ccc;">
                <a href="<@app.client_setup_url />" target="_blank">您的系统组件需要升级。请点这里<span style="color:red;">下载</span>并<span style="color:red;">运行</span> “一起作业安装程序”。</a>
            </div>
        </div>
    </div>
</script>
<script type="text/javascript">
    function nextHomeWork(){
        $.prompt.close();
    }

    $(function(){
        LeftMenu.focus("newFeatures");

        //练习
        $(document).on("click", ".showViewContent", function(){
            var url = $(this).attr("data-url");
            var dataFrame = '<iframe class="vox17zuoyeIframe" src="'+url+'" width="700" marginwidth="0" height="530" marginheight="0" scrolling="no" frameborder="0"></iframe>'
            var textCtn = "语音引擎";
            var widthBox = 720;

            if($(this).data("type") == "readBooks"){
                textCtn = "<span style='color: #f00;'>阅读绘本，更多阅读绘本尽在布置作业页面</span>";
                dataFrame = '<object width="900" height="600" data="http://cdn-cc.17zuoye.com/resources/apps/flash/Reading.swf?_=20141024172856" type="application/x-shockwave-flash"><param name="movie" value="http://cdn-cc.17zuoye.com/resources/apps/flash/Reading.swf?_=20141024172856"><param name="allowScriptAccess" value="always"><param name="allowFullScreen" value="true"><param name="flashvars" value="isPreview=0&gameDataURL=http%3A%2F%2Fwww.17zuoye.com%2Fappdata%2Fflash%2FReading%2Fobtain-ENGLISH-4445.vpage&nextHomeWork=closeReviewWindow&tts_url=http%3A%2F%2Fwww.17zuoye.com%2Ftts.vpage&isTeacher=1&imgDomain=http%3A%2F%2Fcdn-cc.17zuoye.com%2F&domain=http%3A%2F%2Fwww.17zuoye.com"><param name="wmode" value="opaque"></object>'
                widthBox = 940;
            }

            $17.tongji($(this).data("tongji-content"));

            $.prompt("<div id='viewContentBox'></div>", {
                title    : textCtn,
                buttons  : {},
                position : { width: widthBox},
                loaded: function(){
                    $("#viewContentBox").html(dataFrame);
                }
            });
        });
    });
</script>
</@temp.page>