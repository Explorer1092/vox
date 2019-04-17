<#import "serviceV2Module.ftl" as layout/>
<@layout.page
title="一起教育科技产品理念及功能介绍"
keywords="产品介绍,一起作业功能"
description="一起作业网产品介绍：目标更清晰，效果更明显；同步各地教材，匹配不同教学需求；综合化目标体系，数据驱动结果导向；个性化学习路径，激发无限学习潜能；"
currentMenu="产品概念"
pageJs=['init']
pageJsFile={'init': 'public/script/project/help'}
>
<div class="zy-proContainer">
    <#--<div class="pro-bg JS-setHeight-header" style="background: url(<@app.link href="public/skin/default/v5/images/product-bg01.png"/>) center center no-repeat; height: 1115px; background-size: auto 100%">
        <div class="info" style="display: none;">
            <div class="font-big">目标更清晰，效果更明显</div>
            <p class="font-small">同步化各地教材，匹配不同教学需求</p>
            <p class="font-small">综合化目标体系，数据驱动结果导向</p>
            <p class="font-small">个性化学习路径，激发无限学习潜能</p>
        </div>
    </div>-->
    <div class="pro-bg JS-setHeight-header" style="display: none;">
        <div class="JS-conceptSwitch-main">
            <ul class="concept-slideBox slides">
                <li class="homeItem homeItem02">
                    <div class="homeItemBox">
                        <div class="section section-video">
                            <div class="mBox" style="width:630px; margin-left:-320px;">
                                <p class="txt"><span class="txt">数学平台：数据驱动教与学</span></p>
                                <a href="javascript:void(0);" class="playBtn" id="js-clickPlay2"></a>
                            </div>
                        </div>
                    </div>
                </li>
                <li class="homeItem homeItem03">
                    <div class="homeItemBox">
                    <div class="section section-video">
                        <div class="mBox" style="width:630px; margin-left:-320px;">
                            <p class="txt"><span class="txt">英语平台：换种思维看世界</span></p>
                            <a href="javascript:void(0);" class="playBtn" id="js-clickPlay3"></a>
                        </div>
                    </div>
                    </div>
                </li>
                <li class="homeItem homeItem01">
                    <div class="homeItemBox">
                        <div class="section section-video">
                            <div class="mBox" style="width:630px; margin-left:-320px;">
                                <p class="txt"><span class="txt">一道题的科技之旅</span></p>
                                <a href="javascript:void(0);" class="playBtn" id="js-clickPlay1"></a>
                            </div>
                        </div>
                    </div>
                </li>
            </ul>
        </div>
        <ul class="zy-scrollNav JS-conceptSwitch-mode">
            <li style="left: -12px;"><a>1</a></li>
            <li style="right: -1px;"><a>2</a></li>
            <li id="motherLi" style="left: 15px;"><a>3</a></li>
        </ul>
    </div>
    <script type="text/html" id="T:conceptMode1">
        <div style="margin:-40px -20px -20px;">
            <embed width="632" height="374"
                   flashvars="file=//v.17zuoye.cn/class/midmathvideo.mp4&amp;image=<@app.link href="public/skin/default/v5/images/project-vd1.jpg"/>&amp;width=632&amp;height=374"
                   allowfullscreen="true" quality="high" name="single"
                   src="<@app.link href='public/skin/project/about/images/flvplayer.swf'/>"
                   type="application/x-shockwave-flash">
            </embed>
        </div>
    </script>
    <script type="text/html" id="T:conceptMode2">
        <div style="margin:-40px -20px -20px;">
            <embed width="632" height="374"
                   flashvars="file=//v.17zuoye.cn/class/math512video.mp4&amp;image=<@app.link href="public/skin/default/v5/images/project-vd2.jpg"/>&amp;width=632&amp;height=374"
                   allowfullscreen="true" quality="high" name="single"
                   src="<@app.link href='public/skin/project/about/images/flvplayer.swf'/>"
                   type="application/x-shockwave-flash">
            </embed>
        </div>
    </script>
    <script type="text/html" id="T:conceptMode3">
        <div style="margin:-40px -20px -20px;">
            <embed width="632" height="374"
                   flashvars="file=//v.17zuoye.cn/class/eng512video.mp4&amp;image=<@app.link href="public/skin/default/v5/images/project-vd3.jpg"/>&amp;width=632&amp;height=374"
                   allowfullscreen="true" quality="high" name="single"
                   src="<@app.link href='public/skin/project/about/images/flvplayer.swf'/>"
                   type="application/x-shockwave-flash">
            </embed>
        </div>
    </script>
    <div class="pro-bg pro-bg-size JS-backScroll JS-setHeight" data-stellar-background-ratio="0.5" style="background: url(<@app.link href="public/skin/default/v5/images/product-bg02.jpg"/>) center 0 no-repeat fixed; height: 400px; ">
        <#--<img src="<@app.link href="public/skin/default/v5/images/product-bg02.jpg"/>">-->
    </div>
    <div class="pro-con">
        <div class="innerBox">
            <div class="pro-title">减负增效，教学好帮手</div>
            <ul>
                <li>
                    <div class="image"><img src="<@app.link href="public/skin/default/v5/images/product-01.png"/>"></div>
                    <div class="tips">一键推荐，自动批改，获取综合报告</div>
                </li>
                <li>
                    <div class="image"><img src="<@app.link href="public/skin/default/v5/images/product-02.png"/>"></div>
                    <div class="tips">组卷测验，系统生成，实现个性教学</div>
                </li>
                <li>
                    <div class="image"><img src="<@app.link href="public/skin/default/v5/images/product-03.png"/>"></div>
                    <div class="tips">课堂互动，课后沟通，实时学情管理</div>
                </li>
            </ul>
        </div>
    </div>
    <div class="pro-bg pro-bg-size JS-backScroll JS-setHeight" data-stellar-background-ratio="0.5" style="background: url(<@app.link href="public/skin/default/v5/images/product-bg03.jpg"/>) center 0 no-repeat fixed; height: 400px; ">
        <#--<img src="<@app.link href="public/skin/default/v5/images/product-bg03.jpg"/>">-->
    </div>
    <div class="pro-con">
        <div class="innerBox">
            <div class="pro-title">兴趣引导，自主化学习</div>
            <ul>
                <li>
                    <div class="image"><img src="<@app.link href="public/skin/default/v5/images/product-04.png"/>"></div>
                    <div class="tips">随时随地，丰富交互，多样化的练习方式</div>
                </li>
                <li>
                    <div class="image"><img src="<@app.link href="public/skin/default/v5/images/product-05.png"/>"></div>
                    <div class="tips">系统批改，及时反馈，自主掌握学习进度</div>
                </li>
                <li>
                    <div class="image"><img src="<@app.link href="public/skin/default/v5/images/product-06.png"/>"></div>
                    <div class="tips">互动 PK ，进阶成长，培养浓厚学习氛围</div>
                </li>
            </ul>
        </div>
    </div>
    <div class="pro-bg pro-bg-size JS-backScroll JS-setHeight" data-stellar-background-ratio="0.5" style="background: url(<@app.link href="public/skin/default/v5/images/product-bg04.jpg"/>) center 0 no-repeat fixed; height: 400px; background-size: auto 100%">
        <#--<img src="<@app.link href="public/skin/default/v5/images/product-bg04.jpg"/>">-->
    </div>
    <div class="pro-con conTwo">
        <div class="innerBox">
            <div class="pro-title">同步轨迹，智慧伴成长</div>
            <ul>
                <li>
                    <div class="image"><img src="<@app.link href="public/skin/default/v5/images/product-07.png"/>"></div>
                    <div class="tips">练习报告，实时接收，学习情况尽掌握</div>
                </li>
                <li>
                    <div class="image"><img src="<@app.link href="public/skin/default/v5/images/product-08.png"/>"></div>
                    <div class="tips">家校沟通，参与鼓励，点滴进步都可见</div>
                </li>
            </ul>
        </div>
    </div>
    <#--
    <div class="pro-bg Bg">
        <div class="pro-con">
            <div class="innerBox">
                <div class="pro-title titleDif">课题平台</div>
                <ul>
                    <li style="width: 50%;">
                        <div class="image"><img src="<@app.link href="public/skin/default/v5/images/project135-2.jpg"/>" style="width:270px; margin:0 auto;"></div>
                        <div class="tips" style="line-height:35px;margin:30px 120px 0;">“十三五”重点课题-基于混合式作业的学生发展核心素养评价与促进研究</div>
                    </li>
                    <li style="display: none;">
                        <div class="image"><img src="<@app.link href="public/skin/default/v5/images/project135-1.jpg"/>" style="width:270px; margin:0 auto;"></div>
                        <div class="tips" style="line-height:35px;margin:30px;">“十三五”规划课题-教育信息化背景下的中小学在线英语学习形式探究</div>
                    </li>
                    <li style="width: 50%;">
                        <div class="image"><img src="<@app.link href="public/skin/default/v5/images/project135-3.jpg"/>" style="width:270px; margin:0 auto;"></div>
                        <div class="tips" style="line-height:35px;margin:30px 120px 0;">“十三五”重点课题-教师家校合作能力建设研究</div>
                    </li>
                </ul>
            </div>
        </div>
    </div>
    -->
</div>
</@layout.page>