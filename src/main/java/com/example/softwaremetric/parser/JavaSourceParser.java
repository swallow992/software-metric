package com.example.softwaremetric.parser;

import com.example.softwaremetric.model.JavaFieldInfo;
import com.example.softwaremetric.model.JavaMethodInfo;
import com.example.softwaremetric.model.JavaTypeInfo;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NameQualifiedType;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.springframework.stereotype.Component;

@Component
public class JavaSourceParser {

    public List<JavaTypeInfo> parse(Path sourceRoot, Path javaFile, String source) {
        CompilationUnit compilationUnit = parseCompilationUnit(source);
        String packageName = packageName(compilationUnit);
        String sourceFile = sourceRoot.relativize(javaFile).toString().replace('\\', '/');
        List<JavaTypeInfo> types = new ArrayList<>();

        compilationUnit.accept(new ASTVisitor() {
            @Override
            public boolean visit(TypeDeclaration node) {
                types.add(toTypeInfo(sourceFile, packageName, node));
                return true;
            }

            @Override
            public boolean visit(EnumDeclaration node) {
                types.add(toEnumInfo(sourceFile, packageName, node));
                return true;
            }
        });

        return types;
    }

    private CompilationUnit parseCompilationUnit(String source) {
        ASTParser parser = ASTParser.newParser(AST.JLS17);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setSource(source.toCharArray());
        parser.setResolveBindings(false);

        Map<String, String> compilerOptions = JavaCore.getOptions();
        JavaCore.setComplianceOptions(JavaCore.VERSION_17, compilerOptions);
        parser.setCompilerOptions(compilerOptions);

        return (CompilationUnit) parser.createAST(null);
    }

