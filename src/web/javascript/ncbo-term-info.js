      
// This is a variable which defines the path to the ajax request processing script
// This can be changed and should point to the scipt which processes the Ajax queries
//var url  = '/usr/local/lib/apache/apache-tomcat-5.5.15/webapps/ROOT/ajax/scripts/phenote-ontology.cgi';
//var url  = '/phenote-ontology.cgi';
// term info not yet implemented in PhenoteServlet - todo...
// this should be a relative link - need to get servlets code in/close with scripts?
//var url = '/servlet/PhenoteStub'; 
//var url = '/servlet/Phenote'; // tomcat - not jboss
var url = '/phenote/Phenote'; // jboss
      

// getTermInfo should be called by the above url (at least for dichty it does)
// renaming this getTermInfo... from set_ontology,
// ontologyid -> termId

// ontologyName is the name of the ontology (not a term name!)
function getTermInfo(termId,termName,ontologyName,field) {

  // for some reason 0 doesnt work
  //termId = 123;
  
  //alert("ncbo-term-info.js set_ontology called termId: "+termId+" field "+field);
  // isNan is "is Not a Number", ontology id has to be a number
  //if ( !isNaN( termId ) ) { //var pars = 'ontologyid=' + ontologyid;
  var pars = 'termId='+termId+'&ontologyName='+ontologyName+'&field='+field;
            
  //alert("ncbo-term-info.js calling Ajax.Updater with "+pars);
  //this.debug("ncbo-term-info.js calling Ajax.Updater with "+pars);

  // ontologyinfo is the div where the table goes for term info
  // note: this is a "get" not a post! (term comp is post)
  //var myAjax = new Ajax.Updater('termInfo', url, {method: 'get', parameters: pars, onComplete: document.forms[0].qualityInput.value = '' } );
  // took out wipe out on complete
  var myAjax = new Ajax.Updater('termInfo', url, {method: 'get', parameters: pars } );
  //if ( document.forms[0].ontologyid.length > 1 ) { // dont need this
  //document.forms[0].ontologyid[1].value = termId;
  //document.forms[0].ontologyid[2].value = termId; } else {
  document.termForm.termInfoTermId.value = termId; // }
  document.termForm.termInfoTermName.value = termName;
  document.termForm.activeField.value = field;
  //} if isNan taken out
}



