package templater;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.pebbletemplates.pebble.extension.AbstractNodeVisitor;
import io.pebbletemplates.pebble.extension.NodeVisitor;
import io.pebbletemplates.pebble.node.BodyNode;
import io.pebbletemplates.pebble.node.IfNode;
import io.pebbletemplates.pebble.node.Node;
import io.pebbletemplates.pebble.node.PrintNode;
import io.pebbletemplates.pebble.node.expression.BinaryExpression;
import io.pebbletemplates.pebble.node.expression.ContextVariableExpression;
import io.pebbletemplates.pebble.node.expression.Expression;
import io.pebbletemplates.pebble.template.PebbleTemplateImpl;
import io.pebbletemplates.pebble.utils.Pair;

public class UsedVariableVisitor extends AbstractNodeVisitor {

    private Set<String> usedVariables;

    private StringBuilder validTMLconstructor;

    public UsedVariableVisitor(final PebbleTemplateImpl template, final Set<String> usedVariables) {
        super(template);
        this.usedVariables = usedVariables;
        this.validTMLconstructor = new StringBuilder();
    }

    @Override
    public void visit(Node node) {
        if (node instanceof PrintNode) {
            validTMLconstructor.append("(@ ");
            visit(((PrintNode) node).getExpression());
            validTMLconstructor.append(" @)");
        }
        if (node instanceof ContextVariableExpression) {
            ContextVariableExpression contextVariableExpr = (ContextVariableExpression) node;
            usedVariables.add(contextVariableExpr.getName());
            getTemplate();
            validTMLconstructor.append("$VAR");
            super.visit(node);
        }
        else if (node instanceof BinaryExpression) {
            BinaryExpression contextVariableExpr = (BinaryExpression) node;
            visit(contextVariableExpr.getLeftExpression());
            visit(contextVariableExpr.getRightExpression());
        }
        else if (node instanceof ColumnExtensionNode) {
            ColumnExtensionNode columnNode = (ColumnExtensionNode) node;
            visit(columnNode.getColumns());
        }
        else if (node instanceof IfNode) {
            IfNode columnNode = (IfNode) node;
            validTMLconstructor.append("$IF");
        }
        else if (node instanceof CustomIfNode) {

        }
        else {
            super.visit(node);
        }
    }

    @Override
    public void visit(IfNode node) {
        for (Pair<Expression<?>, BodyNode> pairs : node.getConditionsWithBodies()) {
            pairs.getLeft().accept(this);
            pairs.getRight().accept(this);
        }
        if (node.getElseBody() != null) {
            node.getElseBody().accept(this);
        }
    }




    public Set<String> getUsedVariables() {
        return new HashSet<>(usedVariables);
    }
}
