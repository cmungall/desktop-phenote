<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>
<html>
<body>
<ul>
  <c:forEach var="completionTerm" items="${formBean.completionTermList}">
    <li onmouseover="phenoteState.updateTermInfo(new Term('<c:out value="${completionTerm.ID}" />',
                                                          '<c:out value="${completionTerm.escapedName}" escapeXml="false" />',
                                                          '<c:out value="${completionTerm.ontol}" />'));" 
        id='<c:out value="${completionTerm.ID}" />'
        ><c:out value="${completionTerm.name}" /><span class="informal"><c:out value="${completionTerm.compListInformalString}" /></span></li>
  </c:forEach>
</ul>
</body>
</html>