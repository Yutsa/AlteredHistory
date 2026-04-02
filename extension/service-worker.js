console.log("AlteredHistory service worker started");

const BACKEND_URL = "http://localhost:8080";
const BGA_GAME_ID = "1909";
const RETRY_DELAYS = [1000, 2000, 4000];

class SessionExpiredError extends Error {
  constructor() {
    super("Session expired");
    this.name = "SessionExpiredError";
  }
}

function delay(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

function randomDelay() {
  return delay(600 + Math.random() * 600);
}

async function updateImportState(patch) {
  const { importState = {} } = await chrome.storage.local.get("importState");
  const updated = { ...importState, ...patch };
  await chrome.storage.local.set({ importState: updated });
  return updated;
}

async function getBgaHeaders(referer) {
  const { requestToken } = await chrome.storage.local.get("requestToken");
  return {
    "X-Request-Token": requestToken,
    "X-Requested-With": "XMLHttpRequest",
    Referer: referer,
  };
}

async function fetchWithRetry(url, options) {
  for (let attempt = 0; attempt <= RETRY_DELAYS.length; attempt++) {
    try {
      const response = await fetch(url, options);
      if (response.status === 403) throw new SessionExpiredError();
      if (response.ok) return response;
      throw new Error(`HTTP ${response.status}`);
    } catch (err) {
      if (err instanceof SessionExpiredError) throw err;
      if (attempt >= RETRY_DELAYS.length) throw err;
      await delay(RETRY_DELAYS[attempt]);
    }
  }
}

async function collectTableIds(playerId, cutoffTimestamp) {
  const tableIds = [];
  let page = 1;

  while (true) {
    await randomDelay();

    const params = new URLSearchParams({
      player: playerId,
      opponent_id: "0",
      game_id: BGA_GAME_ID,
      finished: "0",
      page: String(page),
      updateStats: page === 1 ? "1" : "0",
      "dojo.preventCache": String(Date.now()),
    });

    const url = `https://boardgamearena.com/gamestats/gamestats/getGames.html?${params}`;
    const headers = await getBgaHeaders(
      `https://boardgamearena.com/gamestats?player=${playerId}`,
    );
    const response = await fetchWithRetry(url, {
      headers,
      credentials: "include",
    });
    const json = await response.json();
    const tables = json?.data?.tables ?? [];

    if (tables.length === 0) break;

    let reachedCutoff = false;
    for (const table of tables) {
      if (parseInt(table.end, 10) < cutoffTimestamp) {
        reachedCutoff = true;
        break;
      }
      tableIds.push(table.table_id);
    }

    await updateImportState({ collectingPage: page });

    if (reachedCutoff) break;
    page++;
  }

  return tableIds;
}

async function sendReplays(tableIds) {
  const counters = { imported: 0, alreadyExists: 0, skipped: 0, failed: 0 };

  for (let i = 0; i < tableIds.length; i++) {
    const tableId = tableIds[i];

    try {
      await randomDelay();

      const params = new URLSearchParams({
        table: tableId,
        translated: "true",
        "dojo.preventCache": String(Date.now()),
      });
      const logsUrl = `https://boardgamearena.com/archive/archive/logs.html?${params}`;
      const headers = await getBgaHeaders(
        `https://boardgamearena.com/gamereview?table=${tableId}`,
      );
      const logsResponse = await fetchWithRetry(logsUrl, {
        headers,
        credentials: "include",
      });
      const replayBody = await logsResponse.text();
      const replayJson = JSON.parse(replayBody);

      if (replayJson.status === "0" || replayJson.status === 0) {
        counters.skipped++;
        await updateImportState({ current: i + 1, ...counters });
        continue;
      }

      const backendResponse = await fetch(`${BACKEND_URL}/api/replays`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: replayBody,
      });
      const backendJson = await backendResponse.json();

      if (backendResponse.status === 201) {
        counters.imported++;
      } else if (backendJson.status === "already_exists") {
        counters.alreadyExists++;
      } else if (backendJson.status === "skipped") {
        counters.skipped++;
      } else {
        counters.failed++;
      }
    } catch (err) {
      if (err instanceof SessionExpiredError) throw err;
      counters.failed++;
    }

    await updateImportState({ current: i + 1, ...counters });
  }

  return counters;
}

async function runImport(cutoffDate) {
  const { importState } = await chrome.storage.local.get("importState");
  if (
    importState?.status === "collecting" ||
    importState?.status === "sending"
  ) {
    return;
  }

  const { requestToken } = await chrome.storage.local.get("requestToken");
  if (!requestToken) {
    await updateImportState({
      status: "error",
      errorMessage:
        "X-Request-Token missing. Please reload a BGA page and try again.",
    });
    return;
  }

  const cutoffTimestamp = new Date(cutoffDate).getTime() / 1000;
  await chrome.storage.local.set({ lastExportDate: new Date().toISOString() });
  await updateImportState({
    status: "collecting",
    collectingPage: 1,
    total: 0,
    current: 0,
    imported: 0,
    alreadyExists: 0,
    skipped: 0,
    failed: 0,
    errorMessage: null,
  });

  try {
    const { playerId } = await chrome.storage.local.get("playerId");
    const tableIds = await collectTableIds(playerId, cutoffTimestamp);

    await updateImportState({
      status: "sending",
      total: tableIds.length,
      current: 0,
    });

    await sendReplays(tableIds);
    await updateImportState({ status: "done" });
  } catch (err) {
    if (err instanceof SessionExpiredError) {
      await chrome.storage.local.remove([
        "requestToken",
        "playerId",
        "playerName",
      ]);
      await updateImportState({
        status: "error",
        errorMessage: "Session expirée, reconnectez-vous sur BGA",
      });
    } else {
      await updateImportState({
        status: "error",
        errorMessage: err.message,
      });
    }
  }
}

chrome.runtime.onMessage.addListener((message, sender) => {
  if (message.type === "BGA_PAGE_LOADED" && sender.tab?.id) {
    chrome.scripting.executeScript({
      target: { tabId: sender.tab.id },
      world: "MAIN",
      func: () => {
        const token =
          typeof bgaConfig !== "undefined" ? bgaConfig.requestToken : null;
        window.postMessage({ type: "ALTERED_HISTORY_TOKEN", token }, "*");
      },
    });
  }

  if (message.type === "TOKEN_EXTRACTED") {
    if (message.token) {
      chrome.storage.local.set({ requestToken: message.token });
      console.log("AlteredHistory: X-Request-Token captured");
    } else {
      console.warn(
        "AlteredHistory: X-Request-Token not found (user may not be logged in)",
      );
    }
  }

  if (message.type === "START_IMPORT") {
    runImport(message.cutoffDate);
  }
});
