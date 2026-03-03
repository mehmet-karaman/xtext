package org.eclipse.xtext.example.homeautomation.tests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.xtext.testing.AbstractLanguageServerTest;
import org.eclipse.xtext.example.homeautomation.tests.RuleEngineTestServerModule;
import org.eclipse.xtext.example.homeautomation.RuleEngineStandaloneSetup;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.DocumentSymbolCapabilities;
import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextDocumentClientCapabilities;
import org.eclipse.lsp4j.WorkspaceClientCapabilities;
import org.eclipse.xtext.testing.DocumentSymbolConfiguration;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.junit.Test;

import com.google.inject.Module;

@org.junit.runner.RunWith(org.eclipse.xtext.testing.XtextRunner.class)
@org.eclipse.xtext.testing.InjectWith(RuleEngineInjectorProvider.class)
public class RuleEngineLspTest extends AbstractLanguageServerTest {

    public RuleEngineLspTest() {
        super("rules");
    }

    @Override
    protected Module getServerModule() {
    	return new RuleEngineTestServerModule();
    }
    
    @Test
    public void testHierarchicalDocumentSymbols() throws Exception {
        new RuleEngineStandaloneSetup().createInjectorAndDoEMFRegistration();
        testDocumentSymbol((DocumentSymbolConfiguration it) -> {
            it.setInitializer((InitializeParams params) -> {
                ClientCapabilities cc = new ClientCapabilities();
                WorkspaceClientCapabilities wcc = new WorkspaceClientCapabilities();
                wcc.setWorkspaceFolders(true);
                cc.setWorkspace(wcc);
                DocumentSymbolCapabilities dsc = new DocumentSymbolCapabilities();
                dsc.setHierarchicalDocumentSymbolSupport(true);
                TextDocumentClientCapabilities tdcc = new TextDocumentClientCapabilities();
                tdcc.setDocumentSymbol(dsc);
                cc.setTextDocument(tdcc);
                params.setCapabilities(cc);
            });
            String model =
                    "Device A can be X, Y\n" +
                    "Rule \"r\" when X then {}\n";
            it.setModel(model);
            it.setAssertSymbols(symbols -> {
                // expect at least one top-level symbol for device A with children
                boolean found = false;
                for (Either<SymbolInformation, DocumentSymbol> e : symbols) {
                    if (e.isRight()) {
                        DocumentSymbol ds = e.getRight();
                        if ("A".equals(ds.getName())) {
                            found = true;
                            assertFalse(ds.getChildren().isEmpty());
                        }
                    }
                }
                assertTrue("Device A symbol should be present with children", found);
            });
        });
    }
}
