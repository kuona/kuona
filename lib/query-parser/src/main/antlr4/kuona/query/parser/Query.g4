grammar Query;

@header {
import kuona.query.Query;
}

@members {
Query result = new Query();

public Query getResult() {
  return result;
}
}

query:
 terms
 filters;

terms: (t=TERM {result.addTerm($t.getText());})*;

filters:
  (f=TERM ':' v=TERM {result.addFilter($f.getText(), $v.getText());})*;

TERM: ID_CHAR+;

ID_CHAR:    [a-zA-Z0-9]
            | '.'
            | '-'
            | '_'
            | '/';

WS : [ \t\r\n]+ -> skip ;

