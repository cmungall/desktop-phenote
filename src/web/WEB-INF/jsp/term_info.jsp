<%@ taglib prefix="phenote" uri="/WEB-INF/tld/phenote-tags.tld" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>
<html>
<body>

<table width="90%">
<c:if test="${formBean.useTerm}">
 <tr>
 <td colspan=2 align=right>
      <a href="javascript:;" onclick="window.parent.useTerm('<c:out value="${formBean.term.name}"/>');  " >[Add Term]</a>
 </td>
 </tr>
</c:if>
  <tr>
    <td width="110">
      <FONT SIZE=+1><STRONG>TERM:</STRONG></FONT></td>
    <td>
      <FONT SIZE=+1><STRONG><div id="termid"><c:out value="${formBean.term.name}"/></div></STRONG></FONT>
    </td>
  </tr>
  <tr valign="top">
    <td>
      <b>ID:</b>
    </td>
    <td>
      <c:out value="${formBean.term.ID}"/>
    </td>
  </tr>
  <logic:notEmpty name="formBean" property="term.synonyms">
    <c:forEach var="synonym" items="${formBean.term.synonyms}">
      <tr valign="top">
        <td>
          <b>Synonyms:</b>
        </td>
        <td>
          <c:out value="${synonym}"/>
        </td>
      </tr>
    </c:forEach>
  </logic:notEmpty>
</table>

<table width="90%">
  <tr valign="top">
    <td width="110">
      <b>Definition:</b>
    </td>
    <td>
      <c:out value="${formBean.term.definition}" escapeXml="false"/>
    </td>
  </tr>
</table>

<b>PARENTS: </b>
<c:forEach var="parent" items="${formBean.parents}">
  <table width="90%">
    <tr valign="top">
      <td width="110">
        <b><phenote:relationhipName beanName="parent" property="type.name" type="parent"/>:</b>
      </td>
      <td>
      <c:choose>
      <c:when test="${ formBean.isExternalViewType }">
        <c:choose>
          <c:when test="${ formBean.useTerm}">
        <a href="/phenote/Phenote/?termId=<c:out value='${parent.parent.ID}' />&ontologyName=<c:out value='${formBean.ontologyName}'/>&field=ENTITY&viewType=EXTERNAL&useTerm=true">
          </c:when>
          <c:otherwise>
        <a href="/phenote/Phenote/?termId=<c:out value='${parent.parent.ID}' />&ontologyName=<c:out value='${formBean.ontologyName}'/>&field=ENTITY&viewType=EXTERNAL">
          </c:otherwise>
        </c:choose>
      </c:when>
      <c:otherwise>
        <a href="javascript:;"
           onclick="phenoteState.updateTermInfo(new Term('<c:out value="${parent.parent.ID}" />','<c:out value="${parent.parent.name}" />','<c:out value="${formBean.ontologyName}" />'));">
      </c:otherwise>
      </c:choose>
          <c:out value="${parent.parent.name}"/></a>
      </td>
    </tr>
  </table>
</c:forEach>
<br><b>CHILDREN: </b>
<c:forEach var="parent" items="${formBean.children}">
  <table width="90%">
    <tr valign="top">
      <td width="110">
        <b><phenote:relationhipName beanName="parent" property="type.name" type="child"/>:</b>
      </td>
      <td>
      <c:choose>
      <c:when test="${ formBean.isExternalViewType }">
        <c:choose>
          <c:when test="${ formBean.useTerm}">
        <a href="/phenote/Phenote/?termId=<c:out value='${parent.child.ID}' />&ontologyName=<c:out value='${formBean.ontologyName}'/>&field=ENTITY&viewType=EXTERNAL&useTerm=true">
          </c:when>
          <c:otherwise>
        <a href="/phenote/Phenote/?termId=<c:out value='${parent.child.ID}' />&ontologyName=<c:out value='${formBean.ontologyName}'/>&field=ENTITY&viewType=EXTERNAL">
          </c:otherwise>
        </c:choose>
      </c:when>
      <c:otherwise>
        <a href="javascript:;"
           onclick="phenoteState.updateTermInfo(new Term('<c:out value="${parent.child.ID}" />','<c:out value="${parent.child.name}" />','<c:out value="${formBean.ontologyName}" />'));">
      </c:otherwise>
      </c:choose>
          <c:out value="${parent.child.name}"/></a>
      </td>
    </tr>
  </table>
</c:forEach>


<c:if test="${formBean.ontologyName == 'ZF'}">
  <table width="90%">
    <tr valign="top">
      <td width="110">
        <b>Start Stage:</b>
      </td>
      <td>
        <c:out value="${formBean.startStage.parent.name}" escapeXml="false"/>
      </td>
    </tr>
    <tr valign="top">
      <td width="110">
        <b>End Stage:</b>
      </td>
      <td>
        <c:out value="${formBean.endStage.parent.name}" escapeXml="false"/>
      </td>
    </tr>
  </table>
</c:if>

<p/>
<table width="90%">
  <tr valign="top">
    <td width="110">
      <b>COMMENT:</b>
    </td>
    <td>
      <c:out value="${formBean.term.comment}" escapeXml="false"/>
    </td>
  </tr>
</table>

</body>
</html>
