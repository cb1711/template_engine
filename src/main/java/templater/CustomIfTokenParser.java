package templater;

/*
 * This file is part of Pebble.
 *
 * Copyright (c) 2014 by Mitchell Bösecke
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

import io.pebbletemplates.pebble.error.ParserException;
import io.pebbletemplates.pebble.lexer.Token;
import io.pebbletemplates.pebble.lexer.TokenStream;
import io.pebbletemplates.pebble.node.BodyNode;
import io.pebbletemplates.pebble.node.IfNode;
import io.pebbletemplates.pebble.node.RenderableNode;
import io.pebbletemplates.pebble.node.expression.Expression;
import io.pebbletemplates.pebble.parser.Parser;
import io.pebbletemplates.pebble.parser.StoppingCondition;
import io.pebbletemplates.pebble.tokenParser.TokenParser;
import io.pebbletemplates.pebble.utils.Pair;

import java.util.ArrayList;
import java.util.List;

public class CustomIfTokenParser implements TokenParser {

    @Override
    public RenderableNode parse(Token token, Parser parser) {
        TokenStream stream = parser.getStream();
        int lineNumber = token.getLineNumber();
        String currentBlock = parser.peekBlockStack();
        boolean supportsYamlBody = false;
        if (currentBlock != null && currentBlock.equals("connection_config")) {
            supportsYamlBody = true;
        }
        // skip the 'if' token
        stream.next();

        List<Pair<Expression<?>, BodyNode>> conditionsWithBodies = new ArrayList<>();

        Expression<?> expression = parser.getExpressionParser().parseExpression();

        stream.expect(Token.Type.EXECUTE_END);

        BodyNode body = parser.subparse(DECIDE_IF_FORK);

        conditionsWithBodies.add(new Pair<>(expression, body));

        BodyNode elseBody = null;
        boolean end = false;
        while (!end) {
            if (stream.current().getValue() == null) {
                throw new ParserException(
                        null,
                        "Unexpected end of template. Pebble was looking for the \"endif\" tag",
                        stream.current().getLineNumber(), stream.getFilename());
            }

            switch (stream.current().getValue()) {
                case "else":
                    stream.next();
                    stream.expect(Token.Type.EXECUTE_END);
                    elseBody = parser.subparse(tkn -> tkn.test(Token.Type.NAME,
                            "endif_connection_config"));
                    break;

                case "elseif":
                    stream.next();
                    expression = parser.getExpressionParser().parseExpression();
                    stream.expect(Token.Type.EXECUTE_END);
                    body = parser.subparse(DECIDE_IF_FORK);
                    conditionsWithBodies.add(new Pair<>(expression, body));
                    break;

                case "endif_connection_config":
                    stream.next();
                    end = true;
                    break;
                default:
                    throw new ParserException(
                            null,
                            "Unexpected end of template. Pebble was looking for the following tags \"else\", \"elseif\", or \"endif\"",
                            stream.current().getLineNumber(), stream.getFilename());
            }
        }

        stream.expect(Token.Type.EXECUTE_END);
        return new CustomIfNode(lineNumber, true, conditionsWithBodies, elseBody);
    }

    private static final StoppingCondition DECIDE_IF_FORK = token -> token
            .test(Token.Type.NAME, "elseif", "else", "endif_connection_config");

    @Override
    public String getTag() {
        return "if_connection_config";
    }
}

