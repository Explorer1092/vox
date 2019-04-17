<#import "../layout/layout.ftl" as temp />
<#macro page title="赠送礼物">
    <@temp.page pageName='center' clazzName='t-center-bg'>
        <style type="text/css">
             /*tip*/
            .dropDownBox_tip{ position:absolute; z-index: 12; }
            .dropDownBox_tip span.arrow{ position: absolute; top:-9px; _top:-8px; left:20px; font: 18px/100% Arial, Helvetica, sans-serif; color: #c5c69a;}
            .dropDownBox_tip span.arrow span.inArrow{ color: #F8F6B9; position:absolute; left:0; top:1px;}
            .dropDownBox_tip span.arrowLeft{ left:-9px; top:15px;}
            .dropDownBox_tip span.arrowLeft span.inArrow{ left:1px; top:0px;}
            .dropDownBox_tip span.arrowRight{ left: auto; right:-9px; top:15px;}
            .dropDownBox_tip span.arrowRight span.inArrow{left:-1px; top:0px;}
            .dropDownBox_tip span.arrowBot{ top:auto; bottom:-9px; _bottom:-11px; left:20px;}
            .dropDownBox_tip span.arrowBot span.inArrow{ left:0; top:-1px}
            .dropDownBox_tip .tip_content{ border:1px solid #c5c69a; background-color:#F8F6B9; border-radius:4px; overflow:hidden; box-shadow: 1px 1px 3px #aaa; padding:5px 0; width:160px; font:12px/22px arial; color:#666; padding:5px;}
                /*mainContainer*/
            .main_container { position:relative; overflow: hidden; clear: both; margin: 0 0 20px; border-radius: 10px; overflow: hidden;}
            .main_container .container_head, .main_container .container_foot, .main_container .container_sub .sub_foot, .main_container .container_sub .sub_head { clear: both; height: 5px; font: 0px/0px arial; }
            .main_container .container_public, .main_container .container_public .sidebar_a, .main_container .container_public .sidebar_b { overflow: hidden; }
            .main_container .container_public .sidebar_a { float: left; display: inline-block; width: 10px; height: 5px; }
            .main_container .container_public .sidebar_b { float: right; display: inline-block; width: 10px; height: 5px; }
            .main_container .container_head { background-position: 0 -55px; }
            .main_container .container_foot { background-position: 0 -16px; }
            .main_container .container_sub { margin: -5px 0 0;}
            .main_container .container_sub .sub_foot { background-position: 0 -38px; }
            .main_container .container_sub .sub_head { background-position: 0 -82px; }
            .main_container .container_title, .main_container .container_summary, .main_container .container_sub .sub_summary { background-color: #fff; height: 100%; overflow: hidden; position: relative;}
            .main_container .container_title { padding: 10px 20px 10px; border-bottom: 1px solid #d1cfcf; height: 24px; overflow: visible; position:relative;}
            .main_container .container_title h1 { color: #333; margin: 0; padding: 0; font: 16px/24px "微软雅黑", "Microsoft YaHei", Arial; float: left; }
            .main_container .container_title p { float: left; color: #aaa; border-left: 1px solid #e1e0e0; margin-left: 15px; padding-left: 15px; line-height: 24px; }
            .main_container .container_sub .sub_summary { background-color: #f8f8f8; }
            .main_container .container_title .container_tab { overflow:hidden; margin:0px 0 0; position:absolute; right:10px; top:5px;}
            .main_container .container_title .container_tab li{ margin-left:-3px; float:left; padding:6px 0 0;}
            .main_container .container_title .container_tab li a{ color:#666; display: inline-block; border-left:1px solid #ddd; padding: 0px 15px 0; line-height: 22px;}
            .main_container .container_title .container_tab li a i{ margin: 0 8px 0 0;}
            .main_container .container_title .container_tab li a strong { vertical-align:middle; display: inline-block; font-weight:normal;}
            .main_container .container_title .container_tab li.active{ padding:0; margin-left:3px; position:relative;}
            .main_container .container_title .container_tab li.active a{ border:solid #ddd; border-width:1px 1px 0; padding:6px 15px 0; height:33px; background-color:#fff;  color:#5c84e6; border-radius:3px 3px 0 0;}
        </style>
    <div class="t-center-container">
        <div class="t-center-slide w-fl-left">
            <span class="leaf leaf-1"></span>
            <span class="leaf leaf-2"></span>
            <span class="leaf leaf-3"></span>
            <span class="leafcope"></span>
            <span class="ts-top"></span>
            <div class="ts-center">
                <ul>
                    <li <#if title == "赠送礼物"> class="active" </#if>><a href="/student/gift/index.vpage" class="w-gray">赠送礼物</a></li>
                    <li <#if title == "收到的礼物"> class="active" </#if>><a href="/student/gift/receive/index.vpage" class="w-gray">收到的礼物</a></li>
                    <li <#if title == "送出的礼物"> class="active" </#if>><a href="/student/gift/send/index.vpage" class="w-gray">送出的礼物</a></li>
                </ul>
            </div>
            <div class="ts-bottom"></div>
        </div>
        <div class="t-center-box w-fl-right">
            <div class="t-messages-data">
                <div class="t-messages-title">
                    <div class="title-inner-back">
                    ${title!''}<#if title != "赠送礼物">（只显示近30天的）</#if>
                    </div>
                </div>
                <#nested />
            </div>
        </div>
        <div class="w-clear"></div>
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
    <div id="tipMsg" class="dropDownBox_tip" style="display: none;">
        <span class="arrow">◆<span class="inArrow">◆</span></span>
        <div id="msg" class="tip_content"></div>
    </div>
    </@temp.page>
</#macro>

