<@app.css href="public/skin/project/afenti/exam/skin_new.css" />
<div class="aft-main">
    <!--
    <div class="title_menu">
        <ul>
            <li class="current"  data-title="examIntro">
                <a href="javascript:void (0)">产品详情</a>
            </li>
            <#--<#if (productType == "exam")!false>
                <li data-title="examComment">
                    <a href="javascript:void (0)">同学评论</a>
                </li>
            </#if>-->
            <li class="toHas">
                <a href="javascript:void (0)"></a>
            </li>
            <li class="toHas">
                <a href="javascript:void (0)"></a>
            </li>
        </ul>
    </div>
    -->
    <#--step -1-->
    <div class="am-product-box" data-type-tab="examIntro-box">
        <img src="<@app.link href="public/skin/project/afenti/exam/aftIntro-bg01.png"/>"/>
        <img src="<@app.link href="public/skin/project/afenti/exam/aftIntro-bg02.png"/>"/>
        <img src="<@app.link href="public/skin/project/afenti/exam/aftIntro-bg03.png"/>"/>
        <img src="<@app.link href="public/skin/project/afenti/exam/aftIntro-bg04.png"/>"/>
        <img src="<@app.link href="public/skin/project/afenti/exam/aftIntro-bg05.png"/>"/>
    </div>
    <#--step -2-->
    <div id="examRank" data-type-tab="examRank-box" style="display: none; height: 1000px;"></div>
    <#--step -3-->
    <div id="examComment" data-type-tab="examComment-box"  style="display: none; height: 1000px;"></div>
    <@ftlmacro.chargeinfo name="all" game="1" />

    <#assign keyType = "AfentiExam"/>
    <#if productType == "afentimath">
        <#assign keyType = "AfentiMath"/>
    </#if>
</div>
    <script type="text/javascript">
        var currentPageNumber = 1;

        $(function(){
            var titleMenu = $(".title_menu");
            //tab switch
            titleMenu.on("click", "li", function(){
                var $this = $(this);
                var $tabName = $this.data("title");
                if($this.hasClass("toHas")){
                    return false;
                }

                if($tabName == "examComment" && $this.data("load") == undefined){
                    $("#examComment").load("/apps/afenti/comment.vpage?type=${keyType!}", function(){
                        $("div[data-type-tab='"+ $tabName +"-box']").height("auto");
                    });
                }

                $this.addClass("current").attr("data-load","1").siblings().removeClass("current");

                $("div[data-type-tab='"+ $tabName +"-box']").show().siblings("div[data-type-tab]").hide();
            });
        });
    </script>

