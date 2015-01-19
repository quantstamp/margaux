package edu.uw.ece.alloy.debugger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.Util;
import edu.mit.csail.sdg.alloy4compiler.ast.Command;
import edu.mit.csail.sdg.alloy4compiler.ast.Expr;
import edu.mit.csail.sdg.alloy4compiler.ast.Func;
import edu.mit.csail.sdg.alloy4compiler.ast.Module;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig;
import edu.mit.csail.sdg.alloy4compiler.parser.CompUtil;
import edu.mit.csail.sdg.gen.LoggerUtil;
import edu.mit.csail.sdg.gen.visitor.FieldDecomposer;
import edu.mit.csail.sdg.gen.visitor.PrettyPrintExpression;
import edu.mit.csail.sdg.gen.visitor.PrettyPrintFunction;
import edu.uw.ece.alloy.util.Utils;

/**
 * This class provides an Alloy program that checks the relational properties over 
 * Each relation in the given alloy program.
 * 
 * The Alloy program has one or more commands. For every command, a slice of the 
 * program is executed. The relations from that slice is extracted and checked.
 * Assuming the given Alloy program is as `P check{q} for i' then a property 
 * like `prop1'. Then, the generated code would be like:
 * 		`open general_props P check{q=>prop1[r1]}' 
 * 		`open general_props P check{prop1[r1]=>q}'
 * It means both directions are checked.
 * @author vajih
 *
 */
public class RelationalPropertiesChecker {

	final private String propertiesModuleFile = "relational_properties.als";

	final private Module world;
	final private String relationalPropertyNameFile;
	final private String alloySepcFileName;


	final Function<String, String> extracerFunction = (String s)->{
		String[] r = s.split(",");
		return r.length > 0 ? r[0] : s;
	};

	public RelationalPropertiesChecker(String relationalPropertyNameFile_, String alloySepcFileName_) {
		this.relationalPropertyNameFile  = relationalPropertyNameFile_;
		this.alloySepcFileName = alloySepcFileName_;
		Module world_ = null;
		try{
			world_ = CompUtil.parseEverything_fromFile(null, null, alloySepcFileName_);
		}catch(Err e){
			LoggerUtil.debug(this, "The input file is not parsed because of: %s%n", e.getMessage());
		}finally{
			world = world_;
		}
	}

	final private List<Sig.Field> getAllFields(){

		final List<Sig.Field> fields = new ArrayList<Sig.Field>();

		//what is inside the world? I am looking for fields
		for(Sig sig:world.getAllSigs()){
			for(Sig.Field field: sig.getFields()){
				fields.add(field);
			}
		}
		return Collections.unmodifiableList(fields);
	}

	final private List<Sig> getAllSigs(){

		return Collections.unmodifiableList(world.getAllSigs().makeCopy());
	}



	final private List<String> getAllProperties(Predicate<String> p, Function<String, String> f) throws FileNotFoundException, IOException{

		String content = Util.readAll(relationalPropertyNameFile);

		return Arrays.asList(content.split("\n")).stream().filter(p).map(f).collect(Collectors.toList());

	}

	final public List<String> getAllBinaryWithDomainWithRangeRelationalProperties() throws FileNotFoundException, IOException{

		return getAllProperties((s)->{return s.contains(",b,d,r");},extracerFunction);

	}

	final public List<String> getAllBinaryWithDomainWithoutRangeRelationalProperties() throws FileNotFoundException, IOException{

		return getAllProperties((s)->{return s.contains(",b,d,0");},extracerFunction);

	}

	final public List<String> getAllBinaryWithoutDomainWithRangeRelationalProperties() throws FileNotFoundException, IOException{

		return getAllProperties((s)->{return s.contains(",b,0,r");},extracerFunction);

	}	

	final public List<String> getAllBinaryWithoutDomainWithoutRangeRelationalProperties() throws FileNotFoundException, IOException{

		return getAllProperties((s)->{return s.contains(",b,0,0");},extracerFunction);

	}

