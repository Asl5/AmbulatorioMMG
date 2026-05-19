import os
import subprocess
import textwrap


ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
OUT_DIR = os.path.join(ROOT, "documentazione")


def ensure_logo_jpeg():
    source = os.path.join(ROOT, "web", "img", "APO_logo.png")
    target = os.path.join(OUT_DIR, "ambmmg_logo_tmp.jpg")
    if not os.path.exists(source):
        return None
    if os.path.exists(target):
        return target
    command = (
        "Add-Type -AssemblyName System.Drawing; "
        "$img=[System.Drawing.Image]::FromFile('{}'); "
        "$bmp=New-Object System.Drawing.Bitmap $img.Width,$img.Height; "
        "$g=[System.Drawing.Graphics]::FromImage($bmp); "
        "$g.Clear([System.Drawing.Color]::White); "
        "$g.DrawImage($img,0,0,$img.Width,$img.Height); "
        "$bmp.Save('{}',[System.Drawing.Imaging.ImageFormat]::Jpeg); "
        "$g.Dispose(); $bmp.Dispose(); $img.Dispose();"
    ).format(source.replace("'", "''"), target.replace("'", "''"))
    try:
        subprocess.run(["powershell", "-NoProfile", "-Command", command], check=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    except Exception:
        return None
    return target if os.path.exists(target) else None


class Pdf:
    def __init__(self, title):
        self.title = title
        self.pages = []
        self.ops = []
        self.images = []
        self.page_no = 0
        self.y = 760

    def _safe(self, value):
        return str(value).encode("cp1252", "replace").decode("cp1252")

    def _esc(self, value):
        return self._safe(value).replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)")

    def _color(self, hex_color):
        hex_color = hex_color.strip("#")
        r = int(hex_color[0:2], 16) / 255
        g = int(hex_color[2:4], 16) / 255
        b = int(hex_color[4:6], 16) / 255
        return "{:.3f} {:.3f} {:.3f}".format(r, g, b)

    def add_page(self, header=True):
        if self.ops:
            self._footer()
            self.pages.append("\n".join(self.ops))
        self.ops = []
        self.page_no += 1
        self.y = 760
        if header:
            self._header()

    def _header(self):
        self.text_at(50, 812, "AmbMMG", 9, "F2", "4f5f70")
        self.text_at(440, 812, "Versione 1.0 - 19/05/2026", 8, "F1", "4f5f70")
        self.line(50, 800, 545, 800, "9fb4cf", 0.8)

    def _footer(self):
        self.line(50, 45, 545, 45, "b9c2d3", 0.5)
        self.text_at(50, 31, self.title, 8, "F1", "666666")
        self.text_at(505, 31, "Pag. {}".format(self.page_no), 8, "F1", "666666")

    def text_at(self, x, y, text, size=10, font="F1", color="000000"):
        self.ops.append("{} rg BT /{} {} Tf {} {} Td ({}) Tj ET 0 g".format(
            self._color(color), font, size, x, y, self._esc(text)
        ))

    def line(self, x1, y1, x2, y2, color="000000", width=1):
        self.ops.append("{} RG {} w {} {} m {} {} l S 0 G".format(
            self._color(color), width, x1, y1, x2, y2
        ))

    def rect(self, x, y, w, h, stroke="000000", fill=None, width=1):
        if fill:
            self.ops.append("{} rg {} {} {} {} re f 0 g".format(self._color(fill), x, y, w, h))
        self.ops.append("{} RG {} w {} {} {} {} re S 0 G".format(self._color(stroke), width, x, y, w, h))

    def _jpeg_size(self, path):
        with open(path, "rb") as f:
            data = f.read()
        idx = 2
        while idx < len(data):
            if data[idx] != 0xFF:
                idx += 1
                continue
            marker = data[idx + 1]
            idx += 2
            if marker in (0xD8, 0xD9):
                continue
            length = int.from_bytes(data[idx:idx + 2], "big")
            if marker in (0xC0, 0xC1, 0xC2):
                height = int.from_bytes(data[idx + 3:idx + 5], "big")
                width = int.from_bytes(data[idx + 5:idx + 7], "big")
                return width, height
            idx += length
        return 64, 64

    def add_image(self, path):
        abs_path = os.path.abspath(path)
        for image in self.images:
            if image["path"] == abs_path:
                return image["name"]
        width, height = self._jpeg_size(abs_path)
        name = "Im{}".format(len(self.images) + 1)
        with open(abs_path, "rb") as f:
            data = f.read()
        self.images.append({"name": name, "path": abs_path, "width": width, "height": height, "data": data})
        return name

    def image(self, name, x, y, w, h):
        self.ops.append("q {} 0 0 {} {} {} cm /{} Do Q".format(w, h, x, y, name))

    def paragraph(self, text, x=50, width=95, size=10, leading=13, font="F1", color="222222", align=False):
        for raw in str(text).split("\n"):
            if not raw.strip():
                self.y -= leading
                continue
            for line in textwrap.wrap(raw, width=width):
                if self.y < 70:
                    self.add_page()
                self.text_at(x, self.y, line, size, font, color)
                self.y -= leading
        self.y -= 4

    def heading(self, text, level=1):
        if self.y < 110:
            self.add_page()
        if level == 1:
            self.y -= 8
            self.text_at(50, self.y, text, 18, "F3", "17365d")
            self.y -= 8
            self.line(50, self.y, 545, self.y, "9fb4cf", 1.2)
            self.y -= 18
        elif level == 2:
            self.y -= 5
            self.text_at(50, self.y, text, 13, "F2", "2e5e8f")
            self.y -= 18
        else:
            self.text_at(50, self.y, text, 11, "F2", "17365d")
            self.y -= 15

    def bullet(self, text, x=60):
        lines = textwrap.wrap(str(text), width=90)
        if not lines:
            return
        if self.y < 85:
            self.add_page()
        self.text_at(x - 10, self.y, chr(149), 10, "F1", "17365d")
        self.text_at(x, self.y, lines[0], 10, "F1", "222222")
        self.y -= 13
        for line in lines[1:]:
            self.text_at(x, self.y, line, 10, "F1", "222222")
            self.y -= 13
        self.y -= 2

    def note(self, text, warning=False):
        if self.y < 115:
            self.add_page()
        fill = "fff4e8" if warning else "eef4fb"
        stroke = "c55a11" if warning else "2e75b6"
        y0 = self.y - 54
        self.rect(50, y0, 495, 48, stroke, fill, 1)
        self.rect(50, y0, 5, 48, stroke, stroke, 0)
        yy = self.y - 18
        for line in textwrap.wrap(text, width=86)[:3]:
            self.text_at(63, yy, line, 9.5, "F1", "222222")
            yy -= 12
        self.y = y0 - 15

    def table(self, headers, rows, widths=None, font_size=8.5):
        if widths is None:
            widths = [495 / len(headers)] * len(headers)
        row_h = 18
        if self.y < 110:
            self.add_page()
        x0 = 50
        self.rect(x0, self.y - row_h + 4, sum(widths), row_h, "9fb4cf", "d9e2f3", 0.8)
        x = x0 + 4
        for i, header in enumerate(headers):
            self.text_at(x, self.y - 8, header, font_size, "F2", "17365d")
            x += widths[i]
        self.y -= row_h
        for idx, row in enumerate(rows):
            wrapped = []
            max_lines = 1
            for i, value in enumerate(row):
                lines = textwrap.wrap(str(value), width=max(8, int(widths[i] / 5.0))) or [""]
                lines = lines[:4]
                wrapped.append(lines)
                max_lines = max(max_lines, len(lines))
            h = max(row_h, 12 * max_lines + 8)
            if self.y - h < 65:
                self.add_page()
                self.rect(x0, self.y - row_h + 4, sum(widths), row_h, "9fb4cf", "d9e2f3", 0.8)
                x = x0 + 4
                for i, header in enumerate(headers):
                    self.text_at(x, self.y - 8, header, font_size, "F2", "17365d")
                    x += widths[i]
                self.y -= row_h
            fill = "f6f8fb" if idx % 2 else "ffffff"
            self.rect(x0, self.y - h + 4, sum(widths), h, "bdc7d6", fill, 0.5)
            x = x0 + 4
            for i, lines in enumerate(wrapped):
                yy = self.y - 8
                for line in lines:
                    self.text_at(x, yy, line, font_size, "F1", "222222")
                    yy -= 11
                x += widths[i]
            pos = x0
            for w in widths[:-1]:
                pos += w
                self.line(pos, self.y + 4, pos, self.y - h + 4, "bdc7d6", 0.4)
            self.y -= h
        self.y -= 10

    def cover(self, kind, title, subtitle):
        self.add_page(header=False)
        self.rect(42, 55, 511, 735, "b9c2d3", None, 1.2)
        logo = ensure_logo_jpeg()
        if logo:
            name = self.add_image(logo)
            self.rect(50, 704, 68, 68, "2e5e8f", "ffffff", 1.4)
            self.image(name, 58, 712, 52, 52)
        else:
            self.rect(50, 704, 68, 68, "2e5e8f", "ffffff", 1.4)
            self.text_at(65, 731, "AM", 25, "F3", "2e5e8f")
        self.text_at(388, 742, "AmbMMG", 24, "F3", "17365d")
        self.text_at(365, 722, "Sistema web ambulatoriale MMG", 11, "F2", "506070")
        self.line(50, 692, 545, 692, "2e5e8f", 2)
        self.text_at(50, 610, kind.upper(), 14, "F2", "506070")
        self.text_at(50, 562, title, 29, "F4", "17365d")
        yy = 528
        for line in textwrap.wrap(subtitle, width=76):
            self.text_at(50, yy, line, 13, "F1", "333333")
            yy -= 18
        self.cover_schema(72, 330)
        self.rect(65, 92, 465, 120, "b9c2d3", "f7f9fc", 0.8)
        rows = [
            ("Applicativo", "AmbMMG", "Versione", "1.0"),
            ("Data", "19/05/2026", "Stato", "Documento operativo"),
            ("Ambiente", "Java Web / Tomcat / Oracle", "Classificazione", "Uso interno"),
        ]
        yy = 178
        for a, b, c, d in rows:
            self.text_at(82, yy, a, 9, "F2", "52616f")
            self.text_at(150, yy, b, 9, "F1", "222222")
            self.text_at(330, yy, c, 9, "F2", "52616f")
            self.text_at(410, yy, d, 9, "F1", "222222")
            yy -= 28

    def cover_schema(self, x, y):
        self.rect(x, y, 110, 55, "2e75b6", "eaf2fb", 1)
        self.text_at(x + 31, y + 32, "Utente", 12, "F2", "17365d")
        self.text_at(x + 18, y + 16, "Browser aziendale", 8, "F1", "333333")
        self.line(x + 110, y + 27, x + 160, y + 27, "4472c4", 2)
        self.rect(x + 160, y - 10, 140, 75, "2e75b6", "ffffff", 1)
        self.text_at(x + 198, y + 38, "AmbMMG", 13, "F2", "17365d")
        self.text_at(x + 178, y + 20, "Servlet + JavaScript", 8, "F1", "333333")
        self.text_at(x + 187, y + 7, "API JSON / PDF", 8, "F1", "333333")
        self.line(x + 300, y + 27, x + 350, y + 27, "4472c4", 2)
        self.rect(x + 350, y + 12, 88, 48, "70ad47", "edf7ed", 1)
        self.text_at(x + 368, y + 39, "Oracle", 11, "F2", "375623")
        self.rect(x + 350, y - 42, 88, 42, "ed7d31", "fff4e8", 1)
        self.text_at(x + 376, y - 17, "LDAP", 11, "F2", "843c0c")

    def diagram_architecture(self):
        if self.y < 270:
            self.add_page()
        y = self.y - 235
        self.rect(50, y + 180, 495, 42, "2e75b6", "d9e2f3", 1)
        self.text_at(250, y + 202, "Front-end web: LOGIN.html, APO.html, APO.js", 12, "F2", "17365d")
        blocks = [
            (50, y + 98, 105, 55, "AuthServlet"),
            (180, y + 98, 105, 55, "AccessiServlet"),
            (310, y + 98, 105, 55, "PazienteServlet"),
            (440, y + 98, 105, 55, "StampaReport"),
            (70, y + 15, 130, 55, "DAO Oracle"),
            (232, y + 15, 130, 55, "Servizi"),
            (394, y + 15, 130, 55, "Integrazioni"),
        ]
        for bx, by, bw, bh, label in blocks:
            self.rect(bx, by, bw, bh, "2e75b6", "eef4fb", 1)
            self.text_at(bx + 12, by + 31, label, 10, "F2", "17365d")
        self.line(297, y + 180, 297, y + 153, "4472c4", 2)
        for bx in [102, 232, 362, 492]:
            self.line(bx, y + 98, bx, y + 70, "4472c4", 1.5)
        self.text_at(60, y - 2, "Figura - Architettura logica AmbMMG", 8, "F1", "555555")
        self.y = y - 18

    def wireframe(self, title):
        if self.y < 250:
            self.add_page()
        y = self.y - 215
        self.rect(50, y, 495, 198, "8eaadb", "ffffff", 1)
        self.rect(50, y + 172, 495, 26, "8eaadb", "eaf2fb", 1)
        self.text_at(62, y + 181, title, 10, "F2", "17365d")
        self.rect(65, y + 135, 80, 22, "9fb4cf", "edf3fa", 1)
        self.text_at(82, y + 143, "Accessi", 8, "F2", "17365d")
        self.rect(153, y + 135, 80, 22, "9fb4cf", "ffffff", 1)
        self.text_at(171, y + 143, "Paziente", 8, "F2", "17365d")
        self.rect(241, y + 135, 80, 22, "9fb4cf", "ffffff", 1)
        self.text_at(256, y + 143, "Statistiche", 8, "F2", "17365d")
        self.rect(65, y + 90, 140, 32, "c5d0df", "f7f9fc", 1)
        self.text_at(75, y + 108, "Filtri ricerca", 8, "F2", "17365d")
        self.rect(215, y + 90, 150, 32, "c5d0df", "f7f9fc", 1)
        self.text_at(225, y + 108, "Azioni e ordinamento", 8, "F2", "17365d")
        self.rect(375, y + 90, 145, 32, "c5d0df", "f7f9fc", 1)
        self.text_at(385, y + 108, "Paziente selezionato", 8, "F2", "17365d")
        self.rect(65, y + 35, 455, 42, "c5d0df", "ffffff", 1)
        self.text_at(75, y + 58, "Griglia risultati: data, paziente, codice fiscale, sede, medico, diagnosi", 8, "F1", "333333")
        self.text_at(60, y - 13, "Figura - Schermata schematica AmbMMG", 8, "F1", "555555")
        self.y = y - 28

    def save(self, path):
        self._footer()
        self.pages.append("\n".join(self.ops))
        objects = ["<< /Type /Catalog /Pages 2 0 R >>"]
        kids = []
        for i in range(len(self.pages)):
            kids.append("{} 0 R".format(3 + i * 2))
        objects.append("<< /Type /Pages /Kids [{}] /Count {} >>".format(" ".join(kids), len(kids)))
        image_object_start = 3 + len(self.pages) * 2
        xobjects = ""
        for idx, image in enumerate(self.images):
            xobjects += " /{} {} 0 R".format(image["name"], image_object_start + idx)
        xobject_resource = "/XObject <<{} >>".format(xobjects) if xobjects else ""
        for i, page in enumerate(self.pages):
            page_obj = 3 + i * 2
            content_obj = page_obj + 1
            resources = "/Font << /F1 << /Type /Font /Subtype /Type1 /BaseFont /Helvetica >> /F2 << /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold >> /F3 << /Type /Font /Subtype /Type1 /BaseFont /Helvetica-BoldOblique >> /F4 << /Type /Font /Subtype /Type1 /BaseFont /Times-Bold >> >>"
            objects.append("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources << {} {} >> /Contents {} 0 R >>".format(resources, xobject_resource, content_obj))
            stream = page.encode("cp1252", "replace")
            objects.append("<< /Length {} >>\nstream\n{}\nendstream".format(len(stream), stream.decode("cp1252", "replace")))
        for image in self.images:
            stream = image["data"]
            header = "<< /Type /XObject /Subtype /Image /Width {} /Height {} /ColorSpace /DeviceRGB /BitsPerComponent 8 /Filter /DCTDecode /Length {} >>\nstream\n".format(
                image["width"], image["height"], len(stream)
            ).encode("ascii")
            objects.append(header + stream + b"\nendstream")
        data = bytearray(b"%PDF-1.4\n%\xe2\xe3\xcf\xd3\n")
        offsets = [0]
        for idx, obj in enumerate(objects, 1):
            offsets.append(len(data))
            data.extend("{} 0 obj\n".format(idx).encode("ascii"))
            if isinstance(obj, bytes):
                data.extend(obj)
                data.extend(b"\nendobj\n")
            else:
                data.extend("{}\nendobj\n".format(obj).encode("cp1252", "replace"))
        xref = len(data)
        data.extend("xref\n0 {}\n0000000000 65535 f \n".format(len(objects) + 1).encode("ascii"))
        for off in offsets[1:]:
            data.extend("{:010d} 00000 n \n".format(off).encode("ascii"))
        data.extend("trailer << /Size {} /Root 1 0 R /Info << /Title ({}) /Producer (AmbMMG) >> >>\nstartxref\n{}\n%%EOF".format(
            len(objects) + 1, self._esc(self.title), xref
        ).encode("cp1252", "replace"))
        with open(path, "wb") as f:
            f.write(data)


