package com.JLox;

import java.util.HashMap;
import java.util.List;

import java.util.ArrayList;
import java.util.Map;

import com.JLox.scanner.Token;
import com.JLox.scanner.TokenType;
import static com.JLox.scanner.TokenType.*;

class Scanner {
  private final String source;
  private final List<Token> tokens = new ArrayList<>();

  private int start = 0;
  private int current = 0;
  private int line = 1;

  private static final Map<String, TokenType> keywords;

  static {
    keywords = new HashMap<>();
    keywords.put("and", And);
    keywords.put("class", Class);
    keywords.put("else", Else);
    keywords.put("false", False);
    keywords.put("for", For);
    keywords.put("fun", Fun);
    keywords.put("if", If);
    keywords.put("nil", Nil);
    keywords.put("or", Or);
    keywords.put("print", Print);
    keywords.put("return", Return);
    keywords.put("super", Super);
    keywords.put("this", This);
    keywords.put("true", True);
    keywords.put("var", Var);
    keywords.put("while", While);
  }

  Scanner(String source) {
    this.source = source;
  }

  List<Token> scanTokens() {
    while (!isAtEnd()) {
      // We are at the beginning of the next lexeme
      start = current;
      scanToken();
    }
    tokens.add(new Token(Eof, "", null, line));
    return tokens;
  }

  private void scanToken() {
    char c = advance();
    switch (c) {
      case '(':
        addToken(LeftParen);
        break;
      case ')':
        addToken(RightParen);
        break;
      case '{':
        addToken(LeftBrace);
        break;
      case '}':
        addToken(RightBrace);
        break;
      case ',':
        addToken(Comma);
        break;
      case '.':
        addToken(Dot);
        break;
      case '-':
        addToken(Minus);
        break;
      case '+':
        addToken(Plus);
        break;
      case ';':
        addToken(Semicolon);
        break;
      case '*':
        addToken(Star);
        break;
      case '!':
        addToken(match('=') ? BangEqual : Bang);
        break;
      case '=':
        addToken(match('=') ? EqualEqual : Equal);
        break;
      case '<':
        addToken(match('=') ? LessEqual : Less);
        break;
      case '>':
        addToken(match('=') ? GreaterEqual : Greater);
        break;
      case '/':
        if (match('/')) {
          while (peek() != '\n' && !isAtEnd())
            advance();
        } else {
          addToken(Slash);
        }
        break;
      case ' ':
      case '\r':
      case '\t':
        break;
      case '\n':
        line++;
        break;
      case '"':
        string();
        break;

      default:
        if (isDigit(c)) {
          number();
        } else if (isAlpha(c)) {
          identifier();
        } else {
          Main.error(line, "Unexpected character.");
        }
        break;
    }
  }

  private void identifier() {
    while (isAlphaNumeric(peek()))
      advance();

    String text = source.substring(start, current);
    TokenType type = keywords.get(text);
    if (text == null)
      type = Identifier;
    addToken(type);
  }

  private void number() {
    while (isDigit(peek()))
      advance();

    // Look for a fractional part
    if (peek() == '.' && isDigit(peekNext())) {
      // Consume the "."
      advance();

      while (isDigit(peek()))
        advance();
    }

    addToken(Number, Double.parseDouble(source.substring(start, current)));

  }

  private void string() {
    while (peek() != '"' && !isAtEnd()) {
      if (peek() == '\n')
        line++;
      advance();
    }

    if (isAtEnd()) {
      Main.error(line, "Unterminated string.");
      return;
    }

    // closing "
    advance();

    String value = source.substring(start + 1, current - 1);
    addToken(String, value);
  }

  private boolean match(char expected) {
    if (isAtEnd())
      return false;
    if (source.charAt(current) != expected)
      return false;

    current++;
    return true;
  }

  // checks next character and return next character (does not moves the scanner
  // to the next character)
  private char peek() {
    if (isAtEnd())
      return '\0';
    return source.charAt(current);
  }

  // just check next character dont consume
  private char peekNext() {
    if (current + 1 > source.length())
      return '\0';
    return source.charAt(current + 1);
  }

  private boolean isAlpha(char c) {
    return (c >= 'a' && c <= 'z') || (c <= 'A' && c >= 'Z') || c == '_';
  }

  private boolean isAlphaNumeric(char c) {
    return isAlpha(c) || isDigit(c);
  }

  private boolean isDigit(char c) {
    return c >= '0' && c <= '9';
  }

  private boolean isAtEnd() {
    return current >= source.length();
  }

  private char advance() {
    current++;
    return source.charAt(current - 1);
  }

  // adds the token provided to tokens list
  private void addToken(TokenType type) {
    addToken(type, null);
  }

  // adds token with its literal to the list
  private void addToken(TokenType type, Object literal) {
    String text = source.substring(start, current);
    tokens.add(new Token(type, text, literal, line));
  }
}
