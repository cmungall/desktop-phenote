create table tableinfo (
    tableinfo_id serial not null,
    primary key (tableinfo_id),
    name varchar(30) not null,
    primary_key_column varchar(30) null,
    is_view int not null default 0,
    view_on_table_id int null,
    superclass_table_id int null,
    is_updateable int not null default 1,
    modification_date date not null default now(),
    constraint tableinfo_c1 unique (name)
);

COMMENT ON TABLE tableinfo IS NULL;

create table db (
    db_id serial not null,
    primary key (db_id),
    name varchar(255) not null,
--    contact_id int,
--    foreign key (contact_id) references contact (contact_id) on delete cascade INITIALLY DEFERRED,
    description varchar(255) null,
    urlprefix varchar(255) null,
    url varchar(255) null,
    uri varchar(255) null,
    constraint db_c1 unique (name)
);

COMMENT ON TABLE db IS 'A database authority. Typical dbs in
bioinformatics are FlyBase, GO, UniProt, NCBI, MGI, etc. The authority
is generally known by this sortened form, which is unique within the
bioinformatics and biomedical realm.';

COMMENT ON COLUMN db.name IS 'A (typically short, mnemonic) name of
the ID-space, database or ID-granting authority. The db.name uniquely
identifies the DB/authority. Examples include FlyBase, GO, MGI. Short
human-friendly names are encouraged, although longer names (such as
full LSID prefixes) may also be used. The name should be a valid XML
NMTOKEN (see XML specification for details) - for example, it should
not start with a number. This constraint is to help syntactic
interoperability with other identifier schemes. To ensure
interoperability with other Chado databases, the same db.names should
be used (e.g. FlyBase should be used consistently instead of FB). This
will prevent duplicate dbxref rows being created if and when databases
are merged. At the same time, uniqueness must be preserved: there must
not be two "GO"s. See supporting docs for more info';



COMMENT ON COLUMN db.description IS 'A human readable description of
this DB/authority. For example, "The model organism database for
Drosophila melanogaster"';

COMMENT ON COLUMN db.url IS 'A W3C compliant URL with the address of a
website containing information about the DB/authority. For example,
http://www.flybase.org, http://www.geneontology.org. The URL is
intended for humans rather than software agents';

COMMENT ON COLUMN db.uri IS 'A W3C compliant URI that contains a
unique namespace for the DB/authority. Some ID schemes (eg LSID)
require this. The URI is intended for software agents rather than
humans. It does not need to be a resolvable URL. However, certain DBs
may prefer the URI to be a resolvable URL that has human-readable
information on the other end. Other DBs may provide URNs (eg LSID
URNs) that require software agents to be resolved. Note that it is
perfectly acceptable for the db.name column to be the same as the URI
column (provided it is a valid URI). However, it is encouraged that a
short form is used as the db.name. See supporting docs for more information';

create table dbxref (
    dbxref_id serial not null,
    primary key (dbxref_id),
    db_id int not null,
    foreign key (db_id) references db (db_id) on delete cascade INITIALLY DEFERRED,
    accession varchar(255) not null,
    version varchar(255) not null default '',
    description text,
    constraint dbxref_c1 unique (db_id,accession,version)
);
create index dbxref_idx1 on dbxref (db_id);
create index dbxref_idx2 on dbxref (accession);
create index dbxref_idx3 on dbxref (version);

COMMENT ON TABLE dbxref IS 'A unique, global, public, stable identifier. Not necessarily an eXternal reference - can reference data items inside the particular chado instance being used. Typically a row in a table can be uniquely identified with a primary identifier (called dbxref_id); a table may also have secondary identifiers (in a linking table <T>_dbxref). A dbxref is generally written as <DB>:<ACCESSION> or as <DB>:<ACCESSION>.the ID-spa<VERSION>. ';

COMMENT ON COLUMN dbxref.accession IS 'The local part of the identifier. Guaranteed by the db authority to be unique for that db.';

-- ================================================
-- TABLE: project
-- ================================================
create table project (
    project_id serial not null,  
    primary key (project_id),
    name varchar(255) not null,
    description varchar(255) not null,
    constraint project_c1 unique (name)
);

COMMENT ON TABLE project IS NULL;

create table cv (
    cv_id serial not null,
    primary key (cv_id),
    name varchar(255) not null,
   definition text,
   constraint cv_c1 unique (name)
);

COMMENT ON TABLE cv IS 'A controlled vocabulary or ontology. A cv is
composed of cvterms (aka terms, classes, types, universals - relations
and properties are also stored in cvterm)) and the relationships
between them';

COMMENT ON COLUMN cv.name IS 'The name of the ontology. This
corresponds to the obo-format -namespace-. cv names uniquely identify
the cv. In obo file format, the cv.name is known as the namespace';

COMMENT ON COLUMN cv.definition IS 'A text description of the criteria for
membership of this ontology';


create table cvterm (
    cvterm_id serial not null,
    primary key (cvterm_id),
    cv_id int not null,
    foreign key (cv_id) references cv (cv_id) on delete cascade INITIALLY DEFERRED,
    name varchar(1024),
    definition text,
    dbxref_id int not null,
    foreign key (dbxref_id) references dbxref (dbxref_id) on delete set null INITIALLY DEFERRED,
    is_obsolete int not null default 0,
    is_instance int not null default 0,
    is_relationshiptype int not null default 0,
    constraint cvterm_c1 unique (name,cv_id,is_obsolete),
    constraint cvterm_c2 unique (dbxref_id)
);

COMMENT ON TABLE cvterm IS 'A term, class, universal or type within an
ontology or controlled vocabulary.  This table is also used for
relations and properties. cvterms constitute nodes in the graph
defined by the collection of cvterms and cvterm_relationships';

COMMENT ON COLUMN cvterm.cv_id IS 'The cv/ontology/namespace to which
this cvterm belongs';

COMMENT ON COLUMN cvterm.name IS 'A concise human-readable name or
label for the cvterm. uniquely identifies a cvterm within a cv';

COMMENT ON COLUMN cvterm.definition IS 'A human-readable text
definition';

COMMENT ON COLUMN cvterm.dbxref_id IS 'Primary identifier dbxref - The
unique global OBO identifier for this cvterm.  Note that a cvterm may
have multiple secondary dbxrefs - see also table: cvterm_dbxref';

COMMENT ON COLUMN cvterm.is_obsolete IS 'Boolean 0=false,1=true; see
GO documentation for details of obsoletion.  note that two terms with
different primary dbxrefs may exist if one is obsolete';

COMMENT ON COLUMN cvterm.is_relationshiptype IS 'Boolean
0=false,1=true relations or relationship types (also known as Typedefs
in OBO format, or as properties or slots) form a cv/ontology in
themselves. We use this flag to indicate whether this cvterm is an
actual term/class/universal or a relation. Relations may be drawn from
the OBO Relations ontology, but are not exclusively drawn from there';

COMMENT ON INDEX cvterm_c1 IS 'a name can mean different things in
different contexts; for example "chromosome" in SO and GO. A name
should be unique within an ontology/cv. A name may exist twice in a
cv, in both obsolete and non-obsolete forms - these will be for
different cvterms with different OBO identifiers; so GO documentation
for more details on obsoletion. Note that occasionally multiple
obsolete terms with the same name will exist in the same cv. If this
is a possibility for the ontology under consideration (eg GO) then the
ID should be appended to the name to ensure uniqueness';

COMMENT ON INDEX cvterm_c2 IS 'the OBO identifier is globally unique';

create index cvterm_idx1 on cvterm (cv_id);
create index cvterm_idx2 on cvterm (name);
create index cvterm_idx3 on cvterm (dbxref_id);

create table cvterm_relationship (
    cvterm_relationship_id serial not null,
    primary key (cvterm_relationship_id),
    type_id int not null,
    foreign key (type_id) references cvterm (cvterm_id) on delete cascade INITIALLY DEFERRED,
    subject_id int not null,
    foreign key (subject_id) references cvterm (cvterm_id) on delete cascade INITIALLY DEFERRED,
    object_id int not null,
    foreign key (object_id) references cvterm (cvterm_id) on delete cascade INITIALLY DEFERRED,
    constraint cvterm_relationship_c1 unique (subject_id,object_id,type_id)
);

COMMENT ON TABLE cvterm_relationship IS 'A relationship linking two
cvterms. Each cvterm_relationship constitutes an edge in the graph
defined by the collection of cvterms and cvterm_relationships. The
meaning of the cvterm_relationship depends on the definition of the
cvterm R refered to by type_id. However, in general the definitions
are such that the statement all SUBJs REL some OBJ is true. The
cvterm_relationship statement is about the subject, not the
object. For example "insect wing part_of thorax"';

COMMENT ON COLUMN cvterm_relationship.subject_id IS 'the subject of
the subj-predicate-obj sentence. The cvterm_relationship is about the
subject. In a graph, this typically corresponds to the child node';

COMMENT ON COLUMN cvterm_relationship.object_id IS 'the object of the
subj-predicate-obj sentence. The cvterm_relationship refers to the
object. In a graph, this typically corresponds to the parent node';

COMMENT ON COLUMN cvterm_relationship.type_id IS 'The nature of the
relationship between subject and object. Note that relations are also
housed in the cvterm table, typically from the OBO relationship
ontology, although other relationship types are allowed';

create index cvterm_relationship_idx1 on cvterm_relationship (type_id);
create index cvterm_relationship_idx2 on cvterm_relationship (subject_id);
create index cvterm_relationship_idx3 on cvterm_relationship (object_id);

create table cvtermpath (
    cvtermpath_id serial not null,
    primary key (cvtermpath_id),
    type_id int,
    foreign key (type_id) references cvterm (cvterm_id) on delete set null INITIALLY DEFERRED,
    subject_id int not null,
    foreign key (subject_id) references cvterm (cvterm_id) on delete cascade INITIALLY DEFERRED,
    object_id int not null,
    foreign key (object_id) references cvterm (cvterm_id) on delete cascade INITIALLY DEFERRED,
    cv_id int not null,
    foreign key (cv_id) references cv (cv_id) on delete cascade INITIALLY DEFERRED,
    pathdistance int,
    constraint cvtermpath_c1 unique (subject_id,object_id,type_id,pathdistance)
);

COMMENT ON TABLE cvtermpath IS 'The reflexive transitive closure of
the cvterm_relationship relation. For a full discussion, see the file
populating-cvtermpath.txt in this directory';

COMMENT ON COLUMN cvtermpath.type_id IS 'The relationship type that
this is a closure over. If null, then this is a closure over ALL
relationship types. If non-null, then this references a relationship
cvterm - note that the closure will apply to both this relationship
AND the OBO_REL:is_a (subclass) relationship';

COMMENT ON COLUMN cvtermpath.cv_id IS 'Closures will mostly be within
one cv. If the closure of a relationship traverses a cv, then this
refers to the cv of the object_id cvterm';

