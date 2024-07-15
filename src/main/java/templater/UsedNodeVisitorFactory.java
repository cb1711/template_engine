package templater;

import java.util.Set;

import io.pebbletemplates.pebble.extension.NodeVisitor;
import io.pebbletemplates.pebble.extension.NodeVisitorFactory;
import io.pebbletemplates.pebble.template.PebbleTemplate;
import io.pebbletemplates.pebble.template.PebbleTemplateImpl;

public class UsedNodeVisitorFactory implements NodeVisitorFactory {

    final Set<String> usedVariables;

    public UsedNodeVisitorFactory(Set<String> usedVariable) {
        this.usedVariables = usedVariable;
    }


    @Override
    public NodeVisitor createVisitor(PebbleTemplate template) {
        return new UsedVariableVisitor((PebbleTemplateImpl) template, usedVariables);
    }
}
