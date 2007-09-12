 % -- ********************************************************* --
 % -- Autogenerated Prolog factfiles 
 % -- see http://www.blipkit.org for details 
 % -- ********************************************************* --


 % -- Property/Slot --
property('OBO_REL:relationship').
metadata_db:entity_label('OBO_REL:relationship', 'relationship').
metadata_db:entity_resource('OBO_REL:relationship', 'relationship').
def('OBO_REL:relationship', 'A relationship between two classes (terms). Relationships between classes are expressed in terms of relations on underlying instances.').

 % -- Property/Slot --
property('OBO_REL:is_a').
metadata_db:entity_label('OBO_REL:is_a', 'is_a').
metadata_db:entity_resource('OBO_REL:is_a', 'relationship').
is_reflexive('OBO_REL:is_a').
is_anti_symmetric('OBO_REL:is_a').
is_transitive('OBO_REL:is_a').
def('OBO_REL:is_a', 'For continuants: C is_a C'' if and only if: given any c that instantiates C at a time t, c instantiates C'' at t. For processes: P is_a P'' if and only if: that given any p that instantiates P, then p instantiates P''.').
def_xref('OBO_REL:is_a', 'PMID:15892874').
metadata_db:entity_comment('OBO_REL:is_a', 'The is_a relationship is considered axiomatic by the obo file format specification, and by OWL').
metadata_db:entity_synonym('OBO_REL:is_a', 'is_subtype_of').
metadata_db:entity_synonym_scope('OBO_REL:is_a', 'is_subtype_of', 'exact').
metadata_db:entity_xref('OBO_REL:is_a', 'owl:subClassOf').

 % -- Property/Slot --
property('OBO_REL:part_of').
metadata_db:entity_label('OBO_REL:part_of', 'part_of').
subclass('OBO_REL:part_of', 'OBO_REL:relationship').
metadata_db:entity_resource('OBO_REL:part_of', 'relationship').
is_reflexive('OBO_REL:part_of').
is_anti_symmetric('OBO_REL:part_of').
is_transitive('OBO_REL:part_of').
def('OBO_REL:part_of', 'For continuants: C part_of C'' if and only if: given any c that instantiates C at a time t, there is some c'' such that c'' instantiates C'' at time t, and c *part_of* c'' at t. For processes: P part_of P'' if and only if: given any p that instantiates P at a time t, there is some p'' such that p'' instantiates P'' at time t, and p *part_of* p'' at t. (Here *part_of* is the instance-level part-relation.)').
def_xref('OBO_REL:part_of', 'PMID:15892874').
metadata_db:entity_comment('OBO_REL:part_of', 'Parthood as a relation between instances: The primitive instance-level relation p part_of p1 is illustrated in assertions such as: this instance of rhodopsin mediated phototransduction part_of this instance of visual perception.    This relation satisfies at least the following standard axioms of mereology: reflexivity (for all p, p part_of p); anti-symmetry (for all p, p1, if p part_of p1 and p1 part_of p then p and p1 are identical); and transitivity (for all p, p1, p2, if p part_of p1 and p1 part_of p2, then p part_of p2). Analogous axioms hold also for parthood as a relation between spatial regions.    For parthood as a relation between continuants, these axioms need to be modified to take account of the incorporation of a temporal argument. Thus for example the axiom of transitivity for continuants will assert that if c part_of c1 at t and c1 part_of c2 at t, then also c part_of c2 at t.    Parthood as a relation between classes: To define part_of as a relation between classes we again need to distinguish the two cases of continuants and processes, even though the explicit reference to instants of time now falls away. For continuants, we have C part_of C1 if and only if any instance of C at any time is an instance-level part of some instance of C1 at that time, as for example in: cell nucleus part_ of cell.').
inverse_of('OBO_REL:part_of', 'OBO_REL:has_part').

 % -- Property/Slot --
