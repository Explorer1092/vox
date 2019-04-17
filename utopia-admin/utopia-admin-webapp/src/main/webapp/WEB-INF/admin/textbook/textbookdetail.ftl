<#import "../layout_default.ftl" as layout_default />
<@layout_default.page page_title='教材详细配置' page_num=4 jqueryVersion ="1.7.2">
<style>
    .form-horizontal .control-label {
        float: left;
        width: 206px;
        padding-top: 5px;
        text-align: right;
    }
</style>
<div class="span9">
    <fieldset>
        <legend>教材详情</legend>
    </fieldset>

    <div class="row-fluid">
        <div class="span12">
            <form class="well form-horizontal" style="background-color: #fff;">
                <fieldset>textbook/textbookdetail
                    <div class="control-group">
                        <label class="control-label" for="productName">bookId：</label>
                        <div class="controls">
                            <label for="title">
                                <input type="text"
                                       value="${(bookId)!''}"
                                       name="articleId" id="bookId" maxlength="500"
                                       class="input" style="width: 20%">
                            </label>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="productName">教材名称：</label>
                        <div class="controls">
                            <label for="title">
                                <input type="text" value="${(name)!''}"
                                       name="title" id="bookName" maxlength="50"
                                       style="width: 20%" disabled class="input">
                            </label>
                        </div>
                    </div>
                    <div class="control-group" id="products">
                        <label class="control-label">来源(没用了,全是自研)：</label>
                        <div class="controls">
                            <select id="book_source">
                                <option value="">请选择</option>
                                <option value="SELF_DEVELOP">自研</option>
                            </select>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="productName">出版社：</label>
                        <div class="controls">
                            <label for="title">
                                <input type="text" value="${(publisher)!''}"
                                       name="title" id="publisher" maxlength="50"
                                       style="width: 20%" disabled class="input">
                            </label>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="productName">出版社简称：</label>
                        <div class="controls">
                            <label for="title">
                                <input type="text" value="${(shortPublisher)!''}"
                                       name="title" id="shortPublisher" maxlength="50"
                                       style="width: 20%" disabled class="input">
                            </label>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="productName">教材简称：</label>
                        <div class="controls">
                            <label for="title">
                                <input type="text" value="${(shortName)!''}"
                                       name="title" id="shortName" maxlength="50"
                                       style="width: 20%" disabled class="input">
                            </label>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="productName">年级：</label>
                        <div class="controls">
                            <label for="title">
                                <input type="text" value="${(clazzLevel)!''}"
                                       name="title" id="clazzLevel" maxlength="50"
                                       style="width: 20%" disabled class="input">
                            </label>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="productName">学期：</label>
                        <div class="controls">
                            <label for="title">
                                <input type="text"
                                       value="<#if termType??&&termType==1>上学期<#elseif termType??&&termType==2>下学期</#if>"
                                       name="title" id="termType" maxlength="50"
                                       style="width: 20%" disabled class="input">
                            </label>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="productName">列表页教材名称：</label>
                        <div class="controls">
                            <label for="title">
                                <input type="text" value="${(listName)!''}"
                                       name="title" id="listName" maxlength="50"
                                       style="width: 20%" class="input">
                            </label>
                        </div>
                    </div>
                    <ul class="nav nav-list">
                        <li class="divider"></li>
                    </ul>
                    <div class="control-group">
                        <label class="control-label">点读机android是否上线：</label>
                        <div class="controls">
                            <table>
                                <tr>
                                    <td><label for="online">上线
                                        <input type="radio" name="picAndroidOnline" id="picAndroidOnlineType"
                                               value="true"/>
                                    </label></td>
                                    <td><label for="offline">下线
                                        <input type="radio" name="picAndroidOnline" id="picAndroidOfflineType"
                                               value="false"/>
                                    </label></td>
                                </tr>
                            </table>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">点读机IOS是否上线：</label>
                        <div class="controls">
                            <table>
                                <tr>
                                    <td><label for="online">上线
                                        <input type="radio" name="picIOSOnline" id="picIOSOnlineType" value="true"/>
                                    </label></td>
                                    <td><label for="offline">下线
                                        <input type="radio" name="picIOSOnline" id="picIOSOfflineType" value="false"/>
                                    </label></td>
                                </tr>
                            </table>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">点读机小程序是否上线：</label>
                        <div class="controls">
                            <table>
                                <tr>
                                    <td><label for="online">上线
                                        <input type="radio" name="picMiniProgramOnline" id="picMiniProgramOnlineType"
                                               value="true"/>
                                    </label></td>
                                    <td><label for="offline">下线
                                        <input type="radio" name="picMiniProgramOnline" id="picMiniProgramOfflineType"
                                               value="false"/>
                                    </label></td>
                                </tr>
                            </table>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">点读机是否仅对认证用户上线：</label>
                        <div class="controls">
                            <table>
                                <tr>
                                    <td><label for="isAuthUserOnline">是
                                        <input type="radio" name="picAuthOnline" id="picAuthOnlineType" value="true"/>
                                    </label></td>
                                    <td><label for="isAuthUserOnline">否
                                        <input type="radio" name="picAuthOnline" id="picAuthOfflineType" value="false"/>
                                    </label></td>
                                    <td>
                                        <span id="picReadComment"
                                              style="color: red">如需设置仅对认证用户上线，需要将点读机先设为下线，再将该条件设为“是”</span>
                                    </td>
                                </tr>
                            </table>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">点读机是否免费：</label>
                        <div class="controls">
                            <table>
                                <tr>
                                    <td><label for="isAuthUserOnline">是
                                        <input type="radio" name="picIsFree" id="picIsFree" value="true"/>
                                    </label></td>
                                    <td><label for="isAuthUserOnline">否
                                        <input type="radio" name="picIsFree" id="picIsPay" value="false"/>
                                    </label></td>
                                    <td>
                                        <span id="picReadComment"
                                              style="color: red">设置成需要付费，请务必在产品管理里配置相关产品</span>
                                    </td>
                                </tr>
                            </table>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">点读机是否支持预览：</label>
                        <div class="controls">
                            <table>
                                <tr>
                                    <td><label for="isAuthUserOnline">是
                                        <input type="radio" name="picIsPreview" id="picIsPreview" value="true"/>
                                    </label></td>
                                    <td><label for="isAuthUserOnline">否
                                        <input type="radio" name="picIsPreview" id="picNotPreview" value="false"/>
                                    </label></td>
                                </tr>
                            </table>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">点读机SDK类型</label>
                        <div class="controls">
                            <select id="sdkTypeSelect">
                                <option value="none">无</option>
                                <option value="waiyan">外研社</option>
                                <option value="renjiao">人教版</option>
                                <option value="hujiao">沪教版</option>
                            </select>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="productName">点读机sdkBookId：</label>
                        <div class="controls">
                            <label for="title">
                                <input type="text"
                                       value="${(picListenConfig.sdkInfo.sdkBookId)!''}"
                                       name="sdkBookId" id="sdkBookId" maxlength="500"
                                       class="input" style="width: 20%">
                            </label>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="productName">点读机 新版 sdkBookId （人教）：</label>
                        <div class="controls">
                            <label for="title">
                                <input type="text"
                                       value="${(picListenConfig.sdkInfo.renjiaoNewSdkBookId)!''}"
                                       name="sdkBookIdV2" id="sdkBookIdV2" maxlength="500"
                                       class="input" style="width: 20%">
                            </label>
                        </div>
                    </div>
                    <ul class="nav nav-list">
                        <li class="divider"></li>
                    </ul>
                    <div class="control-group">
                        <label class="control-label">随身听android是否上线：</label>
                        <div class="controls">
                            <table>
                                <tr>
                                    <td><label for="online">上线
                                        <input type="radio" name="walkManAndroidOnline" id="walkManAndroidOnlineType"
                                               value="true"/>
                                    </label></td>
                                    <td><label for="offline">下线
                                        <input type="radio" name="walkManAndroidOnline" id="walkManAndroidOfflineType"
                                               value="false"/>
                                    </label></td>
                                </tr>
                            </table>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">随身听IOS是否上线：</label>
                        <div class="controls">
                            <table>
                                <tr>
                                    <td><label for="online">上线
                                        <input type="radio" name="walkManIOSOnline" id="walkManIOSOnlineType"
                                               value="true"/>
                                    </label></td>
                                    <td><label for="offline">下线
                                        <input type="radio" name="walkManIOSOnline" id="walkManIOSOfflineType"
                                               value="false"/>
                                    </label></td>
                                </tr>
                            </table>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">随身听小程序是否上线：</label>
                        <div class="controls">
                            <table>
                                <tr>
                                    <td><label for="online">上线
                                        <input type="radio" name="walkManMiniProgramOnline" id="walkManMiniProgramOnlineType"
                                               value="true"/>
                                    </label></td>
                                    <td><label for="offline">下线
                                        <input type="radio" name="walkManMiniProgramOnline" id="walkManMiniProgramOfflineType"
                                               value="false"/>
                                    </label></td>
                                </tr>
                            </table>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">随身听是否免费：</label>
                        <div class="controls">
                            <table>
                                <tr>
                                    <td><label for="online">是
                                        <input type="radio" name="walkManIsFree" id="walkManIsFree"
                                               value="true"/>
                                    </label></td>
                                    <td><label for="offline">否
                                        <input type="radio" name="walkManIsFree" id="walkManIsPay"
                                               value="false"/>
                                    </label></td>
                                    <td>
                                        <span id="walkManComment"
                                              style="color: red">设置成需要付费，请务必在产品管理里配置相关产品</span>
                                    </td>
                                </tr>
                            </table>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="productName">随身听最小支持版本：</label>
                        <div class="controls">
                            <label for="title">
                                <input type="text"
                                       value="${(walkManConfig.leastVersion)!''}"
                                       name="leastVersion" id="leastVersion" maxlength="500"
                                       class="input" style="width: 20%">
                            </label>
                        </div>
                    </div>
                    <div id="textReading">
                        <ul class="nav nav-list">
                            <li class="divider"></li>
                        </ul>
                        <div class="control-group">
                            <label class="control-label">语文朗读android是否上线：</label>
                            <div class="controls">
                                <table>
                                    <tr>
                                        <td><label for="online">上线
                                            <input type="radio" name="textReadAndroidOnline"
                                                   id="textReadAndroidOnlineType"
                                                   value="true"/>
                                        </label></td>
                                        <td><label for="offline">下线
                                            <input type="radio" name="textReadAndroidOnline"
                                                   id="textReadAndroidOfflineType"
                                                   value="false"/>
                                        </label></td>
                                    </tr>
                                </table>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">语文朗读IOS是否上线：</label>
                            <div class="controls">
                                <table>
                                    <tr>
                                        <td><label for="online">上线
                                            <input type="radio" name="textReadIOSOnline" id="textReadIOSOnlineType"
                                                   value="true"/>
                                        </label></td>
                                        <td><label for="offline">下线
                                            <input type="radio" name="textReadIOSOnline" id="textReadIOSOfflineType"
                                                   value="false"/>
                                        </label></td>
                                    </tr>
                                </table>
                            </div>
                        </div>
                    </div>
                    <ul class="nav nav-list">
                        <li class="divider"></li>
                    </ul>
                    <div class="control-group">
                        <label class="control-label">是否跟读：</label>
                        <div class="controls">
                            <table>
                                <tr>
                                    <td><label for="isFollowRead">是
                                        <input type="radio" name="followRead" id="followReadTrue" value="true"/>
                                    </label></td>
                                    <td><label for="isFollowRead">否
                                        <input type="radio" name="followRead" id="followReadFalse" value="false"/>
                                    </label></td>
                                </tr>
                            </table>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">是否有单词表：</label>
                        <div class="controls">
                            <table>
                                <tr>
                                    <td><label for="hasWordList">是
                                        <input type="radio" name="wordList" id="hasWordList" value="true"/>
                                    </label></td>
                                    <td><label for="hasWordList">否
                                        <input type="radio" name="wordList" id="noWordList" value="false"/>
                                    </label></td>
                                </tr>
                            </table>
                        </div>
                    </div>
                    <div class="control-group" id="chineseWordSupport">
                        <label class="control-label">是否有生词表：</label>
                        <div class="controls">
                            <table>
                                <tr>
                                    <td><label for="hasWordList">是
                                        <input type="radio" name="chineseWordSupport" id="hasChineseWordSupport"
                                               value="true"/>
                                    </label></td>
                                    <td><label for="hasWordList">否
                                        <input type="radio" name="chineseWordSupport" id="noChineseWordSupport"
                                               value="false"/>
                                    </label></td>
                                </tr>
                            </table>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="productName">备注：</label>
                        <div class="controls">
                            <label for="comment">
                                <textarea name="comment" id="comment" maxlength="50"
                                          style="width: 60%" class="input">${(comment)!''}</textarea>
                            </label>
                        </div>
                    </div>
                    <div class="control-group">
                        <div class="controls">
                            <input type="button" id="saveBtn" value="保存配置" class="btn btn-large btn-primary">
                        </div>
                    </div>
                </fieldset>
            </form>
        </div>
    </div>