COMMENT ON COLUMN cvtermpath.pathdistance IS 'The number of steps
required to get from the subject cvterm to the object cvterm, counting
from zero (reflexive relationship)';

create index cvtermpath_idx1 on cvtermpath (type_id);
create index cvtermpath_idx2 on cvtermpath (subject_id);
create index cvtermpath_idx3 on cvtermpath (object_id);
create index cvtermpath_idx4 on cvtermpath (cv_id);

create table cvtermsynonym (
    cvtermsynonym_id serial not null,
    primary key (cvtermsynonym_id),
    cvterm_id int not null,
    foreign key (cvterm_id) references cvterm (cvterm_id) on delete cascade INITIALLY DEFERRED,
    synonym varchar(1024) not null,
    type_id int,
    foreign key (type_id) references cvterm (cvterm_id) on delete cascade  INITIALLY DEFERRED,
    constraint cvtermsynonym_c1 unique (cvterm_id,synonym)
);

COMMENT ON TABLE cvtermsynonym IS 'A cvterm actually represents a
distinct class or concept. A concept can be refered to by different
phrases or names. In addition to the primary name (cvterm.name) there
can be a number of alternative aliases or synonyms. For example, -T
cell- as a synonym for -T lymphocyte-';

COMMENT ON COLUMN cvtermsynonym.type_id IS 'A synonym can be exact,
narrow or borader than';

create index cvtermsynonym_idx1 on cvtermsynonym (cvterm_id);


create table cvterm_dbxref (
    cvterm_dbxref_id serial not null,
    primary key (cvterm_dbxref_id),
    cvterm_id int not null,
    foreign key (cvterm_id) references cvterm (cvterm_id) on delete cascade INITIALLY DEFERRED,
    dbxref_id int not null,
    foreign key (dbxref_id) references dbxref (dbxref_id) on delete cascade INITIALLY DEFERRED,
    is_for_definition int not null default 0,
    constraint cvterm_dbxref_c1 unique (cvterm_id,dbxref_id)
);

COMMENT ON TABLE cvterm_dbxref IS 'In addition to the primary
identifier (cvterm.dbxref_id) a cvterm can have zero or more secondary
identifiers/dbxrefs, which may refer to records in external
databases. The exact semantics of cvterm_dbxref are not fixed. For
example: the dbxref could be a pubmed ID that is pertinent to the
cvterm, or it could be an equivalent or similar term in another
ontology. For example, GO cvterms are typically linked to InterPro
IDs, even though the nature of the relationship between them is
largely one of statistical association. The dbxref may be have data
records attached in the same database instance, or it could be a
"hanging" dbxref pointing to some external database. NOTE: If the
desired objective is to link two cvterms together, and the nature of
the relation is known and holds for all instances of the subject
cvterm then consider instead using cvterm_relationship together with a
well-defined relation.';

COMMENT ON COLUMN cvterm_dbxref.is_for_definition IS 'A
cvterm.definition should be supported by one or more references. If
this column is true, the dbxref is not for a term in an external db -
it is a dbxref for provenance information for the definition';

create index cvterm_dbxref_idx1 on cvterm_dbxref (cvterm_id);
create index cvterm_dbxref_idx2 on cvterm_dbxref (dbxref_id);


create table cvtermprop ( 
    cvtermprop_id serial not null, 
    primary key (cvtermprop_id), 
    cvterm_id int not null, 
    foreign key (cvterm_id) references cvterm (cvterm_id) on delete cascade, 
    type_id int not null, 
    foreign key (type_id) references cvterm (cvterm_id) on delete cascade, 
    value text not null default '', 
    rank int not null default 0,

    unique(cvterm_id, type_id, value, rank) 
);

COMMENT ON TABLE cvtermprop IS 'Additional extensible properties can be attached to a cvterm using this table. Corresponds to -AnnotationProperty- in W3C OWL format';

COMMENT ON COLUMN cvtermprop.type_id IS 'The name of the property/slot is a cvterm. The meaning of the property is defined in that cvterm';

COMMENT ON COLUMN cvtermprop.value IS 'The value of the property, represented as text. Numeric values are converted to their text representation';

COMMENT ON COLUMN cvtermprop.rank IS 'Property-Value ordering. Any
cvterm can have multiple values for any particular property type -
these are ordered in a list using rank, counting from zero. For
properties that are single-valued rather than multi-valued, the
default 0 value should be used';

create index cvtermprop_idx1 on cvtermprop (cvterm_id);
create index cvtermprop_idx2 on cvtermprop (type_id);


create table dbxrefprop (
    dbxrefprop_id serial not null,
    primary key (dbxrefprop_id),
    dbxref_id int not null,
    foreign key (dbxref_id) references dbxref (dbxref_id) INITIALLY DEFERRED,
    type_id int not null,
    foreign key (type_id) references cvterm (cvterm_id) INITIALLY DEFERRED,
    value text not null default '',
    rank int not null default 0,
    constraint dbxrefprop_c1 unique (dbxref_id,type_id,rank)
);

COMMENT ON TABLE dbxrefprop IS 'Metadata about a dbxref. Note that this is not defined in the dbxref module, as it depends on the cvterm table. This table has a structure analagous to cvtermprop';

create index dbxrefprop_idx1 on dbxrefprop (dbxref_id);
create index dbxrefprop_idx2 on dbxrefprop (type_id);

CREATE OR REPLACE VIEW cv_root AS
 SELECT 
  cv_id,
  cvterm_id AS root_cvterm_id
 FROM cvterm
 WHERE 
  cvterm_id NOT IN ( SELECT subject_id FROM cvterm_relationship)    AND
  is_obsolete=0;

COMMENT ON VIEW cv_root IS 'the roots of a cv are the set of terms
which have no parents (terms that are not the subject of a
relation). Most cvs will have a single root, some may have >1. All
will have at least 1';

CREATE OR REPLACE VIEW cv_leaf AS
 SELECT 
  cv_id,
  cvterm_id
 FROM cvterm
 WHERE 
  cvterm_id NOT IN ( SELECT object_id FROM cvterm_relationship);

COMMENT ON VIEW cv_leaf IS 'the leaves of a cv are the set of terms
which have no children (terms that are not the object of a
relation). All cvs will have at least 1 leaf';

CREATE OR REPLACE VIEW common_ancestor_cvterm AS
 SELECT
  p1.subject_id          AS cvterm1_id,
  p2.subject_id          AS cvterm2_id,
  p1.object_id           AS ancestor_cvterm_id,
  p1.pathdistance        AS pathdistance1,
  p2.pathdistance        AS pathdistance2,
  p1.pathdistance + p2.pathdistance
                         AS total_pathdistance
 FROM
  cvtermpath AS p1,
  cvtermpath AS p2
 WHERE 
  p1.object_id = p2.object_id;

COMMENT ON VIEW common_ancestor_cvterm IS 'The common ancestor of any
two terms is the intersection of both terms ancestors. Two terms can
have multiple common ancestors. Use total_pathdistance to get the
least common ancestor';

CREATE OR REPLACE VIEW common_descendant_cvterm AS
 SELECT
  p1.object_id           AS cvterm1_id,
  p2.object_id           AS cvterm2_id,
  p1.subject_id          AS ancestor_cvterm_id,
  p1.pathdistance        AS pathdistance1,
  p2.pathdistance        AS pathdistance2,
  p1.pathdistance + p2.pathdistance
                         AS total_pathdistance
 FROM
  cvtermpath AS p1,
  cvtermpath AS p2
 WHERE 
  p1.subject_id = p2.subject_id;

COMMENT ON VIEW common_descendant_cvterm IS 'The common descendant of
any two terms is the intersection of both terms descendants. Two terms
can have multiple common descendants. Use total_pathdistance to get
the least common ancestor';

CREATE OR REPLACE VIEW stats_paths_to_root AS
 SELECT 
  subject_id                            AS cvterm_id, 
  count(DISTINCT cvtermpath_id)         AS total_paths,
  avg(pathdistance)                     AS avg_distance,
  min(pathdistance)                     AS min_distance,
  max(pathdistance)                     AS max_distance
 FROM cvtermpath INNER JOIN cv_root ON (object_id=root_cvterm_id)
 GROUP BY cvterm_id;

COMMENT ON VIEW stats_paths_to_root IS 'per-cvterm statistics on its
placement in the DAG relative to the root. There may be multiple paths
from any term to the root. This gives the total number of paths, and
the average minimum and maximum distances. Here distance is defined by
cvtermpath.pathdistance';

create table pub (
    pub_id serial not null,
    primary key (pub_id),
    title text,
    volumetitle text,
    volume varchar(255),
    series_name varchar(255),
    issue varchar(255),
    pyear varchar(255),
    pages varchar(255),
    miniref varchar(255),
    uniquename text not null,
    type_id int not null,
    foreign key (type_id) references cvterm (cvterm_id) on delete cascade INITIALLY DEFERRED,
    is_obsolete boolean default 'false',
    publisher varchar(255),
    pubplace varchar(255),
    constraint pub_c1 unique (uniquename)
);

COMMENT ON TABLE pub IS 'A documented provenance artefact - publications,
documents, personal communication';

COMMENT ON COLUMN pub.title IS 'descriptive general heading';
COMMENT ON COLUMN pub.volumetitle IS 'title of part if one of a series';
COMMENT ON COLUMN pub.series_name IS 'full name of (journal) series';
COMMENT ON COLUMN pub.pages IS 'page number range[s], eg, 457--459, viii + 664pp, lv--lvii';
COMMENT ON COLUMN pub.type_id IS  'the type of the publication (book, journal, poem, graffiti, etc). Uses pub cv';
CREATE INDEX pub_idx1 ON pub (type_id);

create table pub_relationship (
    pub_relationship_id serial not null,
    primary key (pub_relationship_id),
    subject_id int not null,
    foreign key (subject_id) references pub (pub_id) on delete cascade INITIALLY DEFERRED,
    object_id int not null,
    foreign key (object_id) references pub (pub_id) on delete cascade INITIALLY DEFERRED,
    type_id int not null,
    foreign key (type_id) references cvterm (cvterm_id) on delete cascade INITIALLY DEFERRED,

    constraint pub_relationship_c1 unique (subject_id,object_id,type_id)
);
COMMENT ON TABLE pub_relationship IS 'Handle relationships between
publications, eg, when one publication makes others obsolete, when one
publication contains errata with respect to other publication(s), or
when one publication also appears in another pub (I think these three
are it - at least for fb)';


create index pub_relationship_idx1 on pub_relationship (subject_id);
create index pub_relationship_idx2 on pub_relationship (object_id);
create index pub_relationship_idx3 on pub_relationship (type_id);

