Szerver funkciók helyes sorrendje:

CreateGame(képID, keverés);
	Innentől válaszol a játék kereső üzenetekre, illetve lehet hozzá csatlakozni játékosnak.
	Várjuk, amíg a start gombra rá nem kattint a játékmseter.
GetConnectionCount();
	Le tudjuk kérdezni, hányan csatlakoztak.
StartGame();
	Innentől nem válaszol keresésekre, nem lehet már csatalkozni.
	Akik korábban becsatlakoztak, ekkor kapják meg a keverést és a képet
SyncScore(aktuális pontjaim);
	Ennek hatására beadjuk a saját pontjainkat, és vissza kapjuk a többiek beküldött pontjait.
	A játék során nyilván ez valamilyen frekivel megy, hogy a többiek eredménye frissüljön, ha a miénk nem is változott
FinishGame();
	Innentől leáll a szerver, és nem fut tovább a játék, a kapcsolatokat lezárja.
	Kötelező meghívni, külöben nyitva maradnak a kapcsolatok, ami nem túl jó.

	
Kliens funkciók helyes sorrendje:
ListGameNames(intervallum);
	Visszaadja az intervallumon belül választ adó játékmesterek neveit.
JoinGame(mester név);
	A fenti listából választott játékmester játékához csatlakozik.
	Itt még csinálhatunk mást.
GetPuzzle();
	Innetől blokkolva van a szál. Várja, hogy a játékmester elindítsa a játékot. Ha elindult, megkapja a játékteret, amivel visszatér.
	Visszatérés után már megy a játék.
SyncScore(saját pontszám);
	Elküldi a szervernek a saját pontjainkat, aki válaszban megküldi az aktuálisan nála elérhető összes játékos eredménylét.
LeaveGame();
	Kilép a játékból, és zárja a kapcsolatot.