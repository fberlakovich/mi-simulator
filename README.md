# MI-Simulator #

Dieses repository enthält eine Weiterentwicklung des MI-Simulators wie er in der Vorlesung "Maschinennahes
Programmieren" an der UniBW verwendet wird.
Informationen zu der letzten Version vor der Weiterentwicklung (v1.09, von 2013) können
unter https://www.unibw.de/inf2/lehre/wt19/maschprog/sim-test gefunden werden.

Das Handbuch ist unter https://www.unibw.de/inf2/lehre/wt19/maschprog/mi-manual-pdf.pdf zu finden.

## Probleme und Änderungswünsche ##

Bei Problemen mit dem MI-Simulator können Sie gerne ein Ticket erstellen.
Auch Ideen für nützliche Zusatzfunktionen und Verbesserungen können Sie uns gerne mitteilen.
Die Beschreibung kann sowohl auf Englisch als auch auf Deutsch erfolgen.

> **WICHTIG**
> Wir ersuchen Sie höflichst **keine** Lösungen (z.B: Programme) für Aufgaben in "Maschinennahe Programmierung" im Ticket zu inkludieren.
> Sollte ein Programm notwendig sein um ein Problem zu reproduzieren, können Sie uns das Programm per E-Mail zukommen lassen.
> Dies gibt anderen Studenten die Chance die Aufgaben selbstständig zu lösen.

## JARs erstellen ##
Zum Erstellen und Ausführen des Simulators ist mindestens Java 21 erforderlich.

Um ausführbare JAR Dateien zu erstellen kann das inkludierte Gradle script verwendet werden.
`./gradlew jar` für die GUI-Version des Simulators bzw. `./gradlew cli-jar` für die CLI-Version des Simulators.
Die erstellten JAR Dateien befinden sich dann unter `build/libs`.

## CLI Mode ##

Um das automatische Testen von MI-Programmen zu vereinfachen, wurde dem Simulator ein CLI Modus hinzugefügt.
In diesem Modus können Programme ohne die grafische Oberfläche ausgeführt werden.
Nach jedem ausgeführten Befehl wird der Zustand der MI Maschine ausgegeben.
Der CLI Modus kann mit `java cli.Main <Programmdatei> [Zustandsdatei]` aufgerufen werden.

#### Beispiel ####

Wird das folgende Programm im CLI Modus ausgeführt

```
    SEG
    JUMP s

a:  DD W 1
b:  DD W 2
c:  DD W 3

s:  MOVEA a, R5
if: CMP W I 3, R7
    JEQ end
    ADD W 0+!R5/R7/, R2
    ADD W I 1, R7
    JUMP if

end:
    HALT
```

so wird beispielsweise folgender Text ausgegeben:

```
INS: MOVEA -14 + !R15, R5
R5: 0 -> 3; R15: 15 -> 19; 
C: 0; V: 0; Z: 0; N: 0; 
0: 241; 1: 175; 2: 13; 6: 1; 10: 2; 14: 3; 15: 171; 16: 175; 17: 242; 18: 85; 19: 148; 20: 3; 21: 87; 22: 233; 23: 175; 24: 11; 25: 193; 26: 71; 27: 101; 28: 82; 29: 193; 30: 1; 31: 87; 32: 241; 33: 175; 34: 241; 
```

Dies bedeutet, dass

* Befehl `MOVEA -14 + !R15, R5` ausgeführt wurde
* Register R5 seinen Wert von 0 auf 3 geändert hat und R15 von 15 auf 19
* Die Statusregister C, V, Z und N weiterhin den Wert 0 haben
* Speicheradresse 0 den Wert 241 hat, 1 den Wert 175, 2 den Wert 13 usw

Der CLI Modus akzeptiert noch einen optionalen zweiten Parameter, mit dem eine Zustandsdatei übergeben werden kann.
Beispielsweise könnte folgende Datei als zweite Datei übergeben werden:

```
R1: 3; R2: 3; R3: 7; R4: 2; R5: 2 -> 3; R15: 47 -> 50;
C: 0; V: 0; Z: 0; N: 0;
0: 241; 1: 175; 2: 9; 6: 7; 11: 148; 12: 175; 13: 246; 14: 2; 15: 236; 16: 175; 17: 7; 18: 160; 19: 1; 20: 81; 21: 241; 22: 175; 23: 36; 24: 160; 25: 1; 26: 84; 27: 160; 28: 1; 29: 85; 30: 160; 31: 2; 32: 82; 33: 160; 34: 175; 35: 224; 36: 83; 37: 241; 38: 175; 39: 14; 40: 198; 41: 84; 42: 85; 43: 81; 44: 160; 45: 85; 46: 84; 47: 160; 48: 81; 49: 85; 50: 193; 51: 1; 52: 82; 53: 148; 54: 82; 55: 83; 56: 238; 57: 175; 58: 238; 59: 160; 60: 81; 61: 175; 62: 201;
```
Die MI Maschine wird dann nach dem Einlesen des Programmes mit diesem Zustand initialisiert.
Bei Feldern, in denen eine Zustandsänderung signalisiert wird (z.B. `2 -> 3`), wird der Simulator auf den Zielzustand
initialisiert (im Beispiel `2`).
Dadurch dass die Zustandsdatei auch solche Zustandsübergänge in Textform enthalten kann, kann der ausgegebene Zustand
bei einem Programmdurchlauf direkt als Zustandsdatei verwendet werden.

### Bekannte Probleme des CLI Modus ###

* Aktuell werden auch im CLI Modus Java GUI Threads im Hintergrund gestartet (z.B. `AWT-Eventqueue`).
  Dies liegt daran, dass die Logik und die Darstellungskomponenten des MI-Simulators noch nicht ausreichend entkoppelt
  sind.
* Theoretisch ist die Programmdatei überflüssig, wenn eine Zustandsdatei angegeben wird.
  Der Zustand enthält bereits das kodierte Programm.
  Allerdings unterstützt die Programmlogik des MI-Simulators aktuell noch keine Ausführung ohne ein Programm in
  Textform.

## Bekannte Probleme ##
* Unter Linux kann es mit bestimmten Window Managern zu dem Problem kommen, dass das MI-Simulator Fenster leer ist bzw. kein Inhalt angezeigt wird. In diesem Fall kann es helfen den Simulator mit `export _JAVA_AWT_WM_NONREPARENTING=1` zu starten.
