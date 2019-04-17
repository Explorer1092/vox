//判断功能导航位置
function checkPopMenuPosition(){
    var $popMenu = $(".quiz_footPopUpMenu");
    if($popMenu.offset().top >= $(".m-footer").offset().top){
        $popMenu.addClass("quiz_footPopUpMenu_clear");
    }
    if($(".quiz_footPopUpMenu_contrast").offset().top - 67 <= $popMenu.offset().top){
        $popMenu.removeClass("quiz_footPopUpMenu_clear");
    }
}

//步骤表单验证函数
function appChecker(to){
    //去除未通过验证变红样式
    $(".check_mistake").removeClass("int_vox_mistake_er");
    $(ReadingApp).freezing("from_checker");

    //表单验证
    if(to == 2 || to == 3 || to == 4 || to == "submit"){
        if($17.isBlank(ReadingApp.DataBase.ename)){
            showStep1("I am God");

            $("#step1_title").addClass("int_vox_mistake_er");
            $(ReadingApp).thaw("from_checker");
            return false;
        }
        if($17.isBlank(ReadingApp.DataBase.points)){
            showStep1("I am God");
            $('#selectTree').find('div.pointsSelect').addClass("int_vox_mistake_er");
            //$("#step1_pointsSelect").closest("div.check_mistake").addClass("int_vox_mistake_er");
            $(ReadingApp).thaw("from_checker");
            return false;
        }
        if($17.isBlank(ReadingApp.DataBase.style)){
            showStep1("I am God");
            $('#step1_styles').addClass("int_vox_mistake_er");
            $(ReadingApp).thaw("from_checker");
            return false;
        }
        if($17.isBlank(ReadingApp.DataBase.difficultyLevel)){
            showStep1("I am God");
            $('#step1_levels').addClass("int_vox_mistake_er");
            $(ReadingApp).thaw("from_checker");
            return false;
        }
        $("#step1_title").removeClass("int_vox_mistake_er");
        $('#selectTree').find('div.pointsSelect').removeClass("int_vox_mistake_er");
        $('#step1_styles').removeClass("int_vox_mistake_er");
        $('#step1_levels').removeClass("int_vox_mistake_er");
    }

    //重点词汇验证
    if(to == 4 || to == "submit"){
        $("input.keyMap_enbox").removeClass("int_vox_mistake_er");
        for(var index = 0, readingLength = ReadingApp.DataBase.readingPages.length; index < readingLength; index++){
            if($17.isBlank(ReadingApp.DataBase.readingPages[index].firstHalfPage)){
                var words = ReadingApp.DataBase.readingPages[index].readingSentences || [];
                var keys = ReadingApp.DataBase.readingPages[index].keyWords || [];

                for(var i = 0, l = keys.length; i < l; i++){
                    var check = false;
                    for(var ii = 0, ll = words.length; ii < ll; ii++){
                        if(words[ii].entext && words[ii].entext.toUpperCase().indexOf(keys[i].entext.toUpperCase()) != -1){
                            check = true;
                        }
                        check = $17.isBlank(keys[i].entext) ? true : check;
                    }

                    if(words.length != 0 && !check){
                        ReadingApp.DataBase.pagesIndex = index;
                        showStep3("I am God");

                        $(ReadingApp).thaw("from_checker");
                        $("p.keyMapsJust span.keyMap[data-keymapindex='" + i + "']").find("input.keyMap_enbox").addClass("int_vox_mistake_er");
                    }
                }

                var $parts_en_box = $("#parts_en_box");
                var $parts_cn_box = $("#parts_cn_box");

                $parts_en_box.removeClass("int_vox_mistake_er");
                if($parts_en_box.length > 0 && !$parts_en_box.is(":hidden") && $17.isBlank($parts_en_box.val())){
                    ReadingApp.DataBase.pagesIndex = index;
                    showStep3("I am God");

                    $(ReadingApp).thaw("from_checker");
                    $parts_en_box.addClass("int_vox_mistake_er");
                }
                $parts_cn_box.removeClass("int_vox_mistake_er");
                if($parts_cn_box.length > 0 && !$parts_cn_box.is(":hidden") && $17.isBlank($parts_cn_box.val())){
                    ReadingApp.DataBase.pagesIndex = index;
                    showStep3("I am God");

                    $(ReadingApp).thaw("from_checker");
                    $parts_cn_box.addClass("int_vox_mistake_er");
                }
            }else{
                var wordsFrount = ReadingApp.DataBase.readingPages[index].firstHalfPage.readingSentences || [];
                var keysFrount = ReadingApp.DataBase.readingPages[index].firstHalfPage.keyWords || [];

                for(i = 0, l = keysFrount.length; i < l; i++){
                    check = false;
                    for(var ii = 0, ll = wordsFrount.length; ii < ll; ii++){
                        if(wordsFrount[ii].entext && wordsFrount[ii].entext.toUpperCase().indexOf(keysFrount[i].entext.toUpperCase()) != -1){
                            check = true;
                        }
                        check = $17.isBlank(keysFrount[i].entext) ? true : check;
                    }

                    if(wordsFrount.length != 0 && !check){
                        ReadingApp.DataBase.pagesIndex = index;
                        showStep3("I am God");

                        $(ReadingApp).thaw("from_checker");
                        $("p.keyMapsFrount span.keyMap[data-keymapindex='" + i + "']").find("input.keyMap_enbox").addClass("int_vox_mistake_er");
                    }
                }

                var $front_en_box = $("#front_en_box");
                var $front_cn_box = $("#front_cn_box");

                $front_en_box.removeClass("int_vox_mistake_er");
                if($front_en_box.length > 0 && !$front_en_box.is(":hidden") && $17.isBlank($front_en_box.val())){
                    ReadingApp.DataBase.pagesIndex = index;
                    showStep3("I am God");

                    $(ReadingApp).thaw("from_checker");
                    $front_en_box.addClass("int_vox_mistake_er");
                }
                $front_cn_box.removeClass("int_vox_mistake_er");
                if($front_cn_box.length > 0 && !$front_cn_box.is(":hidden") && $17.isBlank($front_cn_box.val())){
                    ReadingApp.DataBase.pagesIndex = index;
                    showStep3("I am God");

                    $(ReadingApp).thaw("from_checker");
                    $front_cn_box.addClass("int_vox_mistake_er");
                }

                var wordsBack = ReadingApp.DataBase.readingPages[index].afterHalfPage.readingSentences || [];
                var keysBack = ReadingApp.DataBase.readingPages[index].afterHalfPage.keyWords || [];

                for(i = 0, l = keysBack.length; i < l; i++){
                    check = false;
                    for(ii = 0, ll = wordsBack.length; ii < ll; ii++){
                        if(wordsBack[ii].entext && wordsBack[ii].entext.toUpperCase().indexOf(keysBack[i].entext.toUpperCase()) != -1){
                            check = true;
                        }
                        check = $17.isBlank(keysBack[i].entext) ? true : check;
                    }

                    if(wordsBack.length != 0 && !check){
                        ReadingApp.DataBase.pagesIndex = index;
                        showStep3("I am God");

                        $(ReadingApp).thaw("from_checker");
                        $("p.keyMapsBack span.keyMap[data-keymapindex='" + i + "']").find("input.keyMap_enbox").addClass("int_vox_mistake_er");
                    }
                }

                var $back_en_box = $("#back_en_box");
                var $back_cn_box = $("#back_cn_box");

                $back_en_box.removeClass("int_vox_mistake_er");
                if($back_en_box.length > 0 && !$back_en_box.is(":hidden") && $17.isBlank($back_en_box.val())){
                    ReadingApp.DataBase.pagesIndex = index;
                    showStep3("I am God");

                    $(ReadingApp).thaw("from_checker");
                    $back_en_box.addClass("int_vox_mistake_er");
                }
                $back_cn_box.removeClass("int_vox_mistake_er");
                if($back_cn_box.length > 0 && !$back_cn_box.is(":hidden") && $17.isBlank($back_cn_box.val())){
                    ReadingApp.DataBase.pagesIndex = index;
                    showStep3("I am God");

                    $(ReadingApp).thaw("from_checker");
                    $back_cn_box.addClass("int_vox_mistake_er");
                }
            }
        }
    }

    if(to == "submit"){
        var questions = [].concat(ReadingApp.DataBase.readingQuestions);

        var question = null;
        for(var i = 0, l = questions.length; i < l; i++){
            question = questions[i];
            switch(question.type){
                case 1://填空
                    var cache = question.content || "";
                    if(cache.indexOf("[") == -1){
                        $("div[data-rank='" + question.rank + "']").find("textarea.selectionContent").addClass("int_vox_mistake_er");
                        $(ReadingApp).thaw("from_checker");
                    }else{
                        cache = cache.substring(cache.indexOf("[") + 1, cache.length);
                        if(cache.indexOf("]") == -1){
                            $("div[data-rank='" + question.rank + "']").find("textarea.selectionContent").addClass("int_vox_mistake_er");
                            $(ReadingApp).thaw("from_checker");
                        }
                    }
                    break;
                case 2://单选
                    if($17.isBlank(question.content)){
                        $("div[data-rank='" + question.rank + "']").find("input.selectionContent").addClass("int_vox_mistake_er");
                        $(ReadingApp).thaw("from_checker");
                    }
                    for(var j = 0, jl = question.answerOptions.length; j < jl; j++){
                        if($17.isBlank(question.answerOptions[j])){
                            $("div[data-rank='" + question.rank + "']").find("input.selectionOptionContent[data-value='" + j + "']").addClass("int_vox_mistake_er");
                            $(ReadingApp).thaw("from_checker");
                        }
                    }
                    break;
                case 3://判断
                    if($17.isBlank(question.content)){
                        $("div[data-rank='" + question.rank + "']").find("input.selectionContent").addClass("int_vox_mistake_er");
                        $(ReadingApp).thaw("from_checker");
                    }
                    break;
            }
        }
    }

    return $(ReadingApp).isFreezing("from_checker");
}

