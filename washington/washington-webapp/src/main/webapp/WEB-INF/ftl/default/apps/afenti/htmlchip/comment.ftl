<div class="am-comment-box">
    <div class="title-nav">同学评论</div>
    <div class="ac-info">
        <div class="ac-left" id="sendLevel" style="cursor: pointer;">
            <span class="icon icon-star-small" data-level="1"></span>
            <span class="icon icon-star-small" data-level="2"></span>
            <span class="icon icon-star-small" data-level="3"></span>
            <span class="icon icon-star-small" data-level="4"></span>
            <span class="icon icon-star-small" data-level="5"></span>
        </div>
        <div class="ac-right">
            <span id="infoBox">提示</span>
            <div class="ac-info-orange" style="display: none;">
                <div class="at-box">
                    <p>1、请文明发言，勿发表违法、攻击他人等信息。</p>
                    <p>2、用户评论，等管理员审核后才可显示</p>
                    <div class="arrow"></div>
                </div>
            </div>
        </div>
        <div class="clear"></div>
        <div class="ac-enter">
            <textarea id="sendContent" maxlength="50"></textarea>
            <div style="color: #999;">请输入50个字以内的评论</div>
            <div class="ae-detail">
                <a class="btn-orange" title="评论" href="javascript:void (0)" id="sendComment">评论</a>
                <#--<p class="txt-orange"><span class="icon icon-smile"></span>添加表情</p>-->
            </div>
        </div>
    </div>
    <div class="ac-all-box">
        <h2 class="ac-title">全部评论</h2>
        <#if comment.content?size gt 0>
            <div class="pc-center">
                <#list comment.content as v>
                    <dl>
                        <dt>
                        <#--<img width="60" height="60" src="images/ranking-person.png">-->
                        <p class="txt-orange">${v.userName}同学</p>
                        </dt>
                        <dd>
                            <div class="t-pk-skin">
                                <i class="t-pk-arrow"></i>
                                <div class="mes-info">${v.content}</div>
                            </div>
                            <p class="date-time">
                                <#list [1, 2, 3, 4, 5] as lv>
                                    <#if lv lte v.level>
                                        <span class="icon icon-star-small"></span>
                                    <#else>
                                        <span class="icon icon-star-small-active"></span>
                                    </#if>
                                </#list>
                            </p>
                            <p class="comment_but laud-count">
                            ${v.province}
                            </p>
                            <p class="laud-count">
                                #${v.productName} #${v.userName}
                            </p>
                            <p class="laud-count">
                            ${v.createDatetime?string('yyyy-MM-dd HH:mm:ss')}
                            </p>
                        </dd>
                    </dl>
                </#list>
            </div>
            <div class="ws_page-box" id="pageBox">
                <a class="txt-orange" href="javascript:void (0)" data-opt-type="back">上一页</a>
                <#if currentPage gt 10><span>...</span></#if>
                <#list 1..comment.getTotalPages() as g>
                    <#if g_index lt 10>
                        <a href="javascript:void (0)" data-pagenumber="${g}">${g}</a>
                    <#else>
                        <a href="javascript:void (0)" data-pagenumber="${g}" style="display: none;">${g}</a>
                    </#if>
                </#list>
                <#if currentPage lt comment.getTotalPages() && comment.getTotalPages() gt 10><span>...</span></#if>
                <a class="txt-orange" href="javascript:void (0)" data-opt-type="next">下一页</a>
                <#--<strong >跳转</strong>
                <input type="text">
                <a class="go" href="javascript:void (0)">go</a>-->
            </div>
        <#else>
            <div class="pc-center" style="padding: 40px 0; text-align: center; color: #999;">
                还没有评论
            </div>
        </#if>
    </div>
</div>
<script type="text/javascript">
    $(function(){
        var sendLevel = $("#sendLevel");
        var sendContent = $("#sendContent");
        var currentLevel = 5;
        var getTotalPages = ${comment.getTotalPages()};

        //选择星级
        sendLevel.on("click", "span", function(){
            var $this = $(this);
            currentLevel = $this.data("level");
            sendLevel.find("span").each(function(index){
                if($(this).data("level") > currentLevel){
                    $(this).addClass("icon-star-small-active");
                }else{
                    $(this).removeClass("icon-star-small-active");
                }
            });
        });

        $("#sendComment").on("click", function(){
            if( sendContent.val() == "" ){
                alert("评论不能为空");
                return false;
            }
            $.post("/apps/afenti/writecomment.vpage?type=AfentiExam", {
                level : currentLevel,
                content : sendContent.val()
            }, function(data){
                if(data.success){
                    alert("发送成功");
                    sendContent.val("");
                }else{
                    alert(data.info);
                }
            });
        });

        //翻页
        var pageBox = $("#pageBox");
        pageBox.find("a[data-pagenumber]").eq(currentPageNumber-1).addClass("active")
        pageBox.find("a[data-pagenumber]").each(function(index){
            var $this = $(this);

            if(currentPageNumber > 10){
                if(index >= currentPageNumber-10 && index < currentPageNumber){
                    $this.show();
                }else{
                    $this.hide();
                }
            }
        });

        pageBox.on("click", "a[data-pagenumber]", function(){
            var $this = $(this);
            currentPageNumber = $this.data("pagenumber");

            if($this.hasClass("active")){
                return false;
            }

            $("#examComment").load("/apps/afenti/comment.vpage?type=AfentiExam&currentPage=" + currentPageNumber);
        });

        pageBox.on("click", "a[data-opt-type]", function(){
            if($(this).data("opt-type") == "next"){
                if(currentPageNumber < getTotalPages){
                    currentPageNumber++;
                }else{
                    return false;
                }
            }else{
                if(currentPageNumber > 1){
                    currentPageNumber--;
                }else{
                    return false;
                }
            }

            $("#examComment").load("/apps/afenti/comment.vpage?type=AfentiExam&currentPage=" + currentPageNumber);
        });

        //提示
        $("#infoBox").hover(function(){
            $(this).siblings().show();
        }, function(){
            $(this).siblings().hide();
        });
    });
</script>