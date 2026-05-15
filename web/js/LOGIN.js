function getLoginContextPath() {
    const path = window.location.pathname || "/";
    const routeSuffixes = ["/LOGIN.html", "/Login", "/APO.html"];
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

function buildLoginUrl(path) {
    const normalizedPath = String(path || "").replace(/^\/+/, "");
    return getLoginContextPath() + "/" + normalizedPath;
}

function getLoginLandingUrl() {
    const contextPath = getLoginContextPath();
    return contextPath || "/";
}

const loginServicesUrl = buildLoginUrl("api/auth/services");
const loginSubmitUrl = buildLoginUrl("api/auth/login");
const defaultLandingUrl = getLoginLandingUrl();

let loginServicesDebounceId = null;
let loginServicesAbortController = null;
let lastLoadedLoginUsername = "";

function normalizeLoginValue(value) {
    return String(value || "").trim();
}

function showLoginError(message) {
    const errorBox = document.getElementById("login-error");
    if (!errorBox) {
        return;
    }

    errorBox.textContent = message;
    errorBox.classList.remove("d-none");
}

function clearLoginError() {
    const errorBox = document.getElementById("login-error");
    if (!errorBox) {
        return;
    }

    errorBox.textContent = "";
    errorBox.classList.add("d-none");
}

function resetLoginServiceSelect(placeholder, keepEnabled) {
    const select = document.getElementById("login-service");
    if (!select) {
        return;
    }

    select.innerHTML = "";
    const option = document.createElement("option");
    option.value = "";
    option.textContent = typeof placeholder === "string" ? placeholder : "";
    select.appendChild(option);
    select.disabled = keepEnabled !== true;

    if (keepEnabled === true) {
        select.value = "";
    }
}

function setLoginBusy(form, isBusy) {
    const busy = isBusy === true;
    const fields = form.querySelectorAll("input, select, button");
    const submitButton = form.querySelector("button[type='submit']");
    const loader = document.getElementById("login-loader");

    fields.forEach(function (field) {
        if (busy) {
            field.dataset.loginWasDisabled = field.disabled ? "true" : "false";
            field.disabled = true;
            return;
        }

        field.disabled = field.dataset.loginWasDisabled === "true";
        delete field.dataset.loginWasDisabled;
    });

    form.classList.toggle("is-busy", busy);
    form.setAttribute("aria-busy", busy ? "true" : "false");

    if (submitButton) {
        submitButton.textContent = busy ? "Verifica..." : "Entra";
    }

    if (loader) {
        loader.classList.toggle("is-visible", busy);
        loader.setAttribute("aria-hidden", busy ? "false" : "true");
    }
}

function renderLoginServices(services, username) {
    const select = document.getElementById("login-service");
    const normalizedUsername = normalizeLoginValue(username);
    const currentValue = lastLoadedLoginUsername === normalizedUsername
            ? normalizeLoginValue(select.value)
            : "";
    let hasCurrentValue = false;

    select.innerHTML = "";

    const placeholder = document.createElement("option");
    placeholder.value = "";
    placeholder.textContent = "Seleziona servizio";
    select.appendChild(placeholder);

    (Array.isArray(services) ? services : []).forEach(function (service) {
        const code = normalizeLoginValue(service && service.codServizio);
        const description = normalizeLoginValue(service && service.descrizione);
        const label = description || code;

        if (!code || !label) {
            return;
        }

        const option = document.createElement("option");
        option.value = code;
        option.textContent = label;
        if (code === currentValue) {
            hasCurrentValue = true;
        }
        select.appendChild(option);
    });

    lastLoadedLoginUsername = normalizedUsername;

    if (select.options.length <= 1) {
        resetLoginServiceSelect("Nessun servizio disponibile", false);
        return;
    }

    select.disabled = false;
    select.value = hasCurrentValue ? currentValue : "";
}

async function loadLoginServicesForUser(username) {
    const normalizedUsername = normalizeLoginValue(username);
    const userInput = document.getElementById("login-user");
    const currentController = new AbortController();

    if (loginServicesAbortController) {
        loginServicesAbortController.abort();
    }

    if (!normalizedUsername) {
        lastLoadedLoginUsername = "";
        resetLoginServiceSelect("", false);
        return;
    }

    resetLoginServiceSelect("Caricamento servizi...", false);
    loginServicesAbortController = currentController;

    try {
        const response = await fetch(
                loginServicesUrl + "?username=" + encodeURIComponent(normalizedUsername),
                {
                    headers: {
                        "Accept": "application/json"
                    },
                    signal: currentController.signal
                }
        );

        if (!response.ok) {
            throw new Error("Servizi non disponibili");
        }

        const payload = await response.json();
        if (!userInput || normalizeLoginValue(userInput.value) !== normalizedUsername) {
            return;
        }

        clearLoginError();
        renderLoginServices(payload && payload.data ? payload.data.services : [], normalizedUsername);
    } catch (error) {
        if (error && error.name === "AbortError") {
            return;
        }

        if (!userInput || normalizeLoginValue(userInput.value) !== normalizedUsername) {
            return;
        }

        resetLoginServiceSelect("Errore caricamento servizi", false);
        showLoginError("Impossibile caricare i servizi per l'utente indicato.");
    } finally {
        if (loginServicesAbortController === currentController) {
            loginServicesAbortController = null;
        }
    }
}

function scheduleLoginServicesLoad() {
    const userInput = document.getElementById("login-user");
    const username = userInput ? normalizeLoginValue(userInput.value) : "";

    if (loginServicesDebounceId) {
        window.clearTimeout(loginServicesDebounceId);
    }

    loginServicesDebounceId = window.setTimeout(function () {
        void loadLoginServicesForUser(username);
    }, 260);
}

async function submitLogin(form) {
    const username = normalizeLoginValue(document.getElementById("login-user").value);
    const password = document.getElementById("login-password").value || "";
    const serviceCode = normalizeLoginValue(document.getElementById("login-service").value);
    const payload = new URLSearchParams();

    clearLoginError();

    if (!username || !password || !serviceCode) {
        showLoginError("Inserisci utente, password e servizio.");
        return;
    }

    payload.set("username", username);
    payload.set("password", password);
    payload.set("serviceCode", serviceCode);

    setLoginBusy(form, true);

    try {
        const response = await fetch(loginSubmitUrl, {
            method: "POST",
            headers: {
                "Accept": "application/json",
                "Content-Type": "application/x-www-form-urlencoded; charset=UTF-8"
            },
            body: payload.toString()
        });

        const result = await response.json();
        if (response.ok && result && result.esito === "ok") {
            window.location.href = result.data && result.data.redirectUrl
                    ? result.data.redirectUrl
                    : defaultLandingUrl;
            return;
        }

        showLoginError(result && result.message ? result.message : "Credenziali non valide.");
    } catch (error) {
        showLoginError("Errore di autenticazione. Riprova.");
    } finally {
        setLoginBusy(form, false);
    }
}

document.addEventListener("DOMContentLoaded", function () {
    const loginForm = document.querySelector(".login-form");
    const userInput = document.getElementById("login-user");

    resetLoginServiceSelect("", false);

    if (userInput) {
        userInput.addEventListener("input", scheduleLoginServicesLoad);
        userInput.addEventListener("change", scheduleLoginServicesLoad);
    }

    window.setTimeout(function () {
        scheduleLoginServicesLoad();
    }, 120);

    if (loginForm) {
        loginForm.addEventListener("submit", function (event) {
            event.preventDefault();
            void submitLogin(loginForm);
        });
    }
});