function setStyleValue(){
    ReadingApp.DataBase.style = $("#step1_styles").find("b.title").attr("data-value");
}

function setLevelValue(){
    ReadingApp.DataBase.difficultyLevel = $("#step1_levels").find("b.title").attr("data-value");
}

function radioCover(){
    if(!$17.isBlank(ReadingApp.DataBase.coverUri)){
        $.prompt("更换模板会丢失之前上传的图片，是否继续？", {
            title  : "系统提示",
            focus  : 1,
            buttons: { "确定": true, "取消": false },
            submit : function(e, v){
                if(v){
                    ReadingApp.DataBase.coverIndex = $(this).attr("data-coverindex");

                    showStep2();
                }
            }
        });
    }else{
        ReadingApp.DataBase.coverIndex = $(this).attr("data-coverindex");

        showStep2();
    }
}

function radioColor(){
    ReadingApp.DataBase.coverIndex = $("span[data-coverindex].active").attr("data-coverindex");
    ReadingApp.DataBase.colorId = $(this).attr("data-colorid");

    showStep2();
}

function upload_cover_btn(){
    $("#upload_flash").show();

    $("#upload_cover_btn").hide();
}

function ugc_callback(data){
    data = JSON.parse(data);
    ReadingApp.DataBase.coverUri = data.cover;
    ReadingApp.DataBase.coverUri1 = data.least;
    ReadingApp.DataBase.coverUri2 = data.unfinished;
    ReadingApp.DataBase.coverUri3 = data.finished;

    showStep2();
}

function ugc_cancel(){
    $("#upload_flash").hide();

    $("#upload_cover_btn").show();
}

