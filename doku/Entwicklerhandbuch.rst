=========================
Entwicklerhandbuch
=========================

****************************
1.	Installation
****************************
**1.1.	Benötigte Software**

- SDKMAN:
- JAVA JDK: Version 8u121
- Gradle: Version 3.4.1
- INTELLIJ IDEA: 2017.1.1


**1.2.	Vorgehen Installation**


1.2.1.	SDKMAN installieren

SDKMAN gemäss Anleitung (http://sdkman.io/install.html) installieren. Anschliessend soll getestet werden, ob die Installation erfolgreich abgeschlossen wurde, in dem auf der Kommandozeile den Befehl "sdk" ausgeführt wird.

Testen ob Installation in Ordnung: Auf Kommandozeile den SDKMAN-Befehl "sdk" ausführen.
1.2.2.	Java
Um Java zu installieren muss folgender Befehl in der Konsole ausgeführt werden:

sdk i java 8u121


Dies installiert die JDK Version 8u121 in ~/.sdkman/candidates/java/8u121 und setzt alle notwendigen Umgebungsvariablen für Java. Anschliessen soll in der Konsole getestet werden, ob Java richtig installiert wurde.

java -version

1.2.3.	Gradle

Folgender Befehl gilt es auf der Konsole auszuführen:

sdk i gradle 3.4.1


Dies installiert Gradle in der Version 3.4.1 in ~/.sdkman/candidates/gradle/3.4.1 und setzt alle notwendigen Umgebungsvariablen für Gradle. Anschliessen soll in der Konsole getestet werden, ob Gradle richtig installiert wurde.

gradle

1.2.4.	INTELLIJ IDEA

Version 2017.1.1 mit JDK herunterladen (https://download.jetbrains.com/idea/ideaIC-2017.1.1.tar.gz). Anschliessend muss es ins Homeverzeichnis entpackt werden:

tar xf ideaIC-2017.1.1.tar.gz -C ~

Starten von IDEA mittels idea.sh im Verzeichnis ~/idea-IC-171.4073.35/bin

./idea.sh

"""""""""""""""""

**2.	GRETL - Code**

**2.1.	Aufbau**

Das Projekt GRETL ist in drei Packages aufgeteilt: logging, steps und util. Im Package logging befinden sich Java-Klassen, welche zum loggen eingesetzt werden. Im Modul steps befinden sich die Java-Klassen der Steps und die dazugehörigen Tasks. Im Package util befinden sich Klassen, welche verschiedene Aufgaben übernehmen (z.B. DbConnector, versch. Exceptions, SqlReader etc.)



**2.2.	Lib**

Im Verzeichnis lib (gretl/lib) sind die benötigten Bibliotheken abgelegt. Darunter sind vor allem jdbc-Treiber für die unterschiedlichen Datenbanktypen.

**2.3.	Util**

Die folgenden Java-Klassen befinden sich im Package "util".

2.3.1.	DbConnector

Package: 	ch.so.agi.gretl.util

Bei der DbConnector-Klasse handelt es ich um eine Utility-Class, welche nur eine Methode (connect) aufweist. Diese erstellt eine Connection.

2.3.1.1.	Methode connect

Benötigt:  	ConnectionURL (String), UserName (String), Password (String)

Liefert: 	Connection

Die Methode erstellt mittels der ConnectionURI, de, Benutzername und dem dazugehörigen Passwort eine Verbindung zur angegebenen Datenbank und liefert diese als returnvalue zurück.

Beispiel::

   DbConnector x = new DbConnector();
   Connection xcon = x.connect("jdbc:derby:memory:myInMemDB;create=true", "public", null);

2.3.2.	FileExtension

Package:	 ch.so.agi.gretl.util

Die FileExtension-Klasse ist darauf ausgelegt, dass sie von einem beliebigen File die File-Extension ermittelt.

2.3.2.1.	Methode getFileExtension

Benötigt: 	inputFile (File)

Liefert: 	FileExtension (String)

Von dem übermittelten File wird mittels dieser Methode die File-Extension (Dateiendung) ermittelt.

Beispiel::

   String fileExtension = FileExtension.getFileExtension(file);
   if (!fileExtension.equals("sql")){ Do something }

2.3.3.	SqlReader

Package: 	ch.so.agi.gretl.util

Der SqlReader liest die sqlstatements aus einem File aus. 

2.3.3.1. Methode createPushbackReader

Benötigt: File

Liefert: PushbackReader

2.3.3.2. Methode nextSqlStmt 

Benötigt: 

Liefert: 

2.3.3.3.	Methode readsqlStmt

Benötigt: File

Liefert:	SqlStatement (String)

Erstelllt zuerst durch Aufruf der Methode createPushbackReader einen Reader und schreibt am Ende ein Statement raus. 

(HIER FEHLEN NOCH VIELE METHODEN!!!)


Beispiel::

   line = SqlReader.readSqlStmt(targetFile);

2.3.4.	TransactionContext

Package: 	ch.so.agi.gretl.steps

Führt eine Methode auf der Datenbank aus.

2.3.4.1.	Methode getDbConnection

Benötigt: 	ConnectionURL (String), UserName (String), Password (String)

Liefert: 	Connection

Die Methode führt die Methode DbConnector.connect mit den oben erwähnten Parametern aus. Von dieser Methode wird eine Connection zurückgeliefert, welche auch die getDbConnection zurückliefert.

Beispiel::
   public TransactionContext  sourceDb;
   new SqlExecutorStep().execute(sourceDb.getDbConnection(),sqlFiles);

2.3.5.	Logger

Package: 	ch.so.agi.gretl.logging

Beinhaltet die Methode log um Informationen zu loggen.

2.3.5.1.	Methode log

Benötigt: 	LogLevel (int), Message (String)
Die Methode log schreibt die übergebene Nachricht (Message) mit dem LogLevelhinweis (INFO, DEBUG, ERROR, LIVECYCLE), je nach Einstellung nach System.err. Die Standardeinstellung sieht vor, dass logmessages mit dem Info-Level nach System.err geschrieben werden, während die logmessages mit dem Debug-Level gar nicht erst geloggt werden.
Folgende LogLevel gibt es: INFO_LEVEL, DEBUG_LEVEL

Beispiel::

   Logger.log(Logger.INFO_LEVEL,"Task start");


2.3.6.	EmptyFileException

Package: 	ch.so.agi.gretl.core

Die EmptyFileException soll geworfen werden, wenn ein File, welches nicht leer sein darf, trotzdem leer ist. Wenn beispielweise das SQL-File, welches beim Db2Db-Step gelesen werden soll, leer ist, soll keine allgemeine, sondern diese spezifische Exception geworfen werden.

Beispiel::

   throw new EmptyFileException("EmptyFile: "+targetFile.getName());

2.3.7.	NotAllowedSqlExpressionException

Package: 	ch.so.agi.gretl.core

Die NotAllowedSqlExpressionException soll geworfen werden, wenn in einem SQL-Statement einen Ausdruck enthalten ist, der in diesem Zusammenhang nicht erlaubt ist. (Beispiel: Im SQL-File, welches im Db2Db-Step verwendet wird, ist kein Delete, Update, Insert etc. erlaubt).

Beispiel::

   throw new NotAllowedSqlExpressionException();

**2.4.	Core – Test**

2.4.1.	DbConnectorTest

Package: 	ch.so.agi.gretl.core

Die Klasse DbConnectorTest testet gewisse Funktionalitäten der DbConnector-Klasse (s. Kapitel 2.3.1).
connectToDerbyDb: Testet, ob eine Verbindung zur lokalen Derby-Db herstellen kann.
connectionAutoCommit: Testet, ob AutoCommit wirklich off ist.

2.4.2.	FileExtensionTest

Package: 	ch.so.agi.gretl.core

Die Klasse FileExtensionTest überprüft die Funktionalitäten der FileExtension-Klasse (s. Kapitel 2.3.2). Hierfür wird in einem ersten Schritt einen temporären Ordner angelegt, welcher nach den Tests wieder gelöscht wird.
getFileExtension: Prüft, ob die Methode bei einem File mit der Endung .sql auch die Endung sql ermittelt wird.
missingFileExtension: Prüft, ob bei einem File ohne Endung auch wirklich eine Fehlermeldung ausgegeben wird.
mutipleFileExtension: Prüft, ob bei einem File mit mehreren Endungen (file.ext1.ext2) auch wirklich die letzte Fileendung ausgegeben wird.
strangeFileNameExtension: Prüft, ob bei einem File mit folgendem Namen (c:\\file) auch wirklich eine Fehlermeldung ausgeworfen wird.

2.4.3.	LoggerTest

Package: 	ch.so.agi.gretl.logging

Benötigt: 	core/src/test/resources/simplelogger.properties
Mit der LoggerTest-Klasse wird die Funktionalität der Logger-Klasse (s. Kapitel 2.3.5) überprüft.
logInfoTest: Prüft, ob die geworfene Logmeldung der Erwartung entspricht.
logDebugTest: Durch die Herabsetzung des DefaultLogLevels mittels simplelogger.properties wird geprüft, ob die in System.err geworfene Logmeldung der Erwartung entspricht.
inexistentLoglevel: Prüft, ob eine Fehlermeldung zurückgeworfen wird, wenn ein nicht existentes LogLevel verwendet wird.

**2.5.	Steps - Main**

2.5.1.	Db2DbStep

Package: 	ch.so.agi.gretl.steps

Die Db2DbStep-Klasse beinhaltet den Db2Db-Step. Sie dient dem Umformen und Kopieren von einer Datenbank in eine andere. In einem SQL-File wird dabei das SQL-Statement für den Input-Datensatz erstellt, der dann in die Output-Datenbank geschrieben werden soll.

2.5.1.1.	Methode processAllTransferSets

Diese Methode ruft für jedes in der Liste aufgeführte Transferset die Methode processTransferSet auf.

Beispiel::

   processAllTransferSets(TransactionContext sourceDb, TransactionContext targetDb, List<TransferSet> transferSets)

2.5.1.2.	Methode processTransferSet

Dies ist nun die Methode, welche ein TransferSet abarbeitet. Dabei werden verschiedene andere Methoden aufgerufen.
Als erstes wird überprüft, ob im TransferSet die Option getDeleteAllRows auf True gesetzt ist. Ist das der Fall, wird die Methode deleteDestTableContents aufgerufen, welche den Inhalt der ZielTtabelle löscht.
Danach wird mit der Methode extractSingleStatement ein Statement aus dem SQL-File, welches im TransferSet definiert ist, extrahiert und gleich auf unerlaubte Ausdrücke (Delete, Insert, Update etc.) überprüft. Danach wird mit der Methode createResultSet das Statement ausgeführt und anschliessend wird mit der Methode createInsertRowStatement ein SQL-INSERT-Statement vorbereitet. Dieses wird in der Methode transferRow mit den Werten aus dem ResultSet abgefüllt.

Beispiel::

   processTransferSet(sourceDbConnection, targetDbConnection, transferSet);


2.5.1.3	Methode deleteDestTableContents

Diese Methode löscht alle Einträge in der Ziel-Tabelle. Dies geschieht nicht mit "truncate", sondern mit "DELETE FROM". Der Grund dafür ist, dass ein Truncate alleine in einer Transaktion stehen müsste und nicht zusammen mit anderen Querys übermittelt (commited) werden kann.

Beispiel::

   deleteDestTableContents(targetCon, transferSet.getOutputQualifiedSchemaAndTableName());

2.5.1.4 	Methode createResultSet

Diese Methode führt das sqlSelectStatement aus und liefert ein ResultSet (rs) zurück)

Beispiel::

   ResultSet rs = createResultSet(srcCon, selectStatement);

2.5.1.5 	Methode createInsertRowStatement

Diese Methode erstellt das Insert Statement. Dazu werden über die Funktion getMetaData die Metadaten, konkret die columnNames (Spaltennamen) ausgelesen. Die Spaltennamen werden dann zusammengesetzt und im Insert-Statement eingesetzt. Gleichzeitig werden der Anzahl Spalten entsprechend Fragezeichen in die VALUES geschrieben, welche in einer späteren Methode durch die entsprechenden Werten ersetzt werden.

Beispiel::

   createInsertRowStatement(srcCon,rs,transferSet.getOutputQualifiedSchemaAndTableName());

2.5.1.6	Methode extractSingleStatement

Benötigt: File targetFile

Diese Methode extrahiert aus einem definierten File ein SQL Statement. Dabei wird auch auch überprüft ob das File nur ein Statement enthält, oder ob es eventuell auch weitere gibt. Des Weiteren wird auch überprüft, ob eventuelle nicht erlaubte Ausdrücke im Statement vorkommen (z.B. DELETE, INSERT oder UPDATE).

Beispiel::

   extractSingleStatement(transferSet.getInputSqlFile());

2.5.1.7	Methode transferRow

Benötigt: ResultSet rs, PreparedStatement insertRowStatement, int columncount

Diese Methode ersetzt die "?" vominsertRowStatement mit den Werten, die das ResultSet zurückliefert. Im Anschluss wird dieses Statement ausgeführt.

Beispiel::

   while (rs.next()) {transferRow(rs, insertRowStatement, columncount);}


2.5.2.	Db2DbStepTask

Package: 	ch.so.agi.gretl.steps

Die Klasse Db2DbStepTask repräsentiert den Task zum Db2DbStep. Diese Klasse verlangt nach drei Inputs; der sourceDb, der targetDb und eines oder mehrerer TransferSets. Ein Beispiel wie ein solcher Task aussehen könnte:
::

   task TestTask(type: Db2DbStepTask, dependsOn: 'TestTask2') {
       sourceDb =  new TransactionContext("jdbc:postgresql://host:port/db","user",null);
       targetDb = new TransactionContext("jdbc:postgresql://host:port/db","user",null);
       transferSet = [new TransferSet(true,new java.io.File('path/to/file'),'schema.table')];
   }


2.5.3.	SqlExecutorStep

Package: 	ch.so.agi.gretl.steps

Die SqlExecutorStep-Klasse beinhaltet den Step SQLExecutor und führt dementsprechend die übergebenen sql-Statements auf der übergebenen Datenbank aus. Die sql-Statements werden aber nicht commited.

2.5.3.1.	Methode execute

Benötigt: 	db (Connection), sqlfiles (List<File>)

Die Methode execute überprüft in einem ersten Schritt, ob mindestens ein File angegeben wurde und loggt die Filenamen inkl. Pfade. Anschliessend wird überprüft, ob eine DB-Connection übergeben wurde und ob, die Files alle die korrekte Fileextension (Dateiendung) "sql" aufweisen. Zum Abschluss wird jedes File mit der Methode executesqlScript (s. Kapitel 2.5.3.2) ausgeführt.

2.5.3.2.	Methode executeSqlScript

Benötigt: conn (Connection), inputStreamReader (InputStreamReader)

Die Methode executeSqlScript liest mittels der Methode readerSqlStmt (s. Kapitel 2.3.3.1) jede einzelne Zeile eines SQL-Files. Diese wird auch gleich auf der Datenbank ausgeführt (aber nicht commited!).

2.5.4.	SqlExecutorStepTask

Package: 	ch.so.agi.gretl.steps

Die Klasse SqlExecutorStepTask repräsentiert den Task zum SqlExecutorStep. Sie verlangen einen TransactionContext (sourceDb) und und eine Liste mit (SQL-)Files (sqlFiles).
In der TaskAction werden die beiden Inputs (sourceDb, sqlFiles) an die Methode execute des SqlExecutorStep (s. Kapitel 2.5.3.1) übergeben und die Methode ausgeführt. Im Anschluss an diese Methode wird ein Commit auf der Datenbank ausgeführt und so die SQL-Statements ausgeführt und die Daten geschrieben.

2.5.5.	TransferSet

Package: 	ch.so.agi.gretl.steps

Die Klasse TransferSet definiert die Gestalt eines TransferSets. Es besteht aus drei Parametern:
- Ein Boolean-Wert, der definiert, ob der Inhalt der Zieltabelle vorgängig gelöscht werden soll.
- Ein Input-File, in welchem ein SELECT_Statement die Struktur der Input-Daten definiert.
- Ein String, bestehend aus Schema und Tabelle des gewünschten Outputs.

**2.6.	Steps – Test**

2.6.1.	Db2DbStepTest

Package: 	ch.so.agi.gretl.steps

Die Klasse Db2DbStepTest überprüft die Funktionalitäten der Db2DbStep-Klasse. Bisher liegen die folgenden Tests vor:
PositiveTest(): Dieser Test ist ein positiv-Test, das heisst, er überprüft, ob der Db2DbStep grundsätzlich funktioniert.
NotAllowedSqlExpressionInScriptTest(): Dieser Test überprüft, ob bei der Verwendung eines nicht erlaubten Ausdruck in einem SQL-File eine Exception geworfen wird.
Db2DbEmptyFileTest(): Überprüft, ob bei einem leeren File eine EmptyFileException geworfen wird.
SQLExceptionTest(): Überprüft, ob bei einem fehlerhaften SQL-Stetement eine SQLException geworfen wird.

2.6.2.	SqlExecutorStepTest

Package: 	ch.so.agi.gretl.steps

Die Klasse SqlExecutorStepTest überprüft die Funktionalitäten der SqlExecutorStep-Klasse (s. Kapitel 2.5.3). Hierfür wird in einem ersten Schritt einen temporären Ordner angelegt, welcher nach den Tests wieder gelöscht wird.
executeWithoutFiles: Prüft, ob eine Fehlermeldung geworfen wird, wenn keine Files aber eine Datenbankconnection angegeben werden.
executeWithoutDb: Prüft, ob eine Fehlermeldung geworfen wird, wenn zwar ein sqlFile übergeben wird, aber keine Datenbankconnection.
executeDifferentExtensions: Prüft, ob eine Fehlermeldung geworfen wird, wenn eine Datenbankverbindung und in der Fileliste ein SQL-File und ein txt-File übergeben werden.
executeEmptyFile: Prüft, ob alles korrekt und ohne Fehlermeldung ausgeführt wird, wenn eine Datenbankverbindung, ein sql-File mit einer Query und ein sql-File ohne Query übergeben werden. Dazu wird zu Beginn eine Tabelle in der Datenbank angelegt, auf welcher die Query ausgeführt werden kann.
executeWrongQuery: Prüft, ob eine Fehlermeldung geworfen wird, wenn zwar eine Datenbankverbindung und ein sql-File übergeben wird, aber die Query falsch ist. Damit die Query getestet werden kann, wird zu Beginn eine entsprechende Tabelle angelegt.
execute: Prüft, ob alles korrekt und ohne Fehlermeldung ausgeführt wird, wenn eine Datenbankverbindung und zwei sql-Files übergeben werden. Hierzu wird zu Beginn eine Tabelle in der Datenbank angelegt und mit drei Einträgen abgefüllt.

**2.7.	Build.gradle**

In den build.gradle-Files werden alle Einstellungen für gradle festgelegt. Dabei hat jedes Modul (core, steps) wie auch das Projekt selber ein solches build.gradle-File

2.7.1.	Core

Das build.gradle des Moduls core sieht wie folgt aus::

   group 'gretl'
   version '1.0-SNAPSHOT'
   apply plugin: 'java'
   apply plugin: 'maven'
   sourceCompatibility = 1.8
   repositories {
       mavenCentral()
   }
   dependencies {
       testCompile group: 'junit', name: 'junit', version: '4.12'
       compile files('./lib/ojdbc7.jar', './lib/postgresql-42.0.0.jar', './lib/sqljdbc42.jar', './lib/sqlite-jdbc-3.16.1.jar', './lib/derby.jar')
       compile group: 'org.slf4j', name: 'slf4j-api', version: '1.8.0-alpha2'
       compile group: 'org.slf4j', name: 'slf4j-simple', version: '1.8.0-alpha2'

Group legt fest zu welcher Gruppe/Projekt das Modul core gehört und welche Version dieser Gruppe. Mit apply plugin wird festgelegt, dass es sich um ein java und maven-Projekt handelt. Maven wird daher als plugin definiert, damit das lokale Repository (mavenCentral), welches zum Ausführen der Tasks benötigt wird, verwendet werden kann. In den Dependencies werden die Abhängigkeiten aufgeführt (s. Kapitel 3.1).

2.7.2.	Steps

Das build.gradle des Moduls Steps sieht wie folgt aus::

   group 'gretl'
   version '1.0-SNAPSHOT'
   apply plugin: 'java'
   apply plugin: 'maven'
   sourceCompatibility = 1.8
   repositories {
       mavenCentral()
   }
   dependencies {
       testCompile group: 'junit', name: 'junit', version: '4.12'
       compile project (':core')
       compile gradleApi()
   }

Group legt fest zu welcher Gruppe/Projekt das Modul steps gehört und welche Version dieser Gruppe. Mit apply plugin wird festgelegt, dass es sich um ein java und maven-Projekt handelt. Maven wird daher als plugin definiert, damit das lokale Repository (mavenCentral), welches zum Ausführen der Tasks benötigt wird, verwendet werden kann. In den Dependencies werden die Abhängigkeiten aufgeführt (s. Kapitel 3.1).

2.7.3.	Gretl

Das build.gradle des Projekts gretl sieht wie folgt aus::

   group 'gretl'
   version '1.0-SNAPSHOT'
   apply plugin: 'java'
   apply plugin: 'maven'
   sourceCompatibility = 1.8
   repositories {
       mavenCentral()
   }
   dependencies {
       testCompile group: 'junit', name: 'junit', version: '4.12'
       compile project (':core')
       compile gradleApi()
   }

Group legt fest zu welcher Gruppe/Projekt das Projekt gretl gehört und welche Version dieser Gruppe. Mit apply plugin wird festgelegt, dass es sich um ein java und maven-Projekt handelt. Maven wird daher als plugin definiert, damit das lokale Repository (mavenCentral), welches zum Ausführen der Tasks benötigt wird, verwendet werden kann. In den Dependencies werden die Abhängigkeiten aufgeführt (s. Kapitel 3.1).

"""""""""""""""""

**3.	GRETL - Einstellungen**

**3.1.	Dependencies – Abhängigkeiten**

Abhängigkeiten müssen sowohl im build.gradle wie auch in INTELLIJ IDEA definiert werden.

3.1.1.	Core

3.1.1.1.	Build.gradle

Folgende Abhängigkeiten müssen im build.gradle des cores definiert sein:

•	Junit Version 4.12 (testCompile)
•	Files: './lib/ojdbc7.jar', './lib/postgresql-42.0.0.jar', './lib/sqljdbc42.jar', './lib/sqlite-jdbc-3.16.1.jar', './lib/derby.jar' (compile)
•	Slf4j-api Version 1.8.0-alpha2 (compile)
•	Slf4j-simple Version 1.8.0-alpha2 (compile)
•	gradleApi() (compile)

Für die Tests wird Junit benötigt. Da es aber lediglich dort benötigt und verwendet wird, wird es nicht mit compile in den dependencies aufgeführt sondern mit testCompile.
Sämtliche Files werden für die Erstellung der verschiedenen Datenbankverbindungen benötigt. Da diese sowohl im main wie auch im test benötigt werden, werden sie mit compile in den dependencies aufgeführt.
Für das Logging werden zudem noch slf4j-api und slf4j-simple benötigt. Da auch diese sowohl im main wie auch im test benötigt werden, werden sie mit compile in den dependencies aufgeführt.
gradleApi() wird benötigt um die java-Klassen mit gradle zu komplieren.

3.1.1.2.	INTELLIJ IDEA

Um die Abhängigkeiten in der IDE festzulegen muss im Menü File > Project Structure ausgewählt werden. Anschliessend in Modules und dort in core wechseln. Im core_main und core_test sind anschliessend im Reiter Dependencies folgende Abhängigkeiten festzulegen:

•	Derby.jar (main, test)
•	Sqlite-jdbc-3.16.1.jar (main, test)
•	Sqljdbc42.jar (main, test)
•	Ojdbc7.jar (main, test)
•	Postgresql-42.0.0.jar (main, test)
•	Gradle: org.slf4j:slf4j-api:1.8.0-alpha2 (main, test)
•	Gradle: org.slf4j:slf4j-simple:1.8.0-alpha2 (main, test)
•	Core_main (test)
•	Gradle:junit:junit:4.12 (test)
•	Gradle:org.hamcrest:hamcrest-core:1.3 (test)

3.1.2.	Steps

3.1.2.1.	Build.gradle

Folgende Abhängigkeiten müssen im build.gradle der steps definiert sein:

•	Junit Version 4.12 (testCompile)
•	Core (compile project)
•	gradleApi() (compile)

Für die Tests wird Junit benötigt. Da es aber lediglich dort benötigt und verwendet wird, wird es nicht mit compile in den dependencies aufgeführt sondern mit testCompile.
Das Modul core wird im Module steps benötigt daher wird dies mit compile project in den dependencies aufgeführt.
gradleApi() wird benötigt um die java-Klassen mit gradle zu komplieren.

3.1.2.2.	INTELLIJ IDEA

Um die Abhängigkeiten in der IDE festzulegen muss im Menü File > Project Structure ausgewählt werden. Anschliessend in Modules und dort in steps wechseln. Im steps_main und steps_test sind anschliessend im Reiter Dependencies folgende Abhängigkeiten festzulegen:

•	Gradle-installation-beacon-3.3.jar (main, test)
•	Groovy-all-2.4.7.jar (main, test)
•	Steps_main (test)
•	Gradle-api-3.3.jar (main, test)
•	Sqlite-jdbc-3.16.1.jar (main, test)
•	Postgresql-42.0.0.jar (main, test)
•	Sqljdbc42.jar (main, test)
•	Derby.jar (main, test)
•	Ojdbc7.jar (main, test)
•	Core_main (main, test)
•	Gradle: junit:junit:4.12 (test)
•	Gradle:org.slf4j:slf4j-api:1.8.0-alpha2 (main, test)
•	Gradle:org.slf4j:slf4j-simple:1.8.0-alpha2 (main, test)
•	Gradle:org.hamcrest:hamcrest-core:1.3 (test)

3.1.3.	Gretl

3.1.3.1.	Build.gradle

Folgende Abhängigkeiten müssen im build.gradle des gretls definiert sein:

•	Junit Version 4.12 (testCompile)
•	Core (compile project)
•	Steps (compile project)
•	gradleApi() (compile)

Für die Tests wird Junit benötigt. Da es aber lediglich dort benötigt und verwendet wird, wird es nicht mit compile in den dependencies aufgeführt sondern mit testCompile.
Sowohl das Modul core wie auch das Modul steps werden im Projekt gretl benötigt daher werden die beiden mit compile project in den dependencies aufgeführt.
gradleApi() wird benötigt um die java-Klassen mit gradle zu komplieren.

3.1.3.2.	INTELLIJ IDEA

Um die Abhängigkeiten in der IDE festzulegen muss im Menü File > Project Structure ausgewählt werden. Anschliessend in Modules und dort in gretl wechseln. Im gretl_main und gretl_test sind anschliessend im Reiter Dependencies folgende Abhängigkeiten festzulegen:

•	Groovy-all-2.4.7.jar (main, test)
•	Derby.jar (main, test)
•	Gradle-installation-beacon-3.3.jar (main, test)
•	Gretl_main (test)
•	Ojdbc7.jar (main, test)
•	Gradle-api-3.3.jar (main, test)
•	Postgresqll-42.0.0.jar (main, test)
•	Sqlite-jdbc-3.16.1.jar (main, test)
•	Sqljdbc42.jar (main, test)
•	Gradle:junit:junit:4.12 (test)
•	Gradle:org.slf4j:slf4j-api:1.8.0-alpha2 (main, test)
•	Gradle:org.slf4j:slf4j-simple:1.8.0-alpha2 (main, test)
•	Gradle:org.hamcrest:hamcrest-core:1.3 (test)
•	Core_main (main)
•	Steps (main)

**3.2.	Tests ausführen**

Um zu prüfen, ob die Java-Klassen korrekt funktionieren wurden für (fast) jede Klasse Unittest definiert (s. Kapitel 2.4, 2.6). Diese können einzeln oder alle zusammen ausgeführt werden.

3.2.1.	Einzelne Tests ausführen

Um die in den Kapiteln 2.4 und 2.6 aufgeführten Tests ausführen zu können, wird in INTELLIJ IDEA die entsprechende Klasse, welche getestet werden soll geöffnet. Anschliessend kann mittels Rechtsklick auf den Testnamen (z.b. executeWithoutFiles()) im sich öffnenden Kontextmenü "Run *Testnamen()*" ausgewählt werden. Anschliessend wird der Test ausgeführt. Wenn er mit einem exit code 0 abschliesst ist der Test erfolgreich durchgelaufen.

3.2.2.	Alle Tests ausführen

Um alle Tests zu prüfen muss in der Konsole in den Ordner gewechselt werden, in welchem die Datei gradlew liegt (im trunk-Ordner). Anschliessend wird folgender Befehl ausgeführt:
./gradlew test
Wird mit einem "BUILD FAILED" abgeschlossen, so sind nicht alle Tests erfolgreich durchgeführt worden.

3.2.3.	Wo sind die Tests der Task?

Für die Tasks wurden keine Tests erstellt, da diese keine neuen Features prüfen würden, da die Tasks den Steps entsprechen und diese geprüft werden.

**3.3.	Umbenennen - Refactor**

"""""""""""""""""

**4.	GRETL – Gradleprojekt für Tasks**

Damit keine Änderungen (beabsichtigte/versehentliche) vorgenommen werden können, soll aus dem gretl-Projekt ein jar  erstellt werden. Da dadurch eigene Tasks nicht in diesem Projekt definiert werden können, muss ein separates Projekt erstellt werden.

**4.1.	Aufbau**

Der Aufbau eines solchen separaten Task-Projekt könnte wie folgt aussehen.
Build.gradle::

   import ch.so.agi.gretl.core.TransactionContext
   import ch.so.agi.gretl.steps.Db2DbStepTask
   import ch.so.agi.gretl.steps.TransferSet
   import ch.so.agi.gretl.steps.SqlExecutorStepTask

   group 'gretl'
   version '1.0-SNAPSHOT'

  apply plugin: 'java'
  apply plugin: 'maven'

   sourceCompatibility = 1.8
   repositories {
       mavenLocal()
       mavenCentral()
   }

   buildscript {
       repositories {
           mavenLocal()
           mavenCentral()
       }

       dependencies {
           classpath group: 'gretl', name: 'gretl',  version: '1.0-SNAPSHOT'
           classpath group: 'org.apache.derby', name: 'derby', version: '10.8.3.0'
           classpath group: 'org.postgresql', name: 'postgresql', version: '42.0.0'

       }
   }
   dependencies {
       testCompile group: 'junit', name: 'junit', version: '4.12'
       compile group: 'gretl', name: 'gretl', version: '1.0-SNAPSHOT'
   }


   task TestTask(type: Db2DbStepTask, dependsOn: 'sqlExecutorTask') {
       sourceDb =  new TransactionContext(
                    "jdbc:postgresql://geodb-t.verw.rootso.org:5432/sogis",
                 "bjsvwsch",
                 null);
    targetDb = new TransactionContext(
                 "jdbc:postgresql://10.36.54.200:54321/sogis",
                 "bjsvwsch",
                 null);
    transferSet = [new TransferSet(
                 true,
                 new java.io.File(
                       '/home/bjsvwsch/codebasis_test/sql_test.sql'),
                       'public.geo_gemeinden')];
   }

   task SqlExecutorTask(type: SqlExecutorStepTask){
       sourceDb = new ch.so.agi.gretl.core.TransactionContext(
                    "jdbc:postgresql://10.36.54.198:54321/sogis",
                    "barpastu",
                    null);
       sqlFiles = [new File(
                  "/home/barpastu/IdeaProjects/gretlDemo/query_farben.sql")];
   }

   task endTask(dependsOn: ['TestTask','SqlExecutorTask']) {

   }

Dabei ist wichtig, dass die Zeilen bis vor task TestTask identisch sind. Die tasks können individuell erstellt werden.

**4.2.	Individuelle Tasks**

Wie muss vorgegangen werden?

1. Eigene Tasks definieren
2. Allfällige Abhängigkeiten in diesen Tasks definieren
3. EndTask mit allen benötigten Tasks schreiben

**4.3 Eigene Tasks definieren**

Hierfür müssen in einem gradle-Projekt die eigenen gewünschten Tasks aufgeführt werden. Ein Task, der auf dem Db2Db-Step aufbauen soll, hat immer folgende Struktur::

   Task Name_des_Db2Db_Tasks (type: Db2DbStepTask) {
       sourceDb =  new TransactionContext("jdbc:postgresql://mydb:5432/sogis","user","pw");
       targetDb = new TransactionContext("jdbc:postgresql://mydb2:5432/sogis","user","pw");
       transferSet = [new TransferSet(true,new java.io.File('test/sql_test.sql'),'schema.tabelle')];
   }

Hingegen hat ein Task, welche auf dem SQLExecutor-Step aufbauen soll, immer folgende Struktur::

   Task Name_des_SQLExecutor_Tasks (type: SQLExecutorStepTask) {
       sourceDb =  new TransactionContext("jdbc:postgresql://mydb:5432/sogis","user","pw");
       sqlFiles = [new File("/home/test.sql")];
   }

Jeder Task muss entweder vom Typ SQLExecutorTask oder vom Typ Db2DbStepTask sein. Wobei mehrere Tasks den gleichen Typ aufweisen können. Zwingend jedoch ist, dass jeder Task einen eindeutigen Namen aufweist.

4.3.1.	Datenbankverbindungen - TransactionContext

Wobei sowohl beim Db2Db-Task wie auch beim SQLExecutorTask verschiedene Datenbanktypen verwendet werden können. Hierfür muss bei sourceDb resp. targetDb folgende Connectionstrings dem TransactionContext als erster Parameterwert mitgegeben werden.
::

   Postgres: "jdbc:postgresql://mydb:5432/db"
   Derby: "jdbc:derby:memory:myInMemDB;create=true"
   Oracle: "jdbc:oracle:thin:@//mydb:1521/db"
   SQLite: "jdbc:sqlite:D:\\testdb.db"
   MSSQL: "jdbc:sqlserver://mydb:1433"

Als zweiter Parameter wird der Benutzername und als dritter das Passwort übergeben. Im Fall der Derby-DB sind sowohl der Benutzername wie auch das Passwort Null.

4.3.2.	TransferSet

Im Task, welcher auf dem Db2Db-Step aufbaut, wird nebst den beiden Datenbankverbindungen auch ein transferSet benötigt. Als erster Parameterwert muss entweder True oder False übergeben werden. Dabei wird angegeben, ob im Falle einer bereits existierenden Zieltabelle diese zuerst geleert werden soll (True) oder nicht (False). Als zweiter Parameterwert muss das SQL-File angegeben werden, welches das SQL-Statement für die Quelltabellen beinhaltet. Als letzter Parameterwert muss der qualifizierte Schemen- und Tabellennamen der Zieltabelle angegeben werden.

4.3.3.	SqlFiles

Im auf dem SQLExecutor-Step aufbauenden Task muss nebst einer Datenbankverbindung auch noch mindestens ein SQL-File angegeben werden. Wobei die angegebenen Files zwingend die Endung .sql aufweisen müssen.

4.3.4.	Abhängigkeiten in Tasks definieren

Wenn Tasks davon abhängig sind, dass andere Tasks zuvor ausgeführt werden, so kann dies in den Tasks definiert werden. Es ist möglich einen Task von einem oder mehreren Tasks abhängig zu machen. Folgende Beispiele zeigen, wie ein Task von einem oder mehreren Tasks abhängig gemacht wird::

   task SqlExecutorTask1(type: SqlExecutorStepTask, dependsOn: ['SqlExecutorTask', 'SqlExecutorTask3']){
   …
   }

   task SqlExecutorTask1(type: SqlExecutorStepTask, dependsOn: 'SqlExecutorTask'){
   …
   }

Bevor der Task SqlExecutorTask1 ausgeführt wird muss der Task SqlExecutorTask (und SqlExecutorTask3) ausgeführt werden.

4.3.5.	EndTask

Beim Endtask werden alle Task, welche in einem Schritt ausgeführt werden sollen, als Abhängigkeiten aufgeführt. Die Reihenfolge der Definition entspricht, sofern es keine Abhängigkeiten gibt, der Reihenfolge der Ausführung. Ein Beispiel für einen solchen Endtask::

   task endTask(dependsOn: ['SqlExecutorTask','SqlExecutorTask1']) {

   }

Dieser Task wird verwendet um den Job auszuführen. Beim Job handelt es sich um eine Zusammenstellung von Tasks, welche in einem Schritt ausgeführt werden sollen.

**4.4.	Dependencies INTELLIJ IDEA**

Um die Abhängigkeiten in der IDE festzulegen muss im Menü File > Project Structure ausgewählt werden. Anschliessend in Modules und dort ins Projekt wechseln. Im Projekt _main und Projekt _test sind anschliessend im Reiter Dependencies folgende Abhängigkeiten festzulegen:

- Gradle:org.postgresql:postgresql:42.1.1 (main, test)
- Gretl_main (test)
- Gradle:junit:junit:4.12 (test)
- Gradle:org.hamcrest:hamcres-core:1.3 (test)

"""""""""""""""""

**5.  GRETL benutzen**

Damit die individuellen Tasks ausgeführt werden können muss zuerst ein jar des gretls erzeugt werden.

**5.1.	Erzeugen eines builds**

Um das Projekt GRETL im lokalen Repository (.m2/gretl) zu publizieren und daher ein jar zu erzeugen, muss in der Konsole im Projektordner (trunk) folgender Befehl ausgeführt werden::

   gradle install

**5.2.	Tasks/Job ausführen**

Hierzu muss in der Konsole in den Ordner des Gradle-Task-Projekts gewechselt werden. Anschliessend wird folgender Befehl in der Konsole eingegeben und ausgeführt::

   ./gradlew endTask --no-daemon

Wobei endTask der Name des auszuführenden Tasks ist.
**
6.	GRETL intern zur Verfügung stellen**

Alles noch unklar!!!!
