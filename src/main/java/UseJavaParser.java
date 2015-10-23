import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.*;

/**
 * Created by Chang on 10/02/15.
 */
public class UseJavaParser {
    //Attributes for JavaParser
    static List<String> classNames;
    static List<String> interfaceNames;
    static List<AssociationItem> associationItemList;  //Finds association and multiplicity
    static List<ExtendItem> extendItemList; //Saves all the extend relationship
    static Set<UseInterfaceItem> useInterfaceList; //Finds ball and socket interface
    static List<ImplementInterfaceItem> implementInterfaceList;

    static List<String> classStrUML;  // string input to umlGenerator for generating UML class
    static List<String> associationStrUML; // string input to umlGenerator for generating association UML
    static List<String> extendStrUML; // string input to umlGenerator for generating extend relation
    static List<String> interfaceStrUML; // string input ot umlGenerator for generating ball and socket form of interface

    class AssociationItem {
        String startName;
        String endName;
        String attributeName;
        boolean ifMultiple;
    }

    class ExtendItem {
        String superClassName;
        String subClassName;
    }

    class UseInterfaceItem {
        String interfaceName;
        String useName;

        @Override
        public int hashCode() {
            int hashcode = 0;
            hashcode = interfaceName.hashCode() * 20;
            hashcode += useName.hashCode();
            return hashcode;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof UseInterfaceItem) {
                UseInterfaceItem item = (UseInterfaceItem) obj;
                return (item.interfaceName.equals(this.interfaceName) && item.useName.equals(this.useName));
            } else {
                return false;
            }
        }
    }

    class ImplementInterfaceItem {
        String interfaceName;
        String implementName;
    }

    //1. used to save output from ClassVisitor
    static String nameClassVisitor;
    static boolean isInterfaceClassVisitor;
    static int modifierClassVisitor;
    static List<ClassOrInterfaceType> extendClassVisitor;
    static List<ClassOrInterfaceType> implementClassVisitor;

    //2. used to save output from MethodVisitor
    static List<String> nameMethodVisitor;
    static List<Integer> modifierMethodVisitor;
    static List<String> typeMethodVisitor;
    static List<List<Parameter>> parameterListMethodVisitor;

    //3. used to save output from FieldVisitor
    static List<String> nameFieldVisitor;
    static List<Integer> modifierFieldVistor;
    static List<String> typeFieldVisitor;

    //4. used to save output from ConstructorVisitor
    static List<String> nameConstructorVisitor;
    static List<Integer> modifierConstructorVisitor;
    static List<List<Parameter>> parameterListConstructorVisitor;

    //5. used to save inner attribute types of methods
    static ArrayList<String> innerAttributeTypes = new ArrayList<String>();

    UseJavaParser() {
        classNames = new ArrayList<String>();
        interfaceNames = new ArrayList<String>();

        associationItemList = new ArrayList<AssociationItem>();
        extendItemList = new ArrayList<ExtendItem>();
        useInterfaceList = new LinkedHashSet<UseInterfaceItem>();
        implementInterfaceList = new ArrayList<ImplementInterfaceItem>();

        classStrUML = new ArrayList<String>();
        associationStrUML = new ArrayList<String>();
        extendStrUML = new ArrayList<String>();
        interfaceStrUML = new ArrayList<String>();

        extendClassVisitor = new ArrayList<ClassOrInterfaceType>();
        implementClassVisitor = new ArrayList<ClassOrInterfaceType>();

        nameMethodVisitor = new ArrayList<String>();
        modifierMethodVisitor = new ArrayList<Integer>();
        typeMethodVisitor = new ArrayList<String>();
        parameterListMethodVisitor = new ArrayList<List<Parameter>>();

        nameFieldVisitor = new ArrayList<String>();
        modifierFieldVistor = new ArrayList<Integer>();
        typeFieldVisitor = new ArrayList<String>();

        nameConstructorVisitor = new ArrayList<String>();
        modifierConstructorVisitor = new ArrayList<Integer>();
        parameterListConstructorVisitor = new ArrayList<List<Parameter>>();

        innerAttributeTypes = new ArrayList<String>();
    }

    //1. try to visit class and interface names
    public static class ClassVisitor extends VoidVisitorAdapter {
        @Override
        public void visit(ClassOrInterfaceDeclaration n, Object arg) {

            nameClassVisitor = n.getName();
            isInterfaceClassVisitor = n.isInterface();
            extendClassVisitor = n.getExtends();
            implementClassVisitor = n.getImplements();
            modifierClassVisitor = n.getModifiers();
            System.out.println("Class name is: " + n.getName());//print class name
        }

    }

    //2. try to visit method in the class
    public static class MethodVisitor extends VoidVisitorAdapter {

        @Override
        public void visit(MethodDeclaration n, Object arg) {
            modifierMethodVisitor.add(n.getModifiers());
            nameMethodVisitor.add(n.getName());
            typeMethodVisitor.add(n.getType().toString());
            parameterListMethodVisitor.add(n.getParameters());
            System.out.println(n.getName());//print method name
        }
    }

    //3. visit attribute
    public static class FieldVisitor extends VoidVisitorAdapter {
        @Override
        public void visit(FieldDeclaration n, Object arg) {
            typeFieldVisitor.add(n.getType().toString());
            nameFieldVisitor.add(n.getVariables().get(0).toString());
            modifierFieldVistor.add(n.getModifiers());
        }
    }

    //4. visit constructor
    public static class ConstructorVisitor extends VoidVisitorAdapter {
        @Override
        public void visit(ConstructorDeclaration n, Object arg) {
            modifierConstructorVisitor.add(n.getModifiers());
            nameConstructorVisitor.add(n.getName());
            parameterListConstructorVisitor.add(n.getParameters());
        }
    }

    //5. visit inner attributes in methods
    public static class VariableDecVisitor extends VoidVisitorAdapter {
        @Override
        public void visit(VariableDeclarationExpr n, Object arg) {
            innerAttributeTypes.add(n.getType().toString());
        }
    }

    //1. create class UML & save use of interfaces & save association
    public void createClassStrUML() {
        String source = "";
        if (isInterfaceClassVisitor) {
            source += "interface " + nameClassVisitor + " {\n";
        } else {
            if (ModifierSet.isAbstract(modifierClassVisitor)) {
                source += "abstract class " + nameClassVisitor + " {\n";
            } else {
                source += "class " + nameClassVisitor + " {\n";
            }
        }

        //A. Making UML FIELD string
        for (String field : nameFieldVisitor) {
            //1. create field string of class UML
            int index = nameFieldVisitor.indexOf(field);
            // if field has associations with other classes, then it will not be printed in the class UML, but put into associationItemMap
            String substr1 = "";
            if (typeFieldVisitor.get(index).indexOf('[') >= 0) {
                substr1 += typeFieldVisitor.get(index).substring(0, typeFieldVisitor.get(index).indexOf('['));
            } else if (typeFieldVisitor.get(index).contains("Collection") || typeFieldVisitor.get(index).contains("List") || typeFieldVisitor.get(index).contains("Map") || typeFieldVisitor.get(index).contains("Set")) {
                substr1 += typeFieldVisitor.get(index).substring(typeFieldVisitor.get(index).indexOf('<') + 1, typeFieldVisitor.get(index).indexOf('>'));
            }

            if (classNames.indexOf(typeFieldVisitor.get(index)) >= 0 || classNames.indexOf(substr1) >= 0
                    || interfaceNames.indexOf(typeFieldVisitor.get(index)) >= 0 || interfaceNames.indexOf(substr1) >= 0) {
                AssociationItem associationItem = new AssociationItem();
                associationItem.startName = nameClassVisitor;
                if (substr1 != "") {
                    associationItem.endName = substr1;
                }
                else {
                    associationItem.endName = typeFieldVisitor.get(index);
                }

                associationItem.attributeName = field;

                if (substr1 != "") {
                    associationItem.ifMultiple = true;
                } else {
                    associationItem.ifMultiple = false;
                }
                associationItemList.add(associationItem);
            } else {

                String typefieldstr = "";
                if (typeFieldVisitor.get(index).indexOf('[') >= 0) {
                    typefieldstr += typeFieldVisitor.get(index).substring(0, typeFieldVisitor.get(index).indexOf('['));
                    typefieldstr += "(*)";
                } else if (typeFieldVisitor.get(index).contains("Collection") || typeFieldVisitor.get(index).contains("List") || typeFieldVisitor.get(index).contains("Map") || typeFieldVisitor.get(index).contains("Set")) {
                    typefieldstr += typeFieldVisitor.get(index).substring(typeFieldVisitor.get(index).indexOf('<') + 1, typeFieldVisitor.get(index).indexOf('>'));
                    typefieldstr += "(*)";
                } else {
                    typefieldstr += typeFieldVisitor.get(index);
                }

                if (ModifierSet.isPublic(modifierFieldVistor.get(index))) {
                    source += "+" + field + ":" + typefieldstr + "\n";
                } else if (ModifierSet.isPrivate(modifierFieldVistor.get(index))) {
                    source += "-" + field + ":" + typefieldstr + "\n";
                }
            }
        }

        source += "__\n";

        //B. making constructor UML String
        for (String methodName : nameConstructorVisitor) {
            int index = nameConstructorVisitor.indexOf(methodName);
            if (ModifierSet.isPublic(modifierConstructorVisitor.get(index))) {
                String parameterStr = "";

                for (Parameter parameterSingle : parameterListConstructorVisitor.get(index)) {
                    String[] parts = parameterSingle.toString().split(" ");
                    parameterStr += parts[1] + ":" + parameterSingle.getType();
                    if (parameterListConstructorVisitor.get(index).indexOf(parameterSingle) + 1 != parameterListConstructorVisitor.get(index).size())
                        parameterStr += ",";
                }
                source += "+" + methodName + "(" + parameterStr + ")" + "\n";
            }

            //find if any use of interface in parameters, save to useInterfaceList
            for (Parameter parameterSingle : parameterListConstructorVisitor.get(index)) {
                String substr1 = "";
                String paramtertype = parameterSingle.getType().toString();

                if (paramtertype.indexOf('[') >= 0) {
                    substr1 += paramtertype.substring(0, paramtertype.indexOf('['));
                } else if (paramtertype.contains("Collection") || paramtertype.contains("List") || paramtertype.contains("Map") || paramtertype.contains("Set")) {
                    substr1 += paramtertype.substring(paramtertype.indexOf('<') + 1, paramtertype.indexOf('>'));
                } else
                    substr1 += paramtertype;

                for (String interfaceName : interfaceNames) {
                    if (interfaceName.equals(substr1)) {
                        UseInterfaceItem useInterfaceItem = new UseInterfaceItem();
                        useInterfaceItem.interfaceName = interfaceName;
                        useInterfaceItem.useName = nameClassVisitor;

                        //if use is a class, added to useInterfaceList, ignore used by a interface
                        if (classNames.contains(nameClassVisitor))
                            useInterfaceList.add(useInterfaceItem);
                    }
                }
            }
        }


        //System.out.print("methodvisitor: "+nameMethodVisitor);
        //C. making method UML String
        for (String methodName : nameMethodVisitor) {
            int index = nameMethodVisitor.indexOf(methodName);
            if (ModifierSet.isPublic(modifierMethodVisitor.get(index)) || interfaceNames.contains(nameClassVisitor)) {
                String parameterStr = "";

                for (Parameter parameterSingle : parameterListMethodVisitor.get(index)) {
                    String[] parts = parameterSingle.toString().split(" ");
                    parameterStr += parts[1] + ":" + parameterSingle.getType();
                    if (parameterListMethodVisitor.get(index).indexOf(parameterSingle) + 1 != parameterListMethodVisitor.get(index).size())
                        parameterStr += ",";
                }

                source += "+" + methodName + "(" + parameterStr + "):" + typeMethodVisitor.get(index) + "\n";
            }


            //find if any use of interface in parameters, save to useInterfaceList
            for (Parameter parameterSingle : parameterListMethodVisitor.get(index)) {
                String substr1 = "";
                String paramtertype = parameterSingle.getType().toString();

                if (paramtertype.indexOf('[') >= 0) {
                    substr1 += paramtertype.substring(0, paramtertype.indexOf('['));
                } else if (paramtertype.contains("Collection") || paramtertype.contains("List") || paramtertype.contains("Map") || paramtertype.contains("Set")) {
                    substr1 += paramtertype.substring(paramtertype.indexOf('<') + 1, paramtertype.indexOf('>'));
                } else
                    substr1 += paramtertype;

                for (String interfaceName : interfaceNames) {
                    if (interfaceName.equals(substr1)) {
                        UseInterfaceItem useInterfaceItem = new UseInterfaceItem();
                        useInterfaceItem.interfaceName = interfaceName;
                        useInterfaceItem.useName = nameClassVisitor;

                        //if use is a class, added to useInterfaceList, ignore used by a interface
                        if (classNames.contains(nameClassVisitor))
                            useInterfaceList.add(useInterfaceItem);
                    }
                }
            }


            //find if any use of interface in return type, save to useInterfaceList
            String substr1 = "";
            String returntype = typeMethodVisitor.get(index);
            if (returntype.indexOf('[') >= 0) {
                substr1 += returntype.substring(0, returntype.indexOf('['));
            } else if (returntype.contains("Collection") || returntype.contains("List") || returntype.contains("Map") || returntype.contains("Set")) {
                substr1 += returntype.substring(returntype.indexOf('<') + 1, returntype.indexOf('>'));
            } else
                substr1 += returntype;

            for (String interfaceName : interfaceNames) {
                if (interfaceName.equals(substr1)) {
                    UseInterfaceItem useInterfaceItem = new UseInterfaceItem();
                    useInterfaceItem.interfaceName = interfaceName;
                    useInterfaceItem.useName = nameClassVisitor;

                    //if use is a class, added to useInterfaceList, ignore use by a interface
                    if (classNames.contains(nameClassVisitor))
                        useInterfaceList.add(useInterfaceItem);
                }
            }

        }
        source += "}\n";

        //D. find if any use of interface inside a method
        for (String innervarType : innerAttributeTypes) {
            for (String interfaceName : interfaceNames) {
                if (interfaceName.equals(innervarType)) {
                    UseInterfaceItem useInterfaceItem = new UseInterfaceItem();
                    useInterfaceItem.interfaceName = interfaceName;
                    useInterfaceItem.useName = nameClassVisitor;

                    //if use is a class, added to useInterfaceList, ignore use by a interface
                    if (classNames.contains(nameClassVisitor))
                        useInterfaceList.add(useInterfaceItem);
                }
            }
        }

        classStrUML.add(source);//print class string for UML
    }

    //2. create association UML
    public void createAssociationStrUML() {
        String source = "";
        while (!associationItemList.isEmpty()) {
            String class1 = associationItemList.get(0).startName;
            String class2 = associationItemList.get(0).endName;

            int i = 0;
            for (; i < associationItemList.size(); i++) {
                if (associationItemList.get(i).startName.equals(class2) && associationItemList.get(i).endName.equals(class1)) {
                    break;
                }
            }
            if (i < associationItemList.size()) {
                if (associationItemList.get(0).ifMultiple && associationItemList.get(i).ifMultiple) {
                    source += class1 + " \"*\"" + "--" + "\"*\" " + class2 + "\n";
                } else if (associationItemList.get(0).ifMultiple) {
                    source += class1 + " \"1\"" + " --" + "\"*\" " + class2 + "\n";
                } else if (associationItemList.get(i).ifMultiple) {
                    source += class1 + " \"*\"" + "-- " + "\"1\" " + class2 + "\n";
                } else {
                    source += class1 + " \"1\"" + " -- " + "\"1\" " + class2 + "\n";
                }
                associationItemList.remove(i);
                associationItemList.remove(0);
            } else {
                if (associationItemList.get(0).ifMultiple) {
                    if (associationItemList.get(0).endName.toUpperCase().equals(associationItemList.get(0).attributeName.toUpperCase())) {
                        source += class1 + " --" + "\"*\" " + class2 + "\n";
                    } else {
                        //source += class1 + " --" + "\"*\" " + class2 +":" + associationItemMap.get(0).attributeName + "\n";
                        source += class1 + " --" + "\"*\" " + class2 + "\n";
                    }

                } else {
                    //source += class1 + " --" + "\"1\" " + class2 +":" + associationItemMap.get(0).attributeName + "\n";
                    source += class1 + " --" + "\"1\" " + class2 + "\n";
                }
                associationItemList.remove(0);
            }
        }

        associationStrUML.add(source);
    }


    //3. create extend relation UML
    public void createExtendStrUML() {
        String source = "";
        for (ExtendItem item : extendItemList) {
            source += item.superClassName + " <|-- " + item.subClassName + "\n";
        }
        extendStrUML.add(source);
    }

    //4. create Interface UML
    public void createInterfaceStrUML() {
        String source = "";
        int usecase1 = 0;
        int usecase2 = 1;

        for (ImplementInterfaceItem item : implementInterfaceList) {
            source += item.interfaceName + " <|.. " + item.implementName + "\n";
        }

        for (UseInterfaceItem item : useInterfaceList) {
            source += item.useName + " ..> " + item.interfaceName + ": use\n";
        }
        interfaceStrUML.add(source);
    }

    public void clearTempStaticClass() {

        nameMethodVisitor.clear();
        modifierMethodVisitor.clear();
        typeMethodVisitor.clear();
        parameterListMethodVisitor.clear();

        nameFieldVisitor.clear();
        modifierFieldVistor.clear();
        typeFieldVisitor.clear();

        nameConstructorVisitor.clear();
        modifierConstructorVisitor.clear();
        parameterListConstructorVisitor.clear();

        innerAttributeTypes.clear();
    }
}
