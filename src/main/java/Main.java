import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.type.ClassOrInterfaceType;


import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) throws Exception {

        //find all .java source files in the given folderm and save all the files' path into filePaths
        String folderPath = "../uml-parser-test-5/";
        ArrayList<String> filePaths = new ArrayList<String>();
        String fileString;
        File folder = new File(folderPath);
        File[] listofFiles = folder.listFiles();
        for (File file : listofFiles) {
            if (file.isFile()) {
                fileString = file.getName();
                if (fileString.endsWith(".java") || fileString.endsWith(".JAVA"))
                    filePaths.add(fileString);
            }
        }

        UseJavaParser useJavaParser = new UseJavaParser();
        //1.Find names for all classes and interfaces; the class names extended and implemented in each .java file as well
        for (String filePath : filePaths) {
            FileInputStream in = new FileInputStream(folderPath + filePath);
            CompilationUnit cu = JavaParser.parse(in);
            in.close();

            new UseJavaParser.ClassVisitor().visit(cu, null);

            if (useJavaParser.isInterfaceClassVisitor) {
                useJavaParser.interfaceNames.add(useJavaParser.nameClassVisitor);
            } else {
                useJavaParser.classNames.add(useJavaParser.nameClassVisitor);
            }

            if (useJavaParser.extendClassVisitor != null) {
                for (ClassOrInterfaceType item : useJavaParser.extendClassVisitor) {
                    UseJavaParser.ExtendItem extendItem = useJavaParser.new ExtendItem();
                    extendItem.subClassName = useJavaParser.nameClassVisitor;
                    extendItem.superClassName = item.getName();
                    useJavaParser.extendItemList.add(extendItem);
                }
            }

            if (useJavaParser.implementClassVisitor != null) {
                for (ClassOrInterfaceType item : useJavaParser.implementClassVisitor) {
                    UseJavaParser.ImplementInterfaceItem implementInterfaceItem = useJavaParser.new ImplementInterfaceItem();
                    implementInterfaceItem.implementName = useJavaParser.nameClassVisitor;
                    implementInterfaceItem.interfaceName = item.getName();
                    useJavaParser.implementInterfaceList.add(implementInterfaceItem);
                }
            }
        }


        //2.Creates class UML string for each .java file
        for (String filePath : filePaths) {
            FileInputStream in = new FileInputStream(folderPath + filePath);
            CompilationUnit cu = JavaParser.parse(in);
            in.close();

            new UseJavaParser.ClassVisitor().visit(cu, null);
            new UseJavaParser.MethodVisitor().visit(cu, null);
            new UseJavaParser.FieldVisitor().visit(cu, null);
            new UseJavaParser.ConstructorVisitor().visit(cu, null);
            new UseJavaParser.VariableDecVisitor().visit(cu, null);

            useJavaParser.createClassStrUML(); //create UML string for both interface and normal class
            useJavaParser.clearTempStaticClass();
        }

        for (UseJavaParser.AssociationItem item : useJavaParser.associationItemList)
            System.out.println("startName:" + item.startName + " endname: " + item.endName + " attribute:" + item.attributeName + item.ifMultiple);

        //3.Creates association UML string for java classes
        useJavaParser.createAssociationStrUML();

        //4.Creates extend relation UML string between java classes
        useJavaParser.createExtendStrUML();

        //5.Creates ball and socket UML string
        useJavaParser.createInterfaceStrUML();

        //call UMLGenerator that translates string output into plantUML
        UMLGenerator.umlGenerator(useJavaParser.classStrUML, useJavaParser.associationStrUML, useJavaParser.extendStrUML, useJavaParser.interfaceStrUML);

        //test if interfaceNames isEmpty?
        System.out.println(useJavaParser.interfaceNames.isEmpty());
    }
}
