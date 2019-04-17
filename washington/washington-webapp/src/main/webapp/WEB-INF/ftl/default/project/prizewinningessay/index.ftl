<#import "../../layout/project.module.student.ftl" as temp />
<@temp.page title="免费送学豆啦！我和孩子一起作业有奖征文">
    <@sugar.capsule css=["project.prizewinningessay", "student.widget"] />
    <!--//start-->
    <div class="head">
        <div class="head_wrap">
            <div class="head_inner"></div>
        </div>
    </div>
    <div class="main">
        <div class="con"></div>
        <div class="info_img">
            <ul>
                <li>
                    <a href="infoone.vpage" target="_blank"></a>
                </li>
                <li>
                    <a href="infotwo.vpage" target="_blank"></a>
                </li>
            </ul>
        </div>
        <div class="info_way"></div>
        <div class="foot">
            <div class="info_detail">
                <div class="weixin_before">
                    <a href="javascript:void (0);"></a>
                </div>
                <div class="weixin_after" style="display: none;">
                    <p class="weixin"></p>
                </div>
            </div>
        </div>
        <div class="info_new">
            <ul>
                <li>
                    <a href="infoone.vpage" target="_blank"></a>
                </li>
                <li>
                    <a href="infotwo.vpage" target="_blank"></a>
                </li>
            </ul>
        </div>
    </div>
    <!--end//-->
    <script type="text/javascript">
        $(function(){
            $(".info_detail .weixin_before a").on("click", function(){
                var $this       = $(this);
                var $thisSi     = $this.parent().siblings(".weixin_after");
                var weiXinCode  = "<div class='loading_vox' style='height: 100%;'></div>";
                var qrCodeUrl = "<@app.link href="public/skin/studentv3/images/2dbarcode.jpg"/>";

                $this.addClass("dis").html(weiXinCode);

                $.get("/student/qrcode.vpage", function(data){
                    if(data.success){
                        qrCodeUrl = data.qrcode_url;
                    }else{
                        if("请返回首页重新登录" == data.info){
                            $17.alert(data.info, function(){
                                location.href = "/";
                            });
                        }
                    }
                    $this.parent().hide();
                    weiXinCode = "<img src='"+ qrCodeUrl +"' width='120' height='120'/>";
                    $thisSi.show();
                    $thisSi.find("p").html(weiXinCode);
                    $17.tongji("学生端二维码_绑定_专题页");
                });
            });
        });
    </script>
</@temp.page>