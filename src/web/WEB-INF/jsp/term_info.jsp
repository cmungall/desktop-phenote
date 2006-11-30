<%@ taglib prefix="phenote"    uri="/WEB-INF/tld/phenote-tags.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>
<html>
<body>

<b>TERM: </b><c:out value="${formBean.term.name}" />
<br><b>ID: </b><c:out value="${formBean.term.ID}" />
<c:forEach var="synonym" items="${formBean.term.synonyms}" >
  <br><b>Synonym: </b>  <c:out value="${synonym}" />
</c:forEach>
<br>
<br><b>PARENTS: </b>
<c:forEach var="parent" items="${formBean.parents}" >
  <br><b> <phenote:relationhipName beanName="parent" property="type.name" type="parent" />
  : </b><a href="javascript:;" onclick="getTermInfo('<c:out value="${parent.parent.ID}" />','<c:out value="${parent.parent.name}" />','<c:out value="${formBean.ontologyName}" />','<c:out value="${formBean.field}" />')">
  <c:out value="${parent.parent.name}" /></a>
</c:forEach>
<br>
<br><b>CHILDREN: </b>
<c:forEach var="parent" items="${formBean.children}" >
  <br><b>Subclass: </b><a href="javascript:;" onclick="getTermInfo('<c:out value="${parent.child.ID}" />','<c:out value="${parent.child.name}" />','<c:out value="${formBean.ontologyName}" />','<c:out value="${formBean.field}" />')">
  <c:out value="${parent.child.name}" /></a>
</c:forEach>

<br>
<br><b>Definition: </b><c:out value="${formBean.term.definition}" escapeXml="false"/>

</body>
</html>