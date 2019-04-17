<#import "../layout.ftl" as openClassMain>
<@openClassMain.page title='公开课介绍' pageJs="ocdetail">
    <@sugar.capsule css=['openclass'] />
<div class="train-wrap bg-blue">
    <div class="train02-box">
        <div class="inner">
            <h2 class="tit01"></h2>
            <p>学期结束，寒假将至，有没有想好，如何陪孩子度过？不论考的如何，不如一起来次，速算公开课，和附近学校的小朋友，一起享受，学习的快乐。</p>
        </div>
        <div class="inner">
            <h2 class="tit02"></h2>
            <p><span>1</span>课程兴趣导入，介绍数学速算</p>
            <p><span>2</span>现场速算课程讲解</p>
            <p><span>3</span>学生&家长，现场答疑环节</p>
        </div>
        <div class="inner">
            <h2 class="tit03"></h2>
            <ul>
                <li><img src="<@app.link href="/public/images/parent/openclass/v2_1.png"/>" ></li>
                <li><img src="<@app.link href="public/images/parent/openclass/v2_2.png"/>" ></li>
                <li><img src="<@app.link href="public/images/parent/openclass/v2_3.png"/>" ></li>
                <li><img src="<@app.link href="public/images/parent/openclass/v2_4.png"/>" ></li>
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