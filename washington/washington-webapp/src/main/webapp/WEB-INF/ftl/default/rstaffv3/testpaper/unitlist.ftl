<#if book?? && book?has_content>
<div class="text_center" style=" background-color: #eaf2ff; border-bottom: 1px solid #ccdbea; padding: 15px; margin-bottom: 15px;">
    <span>如需创建期中、期末等综合试卷，可以同时选中多个单元开始组卷</span>
    <a href="javascript:void(0);" id="viewOrHiddenBtn" class="btn_vox btn_vox_warning" style="width: 90px;"><span class="viewPaperInfo" data-bookid=${book.id!'0'}>查看已组试卷</span> <span style="display: none;">收起</span></a>
</div>
<div class="selfPaperInfo" style="display: none;">

</div>
<div class="clear"></div>

    <#if units??>
    <dl class="horizontal_vox paperListTab">
        <dt style="text-align: center;">
                <span class="w-build-image w-build-image-${book.color!'Orange'}">
                    <strong class="wb-title">${(book.viewContent)!""}</strong>
                    <#if book.latestVersion?? && book.latestVersion>
                        <span class="wb-new"></span>
                    </#if>
                </span>
        <p style="padding: 10px 0">${book.cname!''}</p>
        <p>
            <a class="btn_vox btn_vox_small" href="/rstaff/testpaper/index.vpage"><strong class="text_blue">更换教材</strong></a>
        </p>
        </dt>
        <dd>
            <div id="unit_list_box" class="tabListBox tabListBox_large">
                <ul>
                    <#list units as u>
                        <li data-unitid="${u.id}" title="${u.cname!''}">
                            <a href="javascript:void(0);" style="height: 40px; text-align: left;">
                                <span class="checkboxs"></span>
                                <span style="display: inline-block; vertical-align: middle; padding-left: 8px; width: 180px; overflow: hidden; white-space: nowrap; text-overflow: ellipsis;">${u.cname!''}</span>
                                <span style="display: inline-block; vertical-align: middle; padding: 0; height: 100%;"></span>
                               <#-- <#if examPaperUnit?seq_contains(u.id)>
                                    <i class='icon_rstaff icon_rstaff_21'></i>
                                </#if>-->
                            </a>
                        </li>
                    </#list>
                </ul>
                <div class="clear"></div>
            </div>
        </dd>
    </dl>
    <div class="clear"></div>
    <div class="text_center">
        <a id="start_to_create_exam_but" href="javascript:void(0);" class="btn_vox btn_vox_primary" style="margin: 10px">开始组卷</a>
    </div>
    </#if>
<script>
    $(function(){

        var bookAndUnit = new $17.Model({
            unitListBoxLi       : $("#unit_list_box li"),
            activeUnitListBoxLi : $("#unit_list_box li.active"),
            startCreatePaperBtn : $("#start_to_create_exam_but"),
            subject             : "${(currentUser.subject)!"UNKNOWN"}",
            bookId              : "${(book.id)!''}"
        });
        bookAndUnit.extend({
            getCreatePaperUrl : function(){
                var $this = this;

                if($this.subject == "ENGLISH"){
                    return "/exam/paper/index.vpage";
                }else if($this.subject == "MATH"){
                    return "/exam/paper/math/index.vpage";
                }
                return "";
            },
            init : function(){
                var $this = this;
                /** 单元或者模块选择 */
                $this.unitListBoxLi.on("click",function(){
                    var _this = $(this);

                    if($("span[class*='checkboxs']",_this).hasClass("checkboxs_active")){
                        $("span[class*='checkboxs']",_this).removeClass("checkboxs_active");
                        _this.removeClass("active");
                    }else{
                        $("span[class*='checkboxs']",_this).addClass("checkboxs_active");
                        _this.addClass("active");
                    }
                });

                /** 开始组卷 */
                $this.startCreatePaperBtn.on("click",function(){
                    var selUnits = [];
                    $("#unit_list_box li.active").each(function(i, value){
                        selUnits.push($(value).data("unitid"));
                    });
                    if(selUnits.length <= 0){
                        $17.alert("请选择要组卷的单元");
                    }else{
                        $17.tongji("教研员-新建试卷按钮");
                        if(!$17.isBlank($this.getCreatePaperUrl())){
                            setTimeout(function(){
                                location.href = $this.getCreatePaperUrl() + "?bookId=" + $this.bookId  + "&units=" + selUnits.toString();
                            }, 200);
                        }
                    }
                });
            }
        }).init();

        //查看已组试卷或收起
        $("#viewOrHiddenBtn").on("click",function(){
            var _this = $(this);
            var _spanVisble = _this.find('span:visible');
            var bookId = _spanVisble.data("bookid");
            if(_spanVisble.hasClass("viewPaperInfo")){
                var _liArray = $(".selfPaperInfo li");
                if(!$17.isBlank(_liArray) && _liArray.length > 0){
                    bookPaperListSlideToggle(_this);
                }else{
                    var _url = "/rstaff/book/getResearchStaffPaperListByBookId.vpage?bookId=" + bookId;
                    $(".selfPaperInfo").load(_url,function(data){
                        $(this).html(data);
                        bookPaperListSlideToggle(_this);
                    });
                }
            }else{
                bookPaperListSlideToggle(_this);
            }
        });
    });

    //已组试卷的展开或收起
    function bookPaperListSlideToggle(_this){
        _this.find('span:visible').hide().siblings().show();
        $(".selfPaperInfo").slideToggle(function(){});
    }

</script>
</#if>