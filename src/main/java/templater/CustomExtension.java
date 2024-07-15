package templater;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.pebbletemplates.pebble.attributes.AttributeResolver;
import io.pebbletemplates.pebble.extension.Extension;
import io.pebbletemplates.pebble.extension.ExtensionCustomizer;
import io.pebbletemplates.pebble.extension.Filter;
import io.pebbletemplates.pebble.extension.Function;
import io.pebbletemplates.pebble.extension.NodeVisitorFactory;
import io.pebbletemplates.pebble.extension.Test;
import io.pebbletemplates.pebble.extension.core.MacroAndBlockRegistrantNodeVisitorFactory;
import io.pebbletemplates.pebble.operator.BinaryOperator;
import io.pebbletemplates.pebble.operator.UnaryOperator;
import io.pebbletemplates.pebble.tokenParser.TokenParser;

public class CustomExtension extends ExtensionCustomizer {

    final Set<String> usedVariableSet;

    public CustomExtension(final Extension delegate,
            final Set<String> usedVariableSet) {
        super(delegate);
        this.usedVariableSet = usedVariableSet;
    }

    @Override
    public List<NodeVisitorFactory> getNodeVisitors() {
        List<NodeVisitorFactory> visitors = new ArrayList<>();
        visitors.add(new UsedNodeVisitorFactory(usedVariableSet));
        visitors.add(new MacroAndBlockRegistrantNodeVisitorFactory());
        return visitors;
    }

    @Override
    public List<TokenParser> getTokenParsers() {
        List<TokenParser> visitors = new ArrayList<>();
        visitors.addAll(super.getTokenParsers());
        visitors.add(new CustomIfTokenParser());
        visitors.add(new SetTokenParser());
        return visitors;
    }
}