    private JavaTypeInfo toTypeInfo(String sourceFile, String packageName, TypeDeclaration node) {
        String name = node.getName().getIdentifier();
        String kind = node.isInterface() ? "接口" : "类";
        String superClassName = node.getSuperclassType() == null ? "" : node.getSuperclassType().toString();

        List<String> interfaces = node.superInterfaceTypes()
                .stream()
                .map(Object::toString)
                .toList();

        List<JavaFieldInfo> fields = new ArrayList<>();
        for (FieldDeclaration fieldDeclaration : node.getFields()) {
            fields.addAll(toFieldInfo(fieldDeclaration));
        }
        Set<String> fieldNames = fields.stream()
                .map(JavaFieldInfo::name)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));

        List<JavaMethodInfo> methods = new ArrayList<>();
        for (MethodDeclaration methodDeclaration : node.getMethods()) {
            methods.add(toMethodInfo(methodDeclaration, fieldNames));
        }
        List<String> typeDependencies = typeDependencies(node);

        return new JavaTypeInfo(
                sourceFile,
                packageName,
                name,
                qualifiedName(packageName, name),
                kind,
                superClassName,
                interfaces,
                methods,
                fields,
                typeDependencies,
                com.example.softwaremetric.model.CkMetricInfo.empty()
        );
    }

    private JavaTypeInfo toEnumInfo(String sourceFile, String packageName, EnumDeclaration node) {
        String name = node.getName().getIdentifier();
        return new JavaTypeInfo(
                sourceFile,
                packageName,
                name,
                qualifiedName(packageName, name),
                "枚举",
                "",
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                com.example.softwaremetric.model.CkMetricInfo.empty()
        );
    }

    private List<JavaFieldInfo> toFieldInfo(FieldDeclaration fieldDeclaration) {
        List<JavaFieldInfo> fields = new ArrayList<>();
        String type = fieldDeclaration.getType().toString();
        String modifiers = fieldDeclaration.modifiers().toString();
        for (Object fragment : fieldDeclaration.fragments()) {
            VariableDeclarationFragment variable = (VariableDeclarationFragment) fragment;
            fields.add(new JavaFieldInfo(variable.getName().getIdentifier(), type, modifiers));
        }
        return fields;
    }

    private JavaMethodInfo toMethodInfo(MethodDeclaration methodDeclaration, Set<String> fieldNames) {
        String returnType = methodDeclaration.isConstructor()
                ? "构造方法"
                : methodDeclaration.getReturnType2() == null ? "void" : methodDeclaration.getReturnType2().toString();
        List<String> parameters = methodDeclaration.parameters()
                .stream()
                .map(parameter -> {
                    SingleVariableDeclaration variable = (SingleVariableDeclaration) parameter;
                return variable.getType() + " " + variable.getName();
                })
                .toList();
        MethodDetailVisitor methodDetailVisitor = new MethodDetailVisitor(fieldNames);
        if (methodDeclaration.getBody() != null) {
            methodDeclaration.getBody().accept(methodDetailVisitor);
        }

        return new JavaMethodInfo(
                methodDeclaration.getName().getIdentifier(),
                returnType,
                parameters,
                methodDeclaration.modifiers().toString(),
                cyclomaticComplexity(methodDeclaration),
                methodDetailVisitor.invokedMethods(),
                methodDetailVisitor.accessedFields()
        );
    }

    private List<String> typeDependencies(TypeDeclaration node) {
        TypeDependencyVisitor visitor = new TypeDependencyVisitor();
        node.accept(visitor);
        visitor.remove(node.getName().getIdentifier());
        return visitor.dependencies();
    }

    private int cyclomaticComplexity(MethodDeclaration methodDeclaration) {
        ComplexityVisitor visitor = new ComplexityVisitor();
        if (methodDeclaration.getBody() != null) {
            methodDeclaration.getBody().accept(visitor);
        }
        return visitor.complexity();
    }

    private String packageName(CompilationUnit compilationUnit) {
        PackageDeclaration packageDeclaration = compilationUnit.getPackage();
        return packageDeclaration == null ? "" : packageDeclaration.getName().getFullyQualifiedName();
    }

    private String qualifiedName(String packageName, String typeName) {
        if (packageName == null || packageName.isBlank()) {
            return typeName;
        }
        return packageName + "." + typeName;
    }

    private static final class ComplexityVisitor extends ASTVisitor {

        private int complexity = 1;

        int complexity() {
            return complexity;
        }

        @Override
        public boolean visit(IfStatement node) {
            complexity++;
            return true;
        }

        @Override
        public boolean visit(ForStatement node) {
            complexity++;
            return true;
        }

        @Override
        public boolean visit(EnhancedForStatement node) {
            complexity++;
            return true;
        }

        @Override
        public boolean visit(WhileStatement node) {
            complexity++;
            return true;
        }

        @Override
        public boolean visit(DoStatement node) {
            complexity++;
            return true;
        }

        @Override
        public boolean visit(SwitchCase node) {
            if (!node.isDefault()) {
                complexity++;
            }
            return true;
        }

        @Override
        public boolean visit(CatchClause node) {
            complexity++;
            return true;
        }

        @Override
        public boolean visit(ConditionalExpression node) {
            complexity++;
            return true;
        }

        @Override
        public boolean visit(InfixExpression node) {
            if (node.getOperator() == InfixExpression.Operator.CONDITIONAL_AND
                    || node.getOperator() == InfixExpression.Operator.CONDITIONAL_OR) {
                complexity += 1 + node.extendedOperands().size();
            }
            return true;
        }
    }

    private static final class TypeDependencyVisitor extends ASTVisitor {

        private final Set<String> dependencies = new LinkedHashSet<>();

        List<String> dependencies() {
            return List.copyOf(dependencies);
        }

        void remove(String typeName) {
            dependencies.remove(typeName);
        }

        @Override
        public boolean visit(SimpleType node) {
            dependencies.add(node.getName().getFullyQualifiedName());
            return true;
        }

        @Override
        public boolean visit(QualifiedType node) {
            dependencies.add(node.getName().getIdentifier());
            return true;
        }

        @Override
        public boolean visit(NameQualifiedType node) {
            dependencies.add(node.getName().getIdentifier());
            return true;
        }

        @Override
        public boolean visit(ClassInstanceCreation node) {
            dependencies.add(node.getType().toString());
            return true;
        }
    }

    private static final class MethodDetailVisitor extends ASTVisitor {

        private final Set<String> fieldNames;
        private final Set<String> invokedMethods = new LinkedHashSet<>();
        private final Set<String> accessedFields = new LinkedHashSet<>();

        private MethodDetailVisitor(Set<String> fieldNames) {
            this.fieldNames = fieldNames;
        }

        List<String> invokedMethods() {
            return List.copyOf(invokedMethods);
        }

        List<String> accessedFields() {
            return List.copyOf(accessedFields);
        }

        @Override
        public boolean visit(MethodInvocation node) {
            invokedMethods.add(node.getName().getIdentifier());
            return true;
        }

        @Override
        public boolean visit(SuperMethodInvocation node) {
            invokedMethods.add(node.getName().getIdentifier());
            return true;
        }

        @Override
        public boolean visit(ClassInstanceCreation node) {
            invokedMethods.add(node.getType().toString());
            return true;
        }

        @Override
        public boolean visit(SimpleName node) {
            if (fieldNames.contains(node.getIdentifier()) && !isDeclarationName(node)) {
                accessedFields.add(node.getIdentifier());
            }
            return true;
        }

        private boolean isDeclarationName(SimpleName node) {
            ASTNode parent = node.getParent();
            return parent instanceof VariableDeclarationFragment
                    && node.getLocationInParent() == VariableDeclarationFragment.NAME_PROPERTY
                    || parent instanceof SingleVariableDeclaration
                    && node.getLocationInParent() == SingleVariableDeclaration.NAME_PROPERTY
                    || parent instanceof MethodDeclaration
                    && node.getLocationInParent() == MethodDeclaration.NAME_PROPERTY
                    || parent instanceof TypeDeclaration
                    && node.getLocationInParent() == TypeDeclaration.NAME_PROPERTY;
        }
    }
}
