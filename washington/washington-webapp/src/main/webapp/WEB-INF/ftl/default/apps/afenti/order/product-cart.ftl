<#import "module.ftl" as com>
<@com.page step=1 title=productType>
<#if productType == "globalmath">
    <#include "../htmlchip/globalmath-cart.ftl">
<#elseif productType == "picaro">
    <#include "../htmlchip/picaro-cart.ftl">
<#else>
    <div class="main">
        <div class="payMainBox">
            <div class="curaddress">请您选择购买内容</div>
            <div class="tabbox">
                <div class="tabLevel productView">
                    <!--//product start-->
                    <div id="productMainList"><#--content--></div>
                    <#switch productType>
                        <#case "afentimath">
                        <#case "afentichinese">
                        <#case "exam">
                            <#include "../htmlchip/exam-cart.ftl">
                            <#break />
                        <#case "basic">
                            <#include "../htmlchip/basic-cart.ftl">
                            <#break />
                        <#case "talent">
                            <#include "../htmlchip/talent-cart.ftl">
                            <#break />
                        <#case "travel">
                            <#include "../htmlchip/travel-cart.ftl">
                            <#break />
                        <#case "picaro">
                            <#include "../htmlchip/picaro-cart.ftl">
                            <#break />
                        <#case "walker">
                            <#include "../htmlchip/walker-cart.ftl">
                            <#break />
                        <#case "iandyou">
                            <#include "../htmlchip/iandyou-cart.ftl">
                            <#break />
                        <#case "sanguodmz">
                            <#include "../htmlchip/sanguodmz-cart.ftl">
                            <#break />
                        <#case "petswar">
                            <#include "../htmlchip/petswar-cart.ftl">
                            <#break />
                        <#case "spg">
                            <#include "../htmlchip/spg-cart.ftl">
                            <#break />
                        <#case "walkerelf">
                            <#include "../htmlchip/walkerelf-cart.ftl">
                            <#break />
                        <#case "stem">
                            <#include "../htmlchip/stem.ftl">
                            <#break />
                        <#case "wukongshizi">
                            <#include "../htmlchip/wukongshizi-cart.ftl">
                            <#break />
                        <#case "wukongpinyin">
                            <#include "../htmlchip/wukongpinyin-cart.ftl">
                            <#break />
                    </#switch>
                    <!--product end//-->
                </div>
            </div>
        </div>
    </div>
