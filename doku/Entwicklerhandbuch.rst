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

Der SqlReader liest Statement für Statement die SQL-Statements aus einem File aus. 

2.3.3.1.	Methode readSqlStmt

Benötigt: File

Liefert:	SQL-Statement (String)

Erstellt zuerst durch Aufruf der Methode createPushbackReader einen Reader und gibt am Ende das erste Statement aus dem File zurück.

Beispiel::

   Connection db = DriverManager.getConnection(ConnectionUrl, UserName, Password)
   List<File> sqlfiles = [new File("/Path/to/File/Filename.sql")]
   readSqlStmt(sqlfiles, db)

2.3.3.2. Methode createPushbackReader

Benötigt: File

Liefert: PushbackReader

Die Methode erstellt mit dem übergebenen File einen PushbackReader. Dieser ermöglicht das File char für char zu lesen und er ermöglicht auch, dass vorausgeschaut wird, welches char als nächstes geliefert wird.

Beispiel::

   sqlfile = new File("/Path/to/File/Filename.sql")
   createPushbackReader(sqlfile)

2.3.3.3. Methode createStatement

Benötigt: Char, PushbackReader, StringBuffer

Liefert: StringBuffer

Mit der Methode createStatement werden die Chars, welche aus dem File ausgelesen werden zu einem Statement zusammengefügt und als StringBuffer zurück gegeben. Dafür wird jedes Char geprüft, ob es nicht das Ende des Files ist oder ein Semikolon ";" und anschliessen mit der Methode handlingGivenCharacters weiterverarbeitet. Das Resultat wird als StringBuffer gespeichert und es wird das nächste char gelesen. Ist entweder das Ende des Files erreicht oder ist das Char ein Semikolon, so wird das nächste Char gelesen und anschliessend das Statement als StringBuffer zurückgegeben.

Beispiel::

   sqlfile = new File("/Path/to/File/Filename.sql")
   sqlFileInputStream = new FileInputStream(sqlfile);
   sqlFileReader = new InputStreamReader(sqlFileInputStream);
   
   reader = new PushbackReader(sqlFileReader);
   int c = reader.read();
   StringBuffer stmt = new StringBuffer();
   
   stmt = createStatement(c, reader, stmt)
   

2.3.3.4. Methode handlingGivenCharacters

Benötigt: Char, PushbackReader, StringBuffer

Liefert: StringBuffer

Diese Methode entscheidet aufgrund des Chars, mit welcher Methode das Char weiterbehandelt werden soll. 

========  ==========================
char      behandelnde Methode
========  ==========================
'-'        checkCharacterAfterHyphen
'\\''      addingQuotedString
';'        splitStatement
'\\n'      replaceLineBreakCharacter
'\\r'      replaceLineBreakCharacter
========  ==========================

Jedes andere Char wird dem übergebenen StringBuffer angefügt. Am Schluss wird der StringBuffer zurückgegeben.

Beispiel::

   sqlfile = new File("/Path/to/File/Filename.sql")
   sqlFileInputStream = new FileInputStream(sqlfile);
   sqlFileReader = new InputStreamReader(sqlFileInputStream);
   
   reader = new PushbackReader(sqlFileReader);
   int c = reader.read();
   StringBuffer stmt = new StringBuffer();
   
   stmt = handlingGivenCharacters(c,reader,stmt);

2.3.3.5. checkCharacterAfterHyphen

Benötigt: PushbackReader, StringBuffer

Liefert: StringBuffer

In der checkCharacterAfterHyphen-Methode wird als erstes das nächste Char gelesen. Im Falle, dass das Ende des Files erreicht ist, wird automatisch ein weitere Bindestrich "-" dem StringBuffer angefügt. Solle es sich um einen weiteren Bindestrich handeln so wird die Methode ignoreCommentsUntilLinebreak ausgeführt. Bei jedem anderen Char wird dem StringBuffer ein weiterer Bindestrich angefügt und anschliessend das gelesene Char angefügt. Am Schluss wird der StringBuffer zurückgegeben

Beispiel::

   sqlfile = new File("/Path/to/File/Filename.sql")
   sqlFileInputStream = new FileInputStream(sqlfile);
   sqlFileReader = new InputStreamReader(sqlFileInputStream);
   
   reader = new PushbackReader(sqlFileReader);
   int c = reader.read();
   StringBuffer stmt = new StringBuffer();  
   
   stmt = checkCharacterAfterHyphen(reader,stmt);

