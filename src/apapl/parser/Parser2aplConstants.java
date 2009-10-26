/* Generated By:JavaCC: Do not edit this line. Parser2aplConstants.java */
package apapl.parser;


/** 
 * Token literal values and constants.
 * Generated by org.javacc.parser.OtherFilesGen#start()
 */
public interface Parser2aplConstants {

  /** End of File. */
  int EOF = 0;
  /** RegularExpression Id. */
  int SINGLE_LINE_COMMANT = 9;
  /** RegularExpression Id. */
  int FORMAL_COMMANT = 10;
  /** RegularExpression Id. */
  int MULTI_LINE_COMMANT = 11;
  /** RegularExpression Id. */
  int FILENAME = 17;
  /** RegularExpression Id. */
  int INCLHEAD = 18;
  /** RegularExpression Id. */
  int CAPSHEAD = 19;
  /** RegularExpression Id. */
  int BBHEAD = 20;
  /** RegularExpression Id. */
  int GBHEAD = 21;
  /** RegularExpression Id. */
  int GPRULEHEAD = 22;
  /** RegularExpression Id. */
  int PRRULEHEAD = 23;
  /** RegularExpression Id. */
  int PCRULEHEAD = 24;
  /** RegularExpression Id. */
  int PLANHEAD = 25;
  /** RegularExpression Id. */
  int AND = 26;
  /** RegularExpression Id. */
  int OR = 27;
  /** RegularExpression Id. */
  int NOT = 28;
  /** RegularExpression Id. */
  int TRUE = 29;
  /** RegularExpression Id. */
  int IF = 30;
  /** RegularExpression Id. */
  int THEN = 31;
  /** RegularExpression Id. */
  int ELSE = 32;
  /** RegularExpression Id. */
  int WHILE = 33;
  /** RegularExpression Id. */
  int DO = 34;
  /** RegularExpression Id. */
  int SELECT = 35;
  /** RegularExpression Id. */
  int MARKER_BEGIN = 36;
  /** RegularExpression Id. */
  int MARKER_END = 37;
  /** RegularExpression Id. */
  int TESTAND = 38;
  /** RegularExpression Id. */
  int SKIPPLAN = 39;
  /** RegularExpression Id. */
  int SEND = 40;
  /** RegularExpression Id. */
  int PRINT = 41;
  /** RegularExpression Id. */
  int CREATE = 42;
  /** RegularExpression Id. */
  int CLONE = 43;
  /** RegularExpression Id. */
  int RELEASE = 44;
  /** RegularExpression Id. */
  int EXECUTE = 45;
  /** RegularExpression Id. */
  int UPDATEBB = 46;
  /** RegularExpression Id. */
  int GOALACTION = 47;
  /** RegularExpression Id. */
  int USE = 48;
  /** RegularExpression Id. */
  int SENSE = 49;
  /** RegularExpression Id. */
  int LINKSENSOR = 50;
  /** RegularExpression Id. */
  int UNLINKSENSOR = 51;
  /** RegularExpression Id. */
  int FOCUS = 52;
  /** RegularExpression Id. */
  int STOPFOCUS = 53;
  /** RegularExpression Id. */
  int CREATEART = 54;
  /** RegularExpression Id. */
  int DISPOSEART = 55;
  /** RegularExpression Id. */
  int ABSTRACTACTION = 56;
  /** RegularExpression Id. */
  int BELIEFUPDATE = 57;
  /** RegularExpression Id. */
  int EXTERNALACTION = 58;
  /** RegularExpression Id. */
  int ATOMICPLAN = 59;
  /** RegularExpression Id. */
  int UPDATEBELIEFBASE = 60;
  /** RegularExpression Id. */
  int CARTAGO = 61;
  /** RegularExpression Id. */
  int TEST = 62;
  /** RegularExpression Id. */
  int GOALACTIONQ = 63;
  /** RegularExpression Id. */
  int COMMA = 64;
  /** RegularExpression Id. */
  int B = 65;
  /** RegularExpression Id. */
  int G = 66;
  /** RegularExpression Id. */
  int GE = 67;
  /** RegularExpression Id. */
  int P = 68;
  /** RegularExpression Id. */
  int AT = 69;
  /** RegularExpression Id. */
  int SLASHJADE = 70;
  /** RegularExpression Id. */
  int UNDERSCORE = 71;
  /** RegularExpression Id. */
  int LBRACE = 72;
  /** RegularExpression Id. */
  int RBRACE = 73;
  /** RegularExpression Id. */
  int DOTCOMMA = 74;
  /** RegularExpression Id. */
  int LISTL = 75;
  /** RegularExpression Id. */
  int LISTR = 76;
  /** RegularExpression Id. */
  int ACCL = 77;
  /** RegularExpression Id. */
  int ACCR = 78;
  /** RegularExpression Id. */
  int LEFTARROW = 79;
  /** RegularExpression Id. */
  int DOT = 80;
  /** RegularExpression Id. */
  int IMPL = 81;
  /** RegularExpression Id. */
  int RELATIONAL = 82;
  /** RegularExpression Id. */
  int SIGN = 83;
  /** RegularExpression Id. */
  int IDENT = 84;
  /** RegularExpression Id. */
  int VARNAME = 85;
  /** RegularExpression Id. */
  int NUM = 86;
  /** RegularExpression Id. */
  int NUMNAME = 87;
  /** RegularExpression Id. */
  int NAMEDOT = 88;
  /** RegularExpression Id. */
  int VERT = 89;
  /** RegularExpression Id. */
  int QUOTE = 90;
  /** RegularExpression Id. */
  int QUOTE1 = 91;

