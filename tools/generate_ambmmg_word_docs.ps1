$ErrorActionPreference = "Stop"

$Root = Resolve-Path (Join-Path $PSScriptRoot "..")
$OutDir = Join-Path $Root "documentazione"
New-Item -ItemType Directory -Force -Path $OutDir | Out-Null

function Add-Paragraph {
    param($Doc, [string]$Text, [int]$Size = 11, [string]$Font = "Calibri", [switch]$Bold, [string]$Color = "000000", [int]$SpaceAfter = 6)
    $range = $Doc.Range($Doc.Content.End - 1, $Doc.Content.End - 1)
    $range.Text = $Text
    $range.Font.Name = $Font
    $range.Font.Size = $Size
    $range.Font.Bold = $(if ($Bold) { 1 } else { 0 })
    $range.Font.Color = [Convert]::ToInt32($Color.Substring(4,2) + $Color.Substring(2,2) + $Color.Substring(0,2), 16)
    $range.ParagraphFormat.SpaceAfter = $SpaceAfter
    $range.InsertParagraphAfter()
}

function Add-Heading {
    param($Doc, [string]$Text, [int]$Level = 1)
    $size = $(if ($Level -eq 1) { 18 } elseif ($Level -eq 2) { 14 } else { 12 })
    Add-Paragraph $Doc $Text $size "Century Gothic" -Bold -Color $(if ($Level -eq 1) { "17365D" } else { "2E5E8F" }) 8
}

function Add-PageBreak {
    param($Doc)
    $range = $Doc.Range($Doc.Content.End - 1, $Doc.Content.End - 1)
    $range.InsertBreak(7) | Out-Null
}

function Add-Table {
    param($Doc, [string[]]$Headers, [object[]]$Rows)
    $range = $Doc.Range($Doc.Content.End - 1, $Doc.Content.End - 1)
    $table = $Doc.Tables.Add($range, $Rows.Count + 1, $Headers.Count)
    $table.Borders.Enable = 1
    $table.Range.Font.Name = "Calibri"
    $table.Range.Font.Size = 9
    for ($c = 1; $c -le $Headers.Count; $c++) {
        $cell = $table.Cell(1, $c)
        $cell.Range.Text = $Headers[$c - 1]
        $cell.Range.Font.Bold = 1
        $cell.Range.Font.Color = 6108689
        $cell.Shading.BackgroundPatternColor = 15921906
    }
    for ($r = 0; $r -lt $Rows.Count; $r++) {
        for ($c = 0; $c -lt $Headers.Count; $c++) {
            $table.Cell($r + 2, $c + 1).Range.Text = [string]$Rows[$r][$c]
        }
    }
    $table.AutoFitBehavior(1) | Out-Null
    $Doc.Range($Doc.Content.End - 1, $Doc.Content.End - 1).InsertParagraphAfter()
}

function Add-Note {
    param($Doc, [string]$Text)
    $range = $Doc.Range($Doc.Content.End - 1, $Doc.Content.End - 1)
    $table = $Doc.Tables.Add($range, 1, 1)
    $table.Borders.Enable = 1
    $table.Cell(1,1).Shading.BackgroundPatternColor = 16448250
    $table.Cell(1,1).Range.Text = $Text
    $table.Cell(1,1).Range.Font.Name = "Calibri"
    $table.Cell(1,1).Range.Font.Size = 10
    $Doc.Range($Doc.Content.End - 1, $Doc.Content.End - 1).InsertParagraphAfter()
}

