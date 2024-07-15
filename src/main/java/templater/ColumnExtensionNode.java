package templater;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import io.pebbletemplates.pebble.extension.NodeVisitor;
import io.pebbletemplates.pebble.node.AbstractRenderableNode;
import io.pebbletemplates.pebble.node.BodyNode;
import io.pebbletemplates.pebble.template.EvaluationContextImpl;
import io.pebbletemplates.pebble.template.PebbleTemplateImpl;

public class ColumnExtensionNode extends AbstractRenderableNode {

    private final BodyNode columns;

    public ColumnExtensionNode(int lineNumber, BodyNode columns) {
        super(lineNumber);
        this.columns = columns;

    }

    @Override
    public void render(final PebbleTemplateImpl self, final Writer writer,
            final EvaluationContextImpl context) throws IOException {
        // Not rendered directly. But will be added later on.
    }

    @Override
    public void accept(final NodeVisitor visitor) {
        visitor.visit(this);
    }

    public BodyNode getColumns() {
        return columns;
    }
}