</div>

<script type="text/javascript">


    $(function () {
        <#if picListenConfig??>
            <#if picListenConfig.isMiniProgramOnline??>
                var picIsMiniProgramOnline =${(picListenConfig.isMiniProgramOnline)?c};
                setRadioVal('picMiniProgramOnline', picIsMiniProgramOnline);
            <#else>
                setRadioVal('picMiniProgramOnline', false);
            </#if>
            var picIsAndroidOnline =${(picListenConfig.isAndroidOnline)?c};
            setRadioVal('picAndroidOnline', picIsAndroidOnline);
            var picIsIOSOnline =${(picListenConfig.isIOSOnline)?c};
            setRadioVal('picIOSOnline', picIsIOSOnline);
            var picIsFree =${(picListenConfig.isFree)?c};
            setRadioVal('picIsFree', picIsFree);
            var picIsAuthUserOnline =${(picListenConfig.isAuthUserOnline)?c};
            setRadioVal('picAuthOnline', picIsAuthUserOnline);
            var picIsPreview =${(picListenConfig.isPreview)?c};
            setRadioVal('picIsPreview', picIsPreview);
            var sdkType = "${picListenConfigSdkType}";
            $('#sdkTypeSelect').val(sdkType);
        </#if>
        <#if walkManConfig??>
            <#if walkManConfig.isMiniProgramOnline??>
                var walkManIsMiniProgramOnline =${(walkManConfig.isMiniProgramOnline)?c};
                setRadioVal('walkManMiniProgramOnline', walkManIsMiniProgramOnline);
            <#else>
                setRadioVal('walkManMiniProgramOnline', false);
            </#if>

            var walkManIsAndroidOnline =${(walkManConfig.isAndroidOnline)?c};
            setRadioVal('walkManAndroidOnline', walkManIsAndroidOnline);
            var walkManIsIOSOnline =${(walkManConfig.isIOSOnline)?c};
            setRadioVal('walkManIOSOnline', walkManIsIOSOnline);
            var walkManIsFree = "true";
            <#if (walkManConfig.isFree)??>
                walkManIsFree =${(walkManConfig.isFree)?c};
            </#if>
            setRadioVal('walkManIsFree', walkManIsFree);
        </#if>
        <#if (textReadConfig??)&&(subjectId??)&&subjectId==101>
            $('#textReading').show();
            var textReadIsAndroidOnline =${(textReadConfig.isAndroidOnline)?c};
            setRadioVal('textReadAndroidOnline', textReadIsAndroidOnline);
            var textReadIsIOSOnline =${(textReadConfig.isIOSOnline)?c};
            setRadioVal('textReadIOSOnline', textReadIsIOSOnline);
        <#else>
            $('#textReading').hide();
        </#if>
        <#if chineseWordSupport??>
            $('#chineseWordSupport').show();
            var chineseWordSupport =${(chineseWordSupport)?c};
            setRadioVal('chineseWordSupport', chineseWordSupport);
        <#else>
            $('#chineseWordSupport').hide();
        </#if>
        <#if isFollowRead??>
            var isFollowRead =${(isFollowRead)?c};
            setRadioVal('followRead', isFollowRead);
        </#if>
        <#if hasWordList??>
            var hasWordList =${(hasWordList)?c};
            setRadioVal('wordList', hasWordList);
        </#if>
        <#if sourceType??>
            var sourceType = "${(sourceType)!''}";
            $('#book_source').val(sourceType);
        </#if>
        var isNew = false;
        var shortPublisher = $('#shortPublisher').val();
        if (!shortPublisher) {
            isNew = true;
        }
        $('#bookId').blur(function () {
            var bookId = $('#bookId').val();
            if (bookId) {
                $.ajax({
                    type: 'post',
                    url: 'getBookProfile.vpage',
                    data: {
                        bookId: bookId
                    },
                    success: function (data) {
                        if (data.success) {
                            $('#bookName').val(data.bookMap.bookName);
                            $('#publisher').val(data.bookMap.publisher);
                            $('#shortName').val(data.bookMap.shortName);
                            $('#shortPublisher').val(data.bookMap.shortPublisher);
                            $('#clazzLevel').val(data.bookMap.clazzLevel);
                            if (data.bookMap.subjectId == 101) {
                                $('#textReading').show();
                                $('#chineseWordSupport').show();
                            }
                            if (data.bookMap.termType == 1) {
                                $('#termType').val('上学期');
                            }
                            if (data.bookMap.termType == 2) {
                                $('#termType').val('下学期');
                            }
                        }
                    }
                });
            }
        });

        $('#saveBtn').on('click', function () {
            var bookId = $('#bookId').val();
            var refBookId = $('#refBookId').val();
            var comment = $('#comment').val();
            var picListenSdkBookId = $('#sdkBookId').val();
            var picListenSdkBookIdV2 = $('#sdkBookIdV2').val();
            var picListenAndroidOnline = getRadioVal('picAndroidOnline');
            var picListenIOSOnline = getRadioVal('picIOSOnline');
            var picMiniProgramOnline = getRadioVal('picMiniProgramOnline');
            var picListenAuthUserOnline = getRadioVal('picAuthOnline');
            var picListenFree = getRadioVal('picIsFree');
            var picListenPreview = getRadioVal('picIsPreview');
            var walkManAndroidOnline = getRadioVal('walkManAndroidOnline');
            var walkManIOSOnline = getRadioVal('walkManIOSOnline');
            var walkManMiniProgramOnline = getRadioVal('walkManMiniProgramOnline');
            var walkManFree = getRadioVal('walkManIsFree');
            var walkManLeastVersion = $('#leastVersion').val();
            var textReadAndroidOnline = getRadioVal('textReadAndroidOnline');
            var textReadIOSOnline = getRadioVal('textReadIOSOnline');
            var followRead = getRadioVal('followRead');
            var chineseWordSupport = getRadioVal('chineseWordSupport');
            var wordList = getRadioVal('wordList');
            var book_source = $("#book_source").find("option:selected").val();
            var sdkType = $("#sdkTypeSelect").find("option:selected").val();
            var listName = $('#listName').val();
            var postData = {
                bookId: bookId,
                refBookId: refBookId,
                comment: comment,
                picListenSdkBookId: picListenSdkBookId,
                picListenSdkBookIdV2: picListenSdkBookIdV2,
                picListenAndroidOnline: picListenAndroidOnline,
                picListenIOSOnline: picListenIOSOnline,
                picMiniProgramOnline: picMiniProgramOnline,
                picListenAuthUserOnline: picListenAuthUserOnline,
                picListenFree: picListenFree,
                picListenPreview: picListenPreview,
                walkManAndroidOnline: walkManAndroidOnline,
                walkManIOSOnline: walkManIOSOnline,
                walkManMiniProgramOnline: walkManMiniProgramOnline,
                walkManFree: walkManFree,
                walkManLeastVersion: walkManLeastVersion,
                textReadAndroidOnline: textReadAndroidOnline,
                textReadIOSOnline: textReadIOSOnline,
                chineseWordSupport: chineseWordSupport,
                followRead: followRead,
                wordList: wordList,
                isNew: isNew,
                book_source: book_source,
                picListenSdkType: sdkType,
                listName: listName
            };

            //数据校验
            if (bookId == '') {
                alert("bookId不能为空");
                return false;
            }
            if (book_source == '') {
                alert("来源不能为空");
                return false;
            }
            if (picListenFree == 'false' || walkManFree == 'false') {
                if (!checkPayBook(bookId)) {
                    alert("教材未关联付费产品，请先设置付费产品再修改教材付费状态");
                    return false;
                }
            }
            if (listName == ''){
                alert("列表页教材名称不能为空");
                return false;
            }

            $.getUrlParam = function (name) {
                var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
                var r = window.location.search.substr(1).match(reg);
                if (r != null) return unescape(r[2]);
                return null;
            };

            $.post('saveTextBookData.vpage', postData, function (data) {
                if (data.success) {
                    var currentPage = $.getUrlParam('currentPage');
                    location.href = 'textbooklist.vpage?currentPage=' + currentPage;
                } else {
                    alert(data.info);
                }
            });
        });
    });

    function getRadioVal(eleName) {
        var type = '';
        $("input[name='" + eleName + "']").each(function (i) {
            if ($(this).is(':checked')) {
                type = $(this).val();
            }
        });
        console.log('type:' + type);
        return type;
    }

    function setRadioVal(eleName, val1) {
        $(":radio[name='" + eleName + "'][value='" + val1 + "']").prop("checked", "checked");
    }

    function checkPayBook(bookId) {
        var check_flag = false;
        $.ajax({
            type: 'post',
            url: 'loadPayBookProductByBookId.vpage',
            async: false,
            data: {
                bookId: bookId
            },
            success: function (data) {
                if (data.success) {
                    check_flag = true;
                }
            }
        });
        return check_flag;
    }
</script>
</@layout_default.page>