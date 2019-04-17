<#import "../layout_default.ftl" as layout_default>
<@layout_default.page page_title="热血跑酷配置">

<span><h1>正在加载数据....挺慢的@_@</h1></span>
<script>
var stageList = null;
var levelSpeed = null;
var monthPrizeConf = null;
var pkItemMale = null;
var pkItemFemale = null;
var shopItem = null;
var allPkItem = null;
function init(callback) {
    var queryUrl = "getInit.vpage";
    $.post(queryUrl, {}, function (data) {
                stageList = data.stageList;
                levelSpeed = data.levelSpeed;
                monthPrizeConf = data.monthPrizeConf;
                pkItemMale = data.pkItemMale;
                pkItemFemale = data.pkItemFemale;
                shopItem = data.shopItem;
                allPkItem = new Array();
                for(var i = 0 ; i < pkItemMale.length ; i++){
                    allPkItem.push(pkItemMale[i]);
                }
                for(var i = 0 ; i < pkItemFemale.length ; i++){
                    allPkItem.push(pkItemFemale[i]);
                }
                if (undefined != callback && null != callback) {
                    callback();
                }
            }
            , 'json');
}
$(function () {
    $(".navbar").remove();
    init(function () {
        $("span").remove();
        //等级配速
        var content = '<br><legend>等级经验及配速</legend><table class="table-hover table-striped table-bordered">'
                + '<tbody>';
        for (var i = 0; i < levelSpeed.length; i++) {
            var lp = levelSpeed[i];
            content += '<tr class="lvl" level="' + lp.level + '">';
            content += '<td><span class="lp' + lp.level + '">' + lp.level + '级</span></td>';
            content += '<td>配速：<span class="sp' + lp.level + '"><input type="text" class="numInt" value="' + lp.speed + '"/></span></td>';
            content += '<td>经验：<span class="exp' + lp.level + '"><input type="text" class="numInt" value="' + lp.exp + '"/></span></td>';
            content += '</tr>';
        }
        content += '<tr><td><input type="button" value="添加等级" class="btn btn-primary addLevel"></td></tr>';
        content += '</tbody></table>';
        content += '<br>';
        content += '<br>';
        content += '<br>';
        $('.row-fluid').append(content);

        //登陆奖励
        var drawPrizeConf = function (title, month, spanClass) {
            var html = '<legend>' + title + '</legend><table class="table table-hover table-striped table-bordered">'
                    + '<tbody>';
            html += '<tr>';
            var sel = '';
            for(var day = 0 ; day < 20 ; day++){
                var pre="第"+(day+1)+"天:"
                for(var m = 0 ; m < 2 ; m++){
                    var preGender = 0 == m ? "男" : "女";
                    var item = 0 == m ? month.prizeItemId.MALE[day] : month.prizeItemId.FEMALE[day];
                    var pkItem = 0== m ? pkItemMale : pkItemFemale;
                    var optionList = buildPkItemOption("没有奖励","",item,pkItem);
                    sel +=pre+preGender+'<select>'+optionList+'</select>&nbsp;';
                }
                sel +='<br>';
            }
            html += '<td><span class="' + spanClass + '">' + sel + '</span></td>';
            html += '</tr>';

            html += '</tbody></table>';
            $('.row-fluid').append(html);
        }
        drawPrizeConf("本月登录奖励", monthPrizeConf.thisMonth, "thisMonth")
        drawPrizeConf("下月登录奖励", monthPrizeConf.nextMonth, "nextMonth")

        content = '<legend>金币商店配置</legend><table class="table table-hover table-striped table-bordered">'
                + '<tbody>';
        content += '<tr><th>商品</th><th>商品显示名称</th><th>商品说明文字(非必填)</th><th>价格</th><th>操作</th></tr>'


        for (var i = 0; i < shopItem.length; i++) {
            content += '<tr class="shopItem">'
            content += '<td><select>';
            content += buildPkItemOption("活力补满","PI_00001",shopItem[i].itemId,allPkItem);
            content += '</select></td>';
            content += '<td><input type="text" class="dispName notBlankStr" value="'+shopItem[i].displayName+'"/></td>'
            content += '<td><input type="text" class="dispBrief" value="'+shopItem[i].brief+'"/></td>'
            content += '<td><input type="text" class="price numInt" value="'+shopItem[i].coinPrice+'"/></td>'
            content += '<td><a role="button" class="btn btn-danger delShopItem" data-toggle="modal">删</a></td>'
            content +="</tr>"
        }
        content += '<tr><input type="button" class="btn btn-primary addShopItem" value="加商品"></tr>'
        content += '</tbody></table>';
        $('.row-fluid').append(content);
        content = '<legend>关卡配置</legend><table class="table table-hover table-striped table-bordered">'
                + '<tbody>';
        content += '<tr><th>关号</th><th>topic</th><th>词汇列表</th><th>★★★金币</th><th>赛道长度</th><th>通关时间及经验</th><th>AI</th><th>障碍数</th><th>地上金币数</th><th>直接失败错题数</th>'
        var inputTextStyle = 'width:50px;'
        for (var i = 0; i < stageList.length; i++) {
            var stg = stageList[i];
            content += '<tr class="stageTr" stageId="' + stg.stageId + '"><td>' + stg.stageId + '</td><td><input type="text" class="topic notBlankStr" style="width:60px;" value="' + stg.topic + '"/></td>'
            var wordHtm = ''
            for (var j = 0; j < stg.wordList.length; j++) {//单词
                var word = stg.wordList[j];
                wordHtm += '<div class="stageWord">ID:<input style="' + inputTextStyle + '" class="numInt wordId" type="text" value="' + word.wordId + '"/>;概率:<input class="numInt achieveRate" style="' + inputTextStyle + '" type="text" value="' + word.achieveRate + '"/>;奖励学豆:<input class="numInt collectIntegeral" style="' + inputTextStyle + '" type="text" value="' + word.collectIntegeral + '"/><a data-toggle="modal" class="btn btn-danger delWord" role="button">删</a></div>';
            }

            content += '<td>' + wordHtm + '<input type="button" value="加词" class="btn btn-primary addWord"/></td>';

            content += '<td><input type="text" class="numInt stageCoinBonus" style="' + inputTextStyle + '" value="' + stg.stageCoinBonus + '"/></td><td><input class="numInt distance" style="' + inputTextStyle + '" type="text" value="' + stg.distance + '"/></td>'

            var tmhtml = ''
            for (var k = 0; k < 3; k++) {
                tmhtml += "<div>"
                for (var m = 0; m < 3; m++) {
                    if (m > k) {
                        tmhtml += '☆'
                    } else {
                        tmhtml += '★'
                    }
                }
                tmhtml += '时间（毫秒）:<input class="numInt timeForStar' + (k + 1) + '" type="text" style="' + inputTextStyle + '" value="' + stg.timeForStar[k] + '"/>;经验:<input class="numInt star' + (k + 1) + 'Exp" type="text" style="' + inputTextStyle + '" value="' + stg.exp[k] + '"/>'
                tmhtml += "</div>"
            }
            content += '<td>' + tmhtml + "</td>";

            content += '<td><div>等级:<input class="numInt aiLevel" type="text" style="' + inputTextStyle + '" value="' + stg.stageAi.level + '"/>;</div><div>每题时间（毫秒）:<input class="numInt aiTimePerQuestion" type="text" style="' + inputTextStyle + '" value="' + stg.stageAi.timePerQuestion + '"/>;</div><div>正确率:<input class="positiveLess1Float aiCorrectRate" type="text" style="' + inputTextStyle + '" value="' + stg.stageAi.correctRate + '"/>;</div></td>'

            content += '<td><input class="numInt barricadeCount" type="text" style="' + inputTextStyle + '" value="' + stg.barricadeCount + '"/></td><td><input class="numInt pickCountCount" type="text" style="' + inputTextStyle + '" value="' + stg.pickCountCount + '"/></td><td><input class="numInt failErrorCount" type="text" style="' + inputTextStyle + '" value="' + stg.failErrorCount + '"/></td>'
            content += '</tr>'
        }
        content += '</tbody></table>';
        $('.row-fluid').append(content);

        $('.row-fluid').append('<div class="span9"><input type="button" value="点我保存" class="btn btn-info btnSave" style="position:fixed;height:60px;width:160px;right:150px;top:300px;font-size:25px;"/></div>')
    });
    var saveCheck = function () {
        var errorMsg = '';
        $("input").css("background-color", "white");
        $(".chkError").removeClass("chkError");
        var errorCount = 0;
        var positiveIntReg = /^[0-9]*[1-9][0-9]*$/
        var erMsg = ''
        $(".numInt").each(function () {//正整数
            if (!$(this).val().match(positiveIntReg)) {
                errorCount++;
                $(this).css("background-color", "#da4f49");
                $(this).addClass("chkError");
                if ('' == erMsg) {
                    erMsg = '需输入正整数\n'
                }
            }
        });
        errorMsg += erMsg;
        erMsg = ''
        var positiveLess1FloatReg = /^(([0-9]+\.[0-9]*[1-9][0-9]*)|([0-9]*[1-9][0-9]*\.[0-9]+)|([0-9]*[1-9][0-9]*))$/
        $(".positiveLess1Float").each(function () {//小于等于1的正整数
            if (!$(this).val().match(positiveLess1FloatReg) || parseFloat($(this).val()) > 1.0) {
                errorCount++;
                $(this).css("background-color", "#da4f49");
                $(this).addClass("chkError");
                if ('' == erMsg) {
                    erMsg = 'AI正确率需输入不大于1的正小数\n'
                }
            }
        });
        errorMsg += erMsg;
        erMsg = ''

        $(".notBlankStr").each(function () {
            if ($.trim($(this).val()) == "") {
                errorCount++;
                $(this).css("background-color", "#da4f49");
                $(this).addClass("chkError");
                if ('' == erMsg) {
                    erMsg = 'topic不可为空\n'
                }
            }
        });
        errorMsg += erMsg;
        erMsg = ''
        $(".stageTr").each(function () {
            var rateSum = 0;
            $(this).find(".achieveRate").each(function () {
                rateSum += parseInt($(this).val());
            });
            if (100 != rateSum) {
                errorCount++;
                $(this).find(".achieveRate").css("background-color", "#da4f49");
                $(this).find(".achieveRate").addClass("chkError");
                erMsg += '第' + $(this).attr("stageId") + '关的拼图掉落概率错误。各拼图掉落概率总和必须为100\n';
            }
        });

        errorMsg += erMsg;
        erMsg = ''


        $(".stageTr").each(function () {
            var timeStar = $(this).find(".timeForStar1").val();
            var thisTime = ''
            for (var i = 2; i < 4; i++) {
                thisTime = $(this).find('.timeForStar' + i).val();
                if (parseInt(thisTime) >= parseInt(timeStar)) {
                    erMsg += '第' + $(this).attr("stageId") + '关的通关时间配错。高星的时间必须快于低星\n';
                    errorCount++;
                    $(this).find('.timeForStar' + i).css("background-color", "#da4f49");
                    $(this).find('.timeForStar' + i).addClass("chkError");
                    break;
                } else {
                    timeStar = thisTime;
                }
            }
        });
        errorMsg += erMsg;
        if (errorCount > 0) {
            $(".chkError:first")[0].focus();
            alert("共有" + $(".chkError").length + "处错误，详见标红处。错误提示:\n" + errorMsg);
            return false;
        }

        return true;
    };
    $(document).on("click", ".btnSave", function () {
        if (saveCheck()) {
            var levelConf = new Array();
            $(".lvl").each(function () {
                var level = {level: $(this).attr("level"), speed: $(this).find("input:first").val(), exp: $(this).find("input:last").val()};
                levelConf.push(level);
            });
            var thisMonthPrizeMale = new Array();
            var thisMonthPrizeFemale = new Array();
            var counter = 0 ;
            $(".thisMonth").find("select").each(function () {
                if(counter % 2 == 0)
                    thisMonthPrizeMale.push($(this).val());
                else
                    thisMonthPrizeFemale.push($(this).val());
                counter ++;
            });
            var thisMonthPrize = {MALE:thisMonthPrizeMale,FEMALE:thisMonthPrizeFemale};

            var nextMonthPrizeMale = new Array();
            var nextMonthPrizeFemale = new Array();
            counter = 0 ;
            $(".nextMonth").find("select").each(function () {
                if ("" != $(this).val()) {
                    if(counter % 2 == 0)
                        nextMonthPrizeMale.push($(this).val());
                    else
                        nextMonthPrizeFemale.push($(this).val());
                }
                counter ++;
            });
            var nextMonthPrize = {MALE:nextMonthPrizeMale,FEMALE:nextMonthPrizeFemale};

            var shopItemList = new Array();
            var shopItemId = 1;
            $(".shopItem").each(function(){
                var shopItem = {};
                shopItem.id = shopItemId;
                shopItemId++;
                shopItem.itemType = $(this).find("select:last").val().indexOf("PI") >= 0 ? "PARKOUR_ITEM" : "PK_FASHION";
                shopItem.itemId = $(this).find("select:last").val();
                shopItem.displayName = $(this).find(".dispName").val();
                shopItem.brief = $(this).find(".dispBrief").val();
                shopItem.coinPrice = $(this).find(".price").val();
                shopItemList.push(shopItem);
            });
            var stageConf = new Array();
            $(".stageTr").each(function () {
                var stage = {};
                stage.stageId = $(this).attr("stageid");
                stage.topic = $(this).find(".topic").val();
                var stageWord = new Array();
                $(this).find(".stageWord").each(function () {
                    var word = {};
                    word.stageId = $(this).parents(".stageTr:first").attr("stageid");
                    word.wordId = $(this).find(".wordId").val();
                    word.achieveRate = $(this).find(".achieveRate").val();
                    word.collectIntegeral = $(this).find(".collectIntegeral").val();
                    stageWord.push(word);
                });
                stage.wordList = stageWord;
                stage.stageCoinBonus = $(this).find(".stageCoinBonus").val();
                var timeForStar = new Array();
                var exp = new Array();
                for (var i = 1; i < 4; i++) {
                    timeForStar.push($(this).find(".timeForStar" + i).val());
                    exp.push($(this).find(".star" + i + "Exp").val());
                }
                stage.timeForStar = timeForStar;
                stage.exp = exp;
                var stageAi = {};
                stageAi.level = $(this).find(".aiLevel").val();
                stageAi.timePerQuestion = $(this).find(".aiTimePerQuestion").val();
                stageAi.correctRate = $(this).find(".aiCorrectRate").val();
                stage.stageAi = stageAi;
                stage.barricadeCount = $(this).find(".barricadeCount").val();
                stage.pickCountCount = $(this).find(".pickCountCount").val();
                stage.failErrorCount = $(this).find(".failErrorCount").val();
                stage.distance = $(this).find(".distance").val();
                stageConf.push(stage);
            });
            var queryUrl = "saveAll.vpage";
            $.post(queryUrl, {levelSpeed: JSON.stringify(levelConf), thisMonthPrize: JSON.stringify(thisMonthPrize), nextMonthPrize: JSON.stringify(nextMonthPrize), stageConf: JSON.stringify(stageConf),shopItemList:JSON.stringify(shopItemList)}, function (data) {
                        if(data.success){
                            alert("保存成功");
                        }else{
                            alert("保存失败");
                        }
                    }
                    , 'json');
        }
    });

    $(document).on("click", ".delWord", function () {
        $(this).parents("div:first").remove();
    });

    $(document).on("click", ".addWord", function () {
        $(this).before('<div class="stageWord">ID:<input type="text" class="numInt wordId" style="width:50px;">;概率:<input type="text" style="width:50px;" class="numInt achieveRate">;奖励学豆:<input type="text" style="width:50px;" class="numInt collectIntegeral"><a data-toggle="modal" class="btn btn-danger delWord" role="button">删</a></div>');
    });
    $(document).on("click", ".addLevel", function () {
        var newLevel = parseInt($(".lvl:last").attr("level"))+1;
        $(".lvl:last").after('<tr level="'+newLevel+'" class="lvl"><td><span class="lp'+newLevel+'">'+newLevel+'级</span></td><td>配速：<span class="sp'+newLevel+'"><input type="text" class="numInt"></span></td><td>经验：<span class="exp'+newLevel+'"><input type="text" class="numInt"></span></td></tr>');
    });

    $(document).on("click", ".addShopItem", function () {
        var html = '<tr class="shopItem">'
        html += '<td><select>';
        html += buildPkItemOption("活力补满","PI_00001","",allPkItem);
        html += '</select></td>';
        html += '<td><input type="text" class="dispName notBlankStr" value=""/></td>'
        html += '<td><input type="text" class="dispBrief" value=""/></td>'
        html += '<td><input type="text" class="price numInt" value=""/></td>'
        html += '<td><a role="button" class="btn btn-danger delShopItem" data-toggle="modal">删</a></td>'
        html +="</tr>"
        $(".shopItem:last").after(html);
    });

    $(document).on("click", ".delShopItem", function () {
        $(this).parents("tr:first").remove();
    });

});

function buildPkItemOption(defaultOptionName, defaultOptionValue,pkItemId, allItem){
    var html = '<option value="'+defaultOptionValue+'">'+defaultOptionName+'</option>';
    for(var i = 0 ; i < allItem.length ; i++){
        var selected = allItem[i].itemId == pkItemId ? 'selected' : '';
        html += '<option value="' + allItem[i].itemId + '" '+selected+'>'+allItem[i].itemName+'</option>';
    }
    return html;

}
</script>
</@layout_default.page>