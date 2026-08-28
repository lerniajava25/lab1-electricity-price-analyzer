Kort beskrivning av implementationen

Lab 1 är uppbyggd kring en Main-klass med flera metoder samt en ElectricityPrice-record som används som datamodell för elpriserna från API:et.
Programmet innehåller även felhantering för tänkbara fel, exempelvis problem vid nätverkskommunikation eller om listan med elpriser är tom.
För att dela upp programmets olika funktioner har jag skapat separata metoder med olika ansvarsområden:

•	showApiIntegration() ansvarar för att hämta elprisdata från API:et. Javas inbyggda HttpClient används för HTTP-anropet. 
	JSON-svaret läses som text och konverteras därefter med Jackson ObjectMapper till en List<ElectricityPrice>. 
	
•	showPrices() tar emot listan med elpriser och skriver ut priserna med hjälp av en enhanced for-loop. 

•	ShowMenu() sköter programmets meny och användarens val. 

•	showPriceAnalysis() beräknar lägsta, högsta och genomsnittliga elpriset under dygnet. 

•	sortedPrices() skapar först en kopia av prislistan och sorterar därefter elpriserna från lägst till högst med hjälp av Comparator. 

•	showBestChargingTime() använder en sliding window-algoritm för att hitta fyra sammanhängande timmar med lägst genomsnittligt elpris. 


Reflektion

Att lösa uppgiften i Java jämfört med JavaScript innebar en viss omställning. Java är mer strikt typat, vilket innebär att exempelvis datatyper,
parametrar och returtyper behöver anges tydligare. Även API-anrop skiljer sig från JavaScript, där jag tidigare har använt fetch(),
medan jag i denna uppgift använde Javas HttpClient.
Jag upplevde även skillnader i hur metoder byggs upp och hur Javas inbyggda API:er används, exempelvis vid sortering med Comparator.
Samtidigt uppskattade jag att IntelliJ automatiskt kan föreslå och importera de klasser som behövs.

En annan sak jag reflekterade över är att Java erbjuder både arrayer och List. En array har en fast storlek,
medan en List är mer flexibel eftersom storleken kan ändras genom att element läggs till eller tas bort. Syntaxen skiljer sig också,
eftersom en List exempelvis använder .get() för att hämta ett element och .size() för att få antalet element, medan en array använder indexering med [] och .length.
I denna uppgift valde jag List<ElectricityPrice>, vilket passar bra för att hantera den data som hämtas från API:et.
