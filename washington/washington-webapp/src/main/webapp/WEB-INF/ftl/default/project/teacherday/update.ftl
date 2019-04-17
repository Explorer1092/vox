<#import "../../layout/webview.layout.ftl" as layout>
<@layout.page title="升级提示">
<style>
    html,body{height:100%;}
    body{background:#f9677e;}
</style>
<div style="position:relative;">
    <img width="100%" src="<@app.link href="public/skin/project/teacherday/images/update.jpg"/>" />
    <a href="http://wx.17zuoye.com/download/17studentapp" style="position:absolute;width:11rem;height:2.25rem;bottom:0;left:50%;transform:translateX(-50%);background:url(<@app.link href='public/skin/project/teacherday/images/update-btn.png'/>);background-size: 100% 100%;"></a>
</div>
</@layout.page>