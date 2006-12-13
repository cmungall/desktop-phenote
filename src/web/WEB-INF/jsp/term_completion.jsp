<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>
<html>
<body>
<ul>
  <c:forEach var="completionTerm" items="${formBean.completionTermList}">
    <li onmouseover="phenoteState.updateTermInfo(new Term('<c:out value="${completionTerm.ID}" />','<c:out value="${completionTerm.escapedName}" escapeXml="false" />','<c:out value="${completionTerm.ontol}" />'));" id='<c:out value="${completionTerm.ID}" />'
        onclick="phenoteState.setCompletedTerm('<c:out value="${completionTerm.escapedName}" />','<c:out value="${completionTerm.field}" />')"><c:out value="${completionTerm.compListDisplayString}" /></li>
  </c:forEach>
</ul>
</body>
</html>