  /** Lexical state. */
  int DEFAULT = 0;
  /** Lexical state. */
  int IN_SINGLE_LINE_COMMANT = 1;
  /** Lexical state. */
  int IN_FORMAL_COMMANT = 2;
  /** Lexical state. */
  int IN_MULTI_LINE_COMMANT = 3;
  /** Lexical state. */
  int IN_INCLUDE_SECTION = 4;

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
    "<SINGLE_LINE_COMMANT>",
    "\"*/\"",
    "\"*/\"",
    "<token of kind 12>",
    "\" \"",
    "\"\\t\"",
    "\"\\n\"",
    "\"\\r\"",
    "<FILENAME>",
    "\"Include:\"",
    "\"BeliefUpdates:\"",
    "\"Beliefs:\"",
    "\"Goals:\"",
    "\"PG-rules:\"",
    "\"PR-rules:\"",
    "\"PC-rules:\"",
    "\"Plans:\"",
    "\"and\"",
    "\"or\"",
    "\"not\"",
    "\"true\"",
    "\"if\"",
    "\"then\"",
    "\"else\"",
    "\"while\"",
    "\"do\"",
    "\"#\"",
    "<MARKER_BEGIN>",
    "<MARKER_END>",
    "\"&\"",
    "\"skip\"",
    "\"send\"",
    "\"print\"",
    "\"create\"",
    "\"clone\"",
    "\"release\"",
    "\"execute\"",
    "\"updateBB\"",
    "<GOALACTION>",
    "\"use\"",
    "\"sense\"",
    "\"linkSensor\"",
    "\"unlinkSensor\"",
    "\"focus\"",
    "\"stopFocus\"",
    "\"createArtifact\"",
    "\"disposeArtifact\"",
    "\"abstractaction\"",
    "\"beliefupdate\"",
    "\"externalaction\"",
    "\"atomicplan\"",
    "\"updatebeliefbase\"",
    "\"cartago\"",
    "\"test\"",
    "\"goalaction\"",
    "\",\"",
    "\"B\"",
    "\"G\"",
    "\"!G\"",
    "\"P\"",
    "\"@\"",
    "\"/JADE\"",
    "\"_\"",
    "\"(\"",
    "\")\"",
    "\";\"",
    "\"[\"",
    "\"]\"",
    "\"{\"",
    "\"}\"",
    "\"<-\"",
    "\".\"",
    "\":-\"",
    "<RELATIONAL>",
    "<SIGN>",
    "<IDENT>",
    "<VARNAME>",
    "<NUM>",
    "<NUMNAME>",
    "<NAMEDOT>",
    "\"|\"",
    "<QUOTE>",
    "<QUOTE1>",
    "\"?\"",
    "\"*\"",
    "\"/\"",
  };

}