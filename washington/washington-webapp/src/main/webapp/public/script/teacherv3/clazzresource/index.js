$(function(){
    var logModule = constantObj.logModule || "m_aHrND8yNXX";
    var book = null;
    var objectiveTabs = null;
    var tabContentMap = {};  //tab下的类对象
    var objectiveConfigType  = {
        LEVEL_READINGS          : "t:LEVEL_READINGS",
        BASIC_APP               : "t:BASIC_APP"
    };

    var levelAndclazzs = $17.homeworkv3.getLevels();
    levelAndclazzs.extendLevelClick = extendLevelClick;
    levelAndclazzs.initialise({
        batchclazzs : constantObj.batchclazzs,
        hasStudents : constantObj.hasStudents,
        clazzClickCb  : function(){}
    });
    ko.applyBindings(levelAndclazzs, document.getElementById('level_and_clazzs'));

    //年级click扩展
    function extendLevelClick(obj){
        if(book == null){
            book = $17.homeworkv3.getBook();
            book.extendSectionClick = extendSectionClick;
            ko.applyBindings(book, document.getElementById('bookInfo'));
        }
        book.initialise($.extend(obj,{
            term : constantObj.term
        }));
    }

    //课时click扩展
    function extendSectionClick(obj){
        $("#tabContent").empty();
        if(objectiveTabs == null){
            objectiveTabs = $17.clazzresource.getObjectiveTabs();
            objectiveTabs.extendTabClick = extendTabClick;
            ko.applyBindings(objectiveTabs, document.getElementById('objectiveTabs'));
        }
        objectiveTabs.initialise($.extend(true,{},obj));
        objectiveTabs.run();
    }

    //作业形式click扩展
    function extendTabClick(obj){
        var _tabType = obj.tabType || obj.objectiveTabType;
        $17.voxLog({
            module: logModule,
            op : "resource_type_click",
            s0 : _tabType
        });
        var $tabContent = $("#tabContent");
        $tabContent.empty();
        var getTabType = "get" + _tabType.slice(0,1) + _tabType.slice(1).toLocaleLowerCase();
        var fn = $17.clazzresource[getTabType];
        var elementId;
        var tabContent;
        if(typeof fn === 'function') {
            $("<div></div>").attr("id",_tabType).attr("data-bind","template:{'name':'" + objectiveConfigType[_tabType]  + "'}").appendTo($tabContent);
            if(tabContentMap[_tabType]){
                tabContent = tabContentMap[_tabType];
            }else{
                tabContent = fn.apply(null, []);
                tabContentMap[_tabType] = tabContent;
            }
            elementId = _tabType;
        }else{
            tabContent = $17.homeworkv3.getDefault();
            $("<div></div>").attr("id","default").attr("data-bind","template:{'name':'t:default'}").appendTo($tabContent);
            elementId = "default";
        }
        tabContent.initialise($.extend(true,{
            examInitComplete        : constantObj.examInitComplete,
            categoryIconPrefixUrl   : constantObj.categoryIconPrefixUrl,
            subject                 : constantObj.subject,
            env                     : constantObj.env
        },obj));
        tabContent.run({
            clazzGroupIdsStr : levelAndclazzs.checkedClazzGroupIds().join(",")
        });
        var node = document.getElementById(elementId);
        ko.cleanNode(node);
        ko.applyBindings(tabContent, node);
        $("#J_HomeworkWay").show();
    }

    $(window).on("scroll",function(){
        /*var $objectiveTabs = $("#objectiveTabs");
        if($(window).scrollTop() > $("#hkTabcontent").offset().top){
            $objectiveTabs.addClass("h-fixHeader");
        }else{
            $objectiveTabs.removeClass("h-fixHeader");
        }*/
    });

});