</#if>
<#if productType != "globalmath">
<script id="t:productBox" type="text/html">
    <#if productType != "picaro">
        <div class="revealPhoto">
            <ul class="switchBox">
                <li>
                    <img src="<@app.link href="public/skin/project/afenti/images/<%=data[0].productType%>.jpg?1.0.6"/>">
                </li>
            </ul>
            <p class="agreement">
                <label for="checkboxFor">
                    <input type="checkbox" id="checkboxFor" style=" vertical-align:  middle;" checked="checked">
                    <span style="display: inline-block; vertical-align: middle;">同意<a href="/help/shopagreement.vpage" target="_blank" class="clrblue">产品购买协议</a></span>
                </label>
            </p>
        </div>
    </#if>

    <div class="revealCtn">
        <#if (productType == "stem")!false>
            <p class="intro">
                <span>趣味数学训练营</span>
            </p>
            <p class="period">
                难度：
                 <span class="js-selectTypeItems">
                    <i data-id="初露头角篇" class="<%if(currentStemIndex == '初露头角篇'){%>sel<%}%>" style="font-size: 18px;">初露头角篇</i>
                    <i data-id="技压群芳篇" class="<%if(currentStemIndex == '技压群芳篇'){%>sel<%}%>" style="font-size: 18px;">技压群芳篇</i>
                    <i data-id="独步天下篇" class="<%if(currentStemIndex == '独步天下篇'){%>sel<%}%>" style="font-size: 18px;">独步天下篇</i>
                </span>
                <span class="js-selectProduct" style="display: inline-block; margin-left: 40px;">
                    <%var itemIndex = 0, baseIndex=currentIndex%>
                    <%for(var i = 0; i < data.length; i++){%>
                        <%if(currentStemIndex == data[i].category){%>
                            <%itemIndex += 1%>
                            <i data-id="<%=itemIndex%>" class="<%if(data[i].statusType > 0){%>gray<%}%>
                            <%if(itemIndex == baseIndex){%>
                                <%if(data[i].statusType > 0){%>
                                    <%currentIndex = -1%>
                                <%}else{%>
                                    sel
                                    <%currentIndex = i%>
                                <%}%>
                            <%}%>">
                                <b><%=(data[i].desc)%></b>段
                            </i>
                        <%}%>
                    <%}%>
                </span>
            </p>
        <#else>
            <p class="intro">
                <span><%=currentIndex == -1 ? data[0].name : data[currentIndex].name%></span>
            </p>
            <p class="period">
                <%=(currentIndex == -1 ? data[0].infoTypeName : data[currentIndex].infoTypeName)%>
                <span class="js-selectProduct" style="margin: -30px 0 0 50px; display: block;">
                    <%for(var i = 0; i < data.length; i++){%>
                        <i data-id="<%=i%>" class="<%if(data[i].statusType > 0){%>gray<%}%> <%if(i == currentIndex){%>sel<%}%>">
                            <%if(data[i].productType == "AfentiTalent"){%>
                                <b><%=(i+1)%></b>阶
                            <%}else if(data[i].productType == "WalkerElf"){%>
                                <b>level<%=(i+1)%></b>
                            <%}else{%>
                                <b><%=data[i].desc%></b>天
                            <%}%>
                        </i>
                    <%}%>
                </span>
            </p>
        </#if>

        <#--走遍美国显示-->
        <%if(currentIndex != -1 && data[currentIndex].type == "TravelAmerica" && data[currentIndex].price == 1){%>
            <p style="padding: 0 0 20px 36px; color: #f00; margin-top: -15px;">
                （仅限购买1次，且不支持退款。）
            </p>
        <%}%>

        <%if(currentIndex != -1 && data[currentIndex].type == "WalkerElf"){%>
            <p style="padding: 0 0 20px 50px; margin-top: -15px;">(
                <%if(currentIndex == 0){%>适合一二年级使用<%}%>
                <%if(currentIndex == 1){%>适合三四年级使用<%}%>
                <%if(currentIndex == 2){%>适合五六年级使用<%}%>
            )</p>
        <%}%>

        <p class="price">网上支付价格：<span><b id="totalPrice"><%=currentIndex == -1 ? 0 : data[currentIndex].price%></b>元</span></p>
        <%if(currentIndex != -1 && data[currentIndex].attributes && data[currentIndex].type != "WalkerElf" && data[currentIndex].type != "Stem101"){%>
            <p class="price"><%==data[currentIndex].attributes%></p>
        <%}%>
        <#if productType == "picaro">
            <p style="margin-bottom: 10px;">
                <label for="checkboxFor">
                    <input type="checkbox" id="checkboxFor" style=" vertical-align:  middle;" checked="checked">
                    <span style="display: inline-block; vertical-align: middle;">同意<a href="/help/shopagreement.vpage" target="_blank" class="clrblue">产品购买协议</a></span>
                </label>
            </p>
        </#if>
        <p class="btn">
            <#--冒险岛和单词达人 不可购买了 置灰掉 -->
            <%if(data[0].type == 'AfentiBasic' || data[0].type == 'AfentiTalent' || data[0].type == 'KaplanPicaro' || data[0].type == 'iandyou100'){%>
                <span style="color: #f00;">此产品暂不支持购买~</span><br/>
                <a href="javascript:void(0);" class="getOrange  getOrange_gray">暂停购买</a>
            <%}else{%>
            <#--<#if (dayToExpire gte 365)!false>-->
                <#if dayToExpireBiggerThan365!false>
                    <span style="color: #f00;">您已经开通了<%=currentIndex == -1 ? data[0].name : data[currentIndex].name%>，可以直接学习使用哦！</span><br/>
                <#else>
                    <a id="buy" href="javascript:void(0);" data-op="purchase-immediately" class="getOrange buyNowSubmit v-studentVoxLogRecord">立即购买</a>
                </#if>
            <%}%>
        </p>
    </div>
    <div class="clear"></div>
    <form action="?" method="post" id="frm">
        <input type="hidden" name="p" value="<%=currentIndex == -1 ? '' : currentIndex%>"/>
        <input type="hidden" name="productId" value="<%=currentIndex == -1 ? '' : data[currentIndex].id%>"/>
        <#if refer?has_content>
            <input type="hidden" name="refer" value="${refer}"/>
        </#if>
    </form>
