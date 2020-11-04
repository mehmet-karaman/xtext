/**
 * Copyright (c) 2017 TypeFox GmbH (http://www.typefox.io) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.xtext.ide.tests.server;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.RenameParams;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.ide.tests.server.AbstractTestLangLanguageServerTest;
import org.junit.Before;
import org.junit.Test;

/**
 * @author koehnlein - Initial contribution and API
 */
@SuppressWarnings("all")
public class RenameTest extends AbstractTestLangLanguageServerTest {
  private String firstFile;
  
  private String secondFile;
  
  @Before
  public void setUp() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("type Test {");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("Test foo");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    final String first = _builder.toString();
    this.firstFile = this.writeFile("MyType1.testlang", first);
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("type Test2 {");
    _builder_1.newLine();
    _builder_1.append("    ");
    _builder_1.append("Test foo");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    final String second = _builder_1.toString();
    this.secondFile = this.writeFile("MyType2.testlang", second);
    this.initialize();
  }
  
  @Test
  public void testRenameBeforeDeclaration() throws Exception {
    Position _position = new Position(0, 5);
    this.doTest(this.firstFile, _position);
  }
  
  @Test
  public void testRenameOnDeclaration() throws Exception {
    Position _position = new Position(0, 6);
    this.doTest(this.firstFile, _position);
  }
  
  @Test
  public void testRenameAfterDeclaration() throws Exception {
    Position _position = new Position(0, 8);
    this.doTest(this.firstFile, _position);
  }
  
  @Test
  public void testRenameOnReference() throws Exception {
    Position _position = new Position(1, 5);
    this.doTest(this.firstFile, _position);
  }
  
  @Test
  public void testRenameAfterReference() throws Exception {
    Position _position = new Position(1, 8);
    this.doTest(this.firstFile, _position);
  }
  
  @Test
  public void testRenameOnReferenceInOtherFile() throws Exception {
    Position _position = new Position(1, 5);
    this.doTest(this.secondFile, _position);
  }
  
  @Test
  public void testRenameAfterReferenceInOtherFile() throws Exception {
    Position _position = new Position(1, 8);
    this.doTest(this.secondFile, _position);
  }
  
  protected void doTest(final String fileName, final Position position) throws Exception {
    TextDocumentIdentifier _textDocumentIdentifier = new TextDocumentIdentifier(fileName);
    final RenameParams params = new RenameParams(_textDocumentIdentifier, position, "Tescht");
    final WorkspaceEdit workspaceEdit = this.languageServer.rename(params).get();
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("changes :");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("MyType1.testlang : Tescht [[0, 5] .. [0, 9]]");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("Tescht [[1, 4] .. [1, 8]]");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("MyType2.testlang : Tescht [[1, 4] .. [1, 8]]");
    _builder.newLine();
    _builder.append("documentChanges : ");
    _builder.newLine();
    this.assertEquals(_builder.toString(), this.toExpectation(workspaceEdit));
  }
}