2.3.3.6. ignoreCommentsUntilLinebreak

Benötigt: PushbackReader

Liefert: nichts

Die Methode ignoreCommentsUntilLinebreak liest das nächste Char vom PushbackReader. Solange das Ende des Files nicht erreicht ist wird geprüft, ob das Char einen Zeilenumbruch ("\\n" oder "\\r") repräsentiert. Wenn dies der Fall ist, so wird das nächste Char gelesen. Wenn es sich dabei weder um einen weiteren Zeilenumbruch noch um das Ende des Files handelt, wird das Lesen des Chars rückgängig gemacht und es wird aus der Methode ausgetreten. Ansonsten wird das Char nicht ungelesen gemacht, sondern direkt aus der Methode ausgetreten. 
Sollte es sich aber nicht um einen Zeilenumbruch gehandlet haben, so wird das nächste Char gelesen.

Beispiel::

   sqlfile = new File("/Path/to/File/Filename.sql")
   sqlFileInputStream = new FileInputStream(sqlfile);
   sqlFileReader = new InputStreamReader(sqlFileInputStream);
   
   reader = new PushbackReader(sqlFileReader);
   
   ignoreCommentsUntilLinebreak(reader);

2.3.3.7. addingQuotedString

Benötigt: Char, PushbackReader, StringBuffer

Liefert: StringBuffer

Die Methode addingQuotedString fügt das übergebene Char dem StringBuffer hinzu. Anschliessend wird solange das nächste Char gelesen, bis entweder das Ende des Files erreicht ist, oder es sich beim Char um ein Apostroph "'" handelt. Am Schluss wird der StringBuffer zurückgegeben.

Beispiel::

   sqlfile = new File("/Path/to/File/Filename.sql")
   sqlFileInputStream = new FileInputStream(sqlfile);
   sqlFileReader = new InputStreamReader(sqlFileInputStream);
   
   reader = new PushbackReader(sqlFileReader);
   int c = reader.read();
   StringBuffer stmt = new StringBuffer();  
   
   stmt = addingQuotedString(c, reader, stmt);

2.3.3.8. splitStatement

Benötigt: Char, PushbackReader, StringBuffer

Liefert: StringBuffer

Als erstes wird in der Methode splitStatement das übergebene Char an den übergebenen StringBuffer angefügt. Anschliessend wird das nächste Char gelesen. Handelt es sich um einen Zeilenumbruch ("\\n" oder "\\r"), so wird das nächste Char gelesen. Repräsentiert diese Char weder einen weiteren Zeilenumbruch noch das Ende des Files so wird das Lesen des Chars wieder rückgängig gemacht.
Handelte es sich beidem gelesenen Char um keinen Zeilenumbruch, so wird geprüft, ob es sich um das Fileende handelt. Sollte dies nicht der Fall sein, so wird das Lesen des Chars wieder rückgängig gemacht.

Beispiel::

   sqlfile = new File("/Path/to/File/Filename.sql")
   sqlFileInputStream = new FileInputStream(sqlfile);
   sqlFileReader = new InputStreamReader(sqlFileInputStream);
   
   reader = new PushbackReader(sqlFileReader);
   int c = reader.read();
   StringBuffer stmt = new StringBuffer();  
   
   stmt = splitStatement(c, reader, stmt);

2.3.3.9. replaceLineBreakCharacter

Benötigt: Char, PushbackReader, StringBuffer

Liefert: StringBuffer

Die Methode replaceLineBreakCharacter prüft, ob es sich bei dem übergebenen Char um einen Zeilenumbruch ("\\n" oder "\\r") handelt und fügt stattdessen dem StringBuffer einen Leerschlag hinzu. Anschliessen wird das nächste Char gelesen und geprüft, ob es sich weder um das Fileende noch um einen weiteren Zeilenumbruch handelt. Ist dies der Fall, so wird das Lesen des Chars rückgängig gemacht. Am Schluss wird der StringBuffer zurückgegeben.

Beispiel::

   sqlfile = new File("/Path/to/File/Filename.sql")
   sqlFileInputStream = new FileInputStream(sqlfile);
   sqlFileReader = new InputStreamReader(sqlFileInputStream);
   
   reader = new PushbackReader(sqlFileReader);
   int c = reader.read();
   StringBuffer stmt = new StringBuffer();  
   
   stmt = replaceLineBreakCharacter(c, reader, stmt);

2.3.3.10. Methode nextSqlStmt 

Benötigt: nichts

Liefert: SQL-Statement (String)