property('OBO_REL:has_part').
metadata_db:entity_label('OBO_REL:has_part', 'has_part').
subclass('OBO_REL:has_part', 'OBO_REL:relationship').
metadata_db:entity_resource('OBO_REL:has_part', 'relationship').
is_reflexive('OBO_REL:has_part').
is_anti_symmetric('OBO_REL:has_part').
is_transitive('OBO_REL:has_part').
inverse_of('OBO_REL:has_part', 'OBO_REL:part_of').

 % -- Property/Slot --
property('OBO_REL:integral_part_of').
metadata_db:entity_label('OBO_REL:integral_part_of', 'integral_part_of').
subclass('OBO_REL:integral_part_of', 'OBO_REL:part_of').
metadata_db:entity_resource('OBO_REL:integral_part_of', 'relationship').
is_reflexive('OBO_REL:integral_part_of').
is_anti_symmetric('OBO_REL:integral_part_of').
is_transitive('OBO_REL:integral_part_of').
def('OBO_REL:integral_part_of', 'C integral_part_of C'' if and only if: C part_of C'' AND C'' has_part C').
def_xref('OBO_REL:integral_part_of', 'PMID:15892874').

 % -- Property/Slot --
property('OBO_REL:has_integral_part').
metadata_db:entity_label('OBO_REL:has_integral_part', 'has_integral_part').
subclass('OBO_REL:has_integral_part', 'OBO_REL:has_part').
metadata_db:entity_resource('OBO_REL:has_integral_part', 'relationship').
is_reflexive('OBO_REL:has_integral_part').
is_anti_symmetric('OBO_REL:has_integral_part').
is_transitive('OBO_REL:has_integral_part').

 % -- Property/Slot --
property('OBO_REL:proper_part_of').
metadata_db:entity_label('OBO_REL:proper_part_of', 'proper_part_of').
subclass('OBO_REL:proper_part_of', 'OBO_REL:part_of').
metadata_db:entity_resource('OBO_REL:proper_part_of', 'relationship').
is_transitive('OBO_REL:proper_part_of').
def('OBO_REL:proper_part_of', 'As for part_of, with the additional constraint that subject and object are distinct').
def_xref('OBO_REL:proper_part_of', 'PMID:15892874').
inverse_of('OBO_REL:proper_part_of', 'OBO_REL:has_proper_part').

 % -- Property/Slot --
property('OBO_REL:has_proper_part').
metadata_db:entity_label('OBO_REL:has_proper_part', 'has_proper_part').
subclass('OBO_REL:has_proper_part', 'OBO_REL:has_part').
metadata_db:entity_resource('OBO_REL:has_proper_part', 'relationship').
is_transitive('OBO_REL:has_proper_part').
inverse_of('OBO_REL:has_proper_part', 'OBO_REL:proper_part_of').

 % -- Property/Slot --
property('OBO_REL:located_in').
metadata_db:entity_label('OBO_REL:located_in', 'located_in').
subclass('OBO_REL:located_in', 'OBO_REL:relationship').
metadata_db:entity_resource('OBO_REL:located_in', 'relationship').
is_reflexive('OBO_REL:located_in').
is_transitive('OBO_REL:located_in').
def('OBO_REL:located_in', 'C located_in C'' if and only if: given any c that instantiates C at a time t, there is some c'' such that: c'' instantiates C'' at time t and c *located_in* c''. (Here *located_in* is the instance-level location relation.)').
def_xref('OBO_REL:located_in', 'PMID:15892874').
metadata_db:entity_comment('OBO_REL:located_in', 'Location as a relation between instances: The primitive instance-level relation c located_in r at t reflects the fact that each continuant is at any given time associated with exactly one spatial region, namely its exact location. Following we can use this relation to define a further instance-level location relation - not between a continuant and the region which it exactly occupies, but rather between one continuant and another. c is located in c1, in this sense, whenever the spatial region occupied by c is part_of the spatial region occupied by c1.    Note that this relation comprehends both the relation of exact location between one continuant and another which obtains when r and r1 are identical (for example, when a portion of fluid exactly fills a cavity), as well as those sorts of inexact location relations which obtain, for example, between brain and head or between ovum and uterus').
inverse_of('OBO_REL:located_in', 'OBO_REL:location_of').

 % -- Property/Slot --