//第三页，更换模板
function step3_change_template(){
    var $self = $(this);
    $.prompt("更换模板会使您本页已经添加的数据丢失，确定要更换模板吗？", {
        title  : "系统提示",
        focus  : 1,
        buttons: { "取消": false, "确定": true },
        submit : function(e, v){
            if(v){
                switch($self.attr("data-layout")){
                    case "ptpt":
                        var newPage = $.extend(true, {}, ReadingApp.Constructor.readingDraftHalfPageTemp);
                        newPage.pageNum = ReadingApp.DataBase.pagesIndex;
                        newPage.pageLayout = "ptpt";
                        ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex] = newPage;
                        break;
                    case "tt":
                        var newPage = $.extend(true, {}, ReadingApp.Constructor.readingDraftHalfPageTemp);
                        newPage.pageNum = ReadingApp.DataBase.pagesIndex;
                        newPage.pageLayout = "tt";
                        ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex] = newPage;
                        break;
                    case "pt":
                        var newPage = $.extend(true, {}, ReadingApp.Constructor.readingDraftPageTemp);
                        newPage.pageNum = ReadingApp.DataBase.pagesIndex;
                        newPage.pageLayout = "pt";
                        ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex] = newPage;
                        break;
                    case "tp":
                        var newPage = $.extend(true, {}, ReadingApp.Constructor.readingDraftPageTemp);
                        newPage.pageNum = ReadingApp.DataBase.pagesIndex;
                        newPage.pageLayout = "tp";
                        ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex] = newPage;
                        break;
                    case "wp":
                        var newPage = $.extend(true, {}, ReadingApp.Constructor.readingDraftPageTemp);
                        newPage.pageNum = ReadingApp.DataBase.pagesIndex;
                        newPage.pageLayout = "wp";
                        ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex] = newPage;
                        break;
                }

                showStep3();
            }
        }
    });
    return false;
}

//删除当前页
function step3_delete_page(){
    if(ReadingApp.DataBase.readingPages.length == 1){
        $17.alert("阅读至少要有一页");
    }else{
        ReadingApp.DataBase.readingPages.splice(ReadingApp.DataBase.pagesIndex, 1);
        ReadingApp.DataBase.pagesIndex -= 1;

        for(var i = 0, l = ReadingApp.DataBase.readingPages.length; i < l; i++){
            ReadingApp.DataBase.readingPages[i].pageNum = i;
        }

        showStep3();
    }

    return false;
}

//添加一页
function step3_add_page(){
    //表单验证
    if(!appChecker(4)){
        return false;
    }

    savePageParts();

    var newPage = $.extend(true, {}, ReadingApp.Constructor.readingDraftHalfPageTemp);
    newPage.pageLayout = "ptpt";
    ReadingApp.DataBase.pagesIndex += 1;
    ReadingApp.DataBase.readingPages.splice(ReadingApp.DataBase.pagesIndex, 0, newPage);

    for(var i = 0, l = ReadingApp.DataBase.readingPages.length; i < l; i++){
        ReadingApp.DataBase.readingPages[i].pageNum = i;
    }

    showStep3();

    $17.backToTop();

    return false;
}

function step3_leftMenuToPages(){
    ReadingApp.DataBase.pagesIndex = $(this).attr("data-pageindex") * 1;

    showStep3();

    return false;
}

function step3_parts_front_done(){
    var $front_en_box = $("#front_en_box");
    var $front_cn_box = $("#front_cn_box");
    var $audio_upload_one_input = $("#audio_upload_one_input");
    $front_en_box.removeClass("int_vox_mistake_er");
    $front_cn_box.removeClass("int_vox_mistake_er");

    $(ReadingApp).freezing("from_checker");

    if($17.isBlank($front_en_box.val())){
        $front_en_box.addClass("int_vox_mistake_er");
        $(ReadingApp).thaw("from_checker");
    }
    if($17.isBlank($front_cn_box.val())){
        $front_cn_box.addClass("int_vox_mistake_er");
        $(ReadingApp).thaw("from_checker");
    }
    if($(ReadingApp).isFreezing("from_checker")){
        var $target = $("#sentence_editer_front");

        if($target.attr("data-actiontype") == "add"){
            var index = $target.attr("data-sentenceid") * 1;
            if(ReadingApp.isDone == true){
                var __check = ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex].firstHalfPage.readingSentences[ReadingApp.doneType == "little" ? index : index + 1];
                __check.entext = $front_en_box.val();
                __check.cntext = $front_cn_box.val();
                __check.audioUri = $audio_upload_one_input.val();

                ReadingApp.isDone = false;
            }else{
                var __check = ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex].firstHalfPage.readingSentences[index];

                var newParts = $.extend(true, {}, ReadingApp.Constructor.readingDraftSentenceTemp);
                newParts.entext = $front_en_box.val();
                newParts.cntext = $front_cn_box.val();
                newParts.audioUri = $audio_upload_one_input.val();

                ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex].firstHalfPage.readingSentences.splice($17.isBlank(__check) ? index : index + 1, 0, newParts);
            }
        }else{
            var parts = ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex].firstHalfPage.readingSentences[$target.attr("data-sentenceid") * 1];
            parts.entext = $front_en_box.val();
            parts.cntext = $front_cn_box.val();
            parts.audioUri = $audio_upload_one_input.val();
            ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex].firstHalfPage.readingSentences[$target.attr("data-sentenceid") * 1] = parts;
        }

        showStep3();
    }
    return false;
}

function step3_parts_back_done(){
    var $back_en_box = $("#back_en_box");
    var $back_cn_box = $("#back_cn_box");
    var $audio_upload_two_input = $("#audio_upload_two_input");
    $back_en_box.removeClass("int_vox_mistake_er");
    $back_cn_box.removeClass("int_vox_mistake_er");

    $(ReadingApp).freezing("from_checker");

    if($17.isBlank($back_en_box.val())){
        $back_en_box.addClass("int_vox_mistake_er");
        $(ReadingApp).thaw("from_checker");
    }
    if($17.isBlank($back_cn_box.val())){
        $back_cn_box.addClass("int_vox_mistake_er");
        $(ReadingApp).thaw("from_checker");
    }
    if($(ReadingApp).isFreezing("from_checker")){
        var $target = $("#sentence_editer_back");

        if($target.attr("data-actiontype") == "add"){
            var index = $target.attr("data-sentenceid") * 1;
            if(ReadingApp.isDone == true){
                var __check = ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex].afterHalfPage.readingSentences[ReadingApp.doneType == "little" ? index : index + 1];
                __check.entext = $front_en_box.val();
                __check.cntext = $front_cn_box.val();
                __check.audioUri = $audio_upload_one_input.val();

                ReadingApp.isDone = false;
            }else{
                var __check = ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex].afterHalfPage.readingSentences[index];

                var newParts = $.extend(true, {}, ReadingApp.Constructor.readingDraftSentenceTemp);
                newParts.entext = $back_en_box.val();
                newParts.cntext = $back_cn_box.val();
                newParts.audioUri = $audio_upload_two_input.val();

                ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex].afterHalfPage.readingSentences.splice($17.isBlank(__check) ? index : index + 1, 0, newParts);
            }
        }else{
            var parts = ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex].afterHalfPage.readingSentences[$target.attr("data-sentenceid") * 1];
            parts.entext = $back_en_box.val();
            parts.cntext = $back_cn_box.val();
            parts.audioUri = $audio_upload_two_input.val();
            ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex].afterHalfPage.readingSentences[$target.attr("data-sentenceid") * 1] = parts;
        }

        showStep3();
    }
    return false;
}