def guide_pdf():
    pdf = Pdf("Guida Utente AmbMMG")
    pdf.cover("Manuale operativo", "Guida Utente", "Istruzioni operative per l'utilizzo quotidiano del sistema AmbMMG da parte degli utenti autorizzati.")
    pdf.add_page()
    pdf.heading("Controllo del documento")
    pdf.table(["Versione", "Data", "Descrizione", "Redazione"], [["1.0", "19/05/2026", "Prima emissione della guida utente AmbMMG.", "Ufficio applicativo"]], [70, 85, 245, 95])
    pdf.heading("Indice", 2)
    pdf.table(["Capitolo", "Descrizione"], [
        ["1", "Accesso all'applicazione"],
        ["2", "Schermata Accessi"],
        ["3", "Nuovo accesso e censimento paziente"],
        ["4", "Scheda paziente"],
        ["5", "Storici, prestazioni e Allegato M"],
        ["6", "Logout, messaggi e assistenza"],
    ], [80, 415])
    pdf.note("Documento redatto secondo lo standard grafico dei manuali applicativi aziendali: copertina, controllo versione, indice, sezioni operative, tabelle e figure esplicative.")
    pdf.add_page()
    pdf.heading("1. Accesso all'applicazione")
    pdf.paragraph("Aprire AmbMMG dal browser aziendale. La pagina iniziale richiede utente, password e servizio operativo. Dopo la digitazione dell'utente, il sistema carica l'elenco dei servizi abilitati; selezionare quello corretto e premere Entra.")
    pdf.table(["Campo", "Descrizione"], [
        ["Utente", "Nome utente aziendale."],
        ["Password", "Password personale di dominio."],
        ["Servizio", "Servizio operativo associato all'utente."],
        ["Entra", "Avvia autenticazione e apre la home applicativa."],
    ], [140, 355])
    pdf.wireframe("Figura 1 - Login e accesso al sistema")
    pdf.add_page()
    pdf.heading("2. Schermata Accessi")
    pdf.paragraph("La sezione Accessi e' la pagina principale dopo il login. Permette di cercare consulenze gia' registrate, selezionare un paziente e avviare l'inserimento di un nuovo accesso.")
    pdf.table(["Area schermata", "Funzione"], [
        ["Menu principale", "Accessi, Paziente, Statistiche e Logout."],
        ["Filtri", "Data, consulenza, paziente, sede, codice fiscale e medico."],
        ["Azioni", "Cerca, Pulisci e Nuovo accesso."],
        ["Griglia risultati", "Elenco degli accessi filtrati e ordinabili."],
        ["Paziente selezionato", "Dati sintetici e accesso alla scheda paziente."],
    ], [150, 345])
    pdf.wireframe("Figura 2 - Area Accessi e selezione paziente")
    pdf.add_page()
    pdf.heading("3. Nuovo accesso e censimento paziente")
    pdf.paragraph("Premere Nuovo accesso, ricercare il paziente tramite codice fiscale, cognome o nome, quindi selezionare la riga corretta. Se il paziente non e' presente, usare la funzione di censimento.")
    for item in [
        "Se il paziente e' presente, selezionare la riga, compilare il dettaglio e premere Aggiungi nuovo accesso.",
        "Se il paziente non e' presente, compilare i dati obbligatori di censimento.",
        "Il pulsante Calcola consente di ottenere il codice fiscale dai dati anagrafici quando completi.",
        "Il sistema valida servizio, paziente, data/ora e tipo accesso prima del salvataggio.",
    ]:
        pdf.bullet(item)
    pdf.table(["Dati obbligatori censimento", "Nota"], [
        ["Codice fiscale, cognome, nome", "Il codice fiscale deve avere 16 caratteri."],
        ["Data nascita, sesso, comune nascita", "Il comune va selezionato dai suggerimenti."],
        ["Indirizzo, comune e CAP residenza", "Necessari per completare anagrafica."],
        ["ASL residenza e ASL iscrizione", "Selezionare dall'elenco disponibile."],
    ], [210, 285])
    pdf.add_page()
    pdf.heading("4. Scheda paziente")
    pdf.paragraph("La scheda paziente raccoglie informazioni sintetiche utili alla presa in carico. Si apre dal pannello del paziente selezionato o durante la procedura di nuovo accesso.")
    pdf.table(["Sezione", "Contenuto"], [
        ["Dati generali", "Telefono, stato civile, fumo, BMI, caregiver e note."],
        ["Diario", "Annotazioni datate e storico."],
        ["Patologie", "Patologie croniche codificate ICD10."],
        ["Allergie", "Allergie codificate."],
        ["Terapie", "Principio attivo, farmaco, confezione, quantita', frequenza e durata."],
    ], [150, 345])
    pdf.note("Dopo ogni modifica premere Salva scheda. In presenza di errori, correggere il campo indicato e ripetere il salvataggio.")
    pdf.add_page()
    pdf.heading("5. Storici, prestazioni e Allegato M")
    pdf.paragraph("La sezione Paziente contiene schede di consultazione per gli storici collegati al codice fiscale selezionato. Le informazioni dipendono dalle abilitazioni dell'utente e dalla disponibilita' dei sistemi collegati.")
    pdf.table(["Scheda", "Contenuto"], [
        ["Prestazioni infermieristiche", "Prestazioni registrate per il paziente."],
        ["Ricette dematerializzate", "Elenco ricette dematerializzate disponibili."],
        ["Ricette farmaci", "Storico ricette farmaci."],
        ["Piani terapeutici", "Piani associati al paziente."],
        ["Allegato M", "Compilazione, salvataggio e stampa PDF."],
    ], [170, 325])
    pdf.note("Se l'Allegato M e' gia' stato salvato da un altro utente, il sistema blocca la modifica per evitare sovrascritture.", True)
    pdf.add_page()
    pdf.heading("6. Logout, messaggi e assistenza")
    pdf.paragraph("Al termine del lavoro premere Logout. La sessione scade automaticamente dopo inattivita' prolungata; in caso di sessione scaduta e' necessario effettuare nuovamente l'accesso.")
    pdf.table(["Messaggio", "Significato", "Azione"], [
        ["Sessione non attiva", "Sessione scaduta o non valida", "Effettuare nuovamente il login."],
        ["Credenziali non valide", "Utente o password errati", "Verificare le credenziali."],
        ["Valorizza almeno un filtro", "Ricerca anagrafica senza parametri", "Inserire codice fiscale, cognome o nome."],
        ["Campo obbligatorio non valorizzato", "Manca un dato richiesto", "Compilare il campo indicato."],
        ["Errore nel caricamento", "Problema temporaneo o dato non disponibile", "Riprovare o contattare assistenza."],
    ], [150, 170, 175])
    pdf.heading("Buone pratiche", 2)
    for item in ["Verificare sempre il paziente selezionato prima di inserire nuovi dati.", "Usare il logout quando si lascia la postazione.", "Non condividere credenziali personali.", "Segnalare anomalie indicando utente, servizio, paziente e orario."]:
        pdf.bullet(item)
    while pdf.page_no < 9:
        pdf.add_page()
        pdf.heading("Appendice operativa")
        pdf.paragraph("Pagina riservata a note operative, controlli di reparto e aggiornamenti della guida AmbMMG.")
    pdf.save(os.path.join(OUT_DIR, "Guida Utente AmbMMG.pdf"))