function Add-Cover {
    param($Doc, [string]$Title, [string]$Kind, [string]$Subtitle)
    Add-Paragraph $Doc "AmbMMG" 24 "Century Gothic" -Bold -Color "17365D" 2
    Add-Paragraph $Doc "Sistema web ambulatoriale MMG" 11 "Century Gothic" -Color "506070" -SpaceAfter 42
    Add-Paragraph $Doc $Kind.ToUpperInvariant() 14 "Century Gothic" -Bold -Color "506070" 10
    Add-Paragraph $Doc $Title 30 "Palatino Linotype" -Bold -Color "17365D" 12
    Add-Paragraph $Doc $Subtitle 14 "Calibri" -Color "333333" -SpaceAfter 28
    Add-Table $Doc @("Applicativo", "Versione", "Data", "Classificazione") @(
        @("AmbMMG", "1.0", "19/05/2026", "Uso interno"),
        @("Ambiente", "Java Web / Tomcat / Oracle", "Stato", "Documento operativo")
    )
    Add-Note $Doc "Schema sintetico: Utente browser aziendale -> AmbMMG Servlet/JavaScript -> Oracle Database, LDAP/Active Directory e JasperReports per la stampa PDF."
    Add-PageBreak $Doc
}

function Add-ControlPage {
    param($Doc, [string]$Kind)
    Add-Heading $Doc "Controllo del documento" 1
    Add-Table $Doc @("Versione", "Data", "Descrizione", "Redazione") @(
        @("1.0", "19/05/2026", "Prima emissione $Kind AmbMMG.", "Ufficio applicativo")
    )
    Add-Heading $Doc "Indice" 2
}

function Add-ArchitectureSchema {
    param($Doc)
    Add-Heading $Doc "Schema architetturale" 2
    Add-Table $Doc @("Front-end web", "Servlet applicativi", "Servizi / DAO", "Sistemi esterni") @(
        @("LOGIN.html, APO.html, APO.js, LOGIN.js", "AuthServlet, AccessiServlet, PazienteServlet, StampaReport", "DataService, LdapService, UserServiceDao, AccessiDao, PazienteDao", "Oracle DB, LDAP, JasperReports, Ricette, DSE, Repository referti")
    )
}

function New-WordDocument {
    param([string]$Title, [string]$Kind, [string]$Subtitle, [scriptblock]$Body, [string]$DocxName, [string]$PdfName)
    $word = New-Object -ComObject Word.Application
    $word.Visible = $false
    $doc = $word.Documents.Add()
    try {
        $doc.PageSetup.TopMargin = 54
        $doc.PageSetup.BottomMargin = 50
        $doc.PageSetup.LeftMargin = 50
        $doc.PageSetup.RightMargin = 50
        $doc.Content.Font.Name = "Calibri"
        $doc.Content.Font.Size = 11
        Add-Cover $doc $Title $Kind $Subtitle
        & $Body $doc
        $doc.Fields.Update() | Out-Null
        $doc.SaveAs([ref](Join-Path $OutDir $DocxName), [ref]16)
        $doc.ExportAsFixedFormat((Join-Path $OutDir $PdfName), 17)
    }
    finally {
        $doc.Close($false)
        $word.Quit()
        [System.Runtime.InteropServices.Marshal]::ReleaseComObject($doc) | Out-Null
        [System.Runtime.InteropServices.Marshal]::ReleaseComObject($word) | Out-Null
    }
}

