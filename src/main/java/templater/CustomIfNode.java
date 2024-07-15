package templater;

/*
 * This file is part of Pebble.
 * <p>
 * Copyright (c) 2014 by Mitchell Bösecke
 * <p>
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

import io.pebbletemplates.pebble.error.PebbleException;
import io.pebbletemplates.pebble.extension.NodeVisitor;
import io.pebbletemplates.pebble.node.AbstractRenderableNode;
import io.pebbletemplates.pebble.node.BodyNode;
import io.pebbletemplates.pebble.node.expression.Expression;
import io.pebbletemplates.pebble.template.EvaluationContextImpl;
import io.pebbletemplates.pebble.template.PebbleTemplateImpl;
import io.pebbletemplates.pebble.utils.Pair;
import io.pebbletemplates.pebble.utils.TypeUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import static io.pebbletemplates.pebble.utils.TypeUtils.compatibleCast;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

public class CustomIfNode extends AbstractRenderableNode {

    private final List<Pair<Expression<?>, BodyNode>> conditionsWithBodies;

    private final BodyNode elseBody;

    private final boolean supportsYamlBody;

    public CustomIfNode(int lineNumber,
            boolean supportsYamlBody, List<Pair<Expression<?>, BodyNode>> conditionsWithBodies) {
        this(lineNumber, supportsYamlBody, conditionsWithBodies, null);
    }

    public CustomIfNode(int lineNumber,
            boolean supportsYamlBody, List<Pair<Expression<?>, BodyNode>> conditionsWithBodies,
            BodyNode elseBody) {
        super(lineNumber);
        this.supportsYamlBody = supportsYamlBody;
        this.conditionsWithBodies = conditionsWithBodies;
        this.elseBody = elseBody;
    }

    @Override
    public void render(PebbleTemplateImpl self, Writer writer, EvaluationContextImpl context)
            throws IOException {

        boolean satisfied = false;
        for (Pair<Expression<?>, BodyNode> ifStatement: this.conditionsWithBodies) {

            Expression<?> conditionalExpression = ifStatement.getLeft();

            try {

                Object result = conditionalExpression.evaluate(self, context);

                if (result != null) {
                    if (result instanceof Boolean
                            || result instanceof Number
                            || result instanceof String) {
                        satisfied = TypeUtils.compatibleCast(result, Boolean.class);
                    } else {
                        throw new PebbleException(
                                null,
                                String.format(
                                        "Unsupported value type %s. Expected Boolean, String, Number in \"if\" statement",
                                        result.getClass().getSimpleName()),
                                this.getLineNumber(),
                                self.getName());
                    }

                } else if (context.isStrictVariables()) {
                    throw new PebbleException(null,
                            "null value given to if statement and strict variables is set to true",
                            this.getLineNumber(), self.getName());
                }

            } catch (RuntimeException ex) {
                throw new PebbleException(ex, "Wrong operand(s) type in conditional expression",
                        this.getLineNumber(), self.getName());
            }

            if (satisfied) {
                String body = renderBody(self, ifStatement.getRight(), context);
                writer.append(body);
                break;
            }
        }

        if (!satisfied && this.elseBody != null) {
            String body = renderBody(self, elseBody, context);
            writer.append(body);
        }
    }

    private String renderBody(PebbleTemplateImpl self, final BodyNode bodyNode, EvaluationContextImpl context)
            throws IOException {
        Yaml yml = new Yaml();
        Writer bodyWriter = new StringWriter();
        bodyNode.render(self, bodyWriter, context);
        String renderedBody = bodyWriter.toString();
        if (supportsYamlBody) {
            return renderedBody;
        }
        try {
            yml.loadAs(renderedBody, String.class);
        } catch (YAMLException e) {
            throw new PebbleException(null, "Only string supported inside body of if node",
                    bodyNode.getLineNumber(), self.getName());
        }
        return renderedBody;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    public List<Pair<Expression<?>, BodyNode>> getConditionsWithBodies() {
        return this.conditionsWithBodies;
    }

    public BodyNode getElseBody() {
        return this.elseBody;
    }

}
