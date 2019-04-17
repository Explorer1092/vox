<div data-title="上传照片" style="display: none;">
    <iframe name="_uploadPhoto" id="uploadPhoto"></iframe>
</div>
<#--style="position:absolute;left:0;top:0;width:100%; height:100%;z-index:999; opacity:100; filter: alpha(opacity=0);" onchange="document.forms[0].submit();"-->
<div class="parent-time-line-box">
    <#if (missions.getContent()?size gt 0)!false>
        <#list missions.getContent() as mis>
            <#if mis??>
            <dl>
                <dt>
                    <span class="wish-icon icon-circle"></span>
                    <span class="big-arrow">◀</span>
                </dt>
                <dd>
                    <div class="parent-time-wish">
                        <div class="pt-title">
                            <i class="wish-icon icon-sweet"></i>心愿：${mis.rewards!'---'}
                            <a class="pt-right parent-btn-mini" href="javascript:void(0);" data-id="${(mis.id)!}">
                            ${(mis.img?has_content)?string("更换照片", "添加照片")}
                                <form id="v-addPhotoSubmit-${(mis.id)!}" action="/student/parentreward/missionpicture.vpage" method="post" enctype="multipart/form-data" target="_uploadPhoto">
                                    <input type='hidden' name='missionId' value="${(mis.id)!}"/>
                                    <input type="file" name="filedata" data-id="${(mis.id)!}" class="v-clickUploadPhoto" style="cursor: pointer;position:absolute;left:0;top:0;width:80px; height:100%;z-index:999; opacity:0; filter: alpha(opacity=0); "/>
                                </form>
                            </a>
                        </div>
                        <div class="pt-content">
                        <#--显示照片-->
                            <div class="photo-box v-photoShowBox-${mis.id!}" style="display: ${(mis.img?has_content)?string("block", "none")}">
                                <div class="arrow">▲</div>
                                <div class="photo">
                                    <#if mis.img?has_content><img src="<@app.avatar href='${mis.img}'/>"/></#if>
                                </div>
                            </div>
                            <div class="pc-time">
                                <p>
                                    <i class="wish-icon icon-time"></i>
                                    <span class="time">${mis.missionDate!'---'}</span>
                                    <span class="goal" title="${mis.mission!'---'}">目标：${mis.mission!'---'}</span>
                                    <span class="progress">进度：${mis.finishCount!0}/${mis.totalCount!0}</span>
                                </p>
                                <#if (mis.missionState == "ONGOING")!false>
                                    <#if (mis.canClick)!false>
                                        <a  class="pt-right parent-btn-mini v-progressParents" href="javascript:void(0);" data-template="${((mis.op == "STUDENT_REMIND_PROGRESS")!false)?string("TEMPLATE_REMIND_UPDATE_PROGRESS", "TEMPLATE_REMIND_REWARD")}" data-id="${(mis.id)!}">
                                            催促家长${((mis.op == "STUDENT_REMIND_PROGRESS")!false)?string("进度+1次", "奖励")}
                                        </a>
                                    <#else>
                                        <a  class="pt-right parent-btn-mini parent-btn-mini-dis" href="javascript:void(0);">已催促</a>
                                    </#if>
                                <#else>
                                    <a  class="pt-right parent-btn-mini parent-btn-mini-dis" href="javascript:void(0);">已奖励</a>
                                </#if>
                                <div class="pt-clear"></div>
                            </div>
                        </div>
                    </div>
                </dd>
            </dl>
            </#if>
        </#list>
        <div id="showMoreContent"></div>
        <dl class="time-finish">
            <dt>
                <span class="wish-icon icon-circle"></span>
                <span class="big-arrow">◀</span>
                <span class="line"></span>
            </dt>
            <dd>
                <div class="parent-time-wish">
                    <div class="pt-content" style="text-align: center;">
                    <#if (missions.getTotalPages() gt 1)!false>
                        <a href="javascript:void(0);" class="parent-btn-mini v-showMoreWish" data-type="${(canMakeWish??)?string("SIGN", "ALL")}">显示更多记录</a>
                    <#else>
                        <i class="wish-icon icon-end"></i>没有更多记录啦！
                    </#if>
                    </div>
                </div>
            </dd>
        </dl>
    <#else>
        <dl class="time-finish">
            <dt>
                <span class="wish-icon icon-circle"></span>
                <span class="big-arrow">◀</span>
                <span class="line"></span>
            </dt>
            <dd>
                <div class="parent-time-wish"  >
                    <div class="pt-content">
                        <i class="wish-icon icon-end"></i>还没有任务！
                    </div>
                </div>
            </dd>
        </dl>
    </#if>
</div>

