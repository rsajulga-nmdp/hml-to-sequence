package org.nmdp.steps;

import org.hamcrest.Matchers;
//import org.jbehave.core.annotations.Alias;
//import org.jbehave.core.annotations.Composite;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;

import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import org.nmdp.Run;

//import static org.hamcrest.Matchers.equalTo;

public class ParseHmlSteps {

	private String hmlFilepath;
	private String obsOutfileName;
	
	public ParseHmlSteps() {
		
	}
	
	private String getFilepath(String filename, boolean fullPath) {
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		File file = new File(classloader.getResource(filename).getFile());
		String absolutePath = file.getAbsolutePath();
		if (fullPath) {
			return absolutePath;
		} else {
			String filepath = absolutePath.
					substring(0, absolutePath.lastIndexOf(File.separator));
			return filepath;
		}
	}
	
	private List<String> readLines(String filename) {
		List<String> lines = Collections.emptyList();
		try {
			lines = Files.readAllLines(
					Paths.get(new File(filename).toURI()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return lines;
	}
	
	private void runHml2Seq(String input, String output) {
		Run.main(new String[]{input, output});
	}
	
	
	@Given("an HML file as $filename")
	public void loadingHmlFile(String hmlFilename) {
		hmlFilepath = getFilepath(hmlFilename, false);
	}
	
	@When("I want to parse the file for results")
	public void parseHmlFile() {
		String filename = "obsOutput.txt";
		runHml2Seq(hmlFilepath, filename);
		obsOutfileName = filename;
	}
	
	@Then("the resulting output will be $expOutfilename")
	public void evaluateHmlFile(String expOutfilename) {
		String expOutFilepath = getFilepath(expOutfilename, true);
		
		assertThat(readLines(obsOutfileName),
				Matchers.equalTo(readLines(expOutFilepath)));
	}
}
