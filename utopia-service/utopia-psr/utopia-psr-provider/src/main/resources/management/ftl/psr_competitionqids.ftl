<h3>PSR ${action} Test Page</h3>
<br>
<html>
<body>
<form action="/psr/${action}.do" method="get">
<table>
    <tr>
        <td>
            用户Id:
        </td><td>
            <input type="text" name="userId" value="${userId}" placeholder="请输入" />
        </td><td>
            (*)
        </td>
    </tr>
    <tr>
        <td>
            subjectId:
        </td><td>
        <input type="text" name="subjectId" value="${subjectId}" placeholder="请输入" />
    </td><td>
        (*)
    </td>
    </tr>
    <tr>
        <td>
            grade:
        </td><td>
            <input type="text" name="grade" value="${grade}" placeholder="请输入" />
        </td><td>
            (*)
        </td>
    </tr>
    <tr>
        <td>
            term:
        </td><td>
            <input type="text" name="term" value="${term}" placeholder="请输入" />
        </td>
    </tr>
    <tr>
        <td>
            取题个数:
        </td><td>
            <input type="text" name="eCount" value="${eCount}" placeholder="请输入" />
        </td><td>
            (*)
        </td>
    </tr>
    <tr>
        <td>
            竞赛类型:
        </td><td>
        <input type="text" name="type" value="${type}" placeholder="请输入" />
    </td><td>
        (*)
    </td>
    </tr>
    <tr>
        <td>
            <input type="hidden" name="onlyResult" value="${onlyResult}" />
            <input type="submit" id="submit" value="提交" />
        </td>
    </tr>
</table>
</form>
<table border="1"><tr><td>${retMsg}</td></tr></table>
</body>
</html>

