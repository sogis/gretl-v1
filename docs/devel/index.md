# GRETL-Entwickler-Handbuch

## 3rd party plugins

Um ein offline-fähiges Docker-Image zu erhalten, müssen sämtliche Abhängigkeiten in das Image kopiert werden. Dazu gehören auch allenfalls benötigte Fremdplugins (z.B. `gradle-download-task`). Dazu muss in der Datei `runtimeImage/gretl/build.gradle` das benötigte Repository angegeben werden (in der Regel: `https://plugins.gradle.org/m2/`) und als Compile-Dependency das Plugin selber (z.B. `compile "de.undercouch:gradle-download-task:3.4.1"`).

Beim Herstellen des Images wird am Ende der Inhalt des Verzeichnisses `/home/gradle/libs` ausgegeben und man kann verifizieren, ob die zusätzlichen Bibliotheken im Image vorhanden sind.
