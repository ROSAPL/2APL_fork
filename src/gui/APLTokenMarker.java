package gui;

import org.gjt.sp.jedit.syntax.*;
import javax.swing.text.Segment;


public class APLTokenMarker extends TokenMarker
{
	
	private final static byte ATOM = 100;
	private final static byte HEAD = Token.KEYWORD1;
	private final static byte LITERAL1 = Token.KEYWORD2;
	private final static byte LITERAL2 = Token.LITERAL2;
	private final static byte OPERATOR = Token.KEYWORD3;
	
  public static final String METHOD_DELIMITERS = " \t~!%^*()-+=|\\#/{}[]:;\"'<>,.?@";

  public APLTokenMarker()
  {
    this(true, false, getKeywords());
  }

  public APLTokenMarker(boolean cpp,  boolean javadoc,  KeywordMap keywords)
  {
    this.cpp = cpp;
    this.javadoc = javadoc;
    this.keywords = keywords;
  }

  public byte markTokensImpl(byte token,  Segment line,  int lineIndex)
  {
    char[] array = line.array;
    int offset = line.offset;
    lastOffset = offset;
    lastKeyword = offset;
    lastWhitespace = offset - 1;
    int length = line.count + offset;
    boolean backslash = false;

loop: for (int i = offset; i < length; i++)
    {
      int i1 = (i + 1);

      char c = array[i];
      if (c == '\\')
      {
        backslash = !backslash;
        continue;
      }
      
	  switch(token)
      {
        case Token.NULL:
          switch(c)
          {
          case '(':
            if (backslash)
            {
              doKeyword(line, i, c);
              backslash = false;
            } else {
              if (doKeyword(line, i, c))
                break;
              addToken(lastWhitespace - lastOffset + 1, token);
              addToken(i - lastWhitespace - 1, Token.METHOD);
              addToken(1, Token.NULL);
              token = Token.NULL;
              lastOffset = lastKeyword = i1;
              lastWhitespace = i;
            }
            break;
          case '#':
            if (backslash)
              backslash = false;
            else if (cpp)
            {
              if (doKeyword(line, i, c))
                break;
              addToken(i - lastOffset, token);
              addToken(length - i, Token.KEYWORD2);
              lastOffset = lastKeyword = length;
              break loop;
            }
            break;
          case '"':
            doKeyword(line, i, c);
            if(backslash)
              backslash = false;
            else
            {
              addToken(i - lastOffset, token);
              token = Token.LITERAL1;
              lastOffset = lastKeyword = i;
            }
            break;
          case '\'':
            doKeyword(line, i, c);
            if (backslash)
              backslash = false;
            else
            {
              addToken(i - lastOffset, token);
              token = Token.LITERAL2;
              lastOffset = lastKeyword = i;
            }
            break;
          case ':':
            if (lastKeyword == offset)
            {
              if (doKeyword(line, i, c))
                break;
              else if (i1 < array.length && array[i1] == ':')
                addToken(i1 - lastOffset, Token.NULL);
              else
                addToken(i1 - lastOffset, Token.LABEL);
              lastOffset = lastKeyword = i1;
              lastWhitespace = i1;
              backslash = false;
            } else if (doKeyword(line, i, c))
              break;
            break;
          case '%':
            backslash = false;
            doKeyword(line, i, c);
            if(length - i > 1)
            {
               addToken(i - lastOffset, token);
               addToken(length - i, Token.COMMENT1);
               lastOffset = lastKeyword = length;
               break loop;
            }
            break;
           case '/':
            backslash = false;
            doKeyword(line, i, c);
            if(length - i > 1)
            {
              switch(array[i1])
              {
                case '*':
                  addToken(i - lastOffset, token);
                  lastOffset = lastKeyword = i;
                  if(javadoc && length - i > 2 && array[i+2] == '*')
                    token = Token.COMMENT2;
                  else
                    token = Token.COMMENT1;
                  break;
                case '/':
                  addToken(i - lastOffset, token);
                  addToken(length - i, Token.COMMENT1);
                  lastOffset = lastKeyword = length;
                  break loop;
              }
            }
            break;
          default:
            backslash = false;
            if (!isTokenChar(c) && c != '_')
              doKeyword(line, i, c);
            if (METHOD_DELIMITERS.indexOf(c) != -1)
            {
              lastWhitespace = i;
            }
            break;
          }
          break;
        case Token.COMMENT1:
        case Token.COMMENT2:
          backslash = false;
          if (c == '*' && length - i > 1)
          {
            if (array[i1] == '/')
            {
              i++;
              addToken((i + 1) - lastOffset, token);
              token = Token.NULL;
              lastOffset = lastKeyword = i + 1;
              lastWhitespace = i;
            }
          }
          break;
        case Token.LITERAL1:
          if (backslash)
            backslash = false;
          else if (c == '"')
          {
            addToken(i1 - lastOffset, token);
            token = Token.NULL;
            lastOffset = lastKeyword = i1;
            lastWhitespace = i;
          }
          break;
        case Token.LITERAL2:
          if (backslash)
            backslash = false;
          else if (c == '\'')
          {
            addToken(i1 - lastOffset, token);
            token = Token.NULL;
            lastOffset = lastKeyword = i1;
            lastWhitespace = i;
          }
          break;
        default:
          throw new InternalError("Invalid state: " + token);
      }
             
    }

    if (token == Token.NULL)
      doKeyword(line, length, '\0');

    switch(token)
    {
      case Token.LITERAL1:
      case Token.LITERAL2:
        addToken(length - lastOffset, Token.INVALID);
        token = Token.NULL;
        break;
      case Token.KEYWORD2:
        addToken(length - lastOffset, token);
        if (!backslash)
          token = Token.NULL;
      default:
        addToken(length - lastOffset, token);
        break;
    }
    
    

    return token;
  } 

