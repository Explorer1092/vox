<script type="text/html" id="regionTreeTest">
        <div class="mobileCRM-V2-header">
            <div class="inner">
                <div class="box">
                    <div class="headerBack" id="regionBack" style="cursor:pointer;">&lt;&nbsp;返回</div>
                    <div class="headerText">选择区域</div>
                </div>
            </div>
        </div>
        <div class="mobileCRM-V2-box mobileCRM-V2-info" id="rTree">
            <ul class="mobileCRM-V2-list">
                    <%for(var first in regionTree){%>
                          <li level="0"  next="<%=regionTree[first].code%>" pre="return" code="0" name="regionList" style="cursor:pointer;"><%=regionTree[first].name%></li>
                                <%for(var second in regionTree[first].children){%>
                                    <li    name="regionList" pre="0" code="<%=regionTree[first].code%>" next="<%=regionTree[first].children[second].code%>" style="display: none;cursor:pointer;"><%=regionTree[first].children[second].name%></li>
                                        <%for(var third in regionTree[first].children[second].children){%>
                                            <li  name="regionList" next="ok" pre="<%=regionTree[first].code%>" code="<%=regionTree[first].children[second].code%>" style="display: none;cursor:pointer;" value="<%=regionTree[first].children[second].children[third].code%>"><%=regionTree[first].children[second].children[third].name%></li>
                                        <%}%>
                                <%}%>
                   <%}%>
            </ul>
        </div>
</script>
<script type="text/javascript">
      //最后一层返回 API,name:城市名称 code:城市code
      $(function(){
          window.regionTreeOK=function(name,code){
          };
          //返回按钮 API
          window.regionTreeReturn=function(){
          };
          $(document).off("click","li[name='regionList']");
          $(document).on("click","li[name='regionList']",function(){
              var next = $(this).attr("next");
              if(next==="ok"){
                  window.regionTreeOK($(this).html(),$(this).attr("value"));
              }else{
                  $("#rTree li").hide();
                  $("#rTree li[code='"+next+"']").show();
              }
          });
          $(document).off("click","#regionBack");
          $(document).on("click","#regionBack",function(){
              var visiElement = $("li[name='regionList']:visible")[0];
              var pre = $(visiElement).attr("pre");
              if(pre=="return"){
                  window.regionTreeReturn();
              }else{
                  $("#rTree ul li").hide();
                  $("#rTree li[code='"+pre+"']").show();
              }
          });
      });
</script>