function step3_parts_done(){
    var $parts_en_box = $("#parts_en_box");
    var $parts_cn_box = $("#parts_cn_box");
    var $audio_upload_one_input = $("#audio_upload_one_input");
    $parts_en_box.removeClass("int_vox_mistake_er");
    $parts_cn_box.removeClass("int_vox_mistake_er");

    $(ReadingApp).freezing("from_checker");

    if($17.isBlank($parts_en_box.val())){
        $parts_en_box.addClass("int_vox_mistake_er");
        $(ReadingApp).thaw("from_checker");
    }
    if($17.isBlank($parts_cn_box.val())){
        $parts_cn_box.addClass("int_vox_mistake_er");
        $(ReadingApp).thaw("from_checker");
    }
    if($(ReadingApp).isFreezing("from_checker")){
        var $target = $("#sentence_editer");

        if($target.attr("data-actiontype") == "add"){
            var index = $target.attr("data-sentenceid") * 1;
            if(ReadingApp.isDone == true){
                var __check = ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex].readingSentences[ReadingApp.doneType == "little" ? index : index + 1];
                __check.entext = $parts_en_box.val();
                __check.cntext = $parts_cn_box.val();
                __check.audioUri = $audio_upload_one_input.val();

                ReadingApp.isDone = false;
            }else{
                var __check = ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex].readingSentences[index];

                var newParts = $.extend(true, {}, ReadingApp.Constructor.readingDraftSentenceTemp);
                newParts.entext = $parts_en_box.val();
                newParts.cntext = $parts_cn_box.val();
                newParts.audioUri = $audio_upload_one_input.val();

                ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex].readingSentences.splice($17.isBlank(__check) ? index : index + 1, 0, newParts);
            }
        }else{
            var parts = ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex].readingSentences[$target.attr("data-sentenceid") * 1];
            parts.entext = $parts_en_box.val();
            parts.cntext = $parts_cn_box.val();
            parts.audioUri = $audio_upload_one_input.val();
            ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex].readingSentences[$target.attr("data-sentenceid") * 1] = parts;
        }

        showStep3();
    }
    return false;
}

//功能菜单按钮
function step3_addSentencePlusBtn(){
    $(this).closest("span.quiz_ugc_old").find("span.buttons").toggle();

    return false;
}

//添加一句
function step3_addFrontSentenceBtn(){
    var $self = $(this);
    $self.closest("span.buttons").hide();
    $("#front_en_box").val("");
    $("#front_cn_box").val("");
    $("#sentence_editer_front").attr({
        "data-sentenceid": $self.attr("data-sentenceid"),
        "data-actiontype": "add"
    }).show();

    return false;
}

//编辑一句
function step3_editFrontSentenceBtn(){
    var $self = $(this);
    var sentenceId = $self.attr("data-sentenceid");
    var sentence = ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex].firstHalfPage.readingSentences[sentenceId];
    $self.closest("span.buttons").hide();
    $("#front_en_box").val(sentence.entext);
    $("#front_cn_box").val(sentence.cntext);
    $("#audio_upload_one_input").val(sentence.audioUri);
    $("#sentence_editer_front").attr({
        "data-sentenceid": sentenceId,
        "data-actiontype": "edit"
    }).show();
    return false;
}

//删除一句
function step3_deleteFrontSentenceBtn(){
    ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex].firstHalfPage.readingSentences.splice($(this).attr("data-sentenceid"), 1);
    showStep3();
    return false;
}

//添加一句
function step3_addBackSentenceBtn(){
    var $self = $(this);
    $self.closest("span.buttons").hide();
    $("#back_en_box").val("");
    $("#back_cn_box").val("");
    $("#sentence_editer_back").attr({
        "data-sentenceid": $self.attr("data-sentenceid"),
        "data-actiontype": "add"
    }).show();

    return false;
}

//编辑一句
function step3_editBackSentenceBtn(){
    var $self = $(this);
    var sentenceId = $self.attr("data-sentenceid");
    var sentence = ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex].afterHalfPage.readingSentences[sentenceId];
    $self.closest("span.buttons").hide();
    $("#back_en_box").val(sentence.entext);
    $("#back_cn_box").val(sentence.cntext);
    $("#audio_upload_two_input").val(sentence.audioUri);
    $("#sentence_editer_back").attr({
        "data-sentenceid": sentenceId,
        "data-actiontype": "edit"
    }).show();

    return false;
}

//删除一句
function step3_deleteBackSentenceBtn(){
    ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex].afterHalfPage.readingSentences.splice($(this).attr("data-sentenceid"), 1);
    showStep3();
    return false;
}

//添加一句
function step3_addSentenceBtn(){
    var $self = $(this);
    $self.closest("span.buttons").hide();
    $("#parts_en_box").val("");
    $("#parts_cn_box").val("");
    $("#sentence_editer").attr({
        "data-sentenceid": $self.attr("data-sentenceid"),
        "data-actiontype": "add"
    }).show();

    return false;
}