	final public List<String> getAllTernaryWithoutDomainWithoutRangeRelationalProperties() throws FileNotFoundException, IOException{

		return getAllProperties((s)->{return s.contains(",t,0,0");},extracerFunction);

	}	

	/**
	 * 
	 * @param fieldName
	 * @param domainName
	 * @param rangeName
	 * @return
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	final private List<String> generateBinaryProperties(final String fieldName_, final String domainName_, final String rangeName_) throws FileNotFoundException, IOException{

		final List<String> properties = new ArrayList<>();
		final String fieldName = fieldName_.replace("this/", "");
		final String domainName = domainName_.replace("this/", "");
		final String rangeName = rangeName_.replace("this/", "");
		
		for(String binary: getAllBinaryWithDomainWithRangeRelationalProperties())
			properties.add( String.format("%s[%s,%s,%s]", binary, fieldName, domainName, rangeName ) );
		for(String binary: getAllBinaryWithoutDomainWithRangeRelationalProperties())
			properties.add( String.format("%s[%s,%s]", binary, fieldName, rangeName ) );
		for(String binary: getAllBinaryWithDomainWithoutRangeRelationalProperties())
			properties.add( String.format("%s[%s,%s]", binary, fieldName, domainName ) );
		for(String binary: getAllBinaryWithoutDomainWithoutRangeRelationalProperties())
			properties.add( String.format("%s[%s]", binary, fieldName) );

		return Collections.unmodifiableList(properties);
	}

	/**
	 * 
	 * @param fieldName
	 * @param domainName
	 * @param rangeName
	 * @return
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	final private List<String> generateTernyProperties(final String fieldName_, final String domainName_, final String midName_, final String rangeName_) throws FileNotFoundException, IOException{

		final List<String> properties = new ArrayList<>();
		final String fieldName = fieldName_.replace("this/", "");
		final String domainName = domainName_.replace("this/", "");
		final String midName = midName_.replace("this/", "");
		final String rangeName = rangeName_.replace("this/", "");
		
		for(String ternary: getAllTernaryWithoutDomainWithoutRangeRelationalProperties() )
			properties.add( String.format("%s[%s]", ternary, fieldName ) );

		properties.addAll(generateBinaryProperties(
				String.format("%s.%s",domainName, fieldName  ),
				midName, rangeName));
		properties.addAll(generateBinaryProperties(
				String.format("%s.%s",fieldName,  rangeName ),
				domainName, midName));

		return Collections.unmodifiableList(properties);
	}



	/**
	 * transformForChecking gets the file name, extracts its command expr, the fields,
	 * then makes new commands for checking whether: 
	 * 		`command_expr => prop_name[field_name]' or  `prop_name[field_name] => command_expr'.
	 * Each property checking is done in a command with form of:
	 * 		`assert  command_expr_if_prop1_field{ command_expr => prop_name[field_name] }
	 * 		 check command_expr_if_prop1_field'
	 * @param filename is the input file name containing the specification
	 * @return the new files containing the original spec as well as properties to be checked.
	 * A new file is created per each property-assertion.
	 * @throws Err 
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	final public List<String> transformForChecking(final String destFolder) throws Err, FileNotFoundException, IOException{

		if( ! (new File (Utils.appendFileName(destFolder, propertiesModuleFile) ).exists() ) ){
			throw new RuntimeException(String.format("`%s' relational properties module is not in the path. ", 
					Utils.appendFileName(destFolder, propertiesModuleFile) ));
		}

		final String openModule = "open "+propertiesModuleFile.replace(".als", "");

		final String newFileName = alloySepcFileName.replace(".als", "_tc.als");
		final String assertionNameFormat = "%s_if_%s_%s"; 
		final String propertyCheckingFormat = "assert %s{ %s implies %s}\n check %s \n";

		List<String> retFiles = new ArrayList<>();


		//All functions and pred with parameters are to be included in the new alloySpec.
		final StringBuilder functions = new StringBuilder();


		for(Func function: world.getAllFunc()){
			if( function.isPrivate == null )
				functions.append( PrettyPrintFunction.makeString(function) );
		}

		//All sigs are extracted to be included in the new alloySpec
		final StringBuilder sigs = new StringBuilder();
		for(Sig sig: world.getAllSigs()){
			//The position of sigs are properly set and no need to visit it.
			sigs.append("\nsig ").append( Utils.readSnippet(sig.pos) );
		}



		for(Command cmd: world.getAllCommands()){

			//The formula contains the expression that will be formulated as command_expr.
			String formula, commandHeader, commandScope;
			//Check is different from run. The pos included in the formula of check command is 
			//spanned over the whole command.
			commandHeader = cmd.label;
			if(! cmd.check && !commandHeader.contains("$")){
				formula = cmd.label;
			}else{
				formula = PrettyPrintExpression.makeString(cmd.formula);
			}
			commandHeader = commandHeader.replace("$", "");
			//the run command is unnamed
			/*if( commandHeader.contains("$") ){					
					formula = Utils.readSnippet(cmd.formula.pos());
					formula = formula.replaceFirst("[^\\{]*\\{", "");
					formula = formula.replaceFirst("\\}[^\\}]*", "");
					commandHeader = commandHeader.replace("$", "");
				}else{

					formula = cmd.label;
				}*/


