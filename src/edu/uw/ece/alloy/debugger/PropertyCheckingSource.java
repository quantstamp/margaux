package edu.uw.ece.alloy.debugger;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.Util;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprBinary;
import edu.uw.ece.alloy.debugger.PropertySet.Property;

/**
 * PropertyChekingSource is and abstract class including all materials needs for
 * generating Alloy source code in order to check a relational property. Any
 * subclass has to implement the toAlloy method to get the result.
 * 
 * @author vajih
 *
 */
public abstract class PropertyCheckingSource {

	final public File sourceFile;

	final public String property;
	final public String fieldName;
	// Make a file name from the property. e.g. total[s.B,B]-> total___s-B,
	// total[s,A]-> total_s
	final public String sanitizedPropertyName;
	final public String sanitizedFieldName;

	final public String propertyFieldName;
	final public String propertyName;

	final public Set<String> binaryProperties;
	final public Set<String> ternaryProperties;

	final public String emptyProperty;

	final public String functions;
	final public String sigs;
	final public String openModule;
	final public String openStatements;
	final public String commandHeader;
	final public String formula;
	final public String commandScope;
	final public String facts;

	final public static String emptyCheckingBinrayFieldFormat = "!empty[ %s ]";
	final public static String emptyCheckingTernaryFieldFormat = "!empty3[ %s ]";

	final public static String SEPARATOR = "_S_p_R_";

	/**
	 * The input property is in the form of prop_name[field,....]. The return is
	 * field.
	 * 
	 * @param field
	 * @return
	 */
	final public static String fieldExtractorFromProperty(final String property) {
		final Pattern bracketPAttern = Pattern.compile("\\[(.*)\\]");
		final Matcher matched = bracketPAttern.matcher(property);
		if (matched.find())
			return property.replaceAll("([^\\[]+\\[)([^\\],]*)(.*\\])", "$2");
		else
			return "";
	}

	/**
	 * The input property is in the form of prop_name[field,....]. The return is
	 * prop_name.
	 * 
	 * @param property
	 * @return
	 */
	public final static String propertyNameExtractorFromProperty(
			final String property) {
		return property.replaceAll("([^/]*/|^)([^\\[]+)(\\[.*)", "$2");
	}

	/**
	 * This function takes a property and returns a standard copy of it that could
	 * be compared with consistency map.
	 * 
	 * @param props
	 * @return
	 */
	public final static String cleanProperty(final String property) {

		final String propName = propertyNameExtractorFromProperty(property);
		String propField = fieldExtractorFromProperty(property);
		final boolean hasNot = property.trim().contains("not ");

		// (State<:holds).Mutex -> (State<:holds).C
		propField = propField.replaceAll("\\.[^\\(].*", ".C");
		// State.(State<:holds) -> A.(State<:holds)
		propField = propField.replaceAll("[^\\.]+\\.\\(", "A.(");
		// State.(State<:holds) -> State.r
		propField = propField.replaceAll("\\([^\\)]+\\)", "r");
		// Remove extra characters.
		propField = propField.replaceAll("\\)", "");

		return String.format("%s%s[%s]", hasNot ? "not " : "", propName, propField)
				.trim();

	}

	public final static String cleanProperty(final Property property) {
		final String propName = property.value;
		String propField = property.fld.value;
		final boolean hasNot = propName.trim().contains("not ");

		if (propField.contains("(")) {
			return cleanProperty(property.toString());
		} else {

			// State<:holds.Mutex -> r.C
			propField = propField.replaceAll(
					"([^(<:|\\.)]+)(<:)(([^(<:|\\.)])+)\\.([^(<:|\\.)]+)", "r.C");
			// State.State<:holds -> A.r
			propField = propField.replaceAll(
					"([^(<:|\\.)]+)\\.([^(<:|\\.)]+)(<:)(([^(<:|\\.)])+)", "A.r");
			// State<:holds -> r
			propField = propField.replaceAll("([^(<:|\\.)]+)(<:)(([^(<:|\\.)])+)",
					"r");

			return String
					.format("%s%s[%s]", hasNot ? "not " : "", propName, propField).trim();
		}
	}

