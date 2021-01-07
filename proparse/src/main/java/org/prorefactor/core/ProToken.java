/********************************************************************************
 * Copyright (c) 2015-2021 Riverside Software
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU Lesser General Public License v3.0
 * which is available at https://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * SPDX-License-Identifier: EPL-2.0 OR LGPL-3.0
 ********************************************************************************/
package org.prorefactor.core;

import java.util.List;

import javax.annotation.Nonnull;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenSource;

import com.google.common.base.Splitter;

public class ProToken implements Token {
  private static final String INVALID_TYPE = "Invalid type number ";

  // All preprocessor statements (&MESSAGE, &ANALYZE-SUSPEND and RESUME, &GLOBAL/SCOPED DEFINE and &UNDEFINE) go to this channel
  public static final int PREPROCESSOR_CHANNEL = 2;
  // All &_PROPARSE statements go to this channel
  public static final int PROPARSE_CHANNEL = 3;

  private ABLNodeType type;
  private int line;
  private int charPositionInLine = 0;
  private int channel = DEFAULT_CHANNEL;
  private String text;
  private int index = -1;

  private int fileIndex = 0;
  private int endFileIndex;
  private String fileName;
  private int endLine;
  private int endCharPositionInLine;
  private int macroSourceNum;

  private String analyzeSuspend = null;
  private ProToken hiddenBefore = null;
  private boolean macroExpansion;
  private boolean synthetic = false;
  private boolean nestedComments = false;

  ProToken(ABLNodeType type, String text) {
    this.type = type;
    this.text = text;
  }

  @Override
  public int getType() {
    return type.getType();
  }

  public ABLNodeType getNodeType() {
    return type;
  }

  @Override
  public String getText() {
    return text;
  }

  @Override
  public int getLine() {
    return line;
  }

  @Override
  public int getCharPositionInLine() {
    return charPositionInLine;
  }

  @Override
  public int getChannel() {
    return channel;
  }

  @Override
  public int getTokenIndex() {
    return index;
  }

  @Override
  public int getStartIndex() {
    return -1;
  }

  @Override
  public int getStopIndex() {
    return -1;
  }

  @Override
  public TokenSource getTokenSource() {
    return null;
  }

  @Override
  public CharStream getInputStream() {
    return null;
  }

  /**
   * @return 0 if token coming from main file, anything else (greater than 0) for tokens coming from include files
   */
  public int getFileIndex() {
    return fileIndex;
  }

  /**
   * TODO Can probably be removed in the future
   * @return Macro source number
   */
  public int getMacroSourceNum() {
    return macroSourceNum;
  }

  public String getFileName() {
    return fileName;
  }

  public int getEndLine() {
    return endLine;
  }

  public int getEndCharPositionInLine() {
    return endCharPositionInLine;
  }

  public int getEndFileIndex() {
    return endFileIndex;
  }

  public boolean isAbbreviated() {
    return type.isAbbreviated(text);
  }

  public boolean hasNestedComments() {
    return nestedComments;
  }

  /**
   * TODO Improve implementation
   *
   * @return Comma-separated list of &amp;ANALYZE-SUSPEND options. Null for code not managed by AppBuilder.
   */
  public String getAnalyzeSuspend() {
    return analyzeSuspend;
  }

  /**
   * TODO See getAnalyzeSuspend()
   *
   * @return True if token is part of an editable section in AppBuilder managed code
   */
  public boolean isEditableInAB() {
    return (analyzeSuspend == null) || isTokenEditableInAB(analyzeSuspend);
  }

  /**
   * TODO Can probably be removed in the future
   *
   * @return True if last character of token was generated from a macro expansion, i.e. {&amp;SOMETHING}. This doesn't
   *         mean that all characters were generated from a macro, e.g. {&amp;prefix}VarName will return false
   */
  public boolean isMacroExpansion() {
    return macroExpansion;
  }

  /**
   * @return True if token has been generated by ProParser and not by the lexer
   */
  public boolean isSynthetic() {
    return synthetic;
  }

  /**
   * @return True if token has been generated by the lexer and not by ProParser
   */
  public boolean isNatural() {
    return !synthetic;
  }

  @Override
  public String toString() {
    return "[\"" + text.replace('\r', ' ').replace('\n', ' ') + "\",<" + type + ">,macro=" + macroSourceNum + ",start="
        + fileIndex + ":" + line + ":" + charPositionInLine + ",end=" + endFileIndex + ":" + endLine + ":"
        + endCharPositionInLine + "]";
  }

  /**
   * @return True if token is part of an editable section in AppBuilder managed code
   */
  public static boolean isTokenEditableInAB(@Nonnull String str) {
    List<String> attrs = Splitter.on(',').omitEmptyStrings().trimResults().splitToList(str);
    if (attrs.isEmpty() || !"_UIB-CODE-BLOCK".equalsIgnoreCase(attrs.get(0)))
      return false;

    if ((attrs.size() >= 3) && "_CUSTOM".equalsIgnoreCase(attrs.get(1))
        && "_DEFINITIONS".equalsIgnoreCase(attrs.get(2)))
      return true;
    else if ((attrs.size() >= 2) && "_CONTROL".equalsIgnoreCase(attrs.get(1)))
      return true;
    else if ((attrs.size() == 4) && "_PROCEDURE".equals(attrs.get(1)))
      return true;
    else if ((attrs.size() == 5) && "_PROCEDURE".equals(attrs.get(1)) && "_FREEFORM".equals(attrs.get(4)))
      return true;
    else if ((attrs.size() >= 2) && "_FUNCTION".equals(attrs.get(1)))
      return true;

    return false;
  }

  public ProToken getHiddenBefore() {
    return hiddenBefore;
  }

  public void setChannel(int channel) {
    this.channel = channel;
  }