//编辑一句
function step3_editSentenceBtn(){
    var $self = $(this);
    var sentenceId = $self.attr("data-sentenceid");
    var sentence = ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex].readingSentences[sentenceId];
    $self.closest("span.buttons").hide();
    $self.closest("span.buttons").hide();
    $("#parts_en_box").val(sentence.entext);
    $("#parts_cn_box").val(sentence.cntext);
    $("#audio_upload_one_input").val(sentence.audioUri);
    $("#sentence_editer").attr({
        "data-sentenceid": sentenceId,
        "data-actiontype": "edit"
    }).show();
    return false;
}

//删除一句
function step3_deleteSentenceBtn(){
    ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex].readingSentences.splice($(this).attr("data-sentenceid"), 1);
    showStep3();
    return false;
}

//添加重点词
function step3_keyMapFrontPlusBtn(){
    ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex].firstHalfPage.keyWords.push({ entext: "", cntext: "" });
    showStep3();
    return false;
}

function step3_keyMapBackPlusBtn(){
    ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex].afterHalfPage.keyWords.push({ entext: "", cntext: "" });
    showStep3();
    return false;
}

function step3_keyMapPlusBtn(){
    ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex].keyWords.push({ entext: "", cntext: "" });
    showStep3();
    return false;
}

function step3_keyMap_box_blur(){
    if(!$17.isBlank(ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex].firstHalfPage)){
        ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex].firstHalfPage.keyWords = [];
        $.each($("p.keyMapsFrount").find("span.keyMap"), function(index, value){
            var $self = $(value);

            ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex].firstHalfPage.keyWords.push({
                entext: $self.find("input.keyMap_enbox").val(),
                cntext: $self.find("input.keyMap_cnbox").val()
            });
        });
    }

    if(!$17.isBlank(ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex].afterHalfPage)){
        ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex].afterHalfPage.keyWords = [];
        $.each($("p.keyMapsBack").find("span.keyMap"), function(index, value){
            var $self = $(value);

            ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex].afterHalfPage.keyWords.push({
                entext: $self.find("input.keyMap_enbox").val(),
                cntext: $self.find("input.keyMap_cnbox").val()
            });
        });
    }

    if(!$17.isBlank(ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex].keyWords)){
        ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex].keyWords = [];
        $.each($("p.keyMapsJust").find("span.keyMap"), function(index, value){
            var $self = $(value);

            ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex].keyWords.push({
                entext: $self.find("input.keyMap_enbox").val(),
                cntext: $self.find("input.keyMap_cnbox").val()
            });
        });
    }

    return false;
}

function step3_keyMapFrontMinus(){
    ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex].firstHalfPage.keyWords.splice($(this).attr("data-keymapindex"), 1);
    showStep3();
    return false;
}

function step3_keyMapBackMinus(){
    ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex].afterHalfPage.keyWords.splice($(this).attr("data-keymapindex"), 1);
    showStep3();
    return false;
}

function step3_keyMapMinus(){
    ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex].keyWords.splice($(this).attr("data-keymapindex"), 1);
    showStep3();
    return false;
}

function setUploadFlash($target, callback){
    $target.getFlash({
        width    : 80,
        height   : 46,
        movie    : "/public/skin/teacher/flash/HttpUpload.swf",
        flashvars: {
            style1  : "//" + location.host + "/public/skin/teacher/images/newquiz/btn_normal.png",
            style2  : "//" + location.host + "/public/skin/teacher/images/newquiz/btn_down.png",
            userid  : $target.attr("data-userid"),
            postUrl : "//" + location.host + $target.attr("data-uploadimageurl"),
            hash    : "",
            callback: callback
        }
    });

    return false;
}

function image_upalod_one_callback(data){
    if(!$17.isBlank(ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex].firstHalfPage)){
        ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex].firstHalfPage.picUri = data;
    }else{
        ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex].picUri = data;
    }

    showStep3();
}

function image_upload_two_callback(data){
    if(!$17.isBlank(ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex].afterHalfPage)){
        ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex].afterHalfPage.picUri = data;
    }else{
        ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex].picUri = data;
    }

    showStep3();
}

function audio_upload_one_callback(data){
    if(!$17.isBlank(ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex].firstHalfPage)){
        ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex].firstHalfPage.readingSentences[$("#sentence_editer_front").attr("data-sentenceid") * 1].audioUri = data;
    }else{
        ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex].readingSentences[$("#sentence_editer").attr("data-sentenceid") * 1].audioUri = data;
    }

    $("#audio_upload_one_input").val(data);
}

function audio_upload_two_callback(data){
    if(!$17.isBlank(ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex].afterHalfPage)){
        ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex].afterHalfPage.readingSentences[$("#sentence_editer_back").attr("data-sentenceid") * 1].audioUri = data;
    }else{
        ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex].readingSentences[$("#sentence_editer").attr("data-sentenceid") * 1].audioUri = data;
    }

    $("#audio_upload_two_input").val(data);
}

function step4_add_danxuan(){
    var danxuan = $.extend(true, {}, ReadingApp.Constructor.readingDraftQuestionTemp);
    danxuan.type = 2;
    danxuan.rank = ReadingApp.DataBase.readingQuestions.length + 1;
    danxuan.answerOptions = ["", ""];
    danxuan.rightAnswer = [0];
    ReadingApp.DataBase.readingQuestions.push(danxuan);

    showStep4();

    //    $("div[data-rank='" + ReadingApp.DataBase.readingQuestions.length + "']").backToCenter(100);

    return false;
}

function step4_add_panduan(){
    var panduan = $.extend(true, {}, ReadingApp.Constructor.readingDraftQuestionTemp);
    panduan.type = 3;
    panduan.rank = ReadingApp.DataBase.readingQuestions.length + 1;
    panduan.answerOptions = ["TRUE", "FALSE"];
    panduan.rightAnswer = [0];
    ReadingApp.DataBase.readingQuestions.push(panduan);

    showStep4();

    //    $("div[data-rank='" + ReadingApp.DataBase.readingQuestions.length + "']").backToCenter(100);

    return false;
}