$techBody = {
    param($Doc)
    Add-ControlPage $Doc "delle specifiche tecniche"
    Add-Table $Doc @("Capitolo", "Descrizione") @(
        @("1", "Scopo e perimetro"),
        @("2", "Architettura e componenti"),
        @("3", "Configurazione, sicurezza e sessione"),
        @("4", "Endpoint e servizi applicativi"),
        @("5", "Base dati e flussi informativi"),
        @("6", "Reportistica e integrazioni"),
        @("7", "Specifiche di test"),
        @("8", "Rilascio e controlli")
    )
    Add-PageBreak $Doc
    Add-Heading $Doc "1. Scopo e perimetro" 1
    Add-Paragraph $Doc "AmbMMG e' una web application a supporto delle attivita' ambulatoriali svolte dagli operatori autorizzati. Il sistema centralizza consultazione accessi, selezione paziente, scheda clinico-amministrativa, prestazioni infermieristiche e Allegato M."
    Add-Paragraph $Doc "Il repository mantiene riferimenti storici APO in nomi file e artifact, mentre la denominazione funzionale documentata e' AmbMMG."
    Add-Note $Doc "Perimetro: codice sorgente, configurazione web, dipendenze, flussi applicativi e specifiche di test derivate dal progetto locale."
    Add-Heading $Doc "Funzioni coperte" 2
    Add-Table $Doc @("Area", "Funzioni") @(
        @("Autenticazione", "Login LDAP, caricamento servizi utente, cambio servizio, logout e sessione."),
        @("Accessi", "Ricerca, ordinamento, inserimento nuovo accesso, eliminazione logica."),
        @("Paziente", "Ricerca anagrafica, censimento, codice fiscale, scheda paziente."),
        @("Storici", "Prestazioni infermieristiche, ricette farmaci, ricette dematerializzate, piani terapeutici."),
        @("Allegato M", "Caricamento, compilazione, salvataggio e stampa PDF Jasper.")
    )
    Add-PageBreak $Doc
    Add-Heading $Doc "2. Architettura e componenti" 1
    Add-Paragraph $Doc "La soluzione segue un modello a livelli: presentazione web, servlet controller, servizi infrastrutturali, DAO e sistemi esterni. La comunicazione client/server avviene tramite chiamate HTTP JSON e risposta ApiResponse."
    Add-ArchitectureSchema $Doc
    Add-Table $Doc @("Livello", "Componenti", "Responsabilita'") @(
        @("Presentazione", "LOGIN.html, APO.html, LOGIN.js, APO.js, CSS", "Interfaccia utente, validazioni preliminari, rendering griglie, modali e chiamate fetch."),
        @("Controller", "AuthServlet, AccessiServlet, PazienteServlet, StampaReport", "Routing HTTP, controllo sessione, validazione parametri, output JSON/PDF."),
        @("Servizi", "DataService, LdapService", "Connessioni Oracle via JNDI/DriverManager e autenticazione LDAP."),
        @("DAO", "UserServiceDao, AccessiDao, PazienteDao", "Query, insert, update e delete sugli oggetti applicativi."),
        @("Report", "AllegatoM2.jasper, JasperReports", "Generazione PDF dell'Allegato M.")
    )
    Add-PageBreak $Doc
    Add-Heading $Doc "3. Configurazione, sicurezza e sessione" 1
    Add-Table $Doc @("Parametro", "Descrizione", "Uso") @(
        @("dbDriver", "Driver JDBC Oracle", "DAO e report Jasper."),
        @("dbUrl", "URL Oracle", "Accesso dati applicativi e sanitari."),
        @("dbUser / dbPassword", "Credenziali database", "Connessioni JDBC."),
        @("dbJndi", "DataSource opzionale", "Uso JNDI in alternativa a DriverManager."),
        @("ldapUrl", "Endpoint LDAP aziendale", "Autenticazione utente."),
        @("ldapBaseDn / ldapSearchFilter", "Base e filtro LDAP", "Recupero attributi profilo."),
        @("session-timeout", "Durata sessione", "60 minuti.")
    )
    Add-Paragraph $Doc "Le misure applicative includono rigenerazione della sessione al login, cookie HttpOnly e SameSite=Lax, header no-cache, filtro SecurityHeadersFilter e controllo sessione su tutti gli endpoint operativi."
    Add-Note $Doc "Le credenziali presenti in configurazione devono essere esternalizzate o sostituite da risorse JNDI prima del rilascio produttivo."
    Add-PageBreak $Doc
    Add-Heading $Doc "4. Endpoint e servizi applicativi" 1
    Add-Table $Doc @("Area", "Endpoint", "Metodo", "Descrizione") @(
        @("Autenticazione", "/api/auth/services, /login, /logout, /session", "GET/POST", "Servizi utente, login, logout e sessione."),
        @("Servizi AmbMMG", "/api/auth/apo-services, /switch-service", "GET/POST", "Elenco e cambio servizio autorizzato."),
        @("Accessi", "/api/accessi/search, /new, /delete", "GET/POST", "Ricerca, inserimento ed eliminazione logica."),
        @("Paziente", "/api/paziente/anagrafe/search, /anagrafe/new, /codice-fiscale, /comuni, /asl", "GET/POST", "Anagrafica e lookup."),
        @("Scheda paziente", "/scheda-paziente e sotto-risorse", "GET/POST/DELETE", "Diario, patologie, allergie, terapie."),
        @("Storici", "/ricette-farmaci, /ricette-dematerializzate, /piani-terapeutici", "GET", "Consultazione storici."),
        @("Allegato M", "/api/paziente/allegato-m, /StampaReport", "GET/POST", "Compilazione e stampa PDF.")
    )
    Add-PageBreak $Doc
    Add-Heading $Doc "5. Base dati e flussi informativi" 1
    Add-Paragraph $Doc "La persistenza e' Oracle. I DAO usano viste e tabelle applicative e sanitarie dedicate. Gli oggetti principali rilevati sono elencati nella tabella seguente."
    Add-Table $Doc @("Oggetto dati", "Uso applicativo") @(
        @("VIEW_CONSULENZE_GMNEW, CONSULENZE_GM", "Elenco, inserimento ed eliminazione logica degli accessi."),
        @("VIEW_ANAGRAFE_DETTAGLIO_GM, TOPSAN.PAZIENTI_BUFFER", "Ricerca e censimento paziente."),
        @("APO_SCHEDE_PAZIENTE, _DIARIO, _PATOLOGIE, _ALLERGIE, _TERAPIE", "Scheda paziente e contenuti clinici."),
        @("PRESTAZIONI_INF_APO, PRESTAZIONI_EFFETTUATE_APO", "Catalogo e registrazione prestazioni infermieristiche."),
        @("ALLEGATOM", "Compilazione e aggiornamento Allegato M."),
        @("SERVIZI, TESTFISIATRIAWEB.UTENTI", "Autorizzazioni utente e servizio.")
    )
    Add-Table $Doc @("Fase nuovo accesso", "Descrizione") @(
        @("Ricerca paziente", "L'utente imposta filtri anagrafici e seleziona il paziente."),
        @("Censimento", "Se assente, il paziente viene inserito con dati obbligatori."),
        @("Compilazione accesso", "Sono valorizzati tipo accesso, diagnosi, patologia e dati scheda."),
        @("Persistenza", "AccessiDao registra la consulenza e i dati collegati."),
        @("Aggiornamento UI", "La griglia accessi viene ricaricata.")
    )
    Add-PageBreak $Doc
    Add-Heading $Doc "6. Reportistica e integrazioni" 1
    Add-Paragraph $Doc "StampaReport produce il PDF Allegato M usando JasperReports. Il report riceve COD_CONSULENZA e COD_SERVIZIO, apre connessione Oracle e restituisce il PDF inline."
    Add-Table $Doc @("Integrazione", "Descrizione", "Modalita'") @(
        @("LDAP / Active Directory", "Autenticazione e attributi utente.", "JNDI LDAP"),
        @("Oracle Database", "Dati accessi, pazienti, servizi, storici e Allegato M.", "JDBC"),
        @("JasperReports", "Produzione PDF Allegato M.", "Report .jasper"),
        @("Sistemi ricette", "Ricette dematerializzate e farmaci.", "URL esterni"),
        @("DSE / Referti", "Consenso DSE e repository referti.", "URL/funzioni DB")
    )
    Add-Heading $Doc "Dipendenze" 2
    Add-Paragraph $Doc "Le dipendenze sono censite nel file sbom.json in formato CycloneDX: Oracle JDBC, Gson, JasperReports, iText, Groovy, Apache Commons, JFreeChart e JCommon."
    Add-PageBreak $Doc
    Add-Heading $Doc "7. Specifiche di test" 1
    Add-Table $Doc @("ID", "Caso di test", "Prerequisiti", "Esito atteso") @(
        @("AMB-01", "Login con credenziali valide", "Utente LDAP abilitato", "Sessione creata e accesso alla home."),
        @("AMB-02", "Login con servizio non autorizzato", "Utente non associato", "Errore e sessione non creata."),
        @("AMB-03", "Ricerca accessi per data e paziente", "Accessi presenti", "Lista filtrata coerente col servizio."),
        @("AMB-04", "Inserimento nuovo accesso", "Paziente valido", "Consulenza inserita e griglia aggiornata."),
        @("AMB-05", "Censimento incompleto", "Campi obbligatori mancanti", "Errore sul campo mancante."),
        @("AMB-06", "Salvataggio scheda paziente", "Paziente selezionato", "Dati persistiti e ricaricabili."),
        @("AMB-07", "Aggiunta diario/patologia/allergia/terapia", "Scheda disponibile", "Elemento visibile nello storico."),
        @("AMB-08", "Prestazioni senza sottocategoria", "Consulenza selezionata", "Validazione bloccante."),
        @("AMB-09", "Allegato M salvato da altro utente", "Record esistente", "Risposta 403 e modifica impedita."),
        @("AMB-10", "Stampa Allegato M", "Parametri validi", "PDF restituito al browser.")
    )
    Add-Heading $Doc "8. Rilascio e controlli" 1
    Add-Table $Doc @("Voce", "Valore") @(
        @("Versione Java", "1.8"),
        @("Container", "Apache Tomcat / Servlet 3.0"),
        @("Formato distribuzione", "WAR"),
        @("DBMS", "Oracle Database"),
        @("Autenticazione", "LDAP / Active Directory")
    )
}

