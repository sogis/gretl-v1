# gretl

Das Datenmanagement-Tool GRETL ist ein Werkzeug, das für Datenimports, Datenumbauten
(Modellumbau) und Datenexports eingesetzt wird. GRETL führt Jobs aus, wobei ein Job aus
mehreren atomaren Tasks besteht. Damit ein Job als vollständig ausgeführt gilt, muss jeder zum
Job gehörende Task vollständig ausgeführt worden sein. Schlägt ein Task fehl, gilt auch der Job
als fehlgeschlagen.

Ein Job besteht aus einem oder mehreren Tasks, die gemäss einem gerichteten Graphen (Directed Acyclic Graph; DAG)
miteinander verknüpft sind.

Ein Job kann aus z.B. aus einer linearen Kette von Tasks bestehen:

    Step 1 – Step 2 – Step 3 – Step n

Beispiel: Datenimport aus INTERLIS-Datei – Datenumbau – Datenexport nach Shapefile

Ein Job kann sich nach einem Task aber auch auf zwei oder mehr verschiedene weitere Tasks
verzweigen:

          - Step 2 – Step 3 – Step n
    Step 1 –
          – Step 4 – Step 5 – Step m

Beispiel: Datenimport aus INTERLIS-Datei – Datenumbau in Zielschema 1 und ein zweiter
Datenumbau in Zielschema 2

Es ist auch möglich, dass zuerst zwei oder mehr Tasks unabhängig voneinander
ausgeführt werden müssen, bevor ein einzelner weiterer Step ausgeführt wird.

    Step 1 –
           – Step 3 – Step 4 – Step n
    Step 2 –

Die Tasks eines Jobs werden per Konfigurationsfile konfiguriert.

## Kleines Besipiel

Erstellen sie in einem neuen Verzeichnis ``gretldemo`` eine neue Datei ``build.gradle``:

```
import ch.so.agi.gretl.tasks.*

apply plugin: 'ch.so.agi.gretl'

buildscript {
	repositories {
		maven {
			url "http://jars.interlis.ch"
		}
		maven {
			url "http://jars.umleditor.org"
		}
		maven {
			url "http://download.osgeo.org/webdav/geotools/"
		}
		mavenCentral()
	}
	dependencies {
		classpath group: 'ch.so.agi', name: 'gretl',  version: '1.0.+'
	}
}

defaultTasks 'validate'


task validate(type: IliValidator){
    dataFiles = ["BeispielA.xtf"]
}
```

Die Datei ``build.gradle`` ist die Job Konfiguration. Dieser kleine Beispiel-Job besteht nur aus einem einzigen Task: ``validate``.

Erstellen Sie nun noch die Datei ``BeispielA.xtf`` (damit danach der Job erfolgreich ausgeführt werden kann).

```
<?xml version="1.0" encoding="UTF-8"?>
<TRANSFER xmlns="http://www.interlis.ch/INTERLIS2.3">
	<HEADERSECTION SENDER="gretldemo" VERSION="2.3">
	</HEADERSECTION>
	<DATASECTION>
		<OeREBKRMtrsfr_V1_1.Transferstruktur BID="B01">
		</OeREBKRMtrsfr_V1_1.Transferstruktur>
	</DATASECTION>
</TRANSFER>
```

Um den job auszuführen, wechslen Sie ins Verzeichnis mit der Job Konfiguration, und geben da das Kommando ``gradle`` ohne 
Argument ein:

    cd gretldemo
    gradle

Sie sollten etwa folgende Ausgabe erhalten:

```
Starting a Gradle Daemon, 1 incompatible and 1 stopped Daemons could not be reused, use --status for details
Download http://jars.umleditor.org/ch/so/agi/gretl/maven-metadata.xml
Download http://jars.umleditor.org/ch/so/agi/gretl/1.0.4-SNAPSHOT/maven-metadata.xml
Download http://jars.umleditor.org/ch/so/agi/gretl/1.0.4-SNAPSHOT/gretl-1.0.4-20180104.152357-34.jar

BUILD SUCCESSFUL in 21s
```

``BUILD SUCCESSFUL`` zeigt an, dass der Job (die Validierung der Datei ``BeispielA.xtf``) erfolgreich ausgeführt wurde.

## System Anforderungen
Um die aktuelle Version von gretl auszuführen, muss 

 - die JAVA-Laufzeitumgebung (JRE), Version 1.8 oder neuer, und 
 - gradle, Version 3.4 oder neuer, auf Ihrem System installiert sein.
 
Die JAVA-Laufzeitumgebung (JRE) kann auf der Website http://www.java.com/ gratis bezogen werden.

Die gradle-Software kann auf der Website http://www.gradle.org/ gratis bezogen werden.

Um gretl laufen zulassen benötigen sie typischerweise eine Internet Verbindung (Ein Installation, die keine Internet Verbindung benötigt ist auch möglich, aber aufwendig).

## Installation
gretl selbst muss nicht explizit installiert werden, sondern wird dynamisch durch das Internet bezogen.

## Ausführen
Um gretl auszuführen, geben Sie auf der Kommandozeile folgendes Kommando ein (wobei ``jobfolder`` der absolute Pfad 
zu ihrem Verzeichnis mit der Job Konfiguration ist.)

    gradle --project-dir jobfolder
    
Alternativ können Sie auch ins Verzeichnis mit der Job Konfiguration wechseln, und da das Kommando ``gradle`` ohne 
Argument verwenden:

    cd jobfolder
    gradle


## Tasks

### CsvExport
Daten aus einer bestehenden Datenbanktabelle werden in eine CSV-Datei exportiert.