create table pub_dbxref (
    pub_dbxref_id serial not null,
    primary key (pub_dbxref_id),
    pub_id int not null,
    foreign key (pub_id) references pub (pub_id) on delete cascade INITIALLY DEFERRED,
    dbxref_id int not null,
    foreign key (dbxref_id) references dbxref (dbxref_id) on delete cascade INITIALLY DEFERRED,
    is_current boolean not null default 'true',
    constraint pub_dbxref_c1 unique (pub_id,dbxref_id)
);
create index pub_dbxref_idx1 on pub_dbxref (pub_id);
create index pub_dbxref_idx2 on pub_dbxref (dbxref_id);

COMMENT ON TABLE pub_dbxref IS 'Handle links to eg, pubmed, biosis,
zoorec, OCLC, mdeline, ISSN, coden...';



create table pubauthor (
    pubauthor_id serial not null,
    primary key (pubauthor_id),
    pub_id int not null,
    foreign key (pub_id) references pub (pub_id) on delete cascade INITIALLY DEFERRED,
    rank int not null,
    editor boolean default 'false',
    surname varchar(100) not null,
    givennames varchar(100),
    suffix varchar(100),

    constraint pubauthor_c1 unique (pub_id, rank)
);

COMMENT ON TABLE pubauthor IS 'an author for a publication. Note the denormalisation (hence lack of _ in table name) - this is deliberate as it is in general too hard to assign IDs to authors.';

COMMENT ON COLUMN pubauthor.givennames IS 'first name, initials';
COMMENT ON COLUMN pubauthor.suffix IS 'Jr., Sr., etc';
COMMENT ON COLUMN pubauthor.rank IS 'order of author in author list for this pub - order is important';

COMMENT ON COLUMN pubauthor.editor IS 'indicates whether the author is an editor for linked publication. Note: this is a boolean field but does not follow the normal chado convention for naming booleans';

create index pubauthor_idx2 on pubauthor (pub_id);

create table pubprop (
    pubprop_id serial not null,
    primary key (pubprop_id),
    pub_id int not null,
    foreign key (pub_id) references pub (pub_id) on delete cascade INITIALLY DEFERRED,
    type_id int not null,
    foreign key (type_id) references cvterm (cvterm_id) on delete cascade INITIALLY DEFERRED,
    value text not null,
    rank integer,

    constraint pubprop_c1 unique (pub_id,type_id,rank)
);

COMMENT ON TABLE pubprop IS 'Property-value pairs for a pub. Follows standard chado pattern - see sequence module for details';

create index pubprop_idx1 on pubprop (pub_id);
create index pubprop_idx2 on pubprop (type_id);

create table organism (
	organism_id serial not null,
	primary key (organism_id),
	abbreviation varchar(255) null,
	genus varchar(255) not null,
	species varchar(255) not null,
	common_name varchar(255) null,
	comment text null,
    constraint organism_c1 unique (genus,species)
);

COMMENT ON TABLE organism IS 'The organismal taxonomic
classification. Note that phylogenies are represented using the
phylogeny module, and taxonomies can be represented using the cvterm
module or the phylogeny module';

COMMENT ON COLUMN organism.species IS 'A type of organism is always
uniquely identified by genus+species. When mapping from the NCBI
taxonomy names.dmp file, the unique-name column must be used where it
is present, as the name column is not always unique (eg environmental
samples). If a particular strain or subspecies is to be represented,
this is appended onto the species name. Follows standard NCBI taxonomy
pattern';

create table organism_dbxref (
    organism_dbxref_id serial not null,
    primary key (organism_dbxref_id),
    organism_id int not null,
    foreign key (organism_id) references organism (organism_id) on delete cascade INITIALLY DEFERRED,
    dbxref_id int not null,
    foreign key (dbxref_id) references dbxref (dbxref_id) on delete cascade INITIALLY DEFERRED,
    constraint organism_dbxref_c1 unique (organism_id,dbxref_id)
);
create index organism_dbxref_idx1 on organism_dbxref (organism_id);
create index organism_dbxref_idx2 on organism_dbxref (dbxref_id);

create table organismprop (
    organismprop_id serial not null,
    primary key (organismprop_id),
    organism_id int not null,
    foreign key (organism_id) references organism (organism_id) on delete cascade INITIALLY DEFERRED,
    type_id int not null,
    foreign key (type_id) references cvterm (cvterm_id) on delete cascade INITIALLY DEFERRED,
    value text null,
    rank int not null default 0,
    constraint organismprop_c1 unique (organism_id,type_id,rank)
);
create index organismprop_idx1 on organismprop (organism_id);
create index organismprop_idx2 on organismprop (type_id);

COMMENT ON TABLE organismprop IS 'tag-value properties - follows standard chado model';

-- ================================================
-- TABLE: organism_relationship
-- ================================================

CREATE TABLE organism_relationship (
    organism_relationship_id serial not null,
    PRIMARY KEY (organism_relationship_id),
    subject_id int not null,
    FOREIGN KEY (subject_id) REFERENCES organism (organism_id) INITIALLY DEFERRED,
    object_id int not null,
    FOREIGN KEY (object_id) REFERENCES organism (organism_id) INITIALLY DEFERRED,
    type_id int not null,
    FOREIGN KEY (type_id) REFERENCES cvterm (cvterm_id) INITIALLY DEFERRED,
    CONSTRAINT organism_relationship_c1 UNIQUE (subject_id, object_id, type_id)
);
CREATE INDEX organism_relationship_idx1 ON organism_relationship (subject_id);
CREATE INDEX organism_relationship_idx2 ON organism_relationship (object_id);
CREATE INDEX organism_relationship_idx3 ON organism_relationship (type_id);

-- ================================================
-- TABLE: organismpath
-- ================================================

CREATE TABLE organismpath (
    organismpath_id serial not null,
    PRIMARY KEY (organismpath_id),
    subject_id int not null,
    FOREIGN KEY (subject_id) REFERENCES organism (organism_id) INITIALLY DEFERRED,
    object_id int not null,
    FOREIGN KEY (object_id) REFERENCES organism (organism_id) INITIALLY DEFERRED,
    type_id int not null,
    FOREIGN KEY (type_id) REFERENCES cvterm (cvterm_id) INITIALLY DEFERRED,
    pathdistance int,
    CONSTRAINT organismpath_c1 UNIQUE (subject_id,object_id,type_id,pathdistance)
);
CREATE INDEX organismpath_idx1 ON organismpath (type_id);
CREATE INDEX organismpath_idx2 ON organismpath (subject_id);
CREATE INDEX organismpath_idx3 ON organismpath (object_id);


create table feature (
    feature_id serial not null,
    primary key (feature_id),
    dbxref_id int,
    foreign key (dbxref_id) references dbxref (dbxref_id) on delete set null INITIALLY DEFERRED,
    organism_id int not null,
    foreign key (organism_id) references organism (organism_id) on delete cascade INITIALLY DEFERRED,
    name varchar(255),
    uniquename text not null,
    residues text,
    seqlen int,
    md5checksum char(32),
    type_id int not null,
    foreign key (type_id) references cvterm (cvterm_id) on delete cascade INITIALLY DEFERRED,
    is_analysis boolean not null default 'false',
    is_obsolete boolean not null default 'false',
    timeaccessioned timestamp not null default current_timestamp,
    timelastmodified timestamp not null default current_timestamp,
    constraint feature_c1 unique (organism_id,uniquename,type_id)
);

COMMENT ON TABLE feature IS 'A feature is a biological sequence or a
section of a biological sequence, or a collection of such
sections. Examples include genes, exons, transcripts, regulatory
regions, polypeptides, protein domains, chromosome sequences, sequence
variations, cross-genome match regions such as hits and HSPs and so
on; see the Sequence Ontology for more';

COMMENT ON COLUMN feature.dbxref_id IS 'An optional primary public stable
identifier for this feature. Secondary identifiers and external
dbxrefs go in table:feature_dbxref';

COMMENT ON COLUMN feature.organism_id IS 'The organism to which this feature
belongs. This column is mandatory';

COMMENT ON COLUMN feature.name IS 'The optional human-readable common name for
a feature, for display purposes';

COMMENT ON COLUMN feature.uniquename IS 'The unique name for a feature; may
not be necessarily be particularly human-readable, although this is
prefered. This name must be unique for this type of feature within
this organism';

COMMENT ON COLUMN feature.residues IS 'A sequence of alphabetic characters
representing biological residues (nucleic acids, amino acids). This
column does not need to be manifested for all features; it is optional
for features such as exons where the residues can be derived from the
featureloc. It is recommended that the value for this column be
manifested for features which may may non-contiguous sublocations (eg
transcripts), since derivation at query time is non-trivial. For
expressed sequence, the DNA sequence should be used rather than the
RNA sequence';

COMMENT ON COLUMN feature.seqlen IS 'The length of the residue feature. See
column:residues. This column is partially redundant with the residues
column, and also with featureloc. This column is required because the
location may be unknown and the residue sequence may not be
manifested, yet it may be desirable to store and query the length of
the feature. The seqlen should always be manifested where the length
of the sequence is known';

COMMENT ON COLUMN feature.md5checksum IS 'The 32-character checksum of the sequence,
calculated using the MD5 algorithm. This is practically guaranteed to
be unique for any feature. This column thus acts as a unique
identifier on the mathematical sequence';

COMMENT ON COLUMN feature.type_id IS 'A required reference to a table:cvterm
giving the feature type. This will typically be a Sequence Ontology
identifier. This column is thus used to subclass the feature table';

COMMENT ON COLUMN feature.is_analysis IS 'Boolean indicating whether this
feature is annotated or the result of an automated analysis. Analysis
results also use the companalysis module. Note that the dividing line
between analysis/annotation may be fuzzy, this should be determined on
a per-project basis in a consistent manner. One requirement is that
there should only be one non-analysis version of each wild-type gene
feature in a genome, whereas the same gene feature can be predicted
multiple times in different analyses';

COMMENT ON COLUMN feature.is_obsolete IS 'Boolean indicating whether this
feature has been obsoleted. Some chado instances may choose to simply
remove the feature altogether, others may choose to keep an obsolete
row in the table';

COMMENT ON COLUMN feature.timeaccessioned IS 'for handling object
accession/modification timestamps (as opposed to db auditing info,
handled elsewhere). The expectation is that these fields would be
available to software interacting with chado';

COMMENT ON COLUMN feature.timelastmodified IS 'for handling object
accession/modification timestamps (as opposed to db auditing info,
handled elsewhere). The expectation is that these fields would be
available to software interacting with chado';

--- COMMENT ON INDEX feature_c1 IS 'Any feature can be globally identified
--- by the combination of organism, uniquename and feature type';

create sequence feature_uniquename_seq;
create index feature_name_ind1 on feature(name);
create index feature_idx1 on feature (dbxref_id);
create index feature_idx2 on feature (organism_id);
create index feature_idx3 on feature (type_id);
create index feature_idx4 on feature (uniquename);
create index feature_idx5 on feature (lower(name));


