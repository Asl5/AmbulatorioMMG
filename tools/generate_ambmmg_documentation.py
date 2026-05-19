import base64
import hashlib
import html
import json
import os
import subprocess
import uuid
from datetime import datetime, timezone


ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
OUT_DIR = os.path.join(ROOT, "documentazione")
CHROME = r"C:\Program Files\Google\Chrome\Application\chrome.exe"
EDGE = r"C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe"
DOC_DATE = "19/05/2026"
DOC_DATE_ISO = "2026-05-19"


def rel(path):
    return os.path.relpath(path, ROOT).replace("\\", "/")


def sha256(path):
    digest = hashlib.sha256()
    with open(path, "rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def parse_jar_name(filename):
    base = filename[:-4] if filename.lower().endswith(".jar") else filename
    parts = base.rsplit("-", 1)
    if len(parts) == 2 and parts[1][:1].isdigit():
        return parts[0], parts[1]
    return base, "unknown"


def file_uri(path):
    return "file:///" + os.path.abspath(path).replace("\\", "/").replace(" ", "%20")


def img_data_uri(path):
    if not os.path.exists(path):
        return ""
    ext = os.path.splitext(path)[1].lower().lstrip(".") or "png"
    mime = "jpeg" if ext in ("jpg", "jpeg") else ext
    with open(path, "rb") as stream:
        return "data:image/{};base64,{}".format(mime, base64.b64encode(stream.read()).decode("ascii"))


def build_sbom():
    jar_paths = []
    for folder in ("lib", "web/WEB-INF/lib"):
        abs_folder = os.path.join(ROOT, folder)
        if not os.path.isdir(abs_folder):
            continue
        for filename in sorted(os.listdir(abs_folder)):
            path = os.path.join(abs_folder, filename)
            if os.path.isfile(path) and filename.lower().endswith(".jar"):
                jar_paths.append(path)

    components = []
    for path in jar_paths:
        filename = os.path.basename(path)
        name, version = parse_jar_name(filename)
        purl_version = "" if version == "unknown" else "@{}".format(version)
        bom_ref = "pkg:generic/{}{}?download_url={}".format(name, purl_version, rel(path))
        components.append({
            "type": "library",
            "name": name,
            "version": version,
            "scope": "required",
            "hashes": [{"alg": "SHA-256", "content": sha256(path)}],
            "purl": bom_ref,
            "bom-ref": bom_ref,
            "properties": [
                {"name": "local.path", "value": rel(path)},
                {"name": "file.name", "value": filename}
            ]
        })

    return {
        "bomFormat": "CycloneDX",
        "specVersion": "1.4",
        "serialNumber": "urn:uuid:{}".format(uuid.uuid4()),
        "version": 1,
        "metadata": {
            "timestamp": datetime.now(timezone.utc).replace(microsecond=0).isoformat().replace("+00:00", "Z"),
            "tools": [{
                "vendor": "AmbMMG",
                "name": "Generatore SBOM interno",
                "version": "1.0"
            }],
            "component": {
                "group": "it.asl5.ca",
                "name": "AmbMMG",
                "version": "1.0.0",
                "type": "application",
                "purl": "pkg:generic/it.asl5.ca/AmbMMG@1.0.0",
                "bom-ref": "pkg:generic/it.asl5.ca/AmbMMG@1.0.0"
            },
            "properties": [
                {"name": "document.type", "value": "Software Bill of Materials"},
                {"name": "project.name", "value": "AmbMMG"},
                {"name": "java.source", "value": "1.8"},
                {"name": "target.container", "value": "Apache Tomcat / Servlet 3.0"},
                {"name": "database", "value": "Oracle Database"},
                {"name": "authentication", "value": "LDAP / Active Directory"}
            ]
        },
        "components": components,
        "dependencies": [{
            "ref": "pkg:generic/it.asl5.ca/AmbMMG@1.0.0",
            "dependsOn": [component["bom-ref"] for component in components]
        }]
    }


def css():
    return """
@page {
  size: A4;
  margin: 19mm 16mm 17mm 16mm;
  @top-left { content: "AmbMMG"; font-family: Calibri, Arial, sans-serif; font-size: 9pt; color: #4a4a4a; }
  @top-right { content: "Versione 1.0"; font-family: Calibri, Arial, sans-serif; font-size: 9pt; color: #4a4a4a; }
  @bottom-left { content: "Documento interno"; font-family: Calibri, Arial, sans-serif; font-size: 8pt; color: #666; }
  @bottom-right { content: "Pag. " counter(page) " di " counter(pages); font-family: Calibri, Arial, sans-serif; font-size: 8pt; color: #666; }
}
* { box-sizing: border-box; }
body {
  margin: 0;
  color: #1f1f1f;
  font-family: Calibri, Arial, sans-serif;
  font-size: 11pt;
  line-height: 1.36;
}
.cover {
  min-height: 252mm;
  padding: 22mm 18mm;
  border: 1.5pt solid #b9c2d3;
  position: relative;
}
.brand-row { display: flex; align-items: center; justify-content: space-between; border-bottom: 2pt solid #2e5e8f; padding-bottom: 8mm; }
.brand-mark {
  width: 26mm; height: 26mm; border: 2pt solid #2e5e8f; color: #2e5e8f;
  display: flex; align-items: center; justify-content: center; font-family: "Century Gothic", Calibri, sans-serif;
  font-size: 18pt; font-weight: 700;
}
.brand-text { text-align: right; font-family: "Century Gothic", Calibri, sans-serif; color: #304a68; }
.doc-class { margin-top: 36mm; font-size: 14pt; color: #506070; font-family: "Century Gothic", Calibri, sans-serif; text-transform: uppercase; letter-spacing: .7pt; }
h1.cover-title { font-family: "Palatino Linotype", Georgia, serif; font-size: 30pt; color: #17365d; margin: 7mm 0 6mm 0; line-height: 1.08; }
.cover-subtitle { font-size: 14pt; color: #363636; max-width: 150mm; }
.cover-box { position: absolute; left: 18mm; right: 18mm; bottom: 24mm; border-top: 1pt solid #b9c2d3; padding-top: 7mm; }
.meta-grid { display: grid; grid-template-columns: 38mm 1fr 38mm 1fr; gap: 2mm 5mm; font-size: 10pt; }
.label { color: #52616f; font-weight: 700; }
.page-break { break-before: page; page-break-before: always; }
h1 { font-family: "Century Gothic", Calibri, sans-serif; font-size: 19pt; color: #17365d; border-bottom: 2pt solid #9fb4cf; padding-bottom: 2.5mm; margin: 0 0 6mm 0; }
h2 { font-family: "Century Gothic", Calibri, sans-serif; font-size: 14pt; color: #2e5e8f; margin: 7mm 0 2.5mm; }
h3 { font-family: Calibri, Arial, sans-serif; font-size: 12pt; color: #17365d; margin: 5mm 0 2mm; }
p { margin: 0 0 3.2mm; text-align: justify; }
ul { margin: 1mm 0 4mm 6mm; padding-left: 5mm; }
li { margin-bottom: 1.4mm; }
table { width: 100%; border-collapse: collapse; margin: 3mm 0 6mm; font-size: 9.5pt; break-inside: avoid; }
th { background: #d9e2f3; color: #17365d; font-weight: 700; border: 1pt solid #9fb4cf; padding: 2mm; text-align: left; }
td { border: 1pt solid #bdc7d6; padding: 2mm; vertical-align: top; }
tr:nth-child(even) td { background: #f6f8fb; }
.toc td:first-child { width: 22mm; color: #17365d; font-weight: 700; }
.note { border-left: 4pt solid #2e75b6; background: #eef4fb; padding: 3mm 4mm; margin: 4mm 0; break-inside: avoid; }
.warning { border-left-color: #c55a11; background: #fff4e8; }
.diagram { margin: 4mm 0 7mm; break-inside: avoid; border: 1pt solid #bcc8d8; padding: 3mm; background: #fbfcfe; }
.screen { border: 1pt solid #8eaadb; background: #fff; padding: 3mm; margin: 4mm 0 6mm; break-inside: avoid; }
.screen-title { font-weight: 700; color: #17365d; border-bottom: 1pt solid #d1d8e6; padding-bottom: 1.5mm; margin-bottom: 2.5mm; }
.wire-toolbar { display: flex; gap: 2mm; margin-bottom: 2.5mm; }
.pill { border: 1pt solid #9fb4cf; background: #edf3fa; padding: 1.5mm 3mm; border-radius: 2mm; font-size: 8.5pt; color: #17365d; }
.wire-grid { display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 2mm; margin-top: 2mm; }
.wire-box { border: 1pt solid #c5d0df; min-height: 12mm; padding: 2mm; background: #f7f9fc; font-size: 8.5pt; }
.two-col { display: grid; grid-template-columns: 1fr 1fr; gap: 7mm; }
.small { font-size: 9pt; color: #4f4f4f; }
.caption { font-size: 9pt; color: #555; margin-top: -3mm; margin-bottom: 4mm; }
.revision th, .revision td { font-size: 9pt; }
.logo-img { max-width: 26mm; max-height: 26mm; object-fit: contain; }
svg text { font-family: Calibri, Arial, sans-serif; }
"""


def doc_shell(title, doc_type, subtitle, body):
    return """<!doctype html>
<html lang="it">
<head>
  <meta charset="utf-8">
  <title>{title}</title>
  <style>{css}</style>
</head>
<body>
  <section class="cover">
    <div class="brand-row">
      <div class="brand-mark">AM</div>
      <div class="brand-text">
        <div style="font-size:18pt;font-weight:700;">AmbMMG</div>
        <div style="font-size:10pt;">Sistema web ambulatoriale MMG</div>
      </div>
    </div>
    <div class="doc-class">{doc_type}</div>
    <h1 class="cover-title">{title}</h1>
    <div class="cover-subtitle">{subtitle}</div>
    <div class="diagram" style="margin-top:18mm;">
      {cover_diagram}
    </div>
    <div class="cover-box">
      <div class="meta-grid">
        <div class="label">Applicativo</div><div>AmbMMG</div>
        <div class="label">Versione</div><div>1.0</div>
        <div class="label">Data</div><div>{date}</div>
        <div class="label">Stato</div><div>Documento operativo</div>
        <div class="label">Ambiente</div><div>Java Web / Tomcat / Oracle</div>
        <div class="label">Classificazione</div><div>Uso interno</div>
      </div>
    </div>
  </section>
  {body}
</body>
</html>""".format(
        title=html.escape(title),
        doc_type=html.escape(doc_type),
        subtitle=html.escape(subtitle),
        date=DOC_DATE,
        cover_diagram=cover_diagram(),
        css=css(),
        body=body,
    )


def cover_diagram():
    return """
<svg viewBox="0 0 720 175" width="100%" height="175" role="img" aria-label="Schema sintetico AmbMMG">
  <rect x="20" y="38" width="130" height="70" rx="6" fill="#eaf2fb" stroke="#2e75b6"/>
  <text x="85" y="67" text-anchor="middle" font-size="18" fill="#17365d">Utente</text>
  <text x="85" y="90" text-anchor="middle" font-size="13" fill="#333">Browser aziendale</text>
  <line x1="150" y1="73" x2="235" y2="73" stroke="#4472c4" stroke-width="3" marker-end="url(#a)"/>
  <rect x="235" y="28" width="165" height="92" rx="6" fill="#ffffff" stroke="#2e75b6"/>
  <text x="318" y="58" text-anchor="middle" font-size="18" fill="#17365d">AmbMMG</text>
  <text x="318" y="82" text-anchor="middle" font-size="13" fill="#333">Servlet + JavaScript</text>
  <text x="318" y="102" text-anchor="middle" font-size="13" fill="#333">Sessione e API JSON</text>
  <line x1="400" y1="73" x2="485" y2="73" stroke="#4472c4" stroke-width="3" marker-end="url(#a)"/>
  <rect x="485" y="18" width="110" height="58" rx="6" fill="#edf7ed" stroke="#70ad47"/>
  <text x="540" y="43" text-anchor="middle" font-size="16" fill="#375623">Oracle DB</text>
  <text x="540" y="62" text-anchor="middle" font-size="12" fill="#333">Dati sanitari</text>
  <rect x="485" y="92" width="110" height="58" rx="6" fill="#fff4e8" stroke="#ed7d31"/>
  <text x="540" y="117" text-anchor="middle" font-size="16" fill="#843c0c">LDAP</text>
  <text x="540" y="136" text-anchor="middle" font-size="12" fill="#333">Autenticazione</text>
  <rect x="620" y="54" width="82" height="58" rx="6" fill="#f3eef8" stroke="#7030a0"/>
  <text x="661" y="79" text-anchor="middle" font-size="15" fill="#4f1f70">Report</text>
  <text x="661" y="98" text-anchor="middle" font-size="12" fill="#333">PDF Jasper</text>
  <defs>
    <marker id="a" markerWidth="10" markerHeight="10" refX="9" refY="3" orient="auto" markerUnits="strokeWidth">
      <path d="M0,0 L0,6 L9,3 z" fill="#4472c4" />
    </marker>
  </defs>
</svg>
"""


def architecture_diagram():
    return """
<svg viewBox="0 0 720 380" width="100%" height="380" role="img" aria-label="Architettura applicativa">
  <rect x="30" y="30" width="660" height="58" fill="#d9e2f3" stroke="#2e75b6"/>
  <text x="360" y="64" text-anchor="middle" font-size="21" fill="#17365d">Front-end web</text>
  <text x="360" y="82" text-anchor="middle" font-size="12" fill="#333">LOGIN.html, APO.html, APO.js, LOGIN.js, CSS applicativi</text>
  <rect x="30" y="125" width="145" height="85" fill="#eef4fb" stroke="#2e75b6"/>
  <text x="103" y="154" text-anchor="middle" font-size="16" fill="#17365d">AuthServlet</text>
  <text x="103" y="176" text-anchor="middle" font-size="12">login, logout</text>
  <text x="103" y="193" text-anchor="middle" font-size="12">servizi utente</text>
  <rect x="205" y="125" width="145" height="85" fill="#eef4fb" stroke="#2e75b6"/>
  <text x="278" y="154" text-anchor="middle" font-size="16" fill="#17365d">AccessiServlet</text>
  <text x="278" y="176" text-anchor="middle" font-size="12">ricerca, nuovo</text>
  <text x="278" y="193" text-anchor="middle" font-size="12">eliminazione</text>
  <rect x="380" y="125" width="145" height="85" fill="#eef4fb" stroke="#2e75b6"/>
  <text x="453" y="154" text-anchor="middle" font-size="16" fill="#17365d">PazienteServlet</text>
  <text x="453" y="176" text-anchor="middle" font-size="12">anagrafe, scheda</text>
  <text x="453" y="193" text-anchor="middle" font-size="12">storici, allegato</text>
  <rect x="555" y="125" width="135" height="85" fill="#eef4fb" stroke="#2e75b6"/>
  <text x="623" y="154" text-anchor="middle" font-size="16" fill="#17365d">StampaReport</text>
  <text x="623" y="176" text-anchor="middle" font-size="12">JasperReports</text>
  <text x="623" y="193" text-anchor="middle" font-size="12">Allegato M</text>
  <rect x="58" y="250" width="170" height="82" fill="#edf7ed" stroke="#70ad47"/>
  <text x="143" y="278" text-anchor="middle" font-size="16" fill="#375623">DAO</text>
  <text x="143" y="301" text-anchor="middle" font-size="12">UserServiceDao</text>
  <text x="143" y="318" text-anchor="middle" font-size="12">AccessiDao, PazienteDao</text>
  <rect x="275" y="250" width="170" height="82" fill="#fff4e8" stroke="#ed7d31"/>
  <text x="360" y="278" text-anchor="middle" font-size="16" fill="#843c0c">Servizi</text>
  <text x="360" y="301" text-anchor="middle" font-size="12">DataService</text>
  <text x="360" y="318" text-anchor="middle" font-size="12">LdapService</text>
  <rect x="492" y="250" width="170" height="82" fill="#f3eef8" stroke="#7030a0"/>
  <text x="577" y="278" text-anchor="middle" font-size="16" fill="#4f1f70">Integrazioni</text>
  <text x="577" y="301" text-anchor="middle" font-size="12">Oracle, LDAP</text>
  <text x="577" y="318" text-anchor="middle" font-size="12">Ricette, DSE, referti</text>
  <path d="M360 88 L360 112" stroke="#4472c4" stroke-width="3"/>
  <path d="M103 210 L143 250 M278 210 L143 250 M453 210 L143 250 M623 210 L577 250" stroke="#4472c4" stroke-width="2" fill="none"/>
</svg>
"""


def login_wireframe():
    return """
<div class="screen">
  <div class="screen-title">Figura 1 - Schermata di autenticazione</div>
  <div class="two-col">
    <div>
      <div class="wire-box" style="height:58mm;background:#eaf2fb;text-align:center;padding-top:20mm;font-size:18pt;color:#17365d;">AmbMMG</div>
    </div>
    <div>
      <div class="wire-box"><b>Utente</b><br>input username</div>
      <div class="wire-box"><b>Password</b><br>input protetto</div>
      <div class="wire-box"><b>Servizio</b><br>select dei servizi autorizzati</div>
      <div class="pill" style="display:inline-block;margin-top:2mm;background:#70ad47;color:white;border-color:#70ad47;">Entra</div>
    </div>
  </div>
</div>
"""


def accessi_wireframe():
    return """
<div class="screen">
  <div class="screen-title">Figura 2 - Area Accessi e dettaglio paziente</div>
  <div class="wire-toolbar">
    <span class="pill">Accessi</span><span class="pill">Paziente</span><span class="pill">Statistiche</span><span class="pill">Logout</span>
  </div>
  <div class="wire-grid">
    <div class="wire-box"><b>Filtri</b><br>data, paziente, sede, medico</div>
    <div class="wire-box"><b>Azioni</b><br>Cerca, Pulisci, Nuovo accesso</div>
    <div class="wire-box"><b>Ordinamento</b><br>campo e direzione</div>
  </div>
  <div class="wire-box" style="height:34mm;margin-top:2mm;"><b>Griglia accessi</b><br>data/ora, paziente, codice fiscale, sede, medico, diagnosi, stato</div>
  <div class="wire-grid">
    <div class="wire-box"><b>Paziente selezionato</b><br>anagrafica sintetica</div>
    <div class="wire-box"><b>Scheda paziente</b><br>diario, patologie, allergie, terapie</div>
    <div class="wire-box"><b>Storici</b><br>prestazioni, ricette, piani</div>
  </div>
</div>
"""


def technical_body():
    return """
<section class="page-break">
  <h1>Controllo del documento</h1>
  <table class="revision">
    <tr><th>Versione</th><th>Data</th><th>Descrizione</th><th>Redazione</th></tr>
    <tr><td>1.0</td><td>19/05/2026</td><td>Prima emissione delle specifiche tecniche AmbMMG.</td><td>Ufficio applicativo</td></tr>
  </table>
  <h2>Indice</h2>
  <table class="toc">
    <tr><td>1</td><td>Scopo e perimetro</td></tr>
    <tr><td>2</td><td>Architettura e componenti</td></tr>
    <tr><td>3</td><td>Configurazione e sicurezza</td></tr>
    <tr><td>4</td><td>Servizi applicativi ed endpoint</td></tr>
    <tr><td>5</td><td>Base dati e flussi informativi</td></tr>
    <tr><td>6</td><td>Reportistica e integrazioni</td></tr>
    <tr><td>7</td><td>Specifiche di test</td></tr>
    <tr><td>8</td><td>Rilascio, vincoli e controlli</td></tr>
  </table>
</section>
<section class="page-break">
  <h1>1. Scopo e perimetro</h1>
  <p>AmbMMG e' una web application a supporto delle attivita' ambulatoriali svolte dagli operatori autorizzati. Il sistema centralizza la consultazione degli accessi, la selezione del paziente, la gestione della scheda clinico-amministrativa, la registrazione delle prestazioni infermieristiche e la compilazione dell'Allegato M.</p>
  <p>Il progetto e' organizzato come applicazione Java Web NetBeans/Ant. La denominazione storica del repository e di alcuni file e' APO, mentre la denominazione funzionale del prodotto documentato e' AmbMMG.</p>
  <div class="note">Il perimetro del documento comprende codice sorgente, configurazione web, dipendenze applicative e comportamenti osservabili dagli endpoint. Non comprende configurazioni infrastrutturali esterne non presenti nel repository.</div>
  <h2>Funzioni coperte</h2>
  <ul>
    <li>Autenticazione LDAP/Active Directory e selezione del servizio operativo.</li>
    <li>Ricerca, inserimento ed eliminazione logica degli accessi/consulenze.</li>
    <li>Ricerca anagrafica, censimento paziente e calcolo del codice fiscale.</li>
    <li>Gestione scheda paziente con diario, patologie, allergie e terapie.</li>
    <li>Consultazione storici: prestazioni infermieristiche, ricette, piani terapeutici.</li>
    <li>Compilazione, salvataggio e stampa PDF dell'Allegato M.</li>
  </ul>
</section>
<section class="page-break">
  <h1>2. Architettura e componenti</h1>
  <p>L'applicazione segue una struttura a livelli: presentazione HTML/CSS/JavaScript, servlet HTTP per la gestione delle richieste, servizi infrastrutturali per connessione dati e LDAP, DAO dedicati all'accesso Oracle e modulo JasperReports per la produzione PDF.</p>
  <div class="diagram">""" + architecture_diagram() + """</div>
  <table>
    <tr><th>Livello</th><th>File / moduli</th><th>Responsabilita'</th></tr>
    <tr><td>Presentazione</td><td>LOGIN.html, APO.html, LOGIN.js, APO.js, APO.css, LOGIN.css</td><td>Interfaccia utente, navigazione, chiamate fetch, validazioni preliminari, rendering griglie e modali.</td></tr>
    <tr><td>Controller</td><td>AuthServlet, AccessiServlet, PazienteServlet, StampaReport</td><td>Routing delle richieste HTTP, controllo sessione, validazione parametri, risposte JSON e PDF.</td></tr>
    <tr><td>Servizi</td><td>DataService, LdapService</td><td>Creazione connessioni Oracle via JNDI/DriverManager e autenticazione LDAP.</td></tr>
    <tr><td>DAO</td><td>UserServiceDao, AccessiDao, PazienteDao</td><td>Query, insert, update e delete sugli oggetti dati applicativi.</td></tr>
    <tr><td>Report</td><td>AllegatoM2.jasper, JasperReports</td><td>Generazione del PDF Allegato M partendo dai parametri COD_CONSULENZA e COD_SERVIZIO.</td></tr>
  </table>
</section>
<section class="page-break">
  <h1>3. Configurazione e sicurezza</h1>
  <p>La configurazione applicativa e' definita nel descrittore web.xml. I parametri database e LDAP sono letti a runtime dai servlet e dai servizi. In ambiente di esercizio e' raccomandato l'uso di risorse JNDI o configurazione esterna per evitare credenziali in chiaro nei sorgenti.</p>
  <table>
    <tr><th>Parametro</th><th>Descrizione</th><th>Utilizzo</th></tr>
    <tr><td>dbDriver</td><td>Driver JDBC Oracle</td><td>DataService, StampaReport</td></tr>
    <tr><td>dbUrl</td><td>URL di connessione Oracle</td><td>Accesso ai dati applicativi e sanitari</td></tr>
    <tr><td>dbUser / dbPassword</td><td>Credenziali Oracle</td><td>Connessioni DAO e report</td></tr>
    <tr><td>ldapUrl</td><td>Endpoint LDAP aziendale</td><td>Autenticazione utente</td></tr>
    <tr><td>ldapBaseDn / ldapSearchFilter</td><td>Base e filtro di ricerca</td><td>Recupero attributi profilo</td></tr>
    <tr><td>session-timeout</td><td>Durata sessione</td><td>60 minuti</td></tr>
  </table>
  <h2>Misure applicative</h2>
  <ul>
    <li>Rigenerazione della sessione al login e invalidazione della sessione precedente.</li>
    <li>Cookie con attributi HttpOnly e SameSite=Lax.</li>
    <li>Header no-cache sulle risposte contenenti dati applicativi o sanitari.</li>
    <li>SecurityHeadersFilter applicato a tutte le URL.</li>
    <li>Controllo della sessione su tutti gli endpoint operativi.</li>
    <li>Verifica del servizio associato all'utente prima di autorizzare l'operativita'.</li>
  </ul>
  <div class="note warning">Le credenziali presenti nel descrittore devono essere sostituite da configurazioni esterne prima di un rilascio produttivo.</div>
</section>
<section class="page-break">
  <h1>4. Servizi applicativi ed endpoint</h1>
  <p>Gli endpoint espongono risposte JSON uniformi tramite ApiResponse. In caso di errore, il servlet imposta lo stato HTTP coerente e restituisce un messaggio applicativo.</p>
  <table>
    <tr><th>Area</th><th>Endpoint</th><th>Metodo</th><th>Descrizione</th></tr>
    <tr><td>Autenticazione</td><td>/api/auth/services<br>/api/auth/login<br>/api/auth/logout<br>/api/auth/session</td><td>GET/POST</td><td>Caricamento servizi, login LDAP, logout e recupero sessione.</td></tr>
    <tr><td>Servizi AmbMMG</td><td>/api/auth/apo-services<br>/api/auth/switch-service</td><td>GET/POST</td><td>Elenco e cambio servizio autorizzato.</td></tr>
    <tr><td>Accessi</td><td>/api/accessi/search<br>/api/accessi/new<br>/api/accessi/delete</td><td>GET/POST</td><td>Ricerca accessi, inserimento nuova consulenza, eliminazione logica.</td></tr>
    <tr><td>Paziente</td><td>/api/paziente/anagrafe/search<br>/anagrafe/new<br>/codice-fiscale<br>/comuni<br>/asl</td><td>GET/POST</td><td>Anagrafica, censimento e lookup di supporto.</td></tr>
    <tr><td>Scheda paziente</td><td>/api/paziente/scheda-paziente e sotto-risorse</td><td>GET/POST/DELETE</td><td>Dati sintetici, diario, patologie, allergie, terapie.</td></tr>
    <tr><td>Storici</td><td>/ricette-farmaci<br>/ricette-dematerializzate<br>/piani-terapeutici</td><td>GET</td><td>Consultazione storico clinico-prescrittivo.</td></tr>
    <tr><td>Allegato M</td><td>/api/paziente/allegato-m<br>/StampaReport</td><td>GET/POST</td><td>Caricamento, salvataggio e stampa PDF.</td></tr>
  </table>
</section>
<section class="page-break">
  <h1>5. Base dati e flussi informativi</h1>
  <p>La persistenza e' Oracle. Le query rilevate operano su viste e tabelle applicative e sanitarie. L'accesso ai dati avviene tramite PreparedStatement nella maggior parte delle chiamate parametriche e tramite metodi DAO separati per dominio funzionale.</p>
  <table>
    <tr><th>Oggetto dati</th><th>Uso applicativo</th></tr>
    <tr><td>VIEW_CONSULENZE_GMNEW, CONSULENZE_GM</td><td>Elenco, inserimento ed eliminazione logica degli accessi.</td></tr>
    <tr><td>VIEW_ANAGRAFE_DETTAGLIO_GM, TOPSAN.PAZIENTI_BUFFER</td><td>Ricerca e censimento paziente.</td></tr>
    <tr><td>APO_SCHEDE_PAZIENTE*</td><td>Scheda paziente, diario, patologie, allergie e terapie.</td></tr>
    <tr><td>PRESTAZIONI_INF_APO, PRESTAZIONI_EFFETTUATE_APO</td><td>Catalogo e registrazione delle prestazioni infermieristiche.</td></tr>
    <tr><td>ALLEGATOM</td><td>Compilazione e aggiornamento dell'Allegato M.</td></tr>
    <tr><td>SERVIZI, TESTFISIATRIAWEB.UTENTI</td><td>Autorizzazione utente e servizio operativo.</td></tr>
  </table>
  <h2>Flusso nuovo accesso</h2>
  <div class="diagram">
    <svg viewBox="0 0 720 155" width="100%" height="155">
      <rect x="15" y="45" width="110" height="45" fill="#eaf2fb" stroke="#2e75b6"/><text x="70" y="72" text-anchor="middle" font-size="13">Ricerca paziente</text>
      <rect x="155" y="45" width="120" height="45" fill="#eaf2fb" stroke="#2e75b6"/><text x="215" y="72" text-anchor="middle" font-size="13">Selezione o censimento</text>
      <rect x="305" y="45" width="120" height="45" fill="#eaf2fb" stroke="#2e75b6"/><text x="365" y="72" text-anchor="middle" font-size="13">Compilazione accesso</text>
      <rect x="455" y="45" width="110" height="45" fill="#eaf2fb" stroke="#2e75b6"/><text x="510" y="72" text-anchor="middle" font-size="13">Salvataggio DB</text>
      <rect x="595" y="45" width="110" height="45" fill="#eaf2fb" stroke="#2e75b6"/><text x="650" y="72" text-anchor="middle" font-size="13">Aggiorna griglia</text>
      <path d="M125 68 H155 M275 68 H305 M425 68 H455 M565 68 H595" stroke="#4472c4" stroke-width="3"/>
    </svg>
  </div>
</section>
<section class="page-break">
  <h1>6. Reportistica e integrazioni</h1>
  <p>La stampa dell'Allegato M e' esposta dal servlet StampaReport. Il report Jasper riceve i parametri COD_CONSULENZA e COD_SERVIZIO, apre una connessione Oracle e restituisce un PDF inline al browser.</p>
  <table>
    <tr><th>Integrazione</th><th>Descrizione</th><th>Modalita'</th></tr>
    <tr><td>LDAP / Active Directory</td><td>Autenticazione e recupero attributi dell'utente.</td><td>JNDI LDAP</td></tr>
    <tr><td>Oracle Database</td><td>Dati accessi, pazienti, servizi, storici e Allegato M.</td><td>JDBC</td></tr>
    <tr><td>JasperReports</td><td>Generazione PDF Allegato M.</td><td>Report .jasper</td></tr>
    <tr><td>Sistemi ricette</td><td>Apertura sistemi di ricette dematerializzate e farmaci.</td><td>URL esterni</td></tr>
    <tr><td>Consenso DSE e repository referti</td><td>Consultazione informazioni collegate al paziente.</td><td>URL / funzioni DB</td></tr>
  </table>
  <h2>Dipendenze applicative</h2>
  <p>Le librerie sono censite nel file sbom.json in formato CycloneDX. Le principali dipendenze rilevate sono Oracle JDBC, Gson, JasperReports, iText, Groovy, Apache Commons, JFreeChart e JCommon.</p>
</section>
<section class="page-break">
  <h1>7. Specifiche di test</h1>
  <table>
    <tr><th>ID</th><th>Caso di test</th><th>Prerequisiti</th><th>Esito atteso</th></tr>
    <tr><td>AMB-01</td><td>Login con credenziali valide</td><td>Utente LDAP abilitato a un servizio</td><td>Sessione creata e accesso alla home.</td></tr>
    <tr><td>AMB-02</td><td>Login con servizio non autorizzato</td><td>Utente non associato al servizio scelto</td><td>Errore applicativo e sessione non creata.</td></tr>
    <tr><td>AMB-03</td><td>Ricerca accessi per data e paziente</td><td>Accessi presenti in base dati</td><td>Lista filtrata e coerente con il servizio.</td></tr>
    <tr><td>AMB-04</td><td>Inserimento nuovo accesso</td><td>Paziente valido</td><td>Consulenza inserita e griglia aggiornata.</td></tr>
    <tr><td>AMB-05</td><td>Censimento paziente incompleto</td><td>Dati obbligatori mancanti</td><td>Errore sul campo mancante.</td></tr>
    <tr><td>AMB-06</td><td>Salvataggio scheda paziente</td><td>Paziente selezionato</td><td>Dati persistiti e ricaricabili.</td></tr>
    <tr><td>AMB-07</td><td>Aggiunta diario/patologia/allergia/terapia</td><td>Scheda disponibile</td><td>Elemento visibile nello storico dedicato.</td></tr>
    <tr><td>AMB-08</td><td>Prestazioni senza sottocategoria</td><td>Consulenza selezionata</td><td>Validazione bloccante.</td></tr>
    <tr><td>AMB-09</td><td>Allegato M salvato da altro utente</td><td>Record esistente con utente differente</td><td>Risposta 403 e modifica impedita.</td></tr>
    <tr><td>AMB-10</td><td>Stampa Allegato M</td><td>Parametri validi e report presente</td><td>PDF restituito al browser.</td></tr>
  </table>
  <h2>Controlli non funzionali</h2>
  <ul>
    <li>Verificare scadenza sessione e comportamento dopo logout.</li>
    <li>Verificare assenza di cache su risposte JSON sensibili.</li>
    <li>Verificare gestione di errori database e LDAP con messaggi non tecnici per l'utente.</li>
    <li>Verificare compatibilita' browser aziendale e apertura dei sistemi esterni.</li>
  </ul>
</section>
<section class="page-break">
  <h1>8. Rilascio, vincoli e controlli</h1>
  <p>Il progetto viene compilato tramite Ant/NetBeans e produce un WAR deployabile su Apache Tomcat. I sorgenti sono Java 8 e l'applicazione usa Servlet 3.0.</p>
  <table>
    <tr><th>Voce</th><th>Valore</th></tr>
    <tr><td>Versione Java</td><td>1.8</td></tr>
    <tr><td>Container</td><td>Apache Tomcat, profilo web Java EE 7</td></tr>
    <tr><td>Formato distribuzione</td><td>WAR</td></tr>
    <tr><td>DBMS</td><td>Oracle Database</td></tr>
    <tr><td>Autenticazione</td><td>LDAP / Active Directory</td></tr>
  </table>
  <div class="note">Prima del rilascio definitivo allineare display-name, nome WAR ed eventuali riferimenti visibili da APO ad AmbMMG, mantenendo solo i riferimenti storici necessari alla compatibilita' tecnica.</div>
</section>
"""


def user_body():
    return """
<section class="page-break">
  <h1>Controllo del documento</h1>
  <table class="revision">
    <tr><th>Versione</th><th>Data</th><th>Descrizione</th><th>Redazione</th></tr>
    <tr><td>1.0</td><td>19/05/2026</td><td>Prima emissione della guida utente AmbMMG.</td><td>Ufficio applicativo</td></tr>
  </table>
  <h2>Indice</h2>
  <table class="toc">
    <tr><td>1</td><td>Accesso all'applicazione</td></tr>
    <tr><td>2</td><td>Schermata Accessi</td></tr>
    <tr><td>3</td><td>Nuovo accesso e censimento paziente</td></tr>
    <tr><td>4</td><td>Scheda paziente</td></tr>
    <tr><td>5</td><td>Storici, prestazioni e Allegato M</td></tr>
    <tr><td>6</td><td>Logout, messaggi e assistenza</td></tr>
  </table>
</section>
<section class="page-break">
  <h1>1. Accesso all'applicazione</h1>
  <p>Per accedere ad AmbMMG aprire il browser aziendale e raggiungere l'indirizzo dell'applicativo. La schermata iniziale richiede utente, password e servizio operativo.</p>
  """ + login_wireframe() + """
  <h2>Passaggi operativi</h2>
  <ol>
    <li>Inserire il nome utente aziendale.</li>
    <li>Inserire la password di dominio.</li>
    <li>Attendere il caricamento dei servizi abilitati.</li>
    <li>Selezionare il servizio con cui si intende lavorare.</li>
    <li>Premere Entra.</li>
  </ol>
  <div class="note">Se il servizio non viene visualizzato, l'utente potrebbe non essere abilitato. In questo caso rivolgersi al referente applicativo.</div>
</section>
<section class="page-break">
  <h1>2. Schermata Accessi</h1>
  <p>La sezione Accessi e' la pagina principale dopo il login. Da qui e' possibile cercare le consulenze registrate, aprire il dettaglio del paziente o avviare l'inserimento di un nuovo accesso.</p>
  """ + accessi_wireframe() + """
  <table>
    <tr><th>Elemento</th><th>Descrizione</th></tr>
    <tr><td>Filtri ricerca</td><td>Permettono di filtrare per data, consulenza, cognome, nome, sede, codice fiscale e medico.</td></tr>
    <tr><td>Cerca</td><td>Esegue la ricerca applicando i filtri compilati.</td></tr>
    <tr><td>Pulisci</td><td>Azzera i filtri e ripristina la visualizzazione standard.</td></tr>
    <tr><td>Nuovo accesso</td><td>Apre la procedura guidata di ricerca paziente e inserimento della nuova consulenza.</td></tr>
    <tr><td>Griglia risultati</td><td>Mostra gli accessi trovati. La riga selezionata alimenta il pannello del paziente.</td></tr>
  </table>
</section>
<section class="page-break">
  <h1>3. Nuovo accesso e censimento paziente</h1>
  <p>Per inserire un nuovo accesso premere Nuovo accesso. Il sistema apre una finestra di ricerca paziente. Compilare almeno uno tra codice fiscale, cognome o nome, quindi premere Cerca.</p>
  <h2>Paziente presente</h2>
  <ul>
    <li>Selezionare la riga del paziente corretto.</li>
    <li>Premere Seleziona paziente.</li>
    <li>Compilare tipo accesso, diagnosi o patologia quando richiesto.</li>
    <li>Confermare con Aggiungi nuovo accesso.</li>
  </ul>
  <h2>Paziente non presente</h2>
  <p>Se il paziente non e' presente in anagrafe, utilizzare la funzione di censimento. Compilare i campi obbligatori e salvare.</p>
  <table>
    <tr><th>Campo</th><th>Obbligatorieta'</th><th>Nota</th></tr>
    <tr><td>Codice fiscale, cognome, nome</td><td>Obbligatori</td><td>Il codice fiscale deve essere di 16 caratteri.</td></tr>
    <tr><td>Data nascita, sesso, comune nascita</td><td>Obbligatori</td><td>Il comune deve essere selezionato dai suggerimenti.</td></tr>
    <tr><td>Indirizzo, comune, CAP residenza</td><td>Obbligatori</td><td>Usati per completare l'anagrafica.</td></tr>
    <tr><td>ASL residenza e ASL iscrizione</td><td>Obbligatori</td><td>Selezionare la voce corretta dall'elenco.</td></tr>
  </table>
  <div class="note">Il pulsante Calcola consente di calcolare il codice fiscale partendo dai dati anagrafici quando tutti i dati necessari sono presenti.</div>
</section>
<section class="page-break">
  <h1>4. Scheda paziente</h1>
  <p>La scheda paziente raccoglie informazioni sintetiche utili alla presa in carico. Si apre dal pannello del paziente selezionato oppure durante la procedura di nuovo accesso, quando richiesto.</p>
  <table>
    <tr><th>Sezione</th><th>Funzione</th></tr>
    <tr><td>Dati generali</td><td>Telefono, stato civile, fumo, BMI, caregiver e note generali.</td></tr>
    <tr><td>Diario</td><td>Inserimento di annotazioni datate e consultazione dello storico.</td></tr>
    <tr><td>Patologie</td><td>Aggiunta e rimozione di patologie croniche codificate ICD10.</td></tr>
    <tr><td>Allergie</td><td>Aggiunta e rimozione di allergie codificate.</td></tr>
    <tr><td>Terapie</td><td>Registrazione di principio attivo, farmaco, confezione, quantita', frequenza e durata.</td></tr>
  </table>
  <h2>Salvataggio</h2>
  <p>Dopo la modifica dei dati premere Salva scheda. Il sistema conferma l'operazione con un messaggio. In caso di errori di validazione, correggere il campo indicato e ripetere il salvataggio.</p>
</section>
<section class="page-break">
  <h1>5. Storici, prestazioni e Allegato M</h1>
  <h2>Storici del paziente</h2>
  <p>La sezione Paziente contiene schede di consultazione per gli storici collegati al codice fiscale selezionato.</p>
  <table>
    <tr><th>Scheda</th><th>Contenuto</th></tr>
    <tr><td>Prestazioni infermieristiche</td><td>Prestazioni registrate, con categorie e sottocategorie.</td></tr>
    <tr><td>Ricette dematerializzate</td><td>Elenco delle ricette dematerializzate disponibili.</td></tr>
    <tr><td>Ricette farmaci</td><td>Storico delle ricette farmaci.</td></tr>
    <tr><td>Piani terapeutici</td><td>Piani terapeutici associati al paziente.</td></tr>
  </table>
  <h2>Prestazioni infermieristiche</h2>
  <p>Aprire Prestazioni infermieristiche, selezionare categoria e sottocategorie previste per il servizio e inserire eventuali note. Il sistema richiede almeno una sottocategoria selezionata.</p>
  <h2>Allegato M</h2>
  <p>La funzione Allegato M consente di caricare, compilare e salvare il documento collegato alla consulenza. Dopo il salvataggio e' disponibile la stampa PDF.</p>
  <div class="note warning">Se l'Allegato M e' gia' stato salvato da un altro utente, il sistema blocca la modifica per evitare sovrascritture.</div>
</section>
<section class="page-break">
  <h1>6. Logout, messaggi e assistenza</h1>
  <p>Al termine del lavoro premere Logout. La sessione scade automaticamente dopo inattivita' prolungata; in caso di sessione scaduta e' necessario effettuare nuovamente l'accesso.</p>
  <table>
    <tr><th>Messaggio</th><th>Significato</th><th>Azione consigliata</th></tr>
    <tr><td>Sessione non attiva</td><td>La sessione e' scaduta o non valida.</td><td>Effettuare nuovamente il login.</td></tr>
    <tr><td>Credenziali non valide</td><td>Utente o password errati.</td><td>Verificare le credenziali aziendali.</td></tr>
    <tr><td>Valorizza almeno un filtro di ricerca</td><td>La ricerca anagrafica non ha parametri.</td><td>Inserire codice fiscale, cognome o nome.</td></tr>
    <tr><td>Campo obbligatorio non valorizzato</td><td>Manca un dato richiesto.</td><td>Compilare il campo indicato.</td></tr>
    <tr><td>Errore nel caricamento</td><td>Problema temporaneo o dato non disponibile.</td><td>Riprovare; se persiste, contattare assistenza.</td></tr>
  </table>
  <h2>Buone pratiche</h2>
  <ul>
    <li>Verificare sempre il paziente selezionato prima di inserire nuovi dati.</li>
    <li>Usare il logout quando si lascia la postazione.</li>
    <li>Non condividere credenziali personali.</li>
    <li>Segnalare anomalie indicando utente, servizio, paziente e orario dell'operazione.</li>
  </ul>
</section>
"""


def write_html_files():
    technical = doc_shell(
        "Specifiche tecniche e specifiche di test",
        "Specifiche tecniche",
        "Architettura, configurazione, integrazioni, flussi applicativi e casi di test del sistema AmbMMG.",
        technical_body(),
    )
    user = doc_shell(
        "Guida Utente",
        "Manuale operativo",
        "Istruzioni operative per l'utilizzo quotidiano del sistema AmbMMG da parte degli utenti autorizzati.",
        user_body(),
    )
    tech_html = os.path.join(OUT_DIR, "Specifiche_Tecniche_AmbMMG.html")
    user_html = os.path.join(OUT_DIR, "Guida_Utente_AmbMMG.html")
    with open(tech_html, "w", encoding="utf-8") as stream:
        stream.write(technical)
    with open(user_html, "w", encoding="utf-8") as stream:
        stream.write(user)
    return tech_html, user_html


def print_pdf(html_path, pdf_path):
    browsers = [path for path in (CHROME, EDGE) if os.path.exists(path)]
    if not browsers:
        raise RuntimeError("Browser headless non disponibile")
    last_error = None
    for browser in browsers:
        profile_dir = os.path.join(ROOT, "tmp", "ambmmg-pdf-profile-{}".format(uuid.uuid4()))
        os.makedirs(profile_dir, exist_ok=True)
        temp_pdf = os.path.join(
            OUT_DIR,
            "{}.tmp-{}.pdf".format(os.path.splitext(os.path.basename(pdf_path))[0], uuid.uuid4())
        )
        command = (
            "& '{}' --headless --disable-gpu --user-data-dir='{}' "
            "--print-to-pdf='{}' '{}'"
        ).format(
            browser.replace("'", "''"),
            profile_dir.replace("'", "''"),
            os.path.abspath(temp_pdf).replace("'", "''"),
            os.path.abspath(html_path).replace("'", "''"),
        )
        try:
            subprocess.run(["powershell", "-NoProfile", "-Command", command], check=True)
            with open(temp_pdf, "rb") as source, open(pdf_path, "wb") as target:
                target.write(source.read())
            return
        except subprocess.CalledProcessError as exc:
            last_error = exc
    raise last_error


def main():
    os.makedirs(OUT_DIR, exist_ok=True)
    with open(os.path.join(OUT_DIR, "sbom.json"), "w", encoding="utf-8") as stream:
        json.dump(build_sbom(), stream, ensure_ascii=False, indent=2)
        stream.write("\n")
    tech_html, user_html = write_html_files()
    print_pdf(tech_html, os.path.join(OUT_DIR, "Specifiche_Tecniche_AmbMMG.pdf"))
    print_pdf(user_html, os.path.join(OUT_DIR, "Guida_Utente_AmbMMG.pdf"))


if __name__ == "__main__":
    main()