<script type="text/html" id="T:目标列表Join">
<%if(missions.length > 0){%>
    <%for(var i = 0; i < missions.length; i++){%>
    <dl>
        <dt>
            <span class="wish-icon icon-circle"></span>
            <span class="big-arrow">◀</span>
        </dt>
        <dd>
            <div class="parent-time-wish">
                <div class="pt-title">
                    <i class="wish-icon icon-sweet"></i>心愿：<%=missions[i].rewards%>
                    <a class="pt-right parent-btn-mini" href="javascript:void(0);" data-id="<%=missions[i].id%>">
                        <%=(missions[i].img ? "更换照片" : "添加照片")%>
                        <form id="v-addPhotoSubmit-<%=missions[i].id%>" action="/student/parentreward/missionpicture.vpage" method="post" enctype="multipart/form-data" target="_uploadPhoto">
                            <input type='hidden' name='missionId' value="<%=missions[i].id%>"/>
                            <input type="file" name="filedata" data-id="<%=missions[i].id%>" class="v-clickUploadPhoto" style="cursor: pointer;position:absolute;left:0;top:0;width:100%; height:100%;z-index:999; opacity:0; filter: alpha(opacity=0);"/>
                        </form>
                    </a>
                </div>
                <div class="pt-content">
                <#--显示照片-->
                    <div class="photo-box v-photoShowBox-<%=missions[i].id%>" style="display: <%=(missions[i].img ? 'block' : 'none')%>">
                        <div class="arrow">▲</div>
                        <div class="photo">
                            <%if(missions[i].img){%><img src="<@app.avatar href='<%=missions[i].img%>'/>"/><%}%>
                        </div>
                    </div>
                    <div class="pc-time">
                        <p>
                            <i class="wish-icon icon-time"></i>
                            <span class="time"><%=missions[i].missionDate%></span>
                            <span class="goal" title="<%=missions[i].mission%>">目标：<%=missions[i].mission%></span>
                            <span class="progress">进度：<%=missions[i].finishCount%>/<%=missions[i].totalCount%></span>
                        </p>
                    <%if(missions[i].missionState == "ONGOING"){%>
                        <%if(missions[i].canClick){%>
                            <a  class="pt-right parent-btn-mini v-progressParents" href="javascript:void(0);"
                                data-template='<%=(missions[i].op == "STUDENT_REMIND_PROGRESS" ? "TEMPLATE_REMIND_UPDATE_PROGRESS" : "TEMPLATE_REMIND_REWARD")%>' data-id="<%=missions[i].id%>">
                                催促家长<%=(missions[i].op == "STUDENT_REMIND_PROGRESS" ? "进度+1次" : "奖励")%>
                            </a>
                        <%}else{%>
                            <a  class="pt-right parent-btn-mini parent-btn-mini-dis" href="javascript:void(0);">已催促</a>
                        <%}%>
                    <%}else{%>
                        <a  class="pt-right parent-btn-mini parent-btn-mini-dis" href="javascript:void(0);">已奖励</a>
                    <%}%>
                        <div class="pt-clear"></div>
                    </div>
                </div>
            </div>
        </dd>
    </dl>
    <%}%>
<%}%>
</script>

<script type="text/javascript">
    $(function(){
        //催促家长
        $(document).on("click", ".v-progressParents",function(){
            var $this = $(this);
            var $missionId = $this.data('id');
            var $template = $this.data('template');

            if($this.hasClass("parent-btn-mini-dis") || $17.isBlank($missionId)){
                return false;
            }
            $this.addClass("parent-btn-mini-dis");
            $.post('/student/parentreward/remindsendnotice.vpage',{missionId: $missionId, template : $template}, function(data){
                if(data.success){
                    $this.addClass("parent-btn-mini-dis").text("已催促");
                    $17.alert("已经提交请到家长通查看");
                }else{
                    $17.alert(data.info);
                    $this.removeClass("parent-btn-mini-dis");
                }
            });

            $17.tongji('家长奖励-点击-催促', $template);
        });

        //上传照片
        $(document).on("change", ".v-clickUploadPhoto", function(){
            var $this = $(this);
            var $thisVal = $this.val();

            if($17.isBlank($thisVal)){
                return false;
            }

            if($thisVal.indexOf(".gif") < 1 && $thisVal.indexOf(".png") < 1 && $thisVal.indexOf(".jpg") < 1 && $thisVal.indexOf(".PNG") < 1 && $thisVal.indexOf(".GIF") < 1 && $thisVal.indexOf(".JPG") < 1){
                $17.alert("你选择的不是图片。");
                return false;
            }

            if($this[0].files[0].size >= 10485760){
                $17.alert("你上传的图片太大，请上传小于10M的图片。");
                return false;
            }

            $(".v-photoShowBox-" + $this.attr("data-id")).find(".photo").text("正在上传中...");
            $("#v-addPhotoSubmit-" + $this.attr("data-id") ).submit();

            $17.tongji('家长奖励-点击-上传照片');
        });

        //显示更多
        var getTotal = 1;
        $(document).on("click", ".v-showMoreWish", function(){
            var $this = $(this);

            if( $17.isBlank($this.attr("data-type")) ){
                return false;
            }

            getTotal++;

            $.get("/student/parentreward/more.vpage", {
                currentPage : getTotal,
                type : $this.attr("data-type")
            }, function(data){
                if(data.success){
                    if(data.missions.number == ${(missions.getTotalPages() - 1)!0}){
                        $this.after('<i class="wish-icon icon-end"></i>没有更多记录啦！').remove();
                    }
                    $("#showMoreContent").append(template("T:目标列表Join", {missions : data.missions.content}));
                }
            });

            $17.tongji('家长奖励-点击-显示更多');
        });
    });

    function callBackInfo(content){
        $17.alert(content);
    }
</script>