Die Methode nextSqlStmt ermittelt das nächste SQL-Statement und liefert dieses zurück.

Beispiel::

   String statement = SqlReader.nextSqlStmt(sqlfile);

2.3.3.11. closePushbackReader

Benötigt: nichts

Liefert: nichts

Das Schliessen des FileInputStreams und des InputStreamReaders, welche benötigt wurden zum Erstellen des PushbackReaders, wird mit der Methode closePushbackReader vorgenommen.

Beispiel::

   closePushbackReader();
   
2.3.4.	EmptyFileException

Package: ch.so.agi.gretl.util

Die EmptyFileException soll geworfen werden, wenn ein File, welches nicht leer sein darf, trotzdem leer ist. Wenn beispielweise das SQL-File, welches beim Db2Db-Step gelesen werden soll, leer ist, soll keine allgemeine, sondern diese spezifische Exception geworfen werden.

Beispiel::

   throw new EmptyFileException("EmptyFile: "+targetFile.getName());
   
2.3.5. EmptyListException

Package: ch.so.agi.gretl.util

----- hier fehlt Text -----

2.3.6.	NotAllowedSqlExpressionException

Package: ch.so.agi.gretl.util

Die NotAllowedSqlExpressionException soll geworfen werden, wenn in einem SQL-Statement einen Ausdruck enthalten ist, der in diesem Zusammenhang nicht erlaubt ist. (Beispiel: Im SQL-File, welches im Db2Db-Step verwendet wird, ist kein Delete, Update, Insert etc. erlaubt).

Beispiel::

   throw new NotAllowedSqlExpressionException();

**2.4.	Util – Test**

2.4.1.	DbConnectorTest

Package: 	ch.so.agi.gretl.core

Die Klasse DbConnectorTest testet gewisse Funktionalitäten der DbConnector-Klasse.
connectToDerbyDb: Testet, ob eine Verbindung zur lokalen Derby-Db herstellen kann.
connectionAutoCommit: Testet, ob AutoCommit wirklich off ist.

2.4.2.	FileExtensionTest

Package: 	ch.so.agi.gretl.core

Die Klasse FileExtensionTest überprüft die Funktionalitäten der FileExtension-Klasse. Hierfür wird in einem ersten Schritt einen temporären Ordner angelegt, welcher nach den Tests wieder gelöscht wird.
getFileExtension: Prüft, ob die Methode bei einem File mit der Endung .sql auch die Endung sql ermittelt wird.
missingFileExtension: Prüft, ob bei einem File ohne Endung auch wirklich eine Fehlermeldung ausgegeben wird.
mutipleFileExtension: Prüft, ob bei einem File mit mehreren Endungen (file.ext1.ext2) auch wirklich die letzte Fileendung ausgegeben wird.
strangeFileNameExtension: Prüft, ob bei einem File mit folgendem Namen (c:\\file) auch wirklich eine Fehlermeldung ausgeworfen wird.

**2.5.	Logging**

(Hier muss noch seeeeeeeeeehr viel gemacht werden!!!!!!!!!!!!!!!!!)

2.5.1.Logger

Package: 	ch.so.agi.gretl.logging

Beinhaltet die Methode log um Informationen zu loggen.

2.5.1.1. Methode log

Benötigt: 	LogLevel (int), Message (String)
Die Methode log schreibt die übergebene Nachricht (Message) mit dem LogLevelhinweis (INFO, DEBUG, ERROR, LIVECYCLE), je nach Einstellung nach System.err. Die Standardeinstellung sieht vor, dass logmessages mit dem Info-Level nach System.err geschrieben werden, während die logmessages mit dem Debug-Level gar nicht erst geloggt werden.
Folgende LogLevel gibt es: INFO_LEVEL, DEBUG_LEVEL

Beispiel::

   Logger.log(Logger.INFO_LEVEL,"Task start");

**2.6.	Logging - Test**

2.6.1.	LoggerTest

Package: 	ch.so.agi.gretl.logging

Mit der LoggerTest-Klasse wird die Funktionalität der Logger-Klasse überprüft. Dabei wird bevor irgendein Test ausgeführt wird eine PrintStream erzeugt und System.err wird so umgestellt, dass dieser den neu erzeugten PrintStream als Output nutzt. 
Vor jedem Test wird zudem der PrintStream zurückgesetzt. Und am Ende aller Test wird System.err wieder zurückgesetzt.
logInfoTest: Prüft, ob die geworfene Logmeldung der Erwartung entspricht.
logDebugTest: Prüft, ob die in System.err geworfene Logmeldung der Erwartung entspricht.
logErrorTest: Prüft, ob die geworfene Logmeldung der Erwartung entspricht.