property('OBO_REL:location_of').
metadata_db:entity_label('OBO_REL:location_of', 'location_of').
subclass('OBO_REL:location_of', 'OBO_REL:relationship').
metadata_db:entity_resource('OBO_REL:location_of', 'relationship').
is_reflexive('OBO_REL:location_of').
is_transitive('OBO_REL:location_of').
inverse_of('OBO_REL:location_of', 'OBO_REL:located_in').

 % -- Property/Slot --
property('OBO_REL:contained_in').
metadata_db:entity_label('OBO_REL:contained_in', 'contained_in').
subclass('OBO_REL:contained_in', 'OBO_REL:relationship').
metadata_db:entity_resource('OBO_REL:contained_in', 'relationship').
def('OBO_REL:contained_in', 'C contained_in C'' if and only if: given any instance c that instantiates C at a time t, there is some c'' such that: c'' instantiates C'' at time t and c located_in c'' at t, and it is not the case that c *overlaps* c'' at t. (c'' is a conduit or cavity.)').
def_xref('OBO_REL:contained_in', 'PMID:15892874').
metadata_db:entity_comment('OBO_REL:contained_in', 'Containment obtains in each case between material and immaterial continuants, for instance: lung contained_in thoracic cavity; bladder contained_in pelvic cavity. Hence containment is not a transitive relation.    If c part_of c1 at t then we have also, by our definition and by the axioms of mereology applied to spatial regions, c located_in c1 at t. Thus, many examples of instance-level location relations for continuants are in fact cases of instance-level parthood. For material continuants location and parthood coincide. Containment is location not involving parthood, and arises only where some immaterial continuant is involved. To understand this relation, we first define overlap for continuants as follows:    c1 overlap c2 at t =def for some c, c part_of c1 at t and c part_of c2 at t.    The containment relation on the instance level can then be defined (see definition):').
inverse_of('OBO_REL:contained_in', 'OBO_REL:contains').

 % -- Property/Slot --
property('OBO_REL:contains').
metadata_db:entity_label('OBO_REL:contains', 'contains').
subclass('OBO_REL:contains', 'OBO_REL:relationship').
metadata_db:entity_resource('OBO_REL:contains', 'relationship').
inverse_of('OBO_REL:contains', 'OBO_REL:contained_in').

 % -- Property/Slot --
property('OBO_REL:adjacent_to').
metadata_db:entity_label('OBO_REL:adjacent_to', 'adjacent_to').
subclass('OBO_REL:adjacent_to', 'OBO_REL:relationship').
metadata_db:entity_resource('OBO_REL:adjacent_to', 'relationship').
def('OBO_REL:adjacent_to', 'C adjacent to C'' if and only if: given any instance c that instantiates C at a time t, there is some c'' such that: c'' instantiates C'' at time t and c and c'' are in spatial proximity').
def_xref('OBO_REL:adjacent_to', 'PMID:15892874').
metadata_db:entity_comment('OBO_REL:adjacent_to', 'Note that adjacent_to as thus defined is not a symmetric relation, in contrast to its instance-level counterpart. For it can be the case that Cs are in general such as to be adjacent to instances of C1 while no analogous statement holds for C1s in general in relation to instances of C. Examples are: nuclear membrane adjacent_to cytoplasm; seminal vesicle adjacent_to urinary bladder; ovary adjacent_to parietal pelvic peritoneum').

 % -- Property/Slot --
