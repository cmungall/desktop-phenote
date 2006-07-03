			README
--------------------------------------------------------------

This is a web widget to search ontologies dynamically using Ajax. The
following describes the various compoents of the widget and how to use it.

Style sheet: /css/control.js
This contains all the styles used in the widget

Javascript:
/javascript/dichty-term-info.js contains a method to call Ajax to
update the Ontology information in the term info panel.
In this javascript the "var url" should point to the processing script.

/javascript/ajax-lib contains Ajax libraries from Prototype
for ajax and its auto completer
http://prototype.conio.net/

Perl
/perl contains an example of a Ajax request processing script.
It is written in CGI perl.

