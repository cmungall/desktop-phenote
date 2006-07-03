      
      // This is a variable which defines the path to the ajax request processing script
      // This can be changed and should point to the scipt which processes the Ajax queries
      //var url  = '/usr/local/lib/apache/apache-tomcat-5.5.15/webapps/ROOT/ajax/scripts/phenote-ontology.cgi';
      //var url  = '/phenote-ontology.cgi';
      // term info not yet implemented in PhenoteServlet - todo...
  var url = '/servlet/PhenoteStub'; 
      
// set_ontology should be called by the above url (at least for dichty it does)

      function set_ontology(ontologyid){

        // for some reason 0 doesnt work
        ontologyid = 123;

        //alert("phenote-control.js set_ontology called ontId isNan:"+isNaN(ontologyid)+" ontId: "+ontologyid);

        // isNan is "is Not a Number", ontology id has to be a number
         if ( !isNaN( ontologyid ) ) {      
            var pars = 'ontologyid=' + ontologyid;

            
            //alert("phenote-control.js calling Ajax.Updater with "+pars);
            //this.debug("phenote-control.js calling Ajax.Updater with "+pars);

            // ontologyinfo is the div where the table goes for term info
            // note: this is a "get" not a post! (term comp is post)
            var myAjax = new Ajax.Updater('ontologyinfo', url, {method: 'get', parameters: pars, onComplete: document.forms[0].ontologyname.value = '' } );
           // have to have [1] next to form item because the edit subform and
           // the main form both have donorid elements
            if ( document.forms[0].ontologyid.length > 1 ) {
               document.forms[0].ontologyid[1].value = ontologyid;
               document.forms[0].ontologyid[2].value = ontologyid;
            }
            else {
               document.forms[0].ontologyid.value = ontologyid;
            }
         }
      }
