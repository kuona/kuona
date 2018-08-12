package kuona.query;

import kuona.query.parser.QueryLexer;
import kuona.query.parser.QueryParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.IOException;
import java.io.InputStream;

public class Parser {
  public static Query parse(InputStream stream) {
    try {
      var lexer = new QueryLexer(CharStreams.fromStream(stream));
      var tokenStream = new CommonTokenStream(lexer);
      var parser = new QueryParser(tokenStream);
      parser.query();
      return parser.getResult();

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
