package com.tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

public class GenerateAst {
  public static void main(String args[]) throws IOException {
    if (args.length != 1) {
      System.err.println("Usage: generate_ast <output directory>");
      System.exit(64);
    }
    String outputDir = args[0];
    defineAst(outputDir, "Expr", Arrays.asList(
   "Assign    : Token name, Expr value",
        "Binary    : Expr left, Token operator, Expr right",
        "Grouping  : Expr expression",
        "Literal   : Object value",
        "Unary     : Token operator, Expr right",
        "Variable  : Token name"
    ));

    defineAst(outputDir, "Stmt", Arrays.asList(
      "Block      : List<Stmt> statements",
           "Expression : Expr expression",
           "Print      : Expr expression",
           "Var        : Token name, Expr initializer"
    ));
  }

  private static void defineAst(String outputDir, String basename, List<String> types) throws IOException {
    String path = outputDir + "/" + basename + ".java";
    PrintWriter writer = new PrintWriter(path, "UTF-8");

    writer.println("package com.JLox.parser;");
    writer.println();
    writer.println("import java.util.List;");
    writer.println("import com.JLox.scanner.Token;");
    writer.println();
    writer.println("public abstract class " + basename + " {");

    defineVisitor(writer, basename, types);

    // The AST classes.
    for (String type : types) {
      String className = type.split(":")[0].trim();
      String fields = type.split(":")[1].trim();
      defineType(writer, basename, className, fields);
    }

    writer.println();
    writer.println("  public abstract <R> R accept(Visitor<R> visitor);");

    writer.println("}");
    writer.close();
  }

  private static void defineVisitor(PrintWriter writer, String basename, List<String> types) {
    writer.println(" public interface Visitor<R> {");

    for (String type : types) {
      String typeName = type.split(":")[0].trim();
      writer.println("    R visit" + typeName + basename + "(" + typeName + " " + basename.toLowerCase() + ");");
    }

    writer.println("  }");
  }

  private static void defineType(PrintWriter writer, String basename, String className, String fieldList) {
    writer.println();
    writer.println(" public static class " + className + " extends " + basename + " {");

    // Constructor.
    writer.println("    public " + className + "(" + fieldList + ") {");

    // Store parameters in fields
    String fields[] = fieldList.split(", ");
    for (String field : fields) {
      String name = field.split(" ")[1];
      writer.println("     this." + name + " = " + name + ";");
    }

    writer.println("    }");

    // Visitor pattern.
    writer.println();
    writer.println("    @Override");
    writer.println("    public<R> R accept(Visitor<R> visitor) {");
    writer.println("         return visitor.visit" + className + basename + "(this);");
    writer.println("    }");
    
    // Fields
    writer.println();
    for (String field : fields) {
      writer.println("    public final " + field + ";");
    }

    writer.println("  }");

  }
}