function step4_add_tiankong(){
    var tiankong = $.extend(true, {}, ReadingApp.Constructor.readingDraftQuestionTemp);
    tiankong.type = 1;
    tiankong.rank = ReadingApp.DataBase.readingQuestions.length + 1;
    ReadingApp.DataBase.readingQuestions.push(tiankong);

    showStep4();

    //    $("div[data-rank='" + ReadingApp.DataBase.readingQuestions.length + "']").backToCenter(100);

    return false;
}

function step4_delete_question(){
    var rank = $(this).attr("data-rank");
    ReadingApp.DataBase.readingQuestions.splice(rank - 1, 1);
    for(var i = 0, l = ReadingApp.DataBase.readingQuestions.length; i < l; i++){
        ReadingApp.DataBase.readingQuestions[i].rank = i + 1;
    }

    showStep4();

    $("div[data-rank='" + (rank != 1 ? rank - 1 : rank) + "']").backToCenter(100);

    return false;
}

function step4_selection(){
    var $self = $(this);
    $self.closest("dl.selection").find("span.radios").removeClass("radios_active").end().end().addClass("radios_active");

    var rank = $self.closest("div[data-rank]").attr("data-rank");
    ReadingApp.DataBase.readingQuestions[rank - 1].rightAnswer = [$self.attr("data-value")];
}

function step4_setContent(){
    var $self = $(this);
    var index = $self.attr("data-index");
    var content = $self.val();

    ReadingApp.DataBase.readingQuestions[index].content = content;

    if($self[0].tagName == "TEXTAREA"){
        ReadingApp.DataBase.readingQuestions[index].rightAnswer = [];
        while(content.indexOf("[") != -1){
            content = content.substring(content.indexOf("[") + 1, content.length);

            ReadingApp.DataBase.readingQuestions[index].rightAnswer.push(content.substring(0, content.indexOf("]")));
        }
    }
}

function step4_setOption(){
    var $self = $(this);
    ReadingApp.DataBase.readingQuestions[$self.attr("data-index")].answerOptions[$self.attr("data-value")] = $self.val();
}

function step4_plusOption(){
    var $self = $(this);
    var questionIndex = $self.attr("data-index");
    var optionIndex = $self.attr("data-value");
    var rightIndex = ReadingApp.DataBase.readingQuestions[questionIndex].rightAnswer[0];
    var rightAnswer = ReadingApp.DataBase.readingQuestions[questionIndex].answerOptions[rightIndex];

    var newArray = [];
    for(var i = 0, l = ReadingApp.DataBase.readingQuestions[questionIndex].answerOptions.length; i < l; i++){
        newArray.push(ReadingApp.DataBase.readingQuestions[questionIndex].answerOptions[i]);
        if(i == optionIndex){
            newArray.push("");
        }
    }
    ReadingApp.DataBase.readingQuestions[questionIndex].answerOptions = newArray;
    ReadingApp.DataBase.readingQuestions[questionIndex].rightAnswer = [$.inArray(rightAnswer, newArray)];

    showStep4();

    return false;
}

function step4_minusOption(){
    var $self = $(this);
    var questionIndex = $self.attr("data-index");
    var optionIndex = $self.attr("data-value");
    var rightIndex = ReadingApp.DataBase.readingQuestions[questionIndex].rightAnswer[0];
    var rightAnswer = ReadingApp.DataBase.readingQuestions[questionIndex].answerOptions[rightIndex];

    ReadingApp.DataBase.readingQuestions[questionIndex].answerOptions.splice(optionIndex, 1);

    var newRightIndex = $.inArray(rightAnswer, ReadingApp.DataBase.readingQuestions[questionIndex].answerOptions);
    ReadingApp.DataBase.readingQuestions[questionIndex].rightAnswer = [newRightIndex == -1 ? null : newRightIndex];

    showStep4();

    return false;
}

function savePageParts(){
    if($17.isBlank(ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex].firstHalfPage)){
        if(!$("#parts_en_box").is(":hidden")){
            step3_parts_done();
        }
    }else{
        if(!$("#front_en_box").is(":hidden")){
            step3_parts_front_done();
        }

        if(!$("#back_en_box").is(":hidden")){
            step3_parts_back_done();
        }
    }
}

function showStep1(godMessage){
    //表单验证
    if((typeof godMessage != "string") && !appChecker(1)){
        return false;
    }

    $("div.quiz_footPopUpMenu").html(template("t:popMenu", ReadingApp.step1.tempInfo));

    if($("#steps1").find("div.stepOne").length != 0){
        $("#steps1").show();
        ReadingApp.$el.empty();

        $17.delegate({
            "#step1_next -> click" : showStep2,
            "#savaReading -> click": readingSave
        });

        return false;
    }

    //检查初始化数据
    if($17.isBlank(ReadingApp.DataBase.readingPages)){
        var firstPage = $.extend(true, {}, ReadingApp.Constructor.readingDraftHalfPageTemp);
        firstPage.pageNum = 0;
        firstPage.pageLayout = "ptpt";
        ReadingApp.DataBase.readingPages = [firstPage];
        ReadingApp.DataBase.pagesIndex = 0;
    }

    //绘制界面
    $("#steps1").html(template(ReadingApp.step1.tempInfo.tempId, {})).show();
    $("#steps1").append(template("t:左导航", {
        step : 1,
        focus: ReadingApp.DataBase.pagesIndex,
        pages: ReadingApp.DataBase.readingPages
    }));

    //重设事件
    $.vox.select("#step1_styles", -1);
    $.vox.select("#step1_levels", -1);
    $17.delegate(ReadingApp.step1.evenConfig);
    $17.delegate(ReadingApp.leftMenu.evenConfig);
    $17.modules.tree({
        data   : ReadingApp.Point,
        target : "#topicTree",
        allOpen: true,
        text   : "title",
        values : ["key"],
        setText: function(text){
            text = text || "";
            if(text.indexOf("话题-") != -1){
                return text.split("话题-")[0] + text.split("话题-")[1];
            }else{
                return text;
            }
        }
    });
    //生成树下拉
    var selectTree = $17.modules.selectTree("#selectTree", "#topicTree");

    //监听树设置事件
    $("#selectTree").on("$17.modules.tree.setValueDone", function(){
        var keys = $("#topicTree").attr("data-keys").split(",");
        if(keys.length == 1){
            if(keys[0].length == 0){
                selectTree.set("", "请选择");
            }else{
                selectTree.set("", "已选择了 1 个话题");
            }
        }else{
            selectTree.set("", "已选择了 " + keys.length + " 个话题");
        }

        //更新数据
        ReadingApp.DataBase.points = (keys.length == 1 && keys[0] == "") ? [] : keys;
    });

    //恢复数据
    $("#step1_title").val(ReadingApp.DataBase.ename);
    var $title = $("#step1_pointsSelect").find("b.title");
    if($17.isBlank(ReadingApp.DataBase.points)){
        $title.html("还未选择");
    }else{
        $title.html("已选择了 " + ReadingApp.DataBase.points.length + " 个话题");
    }
    if(!$17.isBlank(ReadingApp.DataBase.style)){
        $.vox.selectFocus("#step1_styles", ReadingApp.DataBase.style);
    }
    if(!$17.isBlank(ReadingApp.DataBase.difficultyLevel)){
        $.vox.selectFocus("#step1_levels", ReadingApp.DataBase.difficultyLevel);
    }

    checkPopMenuPosition();

    return true;
}

