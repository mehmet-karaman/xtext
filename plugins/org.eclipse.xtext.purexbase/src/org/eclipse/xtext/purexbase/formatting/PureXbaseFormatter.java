/*
 * generated by Xtext
 */
package org.eclipse.xtext.purexbase.formatting;

import org.eclipse.xtext.formatting.impl.AbstractDeclarativeFormatter;
import org.eclipse.xtext.formatting.impl.FormattingConfig;
import org.eclipse.xtext.purexbase.services.PureXbaseGrammarAccess;
import org.eclipse.xtext.purexbase.services.PureXbaseGrammarAccess.SpecialBlockExpressionElements;
import org.eclipse.xtext.xbase.formatting.XbaseFormatter;

import com.google.inject.Inject;

/**
 * This class contains custom formatting description.
 * 
 * see : http://www.eclipse.org/Xtext/documentation/latest/xtext.html#formatting
 * on how and when to use it
 * 
 * Also see {@link org.eclipse.xtext.xtext.XtextFormattingTokenSerializer} as an
 * example
 */
public class PureXbaseFormatter extends AbstractDeclarativeFormatter {

	@Inject
	protected XbaseFormatter xbaseFormatter;

	@Override
	protected void configureFormatting(FormattingConfig c) {
		PureXbaseGrammarAccess ga = (PureXbaseGrammarAccess) getGrammarAccess();

		xbaseFormatter.configure(c, ga.getXbaseGrammarAccess());

		SpecialBlockExpressionElements sbee = ga
				.getSpecialBlockExpressionAccess();
		c.setLinewrap(1, 2, 3).after(sbee.getExpressionsAssignment_1_0_0());
		c.setLinewrap(1, 2, 3).after(sbee.getImportsAssignment_1_0_1());
		c.setLinewrap(1, 2, 3).after(sbee.getSemicolonKeyword_1_1());
		c.setNoSpace().before(sbee.getSemicolonKeyword_1_1());

		c.setLinewrap(0, 1, 3).before(ga.getSL_COMMENTRule());
		c.setLinewrap(0, 1, 3).before(ga.getML_COMMENTRule());
		c.setLinewrap(0, 1, 3).after(ga.getML_COMMENTRule());
	}
}