  public static KeywordMap getKeywords()
  {
    if (daplKeywords == null)
    {
      daplKeywords = new KeywordMap(true);
      
      daplKeywords.add("while", LITERAL2);
      daplKeywords.add("do", LITERAL2);
      daplKeywords.add("if", LITERAL2);
      daplKeywords.add("then", LITERAL2);
      daplKeywords.add("else", LITERAL2);
      
      daplKeywords.add("skip", LITERAL1);
      daplKeywords.add("true", LITERAL1);
      
      daplKeywords.add("not", OPERATOR);
      daplKeywords.add("and", OPERATOR);
      daplKeywords.add("or", OPERATOR);
      daplKeywords.add("+", OPERATOR);
      daplKeywords.add("-", OPERATOR);
      daplKeywords.add("/", OPERATOR);
      daplKeywords.add("=", OPERATOR);
      daplKeywords.add("==", OPERATOR);
      daplKeywords.add("!=", OPERATOR);
      daplKeywords.add("*", OPERATOR);
      daplKeywords.add(";", OPERATOR);
      daplKeywords.add("{", OPERATOR);
      daplKeywords.add("}", OPERATOR);
      daplKeywords.add("|", OPERATOR);
      daplKeywords.add("<-", OPERATOR);
      
      daplKeywords.add("java", ATOM);
      daplKeywords.add("send", ATOM);
      daplKeywords.add("message", ATOM);
      daplKeywords.add("print", ATOM);
      daplKeywords.add("event", ATOM);
      daplKeywords.add("adopta", ATOM);
      daplKeywords.add("adoptz", ATOM);
      daplKeywords.add("dropGoal", ATOM);
      daplKeywords.add("dropSubgoal", ATOM);
      daplKeywords.add("dropExactgoal", ATOM);

      daplKeywords.add("include", HEAD);
      daplKeywords.add("beliefs", HEAD);
      daplKeywords.add("goals", HEAD);
      daplKeywords.add("plans", HEAD);
      daplKeywords.add("pc-rules", HEAD);
      daplKeywords.add("pg-rules", HEAD);
      daplKeywords.add("pr-rules", HEAD);
      daplKeywords.add("beliefupdates", HEAD);
      daplKeywords.add("belief-updates", HEAD);
    }
    return daplKeywords;
  }

  // private members
  private static KeywordMap daplKeywords;

  protected boolean cpp;
  protected boolean javadoc;
  protected KeywordMap keywords;
  protected int lastOffset;
  protected int lastKeyword;
  protected int lastWhitespace;

  protected boolean doKeyword(Segment line,  int i,  char c)
  {
    int i1 = i+1;

    int len = i - lastKeyword;
    byte id = keywords.lookup(line, lastKeyword, len);
    
    if (id==ATOM) {
    	if (c!='(') id=Token.NULL;
    	else id=Token.LITERAL2;
    }
    
    if (id != Token.NULL)
    {
      if (lastKeyword != lastOffset)
      addToken(lastKeyword - lastOffset, Token.NULL);
      addToken(len, id);
      
   	  lastOffset = i;
      lastKeyword = i1;
      lastWhitespace = i;

      return true;
    }
    
    lastKeyword = i1;
    return false;
  }
  
  private boolean isTokenChar(char c)
  {
  	return	c!=' ' &&
  			c!='{' &&
  			c!='}' &&
  			c!='(' &&
  			c!='+' &&
  			c!=')' &&
  			c!='\t';
  }
}

/*
 * ChangeLog:
 * $Log: CTokenMarker.java,v $
 * Revision 1.9  2003/06/30 17:31:09  blaisorblade
 * Fix for line-ends.
 *
 * Revision 1.8  2003/06/29 13:37:27  gfx
 * Support of JDK 1.4.2
 *
 * Revision 1.7  2003/03/13 22:52:48  gfx
 * Improved focus gain
 *
 * Revision 1.6  2002/03/22 21:01:00  gfx
 * Jext 3.1pre2 <stable and dev>
 *
 */