create table featureloc (
    featureloc_id serial not null,
    primary key (featureloc_id),
    feature_id int not null,
    foreign key (feature_id) references feature (feature_id) on delete cascade INITIALLY DEFERRED,
    srcfeature_id int,
    foreign key (srcfeature_id) references feature (feature_id) on delete set null INITIALLY DEFERRED,
    fmin int,
    is_fmin_partial boolean not null default 'false',
    fmax int,
    is_fmax_partial boolean not null default 'false',
    strand smallint,
    phase int,
    residue_info text,
    locgroup int not null default 0,
    rank int not null default 0,
    constraint featureloc_c1 unique (feature_id,locgroup,rank),
    constraint featureloc_c2 check (fmin <= fmax)
);

COMMENT ON TABLE featureloc IS 'The location of a feature relative to
another feature.  IMPORTANT: INTERBASE COORDINATES ARE USED.(This is
vital as it allows us to represent zero-length features eg splice
sites, insertion points without an awkward fuzzy system). Features
typically have exactly ONE location, but this need not be the
case. Some features may not be localized (eg a gene that has been
characterized genetically but no sequence/molecular info is
available). NOTE ON MULTIPLE LOCATIONS: Each feature can have 0 or
more locations. Multiple locations do NOT indicate non-contiguous
locations (if a feature such as a transcript has a non-contiguous
location, then the subfeatures such as exons should always be
manifested). Instead, multiple featurelocs for a feature designate
alternate locations or grouped locations; for instance, a feature
designating a blast hit or hsp will have two locations, one on the
query feature, one on the subject feature.  features representing
sequence variation could have alternate locations instantiated on a
feature on the mutant strain.  the column:rank is used to
differentiate these different locations. Reflexive locations should
never be stored - this is for -proper- (ie non-self) locations only;
i.e. nothing should be located relative to itself';

COMMENT ON COLUMN featureloc.feature_id IS 'The feature that is being located. Any feature can have zero or more featurelocs';

COMMENT ON COLUMN featureloc.srcfeature_id IS 'The source feature which this location is relative to. Every location is relative to another feature (however, this column is nullable, because the srcfeature may not be known). All locations are -proper- that is, nothing should be located relative to itself. No cycles are allowed in the featureloc graph';

COMMENT ON COLUMN featureloc.fmin IS 'The leftmost/minimal boundary in the linear range represented by the featureloc. Sometimes (eg in bioperl) this is called -start- although this is confusing because it does not necessarily represent the 5-prime coordinate. IMPORTANT: This is space-based (INTERBASE) coordinates, counting from zero. To convert this to the leftmost position in a base-oriented system (eg GFF, bioperl), add 1 to fmin';

COMMENT ON COLUMN featureloc.fmax IS 'The rightmost/maximal boundary in the linear range represented by the featureloc. Sometimes (eg in bioperl) this is called -end- although this is confusing because it does not necessarily represent the 3-prime coordinate. IMPORTANT: This is space-based (INTERBASE) coordinates, counting from zero. No conversion is required to go from fmax to the rightmost coordinate in a base-oriented system that counts from 1 (eg GFF, bioperl)';

COMMENT ON COLUMN featureloc.strand IS 'The orientation/directionality of the
location. Should be 0,-1 or +1';

COMMENT ON COLUMN featureloc.rank IS 'Used when a feature has >1
location, otherwise the default rank 0 is used. Some features (eg
blast hits and HSPs) have two locations - one on the query and one on
the subject. Rank is used to differentiate these. Rank=0 is always
used for the query, Rank=1 for the subject. For multiple alignments,
assignment of rank is arbitrary. Rank is also used for
sequence_variant features, such as SNPs. Rank=0 indicates the wildtype
(or baseline) feature, Rank=1 indicates the mutant (or compared) feature';

COMMENT ON COLUMN featureloc.locgroup IS 'This is used to manifest redundant,
derivable extra locations for a feature. The default locgroup=0 is
used for the DIRECT location of a feature. !! MOST CHADO USERS MAY
NEVER USE featurelocs WITH logroup>0 !! Transitively derived locations
are indicated with locgroup>0. For example, the position of an exon on
a BAC and in global chromosome coordinates. This column is used to
differentiate these groupings of locations. the default locgroup 0
is used for the main/primary location, from which the others can be
derived via coordinate transformations. another example of redundant
locations is storing ORF coordinates relative to both transcript and
genome. redundant locations open the possibility of the database
getting into inconsistent states; this schema gives us the flexibility
of both warehouse instantiations with redundant locations (easier for
querying) and management instantiations with no redundant
locations. An example of using both locgroup and rank: imagine a
feature indicating a conserved region between the chromosomes of two
different species. we may want to keep redundant locations on both
contigs and chromosomes. we would thus have 4 locations for the single
conserved region feature - two distinct locgroups (contig level and
chromosome level) and two distinct ranks (for the two species)';

COMMENT ON COLUMN featureloc.residue_info IS 'Alternative residues,
when these differ from feature.residues. for instance, a SNP feature
located on a wild and mutant protein would have different alresidues.
for alignment/similarity features, the altresidues is used to
represent the alignment string (CIGAR format). Note on variation
features; even if we dont want to instantiate a mutant
chromosome/contig feature, we can still represent a SNP etc with 2
locations, one (rank 0) on the genome, the other (rank 1) would have
most fields null, except for altresidues';

COMMENT ON COLUMN featureloc.phase IS 'phase of translation wrt srcfeature_id.
Values are 0,1,2. It may not be possible to manifest this column for
some features such as exons, because the phase is dependant on the
spliceform (the same exon can appear in multiple spliceforms). This column is mostly useful for predicted exons and CDSs';

COMMENT ON COLUMN featureloc.is_fmin_partial IS 'This is typically
false, but may be true if the value for column:fmin is inaccurate or
the leftmost part of the range is unknown/unbounded';

COMMENT ON COLUMN featureloc.is_fmax_partial IS 'This is typically
false, but may be true if the value for column:fmax is inaccurate or
the rightmost part of the range is unknown/unbounded';

--- COMMENT ON INDEX featureloc_c1 IS 'locgroup and rank serve to uniquely
--- partition locations for any one feature';


create index featureloc_idx1 on featureloc (feature_id);
create index featureloc_idx2 on featureloc (srcfeature_id);
create index featureloc_idx3 on featureloc (srcfeature_id,fmin,fmax);

--

create table featureloc_pub (
    featureloc_pub_id serial not null,
    primary key (featureloc_pub_id),
    featureloc_id int not null,
    foreign key (featureloc_id) references featureloc (featureloc_id) on delete cascade INITIALLY DEFERRED,
    pub_id int not null,
    foreign key (pub_id) references pub (pub_id) on delete cascade INITIALLY DEFERRED,
    constraint featureloc_pub_c1 unique (featureloc_id,pub_id)
);
COMMENT ON TABLE featureloc_pub IS 'Provenance of featureloc. Linking table between featurelocs and publications that mention them';

create index featureloc_pub_idx1 on featureloc_pub (featureloc_id);
create index featureloc_pub_idx2 on featureloc_pub (pub_id);

--

create table feature_pub (
    feature_pub_id serial not null,
    primary key (feature_pub_id),
    feature_id int not null,
    foreign key (feature_id) references feature (feature_id) on delete cascade INITIALLY DEFERRED,
    pub_id int not null,
    foreign key (pub_id) references pub (pub_id) on delete cascade INITIALLY DEFERRED,
    constraint feature_pub_c1 unique (feature_id,pub_id)
);
COMMENT ON TABLE feature_pub IS 'Provenance. Linking table between features and publications that mention them';

create index feature_pub_idx1 on feature_pub (feature_id);
create index feature_pub_idx2 on feature_pub (pub_id);

--

create table featureprop (
    featureprop_id serial not null,
    primary key (featureprop_id),
    feature_id int not null,
    foreign key (feature_id) references feature (feature_id) on delete cascade INITIALLY DEFERRED,
    type_id int not null,
    foreign key (type_id) references cvterm (cvterm_id) on delete cascade INITIALLY DEFERRED,
    value text null,
    rank int not null default 0,
    constraint featureprop_c1 unique (feature_id,type_id,rank)
);
COMMENT ON TABLE featureprop IS 'A feature can have any number of slot-value property tags attached to it. This is an alternative to hardcoding a list of columns in the relational schema, and is completely extensible';

COMMENT ON COLUMN featureprop.type_id IS 'The name of the
property/slot is a cvterm. The meaning of the property is defined in
that cvterm. Certain property types will only apply to certain feature
types (e.g. the anticodon property will only apply to tRNA features) ;
the types here come from the sequence feature property ontology';

COMMENT ON COLUMN featureprop.value IS 'The value of the property, represented as text. Numeric values are converted to their text representation. This is less efficient than using native database types, but is easier to query.';

COMMENT ON COLUMN featureprop.rank IS 'Property-Value ordering. Any
feature can have multiple values for any particular property type -
these are ordered in a list using rank, counting from zero. For
properties that are single-valued rather than multi-valued, the
default 0 value should be used';

COMMENT ON INDEX featureprop_c1 IS 'for any one feature, multivalued
property-value pairs must be differentiated by rank';

create index featureprop_idx1 on featureprop (feature_id);
create index featureprop_idx2 on featureprop (type_id);

--

create table featureprop_pub (
    featureprop_pub_id serial not null,
    primary key (featureprop_pub_id),
    featureprop_id int not null,
    foreign key (featureprop_id) references featureprop (featureprop_id) on delete cascade INITIALLY DEFERRED,
    pub_id int not null,
    foreign key (pub_id) references pub (pub_id) on delete cascade INITIALLY DEFERRED,
    constraint featureprop_pub_c1 unique (featureprop_id,pub_id)
);

COMMENT ON TABLE featureprop_pub IS 'Provenance. Any featureprop assignment can optionally be supported by a publication';

create index featureprop_pub_idx1 on featureprop_pub (featureprop_id);
create index featureprop_pub_idx2 on featureprop_pub (pub_id);


create table feature_dbxref (
    feature_dbxref_id serial not null,
    primary key (feature_dbxref_id),
    feature_id int not null,
    foreign key (feature_id) references feature (feature_id) on delete cascade INITIALLY DEFERRED,
    dbxref_id int not null,
    foreign key (dbxref_id) references dbxref (dbxref_id) on delete cascade INITIALLY DEFERRED,
    is_current boolean not null default 'true',
    constraint feature_dbxref_c1 unique (feature_id,dbxref_id)
);

COMMENT ON TABLE feature_dbxref IS 'links a feature to dbxrefs. This is for secondary identifiers; primary identifiers should use feature.dbxref_id';

COMMENT ON COLUMN feature_dbxref.is_current IS 'the is_current boolean indicates whether the linked dbxref is the  current -official- dbxref for the linked feature';

create index feature_dbxref_idx1 on feature_dbxref (feature_id);
create index feature_dbxref_idx2 on feature_dbxref (dbxref_id);

