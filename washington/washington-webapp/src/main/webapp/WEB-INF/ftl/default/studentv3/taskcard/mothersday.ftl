<#if (data.mdcState)??>
    <#--data.mdcState    MAKE表示需要做卡    SEND表示还没发送   SHARE表示还没分享    如果不存在 卡片不用显示-->
    <#switch data.mdcState>
        <#case "MAKE">
            <li class="practice-block">
                <div class="practice-content practice-content-mothersday-make">
                    <h4>
                        <span class="w-discipline-tag w-discipline-tag-9">母亲节</span>
                    </h4>
                    <div class="pc-btn">
                        <a onclick="$17.tongji('感恩母亲节','首页任务卡','制作贺卡');" target="_blank" href="/student/activity/mothersday/index.vpage" class="w-btn w-btn-green">制作贺卡</a>
                    </div>
                </div>
            </li>
            <#break>
        <#case "SEND">
            <li class="practice-block">
                <div class="practice-content practice-content-mothersday-send">
                    <h4>
                        <span class="w-discipline-tag w-discipline-tag-9">母亲节</span>
                    </h4>
                    <div class="pc-btn">
                        <a id="generateQRCodeBut" href="javascript:void (0);" class="w-btn w-btn-green">生成二维码</a>
                    </div>
                </div>
            </li>
            <#break>
        <#case "SHARE">
            <li class="practice-block">
                <div class="practice-content practice-content-mothersday-share">
                    <h4>
                        <span class="w-discipline-tag w-discipline-tag-9">母亲节</span>
                    </h4>
                </div>
            </li>
            <#break>
    </#switch>


    <script type="text/javascript">
        $(function(){
            $('#generateQRCodeBut').on('click',function(){
                $17.getQRCodeImgUrl({campaignId : 28}, function (url) {
                    $17.alert('<div><img style="width: 200px; height: 200px;" src='+url+' alt="二维码"/></div>');
                });
                $17.tongji('感恩母亲节','首页任务卡','生成二维码');

            });
        });
    </script>
</#if>