function showStep2(godMessage){
    //表单验证
    if((typeof godMessage != "string") && !appChecker(2)){
        return false;
    }

    //检查初始化数据
    ReadingApp.DataBase.colorId = ReadingApp.DataBase.colorId || 1;
    ReadingApp.DataBase.coverIndex = ReadingApp.DataBase.coverIndex || 1;
    if($17.isBlank(ReadingApp.DataBase.readingPages)){
        var firstPage = $.extend(true, {}, ReadingApp.Constructor.readingDraftHalfPageTemp);
        firstPage.pageNum = 1;
        firstPage.pageLayout = "ptpt";
        ReadingApp.DataBase.readingPages = [firstPage];
        ReadingApp.DataBase.pagesIndex = 0;
    }

    //绘制界面
    $("#steps1").hide();
    ReadingApp.$el.html(template(ReadingApp.step2.tempInfo.tempId, {
        colorId   : ReadingApp.DataBase.colorId,
        coverIndex: ReadingApp.DataBase.coverIndex,
        cover     : ReadingApp.DataBase.coverUri || "",
        unfinished: ReadingApp.DataBase.coverUri2 || "",
        least     : ReadingApp.DataBase.coverUri1 || ""
    }));

    ReadingApp.$el.append(template("t:左导航", {
        step : 2,
        focus: ReadingApp.DataBase.pagesIndex,
        pages: ReadingApp.DataBase.readingPages
    }));

    $("div.quiz_footPopUpMenu").html(template("t:popMenu", ReadingApp.step2.tempInfo));

    //重设事件
    $17.delegate(ReadingApp.step2.evenConfig);
    $17.delegate(ReadingApp.leftMenu.evenConfig);

    checkPopMenuPosition();

    var $flashTarget = $("#upload_flash");

    var flashvars = {
        userid        : $flashTarget.attr("data-userid"),
        uploadImageUrl: "http://" + location.host + $flashTarget.attr("data-uploadimageurl"),
        hash          : "",
        resourceroot  : "/resources/apps/flash",
        callback      : "ugc_callback",
        cancel        : "ugc_cancel",
        subject       : ReadingApp.DataBase.ename,
        frameUrl      : "http://" + location.host + "/public/skin/teacher/images/newquiz/" + $("#step2_colors").find("s.active").attr("data-colorid") + "_skin_" + (ReadingApp.DataBase.coverIndex == 1 ? 1 : 0) + ".png",
        selecteArea   : (ReadingApp.DataBase.coverIndex == 1 ? 1 : 0),
        buttonUrl     : "http://" + location.host + "/public/skin/teacher/images/newquiz/" + $("#step2_colors").find("s.active").attr("data-colorid") + "_start.png"
    };

    $flashTarget.getFlash({
        movie    : "/public/skin/teacher/flash/CreateCover.swf" + '?' + (new Date().getTime()),
        flashvars: flashvars
    });

    return true;
}

function showStep3(godMessage){
    //表单验证
    if((typeof godMessage != "string") && !appChecker(3)){
        return false;
    }

    //检查初始化数据
    if($17.isBlank(ReadingApp.DataBase.readingPages)){
        var page = $.extend(true, {}, ReadingApp.Constructor.readingDraftHalfPageTemp);
        page.pageNum = 1;
        page.pageLayout = "ptpt";
        page.firstHalfPage.keyWords = [{ entext: "", cntext: "" }];
        page.afterHalfPage.keyWords = [{ entext: "", cntext: "" }];
        ReadingApp.DataBase.readingPages = [page];
    }
    ReadingApp.DataBase.pagesIndex = ReadingApp.DataBase.pagesIndex || 0;

    //绘制界面
    var page = ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex];
    var frontParts = [];
    var frontKeywords = [];
    var backParts = [];
    var backKeywords = [];
    var parts = [];
    var keywords = [];
    var image_one = "";
    var image_two = "";
    if(page.pageLayout == "ptpt" || page.pageLayout == "tt"){
        frontParts = page.firstHalfPage.readingSentences || (page.firstHalfPage.readingSentences = []);
        frontKeywords = page.firstHalfPage.keyWords || (page.firstHalfPage.keyWords = [{ entext: "", cntext: "" }]);
        image_one = page.firstHalfPage.picUri;
        backParts = page.afterHalfPage.readingSentences || (page.afterHalfPage.readingSentences = []);
        backKeywords = page.afterHalfPage.keyWords || (page.afterHalfPage.keyWords = [{ entext: "", cntext: "" }]);
        image_two = page.afterHalfPage.picUri;
    }else{
        parts = page.readingSentences || (page.readingSentences = []);
        keywords = page.keyWords || (page.keyWords = [{ entext: "", cntext: "" }]);
        image_one = page.picUri;
    }
    $("#steps1").hide();
    ReadingApp.$el.html(template(ReadingApp.step3.tempInfo.tempId, {
        isLast        : ReadingApp.DataBase.pagesIndex + 1 == ReadingApp.DataBase.readingPages.length,
        pageNum       : ReadingApp.DataBase.pagesIndex + 1,
        pageLayout    : page.pageLayout,
        sentence_front: frontParts,
        frontKeywords : frontKeywords,
        sentence_back : backParts,
        backKeywords  : backKeywords,
        sentence_parts: parts,
        keywords      : keywords,
        image_one     : image_one,
        image_two     : image_two
    }));
    ReadingApp.$el.append(template("t:左导航", {
        step : 3,
        focus: ReadingApp.DataBase.pagesIndex,
        pages: ReadingApp.DataBase.readingPages
    }));

    $("div.quiz_footPopUpMenu").html(template("t:popMenu", ReadingApp.step3.tempInfo));

    //重设事件
    $17.delegate(ReadingApp.step3.evenConfig);
    $17.delegate(ReadingApp.leftMenu.evenConfig);

    checkPopMenuPosition();

    var $image_upalod_one = $("#image_upload_one");
    var $image_upload_two = $("#image_upload_two");
    var $audio_upload_one = $("#audio_upload_one");
    var $audio_upload_two = $("#audio_upload_two");

    if(!$17.isBlank($image_upalod_one)){
        setUploadFlash($image_upalod_one, "image_upalod_one_callback");
    }

    if(!$17.isBlank($image_upload_two)){
        setUploadFlash($image_upload_two, "image_upload_two_callback");
    }

    if(!$17.isBlank($audio_upload_one)){
        setUploadFlash($audio_upload_one, "audio_upload_one_callback");
    }

    if(!$17.isBlank($audio_upload_two)){
        setUploadFlash($audio_upload_two, "audio_upload_two_callback");
    }

    return true;
}

