<div class="w-background-gray w-ag-right">
    <span class="w-fl-right">已选择 <strong id="20140923_unitNum" class="w-blue"></strong>个单元</span>
</div>
<div class="w-table w-table-border-bot">
    <div class="w-table-head">
        <span class="w-gray w-magR-10">当前教材：<strong id="20140923_bookName"></strong></span>
        <a data-jump-link="/teacher/exam/activity/booklist.vpage" href="/teacher/exam/activity/booklist.vpage" style="width: 130px;" class="v-jump-link w-btn w-btn-mini"><span class="w-icon-public w-icon-switch"></span><span class="w-icon-md">换教材</span></a>
    </div>
    <table>
        <tbody id="20140923_unitListBybookId"></tbody>
    </table>
</div>
<div class="w-clear"></div>
<script id="t:20140923_unitList_单元列表" type="text/html">
    <tr style="display: none;">
        <td><span class="v-allcheckbox w-checkbox"></span></td>
    </tr>
    <% for(var i = 0; i < units.length; i++){ %>
        <tr style="cursor:pointer;" <% if(i % 0 == 0){ %>class="odd"<% } %>>
            <td>&nbsp;&nbsp;<span class="v-nodecheckbox w-checkbox" data-value="<%=units[i].id%>"></span>&nbsp;&nbsp;<%=units[i].name%></td>
        </tr>
    <% } %>
</script>
<script type="text/javascript">
    var UnitList = null;

    $(function(){
        UnitList = new $17.Model({
            subject     : "ENGLISH",
            unitIds     : []
        });
        UnitList.extend({
            setSubject: function(subject){
                this.subject = subject;
                return this;
            },
            getUnitIds: function(){
                return this.unitIds;
            },
            getUnits: function(bookId){
                var $this = this;

                $this.unitIds = [];

                $("#20140923_unitNum").html(0);

                var promise = App.postJSON("/book/unitlist.vpage", {
                    subject : $this.subject,
                    bookId  : bookId
                }, function(data){
                    $("#20140923_bookName").html(data.bookName);

                    $("#20140923_unitListBybookId").html(template("t:20140923_unitList_单元列表", {
                        units: data.units
                    }));

                    $17.modules.checkboxs("#20140923_unitListBybookId", ".v-allcheckbox", ".v-nodecheckbox");

                    $("#20140923_unitListBybookId tr").on("click", function(event){
                        if(event.target.nodeName.toUpperCase() != "SPAN"){
                            $(this).find("span").trigger("click");
                        }
                    });
                });

                $(".v-nodecheckbox").live("$17.modules.checkboxs.click", function(){
                    UnitList.unitIds = $(".v-allcheckbox").attr("data-values").split(",");
                    $("#20140923_unitNum").html(UnitList.unitIds.length);
                });

                return promise;
            },
            resetLink : function(level, clazzs){
                var $that = $(".v-jump-link");
                $that.attr("href", $that.attr("data-jump-link") + "?l=" + level + "&cs=" + clazzs);
            }
        });
    });
</script>