$userBody = {
    param($Doc)
    Add-ControlPage $Doc "della guida utente"
    Add-Table $Doc @("Capitolo", "Descrizione") @(
        @("1", "Accesso all'applicazione"),
        @("2", "Schermata Accessi"),
        @("3", "Nuovo accesso e censimento paziente"),
        @("4", "Scheda paziente"),
        @("5", "Storici, prestazioni e Allegato M"),
        @("6", "Logout, messaggi e assistenza")
    )
    Add-PageBreak $Doc
    Add-Heading $Doc "1. Accesso all'applicazione" 1
    Add-Paragraph $Doc "Aprire AmbMMG dal browser aziendale. Inserire utente e password di dominio. Dopo la digitazione dell'utente, selezionare il servizio abilitato e premere Entra."
    Add-Table $Doc @("Campo", "Descrizione") @(
        @("Utente", "Nome utente aziendale."),
        @("Password", "Password personale di dominio."),
        @("Servizio", "Servizio operativo associato all'utente."),
        @("Entra", "Avvia autenticazione e apre la home applicativa.")
    )
    Add-Note $Doc "Se il servizio non compare, verificare l'abilitazione dell'utenza con il referente applicativo."
    Add-PageBreak $Doc
    Add-Heading $Doc "2. Schermata Accessi" 1
    Add-Paragraph $Doc "La sezione Accessi e' la pagina principale dopo il login. Permette di cercare consulenze, selezionare un paziente e inserire un nuovo accesso."
    Add-Table $Doc @("Area schermata", "Funzione") @(
        @("Menu principale", "Accessi, Paziente, Statistiche e Logout."),
        @("Filtri", "Data, consulenza, paziente, sede, codice fiscale e medico."),
        @("Azioni", "Cerca, Pulisci e Nuovo accesso."),
        @("Griglia risultati", "Elenco degli accessi filtrati e ordinabili."),
        @("Paziente selezionato", "Dati sintetici e accesso alla scheda paziente.")
    )
    Add-PageBreak $Doc
    Add-Heading $Doc "3. Nuovo accesso e censimento paziente" 1
    Add-Paragraph $Doc "Premere Nuovo accesso, ricercare il paziente tramite codice fiscale, cognome o nome, quindi selezionare la riga corretta."
    Add-Table $Doc @("Situazione", "Operazione") @(
        @("Paziente presente", "Selezionare la riga e completare il dettaglio del nuovo accesso."),
        @("Paziente assente", "Aprire il censimento e compilare i dati obbligatori."),
        @("Codice fiscale da calcolare", "Compilare dati anagrafici e premere Calcola."),
        @("Salvataggio", "Premere Aggiungi nuovo accesso per registrare la consulenza.")
    )
    Add-Table $Doc @("Dati obbligatori censimento", "Nota") @(
        @("Codice fiscale, cognome, nome", "Il codice fiscale deve avere 16 caratteri."),
        @("Data nascita, sesso, comune nascita", "Il comune va selezionato dai suggerimenti."),
        @("Indirizzo, comune e CAP residenza", "Necessari per completare anagrafica."),
        @("ASL residenza e ASL iscrizione", "Selezionare dall'elenco disponibile.")
    )
    Add-PageBreak $Doc
    Add-Heading $Doc "4. Scheda paziente" 1
    Add-Paragraph $Doc "La scheda paziente raccoglie informazioni sintetiche utili alla presa in carico. Si apre dal pannello del paziente selezionato o durante il nuovo accesso."
    Add-Table $Doc @("Sezione", "Contenuto") @(
        @("Dati generali", "Telefono, stato civile, fumo, BMI, caregiver e note."),
        @("Diario", "Annotazioni datate e storico."),
        @("Patologie", "Patologie croniche codificate ICD10."),
        @("Allergie", "Allergie codificate."),
        @("Terapie", "Principio attivo, farmaco, confezione, quantita', frequenza e durata.")
    )
    Add-Paragraph $Doc "Dopo ogni modifica premere Salva scheda. In presenza di errori, correggere il campo indicato e ripetere il salvataggio."
    Add-PageBreak $Doc
    Add-Heading $Doc "5. Storici, prestazioni e Allegato M" 1
    Add-Table $Doc @("Scheda", "Contenuto") @(
        @("Prestazioni infermieristiche", "Prestazioni registrate per il paziente."),
        @("Ricette dematerializzate", "Elenco ricette dematerializzate disponibili."),
        @("Ricette farmaci", "Storico ricette farmaci."),
        @("Piani terapeutici", "Piani associati al paziente."),
        @("Allegato M", "Compilazione, salvataggio e stampa PDF.")
    )
    Add-Paragraph $Doc "Per le prestazioni infermieristiche selezionare categoria, sottocategorie e note. Il sistema richiede almeno una sottocategoria."
    Add-Note $Doc "Se l'Allegato M e' gia' stato salvato da un altro utente, il sistema blocca la modifica per evitare sovrascritture."
    Add-PageBreak $Doc
    Add-Heading $Doc "6. Logout, messaggi e assistenza" 1
    Add-Paragraph $Doc "Al termine del lavoro premere Logout. La sessione scade automaticamente dopo inattivita' prolungata."
    Add-Table $Doc @("Messaggio", "Significato", "Azione") @(
        @("Sessione non attiva", "Sessione scaduta o non valida", "Effettuare nuovamente il login."),
        @("Credenziali non valide", "Utente o password errati", "Verificare le credenziali."),
        @("Valorizza almeno un filtro di ricerca", "Ricerca anagrafica senza parametri", "Inserire codice fiscale, cognome o nome."),
        @("Campo obbligatorio non valorizzato", "Manca un dato richiesto", "Compilare il campo indicato."),
        @("Errore nel caricamento", "Problema temporaneo o dato non disponibile", "Riprovare o contattare assistenza.")
    )
}

New-WordDocument "Specifiche tecniche e specifiche di test" "Specifiche tecniche" "Architettura, configurazione, integrazioni, flussi applicativi e casi di test del sistema AmbMMG." $techBody "Specifiche_Tecniche_AmbMMG.docx" "Specifiche_Tecniche_AmbMMG.pdf"
New-WordDocument "Guida Utente" "Manuale operativo" "Istruzioni operative per l'utilizzo quotidiano del sistema AmbMMG da parte degli utenti autorizzati." $userBody "Guida_Utente_AmbMMG.docx" "Guida_Utente_AmbMMG.pdf"