**2.7.	Steps**
   
2.7.1. Db2DbStep  --> Bitte überarbeiten/prüfen

Package: ch.so.agi.gretl.steps

Die Db2DbStep-Klasse beinhaltet den Db2Db-Step. Sie dient dem Umformen und Kopieren von einer Datenbank in eine andere. In einem SQL-File wird dabei das SQL-Statement für den Input-Datensatz erstellt, der dann in die Output-Datenbank geschrieben werden soll.

2.7.1.1. Methode processAllTransferSets

Diese Methode ruft für jedes in der Liste aufgeführte Transferset die Methode processTransferSet auf.

Beispiel::

   processAllTransferSets(TransactionContext sourceDb, TransactionContext targetDb, List<TransferSet> transferSets)

2.7.1.2	Methode processTransferSet

Dies ist nun die Methode, welche ein TransferSet abarbeitet. Dabei werden verschiedene andere Methoden aufgerufen.
Als erstes wird überprüft, ob im TransferSet die Option getDeleteAllRows auf True gesetzt ist. Ist das der Fall, wird die Methode deleteDestTableContents aufgerufen, welche den Inhalt der ZielTtabelle löscht.
Danach wird mit der Methode extractSingleStatement ein Statement aus dem SQL-File, welches im TransferSet definiert ist, extrahiert und gleich auf unerlaubte Ausdrücke (Delete, Insert, Update etc.) überprüft. Danach wird mit der Methode createResultSet das Statement ausgeführt und anschliessend wird mit der Methode createInsertRowStatement ein SQL-INSERT-Statement vorbereitet. Dieses wird in der Methode transferRow mit den Werten aus dem ResultSet abgefüllt.

Beispiel::

   processTransferSet(sourceDbConnection, targetDbConnection, transferSet);


2.7.1.3. Methode deleteDestTableContents

Diese Methode löscht alle Einträge in der Ziel-Tabelle. Dies geschieht nicht mit "truncate", sondern mit "DELETE FROM". Der Grund dafür ist, dass ein Truncate alleine in einer Transaktion stehen müsste und nicht zusammen mit anderen Querys übermittelt (commited) werden kann.

Beispiel::

   deleteDestTableContents(targetCon, transferSet.getOutputQualifiedSchemaAndTableName());

2.7.1.4. Methode createResultSet

Diese Methode führt das sqlSelectStatement aus und liefert ein ResultSet (rs) zurück)

Beispiel::

   ResultSet rs = createResultSet(srcCon, selectStatement);

2.7.1.5. Methode createInsertRowStatement

Diese Methode erstellt das Insert Statement. Dazu werden über die Funktion getMetaData die Metadaten, konkret die columnNames (Spaltennamen) ausgelesen. Die Spaltennamen werden dann zusammengesetzt und im Insert-Statement eingesetzt. Gleichzeitig werden der Anzahl Spalten entsprechend Fragezeichen in die VALUES geschrieben, welche in einer späteren Methode durch die entsprechenden Werten ersetzt werden.

Beispiel::

   createInsertRowStatement(srcCon,rs,transferSet.getOutputQualifiedSchemaAndTableName());

2.7.1.6. Methode extractSingleStatement

Benötigt: File targetFile

Diese Methode extrahiert aus einem definierten File ein SQL Statement. Dabei wird auch auch überprüft ob das File nur ein Statement enthält, oder ob es eventuell auch weitere gibt. Des Weiteren wird auch überprüft, ob eventuelle nicht erlaubte Ausdrücke im Statement vorkommen (z.B. DELETE, INSERT oder UPDATE).

Beispiel::

   extractSingleStatement(transferSet.getInputSqlFile());

2.7.1.7. Methode transferRow

Benötigt: ResultSet rs, PreparedStatement insertRowStatement, int columncount

Diese Methode ersetzt die "?" vominsertRowStatement mit den Werten, die das ResultSet zurückliefert. Im Anschluss wird dieses Statement ausgeführt.

Beispiel::

   while (rs.next()) {transferRow(rs, insertRowStatement, columncount);}


2.7.2. Db2DbStepTask  --> bitte überarbeiten/prüfen

Package: 	ch.so.agi.gretl.steps

