/*
 * generated by Xtext
 */
package org.eclipse.xtext.parsetree.reconstr.parser.antlr;

import com.google.inject.Inject;
import org.eclipse.xtext.parser.antlr.AbstractAntlrParser;
import org.eclipse.xtext.parser.antlr.XtextTokenStream;
import org.eclipse.xtext.parsetree.reconstr.parser.antlr.internal.InternalSerializationErrorTestLanguageParser;
import org.eclipse.xtext.parsetree.reconstr.services.SerializationErrorTestLanguageGrammarAccess;

public class SerializationErrorTestLanguageParser extends AbstractAntlrParser {

	@Inject
	private SerializationErrorTestLanguageGrammarAccess grammarAccess;

	@Override
	protected void setInitialHiddenTokens(XtextTokenStream tokenStream) {
		tokenStream.setInitialHiddenTokens("RULE_WS", "RULE_ML_COMMENT", "RULE_SL_COMMENT");
	}
	

	@Override
	protected InternalSerializationErrorTestLanguageParser createParser(XtextTokenStream stream) {
		return new InternalSerializationErrorTestLanguageParser(stream, getGrammarAccess());
	}

	@Override 
	protected String getDefaultRuleName() {
		return "Model";
	}

	public SerializationErrorTestLanguageGrammarAccess getGrammarAccess() {
		return this.grammarAccess;
	}

	public void setGrammarAccess(SerializationErrorTestLanguageGrammarAccess grammarAccess) {
		this.grammarAccess = grammarAccess;
	}
}