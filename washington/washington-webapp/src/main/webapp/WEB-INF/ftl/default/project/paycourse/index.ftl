<#import '../../parentmobile/layout.ftl' as layout>
<@layout.page className='paycourse' title="在这里向世界 say hello" specialCss="paycourse">
<@sugar.capsule js=['voxLogs']/>
<div class="sayHello-box">
    <div class="bg1"></div>
    <div class="bg2"></div>
    <div class="bg3"></div>
    <div class="bg4"></div>
</div>
<div class="sh-footer">
    <div class="empty"></div>
    <div class="sh-btn">
        <p class="info">现在报名，手慢则无，前5位报名者，视频玩偶送到家，时时开启英语探索之旅！</p>
        <div class="content">
            <a href="paycourse.vpage" class="view_btn" onclick="YQ.voxLogs({module : 'student_fanzhuan', op : 'clickDetail'});">查看</a>
            <p class="tip">查看课程详细介绍<br>需要到家长app上报名哦</p>
        </div>
    </div>
</div>
<script type="text/javascript">
    YQ.voxLogs({module : 'student_fanzhuan', op : 'load'});
</script>
</@layout.page>