Die Klasse Db2DbStepTask repräsentiert den Task zum Db2DbStep. Diese Klasse verlangt nach drei Inputs; der sourceDb, der targetDb und eines oder mehrerer TransferSets. Ein Beispiel wie ein solcher Task aussehen könnte:
::

   task TestTask(type: Db2DbStepTask, dependsOn: 'TestTask2') {
       sourceDb =  new TransactionContext("jdbc:postgresql://host:port/db","user",null);
       targetDb = new TransactionContext("jdbc:postgresql://host:port/db","user",null);
       transferSet = [new TransferSet(true,new java.io.File('path/to/file'),'schema.table')];
   }


2.7.3. SqlExecutorStep

Package: ch.so.agi.gretl.steps

Die SqlExecutorStep-Klasse beinhaltet den Step SQLExecutor und führt dementsprechend die übergebenen sql-Statements auf der übergebenen Datenbank aus.

2.7.3.1. Methode execute

Benötigt: trans (TransactionContext), sqlfiles (List<File>)

Liefert: nichts

Die Methode execute führt zuerst die Methode checkIfAtLeastOneSqlFileIsGiven aus und anschliessend führt sie logPathToInputSqlFiles aus. Danach wird versucht mit dem TransactionContext eine Verbindung zur Datenbank zu erstellen. Danach werden die Methoden checkFileExtensionsForSqlExtension und readSqlFiles ausgeführt. Zum Abschluss wird ein Commit auf der Datenbank ausgeführt. Falls eine Exception geworfen wurde, so wird ein Rollback auf der Datenbank ausgeführt. Am Schluss wird sowohl bei einem Commit wie auch bei einem Rollbakc die Verbindung zur Datenbank geschlossen.

Beispiel::

   SqlExecutorStep x = new SqlExecutorStep();
   TransactionContext sourceDb = new TransactionContext("jdbc:derby:memory:myInMemDB;create=true", "barpastu", null);
   sqlfiles = [new File("/Path/to/File/Filename.sql")]:

   x.execute(sourceDb, sqlListe);
   
2.7.3.2. Methode checkIfAtLeastOneSqlFileIsGiven

Benötigt: sqlFiles (List<File>)

Liefert: nichts

Die Methode prüfte, ob mindestens ein File übergeben wurde.

Beispiel::

   sqlfiles = [new File("/Path/to/File/Filename.sql")]:
   checkFileExtensionsForSqlExtension(sqlfiles);
   
2.7.3.3. Methode readSqlFiles

Benötigt: sqlfiles (List<File>), db (Connection)

Liefert: nichts

Die Methode readSqlFiles führt für jedes übergebene File die Methode executeAllSqlStatements aus.

Beispiel::

   sqlfiles = [new File("/Path/to/File/Filename.sql")]:
   Connection db = Drivermanager.getConnection(ConnectionUrl, Username, Password)
   readSqlFiles(sqlfiles, db);
   
2.7.3.4. Methode executeAllSqlStatements

Benötigt: conn (Connection), sqlfile (File)

Liefert: nichts

Die Methode executeAllSqlStatements führt in einem ersten Schritt die Methode SqlReader.readSqlStmt aus. Anschliessend werden solange die Methoden prepareSqlStatement und SqlReader.nextSqlStmt ausgeführt bis das aus nextSqlStmt resultierende Statement null ist.

Beispiel::

   sqlfile = new File("/Path/to/File/Filename.sql"):
   Connection db = Drivermanager.getConnection(ConnectionUrl, Username, Password)
   executeAllSqlStatements(sqlfile, db);

2.7.3.5. Methode prepareSqlStatement

Benötigt: conn (Connection), statement (String)

Liefert: nichts

In einem ersten Schritt werden die unnötigen Blankspaces am Anfang und am Ende des Statementstrings entfernt. Anschliessend wird, sofern die Länge des Strings grösser 0 ist, ein Statement für den Statementstring kreiert und die Methode executeSqlStatement.

Beispiel::

   Connection con = Drivermanager.getConnection(ConnectionUrl, Username, Password);
   Statement dbstmt = null;
   dbstmt = conn.createStatement();
   String statement = "SQL-Query-Statement"
   
   executeSqlStatement(dbstmt, statement);
   
2.7.3.6. Methode executeSqlStatement

Benötigt: dbstmt (Statement), statement (String)

Liefert: nichts

Die Methode executeSqlStatement versucht das als String übergebene Statement als Statement auf der Datenbank auszuführen. Nachdem dies erfolgreich oder fehlerhaft durchgeführt wurde wird das Datenbank-Statement wieder geschlossen.
  
