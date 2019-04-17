<#import "../layout.ftl" as trusteeMain>
<@trusteeMain.page title='托管班介绍' pageJs="trusteeclazzdesc">
<@sugar.capsule css=['trusteetwo'] />
<div class="active-wrap active-bgpink">
    <div class="active03-box active-spacing">
        <div class="ab02-box-1">为您匹配到学校附近的托管机构</div>
        <div class="ab02-box-2">
            <span class="add">乐思教育</span>
            <span class="tel">13590389001</span>
        </div>
        <div class="ab02-box-3">乐思教育由毕业于211,985高校的老师创办，致力于帮助孩子培养良好的学习习惯，建立高效的学习方法，提高学习效率。现开设晚托，午托，新概念英语，奥数，单词拼读，拼音辅导。</div>
        <div class="ab02-box-4">
            <h2><span>1</span>地理位置：距离西丽小学50米内</h2>
            <div class="box">
                <img src="<@app.link href="public/images/parent/trusteetwo/lsjymap.png"/>" alt="">
            </div>
        </div>
        <div class="ab02-box-4 ab02-box-4a js-oddBox">
            <h2><span>2</span>西丽小学已有11名学生在这里托管</h2>
            <div class="box box-fl">

                <img src="<@app.link href="public/images/parent/trusteetwo/lsjys1.png"/>" alt="">
                <p>“老师非常温和，还送礼物鼓励我们学习”</p>
            </div>
            <div class="box box-fr">
                <img src="<@app.link href="public/images/parent/trusteetwo/lsjys2.png"/>" alt="">
                <p>“在这里学习很快乐”</p>
            </div>
        </div>
        <div class="ab02-box-4 ab02-box-4a js-evenBox">
            <h2><span>2</span>机构特色</h2>
            <div class="box box-fl">
                <img src="<@app.link href="public/images/parent/trusteetwo/lsjyspec1.png"/>" alt="">
                <p>专业教师提供作业辅导</p>
            </div>
            <div class="box box-fr">
                <img src="<@app.link href="public/images/parent/trusteetwo/lsjyspec2.png"/>" alt="">
                <p>教学秩序井然，提高学习效率</p>
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