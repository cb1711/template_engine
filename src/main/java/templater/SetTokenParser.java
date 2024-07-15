package templater;

import io.pebbletemplates.pebble.error.ParserException;
import io.pebbletemplates.pebble.lexer.Token;
import io.pebbletemplates.pebble.lexer.TokenStream;
import io.pebbletemplates.pebble.node.BodyNode;
import io.pebbletemplates.pebble.node.RenderableNode;
import io.pebbletemplates.pebble.node.SetNode;
import io.pebbletemplates.pebble.node.expression.Expression;
import io.pebbletemplates.pebble.parser.Parser;
import io.pebbletemplates.pebble.tokenParser.TokenParser;

public class SetTokenParser implements TokenParser {

    public String getTag(){
        return "extended_columns";
    }

    @Override
    public RenderableNode parse(Token token, Parser parser) {
        TokenStream stream = parser.getStream();
        int lineNumber = token.getLineNumber();

        // skip the "extended_columns" token
        stream.next();
        // expect to see "%}"
        stream.expect(Token.Type.EXECUTE_END);

        parser.pushBlockStack("extended_columns");

        // now we parse the block body
        BodyNode blockBody = parser.subparse(tkn -> tkn.test(Token.Type.NAME,
                "end_extended_columns"));
        parser.popBlockStack();
        Token endblock = stream.current();
        if (!endblock.test(Token.Type.NAME, "end_extended_columns")) {
            throw new ParserException(null,
                    "end_extended_columns tag should be present with block tag starting line number ",
                    token.getLineNumber(), stream.getFilename());
        }
        // skip the 'end_extended_columns' token
        stream.next();
        stream.expect(Token.Type.EXECUTE_END);

        // NodeSet is composed of a name and a value
        return new ColumnExtensionNode(lineNumber, blockBody);
    }

}
