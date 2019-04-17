<#import "../layout.ftl" as trusteeMain>
<@trusteeMain.page title='托管班介绍' pageJs="trusteeclazzdesc">
<@sugar.capsule css=['trusteetwo'] />
<div class="active-wrap active-bgpink">
    <div class="active03-box active-spacing">
        <div class="ab02-box-1">为您匹配到学校附近的托管机构</div>
        <div class="ab02-box-2">
            <span class="add">正大午托</span>
            <span class="tel">13803819892</span>
        </div>
        <div class="ab02-box-3">正大午托位于纬五路第一小学旁院内，教学场所精致温馨，由专业奥数、英语教师管理，作业辅导极为专业,午托班有舒适卫生的环境以及可口营养的餐点，是家长放心的好选择。</div>
        <div class="ab02-box-4">
            <h2><span>1</span>地理位置：距离纬五路第一小学200米</h2>
            <div class="box">
                <img src="<@app.link href="public/images/parent/trusteetwo/zdwtmap.png"/>" alt="">
            </div>
        </div>
        <div class="ab02-box-4 ab02-box-4a js-oddBox">
            <h2><span>2</span>纬五路第一小学已有28名学生在这里托管</h2>
            <div class="box box-fl">
                <img src="<@app.link href="public/images/parent/trusteetwo/zdwts1.png"/>" alt="">
                <p>“正大午托，就像我的第二个家”</p>
            </div>
            <div class="box box-fr">
                <img src="<@app.link href="public/images/parent/trusteetwo/zdwts2.png"/>" alt="">
                <p>“今年生日，正大午托给了我一个难忘的美好回忆”</p>
            </div>
        </div>
        <div class="ab02-box-4 ab02-box-4a js-evenBox">
            <h2><span>2</span>机构特色</h2>
            <div class="box box-fl">
                <img src="<@app.link href="public/images/parent/trusteetwo/zdwtspec1.png"/>" alt="">
                <p>整齐明亮的学习空间，专业全职奥数、英语教师指导作业</p>
            </div>
            <div class="box box-fr">
                <img src="<@app.link href="public/images/parent/trusteetwo/zdwtspec2.png"/>" alt="">
                <p>安全静谧的午睡环境，让孩子的午休时间充分休息</p>
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