property('OBO_REL:transformation_of').
metadata_db:entity_label('OBO_REL:transformation_of', 'transformation_of').
subclass('OBO_REL:transformation_of', 'OBO_REL:relationship').
metadata_db:entity_resource('OBO_REL:transformation_of', 'relationship').
is_transitive('OBO_REL:transformation_of').
def('OBO_REL:transformation_of', 'Relation between two classes, in which instances retain their identity yet change their classification by virtue of some kind of transformation. Formally: C transformation_of C'' if and only if given any c and any t, if c instantiates C at time t, then for some t'', c instantiates C'' at t'' and t'' earlier t, and there is no t2 such that c instantiates C at t2 and c instantiates C'' at t2.').
def_xref('OBO_REL:transformation_of', 'PMID:15892874').
metadata_db:entity_comment('OBO_REL:transformation_of', 'When an embryonic oenocyte (a type of insect cell) is transformed into a larval oenocyte, one and the same continuant entity preserves its identity while instantiating distinct classes at distinct times. The class-level relation transformation_of obtains between continuant classes C and C1 wherever each instance of the class C is such as to have existed at some earlier time as an instance of the distinct class C1 (see Figure 2 in paper). This relation is illustrated first of all at the molecular level of granularity by the relation between mature RNA and the pre-RNA from which it is processed, or between (UV-induced) thymine-dimer and thymine dinucleotide. At coarser levels of granularity it is illustrated by the transformations involved in the creation of red blood cells, for example, from reticulocyte to erythrocyte, and by processes of development, for example, from larva to pupa, or from (post-gastrular) embryo to fetus or from child to adult. It is also manifest in pathological transformations, for example, of normal colon into carcinomatous colon. In each such case, one and the same continuant entity instantiates distinct classes at different times in virtue of phenotypic changes.').

 % -- Property/Slot --
property('OBO_REL:transformed_into').
metadata_db:entity_label('OBO_REL:transformed_into', 'transformed_into').
metadata_db:entity_resource('OBO_REL:transformed_into', 'relationship').
metadata_db:entity_obsolete('OBO_REL:transformed_into', 'property').
metadata_db:entity_comment('OBO_REL:transformed_into', 'Obsoleted').

 % -- Property/Slot --
property('OBO_REL:derives_from').
metadata_db:entity_label('OBO_REL:derives_from', 'derives_from').
subclass('OBO_REL:derives_from', 'OBO_REL:relationship').
metadata_db:entity_resource('OBO_REL:derives_from', 'relationship').
is_transitive('OBO_REL:derives_from').
def('OBO_REL:derives_from', 'Derivation on the instance level (*derives_from*) holds between distinct material continuants when one succeeds the other across a temporal divide in such a way that at least a biologically significant portion of the matter of the earlier continuant is inherited by the later. We say that one class C derives_from class C'' if instances of C are connected to instances of C'' via some chain of instance-level derivation relations. Example: osteocyte derives_from osteoblast. Formally: C derives_immediately_from C'' if and only if: given any c and any t, if c instantiates C at time t, then there is some c'' and some t'', such that c'' instantiates C'' at t'' and t'' earlier-than t and c *derives_from* c''. C derives_from C'' if and only if: there is an chain of immediate derivation relations connecting C to C''.').
def_xref('OBO_REL:derives_from', 'PMID:15892874').
metadata_db:entity_comment('OBO_REL:derives_from', 'Derivation as a relation between instances. The temporal relation of derivation is more complex. Transformation, on the instance level, is just the relation of identity: each adult is identical to some child existing at some earlier time. Derivation on the instance-level is a relation holding between non-identicals. More precisely, it holds between distinct material continuants when one succeeds the other across a temporal divide in such a way that at least a biologically significant portion of the matter of the earlier continuant is inherited by the later. Thus we will have axioms to the effect that from c derives_from c1 we can infer that c and c1 are not identical and that there is some instant of time t such that c1 exists only prior to and c only subsequent to t. We will also be able to infer that the spatial region occupied by c as it begins to exist at t overlaps with the spatial region occupied by c1 as it ceases to exist in the same instant.').
inverse_of('OBO_REL:derives_from', 'OBO_REL:derived_into').

 % -- Property/Slot --
