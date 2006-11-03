<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>

<html>
<head>
  <title>List of Ontologies</title>
  <link type="text/css" rel="stylesheet" href="<%= request.getContextPath()%>/css/phenote.css"/>
</head>

<body>


<table cellpadding="2" cellspacing="1" border="0" width="80%">

  <tr><td colspan="4" class="sectionTitle">Phenote Application</td></tr>
  <tr>
    <td class="listContentBold">
      Version
    </td>
    <td colspan="3" class="listContent">
      0.9
    </td>
  </tr>
  <tr>
    <td class="listContentBold">
      Date
    </td>
    <td colspan="3" class="listContent">
      2006
    </td>
  </tr>
  <tr><td colspan="4" class="sectionTitle"> List of Ontologies</td></tr>
  <tr>
    <td width="100" class="sectionTitle">Ontology Name</td>
    <td class="sectionTitle">Version</td>
    <td class="sectionTitle">File Name</td>
    <td class="sectionTitle">File Date</td>
  </tr>
  <c:forEach var="item" items="${formBean.ontologies}">
    <tr>
      <td class="listContentBold">
        <c:out value='${item.name}'/>
      </td>
      <td class="listContent">
      </td>
      <td class="listContent">
        <c:out value='${item.source}'/>
      </td>
      <td class="listContent">
      </td>
    </tr>
  </c:forEach>
</table>
</body>
</html>

