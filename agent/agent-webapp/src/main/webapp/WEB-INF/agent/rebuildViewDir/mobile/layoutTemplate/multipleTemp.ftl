<#--一级联动-->
<script id="oneLevelTemp" type="text/html">
    <div class="sit-list oneLevel">
        <div class="sit-listLeft">
            <ul>
                <%for(var i = 0; i < array.length; i++) {%>
                <%var item=array[i]%>
                <li data-pid="<%= item.id %>" class="js-firstLevel <% if(item.isSelected) { %> active <% } %>" style="text-align: left;padding-left: 2rem;"> <%=item.name %> </li>
                <% } %>
            </ul>
        </div>
    </div>
</script>

<#--二级联动-->
<script id="twoLevelTemp" type="text/html">
    <div class="sit-list threeLevel">
        <div class="sit-listLeft">
            <ul>
                <%for(var i = 0; i < array.length; i++) {%>
                <%var item=array[i]%>
                <li data-pid="<%= item.id %>" class="js-firstLevel <% if(item.isSelected) { %> active <% } %>"> <%=item.name %> </li>
                <% } %>
            </ul>
        </div>
        <div class="sit-listRight">
            <ul>
                <%for(var i = 0; i < array.length; i++) {%>
                <%var item=array[i].subNodes%>
                    <%for(var j = 0; j < item.length; j++) {%>
                        <li data-cid="<%= item[j].id %>" class="js-secondLevel <% if(item[j].isSelected) { %> active <% } %>" data-pid="<%=array[i].id%>" style="display: none;"> <%=item[j].name %> </li>
                    <% } %>
                <% } %>
            </ul>
        </div>
    </div>
</script>

<script id="thereLevelTemp" type="text/html">
    <#--三级联动-->
    <div class="sit-list threeLevel">
        <div class="sit-listLeft">
            <ul>
                <%for(var i = 0; i < array.length; i++) {%>
                <%var item=array[i]%>
                <li data-pid="<%= item.id %>" class="js-firstLevel <% if(item.isSelected) { %> active <% } %>"> <%=item.name %> </li>
                <% } %>
            </ul>
        </div>
        <div class="sit-listBot"> <#--这是第三级-->
            <ul>
                <%for(var i = 0; i < array.length; i++) {%>
                <%var item=array[i].subNodes%>
                <%for(var j = 0; j < item.length; j++) {%>
                <%var subItem=item[j].subNodes%>
                <%for(var k = 0; k < subItem.length; k++) {%>
                <li data-rid="<%= subItem[k].id %>" class="js-thirdLevel <% if(subItem[k].isSelected) { %> active <% } %>" data-pid="<%=item[j].id%>"  style="display: none;"> <%=subItem[k].name %> </li>
                <% } %>
                <% } %>
                <% } %>
            </ul>
        </div>
        <div class="sit-listRight"><#--这是第二级-->
            <ul>
                <%for(var i = 0; i < array.length; i++) {%>
                <%var item=array[i].subNodes%>
                <%for(var j = 0; j < item.length; j++) {%>
                <li data-cid="<%= item[j].id %>" class="js-secondLevel <% if(item[j].isSelected) { %> active <% } %>" data-pid="<%=array[i].id%>"  style="display: none;"> <%=item[j].name %> </li>
                <% } %>
                <% } %>
            </ul>
        </div>
    </div>
</script>