Beispiel:
```
def db_uri = 'jdbc:postgresql://localhost/gretldemo'
def db_user = "dmluser"
def db_pass = "dmluser"

task csvexport(type: CsvExport){
    database = [db_uri, db_user, db_pass]
	schemaName = "csvexport"
	tableName = "exportdata"
	firstLineIsHeader=true
	attributes = [ "t_id","Aint"]
    dataFile = "data.csv"
}
```

Parameter | Beschreibung
----------|-------------------
database | Datenbank aus der exportiert werden soll
dataFile  | Name der CSV Datei, die erstellt werden soll
tableName | Name der DB-Tabelle, die exportiert werden soll
schemaName | Name des DB-Schemas, in dem die DB-Tabelle ist.
firstLineIsHeader | Definiert, ob eine Headerzeile geschrieben werden soll, oder nicht. Default: true
valueDelimiter | Zeichen, das am Anfang und Ende jeden Wertes geschrieben werden soll. Default ``"``
valueSeparator | Zeichen, das als Trennzeichen zwischen den Werten verwendet werden soll. Default: ``,``
attributes | Spalten der DB-Tabelle, die exportiert werden sollen. Default: alle Spalten
encoding | Zeichencodierung der CSV-Datei, z.B. ``"UTF-8"``. Default: Systemeinstellung

Geometrie-Saplten können nicht exportiert werden.

### CsvImport

Beispiel:
```
def db_uri = 'jdbc:postgresql://localhost/gretldemo'
def db_user = "dmluser"
def db_pass = "dmluser"

task csvimport(type: CsvImport){
    database = [db_uri, db_user, db_pass]
	schemaName = "csvimport"
	tableName = "importdata"
	firstLineIsHeader=true
    dataFile = "data1.csv"
}
```

### CsvValidator

Beispiel:
```
task validate(type: CsvValidator){
	models = "CsvModel"
	firstLineIsHeader=true
    dataFiles = ["data1.csv"]
}
```

### Db2Db

```

task transferSomeData(type: Db2Db) {
    sourceDb = ['jdbc:postgresql://192.168.50.4:5432/xanadu','login1','password1']
    targetDb = ['jdbc:sqlite:/road/to/mandalay.sqlite',null,null]
    transferSets = [
            new TransferSet('../../can/be/a/relative/path/some.sql', 'albums_dest', true)
    ];
}
```
Copies data from a source database (`sourceDb`) to target database (`targetDb`). It can handle an arbitrary count of transfers in one task.

 `sourceDB` / `targetDB`: A list with a valid jdbc database url, the login name and the password.

`transferSets`: A list of `TransferSet`. A transfer set consists of the path to the file containing the sql select statement, the destination table and the option to delete the destination table contents before adding the new data.

Supported Databases: PostgreSQL and SQLite
### Ili2pgExport

Beispiel:
```
def db_uri = 'jdbc:postgresql://localhost/gretldemo'
def db_user = "dmluser"
def db_pass = "dmluser"

task exportData(type: Ili2pgExport){
    database = [db_uri, db_user, db_pass]
    dataFile = "lv03_254900-out.itf"
    dataset = "254900"
    logFile = "ili2pg.log"
}
```

### Ili2pgImport

Beispiel:
```
def db_uri = 'jdbc:postgresql://localhost/gretldemo'
def db_user = "dmluser"
def db_pass = "dmluser"

task importData(type: Ili2pgImport){
    database = [db_uri, db_user, db_pass]
    dataFile = "lv03_254900.itf"
    logFile = "ili2pg.log"
}
```

### Ili2pgImportSchema

Beispiel:
```
def db_uri = 'jdbc:postgresql://localhost/gretldemo'
def db_user = "dmluser"
def db_pass = "dmluser"

task importSchema(type: Ili2pgImportSchema){
    database = [db_uri, db_user, db_pass]
    models = "DM01AVSO24"
    dbschema = "gretldemo"
    logFile = "ili2pg.log"
}
```

### Ili2pgReplace

Beispiel:
```
```

### Ili2pgUpdate

Beispiel:
```
```

### IliValidator

Beispiel:
```
task validate(type: IliValidator){
    dataFiles = ["Beispiel2a.xtf"]
    logFile = "ilivalidator.log"
}
```

### ShpExport

Beispiel:
```
def db_uri = 'jdbc:postgresql://localhost/gretldemo'
def db_user = "dmluser"
def db_pass = "dmluser"

task shpexport(type: ShpExport){
    database = [db_uri, db_user, db_pass]
	schemaName = "shpexport"
	tableName = "exportdata"
    dataFile = "data.shp"
}
```

### ShpImport

Beispiel:
```
def db_uri = 'jdbc:postgresql://localhost/gretldemo'
def db_user = "dmluser"
def db_pass = "dmluser"

task shpimport(type: ShpImport){
    database = [db_uri, db_user, db_pass]
	schemaName = "shpimport"
	tableName = "importdata"
    dataFile = "data.shp"
}
```

### ShpValidator

Beispiel:
```
task validate(type: ShpValidator){
	models = "ShpModel"
    dataFiles = ["data.shp"]
}
```

### SQLExecutor

```
import ch.so.agi.gretl.steps.*

task executeSomeSql(type: SqlExecutor){
    database = ['jdbc:postgresql://192.168.50.4:5432/xanadu','login1','password1']
    sqlFiles = ['/can/be/an/absolute/path/some.sql']
}
```

This task can be used to execute _any_ sql statement in one database.

`database`: A list with a valid jdbc database url, the login name and the password.

`sqlFiles`: A list with files names containing _any_ sql statements.

Supported Databases: PostgreSQL and SQLite
