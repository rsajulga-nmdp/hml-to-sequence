package org.nmdp.steps;

import org.hamcrest.Matchers;
import org.jbehave.core.annotations.Alias;
import org.jbehave.core.annotations.Composite;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;

import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.Matchers.equalTo;

public class ParseHmlSteps {

	private Integer test;
	
	@Given("an HML file")
	public void loadingHmlFile() {
		test = 5;
	}
	
	@When("I want to parse the file for results")
	public void parseHmlFile() {
		test = test + 5;
	}
	
	@Then("The resulting output will be 5")
	public void evaluateHmlFile() {
		assertThat(test, Matchers.equalTo(10));
	}
}
