package com.JLox;

import java.util.*;
import java.util.ArrayList;
import com.JLox.scanner.TokenType;
import com.JLox.parser.Expr;
import com.JLox.parser.Stmt;
import com.JLox.scanner.Token;
import static com.JLox.scanner.TokenType.*;

public class Parser {
  private static class ParseError extends RuntimeException {}

  private final List<Token> tokens;
  private int current = 0;

  Parser(List<Token> tokens) {
    this.tokens = tokens;
  }

  List<Stmt> parse() {
    // try {
      //   return expression();
      // } catch (ParseError error) {
        //   return null;
        // }
        
      List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
          statements.add(statement());
        }
        return statements;
      }

  private Expr expression() {
    return equality();
  }

  private Stmt statement() {
    if (match(Print)) return printStatement();

    return expressionStatement();
  }

  private Stmt printStatement() {
    Expr value = expression();
    consume(Semicolon, "Expect ';' after value.");
    return new Stmt.Print(value);
  }

  private Stmt expressionStatement() {
    Expr expr = expression();
    consume(Semicolon, "Expect ';' after expression.");
    return new Stmt.Expression(expr);
  }

  private Expr equality() {
    Expr expr = comparison();
    
    while (match(BangEqual, EqualEqual))  {
      Token operator = previous();
      Expr right = comparison();
      expr = new Expr.Binary(expr, operator, right);
    }
    return expr;
  }

  private Expr comparison() {
    Expr expr = term();

    while(match(Greater, GreaterEqual, Less, LessEqual)) {
      Token operator = previous();
      Expr right = term();
      expr = new Expr.Binary(expr, operator, right);
    }
    return expr;
  }

  private Expr term() {
    Expr expr = factor();

    while(match(Plus, Minus)) {
      Token operator = previous();
      Expr right = factor();
      expr = new Expr.Binary(expr, operator, right);
    }
    return expr;
  }

  private Expr factor() {
    Expr expr = unary();

    while(match(Slash, Star)) {
      Token operator = previous();
      Expr right = unary();
      expr = new Expr.Binary(expr, operator, right);
    }
    return expr;
  }

  private Expr unary() {
    if(match(Bang, Minus)) {
      Token operator = previous();
      Expr right = unary();
      return new Expr.Unary(operator, right);
    }
    return primary();
  }

  private Expr primary() {
    if(match(False)) return new Expr.Literal(false);
    if(match(True)) return new Expr.Literal(true);
    if(match(Nil)) return new Expr.Literal(null);

    if(match(Number, String)) {
      return new Expr.Literal(previous().literal);
    }

    if(match(LeftParen)) {
      Expr expr = expression();
      consume(RightParen, "Expect ')' after expression.");
      return new Expr.Grouping(expr);
    }

    throw error(peek(), "Expect expression.");
  }

  private boolean match(TokenType... types) {
    for (TokenType type : types) {
      if(check(type)) {
        advance();
        return true;
      }
    }
    return false;
  }

  private Token consume(TokenType type, String message) {
    if(check(type)) return advance();

    throw error(peek(), message);
  }

  private ParseError error(Token token, String message) {
    Main.error(token, message);
    return new ParseError();
  }

  
  private void synchronize() {
    advance();

    while (!isAtEnd()) {
      if (previous().type == Semicolon) return;

      switch (peek().type) {
        case Class:
        case Fun:
        case Var:
        case For:
        case If:
        case While:
        case Print:
        case Return:
          return;
      }

      advance();
    }
  }

  private boolean check(TokenType type) {
    if (isAtEnd()) return false;
    return peek().type == type;
  }

  private Token advance() {
    if (!isAtEnd()) current++;
    return previous();
  }

  private boolean isAtEnd() {
    return peek().type == Eof;
  }

  private Token peek() {
    return tokens.get(current);
  }

  private Token previous() {
    return tokens.get(current-1);
  }
}
