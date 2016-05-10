package edu.uw.ece.alloy.debugger.mutate;

import static org.junit.Assert.*;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.Util;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.ProcessorUtil;
import edu.uw.ece.alloy.util.LazyFile;

/**
 * Testing DebuggerRunnerTest
 * @author vajih
 *
 */
public class DebuggerRunnerTest {

	InetSocketAddress testingHost;
	
	final long startTime = System.currentTimeMillis();

	public final void print(String... args) {
		final long current = System.currentTimeMillis() - startTime;
		System.out.print(current + " - ");
		for (String arg : args)
			System.out.print(arg + " ");
		System.out.println();
	}
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		testingHost = ProcessorUtil.findEmptyLocalSocket();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testStrongestApproximation() throws Err {
		File tmpLocalDirectory = new File("tmp/testing");
		File toBeAnalyzedCode = new LazyFile(tmpLocalDirectory,
				"toBeAnalyzedCode.als");
		Util.writeAll(toBeAnalyzedCode.getAbsolutePath(),
				"sig A{r: one A}\n pred p[]{  some A and no A.r}\nrun {p implies some A}");
		
		File relationalPropModuleOriginal = new LazyFile(tmpLocalDirectory,
				"relationalPropModuleOriginal.als");
		Util.writeAll(relationalPropModuleOriginal.getAbsolutePath(),
				"pred weaklyConnected [ r :univ->univ,  left:univ, right:univ ] {\n"
						+ "\tall d: right | all g: left - d  | d in g.^(r + ~r)\n"
						+ "}\n"
						+ "pred rootedOne [r: univ->univ, left:univ, right: univ]{"
								+ "\tone root:left | right in root.*r" + "}"
						);

		File temporalPropModuleOriginal = new LazyFile(tmpLocalDirectory,
				"temporalPropModuleOriginal.als");
		Util.writeAll(temporalPropModuleOriginal.getAbsolutePath(),
				"open relational_lib as relational_properties\n"
						+ "pred OrdDcrsStrc_SzGrwtStrc_Glbl_SdEnd_EmptNon_[r: univ->univ->univ, left, middle, right: univ, left_first: univ, left_next: univ->univ, right_first: univ, right_next: univ->univ]{\n"
						+ "\tall left': left - relational_properties/last[left,left_next] |let left'' = left'.left_next |\n"
						+ "\tlet c = middle.(left'.r) |let c' = middle.(left''.r) | let delta = c' - c |  (c in c' ) and   (c' !in c ) and   (some delta implies(some c implies relational_properties/lt[relational_properties/max[delta,right_next],relational_properties/min[c,right_next],right_next] ) )\n"
						+ "}");

		File relationalLib = new LazyFile(tmpLocalDirectory,
				"relational_lib.als");
		Util.writeAll(relationalLib.getAbsolutePath(), "module relational_lib\n"
				+ "fun last[elem: univ, next:univ->univ]: one univ { elem - (next.elem) }\n"
				+ "fun max [es: set univ, next: univ->univ ]: lone univ { es - es.^(~(next)) }\n"
				+ "fun min [es: set univ, next: univ->univ ]: lone univ { es - es.^(next) }\n"
				+ "pred lt [e1, e2: univ, next:univ->univ ] { e1 in this/prevs[e2, next] }\n"
				+ "fun prevs [e: univ, next:univ->univ ]: set univ { e.^(~(next)) }\n");
		
		List<File> dependentFiles = new ArrayList<>();
		dependentFiles.add(relationalLib);

		DebuggerRunner runner = new DebuggerRunner(toBeAnalyzedCode, dependentFiles, testingHost);
		runner.approximator = new Approximator(runner.approximator.interfacE, runner.approximator.processManager, runner.approximator.tmpLocalDirectory, 
				toBeAnalyzedCode, relationalPropModuleOriginal, temporalPropModuleOriginal, dependentFiles);
		runner.debuggerAlgorithm.approximator = runner.approximator;
		assertEquals(1, runner.debuggerAlgorithm.model.size());
		assertEquals(1, runner.debuggerAlgorithm.fields.size());
		runner.start();
		runner.debuggerAlgorithm.run();
	}

}