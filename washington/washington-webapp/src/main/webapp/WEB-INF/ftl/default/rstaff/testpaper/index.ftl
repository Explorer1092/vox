<#import "../module.ftl" as com>
<@com.page t=1 s=4>

<ul class="breadcrumb_vox">
    <li><a href="javascript:void(0);">组 卷</a> <span class="divider">/</span></li>
    <li class="active">组卷</li>
</ul>

<div>
    <div class="paperStep">
        <p class="s_1"></p>
    </div>
    <div class="testpaperBox" style="padding: 0;">
        <#--用户课本-->
        <div class="v-book-list w-border-list t-teachingMaterial" style="padding: 15px 0 0;" id="myBookList" style="display: none;">
            <ul>
                <#if rstaffBookList?has_content>
                    <#list rstaffBookList as book>
                            <li class="v-book" data-bookid="${book.id}" <#if book_index gt 5> style="display: none;" </#if>>
                                <dl class="w-imageText-list">
                                    <dt>
                                        <span class="w-build-image w-build-image-${book.color!'Orange'}">
                                            <strong class="wb-title">${(book.viewContent)!""}</strong>
                                            <#if book.latestVersion?? && book.latestVersion>
                                                <span class="wb-new"></span>
                                            </#if>
                                        </span>
                                    </dt>
                                    <dd>
                                        <div class="build-name">${(book.cname)!""}</div>
                                    </dd>
                                </dl>
                            </li>
                    </#list>
                </#if>
                <li id="moreBooks" <#if rstaffBookList?has_content && rstaffBookList?size gt 5> style="display: none;" </#if>>
                    <dl class="w-imageText-list">
                        <dt>
                            <span class="w-build-image w-build-image-Orange">
                                <strong class="wb-title"></strong>
                            </span>
                        </dt>
                        <dd>
                            <div class="build-name">更多教材</div>
                        </dd>
                    </dl>
                </li>
            </ul>
            <#if rstaffBookList?has_content && (rstaffBookList?size > 5)>
                <div id="downUp" class="selectBookMore" style="clear: both; margin: 0 7px; height: 20px;">
                    <div style="float: right;">
                        <a href="javascript:void(0);" data-type="All">显示更多教材 <i class='icon_general icon_general_32'></i></a>
                        <a href="javascript:void(0);" data-type="Little" style="display: none;">收起 <i class='icon_general icon_general_25'></i></a>
                    </div>
                </div>
            </#if>
        </div>
        <#--用户课本封皮-END-->

        <#--课本年级分类-->
        <div id="selectGradeBook" style="display: none;">
            <ul class="container_tab">
                <li data-tablevel="1" class="" style="display: none;"><a href="javascript:void(0);"><strong>一年级</strong></a></li>
                <li data-tablevel="2" class="" style="display: none;"><a href="javascript:void(0);"><strong>二年级</strong></a></li>
                <li data-tablevel="3" class="active"><a href="javascript:void(0);"><strong>三年级</strong></a></li>
                <li data-tablevel="4" class=""><a href="javascript:void(0);"><strong>四年级</strong></a></li>
                <li data-tablevel="5" class=""><a href="javascript:void(0);"><strong>五年级</strong></a></li>
                <li data-tablevel="6" class=""><a href="javascript:void(0);"><strong>六年级</strong></a></li>
                <li style="float: right; padding: 5px;"><input style="width: 120px; margin: 0 0 0 5px; padding: 6px 10px !important;" class="int_vox" value="" placeholder="输入关键字搜索教材" id="gradeBooks_search"></li>
            </ul>

            <div class="homeWorkUfo" style="display: none">
                <div class="ufoInline">
                    <p class="row_vox_left lineHeight">
                <span class="text_gray">已经选择的教材：
                    <strong id="unitCount" class="text_blue text_big">0</strong> 本教材
                    <a href="javascript:void(0);" class="text_blue" id="viewCollapse">查看<i class="icon_general icon_general_32"></i></a>
                </span>
                    </p>
                    <p class="row_vox_right">
                        <a id="cancelBook" href="javascript:void(0);" class="btn_vox btn_vox_small"><strong>取消</strong></a>
                        <a href="javascript:void(0);" class="btn_vox btn_vox_small submitBtn"><strong>保存</strong></a>
                    </p>
                </div>
            </div>
            <#--选中的课本-->
            <div id="setClazzBook" class="v-book-list w-border-list t-teachingMaterial clazzBookjs" style="display: none; background-color: #fffded; border-bottom: 1px solid #fedfa7; margin: 0; padding: 10px 5px;">
                <ul id="selectClazzBook"></ul>
            </div>

            <div class="v-book-list w-border-list t-teachingMaterial clazzBookjs" id="addClazzBook">
                <div style="padding:20px 30px;">设置教材：</div>
                <ul id="gradeBooks_1"></ul>
                <ul id="gradeBooks_2"></ul>
                <ul id="gradeBooks_3"></ul>
                <ul id="gradeBooks_4"></ul>
                <ul id="gradeBooks_5"></ul>
                <ul id="gradeBooks_6"></ul>
            </div>
        </div>
        <#-- 单元显示 -->
        <div id="bookUnitBox" style="display: none;"></div>

        <div class="clear"></div>
    </div>
