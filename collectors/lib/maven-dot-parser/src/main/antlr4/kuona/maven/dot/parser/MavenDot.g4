grammar MavenDot;

graph:
  | DIGRAPH artifact '{' dependency*  '}';

artifact: QUOTE
  (
      groupid=ID SEPARATOR  artifactid=ID SEPARATOR packageing=ID SEPARATOR version=ID
  |   groupid=ID SEPARATOR  artifactid=ID SEPARATOR packageing=ID SEPARATOR version=ID SEPARATOR  scope=ID
  |   groupid=ID SEPARATOR  artifactid=ID SEPARATOR packageing=ID SEPARATOR unknown=ID SEPARATOR version=ID SEPARATOR  scope=ID
  ) QUOTE ;

dependency: from=artifact ARROW to=artifact SEMI ;

DIGRAPH: 'digraph';
QUOTE : '"';
SEMI: ';' ;
ARROW: '->';
SEPARATOR: ':';
ID : ID_CHAR+ ;
ID_CHAR:    [a-zA-Z0-9]
            | '.'
            | '-'
            | '_'
            | '/';

WS : [ \t\r\n]+ -> skip ;

