document.addEventListener("DOMContentLoaded", async () => {
  try {
    const [tab] = await chrome.tabs.query({
      active: true,
      currentWindow: true,
    });

    const url = new URL(tab.url);
    const playerId = url.searchParams.get("id");
    const isBgaHistoryPage =
      url.hostname === "boardgamearena.com" &&
      url.pathname === "/player" &&
      playerId &&
      url.searchParams.get("section") === "lastresults";

    if (!isBgaHistoryPage) {
      showDisconnected();
      return;
    }

    const cookie = await chrome.cookies.get({
      url: "https://boardgamearena.com",
      name: "PHPSESSID",
    });

    if (!cookie) {
      showDisconnected();
      return;
    }

    const results = await chrome.scripting.executeScript({
      target: { tabId: tab.id },
      func: () => {
        const el = document.getElementById("real_player_name");
        return el ? el.textContent.trim() : null;
      },
    });
    const playerName = results?.[0]?.result;

    if (!playerName) {
      showDisconnected();
      return;
    }

    await chrome.storage.local.set({ playerId, playerName });
    showConnected(playerName);
  } catch (err) {
    console.warn("AlteredHistory: session check failed", err);
    showDisconnected();
  }
});

function showConnected(name) {
  document.getElementById("status-loading").classList.add("hidden");
  document.getElementById("status-disconnected").classList.add("hidden");
  document.getElementById("status-connected").classList.remove("hidden");
  document.getElementById("player-name").textContent = name;
  initImportUI();
}

function showDisconnected() {
  document.getElementById("status-loading").classList.add("hidden");
  document.getElementById("status-connected").classList.add("hidden");
  document.getElementById("status-disconnected").classList.remove("hidden");
}

async function initImportUI() {
  const { lastExportDate, importState } = await chrome.storage.local.get([
    "lastExportDate",
    "importState",
  ]);

  const dateInput = document.getElementById("cutoff-date");
  if (lastExportDate) {
    dateInput.value = lastExportDate.slice(0, 16);
  } else {
    const sevenDaysAgo = new Date(Date.now() - 7 * 24 * 60 * 60 * 1000);
    dateInput.value = toDatetimeLocalValue(sevenDaysAgo);
  }

  renderImportState(importState);

  chrome.storage.onChanged.addListener((changes) => {
    if (changes.importState) {
      renderImportState(changes.importState.newValue);
    }
  });

  document.getElementById("btn-import").addEventListener("click", onImportClick);
}

function onImportClick() {
  const cutoffDate = document.getElementById("cutoff-date").value;
  if (!cutoffDate) return;

  document.getElementById("btn-import").disabled = true;
  chrome.runtime.sendMessage({
    type: "START_IMPORT",
    cutoffDate: new Date(cutoffDate).toISOString(),
  });
}

function renderImportState(state) {
  const btn = document.getElementById("btn-import");
  const controls = document.getElementById("import-controls");
  const progress = document.getElementById("import-progress");
  const summary = document.getElementById("import-summary");
  const error = document.getElementById("import-error");

  controls.classList.remove("hidden");
  progress.classList.add("hidden");
  summary.classList.add("hidden");
  error.classList.add("hidden");
  btn.disabled = false;

  if (!state || state.status === "idle") return;

  if (state.status === "collecting") {
    btn.disabled = true;
    progress.classList.remove("hidden");
    document.getElementById("progress-text").textContent =
      `Collecting games... (page ${state.collectingPage || 1})`;
    document.getElementById("progress-fill").style.width = "0%";
  }

  if (state.status === "sending") {
    btn.disabled = true;
    progress.classList.remove("hidden");
    const pct = state.total > 0 ? Math.round((state.current / state.total) * 100) : 0;
    document.getElementById("progress-fill").style.width = `${pct}%`;
    document.getElementById("progress-text").textContent =
      `${state.current} / ${state.total} (${pct}%)`;
  }

  if (state.status === "done") {
    summary.classList.remove("hidden");
    document.getElementById("summary-imported").textContent =
      `Imported: ${state.imported}`;
    document.getElementById("summary-already-exists").textContent =
      `Already existing: ${state.alreadyExists}`;
    document.getElementById("summary-skipped").textContent =
      `Skipped: ${state.skipped}`;
    document.getElementById("summary-failed").textContent =
      `Failed: ${state.failed}`;
  }

  if (state.status === "error") {
    error.classList.remove("hidden");
    document.getElementById("error-message").textContent =
      state.errorMessage || "Unknown error";
  }
}

function toDatetimeLocalValue(date) {
  const pad = (n) => String(n).padStart(2, "0");
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
}
