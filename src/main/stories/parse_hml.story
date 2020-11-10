
Scenario: Parsing an HML file

Given an HML file as test_hml.xml
When I want to parse the file for results
!-- We don't care for the results, just the gallery
Then the resulting output will be test_out.txt