property('OBO_REL:derived_into').
metadata_db:entity_label('OBO_REL:derived_into', 'derived_into').
subclass('OBO_REL:derived_into', 'OBO_REL:relationship').
metadata_db:entity_resource('OBO_REL:derived_into', 'relationship').
is_transitive('OBO_REL:derived_into').
inverse_of('OBO_REL:derived_into', 'OBO_REL:derives_from').

 % -- Property/Slot --
property('OBO_REL:preceded_by').
metadata_db:entity_label('OBO_REL:preceded_by', 'preceded_by').
subclass('OBO_REL:preceded_by', 'OBO_REL:relationship').
metadata_db:entity_resource('OBO_REL:preceded_by', 'relationship').
is_transitive('OBO_REL:preceded_by').
def('OBO_REL:preceded_by', 'P preceded_by P'' if and only if: given any process p that instantiates P at a time t, there is some process p'' such that p'' instantiates P'' at time t'', and t'' is earlier than t.').
def_xref('OBO_REL:preceded_by', 'PMID:15892874').
metadata_db:entity_comment('OBO_REL:preceded_by', 'An example is: translation preceded_by transcription; aging preceded_by development (not however death preceded_by aging). Where derives_from links classes of continuants, preceded_by links classes of processes. Clearly, however, these two relations are not independent of each other. Thus if cells of type C1 derive_from cells of type C, then any cell division involving an instance of C1 in a given lineage is preceded_by cellular processes involving an instance of C.    The assertion P preceded_by P1 tells us something about Ps in general: that is, it tells us something about what happened earlier, given what we know about what happened later. Thus it does not provide information pointing in the opposite direction, concerning instances of P1 in general; that is, that each is such as to be succeeded by some instance of P. Note that an assertion to the effect that P preceded_by P1 is rather weak; it tells us little about the relations between the underlying instances in virtue of which the preceded_by relation obtains. Typically we will be interested in stronger relations, for example in the relation immediately_preceded_by, or in relations which combine preceded_by with a condition to the effect that the corresponding instances of P and P1 share participants, or that their participants are connected by relations of derivation, or (as a first step along the road to a treatment of causality) that the one process in some way affects (for example, initiates or regulates) the other.').
inverse_of('OBO_REL:preceded_by', 'OBO_REL:precedes').

 % -- Property/Slot --
property('OBO_REL:precedes').
metadata_db:entity_label('OBO_REL:precedes', 'precedes').
subclass('OBO_REL:precedes', 'OBO_REL:relationship').
metadata_db:entity_resource('OBO_REL:precedes', 'relationship').
is_transitive('OBO_REL:precedes').
inverse_of('OBO_REL:precedes', 'OBO_REL:preceded_by').

 % -- Property/Slot --
property('OBO_REL:has_participant').
metadata_db:entity_label('OBO_REL:has_participant', 'has_participant').
subclass('OBO_REL:has_participant', 'OBO_REL:relationship').
metadata_db:entity_resource('OBO_REL:has_participant', 'relationship').
def('OBO_REL:has_participant', 'P has_participant C if and only if: given any process p that instantiates P there is some continuant c, and some time t, such that: c instantiates C at t and c participates in p at t').
def_xref('OBO_REL:has_participant', 'PMID:15892874').
metadata_db:entity_comment('OBO_REL:has_participant', 'Has_participant is a primitive instance-level relation between a process, a continuant, and a time at which the continuant participates in some way in the process. The relation obtains, for example, when this particular process of oxygen exchange across this particular alveolar membrane has_participant this particular sample of hemoglobin at this particular time.').
inverse_of('OBO_REL:has_participant', 'OBO_REL:participates_in').

 % -- Property/Slot --
