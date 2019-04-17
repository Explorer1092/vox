<#import "../module.ftl" as module>
<@module.page
title="下载直播软件"
leftMenu="下载直播软件"
>
<#include "bootstrapTemp.ftl">
<div class="microClass">
    <div class="title">下载直播软件</div>
    <div class="mic-openCourse">
        <div class="c-name">微课堂教师端<span class="c-subName">支持windows7/8/10，32/64bit</span></div>
        <a href="${downloadUrl!'#'}" class="btn btn-warning btn-lg" id="downloadBtn">立即下载</a>
        <div class="liveSoftware-box">
            <p>1.如果安装后无法正常启动，请 <a href="http://v.17zuoye.cn/software/DirectX_Repair-v3.3.zip" class="download_btn">点我下载</a> DirectX Repair修复工具。</p>
            <p>2.安装完成后在浏览器中“我的课程”页面，找到对应教师，点击立即上课，开始上课。</p>
        </div>
    </div>
</div>
<script>
var plos = navigator.platform.toUpperCase(),
    btn = document.getElementById('downloadBtn');
if(plos.indexOf('WIN') == -1){ //非windows不可点
    btn.setAttribute('disabled','');
}
</script>
</@module.page>