function showStep4(godMessage){
    //表单验证
    if((typeof godMessage != "string") && !appChecker(4)){
        return false;
    }

    //如果用户未点完成，替用户保存数据
    savePageParts();

    //绘制界面
    $("#steps1").hide();
    ReadingApp.$el.html(template(ReadingApp.step4.tempInfo.tempId, {
        questions: ReadingApp.DataBase.readingQuestions
    }));
    ReadingApp.$el.append(template("t:左导航", {
        step : 4,
        focus: ReadingApp.DataBase.pagesIndex,
        pages: ReadingApp.DataBase.readingPages
    }));

    $("div.quiz_footPopUpMenu").html(template("t:popMenu", ReadingApp.step4.tempInfo));

    //重设事件
    $17.delegate(ReadingApp.step4.evenConfig);
    $17.delegate(ReadingApp.leftMenu.evenConfig);

    checkPopMenuPosition();

    return true;
}

function readingSave(){
    savePageParts();
    postDate("draft");
}

function readingSubmit(){
    //表单验证
    if(!appChecker("submit")){
        return false;
    }
    postDate("publish");
}

function postDate(status){
    if($(ReadingApp).isFreezing("submit")){
        $17.alert("正在努力提交哦。。。");
        return false;
    }

    $(ReadingApp).freezing("submit");

    ReadingApp.DataBase.status = status;
    ReadingApp.DataBase.pagesIndex = 0;

    App.postJSON("/teacher/resource/reading/index.vpage", ReadingApp.DataBase, function(data){
        if(data.success){
            setTimeout(function(){
                location.href = "/teacher/resource/reading.vpage";
            }, 200);
        }

        $(ReadingApp).thaw("submit");
    });
}

function showView2(){
    showView(2);
}
function showView3(){
    showView(3);
}
function showView4(){
    showView(4);
}
function showViewAll(){
    showView(0);
}

function showView(step){
    var flashvars = null;

    switch(step){
        case 2:
            flashvars = {
                isPreview  : 1,                                    //是否是预览
                previewJson: JSON.stringify({
                    previewType: 0,                                //预览封面 0
                    title      : ReadingApp.DataBase.ename,
                    coverUrl   : ReadingApp.DataBase.coverUri,
                    imgDomain  : ReadingApp.imgDomain,
                    colorId    : ReadingApp.DataBase.colorId
                }),
                imgDomain  : ReadingApp.imgDomain,
                isTeacher  : 1,
                tts_url    : $("#UGC-DATA").attr("data-tts-url")
            };
            break;
        case 3:
            var data = $.extend({}, ReadingApp.DataBase.readingPages[ReadingApp.DataBase.pagesIndex]);
            data.previewType = 1;

            flashvars = {
                isPreview  : 1,
                previewJson: JSON.stringify(data),
                imgDomain  : ReadingApp.imgDomain,
                isTeacher  : 1,
                tts_url    : $("#UGC-DATA").attr("data-tts-url")
            };
            break;
        case 4:
            var data = {
                readingQuestions: [].concat(ReadingApp.DataBase.readingQuestions),
                previewType     : 2
            };

            flashvars = {
                isPreview   : 1,
                previewJson : JSON.stringify(data),
                nextHomeWork: "closeReviewWindow",
                imgDomain   : ReadingApp.imgDomain,
                isTeacher   : 1,
                tts_url     : $("#UGC-DATA").attr("data-tts-url")
            };
            break;
        case 0:
            var data = $.extend({}, ReadingApp.DataBase);
            data.previewType = 3;

            flashvars = {
                isPreview   : 1,
                previewJson : JSON.stringify(data),
                imgDomain   : ReadingApp.imgDomain,
                nextHomeWork: "closeReviewWindow",
                isTeacher   : 1,
                tts_url     : $("#UGC-DATA").attr("data-tts-url")
            };
            break;
    }

    $.prompt(template("t:预览", {}), {
        title   : "阅读预览",
        position: { width: 750 },
        buttons : {},
        loaded  : function(){
            $("#showViewContent").getFlash({
                movie    : ReadingApp.FlashURL,
                flashvars: flashvars,
                imgDomain: ReadingApp.imgDomain
            });
        }
    });

    return false;
}

//题目预览结束回调
function closeReviewWindow(){
    $.prompt.close();
    return false;
}
