# hml-to-sequence
Extracts Sequence from HML

Requires Java 8 and Maven

## Build

```
mvn clean package
```

## Usage
This program extracts the consensus sequences from HML (histoimmunogenetic markup language) files. It requires specification of:
- an input folder containing HML files to be parsed (<hml_files_folder>)
- an output filename to pipe the resulting sequences (<output_sequences.txt>)
```
java -jar target/hml2sequence-1.0-SNAPSHOT.jar <hml_files_folder> <output_sequences.txt>
```

### Tests

The functionality of this program is verified through BDD (behavior-driven development). [JBehave](https://jbehave.org/) is used to facilitate BDD tests. To check the status of tests ran through Maven, check `target/jbehave/view/reports.html`.
