package com.JLox.parser;

import java.util.List;
import com.JLox.scanner.Token;

public abstract class Expr {
 public interface Visitor<R> {
    R visitAssignExpr(Assign expr);
    R visitBinaryExpr(Binary expr);
    R visitGroupingExpr(Grouping expr);
    R visitLiteralExpr(Literal expr);
    R visitUnaryExpr(Unary expr);
    R visitVariableExpr(Variable expr);
  }

 public static class Assign extends Expr {
    public Assign(Token name, Expr value) {
     this.name = name;
     this.value = value;
    }

    @Override
    public<R> R accept(Visitor<R> visitor) {
         return visitor.visitAssignExpr(this);
    }

    public final Token name;
    public final Expr value;
  }

 public static class Binary extends Expr {
    public Binary(Expr left, Token operator, Expr right) {
     this.left = left;
     this.operator = operator;
     this.right = right;
    }

    @Override
    public<R> R accept(Visitor<R> visitor) {
         return visitor.visitBinaryExpr(this);
    }

    public final Expr left;
    public final Token operator;
    public final Expr right;
  }

 public static class Grouping extends Expr {
    public Grouping(Expr expression) {
     this.expression = expression;
    }

    @Override
    public<R> R accept(Visitor<R> visitor) {
         return visitor.visitGroupingExpr(this);
    }

    public final Expr expression;
  }

 public static class Literal extends Expr {
    public Literal(Object value) {
     this.value = value;
    }

    @Override
    public<R> R accept(Visitor<R> visitor) {
         return visitor.visitLiteralExpr(this);
    }

    public final Object value;
  }

 public static class Unary extends Expr {
    public Unary(Token operator, Expr right) {
     this.operator = operator;
     this.right = right;
    }

    @Override
    public<R> R accept(Visitor<R> visitor) {
         return visitor.visitUnaryExpr(this);
    }

    public final Token operator;
    public final Expr right;
  }

 public static class Variable extends Expr {
    public Variable(Token name) {
     this.name = name;
    }

    @Override
    public<R> R accept(Visitor<R> visitor) {
         return visitor.visitVariableExpr(this);
    }

    public final Token name;
  }

  public abstract <R> R accept(Visitor<R> visitor);
}