</div>

<script id="t:书本模板" type="text/html">
    <%for(var i = 0; i < rows.length; i++){%>
    <li class="v-book" data-bookid="<%=rows[i].id%>">
        <div class="icon_vox icon_vox_blue icon_vox_344 t-select-icon"></div>
        <dl class="w-imageText-list">
            <dt>
                <span class="w-build-image w-build-image-<%=rows[i].color%>">
                    <strong class="wb-title"><%=rows[i].viewContent%></strong>
                    <%if (rows[i].latestVersion != 'undefined' && rows[i].latestVersion){%>
                        <span class="wb-new"></span>
                    <%}%>
                </span>
            </dt>
            <dd>
                <div class="build-name"><%=rows[i].cname%></div>
            </dd>
        </dl>
    </li>
    <%}%>
</script>
<@sugar.capsule js=["fastLiveFilter"] />
<script type="text/javascript">
    //页面流程对象
    var stepFlow = new $17.Model({
             myBookList : $("#myBookList"),
        selectGradeBook : $("#selectGradeBook"),
            bookUnitBox : $("#bookUnitBox"),
              paperStep : $("div.paperStep p")
    });
    stepFlow.extend({
        toStep1: function(){
            var $this = this;

            $this.myBookList.show();
            $this.selectGradeBook.hide();
            $this.bookUnitBox.hide();
            $this.paperStep.removeClass().addClass("s_1");
        },
        toStep2: function(){
            var $this = this;
            $this.myBookList.hide();
            $this.selectGradeBook.hide();
            $this.bookUnitBox.show();
            $this.paperStep.removeClass().addClass("s_2");

        },
        toStep3: function(){
            var $this = this;

            $this.myBookList.hide();
            $this.selectGradeBook.show();
            $this.bookUnitBox.hide();
            $this.paperStep.removeClass().addClass("s_1");
        }
    });

    $(function(){

        var myBookList = new $17.Model({
                bookTarget : $("#myBookList").find("li.v-book"),
            moreBookTarget : $("#moreBooks")
        });
        myBookList.extend({
            init : function(){
                var $this = this;

                stepFlow.toStep1();
                <#--给教材列表的教材图片挂事件-->
                $this.bookTarget.on("click",function(){
                    $this.loadBookAndUnit($(this).data("bookid"));
                });
                <#--更多教材-->
                $this.moreBookTarget.on("click",function(){
                    $17.tongji("英语教研员--查看更多教材");
                    stepFlow.toStep3();
                    gradeBookSelect.init();
                });
            },
            loadBookAndUnit:function(bookId){
                $("#bookUnitBox").load("/rstaff/book/unit.vpage?bookId=" + bookId,function(){
                    stepFlow.toStep2();
                });
            }
        }).init();

        <#--各年级课本-->
        var gradeBookSelect = new $17.Model({
            clazzLevel    : 3,
            liTabLevel    : $("li[data-tablevel]"),
            searchInput   : $('#gradeBooks_search'),
            selectBookUrl : "/rstaff/book/sortbook",
            liBook       : $(".clazzBookjs li.v-book"),
            bookIds       : [],
            submitBtn       : $(".submitBtn"),
            submitUrl       : "/rstaff/book/saveReseachStaffBook.vpage"
        });
        gradeBookSelect.extend({
            init : function(){
                var $this = this;
                $this.loadGradeBooks($this.clazzLevel);
                // 年级TAB项
                $this.liTabLevel.on("click", function(){
                    var $that   = $(this);
                    var _level  = $that.data("tablevel");
                    $this.loadGradeBooks(_level);
                    $that.radioClass("active");
                    $("ul[id^='gradeBooks_']").hide();
                    $("#gradeBooks_" + _level).show();
                });

                //点击课本
                $this.liBook.die().live({
                    click: function(){
                        var $self = $(this);
                        var $iconDiv = $(this).find("div.icon_general");
                        var $setClazzBook = $("#setClazzBook");
                        var $selectClazzBook = $("#selectClazzBook");
                        var $ufo = $("div.homeWorkUfo");
                        var delBooks = function(){
                            $("#unitCount").html($setClazzBook.find("[data-bookid]").length - 1);
                            $("#addClazzBook").find("li[data-bookid=" + $self.data("bookid") + "]").find("div.icon_general").attr("class","icon_vox icon_vox_blue icon_vox_344 t-select-icon").hide();
                            $setClazzBook.find("li[data-bookid=" + $self.data("bookid") + "]").remove();
                            $this.bookIds.splice($.inArray($self.data("bookid"), $this.bookIds), 1);
                            if($this.bookIds.length == 0){
                                $("#viewCollapse").html("查看<i class='icon_general icon_general_32'></i>");
                                $ufo.hide();
                                $setClazzBook.hide();
                            }
                        };

                        if($iconDiv.hasClass("icon_general_30") || $iconDiv.hasClass("icon_general_28")){
                            $ufo.show();
                            <#--选择的书本-->
                            if($.inArray($self.data("bookid"), $this.bookIds) == -1){
                                var $book = $self.clone();
                                $book.find("div.icon_general").hide().removeClass("icon_general_30").addClass("icon_general_31");
                                $selectClazzBook.prepend($book);

                                $iconDiv.attr("class","icon_general icon_general_28 t-select-icon").css({'display': 'block'});

                                $("#unitCount").html($setClazzBook.find("[data-bookid]").length);

                                $this.bookIds.push($self.data("bookid"));
                            }else{
                                delBooks();
                            }
                        }else{
                            <#--去除选择的书本-->
                            delBooks();
                        }
                    },
                    mouseenter: function(){
                        $(this).find("div.icon_general").css({'display': 'block'});
                    },
                    mouseleave: function(){
                        var $iconDiv = $(this).find("div.icon_general");
                        if($iconDiv.hasClass("icon_general_28")){
                            $iconDiv.css({'display': 'block'});
                        }else{
                            $iconDiv.hide();
                        }
                    }

                });
                //查看
                $("#viewCollapse").die().live("click", function(){
                    var $this = $(this);

                    if($this.text() == "查看"){
                        $this.html("收起<i class='icon_general icon_general_25'></i>");
                    }else{
                        $this.html("查看<i class='icon_general icon_general_32'></i>");
                    }
                    $("#setClazzBook").toggle();
                });

                //取消
                $("#cancelBook").die().live("click",function(){
                    $.each($this.bookIds,function(index){
                        $("#addClazzBook").find("li.v-book").find("div.icon_general").attr("class","icon_vox icon_vox_blue icon_vox_344 t-select-icon").hide();
                        $("#setClazzBook").find("li.v-book").remove();
                    });
                    $("#unitCount").html('0');
                    $this.bookIds.splice(0,$this.bookIds.length);
                    $(".homeWorkUfo").hide();
                    $("#setClazzBook").hide();
                    stepFlow.toStep1();
                });
                //保存
                $this.submitBtn.on("click", function(){
                    $this.saveBook();
                });
            },
            loadGradeBooks : function(level){
                var $this   = this;
                var $target = $("#gradeBooks_" + level);
                //年级下的课本列表不为空,表示加载过课本，直接显示就可以
                if($target.find("li.v-book").length > 0){
                    $this.searchInput.fastLiveFilter("#gradeBooks_" + level).trigger("change");
                }else{
                    $.get($this.selectBookUrl + ".vpage?level=" + level, function(data){
                        if(data.success){
                            $target.html(template("t:书本模板", {
                                rows: data.rows
                            }));
                        }
                        $this.searchInput.fastLiveFilter("#gradeBooks_" + level).trigger("change");
                    });
                }

            },
            saveBook: function(){
                var $this = this;
                var bookIds = [];
                $("#setClazzBook").find("li.v-book").each(function(){
                    var $that = $(this);
                    bookIds.push($that.data("bookid"));
                });

                if(bookIds.length <= 0){
                    return false;
                }
                if($this.submitBtn.isFreezing()){
                    return false;
                }
                $this.submitBtn.freezing();
                $.post($this.submitUrl, {
                    bookIds : bookIds.toString()
                }, function(data){
                    $this.submitBtn.thaw();
                    if(data.success){
                        $.prompt("添加教材成功", {
                            title: "系统提示",
                            buttons: { "知道了": true },
                            submit: function(){
                              setTimeout(function(){ location.href = "/rstaff/testpaper/index.vpage"; }, 200);
                            }
                        });
                    }else{
                       $17.alert(data.info);
                    }
                });
                return false;
            }
        });


        <#--新建试卷--重复单元-->
        if($17.getQuery("step") == "bookandunit"){
            myBookList.loadBookAndUnit(${bookId});
        }
    });

    $(function(){
        <#--显示更多课本 & 收缩课本按钮事件-->
        var downUp = new $17.Model({
            target      : $("#myBookList").find("li"),
            actionBtn   : $("#downUp").find("a")
        });
        downUp.extend({
            showHideList: function(type){
                var $this = this;
                switch(type){
                    case "All":
                        $this.target.show("fast");
                        break;
                    case "Little":
                        $this.target.filter(function(){
                            return $this.target.index($(this)) > 5;
                        }).hide("fast");
                        break;
                    default:
                        throw new Error("Button type '" + type + "' is undefined.teacher/homework/index.vpage#downUp");
                        break;
                }
            },
            init: function(){
                var $this = this;

                $this.resetDownup();

                $this.actionBtn.on("click", function(){
                    var $that = $(this);
                    $this.showHideList($that.hide().siblings().show().end().data("type"));
                });
            },
            resetDownup:function(){
                var $this = this;
                if($this.target.size() > 5){
                    $("#downUp").show();
                }
                $this.actionBtn.each(function(){
                    var $that = $(this);
                    if($that.data("type") == "All"){
                        $that.show();
                    }else{
                        $that.hide();
                    }
                });
                $this.showHideList("Little");
            }
        }).init();
    });
</script>
</@com.page>