	protected PropertyCheckingSource(final File sourceFile_,
			final String property_, final String fieldName_,
			final Set<String> binaryProperties_, final Set<String> ternaryProperties_,
			/* final String assertionName_, final String assertionBody_, */ final String sigs_,
			final String openModule_, final String openStatements_,
			final String functions_, final String commandHeader_,
			final String formula_, final String commandScope_, final String facts_) {

		this.sourceFile = sourceFile_;

		this.property = property_;
		this.fieldName = fieldName_;

		this.sanitizedPropertyName = nameSanitizer(property);
		this.sanitizedFieldName = nameSanitizer(fieldName_);

		this.binaryProperties = binaryProperties_;
		this.ternaryProperties = ternaryProperties_;

		// The following part determines if the filed is binary or ternary.
		// The property name shows whether the field is binary or ternary.
		propertyFieldName = fieldExtractorFromProperty(property);
		propertyName = propertyNameExtractorFromProperty(property);

		emptyProperty = ternaryProperties.contains(propertyName)
				? String.format(emptyCheckingTernaryFieldFormat, propertyFieldName)
				: String.format(emptyCheckingBinrayFieldFormat, propertyFieldName);

		// this.assertionName = assertionName_;
		// this.assertionBody = assertionBody_;

		this.functions = functions_;
		this.sigs = sigs_;
		this.openModule = openModule_;
		this.openStatements = openStatements_;

		this.commandHeader = commandHeader_;
		this.formula = formula_;
		this.commandScope = commandScope_;
		this.facts = facts_;

	}

	final String nameSanitizer(final String name) {
		return name.replaceAll("(,.*|\\])", "").replaceAll("(\\)|\\()", "")
				.replaceAll("\\[", "_F_l_d_").replaceAll("\\.", "_D_o_T_")
				.replaceAll(ExprBinary.Op.DOMAIN.toString(), "_D_m_N_")
				.replaceAll("/", "_S_c_P_").replaceAll("\\+", "_U_n_N_")
				.replaceAll("-", "_D_i_F_").replaceAll("&", "_I_t_S_");
	}

	public String toAlloy() {
		final StringBuilder newAlloySpec = new StringBuilder();
		newAlloySpec.append(openStatements);
		newAlloySpec.append("\n").append(openModule);
		newAlloySpec.append("\n").append(sigs);
		newAlloySpec.append("\n").append(functions);
		newAlloySpec.append("\n").append(facts);
		newAlloySpec.append("\n").append(getNewStatement());
		return newAlloySpec.toString();
	}

	public List<File> toAlloyFile(final File destFolder) {
		assert destFolder.isDirectory() : "not a directory: " + destFolder;

		final File retFileName = new File(destFolder, makeNewFileName());

		try {
			Util.writeAll(retFileName.getAbsolutePath(), this.toAlloy());
		} catch (Err e) {
			e.printStackTrace();
			throw new RuntimeException(
					String.format("Output file could be created: %s\n", e.getMessage()));
		}

		return Collections
				.unmodifiableList(Arrays.asList(retFileName.getAbsoluteFile()));
	}

	@Override
	public String toString() {
		return "PropertyCheckingSource [sourceFile=" + sourceFile + ", property="
				+ property + ", fieldName=" + fieldName + ", sanitizedPropertyName="
				+ sanitizedPropertyName + ", sanitizedFieldName=" + sanitizedFieldName
				+ ", propertyFieldName=" + propertyFieldName + ", propertyName="
				+ propertyName + ", binaryProperties=" + binaryProperties
				+ ", ternaryProperties=" + ternaryProperties + ", emptyProperty="
				+ emptyProperty + ", functions=" + functions + ", sigs=" + sigs
				+ ", openModule=" + openModule + ", openStatements=" + openStatements
				+ ", commandHeader=" + commandHeader + ", formula=" + formula
				+ ", commandScope=" + commandScope + "]";
	}

	public boolean repOk() {

		return sourceFile != null && property != null && !property.trim().isEmpty()
				&& fieldName != null && !fieldName.trim().isEmpty()
				&& sanitizedPropertyName != null
				&& !sanitizedPropertyName.trim().isEmpty() && sanitizedFieldName != null
				&& !sanitizedFieldName.trim().isEmpty() && propertyFieldName != null
				&& !propertyFieldName.trim().isEmpty() && propertyName != null
				&& !propertyName.trim().isEmpty() && binaryProperties != null &&
				// !binaryProperties.isEmpty() &&
				ternaryProperties != null &&
				// !ternaryProperties.isEmpty() &&
				emptyProperty != null && !emptyProperty.trim().isEmpty()
				&& functions != null && sigs != null && !sigs.trim().isEmpty()
				&& openModule != null && openStatements != null &&
				// !openStatements.trim().isEmpty() &&
				commandHeader != null && !commandHeader.trim().isEmpty()
				&& formula != null && !formula.trim().isEmpty() && commandScope != null;

	}

	protected abstract String makeNewFileName();

	protected abstract String getNewStatement();
}
