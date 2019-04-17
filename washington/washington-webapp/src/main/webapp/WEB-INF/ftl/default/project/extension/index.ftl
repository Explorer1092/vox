<#import "../../layout/project.module.ftl" as temp />
<@temp.page title="使用老师APP">
    <@sugar.capsule js=[] css=["extension"] />
    <div class="extension-box">
        <div class="ex-banner"></div>
        <div class="ex-main">
            <div class="ex-column-1">
                <div class="ex-inner">
                    <div class="info">
                        <p>一起作业老师APP上线啦！布置作业比微信更好用；还可以免费给家长发通知奥～ <i class="tag tag1"></i></p>
                        <p>支持图片 <i class="tag tag2"></i> ＋语音 <i class="tag tag3"></i>！快快扫描二维码下载吧～</p>
                    </div>
                    <div class="content">
                        <span class="welfare"></span>
                        <i class="bean-ico"></i>
                        <p class="title">偷偷告诉您：6月25日0时前，下载老师APP，并在任一“班级群”中发送图片，将获得50园丁豆。（28日之前到账）</p>
                    </div>
                </div>
            </div>
            <div class="ex-column-2">
                <div class="ex-inner">
                    <div class="left">
                        <h1>班级群功能</h1>
                        <p>1.系统会将同班家长拉进班级群中</p>
                        <p>2.在班级群中，可以给全班家长发送文字和图片，记录孩子们在学校的点点滴滴</p>
                    </div>
                    <div class="image1"></div>
                </div>
            </div>
            <div class="ex-column-3">
                <div class="ex-inner">
                    <div class="image2"></div>
                    <div class="right">
                        <h1>家长通知功能</h1>
                        <p>1.免费给家长发布通知，支持图片语音形式</p>
                        <p>2.支持确认和提醒，不让家长错过一条通知</p>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <script type="text/javascript">
        $(function(){
            $17.voxLog({
                module : "project-extension",
                op : "load"
            });
        });
    </script>
</@temp.page>