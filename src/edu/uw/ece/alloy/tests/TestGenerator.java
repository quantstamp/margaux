package edu.uw.ece.alloy.tests;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;
import edu.mit.csail.sdg.alloy4compiler.translator.A4SolutionReader;
import edu.mit.csail.sdg.alloy4viz.AlloyInstance;
import edu.mit.csail.sdg.alloy4viz.StaticInstanceReader;
import edu.mit.csail.sdg.alloy4whole.ExampleUsingTheCompiler;
import edu.uw.ece.alloy.util.TestInputs;
import edu.uw.ece.alloy.util.Utils;


@RunWith(Parameterized.class)
public class TestGenerator {
	
	private final File f;

	public TestGenerator(final File f) {
		this.f = f;
	}

	@Parameterized.Parameters
	public static Collection<Object[]> files() {
		return TestInputs.generatorAlloy();
	}

	@Test
	public void run() throws Err {
		String alsPath = f.getAbsolutePath();
		String alsName = f.getName();
		String[] inputs = new String[]{alsPath};
		
		
		
		String alsInst = ExampleUsingTheCompiler.run(inputs);
		String solPath = alsPath.replaceAll(".als", ".out.xml");
		File outFile = new File(solPath);
		assertTrue("The generated answer does not exist!",(outFile).exists());

		String instPath = alsPath.replaceAll(".als", ".xml");
		File instFile = new File(instPath);
		assertTrue("The answer does not exist!",(instFile).exists());

		
		AlloyInstance inst = StaticInstanceReader.parseInstance(instFile);
		System.out.println("The expected instance:\n\t\t"+inst);
		AlloyInstance out = StaticInstanceReader.parseInstance(outFile);
		System.out.println("The resulted instance:\n\t\t"+out);

		assertTrue("The answer is not equel!",inst.equalsWithoutFileName(out));
	}


}