</script>

<script type="text/javascript">
    $(function(){
        //产品数据
        var availableProducts = ${availableProductsJson!'[]'};
        var currentIndex = -1;
        var currentStemIndex = "初露头角篇";

        //选择购买类型
        $(document).on("click", ".js-selectProduct i[data-id]", function(){
            var $this = $(this);
            var $index = $this.data("id");

            if($this.hasClass("gray") || $this.hasClass("sel")){
                return false;
            }

            templateRendering(availableProducts, $index);
        });

        /*--stem101 auto select--*/
        //Stem101
        $(document).on("click", ".js-selectTypeItems i[data-id]", function(){
            var $this = $(this);
            var $index = $this.data("id");

            if($this.hasClass("gray") || $this.hasClass("sel")){
                return false;
            }

            currentStemIndex = $index;

            templateRendering(availableProducts, -1);
            return false;
        });

        //auto select
        var queryProductId = $17.getQuery("productId");
        if( $17.isNumber(queryProductId) && queryProductId.length >= 3){
            var $index = queryProductId.substr(0, 2);
            if($index == 13){
                currentStemIndex = '技压群芳篇';
            }else if($index == 14){
                currentStemIndex = '独步天下篇';
            }

            var $QID = queryProductId%10;
            if(availableProducts.length > $QID){
                currentIndex = $QID;
            }
        }
        /*--stem101 auto select--*/

        //是否同意协议
        $(document).on("click", "#checkboxFor", function(){
            var $buyMenu  = $("#buy");
            if($(this).prop("checked")){
                $buyMenu.removeClass("getOrange_gray");
            }else{
                $buyMenu.addClass("getOrange_gray");
            }
        });

        //submit buy
        $(document).on("click", "#buy", function(){
            var $this = $(this);
            var $frm = $("#frm");

            if($this.hasClass("getOrange_gray")){
                return false;
            }else{
                if($frm.find("[name='p']").val() == "" || $frm.find("[name='productId']").val() == ""){
                    $17.alert("请选择需要购买的类型");
                    return false;
                }

                $frm.submit();
                $17.traceLog({
                    module: 'PayDetail',
                    op: 'load' ,
                    s0: "",
                    s1: '${(refer)!''}'
                });
                return false;
            }
        });

        //渲染方法
        function templateRendering(data, i){
            //rendering
            $("#productMainList").html( template("t:productBox", { data : data, currentIndex : i, currentStemIndex : currentStemIndex}) );
        }

        //status
        function statusInsert(data){
            var validItems = "${validItems!}";//购买过类型Array
            var statusAllList = [];//设置类型是否购买Array

            if(data.length > 0){
                for(var i = 0, len = data.length; i < len; i++){
                    if(validItems != ""  && validItems.indexOf(data[i].id) > -1 ){
                        statusAllList.push(1);
                    }else{
                        statusAllList.push(0);
                    }
                }

                var $intType = $17.getQuery("type");
                for(var b = 0, lenTwo = data.length; b < lenTwo; b++){
                    data[b].statusType = statusAllList[b];//给每个类型设置是否购买属性

                    if(data[b].type == "WalkerElf" || data[b].type == "Stem101"){
                        data[b].infoTypeName = "难度：";
                    }else{
                        data[b].infoTypeName = "周期：";
                    }

                    //是否从外部跳转入口
                    if( !$17.isBlank($intType) && $intType <= data.length){
                        if(b == $intType && data[b].statusType == 0){
                            currentIndex = $intType;
                        }
                    }
                }
            }
        }

        statusInsert(availableProducts);//insert value

        templateRendering(availableProducts, currentIndex);//初始化
    });
</script>
</#if>
</@com.page>