--

create table feature_relationship (
    feature_relationship_id serial not null,
    primary key (feature_relationship_id),
    subject_id int not null,
    foreign key (subject_id) references feature (feature_id) on delete cascade INITIALLY DEFERRED,
    object_id int not null,
    foreign key (object_id) references feature (feature_id) on delete cascade INITIALLY DEFERRED,
    type_id int not null,
    foreign key (type_id) references cvterm (cvterm_id) on delete cascade INITIALLY DEFERRED,
    value text null,
    rank int not null default 0,
    constraint feature_relationship_c1 unique (subject_id,object_id,type_id,rank)
);

COMMENT ON TABLE feature_relationship IS 'features can be arranged in
graphs, eg exon part_of transcript part_of gene; translation madeby
transcript if type is thought of as a verb, each arc makes a statement
[SUBJECT VERB OBJECT] object can also be thought of as parent
(containing feature), and subject as child (contained feature or
subfeature) -- we include the relationship rank/order, because even
though most of the time we can order things implicitly by sequence
coordinates, we cant always do this - eg transpliced genes.  its also
useful for quickly getting implicit introns';

COMMENT ON COLUMN feature_relationship.subject_id IS 'the subject of the subj-predicate-obj sentence. This is typically the subfeature';

COMMENT ON COLUMN feature_relationship.object_id IS 'the object of the subj-predicate-obj sentence. This is typically the container feature';

COMMENT ON COLUMN feature_relationship.type_id IS 'relationship type between subject and object. This is a cvterm, typically from the OBO relationship ontology, although other relationship types are allowed. The most common relationship type is OBO_REL:part_of. Valid relationship types are constrained by the Sequence Ontology';

COMMENT ON COLUMN feature_relationship.rank IS 'The ordering of subject features with respect to the object feature may be important (for example, exon ordering on a transcript - not always derivable if you take trans spliced genes into consideration). rank is used to order these; starts from zero';

COMMENT ON COLUMN feature_relationship.value IS 'Additional notes/comments';

create index feature_relationship_idx1 on feature_relationship (subject_id);
create index feature_relationship_idx2 on feature_relationship (object_id);
create index feature_relationship_idx3 on feature_relationship (type_id);

--
 
create table feature_relationship_pub (
	feature_relationship_pub_id serial not null,
	primary key (feature_relationship_pub_id),
	feature_relationship_id int not null,
	foreign key (feature_relationship_id) references feature_relationship (feature_relationship_id) on delete cascade INITIALLY DEFERRED,
	pub_id int not null,
	foreign key (pub_id) references pub (pub_id) on delete cascade INITIALLY DEFERRED,
    constraint feature_relationship_pub_c1 unique (feature_relationship_id,pub_id)
);

COMMENT ON TABLE feature_relationship_pub IS 'Provenance. Attach optional evidence to a feature_relationship in the form of a publication';

create index feature_relationship_pub_idx1 on feature_relationship_pub (feature_relationship_id);
create index feature_relationship_pub_idx2 on feature_relationship_pub (pub_id);
 
--

create table feature_relationshipprop (
    feature_relationshipprop_id serial not null,
    primary key (feature_relationshipprop_id),
    feature_relationship_id int not null,
    foreign key (feature_relationship_id) references feature_relationship (feature_relationship_id) on delete cascade,
    type_id int not null,
    foreign key (type_id) references cvterm (cvterm_id) on delete cascade INITIALLY DEFERRED,
    value text null,
    rank int not null default 0,
    constraint feature_relationshipprop_c1 unique (feature_relationship_id,type_id,rank)
);

COMMENT ON TABLE feature_relationshipprop IS 'Extensible properties
for feature_relationships. Analagous structure to featureprop. This
table is largely optional and not used with a high frequency. Typical
scenarios may be if one wishes to attach additional data to a
feature_relationship - for example to say that the
feature_relationship is only true in certain contexts';

COMMENT ON COLUMN feature_relationshipprop.type_id IS 'The name of the
property/slot is a cvterm. The meaning of the property is defined in
that cvterm. Currently there is no standard ontology for
feature_relationship property types';

COMMENT ON COLUMN feature_relationshipprop.value IS 'The value of the
property, represented as text. Numeric values are converted to their
text representation. This is less efficient than using native database
types, but is easier to query.';

COMMENT ON COLUMN feature_relationshipprop.rank IS 'Property-Value
ordering. Any feature_relationship can have multiple values for any particular
property type - these are ordered in a list using rank, counting from
zero. For properties that are single-valued rather than multi-valued,
the default 0 value should be used';


create index feature_relationshipprop_idx1 on feature_relationshipprop (feature_relationship_id);
create index feature_relationshipprop_idx2 on feature_relationshipprop (type_id);

--

create table feature_relationshipprop_pub (
    feature_relationshipprop_pub_id serial not null,
    primary key (feature_relationshipprop_pub_id),
    feature_relationshipprop_id int not null,
    foreign key (feature_relationshipprop_id) references feature_relationshipprop (feature_relationshipprop_id) on delete cascade INITIALLY DEFERRED,
    pub_id int not null,
    foreign key (pub_id) references pub (pub_id) on delete cascade INITIALLY DEFERRED,
    constraint feature_relationshipprop_pub_c1 unique (feature_relationshipprop_id,pub_id)
);
create index feature_relationshipprop_pub_idx1 on feature_relationshipprop_pub (feature_relationshipprop_id);
create index feature_relationshipprop_pub_idx2 on feature_relationshipprop_pub (pub_id);

COMMENT ON TABLE feature_relationshipprop_pub IS 'Provenance for feature_relationshipprop';

--

create table feature_cvterm (
    feature_cvterm_id serial not null,
    primary key (feature_cvterm_id),
    feature_id int not null,
    foreign key (feature_id) references feature (feature_id) on delete cascade INITIALLY DEFERRED,
    cvterm_id int not null,
    foreign key (cvterm_id) references cvterm (cvterm_id) on delete cascade INITIALLY DEFERRED,
    pub_id int not null,
    foreign key (pub_id) references pub (pub_id) on delete cascade INITIALLY DEFERRED,
    is_not boolean not null default false,
    constraint feature_cvterm_c1 unique (feature_id,cvterm_id,pub_id)
);

COMMENT ON TABLE feature_cvterm IS 'Associate a term from a cv with a feature, for example, GO annotation';

COMMENT ON COLUMN feature_cvterm.pub_id IS 'Provenance for the annotation. Each annotation should have a single primary publication (which may be of the appropriate type for computational analyses) where more details can be found. Additional provenance dbxrefs can be attached using feature_cvterm_dbxref';

COMMENT ON COLUMN feature_cvterm.is_not IS 'if this is set to true, then this annotation is interpreted as a NEGATIVE annotation - ie the feature does NOT have the specified function, process, component, part, etc. See GO docs for more details';

create index feature_cvterm_idx1 on feature_cvterm (feature_id);
create index feature_cvterm_idx2 on feature_cvterm (cvterm_id);
create index feature_cvterm_idx3 on feature_cvterm (pub_id);

--

create table feature_cvtermprop (
    feature_cvtermprop_id serial not null,
    primary key (feature_cvtermprop_id),
    feature_cvterm_id int not null,
    foreign key (feature_cvterm_id) references feature_cvterm (feature_cvterm_id) on delete cascade,
    type_id int not null,
    foreign key (type_id) references cvterm (cvterm_id) on delete cascade INITIALLY DEFERRED,
    value text null,
    rank int not null default 0,
    constraint feature_cvtermprop_c1 unique (feature_cvterm_id,type_id,rank)
);

COMMENT ON TABLE feature_cvtermprop IS 'Extensible properties for
feature to cvterm associations. Examples: GO evidence codes;
qualifiers; metadata such as the date on which the entry was curated
and the source of the association. See the featureprop table for
meanings of type_id, value and rank';

COMMENT ON COLUMN feature_cvtermprop.type_id IS 'The name of the
property/slot is a cvterm. The meaning of the property is defined in
that cvterm. cvterms may come from the OBO evidence code cv';

COMMENT ON COLUMN feature_cvtermprop.value IS 'The value of the
property, represented as text. Numeric values are converted to their
text representation. This is less efficient than using native database
types, but is easier to query.';

COMMENT ON COLUMN feature_cvtermprop.rank IS 'Property-Value
ordering. Any feature_cvterm can have multiple values for any particular
property type - these are ordered in a list using rank, counting from
zero. For properties that are single-valued rather than multi-valued,
the default 0 value should be used';

create index feature_cvtermprop_idx1 on feature_cvtermprop (feature_cvterm_id);
create index feature_cvtermprop_idx2 on feature_cvtermprop (type_id);

--

create table feature_cvterm_dbxref (
    feature_cvterm_dbxref_id serial not null,
    primary key (feature_cvterm_dbxref_id),
    feature_cvterm_id int not null,
    foreign key (feature_cvterm_id) references feature_cvterm (feature_cvterm_id) on delete cascade,
    dbxref_id int not null,
    foreign key (dbxref_id) references dbxref (dbxref_id) on delete cascade INITIALLY DEFERRED,
    constraint feature_cvterm_dbxref_c1 unique (feature_cvterm_id,dbxref_id)
);
create index feature_cvterm_dbxref_idx1 on feature_cvterm_dbxref (feature_cvterm_id);
create index feature_cvterm_dbxref_idx2 on feature_cvterm_dbxref (dbxref_id);

COMMENT ON TABLE feature_cvterm_dbxref IS
 'Additional dbxrefs for an association. Rows in the feature_cvterm table may be backed up by dbxrefs. For example, a feature_cvterm association that was inferred via a protein-protein interaction may be backed by by refering to the dbxref for the alternate protein. Corresponds to the WITH column in a GO gene association file (but can also be used for other analagous associations). See http://www.geneontology.org/doc/GO.annotation.shtml#file for more details';

--

create table feature_cvterm_pub (
    feature_cvterm_pub_id serial not null,
    primary key (feature_cvterm_pub_id),
    feature_cvterm_id int not null,
    foreign key (feature_cvterm_id) references feature_cvterm (feature_cvterm_id) on delete cascade,
    pub_id int not null,
    foreign key (pub_id) references pub (pub_id) on delete cascade INITIALLY DEFERRED,
    constraint feature_cvterm_pub_c1 unique (feature_cvterm_id,pub_id)
);
create index feature_cvterm_pub_idx1 on feature_cvterm_pub (feature_cvterm_id);
create index feature_cvterm_pub_idx2 on feature_cvterm_pub (pub_id);

