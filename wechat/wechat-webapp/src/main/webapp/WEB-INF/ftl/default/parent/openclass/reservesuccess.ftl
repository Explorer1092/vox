<#import "../layout.ftl" as trusteeMain>
<@trusteeMain.page title='报名成功' pageJs="ocresuccess">
<@sugar.capsule css=['openclass'] />
<div class="train-wrap bg-blue">
    <div class="train05-box">
        <div class="header"></div>
        <div class="main">
            <h2></h2>
            <p>• 公开课时间：${shop.classDate!""}</p>
            <p>• 公开课地点：${shop.address!""}</p>
            <p>• 咨询电话：${shop.phone!""}</p>
            <a href="javascript:void(0)" class="link js-teacherInfoBtn">公开课老师介绍</a>
        </div>
        <div class="footer">
            <a href="javascript:void(0)" class="btn-red fix-footer js-buyClassBtn">购买课程</a><!--按下 添加类active-->
        </div>
    </div>
</div>
<script>
    var shopId = "${shop.shopId!""}";
    ga('trusteeTracker.send', 'pageview');
</script>
</@trusteeMain.page>