  public void setTokenIndex(int index) {
    this.index = index;
  }

  public void setHiddenBefore(ProToken hiddenBefore) {
    this.hiddenBefore = hiddenBefore;
  }

  public void setNodeType(ABLNodeType type) {
    if (type == null)
      throw new IllegalArgumentException(INVALID_TYPE + type);
    this.type = type;
  }

  public static class Builder {
    private ABLNodeType type;
    private StringBuilder text;

    private int line;
    private int endLine;
    private int charPositionInLine;
    private int endCharPositionInLine;
    private int fileIndex;
    private int endFileIndex;
    private String fileName;

    private int macroSourceNum;

    private String analyzeSuspend = null;
    private ProToken hiddenBefore = null;
    private boolean macroExpansion;
    private boolean synthetic = false;
    private boolean writable = false;
    private boolean nestedComments = false;

    public Builder(ABLNodeType type, String text) {
      this.type = type;
      this.text = new StringBuilder(text);
    }

    public Builder(ProToken token) {
      this.type = token.type;
      this.text = new StringBuilder(token.text);
      this.line = token.line;
      this.charPositionInLine = token.charPositionInLine;
      this.fileIndex = token.fileIndex;
      this.endFileIndex = token.endFileIndex;
      this.fileName = token.fileName;
      this.endLine = token.endLine;
      this.endCharPositionInLine = token.endCharPositionInLine;
      this.macroSourceNum = token.macroSourceNum;
      this.analyzeSuspend = token.analyzeSuspend;
      this.hiddenBefore = token.hiddenBefore;
      this.macroExpansion = token.macroExpansion;
      this.synthetic = token.synthetic;
      this.writable = token instanceof WritableProToken;
      this.nestedComments = token.nestedComments;
    }

    public Builder setWritable(boolean writable) {
      this.writable = writable;
      return this;
    }

    public Builder setType(ABLNodeType type) {
      this.type = type;
      return this;
    }

    public Builder setLine(int line) {
      this.line = line;
      return this;
    }

    public Builder setEndLine(int endLine) {
      this.endLine = endLine;
      return this;
    }

    public Builder setCharPositionInLine(int charPositionInLine) {
      this.charPositionInLine = charPositionInLine;
      return this;
    }

    public Builder setEndCharPositionInLine(int endCharPositionInLine) {
      this.endCharPositionInLine = endCharPositionInLine;
      return this;
    }

    public Builder setFileIndex(int fileIndex) {
      this.fileIndex = fileIndex;
      return this;
    }

    public Builder setEndFileIndex(int endFileIndex) {
      this.endFileIndex = endFileIndex;
      return this;
    }

    public Builder setFileName(String fileName) {
      this.fileName = fileName;
      return this;
    }

    public Builder setMacroSourceNum(int macroSourceNum) {
      this.macroSourceNum = macroSourceNum;
      return this;
    }

    public Builder setMacroExpansion(boolean macroExpansion) {
      this.macroExpansion = macroExpansion;
      return this;
    }

    public Builder setAnalyzeSuspend(String analyzeSuspend) {
      this.analyzeSuspend = analyzeSuspend;
      return this;
    }

    public Builder setHiddenBefore(ProToken hiddenBefore) {
      this.hiddenBefore = hiddenBefore;
      return this;
    }

    public Builder setSynthetic(boolean synthetic) {
      this.synthetic = synthetic;
      return this;
    }

    public Builder appendText(String text) {
      this.text.append(text);
      return this;
    }

    public Builder setText(String text) {
      this.text = new StringBuilder(text);
      return this;
    }

    public Builder setNestedComments(boolean nestedComments) {
      this.nestedComments = nestedComments;
      return this;
    }

    /**
     * Merge current builder with another token. Some information is lost in the process.
     */
    public Builder mergeWith(ProToken tok) {
      this.endLine = tok.endLine;
      this.endCharPositionInLine = tok.endCharPositionInLine;
      this.endFileIndex = tok.endFileIndex;
      if (tok.hiddenBefore != null)
        appendText(" ");
      appendText(tok.text);

      return this;
    }

    public ProToken build() {
      if (type == null)
        throw new IllegalArgumentException(INVALID_TYPE + type);

      ProToken tok = writable ? new WritableProToken(type, text.toString()) : new ProToken(type, text.toString());
      tok.line = line;
      tok.endLine = endLine;
      tok.charPositionInLine = charPositionInLine;
      tok.endCharPositionInLine = endCharPositionInLine;
      tok.fileIndex = fileIndex;
      tok.endFileIndex = endFileIndex;
      tok.fileName = fileName;
      tok.macroSourceNum = macroSourceNum;
      tok.macroExpansion = macroExpansion;
      tok.analyzeSuspend = analyzeSuspend;
      tok.hiddenBefore = hiddenBefore;
      tok.synthetic = synthetic;
      tok.nestedComments = nestedComments;

      switch (type) {
        case COMMENT:
        case WS:
          tok.channel = Token.HIDDEN_CHANNEL;
          break;
        case AMPMESSAGE:
        case AMPANALYZESUSPEND:
        case AMPANALYZERESUME:
        case AMPGLOBALDEFINE:
        case AMPSCOPEDDEFINE:
        case AMPUNDEFINE:
        case INCLUDEDIRECTIVE:
        case AMPIF:
        case AMPELSE:
        case AMPELSEIF:
        case AMPENDIF:
        case AMPTHEN:
        case PREPROEXPR_TRUE:
        case PREPROEXPR_FALSE:
          tok.channel = PREPROCESSOR_CHANNEL;
          break;
        case PROPARSEDIRECTIVE:
          tok.channel = PROPARSE_CHANNEL;
          break;
        default:
      }

      return tok;
    }
  }
}
