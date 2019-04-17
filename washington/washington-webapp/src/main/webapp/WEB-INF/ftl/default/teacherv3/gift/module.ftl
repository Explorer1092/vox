<#import "../../nuwa/teachershellv3.ftl" as temp />
<#macro page title="赠送礼物">
    <@temp.page showNav="hide">
    <div style="margin-top: 15px">
    </div>
    <div class="w-base">
        <div class="w-base-title">
            <h3>我的礼物</h3>
            <div class="w-base-right w-base-switch">
                <ul>
                    <li <#if title="赠送礼物"> class="active" </#if>><a href="/teacher/gift/index.vpage"><span class="h-arrow"><i class="w-icon-arrow w-icon-arrow-blue"></i></span><strong>赠送礼物</strong></a></li>
                    <li <#if title="收到的礼物"> class="active" </#if>><a href="/teacher/gift/receive/index.vpage"><span class="h-arrow"><i class="w-icon-arrow w-icon-arrow-blue"></i></span><strong>收到的礼物</strong></a></li>
                    <li <#if title="送出的礼物"> class="active" </#if>><a href="/teacher/gift/send/index.vpage"><span class="h-arrow"><i class="w-icon-arrow w-icon-arrow-blue"></i></span><strong>送出的礼物</strong></a></li>
                </ul>
            </div>
        </div>
        <!--template container-->
        <div class="w-base-container">
            <!--//start-->
            <div class="giftMain">
                <div id="content_box" class="content">
                    <#nested>
                    <div class="clear"></div>
                </div>
            </div>
            <!--end//-->
        </div>
    </div>
    <div id="tipMsg" class="dropDownBox_tip" style="display: none;">
        <span class="arrow">◆<span class="inArrow">◆</span></span>
        <div id="msg" class="tip_content"></div>
    </div>
    <script type="text/javascript">
        $(function(){
            //礼物标签
            $("#gift_tab_list_box li").on('click', function(){
                if($(this).hasClass('active')){
                    return false;
                }
            });
        });
    </script>
    </@temp.page>
</#macro>