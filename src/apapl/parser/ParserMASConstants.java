/* Generated By:JavaCC: Do not edit this line. ParserMASConstants.java */
package apapl.parser;


/** 
 * Token literal values and constants.
 * Generated by org.javacc.parser.OtherFilesGen#start()
 */
public interface ParserMASConstants {

  /** End of File. */
  int EOF = 0;
  /** RegularExpression Id. */
  int SINGLE_LINE_COMMENT = 9;
  /** RegularExpression Id. */
  int FORMAL_COMMENT = 10;
  /** RegularExpression Id. */
  int MULTI_LINE_COMMENT = 11;
  /** RegularExpression Id. */
  int COLON = 13;
  /** RegularExpression Id. */
  int AT = 14;
  /** RegularExpression Id. */
  int COMMA = 15;
  /** RegularExpression Id. */
  int NRAGENTS = 16;
  /** RegularExpression Id. */
  int IDENTIFIER = 17;
  /** RegularExpression Id. */
  int FILENAME = 18;

  /** Lexical state. */
  int DEFAULT = 0;
  /** Lexical state. */
  int IN_SINGLE_LINE_COMMENT = 1;
  /** Lexical state. */
  int IN_FORMAL_COMMENT = 2;
  /** Lexical state. */
  int IN_MULTI_LINE_COMMENT = 3;

  /** Literal token values. */
  String[] tokenImage = {
    "<EOF>",
    "\" \"",
    "\"\\t\"",
    "\"\\n\"",
    "\"\\r\"",
    "\"%\"",
    "\"//\"",
    "<token of kind 7>",
    "\"/*\"",
    "<SINGLE_LINE_COMMENT>",
    "\"*/\"",
    "\"*/\"",
    "<token of kind 12>",
    "\":\"",
    "\"@\"",
    "\",\"",
    "<NRAGENTS>",
    "<IDENTIFIER>",
    "<FILENAME>",
  };

}
