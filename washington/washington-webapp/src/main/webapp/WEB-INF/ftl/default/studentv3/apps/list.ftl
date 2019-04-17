<#macro appsList appName>
    <dt>
        <h3>推荐应用</h3>
        <ul id="appsListBox" class="ta-game-list">

        </ul>
    </dt>

    <script id="t:appsList" type="text/html">
        <%if(appsData.length > 0){%>
            <%for(var i = 0; i < appsData.length; i++){%>
                <li <%if( appsData[i].appName == '${appName!''}'){%> style="display:none;" <%}%> >
                    <a onclick="$17.atongji('<%=appsData[i].appName%>入口-PK馆左侧','<%=appsData[i].appLink%>')" href="javascript:void (0);">
                        <div class="game-icon" style="background-image: url(<@app.link href="public/skin/common/images/app-icon/well/<%=appsData[i].appImg%>.png"/>);"></div>
                        <h5><%if(appsData[i].appName == "阿分题"){%><@ftlmacro.gameAreaVersion/><%}%><%=appsData[i].appName%></h5>
                    </a>
                </li>
            <%}%>
        <%}%>
    </script>

    <script type="text/javascript">
        $(function(){
            $.post('/student/appsList.vpage', {}, function(data){
                if(data.success){
                    $("#appsListBox").html(template("t:appsList",{appsData : data.apps}));
                }else{
                    $("#appsListBox").html('<li style="text-align: center; cursor: default; line-height: 20px;">暂无可<br />推荐的应用</li>');
                }
            });
        });
    </script>
</#macro>