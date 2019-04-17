<#import "../layout.ftl" as openClassMain>
<@openClassMain.page title='公开课介绍' pageJs="ocdetail">
    <@sugar.capsule css=['openclass'] />
<div class="train-wrap bg-blue">
    <div class="train02-box">
        <div class="inner">
            <h2 class="tit01"></h2>
            <p>平时忙碌的晚上，难得闲暇的周末，怎样陪伴孩子度过？玩游戏？趴被窝？不如...和附近的孩子，一起公开课～学习知识也好，培养兴趣也罢，这里，总能让时间，充实的度过。</p>
        </div>
        <div class="inner">
            <h2 class="tit02"></h2>
            <p><span>1</span>课程兴趣导入，介绍乌克丽丽故事+起源+构造（20分钟）</p>
            <p><span>2</span>弹奏示范时间，学生点歌，老师现场演奏（10分钟）</p>
            <p><span>3</span>学生体验单音拨弦练习，基本指法教学；（30分钟）</p>
        </div>
        <div class="inner">
            <h2 class="tit03"></h2>
            <ul>
                <li><img src="<@app.link href="/public/images/parent/openclass/cy_intro01.png"/>" ></li>
                <li><img src="<@app.link href="public/images/parent/openclass/cy_intro02.png"/>" ></li>
                <li><img src="<@app.link href="public/images/parent/openclass/cy_intro03.png"/>" ></li>
                <li><img src="<@app.link href="public/images/parent/openclass/cy_intro04.png"/>" ></li>
            </ul>
        </div>
        <div class="num">
            <#assign reserveCountPublic = ((20 - reserveCount!0) <= 1)?string("1", "${(20 - reserveCount!0)}")/>
            <p class="txt">还剩<span>${reserveCountPublic!0}</span>个名额</p>
        </div>
        <div class="footer">
            <a href="javascript:void(0)" class="btn-red fix-footer js-bookOpenClassBtn">去报名</a>
        </div>
    </div>
</div>
<script>
    ga('trusteeTracker.send', 'pageview');
</script>
</@openClassMain.page>