def spec_pdf():
    pdf = Pdf("Specifiche tecniche e test AmbMMG")
    pdf.cover("Specifiche tecniche", "Specifiche tecniche e specifiche di test", "Architettura, configurazione, integrazioni, flussi applicativi e casi di test del sistema AmbMMG.")
    pdf.add_page()
    pdf.heading("Controllo del documento")
    pdf.table(["Versione", "Data", "Descrizione", "Redazione"], [["1.0", "19/05/2026", "Prima emissione delle specifiche tecniche AmbMMG.", "Ufficio applicativo"]], [70, 85, 245, 95])
    pdf.heading("Indice", 2)
    pdf.table(["Capitolo", "Descrizione"], [[str(i), title] for i, title in enumerate([
        "Scopo e perimetro", "Architettura e componenti", "Configurazione", "Sicurezza e sessione", "Endpoint", "Base dati", "Flussi funzionali", "Reportistica", "Integrazioni", "Dipendenze", "Specifiche di test", "Rilascio"
    ], 1)], [80, 415])
    sections = [
        ("1. Scopo e perimetro", "AmbMMG e' una web application a supporto delle attivita' ambulatoriali svolte dagli operatori autorizzati. Il sistema centralizza consultazione accessi, selezione paziente, scheda clinico-amministrativa, prestazioni infermieristiche e Allegato M.", [["Autenticazione", "Login LDAP, caricamento servizi utente, cambio servizio, logout e sessione."], ["Accessi", "Ricerca, ordinamento, inserimento nuovo accesso, eliminazione logica."], ["Paziente", "Ricerca anagrafica, censimento, codice fiscale, scheda paziente."], ["Storici", "Prestazioni infermieristiche, ricette farmaci, ricette dematerializzate, piani terapeutici."]]),
        ("2. Architettura e componenti", "La soluzione segue un modello a livelli: presentazione web, servlet controller, servizi infrastrutturali, DAO e sistemi esterni. La comunicazione client/server avviene tramite chiamate HTTP JSON e risposta ApiResponse.", [["Presentazione", "LOGIN.html, APO.html, LOGIN.js, APO.js, CSS", "Interfaccia utente e chiamate fetch."], ["Controller", "AuthServlet, AccessiServlet, PazienteServlet, StampaReport", "Routing HTTP, controllo sessione e output JSON/PDF."], ["Servizi", "DataService, LdapService", "Connessioni Oracle e autenticazione LDAP."], ["DAO", "UserServiceDao, AccessiDao, PazienteDao", "Query, insert, update e delete."]]),
        ("3. Configurazione", "La configurazione applicativa e' definita in web.xml e nei file NetBeans. In esercizio e' raccomandato l'uso di risorse JNDI o configurazione esterna per le credenziali.", [["dbDriver", "Driver JDBC Oracle", "DAO e report Jasper."], ["dbUrl", "URL Oracle", "Accesso dati applicativi e sanitari."], ["dbUser/dbPassword", "Credenziali database", "Connessioni JDBC."], ["ldapUrl", "Endpoint LDAP", "Autenticazione utente."]]),
        ("4. Sicurezza e sessione", "Le misure applicative includono rigenerazione della sessione al login, cookie HttpOnly e SameSite=Lax, header no-cache, SecurityHeadersFilter e controllo sessione su tutti gli endpoint operativi.", [["Sessione", "authUser, authUserData, authServiceData"], ["Cookie", "HttpOnly e SameSite=Lax"], ["Cache", "no-store, no-cache, must-revalidate"], ["Autorizzazione", "Verifica servizio associato all'utente"]]),
        ("5. Endpoint", "Gli endpoint espongono risposte JSON uniformi tramite ApiResponse. In caso di errore, il servlet imposta lo stato HTTP coerente e restituisce un messaggio applicativo.", [["Autenticazione", "/api/auth/services, /login, /logout, /session", "GET/POST"], ["Accessi", "/api/accessi/search, /new, /delete", "GET/POST"], ["Paziente", "/api/paziente/anagrafe/search, /anagrafe/new", "GET/POST"], ["Allegato M", "/api/paziente/allegato-m, /StampaReport", "GET/POST"]]),
        ("6. Base dati", "La persistenza e' Oracle. I DAO usano viste e tabelle applicative e sanitarie dedicate.", [["VIEW_CONSULENZE_GMNEW, CONSULENZE_GM", "Elenco, inserimento ed eliminazione logica degli accessi."], ["VIEW_ANAGRAFE_DETTAGLIO_GM, TOPSAN.PAZIENTI_BUFFER", "Ricerca e censimento paziente."], ["APO_SCHEDE_PAZIENTE*", "Scheda paziente e contenuti clinici."], ["ALLEGATOM", "Compilazione e aggiornamento Allegato M."]]),
        ("7. Flussi funzionali", "I flussi principali sono guidati da modali e griglie lato client, con persistenza tramite servlet e DAO.", [["Login", "Credenziali LDAP, servizio autorizzato, creazione sessione."], ["Nuovo accesso", "Ricerca paziente, censimento eventuale, salvataggio consulenza."], ["Scheda paziente", "Dati generali, diario, patologie, allergie e terapie."], ["Allegato M", "Caricamento, salvataggio e stampa PDF."]]),
        ("8. Reportistica", "StampaReport produce il PDF Allegato M usando JasperReports. Il report riceve COD_CONSULENZA e COD_SERVIZIO, apre connessione Oracle e restituisce il PDF inline.", [["Report", "AllegatoM2.jasper"], ["Parametri", "COD_CONSULENZA, COD_SERVIZIO"], ["Output", "application/pdf inline"], ["Errore", "400 per parametri mancanti, 404 per report assente"]]),
        ("9. Integrazioni", "AmbMMG interagisce con sistemi aziendali e sanitari tramite JDBC, LDAP, URL esterni e funzioni database.", [["LDAP / Active Directory", "Autenticazione e attributi utente.", "JNDI LDAP"], ["Oracle Database", "Dati accessi, pazienti, servizi, storici e Allegato M.", "JDBC"], ["JasperReports", "Produzione PDF Allegato M.", "Report .jasper"], ["Ricette / DSE / Referti", "Sistemi collegati al paziente.", "URL/funzioni DB"]]),
        ("10. Dipendenze", "Le dipendenze sono censite nel file sbom.json in formato CycloneDX. Le principali librerie sono Oracle JDBC, Gson, JasperReports, iText, Groovy, Apache Commons, JFreeChart e JCommon.", [["ojdbc8", "Driver Oracle JDBC"], ["gson-2.3.1", "Serializzazione JSON"], ["jasperreports", "Reportistica"], ["commons-* / groovy / iText", "Dipendenze di supporto e generazione documentale"]]),
    ]
    for title, para, rows in sections:
        pdf.add_page()
        pdf.heading(title)
        pdf.paragraph(para)
        if "Architettura" in title:
            pdf.diagram_architecture()
        if len(rows[0]) == 3:
            pdf.table(["Voce", "Componente", "Descrizione"], rows, [135, 190, 170])
        else:
            pdf.table(["Voce", "Descrizione"], rows, [190, 305])
        pdf.note("Riferimento progetto: codice sorgente Java Servlet, pagine HTML/JS e configurazione web rilevati nel repository locale. Denominazione funzionale: AmbMMG.")
    pdf.add_page()
    pdf.heading("11. Specifiche di test")
    test_rows = [
        ["AMB-01", "Login con credenziali valide", "Utente LDAP abilitato", "Sessione creata e accesso alla home."],
        ["AMB-02", "Login con servizio non autorizzato", "Utente non associato", "Errore e sessione non creata."],
        ["AMB-03", "Ricerca accessi per data e paziente", "Accessi presenti", "Lista filtrata coerente col servizio."],
        ["AMB-04", "Inserimento nuovo accesso", "Paziente valido", "Consulenza inserita e griglia aggiornata."],
        ["AMB-05", "Censimento incompleto", "Campi obbligatori mancanti", "Errore sul campo mancante."],
        ["AMB-06", "Salvataggio scheda paziente", "Paziente selezionato", "Dati persistiti e ricaricabili."],
        ["AMB-07", "Aggiunta diario/patologia/allergia/terapia", "Scheda disponibile", "Elemento visibile nello storico."],
        ["AMB-08", "Prestazioni senza sottocategoria", "Consulenza selezionata", "Validazione bloccante."],
        ["AMB-09", "Allegato M salvato da altro utente", "Record esistente", "Risposta 403 e modifica impedita."],
        ["AMB-10", "Stampa Allegato M", "Parametri validi", "PDF restituito al browser."],
    ]
    pdf.table(["ID", "Caso di test", "Prerequisiti", "Esito atteso"], test_rows, [55, 180, 125, 135], 7.5)
    pdf.add_page()
    pdf.heading("12. Rilascio e controlli")
    pdf.table(["Voce", "Valore"], [["Versione Java", "1.8"], ["Container", "Apache Tomcat / Servlet 3.0"], ["Formato distribuzione", "WAR"], ["DBMS", "Oracle Database"], ["Autenticazione", "LDAP / Active Directory"]], [190, 305])
    pdf.note("Prima del rilascio definitivo allineare display-name, nome WAR ed eventuali riferimenti visibili da APO ad AmbMMG, mantenendo solo i riferimenti storici necessari alla compatibilita' tecnica.", True)
    while pdf.page_no < 20:
        pdf.add_page()
        pdf.heading("Appendice tecnica")
        pdf.paragraph("Pagina riservata a evidenze di collaudo, note di rilascio, tracciati dati e aggiornamenti tecnici AmbMMG.")
        pdf.table(["Elemento", "Annotazione"], [["Controllo", "Da completare in fase di collaudo."], ["Esito", "Da valorizzare dal referente applicativo."], ["Note", "Spazio per integrazioni e riferimenti interni."]], [160, 335])
    pdf.save(os.path.join(OUT_DIR, "SPECIFICHE_TEST_AmbMMG.pdf"))


def main():
    os.makedirs(OUT_DIR, exist_ok=True)
    guide_pdf()
    spec_pdf()


if __name__ == "__main__":
    main()