Beispiel::

   Connection con = Drivermanager.getConnection(ConnectionUrl, Username, Password);
   Statement dbstmt = null;
   dbstmt = conn.createStatement();
   String statement = "SQL-Query-Statement"
   
   executeSqlStatement(dbstmt, statement)
   
2.7.4. SqlExecutorTask

Package: ch.so.agi.gretl.steps

Die Klasse SqlExecutorStepTask repräsentiert den Task zum SqlExecutorStep. Sie verlangen einen TransactionContext (sourceDb) und und eine Liste mit Pfaden zu den(SQL-)Files (sqlFiles). In der TaskAction werden die beiden Inputs (sourceDb, sqlFiles) an die Methode execute des SqlExecutorStep übergeben und die Methode ausgeführt.

2.7.4.1. Methode executeSqlExecutor

Benötigt: nichts 

Liefert: nichts

In einem ersten Schritt wird in der Methode executeSqlExecutor geprüft, ob die Inputvariable sqlFiles null ist. Anschliessend wird die Methode convertToValidatedFileList ausgeführt und es wird versucht die Methode SQLExecutorStep().execute auszuführen.

Beispiel::

   executeSQLExecutor()
   
2.7.4.2. Methode convertToValidatedFileList

Benötigt: filePaths (List<String>)

Liefert: List<File>

Die Methode erzeugt in einem ersten Schrit ein Arraylist für Files. Danach werden die übergebenen filePaths einzeln durchgegangen und für jeden Dateipfad wird geprüft, ob er weder null noch eine Länge von 0 hat. Trifft dies nicht zu so wird aus dem Dateipfad ein File erzeugt und geprüft, ob dieses lesbar ist. Zum Abschluss wird das File der Arraylist hinzugefügt.

Beispiel::

   filePaths = ["/path/to/file/filename.sql"]
   List<File> files = convertToValidatedFileList(filePaths)

2.7.5. TransactionContext

Package: ch.so.agi.gretl.steps

Erstellt eine Verbindung zur Datenbank.

2.7.5.1.	Methode getDbConnection

Benötigt: 	dbUri (String), dbUser (String), dbPassword (String)

Liefert: 	Connection

Die Methode führt die Methode DbConnector.connect mit den oben erwähnten Parametern aus. Von dieser Methode wird eine Connection zurückgeliefert, welche auch die getDbConnection zurückliefert. Die Connection wird mit dem AutoCommit False geöffnet.

Beispiel::

   public TransactionContext sourceDb;
   Connection con = sourceDb.getDbConnection();

2.7.6. TransferSet

Package: ch.so.agi.gretl.steps

Die Klasse TransferSet definiert die Gestalt eines TransferSets. Es besteht aus drei Parametern:
- Ein Boolean-Wert, der definiert, ob der Inhalt der Zieltabelle vorgängig gelöscht werden soll.
- Ein Input-File, in welchem ein SELECT_Statement die Struktur der Input-Daten definiert.
- Ein String, bestehend aus Schema und Tabelle des gewünschten Outputs.

2.7.6.1. Methode getDeleteAllRows

Benötigt: nichts

Liefert: Boolean

Die Methode getDeleteAllRows gibt die Instanzvariable deleteAllRows, welche an die Klasse TransferSet übergeben wurde, zurück.

Beispiel::
  
   getDeleteAllRows();
   
2.7.6.2. Methode getInputSqlFile

Benötigt: nichts

Liefert: file

Die Methode getInputSqlFile gibt die Instanzvariable insputSqlfile, welche an die Klasse TransferSet übergeben wurde, zurück.

Beispiel::

   getInputSqlFile();
   
2.7.6.3. Methode getOutputQualifiedSchemaAndTableName

Benötigt: nichts

Liefert: String

Die Methode getOutputQualifiedSchemaAndTableName gibt die Instanzvariable outputQualifiedSchemaAndTableName, welche an die Klasse TransferSet übergeben wurde, zurück.

Beispiel::

   getOutputQualifiedSchemaAndTableName();

**2.8.	Steps – Test**

2.8.1. Db2DbStepTest   ----> überarbeiten/prüfen

Package: ch.so.agi.gretl.steps

