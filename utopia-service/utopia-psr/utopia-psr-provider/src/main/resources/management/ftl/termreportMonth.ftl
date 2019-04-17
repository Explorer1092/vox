<h3>Monthly Term Report  ${action} Test Page</h3>
<br>
<html>
<body>
<form action="/psr/${action}.do" method="get">
<table>
    <tr>
        <td>
            yeadId:
        </td><td>
            <input type="text" name="yearId" value="${yearId}" placeholder="请输入" />
        </td><td>
            (*)
        </td>
    </tr>
    <tr>
        <td>
            termId:
        </td><td>
        <input type="text" name="termId" value="${termId}" placeholder="请输入" />
    </td><td>
        (*)
    </td>
    </tr>
    <tr>
        <td>
            groupId:
        </td><td>
        <input type="text" name="group_id" value="${group_id}" placeholder="请输入" />
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
        </td><td>
            <input type="submit" id="submit" value="提交" />
        </td>
    </tr>
</table>
</form>
<table border="1"><tr><td>${retMsg}</td></tr></table>
</body>
</html>
