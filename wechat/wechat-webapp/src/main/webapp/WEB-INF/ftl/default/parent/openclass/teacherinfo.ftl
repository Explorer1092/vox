<#import "../layout.ftl" as openClassMain>
<@openClassMain.page title='老师介绍' pageJs="octeacher">
<@sugar.capsule css=['openclass'] />
<div class="train-wrap bg-blue">
    <div class="train06-box">
        <div class="header">
            <img src="/public/images/parent/openclass/tea-pic02.png">
            <div class="r-info">
                <p>李老师<i class="sex woman"></i>数学速算</p>
                <p class="describe">精准把握学生心理，善于培养和激发学生学习兴趣</p>
            </div>
        </div>
        <div class="main">
            <div class="inner">
                <h2>老师简介</h2>
                <p>毕业于华南师范大学，具有丰富的教学经验，教学过程耐心且讲究方法，让不同程度的孩子都可以轻松学习</p>
            </div>
            <div class="inner">
                <h2>老师特色</h2>
                <p> • 从事教育行业十多年</p>
                <p> • 参与教育实验教辅编写、出版、编著</p>
                <p> • 辅导学生习作参加各级各类比赛多次获特等奖及一、二等奖</p>
            </div>
            <div class="inner">
                <h2>学生作品</h2>
                <ul>
                    <li><img src="/public/images/parent/openclass/stu-work03.png"><p>孩子现场速算</p></li>
                    <li><img src="/public/images/parent/openclass/stu-work04.png"><p>解答问题</p></li>
                </ul>
            </div>
        </div>
        <div class="footer">
            <a href="javascript:void(0);" class="btn-red fix-footer js-buyClassBtn">购买课程</a>
        </div>
    </div>
</div>
<script>
    var shopId = "${shopId!""}";
    ga('trusteeTracker.send', 'pageview');
</script>
</@openClassMain.page>