<div id="review-detail" title="学校审核详情" style="font-size: small; display: none" school_Id="" >
    <div id="clue-review"></div>
    <input type="button" class="btn" value="关 闭" style="width:80px; margin-left: 20px;"
           onclick="closeApplyDetail()"/>
</div>

<div id="reject-info" title="驳回原因" style="font-size: small; display: none" data-update="" data-clueId="" data-status="">
    <div><strong>驳回原因：</strong><input id="reviewNote" type="text"/></div>
    <input type="button" class="btn btn-primary" value="确 认" style="width:80px; margin-right: 20px;"
           onclick="sureReject()"/>
    <input type="button" class="btn" value="取  消" style="width:80px; margin-left: 20px;" onclick="closeReject()"/>
</div>

<script type="text/html" id="review_detail">
    <#--<div>
        <div style="text-align:center;width:30%;float: left;">
            <span>学校信息</span>
            <div>
                <ul>
                    <li>所在区域:<%=view.provinceName%> <%=view.cityName%> <%=view.countyName%></li>
                    <li>学校全称:<%=view.fullName%></li>
                    <li>学校简称:<%=view.shortName%></li>
                    <li>鉴定状态:<%=view.authenticationState%></li>
                    <li>学段:<%=view.schoolPhase%></li>
                    <li>学制:<%=view.schoolLength%></li>
                    <li>英语起始年级:<%=view.englishStartGrade%></li>
                    <li>学校规模:<%=view.schoolSize%></li>
                    <li>学校位置:<%=view.address%></li>
                </ul>
            </div>
        </div>
        <div style="text-align:center;width:30%;float:left;">
            <span>照片预览</span>
            <span id="photo-clue"></span>
        </div>
        <div style="text-align:center;width:30%;float:left;">
            <span>学校位置</span>
            <div id="inner_map" style="width: 600px; height: 400px;">
        </div>
    </div>
        <table class="table table-bordered;" style="padding-top:10px;clear: both;">
            <tr>
                <th>申请时间</th>
                <th>申请人</th>
                <th>联系方式</th>
                <th>照片</th>
                <th>位置</th>
                <th>审核状态</th>
                <th>审核人</th>
                <th>审核时间</th>
                <th>驳回原因</th>
                <th>操作</th>
            </tr>
            <%if (clues){%>
            <%for(var i = 0; i < clues.length && i < 50; ++i){%>
            <tr>
                <td><%=clues[i].createApplyTime%></td>
                <td><%=clues[i].recorderName%></td>
                <td><%=clues[i].recorderPhone%></td>
                <td class="photo-but">
                    <a href="javascript:photoDetail('<%=clues[i].latitude%>','<%=clues[i].longitude%>','<%=clues[i].photoUrl%>')">点击查看</a>
                </td>
                <td><%=clues[i].address%></td>
                <td><%=clues[i].checkStatus%></td>
                <td><%=clues[i].reviewerName%></td>
                <td><%=clues[i].reviewTime%></td>
                <td><%=clues[i].reviewNote%></td>
                <td><% if(clues[i].checkStatus == "待审核"){%>
                    <a href="javascript:reviewClue('<%=clues[i].clueId%>',2,<%=clues[i].updateTime%>)"
                       data-id="<%=clues[i].clueId%>">通过</a>
                    <a href="javascript:rejectClue('<%=clues[i].clueId%>',-1,<%=clues[i].updateTime%>)"
                       data-id="<%=clues[i].clueId%>">驳回</a>
                    <%}%>
                </td>
            </tr>
            <%}%>
            <%}%>
        </table>-->
    <table width="100%">
        <thead>
        <tr style=" width:100%;text-align: center;vertical-align:top">
            <th style="width:33%;text-align:left"><p style="text-align:center">学校信息</p>
                <p>所在区域:<%=view.provinceName%> <%=view.cityName%> <%=view.countyName%></p>
                <p>学校全称:<%=view.fullName%></p>
                <p>学校简称:<%=view.shortName%></p>
                <p>鉴定状态:<%=view.authenticationState%></p>
                <p>学段:<%=view.schoolPhase%></p>
                <p>学制:<%=view.schoolLength%></p>
                <p>英语起始年级:<%=view.englishStartGrade%></p>
                <p>学校规模:<%=view.schoolSize%></p>
                <p>学校位置:<%=view.address%></p>
            </th>
            <th style="width:33%">照片预览<span id="photo-clue" style="width:400px;height:400px;"></span></th>
            <th style="width:33%">学校位置
                <div id="inner_map" style="width: 400px; height: 400px;"></div>
                <div style="text-align: left;margin-top:20px">
                    经纬度<input id="manualPosition" type="text"/>
                    <button type="button" onclick="useManualPosition()">提交位置</button><br/>
                    位置：<input id="manualAddress" type="text"  readonly style="width:300px"/><br/>
                    <a href="http://lbs.amap.com/console/show/picker" target="_blank" style="color: blue">点击打开坐标获取器</a>
                </div>

            </th>
        </tr>
        </thead>
    </table>
    <table class="table table-striped table-bordered" style="margin-top:50px">
        <tr>
            <th>申请时间</th>
            <th>申请人</th>
            <th>联系方式</th>
            <th>照片</th>
            <th>位置</th>
            <th>审核状态</th>
            <th>审核人</th>
            <th>审核时间</th>
            <th>驳回原因</th>
            <th>操作</th>
        </tr>
        <%if (clues){%>
        <%for(var i = 0; i < clues.length && i < 50; ++i){%>
        <tr>
            <td><%=clues[i].createApplyTime%></td>
            <td><%=clues[i].recorderName%></td>
            <td><%=clues[i].recorderPhone%></td>
            <td class="photo-but">
                <a href="javascript:photoDetail('<%=clues[i].latitude%>','<%=clues[i].longitude%>','<%=clues[i].photoUrl%>')">点击查看</a>
            </td>
            <td><%=clues[i].address%></td>
            <td><%=clues[i].checkStatus%></td>
            <td><%=clues[i].reviewerName%></td>
            <td><%=clues[i].reviewTime%></td>
            <td><%=clues[i].reviewNote%></td>
            <td><% if(clues[i].checkStatus == "待审核"){%>
                <a href="javascript:reviewClue('<%=clues[i].clueId%>',2,<%=clues[i].updateTime%>)"
                   data-id="<%=clues[i].clueId%>">通过</a>
                <a href="javascript:rejectClue('<%=clues[i].clueId%>',-1,<%=clues[i].updateTime%>)"
                   data-id="<%=clues[i].clueId%>">驳回</a>
                <%}%>
            </td>
        </tr>
        <%}%>
        <%}%>
    </table>
</script>