function getApoContextPath() {
    const path = window.location.pathname || "/";
    const routeSuffixes = ["/APO.html", "/LOGIN.html", "/Login"];
    let contextPath = path;

    routeSuffixes.some(function (suffix) {
        if (contextPath.toLowerCase().endsWith(suffix.toLowerCase())) {
            contextPath = contextPath.substring(0, contextPath.length - suffix.length);
            return true;
        }
        return false;
    });

    if (contextPath.length > 1 && contextPath.endsWith("/")) {
        contextPath = contextPath.substring(0, contextPath.length - 1);
    }

    return contextPath === "/" ? "" : contextPath;
}

function buildApoUrl(path) {
    const normalizedPath = String(path || "").replace(/^\/+/, "");
    return getApoContextPath() + "/" + normalizedPath;
}

function getApoLandingUrl() {
    const contextPath = getApoContextPath();
    return contextPath || "/";
}

function normalizeApoLandingUrlInBrowser() {
    const landingUrl = getApoLandingUrl();
    if (landingUrl === "/") {
        return;
    }

    const currentPath = window.location.pathname || "";
    const canonicalPath = landingUrl;
    const landingPathWithSlash = canonicalPath + "/";
    const landingFilePath = canonicalPath + "/APO.html";
    if (currentPath !== landingPathWithSlash && currentPath.toLowerCase() !== landingFilePath.toLowerCase()) {
        return;
    }

    window.history.replaceState(null, document.title, canonicalPath + window.location.search + window.location.hash);
}

const apoSessionUrl = buildApoUrl("api/auth/session");
const apoLogoutUrl = buildApoUrl("api/auth/logout");
const apoServicesUrl = buildApoUrl("api/auth/apo-services");
const apoSwitchServiceUrl = buildApoUrl("api/auth/switch-service");
const apoAccessiUrl = buildApoUrl("api/accessi/search");
const apoMedicoCuranteUrl = buildApoUrl("api/paziente/medico-curante");
const apoConsensoDseUrl = buildApoUrl("api/paziente/consenso-dse");
const apoAnagrafeSearchUrl = buildApoUrl("api/paziente/anagrafe/search");
const apoAnagrafeNewUrl = buildApoUrl("api/paziente/anagrafe/new");
const apoCodiceFiscaleUrl = buildApoUrl("api/paziente/codice-fiscale");
const apoComuniSuggestUrl = buildApoUrl("api/paziente/comuni");
const apoAslSuggestUrl = buildApoUrl("api/paziente/asl");
const apoPrestazioniInfermieristicheUrl = buildApoUrl("api/paziente/prestazioni-infermieristiche");
const apoPrestazioniInfCategorieUrl = buildApoUrl("api/paziente/prestazioni-infermieristiche/categorie");
const apoPrestazioniInfSottocategorieUrl = buildApoUrl("api/paziente/prestazioni-infermieristiche/sottocategorie");
const apoPatologieCronicheIcd10Url = buildApoUrl("api/paziente/icd10/patologie-croniche");
const apoAllergieUrl = buildApoUrl("api/paziente/allergie");
const apoTerapiePrincipiAttiviUrl = buildApoUrl("api/paziente/terapie/principi-attivi");
const apoTerapieFarmaciUrl = buildApoUrl("api/paziente/terapie/farmaci");
const apoTerapieConfezioniUrl = buildApoUrl("api/paziente/terapie/confezioni");
const apoSchedaPazienteUrl = buildApoUrl("api/paziente/scheda-paziente");
const apoSchedaPazienteDiarioUrl = buildApoUrl("api/paziente/scheda-paziente/diario");
const apoSchedaPazientePatologieUrl = buildApoUrl("api/paziente/scheda-paziente/patologie");
const apoSchedaPazienteAllergieUrl = buildApoUrl("api/paziente/scheda-paziente/allergie");
const apoSchedaPazienteTerapieUrl = buildApoUrl("api/paziente/scheda-paziente/terapie");
const apoRicetteFarmaciStoricoUrl = buildApoUrl("api/paziente/ricette-farmaci");
const apoRicetteDematerializzateStoricoUrl = buildApoUrl("api/paziente/ricette-dematerializzate");
const apoPianiTerapeuticiUrl = buildApoUrl("api/paziente/piani-terapeutici");
const apoAllegatoMUrl = buildApoUrl("api/paziente/allegato-m");
const apoNewAccessoUrl = buildApoUrl("api/accessi/new");
const apoDeleteAccessoUrl = buildApoUrl("api/accessi/delete");
const apoLoginUrl = buildApoUrl("Login");
const apoRicettaDematerializzataUrl = "https://pvtopric.asl5sp.int/Ricette2026/";
const apoRicettaFarmaciUrl = "https://pvtopric.asl5sp.int/Top_Ricetta/RicettaFarmaci";
const apoCertificatoMalattiaUrl = "https://sistemats1.sanita.finanze.it/portale/area-riservata-operatore";
const apoRepositoryRefertiUrl = "http://10.105.106.230:8080/whale/autoLogin";
const apoConsensoDseInvokeUrl = "http://consenso-dse/Invoke";

const apoState = {
    sessionData: null,
    apoServices: [],
    apoServicesLoading: false,
    apoServicesLoaded: false,
    apoServicesError: "",
    apoServiceSwitching: false,
    accessi: [],
    filteredAccessi: [],
    storicoAccessi: [],
    storicoCodiceFiscale: "",
    storicoLoading: false,
    storicoLoaded: false,
    storicoError: "",
    storicoRequestId: 0,
    prestazioniInfermieristiche: [],
    prestazioniInfermieristicheCodiceFiscale: "",
    prestazioniInfermieristicheLoading: false,
    prestazioniInfermieristicheLoaded: false,
    prestazioniInfermieristicheError: "",
    prestazioniInfermieristicheRequestId: 0,
    prestazioniInfCategorie: [],
    prestazioniInfCategorieCodServizio: "",
    prestazioniInfCategorieLoading: false,
    prestazioniInfCategorieLoaded: false,
    prestazioniInfCategorieError: "",
    prestazioniInfSottocategorie: [],
    prestazioniInfSottocategorieKey: "",
    prestazioniInfSottocategorieLoading: false,
    prestazioniInfSottocategorieLoaded: false,
    prestazioniInfSottocategorieError: "",
    prestazioniInfCategoria: "",
    prestazioniInfSottocategorieSelezionate: [],
    prestazioniInfNote: "",
    prestazioniInfMessage: "",
    prestazioniInfMessageType: "info",
    prestazioniInfSaving: false,
    ricetteFarmaci: [],
    ricetteFarmaciCodiceFiscale: "",
    ricetteFarmaciLoading: false,
    ricetteFarmaciLoaded: false,
    ricetteFarmaciError: "",
    ricetteFarmaciRequestId: 0,
    ricetteDematerializzate: [],
    ricetteDematerializzateCodiceFiscale: "",
    ricetteDematerializzateLoading: false,
    ricetteDematerializzateLoaded: false,
    ricetteDematerializzateError: "",
    ricetteDematerializzateRequestId: 0,
    pianiTerapeutici: [],
    pianiTerapeuticiCodiceFiscale: "",
    pianiTerapeuticiLoading: false,
    pianiTerapeuticiLoaded: false,
    pianiTerapeuticiError: "",
    pianiTerapeuticiRequestId: 0,
    allegatoM: null,
    allegatoMKey: "",
    allegatoMLoading: false,
    allegatoMLoaded: false,
    allegatoMError: "",
    allegatoMRequestId: 0,
    medicoCurante: null,
    medicoCuranteCodiceFiscale: "",
    medicoCuranteLoading: false,
    medicoCuranteLoaded: false,
    medicoCuranteError: "",
    medicoCuranteRequestId: 0,
    consensoDseHtml: "",
    consensoDseCodiceFiscale: "",
    consensoDseLoading: false,
    consensoDseLoaded: false,
    consensoDseError: "",
    consensoDseRequestId: 0,
    selectedAccessoId: "",
    activeSection: "accessi",
    activePazienteSection: "diagnosi",
    activePazienteHistoryTab: "scheda",
    activeSchedaPazienteHistorySection: "diario",
    activeStoricoTab: "accessi",
    selectFirstStoricoAccessoOnLoad: false,
    pendingStoricoAccessoId: "",
    storicoConfirmLastFocus: null,
    nuovoAccessoResults: [],
    nuovoAccessoSelectedId: "",
    nuovoAccessoSelectedTipi: ["visita-medica"],
    nuovoAccessoPatologia: "ortopedica",
    nuovoAccessoLoading: false,
    nuovoAccessoError: "",
    nuovoAccessoLastFocus: null,
    nuovoAccessoDetailLastFocus: null,
    schedaPazienteLastFocus: null,
    schedaPazienteDiarioModalLastFocus: null,
    schedaPazienteDiarioModalCodPaz: "",
    schedaPazienteExistingRequestId: 0,
    schedaPazienteSaving: false,
    schedaPazientePendingAccessoPayload: null,
    schedaPazientePendingPatient: null,
    schedaPazienteCaregiverResults: [],
    schedaPazientePatologieSource: [],
    schedaPazientePatologieLoading: false,
    schedaPazientePatologieLoaded: false,
    schedaPazientePatologieError: "",
    schedaPazientePatologieCache: {},
    schedaPazientePatologieResults: [],
    schedaPazientePatologieSelected: [],
    schedaPazientePatologieRequestId: 0,
    schedaPazienteAllergieSource: [],
    schedaPazienteAllergieLoading: false,
    schedaPazienteAllergieLoaded: false,
    schedaPazienteAllergieError: "",
    schedaPazienteAllergieResults: [],
    schedaPazienteAllergieSelected: [],
    schedaPazienteAllergieRequestId: 0,
    schedaPazienteTerapieSelected: [],
    schedaPazienteHistoryTerapie: [],
    schedaPazienteHistoryTerapieCodPaz: "",
    schedaPazienteHistoryTerapieLoading: false,
    schedaPazienteHistoryTerapieLoaded: false,
    schedaPazienteHistoryTerapieError: "",
    schedaPazienteHistoryTerapieRequestId: 0,
    schedaPazienteTerapiaModalLastFocus: null,
    schedaPazienteTerapiaModalCodPaz: "",
    schedaPazienteTerapiaModalAccesso: null,
    schedaPazienteDiario: [],
    schedaPazienteDiarioCodPaz: "",
    schedaPazienteDiarioLoading: false,
    schedaPazienteDiarioLoaded: false,
    schedaPazienteDiarioError: "",
    schedaPazienteDiarioRequestId: 0,
    schedaPazienteHistoryPatologie: [],
    schedaPazienteHistoryPatologieCodPaz: "",
    schedaPazienteHistoryPatologieLoading: false,
    schedaPazienteHistoryPatologieLoaded: false,
    schedaPazienteHistoryPatologieError: "",
    schedaPazienteHistoryPatologieRequestId: 0,
    schedaPazienteHistoryAllergie: [],
    schedaPazienteHistoryAllergieCodPaz: "",
    schedaPazienteHistoryAllergieLoading: false,
    schedaPazienteHistoryAllergieLoaded: false,
    schedaPazienteHistoryAllergieError: "",
    schedaPazienteHistoryAllergieRequestId: 0,
    pendingDeleteAccessoId: "",
    deleteConfirmLastFocus: null,
    deleteConsulenzaLoading: false,
    searchToastTimer: null,
    detailOpen: false,
    isLoading: false,
    pendingAction: null, // New state variable: can be 'selectAccesso' or 'openAllegatoM'
    mainAccessoId: "",
    lastAccessoClickId: "",
    lastAccessoClickAt: 0,
    censimentoLastFocus: null,
    censimentoSaving: false,
    censimentoComuni: [],
    censimentoComuniLoaded: false,
    censimentoComuniLoading: false,
    censimentoAsl: [],
    censimentoAslLoaded: false,
    censimentoAslLoading: false
};

const apoStoricoTabs = [
    {id: "accessi", label: "Storico accessi"},
    {id: "ricette-dem", label: "Ricetta dematerializzata"},
    {id: "ricette-farm", label: "Ricette farmaci"},
    {id: "prestazioni-inf", label: "Prestazioni Infermieristiche"}
];

const apoSchedaPazienteHistorySections = [
    {id: "diario", label: "Diario"},
    {id: "patologie-croniche", label: "Patologie croniche"},
    {id: "allergie", label: "Allergie"},
    {id: "terapie", label: "Terapie in corso"}
];

const apoSchedaPazienteTerapieFrequenzaOptions = [
    {value: "giorno", label: "giorno"},
    {value: "settimana", label: "settimana"},
    {value: "mese", label: "mese"}
];

const apoSchedaPazienteTerapieDurataOptions = [
    {value: "giorni", label: "giorni"},
    {value: "settimane", label: "settimane"},
    {value: "mesi", label: "mesi"}
];

const apoPazienteSections = [
    {id: "diagnosi", label: "Diagnosi"},
    {id: "prestazioni-infermieristiche", label: "Prestazioni infermieristiche"},
    {id: "allegato-m", label: "Allegato M"},
    {id: "ricetta-specialistica", label: "Ricetta dematerializzata"},
    {id: "ricetta-farmaci", label: "Ricetta farmaci"},
    {id: "certificato-malattia", label: "Certificato malattia"},
    {id: "repository-referti", label: "Repository referti"},
    {id: "piani-terapeutici", label: "Piani terapeutici"},
    {id: "richiesta-prestazioni", label: "Richiesta prestazioni"},
    {id: "elimina-consulenza", label: "Elimina consulenza"},
    {id: "cruscotto-consenso", label: "Cruscotto consenso"}
];

const apoStoricoConsulenzaDisabledSections = [
    "ricetta-specialistica",
    "ricetta-farmaci",
    "certificato-malattia",
    "richiesta-prestazioni"
];

const apoCensimentoLookupFields = [
    {
        inputId: "apo-censimento-comune-nascita",
        hiddenId: "apo-censimento-cod-comune-nascita",
        listType: "comuni"
    },
    {
        inputId: "apo-censimento-comune-residenza",
        hiddenId: "apo-censimento-cod-comune-residenza",
        listType: "comuni"
    },
    {
        inputId: "apo-censimento-comune-domicilio",
        hiddenId: "apo-censimento-cod-comune-domicilio",
        listType: "comuni"
    },
    {
        inputId: "apo-censimento-asl-residenza",
        hiddenId: "apo-censimento-cod-asl-residenza",
        listType: "asl"
    },
    {
        inputId: "apo-censimento-asl-iscrizione",
        hiddenId: "apo-censimento-cod-asl-iscrizione",
        listType: "asl"
    }
];

function normalizeApoValue(value) {
    return String(value || "").trim().toUpperCase();
}

function normalizeApoDisplayValue(value) {
    return String(value || "").trim();
}

function getApoFilterValue(id) {
    const element = document.getElementById(id);
    return normalizeApoValue(element ? element.value : "");
}

function getApoSortField() {
    const field = document.getElementById("apo-sort-field");
    return String(field ? field.value : "data").trim() || "data";
}

function getApoSortDirection() {
    const direction = document.getElementById("apo-sort-direction");
    return String(direction ? direction.value : "desc").trim() === "asc" ? "asc" : "desc";
}

function getApoCurrentDate() {
    const now = new Date();
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, "0");
    const day = String(now.getDate()).padStart(2, "0");
    return year + "-" + month + "-" + day;
}

function getApoCurrentTime() {
    const now = new Date();
    const hours = String(now.getHours()).padStart(2, "0");
    const minutes = String(now.getMinutes()).padStart(2, "0");
    return hours + ":" + minutes;
}

function getApoNumericValue(value) {
    const match = String(value || "").match(/\d+/);
    return match ? parseInt(match[0], 10) : NaN;
}

function getApoAgeForDate(year, month, day) {
    const now = new Date();
    let age = now.getFullYear() - year;
    const currentMonth = now.getMonth() + 1;
    const currentDay = now.getDate();
    if (currentMonth < month || (currentMonth === month && currentDay < day)) {
        age -= 1;
    }
    return age;
}

function inferApoFourDigitYear(twoDigitYear, month, day, eta) {
    const yearNumber = parseInt(twoDigitYear, 10);
    if (String(twoDigitYear || "").length === 4) {
        return yearNumber;
    }

    const currentYear = new Date().getFullYear();
    const currentTwoDigitYear = currentYear % 100;
    const ageValue = getApoNumericValue(eta);
    const candidates = [1900 + yearNumber, 2000 + yearNumber];
    if (!Number.isNaN(ageValue)) {
        return candidates.reduce(function (bestYear, candidateYear) {
            const bestDistance = Math.abs(getApoAgeForDate(bestYear, month, day) - ageValue);
            const candidateDistance = Math.abs(getApoAgeForDate(candidateYear, month, day) - ageValue);
            return candidateDistance < bestDistance ? candidateYear : bestYear;
        }, candidates[0]);
    }

    return yearNumber <= currentTwoDigitYear ? 2000 + yearNumber : 1900 + yearNumber;
}

function formatApoAnagrafeDate(value, eta) {
    const rawValue = normalizeApoDisplayValue(value);
    if (!rawValue) {
        return "";
    }

    const monthMap = {
        GEN: "01",
        FEB: "02",
        MAR: "03",
        APR: "04",
        MAG: "05",
        GIU: "06",
        LUG: "07",
        AGO: "08",
        SET: "09",
        OTT: "10",
        NOV: "11",
        DIC: "12",
        JAN: "01",
        MAY: "05",
        JUN: "06",
        JUL: "07",
        AUG: "08",
        SEP: "09",
        OCT: "10",
        DEC: "12"
    };

    const isoMatch = rawValue.match(/^(\d{4})-(\d{1,2})-(\d{1,2})(?:[ T]\d{1,2}:\d{2}(?::\d{2})?(?:\.\d+)?)?$/);
    if (isoMatch) {
        return isoMatch[3].padStart(2, "0") + "/" + isoMatch[2].padStart(2, "0") + "/" + isoMatch[1];
    }

    const alphaMatch = rawValue.toUpperCase().match(/^(\d{1,2})-([A-Z]{3})-(\d{2}|\d{4})$/);
    if (alphaMatch) {
        const day = alphaMatch[1].padStart(2, "0");
        const month = monthMap[alphaMatch[2]];
        if (!month) {
            return rawValue;
        }
        const year = inferApoFourDigitYear(alphaMatch[3], parseInt(month, 10), parseInt(day, 10), eta);
        return day + "/" + month + "/" + year;
    }

    const numericMatch = rawValue.match(/^(\d{1,2})\/(\d{1,2})\/(\d{2}|\d{4})$/);
    if (numericMatch) {
        const day = numericMatch[1].padStart(2, "0");
        const month = numericMatch[2].padStart(2, "0");
        const year = inferApoFourDigitYear(numericMatch[3], parseInt(month, 10), parseInt(day, 10), eta);
        return day + "/" + month + "/" + year;
    }

    return rawValue;
}

function setApoSessionMessage(message) {
    const box = document.getElementById("session-message");
    if (!box) {
        return;
    }

    const text = String(message || "").trim();
    box.textContent = text;
    box.classList.toggle("d-none", text === "");
}

function setApoSearchMessage(message, type) {
    const box = document.getElementById("apo-page-toast");
    if (!box) {
        return;
    }

    if (apoState.searchToastTimer) {
        window.clearTimeout(apoState.searchToastTimer);
        apoState.searchToastTimer = null;
    }

    const text = String(message || "").trim();
    if (!text) {
        box.classList.add("apo-page-toast-hidden");
        return;
    }

    const normalizedType = String(type || "info").trim().toLowerCase();
    const toastType = normalizedType === "danger" || normalizedType === "error" ? "danger"
        : normalizedType === "warning" ? "warning"
        : normalizedType === "success" ? "success"
        : "info";

    box.className = "apo-page-toast apo-page-toast-" + toastType + " apo-page-toast-hidden";
    box.textContent = text;
    box.setAttribute("role", toastType === "danger" ? "alert" : "status");

    window.requestAnimationFrame(function () {
        box.classList.remove("apo-page-toast-hidden");
    });

    apoState.searchToastTimer = window.setTimeout(function () {
        box.classList.add("apo-page-toast-hidden");
        apoState.searchToastTimer = null;
    }, 5000);
}

function setApoSearchBusy(isBusy) {
    const searchButton = document.getElementById("apo-search-button");
    const clearButton = document.getElementById("apo-clear-button");
    const sortField = document.getElementById("apo-sort-field");
    const sortDirection = document.getElementById("apo-sort-direction");
    const busy = isBusy === true;

    apoState.isLoading = busy;

    if (searchButton) {
        searchButton.disabled = busy;
        searchButton.textContent = busy ? "Ricerca..." : "Cerca";
    }
    if (clearButton) {
        clearButton.disabled = busy;
    }
    if (sortField) {
        sortField.disabled = busy;
    }
    if (sortDirection) {
        sortDirection.disabled = busy;
    }
}

function createApoSpinnerLoader(labelText, extraClass) {
    const loader = document.createElement("div");
    loader.className = "apo-spinner-loader" + (extraClass ? " " + extraClass : "");
    loader.setAttribute("role", "status");
    loader.setAttribute("aria-live", "polite");

    const spinner = document.createElement("span");
    spinner.className = "apo-spinner-loader-icon";
    spinner.setAttribute("aria-hidden", "true");

    const label = document.createElement("span");
    label.className = "apo-spinner-loader-text";
    label.textContent = normalizeApoDisplayValue(labelText) || "Caricamento...";

    loader.appendChild(spinner);
    loader.appendChild(label);
    return loader;
}

function getApoAccessiParams() {
    const params = new URLSearchParams();
    const urlParams = new URLSearchParams(window.location.search);
    
    const filterDate = String(document.getElementById("apo-filter-date") ? document.getElementById("apo-filter-date").value : "").trim();
    const dateOperator = String(document.getElementById("apo-filter-date-operator") ? document.getElementById("apo-filter-date-operator").value : "=").trim();
    const cognome = getApoFilterValue("apo-filter-paziente-cognome");
    const nome = getApoFilterValue("apo-filter-paziente-nome");
    const codiceFiscale = getApoFilterValue("apo-filter-codice-fiscale");
    const accettazione = urlParams.get("accettazione");

    if (filterDate) {
        params.set("data", filterDate);
        params.set("dataOperator", dateOperator || "=");
    }
    if (cognome) {
        params.set("cognome", cognome);
    }
    if (nome) {
        params.set("nome", nome);
    }
    if (codiceFiscale) {
        params.set("codiceFiscale", codiceFiscale);
    }
    if (accettazione) {
        params.set("consulenza", accettazione);
    }

    return params;
}

function normalizeApoAccesso(item, index) {
    const source = item && typeof item === "object" ? item : {};
    const cognome = normalizeApoDisplayValue(source.cognome);
    const nome = normalizeApoDisplayValue(source.nome);
    const paziente = normalizeApoDisplayValue(source.paziente || (cognome + " " + nome).trim());
    const eta = normalizeApoDisplayValue(source.eta);

    return {
        id: normalizeApoDisplayValue(source.id || ("ROW_" + index)),
        codConsulenze: normalizeApoDisplayValue(source.codConsulenze || source.codConsulenza),
        consulenza: normalizeApoDisplayValue(source.consulenza || source.codConsulenze || source.codConsulenza),
        sede: normalizeApoDisplayValue(source.sede || source.descServizio || source.codServizio),
        dataDisplay: normalizeApoDisplayValue(source.dataDisplay || source.dataConsulenze),
        dataOra: normalizeApoDisplayValue(source.dataOra || source.dataDisplay || source.dataConsulenze),
        orarioConsulenza: normalizeApoDisplayValue(source.orarioConsulenza),
        dataIso: normalizeApoDisplayValue(source.dataIso),
        codFiscale: normalizeApoDisplayValue(source.codFiscale),
        paziente: paziente,
        cognome: cognome,
        nome: nome,
        dataNascita: formatApoAnagrafeDate(source.dataNascita, eta),
        operatore: normalizeApoDisplayValue(source.operatore || source.medico || source.modUtente),
        codServizio: normalizeApoDisplayValue(source.codServizio),
        descServizio: normalizeApoDisplayValue(source.descServizio),
        valore: normalizeApoDisplayValue(source.valore),
        patologia: normalizeApoDisplayValue(source.patologia),
        note: normalizeApoDisplayValue(source.note),
        eta: eta,
        codPaz: normalizeApoDisplayValue(source.codPaz),
        pin: normalizeApoDisplayValue(source.pin),
        modUtente: normalizeApoDisplayValue(source.modUtente),
        medico: normalizeApoDisplayValue(source.medico),
        fuoriReg: normalizeApoDisplayValue(source.fuoriReg),
        codPrestazioneNote: normalizeApoDisplayValue(source.codPrestazioneNote)
    };
}

function normalizeApoAccessi(items) {
    if (!Array.isArray(items)) {
        return [];
    }

    return items.map(function (item, index) {
        return normalizeApoAccesso(item, index);
    });
}

function normalizeApoAnagrafePaziente(item, index) {
    const source = item && typeof item === "object" ? item : {};
    const codFiscale = normalizeApoDisplayValue(source.codFiscale);
    const eta = normalizeApoDisplayValue(source.eta);
    const paziente = {
        id: "ANAGRAFE_" + index,
        cognome: normalizeApoDisplayValue(source.cognome),
        nome: normalizeApoDisplayValue(source.nome),
        paziente: normalizeApoDisplayValue(source.paziente),
        dataNascita: formatApoAnagrafeDate(source.dataNascita, eta),
        codFiscale: codFiscale,
        sesso: normalizeApoDisplayValue(source.sesso),
        eta: eta,
        codComuneRes: normalizeApoDisplayValue(source.codComuneRes || source.codComRes || source.codComumeRes),
        codPaz: normalizeApoDisplayValue(source.codPaz),
        pin: normalizeApoDisplayValue(source.pin)
    };

    Object.keys(source).forEach(function (key) {
        if (!Object.prototype.hasOwnProperty.call(paziente, key)) {
            paziente[key] = normalizeApoDisplayValue(source[key]);
        }
    });

    return paziente;
}

function normalizeApoAnagrafePazienti(items) {
    if (!Array.isArray(items)) {
        return [];
    }

    return items.map(function (item, index) {
        return normalizeApoAnagrafePaziente(item, index);
    });
}

function normalizeApoPrestazioneInfermieristica(item, index) {
    const source = item && typeof item === "object" ? item : {};
    return {
        id: normalizeApoDisplayValue(source.id || ("PRESTAZIONE_" + index)),
        codServizio: normalizeApoDisplayValue(source.codServizio),
        descrizione: normalizeApoDisplayValue(source.descrizione),
        dataIns: normalizeApoDisplayValue(source.dataIns),
        dataInsSort: normalizeApoDisplayValue(source.dataInsSort),
        operatore: normalizeApoDisplayValue(source.operatore),
        descOperatore: normalizeApoDisplayValue(source.descOperatore || source.operatore),
        idConsulenza: normalizeApoDisplayValue(source.idConsulenza),
        codPrestazione: normalizeApoDisplayValue(source.codPrestazione),
        categoria: normalizeApoDisplayValue(source.categoria),
        sottoCategoria: normalizeApoDisplayValue(source.sottoCategoria || source.sottocategoria || source.sotto_categoria),
        prestazione: normalizeApoDisplayValue(source.prestazione)
    };
}

function normalizeApoPrestazioniInfermieristiche(items) {
    if (!Array.isArray(items)) {
        return [];
    }

    return items.map(function (item, index) {
        return normalizeApoPrestazioneInfermieristica(item, index);
    });
}

function normalizeApoPrestazioniInfCategorie(items) {
    if (!Array.isArray(items)) {
        return [];
    }

    return items.map(function (item) {
        const source = item && typeof item === "object" ? item : {};
        return normalizeApoDisplayValue(source.categoria || item);
    }).filter(function (categoria) {
        return categoria !== "";
    });
}

function normalizeApoPrestazioniInfSottocategorie(items) {
    if (!Array.isArray(items)) {
        return [];
    }

    return items.map(function (item, index) {
        const source = item && typeof item === "object" ? item : {};
        const id = normalizeApoDisplayValue(source.id || ("SOTTOCATEGORIA_" + index));
        const sottoCategoria = normalizeApoDisplayValue(source.sottoCategoria || source.sottocategoria || source.sotto_categoria);
        return {
            id: id,
            sottoCategoria: sottoCategoria
        };
    }).filter(function (item) {
        return item.id !== "" || item.sottoCategoria !== "";
    });
}

function normalizeApoRicettaFarmaco(item, index) {
    const source = item && typeof item === "object" ? item : {};
    return {
        id: normalizeApoDisplayValue(source.codRicetta || source.ricetta || ("RICETTA_FARMACO_" + index)),
        codRicetta: normalizeApoDisplayValue(source.codRicetta),
        codServizio: normalizeApoDisplayValue(source.codServizio),
        descrizione: normalizeApoDisplayValue(source.descrizione),
        cfMedico: normalizeApoDisplayValue(source.cfMedico),
        medico: normalizeApoDisplayValue(source.medico),
        ricetta: normalizeApoDisplayValue(source.ricetta),
        codAccettazione: normalizeApoDisplayValue(source.codAccettazione),
        dataRicetta: normalizeApoDisplayValue(source.dataRicetta),
        dataRicettaSort: normalizeApoDisplayValue(source.dataRicettaSort),
        farmaco: normalizeApoDisplayValue(source.farmaco),
        codEsenzione: normalizeApoDisplayValue(source.codEsenzione)
    };
}

function normalizeApoRicetteFarmaci(items) {
    if (!Array.isArray(items)) {
        return [];
    }

    return items.map(function (item, index) {
        return normalizeApoRicettaFarmaco(item, index);
    });
}

function normalizeApoRicettaDematerializzata(item, index) {
    const source = item && typeof item === "object" ? item : {};
    return {
        id: normalizeApoDisplayValue(source.codRichiesta || source.ricetta || ("RICETTA_DEM_" + index)),
        codRichiesta: normalizeApoDisplayValue(source.codRichiesta),
        codAccettazione: normalizeApoDisplayValue(source.codAccettazione),
        codServizio: normalizeApoDisplayValue(source.codServizio),
        descrizione: normalizeApoDisplayValue(source.descrizione),
        cfMedico: normalizeApoDisplayValue(source.cfMedico),
        medico: normalizeApoDisplayValue(source.medico),
        ricetta: normalizeApoDisplayValue(source.ricetta),
        dataRicetta: normalizeApoDisplayValue(source.dataRicetta),
        dataRicettaSort: normalizeApoDisplayValue(source.dataRicettaSort),
        esame: normalizeApoDisplayValue(source.esame),
        codEsenzione: normalizeApoDisplayValue(source.codEsenzione)
    };
}

function normalizeApoRicetteDematerializzate(items) {
    if (!Array.isArray(items)) {
        return [];
    }

    return items.map(function (item, index) {
        return normalizeApoRicettaDematerializzata(item, index);
    });
}

function normalizeApoPianoTerapeutico(item, index) {
    const source = item && typeof item === "object" ? item : {};
    return {
        id: normalizeApoDisplayValue(source.codPiano || ("PIANO_TERAPEUTICO_" + index)),
        codPiano: normalizeApoDisplayValue(source.codPiano),
        servizio: normalizeApoDisplayValue(source.servizio),
        dataPiano: normalizeApoDisplayValue(source.dataPiano),
        dataPianoSort: normalizeApoDisplayValue(source.dataPianoSort),
        dataScadenza: normalizeApoDisplayValue(source.dataScadenza),
        dataScadenzaSort: normalizeApoDisplayValue(source.dataScadenzaSort),
        medicoCurante: normalizeApoDisplayValue(source.medicoCurante),
        durata: normalizeApoDisplayValue(source.durata),
        note: normalizeApoDisplayValue(source.note),
        sostanza: normalizeApoDisplayValue(source.sostanza),
        farmaco: normalizeApoDisplayValue(source.farmaco),
        dosaggio: normalizeApoDisplayValue(source.dosaggio),
        unitaPosologica: normalizeApoDisplayValue(source.unitaPosologica),
        posologia: normalizeApoDisplayValue(source.posologia),
        diagnosi: normalizeApoDisplayValue(source.diagnosi)
    };
}

function normalizeApoPianiTerapeutici(items) {
    if (!Array.isArray(items)) {
        return [];
    }

    return items.map(function (item, index) {
        return normalizeApoPianoTerapeutico(item, index);
    });
}

function normalizeApoAllegatoM(item) {
    const source = item && typeof item === "object" ? item : {};
    const allegato = {};

    Object.keys(source).forEach(function (key) {
        allegato[key] = normalizeApoDisplayValue(source[key]);
    });

    return allegato;
}

function normalizeApoServiceOption(item) {
    const source = item && typeof item === "object" ? item : {};
    return {
        codServizio: normalizeApoDisplayValue(source.codServizio),
        descrizione: normalizeApoDisplayValue(source.descrizione)
    };
}

function normalizeApoServiceOptions(items) {
    if (!Array.isArray(items)) {
        return [];
    }

    return items.map(function (item) {
        return normalizeApoServiceOption(item);
    }).filter(function (item) {
        return item.codServizio;
    });
}

function formatApoLoggedService(payload) {
    const data = payload && typeof payload === "object" ? payload : {};
    const serviceData = data.serviceData && typeof data.serviceData === "object" ? data.serviceData : {};
    const description = String(serviceData.descrizione || "").trim();

    if (description) {
        return description;
    }
    return "Servizio non disponibile";
}

function setApoHiddenValue(id, value) {
    const input = document.getElementById(id);
    if (input) {
        input.value = normalizeApoDisplayValue(value);
    }
}

function updateApoHiddenSessionValues(payload) {
    const data = payload && typeof payload === "object" ? payload : {};
    const serviceData = data.serviceData && typeof data.serviceData === "object" ? data.serviceData : {};

    setApoHiddenValue("cf_utente", serviceData.codFiscale);
    setApoHiddenValue("servizio", serviceData.codServizio);
    setApoHiddenValue("gruppo_utente", serviceData.codGruppo);
    setApoHiddenValue("qualifica", serviceData.codQualifica);
}

function normalizeApoUserDataValue(value) {
    if (Array.isArray(value)) {
        for (let index = 0; index < value.length; index += 1) {
            const normalized = normalizeApoDisplayValue(value[index]);
            if (normalized) {
                return normalized;
            }
        }
        return "";
    }
    return normalizeApoDisplayValue(value);
}

function getApoObjectDisplayValue(source, keys) {
    if (!source || typeof source !== "object" || !Array.isArray(keys)) {
        return "";
    }

    for (let keyIndex = 0; keyIndex < keys.length; keyIndex += 1) {
        const directKey = keys[keyIndex];
        if (Object.prototype.hasOwnProperty.call(source, directKey)) {
            const directValue = normalizeApoUserDataValue(source[directKey]);
            if (directValue) {
                return directValue;
            }
        }
    }

    const sourceKeys = Object.keys(source);
    for (let keyIndex = 0; keyIndex < keys.length; keyIndex += 1) {
        const normalizedKey = String(keys[keyIndex]).toLowerCase();
        for (let sourceIndex = 0; sourceIndex < sourceKeys.length; sourceIndex += 1) {
            const sourceKey = sourceKeys[sourceIndex];
            if (sourceKey.toLowerCase() === normalizedKey) {
                const sourceValue = normalizeApoUserDataValue(source[sourceKey]);
                if (sourceValue) {
                    return sourceValue;
                }
            }
        }
    }

    return "";
}

function getApoNameInitial(value) {
    const normalized = normalizeApoDisplayValue(value);
    return normalized ? normalized.charAt(0).toUpperCase() : "";
}

function getApoInitialsFromText(value) {
    const parts = normalizeApoDisplayValue(value).split(/\s+/).filter(function (part) {
        return !!part;
    });
    if (!parts.length) {
        return "";
    }
    return parts.slice(0, 2).map(function (part) {
        return getApoNameInitial(part);
    }).join("");
}

function getApoLoggedUserInfo(payload) {
    const data = payload && typeof payload === "object" ? payload : {};
    const userData = data.userData && typeof data.userData === "object" ? data.userData : {};
    const username = normalizeApoDisplayValue(data.username);
    const cognome = getApoObjectDisplayValue(userData, ["sn", "surname", "cognome"]);
    const nome = getApoObjectDisplayValue(userData, ["givenName", "nome"]);
    const displayName = getApoObjectDisplayValue(userData, ["displayName", "cn", "name"]);
    const matricola = getApoObjectDisplayValue(userData, ["matricola", "employeeNumber", "employeeID", "employeeId"]);
    const account = getApoObjectDisplayValue(userData, ["sAMAccountName", "samAccountName", "uid", "userPrincipalName"]);
    const fullName = normalizeApoDisplayValue([cognome, nome].filter(function (part) {
        return !!part;
    }).join(" ")) || displayName || username || "Utente";
    const userCode = matricola || account || username || "-";
    const initials = (getApoNameInitial(cognome) + getApoNameInitial(nome))
        || getApoInitialsFromText(displayName || fullName)
        || getApoNameInitial(username)
        || "--";

    return {
        fullName: fullName,
        identifier: (matricola ? "Matricola: " : "") + userCode,
        initials: initials
    };
}

function renderApoLoggedUserCard(payload) {
    const card = document.getElementById("gestfarm-user-card");
    const avatar = document.getElementById("gestfarm-user-avatar");
    const name = document.getElementById("gestfarm-user-name");
    const code = document.getElementById("gestfarm-user-code");
    const info = getApoLoggedUserInfo(payload);

    if (avatar) {
        avatar.textContent = info.initials;
    }
    if (name) {
        name.textContent = info.fullName;
    }
    if (code) {
        code.textContent = info.identifier;
    }
    if (card) {
        card.title = info.fullName + " - " + info.identifier;
    }
}

function getApoHiddenValue(id) {
    const input = document.getElementById(id);
    return normalizeApoValue(input ? input.value : "");
}

function updateApoSelectedConsulenzaHiddenFields(accesso) {
    setApoHiddenValue("cod_servizio_consulenza", normalizeApoDisplayValue(accesso && accesso.codServizio));
}

function applyApoSessionData(response) {
    const payload = response && response.data ? response.data : {};
    const label = document.getElementById("gestfarm-service-label");
    const logoutButton = document.getElementById("logout-button");

    apoState.sessionData = payload;
    if (Array.isArray(payload.apoServices)) {
        apoState.apoServices = normalizeApoServiceOptions(payload.apoServices);
        apoState.apoServicesLoaded = true;
        apoState.apoServicesLoading = false;
        apoState.apoServicesError = "";
    }
    updateApoHiddenSessionValues(payload);
    renderApoLoggedUserCard(payload);
    syncApoPazienteMenuForUser();
    if (apoState.activeSection === "paziente") {
        renderApoPazienteBody();
    }

    if (label) {
        label.textContent = formatApoLoggedService(payload);
    }
    renderApoServiceSelect();
    if (!apoState.apoServicesLoaded && !apoState.apoServicesLoading) {
        void loadApoServiceOptions();
    }
    if (logoutButton) {
        logoutButton.disabled = false;
    }
}

function getApoCurrentServiceData() {
    const sessionData = getApoSessionData();
    return sessionData.serviceData && typeof sessionData.serviceData === "object" ? sessionData.serviceData : {};
}

function getApoCurrentServiceCode() {
    const serviceData = getApoCurrentServiceData();
    return normalizeApoDisplayValue(serviceData.codServizio || getApoHiddenValue("servizio"));
}

function formatApoServiceSelectLabel(description, fallback) {
    const text = normalizeApoDisplayValue(description);
    const cleanedText = text.replace(/^CASA DELLA COMUNITA['’]\s*-\s*/i, "CDC ").trim();
    return cleanedText || normalizeApoDisplayValue(fallback);
}

function renderApoServiceSelect() {
    const select = document.getElementById("apo-filter-sede");
    if (!select) {
        return;
    }

    const serviceData = getApoCurrentServiceData();
    const currentCode = getApoCurrentServiceCode();
    const normalizedCurrentCode = normalizeApoValue(currentCode);
    const currentDescription = formatApoServiceSelectLabel(serviceData.descrizione, currentCode);
    let hasCurrentService = false;

    select.innerHTML = "";

    apoState.apoServices.forEach(function (service) {
        const code = normalizeApoDisplayValue(service.codServizio);
        const normalizedCode = normalizeApoValue(code);
        if (!code) {
            return;
        }

        const option = document.createElement("option");
        option.value = code;
        option.textContent = formatApoServiceSelectLabel(service.descrizione, code);
        select.appendChild(option);
        if (normalizedCode === normalizedCurrentCode) {
            hasCurrentService = true;
        }
    });

    if (currentCode && !hasCurrentService) {
        const currentOption = document.createElement("option");
        currentOption.value = currentCode;
        currentOption.textContent = currentDescription;
        select.insertBefore(currentOption, select.firstChild);
    }

    if (!select.options.length) {
        const option = document.createElement("option");
        option.value = "";
        option.textContent = apoState.apoServicesLoading ? "Caricamento sedi..." : "Sedi non disponibili";
        select.appendChild(option);
    }

    select.value = currentCode;
    select.disabled = apoState.apoServicesLoading || apoState.apoServiceSwitching || !currentCode || !select.options.length;
}

async function loadApoServiceOptions() {
    apoState.apoServicesLoading = true;
    apoState.apoServicesError = "";
    renderApoServiceSelect();

    try {
        const response = await fetch(apoServicesUrl, {
            headers: {
                "Accept": "application/json"
            }
        });

        if (response.status === 401) {
            window.location.href = apoLoginUrl;
            return;
        }

        let payload = null;
        try {
            payload = await response.json();
        } catch (error) {
            payload = null;
        }

        if (!response.ok || !payload || payload.esito !== "ok") {
            throw new Error(payload && payload.message ? payload.message : "Errore nel caricamento delle sedi.");
        }

        apoState.apoServices = normalizeApoServiceOptions(payload.data && payload.data.services);
        apoState.apoServicesLoaded = true;
    } catch (error) {
        apoState.apoServices = [];
        apoState.apoServicesError = error && error.message ? error.message : "Errore nel caricamento delle sedi.";
        setApoSearchMessage(apoState.apoServicesError, "danger");
    } finally {
        apoState.apoServicesLoading = false;
        renderApoServiceSelect();
    }
}

async function switchApoService(serviceCode) {
    const normalizedServiceCode = normalizeApoDisplayValue(serviceCode);
    const currentServiceCode = getApoCurrentServiceCode();
    if (!normalizedServiceCode || normalizeApoValue(normalizedServiceCode) === normalizeApoValue(currentServiceCode)) {
        renderApoServiceSelect();
        return;
    }

    const params = new URLSearchParams();
    params.set("serviceCode", normalizedServiceCode);
    apoState.apoServiceSwitching = true;
    renderApoServiceSelect();
    setApoSearchMessage("Cambio sede in corso...", "info");

    try {
        const response = await fetch(apoSwitchServiceUrl, {
            method: "POST",
            headers: {
                "Accept": "application/json",
                "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8"
            },
            body: params.toString()
        });

        if (response.status === 401) {
            window.location.href = apoLoginUrl;
            return;
        }

        let payload = null;
        try {
            payload = await response.json();
        } catch (error) {
            payload = null;
        }

        if (!response.ok || !payload || payload.esito !== "ok") {
            throw new Error(payload && payload.message ? payload.message : "Errore nel cambio sede.");
        }

        const redirectUrl = payload.data && payload.data.redirectUrl ? payload.data.redirectUrl : getApoLandingUrl();
        window.location.href = redirectUrl;
    } catch (error) {
        apoState.apoServiceSwitching = false;
        renderApoServiceSelect();
        setApoSearchMessage(error && error.message ? error.message : "Errore nel cambio sede.", "danger");
    }
}

async function loadApoSessionState() {
    try {
        const response = await fetch(apoSessionUrl, {
            headers: {
                "Accept": "application/json"
            }
        });

        if (response.status === 401) {
            window.location.href = apoLoginUrl;
            return;
        }

        if (!response.ok) {
            throw new Error("Errore nel caricamento della sessione.");
        }

        // Verifica che la risposta sia effettivamente JSON per evitare l'errore "Unexpected token <"
        const contentType = response.headers.get("content-type");
        if (!contentType || !contentType.includes("application/json")) {
            throw new Error("Risposta del server non valida (ricevuto HTML invece di JSON). Verifica il percorso dell'applicazione o la sessione.");
        }

        const payload = await response.json();
        if (payload && payload.esito === "ok") {
            setApoSessionMessage("");
            applyApoSessionData(payload);
            return;
        }

        throw new Error("Impossibile verificare la sessione attiva.");
    } catch (error) {
        setApoSessionMessage(error && error.message ? error.message : "Errore nel caricamento della sessione.");
        const label = document.getElementById("gestfarm-service-label");
        if (label) {
            label.textContent = "Servizio non disponibile";
        }
    }
}

async function logoutFromApo() {
    const button = document.getElementById("logout-button");

    if (button) {
        button.disabled = true;
        button.textContent = "Uscita...";
    }

    try {
        await fetch(apoLogoutUrl, {
            method: "POST",
            headers: {
                "Accept": "application/json"
            }
        });
    } catch (error) {
    } finally {
        window.location.href = apoLoginUrl;
    }
}

function compareApoValues(first, second, direction) {
    if (first === second) {
        return 0;
    }
    if (direction === "asc") {
        return first > second ? 1 : -1;
    }
    return first < second ? 1 : -1;
}

function getApoSortValue(item, field) {
    if (field === "data") {
        return String(item.dataIso || "").trim() + "|" + String(item.dataDisplay || "").trim();
    }
    if (field === "consulenza") {
        return item.consulenza;
    }
    if (field === "sede") {
        return item.sede;
    }
    if (field === "codiceFiscale") {
        return item.codFiscale;
    }
    if (field === "paziente") {
        return item.paziente;
    }
    if (field === "dataNascita") {
        const parts = String(item.dataNascita || "").split("/");
        return parts.length === 3 ? parts[2] + "-" + parts[1] + "-" + parts[0] : String(item.dataNascita || "");
    }
    if (field === "operatore") {
        return item.operatore;
    }
    return "";
}

function sortApoAccessi(items) {
    const field = getApoSortField();
    const direction = getApoSortDirection();

    return items.slice().sort(function (left, right) {
        return compareApoValues(getApoSortValue(left, field), getApoSortValue(right, field), direction);
    });
}

function updateApoAccessiTitle() {
    const title = document.getElementById("apo-accessi-title");
    if (!title) {
        return;
    }

    title.textContent = "Accessi: " + apoState.filteredAccessi.length;
}

function getApoPazienteSectionConfig(sectionId) {
    const normalizedId = String(sectionId || "").trim().toLowerCase();

    for (let index = 0; index < apoPazienteSections.length; index += 1) {
        if (apoPazienteSections[index].id === normalizedId) {
            return apoPazienteSections[index];
        }
    }

    return apoPazienteSections[0];
}

function getApoLoggedRoleValue(fieldName, hiddenId) {
    const sessionData = getApoSessionData();
    const serviceData = sessionData.serviceData && typeof sessionData.serviceData === "object" ? sessionData.serviceData : {};
    return normalizeApoValue(serviceData[fieldName] || getApoHiddenValue(hiddenId));
}

function isApoLoggedInfermiere() {
    return getApoLoggedRoleValue("codQualifica", "qualifica") === "INF"
        && getApoLoggedRoleValue("codGruppo", "gruppo_utente") === "INF";
}

function isApoMainConsulenzaSelected() {
    const accesso = getApoSelectedAccesso();
    const mainAccesso = getApoAccessoById(apoState.mainAccessoId);
    return !!(accesso && mainAccesso && isSameApoAccesso(accesso, mainAccesso));
}

function isApoStoricoConsulenzaSelected() {
    const accesso = getApoSelectedAccesso();
    const mainAccesso = getApoAccessoById(apoState.mainAccessoId);
    return !!(accesso && mainAccesso && !isSameApoAccesso(accesso, mainAccesso));
}

function isApoStoricoConsulenzaSectionDisabled(sectionId) {
    const normalizedId = String(sectionId || "").trim().toLowerCase();
    return apoStoricoConsulenzaDisabledSections.indexOf(normalizedId) !== -1
        && isApoStoricoConsulenzaSelected();
}

function isApoPazienteSectionVisible(sectionId) {
    const normalizedId = String(sectionId || "").trim().toLowerCase();
    if (normalizedId === "prestazioni-infermieristiche") {
        return isApoLoggedInfermiere();
    }
    if (isApoLoggedInfermiere()) {
        return normalizedId === "diagnosi"
            || normalizedId === "elimina-consulenza"
            || normalizedId === "cruscotto-consenso";
    }
    return true;
}

function isApoPazienteSectionAllowed(sectionId) {
    return isApoPazienteSectionVisible(sectionId)
        && !isApoStoricoConsulenzaSectionDisabled(sectionId);
}

function isApoCurrentDiagnosiCompiled() {
    const accesso = getApoSelectedAccesso();
    const diagnosi = normalizeApoDisplayValue(accesso && accesso.note);
    return !!diagnosi && diagnosi !== "-";
}

function isApoCurrentAllegatoMCompiled() {
    const accesso = getApoSelectedAccesso();
    const allegatoMKey = getApoAllegatoMKey(accesso);
    return !!(allegatoMKey
        && apoState.allegatoMKey === allegatoMKey
        && apoState.allegatoMLoaded
        && !apoState.allegatoMError
        && apoState.allegatoM
        && typeof apoState.allegatoM === "object"
        && Object.keys(apoState.allegatoM).length > 0);
}

function isApoCurrentPianiTerapeuticiCompiled() {
    const accesso = getApoSelectedAccesso();
    const codiceFiscale = normalizeApoValue(accesso && accesso.codFiscale);
    return !!(codiceFiscale
        && apoState.pianiTerapeuticiCodiceFiscale === codiceFiscale
        && apoState.pianiTerapeuticiLoaded
        && !apoState.pianiTerapeuticiError
        && apoState.pianiTerapeutici.length > 0);
}

function hasApoPazienteSectionStatusCheck(sectionId) {
    const normalizedId = String(sectionId || "").trim().toLowerCase();
    if (normalizedId === "diagnosi") {
        return isApoCurrentDiagnosiCompiled();
    }
    if (normalizedId === "allegato-m") {
        return isApoCurrentAllegatoMCompiled();
    }
    if (normalizedId === "piani-terapeutici") {
        return isApoCurrentPianiTerapeuticiCompiled();
    }
    return false;
}

function isApoPazienteMenuStatusLoading() {
    const accesso = getApoSelectedAccesso();
    const codiceFiscale = normalizeApoValue(accesso && accesso.codFiscale);
    const allegatoMKey = getApoAllegatoMKey(accesso);

    if (!accesso) {
        return false;
    }

    if (allegatoMKey && (apoState.allegatoMLoading
            || apoState.allegatoMKey !== allegatoMKey
            || (!apoState.allegatoMLoaded && !apoState.allegatoMError))) {
        return true;
    }

    if (codiceFiscale && (apoState.pianiTerapeuticiLoading
            || apoState.pianiTerapeuticiCodiceFiscale !== codiceFiscale
            || (!apoState.pianiTerapeuticiLoaded && !apoState.pianiTerapeuticiError))) {
        return true;
    }

    if (codiceFiscale && (apoState.consensoDseLoading
            || apoState.consensoDseCodiceFiscale !== codiceFiscale
            || (!apoState.consensoDseLoaded && !apoState.consensoDseError))) {
        return true;
    }

    return false;
}

function syncApoPazienteMenuLoader() {
    const menu = document.getElementById("apo-paziente-menu");
    if (!menu) {
        return;
    }

    let loader = document.getElementById("apo-paziente-menu-loader");
    if (!loader) {
        loader = createApoSpinnerLoader("Aggiornamento stati...", "apo-paziente-menu-loader");
        loader.id = "apo-paziente-menu-loader";
        menu.insertBefore(loader, menu.firstChild);
    }

    loader.hidden = !isApoPazienteMenuStatusLoading();
}

function getApoAllowedPazienteSectionConfig(sectionId) {
    const config = getApoPazienteSectionConfig(sectionId);
    if (isApoPazienteSectionAllowed(config.id)) {
        return config;
    }
    return getApoPazienteSectionConfig("diagnosi");
}

function updateApoPazienteMenuActiveState(sectionId) {
    const activeId = String(sectionId || "").trim();
    const buttons = document.querySelectorAll("#apo-paziente-menu button[data-paziente-section]");

    buttons.forEach(function (button) {
        const buttonSection = String(button.getAttribute("data-paziente-section") || "").trim();
        const isActive = buttonSection === activeId;
        button.classList.toggle("is-active", isActive);
        button.setAttribute("aria-current", isActive ? "page" : "false");
    });
}

function syncApoPazienteMenuForUser() {
    const buttons = document.querySelectorAll("#apo-paziente-menu button[data-paziente-section]");

    if (!isApoPazienteSectionAllowed(apoState.activePazienteSection)) {
        apoState.activePazienteSection = "diagnosi";
    }

    buttons.forEach(function (button) {
        const sectionId = String(button.getAttribute("data-paziente-section") || "").trim();
        const isVisible = isApoPazienteSectionVisible(sectionId);
        const isAllowed = isApoPazienteSectionAllowed(sectionId);
        const isDisabledForStorico = isApoStoricoConsulenzaSectionDisabled(sectionId);
        const hasStatusCheck = hasApoPazienteSectionStatusCheck(sectionId);
        const wrapper = sectionId === "cruscotto-consenso" ? button.closest(".apo-paziente-consenso-box") : button;
        if (wrapper) {
            wrapper.classList.toggle("is-hidden", !isVisible);
        }
        button.disabled = !isAllowed;
        button.classList.toggle("is-disabled-storico", isDisabledForStorico);
        button.classList.toggle("has-status-check", hasStatusCheck);
        button.setAttribute("aria-disabled", isAllowed ? "false" : "true");
        if (isDisabledForStorico) {
            button.title = "Azione non disponibile per una consulenza dallo storico.";
        } else {
            button.removeAttribute("title");
        }
    });
    syncApoPazienteMenuLoader();
    updateApoPazienteMenuActiveState(apoState.activePazienteSection);
}

function renderApoPazienteBody() {
    const title = document.getElementById("apo-paziente-title");
    const titleMain = document.getElementById("apo-paziente-title-main");
    const body = document.getElementById("apo-paziente-body");
    const config = getApoAllowedPazienteSectionConfig(apoState.activePazienteSection);
    const accesso = getApoSelectedAccesso();

    apoState.activePazienteSection = config.id;
    updateApoSelectedConsulenzaHiddenFields(accesso);
    syncApoPazienteMenuForUser();

    if (title) {
        renderApoPazienteTitle(title, config);
    }
    if (titleMain) {
        renderApoPazienteTitleMain(titleMain);
    }
    if (!body) {
        return;
    }

    body.innerHTML = "";

    const stage = document.createElement("div");
    stage.className = "apo-paziente-stage";

    const sheet = document.createElement("article");
    sheet.className = "apo-paziente-sheet";

    if (config.id === "diagnosi") {
        sheet.classList.add("apo-diagnosi-sheet");
        renderApoDiagnosiSheet(sheet, accesso);
        stage.appendChild(sheet);
        body.appendChild(stage);
        return;
    }

    if (config.id === "prestazioni-infermieristiche") {
        renderApoPrestazioniInfermieristicheSheet(sheet);
        stage.appendChild(sheet);
        body.appendChild(stage);
        return;
    }

    if (config.id === "allegato-m") {
        renderApoAllegatoMSheet(sheet, accesso);
        stage.appendChild(sheet);
        body.appendChild(stage);
        return;
    }

    if (config.id === "ricetta-specialistica") {
        renderApoRicettaLinkSheet(sheet, "dematerializzata");
        stage.appendChild(sheet);
        body.appendChild(stage);
        return;
    }

    if (config.id === "ricetta-farmaci") {
        renderApoRicettaLinkSheet(sheet, "farmaci");
        stage.appendChild(sheet);
        body.appendChild(stage);
        return;
    }

    if (config.id === "certificato-malattia") {
        renderApoCertificatoMalattiaLinkSheet(sheet);
        stage.appendChild(sheet);
        body.appendChild(stage);
        return;
    }

    if (config.id === "repository-referti") {
        renderApoRepositoryRefertiLinkSheet(sheet);
        stage.appendChild(sheet);
        body.appendChild(stage);
        return;
    }

    if (config.id === "cruscotto-consenso") {
        renderApoConsensoDseLinkSheet(sheet);
        stage.appendChild(sheet);
        body.appendChild(stage);
        return;
    }

    if (config.id === "piani-terapeutici") {
        renderApoPianiTerapeuticiSheet(sheet, accesso);
        stage.appendChild(sheet);
        body.appendChild(stage);
        return;
    }

    if (config.id === "richiesta-prestazioni") {
        const emptyState = document.createElement("div");
        emptyState.className = "empty-state";
        emptyState.textContent = "Funzionalità ancora non disponibile.";
        sheet.appendChild(emptyState);
        stage.appendChild(sheet);
        body.appendChild(stage);
        return;
    }

    const subtitle = document.createElement("p");
    subtitle.className = "apo-paziente-sheet-subtitle";
    subtitle.textContent = "Corpo della sezione selezionata.";

    const emptyState = document.createElement("div");
    emptyState.className = "empty-state";
    emptyState.textContent = "Nessun oggetto disponibile per " + config.label + ".";

    sheet.appendChild(subtitle);
    sheet.appendChild(emptyState);
    stage.appendChild(sheet);
    body.appendChild(stage);
}

function getApoSessionData() {
    return apoState.sessionData && typeof apoState.sessionData === "object" ? apoState.sessionData : {};
}

function getApoLoggedUsername() {
    const sessionData = getApoSessionData();
    return normalizeApoDisplayValue(sessionData.username);
}

function getApoLoggedServiceCode() {
    const sessionData = getApoSessionData();
    const serviceData = sessionData.serviceData && typeof sessionData.serviceData === "object" ? sessionData.serviceData : {};
    return normalizeApoDisplayValue(serviceData.codServizio);
}

function getApoPrestazioniInfSottocategorieKey(codServizio, categoria) {
    return normalizeApoValue(codServizio) + "|" + normalizeApoValue(categoria);
}

function isApoPrestazioniInfSectionActive() {
    return apoState.activeSection === "paziente"
        && apoState.activePazienteSection === "prestazioni-infermieristiche";
}

function setApoPrestazioniInfMessage(message, type) {
    apoState.prestazioniInfMessage = "";
    apoState.prestazioniInfMessageType = normalizeApoDisplayValue(type) || "info";
    setApoSearchMessage(message, type);
}

async function loadApoPrestazioniInfCategorie() {
    const codServizio = getApoLoggedServiceCode();
    const normalizedCodServizio = normalizeApoValue(codServizio);

    if (!normalizedCodServizio) {
        apoState.prestazioniInfCategorie = [];
        apoState.prestazioniInfCategorieCodServizio = "";
        apoState.prestazioniInfCategorieError = "Servizio loggato non disponibile.";
        apoState.prestazioniInfCategorieLoaded = true;
        apoState.prestazioniInfCategorieLoading = false;
        if (isApoPrestazioniInfSectionActive()) {
            renderApoPazienteBody();
        }
        return;
    }

    if (apoState.prestazioniInfCategorieCodServizio === normalizedCodServizio
            && (apoState.prestazioniInfCategorieLoading
                || apoState.prestazioniInfCategorieLoaded
                || apoState.prestazioniInfCategorieError)) {
        return;
    }

    apoState.prestazioniInfCategorieCodServizio = normalizedCodServizio;
    apoState.prestazioniInfCategorie = [];
    apoState.prestazioniInfCategorieError = "";
    apoState.prestazioniInfCategorieLoading = true;
    apoState.prestazioniInfCategorieLoaded = false;
    if (isApoPrestazioniInfSectionActive()) {
        renderApoPazienteBody();
    }

    try {
        const response = await fetch(apoPrestazioniInfCategorieUrl, {
            headers: {
                "Accept": "application/json"
            }
        });
        if (response.status === 401) {
            window.location.href = apoLoginUrl;
            return;
        }

        let payload = null;
        try {
            payload = await response.json();
        } catch (error) {
            payload = null;
        }
        if (!response.ok || !payload || payload.esito !== "ok") {
            throw new Error(payload && payload.message ? payload.message : "Errore nel caricamento delle categorie.");
        }

        apoState.prestazioniInfCategorie = normalizeApoPrestazioniInfCategorie(payload.data && payload.data.categorie);
        apoState.prestazioniInfCategorieError = "";
        apoState.prestazioniInfCategorieLoaded = true;
    } catch (error) {
        apoState.prestazioniInfCategorie = [];
        apoState.prestazioniInfCategorieError = error && error.message ? error.message : "Errore nel caricamento delle categorie.";
        apoState.prestazioniInfCategorieLoaded = true;
        if (isApoPrestazioniInfSectionActive()) {
            setApoPrestazioniInfMessage(apoState.prestazioniInfCategorieError, "danger");
        }
    } finally {
        apoState.prestazioniInfCategorieLoading = false;
        if (isApoPrestazioniInfSectionActive()) {
            renderApoPazienteBody();
        }
    }
}

async function loadApoPrestazioniInfSottocategorie(categoria) {
    const codServizio = getApoLoggedServiceCode();
    const normalizedCategoria = normalizeApoDisplayValue(categoria);
    const key = getApoPrestazioniInfSottocategorieKey(codServizio, normalizedCategoria);

    if (!normalizeApoValue(codServizio) || !normalizedCategoria) {
        apoState.prestazioniInfSottocategorie = [];
        apoState.prestazioniInfSottocategorieKey = "";
        apoState.prestazioniInfSottocategorieError = "";
        apoState.prestazioniInfSottocategorieLoaded = false;
        apoState.prestazioniInfSottocategorieLoading = false;
        if (isApoPrestazioniInfSectionActive()) {
            renderApoPazienteBody();
        }
        return;
    }

    if (apoState.prestazioniInfSottocategorieKey === key
            && (apoState.prestazioniInfSottocategorieLoading
                || apoState.prestazioniInfSottocategorieLoaded
                || apoState.prestazioniInfSottocategorieError)) {
        return;
    }

    const params = new URLSearchParams();
    params.set("categoria", normalizedCategoria);

    apoState.prestazioniInfSottocategorieKey = key;
    apoState.prestazioniInfSottocategorie = [];
    apoState.prestazioniInfSottocategorieError = "";
    apoState.prestazioniInfSottocategorieLoading = true;
    apoState.prestazioniInfSottocategorieLoaded = false;
    if (isApoPrestazioniInfSectionActive()) {
        renderApoPazienteBody();
    }

    try {
        const response = await fetch(apoPrestazioniInfSottocategorieUrl + "?" + params.toString(), {
            headers: {
                "Accept": "application/json"
            }
        });
        if (response.status === 401) {
            window.location.href = apoLoginUrl;
            return;
        }

        let payload = null;
        try {
            payload = await response.json();
        } catch (error) {
            payload = null;
        }
        if (!response.ok || !payload || payload.esito !== "ok") {
            throw new Error(payload && payload.message ? payload.message : "Errore nel caricamento delle sottocategorie.");
        }

        apoState.prestazioniInfSottocategorie = normalizeApoPrestazioniInfSottocategorie(payload.data && payload.data.sottocategorie);
        apoState.prestazioniInfSottocategorieError = "";
        apoState.prestazioniInfSottocategorieLoaded = true;
    } catch (error) {
        apoState.prestazioniInfSottocategorie = [];
        apoState.prestazioniInfSottocategorieError = error && error.message ? error.message : "Errore nel caricamento delle sottocategorie.";
        apoState.prestazioniInfSottocategorieLoaded = true;
        if (isApoPrestazioniInfSectionActive()) {
            setApoPrestazioniInfMessage(apoState.prestazioniInfSottocategorieError, "danger");
        }
    } finally {
        apoState.prestazioniInfSottocategorieLoading = false;
        if (isApoPrestazioniInfSectionActive()) {
            renderApoPazienteBody();
        }
    }
}

function buildApoRicettaDematerializzataUrl() {
    const accesso = getApoSelectedAccesso();
    const codiceFiscale = normalizeApoDisplayValue(accesso && accesso.codFiscale);
    const codConsulenza = normalizeApoDisplayValue(accesso && (accesso.codConsulenze || accesso.codConsulenza || accesso.consulenza));
    const username = getApoLoggedUsername();
    const codServizio = getApoLoggedServiceCode();

    if (!codiceFiscale) {
        return {url: "", message: "Seleziona un paziente prima di aprire la ricetta dematerializzata."};
    }
    if (!username) {
        return {url: "", message: "Utente loggato non disponibile."};
    }
    if (!codServizio) {
        return {url: "", message: "Servizio loggato non disponibile."};
    }

    const url = new URL(apoRicettaDematerializzataUrl);
    url.searchParams.set("id_cod_fiscale", codiceFiscale);
    url.searchParams.set("id_medico", username);
    url.searchParams.set("servizio", codServizio);
    url.searchParams.set("invalidate", "YES");
    url.searchParams.set("user", username);
    url.searchParams.set("demat", "SI");
    url.searchParams.set("censisciRossa", "SI");
    url.searchParams.set("evento", codConsulenza);
    url.searchParams.set("Note", "");

    return {url: url.toString(), message: ""};
}

function buildApoRicettaFarmaciUrl() {
    const accesso = getApoSelectedAccesso();
    const codiceFiscale = normalizeApoDisplayValue(accesso && accesso.codFiscale);
    const codConsulenza = normalizeApoDisplayValue(accesso && (accesso.codConsulenze || accesso.codConsulenza || accesso.consulenza));
    const username = getApoLoggedUsername();
    const codServizio = getApoLoggedServiceCode();

    if (!codiceFiscale) {
        return {url: "", message: "Seleziona un paziente prima di aprire la ricetta farmaci."};
    }
    if (!username) {
        return {url: "", message: "Utente loggato non disponibile."};
    }
    if (!codServizio) {
        return {url: "", message: "Servizio loggato non disponibile."};
    }

    const url = new URL(apoRicettaFarmaciUrl);
    url.searchParams.set("id_cod_fiscale", codiceFiscale);
    url.searchParams.set("utente", username);
    url.searchParams.set("servizio", codServizio);
    url.searchParams.set("MENU", "RicettaFarmaciDM");
    url.searchParams.set("id_titolare", username);
    url.searchParams.set("evento", codConsulenza);

    return {url: url.toString(), message: ""};
}

function buildApoRepositoryRefertiUrl() {
    const accesso = getApoSelectedAccesso();
    const pinAzienda = normalizeApoDisplayValue(accesso && (accesso.pin || accesso.pinAzienda));
    const username = getApoLoggedUsername();

    if (!pinAzienda) {
        return {url: "", message: "Pin azienda del paziente non disponibile."};
    }
    if (!username) {
        return {url: "", message: "Utente loggato non disponibile."};
    }

    const url = new URL(apoRepositoryRefertiUrl);
    url.searchParams.set("utente", username);
    url.searchParams.set("postazione", username);
    url.searchParams.set("pagina", "VISDOC");
    url.searchParams.set("idPatient", pinAzienda);

    return {url: url.toString(), message: ""};
}

function buildApoConsensoDseWindowUrl() {
    const accesso = getApoSelectedAccesso();
    const codiceFiscale = normalizeApoDisplayValue(accesso && accesso.codFiscale);

    if (!codiceFiscale) {
        return {url: "", message: "Seleziona un paziente prima di aprire il cruscotto consenso."};
    }

    const url = new URL(apoConsensoDseInvokeUrl);
    url.searchParams.set("SISTEMA", "TOPAMB");
    url.searchParams.set("PASSWORD", "nhb0vfa9SC");
    url.searchParams.set("UTENTE", "m.manzotti");
    url.searchParams.set("COD_FISCALE", codiceFiscale);

    return {url: url.toString(), message: ""};
}

function openApoBrowserPopup(url) {
    const popup = window.open(url, "_blank");

    if (!popup) {
        window.alert("Il browser ha bloccato l'apertura della scheda. Abilita i popup per APO e riprova.");
        return;
    }

    popup.focus();
}

function openApoRicettaWindow(type) {
    const isFarmaci = type === "farmaci";
    const result = isFarmaci ? buildApoRicettaFarmaciUrl() : buildApoRicettaDematerializzataUrl();
    const windowName = isFarmaci ? "APORicettaFarmaci" : "APORicettaDematerializzata";

    if (!result.url) {
        window.alert(result.message || "Impossibile aprire la ricetta.");
        return;
    }

    openApoBrowserPopup(result.url, windowName);
}

function openApoCertificatoMalattiaWindow() {
    openApoBrowserPopup(apoCertificatoMalattiaUrl, "APOCertificatoMalattia");
}

function openApoRepositoryRefertiWindow() {
    const result = buildApoRepositoryRefertiUrl();

    if (!result.url) {
        window.alert(result.message || "Impossibile aprire il repository referti.");
        return;
    }

    openApoBrowserPopup(result.url, "APORepositoryReferti");
}

function openApoConsensoDseWindow() {
    const result = buildApoConsensoDseWindowUrl();

    if (!result.url) {
        window.alert(result.message || "Impossibile aprire il cruscotto consenso.");
        return;
    }

    openApoBrowserPopup(result.url, "APOConsensoDSE");
}

function renderApoRicettaLinkSheet(sheet, type) {
    const isFarmaci = type === "farmaci";
    const result = isFarmaci ? buildApoRicettaFarmaciUrl() : buildApoRicettaDematerializzataUrl();
    const pageLabel = isFarmaci ? "Ricette Farmaci" : "Ricette Dematerializzate";
    const windowName = isFarmaci ? "APORicettaFarmaci" : "APORicettaDematerializzata";

    sheet.classList.add("apo-ricetta-sheet");

    if (!result.url) {
        const emptyState = document.createElement("div");
        emptyState.className = "empty-state";
        emptyState.textContent = result.message || "Impossibile preparare il link della ricetta.";
        sheet.appendChild(emptyState);
        return;
    }

    const text = document.createElement("p");
    text.className = "apo-ricetta-link-text";
    text.appendChild(document.createTextNode("Per aprire la pagina delle " + pageLabel + " premere su "));

    const link = document.createElement("a");
    link.className = "apo-ricetta-link";
    link.href = result.url;
    link.target = "_blank";
    link.rel = "noopener noreferrer";
    link.textContent = "questo link";
    link.addEventListener("click", function (event) {
        event.preventDefault();
        openApoBrowserPopup(result.url, windowName);
    });

    text.appendChild(link);
    sheet.appendChild(text);
}

function renderApoRepositoryRefertiLinkSheet(sheet) {
    const result = buildApoRepositoryRefertiUrl();

    sheet.classList.add("apo-ricetta-sheet");

    if (!result.url) {
        const emptyState = document.createElement("div");
        emptyState.className = "empty-state";
        emptyState.textContent = result.message || "Impossibile preparare il link del repository referti.";
        sheet.appendChild(emptyState);
        return;
    }

    const text = document.createElement("p");
    text.className = "apo-ricetta-link-text";
    text.appendChild(document.createTextNode("Per aprire la pagina del Repository referti premere su "));

    const link = document.createElement("a");
    link.className = "apo-ricetta-link";
    link.href = result.url;
    link.target = "_blank";
    link.rel = "noopener noreferrer";
    link.textContent = "questo link";
    link.addEventListener("click", function (event) {
        event.preventDefault();
        openApoBrowserPopup(result.url, "APORepositoryReferti");
    });

    text.appendChild(link);
    sheet.appendChild(text);
}

function renderApoConsensoDseLinkSheet(sheet) {
    const result = buildApoConsensoDseWindowUrl();

    sheet.classList.add("apo-ricetta-sheet");

    if (!result.url) {
        const emptyState = document.createElement("div");
        emptyState.className = "empty-state";
        emptyState.textContent = result.message || "Impossibile preparare il link del cruscotto consenso.";
        sheet.appendChild(emptyState);
        return;
    }

    const text = document.createElement("p");
    text.className = "apo-ricetta-link-text";
    text.appendChild(document.createTextNode("Per aprire la pagina del Cruscotto consenso premere su "));

    const link = document.createElement("a");
    link.className = "apo-ricetta-link";
    link.href = result.url;
    link.target = "_blank";
    link.rel = "noopener noreferrer";
    link.textContent = "questo link";
    link.addEventListener("click", function (event) {
        event.preventDefault();
        openApoBrowserPopup(result.url, "APOConsensoDSE");
    });

    text.appendChild(link);
    sheet.appendChild(text);
}

function renderApoCertificatoMalattiaLinkSheet(sheet) {
    const text = document.createElement("p");
    text.className = "apo-ricetta-link-text";
    text.appendChild(document.createTextNode("Per aprire la pagina del Certificato malattia premere su "));

    const link = document.createElement("a");
    link.className = "apo-ricetta-link";
    link.href = apoCertificatoMalattiaUrl;
    link.target = "_blank";
    link.rel = "noopener noreferrer";
    link.textContent = "questo link";
    link.addEventListener("click", function (event) {
        event.preventDefault();
        openApoCertificatoMalattiaWindow();
    });

    sheet.classList.add("apo-ricetta-sheet");
    text.appendChild(link);
    sheet.appendChild(text);
}

function createApoPazienteSheetField(labelText, valueText, extraClass) {
    const field = document.createElement("div");
    field.className = "apo-paziente-sheet-field" + (extraClass ? " " + extraClass : "");

    const label = document.createElement("div");
    label.className = "apo-paziente-sheet-label";
    label.textContent = labelText;

    const value = document.createElement("div");
    value.className = "apo-paziente-sheet-value";
    value.textContent = String(valueText || "").trim() || "-";

    field.appendChild(label);
    field.appendChild(value);
    return field;
}

function createApoPrestazioniInfSelectField(labelText, selectElement) {
    const field = document.createElement("label");
    field.className = "apo-prestazioni-inf-field";

    const label = document.createElement("span");
    label.className = "apo-prestazioni-inf-label";
    label.textContent = labelText;

    field.appendChild(label);
    field.appendChild(selectElement);
    return field;
}

function appendApoPrestazioniInfOption(select, value, text, isSelected) {
    const option = document.createElement("option");
    option.value = normalizeApoDisplayValue(value);
    option.textContent = normalizeApoDisplayValue(text);
    option.selected = isSelected === true;
    select.appendChild(option);
}

function handleApoPrestazioniInfCategoriaChange(value) {
    const categoria = normalizeApoDisplayValue(value);
    apoState.prestazioniInfCategoria = categoria;
    apoState.prestazioniInfSottocategorieSelezionate = [];
    apoState.prestazioniInfSottocategorie = [];
    apoState.prestazioniInfSottocategorieKey = "";
    apoState.prestazioniInfSottocategorieLoaded = false;
    apoState.prestazioniInfSottocategorieError = "";
    apoState.prestazioniInfMessage = "";
    if (categoria) {
        void loadApoPrestazioniInfSottocategorie(categoria);
    } else {
        renderApoPazienteBody();
    }
}

async function handleApoPrestazioniInfAggiungi() {
    if (!apoState.prestazioniInfCategoria) {
        setApoPrestazioniInfMessage("Seleziona una categoria.", "warning");
        return;
    }
    if (!apoState.prestazioniInfSottocategorieSelezionate.length) {
        setApoPrestazioniInfMessage("Seleziona almeno una sottocategoria.", "warning");
        return;
    }

    if (apoState.prestazioniInfSaving) {
        return;
    }

    const accesso = getApoSelectedAccesso();
    const codiceFiscale = normalizeApoDisplayValue(accesso && accesso.codFiscale);
    const codConsulenza = getApoAccessoCodConsulenza(accesso);
    const selectedIds = apoState.prestazioniInfSottocategorieSelezionate.filter(function (id) {
        return normalizeApoDisplayValue(id) !== "";
    });
    const params = new URLSearchParams();

    if (!accesso || !codiceFiscale || !codConsulenza) {
        setApoPrestazioniInfMessage("Consulenza o paziente selezionato non disponibile.", "danger");
        return;
    }
    if (!selectedIds.length) {
        setApoPrestazioniInfMessage("Seleziona almeno una sottocategoria valida.", "warning");
        return;
    }

    params.set("codiceFiscale", codiceFiscale);
    params.set("codConsulenza", codConsulenza);
    params.set("note", apoState.prestazioniInfNote);
    selectedIds.forEach(function (idCategoria) {
        params.append("idCategoria", idCategoria);
    });

    apoState.prestazioniInfSaving = true;
    apoState.prestazioniInfMessage = "";
    apoState.prestazioniInfMessageType = "info";
    setApoSearchMessage("", "info");
    renderApoPazienteBody();

    try {
        const response = await fetch(apoPrestazioniInfermieristicheUrl, {
            method: "POST",
            headers: {
                "Accept": "application/json",
                "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8"
            },
            body: params.toString()
        });

        if (response.status === 401) {
            window.location.href = apoLoginUrl;
            return;
        }

        let payload = null;
        try {
            payload = await response.json();
        } catch (error) {
            payload = null;
        }

        if (!response.ok || !payload || payload.esito !== "ok") {
            throw new Error(payload && payload.message ? payload.message : "Errore nel salvataggio delle prestazioni infermieristiche.");
        }

        apoState.prestazioniInfSottocategorieSelezionate = [];
        apoState.prestazioniInfNote = "";
        setApoPrestazioniInfMessage(payload.message || "Prestazioni infermieristiche aggiunte correttamente.", "success");
        clearApoPrestazioniInfermieristiche();
        void loadApoPrestazioniInfermieristicheForSelectedPaziente(accesso);
    } catch (error) {
        setApoPrestazioniInfMessage(error && error.message ? error.message : "Errore nel salvataggio delle prestazioni infermieristiche.", "danger");
    } finally {
        apoState.prestazioniInfSaving = false;
        if (isApoPrestazioniInfSectionActive()) {
            renderApoPazienteBody();
        }
    }
}

function renderApoPrestazioniInfermieristicheSheet(sheet) {
    sheet.classList.add("apo-prestazioni-inf-sheet");

    if (!getApoLoggedServiceCode()) {
        const emptyState = document.createElement("div");
        emptyState.className = "empty-state";
        emptyState.textContent = "Servizio loggato non disponibile.";
        sheet.appendChild(emptyState);
        return;
    }

    if (!apoState.prestazioniInfCategorieLoading
            && !apoState.prestazioniInfCategorieLoaded
            && !apoState.prestazioniInfCategorieError) {
        void loadApoPrestazioniInfCategorie();
    }
    if (apoState.prestazioniInfCategoria
            && !apoState.prestazioniInfSottocategorieLoading
            && !apoState.prestazioniInfSottocategorieLoaded
            && !apoState.prestazioniInfSottocategorieError) {
        void loadApoPrestazioniInfSottocategorie(apoState.prestazioniInfCategoria);
    }

    const form = document.createElement("form");
    form.className = "apo-prestazioni-inf-form";
    form.addEventListener("submit", function (event) {
        event.preventDefault();
        void handleApoPrestazioniInfAggiungi();
    });

    const fields = document.createElement("div");
    fields.className = "apo-prestazioni-inf-grid";

    const categoriaSelect = document.createElement("select");
    categoriaSelect.className = "form-select form-control";
    categoriaSelect.disabled = apoState.prestazioniInfCategorieLoading || apoState.prestazioniInfSaving;
    appendApoPrestazioniInfOption(categoriaSelect, "", apoState.prestazioniInfCategorieLoading ? "Caricamento..." : "Seleziona categoria", !apoState.prestazioniInfCategoria);
    apoState.prestazioniInfCategorie.forEach(function (categoria) {
        appendApoPrestazioniInfOption(categoriaSelect, categoria, categoria, categoria === apoState.prestazioniInfCategoria);
    });
    categoriaSelect.addEventListener("change", function () {
        handleApoPrestazioniInfCategoriaChange(categoriaSelect.value);
    });

    const sottocategoriaList = document.createElement("div");
    sottocategoriaList.className = "apo-prestazioni-inf-multiselect";
    sottocategoriaList.setAttribute("role", "listbox");
    sottocategoriaList.setAttribute("aria-multiselectable", "true");

    if (apoState.prestazioniInfSottocategorieLoading || !apoState.prestazioniInfSottocategorie.length) {
        const placeholder = document.createElement("div");
        placeholder.className = "apo-prestazioni-inf-multiselect-placeholder";
        placeholder.textContent = apoState.prestazioniInfSottocategorieLoading ? "Caricamento..." : "Seleziona categoria";
        sottocategoriaList.appendChild(placeholder);
    }

    apoState.prestazioniInfSottocategorie.forEach(function (item) {
        const option = document.createElement("button");
        const selected = apoState.prestazioniInfSottocategorieSelezionate.indexOf(item.id) !== -1;
        option.type = "button";
        option.className = "apo-prestazioni-inf-choice" + (selected ? " is-selected" : "");
        option.value = item.id;
        option.dataset.id = item.id;
        option.setAttribute("value", item.id);
        option.setAttribute("role", "option");
        option.setAttribute("aria-selected", selected ? "true" : "false");
        option.disabled = apoState.prestazioniInfSaving;
        option.textContent = item.sottoCategoria || item.id;
        option.addEventListener("click", function () {
            if (apoState.prestazioniInfSaving) {
                return;
            }
            const currentIndex = apoState.prestazioniInfSottocategorieSelezionate.indexOf(item.id);
            const shouldSelect = currentIndex === -1;
            if (shouldSelect) {
                apoState.prestazioniInfSottocategorieSelezionate.push(item.id);
            } else {
                apoState.prestazioniInfSottocategorieSelezionate.splice(currentIndex, 1);
            }
            apoState.prestazioniInfMessage = "";
            option.classList.toggle("is-selected", shouldSelect);
            option.setAttribute("aria-selected", shouldSelect ? "true" : "false");
        });
        sottocategoriaList.appendChild(option);
    });

    fields.appendChild(createApoPrestazioniInfSelectField("Categoria", categoriaSelect));
    fields.appendChild(createApoPrestazioniInfSelectField("Sottocategoria", sottocategoriaList));

    const noteField = document.createElement("label");
    noteField.className = "apo-prestazioni-inf-field apo-prestazioni-inf-note-field";
    const noteLabel = document.createElement("span");
    noteLabel.className = "apo-prestazioni-inf-label";
    noteLabel.textContent = "Note";
    const note = document.createElement("textarea");
    note.className = "form-control apo-prestazioni-inf-note";
    note.value = apoState.prestazioniInfNote;
    note.disabled = apoState.prestazioniInfSaving;
    note.addEventListener("input", function () {
        apoState.prestazioniInfNote = note.value;
    });
    noteField.appendChild(noteLabel);
    noteField.appendChild(note);

    const actions = document.createElement("div");
    actions.className = "apo-prestazioni-inf-actions";
    const addButton = document.createElement("button");
    addButton.type = "submit";
    addButton.className = "btn btn-success";
    addButton.disabled = apoState.prestazioniInfSaving;
    addButton.textContent = apoState.prestazioniInfSaving ? "Salvataggio..." : "Aggiungi";
    actions.appendChild(addButton);

    form.appendChild(fields);
    form.appendChild(noteField);
    form.appendChild(actions);
    sheet.appendChild(form);
}

function createApoPianoTerapeuticoField(labelText, valueText, extraClass) {
    const field = document.createElement("div");
    field.className = "apo-piano-terapeutico-field" + (extraClass ? " " + extraClass : "");

    const label = document.createElement("span");
    label.className = "apo-piano-terapeutico-label";
    label.textContent = labelText;

    const value = document.createElement("span");
    value.className = "apo-piano-terapeutico-value";
    value.textContent = normalizeApoDisplayValue(valueText) || "-";

    field.appendChild(label);
    field.appendChild(value);
    return field;
}

function formatApoPianoTerapeuticoDosaggio(item) {
    const dosaggio = normalizeApoDisplayValue(item && item.dosaggio);
    const unitaPosologica = normalizeApoDisplayValue(item && item.unitaPosologica);
    return [dosaggio, unitaPosologica].filter(function (value) {
        return !!value;
    }).join(" ");
}

function renderApoPianiTerapeuticiSheet(sheet, accesso) {
    sheet.classList.add("apo-piani-terapeutici-sheet");
    const codiceFiscale = normalizeApoDisplayValue(accesso && accesso.codFiscale);
    const normalizedCodiceFiscale = normalizeApoValue(codiceFiscale);

    if (!accesso || !normalizedCodiceFiscale) {
        const emptyState = document.createElement("div");
        emptyState.className = "empty-state";
        emptyState.textContent = "Seleziona un paziente per visualizzare i piani terapeutici.";
        sheet.appendChild(emptyState);
        return;
    }

    if (apoState.pianiTerapeuticiCodiceFiscale !== normalizedCodiceFiscale
            && !apoState.pianiTerapeuticiLoading) {
        void loadApoPianiTerapeuticiForSelectedPaziente(accesso);
    }

    if (apoState.pianiTerapeuticiLoading
            || apoState.pianiTerapeuticiCodiceFiscale !== normalizedCodiceFiscale) {
        const emptyState = document.createElement("div");
        emptyState.className = "empty-state";
        emptyState.textContent = "Caricamento piani terapeutici...";
        sheet.appendChild(emptyState);
        return;
    }

    if (apoState.pianiTerapeuticiError) {
        const emptyState = document.createElement("div");
        emptyState.className = "empty-state";
        emptyState.textContent = apoState.pianiTerapeuticiError;
        sheet.appendChild(emptyState);
        return;
    }

    if (!apoState.pianiTerapeutici.length) {
        const emptyState = document.createElement("div");
        emptyState.className = "empty-state";
        emptyState.textContent = "Nessun piano terapeutico attivo disponibile.";
        sheet.appendChild(emptyState);
        return;
    }

    const list = document.createElement("div");
    list.className = "apo-piani-terapeutici-list";

    apoState.pianiTerapeutici.forEach(function (item) {
        const card = document.createElement("article");
        card.className = "apo-piano-terapeutico-card";

        const header = document.createElement("div");
        header.className = "apo-piano-terapeutico-header";

        const title = document.createElement("h3");
        title.textContent = item.codPiano || "-";

        const dates = document.createElement("div");
        dates.className = "apo-piano-terapeutico-dates";
        dates.textContent = "Inizio " + (item.dataPiano || "-") + " - Scadenza " + (item.dataScadenza || "-");

        header.appendChild(title);
        header.appendChild(dates);

        const grid = document.createElement("div");
        grid.className = "apo-piano-terapeutico-grid";
        grid.appendChild(createApoPianoTerapeuticoField("Cod piano", item.codPiano));
        grid.appendChild(createApoPianoTerapeuticoField("Servizio", item.servizio));
        grid.appendChild(createApoPianoTerapeuticoField("Principio attivo", item.sostanza));
        grid.appendChild(createApoPianoTerapeuticoField("Farmaco", item.farmaco));
        grid.appendChild(createApoPianoTerapeuticoField("Dosaggio", formatApoPianoTerapeuticoDosaggio(item)));
        grid.appendChild(createApoPianoTerapeuticoField("Posologia", item.posologia));
        grid.appendChild(createApoPianoTerapeuticoField("Diagnosi", item.diagnosi, "apo-piano-terapeutico-field-wide"));

        card.appendChild(header);
        card.appendChild(grid);
        list.appendChild(card);
    });

    sheet.appendChild(list);
}

function renderApoDiagnosiSheet(sheet, accesso) {
    if (!accesso) {
        const emptyState = document.createElement("div");
        emptyState.className = "empty-state";
        emptyState.textContent = "Seleziona una consulenza per visualizzare la diagnosi.";
        sheet.appendChild(emptyState);
        return;
    }

    const esameRow = document.createElement("div");
    esameRow.className = "apo-diagnosi-esame-row";

    esameRow.appendChild(createApoPazienteSheetField("Esame", accesso.valore || "-"));
    esameRow.appendChild(createApoPazienteSheetField("Patologia", accesso.patologia || "-"));
    sheet.appendChild(esameRow);
    sheet.appendChild(createApoPazienteSheetField("Diagnosi", accesso.note || "-", "apo-paziente-sheet-field-notes"));
}

function getApoAllegatoMKey(accesso) {
    const codConsulenza = normalizeApoDisplayValue(accesso && (accesso.codConsulenze || accesso.codConsulenza || accesso.consulenza));
    const codServizio = getApoSelectedConsulenzaCodServizio() || normalizeApoDisplayValue(accesso && accesso.codServizio);

    if (!codConsulenza || !codServizio) {
        return "";
    }

    return codConsulenza + "|" + codServizio;
}

function getApoAllegatoMValue(allegato, fieldName) {
    const source = allegato && typeof allegato === "object" ? allegato : {};
    return normalizeApoDisplayValue(source[fieldName]);
}

function isApoAllegatoMSaved(allegato) {
    return !!(allegato
        && typeof allegato === "object"
        && Object.keys(allegato).length > 0);
}

function getApoAllegatoMSavedBy(allegato) {
    const source = allegato && typeof allegato === "object" ? allegato : {};
    return normalizeApoDisplayValue(source.modUtente
        || source.codUtente
        || source.utente
        || source.username);
}

function canShowApoAllegatoMSaveButton(allegatoMKey, allegato) {
    if (!isApoMainConsulenzaSelected()
            || apoState.allegatoMLoading
            || apoState.allegatoMKey !== allegatoMKey
            || !apoState.allegatoMLoaded
            || apoState.allegatoMError) {
        return false;
    }

    if (!isApoAllegatoMSaved(allegato)) {
        return true;
    }

    const savedBy = getApoAllegatoMSavedBy(allegato);
    const loggedUsername = getApoLoggedUsername();
    return !!savedBy
        && !!loggedUsername
        && normalizeApoValue(savedBy) === normalizeApoValue(loggedUsername);
}

function isApoAllegatoMTruthy(value) {
    const normalizedValue = normalizeApoValue(value);
    return normalizedValue === "1"
        || normalizedValue === "S"
        || normalizedValue === "SI"
        || normalizedValue === "Y"
        || normalizedValue === "YES"
        || normalizedValue === "TRUE"
        || normalizedValue === "T"
        || normalizedValue === "X";
}

function isApoAllegatoMFieldChecked(allegato, fieldName) {
    return isApoAllegatoMTruthy(getApoAllegatoMValue(allegato, fieldName));
}

function isApoAllegatoMEsitoChecked(allegato, fieldName, code) {
    if (isApoAllegatoMFieldChecked(allegato, fieldName)) {
        return true;
    }

    const esito = normalizeApoValue(getApoAllegatoMValue(allegato, "esitoInterv"));
    const normalizedCode = normalizeApoValue(code);
    if (!esito || !normalizedCode) {
        return false;
    }

    return esito === normalizedCode || esito.indexOf(normalizedCode) !== -1;
}

function createApoAllegatoInlineField(labelText, placeholderText, valueText, fieldName) {
    const field = document.createElement("label");
    field.className = "apo-allegato-inline-field";

    const label = document.createElement("span");
    label.textContent = labelText;

    const input = document.createElement("input");
    input.type = "text";
    input.placeholder = placeholderText || "";
    input.value = normalizeApoDisplayValue(valueText);
    if (fieldName) {
        input.name = fieldName;
    }

    field.appendChild(label);
    field.appendChild(input);
    return field;
}

function createApoAllegatoTextarea(labelText, className, placeholderText, valueText, fieldName) {
    const field = document.createElement("label");
    field.className = "apo-allegato-textarea-field" + (className ? " " + className : "");

    const label = document.createElement("span");
    label.textContent = labelText;

    const textarea = document.createElement("textarea");
    textarea.placeholder = placeholderText || "";
    textarea.value = normalizeApoDisplayValue(valueText);
    if (fieldName) {
        textarea.name = fieldName;
    }

    field.appendChild(label);
    field.appendChild(textarea);
    return field;
}

function createApoAllegatoCheckbox(optionConfig, allegato) {
    const option = typeof optionConfig === "object" && optionConfig ? optionConfig : {label: optionConfig};
    const wrapper = document.createElement("label");
    wrapper.className = "apo-allegato-check";

    const text = document.createElement("span");
    text.textContent = option.label || "";

    const input = document.createElement("input");
    input.type = "checkbox";
    input.checked = option.checked === true
        || (option.esitoCode ? isApoAllegatoMEsitoChecked(allegato, option.field, option.esitoCode) : isApoAllegatoMFieldChecked(allegato, option.field));
    if (option.field) {
        input.name = option.field;
    }
    if (option.value) {
        input.value = option.value;
    } else {
        input.value = "1";
    }
    input.dataset.allegatoValueType = option.valueType || "flag";

    wrapper.appendChild(input);
    if (option.label) {
        wrapper.appendChild(text);
    }
    return wrapper;
}

function createApoAllegatoRow(rowConfig, allegato) {
    const row = document.createElement("div");
    row.className = "apo-allegato-clinical-row";

    const label = document.createElement("span");
    label.textContent = rowConfig.label;

    const controls = document.createElement("div");
    controls.className = "apo-allegato-clinical-controls";
    const options = rowConfig.options && rowConfig.options.length ? rowConfig.options : [{label: "", field: rowConfig.field}];
    options.forEach(function (option) {
        controls.appendChild(createApoAllegatoCheckbox(option, allegato));
    });

    row.appendChild(label);
    row.appendChild(controls);
    return row;
}

function createApoAllegatoSection(titleText, rows, allegato) {
    const section = document.createElement("section");
    section.className = "apo-allegato-clinical-section";

    const title = document.createElement("h3");
    title.textContent = titleText;
    section.appendChild(title);

    rows.forEach(function (rowConfig) {
        section.appendChild(createApoAllegatoRow(rowConfig, allegato));
    });

    return section;
}

function createApoAllegatoButton(labelText, variant) {
    const button = document.createElement("button");
    button.type = "button";
    button.className = "apo-allegato-action-button apo-allegato-action-" + variant;
    button.textContent = labelText;
    return button;
}

function createApoAllegatoStatusMessage(messageText, variant) {
    const message = document.createElement("div");
    message.className = "apo-allegato-form-message" + (variant ? " is-" + variant : "");
    message.textContent = normalizeApoDisplayValue(messageText);
    message.setAttribute("role", variant === "danger" ? "alert" : "status");
    return message;
}

function setApoAllegatoMButtonsDisabled(form, disabled) {
    if (!form) {
        return;
    }

    const buttons = form.querySelectorAll(".apo-allegato-action-button");
    buttons.forEach(function (button) {
        button.disabled = disabled === true;
    });
}

function buildApoAllegatoMSaveParams(accesso, form) {
    const params = new URLSearchParams();
    const codPaz = normalizeApoDisplayValue(accesso && accesso.codPaz);
    const codConsulenza = normalizeApoDisplayValue(accesso && (accesso.codConsulenze || accesso.codConsulenza || accesso.consulenza));
    const codServizio = getApoSelectedConsulenzaCodServizio() || normalizeApoDisplayValue(accesso && accesso.codServizio);

    if (!codPaz || !codConsulenza || !codServizio) {
        return {
            params: null,
            message: "Codice paziente, codice consulenza o codice servizio mancante."
        };
    }

    params.set("codPaz", codPaz);
    params.set("codConsulenza", codConsulenza);
    params.set("codServizio", codServizio);

    form.querySelectorAll("input[name], textarea[name]").forEach(function (field) {
        const fieldName = String(field.name || "").trim();
        if (!fieldName) {
            return;
        }
        if (field.type === "checkbox") {
            const checkedValue = normalizeApoDisplayValue(field.value) || "1";
            const uncheckedValue = field.dataset.allegatoValueType === "text" ? "" : "0";
            params.set(fieldName, field.checked ? checkedValue : uncheckedValue);
        } else {
            params.set(fieldName, normalizeApoDisplayValue(field.value));
        }
    });

    return {params: params, message: ""};
}

async function saveApoAllegatoM(accesso, form, saveButton) {
    const saveRequest = buildApoAllegatoMSaveParams(accesso, form);
    if (!saveRequest.params) {
        setApoSearchMessage(saveRequest.message, "danger");
        return;
    }

    setApoSearchMessage("", "info");
    setApoAllegatoMButtonsDisabled(form, true);
    if (saveButton) {
        saveButton.textContent = "Salvataggio...";
    }

    try {
        const response = await fetch(apoAllegatoMUrl, {
            method: "POST",
            headers: {
                "Accept": "application/json",
                "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8"
            },
            body: saveRequest.params.toString()
        });

        if (response.status === 401) {
            window.location.href = apoLoginUrl;
            return;
        }

        let payload = null;
        try {
            payload = await response.json();
        } catch (error) {
            payload = null;
        }

        if (!response.ok || !payload || payload.esito !== "ok") {
            throw new Error(payload && payload.message ? payload.message : "Errore nel salvataggio dell'Allegato M.");
        }

        apoState.allegatoM = normalizeApoAllegatoM(payload.data && payload.data.allegatoM);
        apoState.allegatoMKey = getApoAllegatoMKey(accesso);
        apoState.allegatoMError = "";
        apoState.allegatoMLoaded = true;
        syncApoPazienteMenuForUser();
        setApoSearchMessage(payload.message || "Allegato M salvato correttamente.", "success");
    } catch (error) {
        setApoSearchMessage(error && error.message ? error.message : "Errore nel salvataggio dell'Allegato M.", "danger");
    } finally {
        setApoAllegatoMButtonsDisabled(form, false);
        if (saveButton) {
            saveButton.textContent = "Salva";
        }
    }
}

function renderApoAllegatoMSheet(sheet, accesso) {
    sheet.classList.add("apo-allegato-sheet");
    const allegatoMKey = getApoAllegatoMKey(accesso);

    if (!accesso || !allegatoMKey) {
        const emptyState = document.createElement("div");
        emptyState.className = "empty-state";
        emptyState.textContent = "Seleziona una consulenza con codice servizio per visualizzare Allegato M.";
        sheet.appendChild(emptyState);
        return;
    }

    if (apoState.allegatoMKey !== allegatoMKey && !apoState.allegatoMLoading) {
        void loadApoAllegatoMForSelectedAccesso(accesso);
    }

    const hasCurrentAllegato = apoState.allegatoMKey === allegatoMKey
        && apoState.allegatoM
        && typeof apoState.allegatoM === "object";
    const allegato = hasCurrentAllegato ? apoState.allegatoM : {};

    const form = document.createElement("div");
    form.className = "apo-allegato-m-form";

    const contactRow = document.createElement("div");
    contactRow.className = "apo-allegato-contact-row";
    contactRow.appendChild(createApoAllegatoInlineField("Email", "email@dominio.it", getApoAllegatoMValue(allegato, "email"), "email"));
    contactRow.appendChild(createApoAllegatoInlineField("Telefono", "Numero di telefono", getApoAllegatoMValue(allegato, "telefono"), "telefono"));

    const clinicalGrid = document.createElement("div");
    clinicalGrid.className = "apo-allegato-clinical-grid";
    clinicalGrid.appendChild(createApoAllegatoSection("SISTEMA NERVOSO", [
        {label: "Indenne", field: "snIndenne"},
        {label: "Coscienza obnubilata", field: "snCoscienza"},
        {label: "Perdita di coscienza", field: "snPerdita"},
        {label: "Stato di agitazione", field: "snStato"},
        {label: "Convulsioni", field: "snConvulsioni"},
        {label: "Romberg", field: "snRomberg"},
        {label: "Rigor nucalis", field: "snRigor"},
        {label: "Deficit motorio", field: "snDefMot"},
        {label: "Deficit sensitivo", field: "snDefSen"},
        {label: "Deviazione dello sguardo", field: "snDeviazione"}
    ], allegato));
    clinicalGrid.appendChild(createApoAllegatoSection("PUPILLE", [
        {label: "Normali", options: [{label: "SX", field: "pllNormSx"}, {label: "DX", field: "pllNormDx"}]},
        {label: "Miosi", options: [{label: "SX", field: "pllMioSx"}, {label: "DX", field: "pllMioDx"}]},
        {label: "Midriasi", options: [{label: "SX", field: "pllMidSx"}, {label: "DX", field: "pllMidDx"}]},
        {label: "R. fotomotori", options: [{label: "SX", field: "pllFotSx"}, {label: "DX", field: "pllFotDx"}]},
        {label: "Nistagmo", field: "pllNistagmo"}
    ], allegato));
    clinicalGrid.appendChild(createApoAllegatoSection("APP. CIRCOLATORIO", [
        {label: "Normale", field: "appccNormale"},
        {label: "Aritmia", field: "appccAritmia"},
        {label: "Cianosi", field: "appccCianosi"},
        {label: "Edemi", field: "appccEdemi"}
    ], allegato));
    clinicalGrid.appendChild(createApoAllegatoSection("APP. RESPIRATORIO", [
        {label: "Indenne", field: "apprespIndenne"},
        {label: "Rumori", field: "apprespRum"},
        {label: "Rumori secchi", field: "apprespRumS"},
        {label: "Enfisema sottocutaneo", field: "apprespEnfisema"}
    ], allegato));
    clinicalGrid.appendChild(createApoAllegatoSection("ADDOME", [
        {label: "Murphy", field: "addMurphy"},
        {label: "Blumberg", field: "addBluem"},
        {label: "Rovsing", field: "addRov"},
        {label: "Giordano", options: [{label: "SX", field: "addGioSx"}, {label: "DX", field: "addGioDx"}]},
        {label: "Ascite", field: "addAscite"}
    ], allegato));

    const esito = document.createElement("section");
    esito.className = "apo-allegato-esito";
    const esitoTitle = document.createElement("h3");
    esitoTitle.textContent = "ESITO INTERVENTO";
    const esitoGrid = document.createElement("div");
    esitoGrid.className = "apo-allegato-esito-grid";
    const pazienteLabel = document.createElement("span");
    pazienteLabel.className = "apo-allegato-paziente-label";
    pazienteLabel.textContent = "Paziente:";
    esitoGrid.appendChild(pazienteLabel);
    [
        {label: "Rinvio a MMG", field: "mmg", esitoCode: "MMG"},
        {label: "Si invia a PS", field: "ps", esitoCode: "PS"},
        {label: "Si invia a COT", field: "cot", esitoCode: "COT"},
        {label: "Attivazione 118", field: "inv118", esitoCode: "118"}
    ].forEach(function (option) {
        esitoGrid.appendChild(createApoAllegatoCheckbox(option, allegato));
    });
    esito.appendChild(esitoTitle);
    esito.appendChild(esitoGrid);

    const nonDifferibile = document.createElement("label");
    nonDifferibile.className = "apo-allegato-nondifferibile";
    const nonDifferibileInput = document.createElement("input");
    nonDifferibileInput.type = "checkbox";
    nonDifferibileInput.name = "differibile";
    nonDifferibileInput.value = "1";
    nonDifferibileInput.dataset.allegatoValueType = "flag";
    nonDifferibileInput.checked = isApoAllegatoMFieldChecked(allegato, "differibile");
    const nonDifferibileText = document.createElement("span");
    nonDifferibileText.textContent = "L'INTERVENTO NON PRESENTA CARATTERE DI PRESTAZIONE NON DIFFERIBILE";
    nonDifferibile.appendChild(nonDifferibileInput);
    nonDifferibile.appendChild(nonDifferibileText);

    const actions = document.createElement("div");
    actions.className = "apo-allegato-actions";
    if (canShowApoAllegatoMSaveButton(allegatoMKey, allegato)) {
        const saveButton = createApoAllegatoButton("Salva", "save");
        saveButton.addEventListener("click", function () {
            void saveApoAllegatoM(accesso, form, saveButton);
        });
        actions.appendChild(saveButton);
    }
    const printButton = createApoAllegatoButton("Stampa", "print");
    printButton.addEventListener("click", function () {
        openApoAllegatoMReport(accesso, true);
    });
    actions.appendChild(printButton);

    if (apoState.allegatoMLoading || apoState.allegatoMKey !== allegatoMKey) {
        form.appendChild(createApoAllegatoStatusMessage("Caricamento Allegato M...", "info"));
    } else if (apoState.allegatoMError) {
        form.appendChild(createApoAllegatoStatusMessage(apoState.allegatoMError, "danger"));
    }
    form.appendChild(contactRow);
    form.appendChild(createApoAllegatoTextarea("Anamnesi", "apo-allegato-textarea-large", "Inserisci anamnesi del paziente", getApoAllegatoMValue(allegato, "motCirc"), "motCirc"));
    form.appendChild(createApoAllegatoTextarea("Esame obiettivo", "apo-allegato-textarea-medium", "Descrivi l'esame obiettivo", getApoAllegatoMValue(allegato, "altro"), "altro"));
    form.appendChild(clinicalGrid);
    form.appendChild(createApoAllegatoTextarea("Terapia", "apo-allegato-textarea-medium", "Inserisci terapia o indicazioni", getApoAllegatoMValue(allegato, "terapia"), "terapia"));
    form.appendChild(esito);
    form.appendChild(nonDifferibile);
    form.appendChild(actions);
    sheet.appendChild(form);
}

function renderApoPazienteTitle(title, config) {
    title.innerHTML = "";

    const action = document.createElement("span");
    action.className = "apo-paziente-title-action";
    action.textContent = config && config.label ? config.label : getApoPazienteSectionConfig(apoState.activePazienteSection).label;

    title.appendChild(action);
}

function renderApoPazienteTitleMain(main) {
    const accesso = getApoSelectedAccesso();
    const consulenza = accesso ? (accesso.codConsulenze || accesso.consulenza || "-") : "-";
    const dataConsulenza = accesso ? (accesso.dataOra || accesso.dataDisplay || "-") : "-";
    const sede = normalizeApoDisplayValue(accesso && (accesso.sede || accesso.descServizio || accesso.codServizio));
    const mainAccesso = getApoAccessoById(apoState.mainAccessoId);

    main.innerHTML = "";
    main.classList.remove("is-main-consulenza", "is-storico-consulenza");

    if (accesso && mainAccesso) {
        main.classList.add(isSameApoAccesso(accesso, mainAccesso) ? "is-main-consulenza" : "is-storico-consulenza");
    }

    const consulenzaValue = document.createElement("span");
    consulenzaValue.className = "apo-paziente-title-consulenza";
    consulenzaValue.textContent = consulenza;

    main.appendChild(document.createTextNode("Consulenza "));
    main.appendChild(consulenzaValue);
    main.appendChild(document.createTextNode(" del " + dataConsulenza));
    if (sede) {
        main.appendChild(document.createTextNode(" presso " + sede));
    }
}

function createApoSelectedPatientItem(labelText, valueText, extraClass) {
    const item = document.createElement("div");
    item.className = "apo-paziente-selected-item" + (extraClass ? " " + extraClass : "");

    const label = document.createElement("span");
    label.className = "apo-paziente-selected-label";
    label.textContent = labelText;

    const value = document.createElement("span");
    value.className = "apo-paziente-selected-value";
    value.textContent = String(valueText || "").trim() || "-";

    item.appendChild(label);
    item.appendChild(value);
    return item;
}

function clearApoMedicoCurante() {
    apoState.medicoCurante = null;
    apoState.medicoCuranteCodiceFiscale = "";
    apoState.medicoCuranteLoading = false;
    apoState.medicoCuranteLoaded = false;
    apoState.medicoCuranteError = "";
    apoState.medicoCuranteRequestId += 1;
}

function clearApoConsensoDse() {
    apoState.consensoDseHtml = "";
    apoState.consensoDseCodiceFiscale = "";
    apoState.consensoDseLoading = false;
    apoState.consensoDseLoaded = false;
    apoState.consensoDseError = "";
    apoState.consensoDseRequestId += 1;
    renderApoConsensoDse();
}

function normalizeApoMedicoCurante(data) {
    const source = data && typeof data === "object" ? data : {};
    return {
        pinAzienda: normalizeApoDisplayValue(source.pinAzienda),
        matricola: normalizeApoDisplayValue(source.matricola),
        nome: normalizeApoDisplayValue(source.nome),
        cognome: normalizeApoDisplayValue(source.cognome),
        codFiscale: normalizeApoDisplayValue(source.codFiscale),
        descrizione: normalizeApoDisplayValue(source.descrizione)
    };
}

function formatApoMedicoCurante() {
    const medico = apoState.medicoCurante || {};
    const descrizione = normalizeApoDisplayValue(medico.descrizione || ((medico.cognome || "") + " " + (medico.nome || "")).trim());

    if (descrizione) {
        return descrizione;
    }
    if (medico.matricola) {
        return medico.matricola;
    }
    if (medico.codFiscale) {
        return medico.codFiscale;
    }
    return "-";
}

function getApoMedicoCuranteDisplay(accesso) {
    const codiceFiscale = normalizeApoValue(accesso && accesso.codFiscale);

    if (!codiceFiscale || apoState.medicoCuranteCodiceFiscale !== codiceFiscale) {
        return "-";
    }
    if (apoState.medicoCuranteLoading) {
        return "Caricamento...";
    }
    if (apoState.medicoCuranteError) {
        return "-";
    }

    return formatApoMedicoCurante();
}

function getApoConsensoDseStatusFromHtml(html) {
    const rawHtml = String(html || "").trim().toLowerCase();
    const parser = document.createElement("div");
    parser.innerHTML = html || "";
    const text = String(parser.textContent || "").trim().toLowerCase();
    const value = rawHtml + " " + text;

    if (!value.trim()) {
        return "inespresso";
    }
    if (value.indexOf("consensoinespresso") !== -1
            || value.indexOf("inespresso") !== -1
            || value.indexOf("non e ancora stato espresso") !== -1
            || value.indexOf("non è ancora stato espresso") !== -1) {
        return "inespresso";
    }
    if (value.indexOf("consensopositivo") !== -1
            || value.indexOf("consensoespresso") !== -1
            || value.indexOf("consenso espresso") !== -1
            || value.indexOf("consensoaccolto") !== -1
            || value.indexOf("consensoattivo") !== -1
            || value.indexOf("consensoconcesso") !== -1
            || value.indexOf("consensoaccordato") !== -1
            || value.indexOf("acconsent") !== -1
            || value.indexOf("favorevole") !== -1) {
        return "presente";
    }

    return "negato";
}

function setApoConsensoDseBoxStatus(status) {
    document.querySelectorAll(".apo-paziente-consenso-box").forEach(function (box) {
        box.classList.remove("is-consenso-inespresso", "is-consenso-presente", "is-consenso-negato", "is-consenso-loading");
        box.classList.add("is-consenso-" + status);
    });
}

function renderApoConsensoDse() {
    const targets = document.querySelectorAll(".apo-paziente-consenso-html");

    if (apoState.consensoDseLoading) {
        setApoConsensoDseBoxStatus("loading");
        targets.forEach(function (target) {
            target.textContent = "Caricamento consenso DSE...";
        });
        return;
    }

    if (apoState.consensoDseError) {
        setApoConsensoDseBoxStatus("negato");
        targets.forEach(function (target) {
            target.textContent = apoState.consensoDseError;
        });
        return;
    }

    const html = String(apoState.consensoDseHtml || "").trim();
    setApoConsensoDseBoxStatus(getApoConsensoDseStatusFromHtml(html));
    targets.forEach(function (target) {
        if (html) {
            target.innerHTML = html;
        } else {
            target.textContent = "Consenso non disponibile.";
        }
    });
}

async function loadApoConsensoDseForSelectedPaziente(accesso) {
    const codiceFiscale = normalizeApoDisplayValue(accesso && accesso.codFiscale);
    const normalizedCodiceFiscale = normalizeApoValue(codiceFiscale);

    if (!normalizedCodiceFiscale) {
        clearApoConsensoDse();
        syncApoPazienteMenuForUser();
        return;
    }

    if (apoState.consensoDseCodiceFiscale === normalizedCodiceFiscale
            && (apoState.consensoDseLoading || apoState.consensoDseLoaded || apoState.consensoDseError)) {
        renderApoConsensoDse();
        syncApoPazienteMenuForUser();
        return;
    }

    const requestId = apoState.consensoDseRequestId + 1;
    const params = new URLSearchParams();
    params.set("codiceFiscale", codiceFiscale);

    apoState.consensoDseRequestId = requestId;
    apoState.consensoDseCodiceFiscale = normalizedCodiceFiscale;
    apoState.consensoDseHtml = "";
    apoState.consensoDseError = "";
    apoState.consensoDseLoading = true;
    apoState.consensoDseLoaded = false;
    renderApoConsensoDse();
    syncApoPazienteMenuForUser();

    try {
        const response = await fetch(apoConsensoDseUrl + "?" + params.toString(), {
            headers: {
                "Accept": "application/json"
            }
        });

        if (response.status === 401) {
            window.location.href = apoLoginUrl;
            return;
        }

        let payload = null;
        try {
            payload = await response.json();
        } catch (error) {
            payload = null;
        }

        if (!response.ok || !payload || payload.esito !== "ok") {
            throw new Error(payload && payload.message ? payload.message : "Errore nel caricamento del consenso DSE.");
        }

        if (apoState.consensoDseRequestId !== requestId || apoState.consensoDseCodiceFiscale !== normalizedCodiceFiscale) {
            return;
        }

        apoState.consensoDseHtml = normalizeApoDisplayValue(payload.data && payload.data.html);
        apoState.consensoDseError = "";
        apoState.consensoDseLoaded = true;
    } catch (error) {
        if (apoState.consensoDseRequestId !== requestId) {
            return;
        }

        apoState.consensoDseHtml = "";
        apoState.consensoDseError = error && error.message ? error.message : "Errore nel caricamento del consenso DSE.";
        apoState.consensoDseLoaded = true;
    } finally {
        if (apoState.consensoDseRequestId === requestId) {
            apoState.consensoDseLoading = false;
            renderApoConsensoDse();
            syncApoPazienteMenuForUser();
        }
    }
}

async function loadApoMedicoCuranteForSelectedPaziente(accesso) {
    const codiceFiscale = normalizeApoDisplayValue(accesso && accesso.codFiscale);
    const normalizedCodiceFiscale = normalizeApoValue(codiceFiscale);

    if (!normalizedCodiceFiscale) {
        clearApoMedicoCurante();
        return;
    }

    if (apoState.medicoCuranteCodiceFiscale === normalizedCodiceFiscale
            && (apoState.medicoCuranteLoading || apoState.medicoCuranteLoaded || apoState.medicoCuranteError)) {
        return;
    }

    const requestId = apoState.medicoCuranteRequestId + 1;
    const params = new URLSearchParams();
    params.set("codiceFiscale", codiceFiscale);

    apoState.medicoCuranteRequestId = requestId;
    apoState.medicoCuranteCodiceFiscale = normalizedCodiceFiscale;
    apoState.medicoCurante = null;
    apoState.medicoCuranteError = "";
    apoState.medicoCuranteLoading = true;
    apoState.medicoCuranteLoaded = false;
    renderApoSelectedPaziente();

    try {
        const response = await fetch(apoMedicoCuranteUrl + "?" + params.toString(), {
            headers: {
                "Accept": "application/json"
            }
        });

        if (response.status === 401) {
            window.location.href = apoLoginUrl;
            return;
        }

        let payload = null;
        try {
            payload = await response.json();
        } catch (error) {
            payload = null;
        }

        if (!response.ok || !payload || payload.esito !== "ok") {
            throw new Error(payload && payload.message ? payload.message : "Errore nel caricamento del medico curante.");
        }

        if (apoState.medicoCuranteRequestId !== requestId || apoState.medicoCuranteCodiceFiscale !== normalizedCodiceFiscale) {
            return;
        }

        apoState.medicoCurante = normalizeApoMedicoCurante(payload.data && payload.data.medicoCurante);
        apoState.medicoCuranteError = "";
        apoState.medicoCuranteLoaded = true;
    } catch (error) {
        if (apoState.medicoCuranteRequestId !== requestId) {
            return;
        }

        apoState.medicoCurante = null;
        apoState.medicoCuranteError = error && error.message ? error.message : "Errore nel caricamento del medico curante.";
        apoState.medicoCuranteLoaded = true;
    } finally {
        if (apoState.medicoCuranteRequestId === requestId) {
            apoState.medicoCuranteLoading = false;
            renderApoSelectedPaziente();
        }
    }
}

function renderApoSelectedPaziente() {
    const body = document.getElementById("apo-paziente-selected-body");
    const schedaButton = document.getElementById("apo-selected-scheda-button");
    const accesso = getApoSelectedAccesso();

    updateApoSelectedConsulenzaHiddenFields(accesso);
    if (schedaButton) {
        schedaButton.disabled = !accesso || !normalizeApoDisplayValue(accesso.codPaz);
    }

    if (!body) {
        return;
    }

    body.innerHTML = "";

    const sheet = document.createElement("article");
    sheet.className = "apo-paziente-selected-sheet";

    if (!accesso) {
        const emptyState = document.createElement("div");
        emptyState.className = "empty-state";
        emptyState.textContent = "Seleziona una riga in Accessi per visualizzare il paziente selezionato.";
        sheet.appendChild(emptyState);
        body.appendChild(sheet);
        clearApoMedicoCurante();
        clearApoConsensoDse();
        clearApoStoricoAccessi();
        clearApoPrestazioniInfermieristiche();
        clearApoRicetteFarmaci();
        clearApoRicetteDematerializzate();
        clearApoPianiTerapeutici();
        clearApoAllegatoM();
        clearApoSchedaPazienteDiario();
        clearApoSchedaPazienteHistoryPatologie();
        clearApoSchedaPazienteHistoryAllergie();
        clearApoSchedaPazienteHistoryTerapie();
        renderApoStoricoAccessi();
        return;
    }

    const grid = document.createElement("div");
    grid.className = "apo-paziente-selected-grid";
    grid.appendChild(createApoSelectedPatientItem("Paziente", accesso.paziente || "-"));
    grid.appendChild(createApoSelectedPatientItem("Eta", accesso.eta || "-"));
    grid.appendChild(createApoSelectedPatientItem("Cod fiscale", accesso.codFiscale || "-"));
    grid.appendChild(createApoSelectedPatientItem("Data nascita", accesso.dataNascita || "-"));
    grid.appendChild(createApoSelectedPatientItem("Medico curante", getApoMedicoCuranteDisplay(accesso), "apo-paziente-selected-item-wide"));
    sheet.appendChild(grid);
    body.appendChild(sheet);
    void loadApoMedicoCuranteForSelectedPaziente(accesso);
    void loadApoConsensoDseForSelectedPaziente(accesso);
    void loadApoStoricoAccessiForSelectedPaziente(accesso);
    void loadApoPrestazioniInfermieristicheForSelectedPaziente(accesso);
    void loadApoRicetteFarmaciForSelectedPaziente(accesso);
    void loadApoRicetteDematerializzateForSelectedPaziente(accesso);
    void loadApoPianiTerapeuticiForSelectedPaziente(accesso);
    void loadApoAllegatoMForSelectedAccesso(accesso);
}

function clearApoStoricoAccessi() {
    apoState.storicoAccessi = [];
    apoState.storicoCodiceFiscale = "";
    apoState.storicoLoading = false;
    apoState.storicoLoaded = false;
    apoState.storicoError = "";
    apoState.selectFirstStoricoAccessoOnLoad = false;
    apoState.pendingStoricoAccessoId = "";
    apoState.storicoRequestId += 1;
}

function prepareApoPazienteStoricoAccessi() {
    apoState.activePazienteHistoryTab = "scheda";
    apoState.activeSchedaPazienteHistorySection = "diario";
    apoState.activeStoricoTab = "accessi";
    apoState.selectFirstStoricoAccessoOnLoad = String(apoState.selectedAccessoId || "").trim() !== "";
    renderApoPazienteHistoryTabs();
    renderApoSchedaPazienteHistorySection();
}

function findApoStoricoAccessoMatchingAccesso(accesso) {
    if (!accesso) {
        return null;
    }

    for (let index = 0; index < apoState.storicoAccessi.length; index += 1) {
        if (isSameApoAccesso(apoState.storicoAccessi[index], accesso)) {
            return apoState.storicoAccessi[index];
        }
    }

    return null;
}

function selectFirstApoStoricoAccessoIfRequested() {
    if (!apoState.selectFirstStoricoAccessoOnLoad) {
        return false;
    }
    if (apoState.activeStoricoTab !== "accessi" || apoState.storicoLoading) {
        return false;
    }
    if (apoState.storicoError || (apoState.storicoLoaded && !apoState.storicoAccessi.length)) {
        apoState.selectFirstStoricoAccessoOnLoad = false;
        return false;
    }

    const mainAccesso = getApoAccessoById(apoState.mainAccessoId);
    const selectedAccesso = getApoSelectedAccesso();
    const storicoAccesso = findApoStoricoAccessoMatchingAccesso(mainAccesso)
            || findApoStoricoAccessoMatchingAccesso(selectedAccesso);
    const storicoAccessoId = String(storicoAccesso && storicoAccesso.id || "").trim();
    if (!storicoAccessoId) {
        apoState.selectFirstStoricoAccessoOnLoad = false;
        return false;
    }

    apoState.selectFirstStoricoAccessoOnLoad = false;
    if (apoState.selectedAccessoId === storicoAccessoId) {
        return false;
    }

    apoState.selectedAccessoId = storicoAccessoId;
    setApoDetailOpen(false);
    renderApoAccessi();
    renderApoSelectedPaziente();
    renderApoPazienteBody();
    return true;
}

function clearApoPrestazioniInfermieristiche() {
    apoState.prestazioniInfermieristiche = [];
    apoState.prestazioniInfermieristicheCodiceFiscale = "";
    apoState.prestazioniInfermieristicheLoading = false;
    apoState.prestazioniInfermieristicheLoaded = false;
    apoState.prestazioniInfermieristicheError = "";
    apoState.prestazioniInfermieristicheRequestId += 1;
}

function clearApoRicetteFarmaci() {
    apoState.ricetteFarmaci = [];
    apoState.ricetteFarmaciCodiceFiscale = "";
    apoState.ricetteFarmaciLoading = false;
    apoState.ricetteFarmaciLoaded = false;
    apoState.ricetteFarmaciError = "";
    apoState.ricetteFarmaciRequestId += 1;
}

function clearApoRicetteDematerializzate() {
    apoState.ricetteDematerializzate = [];
    apoState.ricetteDematerializzateCodiceFiscale = "";
    apoState.ricetteDematerializzateLoading = false;
    apoState.ricetteDematerializzateLoaded = false;
    apoState.ricetteDematerializzateError = "";
    apoState.ricetteDematerializzateRequestId += 1;
}

function clearApoPianiTerapeutici() {
    apoState.pianiTerapeutici = [];
    apoState.pianiTerapeuticiCodiceFiscale = "";
    apoState.pianiTerapeuticiLoading = false;
    apoState.pianiTerapeuticiLoaded = false;
    apoState.pianiTerapeuticiError = "";
    apoState.pianiTerapeuticiRequestId += 1;
}

function clearApoAllegatoM() {
    apoState.allegatoM = null;
    apoState.allegatoMKey = "";
    apoState.allegatoMLoading = false;
    apoState.allegatoMLoaded = false;
    apoState.allegatoMError = "";
    apoState.allegatoMRequestId += 1;
}

function clearApoSchedaPazienteDiario() {
    apoState.schedaPazienteDiario = [];
    apoState.schedaPazienteDiarioCodPaz = "";
    apoState.schedaPazienteDiarioLoading = false;
    apoState.schedaPazienteDiarioLoaded = false;
    apoState.schedaPazienteDiarioError = "";
    apoState.schedaPazienteDiarioRequestId += 1;
}

function clearApoSchedaPazienteHistoryPatologie() {
    apoState.schedaPazienteHistoryPatologie = [];
    apoState.schedaPazienteHistoryPatologieCodPaz = "";
    apoState.schedaPazienteHistoryPatologieLoading = false;
    apoState.schedaPazienteHistoryPatologieLoaded = false;
    apoState.schedaPazienteHistoryPatologieError = "";
    apoState.schedaPazienteHistoryPatologieRequestId += 1;
}

function clearApoSchedaPazienteHistoryAllergie() {
    apoState.schedaPazienteHistoryAllergie = [];
    apoState.schedaPazienteHistoryAllergieCodPaz = "";
    apoState.schedaPazienteHistoryAllergieLoading = false;
    apoState.schedaPazienteHistoryAllergieLoaded = false;
    apoState.schedaPazienteHistoryAllergieError = "";
    apoState.schedaPazienteHistoryAllergieRequestId += 1;
}

function clearApoSchedaPazienteHistoryTerapie() {
    apoState.schedaPazienteHistoryTerapie = [];
    apoState.schedaPazienteHistoryTerapieCodPaz = "";
    apoState.schedaPazienteHistoryTerapieLoading = false;
    apoState.schedaPazienteHistoryTerapieLoaded = false;
    apoState.schedaPazienteHistoryTerapieError = "";
    apoState.schedaPazienteHistoryTerapieRequestId += 1;
}

function isSameApoAccesso(left, right) {
    if (!left || !right) {
        return false;
    }

    return normalizeApoValue(left.codFiscale) === normalizeApoValue(right.codFiscale)
        && normalizeApoValue(left.codConsulenze || left.consulenza) === normalizeApoValue(right.codConsulenze || right.consulenza)
        && normalizeApoValue(left.dataIso || left.dataOra || left.dataDisplay) === normalizeApoValue(right.dataIso || right.dataOra || right.dataDisplay);
}

function sortApoStoricoAccessi(items) {
    return items.slice().sort(function (left, right) {
        const leftDate = String(left.dataIso || left.dataOra || left.dataDisplay || "").trim();
        const rightDate = String(right.dataIso || right.dataOra || right.dataDisplay || "").trim();
        return compareApoValues(leftDate, rightDate, "desc");
    });
}

function getApoSchedaPazienteUtenteInsData(source) {
    const item = source && typeof source === "object" ? source : {};
    const cognome = normalizeApoDisplayValue(item.utenteInsCognome || item.utente_ins_cognome || item.cognomeUtenteIns);
    const nome = normalizeApoDisplayValue(item.utenteInsNome || item.utente_ins_nome || item.nomeUtenteIns);
    const descrizione = normalizeApoDisplayValue(item.utenteInsDescrizione || item.utenteIns || item.utente_ins);
    const nomeCompleto = [cognome, nome].filter(function (part) {
        return !!part;
    }).join(" ");

    return {
        cognome: cognome,
        nome: nome,
        descrizione: nomeCompleto || descrizione
    };
}

function appendApoSchedaPazienteUtenteIns(container, source, className) {
    const utente = getApoSchedaPazienteUtenteInsData(source);
    if (!container || !utente.descrizione) {
        return;
    }

    const element = document.createElement("span");
    element.className = className || "apo-scheda-paziente-utente-ins";
    element.textContent = utente.descrizione;
    container.appendChild(element);
}

function normalizeApoSchedaPazienteDiarioItem(item, index) {
    const source = item && typeof item === "object" ? item : {};
    const utente = getApoSchedaPazienteUtenteInsData(source);
    return {
        id: normalizeApoDisplayValue(source.id),
        dataDiario: normalizeApoDisplayValue(source.dataDiario || source.data_diario),
        descrizione: normalizeApoDisplayValue(source.descrizione),
        utenteInsCognome: utente.cognome,
        utenteInsNome: utente.nome,
        utenteInsDescrizione: utente.descrizione
    };
}

function normalizeApoSchedaPazienteDiario(items) {
    if (!Array.isArray(items)) {
        return [];
    }
    return items.map(function (item, index) {
        return normalizeApoSchedaPazienteDiarioItem(item, index);
    });
}

function normalizeApoSchedaPazienteHistoryPatologia(item, index) {
    const source = item && typeof item === "object" ? item : {};
    const utente = getApoSchedaPazienteUtenteInsData(source);
    return {
        id: normalizeApoDisplayValue(source.id || ("PATOLOGIA_" + index)),
        codice: normalizeApoDisplayValue(source.codice),
        descrizione: normalizeApoDisplayValue(source.descrizione || source.patologia),
        utenteInsCognome: utente.cognome,
        utenteInsNome: utente.nome,
        utenteInsDescrizione: utente.descrizione
    };
}

function normalizeApoSchedaPazienteHistoryPatologie(items) {
    if (!Array.isArray(items)) {
        return [];
    }
    return items.map(function (item, index) {
        return normalizeApoSchedaPazienteHistoryPatologia(item, index);
    });
}

function normalizeApoSchedaPazienteAllergia(item, index) {
    const source = item && typeof item === "object" ? item : {};
    const codice = normalizeApoDisplayValue(source.codice || source.codiceAllergia || source.codice_allergia);
    const descrizione = normalizeApoDisplayValue(source.descrizione || source.allergia);
    const utente = getApoSchedaPazienteUtenteInsData(source);
    const idParts = [codice, descrizione].filter(function (part) {
        return !!part;
    });

    return {
        id: normalizeApoDisplayValue(source.id) || idParts.join("|") || ("ALLERGIA_" + index),
        codice: codice,
        descrizione: descrizione,
        utenteInsCognome: utente.cognome,
        utenteInsNome: utente.nome,
        utenteInsDescrizione: utente.descrizione
    };
}

function normalizeApoSchedaPazienteAllergie(items) {
    if (!Array.isArray(items)) {
        return [];
    }
    return items.map(function (item, index) {
        return normalizeApoSchedaPazienteAllergia(item, index);
    });
}

function getApoSchedaPazienteHistorySectionConfig(sectionId) {
    const normalizedSectionId = String(sectionId || "").trim();
    for (let index = 0; index < apoSchedaPazienteHistorySections.length; index += 1) {
        if (apoSchedaPazienteHistorySections[index].id === normalizedSectionId) {
            return apoSchedaPazienteHistorySections[index];
        }
    }
    return apoSchedaPazienteHistorySections[0];
}

function renderApoSchedaPazienteHistoryTabs() {
    const activeConfig = getApoSchedaPazienteHistorySectionConfig(apoState.activeSchedaPazienteHistorySection);
    const body = document.getElementById("apo-paziente-scheda-body");

    document.querySelectorAll("[data-scheda-paziente-history-section]").forEach(function (tab) {
        const sectionId = String(tab.getAttribute("data-scheda-paziente-history-section") || "").trim();
        const isActive = sectionId === activeConfig.id;
        tab.classList.toggle("is-active", isActive);
        tab.setAttribute("aria-selected", isActive ? "true" : "false");
        tab.tabIndex = isActive ? 0 : -1;
        if (body && isActive && tab.id) {
            body.setAttribute("aria-labelledby", tab.id);
        }
    });
}

function renderApoSchedaPazienteHistoryPlaceholder(body, config) {
    const emptyState = document.createElement("div");
    emptyState.className = "empty-state";
    emptyState.textContent = "Nessun dato disponibile per " + config.label + ".";
    body.appendChild(emptyState);
}

function createApoSchedaPazienteDiarioAddControls(accesso) {
    const codPaz = normalizeApoDisplayValue(accesso && accesso.codPaz);
    const form = document.createElement("form");
    form.className = "apo-paziente-diario-add";

    const addButton = document.createElement("button");
    addButton.type = "submit";
    addButton.className = "btn btn-success";
    addButton.textContent = "Aggiungi nuovo diario";
    addButton.disabled = !codPaz;

    form.addEventListener("submit", function (event) {
        event.preventDefault();
        openApoSchedaPazienteDiarioModal(accesso);
    });

    form.appendChild(addButton);
    return form;
}

function renderApoSchedaPazienteDiario(body, accesso) {
    const codPaz = normalizeApoDisplayValue(accesso && accesso.codPaz);
    if (!codPaz) {
        const emptyState = document.createElement("div");
        emptyState.className = "empty-state";
        emptyState.textContent = "Codice paziente non disponibile.";
        body.appendChild(emptyState);
        return;
    }

    body.appendChild(createApoSchedaPazienteDiarioAddControls(accesso));

    if (apoState.schedaPazienteDiarioCodPaz !== codPaz && !apoState.schedaPazienteDiarioLoading) {
        void loadApoSchedaPazienteDiarioForSelectedPaziente(accesso);
    }

    if (apoState.schedaPazienteDiarioLoading || apoState.schedaPazienteDiarioCodPaz !== codPaz) {
        body.appendChild(createApoSpinnerLoader("Caricamento diario...", "apo-storico-loader"));
        return;
    }

    if (apoState.schedaPazienteDiarioError) {
        const emptyState = document.createElement("div");
        emptyState.className = "empty-state";
        emptyState.textContent = apoState.schedaPazienteDiarioError;
        body.appendChild(emptyState);
        return;
    }

    if (!apoState.schedaPazienteDiario.length) {
        const emptyState = document.createElement("div");
        emptyState.className = "empty-state";
        emptyState.textContent = "Nessuna nota diario disponibile.";
        body.appendChild(emptyState);
        return;
    }

    const list = document.createElement("div");
    list.className = "apo-paziente-history-list";
    apoState.schedaPazienteDiario.forEach(function (item) {
        const row = document.createElement("article");
        row.className = "apo-paziente-history-item apo-paziente-history-item-static apo-paziente-diario-row apo-paziente-patologia-row";

        const header = document.createElement("div");
        header.className = "apo-paziente-patologia-row-header";

        const dateBlock = document.createElement("div");
        dateBlock.className = "apo-paziente-history-meta-block";

        const date = document.createElement("span");
        date.className = "apo-paziente-history-date";
        date.textContent = item.dataDiario || "-";
        dateBlock.appendChild(date);
        appendApoSchedaPazienteUtenteIns(dateBlock, item, "apo-paziente-history-inseritore");

        const deleteButton = document.createElement("button");
        deleteButton.type = "button";
        deleteButton.className = "apo-paziente-patologia-delete-button";
        deleteButton.textContent = "X";
        deleteButton.title = "Elimina diario";
        deleteButton.setAttribute("aria-label", "Elimina diario del " + (item.dataDiario || ""));
        deleteButton.disabled = !normalizeApoDisplayValue(item.id);
        deleteButton.addEventListener("click", function (event) {
            event.preventDefault();
            event.stopPropagation();
            deleteButton.disabled = true;
            void deleteApoSchedaPazienteDiario(accesso, item);
        });

        const description = document.createElement("strong");
        description.className = "apo-paziente-history-title";
        description.textContent = item.descrizione || "-";

        header.appendChild(dateBlock);
        header.appendChild(deleteButton);
        row.appendChild(header);
        row.appendChild(description);
        list.appendChild(row);
    });
    body.appendChild(list);
}

function setApoSchedaPazienteDiarioSaving(isSaving) {
    const modal = document.getElementById("apo-diario-modal");
    const saveButton = document.getElementById("apo-diario-save");
    const saving = isSaving === true;

    if (modal) {
        modal.querySelectorAll("input, textarea, button").forEach(function (field) {
            field.disabled = saving;
        });
    }
    if (saveButton) {
        saveButton.textContent = saving ? "Salvataggio..." : "Salva diario";
    }
}

function openApoSchedaPazienteDiarioModal(accesso, dataDiario) {
    const codPaz = normalizeApoDisplayValue(accesso && accesso.codPaz);
    const modal = document.getElementById("apo-diario-modal");
    const dateInput = document.getElementById("apo-diario-data");
    const timeInput = document.getElementById("apo-diario-ora");
    const descriptionInput = document.getElementById("apo-diario-descrizione");
    if (!modal || !codPaz) {
        setApoSearchMessage("Codice paziente non disponibile.", "warning");
        return;
    }

    apoState.schedaPazienteDiarioModalLastFocus = document.activeElement;
    apoState.schedaPazienteDiarioModalCodPaz = codPaz;
    modal.dataset.codPaz = codPaz;
    if (dateInput) {
        dateInput.value = normalizeApoDisplayValue(dataDiario) || getApoCurrentDate();
    }
    if (timeInput) {
        timeInput.value = getApoCurrentTime();
    }
    if (descriptionInput) {
        descriptionInput.value = "";
    }

    setApoSchedaPazienteDiarioSaving(false);
    modal.classList.remove("is-hidden");
    modal.setAttribute("aria-hidden", "false");
    if (descriptionInput) {
        descriptionInput.focus();
    }
}

function closeApoSchedaPazienteDiarioModal(restoreFocus) {
    const modal = document.getElementById("apo-diario-modal");
    if (modal) {
        modal.classList.add("is-hidden");
        modal.setAttribute("aria-hidden", "true");
        delete modal.dataset.codPaz;
    }
    apoState.schedaPazienteDiarioModalCodPaz = "";
    setApoSchedaPazienteDiarioSaving(false);

    if (restoreFocus && apoState.schedaPazienteDiarioModalLastFocus
            && typeof apoState.schedaPazienteDiarioModalLastFocus.focus === "function") {
        apoState.schedaPazienteDiarioModalLastFocus.focus();
    }
}

async function saveApoSchedaPazienteDiario() {
    const modal = document.getElementById("apo-diario-modal");
    const dateInput = document.getElementById("apo-diario-data");
    const timeInput = document.getElementById("apo-diario-ora");
    const descriptionInput = document.getElementById("apo-diario-descrizione");
    const accesso = getApoSelectedAccesso();
    const codPaz = normalizeApoDisplayValue((modal && modal.dataset.codPaz) || apoState.schedaPazienteDiarioModalCodPaz || (accesso && accesso.codPaz));
    const dataValue = normalizeApoDisplayValue(dateInput && dateInput.value);
    const timeValue = normalizeApoDisplayValue(timeInput && timeInput.value);
    const description = normalizeApoDisplayValue(descriptionInput && descriptionInput.value);

    if (!codPaz) {
        setApoSearchMessage("Codice paziente non disponibile.", "warning");
        return false;
    }
    if (!dataValue || !timeValue) {
        setApoSearchMessage("Valorizza data e ora del diario.", "warning");
        return false;
    }
    if (!description) {
        setApoSearchMessage("Inserisci la descrizione del diario.", "warning");
        if (descriptionInput) {
            descriptionInput.focus();
        }
        return false;
    }

    const params = new URLSearchParams();
    params.set("codPaz", codPaz);
    params.set("dataDiario", dataValue + " " + timeValue);
    params.set("descrizione", description);

    setApoSchedaPazienteDiarioSaving(true);
    try {
        const response = await fetch(apoSchedaPazienteDiarioUrl, {
            method: "POST",
            headers: {
                "Accept": "application/json",
                "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8"
            },
            body: params.toString()
        });

        if (response.status === 401) {
            window.location.href = apoLoginUrl;
            return false;
        }

        let payload = null;
        try {
            payload = await response.json();
        } catch (error) {
            payload = null;
        }

        if (!response.ok || !payload || payload.esito !== "ok") {
            throw new Error(payload && payload.message ? payload.message : "Errore nel salvataggio del diario.");
        }

        apoState.schedaPazienteDiarioCodPaz = "";
        apoState.schedaPazienteDiarioLoaded = false;
        apoState.schedaPazienteDiarioError = "";
        closeApoSchedaPazienteDiarioModal(true);
        setApoSearchMessage(payload.message || "Diario aggiunto correttamente.", "success");
        void loadApoSchedaPazienteDiarioForSelectedPaziente(accesso || {codPaz: codPaz});
        return true;
    } catch (error) {
        setApoSearchMessage(error && error.message ? error.message : "Errore nel salvataggio del diario.", "danger");
        setApoSchedaPazienteDiarioSaving(false);
        return false;
    }
}

async function deleteApoSchedaPazienteDiario(accesso, item) {
    const codPaz = normalizeApoDisplayValue(accesso && accesso.codPaz);
    const idDiario = normalizeApoDisplayValue(item && item.id);
    if (!codPaz || !idDiario) {
        setApoSearchMessage("ID diario o codice paziente non disponibile.", "warning");
        return false;
    }

    const params = new URLSearchParams();
    params.set("id", idDiario);
    params.set("codPaz", codPaz);

    try {
        const response = await fetch(apoSchedaPazienteDiarioUrl + "?" + params.toString(), {
            method: "DELETE",
            headers: {
                "Accept": "application/json"
            }
        });

        if (response.status === 401) {
            window.location.href = apoLoginUrl;
            return false;
        }

        let payload = null;
        try {
            payload = await response.json();
        } catch (error) {
            payload = null;
        }

        if (!response.ok || !payload || payload.esito !== "ok") {
            throw new Error(payload && payload.message ? payload.message : "Errore nell'eliminazione del diario.");
        }

        const deleted = payload.data && Number(payload.data.deleted) > 0;
        apoState.schedaPazienteDiarioCodPaz = "";
        apoState.schedaPazienteDiarioLoaded = false;
        apoState.schedaPazienteDiarioError = "";
        setApoSearchMessage(payload.message || "Diario eliminato correttamente.", deleted ? "success" : "warning");
        void loadApoSchedaPazienteDiarioForSelectedPaziente(accesso);
        return true;
    } catch (error) {
        setApoSearchMessage(error && error.message ? error.message : "Errore nell'eliminazione del diario.", "danger");
        renderApoSchedaPazienteHistorySection();
        return false;
    }
}

function formatApoSchedaPazientePatologiaSuggestion(itemData) {
    return [
        normalizeApoDisplayValue(itemData && itemData.codice),
        normalizeApoDisplayValue(itemData && itemData.descrizione)
    ].filter(function (part) {
        return !!part;
    }).join(" - ");
}

function getApoSchedaPazientePatologiaCodeFromInput(inputValue, suggestions) {
    const normalizedValue = normalizeApoDisplayValue(inputValue);
    if (!normalizedValue) {
        return "";
    }

    for (let index = 0; index < suggestions.length; index += 1) {
        const suggestion = suggestions[index];
        if (normalizeApoValue(formatApoSchedaPazientePatologiaSuggestion(suggestion)) === normalizeApoValue(normalizedValue)
                || normalizeApoValue(suggestion.codice) === normalizeApoValue(normalizedValue)) {
            return normalizeApoDisplayValue(suggestion.codice);
        }
    }

    const separatorIndex = normalizedValue.indexOf(" - ");
    return separatorIndex > 0 ? normalizedValue.substring(0, separatorIndex).trim() : normalizedValue;
}

function formatApoSchedaPazienteAllergiaSuggestion(itemData) {
    return normalizeApoDisplayValue(itemData && itemData.descrizione)
        || normalizeApoDisplayValue(itemData && itemData.codice);
}

function formatApoSchedaPazienteAllergiaHistoryDescription(itemData) {
    const description = normalizeApoDisplayValue(itemData && itemData.descrizione);
    const code = normalizeApoDisplayValue(itemData && itemData.codice);
    if (!description || !code) {
        return description;
    }

    const separatorIndex = description.indexOf(" - ");
    if (separatorIndex > 0
            && normalizeApoValue(description.substring(0, separatorIndex)) === normalizeApoValue(code)) {
        return description.substring(separatorIndex + 3).trim();
    }
    return description;
}

function getApoSchedaPazienteAllergiaCodeFromInput(inputValue, suggestions) {
    const normalizedValue = normalizeApoDisplayValue(inputValue);
    if (!normalizedValue) {
        return "";
    }

    for (let index = 0; index < suggestions.length; index += 1) {
        const suggestion = suggestions[index];
        if (normalizeApoValue(formatApoSchedaPazienteAllergiaSuggestion(suggestion)) === normalizeApoValue(normalizedValue)
                || normalizeApoValue(suggestion.codice) === normalizeApoValue(normalizedValue)) {
            return normalizeApoDisplayValue(suggestion.codice);
        }
    }

    const separatorIndex = normalizedValue.indexOf(" - ");
    return separatorIndex > 0 ? normalizedValue.substring(0, separatorIndex).trim() : normalizedValue;
}

function normalizeApoSchedaPazienteTerapieLookupItems(items, valueKeys) {
    if (!Array.isArray(items)) {
        return [];
    }

    return items.map(function (item, index) {
        const source = item && typeof item === "object" ? item : {};
        let value = typeof item === "string" ? normalizeApoDisplayValue(item) : "";
        if (!value) {
            for (let keyIndex = 0; keyIndex < valueKeys.length; keyIndex += 1) {
                value = normalizeApoDisplayValue(source[valueKeys[keyIndex]]);
                if (value) {
                    break;
                }
            }
        }
        return {
            id: normalizeApoValue(value) || ("TERAPIA_LOOKUP_" + index),
            value: value,
            label: value
        };
    }).filter(function (itemData) {
        return !!itemData.value;
    });
}

async function loadApoSchedaPazienteTerapieLookup(url, params, dataKey, valueKeys, errorMessage) {
    const queryString = params.toString();
    const response = await fetch(queryString ? url + "?" + queryString : url, {
        headers: {
            "Accept": "application/json"
        }
    });
    if (response.status === 401) {
        window.location.href = apoLoginUrl;
        return [];
    }

    let payload = null;
    try {
        payload = await response.json();
    } catch (error) {
        payload = null;
    }

    if (!response.ok || !payload || payload.esito !== "ok") {
        throw new Error(payload && payload.message ? payload.message : errorMessage);
    }

    return normalizeApoSchedaPazienteTerapieLookupItems(payload.data && payload.data[dataKey], valueKeys);
}

function createApoSchedaPazienteTerapieSelect(options, selectedValue) {
    const select = document.createElement("select");
    select.className = "form-select form-control apo-scheda-paziente-select";
    const normalizedSelected = normalizeApoValue(selectedValue);
    options.forEach(function (optionConfig) {
        const option = document.createElement("option");
        option.value = optionConfig.value;
        option.textContent = optionConfig.label;
        option.selected = normalizeApoValue(option.value) === normalizedSelected;
        select.appendChild(option);
    });
    return select;
}

function makeApoSchedaPazienteTerapiaKey(itemData) {
    return [
        normalizeApoValue(itemData && itemData.principioAttivo),
        normalizeApoValue(itemData && itemData.farmaco),
        normalizeApoValue(itemData && itemData.confezione),
        normalizeApoValue(itemData && itemData.quantita),
        normalizeApoValue(itemData && itemData.frequenzaUnita),
        normalizeApoValue(itemData && itemData.durataValore),
        normalizeApoValue(itemData && itemData.durataUnita)
    ].join("|");
}

function normalizeApoSchedaPazienteTerapia(item, index) {
    const source = item && typeof item === "object" ? item : {};
    const utente = getApoSchedaPazienteUtenteInsData(source);
    const principioAttivo = normalizeApoDisplayValue(source.principioAttivo || source.pa || source.princAttivo);
    const farmaco = typeof item === "string"
        ? normalizeApoDisplayValue(item)
        : normalizeApoDisplayValue(source.farmaco || source.descrizione || source.terapia || source.nomeFarmaco);
    const confezione = normalizeApoDisplayValue(source.confezione || source.c || source.confezioneFarmaco);
    const quantita = normalizeApoDisplayValue(source.quantita || source.quantitaFarmaco || source.dosaggio);
    const frequenzaUnita = normalizeApoDisplayValue(source.frequenzaUnita || source.frequenza || source.unitaFrequenza);
    const durataUnitaDb = normalizeApoDisplayValue(source.unita || source.UNITA || source.durataUnita || source.unitaDurata);
    const durata = parseApoTerapiaDurationParts(
        normalizeApoDisplayValue(source.durataValore || source.durata || source.numeroDurata),
        durataUnitaDb
    );
    const durataValore = durata.valore;
    const durataUnita = durataUnitaDb || durata.unita;
    const dataIns = normalizeApoDisplayValue(source.dataIns || source.data_ins || source.DATA_INS);
    const dataInsSort = normalizeApoDisplayValue(source.dataInsSort || source.data_ins_sort || source.DATA_INS_SORT);
    const keyParts = [principioAttivo, farmaco, confezione, quantita, frequenzaUnita, durataValore, durataUnita].filter(function (part) {
        return !!part;
    });

    return {
        id: normalizeApoDisplayValue(source.id) || keyParts.join("|") || ("TERAPIA_" + index),
        principioAttivo: principioAttivo,
        farmaco: farmaco,
        confezione: confezione,
        quantita: quantita,
        frequenzaUnita: frequenzaUnita,
        durataValore: durataValore,
        durataUnita: durataUnita,
        dataIns: dataIns,
        dataInsSort: dataInsSort,
        utenteInsCognome: utente.cognome,
        utenteInsNome: utente.nome,
        utenteInsDescrizione: utente.descrizione
    };
}

function parseApoSchedaPazienteTerapieText(value) {
    return normalizeApoDisplayValue(value).split(/[;\n]+/).map(function (part) {
        return normalizeApoDisplayValue(part);
    }).filter(function (part) {
        return !!part;
    });
}

function normalizeApoSchedaPazienteTerapie(items) {
    let sourceItems = items;
    if (typeof items === "string") {
        sourceItems = parseApoSchedaPazienteTerapieText(items);
    }
    if (!Array.isArray(sourceItems)) {
        return [];
    }

    return sourceItems.map(function (item, index) {
        return normalizeApoSchedaPazienteTerapia(item, index);
    }).filter(function (itemData) {
        return !!itemData.farmaco;
    });
}

function dedupeApoSchedaPazienteTerapie(items) {
    const seen = {};
    return normalizeApoSchedaPazienteTerapie(items).filter(function (itemData) {
        const key = makeApoSchedaPazienteTerapiaKey(itemData);
        if (!key || seen[key]) {
            return false;
        }
        seen[key] = true;
        return true;
    });
}

function getApoSchedaPazienteTerapieFromSource(source) {
    if (!source || typeof source !== "object") {
        return [];
    }
    if (Array.isArray(source.terapieSelezionate)) {
        return source.terapieSelezionate;
    }
    if (Array.isArray(source.terapieInCorso)) {
        return source.terapieInCorso;
    }
    if (Array.isArray(source.terapie)) {
        return source.terapie;
    }
    return getApoSchedaPazienteField(source, ["terapieInCorso", "terapie", "terapiaInCorso", "terapia"]);
}

function formatApoTerapiaDurationText(durataValore, durataUnita) {
    const durataUnitaDb = normalizeApoDisplayValue(durataUnita);
    const durata = parseApoTerapiaDurationParts(durataValore, durataUnita);
    return [durata.valore, durataUnitaDb || durata.unita].filter(function (part) {
        return !!part;
    }).join(" ");
}

function formatApoSchedaPazienteTerapia(itemData) {
    const principioAttivo = normalizeApoDisplayValue(itemData && itemData.principioAttivo);
    const farmaco = normalizeApoDisplayValue(itemData && itemData.farmaco);
    const confezione = normalizeApoDisplayValue(itemData && itemData.confezione);
    const quantita = normalizeApoDisplayValue(itemData && itemData.quantita);
    const frequenzaUnita = normalizeApoDisplayValue(itemData && itemData.frequenzaUnita);
    const durata = formatApoTerapiaDurationText(itemData && itemData.durataValore, itemData && itemData.durataUnita);
    const parts = [];

    if (principioAttivo) {
        parts.push(principioAttivo);
    }
    if (farmaco) {
        parts.push(farmaco);
    }
    if (confezione) {
        parts.push(confezione);
    }
    if (quantita) {
        parts.push(quantita);
    }
    if (frequenzaUnita) {
        parts.push("al " + frequenzaUnita);
    }
    if (durata) {
        parts.push("per " + durata);
    }

    return parts.join(" - ");
}

function formatApoSchedaPazienteTerapiaTitle(itemData) {
    const principioAttivo = normalizeApoDisplayValue(itemData && itemData.principioAttivo);
    const farmaco = normalizeApoDisplayValue(itemData && itemData.farmaco);
    return [principioAttivo, farmaco].filter(function (part) {
        return !!part;
    }).join(" - ");
}

function formatApoSchedaPazienteTerapiaDescription(itemData) {
    const confezione = normalizeApoDisplayValue(itemData && itemData.confezione);
    const quantita = normalizeApoDisplayValue(itemData && itemData.quantita);
    const frequenzaUnita = normalizeApoDisplayValue(itemData && itemData.frequenzaUnita);
    const durata = formatApoTerapiaDurationText(itemData && itemData.durataValore, itemData && itemData.durataUnita);
    const doseParts = [];

    if (quantita) {
        doseParts.push(quantita);
    }
    if (frequenzaUnita) {
        doseParts.push("al " + frequenzaUnita);
    }
    if (durata) {
        doseParts.push("per " + durata);
    }

    if (confezione && doseParts.length) {
        return confezione + " - " + doseParts.join(" ");
    }
    if (confezione) {
        return confezione;
    }
    return doseParts.join(" ");
}

function parseApoTerapiaDate(value) {
    const normalized = normalizeApoDisplayValue(value);
    let match = normalized.match(/^(\d{2})\/(\d{2})\/(\d{4})/);
    if (match) {
        return new Date(Number(match[3]), Number(match[2]) - 1, Number(match[1]));
    }

    match = normalized.match(/^(\d{4})-(\d{2})-(\d{2})/);
    if (match) {
        return new Date(Number(match[1]), Number(match[2]) - 1, Number(match[3]));
    }

    return null;
}

function formatApoTerapiaDate(dateValue) {
    if (!(dateValue instanceof Date) || Number.isNaN(dateValue.getTime())) {
        return "";
    }

    return String(dateValue.getDate()).padStart(2, "0") + "/"
            + String(dateValue.getMonth() + 1).padStart(2, "0") + "/"
            + dateValue.getFullYear();
}

function normalizeApoTerapiaDurationUnit(value) {
    const unit = normalizeApoValue(value);
    if (!unit) {
        return "";
    }
    if (unit === "G" || unit === "GG") {
        return "giorni";
    }
    if (unit === "S" || unit === "SETT") {
        return "settimane";
    }
    if (unit === "M") {
        return "mesi";
    }
    if (unit.indexOf("GIORN") === 0) {
        return "giorni";
    }
    if (unit.indexOf("SETTIMAN") === 0) {
        return "settimane";
    }
    if (unit.indexOf("MES") === 0) {
        return "mesi";
    }
    return "";
}

function parseApoTerapiaDurationParts(durataValore, durataUnita) {
    const value = normalizeApoDisplayValue(durataValore);
    const explicitUnit = normalizeApoTerapiaDurationUnit(durataUnita);
    const match = value.match(/^(?:per\s+)?(\d+(?:[,.]\d+)?)\s*[;:,\-]?\s*(giorno|giorni|g|gg|settimana|settimane|sett|s|mese|mesi|m)\b/i);
    const numberOnlyMatch = value.match(/^(?:per\s+)?(\d+(?:[,.]\d+)?)/i);

    if (match) {
        return {
            valore: match[1],
            unita: normalizeApoTerapiaDurationUnit(match[2]) || explicitUnit
        };
    }

    return {
        valore: numberOnlyMatch ? numberOnlyMatch[1] : value,
        unita: explicitUnit
    };
}

function addApoTerapiaDuration(dateValue, durataValore, durataUnita) {
    const durata = parseApoTerapiaDurationParts(durataValore, durataUnita);
    const amount = Number(durata.valore.replace(",", "."));
    const unit = normalizeApoTerapiaDurationUnit(durata.unita);
    if (!(dateValue instanceof Date) || Number.isNaN(dateValue.getTime()) || !amount || !unit) {
        return null;
    }

    const result = new Date(dateValue.getTime());
    if (unit.indexOf("settiman") === 0) {
        result.setDate(result.getDate() + (amount * 7));
    } else if (unit.indexOf("mes") === 0) {
        result.setMonth(result.getMonth() + amount);
    } else if (unit.indexOf("giorn") === 0) {
        result.setDate(result.getDate() + amount);
    }
    return result;
}

function formatApoSchedaPazienteTerapiaPeriodo(itemData) {
    return "";
}

function renderApoSchedaPazienteTerapieSelectedList(list, items, onRemove, emptyText) {
    if (!list) {
        return;
    }

    list.innerHTML = "";
    if (!items.length) {
        const emptyItem = document.createElement("li");
        emptyItem.className = "apo-scheda-paziente-selected-empty";
        emptyItem.textContent = emptyText || "Nessuna terapia in corso selezionata.";
        list.appendChild(emptyItem);
        return;
    }

    items.forEach(function (itemData) {
        const listItem = document.createElement("li");
        listItem.className = "apo-scheda-paziente-terapia-row";
        const text = document.createElement("span");
        text.className = "apo-scheda-paziente-selected-text";
        text.textContent = formatApoSchedaPazienteTerapia(itemData) || "-";

        listItem.appendChild(text);
        if (typeof onRemove === "function") {
            const removeButton = document.createElement("button");
            removeButton.type = "button";
            removeButton.className = "apo-scheda-paziente-selected-remove";
            removeButton.setAttribute("aria-label", "Rimuovi terapia selezionata");
            removeButton.textContent = "X";
            removeButton.addEventListener("click", function () {
                onRemove(itemData);
            });
            listItem.appendChild(removeButton);
        }
        list.appendChild(listItem);
    });
}

function createApoSchedaPazienteTerapieAddControls(onAdd, options) {
    const config = options && typeof options === "object" ? options : {};
    const controls = document.createElement("div");
    controls.className = config.className || "apo-scheda-paziente-terapie-add";

    const lookupRow = document.createElement("div");
    lookupRow.className = "apo-scheda-paziente-terapie-lookup-row";

    const doseRow = document.createElement("div");
    doseRow.className = "apo-scheda-paziente-terapie-dose-row";

    function createLookupField(placeholder) {
        const field = document.createElement("div");
        field.className = "apo-scheda-paziente-terapie-lookup-field";

        const select = document.createElement("select");
        select.className = "form-select form-control apo-scheda-paziente-select apo-scheda-paziente-terapie-lookup-select";
        select.disabled = true;

        field.appendChild(select);
        return {
            field: field,
            select: select,
            placeholder: placeholder,
            items: [],
            requestId: 0
        };
    }

    const principioLookup = createLookupField("Principio attivo");
    const farmacoLookup = createLookupField("Farmaco");
    const confezioneLookup = createLookupField("Confezione");

    const quantitaInput = document.createElement("input");
    quantitaInput.type = "text";
    quantitaInput.className = "form-control apo-scheda-paziente-input";
    quantitaInput.placeholder = "Quantita'";

    const frequenzaGroup = document.createElement("div");
    frequenzaGroup.className = "apo-scheda-paziente-terapie-inline";
    const frequenzaLabel = document.createElement("span");
    frequenzaLabel.textContent = "al";
    const frequenzaSelect = createApoSchedaPazienteTerapieSelect(apoSchedaPazienteTerapieFrequenzaOptions, "giorno");
    frequenzaGroup.appendChild(frequenzaLabel);
    frequenzaGroup.appendChild(frequenzaSelect);

    const durataGroup = document.createElement("div");
    durataGroup.className = "apo-scheda-paziente-terapie-inline apo-scheda-paziente-terapie-durata";
    const durataLabel = document.createElement("span");
    durataLabel.textContent = "per";
    const durataInput = document.createElement("input");
    durataInput.type = "number";
    durataInput.min = "0";
    durataInput.step = "1";
    durataInput.className = "form-control apo-scheda-paziente-input";
    durataInput.placeholder = "N.";
    const durataSelect = createApoSchedaPazienteTerapieSelect(apoSchedaPazienteTerapieDurataOptions, "giorni");
    durataGroup.appendChild(durataLabel);
    durataGroup.appendChild(durataInput);
    durataGroup.appendChild(durataSelect);

    const addButton = document.createElement("button");
    addButton.type = "button";
    addButton.className = "btn btn-success" + (config.buttonClass ? " " + config.buttonClass : "");
    addButton.textContent = config.buttonText || "Aggiungi";
    let adding = false;

    function setAdding(isAdding) {
        adding = isAdding === true;
        addButton.disabled = adding;
        addButton.textContent = adding ? (config.addingText || "Aggiungo...") : (config.buttonText || "Aggiungi");
    }

    function getLookupValue(lookup) {
        return normalizeApoDisplayValue(lookup && lookup.select ? lookup.select.value : "");
    }

    function setLookupOptions(lookup, items, placeholderText, isDisabled) {
        lookup.items = Array.isArray(items) ? items : [];
        lookup.select.innerHTML = "";

        const placeholderOption = document.createElement("option");
        placeholderOption.value = "";
        placeholderOption.textContent = placeholderText || ("Seleziona " + lookup.placeholder.toLowerCase());
        placeholderOption.selected = true;
        lookup.select.appendChild(placeholderOption);

        lookup.items.forEach(function (itemData) {
            const option = document.createElement("option");
            option.value = normalizeApoDisplayValue(itemData && itemData.value);
            option.textContent = normalizeApoDisplayValue(itemData && (itemData.label || itemData.value));
            lookup.select.appendChild(option);
        });
        lookup.select.disabled = isDisabled === true;
    }

    function resetLookup(lookup, placeholderText, isDisabled) {
        lookup.requestId += 1;
        setLookupOptions(lookup, [], placeholderText, isDisabled !== false);
    }

    async function loadLookupOptions(lookup, lookupConfig) {
        lookup.requestId += 1;
        const currentRequestId = lookup.requestId;
        const guardMessage = typeof lookupConfig.guard === "function" ? lookupConfig.guard() : "";

        if (guardMessage) {
            setLookupOptions(lookup, [], guardMessage, true);
            return;
        }

        const params = new URLSearchParams();
        if (typeof lookupConfig.params === "function") {
            lookupConfig.params(params);
        }

        setLookupOptions(lookup, [], "Caricamento...", true);
        try {
            const results = await loadApoSchedaPazienteTerapieLookup(
                lookupConfig.url,
                params,
                lookupConfig.dataKey,
                lookupConfig.valueKeys,
                lookupConfig.errorMessage
            );
            if (currentRequestId !== lookup.requestId) {
                return;
            }
            setLookupOptions(lookup, results, lookupConfig.placeholderText, false);
            if (!results.length) {
                setApoSearchMessage(lookupConfig.emptyText, "warning");
            }
        } catch (error) {
            if (currentRequestId !== lookup.requestId) {
                return;
            }
            setLookupOptions(lookup, [], lookupConfig.errorMessage, true);
            setApoSearchMessage(error && error.message ? error.message : lookupConfig.errorMessage, "danger");
        }
    }

    const principioConfig = {
        url: apoTerapiePrincipiAttiviUrl,
        dataKey: "principiAttivi",
        valueKeys: ["principioAttivo", "pa"],
        placeholderText: "Seleziona principio attivo",
        emptyText: "Nessun principio attivo trovato.",
        errorMessage: "Errore nel caricamento dei principi attivi."
    };
    const farmacoConfig = {
        url: apoTerapieFarmaciUrl,
        dataKey: "farmaci",
        valueKeys: ["farmaco", "f"],
        placeholderText: "Seleziona farmaco",
        emptyText: "Nessun farmaco trovato.",
        errorMessage: "Errore nel caricamento dei farmaci.",
        guard: function () {
            return getLookupValue(principioLookup) ? "" : "Seleziona prima il principio attivo.";
        },
        params: function (params) {
            params.set("principioAttivo", getLookupValue(principioLookup));
        }
    };
    const confezioneConfig = {
        url: apoTerapieConfezioniUrl,
        dataKey: "confezioni",
        valueKeys: ["confezione", "c"],
        placeholderText: "Seleziona confezione",
        emptyText: "Nessuna confezione trovata.",
        errorMessage: "Errore nel caricamento delle confezioni.",
        guard: function () {
            return getLookupValue(farmacoLookup) ? "" : "Seleziona prima il farmaco.";
        },
        params: function (params) {
            params.set("farmaco", getLookupValue(farmacoLookup));
        }
    };

    resetLookup(principioLookup, "Caricamento principi attivi...", true);
    resetLookup(farmacoLookup, "Seleziona prima il principio attivo.", true);
    resetLookup(confezioneLookup, "Seleziona prima il farmaco.", true);

    void loadLookupOptions(principioLookup, principioConfig);

    principioLookup.select.addEventListener("change", function () {
        resetLookup(farmacoLookup, "Seleziona farmaco", true);
        resetLookup(confezioneLookup, "Seleziona prima il farmaco.", true);
        if (getLookupValue(principioLookup)) {
            void loadLookupOptions(farmacoLookup, farmacoConfig);
        }
    });

    farmacoLookup.select.addEventListener("change", function () {
        resetLookup(confezioneLookup, "Seleziona confezione", true);
        if (getLookupValue(farmacoLookup)) {
            void loadLookupOptions(confezioneLookup, confezioneConfig);
        }
    });

    function resetControls() {
        principioLookup.select.value = "";
        resetLookup(farmacoLookup, "Seleziona prima il principio attivo.", true);
        resetLookup(confezioneLookup, "Seleziona prima il farmaco.", true);
        quantitaInput.value = "";
        durataInput.value = "";
        frequenzaSelect.value = "giorno";
        durataSelect.value = "giorni";
        principioLookup.select.focus();
    }

    async function addCurrentTerapia() {
        if (adding) {
            return;
        }
        const terapia = normalizeApoSchedaPazienteTerapia({
            principioAttivo: getLookupValue(principioLookup),
            farmaco: getLookupValue(farmacoLookup),
            confezione: getLookupValue(confezioneLookup),
            quantita: quantitaInput.value,
            frequenzaUnita: frequenzaSelect.value,
            durataValore: durataInput.value,
            durataUnita: normalizeApoDisplayValue(durataInput.value) ? durataSelect.value : ""
        }, 0);

        if (!terapia.farmaco) {
            setApoSearchMessage("Inserisci un farmaco per aggiungere la terapia.", "warning");
            farmacoLookup.select.focus();
            return;
        }

        setAdding(true);
        let added = true;
        try {
            if (typeof onAdd === "function") {
                added = await onAdd(terapia);
            }
        } finally {
            setAdding(false);
        }
        if (added !== false) {
            resetControls();
        }
    }

    [quantitaInput, frequenzaSelect, durataInput, durataSelect].forEach(function (field) {
        field.addEventListener("keydown", function (event) {
            if (event.key === "Enter") {
                event.preventDefault();
                void addCurrentTerapia();
            }
        });
    });
    addButton.addEventListener("click", function () {
        void addCurrentTerapia();
    });

    lookupRow.appendChild(principioLookup.field);
    lookupRow.appendChild(farmacoLookup.field);
    lookupRow.appendChild(confezioneLookup.field);
    doseRow.appendChild(quantitaInput);
    doseRow.appendChild(frequenzaGroup);
    doseRow.appendChild(durataGroup);
    doseRow.appendChild(addButton);
    controls.appendChild(lookupRow);
    controls.appendChild(doseRow);
    return controls;
}

function createApoSchedaPazienteTerapiaOpenButton(accesso) {
    const codPaz = normalizeApoDisplayValue(accesso && accesso.codPaz);
    const form = document.createElement("form");
    form.className = "apo-paziente-terapie-add";

    const addButton = document.createElement("button");
    addButton.type = "submit";
    addButton.className = "btn btn-success";
    addButton.textContent = "Aggiungi nuova terapia";
    addButton.disabled = !codPaz;

    form.addEventListener("submit", function (event) {
        event.preventDefault();
        openApoSchedaPazienteTerapiaModal(accesso);
    });

    form.appendChild(addButton);
    return form;
}

function setApoSchedaPazienteTerapiaSaving(isSaving) {
    const modal = document.getElementById("apo-terapia-modal");
    if (!modal) {
        return;
    }
    const addButton = modal.querySelector(".apo-terapia-save-button");
    if (addButton) {
        addButton.textContent = isSaving === true ? "Aggiungo..." : "Aggiungi terapia";
    }
}

function openApoSchedaPazienteTerapiaModal(accesso) {
    const codPaz = normalizeApoDisplayValue(accesso && accesso.codPaz);
    const modal = document.getElementById("apo-terapia-modal");
    const host = document.getElementById("apo-terapia-form-host");
    if (!modal || !host || !codPaz) {
        setApoSearchMessage("Codice paziente non disponibile.", "warning");
        return;
    }

    apoState.schedaPazienteTerapiaModalLastFocus = document.activeElement;
    apoState.schedaPazienteTerapiaModalCodPaz = codPaz;
    apoState.schedaPazienteTerapiaModalAccesso = accesso;
    modal.dataset.codPaz = codPaz;
    host.innerHTML = "";

    const controls = createApoSchedaPazienteTerapieAddControls(async function (terapia) {
        setApoSchedaPazienteTerapiaSaving(true);
        const saved = await saveApoSchedaPazienteHistoryTerapia(accesso, terapia);
        setApoSchedaPazienteTerapiaSaving(false);
        if (saved) {
            closeApoSchedaPazienteTerapiaModal(true);
        }
        return saved;
    }, {
        className: "apo-scheda-paziente-terapie-add apo-terapia-modal-controls",
        buttonClass: "apo-terapia-save-button",
        buttonText: "Aggiungi terapia",
        addingText: "Aggiungo..."
    });
    host.appendChild(controls);

    setApoSchedaPazienteTerapiaSaving(false);
    modal.classList.remove("is-hidden");
    modal.setAttribute("aria-hidden", "false");
    const firstField = host.querySelector("select:not([disabled]), input:not([disabled])");
    if (firstField) {
        firstField.focus();
    }
}

function closeApoSchedaPazienteTerapiaModal(restoreFocus) {
    const modal = document.getElementById("apo-terapia-modal");
    const host = document.getElementById("apo-terapia-form-host");
    if (modal) {
        modal.classList.add("is-hidden");
        modal.setAttribute("aria-hidden", "true");
        delete modal.dataset.codPaz;
    }
    if (host) {
        host.innerHTML = "";
    }
    apoState.schedaPazienteTerapiaModalCodPaz = "";
    apoState.schedaPazienteTerapiaModalAccesso = null;
    setApoSchedaPazienteTerapiaSaving(false);

    if (restoreFocus && apoState.schedaPazienteTerapiaModalLastFocus
            && typeof apoState.schedaPazienteTerapiaModalLastFocus.focus === "function") {
        apoState.schedaPazienteTerapiaModalLastFocus.focus();
    }
}

function addApoSchedaPazienteTerapiaToList(items, terapia) {
    const normalized = normalizeApoSchedaPazienteTerapia(terapia, items.length);
    const key = makeApoSchedaPazienteTerapiaKey(normalized);
    const exists = items.some(function (selected) {
        return makeApoSchedaPazienteTerapiaKey(selected) === key;
    });
    if (!exists) {
        items.push(normalized);
    }
}

function removeApoSchedaPazienteTerapiaFromList(items, terapia) {
    const key = makeApoSchedaPazienteTerapiaKey(terapia);
    return items.filter(function (selected) {
        return makeApoSchedaPazienteTerapiaKey(selected) !== key;
    });
}

function createApoSchedaPazienteHistoryPatologieAddControls(accesso) {
    const codPaz = normalizeApoDisplayValue(accesso && accesso.codPaz);
    const form = document.createElement("form");
    form.className = "apo-paziente-patologie-add";

    const field = document.createElement("div");
    field.className = "apo-paziente-patologie-add-field";

    const input = document.createElement("input");
    input.type = "search";
    input.className = "form-control apo-paziente-patologie-add-input";
    input.placeholder = "Cerca patologia ICD-10";
    input.autocomplete = "off";

    const suggestionsList = document.createElement("div");
    suggestionsList.className = "apo-paziente-patologie-suggestions is-hidden";

    const addButton = document.createElement("button");
    addButton.type = "submit";
    addButton.className = "btn btn-success";
    addButton.textContent = "Aggiungi";
    addButton.disabled = !codPaz;

    let suggestions = [];
    let selectedSuggestion = null;
    let searchTimer = null;
    let requestId = 0;
    let adding = false;

    function setAdding(isAdding) {
        adding = isAdding === true;
        input.disabled = adding;
        addButton.disabled = adding || !codPaz;
        addButton.textContent = adding ? "Aggiungo..." : "Aggiungi";
    }

    function hideSuggestions() {
        suggestionsList.classList.add("is-hidden");
        suggestionsList.innerHTML = "";
    }

    function renderSuggestions(items, emptyText) {
        suggestionsList.innerHTML = "";
        if (!items.length) {
            const empty = document.createElement("div");
            empty.className = "apo-paziente-patologie-suggestion-empty";
            empty.textContent = emptyText || "Nessuna patologia trovata.";
            suggestionsList.appendChild(empty);
            suggestionsList.classList.remove("is-hidden");
            return;
        }

        items.slice(0, 8).forEach(function (itemData) {
            const button = document.createElement("button");
            button.type = "button";
            button.className = "apo-paziente-patologie-suggestion";
            button.textContent = formatApoSchedaPazientePatologiaSuggestion(itemData);
            button.addEventListener("mousedown", function (event) {
                event.preventDefault();
            });
            button.addEventListener("click", function () {
                selectedSuggestion = itemData;
                input.value = formatApoSchedaPazientePatologiaSuggestion(itemData);
                hideSuggestions();
                void addSelectedPatologia();
            });
            suggestionsList.appendChild(button);
        });
        suggestionsList.classList.remove("is-hidden");
    }

    function filterSuggestions(filterValue, source) {
        const filtro = normalizeApoValue(filterValue);
        if (!filtro) {
            return [];
        }
        return source.filter(function (itemData) {
            return normalizeApoValue(itemData.codice).indexOf(filtro) !== -1
                || normalizeApoValue(itemData.descrizione).indexOf(filtro) !== -1
                || normalizeApoValue(itemData.categoria).indexOf(filtro) !== -1
                || normalizeApoValue(itemData.codiceCategoria).indexOf(filtro) !== -1;
        });
    }

    async function searchSuggestions() {
        const filtro = normalizeApoDisplayValue(input.value);
        selectedSuggestion = null;
        requestId += 1;
        const currentRequestId = requestId;

        if (filtro.length < 2) {
            suggestions = [];
            hideSuggestions();
            return;
        }

        renderSuggestions([], "Ricerca in corso...");
        try {
            const response = await fetch(apoPatologieCronicheIcd10Url + "?filtro=" + encodeURIComponent(filtro), {
                headers: {
                    "Accept": "application/json"
                }
            });
            if (response.status === 401) {
                window.location.href = apoLoginUrl;
                return;
            }

            let payload = null;
            try {
                payload = await response.json();
            } catch (error) {
                payload = null;
            }
            if (!response.ok || !payload || payload.esito !== "ok") {
                throw new Error(payload && payload.message ? payload.message : "Errore nella ricerca delle patologie.");
            }
            if (currentRequestId !== requestId) {
                return;
            }
            const source = normalizeApoSchedaPazientePatologie(payload.data && payload.data.patologie);
            suggestions = filterSuggestions(filtro, source);
            renderSuggestions(suggestions, "Nessuna patologia trovata.");
        } catch (error) {
            if (currentRequestId !== requestId) {
                return;
            }
            suggestions = [];
            renderSuggestions([], error && error.message ? error.message : "Errore nella ricerca delle patologie.");
        }
    }

    async function addSelectedPatologia() {
        if (adding) {
            return;
        }
        const codicePatologia = selectedSuggestion
            ? normalizeApoDisplayValue(selectedSuggestion.codice)
            : getApoSchedaPazientePatologiaCodeFromInput(input.value, suggestions);
        if (!codPaz || !codicePatologia) {
            setApoSearchMessage("Seleziona una patologia cronica da aggiungere.", "warning");
            input.focus();
            return;
        }

        setAdding(true);
        const saved = await saveApoSchedaPazienteHistoryPatologia(accesso, codicePatologia);
        setAdding(false);
        if (saved) {
            input.value = "";
            selectedSuggestion = null;
            suggestions = [];
            hideSuggestions();
        }
    }

    input.addEventListener("input", function () {
        if (searchTimer) {
            window.clearTimeout(searchTimer);
        }
        searchTimer = window.setTimeout(function () {
            void searchSuggestions();
        }, 250);
    });
    input.addEventListener("focus", function () {
        if (suggestions.length) {
            renderSuggestions(suggestions);
        }
    });
    input.addEventListener("blur", function () {
        window.setTimeout(hideSuggestions, 150);
    });
    input.addEventListener("keydown", function (event) {
        if (event.key === "Enter") {
            event.preventDefault();
            if (!selectedSuggestion && suggestions.length) {
                selectedSuggestion = suggestions[0];
                input.value = formatApoSchedaPazientePatologiaSuggestion(selectedSuggestion);
            }
            void addSelectedPatologia();
        }
    });
    form.addEventListener("submit", function (event) {
        event.preventDefault();
        void addSelectedPatologia();
    });

    field.appendChild(input);
    field.appendChild(suggestionsList);
    form.appendChild(field);
    form.appendChild(addButton);
    return form;
}

function renderApoSchedaPazienteHistoryPatologie(body, accesso) {
    const codPaz = normalizeApoDisplayValue(accesso && accesso.codPaz);
    if (!codPaz) {
        const emptyState = document.createElement("div");
        emptyState.className = "empty-state";
        emptyState.textContent = "Codice paziente non disponibile.";
        body.appendChild(emptyState);
        return;
    }

    body.appendChild(createApoSchedaPazienteHistoryPatologieAddControls(accesso));

    if (apoState.schedaPazienteHistoryPatologieCodPaz !== codPaz && !apoState.schedaPazienteHistoryPatologieLoading) {
        void loadApoSchedaPazientePatologieForSelectedPaziente(accesso);
    }

    if (apoState.schedaPazienteHistoryPatologieLoading || apoState.schedaPazienteHistoryPatologieCodPaz !== codPaz) {
        body.appendChild(createApoSpinnerLoader("Caricamento patologie croniche...", "apo-storico-loader"));
        return;
    }

    if (apoState.schedaPazienteHistoryPatologieError) {
        const emptyState = document.createElement("div");
        emptyState.className = "empty-state";
        emptyState.textContent = apoState.schedaPazienteHistoryPatologieError;
        body.appendChild(emptyState);
        return;
    }

    if (!apoState.schedaPazienteHistoryPatologie.length) {
        const emptyState = document.createElement("div");
        emptyState.className = "empty-state";
        emptyState.textContent = "Nessuna patologia cronica disponibile.";
        body.appendChild(emptyState);
        return;
    }

    const list = document.createElement("div");
    list.className = "apo-paziente-history-list";
    apoState.schedaPazienteHistoryPatologie.forEach(function (item) {
        const row = document.createElement("article");
        row.className = "apo-paziente-history-item apo-paziente-history-item-static apo-paziente-patologia-row";

        const header = document.createElement("div");
        header.className = "apo-paziente-patologia-row-header";

        const code = document.createElement("span");
        code.className = "apo-paziente-history-date";
        code.textContent = item.codice || "-";

        const deleteButton = document.createElement("button");
        deleteButton.type = "button";
        deleteButton.className = "apo-paziente-patologia-delete-button";
        deleteButton.textContent = "X";
        deleteButton.title = "Elimina patologia";
        deleteButton.setAttribute("aria-label", "Elimina patologia " + (item.codice || ""));
        deleteButton.disabled = !normalizeApoDisplayValue(item.id);
        deleteButton.addEventListener("click", function (event) {
            event.preventDefault();
            event.stopPropagation();
            deleteButton.disabled = true;
            void deleteApoSchedaPazienteHistoryPatologia(accesso, item);
        });

        const description = document.createElement("strong");
        description.className = "apo-paziente-history-title";
        description.textContent = formatApoSchedaPazienteAllergiaHistoryDescription(item) || "-";

        header.appendChild(code);
        header.appendChild(deleteButton);
        row.appendChild(header);
        row.appendChild(description);
        appendApoSchedaPazienteUtenteIns(row, item, "apo-paziente-history-inseritore");
        list.appendChild(row);
    });
    body.appendChild(list);
}

function createApoSchedaPazienteHistoryAllergieAddControls(accesso) {
    const codPaz = normalizeApoDisplayValue(accesso && accesso.codPaz);
    const form = document.createElement("form");
    form.className = "apo-paziente-patologie-add";

    const field = document.createElement("div");
    field.className = "apo-paziente-patologie-add-field";

    const input = document.createElement("input");
    input.type = "search";
    input.className = "form-control apo-paziente-patologie-add-input";
    input.placeholder = "Cerca allergia";
    input.autocomplete = "off";

    const suggestionsList = document.createElement("div");
    suggestionsList.className = "apo-paziente-patologie-suggestions is-hidden";

    const addButton = document.createElement("button");
    addButton.type = "submit";
    addButton.className = "btn btn-success";
    addButton.textContent = "Aggiungi";
    addButton.disabled = !codPaz;

    let suggestions = [];
    let selectedSuggestion = null;
    let searchTimer = null;
    let requestId = 0;
    let adding = false;

    function setAdding(isAdding) {
        adding = isAdding === true;
        input.disabled = adding;
        addButton.disabled = adding || !codPaz;
        addButton.textContent = adding ? "Aggiungo..." : "Aggiungi";
    }

    function hideSuggestions() {
        suggestionsList.classList.add("is-hidden");
        suggestionsList.innerHTML = "";
    }

    function renderSuggestions(items, emptyText) {
        suggestionsList.innerHTML = "";
        if (!items.length) {
            const empty = document.createElement("div");
            empty.className = "apo-paziente-patologie-suggestion-empty";
            empty.textContent = emptyText || "Nessuna allergia trovata.";
            suggestionsList.appendChild(empty);
            suggestionsList.classList.remove("is-hidden");
            return;
        }

        items.slice(0, 8).forEach(function (itemData) {
            const button = document.createElement("button");
            button.type = "button";
            button.className = "apo-paziente-patologie-suggestion";
            button.textContent = formatApoSchedaPazienteAllergiaSuggestion(itemData);
            button.addEventListener("mousedown", function (event) {
                event.preventDefault();
            });
            button.addEventListener("click", function () {
                selectedSuggestion = itemData;
                input.value = formatApoSchedaPazienteAllergiaSuggestion(itemData);
                hideSuggestions();
                void addSelectedAllergia();
            });
            suggestionsList.appendChild(button);
        });
        suggestionsList.classList.remove("is-hidden");
    }

    function filterSuggestions(filterValue, source) {
        const filtro = normalizeApoValue(filterValue);
        if (!filtro) {
            return [];
        }
        return source.filter(function (itemData) {
            return normalizeApoValue(itemData.codice).indexOf(filtro) !== -1
                || normalizeApoValue(itemData.descrizione).indexOf(filtro) !== -1;
        });
    }

    async function searchSuggestions() {
        const filtro = normalizeApoDisplayValue(input.value);
        selectedSuggestion = null;
        requestId += 1;
        const currentRequestId = requestId;

        if (filtro.length < 2) {
            suggestions = [];
            hideSuggestions();
            return;
        }

        renderSuggestions([], "Ricerca in corso...");
        try {
            const response = await fetch(apoAllergieUrl + "?filtro=" + encodeURIComponent(filtro), {
                headers: {
                    "Accept": "application/json"
                }
            });
            if (response.status === 401) {
                window.location.href = apoLoginUrl;
                return;
            }

            let payload = null;
            try {
                payload = await response.json();
            } catch (error) {
                payload = null;
            }
            if (!response.ok || !payload || payload.esito !== "ok") {
                throw new Error(payload && payload.message ? payload.message : "Errore nella ricerca delle allergie.");
            }
            if (currentRequestId !== requestId) {
                return;
            }
            const source = normalizeApoSchedaPazienteAllergie(payload.data && payload.data.allergie);
            suggestions = filterSuggestions(filtro, source);
            renderSuggestions(suggestions, "Nessuna allergia trovata.");
        } catch (error) {
            if (currentRequestId !== requestId) {
                return;
            }
            suggestions = [];
            renderSuggestions([], error && error.message ? error.message : "Errore nella ricerca delle allergie.");
        }
    }

    async function addSelectedAllergia() {
        if (adding) {
            return;
        }
        const codiceAllergia = selectedSuggestion
            ? normalizeApoDisplayValue(selectedSuggestion.codice)
            : getApoSchedaPazienteAllergiaCodeFromInput(input.value, suggestions);
        if (!codPaz || !codiceAllergia) {
            setApoSearchMessage("Seleziona un'allergia da aggiungere.", "warning");
            input.focus();
            return;
        }

        setAdding(true);
        const saved = await saveApoSchedaPazienteHistoryAllergia(accesso, codiceAllergia);
        setAdding(false);
        if (saved) {
            input.value = "";
            selectedSuggestion = null;
            suggestions = [];
            hideSuggestions();
        }
    }

    input.addEventListener("input", function () {
        if (searchTimer) {
            window.clearTimeout(searchTimer);
        }
        searchTimer = window.setTimeout(function () {
            void searchSuggestions();
        }, 250);
    });
    input.addEventListener("focus", function () {
        if (suggestions.length) {
            renderSuggestions(suggestions);
        }
    });
    input.addEventListener("blur", function () {
        window.setTimeout(hideSuggestions, 150);
    });
    input.addEventListener("keydown", function (event) {
        if (event.key === "Enter") {
            event.preventDefault();
            if (!selectedSuggestion && suggestions.length) {
                selectedSuggestion = suggestions[0];
                input.value = formatApoSchedaPazienteAllergiaSuggestion(selectedSuggestion);
            }
            void addSelectedAllergia();
        }
    });
    form.addEventListener("submit", function (event) {
        event.preventDefault();
        void addSelectedAllergia();
    });

    field.appendChild(input);
    field.appendChild(suggestionsList);
    form.appendChild(field);
    form.appendChild(addButton);
    return form;
}

function renderApoSchedaPazienteHistoryAllergie(body, accesso) {
    const codPaz = normalizeApoDisplayValue(accesso && accesso.codPaz);
    if (!codPaz) {
        const emptyState = document.createElement("div");
        emptyState.className = "empty-state";
        emptyState.textContent = "Codice paziente non disponibile.";
        body.appendChild(emptyState);
        return;
    }

    body.appendChild(createApoSchedaPazienteHistoryAllergieAddControls(accesso));

    if (apoState.schedaPazienteHistoryAllergieCodPaz !== codPaz && !apoState.schedaPazienteHistoryAllergieLoading) {
        void loadApoSchedaPazienteAllergieForSelectedPaziente(accesso);
    }

    if (apoState.schedaPazienteHistoryAllergieLoading || apoState.schedaPazienteHistoryAllergieCodPaz !== codPaz) {
        body.appendChild(createApoSpinnerLoader("Caricamento allergie...", "apo-storico-loader"));
        return;
    }

    if (apoState.schedaPazienteHistoryAllergieError) {
        const emptyState = document.createElement("div");
        emptyState.className = "empty-state";
        emptyState.textContent = apoState.schedaPazienteHistoryAllergieError;
        body.appendChild(emptyState);
        return;
    }

    if (!apoState.schedaPazienteHistoryAllergie.length) {
        const emptyState = document.createElement("div");
        emptyState.className = "empty-state";
        emptyState.textContent = "Nessuna allergia disponibile.";
        body.appendChild(emptyState);
        return;
    }

    const list = document.createElement("div");
    list.className = "apo-paziente-history-list";
    apoState.schedaPazienteHistoryAllergie.forEach(function (item) {
        const row = document.createElement("article");
        row.className = "apo-paziente-history-item apo-paziente-history-item-static apo-paziente-patologia-row";

        const header = document.createElement("div");
        header.className = "apo-paziente-patologia-row-header";

        const code = document.createElement("span");
        code.className = "apo-paziente-history-date";
        code.textContent = item.codice || "-";

        const deleteButton = document.createElement("button");
        deleteButton.type = "button";
        deleteButton.className = "apo-paziente-patologia-delete-button";
        deleteButton.textContent = "X";
        deleteButton.title = "Elimina allergia";
        deleteButton.setAttribute("aria-label", "Elimina allergia " + (item.codice || ""));
        deleteButton.disabled = !normalizeApoDisplayValue(item.id);
        deleteButton.addEventListener("click", function (event) {
            event.preventDefault();
            event.stopPropagation();
            deleteButton.disabled = true;
            void deleteApoSchedaPazienteHistoryAllergia(accesso, item);
        });

        const description = document.createElement("strong");
        description.className = "apo-paziente-history-title";
        description.textContent = item.descrizione || "-";

        header.appendChild(code);
        header.appendChild(deleteButton);
        row.appendChild(header);
        row.appendChild(description);
        appendApoSchedaPazienteUtenteIns(row, item, "apo-paziente-history-inseritore");
        list.appendChild(row);
    });
    body.appendChild(list);
}

function renderApoSchedaPazienteHistoryTerapie(body, accesso) {
    const codPaz = normalizeApoDisplayValue(accesso && accesso.codPaz);
    if (!codPaz) {
        const emptyState = document.createElement("div");
        emptyState.className = "empty-state";
        emptyState.textContent = "Codice paziente non disponibile.";
        body.appendChild(emptyState);
        return;
    }

    body.appendChild(createApoSchedaPazienteTerapiaOpenButton(accesso));

    if (apoState.schedaPazienteHistoryTerapieCodPaz !== codPaz && !apoState.schedaPazienteHistoryTerapieLoading) {
        void loadApoSchedaPazienteTerapieForSelectedPaziente(accesso);
    }

    if (apoState.schedaPazienteHistoryTerapieLoading || apoState.schedaPazienteHistoryTerapieCodPaz !== codPaz) {
        body.appendChild(createApoSpinnerLoader("Caricamento terapie...", "apo-storico-loader"));
        return;
    }

    if (apoState.schedaPazienteHistoryTerapieError) {
        const emptyState = document.createElement("div");
        emptyState.className = "empty-state";
        emptyState.textContent = apoState.schedaPazienteHistoryTerapieError;
        body.appendChild(emptyState);
        return;
    }

    if (!apoState.schedaPazienteHistoryTerapie.length) {
        const emptyState = document.createElement("div");
        emptyState.className = "empty-state";
        emptyState.textContent = "Nessuna terapia in corso disponibile.";
        body.appendChild(emptyState);
        return;
    }

    const list = document.createElement("div");
    list.className = "apo-paziente-history-list apo-paziente-terapie-list";
    apoState.schedaPazienteHistoryTerapie.forEach(function (item) {
        const row = document.createElement("article");
        row.className = "apo-paziente-history-item apo-paziente-history-item-static apo-paziente-patologia-row apo-paziente-terapia-row";

        const header = document.createElement("div");
        header.className = "apo-paziente-patologia-row-header";

        const metaBlock = document.createElement("div");
        metaBlock.className = "apo-paziente-history-meta-block";

        const title = document.createElement("span");
        title.className = "apo-paziente-history-date";
        title.textContent = formatApoSchedaPazienteTerapiaTitle(item) || item.farmaco || "-";
        metaBlock.appendChild(title);

        const periodo = formatApoSchedaPazienteTerapiaPeriodo(item);
        if (periodo) {
            const periodoElement = document.createElement("span");
            periodoElement.className = "apo-paziente-history-inseritore apo-paziente-terapia-periodo";
            periodoElement.textContent = periodo;
            metaBlock.appendChild(periodoElement);
        }
        appendApoSchedaPazienteUtenteIns(metaBlock, item, "apo-paziente-history-inseritore");

        const deleteButton = document.createElement("button");
        deleteButton.type = "button";
        deleteButton.className = "apo-paziente-patologia-delete-button";
        deleteButton.textContent = "X";
        deleteButton.title = "Elimina terapia";
        deleteButton.setAttribute("aria-label", "Elimina terapia " + (item.farmaco || ""));
        deleteButton.addEventListener("click", function (event) {
            event.preventDefault();
            event.stopPropagation();
            deleteButton.disabled = true;
            void deleteApoSchedaPazienteHistoryTerapia(accesso, item);
        });

        const description = document.createElement("strong");
        description.className = "apo-paziente-history-title";
        description.textContent = formatApoSchedaPazienteTerapiaDescription(item) || "-";

        header.appendChild(metaBlock);
        header.appendChild(deleteButton);
        row.appendChild(header);
        row.appendChild(description);
        list.appendChild(row);
    });
    body.appendChild(list);
}

function renderApoSchedaPazienteHistorySection() {
    const body = document.getElementById("apo-paziente-scheda-body");
    const accesso = getApoSelectedAccesso();
    const activeConfig = getApoSchedaPazienteHistorySectionConfig(apoState.activeSchedaPazienteHistorySection);

    renderApoSchedaPazienteHistoryTabs();
    if (!body) {
        return;
    }

    body.innerHTML = "";
    if (!accesso) {
        const emptyState = document.createElement("div");
        emptyState.className = "empty-state";
        emptyState.textContent = "Nessun paziente selezionato.";
        body.appendChild(emptyState);
        return;
    }

    if (activeConfig.id === "diario") {
        renderApoSchedaPazienteDiario(body, accesso);
        return;
    }

    if (activeConfig.id === "patologie-croniche") {
        renderApoSchedaPazienteHistoryPatologie(body, accesso);
        return;
    }

    if (activeConfig.id === "allergie") {
        renderApoSchedaPazienteHistoryAllergie(body, accesso);
        return;
    }

    if (activeConfig.id === "terapie") {
        renderApoSchedaPazienteHistoryTerapie(body, accesso);
        return;
    }

    renderApoSchedaPazienteHistoryPlaceholder(body, activeConfig);
}

function setApoSchedaPazienteHistorySection(sectionId) {
    const config = getApoSchedaPazienteHistorySectionConfig(sectionId);
    apoState.activeSchedaPazienteHistorySection = config.id;
    renderApoSchedaPazienteHistorySection();
}

async function loadApoSchedaPazienteDiarioForSelectedPaziente(accesso) {
    const codPaz = normalizeApoDisplayValue(accesso && accesso.codPaz);
    if (!codPaz) {
        clearApoSchedaPazienteDiario();
        renderApoSchedaPazienteHistorySection();
        return;
    }

    if (apoState.schedaPazienteDiarioCodPaz === codPaz
            && (apoState.schedaPazienteDiarioLoading
                || apoState.schedaPazienteDiarioLoaded
                || apoState.schedaPazienteDiarioError)) {
        renderApoSchedaPazienteHistorySection();
        return;
    }

    const requestId = apoState.schedaPazienteDiarioRequestId + 1;
    const params = new URLSearchParams();
    params.set("codPaz", codPaz);

    apoState.schedaPazienteDiarioRequestId = requestId;
    apoState.schedaPazienteDiarioCodPaz = codPaz;
    apoState.schedaPazienteDiario = [];
    apoState.schedaPazienteDiarioError = "";
    apoState.schedaPazienteDiarioLoading = true;
    apoState.schedaPazienteDiarioLoaded = false;
    renderApoSchedaPazienteHistorySection();

    try {
        const response = await fetch(apoSchedaPazienteDiarioUrl + "?" + params.toString(), {
            headers: {
                "Accept": "application/json"
            }
        });

        if (response.status === 401) {
            window.location.href = apoLoginUrl;
            return;
        }

        let payload = null;
        try {
            payload = await response.json();
        } catch (error) {
            payload = null;
        }

        if (!response.ok || !payload || payload.esito !== "ok") {
            throw new Error(payload && payload.message ? payload.message : "Errore nel caricamento del diario.");
        }

        if (apoState.schedaPazienteDiarioRequestId !== requestId
                || apoState.schedaPazienteDiarioCodPaz !== codPaz) {
            return;
        }

        apoState.schedaPazienteDiario = normalizeApoSchedaPazienteDiario(payload.data && payload.data.diario);
        apoState.schedaPazienteDiarioError = "";
        apoState.schedaPazienteDiarioLoaded = true;
    } catch (error) {
        if (apoState.schedaPazienteDiarioRequestId !== requestId) {
            return;
        }
        apoState.schedaPazienteDiario = [];
        apoState.schedaPazienteDiarioError = error && error.message ? error.message : "Errore nel caricamento del diario.";
        apoState.schedaPazienteDiarioLoaded = true;
    } finally {
        if (apoState.schedaPazienteDiarioRequestId === requestId) {
            apoState.schedaPazienteDiarioLoading = false;
            renderApoSchedaPazienteHistorySection();
        }
    }
}

async function loadApoSchedaPazientePatologieForSelectedPaziente(accesso) {
    const codPaz = normalizeApoDisplayValue(accesso && accesso.codPaz);
    if (!codPaz) {
        clearApoSchedaPazienteHistoryPatologie();
        renderApoSchedaPazienteHistorySection();
        return;
    }

    if (apoState.schedaPazienteHistoryPatologieCodPaz === codPaz
            && (apoState.schedaPazienteHistoryPatologieLoading
                || apoState.schedaPazienteHistoryPatologieLoaded
                || apoState.schedaPazienteHistoryPatologieError)) {
        renderApoSchedaPazienteHistorySection();
        return;
    }

    const requestId = apoState.schedaPazienteHistoryPatologieRequestId + 1;
    const params = new URLSearchParams();
    params.set("codPaz", codPaz);

    apoState.schedaPazienteHistoryPatologieRequestId = requestId;
    apoState.schedaPazienteHistoryPatologieCodPaz = codPaz;
    apoState.schedaPazienteHistoryPatologie = [];
    apoState.schedaPazienteHistoryPatologieError = "";
    apoState.schedaPazienteHistoryPatologieLoading = true;
    apoState.schedaPazienteHistoryPatologieLoaded = false;
    renderApoSchedaPazienteHistorySection();

    try {
        const response = await fetch(apoSchedaPazientePatologieUrl + "?" + params.toString(), {
            headers: {
                "Accept": "application/json"
            }
        });

        if (response.status === 401) {
            window.location.href = apoLoginUrl;
            return;
        }

        let payload = null;
        try {
            payload = await response.json();
        } catch (error) {
            payload = null;
        }

        if (!response.ok || !payload || payload.esito !== "ok") {
            throw new Error(payload && payload.message ? payload.message : "Errore nel caricamento delle patologie croniche.");
        }

        if (apoState.schedaPazienteHistoryPatologieRequestId !== requestId
                || apoState.schedaPazienteHistoryPatologieCodPaz !== codPaz) {
            return;
        }

        apoState.schedaPazienteHistoryPatologie = normalizeApoSchedaPazienteHistoryPatologie(payload.data && payload.data.patologie);
        apoState.schedaPazienteHistoryPatologieError = "";
        apoState.schedaPazienteHistoryPatologieLoaded = true;
    } catch (error) {
        if (apoState.schedaPazienteHistoryPatologieRequestId !== requestId) {
            return;
        }
        apoState.schedaPazienteHistoryPatologie = [];
        apoState.schedaPazienteHistoryPatologieError = error && error.message ? error.message : "Errore nel caricamento delle patologie croniche.";
        apoState.schedaPazienteHistoryPatologieLoaded = true;
    } finally {
        if (apoState.schedaPazienteHistoryPatologieRequestId === requestId) {
            apoState.schedaPazienteHistoryPatologieLoading = false;
            renderApoSchedaPazienteHistorySection();
        }
    }
}

async function saveApoSchedaPazienteHistoryPatologia(accesso, codicePatologia) {
    const codPaz = normalizeApoDisplayValue(accesso && accesso.codPaz);
    const codice = normalizeApoDisplayValue(codicePatologia);
    if (!codPaz || !codice) {
        setApoSearchMessage("Codice paziente o patologia non disponibile.", "warning");
        return false;
    }

    const params = new URLSearchParams();
    params.set("codPaz", codPaz);
    params.set("codicePatologia", codice);

    try {
        const response = await fetch(apoSchedaPazientePatologieUrl, {
            method: "POST",
            headers: {
                "Accept": "application/json",
                "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8"
            },
            body: params.toString()
        });

        if (response.status === 401) {
            window.location.href = apoLoginUrl;
            return false;
        }

        let payload = null;
        try {
            payload = await response.json();
        } catch (error) {
            payload = null;
        }

        if (!response.ok || !payload || payload.esito !== "ok") {
            throw new Error(payload && payload.message ? payload.message : "Errore nel salvataggio della patologia cronica.");
        }

        apoState.schedaPazienteHistoryPatologieCodPaz = "";
        apoState.schedaPazienteHistoryPatologieLoaded = false;
        apoState.schedaPazienteHistoryPatologieError = "";
        setApoSearchMessage(payload.message || "Patologia cronica aggiunta correttamente.", "success");
        void loadApoSchedaPazientePatologieForSelectedPaziente(accesso);
        return true;
    } catch (error) {
        setApoSearchMessage(error && error.message ? error.message : "Errore nel salvataggio della patologia cronica.", "danger");
        return false;
    }
}

async function deleteApoSchedaPazienteHistoryPatologia(accesso, item) {
    const codPaz = normalizeApoDisplayValue(accesso && accesso.codPaz);
    const idPatologia = normalizeApoDisplayValue(item && item.id);
    if (!codPaz || !idPatologia) {
        setApoSearchMessage("ID patologia o codice paziente non disponibile.", "warning");
        return false;
    }

    const params = new URLSearchParams();
    params.set("id", idPatologia);
    params.set("codPaz", codPaz);

    try {
        const response = await fetch(apoSchedaPazientePatologieUrl + "?" + params.toString(), {
            method: "DELETE",
            headers: {
                "Accept": "application/json"
            }
        });

        if (response.status === 401) {
            window.location.href = apoLoginUrl;
            return false;
        }

        let payload = null;
        try {
            payload = await response.json();
        } catch (error) {
            payload = null;
        }

        if (!response.ok || !payload || payload.esito !== "ok") {
            throw new Error(payload && payload.message ? payload.message : "Errore nell'eliminazione della patologia cronica.");
        }

        const deleted = payload.data && Number(payload.data.deleted) > 0;
        apoState.schedaPazienteHistoryPatologieCodPaz = "";
        apoState.schedaPazienteHistoryPatologieLoaded = false;
        apoState.schedaPazienteHistoryPatologieError = "";
        setApoSearchMessage(payload.message || "Patologia cronica eliminata correttamente.", deleted ? "success" : "warning");
        void loadApoSchedaPazientePatologieForSelectedPaziente(accesso);
        return true;
    } catch (error) {
        setApoSearchMessage(error && error.message ? error.message : "Errore nell'eliminazione della patologia cronica.", "danger");
        renderApoSchedaPazienteHistorySection();
        return false;
    }
}

async function loadApoSchedaPazienteAllergieForSelectedPaziente(accesso) {
    const codPaz = normalizeApoDisplayValue(accesso && accesso.codPaz);
    if (!codPaz) {
        clearApoSchedaPazienteHistoryAllergie();
        renderApoSchedaPazienteHistorySection();
        return;
    }

    if (apoState.schedaPazienteHistoryAllergieCodPaz === codPaz
            && (apoState.schedaPazienteHistoryAllergieLoading
                || apoState.schedaPazienteHistoryAllergieLoaded
                || apoState.schedaPazienteHistoryAllergieError)) {
        renderApoSchedaPazienteHistorySection();
        return;
    }

    const requestId = apoState.schedaPazienteHistoryAllergieRequestId + 1;
    const params = new URLSearchParams();
    params.set("codPaz", codPaz);

    apoState.schedaPazienteHistoryAllergieRequestId = requestId;
    apoState.schedaPazienteHistoryAllergieCodPaz = codPaz;
    apoState.schedaPazienteHistoryAllergie = [];
    apoState.schedaPazienteHistoryAllergieError = "";
    apoState.schedaPazienteHistoryAllergieLoading = true;
    apoState.schedaPazienteHistoryAllergieLoaded = false;
    renderApoSchedaPazienteHistorySection();

    try {
        const response = await fetch(apoSchedaPazienteAllergieUrl + "?" + params.toString(), {
            headers: {
                "Accept": "application/json"
            }
        });

        if (response.status === 401) {
            window.location.href = apoLoginUrl;
            return;
        }

        let payload = null;
        try {
            payload = await response.json();
        } catch (error) {
            payload = null;
        }

        if (!response.ok || !payload || payload.esito !== "ok") {
            throw new Error(payload && payload.message ? payload.message : "Errore nel caricamento delle allergie.");
        }

        if (apoState.schedaPazienteHistoryAllergieRequestId !== requestId
                || apoState.schedaPazienteHistoryAllergieCodPaz !== codPaz) {
            return;
        }

        apoState.schedaPazienteHistoryAllergie = normalizeApoSchedaPazienteAllergie(payload.data && payload.data.allergie);
        apoState.schedaPazienteHistoryAllergieError = "";
        apoState.schedaPazienteHistoryAllergieLoaded = true;
    } catch (error) {
        if (apoState.schedaPazienteHistoryAllergieRequestId !== requestId) {
            return;
        }
        apoState.schedaPazienteHistoryAllergie = [];
        apoState.schedaPazienteHistoryAllergieError = error && error.message ? error.message : "Errore nel caricamento delle allergie.";
        apoState.schedaPazienteHistoryAllergieLoaded = true;
    } finally {
        if (apoState.schedaPazienteHistoryAllergieRequestId === requestId) {
            apoState.schedaPazienteHistoryAllergieLoading = false;
            renderApoSchedaPazienteHistorySection();
        }
    }
}

async function saveApoSchedaPazienteHistoryAllergia(accesso, codiceAllergia) {
    const codPaz = normalizeApoDisplayValue(accesso && accesso.codPaz);
    const codice = normalizeApoDisplayValue(codiceAllergia);
    if (!codPaz || !codice) {
        setApoSearchMessage("Codice paziente o allergia non disponibile.", "warning");
        return false;
    }

    const params = new URLSearchParams();
    params.set("codPaz", codPaz);
    params.set("codiceAllergia", codice);

    try {
        const response = await fetch(apoSchedaPazienteAllergieUrl, {
            method: "POST",
            headers: {
                "Accept": "application/json",
                "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8"
            },
            body: params.toString()
        });

        if (response.status === 401) {
            window.location.href = apoLoginUrl;
            return false;
        }

        let payload = null;
        try {
            payload = await response.json();
        } catch (error) {
            payload = null;
        }

        if (!response.ok || !payload || payload.esito !== "ok") {
            throw new Error(payload && payload.message ? payload.message : "Errore nel salvataggio dell'allergia.");
        }

        apoState.schedaPazienteHistoryAllergieCodPaz = "";
        apoState.schedaPazienteHistoryAllergieLoaded = false;
        apoState.schedaPazienteHistoryAllergieError = "";
        setApoSearchMessage(payload.message || "Allergia aggiunta correttamente.", "success");
        void loadApoSchedaPazienteAllergieForSelectedPaziente(accesso);
        return true;
    } catch (error) {
        setApoSearchMessage(error && error.message ? error.message : "Errore nel salvataggio dell'allergia.", "danger");
        return false;
    }
}

async function deleteApoSchedaPazienteHistoryAllergia(accesso, item) {
    const codPaz = normalizeApoDisplayValue(accesso && accesso.codPaz);
    const idAllergia = normalizeApoDisplayValue(item && item.id);
    if (!codPaz || !idAllergia) {
        setApoSearchMessage("ID allergia o codice paziente non disponibile.", "warning");
        return false;
    }

    const params = new URLSearchParams();
    params.set("id", idAllergia);
    params.set("codPaz", codPaz);

    try {
        const response = await fetch(apoSchedaPazienteAllergieUrl + "?" + params.toString(), {
            method: "DELETE",
            headers: {
                "Accept": "application/json"
            }
        });

        if (response.status === 401) {
            window.location.href = apoLoginUrl;
            return false;
        }

        let payload = null;
        try {
            payload = await response.json();
        } catch (error) {
            payload = null;
        }

        if (!response.ok || !payload || payload.esito !== "ok") {
            throw new Error(payload && payload.message ? payload.message : "Errore nell'eliminazione dell'allergia.");
        }

        const deleted = payload.data && Number(payload.data.deleted) > 0;
        apoState.schedaPazienteHistoryAllergieCodPaz = "";
        apoState.schedaPazienteHistoryAllergieLoaded = false;
        apoState.schedaPazienteHistoryAllergieError = "";
        setApoSearchMessage(payload.message || "Allergia eliminata correttamente.", deleted ? "success" : "warning");
        void loadApoSchedaPazienteAllergieForSelectedPaziente(accesso);
        return true;
    } catch (error) {
        setApoSearchMessage(error && error.message ? error.message : "Errore nell'eliminazione dell'allergia.", "danger");
        renderApoSchedaPazienteHistorySection();
        return false;
    }
}

async function loadApoSchedaPazienteTerapieForSelectedPaziente(accesso) {
    const codPaz = normalizeApoDisplayValue(accesso && accesso.codPaz);
    if (!codPaz) {
        clearApoSchedaPazienteHistoryTerapie();
        renderApoSchedaPazienteHistorySection();
        return;
    }

    if (apoState.schedaPazienteHistoryTerapieCodPaz === codPaz
            && (apoState.schedaPazienteHistoryTerapieLoading
                || apoState.schedaPazienteHistoryTerapieLoaded
                || apoState.schedaPazienteHistoryTerapieError)) {
        renderApoSchedaPazienteHistorySection();
        return;
    }

    const requestId = apoState.schedaPazienteHistoryTerapieRequestId + 1;
    const params = new URLSearchParams();
    params.set("codPaz", codPaz);

    apoState.schedaPazienteHistoryTerapieRequestId = requestId;
    apoState.schedaPazienteHistoryTerapieCodPaz = codPaz;
    apoState.schedaPazienteHistoryTerapie = [];
    apoState.schedaPazienteHistoryTerapieError = "";
    apoState.schedaPazienteHistoryTerapieLoading = true;
    apoState.schedaPazienteHistoryTerapieLoaded = false;
    renderApoSchedaPazienteHistorySection();

    try {
        const response = await fetch(apoSchedaPazienteTerapieUrl + "?" + params.toString(), {
            headers: {
                "Accept": "application/json"
            }
        });

        if (response.status === 401) {
            window.location.href = apoLoginUrl;
            return;
        }

        let payload = null;
        try {
            payload = await response.json();
        } catch (error) {
            payload = null;
        }

        if (!response.ok || !payload || payload.esito !== "ok") {
            throw new Error(payload && payload.message ? payload.message : "Errore nel caricamento delle terapie.");
        }

        if (apoState.schedaPazienteHistoryTerapieRequestId !== requestId
                || apoState.schedaPazienteHistoryTerapieCodPaz !== codPaz) {
            return;
        }

        apoState.schedaPazienteHistoryTerapie = normalizeApoSchedaPazienteTerapie(payload.data && payload.data.terapie);
        apoState.schedaPazienteHistoryTerapieError = "";
        apoState.schedaPazienteHistoryTerapieLoaded = true;
    } catch (error) {
        if (apoState.schedaPazienteHistoryTerapieRequestId !== requestId) {
            return;
        }
        apoState.schedaPazienteHistoryTerapie = [];
        apoState.schedaPazienteHistoryTerapieError = error && error.message ? error.message : "Errore nel caricamento delle terapie.";
        apoState.schedaPazienteHistoryTerapieLoaded = true;
    } finally {
        if (apoState.schedaPazienteHistoryTerapieRequestId === requestId) {
            apoState.schedaPazienteHistoryTerapieLoading = false;
            renderApoSchedaPazienteHistorySection();
        }
    }
}

async function saveApoSchedaPazienteHistoryTerapia(accesso, terapia, options) {
    const config = options && typeof options === "object" ? options : {};
    const codPaz = normalizeApoDisplayValue(accesso && accesso.codPaz);
    const item = normalizeApoSchedaPazienteTerapia(terapia, 0);
    if (!codPaz || !item.farmaco) {
        setApoSearchMessage("Codice paziente o farmaco non disponibile.", "warning");
        return false;
    }

    const params = new URLSearchParams();
    params.set("codPaz", codPaz);
    params.set("principioAttivo", item.principioAttivo);
    params.set("farmaco", item.farmaco);
    params.set("confezione", item.confezione);
    params.set("quantita", item.quantita);
    params.set("frequenzaUnita", item.frequenzaUnita);
    params.set("durataValore", item.durataValore);
    params.set("durataUnita", item.durataUnita);

    try {
        const response = await fetch(apoSchedaPazienteTerapieUrl, {
            method: "POST",
            headers: {
                "Accept": "application/json",
                "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8"
            },
            body: params.toString()
        });

        if (response.status === 401) {
            window.location.href = apoLoginUrl;
            return false;
        }

        let payload = null;
        try {
            payload = await response.json();
        } catch (error) {
            payload = null;
        }

        if (!response.ok || !payload || payload.esito !== "ok") {
            throw new Error(payload && payload.message ? payload.message : "Errore nel salvataggio della terapia.");
        }

        setApoSearchMessage(payload.message || "Terapia aggiunta correttamente.", "success");
        if (config.reload !== false) {
            apoState.schedaPazienteHistoryTerapieCodPaz = "";
            apoState.schedaPazienteHistoryTerapieLoaded = false;
            apoState.schedaPazienteHistoryTerapieError = "";
            void loadApoSchedaPazienteTerapieForSelectedPaziente(accesso);
        }
        return true;
    } catch (error) {
        setApoSearchMessage(error && error.message ? error.message : "Errore nel salvataggio della terapia.", "danger");
        return false;
    }
}

async function deleteApoSchedaPazienteHistoryTerapia(accesso, terapia) {
    const codPaz = normalizeApoDisplayValue(accesso && accesso.codPaz);
    const item = normalizeApoSchedaPazienteTerapia(terapia, 0);
    if (!codPaz || !item.farmaco) {
        setApoSearchMessage("Codice paziente o terapia non disponibile.", "warning");
        return false;
    }

    const params = new URLSearchParams();
    params.set("codPaz", codPaz);
    params.set("id", item.id);
    params.set("principioAttivo", item.principioAttivo);
    params.set("farmaco", item.farmaco);
    params.set("confezione", item.confezione);
    params.set("quantita", item.quantita);
    params.set("frequenzaUnita", item.frequenzaUnita);
    params.set("durataValore", item.durataValore);
    params.set("durataUnita", item.durataUnita);

    try {
        const response = await fetch(apoSchedaPazienteTerapieUrl + "?" + params.toString(), {
            method: "DELETE",
            headers: {
                "Accept": "application/json"
            }
        });

        if (response.status === 401) {
            window.location.href = apoLoginUrl;
            return false;
        }

        let payload = null;
        try {
            payload = await response.json();
        } catch (error) {
            payload = null;
        }

        if (!response.ok || !payload || payload.esito !== "ok") {
            throw new Error(payload && payload.message ? payload.message : "Errore nell'eliminazione della terapia.");
        }

        const deleted = payload.data && Number(payload.data.deleted) > 0;
        apoState.schedaPazienteHistoryTerapieCodPaz = "";
        apoState.schedaPazienteHistoryTerapieLoaded = false;
        apoState.schedaPazienteHistoryTerapieError = "";
        setApoSearchMessage(payload.message || "Terapia eliminata correttamente.", deleted ? "success" : "warning");
        void loadApoSchedaPazienteTerapieForSelectedPaziente(accesso);
        return true;
    } catch (error) {
        setApoSearchMessage(error && error.message ? error.message : "Errore nell'eliminazione della terapia.", "danger");
        renderApoSchedaPazienteHistorySection();
        return false;
    }
}

function renderApoPazienteHistoryTabs() {
    const activeTab = apoState.activePazienteHistoryTab === "storico" ? "storico" : "scheda";
    const schedaPanel = document.getElementById("apo-paziente-scheda-panel");
    const storicoPanel = document.getElementById("apo-paziente-storico-panel");

    document.querySelectorAll("[data-paziente-history-tab]").forEach(function (tab) {
        const tabId = String(tab.getAttribute("data-paziente-history-tab") || "").trim();
        const isActive = tabId === activeTab;
        tab.classList.toggle("is-active", isActive);
        tab.setAttribute("aria-selected", isActive ? "true" : "false");
        tab.tabIndex = isActive ? 0 : -1;
    });

    if (schedaPanel) {
        const isSchedaActive = activeTab === "scheda";
        schedaPanel.classList.toggle("is-hidden", !isSchedaActive);
        schedaPanel.setAttribute("aria-hidden", isSchedaActive ? "false" : "true");
    }
    if (storicoPanel) {
        const isStoricoActive = activeTab === "storico";
        storicoPanel.classList.toggle("is-hidden", !isStoricoActive);
        storicoPanel.setAttribute("aria-hidden", isStoricoActive ? "false" : "true");
    }
}

function setApoPazienteHistoryTab(tabId) {
    apoState.activePazienteHistoryTab = tabId === "storico" ? "storico" : "scheda";
    renderApoPazienteHistoryTabs();
    if (apoState.activePazienteHistoryTab === "storico") {
        renderApoStoricoAccessi();
    } else {
        renderApoSchedaPazienteHistorySection();
    }
}

function getApoStoricoTabConfig(tabId) {
    const normalizedTabId = String(tabId || "").trim();
    for (let index = 0; index < apoStoricoTabs.length; index += 1) {
        if (apoStoricoTabs[index].id === normalizedTabId) {
            return apoStoricoTabs[index];
        }
    }
    return apoStoricoTabs[0];
}

function renderApoStoricoTabs() {
    const tabs = document.querySelectorAll("[data-storico-tab]");
    const body = document.getElementById("apo-paziente-storico-body");
    const activeConfig = getApoStoricoTabConfig(apoState.activeStoricoTab);

    tabs.forEach(function (tab) {
        const tabId = String(tab.getAttribute("data-storico-tab") || "").trim();
        const isActive = tabId === activeConfig.id;
        tab.classList.toggle("is-active", isActive);
        tab.setAttribute("aria-selected", isActive ? "true" : "false");
        tab.tabIndex = isActive ? 0 : -1;
        if (body && isActive && tab.id) {
            body.setAttribute("aria-labelledby", tab.id);
        }
    });
}

function setApoStoricoTab(tabId) {
    const config = getApoStoricoTabConfig(tabId);
    const accesso = getApoSelectedAccesso();
    apoState.activeStoricoTab = config.id;
    renderApoStoricoAccessi();
    if (config.id === "prestazioni-inf" && accesso) {
        void loadApoPrestazioniInfermieristicheForSelectedPaziente(accesso);
    }
    if (config.id === "ricette-farm" && accesso) {
        void loadApoRicetteFarmaciForSelectedPaziente(accesso);
    }
    if (config.id === "ricette-dem" && accesso) {
        void loadApoRicetteDematerializzateForSelectedPaziente(accesso);
    }
    if (config.id !== "accessi") {
        apoState.selectFirstStoricoAccessoOnLoad = false;
    }
}

function renderApoStoricoTabPlaceholder(body, config) {
    const emptyState = document.createElement("div");
    emptyState.className = "empty-state";
    emptyState.textContent = "Nessun dato disponibile per " + config.label + ".";
    body.appendChild(emptyState);
}

async function loadApoStoricoAccessiForSelectedPaziente(accesso) {
    const codiceFiscale = normalizeApoDisplayValue(accesso && accesso.codFiscale);
    const normalizedCodiceFiscale = normalizeApoValue(codiceFiscale);

    if (!normalizedCodiceFiscale) {
        clearApoStoricoAccessi();
        renderApoStoricoAccessi();
        return;
    }

    if (apoState.storicoCodiceFiscale === normalizedCodiceFiscale
            && (apoState.storicoLoading || apoState.storicoLoaded || apoState.storicoError)) {
        if (!selectFirstApoStoricoAccessoIfRequested()) {
            renderApoStoricoAccessi();
        }
        return;
    }

    const requestId = apoState.storicoRequestId + 1;
    const params = new URLSearchParams();
    params.set("codiceFiscale", codiceFiscale);
    params.set("allServices", "true");

    apoState.storicoRequestId = requestId;
    apoState.storicoCodiceFiscale = normalizedCodiceFiscale;
    apoState.storicoAccessi = [];
    apoState.storicoError = "";
    apoState.storicoLoading = true;
    apoState.storicoLoaded = false;
    renderApoStoricoAccessi();

    try {
        const response = await fetch(apoAccessiUrl + "?" + params.toString(), {
            headers: {
                "Accept": "application/json"
            }
        });

        if (response.status === 401) {
            window.location.href = apoLoginUrl;
            return;
        }

        let payload = null;
        try {
            payload = await response.json();
        } catch (error) {
            payload = null;
        }

        if (!response.ok || !payload || payload.esito !== "ok") {
            throw new Error(payload && payload.message ? payload.message : "Errore nel caricamento dello storico accessi.");
        }

        if (apoState.storicoRequestId !== requestId || apoState.storicoCodiceFiscale !== normalizedCodiceFiscale) {
            return;
        }

        apoState.storicoAccessi = sortApoStoricoAccessi(normalizeApoAccessi(payload.data && payload.data.accessi));
        apoState.storicoError = "";
        apoState.storicoLoaded = true;
    } catch (error) {
        if (apoState.storicoRequestId !== requestId) {
            return;
        }

        apoState.storicoAccessi = [];
        apoState.storicoError = error && error.message ? error.message : "Errore nel caricamento dello storico accessi.";
        apoState.storicoLoaded = true;
    } finally {
        if (apoState.storicoRequestId === requestId) {
            apoState.storicoLoading = false;
            if (!selectFirstApoStoricoAccessoIfRequested()) {
                renderApoStoricoAccessi();
            }
        }
    }
}

async function loadApoPrestazioniInfermieristicheForSelectedPaziente(accesso) {
    const codiceFiscale = normalizeApoDisplayValue(accesso && accesso.codFiscale);
    const normalizedCodiceFiscale = normalizeApoValue(codiceFiscale);

    if (!normalizedCodiceFiscale) {
        clearApoPrestazioniInfermieristiche();
        renderApoStoricoAccessi();
        return;
    }

    if (apoState.prestazioniInfermieristicheCodiceFiscale === normalizedCodiceFiscale
            && (apoState.prestazioniInfermieristicheLoading
                || apoState.prestazioniInfermieristicheLoaded
                || apoState.prestazioniInfermieristicheError)) {
        renderApoStoricoAccessi();
        return;
    }

    const requestId = apoState.prestazioniInfermieristicheRequestId + 1;
    const params = new URLSearchParams();
    params.set("codiceFiscale", codiceFiscale);

    apoState.prestazioniInfermieristicheRequestId = requestId;
    apoState.prestazioniInfermieristicheCodiceFiscale = normalizedCodiceFiscale;
    apoState.prestazioniInfermieristiche = [];
    apoState.prestazioniInfermieristicheError = "";
    apoState.prestazioniInfermieristicheLoading = true;
    apoState.prestazioniInfermieristicheLoaded = false;
    renderApoStoricoAccessi();

    try {
        const response = await fetch(apoPrestazioniInfermieristicheUrl + "?" + params.toString(), {
            headers: {
                "Accept": "application/json"
            }
        });

        if (response.status === 401) {
            window.location.href = apoLoginUrl;
            return;
        }

        let payload = null;
        try {
            payload = await response.json();
        } catch (error) {
            payload = null;
        }

        if (!response.ok || !payload || payload.esito !== "ok") {
            throw new Error(payload && payload.message ? payload.message : "Errore nel caricamento delle prestazioni infermieristiche.");
        }

        if (apoState.prestazioniInfermieristicheRequestId !== requestId
                || apoState.prestazioniInfermieristicheCodiceFiscale !== normalizedCodiceFiscale) {
            return;
        }

        apoState.prestazioniInfermieristiche = normalizeApoPrestazioniInfermieristiche(payload.data && payload.data.prestazioni);
        apoState.prestazioniInfermieristicheError = "";
        apoState.prestazioniInfermieristicheLoaded = true;
    } catch (error) {
        if (apoState.prestazioniInfermieristicheRequestId !== requestId) {
            return;
        }

        apoState.prestazioniInfermieristiche = [];
        apoState.prestazioniInfermieristicheError = error && error.message ? error.message : "Errore nel caricamento delle prestazioni infermieristiche.";
        apoState.prestazioniInfermieristicheLoaded = true;
    } finally {
        if (apoState.prestazioniInfermieristicheRequestId === requestId) {
            apoState.prestazioniInfermieristicheLoading = false;
            renderApoStoricoAccessi();
        }
    }
}

async function loadApoRicetteFarmaciForSelectedPaziente(accesso) {
    const codiceFiscale = normalizeApoDisplayValue(accesso && accesso.codFiscale);
    const normalizedCodiceFiscale = normalizeApoValue(codiceFiscale);

    if (!normalizedCodiceFiscale) {
        clearApoRicetteFarmaci();
        renderApoStoricoAccessi();
        return;
    }

    if (apoState.ricetteFarmaciCodiceFiscale === normalizedCodiceFiscale
            && (apoState.ricetteFarmaciLoading
                || apoState.ricetteFarmaciLoaded
                || apoState.ricetteFarmaciError)) {
        renderApoStoricoAccessi();
        return;
    }

    const requestId = apoState.ricetteFarmaciRequestId + 1;
    const params = new URLSearchParams();
    params.set("codiceFiscale", codiceFiscale);

    apoState.ricetteFarmaciRequestId = requestId;
    apoState.ricetteFarmaciCodiceFiscale = normalizedCodiceFiscale;
    apoState.ricetteFarmaci = [];
    apoState.ricetteFarmaciError = "";
    apoState.ricetteFarmaciLoading = true;
    apoState.ricetteFarmaciLoaded = false;
    renderApoStoricoAccessi();

    try {
        const response = await fetch(apoRicetteFarmaciStoricoUrl + "?" + params.toString(), {
            headers: {
                "Accept": "application/json"
            }
        });

        if (response.status === 401) {
            window.location.href = apoLoginUrl;
            return;
        }

        let payload = null;
        try {
            payload = await response.json();
        } catch (error) {
            payload = null;
        }

        if (!response.ok || !payload || payload.esito !== "ok") {
            throw new Error(payload && payload.message ? payload.message : "Errore nel caricamento delle ricette farmaci.");
        }

        if (apoState.ricetteFarmaciRequestId !== requestId
                || apoState.ricetteFarmaciCodiceFiscale !== normalizedCodiceFiscale) {
            return;
        }

        apoState.ricetteFarmaci = normalizeApoRicetteFarmaci(payload.data && payload.data.ricette);
        apoState.ricetteFarmaciError = "";
        apoState.ricetteFarmaciLoaded = true;
    } catch (error) {
        if (apoState.ricetteFarmaciRequestId !== requestId) {
            return;
        }

        apoState.ricetteFarmaci = [];
        apoState.ricetteFarmaciError = error && error.message ? error.message : "Errore nel caricamento delle ricette farmaci.";
        apoState.ricetteFarmaciLoaded = true;
    } finally {
        if (apoState.ricetteFarmaciRequestId === requestId) {
            apoState.ricetteFarmaciLoading = false;
            renderApoStoricoAccessi();
        }
    }
}

async function loadApoRicetteDematerializzateForSelectedPaziente(accesso) {
    const codiceFiscale = normalizeApoDisplayValue(accesso && accesso.codFiscale);
    const normalizedCodiceFiscale = normalizeApoValue(codiceFiscale);

    if (!normalizedCodiceFiscale) {
        clearApoRicetteDematerializzate();
        renderApoStoricoAccessi();
        return;
    }

    if (apoState.ricetteDematerializzateCodiceFiscale === normalizedCodiceFiscale
            && (apoState.ricetteDematerializzateLoading
                || apoState.ricetteDematerializzateLoaded
                || apoState.ricetteDematerializzateError)) {
        renderApoStoricoAccessi();
        return;
    }

    const requestId = apoState.ricetteDematerializzateRequestId + 1;
    const params = new URLSearchParams();
    params.set("codiceFiscale", codiceFiscale);

    apoState.ricetteDematerializzateRequestId = requestId;
    apoState.ricetteDematerializzateCodiceFiscale = normalizedCodiceFiscale;
    apoState.ricetteDematerializzate = [];
    apoState.ricetteDematerializzateError = "";
    apoState.ricetteDematerializzateLoading = true;
    apoState.ricetteDematerializzateLoaded = false;
    renderApoStoricoAccessi();

    try {
        const response = await fetch(apoRicetteDematerializzateStoricoUrl + "?" + params.toString(), {
            headers: {
                "Accept": "application/json"
            }
        });

        if (response.status === 401) {
            window.location.href = apoLoginUrl;
            return;
        }

        let payload = null;
        try {
            payload = await response.json();
        } catch (error) {
            payload = null;
        }

        if (!response.ok || !payload || payload.esito !== "ok") {
            throw new Error(payload && payload.message ? payload.message : "Errore nel caricamento delle ricette dematerializzate.");
        }

        if (apoState.ricetteDematerializzateRequestId !== requestId
                || apoState.ricetteDematerializzateCodiceFiscale !== normalizedCodiceFiscale) {
            return;
        }

        apoState.ricetteDematerializzate = normalizeApoRicetteDematerializzate(payload.data && payload.data.ricette);
        apoState.ricetteDematerializzateError = "";
        apoState.ricetteDematerializzateLoaded = true;
    } catch (error) {
        if (apoState.ricetteDematerializzateRequestId !== requestId) {
            return;
        }

        apoState.ricetteDematerializzate = [];
        apoState.ricetteDematerializzateError = error && error.message ? error.message : "Errore nel caricamento delle ricette dematerializzate.";
        apoState.ricetteDematerializzateLoaded = true;
    } finally {
        if (apoState.ricetteDematerializzateRequestId === requestId) {
            apoState.ricetteDematerializzateLoading = false;
            renderApoStoricoAccessi();
        }
    }
}

async function loadApoPianiTerapeuticiForSelectedPaziente(accesso) {
    const codiceFiscale = normalizeApoDisplayValue(accesso && accesso.codFiscale);
    const normalizedCodiceFiscale = normalizeApoValue(codiceFiscale);

    if (!normalizedCodiceFiscale) {
        clearApoPianiTerapeutici();
        syncApoPazienteMenuForUser();
        if (apoState.activePazienteSection === "piani-terapeutici") {
            renderApoPazienteBody();
        }
        return;
    }

    if (apoState.pianiTerapeuticiCodiceFiscale === normalizedCodiceFiscale
            && (apoState.pianiTerapeuticiLoading
                || apoState.pianiTerapeuticiLoaded
                || apoState.pianiTerapeuticiError)) {
        syncApoPazienteMenuForUser();
        if (apoState.activePazienteSection === "piani-terapeutici") {
            renderApoPazienteBody();
        }
        return;
    }

    const requestId = apoState.pianiTerapeuticiRequestId + 1;
    const params = new URLSearchParams();
    params.set("codiceFiscale", codiceFiscale);

    apoState.pianiTerapeuticiRequestId = requestId;
    apoState.pianiTerapeuticiCodiceFiscale = normalizedCodiceFiscale;
    apoState.pianiTerapeutici = [];
    apoState.pianiTerapeuticiError = "";
    apoState.pianiTerapeuticiLoading = true;
    apoState.pianiTerapeuticiLoaded = false;
    syncApoPazienteMenuForUser();
    if (apoState.activePazienteSection === "piani-terapeutici") {
        renderApoPazienteBody();
    }

    try {
        const response = await fetch(apoPianiTerapeuticiUrl + "?" + params.toString(), {
            headers: {
                "Accept": "application/json"
            }
        });

        if (response.status === 401) {
            window.location.href = apoLoginUrl;
            return;
        }

        let payload = null;
        try {
            payload = await response.json();
        } catch (error) {
            payload = null;
        }

        if (!response.ok || !payload || payload.esito !== "ok") {
            throw new Error(payload && payload.message ? payload.message : "Errore nel caricamento dei piani terapeutici.");
        }

        if (apoState.pianiTerapeuticiRequestId !== requestId
                || apoState.pianiTerapeuticiCodiceFiscale !== normalizedCodiceFiscale) {
            return;
        }

        apoState.pianiTerapeutici = normalizeApoPianiTerapeutici(payload.data && payload.data.piani);
        apoState.pianiTerapeuticiError = "";
        apoState.pianiTerapeuticiLoaded = true;
    } catch (error) {
        if (apoState.pianiTerapeuticiRequestId !== requestId) {
            return;
        }

        apoState.pianiTerapeutici = [];
        apoState.pianiTerapeuticiError = error && error.message ? error.message : "Errore nel caricamento dei piani terapeutici.";
        apoState.pianiTerapeuticiLoaded = true;
    } finally {
        if (apoState.pianiTerapeuticiRequestId === requestId) {
            apoState.pianiTerapeuticiLoading = false;
            syncApoPazienteMenuForUser();
            if (apoState.activePazienteSection === "piani-terapeutici") {
                renderApoPazienteBody();
            }
        }
    }
}

async function loadApoAllegatoMForSelectedAccesso(accesso) {
    const codConsulenza = normalizeApoDisplayValue(accesso && (accesso.codConsulenze || accesso.codConsulenza || accesso.consulenza));
    const codServizio = getApoSelectedConsulenzaCodServizio() || normalizeApoDisplayValue(accesso && accesso.codServizio);
    const allegatoMKey = getApoAllegatoMKey(accesso);

    if (!allegatoMKey) {
        clearApoAllegatoM();
        syncApoPazienteMenuForUser();
        if (apoState.activePazienteSection === "allegato-m") {
            renderApoPazienteBody();
        }
        return;
    }

    if (apoState.allegatoMKey === allegatoMKey
            && (apoState.allegatoMLoading || apoState.allegatoMLoaded || apoState.allegatoMError)) {
        syncApoPazienteMenuForUser();
        return;
    }

    const requestId = apoState.allegatoMRequestId + 1;
    const params = new URLSearchParams();
    params.set("codConsulenza", codConsulenza);
    params.set("codServizio", codServizio);

    apoState.allegatoMRequestId = requestId;
    apoState.allegatoMKey = allegatoMKey;
    apoState.allegatoM = null;
    apoState.allegatoMError = "";
    apoState.allegatoMLoading = true;
    apoState.allegatoMLoaded = false;
    syncApoPazienteMenuForUser();
    if (apoState.activePazienteSection === "allegato-m") {
        renderApoPazienteBody();
    }

    try {
        const response = await fetch(apoAllegatoMUrl + "?" + params.toString(), {
            headers: {
                "Accept": "application/json"
            }
        });

        if (response.status === 401) {
            window.location.href = apoLoginUrl;
            return;
        }

        let payload = null;
        try {
            payload = await response.json();
        } catch (error) {
            payload = null;
        }

        if (!response.ok || !payload || payload.esito !== "ok") {
            throw new Error(payload && payload.message ? payload.message : "Errore nel caricamento dell'Allegato M.");
        }

        if (apoState.allegatoMRequestId !== requestId || apoState.allegatoMKey !== allegatoMKey) {
            return;
        }

        apoState.allegatoM = normalizeApoAllegatoM(payload.data && payload.data.allegatoM);
        apoState.allegatoMError = "";
        apoState.allegatoMLoaded = true;
    } catch (error) {
        if (apoState.allegatoMRequestId !== requestId) {
            return;
        }

        apoState.allegatoM = null;
        apoState.allegatoMError = error && error.message ? error.message : "Errore nel caricamento dell'Allegato M.";
        apoState.allegatoMLoaded = true;
    } finally {
        if (apoState.allegatoMRequestId === requestId) {
            apoState.allegatoMLoading = false;
            syncApoPazienteMenuForUser();
            if (apoState.activePazienteSection === "allegato-m") {
                renderApoPazienteBody();
            }
        }
    }
}

function splitApoPrestazioneInfermieristicaText(value) {
    const text = normalizeApoDisplayValue(value);
    const separator = " - ";
    const separatorIndex = text.indexOf(separator);

    if (separatorIndex === -1) {
        return {
            categoria: "",
            sottoCategoria: text
        };
    }

    return {
        categoria: normalizeApoDisplayValue(text.substring(0, separatorIndex)),
        sottoCategoria: normalizeApoDisplayValue(text.substring(separatorIndex + separator.length))
    };
}

function getApoPrestazioneInfermieristicaCategoria(item) {
    const categoria = normalizeApoDisplayValue(item && item.categoria);
    if (categoria) {
        return categoria;
    }

    return splitApoPrestazioneInfermieristicaText(item && item.prestazione).categoria || "Senza categoria";
}

function getApoPrestazioneInfermieristicaSottocategoria(item) {
    const sottoCategoria = normalizeApoDisplayValue(item && item.sottoCategoria);
    if (sottoCategoria) {
        return sottoCategoria;
    }

    return splitApoPrestazioneInfermieristicaText(item && item.prestazione).sottoCategoria || "-";
}

function groupApoPrestazioniInfermieristicheByCategoria(items) {
    const groups = [];
    const byCategoria = {};

    items.forEach(function (item, index) {
        const categoria = getApoPrestazioneInfermieristicaCategoria(item);
        const key = normalizeApoValue(categoria) || ("CATEGORIA_" + index);
        let group = byCategoria[key];

        if (!group) {
            group = {
                categoria: categoria,
                sottocategorie: []
            };
            byCategoria[key] = group;
            groups.push(group);
        }

        group.sottocategorie.push(getApoPrestazioneInfermieristicaSottocategoria(item));
    });

    return groups;
}

function groupApoPrestazioniInfermieristiche(items) {
    const groups = [];
    const byConsulenza = {};

    items.forEach(function (item, index) {
        const idConsulenza = normalizeApoDisplayValue(item && item.idConsulenza);
        const key = idConsulenza || ("SENZA_CONSULENZA_" + index);
        let group = byConsulenza[key];

        if (!group) {
            group = {
                idConsulenza: idConsulenza,
                dataIns: normalizeApoDisplayValue(item && item.dataIns),
                dataInsSort: normalizeApoDisplayValue(item && item.dataInsSort),
                sede: normalizeApoDisplayValue(item && item.descrizione),
                operatore: normalizeApoDisplayValue(item && item.descOperatore),
                prestazioni: []
            };
            byConsulenza[key] = group;
            groups.push(group);
        }

        group.prestazioni.push(item);
        if (!group.dataIns && item.dataIns) {
            group.dataIns = normalizeApoDisplayValue(item.dataIns);
        }
        if (!group.dataInsSort && item.dataInsSort) {
            group.dataInsSort = normalizeApoDisplayValue(item.dataInsSort);
        }
        if (!group.sede && item.descrizione) {
            group.sede = normalizeApoDisplayValue(item.descrizione);
        }
        if (!group.operatore && item.descOperatore) {
            group.operatore = normalizeApoDisplayValue(item.descOperatore);
        }
    });

    return groups.sort(function (left, right) {
        return compareApoValues(left.dataInsSort || left.dataIns, right.dataInsSort || right.dataIns, "desc");
    });
}

function renderApoPrestazioniInfermieristiche(body) {
    if (apoState.prestazioniInfermieristicheLoading) {
        body.appendChild(createApoSpinnerLoader("Caricamento prestazioni infermieristiche...", "apo-storico-loader"));
        return;
    }

    if (apoState.prestazioniInfermieristicheError) {
        const emptyState = document.createElement("div");
        emptyState.className = "empty-state";
        emptyState.textContent = apoState.prestazioniInfermieristicheError;
        body.appendChild(emptyState);
        return;
    }

    const prestazioni = apoState.prestazioniInfermieristiche;
    if (!prestazioni.length) {
        const emptyState = document.createElement("div");
        emptyState.className = "empty-state";
        emptyState.textContent = "Nessuna prestazione infermieristica disponibile.";
        body.appendChild(emptyState);
        return;
    }

    const list = document.createElement("div");
    list.className = "apo-paziente-history-list";

    groupApoPrestazioniInfermieristiche(prestazioni).forEach(function (group) {
        const row = document.createElement("article");
        row.className = "apo-paziente-history-item apo-paziente-history-item-static apo-prestazioni-inf-group";

        const header = document.createElement("div");
        header.className = "apo-prestazioni-inf-header";

        const date = document.createElement("div");
        date.className = "apo-paziente-history-date";
        date.textContent = group.dataIns || "-";

        const consulenza = document.createElement("div");
        consulenza.className = "apo-prestazioni-inf-consulenza";
        consulenza.textContent = 'Consulenza ' + group.idConsulenza || "-";

        header.appendChild(date);
        header.appendChild(consulenza);

        const servizio = document.createElement("div");
        servizio.className = "apo-paziente-history-meta";
        servizio.textContent = group.sede || "-";

        const operatore = document.createElement("div");
        operatore.className = "apo-paziente-history-meta";
        operatore.textContent = group.operatore || "-";

        const prestazioniList = document.createElement("div");
        prestazioniList.className = "apo-prestazioni-inf-items";

        groupApoPrestazioniInfermieristicheByCategoria(group.prestazioni).forEach(function (categoriaGroup) {
            const categoriaItem = document.createElement("section");
            categoriaItem.className = "apo-prestazioni-inf-category";

            const categoriaTitle = document.createElement("div");
            categoriaTitle.className = "apo-prestazioni-inf-category-title";
            categoriaTitle.textContent = categoriaGroup.categoria || "Senza categoria";

            const sottocategorie = document.createElement("div");
            sottocategorie.className = "apo-prestazioni-inf-subitems";
            categoriaGroup.sottocategorie.forEach(function (sottoCategoriaText) {
                const sottoCategoria = document.createElement("div");
                sottoCategoria.className = "apo-prestazioni-inf-subitem";
                sottoCategoria.textContent = sottoCategoriaText || "-";
                sottocategorie.appendChild(sottoCategoria);
            });

            categoriaItem.appendChild(categoriaTitle);
            categoriaItem.appendChild(sottocategorie);
            prestazioniList.appendChild(categoriaItem);
        });

        row.appendChild(header);
        row.appendChild(servizio);
        row.appendChild(operatore);
        row.appendChild(prestazioniList);
        list.appendChild(row);
    });

    body.appendChild(list);
}

function groupApoRicetteFarmaci(items) {
    const groups = [];
    const byKey = {};

    items.forEach(function (item, index) {
        const key = normalizeApoValue(item.codRicetta || item.ricetta || ("RICETTA_FARMACO_" + index));
        let group = byKey[key];
        if (!group) {
            group = {
                ricetta: item.ricetta,
                medico: item.medico,
                codAccettazione: item.codAccettazione,
                dataRicetta: item.dataRicetta,
                dataRicettaSort: item.dataRicettaSort,
                descrizione: item.descrizione,
                farmaci: []
            };
            byKey[key] = group;
            groups.push(group);
        }

        const farmaco = normalizeApoDisplayValue(item.farmaco);
        if (farmaco) {
            group.farmaci.push(farmaco);
        }
    });

    return groups;
}

function appendApoHistoryCornerCode(row, value) {
    const code = normalizeApoDisplayValue(value);
    if (!row || !code) {
        return;
    }

    row.classList.add("apo-paziente-history-item-has-code");
    const codeElement = document.createElement("div");
    codeElement.className = "apo-paziente-history-corner-code";
    codeElement.textContent = "Consulenza " + code;
    row.appendChild(codeElement);
}

function renderApoRicetteFarmaci(body) {
    if (apoState.ricetteFarmaciLoading) {
        body.appendChild(createApoSpinnerLoader("Caricamento ricette farmaci...", "apo-storico-loader"));
        return;
    }

    if (apoState.ricetteFarmaciError) {
        const emptyState = document.createElement("div");
        emptyState.className = "empty-state";
        emptyState.textContent = apoState.ricetteFarmaciError;
        body.appendChild(emptyState);
        return;
    }

    const ricette = groupApoRicetteFarmaci(apoState.ricetteFarmaci);
    if (!ricette.length) {
        const emptyState = document.createElement("div");
        emptyState.className = "empty-state";
        emptyState.textContent = "Nessuna ricetta farmaci disponibile.";
        body.appendChild(emptyState);
        return;
    }

    const list = document.createElement("div");
    list.className = "apo-paziente-history-list";

    ricette.forEach(function (item) {
        const row = document.createElement("article");
        row.className = "apo-paziente-history-item apo-paziente-history-item-static";

        const date = document.createElement("div");
        date.className = "apo-paziente-history-date";
        date.textContent = item.dataRicetta || "-";
        appendApoHistoryCornerCode(row, item.codAccettazione);

        const title = document.createElement("div");
        title.className = "apo-paziente-history-title";
        title.textContent = item.ricetta || "-";

        const medico = document.createElement("div");
        medico.className = "apo-paziente-history-meta";
        medico.textContent = item.medico || "-";

        const servizio = document.createElement("div");
        servizio.className = "apo-paziente-history-meta";
        servizio.textContent = item.descrizione || "-";

        const farmaci = document.createElement("ul");
        farmaci.className = "apo-ricette-farmaci-list";

        if (!item.farmaci.length) {
            const farmaco = document.createElement("li");
            farmaco.textContent = "-";
            farmaci.appendChild(farmaco);
        } else {
            item.farmaci.forEach(function (farmacoText) {
                const farmaco = document.createElement("li");
                farmaco.textContent = farmacoText;
                farmaci.appendChild(farmaco);
            });
        }

        row.appendChild(date);
        row.appendChild(title);
        row.appendChild(servizio);
        row.appendChild(medico);
        row.appendChild(farmaci);
        list.appendChild(row);
    });

    body.appendChild(list);
}

function groupApoRicetteDematerializzate(items) {
    const groups = [];
    const byKey = {};

    items.forEach(function (item, index) {
        const key = normalizeApoValue(item.codRichiesta || item.ricetta || ("RICETTA_DEM_" + index));
        let group = byKey[key];
        if (!group) {
            group = {
                ricetta: item.ricetta,
                medico: item.medico,
                codAccettazione: item.codAccettazione,
                dataRicetta: item.dataRicetta,
                dataRicettaSort: item.dataRicettaSort,
                descrizione: item.descrizione,
                esami: []
            };
            byKey[key] = group;
            groups.push(group);
        }

        const esame = normalizeApoDisplayValue(item.esame);
        if (esame) {
            group.esami.push(esame);
        }
    });

    return groups;
}

function renderApoRicetteDematerializzate(body) {
    if (apoState.ricetteDematerializzateLoading) {
        body.appendChild(createApoSpinnerLoader("Caricamento ricette dematerializzate...", "apo-storico-loader"));
        return;
    }

    if (apoState.ricetteDematerializzateError) {
        const emptyState = document.createElement("div");
        emptyState.className = "empty-state";
        emptyState.textContent = apoState.ricetteDematerializzateError;
        body.appendChild(emptyState);
        return;
    }

    const ricette = groupApoRicetteDematerializzate(apoState.ricetteDematerializzate);
    if (!ricette.length) {
        const emptyState = document.createElement("div");
        emptyState.className = "empty-state";
        emptyState.textContent = "Nessuna ricetta dematerializzata disponibile.";
        body.appendChild(emptyState);
        return;
    }

    const list = document.createElement("div");
    list.className = "apo-paziente-history-list";

    ricette.forEach(function (item) {
        const row = document.createElement("article");
        row.className = "apo-paziente-history-item apo-paziente-history-item-static";

        const date = document.createElement("div");
        date.className = "apo-paziente-history-date";
        date.textContent = item.dataRicetta || "-";
        appendApoHistoryCornerCode(row, item.codAccettazione);

        const title = document.createElement("div");
        title.className = "apo-paziente-history-title";
        title.textContent = item.ricetta || "-";

        const medico = document.createElement("div");
        medico.className = "apo-paziente-history-meta";
        medico.textContent = item.medico || "-";

        const servizio = document.createElement("div");
        servizio.className = "apo-paziente-history-meta";
        servizio.textContent = item.descrizione || "-";

        const esami = document.createElement("ul");
        esami.className = "apo-ricette-farmaci-list";

        if (!item.esami.length) {
            const esame = document.createElement("li");
            esame.textContent = "-";
            esami.appendChild(esame);
        } else {
            item.esami.forEach(function (esameText) {
                const esame = document.createElement("li");
                esame.textContent = esameText;
                esami.appendChild(esame);
            });
        }

        row.appendChild(date);
        row.appendChild(title);
        row.appendChild(servizio);
        row.appendChild(medico);
        row.appendChild(esami);
        list.appendChild(row);
    });

    body.appendChild(list);
}

function renderApoStoricoAccessi() {
    const body = document.getElementById("apo-paziente-storico-body");
    const accesso = getApoSelectedAccesso();
    const mainAccesso = getApoAccessoById(apoState.mainAccessoId);
    const activeTab = getApoStoricoTabConfig(apoState.activeStoricoTab);

    renderApoStoricoTabs();

    if (!body) {
        return;
    }

    body.innerHTML = "";

    if (!accesso) {
        const emptyState = document.createElement("div");
        emptyState.className = "empty-state";
        emptyState.textContent = "Nessun paziente selezionato.";
        body.appendChild(emptyState);
        return;
    }

    if (activeTab.id === "prestazioni-inf") {
        renderApoPrestazioniInfermieristiche(body);
        return;
    }

    if (activeTab.id === "ricette-farm") {
        renderApoRicetteFarmaci(body);
        return;
    }

    if (activeTab.id === "ricette-dem") {
        renderApoRicetteDematerializzate(body);
        return;
    }

    if (activeTab.id !== "accessi") {
        renderApoStoricoTabPlaceholder(body, activeTab);
        return;
    }

    if (apoState.storicoLoading) {
        body.appendChild(createApoSpinnerLoader("Caricamento storico accessi...", "apo-storico-loader"));
        return;
    }

    if (apoState.storicoError) {
        const emptyState = document.createElement("div");
        emptyState.className = "empty-state";
        emptyState.textContent = apoState.storicoError;
        body.appendChild(emptyState);
        return;
    }

    const storico = apoState.storicoAccessi;
    if (!storico.length) {
        const emptyState = document.createElement("div");
        emptyState.className = "empty-state";
        emptyState.textContent = "Nessuno storico accessi disponibile.";
        body.appendChild(emptyState);
        return;
    }

    const list = document.createElement("div");
    list.className = "apo-paziente-history-list";

    storico.forEach(function (item) {
        const row = document.createElement("article");
        row.className = "apo-paziente-history-item";
        row.dataset.storicoAccessoId = item.id;
        row.dataset.codConsulenza = normalizeApoDisplayValue(item.codConsulenze || item.codConsulenza || item.consulenza);
        row.dataset.codServizio = normalizeApoDisplayValue(item.codServizio);
        row.dataset.descServizio = normalizeApoDisplayValue(item.sede || item.descServizio);
        row.tabIndex = 0;
        row.setAttribute("role", "button");
        row.classList.toggle("is-main-selection", isSameApoAccesso(item, mainAccesso));
        row.classList.toggle("is-selected", item.id === apoState.selectedAccessoId);

        const date = document.createElement("div");
        date.className = "apo-paziente-history-date";
        date.textContent = item.dataOra || item.dataDisplay || "-";
        
        const allegatoMIcon = document.createElement("button");
        allegatoMIcon.type = "button";
        allegatoMIcon.className = "apo-allegato-m-icon";
        allegatoMIcon.textContent = "M";
        allegatoMIcon.title = "Apri Allegato M per questa consulenza";
        allegatoMIcon.setAttribute("aria-label", "Apri Allegato M per consulenza " + (row.dataset.codConsulenza || "-"));
        allegatoMIcon.addEventListener("click", function(event) {
            const historyItem = event.currentTarget.closest(".apo-paziente-history-item");
            const sedeMeta = historyItem ? historyItem.querySelector(".apo-paziente-history-meta[data-cod-servizio]") : null;
            const reportItem = {
                codConsulenza: historyItem ? historyItem.dataset.codConsulenza : (item.codConsulenze || item.codConsulenza || item.consulenza),
                codServizio: sedeMeta ? sedeMeta.dataset.codServizio : (historyItem ? historyItem.dataset.codServizio : item.codServizio)
            };
            event.stopPropagation();
            openApoAllegatoMReport(reportItem, false);
        });
        allegatoMIcon.addEventListener("keydown", function(event) {
            event.stopPropagation();
        });

        const title = document.createElement("div");
        title.className = "apo-paziente-history-title";
        title.textContent = "Consulenza " + (item.codConsulenze || item.consulenza || "-");

        const sede = document.createElement("div");
        sede.className = "apo-paziente-history-meta";
        sede.dataset.codServizio = normalizeApoDisplayValue(item.codServizio);
        sede.textContent = item.sede || "-";

        const operatore = document.createElement("div");
        operatore.className = "apo-paziente-history-meta";
        operatore.textContent = item.operatore || "-";

        row.appendChild(date);
        row.appendChild(title);
        row.appendChild(sede);
        row.appendChild(operatore);
        row.appendChild(allegatoMIcon);
        list.appendChild(row);
    });

    body.appendChild(list);
}

function getApoStoricoAccessoById(accessoId) {
    const normalizedId = String(accessoId || "").trim();
    if (!normalizedId) {
        return null;
    }

    for (let index = 0; index < apoState.storicoAccessi.length; index += 1) {
        if (String(apoState.storicoAccessi[index].id || "").trim() === normalizedId) {
            return apoState.storicoAccessi[index];
        }
    }

    return null;
}

function applyApoStoricoAccessoSelection(accessoId) {
    const normalizedId = String(accessoId || "").trim();
    const accesso = getApoStoricoAccessoById(normalizedId);
    if (!accesso) {
        return;
    }

    apoState.selectFirstStoricoAccessoOnLoad = false;
    apoState.pendingStoricoAccessoId = "";
    apoState.selectedAccessoId = normalizedId;
    apoState.activePazienteSection = "diagnosi";
    setApoDetailOpen(false);
    renderApoAccessi();
    renderApoSelectedPaziente();
    renderApoStoricoAccessi();
    renderApoPazienteBody();
}

function formatApoStoricoAccessoConfirmLabel(accesso) {
    if (!accesso) {
        return "la consulenza selezionata";
    }

    const codice = normalizeApoDisplayValue(accesso.codConsulenze || accesso.consulenza);
    const data = normalizeApoDisplayValue(accesso.dataOra || accesso.dataDisplay);
    if (codice && data) {
        return "la consulenza " + codice + " del " + data;
    }
    if (codice) {
        return "la consulenza " + codice;
    }
    return "la consulenza selezionata";
}

function hideApoStoricoAccessoConfirmModal(restoreFocus) {
    const modal = document.getElementById("apo-storico-confirm-modal");
    if (modal) {
        modal.classList.add("is-hidden");
        modal.setAttribute("aria-hidden", "true");
    }

    apoState.pendingStoricoAccessoId = "";
    if (restoreFocus && apoState.storicoConfirmLastFocus && typeof apoState.storicoConfirmLastFocus.focus === "function") {
        apoState.storicoConfirmLastFocus.focus();
    }
    apoState.storicoConfirmLastFocus = null;
}

function showApoStoricoAccessoConfirmModal(accessoId, actionType = 'selectAccesso') { // Added actionType
    const normalizedId = String(accessoId || "").trim();
    const modal = document.getElementById("apo-storico-confirm-modal");
    const text = document.getElementById("apo-storico-confirm-text");
    const cancelButton = document.getElementById("apo-storico-confirm-cancel");
    const accesso = getApoStoricoAccessoById(normalizedId);

    if (!modal || !accesso) { // If modal or access not found, proceed with default selection
        applyApoStoricoAccessoSelection(normalizedId);
        return;
    }

    apoState.pendingStoricoAccessoId = normalizedId;
    apoState.storicoConfirmLastFocus = document.activeElement;
    if (text) {
        apoState.pendingAction = actionType; // Store the action type
        let message = "Sei sicuro di voler cambiare la consulenza selezionata con " + formatApoStoricoAccessoConfirmLabel(accesso) + "?";
        text.textContent = message;

    }

    modal.classList.remove("is-hidden");
    modal.setAttribute("aria-hidden", "false");
    if (cancelButton) {
        cancelButton.focus();
    }
}

function confirmApoStoricoAccessoChange() {
    const pendingId = String(apoState.pendingStoricoAccessoId || "").trim();
    const actionToPerform = apoState.pendingAction; // Retrieve the action type
    hideApoStoricoAccessoConfirmModal(false);
    if (pendingId) {
        applyApoStoricoAccessoSelection(pendingId);
    }
    apoState.pendingAction = null; // Reset pending action
}

function selectApoStoricoAccesso(accessoId) {
    const normalizedId = String(accessoId || "").trim();
    const accesso = getApoStoricoAccessoById(normalizedId);
    const selectedAccesso = getApoSelectedAccesso();
    const isAlreadySelected = accesso && selectedAccesso && isSameApoAccesso(accesso, selectedAccesso);

    if (!accesso) {
        return;
    }

    if (!isAlreadySelected && String(apoState.selectedAccessoId || "").trim()) {
        showApoStoricoAccessoConfirmModal(normalizedId, 'selectAccesso'); // Explicitly pass 'selectAccesso'
        return;
    }

    applyApoStoricoAccessoSelection(normalizedId);
}

function getApoAccessoCodConsulenza(accesso) {
    return normalizeApoDisplayValue(accesso && (accesso.codConsulenze || accesso.codConsulenza || accesso.consulenza));
}

function formatApoDeleteConsulenzaMessage(accesso) {
    const codice = getApoAccessoCodConsulenza(accesso) || "-";
    const data = normalizeApoDisplayValue(accesso && (accesso.dataOra || accesso.dataDisplay)) || "-";
    const paziente = normalizeApoDisplayValue(accesso && (accesso.paziente || ((accesso.cognome || "") + " " + (accesso.nome || "")).trim())) || "-";
    return "Sei sicuro di eliminare la consulenza " + codice + " del giorno " + data + " per il paziente " + paziente + "?";
}

function setApoDeleteConfirmMessage(message, type) {
    const box = document.getElementById("apo-delete-confirm-message");
    if (!box) {
        return;
    }

    box.className = "alert d-none";
    box.textContent = "";

    const text = String(message || "").trim();
    if (!text) {
        return;
    }

    const normalizedType = String(type || "info").trim().toLowerCase();
    const typeClass = normalizedType === "danger" ? "alert-danger"
        : normalizedType === "warning" ? "alert-warning"
        : normalizedType === "success" ? "alert-success"
        : "alert-info";

    box.textContent = text;
    box.className = "alert " + typeClass;
}

function setApoDeleteConsulenzaBusy(isBusy) {
    const busy = isBusy === true;
    const okButton = document.getElementById("apo-delete-confirm-ok");
    const cancelButton = document.getElementById("apo-delete-confirm-cancel");

    apoState.deleteConsulenzaLoading = busy;
    if (okButton) {
        okButton.disabled = busy;
        okButton.textContent = busy ? "Eliminazione..." : "Conferma";
    }
    if (cancelButton) {
        cancelButton.disabled = busy;
    }
}

function hideApoDeleteConsulenzaConfirmModal(restoreFocus) {
    const modal = document.getElementById("apo-delete-confirm-modal");
    if (modal) {
        modal.classList.add("is-hidden");
        modal.setAttribute("aria-hidden", "true");
    }

    setApoDeleteConsulenzaBusy(false);
    setApoDeleteConfirmMessage("", "info");
    apoState.pendingDeleteAccessoId = "";
    if (restoreFocus && apoState.deleteConfirmLastFocus && typeof apoState.deleteConfirmLastFocus.focus === "function") {
        apoState.deleteConfirmLastFocus.focus();
    }
    apoState.deleteConfirmLastFocus = null;
}

function showApoDeleteConsulenzaConfirmModal() {
    const accesso = getApoSelectedAccesso();
    const codConsulenza = getApoAccessoCodConsulenza(accesso);
    const modal = document.getElementById("apo-delete-confirm-modal");
    const text = document.getElementById("apo-delete-confirm-text");
    const cancelButton = document.getElementById("apo-delete-confirm-cancel");

    if (!accesso || !codConsulenza) {
        window.alert("Seleziona una consulenza dallo storico accessi prima di eliminarla.");
        return;
    }
    if (!modal) {
        return;
    }

    apoState.pendingDeleteAccessoId = accesso.id;
    apoState.deleteConfirmLastFocus = document.activeElement;
    setApoDeleteConfirmMessage("", "info");
    setApoDeleteConsulenzaBusy(false);
    if (text) {
        text.textContent = formatApoDeleteConsulenzaMessage(accesso);
    }

    modal.classList.remove("is-hidden");
    modal.setAttribute("aria-hidden", "false");
    if (cancelButton) {
        cancelButton.focus();
    }
}

function removeApoAccessoByCodConsulenza(items, codConsulenza) {
    const normalizedCodConsulenza = normalizeApoValue(codConsulenza);
    if (!Array.isArray(items) || !normalizedCodConsulenza) {
        return Array.isArray(items) ? items.slice() : [];
    }

    return items.filter(function (item) {
        return normalizeApoValue(getApoAccessoCodConsulenza(item)) !== normalizedCodConsulenza;
    });
}

function resetApoSelectionAfterDeletedConsulenza(deletedAccesso) {
    const codConsulenza = getApoAccessoCodConsulenza(deletedAccesso);
    const mainAccesso = getApoAccessoById(apoState.mainAccessoId);
    const deletingMainAccesso = deletedAccesso && mainAccesso && isSameApoAccesso(deletedAccesso, mainAccesso);
    const fallbackMainId = deletingMainAccesso ? "" : String(apoState.mainAccessoId || "").trim();

    apoState.accessi = removeApoAccessoByCodConsulenza(apoState.accessi, codConsulenza);
    apoState.filteredAccessi = removeApoAccessoByCodConsulenza(apoState.filteredAccessi, codConsulenza);
    apoState.storicoAccessi = removeApoAccessoByCodConsulenza(apoState.storicoAccessi, codConsulenza);

    if (deletingMainAccesso) {
        apoState.mainAccessoId = "";
        apoState.selectedAccessoId = "";
        clearApoStoricoAccessi();
        setApoActiveSection("accessi");
        return;
    }

    if (fallbackMainId) {
        apoState.selectedAccessoId = fallbackMainId;
    } else {
        apoState.selectedAccessoId = "";
    }
    clearApoStoricoAccessi();
    renderApoAccessi();
    renderApoSelectedPaziente();
    renderApoPazienteBody();
}

async function confirmApoDeleteConsulenza() {
    const accesso = getApoAccessoById(apoState.pendingDeleteAccessoId);
    const codConsulenza = getApoAccessoCodConsulenza(accesso);
    const params = new URLSearchParams();

    if (!accesso || !codConsulenza) {
        setApoDeleteConfirmMessage("Consulenza selezionata non disponibile.", "danger");
        return;
    }

    params.set("codConsulenza", codConsulenza);
    setApoDeleteConsulenzaBusy(true);
    setApoDeleteConfirmMessage("", "info");

    try {
        const response = await fetch(apoDeleteAccessoUrl, {
            method: "POST",
            headers: {
                "Accept": "application/json",
                "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8"
            },
            body: params.toString()
        });

        if (response.status === 401) {
            window.location.href = apoLoginUrl;
            return;
        }

        let payload = null;
        try {
            payload = await response.json();
        } catch (error) {
            payload = null;
        }

        if (!response.ok || !payload || payload.esito !== "ok") {
            throw new Error(payload && payload.message ? payload.message : "Errore nell'eliminazione della consulenza.");
        }

        hideApoDeleteConsulenzaConfirmModal(false);
        resetApoSelectionAfterDeletedConsulenza(accesso);
        await searchApoAccessi();
        if (apoState.activeSection === "paziente" && getApoSelectedAccesso()) {
            prepareApoPazienteStoricoAccessi();
            renderApoSelectedPaziente();
            renderApoPazienteBody();
        }
        setApoSearchMessage(payload.message || "Consulenza eliminata correttamente.", "success");
    } catch (error) {
        setApoDeleteConfirmMessage(error && error.message ? error.message : "Errore nell'eliminazione della consulenza.", "danger");
    } finally {
        setApoDeleteConsulenzaBusy(false);
    }
}

function getApoSelectedConsulenzaCodServizio() {
    const input = document.getElementById("cod_servizio_consulenza");
    return normalizeApoDisplayValue(input ? input.value : "");
}

function buildApoAllegatoMReportUrl(item, useSelectedCodServizio) {
    if (!item) {
        return {url: "", message: "Impossibile aprire il report Allegato M."};
    }

    const codConsulenza = normalizeApoDisplayValue(item.codConsulenze || item.codConsulenza || item.consulenza);
    const codServizio = useSelectedCodServizio === true
        ? (getApoSelectedConsulenzaCodServizio() || normalizeApoDisplayValue(item.codServizio))
        : normalizeApoDisplayValue(item.codServizio);

    if (!codConsulenza || !codServizio) {
        return {url: "", message: "Parametri insufficienti per l'apertura del report (Codice Consulenza o Codice Servizio mancante)."};
    }

    const params = new URLSearchParams();
    params.set("COD_CONSULENZA", codConsulenza);
    params.set("COD_SERVIZIO", codServizio);

    return {url: buildApoUrl("StampaReport") + "?" + params.toString(), message: ""};
}

function openApoAllegatoMReport(item, useSelectedCodServizio) {
    const result = buildApoAllegatoMReportUrl(item, useSelectedCodServizio);

    if (!result.url) {
        window.alert(result.message || "Impossibile aprire il report Allegato M.");
        return;
    }

    openApoBrowserPopup(result.url, "APOReportAllegatoM");
}

function setApoPazienteSection(sectionId) {
    const config = getApoAllowedPazienteSectionConfig(sectionId);

    apoState.activePazienteSection = config.id;
    syncApoPazienteMenuForUser();
    renderApoPazienteBody();
}

function getApoAccessoById(id) {
    const normalizedId = String(id || "").trim();
    if (!normalizedId) {
        return null;
    }

    for (let index = 0; index < apoState.filteredAccessi.length; index += 1) {
        if (String(apoState.filteredAccessi[index].id || "").trim() === normalizedId) {
            return apoState.filteredAccessi[index];
        }
    }

    for (let index = 0; index < apoState.storicoAccessi.length; index += 1) {
        if (String(apoState.storicoAccessi[index].id || "").trim() === normalizedId) {
            return apoState.storicoAccessi[index];
        }
    }

    return null;
}

function getApoAccessoGridById(id) {
    const normalizedId = String(id || "").trim();
    if (!normalizedId) {
        return null;
    }

    for (let index = 0; index < apoState.filteredAccessi.length; index += 1) {
        if (String(apoState.filteredAccessi[index].id || "").trim() === normalizedId) {
            return apoState.filteredAccessi[index];
        }
    }

    return null;
}

function getApoSelectedAccesso() {
    return getApoAccessoById(apoState.selectedAccessoId);
}

function createApoDetailField(labelText, valueText, extraClass) {
    const field = document.createElement("div");
    field.className = "apo-detail-field" + (extraClass ? " " + extraClass : "");

    const label = document.createElement("span");
    label.className = "apo-detail-label";
    label.textContent = labelText;

    const value = document.createElement("span");
    value.className = "apo-detail-value";
    value.textContent = String(valueText || "").trim() || "-";

    field.appendChild(label);
    field.appendChild(value);
    return field;
}

function createApoDetailSection(titleText) {
    const section = document.createElement("section");
    section.className = "apo-detail-section";

    const title = document.createElement("div");
    title.className = "apo-detail-section-title";
    title.textContent = titleText;

    section.appendChild(title);
    return section;
}

function renderApoDetail() {
    const detailBody = document.getElementById("apo-detail-body");
    const accesso = getApoSelectedAccesso();

    if (!detailBody) {
        return;
    }

    detailBody.innerHTML = "";

    if (!accesso) {
        return;
    }

    const content = document.createElement("div");
    content.className = "apo-detail-content";

    const sheet = document.createElement("article");
    sheet.className = "apo-detail-sheet";

    const patientSection = createApoDetailSection("Dati assistito");
    const patientGrid = document.createElement("div");
    patientGrid.className = "apo-detail-grid";
    patientGrid.appendChild(createApoDetailField("Paziente", accesso.paziente || "-"));
    patientGrid.appendChild(createApoDetailField("Codice fiscale", accesso.codFiscale || "-"));
    patientSection.appendChild(patientGrid);

    const clinicalSection = createApoDetailSection("Indicazioni cliniche");
    const clinicalGrid = document.createElement("div");
    clinicalGrid.className = "apo-detail-grid";
    clinicalGrid.appendChild(createApoDetailField("Operatore", accesso.operatore || accesso.medico || "-"));
    clinicalGrid.appendChild(createApoDetailField("Prestazione", accesso.valore || "-"));
    clinicalGrid.appendChild(createApoDetailField("Note", accesso.note || "-", "apo-detail-field-notes"));
    clinicalSection.appendChild(clinicalGrid);

    sheet.appendChild(patientSection);
    sheet.appendChild(clinicalSection);
    content.appendChild(sheet);

    detailBody.appendChild(content);
}

function setApoDetailOpen(isOpen) {
    const layout = document.getElementById("apo-accessi-layout");
    const detailCard = document.getElementById("apo-detail-card");
    const detailBody = document.getElementById("apo-detail-body");
    const shouldOpen = isOpen === true && String(apoState.selectedAccessoId || "").trim() !== "";

    apoState.detailOpen = shouldOpen;

    if (layout) {
        layout.classList.toggle("is-detail-open", shouldOpen);
    }
    if (detailCard) {
        detailCard.setAttribute("aria-hidden", shouldOpen ? "false" : "true");
    }
    if (detailBody && !shouldOpen) {
        detailBody.innerHTML = "";
    }
}

function appendApoCell(row, value) {
    const cell = document.createElement("td");
    cell.textContent = String(value || "").trim();
    row.appendChild(cell);
}

function formatApoAccessoGridDate(accesso) {
    return normalizeApoDisplayValue(accesso && (accesso.dataOra || accesso.dataDisplay));
}

function getApoNuovoAccessoFilterValue(id) {
    const element = document.getElementById(id);
    return normalizeApoDisplayValue(element ? element.value : "");
}

function setApoNuovoAccessoMessage(message, type) {
    const box = document.getElementById("apo-new-accesso-message");
    if (!box) {
        return;
    }

    box.className = "alert d-none";
    box.textContent = "";

    const text = String(message || "").trim();
    if (!text) {
        return;
    }

    const normalizedType = String(type || "info").trim().toLowerCase();
    const typeClass = normalizedType === "danger" ? "alert-danger"
        : normalizedType === "warning" ? "alert-warning"
        : normalizedType === "success" ? "alert-success"
        : "alert-info";

    box.textContent = text;
    box.className = "alert " + typeClass;
}

function setApoNuovoAccessoNoPazienteMessage() {
    const box = document.getElementById("apo-new-accesso-message");
    if (!box) {
        return;
    }

    box.className = "alert alert-warning";
    box.textContent = "";
    box.appendChild(document.createTextNode("Nessun paziente trovato. Per censire il paziente, premere "));

    const link = document.createElement("a");
    link.href = "#";
    link.className = "apo-new-accesso-censimento-link";
    link.textContent = "qui";
    link.addEventListener("click", function (event) {
        event.preventDefault();
        openApoCensimentoPazienteModal();
    });

    box.appendChild(link);
    box.appendChild(document.createTextNode("."));
}

function setApoNuovoAccessoBusy(isBusy) {
    const searchButton = document.getElementById("apo-new-accesso-search");
    apoState.nuovoAccessoLoading = isBusy === true;
    if (searchButton) {
        searchButton.disabled = apoState.nuovoAccessoLoading;
        searchButton.textContent = apoState.nuovoAccessoLoading ? "Ricerca..." : "Cerca";
    }
    updateApoNuovoAccessoSelectButton();
}

function updateApoNuovoAccessoSelectButton() {
    const selectButton = document.getElementById("apo-new-accesso-select-patient");
    if (!selectButton) {
        return;
    }

    selectButton.disabled = apoState.nuovoAccessoLoading || !String(apoState.nuovoAccessoSelectedId || "").trim();
}

function renderApoNuovoAccessoResults() {
    const tbody = document.getElementById("apo-new-accesso-results-body");
    if (!tbody) {
        return;
    }

    tbody.innerHTML = "";

    if (!apoState.nuovoAccessoResults.length) {
        const row = document.createElement("tr");
        row.className = "riepilogo-empty-row";
        const cell = document.createElement("td");
        cell.colSpan = 7;
        cell.textContent = apoState.nuovoAccessoLoading ? "Ricerca in corso..." : "Nessun paziente da visualizzare.";
        row.appendChild(cell);
        tbody.appendChild(row);
        return;
    }

    apoState.nuovoAccessoResults.forEach(function (item) {
        const row = document.createElement("tr");
        row.dataset.nuovoAccessoPatientId = item.id;
        row.tabIndex = 0;
        row.setAttribute("role", "button");
        row.classList.toggle("is-selected", item.id === apoState.nuovoAccessoSelectedId);

        appendApoCell(row, item.codFiscale);
        appendApoCell(row, item.cognome);
        appendApoCell(row, item.nome);
        appendApoCell(row, item.dataNascita);
        appendApoCell(row, item.sesso);
        appendApoCell(row, item.eta);
        appendApoCell(row, item.pin);

        tbody.appendChild(row);
    });
}

function openApoNuovoAccessoModal() {
    const modal = document.getElementById("apo-new-accesso-modal");
    const form = document.getElementById("apo-new-accesso-form");
    const codiceFiscale = document.getElementById("apo-new-accesso-cf");
    if (!modal) {
        return;
    }

    apoState.nuovoAccessoLastFocus = document.activeElement;
    apoState.nuovoAccessoResults = [];
    apoState.nuovoAccessoSelectedId = "";
    apoState.nuovoAccessoError = "";
    setApoNuovoAccessoBusy(false);
    setApoNuovoAccessoMessage("", "info");
    if (form) {
        form.reset();
    }
    renderApoNuovoAccessoResults();
    updateApoNuovoAccessoSelectButton();

    modal.classList.remove("is-hidden");
    modal.setAttribute("aria-hidden", "false");
    if (codiceFiscale) {
        codiceFiscale.focus();
    }
}

function closeApoNuovoAccessoModal(restoreFocus) {
    const modal = document.getElementById("apo-new-accesso-modal");
    if (modal) {
        modal.classList.add("is-hidden");
        modal.setAttribute("aria-hidden", "true");
    }

    setApoNuovoAccessoBusy(false);
    if (restoreFocus && apoState.nuovoAccessoLastFocus && typeof apoState.nuovoAccessoLastFocus.focus === "function") {
        apoState.nuovoAccessoLastFocus.focus();
    }
    apoState.nuovoAccessoLastFocus = null;
}

function setApoCensimentoMessage(message, type) {
    const box = document.getElementById("apo-censimento-message");
    if (!box) {
        return;
    }

    box.className = "alert d-none";
    box.textContent = "";

    const text = String(message || "").trim();
    if (!text) {
        return;
    }

    const normalizedType = String(type || "info").trim().toLowerCase();
    const typeClass = normalizedType === "danger" ? "alert-danger"
        : normalizedType === "warning" ? "alert-warning"
        : normalizedType === "success" ? "alert-success"
        : "alert-info";

    box.textContent = text;
    box.className = "alert " + typeClass;
}

function normalizeApoCensimentoLookupItem(item, codeKey, labelKey) {
    const source = item && typeof item === "object" ? item : {};
    const code = normalizeApoDisplayValue(source[codeKey] || source.codice || source.code);
    const label = normalizeApoDisplayValue(source[labelKey] || source.descrizione || source.label || source.desComune);
    return {
        code: code,
        label: label
    };
}

function renderApoCensimentoLookupDatalist(listId, items) {
    const datalist = document.getElementById(listId);
    if (!datalist) {
        return;
    }

    datalist.innerHTML = "";
    (Array.isArray(items) ? items : []).forEach(function (item) {
        if (!item.code || !item.label) {
            return;
        }

        const option = document.createElement("option");
        option.value = item.label;
        option.label = item.code;
        option.dataset.code = item.code;
        datalist.appendChild(option);
    });
}

async function loadApoCensimentoComuni() {
    if (apoState.censimentoComuniLoaded || apoState.censimentoComuniLoading) {
        return;
    }

    apoState.censimentoComuniLoading = true;
    try {
        const response = await fetch(apoComuniSuggestUrl, {
            headers: {
                "Accept": "application/json"
            }
        });
        if (response.status === 401) {
            window.location.href = apoLoginUrl;
            return;
        }

        let payload = null;
        try {
            payload = await response.json();
        } catch (error) {
            payload = null;
        }
        if (!response.ok || !payload || payload.esito !== "ok") {
            throw new Error(payload && payload.message ? payload.message : "Errore nel caricamento dei comuni.");
        }

        apoState.censimentoComuni = (payload.data && Array.isArray(payload.data.comuni) ? payload.data.comuni : []).map(function (item) {
            return normalizeApoCensimentoLookupItem(item, "codComune", "desComune");
        });
        apoState.censimentoComuniLoaded = true;
        renderApoCensimentoLookupDatalist("apo-censimento-comuni-list", apoState.censimentoComuni);
        syncApoCensimentoLookupFields();
    } catch (error) {
        apoState.censimentoComuni = [];
        apoState.censimentoComuniLoaded = false;
        setApoCensimentoMessage(error && error.message ? error.message : "Errore nel caricamento dei comuni.", "warning");
    } finally {
        apoState.censimentoComuniLoading = false;
    }
}

async function loadApoCensimentoAsl() {
    if (apoState.censimentoAslLoaded || apoState.censimentoAslLoading) {
        return;
    }

    apoState.censimentoAslLoading = true;
    try {
        const response = await fetch(apoAslSuggestUrl, {
            headers: {
                "Accept": "application/json"
            }
        });
        if (response.status === 401) {
            window.location.href = apoLoginUrl;
            return;
        }

        let payload = null;
        try {
            payload = await response.json();
        } catch (error) {
            payload = null;
        }
        if (!response.ok || !payload || payload.esito !== "ok") {
            throw new Error(payload && payload.message ? payload.message : "Errore nel caricamento delle ASL.");
        }

        apoState.censimentoAsl = (payload.data && Array.isArray(payload.data.asl) ? payload.data.asl : []).map(function (item) {
            return normalizeApoCensimentoLookupItem(item, "codAsl", "descrizione");
        });
        apoState.censimentoAslLoaded = true;
        renderApoCensimentoLookupDatalist("apo-censimento-asl-list", apoState.censimentoAsl);
        syncApoCensimentoLookupFields();
    } catch (error) {
        apoState.censimentoAsl = [];
        apoState.censimentoAslLoaded = false;
        setApoCensimentoMessage(error && error.message ? error.message : "Errore nel caricamento delle ASL.", "warning");
    } finally {
        apoState.censimentoAslLoading = false;
    }
}

function loadApoCensimentoLookups() {
    void loadApoCensimentoComuni();
    void loadApoCensimentoAsl();
}

function getApoCensimentoLookupItems(listType) {
    return listType === "asl" ? apoState.censimentoAsl : apoState.censimentoComuni;
}

function findApoCensimentoLookupItem(listType, value) {
    const normalizedValue = normalizeApoValue(value);
    const items = getApoCensimentoLookupItems(listType);
    if (!normalizedValue || !Array.isArray(items)) {
        return null;
    }

    for (let index = 0; index < items.length; index += 1) {
        if (normalizeApoValue(items[index].label) === normalizedValue
                || normalizeApoValue(items[index].code) === normalizedValue) {
            return items[index];
        }
    }
    return null;
}

function syncApoCensimentoLookupField(config) {
    const input = document.getElementById(config.inputId);
    const hidden = document.getElementById(config.hiddenId);
    if (!input || !hidden) {
        return;
    }

    const item = findApoCensimentoLookupItem(config.listType, input.value);
    hidden.value = item ? item.code : "";
}

function syncApoCensimentoLookupFields() {
    apoCensimentoLookupFields.forEach(function (config) {
        syncApoCensimentoLookupField(config);
    });
}

function clearApoCensimentoLookupCodes() {
    apoCensimentoLookupFields.forEach(function (config) {
        const hidden = document.getElementById(config.hiddenId);
        if (hidden) {
            hidden.value = "";
        }
    });
}

function getApoCensimentoElement(name) {
    const form = document.getElementById("apo-censimento-form");
    return form && form.elements ? form.elements[name] : null;
}

function getApoCensimentoValue(name) {
    const element = getApoCensimentoElement(name);
    if (!element) {
        return "";
    }
    if (typeof RadioNodeList !== "undefined" && element instanceof RadioNodeList) {
        return normalizeApoDisplayValue(element.value);
    }
    return normalizeApoDisplayValue(element.value);
}

function focusApoCensimentoField(name) {
    const element = getApoCensimentoElement(name);
    if (!element) {
        return;
    }
    if (typeof RadioNodeList !== "undefined" && element instanceof RadioNodeList) {
        for (let index = 0; index < element.length; index += 1) {
            if (element[index] && typeof element[index].focus === "function") {
                element[index].focus();
                return;
            }
        }
        return;
    }
    if (typeof element.focus === "function") {
        element.focus();
    }
}

function setApoCensimentoBusy(isBusy) {
    const busy = isBusy === true;
    const saveButton = document.getElementById("apo-censimento-save");
    const cancelButton = document.getElementById("apo-censimento-cancel");
    const closeButton = document.getElementById("apo-censimento-close");
    const calculateButton = document.getElementById("apo-censimento-calcola-cf");

    apoState.censimentoSaving = busy;
    if (saveButton) {
        saveButton.disabled = busy;
        saveButton.textContent = busy ? "Salvataggio..." : "Salva";
    }
    if (cancelButton) {
        cancelButton.disabled = busy;
    }
    if (closeButton) {
        closeButton.setAttribute("aria-disabled", busy ? "true" : "false");
    }
    if (calculateButton) {
        calculateButton.disabled = busy;
    }

    document.querySelectorAll("#apo-censimento-form input").forEach(function (input) {
        input.disabled = busy;
    });
}

function fillApoCensimentoFromNuovoAccessoSearch() {
    const values = {
        "apo-censimento-codice-fiscale": getApoNuovoAccessoFilterValue("apo-new-accesso-cf"),
        "apo-censimento-cognome": getApoNuovoAccessoFilterValue("apo-new-accesso-cognome"),
        "apo-censimento-nome": getApoNuovoAccessoFilterValue("apo-new-accesso-nome")
    };

    Object.keys(values).forEach(function (id) {
        const input = document.getElementById(id);
        if (input) {
            input.value = values[id];
        }
    });
}

function openApoCensimentoPazienteModal() {
    const modal = document.getElementById("apo-censimento-modal");
    const form = document.getElementById("apo-censimento-form");
    const codiceFiscale = document.getElementById("apo-censimento-codice-fiscale");
    if (!modal) {
        return;
    }

    apoState.censimentoLastFocus = document.activeElement;
    if (form) {
        form.reset();
    }
    clearApoCensimentoLookupCodes();
    fillApoCensimentoFromNuovoAccessoSearch();
    setApoCensimentoBusy(false);
    setApoCensimentoMessage("", "info");
    loadApoCensimentoLookups();

    modal.classList.remove("is-hidden");
    modal.setAttribute("aria-hidden", "false");
    if (codiceFiscale) {
        codiceFiscale.focus();
    }
}

function closeApoCensimentoPazienteModal(restoreFocus) {
    const modal = document.getElementById("apo-censimento-modal");
    if (apoState.censimentoSaving) {
        return;
    }
    if (modal) {
        modal.classList.add("is-hidden");
        modal.setAttribute("aria-hidden", "true");
    }
    if (restoreFocus && apoState.censimentoLastFocus && typeof apoState.censimentoLastFocus.focus === "function") {
        apoState.censimentoLastFocus.focus();
    }
    apoState.censimentoLastFocus = null;
}

function getApoCensimentoPayload() {
    syncApoCensimentoLookupFields();

    const fields = [
        "codiceFiscale",
        "cognome",
        "nome",
        "dataNascita",
        "sesso",
        "comuneNascita",
        "codComuneNascita",
        "indirizzoResidenza",
        "comuneResidenza",
        "codComuneResidenza",
        "capResidenza",
        "aslResidenza",
        "codAslResidenza",
        "telResidenza",
        "indirizzoDomicilio",
        "comuneDomicilio",
        "codComuneDomicilio",
        "capDomicilio",
        "aslIscrizione",
        "codAslIscrizione",
        "telDomicilio"
    ];
    const payload = {};

    fields.forEach(function (field) {
        payload[field] = getApoCensimentoValue(field);
    });
    payload.codiceFiscale = normalizeApoValue(payload.codiceFiscale);
    payload.cognome = normalizeApoValue(payload.cognome);
    payload.nome = normalizeApoValue(payload.nome);
    payload.sesso = normalizeApoValue(payload.sesso);
    payload.capResidenza = normalizeApoValue(payload.capResidenza);
    payload.capDomicilio = normalizeApoValue(payload.capDomicilio);
    payload.codComuneNascita = normalizeApoValue(payload.codComuneNascita);
    payload.codComuneResidenza = normalizeApoValue(payload.codComuneResidenza);
    payload.codComuneDomicilio = normalizeApoValue(payload.codComuneDomicilio);
    payload.codAslResidenza = normalizeApoValue(payload.codAslResidenza);
    payload.codAslIscrizione = normalizeApoValue(payload.codAslIscrizione);

    return payload;
}

function validateApoCensimentoPayload(payload) {
    const requiredFields = [
        {name: "codiceFiscale", label: "Codice fiscale"},
        {name: "cognome", label: "Cognome"},
        {name: "nome", label: "Nome"},
        {name: "dataNascita", label: "Data nascita"},
        {name: "sesso", label: "Sesso"},
        {name: "codComuneNascita", label: "Comune nascita", focus: "comuneNascita"},
        {name: "indirizzoResidenza", label: "Indirizzo residenza"},
        {name: "codComuneResidenza", label: "Comune residenza", focus: "comuneResidenza"},
        {name: "capResidenza", label: "Cap residenza"},
        {name: "codAslResidenza", label: "Asl residenza", focus: "aslResidenza"},
        {name: "codAslIscrizione", label: "Asl iscrizione", focus: "aslIscrizione"}
    ];

    for (let index = 0; index < requiredFields.length; index += 1) {
        const field = requiredFields[index];
        if (!normalizeApoDisplayValue(payload[field.name])) {
            if (field.focus && normalizeApoDisplayValue(payload[field.focus])) {
                return {ok: false, field: field.focus, message: "Seleziona il campo " + field.label + " dai suggerimenti."};
            }
            return {ok: false, field: field.focus || field.name, message: "Valorizza il campo " + field.label + "."};
        }
    }

    if (payload.codiceFiscale.length !== 16) {
        return {ok: false, field: "codiceFiscale", message: "Il codice fiscale deve essere di 16 caratteri."};
    }
    if (payload.sesso !== "M" && payload.sesso !== "F") {
        return {ok: false, field: "sesso", message: "Seleziona M o F per il sesso."};
    }
    if (payload.capResidenza.length !== 5) {
        return {ok: false, field: "capResidenza", message: "Il CAP residenza deve essere di 5 caratteri."};
    }
    if (payload.capDomicilio && payload.capDomicilio.length !== 5) {
        return {ok: false, field: "capDomicilio", message: "Il CAP domicilio deve essere di 5 caratteri."};
    }
    if (payload.comuneDomicilio && !payload.codComuneDomicilio) {
        return {ok: false, field: "comuneDomicilio", message: "Seleziona il Comune domicilio dai suggerimenti."};
    }

    return {ok: true, field: "", message: ""};
}

async function calculateApoCensimentoCodiceFiscale() {
    const payload = getApoCensimentoPayload();
    const requiredFields = [
        {name: "cognome", label: "Cognome"},
        {name: "nome", label: "Nome"},
        {name: "dataNascita", label: "Data nascita"},
        {name: "sesso", label: "Sesso"},
        {name: "codComuneNascita", label: "Comune nascita", focus: "comuneNascita"}
    ];
    for (let index = 0; index < requiredFields.length; index += 1) {
        const field = requiredFields[index];
        if (!normalizeApoDisplayValue(payload[field.name])) {
            const message = field.focus && normalizeApoDisplayValue(payload[field.focus])
                ? "Seleziona il campo " + field.label + " dai suggerimenti per calcolare il codice fiscale."
                : "Valorizza il campo " + field.label + " per calcolare il codice fiscale.";
            setApoCensimentoMessage(message, "warning");
            focusApoCensimentoField(field.focus || field.name);
            return;
        }
    }

    const params = new URLSearchParams();
    params.set("cognome", payload.cognome);
    params.set("nome", payload.nome);
    params.set("dataNascita", payload.dataNascita);
    params.set("sesso", payload.sesso);
    params.set("comuneNascita", payload.comuneNascita);
    params.set("codComuneNascita", payload.codComuneNascita);

    const calculateButton = document.getElementById("apo-censimento-calcola-cf");
    if (calculateButton) {
        calculateButton.disabled = true;
        calculateButton.textContent = "Calcolo...";
    }
    setApoCensimentoMessage("", "info");

    try {
        const response = await fetch(apoCodiceFiscaleUrl + "?" + params.toString(), {
            headers: {
                "Accept": "application/json"
            }
        });
        if (response.status === 401) {
            window.location.href = apoLoginUrl;
            return;
        }

        let payloadResponse = null;
        try {
            payloadResponse = await response.json();
        } catch (error) {
            payloadResponse = null;
        }
        if (!response.ok || !payloadResponse || payloadResponse.esito !== "ok") {
            throw new Error(payloadResponse && payloadResponse.message ? payloadResponse.message : "Impossibile calcolare il codice fiscale.");
        }

        const codiceFiscale = normalizeApoValue(payloadResponse.data && payloadResponse.data.codiceFiscale);
        const input = document.getElementById("apo-censimento-codice-fiscale");
        if (input) {
            input.value = codiceFiscale;
            input.focus();
        }
        setApoCensimentoMessage("Codice fiscale calcolato.", "success");
    } catch (error) {
        setApoCensimentoMessage(error && error.message ? error.message : "Impossibile calcolare il codice fiscale.", "danger");
    } finally {
        if (calculateButton) {
            calculateButton.disabled = false;
            calculateButton.textContent = "Calcola";
        }
    }
}

async function saveApoCensimentoPaziente(payload) {
    const params = new URLSearchParams();
    Object.keys(payload || {}).forEach(function (key) {
        params.set(key, normalizeApoDisplayValue(payload[key]));
    });

    setApoCensimentoBusy(true);
    setApoCensimentoMessage("", "info");

    try {
        const response = await fetch(apoAnagrafeNewUrl, {
            method: "POST",
            headers: {
                "Accept": "application/json",
                "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8"
            },
            body: params.toString()
        });

        if (response.status === 401) {
            window.location.href = apoLoginUrl;
            return;
        }

        let payloadResponse = null;
        try {
            payloadResponse = await response.json();
        } catch (error) {
            payloadResponse = null;
        }

        if (!response.ok || !payloadResponse || payloadResponse.esito !== "ok") {
            throw new Error(payloadResponse && payloadResponse.message ? payloadResponse.message : "Errore nel censimento del paziente.");
        }

        const created = normalizeApoAnagrafePaziente(payloadResponse.data && payloadResponse.data.paziente, 0);
        apoState.nuovoAccessoResults = created.codFiscale ? [created] : [];
        apoState.nuovoAccessoSelectedId = created.id || "";
        renderApoNuovoAccessoResults();
        updateApoNuovoAccessoSelectButton();
        setApoCensimentoBusy(false);
        closeApoCensimentoPazienteModal(false);
        setApoNuovoAccessoMessage("Paziente censito e selezionato.", "success");
    } catch (error) {
        setApoCensimentoMessage(error && error.message ? error.message : "Errore nel censimento del paziente.", "danger");
    } finally {
        setApoCensimentoBusy(false);
    }
}

function handleApoCensimentoSubmit(event) {
    if (event) {
        event.preventDefault();
    }

    const payload = getApoCensimentoPayload();
    const validation = validateApoCensimentoPayload(payload);
    if (!validation.ok) {
        setApoCensimentoMessage(validation.message, "warning");
        focusApoCensimentoField(validation.field);
        return;
    }

    void saveApoCensimentoPaziente(payload);
}

function getApoNuovoAccessoSelectedPaziente() {
    const selectedId = String(apoState.nuovoAccessoSelectedId || "").trim();
    if (!selectedId) {
        return null;
    }

    for (let index = 0; index < apoState.nuovoAccessoResults.length; index += 1) {
        if (apoState.nuovoAccessoResults[index].id === selectedId) {
            return apoState.nuovoAccessoResults[index];
        }
    }

    return null;
}

function getApoCurrentDateTimeLocal() {
    const now = new Date();
    const pad = function (value) {
        return String(value).padStart(2, "0");
    };

    return now.getFullYear() + "-"
        + pad(now.getMonth() + 1) + "-"
        + pad(now.getDate()) + "T"
        + pad(now.getHours()) + ":"
        + pad(now.getMinutes());
}

function normalizeApoNuovoAccessoTipo(tipo) {
    return tipo === "certificazioni" ? "certificazioni" : "visita-medica";
}

function getApoNuovoAccessoTipoConfig(tipo) {
    const normalizedTipo = normalizeApoNuovoAccessoTipo(tipo);
    if (normalizedTipo === "certificazioni") {
        return {
            id: "certificazioni",
            label: "Certificazioni",
            code: "CERT_AM2"
        };
    }

    return {
        id: "visita-medica",
        label: "Visita medica",
        code: "VMED_AM2"
    };
}

function getApoNuovoAccessoSelectedTipo() {
    const selectedTipi = Array.isArray(apoState.nuovoAccessoSelectedTipi) ? apoState.nuovoAccessoSelectedTipi : [];
    return normalizeApoNuovoAccessoTipo(selectedTipi.length ? selectedTipi[0] : "visita-medica");
}

function normalizeApoNuovoAccessoPatologia(value) {
    const normalizedValue = normalizeApoValue(value);
    if (normalizedValue === "INTERINALE") {
        return "interinale";
    }
    if (normalizedValue === "CHIRURGICA") {
        return "chirurgica";
    }
    return "ortopedica";
}

function getApoNuovoAccessoPatologiaConfig(value) {
    const normalizedValue = normalizeApoNuovoAccessoPatologia(value);
    if (normalizedValue === "interinale") {
        return {id: "interinale", label: "Interinale"};
    }
    if (normalizedValue === "chirurgica") {
        return {id: "chirurgica", label: "Chirurgica"};
    }
    return {id: "ortopedica", label: "Ortopedica"};
}

function syncApoNuovoAccessoPatologiaButtons() {
    const selectedPatologia = normalizeApoNuovoAccessoPatologia(apoState.nuovoAccessoPatologia);
    document.querySelectorAll("[data-new-accesso-patologia]").forEach(function (button) {
        const patologia = normalizeApoNuovoAccessoPatologia(button.getAttribute("data-new-accesso-patologia"));
        const isActive = patologia === selectedPatologia;
        button.classList.toggle("is-active", isActive);
        button.setAttribute("aria-pressed", isActive ? "true" : "false");
    });
}

function resetApoNuovoAccessoPatologia() {
    apoState.nuovoAccessoPatologia = "ortopedica";
    syncApoNuovoAccessoPatologiaButtons();
}

function toggleApoNuovoAccessoPatologia(value) {
    apoState.nuovoAccessoPatologia = normalizeApoNuovoAccessoPatologia(value);
    syncApoNuovoAccessoPatologiaButtons();
}

function syncApoNuovoAccessoTipoHiddenValue() {
    const hiddenInput = document.getElementById("apo-new-accesso-tipo-codice");
    if (!hiddenInput) {
        return;
    }

    hiddenInput.value = getApoNuovoAccessoTipoConfig(getApoNuovoAccessoSelectedTipo()).code;
}

function syncApoNuovoAccessoTipoButtons() {
    const selectedTipo = getApoNuovoAccessoSelectedTipo();
    document.querySelectorAll("[data-new-accesso-type]").forEach(function (button) {
        const tipo = normalizeApoNuovoAccessoTipo(button.getAttribute("data-new-accesso-type"));
        const isActive = tipo === selectedTipo;
        button.classList.toggle("is-active", isActive);
        button.setAttribute("aria-pressed", isActive ? "true" : "false");
    });
    syncApoNuovoAccessoTipoHiddenValue();
}

function resetApoNuovoAccessoTipi() {
    apoState.nuovoAccessoSelectedTipi = ["visita-medica"];
    syncApoNuovoAccessoTipoButtons();
}

function toggleApoNuovoAccessoTipo(tipo) {
    const normalizedTipo = normalizeApoNuovoAccessoTipo(tipo);
    apoState.nuovoAccessoSelectedTipi = [normalizedTipo];
    syncApoNuovoAccessoTipoButtons();
}

function setApoNuovoAccessoDetailMessage(message, type) {
    const box = document.getElementById("apo-new-accesso-detail-message");
    if (!box) {
        return;
    }

    box.className = "alert d-none";
    box.textContent = "";

    const text = String(message || "").trim();
    if (!text) {
        return;
    }

    const normalizedType = String(type || "info").trim().toLowerCase();
    const typeClass = normalizedType === "danger" ? "alert-danger"
        : normalizedType === "warning" ? "alert-warning"
        : normalizedType === "success" ? "alert-success"
        : "alert-info";

    box.textContent = text;
    box.className = "alert " + typeClass;
}

function setApoNuovoAccessoDetailBusy(isBusy) {
    const busy = isBusy === true;
    const confirmButton = document.getElementById("apo-new-accesso-confirm-add");
    const dataOra = document.getElementById("apo-new-accesso-data-ora");
    const diagnosi = document.getElementById("apo-new-accesso-diagnosi");
    const backButton = document.getElementById("apo-new-accesso-detail-back");

    if (confirmButton) {
        confirmButton.disabled = busy;
        confirmButton.textContent = busy ? "Inserimento..." : "Aggiungi nuovo accesso";
    }
    if (dataOra) {
        dataOra.disabled = busy;
    }
    if (diagnosi) {
        diagnosi.disabled = busy;
    }
    if (backButton) {
        backButton.disabled = busy;
    }

    document.querySelectorAll("[data-new-accesso-type]").forEach(function (button) {
        button.disabled = busy;
    });
    document.querySelectorAll("[data-new-accesso-patologia]").forEach(function (button) {
        button.disabled = busy;
    });
}

function appendApoNuovoAccessoPatientSummaryItem(container, label, value) {
    const item = document.createElement("div");
    item.className = "apo-new-accesso-selected-patient-item";

    const labelElement = document.createElement("span");
    labelElement.className = "apo-new-accesso-selected-patient-label";
    labelElement.textContent = label;

    const valueElement = document.createElement("strong");
    valueElement.textContent = normalizeApoDisplayValue(value) || "-";

    item.appendChild(labelElement);
    item.appendChild(valueElement);
    container.appendChild(item);
}

function renderApoNuovoAccessoSelectedPatientSummary(patient) {
    const box = document.getElementById("apo-new-accesso-selected-patient");
    if (!box) {
        return;
    }

    box.innerHTML = "";
    if (!patient) {
        return;
    }

    appendApoNuovoAccessoPatientSummaryItem(box, "Paziente", (patient.cognome + " " + patient.nome).trim());
    appendApoNuovoAccessoPatientSummaryItem(box, "Codice fiscale", patient.codFiscale);
    appendApoNuovoAccessoPatientSummaryItem(box, "Data nascita", patient.dataNascita);
    appendApoNuovoAccessoPatientSummaryItem(box, "Eta", patient.eta);
}

function openApoNuovoAccessoDetailModal() {
    const selected = getApoNuovoAccessoSelectedPaziente();
    const modal = document.getElementById("apo-new-accesso-detail-modal");
    const form = document.getElementById("apo-new-accesso-detail-form");
    const dataOra = document.getElementById("apo-new-accesso-data-ora");
    const diagnosi = document.getElementById("apo-new-accesso-diagnosi");
    if (!selected) {
        setApoNuovoAccessoMessage("Seleziona un paziente.", "warning");
        updateApoNuovoAccessoSelectButton();
        return;
    }
    if (!modal) {
        return;
    }

    apoState.nuovoAccessoDetailLastFocus = document.activeElement;
    closeApoNuovoAccessoModal(false);
    if (form) {
        form.reset();
    }
    if (dataOra) {
        dataOra.value = getApoCurrentDateTimeLocal();
    }
    if (diagnosi) {
        diagnosi.value = "";
    }
    setApoNuovoAccessoDetailBusy(false);
    setApoNuovoAccessoDetailMessage("", "info");
    resetApoNuovoAccessoTipi();
    resetApoNuovoAccessoPatologia();
    renderApoNuovoAccessoSelectedPatientSummary(selected);

    modal.classList.remove("is-hidden");
    modal.setAttribute("aria-hidden", "false");
    if (dataOra) {
        dataOra.focus();
    }
}

function closeApoNuovoAccessoDetailModal(restoreFocus) {
    const modal = document.getElementById("apo-new-accesso-detail-modal");
    if (modal) {
        modal.classList.add("is-hidden");
        modal.setAttribute("aria-hidden", "true");
    }

    if (restoreFocus) {
        let focusTarget = apoState.nuovoAccessoDetailLastFocus;
        const fallbackFocus = document.getElementById("apo-new-accesso-button");
        if (focusTarget && typeof focusTarget.closest === "function" && focusTarget.closest(".is-hidden")) {
            focusTarget = fallbackFocus;
        }
        if (!focusTarget) {
            focusTarget = fallbackFocus;
        }
        if (focusTarget && typeof focusTarget.focus === "function") {
            focusTarget.focus();
        }
    }
    apoState.nuovoAccessoDetailLastFocus = null;
    renderApoNuovoAccessoSelectedPatientSummary(null);
}

function getApoSchedaPazienteField(patient, aliases) {
    return getApoObjectDisplayValue(patient, aliases);
}

function formatApoSchedaPazienteFumo(value) {
    const normalizedValue = normalizeApoValue(value);
    if (normalizedValue === "1"
            || normalizedValue === "S"
            || normalizedValue === "SI"
            || normalizedValue === "Y"
            || normalizedValue === "YES"
            || normalizedValue === "TRUE"
            || normalizedValue === "T") {
        return "SI";
    }
    return "NO";
}

function formatApoSchedaPazienteSesso(value) {
    const normalizedValue = normalizeApoValue(value);
    if (normalizedValue === "M" || normalizedValue === "MASCHIO") {
        return "Maschio";
    }
    if (normalizedValue === "F" || normalizedValue === "FEMMINA") {
        return "Femmina";
    }
    return "Non specificato";
}

function formatApoSchedaPazienteEta(value) {
    const normalizedValue = normalizeApoDisplayValue(value);
    return normalizedValue ? normalizedValue + " et\u00e0" : "";
}

const apoSchedaPazienteStatoCivileOptions = [
    {value: "1", label: "Celibe/Nubile"},
    {value: "2", label: "Coniugato/a"},
    {value: "3", label: "Separato/a"},
    {value: "4", label: "Divorziato/a"},
    {value: "5", label: "Vedovo/a"},
    {value: "6", label: "Unito/a civilmente"},
    {value: "7", label: "Gi\u00e0 in unione civile (in decesso del partner)"},
    {value: "8", label: "Gi\u00e0 in unione civile (per scioglimento unione)"},
    {value: "9", label: "Non rilevato"},
    {value: "99", label: "Dato mancante"}
];

function getApoSchedaPazienteStatoCivileValue(value) {
    const normalizedValue = normalizeApoValue(value);
    for (let index = 0; index < apoSchedaPazienteStatoCivileOptions.length; index += 1) {
        const option = apoSchedaPazienteStatoCivileOptions[index];
        if (normalizedValue === normalizeApoValue(option.value)
                || normalizedValue === normalizeApoValue(option.label)) {
            return option.value;
        }
    }
    return "99";
}

function createApoSchedaPazienteItem(labelText, valueText, isWide) {
    const item = document.createElement("div");
    item.className = "apo-scheda-paziente-item" + (isWide ? " apo-scheda-paziente-item-wide" : "");

    const label = document.createElement("span");
    label.className = "apo-scheda-paziente-label";
    label.textContent = labelText;

    const value = document.createElement("span");
    value.className = "apo-scheda-paziente-value";
    value.textContent = normalizeApoDisplayValue(valueText) || "-";

    item.appendChild(label);
    item.appendChild(value);
    return item;
}

function createApoSchedaPazienteSection(titleText) {
    const section = document.createElement("section");
    section.className = "apo-scheda-paziente-section";

    const title = document.createElement("h3");
    title.className = "apo-scheda-paziente-section-title";
    title.textContent = titleText;

    const grid = document.createElement("div");
    grid.className = "apo-scheda-paziente-section-grid";

    section.appendChild(title);
    section.appendChild(grid);
    return {section: section, grid: grid};
}

function createApoSchedaPazienteTextareaItem(labelText, valueText, fieldName) {
    const item = document.createElement("label");
    item.className = "apo-scheda-paziente-item apo-scheda-paziente-item-wide";

    const label = document.createElement("span");
    label.className = "apo-scheda-paziente-label";
    label.textContent = labelText;

    const textarea = document.createElement("textarea");
    textarea.className = "apo-scheda-paziente-textarea";
    textarea.value = normalizeApoDisplayValue(valueText);
    if (fieldName) {
        textarea.setAttribute("data-scheda-paziente-field", fieldName);
    }

    item.appendChild(label);
    item.appendChild(textarea);
    return item;
}

function createApoSchedaPazienteDiarioItem(source) {
    const diarioItem = createApoSchedaPazienteTextareaItem(
        "Diario",
        getApoSchedaPazienteField(source, ["diario", "notaDiario", "noteDiario"]),
        "diario"
    );
    const diarioStorico = normalizeApoSchedaPazienteDiario(
        Array.isArray(source && source.diarioStorico) ? source.diarioStorico : []
    );

    diarioItem.classList.add("apo-scheda-paziente-diario-item");
    if (!diarioStorico.length) {
        return diarioItem;
    }

    const list = document.createElement("div");
    list.className = "apo-scheda-paziente-diario-storico";

    diarioStorico.forEach(function (itemData) {
        const row = document.createElement("article");
        row.className = "apo-scheda-paziente-diario-storico-item";

        const dateBlock = document.createElement("div");
        dateBlock.className = "apo-scheda-paziente-diario-storico-date-block";

        const date = document.createElement("span");
        date.className = "apo-scheda-paziente-diario-storico-date";
        date.textContent = itemData.dataDiario || "-";
        dateBlock.appendChild(date);
        appendApoSchedaPazienteUtenteIns(dateBlock, itemData, "apo-scheda-paziente-diario-storico-utente");

        const description = document.createElement("span");
        description.className = "apo-scheda-paziente-diario-storico-text";
        description.textContent = itemData.descrizione || "-";

        row.appendChild(dateBlock);
        row.appendChild(description);
        list.appendChild(row);
    });

    diarioItem.appendChild(list);
    return diarioItem;
}

function createApoSchedaPazienteInputItem(labelText, valueText, isWide, fieldName) {
    const item = document.createElement("label");
    item.className = "apo-scheda-paziente-item" + (isWide ? " apo-scheda-paziente-item-wide" : "");

    const label = document.createElement("span");
    label.className = "apo-scheda-paziente-label";
    label.textContent = labelText;

    const input = document.createElement("input");
    input.type = "text";
    input.className = "form-control apo-scheda-paziente-input";
    input.value = normalizeApoDisplayValue(valueText);
    if (fieldName) {
        input.setAttribute("data-scheda-paziente-field", fieldName);
    }

    item.appendChild(label);
    item.appendChild(input);
    return item;
}

function createApoSchedaPazienteSelectItem(labelText, options, selectedValue, isWide, fieldName) {
    const item = document.createElement("label");
    item.className = "apo-scheda-paziente-item" + (isWide ? " apo-scheda-paziente-item-wide" : "");

    const label = document.createElement("span");
    label.className = "apo-scheda-paziente-label";
    label.textContent = labelText;

    const select = document.createElement("select");
    select.className = "form-select form-control apo-scheda-paziente-select";
    if (fieldName) {
        select.setAttribute("data-scheda-paziente-field", fieldName);
    }
    const normalizedSelected = normalizeApoValue(selectedValue);
    options.forEach(function (optionConfig) {
        const option = document.createElement("option");
        option.value = normalizeApoDisplayValue(optionConfig.value);
        option.textContent = normalizeApoDisplayValue(optionConfig.label);
        option.selected = normalizeApoValue(option.value) === normalizedSelected
            || normalizeApoValue(option.textContent) === normalizedSelected;
        select.appendChild(option);
    });

    item.appendChild(label);
    item.appendChild(select);
    return item;
}

function parseApoSchedaPazienteNumber(value) {
    const normalizedValue = normalizeApoDisplayValue(value).replace(",", ".");
    const parsed = Number.parseFloat(normalizedValue);
    return Number.isFinite(parsed) ? parsed : 0;
}

function formatApoSchedaPazienteMassIndex(value) {
    if (!Number.isFinite(value) || value <= 0) {
        return "";
    }
    return String(Math.round(value * 100) / 100).replace(".", ",");
}

function calculateApoSchedaPazienteMassIndex(pesoValue, altezzaValue) {
    const peso = parseApoSchedaPazienteNumber(pesoValue);
    let altezza = parseApoSchedaPazienteNumber(altezzaValue);
    if (altezza > 3) {
        altezza = altezza / 100;
    }
    if (peso <= 0 || altezza <= 0) {
        return "";
    }
    return formatApoSchedaPazienteMassIndex(peso / (altezza * altezza));
}

function createApoSchedaPazienteMassIndexItem(pesoValue, altezzaValue, massIndexValue) {
    const item = document.createElement("div");
    item.className = "apo-scheda-paziente-item apo-scheda-paziente-mass-item";

    const label = document.createElement("span");
    label.className = "apo-scheda-paziente-label";
    label.textContent = "Body Mass Index (IBM)";

    const grid = document.createElement("div");
    grid.className = "apo-scheda-paziente-mass-grid";

    const pesoInput = document.createElement("input");
    pesoInput.type = "text";
    pesoInput.inputMode = "decimal";
    pesoInput.className = "form-control apo-scheda-paziente-input";
    pesoInput.setAttribute("data-scheda-paziente-field", "ibmKg");
    pesoInput.placeholder = "";
    pesoInput.value = normalizeApoDisplayValue(pesoValue).replace(",", ".");
    const pesoBox = document.createElement("div");
    pesoBox.className = "apo-scheda-paziente-unit-box";
    const pesoUnit = document.createElement("span");
    pesoUnit.className = "apo-scheda-paziente-unit";
    pesoUnit.textContent = "kg";
    pesoBox.appendChild(pesoInput);
    pesoBox.appendChild(pesoUnit);

    const altezzaInput = document.createElement("input");
    altezzaInput.type = "text";
    altezzaInput.inputMode = "decimal";
    altezzaInput.className = "form-control apo-scheda-paziente-input";
    altezzaInput.setAttribute("data-scheda-paziente-field", "ibmCm");
    altezzaInput.placeholder = "";
    altezzaInput.value = normalizeApoDisplayValue(altezzaValue).replace(",", ".");

    const fractionSign = document.createElement("span");
    fractionSign.className = "apo-scheda-paziente-formula-sign";
    fractionSign.textContent = "/";

    const altezzaBox = document.createElement("div");
    altezzaBox.className = "apo-scheda-paziente-height-box apo-scheda-paziente-unit-box";
    const altezzaUnit = document.createElement("span");
    altezzaUnit.className = "apo-scheda-paziente-unit apo-scheda-paziente-height-unit";
    altezzaUnit.textContent = "cm";
    const exponent = document.createElement("sup");
    exponent.className = "apo-scheda-paziente-exponent";
    exponent.textContent = "2";
    altezzaBox.appendChild(altezzaInput);
    altezzaBox.appendChild(altezzaUnit);
    altezzaBox.appendChild(exponent);

    const equalsSign = document.createElement("span");
    equalsSign.className = "apo-scheda-paziente-formula-sign";
    equalsSign.textContent = "=";

    const resultInput = document.createElement("input");
    resultInput.type = "text";
    resultInput.className = "form-control apo-scheda-paziente-input apo-scheda-paziente-mass-result";
    resultInput.setAttribute("data-scheda-paziente-field", "ibm");
    resultInput.readOnly = true;

    function syncMassIndex() {
        resultInput.value = calculateApoSchedaPazienteMassIndex(pesoInput.value, altezzaInput.value)
            || normalizeApoDisplayValue(massIndexValue);
    }

    pesoInput.addEventListener("input", syncMassIndex);
    altezzaInput.addEventListener("input", syncMassIndex);
    syncMassIndex();

    grid.appendChild(pesoBox);
    grid.appendChild(fractionSign);
    grid.appendChild(altezzaBox);
    grid.appendChild(equalsSign);
    grid.appendChild(resultInput);
    item.appendChild(label);
    item.appendChild(grid);
    return item;
}

function createApoSchedaPazienteTableHeader(labels) {
    const thead = document.createElement("thead");
    const row = document.createElement("tr");
    labels.forEach(function (labelText) {
        const cell = document.createElement("th");
        cell.scope = "col";
        cell.textContent = labelText;
        row.appendChild(cell);
    });
    thead.appendChild(row);
    return thead;
}

function appendApoSchedaPazienteEmptyRow(tbody, colSpan, text) {
    const row = document.createElement("tr");
    row.className = "riepilogo-empty-row";
    const cell = document.createElement("td");
    cell.colSpan = colSpan;
    cell.textContent = text;
    row.appendChild(cell);
    tbody.appendChild(row);
}

function normalizeApoSchedaPazientePatologia(item, index) {
    const source = item && typeof item === "object" ? item : {};
    const codiceCategoria = normalizeApoDisplayValue(source.codiceCategoria || source.codCategoria || source.codice_categoria);
    const categoria = normalizeApoDisplayValue(source.categoria);
    const codice = normalizeApoDisplayValue(source.codice);
    const descrizione = normalizeApoDisplayValue(source.descrizione || source.patologia);
    const utente = getApoSchedaPazienteUtenteInsData(source);
    const idParts = [codiceCategoria, codice, descrizione].filter(function (part) {
        return !!part;
    });

    return {
        id: idParts.join("|") || ("PATOLOGIA_" + index),
        codiceCategoria: codiceCategoria,
        categoria: categoria,
        codice: codice,
        descrizione: descrizione,
        utenteInsCognome: utente.cognome,
        utenteInsNome: utente.nome,
        utenteInsDescrizione: utente.descrizione
    };
}

function normalizeApoSchedaPazientePatologie(items) {
    if (!Array.isArray(items)) {
        return [];
    }

    return items.map(function (item, index) {
        return normalizeApoSchedaPazientePatologia(item, index);
    });
}

function dedupeApoSchedaPazientePatologie(items) {
    const seen = {};
    return normalizeApoSchedaPazientePatologie(items).filter(function (itemData) {
        const key = normalizeApoValue(itemData.codice) || normalizeApoValue(itemData.id);
        if (!key || seen[key]) {
            return false;
        }
        seen[key] = true;
        return true;
    });
}

function getApoSchedaPazientePatologieFromSource(source) {
    if (!source || typeof source !== "object") {
        return [];
    }
    if (Array.isArray(source.patologieCroniche)) {
        return source.patologieCroniche;
    }
    if (Array.isArray(source.patologie)) {
        return source.patologie;
    }
    return [];
}

function isApoSchedaPazientePatologiaSelected(itemData) {
    return apoState.schedaPazientePatologieSelected.some(function (selected) {
        return selected.id === itemData.id;
    });
}

function renderApoSchedaPazientePatologieResults(tbody, results, onSelect, emptyText) {
    if (!tbody) {
        return;
    }

    tbody.innerHTML = "";
    if (!Array.isArray(results) || !results.length) {
        appendApoSchedaPazienteEmptyRow(tbody, 3, emptyText || "Nessuna patologia cronica da visualizzare.");
        return;
    }

    results.forEach(function (itemData) {
        const row = document.createElement("tr");
        row.className = "apo-scheda-paziente-patologia-row";
        row.tabIndex = 0;
        row.setAttribute("role", "button");
        row.classList.toggle("is-selected", isApoSchedaPazientePatologiaSelected(itemData));

        appendApoCell(row, itemData.codice);
        appendApoCell(row, itemData.descrizione);
        appendApoCell(row, itemData.categoria);

        function selectResult() {
            if (typeof onSelect === "function") {
                onSelect(itemData);
            }
        }

        row.addEventListener("dblclick", selectResult);
        row.addEventListener("keydown", function (event) {
            if (event.key === "Enter" || event.key === " ") {
                event.preventDefault();
                selectResult();
            }
        });
        tbody.appendChild(row);
    });
}

function renderApoSchedaPazientePatologieSelectedList(list, onRemove) {
    if (!list) {
        return;
    }

    list.innerHTML = "";
    if (!apoState.schedaPazientePatologieSelected.length) {
        const emptyItem = document.createElement("li");
        emptyItem.className = "apo-scheda-paziente-selected-empty";
        emptyItem.textContent = "Nessuna patologia selezionata.";
        list.appendChild(emptyItem);
        return;
    }

    apoState.schedaPazientePatologieSelected.forEach(function (itemData) {
        const listItem = document.createElement("li");
        const text = document.createElement("span");
        text.className = "apo-scheda-paziente-selected-text";
        const selectedDescription = [
            itemData.codice,
            itemData.descrizione,
            itemData.categoria ? "(" + itemData.categoria + ")" : ""
        ].filter(function (part) {
            return !!part;
        }).join(" - ");

        const selectedMain = document.createElement("span");
        selectedMain.textContent = selectedDescription;
        text.appendChild(selectedMain);
        appendApoSchedaPazienteUtenteIns(text, itemData, "apo-scheda-paziente-selected-utente");

        const removeButton = document.createElement("button");
        removeButton.type = "button";
        removeButton.className = "apo-scheda-paziente-selected-remove";
        removeButton.setAttribute("aria-label", "Rimuovi patologia selezionata");
        removeButton.textContent = "X";
        removeButton.addEventListener("click", function () {
            if (typeof onRemove === "function") {
                onRemove(itemData);
            }
        });

        listItem.appendChild(text);
        listItem.appendChild(removeButton);
        list.appendChild(listItem);
    });
}

async function loadApoSchedaPazientePatologieByFiltro(filterValue) {
    const filtro = normalizeApoDisplayValue(filterValue);
    const cacheKey = normalizeApoValue(filtro);
    if (!cacheKey) {
        return [];
    }

    apoState.schedaPazientePatologieLoading = true;
    apoState.schedaPazientePatologieError = "";
    try {
        const params = new URLSearchParams();
        params.set("filtro", filtro);
        const response = await fetch(apoPatologieCronicheIcd10Url + "?" + params.toString(), {
            headers: {
                "Accept": "application/json"
            }
        });

        if (response.status === 401) {
            window.location.href = apoLoginUrl;
            return [];
        }

        let payload = null;
        try {
            payload = await response.json();
        } catch (error) {
            payload = null;
        }

        if (!response.ok || !payload || payload.esito !== "ok") {
            throw new Error(payload && payload.message
                ? payload.message
                : "Errore nel caricamento delle patologie croniche. Status HTTP " + response.status + ".");
        }

        const results = normalizeApoSchedaPazientePatologie(payload.data && payload.data.patologie);
        apoState.schedaPazientePatologieSource = results;
        apoState.schedaPazientePatologieLoaded = true;
        return results;
    } catch (error) {
        apoState.schedaPazientePatologieLoaded = false;
        apoState.schedaPazientePatologieError = error && error.message ? error.message : "Errore nel caricamento delle patologie croniche.";
        setApoSearchMessage(apoState.schedaPazientePatologieError, "danger");
        return [];
    } finally {
        apoState.schedaPazientePatologieLoading = false;
    }
}

function filterApoSchedaPazientePatologie(filterValue) {
    const filtro = normalizeApoValue(filterValue);
    if (!filtro) {
        return [];
    }

    return apoState.schedaPazientePatologieSource.filter(function (itemData) {
        return normalizeApoValue(itemData.codice).indexOf(filtro) !== -1
            || normalizeApoValue(itemData.descrizione).indexOf(filtro) !== -1
            || normalizeApoValue(itemData.categoria).indexOf(filtro) !== -1
            || normalizeApoValue(itemData.codiceCategoria).indexOf(filtro) !== -1;
    });
}

function createApoSchedaPazientePatologieGrid(existingPatologie) {
    apoState.schedaPazientePatologieResults = [];
    apoState.schedaPazientePatologieSelected = dedupeApoSchedaPazientePatologie(existingPatologie);

    const item = document.createElement("div");
    item.className = "apo-scheda-paziente-item apo-scheda-paziente-item-wide";

    const label = document.createElement("span");
    label.className = "apo-scheda-paziente-label";
    label.textContent = "Patologie croniche (ICD-10-IM)";

    const filter = document.createElement("input");
    filter.type = "search";
    filter.className = "form-control apo-scheda-paziente-input apo-scheda-paziente-table-filter";
    filter.placeholder = "Cerca per codice, descrizione o categoria";
    filter.autocomplete = "off";

    const suggestionsList = document.createElement("div");
    suggestionsList.className = "apo-paziente-patologie-suggestions is-hidden";

    const searchButton = document.createElement("button");
    searchButton.type = "button";
    searchButton.className = "btn btn-success";
    searchButton.textContent = "Aggiungi";

    const field = document.createElement("div");
    field.className = "apo-scheda-paziente-patologie-field";
    field.appendChild(filter);
    field.appendChild(suggestionsList);

    const searchControls = document.createElement("div");
    searchControls.className = "apo-scheda-paziente-patologie-search";
    searchControls.appendChild(field);
    searchControls.appendChild(searchButton);

    const selectedList = document.createElement("ul");
    selectedList.className = "apo-scheda-paziente-selected-list";
    let suggestions = [];
    let selectedSuggestion = null;
    let searchTimer = null;

    function hideSuggestions() {
        suggestionsList.classList.add("is-hidden");
        suggestionsList.innerHTML = "";
    }

    function renderSuggestions(items, emptyText) {
        suggestionsList.innerHTML = "";
        if (!items.length) {
            const empty = document.createElement("div");
            empty.className = "apo-paziente-patologie-suggestion-empty";
            empty.textContent = emptyText || "Nessuna patologia trovata.";
            suggestionsList.appendChild(empty);
            suggestionsList.classList.remove("is-hidden");
            return;
        }

        items.slice(0, 8).forEach(function (itemData) {
            const button = document.createElement("button");
            button.type = "button";
            button.className = "apo-paziente-patologie-suggestion";
            button.textContent = formatApoSchedaPazientePatologiaSuggestion(itemData);
            button.addEventListener("mousedown", function (event) {
                event.preventDefault();
            });
            button.addEventListener("click", function () {
                addPatologia(itemData);
                filter.value = "";
                selectedSuggestion = null;
                suggestions = [];
                hideSuggestions();
                filter.focus();
            });
            suggestionsList.appendChild(button);
        });
        suggestionsList.classList.remove("is-hidden");
    }

    async function searchPatologie() {
        const filtro = normalizeApoDisplayValue(filter.value);
        const requestId = apoState.schedaPazientePatologieRequestId + 1;
        apoState.schedaPazientePatologieRequestId = requestId;
        selectedSuggestion = null;
        if (filtro.length < 2) {
            suggestions = [];
            hideSuggestions();
            return;
        }

        searchButton.disabled = true;
        searchButton.textContent = "Ricerca...";
        renderSuggestions([], "Ricerca in corso...");

        const serverResults = await loadApoSchedaPazientePatologieByFiltro(filtro);
        searchButton.disabled = false;
        searchButton.textContent = "Aggiungi";
        if (requestId !== apoState.schedaPazientePatologieRequestId) {
            return;
        }
        apoState.schedaPazientePatologieSource = serverResults;
        if (apoState.schedaPazientePatologieError && !serverResults.length) {
            suggestions = [];
            renderSuggestions([], "Errore nel caricamento delle patologie croniche.");
            return;
        }

        suggestions = filterApoSchedaPazientePatologie(filtro);
        renderSuggestions(suggestions, "Nessuna patologia cronica trovata.");
    }

    function addPatologia(itemData) {
        if (!isApoSchedaPazientePatologiaSelected(itemData)) {
            apoState.schedaPazientePatologieSelected.push(itemData);
        }
        renderApoSchedaPazientePatologieSelectedList(selectedList, removePatologia);
    }

    function removePatologia(itemData) {
        apoState.schedaPazientePatologieSelected = apoState.schedaPazientePatologieSelected.filter(function (selected) {
            return selected.id !== itemData.id;
        });
        renderApoSchedaPazientePatologieSelectedList(selectedList, removePatologia);
    }

    function addCurrentPatologia() {
        const itemData = selectedSuggestion || suggestions[0];
        if (!itemData) {
            setApoSearchMessage("Seleziona una patologia cronica da aggiungere.", "warning");
            filter.focus();
            return;
        }
        addPatologia(itemData);
        filter.value = "";
        selectedSuggestion = null;
        suggestions = [];
        hideSuggestions();
        filter.focus();
    }

    filter.addEventListener("input", function () {
        if (searchTimer) {
            window.clearTimeout(searchTimer);
        }
        searchTimer = window.setTimeout(function () {
            void searchPatologie();
        }, 250);
    });
    filter.addEventListener("focus", function () {
        if (suggestions.length) {
            renderSuggestions(suggestions);
        }
    });
    filter.addEventListener("blur", function () {
        window.setTimeout(hideSuggestions, 150);
    });
    filter.addEventListener("keydown", function (event) {
        if (event.key === "Enter") {
            event.preventDefault();
            selectedSuggestion = suggestions[0] || null;
            addCurrentPatologia();
        }
    });
    searchButton.addEventListener("click", function () {
        addCurrentPatologia();
    });

    renderApoSchedaPazientePatologieSelectedList(selectedList, removePatologia);

    item.appendChild(label);
    item.appendChild(searchControls);
    item.appendChild(selectedList);
    return item;
}

function dedupeApoSchedaPazienteAllergie(items) {
    const seen = {};
    return normalizeApoSchedaPazienteAllergie(items).filter(function (itemData) {
        const key = normalizeApoValue(itemData.codice) || normalizeApoValue(itemData.id);
        if (!key || seen[key]) {
            return false;
        }
        seen[key] = true;
        return true;
    });
}

function getApoSchedaPazienteAllergieFromSource(source) {
    if (!source || typeof source !== "object") {
        return [];
    }
    if (Array.isArray(source.allergieSelezionate)) {
        return source.allergieSelezionate;
    }
    if (Array.isArray(source.allergieScheda)) {
        return source.allergieScheda;
    }
    if (Array.isArray(source.allergie)) {
        return source.allergie;
    }
    return [];
}

function isApoSchedaPazienteAllergiaSelected(itemData) {
    return apoState.schedaPazienteAllergieSelected.some(function (selected) {
        return normalizeApoValue(selected.codice) === normalizeApoValue(itemData && itemData.codice);
    });
}

function renderApoSchedaPazienteAllergieSelectedList(list, onRemove) {
    if (!list) {
        return;
    }

    list.innerHTML = "";
    if (!apoState.schedaPazienteAllergieSelected.length) {
        const emptyItem = document.createElement("li");
        emptyItem.className = "apo-scheda-paziente-selected-empty";
        emptyItem.textContent = "Nessuna allergia selezionata.";
        list.appendChild(emptyItem);
        return;
    }

    apoState.schedaPazienteAllergieSelected.forEach(function (itemData) {
        const listItem = document.createElement("li");
        const text = document.createElement("span");
        text.className = "apo-scheda-paziente-selected-text";

        const selectedMain = document.createElement("span");
        selectedMain.textContent = formatApoSchedaPazienteAllergiaSuggestion(itemData);
        text.appendChild(selectedMain);
        appendApoSchedaPazienteUtenteIns(text, itemData, "apo-scheda-paziente-selected-utente");

        const removeButton = document.createElement("button");
        removeButton.type = "button";
        removeButton.className = "apo-scheda-paziente-selected-remove";
        removeButton.setAttribute("aria-label", "Rimuovi allergia selezionata");
        removeButton.textContent = "X";
        removeButton.addEventListener("click", function () {
            if (typeof onRemove === "function") {
                onRemove(itemData);
            }
        });

        listItem.appendChild(text);
        listItem.appendChild(removeButton);
        list.appendChild(listItem);
    });
}

async function loadApoSchedaPazienteAllergieByFiltro(filterValue) {
    const filtro = normalizeApoDisplayValue(filterValue);
    const cacheKey = normalizeApoValue(filtro);
    if (!cacheKey) {
        return [];
    }

    apoState.schedaPazienteAllergieLoading = true;
    apoState.schedaPazienteAllergieError = "";
    try {
        const params = new URLSearchParams();
        params.set("filtro", filtro);
        const response = await fetch(apoAllergieUrl + "?" + params.toString(), {
            headers: {
                "Accept": "application/json"
            }
        });

        if (response.status === 401) {
            window.location.href = apoLoginUrl;
            return [];
        }

        let payload = null;
        try {
            payload = await response.json();
        } catch (error) {
            payload = null;
        }

        if (!response.ok || !payload || payload.esito !== "ok") {
            throw new Error(payload && payload.message
                ? payload.message
                : "Errore nel caricamento delle allergie. Status HTTP " + response.status + ".");
        }

        const results = normalizeApoSchedaPazienteAllergie(payload.data && payload.data.allergie);
        apoState.schedaPazienteAllergieSource = results;
        apoState.schedaPazienteAllergieLoaded = true;
        return results;
    } catch (error) {
        apoState.schedaPazienteAllergieLoaded = false;
        apoState.schedaPazienteAllergieError = error && error.message ? error.message : "Errore nel caricamento delle allergie.";
        setApoSearchMessage(apoState.schedaPazienteAllergieError, "danger");
        return [];
    } finally {
        apoState.schedaPazienteAllergieLoading = false;
    }
}

function filterApoSchedaPazienteAllergie(filterValue) {
    const filtro = normalizeApoValue(filterValue);
    if (!filtro) {
        return [];
    }

    return apoState.schedaPazienteAllergieSource.filter(function (itemData) {
        return normalizeApoValue(itemData.codice).indexOf(filtro) !== -1
            || normalizeApoValue(itemData.descrizione).indexOf(filtro) !== -1;
    });
}

function createApoSchedaPazienteAllergieGrid(existingAllergie) {
    apoState.schedaPazienteAllergieResults = [];
    apoState.schedaPazienteAllergieSelected = dedupeApoSchedaPazienteAllergie(existingAllergie);

    const item = document.createElement("div");
    item.className = "apo-scheda-paziente-item apo-scheda-paziente-item-wide";

    const label = document.createElement("span");
    label.className = "apo-scheda-paziente-label";
    label.textContent = "Allergie";

    const filter = document.createElement("input");
    filter.type = "search";
    filter.className = "form-control apo-scheda-paziente-input apo-scheda-paziente-table-filter";
    filter.placeholder = "Cerca allergia";
    filter.autocomplete = "off";

    const suggestionsList = document.createElement("div");
    suggestionsList.className = "apo-paziente-patologie-suggestions is-hidden";

    const searchButton = document.createElement("button");
    searchButton.type = "button";
    searchButton.className = "btn btn-success";
    searchButton.textContent = "Aggiungi";

    const field = document.createElement("div");
    field.className = "apo-scheda-paziente-patologie-field";
    field.appendChild(filter);
    field.appendChild(suggestionsList);

    const searchControls = document.createElement("div");
    searchControls.className = "apo-scheda-paziente-patologie-search";
    searchControls.appendChild(field);
    searchControls.appendChild(searchButton);

    const selectedList = document.createElement("ul");
    selectedList.className = "apo-scheda-paziente-selected-list";
    let suggestions = [];
    let selectedSuggestion = null;
    let searchTimer = null;

    function hideSuggestions() {
        suggestionsList.classList.add("is-hidden");
        suggestionsList.innerHTML = "";
    }

    function renderSuggestions(items, emptyText) {
        suggestionsList.innerHTML = "";
        if (!items.length) {
            const empty = document.createElement("div");
            empty.className = "apo-paziente-patologie-suggestion-empty";
            empty.textContent = emptyText || "Nessuna allergia trovata.";
            suggestionsList.appendChild(empty);
            suggestionsList.classList.remove("is-hidden");
            return;
        }

        items.slice(0, 8).forEach(function (itemData) {
            const button = document.createElement("button");
            button.type = "button";
            button.className = "apo-paziente-patologie-suggestion";
            button.textContent = formatApoSchedaPazienteAllergiaSuggestion(itemData);
            button.addEventListener("mousedown", function (event) {
                event.preventDefault();
            });
            button.addEventListener("click", function () {
                addAllergia(itemData);
                filter.value = "";
                selectedSuggestion = null;
                suggestions = [];
                hideSuggestions();
                filter.focus();
            });
            suggestionsList.appendChild(button);
        });
        suggestionsList.classList.remove("is-hidden");
    }

    async function searchAllergie() {
        const filtro = normalizeApoDisplayValue(filter.value);
        const requestId = apoState.schedaPazienteAllergieRequestId + 1;
        apoState.schedaPazienteAllergieRequestId = requestId;
        selectedSuggestion = null;
        if (filtro.length < 2) {
            suggestions = [];
            hideSuggestions();
            return;
        }

        searchButton.disabled = true;
        searchButton.textContent = "Ricerca...";
        renderSuggestions([], "Ricerca in corso...");

        const serverResults = await loadApoSchedaPazienteAllergieByFiltro(filtro);
        searchButton.disabled = false;
        searchButton.textContent = "Aggiungi";
        if (requestId !== apoState.schedaPazienteAllergieRequestId) {
            return;
        }
        apoState.schedaPazienteAllergieSource = serverResults;
        if (apoState.schedaPazienteAllergieError && !serverResults.length) {
            suggestions = [];
            renderSuggestions([], "Errore nel caricamento delle allergie.");
            return;
        }

        suggestions = filterApoSchedaPazienteAllergie(filtro);
        renderSuggestions(suggestions, "Nessuna allergia trovata.");
    }

    function addAllergia(itemData) {
        if (!isApoSchedaPazienteAllergiaSelected(itemData)) {
            apoState.schedaPazienteAllergieSelected.push(itemData);
        }
        renderApoSchedaPazienteAllergieSelectedList(selectedList, removeAllergia);
    }

    function removeAllergia(itemData) {
        apoState.schedaPazienteAllergieSelected = apoState.schedaPazienteAllergieSelected.filter(function (selected) {
            return normalizeApoValue(selected.codice) !== normalizeApoValue(itemData && itemData.codice);
        });
        renderApoSchedaPazienteAllergieSelectedList(selectedList, removeAllergia);
    }

    function addCurrentAllergia() {
        const itemData = selectedSuggestion || suggestions[0];
        if (!itemData) {
            setApoSearchMessage("Seleziona un'allergia da aggiungere.", "warning");
            filter.focus();
            return;
        }
        addAllergia(itemData);
        filter.value = "";
        selectedSuggestion = null;
        suggestions = [];
        hideSuggestions();
        filter.focus();
    }

    filter.addEventListener("input", function () {
        if (searchTimer) {
            window.clearTimeout(searchTimer);
        }
        searchTimer = window.setTimeout(function () {
            void searchAllergie();
        }, 250);
    });
    filter.addEventListener("focus", function () {
        if (suggestions.length) {
            renderSuggestions(suggestions);
        }
    });
    filter.addEventListener("blur", function () {
        window.setTimeout(hideSuggestions, 150);
    });
    filter.addEventListener("keydown", function (event) {
        if (event.key === "Enter") {
            event.preventDefault();
            selectedSuggestion = suggestions[0] || null;
            addCurrentAllergia();
        }
    });
    searchButton.addEventListener("click", function () {
        addCurrentAllergia();
    });

    renderApoSchedaPazienteAllergieSelectedList(selectedList, removeAllergia);

    item.appendChild(label);
    item.appendChild(searchControls);
    item.appendChild(selectedList);
    return item;
}

function createApoSchedaPazienteTerapieGrid(existingTerapie, source) {
    apoState.schedaPazienteTerapieSelected = dedupeApoSchedaPazienteTerapie(existingTerapie);
    const codPaz = normalizeApoDisplayValue(getApoSchedaPazienteCodPaz(source));
    const persistImmediately = !!codPaz && !apoState.schedaPazientePendingAccessoPayload;

    const item = document.createElement("div");
    item.className = "apo-scheda-paziente-item apo-scheda-paziente-item-wide apo-scheda-paziente-terapie-item";

    const label = document.createElement("span");
    label.className = "apo-scheda-paziente-label";
    label.textContent = "Terapie in corso";

    const selectedList = document.createElement("ul");
    selectedList.className = "apo-scheda-paziente-selected-list";

    function renderList() {
        renderApoSchedaPazienteTerapieSelectedList(
            selectedList,
            apoState.schedaPazienteTerapieSelected,
            function (itemData) {
                apoState.schedaPazienteTerapieSelected = removeApoSchedaPazienteTerapiaFromList(
                    apoState.schedaPazienteTerapieSelected,
                    itemData
                );
                renderList();
            },
            "Nessuna terapia in corso selezionata."
        );
    }

    const controls = createApoSchedaPazienteTerapieAddControls(async function (terapia) {
        if (persistImmediately) {
            const saved = await saveApoSchedaPazienteHistoryTerapia({codPaz: codPaz}, terapia, {reload: false});
            if (!saved) {
                return false;
            }
        }
        addApoSchedaPazienteTerapiaToList(apoState.schedaPazienteTerapieSelected, terapia);
        renderList();
        return true;
    });

    renderList();
    item.appendChild(label);
    item.appendChild(controls);
    item.appendChild(selectedList);
    return item;
}

function getApoSchedaPazienteCaregiverData(source) {
    const caregiverText = getApoSchedaPazienteField(source, ["caregiver", "careGiver", "nomeCaregiver", "referente", "familiareRiferimento"]);
    let cognome = getApoSchedaPazienteField(source, ["caregiverCognome", "cognomeCaregiver", "referenteCognome", "cognomeReferente"]);
    let nome = getApoSchedaPazienteField(source, ["caregiverNome", "nomeCaregiver", "referenteNome", "nomeReferente"]);
    let codiceFiscale = getApoSchedaPazienteField(source, ["caregiverCodFiscale", "caregiverCodiceFiscale", "codFiscaleCaregiver", "codiceFiscaleCaregiver", "cfCaregiver"]);

    if (!codiceFiscale && caregiverText && /^[A-Z0-9]{11,16}$/.test(normalizeApoValue(caregiverText))) {
        codiceFiscale = caregiverText;
    }

    if (!codiceFiscale && (!cognome || !nome) && caregiverText) {
        const parts = caregiverText.split(/\s+/).filter(function (part) {
            return !!part;
        });
        if (!cognome && parts.length) {
            cognome = parts[0];
        }
        if (!nome && parts.length > 1) {
            nome = parts.slice(1).join(" ");
        }
    }

    return {
        cognome: cognome,
        nome: nome,
        codiceFiscale: codiceFiscale
    };
}

function selectApoSchedaPazienteCaregiver(cognomeInput, nomeInput, codiceFiscaleInput, resultsBody, selected) {
    if (!selected) {
        return;
    }
    if (cognomeInput) {
        cognomeInput.value = selected.cognome || "";
    }
    if (nomeInput) {
        nomeInput.value = selected.nome || "";
    }
    if (codiceFiscaleInput) {
        codiceFiscaleInput.value = selected.codFiscale || "";
    }
    apoState.schedaPazienteCaregiverResults = [];
    renderApoSchedaPazienteCaregiverResults(resultsBody, [], null, "Caregiver selezionato.");
}

function renderApoSchedaPazienteCaregiverResults(tbody, results, onSelect, emptyText) {
    if (!tbody) {
        return;
    }

    tbody.innerHTML = "";
    if (!Array.isArray(results) || !results.length) {
        appendApoSchedaPazienteEmptyRow(tbody, 4, emptyText || "Nessun caregiver da visualizzare.");
        return;
    }

    results.forEach(function (itemData) {
        const row = document.createElement("tr");
        row.className = "apo-scheda-paziente-caregiver-row";
        row.tabIndex = 0;
        row.setAttribute("role", "button");
        appendApoCell(row, itemData.codFiscale);
        appendApoCell(row, itemData.cognome);
        appendApoCell(row, itemData.nome);
        appendApoCell(row, itemData.dataNascita);

        function selectResult() {
            if (typeof onSelect === "function") {
                onSelect(itemData);
            }
        }

        row.addEventListener("click", selectResult);
        row.addEventListener("keydown", function (event) {
            if (event.key === "Enter" || event.key === " ") {
                event.preventDefault();
                selectResult();
            }
        });
        tbody.appendChild(row);
    });
}

async function searchApoSchedaPazienteCaregiver(cognomeInput, nomeInput, codiceFiscaleInput, resultsBody, searchButton, options) {
    const config = options && typeof options === "object" ? options : {};
    const codiceFiscale = normalizeApoDisplayValue(codiceFiscaleInput ? codiceFiscaleInput.value : "");
    const cognome = normalizeApoDisplayValue(cognomeInput ? cognomeInput.value : "");
    const nome = normalizeApoDisplayValue(nomeInput ? nomeInput.value : "");
    if (!codiceFiscale && !cognome && !nome) {
        apoState.schedaPazienteCaregiverResults = [];
        renderApoSchedaPazienteCaregiverResults(resultsBody, [], null, "Valorizza almeno un filtro di ricerca.");
        setApoSearchMessage("Valorizza almeno un filtro di ricerca per il caregiver.", "warning");
        return;
    }

    const params = new URLSearchParams();
    if (codiceFiscale) {
        params.set("codiceFiscale", codiceFiscale);
    }
    if (cognome) {
        params.set("cognome", cognome);
    }
    if (nome) {
        params.set("nome", nome);
    }

    if (searchButton) {
        searchButton.disabled = true;
        searchButton.textContent = "Ricerca...";
    }
    apoState.schedaPazienteCaregiverResults = [];
    renderApoSchedaPazienteCaregiverResults(resultsBody, [], null, "Ricerca in corso...");

    try {
        const response = await fetch(apoAnagrafeSearchUrl + "?" + params.toString(), {
            headers: {
                "Accept": "application/json"
            }
        });

        if (response.status === 401) {
            window.location.href = apoLoginUrl;
            return;
        }

        let payload = null;
        try {
            payload = await response.json();
        } catch (error) {
            payload = null;
        }

        if (!response.ok || !payload || payload.esito !== "ok") {
            throw new Error(payload && payload.message ? payload.message : "Errore nella ricerca anagrafica.");
        }

        apoState.schedaPazienteCaregiverResults = normalizeApoAnagrafePazienti(payload.data && payload.data.pazienti);
        if (config.autoSelectFirst && apoState.schedaPazienteCaregiverResults.length) {
            selectApoSchedaPazienteCaregiver(
                cognomeInput,
                nomeInput,
                codiceFiscaleInput,
                resultsBody,
                apoState.schedaPazienteCaregiverResults[0]
            );
            return;
        }
        renderApoSchedaPazienteCaregiverResults(resultsBody, apoState.schedaPazienteCaregiverResults, function (selected) {
            selectApoSchedaPazienteCaregiver(cognomeInput, nomeInput, codiceFiscaleInput, resultsBody, selected);
        }, "Nessun caregiver trovato.");
    } catch (error) {
        apoState.schedaPazienteCaregiverResults = [];
        renderApoSchedaPazienteCaregiverResults(resultsBody, [], null, "Errore nella ricerca caregiver.");
        setApoSearchMessage(error && error.message ? error.message : "Errore nella ricerca caregiver.", "danger");
    } finally {
        if (searchButton) {
            searchButton.disabled = false;
            searchButton.textContent = "Cerca";
        }
    }
}

function createApoSchedaPazienteCaregiverItem(source) {
    const caregiver = getApoSchedaPazienteCaregiverData(source);
    const item = document.createElement("div");
    item.className = "apo-scheda-paziente-item apo-scheda-paziente-item-wide apo-scheda-paziente-caregiver-item";

    const label = document.createElement("span");
    label.className = "apo-scheda-paziente-label";
    label.textContent = "Caregiver";

    const controls = document.createElement("div");
    controls.className = "apo-scheda-paziente-caregiver-grid";

    const cognomeInput = document.createElement("input");
    cognomeInput.type = "text";
    cognomeInput.className = "form-control apo-scheda-paziente-input";
    cognomeInput.placeholder = "Cognome";
    cognomeInput.value = normalizeApoDisplayValue(caregiver.cognome);

    const nomeInput = document.createElement("input");
    nomeInput.type = "text";
    nomeInput.className = "form-control apo-scheda-paziente-input";
    nomeInput.placeholder = "Nome";
    nomeInput.value = normalizeApoDisplayValue(caregiver.nome);

    const codiceFiscaleInput = document.createElement("input");
    codiceFiscaleInput.type = "text";
    codiceFiscaleInput.className = "form-control apo-scheda-paziente-input";
    codiceFiscaleInput.setAttribute("data-scheda-paziente-field", "caregiver");
    codiceFiscaleInput.placeholder = "Cod fiscale";
    codiceFiscaleInput.value = normalizeApoDisplayValue(caregiver.codiceFiscale);

    const clearButton = document.createElement("button");
    clearButton.type = "button";
    clearButton.className = "btn btn-outline-secondary";
    clearButton.textContent = "Pulisci";

    const searchButton = document.createElement("button");
    searchButton.type = "button";
    searchButton.className = "btn btn-success";
    searchButton.textContent = "Cerca";

    const tableWrap = document.createElement("div");
    tableWrap.className = "apo-scheda-paziente-table-wrap apo-scheda-paziente-caregiver-results";

    const table = document.createElement("table");
    table.className = "table table-sm apo-scheda-paziente-table";
    table.appendChild(createApoSchedaPazienteTableHeader(["Cod fiscale", "Cognome", "Nome", "Data nascita"]));

    const tbody = document.createElement("tbody");
    table.appendChild(tbody);
    tableWrap.appendChild(table);

    function clearCaregiver() {
        cognomeInput.value = "";
        nomeInput.value = "";
        codiceFiscaleInput.value = "";
        apoState.schedaPazienteCaregiverResults = [];
        renderApoSchedaPazienteCaregiverResults(tbody, [], null, "Nessun caregiver da visualizzare.");
        cognomeInput.focus();
    }

    function searchCaregiver() {
        void searchApoSchedaPazienteCaregiver(cognomeInput, nomeInput, codiceFiscaleInput, tbody, searchButton);
    }

    [cognomeInput, nomeInput, codiceFiscaleInput].forEach(function (input) {
        input.addEventListener("keydown", function (event) {
            if (event.key === "Enter") {
                event.preventDefault();
                searchCaregiver();
            }
        });
    });
    clearButton.addEventListener("click", clearCaregiver);
    searchButton.addEventListener("click", searchCaregiver);

    renderApoSchedaPazienteCaregiverResults(tbody, [], null, "Nessun caregiver da visualizzare.");
    if (normalizeApoDisplayValue(caregiver.codiceFiscale)) {
        window.setTimeout(function () {
            void searchApoSchedaPazienteCaregiver(
                cognomeInput,
                nomeInput,
                codiceFiscaleInput,
                tbody,
                searchButton,
                {autoSelectFirst: true}
            );
        }, 0);
    }

    controls.appendChild(cognomeInput);
    controls.appendChild(nomeInput);
    controls.appendChild(codiceFiscaleInput);
    controls.appendChild(clearButton);
    controls.appendChild(searchButton);

    item.appendChild(label);
    item.appendChild(controls);
    item.appendChild(tableWrap);
    return item;
}

function renderApoSchedaPaziente(patient) {
    const body = document.getElementById("apo-scheda-paziente-body");
    if (!body) {
        return;
    }

    const source = patient && typeof patient === "object" ? patient : {};
    const cognome = getApoSchedaPazienteField(source, ["cognome", "surname", "sn"]);
    const nome = getApoSchedaPazienteField(source, ["nome", "givenName"]);
    const paziente = normalizeApoDisplayValue([cognome, nome].filter(function (part) {
        return !!part;
    }).join(" ")) || getApoSchedaPazienteField(source, ["paziente"]);
    const eta = getApoSchedaPazienteField(source, ["eta", "etaAnni"]);
    const dataNascita = formatApoAnagrafeDate(getApoSchedaPazienteField(source, ["dataNascita", "dataNas", "dtNascita"]), eta);
    const fumo = formatApoSchedaPazienteFumo(getApoSchedaPazienteField(source, ["fumo", "fumatore", "flagFumo", "flgFumo"]));
    const sesso = formatApoSchedaPazienteSesso(getApoSchedaPazienteField(source, ["sesso"]));
    const statoCivile = getApoSchedaPazienteStatoCivileValue(getApoSchedaPazienteField(source, ["statoCivile", "desStatoCivile", "statoCivil"]));

    body.innerHTML = "";
    const datiAnagrafici = createApoSchedaPazienteSection("Dati anagrafici");
    datiAnagrafici.section.classList.add("apo-scheda-paziente-anagrafici-section");
    datiAnagrafici.grid.appendChild(createApoSchedaPazienteItem("Paziente", paziente, true));
    datiAnagrafici.grid.appendChild(createApoSchedaPazienteItem("ETA'", formatApoSchedaPazienteEta(eta), false));
    datiAnagrafici.grid.appendChild(createApoSchedaPazienteItem("Sesso", sesso, false));
    datiAnagrafici.grid.appendChild(createApoSchedaPazienteItem("Codice fiscale", getApoSchedaPazienteField(source, ["codFiscale", "codiceFiscale", "cf"]), false));
    datiAnagrafici.grid.appendChild(createApoSchedaPazienteItem("Data nascita", dataNascita, false));
    datiAnagrafici.grid.appendChild(createApoSchedaPazienteInputItem("Telefono", getApoSchedaPazienteField(source, ["telefono", "tel", "telefonoResidenza", "telRes", "telefonoDomicilio", "telDom", "cellulare"]), false, "telefono"));
    datiAnagrafici.grid.appendChild(createApoSchedaPazienteSelectItem("Stato civile", apoSchedaPazienteStatoCivileOptions, statoCivile, false, "statoCivile"));
    body.appendChild(datiAnagrafici.section);
    body.appendChild(createApoSchedaPazienteCaregiverItem(source));

    const fattoriRischio = createApoSchedaPazienteSection("Fattori di rischio");
    fattoriRischio.section.classList.add("apo-scheda-paziente-rischio-section");
    fattoriRischio.grid.appendChild(createApoSchedaPazienteSelectItem("Fumo", [
        {value: "1", label: "SI"},
        {value: "0", label: "NO"}
    ], fumo, false, "fumo"));
    fattoriRischio.grid.appendChild(createApoSchedaPazienteMassIndexItem(
        getApoSchedaPazienteField(source, ["peso", "pesoKg"]),
        getApoSchedaPazienteField(source, ["altezza", "altezzaCm", "altezzaMt"]),
        getApoSchedaPazienteField(source, ["massIndex", "bodyMassIndex", "bmi", "indiceMassaCorporea", "imc"])
    ));
    fattoriRischio.grid.appendChild(createApoSchedaPazientePatologieGrid(getApoSchedaPazientePatologieFromSource(source)));
    fattoriRischio.grid.appendChild(createApoSchedaPazienteAllergieGrid(getApoSchedaPazienteAllergieFromSource(source)));
    fattoriRischio.grid.appendChild(createApoSchedaPazienteTerapieGrid(getApoSchedaPazienteTerapieFromSource(source), source));
    body.appendChild(fattoriRischio.section);

    body.appendChild(createApoSchedaPazienteDiarioItem(source));
}

function setApoSchedaPazienteSaving(isSaving) {
    const modal = document.getElementById("apo-scheda-paziente-modal");
    const saveButton = document.getElementById("apo-scheda-paziente-ok");
    const saving = isSaving === true;

    apoState.schedaPazienteSaving = saving;
    if (saveButton) {
        saveButton.disabled = saving;
        saveButton.textContent = saving ? "Salvataggio..." : "Salva scheda";
    }
    if (modal) {
        modal.querySelectorAll("input, select, textarea, button").forEach(function (field) {
            field.disabled = saving;
        });
    }
}

function getApoSchedaPazienteInputValue(fieldName) {
    const modal = document.getElementById("apo-scheda-paziente-modal");
    const field = modal ? modal.querySelector("[data-scheda-paziente-field=\"" + fieldName + "\"]") : null;
    return normalizeApoDisplayValue(field ? field.value : "");
}

function collectApoSchedaPazientePatologieCodes() {
    const codes = [];
    apoState.schedaPazientePatologieSelected.forEach(function (itemData) {
        const codice = normalizeApoDisplayValue(itemData && itemData.codice);
        if (codice && codes.indexOf(codice) === -1) {
            codes.push(codice);
        }
    });
    return codes.join("|");
}

function collectApoSchedaPazienteAllergieCodes() {
    const codes = [];
    apoState.schedaPazienteAllergieSelected.forEach(function (itemData) {
        const codice = normalizeApoDisplayValue(itemData && itemData.codice);
        if (codice && codes.indexOf(codice) === -1) {
            codes.push(codice);
        }
    });
    return codes.join("|");
}

function collectApoSchedaPazienteAllergieText() {
    return apoState.schedaPazienteAllergieSelected.map(function (itemData) {
        return formatApoSchedaPazienteAllergiaSuggestion(itemData);
    }).filter(function (description) {
        return !!description;
    }).join("; ");
}

function collectApoSchedaPazienteTerapieText() {
    return apoState.schedaPazienteTerapieSelected.map(function (itemData) {
        return formatApoSchedaPazienteTerapia(itemData);
    }).filter(function (description) {
        return !!description;
    }).join("; ");
}

function collectApoSchedaPazienteTerapieJson() {
    return JSON.stringify(apoState.schedaPazienteTerapieSelected.map(function (itemData) {
        const terapia = normalizeApoSchedaPazienteTerapia(itemData, 0);
        return {
            principioAttivo: terapia.principioAttivo,
            farmaco: terapia.farmaco,
            confezione: terapia.confezione,
            quantita: terapia.quantita,
            frequenzaUnita: terapia.frequenzaUnita,
            durataValore: terapia.durataValore,
            durataUnita: terapia.durataUnita
        };
    }));
}

function collectApoSchedaPazientePayload() {
    return {
        schedaPaziente: "1",
        schedaTelefono: getApoSchedaPazienteInputValue("telefono"),
        schedaStatoCivile: getApoSchedaPazienteInputValue("statoCivile"),
        schedaFumo: getApoSchedaPazienteInputValue("fumo"),
        schedaIbm: getApoSchedaPazienteInputValue("ibm"),
        schedaIbmKg: getApoSchedaPazienteInputValue("ibmKg"),
        schedaIbmCm: getApoSchedaPazienteInputValue("ibmCm"),
        schedaAllergie: collectApoSchedaPazienteAllergieText(),
        schedaAllergieCodes: collectApoSchedaPazienteAllergieCodes(),
        schedaTerapie: collectApoSchedaPazienteTerapieText(),
        schedaTerapieJson: collectApoSchedaPazienteTerapieJson(),
        schedaCaregiver: getApoSchedaPazienteInputValue("caregiver"),
        schedaDiario: getApoSchedaPazienteInputValue("diario"),
        schedaPatologie: collectApoSchedaPazientePatologieCodes()
    };
}

function getApoSchedaPazienteCodPaz(source) {
    return getApoSchedaPazienteField(source, ["codPaz", "codPaziente", "cod_paz", "codicePaziente"]);
}

function mergeApoSchedaPazienteExistingData(patient, scheda) {
    const merged = Object.assign({}, patient || {});
    Object.keys(scheda || {}).forEach(function (key) {
        const value = scheda[key];
        if (Array.isArray(value)) {
            merged[key] = value;
            return;
        }
        if (normalizeApoDisplayValue(value)) {
            merged[key] = value;
        }
    });
    return merged;
}

async function loadApoSchedaPazienteExisting(codPaz) {
    const normalizedCodPaz = normalizeApoDisplayValue(codPaz);
    if (!normalizedCodPaz) {
        return null;
    }

    const params = new URLSearchParams();
    params.set("codPaz", normalizedCodPaz);
    const response = await fetch(apoSchedaPazienteUrl + "?" + params.toString(), {
        headers: {
            "Accept": "application/json"
        }
    });

    if (response.status === 401) {
        window.location.href = apoLoginUrl;
        return null;
    }

    let payload = null;
    try {
        payload = await response.json();
    } catch (error) {
        payload = null;
    }

    if (!response.ok || !payload || payload.esito !== "ok") {
        throw new Error(payload && payload.message ? payload.message : "Errore nel caricamento della scheda paziente.");
    }

    if (!payload.data || payload.data.exists !== true) {
        return null;
    }
    return payload.data.scheda || null;
}

async function hydrateApoSchedaPazienteModal(patient, requestId) {
    const codPaz = getApoSchedaPazienteCodPaz(patient);
    if (!codPaz) {
        return;
    }

    try {
        const scheda = await loadApoSchedaPazienteExisting(codPaz);
        const modal = document.getElementById("apo-scheda-paziente-modal");
        if (apoState.schedaPazienteExistingRequestId !== requestId
                || !modal
                || modal.classList.contains("is-hidden")
                || !scheda) {
            return;
        }

        renderApoSchedaPaziente(mergeApoSchedaPazienteExistingData(patient, scheda));
        setApoSchedaPazienteSaving(false);
    } catch (error) {
        if (apoState.schedaPazienteExistingRequestId === requestId) {
            setApoSearchMessage(error && error.message ? error.message : "Errore nel caricamento della scheda paziente.", "danger");
        }
    }
}

function openApoSchedaPazienteModal(patient) {
    const modal = document.getElementById("apo-scheda-paziente-modal");
    const okButton = document.getElementById("apo-scheda-paziente-ok");
    const backButton = document.getElementById("apo-scheda-paziente-back");
    if (!modal) {
        return;
    }

    const requestId = apoState.schedaPazienteExistingRequestId + 1;
    apoState.schedaPazienteExistingRequestId = requestId;
    apoState.schedaPazienteLastFocus = document.activeElement;
    renderApoSchedaPaziente(patient);
    setApoSchedaPazienteSaving(false);
    if (backButton) {
        backButton.classList.toggle("is-hidden", !apoState.schedaPazientePendingAccessoPayload);
    }
    modal.classList.remove("is-hidden");
    modal.setAttribute("aria-hidden", "false");
    void hydrateApoSchedaPazienteModal(patient, requestId);
    if (okButton) {
        okButton.focus();
    }
}

function openApoSelectedSchedaPazienteModal() {
    const accesso = getApoSelectedAccesso();
    if (!accesso || !normalizeApoDisplayValue(accesso.codPaz)) {
        setApoSearchMessage("Seleziona un paziente per aprire la scheda.", "warning");
        return;
    }

    apoState.schedaPazientePendingAccessoPayload = null;
    apoState.schedaPazientePendingPatient = null;
    openApoSchedaPazienteModal(accesso);
}

function closeApoSchedaPazienteModal(restoreFocus) {
    const modal = document.getElementById("apo-scheda-paziente-modal");
    if (modal) {
        modal.classList.add("is-hidden");
        modal.setAttribute("aria-hidden", "true");
    }
    renderApoSchedaPaziente(null);
    apoState.schedaPazienteExistingRequestId += 1;

    if (restoreFocus) {
        let focusTarget = apoState.schedaPazienteLastFocus;
        const fallbackFocus = document.getElementById("apo-new-accesso-button");
        if (focusTarget && typeof focusTarget.closest === "function" && focusTarget.closest(".is-hidden")) {
            focusTarget = fallbackFocus;
        }
        if (!focusTarget) {
            focusTarget = fallbackFocus;
        }
        if (focusTarget && typeof focusTarget.focus === "function") {
            focusTarget.focus();
        }
    }
    apoState.schedaPazienteLastFocus = null;
}

function backToApoSchedaPazientePreviousModal() {
    const pendingPayload = apoState.schedaPazientePendingAccessoPayload;
    const pendingPatient = apoState.schedaPazientePendingPatient;
    const detailModal = document.getElementById("apo-new-accesso-detail-modal");
    const dataOra = document.getElementById("apo-new-accesso-data-ora");
    const diagnosi = document.getElementById("apo-new-accesso-diagnosi");
    const confirmButton = document.getElementById("apo-new-accesso-confirm-add");

    closeApoSchedaPazienteModal(false);
    if (!pendingPayload || !pendingPatient || !detailModal) {
        return;
    }

    setApoNuovoAccessoDetailBusy(false);
    setApoNuovoAccessoDetailMessage("", "info");
    renderApoNuovoAccessoSelectedPatientSummary(pendingPatient);
    if (dataOra) {
        dataOra.value = normalizeApoDisplayValue(pendingPayload.dataOra);
    }
    if (diagnosi) {
        diagnosi.value = normalizeApoDisplayValue(pendingPayload.diagnosi);
    }
    toggleApoNuovoAccessoTipo(pendingPayload.tipoAccesso);
    toggleApoNuovoAccessoPatologia(pendingPayload.patologia);

    detailModal.classList.remove("is-hidden");
    detailModal.setAttribute("aria-hidden", "false");
    if (confirmButton) {
        confirmButton.focus();
    }
}

function backToApoNuovoAccessoSearchModal() {
    const detailModal = document.getElementById("apo-new-accesso-detail-modal");
    const modal = document.getElementById("apo-new-accesso-modal");
    const selectButton = document.getElementById("apo-new-accesso-select-patient");
    const codiceFiscale = document.getElementById("apo-new-accesso-cf");
    if (detailModal) {
        detailModal.classList.add("is-hidden");
        detailModal.setAttribute("aria-hidden", "true");
    }
    if (!modal) {
        return;
    }

    apoState.nuovoAccessoLastFocus = document.getElementById("apo-new-accesso-button");
    apoState.nuovoAccessoDetailLastFocus = null;
    renderApoNuovoAccessoResults();
    updateApoNuovoAccessoSelectButton();

    modal.classList.remove("is-hidden");
    modal.setAttribute("aria-hidden", "false");
    if (selectButton && !selectButton.disabled) {
        selectButton.focus();
    } else if (codiceFiscale) {
        codiceFiscale.focus();
    }
}

function handleApoNuovoAccessoDetailSubmit(event) {
    if (event) {
        event.preventDefault();
    }

    const selected = getApoNuovoAccessoSelectedPaziente();
    const dataOra = document.getElementById("apo-new-accesso-data-ora");
    const diagnosi = document.getElementById("apo-new-accesso-diagnosi");
    const tipoConfig = getApoNuovoAccessoTipoConfig(getApoNuovoAccessoSelectedTipo());
    const patologiaConfig = getApoNuovoAccessoPatologiaConfig(apoState.nuovoAccessoPatologia);
    const dataOraValue = normalizeApoDisplayValue(dataOra ? dataOra.value : "");

    if (!selected) {
        setApoNuovoAccessoDetailMessage("Seleziona un paziente.", "warning");
        return;
    }

    if (!dataOraValue) {
        setApoNuovoAccessoDetailMessage("Valorizza data e ora dell'accesso.", "warning");
        if (dataOra) {
            dataOra.focus();
        }
        return;
    }

    const accessoPayload = {
        codFiscale: selected.codFiscale,
        cognome: selected.cognome,
        nome: selected.nome,
        sesso: selected.sesso,
        pin: selected.pin,
        codPaz: selected.codPaz,
        dataOra: dataOraValue,
        diagnosi: normalizeApoDisplayValue(diagnosi ? diagnosi.value : ""),
        tipoAccesso: tipoConfig.id,
        tipoAccessoLabel: tipoConfig.label,
        tipoAccessoValue: tipoConfig.code,
        patologia: patologiaConfig.label,
        codComuneRes: selected.codComuneRes
    };

    apoState.schedaPazientePendingAccessoPayload = accessoPayload;
    apoState.schedaPazientePendingPatient = selected;
    closeApoNuovoAccessoDetailModal(false);
    openApoSchedaPazienteModal(selected);
}

async function createApoNuovoAccesso(payload, selectedPatient) {
    const params = new URLSearchParams();
    Object.keys(payload || {}).forEach(function (key) {
        params.set(key, normalizeApoDisplayValue(payload[key]));
    });

    try {
        const response = await fetch(apoNewAccessoUrl, {
            method: "POST",
            headers: {
                "Accept": "application/json",
                "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8"
            },
            body: params.toString()
        });

        if (response.status === 401) {
            window.location.href = apoLoginUrl;
            return;
        }

        let payloadResponse = null;
        try {
            payloadResponse = await response.json();
        } catch (error) {
            payloadResponse = null;
        }

        if (!response.ok || !payloadResponse || payloadResponse.esito !== "ok") {
            throw new Error(payloadResponse && payloadResponse.message ? payloadResponse.message : "Errore nell'inserimento del nuovo accesso.");
        }

        await searchApoAccessi();
        setApoSearchMessage(payloadResponse.message || "Nuovo accesso inserito correttamente.", "success");
        return true;
    } catch (error) {
        setApoSearchMessage(error && error.message ? error.message : "Errore nell'inserimento del nuovo accesso.", "danger");
        return false;
    }
}

async function saveApoSchedaPaziente() {
    if (apoState.schedaPazienteSaving) {
        return;
    }

    const pendingPayload = apoState.schedaPazientePendingAccessoPayload;
    if (!pendingPayload) {
        await saveApoSchedaPazienteForSelectedPatient();
        return;
    }

    setApoSchedaPazienteSaving(true);
    const saved = await createApoNuovoAccesso(
        Object.assign({}, pendingPayload, collectApoSchedaPazientePayload()),
        apoState.schedaPazientePendingPatient
    );
    setApoSchedaPazienteSaving(false);
    if (!saved) {
        return;
    }

    apoState.schedaPazientePendingAccessoPayload = null;
    apoState.schedaPazientePendingPatient = null;
    closeApoSchedaPazienteModal(true);
}

async function saveApoSchedaPazienteForSelectedPatient() {
    const accesso = getApoSelectedAccesso();
    const codPaz = normalizeApoDisplayValue(accesso && accesso.codPaz);
    if (!codPaz) {
        setApoSearchMessage("Codice paziente non disponibile.", "warning");
        return;
    }

    const params = new URLSearchParams();
    params.set("codPaz", codPaz);
    const payload = collectApoSchedaPazientePayload();
    Object.keys(payload).forEach(function (key) {
        params.set(key, normalizeApoDisplayValue(payload[key]));
    });

    setApoSchedaPazienteSaving(true);
    try {
        const response = await fetch(apoSchedaPazienteUrl, {
            method: "POST",
            headers: {
                "Accept": "application/json",
                "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8"
            },
            body: params.toString()
        });

        if (response.status === 401) {
            window.location.href = apoLoginUrl;
            return;
        }

        let responsePayload = null;
        try {
            responsePayload = await response.json();
        } catch (error) {
            responsePayload = null;
        }

        if (!response.ok || !responsePayload || responsePayload.esito !== "ok") {
            throw new Error(responsePayload && responsePayload.message ? responsePayload.message : "Errore nel salvataggio della scheda paziente.");
        }

        clearApoSchedaPazienteDiario();
        clearApoSchedaPazienteHistoryPatologie();
        clearApoSchedaPazienteHistoryAllergie();
        clearApoSchedaPazienteHistoryTerapie();
        closeApoSchedaPazienteModal(true);
        setApoSearchMessage(responsePayload.message || "Scheda paziente salvata correttamente.", "success");
        renderApoSchedaPazienteHistorySection();
    } catch (error) {
        setApoSearchMessage(error && error.message ? error.message : "Errore nel salvataggio della scheda paziente.", "danger");
    } finally {
        setApoSchedaPazienteSaving(false);
    }
}

async function searchApoNuovoAccessoPazienti() {
    const codiceFiscale = getApoNuovoAccessoFilterValue("apo-new-accesso-cf");
    const cognome = getApoNuovoAccessoFilterValue("apo-new-accesso-cognome");
    const nome = getApoNuovoAccessoFilterValue("apo-new-accesso-nome");
    if (!codiceFiscale && !cognome && !nome) {
        apoState.nuovoAccessoResults = [];
        apoState.nuovoAccessoSelectedId = "";
        renderApoNuovoAccessoResults();
        updateApoNuovoAccessoSelectButton();
        setApoNuovoAccessoMessage("Valorizza almeno un filtro di ricerca.", "warning");
        return;
    }

    const params = new URLSearchParams();
    if (codiceFiscale) {
        params.set("codiceFiscale", codiceFiscale);
    }
    if (cognome) {
        params.set("cognome", cognome);
    }
    if (nome) {
        params.set("nome", nome);
    }

    setApoNuovoAccessoBusy(true);
    setApoNuovoAccessoMessage("", "info");
    apoState.nuovoAccessoResults = [];
    apoState.nuovoAccessoSelectedId = "";
    renderApoNuovoAccessoResults();
    updateApoNuovoAccessoSelectButton();

    try {
        const response = await fetch(apoAnagrafeSearchUrl + "?" + params.toString(), {
            headers: {
                "Accept": "application/json"
            }
        });

        if (response.status === 401) {
            window.location.href = apoLoginUrl;
            return;
        }

        let payload = null;
        try {
            payload = await response.json();
        } catch (error) {
            payload = null;
        }

        if (!response.ok || !payload || payload.esito !== "ok") {
            throw new Error(payload && payload.message ? payload.message : "Errore nella ricerca anagrafica.");
        }

        apoState.nuovoAccessoResults = normalizeApoAnagrafePazienti(payload.data && payload.data.pazienti);
        apoState.nuovoAccessoSelectedId = "";
        if (!apoState.nuovoAccessoResults.length) {
            setApoNuovoAccessoNoPazienteMessage();
        }
    } catch (error) {
        apoState.nuovoAccessoResults = [];
        apoState.nuovoAccessoSelectedId = "";
        apoState.nuovoAccessoError = error && error.message ? error.message : "Errore nella ricerca anagrafica.";
        setApoNuovoAccessoMessage(apoState.nuovoAccessoError, "danger");
    } finally {
        setApoNuovoAccessoBusy(false);
        renderApoNuovoAccessoResults();
        updateApoNuovoAccessoSelectButton();
    }
}

function selectApoNuovoAccessoPaziente(patientId) {
    const normalizedId = String(patientId || "").trim();
    if (!normalizedId) {
        return;
    }

    let selected = null;
    for (let index = 0; index < apoState.nuovoAccessoResults.length; index += 1) {
        if (apoState.nuovoAccessoResults[index].id === normalizedId) {
            selected = apoState.nuovoAccessoResults[index];
            break;
        }
    }
    if (!selected) {
        return;
    }

    apoState.nuovoAccessoSelectedId = normalizedId;
    renderApoNuovoAccessoResults();
    setApoNuovoAccessoMessage("", "info");
    updateApoNuovoAccessoSelectButton();
}

function renderApoEmptyRow() {
    const tbody = document.getElementById("apo-accessi-body");
    if (!tbody) {
        return;
    }

    tbody.innerHTML = "";

    const row = document.createElement("tr");
    row.className = "riepilogo-empty-row";

    const cell = document.createElement("td");
    cell.colSpan = 7;
    cell.textContent = "Nessun accesso disponibile.";

    row.appendChild(cell);
    tbody.appendChild(row);
}

function renderApoAccessi() {
    const tbody = document.getElementById("apo-accessi-body");
    if (!tbody) {
        return;
    }

    tbody.innerHTML = "";
    updateApoAccessiTitle();

    if (!apoState.filteredAccessi.length) {
        renderApoEmptyRow();
        return;
    }

    apoState.filteredAccessi.forEach(function (item) {
        const row = document.createElement("tr");
        row.className = "riepilogo-movement-row";
        row.dataset.accessoId = item.id;
        row.classList.toggle("is-selected", apoState.selectedAccessoId === item.id);

        appendApoCell(row, item.consulenza);
        appendApoCell(row, item.sede);
        appendApoCell(row, formatApoAccessoGridDate(item));
        appendApoCell(row, item.codFiscale);
        appendApoCell(row, item.paziente);
        appendApoCell(row, item.dataNascita);
        appendApoCell(row, item.operatore);

        tbody.appendChild(row);
    });
}

function applyApoCurrentAccessiResult() {
    apoState.filteredAccessi = sortApoAccessi(apoState.accessi);

    if (!apoState.filteredAccessi.some(function (item) {
        return item.id === apoState.selectedAccessoId;
    })) {
        apoState.selectedAccessoId = "";
        setApoDetailOpen(false);
    }

    renderApoAccessi();
    renderApoDetail();
    renderApoSelectedPaziente();
    renderApoPazienteBody();
}

async function searchApoAccessi() {
    const params = getApoAccessiParams();
    const requestUrl = params.toString() ? apoAccessiUrl + "?" + params.toString() : apoAccessiUrl;

    setApoSearchBusy(true);
    setApoSearchMessage("", "info");

    try {
        const response = await fetch(requestUrl, {
            headers: {
                "Accept": "application/json"
            }
        });

        if (response.status === 401) {
            window.location.href = apoLoginUrl;
            return;
        }

        let payload = null;
        try {
            payload = await response.json();
        } catch (error) {
            payload = null;
        }

        if (!response.ok) {
            throw new Error(payload && payload.message ? payload.message : "Errore nel caricamento degli accessi.");
        }

        if (!payload || payload.esito !== "ok") {
            throw new Error(payload && payload.message ? payload.message : "Errore nel caricamento degli accessi.");
        }

        apoState.accessi = normalizeApoAccessi(payload.data && payload.data.accessi);
        applyApoCurrentAccessiResult();

        if (!apoState.accessi.length) {
            setApoSearchMessage("Nessun accesso disponibile.", "info");
        }
    } catch (error) {
        apoState.accessi = [];
        applyApoCurrentAccessiResult();
        setApoSearchMessage(error && error.message ? error.message : "Errore nel caricamento degli accessi.", "danger");
    } finally {
        setApoSearchBusy(false);
    }
}

function resetApoFilters() {
    const form = document.getElementById("apo-filter-form");
    const dateInput = document.getElementById("apo-filter-date");
    if (form) {
        form.reset();
    }

    const dateOperator = document.getElementById("apo-filter-date-operator");
    if (dateOperator) {
        dateOperator.value = "=";
    }
    if (dateInput) {
        dateInput.value = getApoCurrentDate();
    }
    renderApoServiceSelect();

    apoState.selectedAccessoId = "";
    setApoSearchMessage("", "info");
    setApoDetailOpen(false);
    void searchApoAccessi();
}

function selectApoAccesso(accessoId) {
    const normalizedId = String(accessoId || "").trim();

    if (apoState.selectedAccessoId === normalizedId && apoState.detailOpen) {
        apoState.selectedAccessoId = "";
        setApoDetailOpen(false);
        renderApoAccessi();
        renderApoSelectedPaziente();
        renderApoPazienteBody();
        return;
    }

    apoState.selectedAccessoId = normalizedId;
    setApoDetailOpen(true);
    renderApoAccessi();
    renderApoDetail();
    renderApoSelectedPaziente();
    renderApoPazienteBody();
}

function openApoPazienteFromAccesso(accessoId) {
    const normalizedId = String(accessoId || "").trim();
    if (!normalizedId) {
        return;
    }

    apoState.lastAccessoClickId = "";
    apoState.lastAccessoClickAt = 0;
    apoState.mainAccessoId = normalizedId;
    apoState.selectedAccessoId = normalizedId;
    setApoDetailOpen(false);
    renderApoAccessi();
    renderApoSelectedPaziente();
    setApoActiveSection("paziente");
    renderApoPazienteBody();
}

function openApoPazienteFromSelectedAccessoGrid() {
    const selectedAccesso = getApoAccessoGridById(apoState.selectedAccessoId);
    if (!selectedAccesso) {
        setApoSearchMessage("Devi selezionare un paziente.", "warning");
        return;
    }

    openApoPazienteFromAccesso(selectedAccesso.id);
}

function getApoCurrentAccessoGridIndex() {
    const currentId = String(apoState.mainAccessoId || apoState.selectedAccessoId || "").trim();
    if (!currentId) {
        return -1;
    }

    for (let index = 0; index < apoState.filteredAccessi.length; index += 1) {
        if (String(apoState.filteredAccessi[index].id || "").trim() === currentId) {
            return index;
        }
    }

    return -1;
}

function openAdjacentApoPaziente(direction) {
    const offset = direction === "previous" ? -1 : 1;
    const currentIndex = getApoCurrentAccessoGridIndex();
    const nextIndex = currentIndex + offset;
    const nextAccesso = nextIndex >= 0 && nextIndex < apoState.filteredAccessi.length
        ? apoState.filteredAccessi[nextIndex]
        : null;

    if (!nextAccesso) {
        setApoSearchMessage(direction === "previous"
            ? "Non ci sono pazienti precedenti."
            : "Non ci sono pazienti successivi.", "warning");
        return;
    }

    openApoPazienteFromAccesso(nextAccesso.id);
}

function handleApoAccessoRowClick(accessoId) {
    const normalizedId = String(accessoId || "").trim();
    const now = Date.now();
    const isDoubleClick = apoState.lastAccessoClickId === normalizedId
        && now - apoState.lastAccessoClickAt <= 500;

    if (!normalizedId) {
        return;
    }

    apoState.lastAccessoClickId = normalizedId;
    apoState.lastAccessoClickAt = now;

    if (isDoubleClick) {
        openApoPazienteFromAccesso(normalizedId);
        return;
    }

    selectApoAccesso(normalizedId);
}

function setApoActiveSection(sectionName) {
    const accessiLayout = document.getElementById("apo-accessi-layout");
    const pazienteLayout = document.getElementById("apo-paziente-layout");
    const statisticheLayout = document.getElementById("apo-statistiche-layout");
    const accessiButton = document.getElementById("nav-accessi-button");
    const pazienteButton = document.getElementById("nav-paziente-button");
    const statisticheButton = document.getElementById("nav-statistiche-button");
    const normalizedSection = sectionName === "paziente" ? "paziente" : sectionName === "statistiche" ? "statistiche" : "accessi";

    apoState.activeSection = normalizedSection;

    if (normalizedSection !== "accessi") {
        setApoDetailOpen(false);
    }
    if (normalizedSection === "paziente") {
        setApoPazienteSection("diagnosi");
        prepareApoPazienteStoricoAccessi();
    }

    if (accessiLayout) {
        accessiLayout.classList.toggle("is-hidden", normalizedSection !== "accessi");
    }
    if (pazienteLayout) {
        pazienteLayout.classList.toggle("is-hidden", normalizedSection !== "paziente");
    }
    if (statisticheLayout) {
        statisticheLayout.classList.toggle("is-hidden", normalizedSection !== "statistiche");
    }
    if (accessiButton) {
        accessiButton.classList.toggle("is-active", normalizedSection === "accessi");
        accessiButton.setAttribute("aria-current", normalizedSection === "accessi" ? "page" : "false");
    }
    if (pazienteButton) {
        pazienteButton.classList.toggle("is-active", normalizedSection === "paziente");
        pazienteButton.setAttribute("aria-current", normalizedSection === "paziente" ? "page" : "false");
    }
    if (statisticheButton) {
        statisticheButton.classList.toggle("is-active", normalizedSection === "statistiche");
        statisticheButton.setAttribute("aria-current", normalizedSection === "statistiche" ? "page" : "false");
    }

    renderApoSelectedPaziente();
    if (normalizedSection === "paziente") {
        renderApoPazienteBody();
    }
}

function randomBetween(min, max) {
    return min + Math.random() * (max - min);
}

function createApoSpaceParticles() {
    const layer = document.querySelector(".space-particles");
    if (!layer) {
        return;
    }

    layer.innerHTML = "";

    for (let index = 0; index < 42; index += 1) {
        const star = document.createElement("span");
        const isPlanet = index % 13 === 0;
        const size = isPlanet ? randomBetween(12, 28) : randomBetween(1.4, 4);

        star.className = "space-particle" + (isPlanet ? " is-planet" : "");
        star.style.setProperty("--x", randomBetween(3, 96).toFixed(2) + "%");
        star.style.setProperty("--y", randomBetween(4, 92).toFixed(2) + "%");
        star.style.setProperty("--size", size.toFixed(2) + "px");
        star.style.setProperty("--alpha", randomBetween(0.42, 0.9).toFixed(2));
        star.style.setProperty("--alpha-min", randomBetween(0.1, 0.28).toFixed(2));
        star.style.setProperty("--alpha-mid", randomBetween(0.22, 0.58).toFixed(2));
        star.style.setProperty("--dx", randomBetween(-18, 18).toFixed(2) + "px");
        star.style.setProperty("--dy", randomBetween(-14, 14).toFixed(2) + "px");
        star.style.setProperty("--dx-half", randomBetween(-10, 10).toFixed(2) + "px");
        star.style.setProperty("--dy-half", randomBetween(-8, 8).toFixed(2) + "px");
        star.style.setProperty("--dur", randomBetween(8, 16).toFixed(2) + "s");
        star.style.setProperty("--twinkle", randomBetween(2.2, 5.4).toFixed(2) + "s");
        star.style.setProperty("--delay", randomBetween(0, 4).toFixed(2) + "s");
        star.style.setProperty("--twinkle-delay", randomBetween(0, 3).toFixed(2) + "s");
        layer.appendChild(star);
    }
}

document.addEventListener("DOMContentLoaded", function () {
    normalizeApoLandingUrlInBrowser();
    createApoSpaceParticles();
    setApoActiveSection("accessi");
    setApoPazienteSection(apoState.activePazienteSection);
    renderApoSelectedPaziente();

    const filterForm = document.getElementById("apo-filter-form");
    const clearButton = document.getElementById("apo-clear-button");
    const sortField = document.getElementById("apo-sort-field");
    const sortDirection = document.getElementById("apo-sort-direction");
    const dateInput = document.getElementById("apo-filter-date");
    const serviceSelect = document.getElementById("apo-filter-sede");
    const accessiButton = document.getElementById("nav-accessi-button");
    const pazienteButton = document.getElementById("nav-paziente-button");
    const statisticheButton = document.getElementById("nav-statistiche-button");
    const pazienteMenu = document.getElementById("apo-paziente-menu");
    const pazienteHistoryTabs = document.getElementById("apo-paziente-history-main-tabs");
    const pazienteSchedaTabs = document.getElementById("apo-paziente-scheda-tabs");
    const storicoTabs = document.getElementById("apo-paziente-storico-tabs");
    const storicoBody = document.getElementById("apo-paziente-storico-body");
    const storicoConfirmModal = document.getElementById("apo-storico-confirm-modal");
    const storicoConfirmCancel = document.getElementById("apo-storico-confirm-cancel");
    const storicoConfirmOk = document.getElementById("apo-storico-confirm-ok");
    const deleteConfirmModal = document.getElementById("apo-delete-confirm-modal");
    const deleteConfirmCancel = document.getElementById("apo-delete-confirm-cancel");
    const deleteConfirmOk = document.getElementById("apo-delete-confirm-ok");
    const newAccessoButton = document.getElementById("apo-new-accesso-button");
    const newAccessoModal = document.getElementById("apo-new-accesso-modal");
    const newAccessoForm = document.getElementById("apo-new-accesso-form");
    const newAccessoClose = document.getElementById("apo-new-accesso-close");
    const newAccessoSelectPatient = document.getElementById("apo-new-accesso-select-patient");
    const newAccessoResultsBody = document.getElementById("apo-new-accesso-results-body");
    const newAccessoDetailModal = document.getElementById("apo-new-accesso-detail-modal");
    const newAccessoDetailForm = document.getElementById("apo-new-accesso-detail-form");
    const newAccessoDetailBack = document.getElementById("apo-new-accesso-detail-back");
    const newAccessoDetailClose = document.getElementById("apo-new-accesso-detail-close");
    const schedaPazienteModal = document.getElementById("apo-scheda-paziente-modal");
    const schedaPazienteOk = document.getElementById("apo-scheda-paziente-ok");
    const schedaPazienteBack = document.getElementById("apo-scheda-paziente-back");
    const schedaPazienteClose = document.getElementById("apo-scheda-paziente-close");
    const selectedSchedaButton = document.getElementById("apo-selected-scheda-button");
    const diarioModal = document.getElementById("apo-diario-modal");
    const diarioForm = document.getElementById("apo-diario-form");
    const diarioClose = document.getElementById("apo-diario-close");
    const terapiaModal = document.getElementById("apo-terapia-modal");
    const terapiaClose = document.getElementById("apo-terapia-close");
    const logoutButton = document.getElementById("logout-button");
    const detailCloseButton = document.getElementById("apo-detail-close-button");
    const detailConsensoButton = document.getElementById("apo-detail-consenso-button");
    const censimentoModal = document.getElementById("apo-censimento-modal");
    const censimentoForm = document.getElementById("apo-censimento-form");
    const censimentoClose = document.getElementById("apo-censimento-close");
    const censimentoCancel = document.getElementById("apo-censimento-cancel");
    const censimentoCalcolaCf = document.getElementById("apo-censimento-calcola-cf");
    const tbody = document.getElementById("apo-accessi-body");

    if (dateInput && !String(dateInput.value || "").trim()) {
        dateInput.value = getApoCurrentDate();
    }
    renderApoPazienteHistoryTabs();
    renderApoSchedaPazienteHistorySection();

    if (filterForm) {
        filterForm.addEventListener("submit", function (event) {
            event.preventDefault();
            void searchApoAccessi();
        });
    }

    if (clearButton) {
        clearButton.addEventListener("click", function () {
            resetApoFilters();
        });
    }

    if (sortField) {
        sortField.addEventListener("change", function () {
            applyApoCurrentAccessiResult();
        });
    }

    if (sortDirection) {
        sortDirection.addEventListener("change", function () {
            applyApoCurrentAccessiResult();
        });
    }

    if (serviceSelect) {
        serviceSelect.addEventListener("change", function () {
            void switchApoService(serviceSelect.value);
        });
    }

    if (newAccessoButton) {
        newAccessoButton.addEventListener("click", function () {
            openApoNuovoAccessoModal();
        });
    }

    if (newAccessoForm) {
        newAccessoForm.addEventListener("submit", function (event) {
            event.preventDefault();
            void searchApoNuovoAccessoPazienti();
        });
    }

    if (newAccessoSelectPatient) {
        newAccessoSelectPatient.addEventListener("click", function () {
            openApoNuovoAccessoDetailModal();
        });
    }

    if (newAccessoClose) {
        newAccessoClose.addEventListener("click", function () {
            closeApoNuovoAccessoModal(true);
        });

        newAccessoClose.addEventListener("keydown", function (event) {
            if (event.key !== "Enter" && event.key !== " ") {
                return;
            }

            event.preventDefault();
            closeApoNuovoAccessoModal(true);
        });
    }

    if (censimentoForm) {
        censimentoForm.addEventListener("submit", function (event) {
            handleApoCensimentoSubmit(event);
        });
    }

    apoCensimentoLookupFields.forEach(function (config) {
        const input = document.getElementById(config.inputId);
        if (!input) {
            return;
        }

        input.addEventListener("focus", function () {
            if (config.listType === "asl") {
                void loadApoCensimentoAsl();
            } else {
                void loadApoCensimentoComuni();
            }
        });
        input.addEventListener("input", function () {
            syncApoCensimentoLookupField(config);
        });
        input.addEventListener("change", function () {
            syncApoCensimentoLookupField(config);
        });
    });

    if (censimentoCalcolaCf) {
        censimentoCalcolaCf.addEventListener("click", function () {
            void calculateApoCensimentoCodiceFiscale();
        });
    }

    if (censimentoCancel) {
        censimentoCancel.addEventListener("click", function () {
            closeApoCensimentoPazienteModal(true);
        });
    }

    if (censimentoClose) {
        censimentoClose.addEventListener("click", function () {
            closeApoCensimentoPazienteModal(true);
        });

        censimentoClose.addEventListener("keydown", function (event) {
            if (event.key !== "Enter" && event.key !== " ") {
                return;
            }

            event.preventDefault();
            closeApoCensimentoPazienteModal(true);
        });
    }

    if (censimentoModal) {
        censimentoModal.addEventListener("keydown", function (event) {
            if (event.key === "Escape") {
                event.preventDefault();
                closeApoCensimentoPazienteModal(true);
            }
        });
    }

    if (newAccessoModal) {
        newAccessoModal.addEventListener("keydown", function (event) {
            if (event.key === "Escape") {
                event.preventDefault();
                closeApoNuovoAccessoModal(true);
            }
        });
    }

    if (newAccessoDetailForm) {
        newAccessoDetailForm.addEventListener("submit", function (event) {
            handleApoNuovoAccessoDetailSubmit(event);
        });
    }

    if (newAccessoDetailBack) {
        newAccessoDetailBack.addEventListener("click", function () {
            backToApoNuovoAccessoSearchModal();
        });
    }

    if (newAccessoDetailClose) {
        newAccessoDetailClose.addEventListener("click", function () {
            closeApoNuovoAccessoDetailModal(true);
        });

        newAccessoDetailClose.addEventListener("keydown", function (event) {
            if (event.key !== "Enter" && event.key !== " ") {
                return;
            }

            event.preventDefault();
            closeApoNuovoAccessoDetailModal(true);
        });
    }

    if (newAccessoDetailModal) {
        newAccessoDetailModal.addEventListener("click", function (event) {
            const typeButton = event.target.closest("[data-new-accesso-type]");
            if (typeButton) {
                toggleApoNuovoAccessoTipo(typeButton.getAttribute("data-new-accesso-type"));
                return;
            }
            const patologiaButton = event.target.closest("[data-new-accesso-patologia]");
            if (patologiaButton) {
                toggleApoNuovoAccessoPatologia(patologiaButton.getAttribute("data-new-accesso-patologia"));
                return;
            }
        });

        newAccessoDetailModal.addEventListener("keydown", function (event) {
            if (event.key === "Escape") {
                event.preventDefault();
                closeApoNuovoAccessoDetailModal(true);
            }
        });
    }

    if (schedaPazienteOk) {
        schedaPazienteOk.addEventListener("click", function () {
            void saveApoSchedaPaziente();
        });
    }

    if (schedaPazienteBack) {
        schedaPazienteBack.addEventListener("click", backToApoSchedaPazientePreviousModal);
    }

    if (schedaPazienteClose) {
        schedaPazienteClose.addEventListener("click", function () {
            apoState.schedaPazientePendingAccessoPayload = null;
            apoState.schedaPazientePendingPatient = null;
            closeApoSchedaPazienteModal(true);
        });

        schedaPazienteClose.addEventListener("keydown", function (event) {
            if (event.key !== "Enter" && event.key !== " ") {
                return;
            }

            event.preventDefault();
            apoState.schedaPazientePendingAccessoPayload = null;
            apoState.schedaPazientePendingPatient = null;
            closeApoSchedaPazienteModal(true);
        });
    }

    if (selectedSchedaButton) {
        selectedSchedaButton.addEventListener("click", function () {
            openApoSelectedSchedaPazienteModal();
        });
    }

    if (schedaPazienteModal) {
        schedaPazienteModal.addEventListener("keydown", function (event) {
            if (event.key === "Escape") {
                event.preventDefault();
            }
        });
    }

    if (diarioForm) {
        diarioForm.addEventListener("submit", function (event) {
            event.preventDefault();
            void saveApoSchedaPazienteDiario();
        });
    }

    if (diarioClose) {
        diarioClose.addEventListener("click", function () {
            closeApoSchedaPazienteDiarioModal(true);
        });

        diarioClose.addEventListener("keydown", function (event) {
            if (event.key !== "Enter" && event.key !== " ") {
                return;
            }

            event.preventDefault();
            closeApoSchedaPazienteDiarioModal(true);
        });
    }

    if (diarioModal) {
        diarioModal.addEventListener("keydown", function (event) {
            if (event.key === "Escape") {
                event.preventDefault();
                closeApoSchedaPazienteDiarioModal(true);
            }
        });
    }

    if (terapiaClose) {
        terapiaClose.addEventListener("click", function () {
            closeApoSchedaPazienteTerapiaModal(true);
        });

        terapiaClose.addEventListener("keydown", function (event) {
            if (event.key !== "Enter" && event.key !== " ") {
                return;
            }

            event.preventDefault();
            closeApoSchedaPazienteTerapiaModal(true);
        });
    }

    if (terapiaModal) {
        terapiaModal.addEventListener("keydown", function (event) {
            if (event.key === "Escape") {
                event.preventDefault();
                closeApoSchedaPazienteTerapiaModal(true);
            }
        });
    }

    if (newAccessoResultsBody) {
        newAccessoResultsBody.addEventListener("click", function (event) {
            const row = event.target.closest("tr[data-nuovo-accesso-patient-id]");
            if (!row) {
                return;
            }
            selectApoNuovoAccessoPaziente(row.getAttribute("data-nuovo-accesso-patient-id"));
        });

        newAccessoResultsBody.addEventListener("keydown", function (event) {
            if (event.key !== "Enter" && event.key !== " ") {
                return;
            }

            const row = event.target.closest("tr[data-nuovo-accesso-patient-id]");
            if (!row) {
                return;
            }

            event.preventDefault();
            selectApoNuovoAccessoPaziente(row.getAttribute("data-nuovo-accesso-patient-id"));
        });
    }

    if (accessiButton) {
        accessiButton.addEventListener("click", function () {
            setApoActiveSection("accessi");
        });
    }

    if (pazienteButton) {
        pazienteButton.addEventListener("click", function () {
            openApoPazienteFromSelectedAccessoGrid();
        });
    }

    if (statisticheButton) {
        statisticheButton.addEventListener("click", function () {
            setApoActiveSection("statistiche");
        });
    }

    if (pazienteMenu) {
        pazienteMenu.addEventListener("click", function (event) {
            const navigationButton = event.target.closest("button[data-paziente-navigation]");
            if (navigationButton) {
                openAdjacentApoPaziente(navigationButton.getAttribute("data-paziente-navigation"));
                return;
            }

            const button = event.target.closest("button[data-paziente-section]");
            if (!button) {
                return;
            }
            const sectionId = button.getAttribute("data-paziente-section");
            if (sectionId === "elimina-consulenza") {
                showApoDeleteConsulenzaConfirmModal();
                return;
            }
            setApoPazienteSection(sectionId);
            if (sectionId === "ricetta-specialistica") {
                openApoRicettaWindow("dematerializzata");
            }
            if (sectionId === "ricetta-farmaci") {
                openApoRicettaWindow("farmaci");
            }
            if (sectionId === "certificato-malattia") {
                openApoCertificatoMalattiaWindow();
            }
            if (sectionId === "repository-referti") {
                openApoRepositoryRefertiWindow();
            }
            if (sectionId === "cruscotto-consenso") {
                openApoConsensoDseWindow();
            }
        });
    }

    if (pazienteHistoryTabs) {
        pazienteHistoryTabs.addEventListener("click", function (event) {
            const button = event.target.closest("button[data-paziente-history-tab]");
            if (!button) {
                return;
            }
            setApoPazienteHistoryTab(button.getAttribute("data-paziente-history-tab"));
        });
    }

    if (pazienteSchedaTabs) {
        pazienteSchedaTabs.addEventListener("click", function (event) {
            const button = event.target.closest("button[data-scheda-paziente-history-section]");
            if (!button) {
                return;
            }
            setApoSchedaPazienteHistorySection(button.getAttribute("data-scheda-paziente-history-section"));
        });
    }

    if (storicoTabs) {
        storicoTabs.addEventListener("click", function (event) {
            const button = event.target.closest("button[data-storico-tab]");
            if (!button) {
                return;
            }
            setApoStoricoTab(button.getAttribute("data-storico-tab"));
        });
    }

    if (storicoBody) {
        storicoBody.addEventListener("click", function (event) {
            const row = event.target.closest("[data-storico-accesso-id]");
            if (!row) {
                return;
            }
            selectApoStoricoAccesso(row.getAttribute("data-storico-accesso-id"));
        });

        storicoBody.addEventListener("keydown", function (event) {
            if (event.key !== "Enter" && event.key !== " ") {
                return;
            }

            const row = event.target.closest("[data-storico-accesso-id]");
            if (!row) {
                return;
            }

            event.preventDefault();
            selectApoStoricoAccesso(row.getAttribute("data-storico-accesso-id"));
        });
    }

    if (storicoConfirmCancel) {
        storicoConfirmCancel.addEventListener("click", function () {
            hideApoStoricoAccessoConfirmModal(true);
        });
    }

    if (storicoConfirmOk) {
        storicoConfirmOk.addEventListener("click", function () {
            confirmApoStoricoAccessoChange();
        });
    }

    if (storicoConfirmModal) {
        storicoConfirmModal.addEventListener("keydown", function (event) {
            if (event.key === "Escape") {
                event.preventDefault();
                hideApoStoricoAccessoConfirmModal(true);
            }
        });
    }

    if (deleteConfirmCancel) {
        deleteConfirmCancel.addEventListener("click", function () {
            if (!apoState.deleteConsulenzaLoading) {
                hideApoDeleteConsulenzaConfirmModal(true);
            }
        });
    }

    if (deleteConfirmOk) {
        deleteConfirmOk.addEventListener("click", function () {
            void confirmApoDeleteConsulenza();
        });
    }

    if (deleteConfirmModal) {
        deleteConfirmModal.addEventListener("keydown", function (event) {
            if (event.key === "Escape" && !apoState.deleteConsulenzaLoading) {
                event.preventDefault();
                hideApoDeleteConsulenzaConfirmModal(true);
            }
        });
    }

    if (logoutButton) {
        logoutButton.addEventListener("click", function () {
            void logoutFromApo();
        });
    }

    if (detailCloseButton) {
        detailCloseButton.addEventListener("click", function () {
            apoState.selectedAccessoId = "";
            setApoDetailOpen(false);
            renderApoAccessi();
            renderApoSelectedPaziente();
            renderApoPazienteBody();
        });
    }

    if (detailConsensoButton) {
        detailConsensoButton.addEventListener("click", function () {
            openApoConsensoDseWindow();
        });
    }

    if (tbody) {
        tbody.addEventListener("click", function (event) {
            const row = event.target.closest("tr[data-accesso-id]");
            if (!row) {
                return;
            }
            handleApoAccessoRowClick(row.dataset.accessoId);
        });
    }

    void searchApoAccessi();
    void loadApoSessionState();
});
