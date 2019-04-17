<#import "../layout.ftl" as trusteeMain>
<@trusteeMain.page title='托管班介绍' pageJs="trusteeclazzdesc">
<@sugar.capsule css=['trusteetwo'] />
<div class="active-wrap active-bgpink">
    <div class="active03-box active-spacing">
        <div class="ab02-box-1">为您匹配到学校附近的托管机构</div>
        <div class="ab02-box-2">
            <span class="add">欧拉作业吧</span>
            <span class="tel">15915423041</span>
        </div>
        <div class="ab02-box-3">欧拉作业吧位于深圳市南山区花园式小区内，教学场所环境优雅，管理正规，以中小学文化课的课外辅导和专业精品8-10人作业辅导班为主,午托班为辅，为学生、家长提供规范化、规模化的一站式服务。</div>
        <div class="ab02-box-4">
            <h2><span>1</span>地理位置：距离前海小学500米内</h2>
            <div class="box">
                <img src="<@app.link href="public/images/parent/trusteetwo/qholmap.png"/>" alt="">
            </div>
        </div>
        <div class="ab02-box-4 ab02-box-4a js-oddBox">
            <h2><span>2</span>前海小学已有20名学生在这里托管</h2>
            <div class="box box-fl">
                <img src="<@app.link href="public/images/parent/trusteetwo/qhols1.png"/>" alt="">
                <p>“午餐干净又好吃”</p>
            </div>
            <div class="box box-fr">
                <img src="<@app.link href="public/images/parent/trusteetwo/qhols2.png"/>" alt="">
                <p>“哪里不懂老师总能及时帮我解答”</p>
            </div>
        </div>
        <div class="ab02-box-4 ab02-box-4a js-evenBox">
            <h2><span>2</span>机构特色</h2>
            <div class="box box-fl">
                <img src="<@app.link href="public/images/parent/trusteetwo/qholspec1.png"/>" alt="">
                <p>学习环境整洁舒适</p>
            </div>
            <div class="box box-fr">
                <img src="<@app.link href="public/images/parent/trusteetwo/qholspec2.png"/>" alt="">
                <p>重点培养好习惯</p>
            </div>
        </div>
        <div class="footer">
            <div class="empty"></div>
            <a href="javascript:void(0);" class="active-bottom-know order_icon js-goToBookBtn">去预约体验</a>
        </div>
    </div>
</div>
<script>
    var uid = ${currentUserId!0000};
    ga('trusteeTracker.send', 'pageview');
</script>
</@trusteeMain.page>