Die Klasse Db2DbStepTest überprüft die Funktionalitäten der Db2DbStep-Klasse. Bisher liegen die folgenden Tests vor:
PositiveTest(): Dieser Test ist ein positiv-Test, das heisst, er überprüft, ob der Db2DbStep grundsätzlich funktioniert.
NotAllowedSqlExpressionInScriptTest(): Dieser Test überprüft, ob bei der Verwendung eines nicht erlaubten Ausdruck in einem SQL-File eine Exception geworfen wird.
Db2DbEmptyFileTest(): Überprüft, ob bei einem leeren File eine EmptyFileException geworfen wird.
SQLExceptionTest(): Überprüft, ob bei einem fehlerhaften SQL-Stetement eine SQLException geworfen wird.

2.8.2. SqlExecutorStepTest

Package: ch.so.agi.gretl.steps

Die Klasse SqlExecutorStepTest überprüft die Funktionalitäten der SqlExecutorStep-Klasse. Hierfür wird in einem ersten Schritt einen temporären Ordner angelegt, welcher nach den Tests wieder gelöscht wird. Anschliessend wird eine Testdatenbank mit Testdaten angelegt. Diese wird nach dem Abschluss der Tests wieder verworfen.

executeWithoutFiles: Prüft, ob eine Fehlermeldung geworfen wird, wenn keine Files aber eine Datenbankconnection angegeben werden.

executeWithoutDb: Prüft, ob eine Fehlermeldung geworfen wird, wenn zwar ein sqlFile übergeben wird, aber keine Datenbankconnection.

executeDifferentExtensions: Prüft, ob eine Fehlermeldung geworfen wird, wenn eine Datenbankverbindung und in der Fileliste ein SQL-File und ein txt-File übergeben werden.

executeEmptyFile: Prüft, ob alles korrekt und ohne Fehlermeldung ausgeführt wird, wenn eine Datenbankverbindung, ein sql-File mit einer Query und ein sql-File ohne Query übergeben werden. Dazu wird zu Beginn eine Tabelle in der Datenbank angelegt, auf welcher die Query ausgeführt werden kann.

executeWrongQuery: Prüft, ob eine Fehlermeldung geworfen wird, wenn zwar eine Datenbankverbindung und ein sql-File übergeben wird, aber die Query falsch ist. Damit die Query getestet werden kann, wird zu Beginn eine entsprechende Tabelle angelegt.

executePositiveTest: Prüft, ob alles korrekt und ohne Fehlermeldung ausgeführt wird, wenn eine Datenbankverbindung und zwei sql-Files übergeben werden. Hierzu wird zu Beginn eine Tabelle in der Datenbank angelegt und mit drei Einträgen abgefüllt.

checkIfConnectionIsClosed: Prüft, ob nach dem Ausführen des Steps die Datenbankverbindung korrekt geschlossen wurde.

notClosedConnectionThrowsError: Prüft, ob eine Datenbankverbindung, welche nach dem Ausführen des Steps nicht erfolgreich geschlossen wurde, eine Fehler verursacht.

**2.9.	Build.gradle**

In den build.gradle-Files werden alle Einstellungen für gradle festgelegt.

Das build.gradle des Moduls gretl sieht wie folgt aus::

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
      compile gradleApi()
   }


Group legt fest zu welcher Gruppe/Projekt das Modul gretl gehört und welche Version dieser Gruppe. Mit apply plugin wird festgelegt, dass es sich um ein java und maven-Projekt handelt. Maven wird daher als plugin definiert, damit das lokale Repository (mavenCentral), welches zum Ausführen der Tasks benötigt wird, verwendet werden kann. In den Dependencies werden die Abhängigkeiten aufgeführt.


