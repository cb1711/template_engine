package templater;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.yaml.snakeyaml.Yaml;
import io.pebbletemplates.pebble.PebbleEngine;
import io.pebbletemplates.pebble.extension.Extension;
import io.pebbletemplates.pebble.extension.ExtensionCustomizer;
import io.pebbletemplates.pebble.extension.NodeVisitor;
import io.pebbletemplates.pebble.extension.core.CoreExtension;
import io.pebbletemplates.pebble.lexer.LexerImpl;
import io.pebbletemplates.pebble.lexer.Syntax;
import io.pebbletemplates.pebble.lexer.TokenStream;
import io.pebbletemplates.pebble.loader.StringLoader;
import io.pebbletemplates.pebble.node.IfNode;
import io.pebbletemplates.pebble.node.RootNode;
import io.pebbletemplates.pebble.node.expression.ContextVariableExpression;
import io.pebbletemplates.pebble.parser.Parser;
import io.pebbletemplates.pebble.parser.ParserImpl;
import io.pebbletemplates.pebble.parser.ParserOptions;
import io.pebbletemplates.pebble.template.PebbleTemplate;
import io.pebbletemplates.pebble.template.PebbleTemplateImpl;

public class TemplateEngine {

    private static String readFileAsString(String fileName) throws IOException {
        String data = "";
        data = new String(
                Files.readAllBytes(Paths.get(fileName)));
        return data;
    }



    public static void main(String args[]) throws IOException {
        Set<String> usedVariables = new HashSet<>();
        Syntax syntax = new Syntax.Builder().setPrintOpenDelimiter("(@").setPrintCloseDelimiter(
                "@)").setExecuteOpenDelimiter("(%").setExecuteCloseDelimiter("%)").setEnableNewLineTrimming(false).build();
        PebbleEngine engine = new PebbleEngine.Builder()
                .strictVariables(false)
                .syntax(syntax)
                .extension(new CustomExtension(new CoreExtension(), usedVariables))
                .build();
        PebbleTemplate compiledTemplate = engine.getTemplate("ax2.tml");
        String data = readFileAsString("/Users/chaitanya.bhatia/Documents/template/src/main/resources/abcd.tml");
        Writer writer = new StringWriter();
        Map<String, Object> context = new HashMap<>();
        context.put("state", "karnataka");
        context.put("columntype", "karnataka");
        Map<String, String> birds = new HashMap<>();
        birds.put("fly", "hawk");
        birds.put("nofly", "EMU");
        Map<String, String> ship = new HashMap<>();
        ship.put("Mast", "35ft");
        ship.put("Rudder", "Broken Needs Replacement");
        ship.put("Sails", "Hoisted");
        context.put("Ship", ship);
        context.put("birds", birds);
        context.put("AdHoc", "abcd");
        compiledTemplate.evaluate(writer, context);
        if (((PebbleTemplateImpl) compiledTemplate).hasBlock("extended_columns")) {
            Writer extendedColumnsWriter = new StringWriter();
            compiledTemplate.evaluateBlock("extended_columns", extendedColumnsWriter, context);
            String extendedColumnsString = extendedColumnsWriter.toString();
        }
        String output = writer.toString();
        long time = System.currentTimeMillis();
        PebbleTemplate compiledTemplate2 = engine.getTemplate("abcd.tml");
        Writer writer2 = new StringWriter();
        compiledTemplate2.evaluate(writer2, context);
        String output2 = writer2.toString();
        long elapsed = System.currentTimeMillis() - time;
        System.out.println(elapsed);
    }
}
