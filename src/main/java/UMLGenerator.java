import net.sourceforge.plantuml.SourceStringReader;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Collection;

/**
 * Created by Chang on 10/02/15.
 */
public class UMLGenerator {
    public static void umlGenerator(Collection<String> classStrUML, Collection<String> associationStrUML, Collection<String> extendStrUML, Collection<String> ballsocketStrUML) throws Exception {
        OutputStream png = new FileOutputStream("test.png");
        String sourceString = "@startuml\n";
        sourceString += "title UML Diagram Chang\n";
        sourceString += "skinparam classAttributeIconSize 0\n";
        sourceString += "skinparam usecaseBackgroundColor #A80036\n";
        sourceString += "skinparam usecaseBorderColor Transparent\n";
        sourceString += "skinparam usecaseFontSize 1\n";
        sourceString += "skinparam usecaseFontColor #A80036\n";

        for (String item : classStrUML) {
            sourceString += item;
        }

        for (String item : associationStrUML) {
            sourceString += item;
        }

        for (String item : extendStrUML) {
            sourceString += item;
        }

        for (String item : ballsocketStrUML) {
            sourceString += item;
        }

        sourceString += "@enduml\n";
        SourceStringReader reader = new SourceStringReader(sourceString);
        String desc = reader.generateImage(png); // Writes the first image to png
    }
}