property('OBO_REL:participates_in').
metadata_db:entity_label('OBO_REL:participates_in', 'participates_in').
subclass('OBO_REL:participates_in', 'OBO_REL:relationship').
metadata_db:entity_resource('OBO_REL:participates_in', 'relationship').
inverse_of('OBO_REL:participates_in', 'OBO_REL:has_participant').

 % -- Property/Slot --
property('OBO_REL:has_agent').
metadata_db:entity_label('OBO_REL:has_agent', 'has_agent').
subclass('OBO_REL:has_agent', 'OBO_REL:has_participant').
metadata_db:entity_resource('OBO_REL:has_agent', 'relationship').
def('OBO_REL:has_agent', 'As for has_participant, but with the additional condition that the component instance is causally active in the relevant process').
def_xref('OBO_REL:has_agent', 'PMID:15892874').
inverse_of('OBO_REL:has_agent', 'OBO_REL:agent_in').

 % -- Property/Slot --
property('OBO_REL:agent_in').
metadata_db:entity_label('OBO_REL:agent_in', 'agent_in').
subclass('OBO_REL:agent_in', 'OBO_REL:participates_in').
metadata_db:entity_resource('OBO_REL:agent_in', 'relationship').
inverse_of('OBO_REL:agent_in', 'OBO_REL:has_agent').

 % -- Property/Slot --
property('OBO_REL:instance_of').
metadata_db:entity_label('OBO_REL:instance_of', 'instance_of').
subclass('OBO_REL:instance_of', 'OBO_REL:relationship').
metadata_db:entity_resource('OBO_REL:instance_of', 'relationship').
def('OBO_REL:instance_of', 'A relation between an instance and a class. For components: a primitive relation between a component instance and a class which it instantiates at a specific time. For processes: a primitive relation, between a process instance and a class which it instantiates, holding independently of time').
def_xref('OBO_REL:instance_of', 'PMID:15892874').
metadata_db:entity_comment('OBO_REL:instance_of', 'The instance_of relationship is considered axiomatic by the obo file format specification; ie it is taken for granted. The is_a relation is still included in this ontology for completeness').

 % -- Property/Slot --
property('OBO_REL:has_improper_part').
metadata_db:entity_label('OBO_REL:has_improper_part', 'has_improper_part').
subclass('OBO_REL:has_improper_part', 'OBO_REL:has_part').
metadata_db:entity_resource('OBO_REL:has_improper_part', 'relationship').
metadata_db:entity_obsolete('OBO_REL:has_improper_part', 'property').
is_reflexive('OBO_REL:has_improper_part').
is_transitive('OBO_REL:has_improper_part').
metadata_db:entity_comment('OBO_REL:has_improper_part', 'See reasons for obsoletion of improper_part_of').
inverse_of('OBO_REL:has_improper_part', 'OBO_REL:improper_part_of').

 % -- Property/Slot --
property('OBO_REL:improper_part_of').
metadata_db:entity_label('OBO_REL:improper_part_of', 'improper_part_of').
subclass('OBO_REL:improper_part_of', 'OBO_REL:part_of').
metadata_db:entity_resource('OBO_REL:improper_part_of', 'relationship').
metadata_db:entity_obsolete('OBO_REL:improper_part_of', 'property').
is_reflexive('OBO_REL:improper_part_of').
is_transitive('OBO_REL:improper_part_of').
def('OBO_REL:improper_part_of', 'As for part_of, with the additional constraint that subject and object may be identical').
def_xref('OBO_REL:improper_part_of', 'PMID:15892874').
metadata_db:entity_comment('OBO_REL:improper_part_of', 'OBSOLETE. The definition is "As for part_of, with the additional constraint that subject and object may be identical". However, part_of is already reflexive, therefore improper_part_of is identical to part_of. If read differently, as "improper_part_of is part_of but not proper_part_of",improper_part_of becomes identity. So, improper_part_of is either identical to part_of or to identity, and not an intuitive synonym for either of them. [Robert Hoehndorf]').
inverse_of('OBO_REL:improper_part_of', 'OBO_REL:has_improper_part').

