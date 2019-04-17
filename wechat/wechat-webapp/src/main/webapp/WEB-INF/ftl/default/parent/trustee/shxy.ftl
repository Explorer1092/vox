<#import "../layout.ftl" as trusteeMain>
<@trusteeMain.page title='托管班介绍' pageJs="trusteeclazzdesc">
<@sugar.capsule css=['trusteetwo'] />
<div class="active-wrap active-bgpink">
    <div class="active03-box active-spacing">
        <div class="ab02-box-1">为您匹配到学校附近的托管机构</div>
        <div class="ab02-box-2">
            <span class="add">伊河路小学书香源</span>
            <span class="tel">67976388</span>
        </div>
        <div class="ab02-box-3">书香源在全国有300多家教育托管中心，拥有经验丰富的教师团队，完善的好习惯培养体系。作业辅导采取8人小班制，培养孩子独立作业习惯，及时一对一解惑，迭代知识点过关。</div>
        <div class="ab02-box-4">
            <h2><span>1</span>地理位置：距离伊河路小学30米</h2>
            <div class="box">
                <img src="<@app.link href="public/images/parent/trusteetwo/active-a02-img-1.png"/>" alt="">
            </div>
        </div>
        <div class="ab02-box-4 ab02-box-4a js-oddBox">
            <h2><span>2</span>伊河路小学已有170名学生在这里托管</h2>
            <div class="box box-fl">
                <img src="<@app.link href="public/images/parent/trusteetwo/active-a02-img-3.png"/>" alt="">
                <p>“在这里，好玩有趣的课外活动让我每天都有不同的惊喜”</p>
            </div>
            <div class="box box-fr">
                <img src="<@app.link href="public/images/parent/trusteetwo/active-a02-img-4.png"/>" alt="">
                <p>“在这里，有不懂的地方，老师会及时给我讲解”</p>
            </div>
        </div>
        <div class="ab02-box-4 ab02-box-4b js-evenBox">
            <h2><span>2</span>机构特色</h2>
            <div class="box box-fl">
                <img src="<@app.link href="public/images/parent/trusteetwo/active-b02-img-3.png"/>" alt="">
                <p>经验丰富的教师团队</p>
            </div>
            <div class="box box-fr">
                <img src="<@app.link href="public/images/parent/trusteetwo/active-b02-img-4.png"/>" alt="">
                <p>精品作业辅导班</p>
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