<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>
<html>
<body>
<ul>
  <c:forEach var="completionTerm" items="${formBean.completionTermList}">
    <li onmouseover="getTermInfo('<c:out value="${completionTerm.ID}" />','<c:out value="${completionTerm.name}" />','<c:out value="${completionTerm.ontol}" />','<c:out value="${completionTerm.field}" />')" id='<c:out value="${completionTerm.ID}" />'
        onclick="selectTerm('<c:out value="${completionTerm.name}" />','<c:out value="${completionTerm.field}" />')"><c:out value="${completionTerm.name}" /></li>
  </c:forEach>
</ul>
</body>
</html>