<#import "../layout/project.module.ftl" as temp />
<@temp.page title="夏日通告">
<@sugar.capsule css=["project.summernotice"] />
<div class="main">
    <div class="head">
        <div class="inner">
            <h1></h1>
            <h2>亲爱的老师，新学期到啦，一起作业网推出了新的园丁豆规则，同时也为老师们准备了更多福利哦！ </h2>
        </div>
    </div>
    <div class="con">
        <div class="inner">
            <div class="ipad"></div>
            <h3>先来看看都有哪些福利吧~</h3>
            <div class="font-con">
                <h4>1.全球智慧，免费分享！</h4>
                <p>巨资购入世界顶级的思必驰语音识别技术（语音精准打分）、TTS语音合成技术（文字生成听力朗读）、迪士尼阅读绘本，并投入大量科研技术首创“智慧课堂”，让最先进的教育技术、工具和资源，免费服务于中国教师。</p>
            </div>
            <div class="font-con">
                <h4>2.超多奖品，拿到手软！</h4>
                <p>布置作业，每周都送红米手机和小米平板！更多活动，敬请期待！</p>
            </div>
            <div class="font-con">
                <h4>3.活跃打折，奖品更低价！</h4>
                <p>持续活跃就能轻松获得奖品中心9折优惠，奖品不再遥远！</p>
            </div>
            <div class="font-con">
                <h4>4.园丁豆攻略，轻松拿园丁豆！</h4>
                <p>发布官方攻略，传授园丁豆秘籍，轻松掘金不用愁！</p>
            </div>
        </div>
    </div>
    <div class="colum">
        <div class="inner">
            <h3>我们实行了新的园丁豆规则 ~</h3>
            <div class="font-con">
                <h4>1.增加减负系数</h4>
                <p>每个老师每周给每个班级布置作业（含测验）按次数给予不同比例的奖励：第一次系数为1，第二次为1.5，第三次或三次以上均为0.1。不限制老师的布置作业次数，学生的学豆收入也不受减负系数影响。</p>
                <p>为了响应教育部减轻学生负担的号召，根据家长反馈，同时经一起作业大数据平台分析：每周布置两次作业，每次作业15分钟，每次作业包含多种题型，对学生成绩的提升效果最显著。</p>
            </div>
            <div class="font-con">
                <h4>2.增加作业完成比例</h4>
                <p>作业和测验的园丁豆收入，将乘以作业完成比例。作业完成比例为【本次按时完成作业的人数】除以上学期平均完成作业人数</p>
                <p>
                    大数据表明，同一个班级的学生在同一个时间段内完成作业，可以保持很高的互动性，对提高学生学习积极性有很大帮助。
                </p>
                <p><a href="${(ProductConfig.getUcenterUrl())!''}/teacher/center/index.vpage?types=mygold" target="_blank">点击查看园丁豆详细规则></a></p>
                <p><a href="http://www.17huayuan.com/forum.php?mod=viewthread&tid=12165&extra=page%3D1" target="_blank">关于规则改版的背景请点击查看：《一起作业网CEO刘畅致全国教师用户的信》</a></p>
            </div>
        </div>
    </div>
</div>
</@temp.page>