COMMENT ON TABLE feature_cvterm_pub IS 'Secondary pubs for an
association. Each feature_cvterm association is supported by a single
primary publication. Additional secondary pubs can be added using this
linking table (in a GO gene association file, these corresponding to
any IDs after the pipe symbol in the publications column';

--

create table synonym (
    synonym_id serial not null,
    primary key (synonym_id),
    name varchar(255) not null,
    type_id int not null,
    foreign key (type_id) references cvterm (cvterm_id) on delete cascade INITIALLY DEFERRED,
    synonym_sgml varchar(255) not null,
    constraint synonym_c1 unique (name,type_id)
);

COMMENT ON TABLE synonym IS 'A synonym for a feature. One feature can have multiple synonyms, and the same synonym can apply to multiple features';

COMMENT ON COLUMN synonym.name IS 'The synonym itself. Should be human-readable machine-searchable ascii text';

COMMENT ON COLUMN synonym.synonym_sgml IS 'The fully specified synonym, with any non-ascii characters encoded in SGML';

COMMENT ON COLUMN synonym.type_id IS 'types would be symbol and fullname for now';

create index synonym_idx1 on synonym (type_id);
create index synonym_idx2 on synonym ((lower(synonym_sgml)));

--

create table feature_synonym (
    feature_synonym_id serial not null,
    primary key (feature_synonym_id),
    synonym_id int not null,
    foreign key (synonym_id) references synonym (synonym_id) on delete cascade INITIALLY DEFERRED,
    feature_id int not null,
    foreign key (feature_id) references feature (feature_id) on delete cascade INITIALLY DEFERRED,
    pub_id int not null,
    foreign key (pub_id) references pub (pub_id) on delete cascade INITIALLY DEFERRED,
    is_current boolean not null default 'true',
    is_internal boolean not null default 'false',
    constraint feature_synonym_c1 unique (synonym_id,feature_id,pub_id)
);

COMMENT ON TABLE feature_synonym IS 'Linking table between feature and synonym';

COMMENT ON COLUMN feature_synonym.pub_id IS 'the pub_id link is for relating the usage of a given synonym to the publication in which it was used';

COMMENT ON COLUMN feature_synonym.is_current IS 'the is_current boolean indicates whether the linked synonym is the  current -official- symbol for the linked feature';

COMMENT ON COLUMN feature_synonym.is_internal IS 'typically a synonym exists so that somebody querying the db with an obsolete name can find the object theyre looking for (under its current name.  If the synonym has been used publicly & deliberately (eg in a paper), it my also be listed in reports as a synonym.   If the synonym was not used deliberately (eg, there was a typo which went public), then the is_internal boolean may be set to -true- so that it is known that the 
synonym is -internal- and should be queryable but should not be listed in reports as a valid synonym';

create index feature_synonym_idx1 on feature_synonym (synonym_id);
create index feature_synonym_idx2 on feature_synonym (feature_id);
create index feature_synonym_idx3 on feature_synonym (pub_id);
CREATE SCHEMA genetic_code;
SET search_path = genetic_code,public;

CREATE TABLE gencode (
        gencode_id      INTEGER PRIMARY KEY NOT NULL,
        organismstr     VARCHAR(512) NOT NULL
);

CREATE TABLE gencode_codon_aa (
        gencode_id      INTEGER NOT NULL REFERENCES gencode(gencode_id),
        codon           CHAR(3) NOT NULL,
        aa              CHAR(1) NOT NULL
);
CREATE INDEX gencode_codon_aa_i1 ON gencode_codon_aa(gencode_id,codon,aa);

CREATE TABLE gencode_startcodon (
        gencode_id      INTEGER NOT NULL REFERENCES gencode(gencode_id),
        codon           CHAR(3)
);
SET search_path = public;
-- DEPENDENCY:
--  chado/modules/bridges/sofa-bridge.sql

-- The standard Chado pattern for protein coding genes
-- is a feature of type 'gene' with 'mRNA' features as parts
-- REQUIRES: 'mrna' view from sofa-bridge.sql
CREATE OR REPLACE VIEW protein_coding_gene AS
 SELECT
  DISTINCT gene.*
 FROM
  feature AS gene
  INNER JOIN feature_relationship AS fr ON (gene.feature_id=fr.object_id)
  INNER JOIN mrna ON (mrna.feature_id=fr.subject_id);


-- introns are implicit from surrounding exons
-- combines intron features with location and parent transcript
-- the same intron appearing in multiple transcripts will appear
-- multiple times
CREATE VIEW intron_combined_view AS
 SELECT
  x1.feature_id         AS exon1_id,
  x2.feature_id         AS exon2_id,
  CASE WHEN l1.strand=-1  THEN l2.fmax ELSE l1.fmax END AS fmin,
  CASE WHEN l1.strand=-1  THEN l1.fmin ELSE l2.fmin END AS fmax,
  l1.strand             AS strand,
  l1.srcfeature_id      AS srcfeature_id,
  r1.rank               AS intron_rank,
  r1.object_id          AS transcript_id
 FROM
 cvterm
  INNER JOIN 
   feature                AS x1    ON (x1.type_id=cvterm.cvterm_id)
    INNER JOIN
     feature_relationship AS r1    ON (x1.feature_id=r1.subject_id)
    INNER JOIN
     featureloc           AS l1    ON (x1.feature_id=l1.feature_id)
  INNER JOIN
   feature                AS x2    ON (x2.type_id=cvterm.cvterm_id)
    INNER JOIN
     feature_relationship AS r2    ON (x2.feature_id=r2.subject_id)
    INNER JOIN
     featureloc           AS l2    ON (x2.feature_id=l2.feature_id)
 WHERE
  cvterm.name='exon'            AND
  (r2.rank - r1.rank) = 1       AND
  r1.object_id=r2.object_id     AND
  l1.strand = l2.strand         AND
  l1.srcfeature_id = l2.srcfeature_id         AND
  l1.locgroup=0                 AND
  l2.locgroup=0;

-- intron locations. intron IDs are the (exon1,exon2) ID pair
-- this means that introns may be counted twice if the start of
-- the 5' exon or the end of the 3' exon vary
-- introns shared by transcripts will not appear twice
CREATE VIEW intronloc_view AS
 SELECT DISTINCT
  exon1_id,
  exon2_id,
  fmin,
  fmax,
  strand,
  srcfeature_id
 FROM intron_combined_view;
-- ================================================
-- TABLE: analysis
-- ================================================

-- an analysis is a particular type of a computational analysis;
-- it may be a blast of one sequence against another, or an all by all
-- blast, or a different kind of analysis altogether.
-- it is a single unit of computation 
--
-- name: 
--   a way of grouping analyses. this should be a handy
--   short identifier that can help people find an analysis they
--   want. for instance "tRNAscan", "cDNA", "FlyPep", "SwissProt"
--   it should not be assumed to be unique. for instance, there may
--   be lots of seperate analyses done against a cDNA database.
--
-- program: 
--   e.g. blastx, blastp, sim4, genscan
--
-- programversion:
--   e.g. TBLASTX 2.0MP-WashU [09-Nov-2000]
--
-- algorithm:
--   e.g. blast
--
-- sourcename: 
--   e.g. cDNA, SwissProt
--
-- queryfeature_id:
--   the sequence that was used as the query sequence can be
--   optionally included via queryfeature_id - even though this
--   is redundant with the tables below. this can still
--   be useful - for instance, we may have an analysis that blasts
--   contigs against a database. we may then transform those hits
--   into global coordinates; it may be useful to keep a record
--   of which contig was blasted as the query.
--
--
-- MAPPING (bioperl): maps to Bio::Search::Result::ResultI
-- ** not anymore, b/c we are using analysis in a more general sense
-- ** to represent microarray analysis

--
-- sourceuri: 
--   This is an optional permanent URL/URI for the source of the
--   analysis. The idea is that someone could recreate the analysis
--   directly by going to this URI and fetching the source data
--   (eg the blast database, or the training model).

create table analysis (
    analysis_id serial not null,
    primary key (analysis_id),
    name varchar(255),
    description text,
    program varchar(255) not null,
    programversion varchar(255) not null,
    algorithm varchar(255),
    sourcename varchar(255),
    sourceversion varchar(255),
    sourceuri text,
    timeexecuted timestamp not null default current_timestamp,
    constraint analysis_c1 unique (program,programversion,sourcename)
);

-- ================================================
-- TABLE: analysisprop
-- ================================================

create table analysisprop (
    analysisprop_id serial not null,
    primary key (analysisprop_id),
    analysis_id int not null,
    foreign key (analysis_id) references analysis (analysis_id) on delete cascade INITIALLY DEFERRED,
    type_id int not null,
    foreign key (type_id) references cvterm (cvterm_id) on delete cascade INITIALLY DEFERRED,
    value text,
    constraint analysisprop_c1 unique (analysis_id,type_id,value)
);
create index analysisprop_idx1 on analysisprop (analysis_id);
create index analysisprop_idx2 on analysisprop (type_id);

-- ================================================
-- TABLE: analysisfeature
-- ================================================

-- computational analyses generate features (eg genscan generates
-- transcripts and exons; sim4 alignments generate similarity/match
-- features)

-- analysisfeatures are stored using the feature table from
-- the sequence module. the analysisfeature table is used to
-- decorate these features, with analysis specific attributes.
--
-- a feature is an analysisfeature if and only if there is
-- a corresponding entry in the analysisfeature table
--
-- analysisfeatures will have two or more featureloc entries,
-- with rank indicating query/subject

--  analysis_id:
--    scoredsets are grouped into analyses
--
--  rawscore:
--    this is the native score generated by the program; for example,
--    the bitscore generated by blast, sim4 or genscan scores.
--    one should not assume that high is necessarily better than low.
--
--  normscore:
--    this is the rawscore but semi-normalized. complete normalization
--    to allow comparison of features generated by different programs
--    would be nice but too difficult. instead the normalization should
--    strive to enforce the following semantics:
--
--    * normscores are floating point numbers >= 0
--    * high normscores are better than low one.
--
--    for most programs, it would be sufficient to make the normscore
--    the same as this rawscore, providing these semantics are
--    satisfied.
--
--  significance:
--    this is some kind of expectation or probability metric,
--    representing the probability that the scoredset would appear
--    randomly given the model.
--    as such, any program or person querying this table can assume
--    the following semantics:
--     * 0 <= significance <= n, where n is a positive number, theoretically
--       unbounded but unlikely to be more than 10
--     * low numbers are better than high numbers.
--
--  identity:
--    percent identity between the locations compared
--
--  note that these 4 metrics do not cover the full range of scores
--  possible; it would be undesirable to list every score possible, as
--  this should be kept extensible. instead, for non-standard scores, use
--  the scoredsetprop table.

create table analysisfeature (
    analysisfeature_id serial not null,
    primary key (analysisfeature_id),
    feature_id int not null,
    foreign key (feature_id) references feature (feature_id) on delete cascade INITIALLY DEFERRED,
    analysis_id int not null,
    foreign key (analysis_id) references analysis (analysis_id) on delete cascade INITIALLY DEFERRED,
    rawscore double precision,
    normscore double precision,
    significance double precision,
    identity double precision,
    constraint analysisfeature_c1 unique (feature_id,analysis_id)
);
create index analysisfeature_idx1 on analysisfeature (feature_id);
create index analysisfeature_idx2 on analysisfeature (analysis_id);

CREATE TABLE phenotype (
    phenotype_id SERIAL NOT NULL,
    primary key (phenotype_id),
    uniquename TEXT NOT NULL,  
    observable_id INT,
    FOREIGN KEY (observable_id) REFERENCES cvterm (cvterm_id) ON DELETE CASCADE,
    attr_id INT,
    FOREIGN KEY (attr_id) REFERENCES cvterm (cvterm_id) ON DELETE SET NULL,
    value TEXT,
    cvalue_id INT,
    FOREIGN KEY (cvalue_id) REFERENCES cvterm (cvterm_id) ON DELETE SET NULL,
    assay_id INT,
    FOREIGN KEY (assay_id) REFERENCES cvterm (cvterm_id) ON DELETE SET NULL,
    CONSTRAINT phenotype_c1 UNIQUE (uniquename)
);
CREATE INDEX phenotype_idx1 ON phenotype (cvalue_id);
CREATE INDEX phenotype_idx2 ON phenotype (observable_id);
CREATE INDEX phenotype_idx3 ON phenotype (attr_id);

COMMENT ON TABLE phenotype IS 'a phenotypic statement, or a single atomic phenotypic observation a controlled sentence describing observable effect of non-wt function -- e.g. Obs=eye, attribute=color, cvalue=red';

COMMENT ON COLUMN phenotype.observable_id IS 'The entity: e.g. anatomy_part, biological_process';
COMMENT ON COLUMN phenotype.attr_id IS 'Phenotypic attribute (quality, property, attribute, character) - drawn from PATO';
COMMENT ON COLUMN phenotype.value IS 'value of attribute - unconstrained free text. Used only if cvalue_id is not appropriate';
COMMENT ON COLUMN phenotype.cvalue_id IS 'Phenotype attribute value (state)';
COMMENT ON COLUMN phenotype.assay_id IS 'evidence type';

CREATE TABLE phenotype_cvterm (
    phenotype_cvterm_id SERIAL NOT NULL,
    primary key (phenotype_cvterm_id),
    phenotype_id INT NOT NULL,
    FOREIGN KEY (phenotype_id) REFERENCES phenotype (phenotype_id) ON DELETE CASCADE,
    cvterm_id INT NOT NULL,
    FOREIGN KEY (cvterm_id) REFERENCES cvterm (cvterm_id) ON DELETE CASCADE,
    CONSTRAINT phenotype_cvterm_c1 UNIQUE (phenotype_id, cvterm_id)
);
CREATE INDEX phenotype_cvterm_idx1 ON phenotype_cvterm (phenotype_id);
CREATE INDEX phenotype_cvterm_idx2 ON phenotype_cvterm (cvterm_id);

COMMENT ON TABLE phenotype_cvterm IS NULL;

 
CREATE TABLE feature_phenotype (
    feature_phenotype_id SERIAL NOT NULL,
    primary key (feature_phenotype_id),
    feature_id INT NOT NULL,
    FOREIGN KEY (feature_id) REFERENCES feature (feature_id) ON DELETE CASCADE,
    phenotype_id INT NOT NULL,
    FOREIGN KEY (phenotype_id) REFERENCES phenotype (phenotype_id) ON DELETE CASCADE,
    CONSTRAINT feature_phenotype_c1 UNIQUE (feature_id,phenotype_id)       
);
CREATE INDEX feature_phenotype_idx1 ON feature_phenotype (feature_id);
CREATE INDEX feature_phenotype_idx2 ON feature_phenotype (phenotype_id);

COMMENT ON TABLE feature_phenotype IS NULL;


-- ==========================================
-- Chado genetics module
--
-- 2006-04-11
--   split out phenotype tables into phenotype module
--
-- redesigned 2003-10-28
--
-- changes 2003-11-10:
--   incorporating suggestions to make everything a gcontext; use 
--   gcontext_relationship to make some gcontexts derivable from others. we 
--   would incorporate environment this way - just add the environment 
--   descriptors as properties of the child gcontext
--
-- changes 2004-06 (Documented by DE: 10-MAR-2005):
--   Many, including rename of gcontext to genotype,  split 
--   phenstatement into phenstatement & phenotype, created environment
--
-- see doc/genetic-notes.txt
-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
-- ============
-- DEPENDENCIES
-- ============
-- :import feature from sequence
-- :import phenotype from phenotype
-- :import cvterm from cv
-- :import pub from pub
-- :import dbxref from general
-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


-- ================================================
-- TABLE: genotype
-- ================================================
-- genetic context
-- the uniquename should be derived from the features
-- making up the genoptype
--
-- uniquename: a human-readable unique identifier
--
create table genotype (
    genotype_id serial not null,
    primary key (genotype_id),
    uniquename text not null,      
    description varchar(255),
    constraint genotype_c1 unique (uniquename)
);
create index genotype_idx1 on genotype(uniquename);

COMMENT ON TABLE genotype IS NULL;


-- ===============================================
-- TABLE: feature_genotype
-- ================================================
-- A genotype is defined by a collection of features
-- mutations, balancers, deficiencies, haplotype blocks, engineered
-- constructs
-- 
-- rank can be used for n-ploid organisms
-- 
-- group can be used for distinguishing the chromosomal groups
-- 
-- (RNAi products and so on can be treated as different groups, as
-- they do not fall on a particular chromosome)
-- 
-- OPEN QUESTION: for multicopy transgenes, should we include a 'n_copies'
-- column as well?
-- 
-- chromosome_id       : a feature of SO type 'chromosome'
-- rank                : preserves order
-- group               : spatially distinguishable group
--
create table feature_genotype (
    feature_genotype_id serial not null,
    primary key (feature_genotype_id),
    feature_id int not null,
    foreign key (feature_id) references feature (feature_id) on delete cascade,
    genotype_id int not null,
    foreign key (genotype_id) references genotype (genotype_id) on delete cascade,
    chromosome_id int,
    foreign key (chromosome_id) references feature (feature_id) on delete set null,
    rank int not null,
    cgroup    int not null,
    cvterm_id int not null,
    foreign key (cvterm_id) references cvterm (cvterm_id) on delete cascade,
    constraint feature_genotype_c1 unique (feature_id, genotype_id, cvterm_id, chromosome_id, rank, cgroup)
);
create index feature_genotype_idx1 on feature_genotype (feature_id);
create index feature_genotype_idx2 on feature_genotype (genotype_id);

COMMENT ON TABLE feature_genotype IS NULL;



-- ================================================
-- TABLE: environment
-- ================================================
-- The environmental component of a phenotype description
create table environment (
    environment_id serial not NULL,
    primary key  (environment_id),
    uniquename text not null,
    description text,
    constraint environment_c1 unique (uniquename)
);
create index environment_idx1 on environment(uniquename);

COMMENT ON TABLE environment IS NULL;


-- ================================================
-- TABLE: environment_cvterm
-- ================================================
create table environment_cvterm (
    environment_cvterm_id serial not null,
    primary key  (environment_cvterm_id),
    environment_id int not null,
    foreign key (environment_id) references environment (environment_id) on delete cascade,
    cvterm_id int not null,
    foreign key (cvterm_id) references cvterm (cvterm_id) on delete cascade,
    constraint environment_cvterm_c1 unique (environment_id, cvterm_id)
);
create index environment_cvterm_idx1 on environment_cvterm (environment_id);
create index environment_cvterm_idx2 on environment_cvterm (cvterm_id);

COMMENT ON TABLE environment_cvterm IS NULL;

CREATE TABLE phenstatement (
    phenstatement_id SERIAL NOT NULL,
    primary key (phenstatement_id),
    genotype_id INT NOT NULL,
    FOREIGN KEY (genotype_id) REFERENCES genotype (genotype_id) ON DELETE CASCADE,
    environment_id INT NOT NULL,
    FOREIGN KEY (environment_id) REFERENCES environment (environment_id) ON DELETE CASCADE,
    phenotype_id INT NOT NULL,
    FOREIGN KEY (phenotype_id) REFERENCES phenotype (phenotype_id) ON DELETE CASCADE,
    type_id INT NOT NULL,
    FOREIGN KEY (type_id) REFERENCES cvterm (cvterm_id) ON DELETE CASCADE,
    pub_id INT NOT NULL,
    FOREIGN KEY (pub_id) REFERENCES pub (pub_id) ON DELETE CASCADE,
    CONSTRAINT phenstatement_c1 UNIQUE (genotype_id,phenotype_id,environment_id,type_id,pub_id)
);
CREATE INDEX phenstatement_idx1 ON phenstatement (genotype_id);
CREATE INDEX phenstatement_idx2 ON phenstatement (phenotype_id);

COMMENT ON TABLE phenstatement IS 'Phenotypes are things like "larval lethal".  Phenstatements are things like "dpp[1] is recessive larval lethal". So essentially phenstatement is a linking table expressing the relationship between genotype, environment, and phenotype.';

CREATE TABLE phendesc (
    phendesc_id SERIAL NOT NULL,
    primary key (phendesc_id),
    genotype_id INT NOT NULL,
    FOREIGN KEY (genotype_id) REFERENCES genotype (genotype_id) ON DELETE CASCADE,
    environment_id INT NOT NULL,
    FOREIGN KEY (environment_id) REFERENCES environment ( environment_id) ON DELETE CASCADE,
    description TEXT NOT NULL,
    pub_id INT NOT NULL,
    FOREIGN KEY (pub_id) REFERENCES pub (pub_id) ON DELETE CASCADE,
    CONSTRAINT phendesc_c1 UNIQUE (genotype_id,environment_id,pub_id)
);
CREATE INDEX phendesc_idx1 ON phendesc (genotype_id);
CREATE INDEX phendesc_idx2 ON phendesc (environment_id);
CREATE INDEX phendesc_idx3 ON phendesc (pub_id);

COMMENT ON TABLE phendesc IS 'a summary of a _set_ of phenotypic statements for any one gcontext made in any one publication';

CREATE TABLE phenotype_comparison (
    phenotype_comparison_id SERIAL NOT NULL,
    primary key (phenotype_comparison_id),
    genotype1_id INT NOT NULL,
        FOREIGN KEY (genotype1_id) REFERENCES genotype (genotype_id) ON DELETE CASCADE,
    environment1_id INT NOT NULL,
        FOREIGN KEY (environment1_id) REFERENCES environment (environment_id) ON DELETE CASCADE,
    genotype2_id INT NOT NULL,
        FOREIGN KEY (genotype2_id) REFERENCES genotype (genotype_id) ON DELETE CASCADE,
    environment2_id INT NOT NULL,
        FOREIGN KEY (environment2_id) REFERENCES environment (environment_id) ON DELETE CASCADE,
    phenotype1_id INT NOT NULL,
        FOREIGN KEY (phenotype1_id) REFERENCES phenotype (phenotype_id) ON DELETE CASCADE,
    phenotype2_id INT,
        FOREIGN KEY (phenotype2_id) REFERENCES phenotype (phenotype_id) ON DELETE CASCADE,
    type_id INT NOT NULL,
        FOREIGN KEY (type_id) REFERENCES cvterm (cvterm_id) ON DELETE CASCADE,
    pub_id INT NOT NULL,
    FOREIGN KEY (pub_id) REFERENCES pub (pub_id) ON DELETE CASCADE,
    CONSTRAINT phenotype_comparison_c1 UNIQUE (genotype1_id,environment1_id,genotype2_id,environment2_id,phenotype1_id,type_id,pub_id)
);

COMMENT ON TABLE phenotype_comparison IS 'comparison of phenotypes eg, genotype1/environment1/phenotype1 "non-suppressible" wrt  genotype2/environment2/phenotype2';


--------------------------------
---- dfeatureloc ---------------
--------------------------------
-- dfeatureloc is meant as an alternate representation of
-- the data in featureloc (see the descrption of featureloc
-- in sequence.sql).  In dfeatureloc, fmin and fmax are 
-- replaced with nbeg and nend.  Whereas fmin and fmax
-- are absolute coordinates relative to the parent feature, nbeg 
-- and nend are the beginning and ending coordinates
-- relative to the feature itself.  For example, nbeg would
-- mark the 5' end of a gene and nend would mark the 3' end.

CREATE OR REPLACE VIEW dfeatureloc (
 featureloc_id,
 feature_id,
 srcfeature_id,
 nbeg,
 is_nbeg_partial,
 nend,
 is_nend_partial,
 strand,
 phase,
 residue_info,
 locgroup,
 rank
) AS
SELECT featureloc_id, feature_id, srcfeature_id, fmin, is_fmin_partial,
       fmax, is_fmax_partial, strand, phase, residue_info, locgroup, rank
FROM featureloc
WHERE (strand < 0 or phase < 0)
UNION
SELECT featureloc_id, feature_id, srcfeature_id, fmax, is_fmax_partial,
       fmin, is_fmin_partial, strand, phase, residue_info, locgroup, rank
FROM featureloc
WHERE (strand is NULL or strand >= 0 or phase >= 0) ;

--------------------------------
---- f_type --------------------
--------------------------------
CREATE OR REPLACE VIEW f_type
AS
  SELECT  f.feature_id,
          f.name,
          f.dbxref_id,
          c.name AS type,
          f.residues,
          f.seqlen,
          f.md5checksum,
          f.type_id,
          f.timeaccessioned,
          f.timelastmodified
    FROM  feature f, cvterm c
   WHERE  f.type_id = c.cvterm_id;

--------------------------------
---- fnr_type ------------------
--------------------------------
CREATE OR REPLACE VIEW fnr_type
AS
  SELECT  f.feature_id,
          f.name,
          f.dbxref_id,
          c.name AS type,
          f.residues,
          f.seqlen,
          f.md5checksum,
          f.type_id,
          f.timeaccessioned,
          f.timelastmodified
    FROM  feature f left outer join analysisfeature af
          on (f.feature_id = af.feature_id), cvterm c
   WHERE  f.type_id = c.cvterm_id
          and af.feature_id is null;

--------------------------------
---- f_loc ---------------------
--------------------------------
-- Note from Scott:  I changed this view to depend on dfeatureloc,
-- since I don't know what it is used for.  The change should
-- be transparent.  I also changed dbxrefstr to dbxref_id since
-- dbxrefstr is no longer in feature

CREATE OR REPLACE VIEW f_loc
AS
  SELECT  f.feature_id,
          f.name,
          f.dbxref_id,
          fl.nbeg,
          fl.nend,
          fl.strand
    FROM  dfeatureloc fl, f_type f
   WHERE  f.feature_id = fl.feature_id;

--------------------------------
---- fp_key -------------------
--------------------------------
CREATE OR REPLACE VIEW fp_key
AS
  SELECT  fp.feature_id,
          c.name AS pkey,
          fp.value
    FROM  featureprop fp, cvterm c
   WHERE  fp.featureprop_id = c.cvterm_id;

-- [symmetric,reflexive]
-- intervals have at least one interbase point in common
-- (i.e. overlap OR abut)
-- EXAMPLE QUERY:
--   (features of same type that overlap)
--   SELECT r.*
--   FROM feature AS x 
--   INNER JOIN feature_meets AS r ON (x.feature_id=r.subject_id)
--   INNER JOIN feature AS y ON (y.feature_id=r.object_id)
--   WHERE x.type_id=y.type_id
CREATE OR REPLACE VIEW feature_meets (
  subject_id,
  object_id
) AS
SELECT
 x.feature_id,
 y.feature_id
FROM
 featureloc AS x,
 featureloc AS y
WHERE
 x.srcfeature_id=y.srcfeature_id
 AND
 ( x.fmax >= y.fmin AND x.fmin <= y.fmax );

COMMENT ON VIEW feature_meets IS 'intervals have at least one
interbase point in common (ie overlap OR abut). symmetric,reflexive';

-- [symmetric,reflexive]
-- as above, strands match
CREATE OR REPLACE VIEW feature_meets_on_same_strand (
  subject_id,
  object_id
) AS
SELECT
 x.feature_id,
 y.feature_id
FROM
 featureloc AS x,
 featureloc AS y
WHERE
 x.srcfeature_id=y.srcfeature_id
 AND
 x.strand = y.strand
 AND
 ( x.fmax >= y.fmin AND x.fmin <= y.fmax );

COMMENT ON VIEW feature_meets_on_same_strand IS 'as feature_meets, but
featurelocs must be on the same strand. symmetric,reflexive';


-- [symmetric]
-- intervals have no interbase points in common and do not abut
CREATE OR REPLACE VIEW feature_disjoint (
  subject_id,
  object_id
) AS
SELECT
 x.feature_id,
 y.feature_id
FROM
 featureloc AS x,
 featureloc AS y
WHERE
 x.srcfeature_id=y.srcfeature_id
 AND
 ( x.fmax < y.fmin AND x.fmin > y.fmax );

COMMENT ON VIEW feature_disjoint IS 'featurelocs do not meet. symmetric';

-- 4-ary relation
CREATE OR REPLACE VIEW feature_union AS
SELECT
  x.feature_id  AS subject_id,
  y.feature_id  AS object_id,
  x.srcfeature_id,
  x.strand      AS subject_strand,
  y.strand      AS object_strand,
  CASE WHEN x.fmin<y.fmin THEN x.fmin ELSE y.fmin END AS fmin,
  CASE WHEN x.fmax>y.fmax THEN x.fmax ELSE y.fmax END AS fmax
FROM
 featureloc AS x,
 featureloc AS y
WHERE
 x.srcfeature_id=y.srcfeature_id
 AND
 ( x.fmax >= y.fmin AND x.fmin <= y.fmax );

COMMENT ON VIEW feature_union IS 'set-union on interval defined by featureloc. featurelocs must meet';


-- 4-ary relation
CREATE OR REPLACE VIEW feature_intersection AS
SELECT
  x.feature_id  AS subject_id,
  y.feature_id  AS object_id,
  x.srcfeature_id,
  x.strand      AS subject_strand,
  y.strand      AS object_strand,
  CASE WHEN x.fmin<y.fmin THEN y.fmin ELSE x.fmin END AS fmin,
  CASE WHEN x.fmax>y.fmax THEN y.fmax ELSE x.fmax END AS fmax
FROM
 featureloc AS x,
 featureloc AS y
WHERE
 x.srcfeature_id=y.srcfeature_id
 AND
 ( x.fmax >= y.fmin AND x.fmin <= y.fmax );

COMMENT ON VIEW feature_intersection IS 'set-intersection on interval defined by featureloc. featurelocs must meet';

-- 4-ary relation
-- subtract object interval from subject interval
--  (may leave zero, one or two intervals)
CREATE OR REPLACE VIEW feature_difference (
  subject_id,
  object_id,
  srcfeature_id,
  fmin,
  fmax,
  strand
) AS
-- left interval
SELECT
  x.feature_id,
  y.feature_id,
  x.strand,
  x.srcfeature_id,
  x.fmin,
  y.fmin
FROM
 featureloc AS x,
 featureloc AS y
WHERE
 x.srcfeature_id=y.srcfeature_id
 AND
 (x.fmin < y.fmin AND x.fmax >= y.fmax )
UNION
-- right interval
SELECT
  x.feature_id,
  y.feature_id,
  x.strand,
  x.srcfeature_id,
  y.fmax,
  x.fmax
FROM
 featureloc AS x,
 featureloc AS y
WHERE
 x.srcfeature_id=y.srcfeature_id
 AND
 (x.fmax > y.fmax AND x.fmin <= y.fmin );

COMMENT ON VIEW feature_difference IS 'set-distance on interval defined by featureloc. featurelocs must meet';

-- 4-ary relation
CREATE OR REPLACE VIEW feature_distance AS
SELECT
  x.feature_id  AS subject_id,
  y.feature_id  AS object_id,
  x.srcfeature_id,
  x.strand      AS subject_strand,
  y.strand      AS object_strand,
  CASE WHEN x.fmax <= y.fmin THEN (x.fmax-y.fmin) ELSE (y.fmax-x.fmin) END AS distance
FROM
 featureloc AS x,
 featureloc AS y
WHERE
 x.srcfeature_id=y.srcfeature_id
 AND
 ( x.fmax <= y.fmin OR x.fmin >= y.fmax );

COMMENT ON VIEW feature_difference IS 'size of gap between two features. must be abutting or disjoint';

-- [transitive,reflexive]
-- (should this be made non-reflexive?)
-- subject intervals contains (or is same as) object interval
CREATE OR REPLACE VIEW feature_contains (
  subject_id,
  object_id
) AS
SELECT
 x.feature_id,
 y.feature_id
FROM
 featureloc AS x,
 featureloc AS y
WHERE
 x.srcfeature_id=y.srcfeature_id
 AND
 ( y.fmin >= x.fmin AND y.fmin <= x.fmax );

COMMENT ON VIEW feature_contains IS 'subject intervals contains (or is
same as) object interval. transitive,reflexive';

-- featureset relations:
--  a featureset relation is true between any two features x and y
--  if the relation is true for any x' and y' where x' and y' are
--  subfeatures of x and y

-- see feature_meets
-- example: two transcripts meet if any of their exons or CDSs overlap
-- or abut
CREATE OR REPLACE VIEW featureset_meets (
  subject_id,
  object_id
) AS
SELECT
 x.object_id,
 y.object_id
FROM
 feature_meets AS r
 INNER JOIN feature_relationship AS x ON (r.subject_id = x.subject_id)
 INNER JOIN feature_relationship AS y ON (r.object_id = y.subject_id);

