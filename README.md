# hml-to-sequence
Extracts Sequence from HML

Requires Java 8 and Maven

## Build

```
mvn clean package
```

## Run
```
java -jar target/hml2sequence-1.0-SNAPSHOT.jar
```

#Usage
This program extracts the consensus sequences from HML (histoimmunogenetic markup language) files. It requires specification of:
- an input folder containing HML files to be parsed (<hml_files_folder>)
- an output filename to pipe the resulting sequences (<output_sequences.txt>)
```
java -jar target/hml2sequence-1.0-SNAPSHOT.jar <hml_files_folder> <output_sequences.txt>
```