			commandScope = Utils.readSnippet(cmd.pos);
			commandScope = commandScope.contains("for") ? commandScope.replaceFirst("[^(for)]*(for )", "for ") : "";

			for(Sig.Field field : getAllFields()){
				final FieldDecomposer  fldDeocmposer = new FieldDecomposer();
				final String fieldName = field.label;
				final List<Expr> sigsInField =  fldDeocmposer.extractFieldsItems(field);

				List<String> properties = new ArrayList<>();
				//Since the owner of the relation is not returned, the size of sigsInField is `1' for binary relations.
				if( sigsInField.size() == 1 ){
					properties.addAll( generateBinaryProperties(fieldName, field.sig.label, sigsInField.get(0).toString() ) );
				}else if( sigsInField.size() == 2 ){
					properties.addAll( generateTernyProperties(fieldName, field.sig.label, sigsInField.get(0).toString(), sigsInField.get(1).toString() ) );
				}		

				for(String property: properties){

					//Make a file name from the property. e.g. total[s.B,B]-> total___s-B, total[s,A]-> total_s
					
					String propertyName = property.replaceAll(",.*", "").replaceAll("\\[","_").replaceAll("\\.", "___");

					//System.out.printf("%s\n\t%s\n",propertyName,property);
					
					final List<String> assertionNames = Arrays.asList(	String.format(assertionNameFormat, commandHeader, propertyName , fieldName),
							String.format(assertionNameFormat, propertyName, fieldName, commandHeader));
					final List<String> assertionBody = Arrays.asList(String.format(propertyCheckingFormat, assertionNames.get(0), 
							formula, property, assertionNames.get(0), commandScope ),
							String.format(propertyCheckingFormat, assertionNames.get(1), 
									property, formula, assertionNames.get(1), commandScope ));

					for(int i = 0; i < 2; ++i){
						final StringBuilder newAlloySpec = new StringBuilder();
						newAlloySpec.append("\n").append(assertionBody.get(i));
						newAlloySpec.insert(0, functions);
						newAlloySpec.insert(0, sigs);
						newAlloySpec.insert(0, openModule);

						final String retFileName = Utils.appendFileName(destFolder, assertionNames.get(i) + "_" + newFileName); 

						Util.writeAll(  retFileName,
								newAlloySpec.toString());

						retFiles.add(retFileName);
					}

				}

			}
		}

		//world.getAllCommands().stream().forEach((t)->{System.out.printf("%s \n %s \n %s \n %s \n %b \n %s \n\n\n",t.label, Utils.readSnippet(t.pos()),Utils.readSnippet(t.formula.pos), t.getHTML(), t.check, t);});

		return Collections.unmodifiableList(retFiles);
	}
}