"""""""""""""""""

**3.	GRETL - Einstellungen**

**3.1.	Dependencies – Abhängigkeiten**

Abhängigkeiten müssen sowohl im build.gradle wie auch in INTELLIJ IDEA definiert werden.

3.1.1.	Build.gradle

Folgende Abhängigkeiten müssen im build.gradle des cores definiert sein:

•	Junit Version 4.12 (testCompile)
•	Files: './lib/ojdbc7.jar', './lib/postgresql-42.0.0.jar', './lib/sqljdbc42.jar', './lib/sqlite-jdbc-3.16.1.jar', './lib/derby.jar' (compile)
•	gradleApi() (compile)

Für die Tests wird Junit benötigt. Da es aber lediglich dort benötigt und verwendet wird, wird es nicht mit compile in den dependencies aufgeführt sondern mit testCompile.
Sämtliche Files werden für die Erstellung der verschiedenen Datenbankverbindungen benötigt. Da diese sowohl im main wie auch im test benötigt werden, werden sie mit compile in den dependencies aufgeführt.
gradleApi() wird benötigt um die java-Klassen mit gradle zu komplieren.

3.1.2.	INTELLIJ IDEA

Um die Abhängigkeiten in der IDE festzulegen muss im Menü File > Project Structure ausgewählt werden. Anschliessend in Modules und dort in core wechseln. Im core_main und core_test sind anschliessend im Reiter Dependencies folgende Abhängigkeiten festzulegen:

-	derby.jar (main, test)
-	sqlite-jdbc-3.16.1.jar (main, test)
-	sqljdbc42.jar (main, test)
-	ojdbc7.jar (main, test)
-	postgresql-42.0.0.jar (main, test)
-	gradle-installation-baecon-3.3.jar (main, test)
-	gradle-api-3.3.jar (main, test)
-  groovy-all-2.4.7.jar (main, test)
-	gretl_main (test)
-	Gradle:junit:junit:4.12 (test)
-	Gradle:org.hamcrest:hamcrest-core:1.3 (test)

**3.2.	Tests ausführen**

Um zu prüfen, ob die Java-Klassen korrekt funktionieren wurden für (fast) jede Klasse Unittest definiert. Diese können einzeln oder alle zusammen ausgeführt werden.

3.2.1.	Einzelne Tests ausführen

Um die Tests ausführen zu können, wird in INTELLIJ IDEA die entsprechende Klasse, welche getestet werden soll geöffnet. Anschliessend kann mittels Rechtsklick auf den Testnamen (z.b. executeWithoutFiles()) im sich öffnenden Kontextmenü "Run *Testnamen()*" ausgewählt werden. Anschliessend wird der Test ausgeführt. Wenn er mit einem exit code 0 abschliesst ist der Test erfolgreich durchgelaufen.

3.2.2.	Alle Tests ausführen

Um alle Tests zu prüfen muss in der Konsole in den Ordner gewechselt werden, in welchem die Datei gradlew liegt (im trunk-Ordner). Anschliessend wird folgender Befehl ausgeführt:
./gradlew test
Wird mit einem "BUILD FAILED" abgeschlossen, so sind nicht alle Tests erfolgreich durchgeführt worden.

3.2.3.	Wo sind die Tests der Task?

Für die Tasks wurden keine Tests erstellt, da diese keine neuen Features prüfen würden, da die Tasks den Steps entsprechen und diese geprüft werden.

**3.3.	Umbenennen - Refactor**

Um den Namen einer Variable, Methode o.ä. zu ändern. muss der Name markiert und mit Rechtsklick darauf geklickt werden. Anschliessend muss Refactor > Rename ausgewählt werden und der neue Name eingegeben werden. Mit Enter werden die Änderungen überall, wo die Variable resp Methode verwendet wird, vorgenommen.

"""""""""""""""""

**4.	GRETL – Gradleprojekt für Tasks**

Damit keine Änderungen (beabsichtigte/versehentliche) vorgenommen werden können, soll aus dem gretl-Projekt ein jar erstellt werden. Da dadurch eigene Tasks nicht in diesem Projekt definiert werden können, muss ein separates Projekt erstellt werden.

**4.1.	Aufbau**  ----> überarbeiten, wenn klar wie aufgebaut.

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
       database =  new TransactionContext("jdbc:postgresql://mydb:5432/sogis","user","pw");
       sqlFiles = ["/home/test.sql"];
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

Im auf dem SQLExecutor-Step aufbauenden Task muss nebst einer Datenbankverbindung auch noch mindestens ein Pfad zu einem SQL-File angegeben werden. Wobei die angegebenen Files zwingend die Endung .sql aufweisen müssen.

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

**5.1.	Erzeugen eines builds**  ---> überarbeiten, wenn klar wohin publiziert wird

Um das Projekt GRETL im lokalen Repository (.m2/gretl) zu publizieren und daher ein jar zu erzeugen, muss in der Konsole im Projektordner (trunk) folgender Befehl ausgeführt werden::

   gradle install

**5.2.	Tasks/Job ausführen**

Hierzu muss in der Konsole in den Ordner des Gradle-Task-Projekts gewechselt werden. Anschliessend wird folgender Befehl in der Konsole eingegeben und ausgeführt::

   ./gradlew endTask --no-daemon

Wobei endTask der Name des auszuführenden Tasks ist.

**6.	GRETL intern zur Verfügung stellen**  ---> überarbeiten, wenn klar wo zur Verfügung gestellt

Alles noch unklar!!!!
